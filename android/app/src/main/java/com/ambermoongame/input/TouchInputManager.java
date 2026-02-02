package com.ambermoongame.input;

import android.view.MotionEvent;

import com.ambermoongame.ui.TouchControlOverlay;

/**
 * Manages touch input for the Android port.
 * Translates touch events into game actions matching the desktop keyboard controls.
 *
 * Touch Control Mapping (matches desktop KeyBindings):
 * - Virtual D-Pad: Movement (A/D/W/S keys)
 * - Jump Button: Space key
 * - Attack Button: Left mouse click / F key
 * - Interact Button: E key
 * - Sprint Button (hold): Shift key
 * - Inventory Button: I key
 * - Menu Button: M key
 * - Screen Tap: Mouse position + click
 * - Screen Drag: Aiming direction
 */
public class TouchInputManager {

    // Screen dimensions
    private int screenWidth;
    private int screenHeight;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float offsetX = 0;
    private float offsetY = 0;

    // Target game resolution
    private final int targetWidth;
    private final int targetHeight;

    // Touch state tracking
    private float touchX = 0;
    private float touchY = 0;
    private float gameX = 0;  // Converted to game coordinates
    private float gameY = 0;

    // Multi-touch tracking (up to 10 pointers)
    private static final int MAX_POINTERS = 10;
    private float[] pointerX = new float[MAX_POINTERS];
    private float[] pointerY = new float[MAX_POINTERS];
    private boolean[] pointerDown = new boolean[MAX_POINTERS];

    // Virtual button states (match desktop key bindings)
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean moveUp = false;
    private boolean moveDown = false;
    private boolean jump = false;
    private boolean jumpJustPressed = false;
    private boolean sprint = false;
    private boolean interact = false;
    private boolean interactJustPressed = false;
    private boolean attack = false;
    private boolean attackJustPressed = false;
    private boolean inventory = false;
    private boolean inventoryJustPressed = false;
    private boolean menu = false;
    private boolean menuJustPressed = false;

    // Screen touch state (for aiming/clicking)
    private boolean screenTouched = false;
    private boolean screenJustTouched = false;
    private boolean screenJustReleased = false;

    // Previous frame states for "just pressed" detection
    private boolean prevJump = false;
    private boolean prevInteract = false;
    private boolean prevAttack = false;
    private boolean prevInventory = false;
    private boolean prevMenu = false;
    private boolean prevScreenTouched = false;

    // UI click consumption
    private boolean clickConsumedByUI = false;

    // Touch control overlay reference
    private TouchControlOverlay touchControlOverlay;

    // Scroll/zoom state
    private float pinchStartDistance = 0;
    private boolean isPinching = false;
    private int scrollDirection = 0;
    private int zoomScrollDirection = 0;

    public TouchInputManager(int targetWidth, int targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.screenWidth = targetWidth;
        this.screenHeight = targetHeight;
    }

    /**
     * Set the touch control overlay for button state updates.
     */
    public void setTouchControlOverlay(TouchControlOverlay overlay) {
        this.touchControlOverlay = overlay;
    }

    /**
     * Update screen dimensions and scaling factors.
     */
    public void setScreenDimensions(int width, int height, float scaleX, float scaleY,
                                    float offsetX, float offsetY) {
        this.screenWidth = width;
        this.screenHeight = height;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /**
     * Reset frame state for new update cycle.
     */
    public void resetFrame() {
        // Store previous states
        prevJump = jump;
        prevInteract = interact;
        prevAttack = attack;
        prevInventory = inventory;
        prevMenu = menu;
        prevScreenTouched = screenTouched;

        // Reset "just pressed" flags
        jumpJustPressed = false;
        interactJustPressed = false;
        attackJustPressed = false;
        inventoryJustPressed = false;
        menuJustPressed = false;
        screenJustTouched = false;
        screenJustReleased = false;

        // Reset scroll directions
        scrollDirection = 0;
        zoomScrollDirection = 0;

        // Reset UI consumption
        clickConsumedByUI = false;
    }

    /**
     * Handle touch events from the activity.
     */
    public boolean handleTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        // Update all pointer positions
        for (int i = 0; i < event.getPointerCount(); i++) {
            int id = event.getPointerId(i);
            if (id < MAX_POINTERS) {
                pointerX[id] = event.getX(i);
                pointerY[id] = event.getY(i);
            }
        }

        // Handle primary touch position
        touchX = event.getX();
        touchY = event.getY();
        gameX = screenToGameX(touchX);
        gameY = screenToGameY(touchY);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (pointerId < MAX_POINTERS) {
                    pointerDown[pointerId] = true;
                }
                handleTouchDown(event.getX(pointerIndex), event.getY(pointerIndex), pointerId);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (pointerId < MAX_POINTERS) {
                    pointerDown[pointerId] = false;
                }
                handleTouchUp(event.getX(pointerIndex), event.getY(pointerIndex), pointerId);
                break;

            case MotionEvent.ACTION_MOVE:
                handleTouchMove(event);
                break;

            case MotionEvent.ACTION_CANCEL:
                clearAllTouches();
                break;
        }

