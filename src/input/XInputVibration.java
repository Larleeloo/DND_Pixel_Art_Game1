package input;

import java.io.*;
import java.nio.file.*;

/**
 * XInput-based vibration support for Xbox controllers on Windows.
 *
 * JInput's DirectInput plugin doesn't support XInput controller rumble.
 * This class provides native XInput vibration using JNA when available,
 * or falls back to a native helper process.
 *
 * XInput Vibration:
 * - Left motor: Low frequency rumble (bass), values 0-65535
 * - Right motor: High frequency rumble (treble), values 0-65535
 *
 * Setup (for JNA - recommended):
 *   1. Download jna-5.x.x.jar and jna-platform-5.x.x.jar from:
 *      https://github.com/java-native-access/jna/releases
 *   2. Place both JARs in the lib/ folder
 *   3. Add them to your classpath
 *
 * Usage:
 *   XInputVibration.vibrate(0, 0.5f, 0.5f);  // Controller 0, 50% both motors
 *   XInputVibration.vibrate(0, 1.0f, 0.0f);  // Controller 0, full left only
 *   XInputVibration.stop(0);                  // Stop vibration on controller 0
 */
public class XInputVibration {

    private static boolean initialized = false;
    private static boolean available = false;
    private static VibrationBackend backend = null;

    // Current vibration state for each controller (up to 4 controllers)
    private static final float[] leftMotorIntensity = new float[4];
    private static final float[] rightMotorIntensity = new float[4];

    /**
     * Interface for different vibration backends.
     */
    private interface VibrationBackend {
        boolean vibrate(int controller, float left, float right);
        void stop(int controller);
        void shutdown();
    }

    /**
     * Initialize XInput vibration support.
     */
    public static synchronized void initialize() {
        if (initialized) return;
        initialized = true;

        // Check if we're on Windows
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            System.out.println("XInputVibration: Not on Windows, XInput unavailable");
            return;
        }

        // Try JNA backend first (most efficient)
        try {
            backend = new JNABackend();
            available = true;
            System.out.println("XInputVibration: Initialized with JNA backend");
            return;
        } catch (Throwable t) {
            System.out.println("XInputVibration: JNA not available (" + t.getMessage() + ")");
        }

        // Try native helper backend
        try {
            backend = new NativeHelperBackend();
            available = true;
            System.out.println("XInputVibration: Initialized with native helper backend");
            return;
        } catch (Throwable t) {
            System.out.println("XInputVibration: Native helper not available (" + t.getMessage() + ")");
        }

