package input;

import net.java.games.input.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages Xbox controller input using JInput library.
 * Maps controller inputs to virtual keyboard/mouse events that the game understands.
 *
 * Controller Mappings:
 * - Left Stick (LS): Movement (WASD equivalent)
 *   - LS Right → 'D' key
 *   - LS Left → 'A' key
 *   - LS Up → 'W' key
 *   - LS Down → 'S' key
 *   - LS Click (L3) → Shift (Sprint)
 * - Right Stick (RS): Mouse pointer control
 *   - RS Move → Virtual mouse cursor movement
 * - Triggers:
 *   - Right Trigger (RT) → Left mouse click
 * - Bumpers:
 *   - Left Bumper (LB) → Hotbar previous slot
 *   - Right Bumper (RB) → Hotbar next slot
 * - Buttons:
 *   - A Button → Space (Jump)
 *   - X Button → 'E' key (Interact/Mine)
 *   - Y Button → 'I' key (Inventory)
 *   - Start Button → 'M' key (Menu/Settings)
 *   - Back Button → Escape key
 *
 * Vibration/Haptic Feedback:
 * - Supports rumble motors (left = low frequency, right = high frequency)
 * - Use vibrate() methods to trigger haptic feedback
 * - Patterns defined in VibrationPattern enum
 */
public class ControllerManager {

    private static ControllerManager instance;

    private Controller xboxController;
    private boolean controllerConnected = false;
    private boolean jinputAvailable = false;

    // Rumble/vibration support
    private Rumbler leftRumbler = null;   // Low frequency motor (JInput)
    private Rumbler rightRumbler = null;  // High frequency motor (JInput)
    private boolean vibrationEnabled = true;
    private ExecutorService vibrationExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean isVibrating = false;
    private volatile long vibrationEndTime = 0;

    // XInput vibration support (preferred on Windows for Xbox controllers)
    private boolean useXInputVibration = false;
    private int xinputControllerIndex = 0;

    // Stick axis values (-1.0 to 1.0)
    private float leftStickX = 0;
    private float leftStickY = 0;
    private float rightStickX = 0;
    private float rightStickY = 0;

    // Button states
    private boolean buttonA = false;
    private boolean buttonB = false;
    private boolean buttonX = false;
    private boolean buttonY = false;
    private boolean buttonStart = false;
    private boolean buttonBack = false;
    private boolean buttonLB = false;
    private boolean buttonRB = false;
    private boolean buttonL3 = false;  // Left stick click
    private boolean buttonR3 = false;  // Right stick click

    // Previous button states for "just pressed" detection
    private boolean prevButtonA = false;
    private boolean prevButtonX = false;
    private boolean prevButtonY = false;
    private boolean prevButtonStart = false;
    private boolean prevButtonL3 = false;
    private boolean prevButtonR3 = false;
    private boolean prevButtonBack = false;
    private boolean prevButtonLB = false;
    private boolean prevButtonRB = false;

    // Right trigger click state (for "just pressed" detection)
    private boolean rightTriggerPressed = false;
    private boolean prevRightTriggerPressed = false;
    private static final float TRIGGER_THRESHOLD = 0.5f;

    // Triggers (0.0 to 1.0)
    private float leftTrigger = 0;
    private float rightTrigger = 0;

    // D-Pad
    private boolean dpadUp = false;
    private boolean dpadDown = false;
    private boolean dpadLeft = false;
    private boolean dpadRight = false;

    // Previous D-Pad states for "just pressed" detection
    private boolean prevDpadUp = false;
    private boolean prevDpadDown = false;
    private boolean prevDpadLeft = false;
    private boolean prevDpadRight = false;

    // Virtual mouse position (controlled by right stick)
    private float virtualMouseX = 960;  // Center of 1920x1080 screen
    private float virtualMouseY = 540;
    private static final float MOUSE_SENSITIVITY = 12.0f;  // Pixels per frame at full stick deflection

    // Dead zone for analog sticks (to prevent drift)
    private static final float DEAD_ZONE = 0.15f;

    // Threshold for treating stick as "pressed" for WASD mapping
    private static final float STICK_PRESS_THRESHOLD = 0.5f;

