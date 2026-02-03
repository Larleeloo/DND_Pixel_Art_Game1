package com.ambermoongame.input;

import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Manages game controller (gamepad) input for Android.
 * Uses Android's native InputDevice API for controller support.
 *
 * Controller Mappings (matches desktop ControllerManager):
 * - Left Stick: Movement (WASD equivalent)
 * - Right Stick: Aiming/Virtual mouse cursor
 * - A Button: Jump (Space)
 * - B Button: Back (Escape)
 * - X Button: Interact (E)
 * - Y Button: Inventory (I)
 * - LB: Hotbar Previous
 * - RB: Hotbar Next
 * - LT: Interact/Fire
 * - RT: Attack/Click
 * - Start: Menu (M)
 * - Back/Select: Cancel (Escape)
 * - L3: Sprint (Shift)
 * - D-Pad: Navigation
 */
public class AndroidControllerManager {

    private static final String TAG = "ControllerManager";

    // Dead zone for analog sticks
    private static final float DEAD_ZONE = 0.15f;
    private static final float STICK_PRESS_THRESHOLD = 0.5f;
    private static final float TRIGGER_THRESHOLD = 0.5f;

    // Virtual mouse position (controlled by right stick)
    private float virtualMouseX = 960;
    private float virtualMouseY = 540;
    private static final float MOUSE_SENSITIVITY = 12.0f;

    // Stick axis values
    private float leftStickX = 0;
    private float leftStickY = 0;
    private float rightStickX = 0;
    private float rightStickY = 0;

    // Triggers
    private float leftTrigger = 0;
    private float rightTrigger = 0;

    // Button states
    private boolean buttonA = false;
    private boolean buttonB = false;
    private boolean buttonX = false;
    private boolean buttonY = false;
    private boolean buttonStart = false;
    private boolean buttonBack = false;
    private boolean buttonLB = false;
    private boolean buttonRB = false;
    private boolean buttonL3 = false;
    private boolean buttonR3 = false;

    // D-Pad states
    private boolean dpadUp = false;
    private boolean dpadDown = false;
    private boolean dpadLeft = false;
    private boolean dpadRight = false;

    // Previous frame states for "just pressed" detection
    private boolean prevButtonA = false;
    private boolean prevButtonB = false;
    private boolean prevButtonX = false;
    private boolean prevButtonY = false;
    private boolean prevButtonStart = false;
    private boolean prevButtonBack = false;
    private boolean prevButtonLB = false;
    private boolean prevButtonRB = false;
    private boolean prevButtonL3 = false;
    private boolean prevDpadUp = false;
    private boolean prevDpadDown = false;
    private boolean prevDpadLeft = false;
    private boolean prevDpadRight = false;

    // Trigger press states
    private boolean leftTriggerPressed = false;
    private boolean rightTriggerPressed = false;
    private boolean prevLeftTriggerPressed = false;
    private boolean prevRightTriggerPressed = false;

    // Controller connection state
    private boolean controllerConnected = false;
    private boolean usingController = false;
    private String controllerName = "None";

    public AndroidControllerManager() {
        checkControllerConnection();
    }