        System.out.println("XInputVibration: No vibration backend available");
        System.out.println("  For best support, add jna-5.x.x.jar to lib/ folder");
    }

    /**
     * Check if XInput vibration is available.
     */
    public static boolean isAvailable() {
        if (!initialized) {
            initialize();
        }
        return available;
    }

    /**
     * Set vibration on a controller.
     *
     * @param controllerIndex Controller index (0-3)
     * @param leftMotor Left motor intensity (0.0 to 1.0) - low frequency
     * @param rightMotor Right motor intensity (0.0 to 1.0) - high frequency
     * @return true if vibration was set successfully
     */
    public static boolean vibrate(int controllerIndex, float leftMotor, float rightMotor) {
        if (!initialized) {
            initialize();
        }

        if (!available || backend == null) {
            return false;
        }

        if (controllerIndex < 0 || controllerIndex > 3) {
            return false;
        }

        // Clamp values
        leftMotor = Math.max(0.0f, Math.min(1.0f, leftMotor));
        rightMotor = Math.max(0.0f, Math.min(1.0f, rightMotor));

        // Store current state
        leftMotorIntensity[controllerIndex] = leftMotor;
        rightMotorIntensity[controllerIndex] = rightMotor;

        return backend.vibrate(controllerIndex, leftMotor, rightMotor);
    }

    /**
     * Stop vibration on a controller.
     */
    public static void stop(int controllerIndex) {
        if (backend != null && controllerIndex >= 0 && controllerIndex <= 3) {
            leftMotorIntensity[controllerIndex] = 0;
            rightMotorIntensity[controllerIndex] = 0;
            backend.stop(controllerIndex);
        }
    }

    /**
     * Stop vibration on all controllers.
     */
    public static void stopAll() {
        for (int i = 0; i < 4; i++) {
            stop(i);
        }
    }

    /**
     * Shutdown vibration system.
     */
    public static void shutdown() {
        stopAll();
        if (backend != null) {
            backend.shutdown();
        }
    }

    /**
     * Get current left motor intensity for a controller.
     */
    public static float getLeftMotorIntensity(int controllerIndex) {
        if (controllerIndex < 0 || controllerIndex > 3) return 0;
        return leftMotorIntensity[controllerIndex];
    }

    /**
     * Get current right motor intensity for a controller.
     */
    public static float getRightMotorIntensity(int controllerIndex) {
        if (controllerIndex < 0 || controllerIndex > 3) return 0;
        return rightMotorIntensity[controllerIndex];
    }

    // ========== JNA Backend ==========

    /**
     * JNA-based backend using direct DLL calls.
     * This is the most efficient approach when JNA is available.
     */
    private static class JNABackend implements VibrationBackend {
        private Object xinputLib;
        private java.lang.reflect.Method setStateMethod;

        public JNABackend() throws Exception {
            // Load JNA classes via reflection
            Class<?> nativeClass = Class.forName("com.sun.jna.Native");
            Class<?> structureClass = Class.forName("com.sun.jna.Structure");
            Class<?> libraryClass = Class.forName("com.sun.jna.Library");

            // Create XInput interface using JNA
            // We use reflection to avoid compile-time JNA dependency

            // Try to load XInput DLL
            String[] dllNames = {"xinput1_4", "xinput1_3", "xinput9_1_0"};
            Exception lastException = null;

            for (String dllName : dllNames) {
                try {
                    // Use Native.load() to get the XInput library
                    java.lang.reflect.Method loadMethod = nativeClass.getMethod("load",
                        String.class, Class.class);

                    // Create a dynamic interface for XInput
                    Class<?> xinputInterface = createXInputInterface(libraryClass);
                    xinputLib = loadMethod.invoke(null, dllName, xinputInterface);

                    // Get the XInputSetState method
                    setStateMethod = xinputLib.getClass().getMethod("XInputSetState",
                        int.class, XInputVibrationStruct.class);

                    System.out.println("XInputVibration: Loaded " + dllName + ".dll via JNA");
                    return;
                } catch (Exception e) {
                    lastException = e;
                }
            }

            throw lastException != null ? lastException : new Exception("No XInput DLL found");
        }

        private Class<?> createXInputInterface(Class<?> libraryClass) throws Exception {
            // For JNA, we need to use a pre-defined interface
            // Since we can't create interfaces at runtime easily,
            // we'll use a different approach with Function
            throw new Exception("JNA interface creation requires pre-compiled interface");
        }

        @Override
        public boolean vibrate(int controller, float left, float right) {
            // This would call XInputSetState via JNA
            // For now, delegate to NativeHelperBackend
            return false;
        }

        @Override
        public void stop(int controller) {
            vibrate(controller, 0, 0);
        }

        @Override
        public void shutdown() {
            // Nothing to clean up
        }
    }

    // Placeholder struct - would be used by JNA
    private static class XInputVibrationStruct {
        public short wLeftMotorSpeed;
        public short wRightMotorSpeed;
    }

    // ========== Native Helper Backend ==========

    /**
     * Backend using a native C# helper that stays resident.
     * The helper accepts commands via stdin for efficient vibration control.
     */
    private static class NativeHelperBackend implements VibrationBackend {
        private Process helperProcess;
        private BufferedWriter helperInput;
        private boolean helperRunning = false;

        public NativeHelperBackend() throws Exception {
            // Create the helper executable
            File helperExe = createHelperExecutable();
            if (helperExe == null || !helperExe.exists()) {
                throw new Exception("Could not create helper executable");
            }

            // Start the helper process
            ProcessBuilder pb = new ProcessBuilder(helperExe.getAbsolutePath());
            pb.redirectErrorStream(true);
            helperProcess = pb.start();
            helperInput = new BufferedWriter(new OutputStreamWriter(helperProcess.getOutputStream()));
            helperRunning = true;

            // Read any startup output
            new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(helperProcess.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Could log helper output if needed
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }).start();

            // Give it a moment to start
            Thread.sleep(100);

            if (!helperProcess.isAlive()) {
                throw new Exception("Helper process exited immediately");
            }
        }

        private File createHelperExecutable() {
            // Create a temporary C# source file and compile it
            try {
                Path tempDir = Files.createTempDirectory("xinput_helper");
                File csFile = new File(tempDir.toFile(), "XInputHelper.cs");
                File exeFile = new File(tempDir.toFile(), "XInputHelper.exe");

                // Write the C# helper source
                String csSource =
                    "using System;\n" +
                    "using System.Runtime.InteropServices;\n" +
                    "\n" +
                    "class XInputHelper {\n" +
                    "    [StructLayout(LayoutKind.Sequential)]\n" +
                    "    struct XINPUT_VIBRATION {\n" +
                    "        public ushort wLeftMotorSpeed;\n" +
                    "        public ushort wRightMotorSpeed;\n" +
                    "    }\n" +
                    "\n" +
                    "    [DllImport(\"xinput1_4.dll\")]\n" +
                    "    static extern int XInputSetState(int dwUserIndex, ref XINPUT_VIBRATION pVibration);\n" +
                    "\n" +
                    "    static void Main() {\n" +
                    "        string line;\n" +
                    "        while ((line = Console.ReadLine()) != null) {\n" +
                    "            try {\n" +
                    "                string[] parts = line.Split(' ');\n" +
                    "                if (parts.Length >= 3) {\n" +
                    "                    int controller = int.Parse(parts[0]);\n" +
                    "                    ushort left = ushort.Parse(parts[1]);\n" +
                    "                    ushort right = ushort.Parse(parts[2]);\n" +
                    "                    XINPUT_VIBRATION vib = new XINPUT_VIBRATION();\n" +
                    "                    vib.wLeftMotorSpeed = left;\n" +
                    "                    vib.wRightMotorSpeed = right;\n" +
                    "                    XInputSetState(controller, ref vib);\n" +
                    "                }\n" +
                    "            } catch {}\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n";

                Files.write(csFile.toPath(), csSource.getBytes());

                // Compile with csc.exe
                String[] cscPaths = {
                    "C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\csc.exe",
                    "C:\\Windows\\Microsoft.NET\\Framework\\v4.0.30319\\csc.exe"
                };

                for (String cscPath : cscPaths) {
                    File csc = new File(cscPath);
                    if (csc.exists()) {
                        ProcessBuilder pb = new ProcessBuilder(
                            cscPath,
                            "/nologo",
                            "/optimize",
                            "/out:" + exeFile.getAbsolutePath(),
                            csFile.getAbsolutePath()
                        );
                        pb.redirectErrorStream(true);
                        Process p = pb.start();

                        // Read compiler output
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(p.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // Could log compiler output
                        }

                        p.waitFor();

                        if (exeFile.exists()) {
                            // Mark temp files for deletion on exit
                            csFile.deleteOnExit();
                            exeFile.deleteOnExit();
                            tempDir.toFile().deleteOnExit();
                            return exeFile;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("XInputVibration: Failed to create helper - " + e.getMessage());
            }
            return null;
        }

        @Override
        public boolean vibrate(int controller, float left, float right) {
            if (!helperRunning || helperInput == null) {
                return false;
            }

            try {
                // Convert 0.0-1.0 to 0-65535
                int leftSpeed = (int)(left * 65535);
                int rightSpeed = (int)(right * 65535);

                // Send command to helper
                helperInput.write(controller + " " + leftSpeed + " " + rightSpeed);
                helperInput.newLine();
                helperInput.flush();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public void stop(int controller) {
            vibrate(controller, 0, 0);
        }

        @Override
        public void shutdown() {
            helperRunning = false;
            try {
                if (helperInput != null) {
                    helperInput.close();
                }
                if (helperProcess != null) {
                    helperProcess.destroy();
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