    private ControllerManager() {
        initializeJInput();
    }

    public static ControllerManager getInstance() {
        if (instance == null) {
            instance = new ControllerManager();
        }
        return instance;
    }

    /**
     * Initialize JInput and find Xbox controller.
     */
    private void initializeJInput() {
        try {
            // Check if JInput is available
            Class.forName("net.java.games.input.ControllerEnvironment");
            jinputAvailable = true;
            System.out.println("JInput library found - Controller support enabled");

            findXboxController();
        } catch (ClassNotFoundException e) {
            jinputAvailable = false;
            System.out.println("JInput library not found - Controller support disabled");
            System.out.println("To enable Xbox controller support, add JInput library to classpath");
        } catch (Exception e) {
            jinputAvailable = false;
            System.out.println("Failed to initialize JInput: " + e.getMessage());
        }
    }

    /**
     * Scan for Xbox/gamepad controllers.
     */
    public void findXboxController() {
        if (!jinputAvailable) return;

        try {
            Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

            for (Controller controller : controllers) {
                Controller.Type type = controller.getType();
                String name = controller.getName().toLowerCase();

                // Skip known non-gamepad devices that have "controller" in the name
                if (name.contains("led") || name.contains("aura") || name.contains("rgb") ||
                    name.contains("lighting") || name.contains("chroma")) {
                    continue;
                }

                // Primary check: Look for proper gamepad/stick type controllers
                if (type == Controller.Type.GAMEPAD || type == Controller.Type.STICK) {
                    xboxController = controller;
                    controllerConnected = true;
                    System.out.println("Xbox controller connected: " + controller.getName());
                    initializeRumblers();
                    return;
                }

                // Fallback: Check for known Xbox controller names that might not report correct type
                if (name.contains("xbox") || name.contains("xinput")) {
                    xboxController = controller;
                    controllerConnected = true;
                    System.out.println("Xbox controller connected: " + controller.getName());
                    initializeRumblers();
                    return;
                }
            }

            if (!controllerConnected) {
                System.out.println("No Xbox controller found. Connect a controller and restart the game.");
            }
        } catch (Exception e) {
            System.out.println("Error scanning for controllers: " + e.getMessage());
        }
    }