    /**
     * Check if any game controller is connected.
     */
    public void checkControllerConnection() {
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice device = InputDevice.getDevice(deviceId);
            if (device != null && isGameController(device)) {
                controllerConnected = true;
                controllerName = device.getName();
                return;
            }
        }
        controllerConnected = false;
        controllerName = "None";
    }

    private boolean isGameController(InputDevice device) {
        int sources = device.getSources();
        return ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
            || ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK);
    }

    /**
     * Poll controller state (called each frame).
     */
    public void poll() {
        // Save previous states
        prevButtonA = buttonA;
        prevButtonB = buttonB;
        prevButtonX = buttonX;
        prevButtonY = buttonY;
        prevButtonStart = buttonStart;
        prevButtonBack = buttonBack;
        prevButtonLB = buttonLB;
        prevButtonRB = buttonRB;
        prevButtonL3 = buttonL3;
        prevDpadUp = dpadUp;
        prevDpadDown = dpadDown;
        prevDpadLeft = dpadLeft;
        prevDpadRight = dpadRight;
        prevLeftTriggerPressed = leftTriggerPressed;
        prevRightTriggerPressed = rightTriggerPressed;

        // Update trigger pressed states
        leftTriggerPressed = leftTrigger >= TRIGGER_THRESHOLD;
        rightTriggerPressed = rightTrigger >= TRIGGER_THRESHOLD;

        // Update virtual mouse position
        if (rightStickX != 0 || rightStickY != 0) {
            virtualMouseX += rightStickX * MOUSE_SENSITIVITY;
            virtualMouseY += rightStickY * MOUSE_SENSITIVITY;

            // Clamp to screen bounds
            virtualMouseX = Math.max(0, Math.min(1920, virtualMouseX));
            virtualMouseY = Math.max(0, Math.min(1080, virtualMouseY));

            usingController = true;
        }
    }

    /**
     * Handle motion events (analog sticks, triggers).
     */
    public boolean handleMotionEvent(MotionEvent event) {
        // Left stick
        leftStickX = applyDeadZone(event.getAxisValue(MotionEvent.AXIS_X));
        leftStickY = applyDeadZone(event.getAxisValue(MotionEvent.AXIS_Y));

        // Right stick
        rightStickX = applyDeadZone(event.getAxisValue(MotionEvent.AXIS_Z));
        rightStickY = applyDeadZone(event.getAxisValue(MotionEvent.AXIS_RZ));

        // Alternative right stick mapping (some controllers use RX/RY)
        if (rightStickX == 0 && rightStickY == 0) {
            rightStickX = applyDeadZone(event.getAxisValue(MotionEvent.AXIS_RX));
            rightStickY = applyDeadZone(event.getAxisValue(MotionEvent.AXIS_RY));
        }

        // Triggers
        leftTrigger = event.getAxisValue(MotionEvent.AXIS_LTRIGGER);
        rightTrigger = event.getAxisValue(MotionEvent.AXIS_RTRIGGER);

        // Alternative trigger mapping
        if (leftTrigger == 0) {
            leftTrigger = event.getAxisValue(MotionEvent.AXIS_BRAKE);
        }
        if (rightTrigger == 0) {
            rightTrigger = event.getAxisValue(MotionEvent.AXIS_GAS);
        }

        // D-Pad via hat axes
        float hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
        float hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);

        dpadLeft = hatX < -0.5f;
        dpadRight = hatX > 0.5f;
        dpadUp = hatY < -0.5f;
        dpadDown = hatY > 0.5f;

        if (leftStickX != 0 || leftStickY != 0 || rightStickX != 0 || rightStickY != 0) {
            usingController = true;
        }

        return true;
    }

    /**
     * Handle key events (buttons).
     */
    public boolean handleKeyEvent(KeyEvent event) {
        boolean pressed = event.getAction() == KeyEvent.ACTION_DOWN;
        usingController = true;

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BUTTON_A:
                buttonA = pressed;
                return true;
            case KeyEvent.KEYCODE_BUTTON_B:
                buttonB = pressed;
                return true;
            case KeyEvent.KEYCODE_BUTTON_X:
                buttonX = pressed;
                return true;
            case KeyEvent.KEYCODE_BUTTON_Y:
                buttonY = pressed;
                return true;
            case KeyEvent.KEYCODE_BUTTON_START:
                buttonStart = pressed;
                return true;
            case KeyEvent.KEYCODE_BUTTON_SELECT:
                buttonBack = pressed;
                return true;
            case KeyEvent.KEYCODE_BUTTON_L1:
                buttonLB = pressed;
                return true;
            case KeyEvent.KEYCODE_BUTTON_R1:
                buttonRB = pressed;
                return true;
            case KeyEvent.KEYCODE_BUTTON_THUMBL:
                buttonL3 = pressed;
                return true;
            case KeyEvent.KEYCODE_BUTTON_THUMBR:
                buttonR3 = pressed;
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                dpadUp = pressed;
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                dpadDown = pressed;
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                dpadLeft = pressed;
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                dpadRight = pressed;
                return true;
        }

        return false;
    }

    private float applyDeadZone(float value) {
        if (Math.abs(value) < DEAD_ZONE) {
            return 0;
        }
        float sign = value > 0 ? 1 : -1;
        return sign * (Math.abs(value) - DEAD_ZONE) / (1 - DEAD_ZONE);
    }

    // ==================== Left Stick (Movement) ====================

    public boolean isLeftStickUp() { return leftStickY < -STICK_PRESS_THRESHOLD; }
    public boolean isLeftStickDown() { return leftStickY > STICK_PRESS_THRESHOLD; }
    public boolean isLeftStickLeft() { return leftStickX < -STICK_PRESS_THRESHOLD; }
    public boolean isLeftStickRight() { return leftStickX > STICK_PRESS_THRESHOLD; }
    public boolean isLeftStickClicked() { return buttonL3; }
    public boolean isLeftStickJustClicked() { return buttonL3 && !prevButtonL3; }

    // ==================== Button States ====================

    public boolean isButtonAPressed() { return buttonA; }
    public boolean isButtonAJustPressed() { return buttonA && !prevButtonA; }

    public boolean isButtonBPressed() { return buttonB; }
    public boolean isButtonBJustPressed() { return buttonB && !prevButtonB; }

    public boolean isButtonXPressed() { return buttonX; }
    public boolean isButtonXJustPressed() { return buttonX && !prevButtonX; }

    public boolean isButtonYPressed() { return buttonY; }
    public boolean isButtonYJustPressed() { return buttonY && !prevButtonY; }

    public boolean isButtonStartPressed() { return buttonStart; }
    public boolean isButtonStartJustPressed() { return buttonStart && !prevButtonStart; }

    public boolean isButtonBackPressed() { return buttonBack; }
    public boolean isButtonBackJustPressed() { return buttonBack && !prevButtonBack; }

    public boolean isButtonLBPressed() { return buttonLB; }
    public boolean isButtonLBJustPressed() { return buttonLB && !prevButtonLB; }

    public boolean isButtonRBPressed() { return buttonRB; }
    public boolean isButtonRBJustPressed() { return buttonRB && !prevButtonRB; }

    // ==================== Triggers ====================

    public boolean isLeftTriggerPressed() { return leftTriggerPressed; }
    public boolean isLeftTriggerJustPressed() { return leftTriggerPressed && !prevLeftTriggerPressed; }
    public boolean isLeftTriggerJustReleased() { return !leftTriggerPressed && prevLeftTriggerPressed; }

    public boolean isRightTriggerPressed() { return rightTriggerPressed; }
    public boolean isRightTriggerJustPressed() { return rightTriggerPressed && !prevRightTriggerPressed; }
    public boolean isRightTriggerJustReleased() { return !rightTriggerPressed && prevRightTriggerPressed; }

    // ==================== D-Pad ====================

    public boolean isDPadUp() { return dpadUp; }
    public boolean isDPadDown() { return dpadDown; }
    public boolean isDPadLeft() { return dpadLeft; }
    public boolean isDPadRight() { return dpadRight; }

    public boolean isDPadUpJustPressed() { return dpadUp && !prevDpadUp; }
    public boolean isDPadDownJustPressed() { return dpadDown && !prevDpadDown; }
    public boolean isDPadLeftJustPressed() { return dpadLeft && !prevDpadLeft; }
    public boolean isDPadRightJustPressed() { return dpadRight && !prevDpadRight; }

    // ==================== Virtual Mouse ====================

    public int getVirtualMouseX() { return Math.round(virtualMouseX); }
    public int getVirtualMouseY() { return Math.round(virtualMouseY); }

    public void setVirtualMousePosition(int x, int y) {
        virtualMouseX = x;
        virtualMouseY = y;
    }

    public boolean isRightStickActive() {
        return rightStickX != 0 || rightStickY != 0;
    }

    // ==================== Raw Values ====================

    public float getLeftStickX() { return leftStickX; }
    public float getLeftStickY() { return leftStickY; }
    public float getRightStickX() { return rightStickX; }
    public float getRightStickY() { return rightStickY; }
    public float getLeftTrigger() { return leftTrigger; }
    public float getRightTrigger() { return rightTrigger; }

    // ==================== Connection Status ====================

    public boolean isControllerConnected() { return controllerConnected; }
    public boolean isUsingController() { return usingController && controllerConnected; }
    public String getControllerName() { return controllerName; }

    public void setUsingController(boolean using) {
        this.usingController = using;
    }
}