        // Handle pinch zoom
        handlePinchZoom(event);

        return true;
    }

    private void handleTouchDown(float x, float y, int pointerId) {
        // First check if touch overlay handled it
        if (touchControlOverlay != null && touchControlOverlay.handleTouchDown(x, y, pointerId)) {
            return;
        }

        // Otherwise treat as screen touch (for aiming/interaction)
        if (!screenTouched) {
            screenJustTouched = true;
        }
        screenTouched = true;
    }

    private void handleTouchUp(float x, float y, int pointerId) {
        // Notify touch overlay
        if (touchControlOverlay != null) {
            touchControlOverlay.handleTouchUp(x, y, pointerId);
        }

        // Check if any pointers are still down
        boolean anyDown = false;
        for (boolean down : pointerDown) {
            if (down) {
                anyDown = true;
                break;
            }
        }

        if (!anyDown) {
            screenJustReleased = screenTouched;
            screenTouched = false;
        }
    }

    private void handleTouchMove(MotionEvent event) {
        // Update touch overlay (for d-pad dragging)
        if (touchControlOverlay != null) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                int id = event.getPointerId(i);
                touchControlOverlay.handleTouchMove(event.getX(i), event.getY(i), id);
            }
        }
    }

    private void handlePinchZoom(MotionEvent event) {
        if (event.getPointerCount() >= 2) {
            float x1 = event.getX(0);
            float y1 = event.getY(0);
            float x2 = event.getX(1);
            float y2 = event.getY(1);
            float distance = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

            if (!isPinching) {
                isPinching = true;
                pinchStartDistance = distance;
            } else {
                float delta = distance - pinchStartDistance;
                if (Math.abs(delta) > 50) { // Threshold
                    zoomScrollDirection = delta > 0 ? -1 : 1; // Pinch out = zoom in = -1
                    pinchStartDistance = distance;
                }
            }
        } else {
            isPinching = false;
        }
    }

    private void clearAllTouches() {
        for (int i = 0; i < MAX_POINTERS; i++) {
            pointerDown[i] = false;
        }
        screenTouched = false;
        moveLeft = false;
        moveRight = false;
        moveUp = false;
        moveDown = false;
        jump = false;
        sprint = false;
        interact = false;
        attack = false;
        inventory = false;
        menu = false;
    }

    // ==================== Button State Setters (called by TouchControlOverlay) ====================

    public void setMoveLeft(boolean pressed) { this.moveLeft = pressed; }
    public void setMoveRight(boolean pressed) { this.moveRight = pressed; }
    public void setMoveUp(boolean pressed) { this.moveUp = pressed; }
    public void setMoveDown(boolean pressed) { this.moveDown = pressed; }

    public void setJump(boolean pressed) {
        if (pressed && !this.jump) jumpJustPressed = true;
        this.jump = pressed;
    }

    public void setSprint(boolean pressed) { this.sprint = pressed; }

    public void setInteract(boolean pressed) {
        if (pressed && !this.interact) interactJustPressed = true;
        this.interact = pressed;
    }

    public void setAttack(boolean pressed) {
        if (pressed && !this.attack) attackJustPressed = true;
        this.attack = pressed;
    }

    public void setInventory(boolean pressed) {
        if (pressed && !this.inventory) inventoryJustPressed = true;
        this.inventory = pressed;
    }

    public void setMenu(boolean pressed) {
        if (pressed && !this.menu) menuJustPressed = true;
        this.menu = pressed;
    }

    // ==================== Key State Methods (match desktop InputManager API) ====================

    /**
     * Check if a key is pressed (by character).
     * Maps virtual buttons to key characters.
     */
    public boolean isKeyPressed(char c) {
        char lower = Character.toLowerCase(c);
        switch (lower) {
            case 'a': return moveLeft;
            case 'd': return moveRight;
            case 'w': return moveUp;
            case 's': return moveDown;
            case ' ': return jump;
            case 'e': return interact;
            case 'i': return inventory;
            case 'm': return menu;
            case 'f': return attack;
            default: return false;
        }
    }

    /**
     * Check if a key was just pressed (by character).
     */
    public boolean isKeyJustPressed(char c) {
        char lower = Character.toLowerCase(c);
        switch (lower) {
            case ' ': return jumpJustPressed;
            case 'e': return interactJustPressed;
            case 'i': return inventoryJustPressed;
            case 'm': return menuJustPressed;
            case 'f': return attackJustPressed;
            default: return false;
        }
    }

    /**
     * Check if a key is pressed (by key code).
     * Uses Android KeyEvent codes mapped to game actions.
     */
    public boolean isKeyPressed(int keyCode) {
        // Map common key codes
        switch (keyCode) {
            case 32: // VK_SPACE equivalent
                return jump;
            case 16: // VK_SHIFT equivalent
                return sprint;
            case 27: // VK_ESCAPE equivalent
                return false; // Handle via back button
            default:
                return false;
        }
    }

    public boolean isKeyJustPressed(int keyCode) {
        switch (keyCode) {
            case 32: return jumpJustPressed;
            default: return false;
        }
    }

    // ==================== Mouse/Touch State Methods ====================

    public boolean isLeftMousePressed() {
        return attack || screenTouched;
    }

    public boolean isLeftMouseJustPressed() {
        return attackJustPressed || screenJustTouched;
    }

    public boolean isRightMouseJustPressed() {
        return false; // No right-click equivalent on touch
    }

    public int getMouseX() {
        return Math.round(gameX);
    }

    public int getMouseY() {
        return Math.round(gameY);
    }

    // ==================== Scroll/Zoom Methods ====================

    public int getScrollDirection() {
        int dir = scrollDirection;
        scrollDirection = 0;
        return dir;
    }

    public int getZoomScrollDirection() {
        int dir = zoomScrollDirection;
        zoomScrollDirection = 0;
        return dir;
    }

    // ==================== Navigation Methods (for inventory) ====================

    public boolean isNavigateUpJustPressed() { return false; } // Use touch/d-pad
    public boolean isNavigateDownJustPressed() { return false; }
    public boolean isNavigateLeftJustPressed() { return false; }
    public boolean isNavigateRightJustPressed() { return false; }
    public boolean isSelectJustPressed() { return screenJustTouched; }

    // ==================== Hotbar Methods ====================

    public boolean isHotbarPreviousPressed() { return false; } // Use swipe or buttons
    public boolean isHotbarNextPressed() { return false; }

    // ==================== UI Click Consumption ====================

    public void consumeClick() {
        clickConsumedByUI = true;
    }

    public boolean isClickConsumedByUI() {
        return clickConsumedByUI;
    }

    public void resetClickConsumed() {
        clickConsumedByUI = false;
    }

    // ==================== Controller Compatibility ====================

    public boolean isUsingController() {
        return false; // Touch input, not controller
    }

    public boolean isControllerClickJustPressed() { return false; }
    public boolean isControllerClickHeld() { return false; }
    public boolean isControllerClickJustReleased() { return false; }

    // ==================== Coordinate Conversion ====================

    private float screenToGameX(float screenX) {
        return (screenX - offsetX) / scaleX;
    }

    private float screenToGameY(float screenY) {
        return (screenY - offsetY) / scaleY;
    }

    // ==================== Utility ====================

    public int getTouchCount() {
        int count = 0;
        for (boolean down : pointerDown) {
            if (down) count++;
        }
        return count;
    }

    public boolean isScreenTouched() {
        return screenTouched;
    }

    public boolean isScreenJustTouched() {
        return screenJustTouched;
    }

    public boolean isScreenJustReleased() {
        return screenJustReleased;
    }
}