    /**
     * Poll the controller for current state.
     * Should be called once per frame from the game loop.
     */
    public void poll() {
        if (!jinputAvailable || !controllerConnected || xboxController == null) {
            return;
        }

        // Save previous button states
        prevButtonA = buttonA;
        prevButtonX = buttonX;
        prevButtonY = buttonY;
        prevButtonStart = buttonStart;
        prevButtonL3 = buttonL3;
        prevButtonR3 = buttonR3;
        prevButtonBack = buttonBack;
        prevButtonLB = buttonLB;
        prevButtonRB = buttonRB;
        prevRightTriggerPressed = rightTriggerPressed;

        // Save previous D-Pad states
        prevDpadUp = dpadUp;
        prevDpadDown = dpadDown;
        prevDpadLeft = dpadLeft;
        prevDpadRight = dpadRight;

        // Poll the controller
        if (!xboxController.poll()) {
            // Controller disconnected
            controllerConnected = false;
            System.out.println("Xbox controller disconnected");
            return;
        }

        // Read all components
        Component[] components = xboxController.getComponents();

        for (Component component : components) {
            Component.Identifier id = component.getIdentifier();
            float value = component.getPollData();

            // Analog sticks
            if (id == Component.Identifier.Axis.X) {
                leftStickX = applyDeadZone(value);
            } else if (id == Component.Identifier.Axis.Y) {
                leftStickY = applyDeadZone(value);
            } else if (id == Component.Identifier.Axis.RX) {
                rightStickX = applyDeadZone(value);
            } else if (id == Component.Identifier.Axis.RY) {
                rightStickY = applyDeadZone(value);
            }
            // Triggers - handle controllers that use combined Z axis for both triggers
            else if (id == Component.Identifier.Axis.Z) {
                // Some controllers map both triggers to Z axis (positive = RT, negative = LT)
                if (value > 0) {
                    rightTrigger = value;
                    leftTrigger = 0;  // Reset left trigger when right is pressed
                } else if (value < 0) {
                    leftTrigger = -value;
                    rightTrigger = 0;  // Reset right trigger when left is pressed
                } else {
                    // Value is 0 - both triggers released
                    leftTrigger = 0;
                    rightTrigger = 0;
                }
            }
            // Some controllers use RZ axis specifically for right trigger
            else if (id == Component.Identifier.Axis.RZ) {
                // RZ axis is typically 0.0 to 1.0 for right trigger
                rightTrigger = Math.max(0, value);
            }
            // Buttons (Xbox controller button mapping)
            else if (id == Component.Identifier.Button._0) {
                buttonA = value > 0.5f;  // A button
            } else if (id == Component.Identifier.Button._1) {
                buttonB = value > 0.5f;  // B button
            } else if (id == Component.Identifier.Button._2) {
                buttonX = value > 0.5f;  // X button
            } else if (id == Component.Identifier.Button._3) {
                buttonY = value > 0.5f;  // Y button
            } else if (id == Component.Identifier.Button._4) {
                buttonLB = value > 0.5f;  // Left bumper
            } else if (id == Component.Identifier.Button._5) {
                buttonRB = value > 0.5f;  // Right bumper
            } else if (id == Component.Identifier.Button._6) {
                buttonBack = value > 0.5f;  // Back/Select
            } else if (id == Component.Identifier.Button._7) {
                buttonStart = value > 0.5f;  // Start
            } else if (id == Component.Identifier.Button._8) {
                buttonL3 = value > 0.5f;  // Left stick click
            } else if (id == Component.Identifier.Button._9) {
                buttonR3 = value > 0.5f;  // Right stick click
            }
            // D-Pad (POV hat)
            else if (id == Component.Identifier.Axis.POV) {
                updateDPad(value);
            }
        }

        // Update virtual mouse position based on left stick
        updateVirtualMouse();

        // Update right trigger pressed state (for click detection)
        rightTriggerPressed = rightTrigger >= TRIGGER_THRESHOLD;
    }

    /**
     * Apply dead zone to analog stick value.
     */
    private float applyDeadZone(float value) {
        if (Math.abs(value) < DEAD_ZONE) {
            return 0;
        }
        // Rescale to full range after dead zone
        float sign = value > 0 ? 1 : -1;
        return sign * (Math.abs(value) - DEAD_ZONE) / (1 - DEAD_ZONE);
    }

    /**
     * Update D-Pad state from POV hat value.
     */
    private void updateDPad(float povValue) {
        dpadUp = false;
        dpadDown = false;
        dpadLeft = false;
        dpadRight = false;

        if (povValue == Component.POV.UP) {
            dpadUp = true;
        } else if (povValue == Component.POV.DOWN) {
            dpadDown = true;
        } else if (povValue == Component.POV.LEFT) {
            dpadLeft = true;
        } else if (povValue == Component.POV.RIGHT) {
            dpadRight = true;
        } else if (povValue == Component.POV.UP_LEFT) {
            dpadUp = true;
            dpadLeft = true;
        } else if (povValue == Component.POV.UP_RIGHT) {
            dpadUp = true;
            dpadRight = true;
        } else if (povValue == Component.POV.DOWN_LEFT) {
            dpadDown = true;
            dpadLeft = true;
        } else if (povValue == Component.POV.DOWN_RIGHT) {
            dpadDown = true;
            dpadRight = true;
        }
    }

    /**
     * Update virtual mouse position based on right stick movement.
     */
    private void updateVirtualMouse() {
        if (rightStickX != 0 || rightStickY != 0) {
            virtualMouseX += rightStickX * MOUSE_SENSITIVITY;
            virtualMouseY += rightStickY * MOUSE_SENSITIVITY;

            // Clamp to screen bounds
            virtualMouseX = Math.max(0, Math.min(1920, virtualMouseX));
            virtualMouseY = Math.max(0, Math.min(1080, virtualMouseY));
        }
    }

    // ========== Virtual Keyboard Mappings (Left Stick → WASD) ==========

