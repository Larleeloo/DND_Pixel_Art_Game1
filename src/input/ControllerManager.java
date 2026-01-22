package input;

import net.java.games.input.*;
import java.util.ArrayList;
import java.util.List;

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
 * - Right Stick (RS): Mouse pointer control
 *   - RS Move → Virtual mouse cursor movement
 *   - RS Click (R3) → Left mouse click
 * - Buttons:
 *   - A Button → Space (Jump)
 *   - X Button → 'E' key (Interact/Mine)
 *   - Y Button → 'I' key (Inventory)
 *   - Start Button → 'M' key (Menu/Settings)
 *   - Back Button → Escape key
 */
public class ControllerManager {

    private static ControllerManager instance;

    private Controller xboxController;
    private boolean controllerConnected = false;
    private boolean jinputAvailable = false;

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

    // Triggers (0.0 to 1.0)
    private float leftTrigger = 0;
    private float rightTrigger = 0;

    // D-Pad
    private boolean dpadUp = false;
    private boolean dpadDown = false;
    private boolean dpadLeft = false;
    private boolean dpadRight = false;

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
                    return;
                }

                // Fallback: Check for known Xbox controller names that might not report correct type
                if (name.contains("xbox") || name.contains("xinput")) {
                    xboxController = controller;
                    controllerConnected = true;
                    System.out.println("Xbox controller connected: " + controller.getName());
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
            // Triggers
            else if (id == Component.Identifier.Axis.Z) {
                // Some controllers map triggers to Z axis
                if (value > 0) {
                    rightTrigger = value;
                } else {
                    leftTrigger = -value;
                }
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
     * Check if Right Stick is clicked (R3) - maps to left mouse click.
     */
    public boolean isRightStickClicked() {
        return buttonR3;
    }

    /**
     * Check if Right Stick was just clicked this frame.
     */
    public boolean isRightStickJustClicked() {
        return buttonR3 && !prevButtonR3;
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
}