    /**
     * Check if left stick simulates 'W' key (LS Up).
     */
    public boolean isLeftStickUp() {
        return leftStickY < -STICK_PRESS_THRESHOLD;
    }

    /**
     * Check if left stick simulates 'S' key (LS Down).
     */
    public boolean isLeftStickDown() {
        return leftStickY > STICK_PRESS_THRESHOLD;
    }

    /**
     * Check if left stick simulates 'A' key (LS Left).
     */
    public boolean isLeftStickLeft() {
        return leftStickX < -STICK_PRESS_THRESHOLD;
    }

    /**
     * Check if left stick simulates 'D' key (LS Right).
     */
    public boolean isLeftStickRight() {
        return leftStickX > STICK_PRESS_THRESHOLD;
    }

    // ========== Virtual Button Mappings ==========

    /**
     * Check if A button is pressed (maps to Space/Jump).
     */
    public boolean isButtonAPressed() {
        return buttonA;
    }

    /**
     * Check if A button was just pressed this frame.
     */
    public boolean isButtonAJustPressed() {
        return buttonA && !prevButtonA;
    }

    /**
     * Check if X button is pressed (maps to 'E'/Interact).
     */
    public boolean isButtonXPressed() {
        return buttonX;
    }

    /**
     * Check if X button was just pressed this frame.
     */
    public boolean isButtonXJustPressed() {
        return buttonX && !prevButtonX;
    }

    /**
     * Check if Y button is pressed (maps to 'I'/Inventory).
     */
    public boolean isButtonYPressed() {
        return buttonY;
    }

    /**
     * Check if Y button was just pressed this frame.
     */
    public boolean isButtonYJustPressed() {
        return buttonY && !prevButtonY;
    }

    /**
     * Check if Start button is pressed (maps to 'M'/Menu).
     */
    public boolean isButtonStartPressed() {
        return buttonStart;
    }

    /**
     * Check if Start button was just pressed this frame.
     */
    public boolean isButtonStartJustPressed() {
        return buttonStart && !prevButtonStart;
    }

    /**
     * Check if Left Stick is clicked (L3) - maps to Sprint/Shift.
     */
    public boolean isLeftStickClicked() {
        return buttonL3;
    }

    /**
     * Check if Left Stick was just clicked this frame.
     */
    public boolean isLeftStickJustClicked() {
        return buttonL3 && !prevButtonL3;
    }

    /**
     * Check if Right Trigger is pressed (maps to left mouse click).
     */
    public boolean isRightTriggerPressed() {
        return rightTriggerPressed;
    }

    /**
     * Check if Right Trigger was just pressed this frame.
     */
    public boolean isRightTriggerJustPressed() {
        return rightTriggerPressed && !prevRightTriggerPressed;
    }

    /**
     * Check if Right Trigger was just released this frame.
     */
    public boolean isRightTriggerJustReleased() {
        return !rightTriggerPressed && prevRightTriggerPressed;
    }

    /**
     * Check if Left Bumper (LB) was just pressed - for hotbar navigation left.
     */
    public boolean isButtonLBJustPressed() {
        return buttonLB && !prevButtonLB;
    }

    /**
     * Check if Right Bumper (RB) was just pressed - for hotbar navigation right.
     */
    public boolean isButtonRBJustPressed() {
        return buttonRB && !prevButtonRB;
    }

    /**
     * Check if Back button is pressed (maps to Escape key).
     */
    public boolean isButtonBackJustPressed() {
        return buttonBack && !prevButtonBack;
    }

    // ========== Virtual Mouse (Right Stick) ==========

    /**
     * Get virtual mouse X position controlled by right stick.
     */
    public int getVirtualMouseX() {
        return Math.round(virtualMouseX);
    }

    /**
     * Get virtual mouse Y position controlled by right stick.
     */
    public int getVirtualMouseY() {
        return Math.round(virtualMouseY);
    }

    /**
     * Check if right stick is being moved (for mouse control).
     */
    public boolean isRightStickActive() {
        return rightStickX != 0 || rightStickY != 0;
    }

    /**
     * Set virtual mouse position (e.g., to sync with real mouse).
     */
    public void setVirtualMousePosition(int x, int y) {
        virtualMouseX = x;
        virtualMouseY = y;
    }

    // ========== Raw Axis Values ==========

    public float getLeftStickX() { return leftStickX; }
    public float getLeftStickY() { return leftStickY; }
    public float getRightStickX() { return rightStickX; }
    public float getRightStickY() { return rightStickY; }
    public float getLeftTrigger() { return leftTrigger; }
    public float getRightTrigger() { return rightTrigger; }

    // ========== Raw Button States ==========

    public boolean isButtonBPressed() { return buttonB; }
    public boolean isButtonLBPressed() { return buttonLB; }
    public boolean isButtonRBPressed() { return buttonRB; }
    public boolean isButtonBackPressed() { return buttonBack; }
    public boolean isButtonR3Pressed() { return buttonR3; }

    // ========== D-Pad ==========

    public boolean isDPadUp() { return dpadUp; }
    public boolean isDPadDown() { return dpadDown; }
    public boolean isDPadLeft() { return dpadLeft; }
    public boolean isDPadRight() { return dpadRight; }

    /**
     * Check if D-Pad Up was just pressed this frame.
     */
    public boolean isDPadUpJustPressed() { return dpadUp && !prevDpadUp; }

    /**
     * Check if D-Pad Down was just pressed this frame.
     */
    public boolean isDPadDownJustPressed() { return dpadDown && !prevDpadDown; }

    /**
     * Check if D-Pad Left was just pressed this frame.
     */
    public boolean isDPadLeftJustPressed() { return dpadLeft && !prevDpadLeft; }

    /**
     * Check if D-Pad Right was just pressed this frame.
     */
    public boolean isDPadRightJustPressed() { return dpadRight && !prevDpadRight; }

    // ========== Status ==========

    public boolean isControllerConnected() {
        return controllerConnected;
    }

    public boolean isJInputAvailable() {
        return jinputAvailable;
    }

    public String getControllerName() {
        return xboxController != null ? xboxController.getName() : "None";
    }

    /**
     * Attempt to reconnect controller if disconnected.
     */
    public void attemptReconnect() {
        if (!controllerConnected && jinputAvailable) {
            findXboxController();
        }
    }

    /**
     * Get list of all connected controllers for debugging.
     */
    public List<String> getAvailableControllers() {
        List<String> result = new ArrayList<>();
        if (!jinputAvailable) {
            result.add("JInput not available");
            return result;
        }

        try {
            Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
            for (Controller controller : controllers) {
                result.add(controller.getName() + " [" + controller.getType() + "]");
            }
        } catch (Exception e) {
            result.add("Error: " + e.getMessage());
        }
        return result;
    }

    // ========== Vibration/Rumble Support ==========

    /**
     * Initialize rumbler motors for vibration support.
     * Tries XInput first (better support for Xbox controllers on Windows),
     * then falls back to JInput rumblers.
     */
    private void initializeRumblers() {
        if (xboxController == null) return;

        // First, try XInput for Windows (works better with Xbox controllers)
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            try {
                // Initialize XInput vibration
                XInputVibration.initialize();

                // Test if XInput can set vibration (brief test pulse)
                String controllerName = xboxController.getName().toLowerCase();
                if (controllerName.contains("xbox") || controllerName.contains("xinput") ||
                    controllerName.contains("controller")) {
                    useXInputVibration = true;
                    xinputControllerIndex = 0; // Default to first controller
                    System.out.println("Controller vibration: Using XInput for Xbox controller");

                    // Do a quick test vibration to verify it works
                    testVibration();
                    return;
                }
            } catch (Exception e) {
                System.out.println("Controller vibration: XInput init failed - " + e.getMessage());
            }
        }

        // Fallback to JInput rumblers
        try {
            Rumbler[] rumblers = xboxController.getRumblers();
            if (rumblers == null || rumblers.length == 0) {
                System.out.println("Controller vibration: No JInput rumblers found");

                // On Windows, still try XInput as final fallback
                if (os.contains("win")) {
                    useXInputVibration = true;
                    System.out.println("Controller vibration: Falling back to XInput");
                    testVibration();
                }
                return;
            }

            System.out.println("Controller vibration: Found " + rumblers.length + " JInput rumbler(s)");

            for (Rumbler rumbler : rumblers) {
                String name = rumbler.getAxisName().toLowerCase();
                System.out.println("  - Rumbler: " + rumbler.getAxisName() + " [" + rumbler.getAxisIdentifier() + "]");

                // Try to identify left (low frequency) vs right (high frequency) motor
                if (name.contains("left") || name.contains("low") || name.contains("x")) {
                    leftRumbler = rumbler;
                } else if (name.contains("right") || name.contains("high") || name.contains("y")) {
                    rightRumbler = rumbler;
                } else {
                    // If we can't identify, assign to whichever is null
                    if (leftRumbler == null) {
                        leftRumbler = rumbler;
                    } else if (rightRumbler == null) {
                        rightRumbler = rumbler;
                    }
                }
            }

            // If we only found one rumbler, use it for both
            if (leftRumbler != null && rightRumbler == null) {
                rightRumbler = leftRumbler;
            } else if (rightRumbler != null && leftRumbler == null) {
                leftRumbler = rightRumbler;
            }

            if (leftRumbler != null || rightRumbler != null) {
                System.out.println("Controller vibration: JInput rumblers initialized");
            }
        } catch (Exception e) {
            System.out.println("Controller vibration: JInput init failed - " + e.getMessage());
        }
    }

    /**
     * Test vibration with a brief pulse to verify it works.
     */
    private void testVibration() {
        vibrationExecutor.submit(() -> {
            try {
                if (useXInputVibration) {
                    XInputVibration.vibrate(xinputControllerIndex, 0.5f, 0.5f);
                    Thread.sleep(100);
                    XInputVibration.stop(xinputControllerIndex);
                    System.out.println("Controller vibration: Test pulse sent via XInput");
                }
            } catch (Exception e) {
                System.out.println("Controller vibration: Test failed - " + e.getMessage());
            }
        });
    }

    /**
     * Check if vibration/rumble is supported.
     */
    public boolean isVibrationSupported() {
        return controllerConnected && (useXInputVibration || leftRumbler != null || rightRumbler != null);
    }

    /**
     * Enable or disable vibration.
     */
    public void setVibrationEnabled(boolean enabled) {
        this.vibrationEnabled = enabled;
        if (!enabled) {
            stopVibration();
        }
    }

    /**
     * Check if vibration is enabled.
     */
    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }

    /**
     * Trigger a vibration pattern.
     * @param pattern The vibration pattern to execute
     */
    public void vibrate(VibrationPattern pattern) {
        if (!vibrationEnabled || !isVibrationSupported() || pattern == null) {
            return;
        }

        if (pattern.isComplex()) {
            // Execute complex multi-step pattern asynchronously
            executeComplexPattern(pattern);
        } else {
            // Execute simple single-pulse pattern
            executeSimpleVibration(pattern.getIntensity(), pattern.getDurationMs(), pattern.getMotor());
        }
    }

    /**
     * Trigger a simple vibration with specified intensity and duration.
     * @param intensity Vibration strength (0.0 to 1.0)
     * @param durationMs Duration in milliseconds
     */
    public void vibrate(float intensity, int durationMs) {
        vibrate(intensity, durationMs, VibrationPattern.MotorType.BOTH);
    }

    /**
     * Trigger a vibration with specified motor selection.
     * @param intensity Vibration strength (0.0 to 1.0)
     * @param durationMs Duration in milliseconds
     * @param motor Which motor(s) to use
     */
    public void vibrate(float intensity, int durationMs, VibrationPattern.MotorType motor) {
        if (!vibrationEnabled || !isVibrationSupported()) {
            return;
        }
        executeSimpleVibration(intensity, durationMs, motor);
    }

    /**
     * Execute a simple single-pulse vibration.
     */
    private void executeSimpleVibration(float intensity, int durationMs, VibrationPattern.MotorType motor) {
        vibrationExecutor.submit(() -> {
            try {
                isVibrating = true;
                vibrationEndTime = System.currentTimeMillis() + durationMs;

                // Set motor intensities
                setMotorIntensity(intensity, motor);

                // Wait for duration
                Thread.sleep(durationMs);

                // Stop vibration (only if we haven't started a new one)
                if (System.currentTimeMillis() >= vibrationEndTime) {
                    stopMotors();
                }
            } catch (InterruptedException e) {
                stopMotors();
                Thread.currentThread().interrupt();
            } finally {
                isVibrating = false;
            }
        });
    }

    /**
     * Execute a complex multi-step vibration pattern.
     */
    private void executeComplexPattern(VibrationPattern pattern) {
        VibrationPattern.VibrationStep[] steps = pattern.getSteps();
        if (steps == null || steps.length == 0) return;

        vibrationExecutor.submit(() -> {
            try {
                isVibrating = true;
                long totalDuration = 0;
                for (VibrationPattern.VibrationStep step : steps) {
                    totalDuration += step.durationMs;
                }
                vibrationEndTime = System.currentTimeMillis() + totalDuration;

                // Execute each step in sequence
                for (VibrationPattern.VibrationStep step : steps) {
                    if (Thread.currentThread().isInterrupted()) break;

                    setMotorIntensity(step.intensity, step.motor);
                    Thread.sleep(step.durationMs);
                }

                // Stop vibration at the end
                stopMotors();
            } catch (InterruptedException e) {
                stopMotors();
                Thread.currentThread().interrupt();
            } finally {
                isVibrating = false;
            }
        });
    }

    /**
     * Set motor intensity based on motor type selection.
     */
    private void setMotorIntensity(float intensity, VibrationPattern.MotorType motor) {
        // Use XInput if available (better support for Xbox controllers)
        if (useXInputVibration) {
            try {
                float leftIntensity = 0;
                float rightIntensity = 0;

                switch (motor) {
                    case LEFT:
                        leftIntensity = intensity;
                        rightIntensity = 0;
                        break;
                    case RIGHT:
                        leftIntensity = 0;
                        rightIntensity = intensity;
                        break;
                    case BOTH:
                    default:
                        leftIntensity = intensity;
                        rightIntensity = intensity;
                        break;
                }

                XInputVibration.vibrate(xinputControllerIndex, leftIntensity, rightIntensity);
                return;
            } catch (Exception e) {
                // Fall through to JInput
            }
        }

        // Fallback to JInput rumblers
        try {
            switch (motor) {
                case LEFT:
                    if (leftRumbler != null) {
                        leftRumbler.rumble(intensity);
                    }
                    if (rightRumbler != null && rightRumbler != leftRumbler) {
                        rightRumbler.rumble(0);
                    }
                    break;
                case RIGHT:
                    if (rightRumbler != null) {
                        rightRumbler.rumble(intensity);
                    }
                    if (leftRumbler != null && leftRumbler != rightRumbler) {
                        leftRumbler.rumble(0);
                    }
                    break;
                case BOTH:
                default:
                    if (leftRumbler != null) {
                        leftRumbler.rumble(intensity);
                    }
                    if (rightRumbler != null && rightRumbler != leftRumbler) {
                        rightRumbler.rumble(intensity);
                    }
                    break;
            }
        } catch (Exception e) {
            // Silently handle rumbler errors
        }
    }

    /**
     * Stop all motor vibration.
     */
    private void stopMotors() {
        // Stop XInput vibration
        if (useXInputVibration) {
            try {
                XInputVibration.stop(xinputControllerIndex);
            } catch (Exception e) {
                // Silently handle errors
            }
        }

        // Stop JInput rumblers
        try {
            if (leftRumbler != null) {
                leftRumbler.rumble(0);
            }
            if (rightRumbler != null && rightRumbler != leftRumbler) {
                rightRumbler.rumble(0);
            }
        } catch (Exception e) {
            // Silently handle rumbler errors
        }
    }

    /**
     * Immediately stop any ongoing vibration.
     */
    public void stopVibration() {
        vibrationEndTime = 0;
        stopMotors();
    }

    /**
     * Check if controller is currently vibrating.
     */
    public boolean isVibrating() {
        return isVibrating;
    }

    /**
     * Shutdown the vibration executor (call on game exit).
     */
    public void shutdown() {
        stopVibration();
        vibrationExecutor.shutdownNow();
    }
}
