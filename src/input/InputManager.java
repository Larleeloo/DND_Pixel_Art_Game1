package input;

import java.awt.event.*;
import java.util.HashSet;

/**
 * Manages all input for the game including keyboard, mouse, and Xbox controller.
 *
 * Xbox Controller Support:
 * When an Xbox controller is connected, the following mappings are active:
 * - Right Stick (RS): Movement (WASD equivalent)
 *   - RS Right → 'D' key
 *   - RS Left → 'A' key
 *   - RS Up → 'W' key
 *   - RS Down → 'S' key
 * - Left Stick (LS): Mouse pointer control
 *   - LS Move → Virtual mouse cursor movement
 *   - LS Click (L3) → Left mouse click
 * - Buttons:
 *   - A Button → Space (Jump)
 *   - X Button → 'E' key (Interact/Mine)
 *   - Y Button → 'I' key (Inventory)
 *   - Start Button → 'M' key (Menu/Settings)
 *
 * Requires JInput library for controller support.
 */
public class InputManager implements KeyListener, MouseWheelListener, MouseListener, MouseMotionListener {

    private HashSet<Character> pressed = new HashSet<>();
    private HashSet<Character> justPressed = new HashSet<>();

    // Special key tracking (for Shift, Ctrl, Alt, arrow keys, etc.)
    private HashSet<Integer> keysPressed = new HashSet<>();
    private HashSet<Integer> keysJustPressed = new HashSet<>();

    // Mouse wheel state: -1 = scroll up, 0 = no scroll, 1 = scroll down
    private int scrollDirection = 0;

    // Scroll wheel sensitivity - accumulate scroll before triggering action
    private double scrollAccumulator = 0;
    private static final double SCROLL_THRESHOLD = 1.5;  // Units of scroll needed to trigger

    // Mouse button state tracking
    // Button 1 = left click, Button 2 = middle click, Button 3 = right click
    private HashSet<Integer> mouseButtonsPressed = new HashSet<>();
    private HashSet<Integer> mouseButtonsJustPressed = new HashSet<>();
    private int mouseX = 0;
    private int mouseY = 0;

    // UI click consumption - when true, clicks should be ignored by game logic
    private boolean clickConsumedByUI = false;

    // Controller manager for Xbox controller support
    private ControllerManager controllerManager;

    // Track whether controller is actively being used (for mouse position)
    private boolean usingController = false;

    /**
     * Initialize the controller manager. Call this once during game initialization.
     */
    public void initializeController() {
        controllerManager = ControllerManager.getInstance();
    }

    /**
     * Poll the controller for current state.
     * Should be called once per frame from the game loop.
     */
    public void pollController() {
        if (controllerManager != null) {
            controllerManager.poll();

            // Track if controller is being used for mouse position
            if (controllerManager.isLeftStickActive()) {
                usingController = true;
            }
        }
    }

    /**
     * Check if a key is pressed (keyboard or controller equivalent).
     * Controller mappings:
     * - 'a' → RS Left
     * - 'd' → RS Right
     * - 'w' → RS Up
     * - 's' → RS Down
     * - ' ' (space) → A Button
     * - 'e' → X Button
     * - 'i' → Y Button
     * - 'm' → Start Button
     */
    public boolean isKeyPressed(char c) {
        // Check keyboard first
        if (pressed.contains(Character.toLowerCase(c))) {
            return true;
        }

        // Check controller mappings
        if (controllerManager != null && controllerManager.isControllerConnected()) {
            char lower = Character.toLowerCase(c);
            switch (lower) {
                case 'a': return controllerManager.isRightStickLeft();
                case 'd': return controllerManager.isRightStickRight();
                case 'w': return controllerManager.isRightStickUp();
                case 's': return controllerManager.isRightStickDown();
                case ' ': return controllerManager.isButtonAPressed();
                case 'e': return controllerManager.isButtonXPressed();
                case 'i': return controllerManager.isButtonYPressed();
                case 'm': return controllerManager.isButtonStartPressed();
            }
        }

        return false;
    }

    /**
     * Check if a key is pressed by key code (for special keys like Shift, Ctrl, arrows).
     * Use KeyEvent.VK_* constants.
     */
    public boolean isKeyPressed(int keyCode) {
        return keysPressed.contains(keyCode);
    }

    /**
     * Check if a key was just pressed by key code.
     */
    public boolean isKeyJustPressed(int keyCode) {
        if (keysJustPressed.contains(keyCode)) {
            keysJustPressed.remove(keyCode);
            return true;
        }
        return false;
    }

    /**
     * Check if a key was just pressed this frame (keyboard or controller equivalent).
     * Controller mappings:
     * - ' ' (space) → A Button just pressed
     * - 'e' → X Button just pressed
     * - 'i' → Y Button just pressed
     * - 'm' → Start Button just pressed
     */
    public boolean isKeyJustPressed(char c) {
        char lower = Character.toLowerCase(c);

        // Check keyboard first
        if (justPressed.contains(lower)) {
            justPressed.remove(lower);
            return true;
        }

        // Check controller mappings for "just pressed" buttons
        if (controllerManager != null && controllerManager.isControllerConnected()) {
            switch (lower) {
                case ' ': return controllerManager.isButtonAJustPressed();
                case 'e': return controllerManager.isButtonXJustPressed();
                case 'i': return controllerManager.isButtonYJustPressed();
                case 'm': return controllerManager.isButtonStartJustPressed();
            }
        }

        return false;
    }

    /**
     * Gets the scroll wheel direction since last check.
     * Returns -1 for scroll up, 1 for scroll down, 0 for no scroll.
     * Clears the scroll state after reading.
     */
    public int getScrollDirection() {
        int dir = scrollDirection;
        scrollDirection = 0;
        return dir;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        char c = Character.toLowerCase(e.getKeyChar());
        if (!pressed.contains(c)) {
            justPressed.add(c);
        }
        pressed.add(c);

        // Track key codes for special keys (Shift, Ctrl, arrows, etc.)
        int keyCode = e.getKeyCode();
        if (!keysPressed.contains(keyCode)) {
            keysJustPressed.add(keyCode);
        }
        keysPressed.add(keyCode);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressed.remove(Character.toLowerCase(e.getKeyChar()));
        keysPressed.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Accumulate scroll rotation (use precise wheel rotation for smoother control)
        double rotation = e.getPreciseWheelRotation();
        scrollAccumulator += rotation;

        // Only trigger scroll action when threshold is reached
        if (scrollAccumulator >= SCROLL_THRESHOLD) {
            scrollDirection = 1;  // Scroll down
            scrollAccumulator = 0;  // Reset accumulator
        } else if (scrollAccumulator <= -SCROLL_THRESHOLD) {
            scrollDirection = -1;  // Scroll up
            scrollAccumulator = 0;  // Reset accumulator
        }
    }

    // ========== Mouse Button Methods ==========

    /**
     * Check if a mouse button is currently pressed.
     * @param button 1 = left, 2 = middle, 3 = right
     */
    public boolean isMouseButtonPressed(int button) {
        return mouseButtonsPressed.contains(button);
    }

    /**
     * Check if a mouse button was just pressed this frame.
     * @param button 1 = left, 2 = middle, 3 = right
     */
    public boolean isMouseButtonJustPressed(int button) {
        if (mouseButtonsJustPressed.contains(button)) {
            mouseButtonsJustPressed.remove(button);
            return true;
        }
        return false;
    }

    /**
     * Convenience method for left mouse button just pressed.
     * Also returns true if controller left stick was just clicked (L3).
     */
    public boolean isLeftMouseJustPressed() {
        // Check physical mouse
        if (isMouseButtonJustPressed(MouseEvent.BUTTON1)) {
            return true;
        }
        // Check controller left stick click (L3)
        if (controllerManager != null && controllerManager.isControllerConnected()) {
            return controllerManager.isLeftStickJustClicked();
        }
        return false;
    }

    /**
     * Convenience method for right mouse button just pressed.
     */
    public boolean isRightMouseJustPressed() {
        return isMouseButtonJustPressed(MouseEvent.BUTTON3);
    }

    /**
     * Check if left mouse button is currently pressed.
     * Also returns true if controller left stick is clicked (L3).
     */
    public boolean isLeftMousePressed() {
        // Check physical mouse
        if (isMouseButtonPressed(MouseEvent.BUTTON1)) {
            return true;
        }
        // Check controller left stick click (L3)
        if (controllerManager != null && controllerManager.isControllerConnected()) {
            return controllerManager.isLeftStickClicked();
        }
        return false;
    }

    /**
     * Marks the current click as consumed by UI elements.
     * Call this when a UI button or element handles a click to prevent
     * game logic from also responding to it.
     */
    public void consumeClick() {
        clickConsumedByUI = true;
    }

    /**
     * Checks if the current click was consumed by UI elements.
     * @return true if the click was handled by UI and should be ignored
     */
    public boolean isClickConsumedByUI() {
        return clickConsumedByUI;
    }

    /**
     * Resets the click consumed flag. Call at the start of each frame
     * before processing input to clear the previous frame's consumption state.
     */
    public void resetClickConsumed() {
        clickConsumedByUI = false;
    }

    /**
     * Get the current mouse X position.
     * Returns controller virtual mouse position if controller is being used.
     */
    public int getMouseX() {
        if (usingController && controllerManager != null && controllerManager.isControllerConnected()) {
            return controllerManager.getVirtualMouseX();
        }
        return mouseX;
    }

    /**
     * Get the current mouse Y position.
     * Returns controller virtual mouse position if controller is being used.
     */
    public int getMouseY() {
        if (usingController && controllerManager != null && controllerManager.isControllerConnected()) {
            return controllerManager.getVirtualMouseY();
        }
        return mouseY;
    }

    /**
     * Check if the controller's virtual mouse is active.
     */
    public boolean isUsingController() {
        return usingController && controllerManager != null && controllerManager.isControllerConnected();
    }

    /**
     * Get the controller manager for direct access to controller state.
     */
    public ControllerManager getControllerManager() {
        return controllerManager;
    }

    // ========== MouseListener Implementation ==========

    @Override
    public void mousePressed(MouseEvent e) {
        int button = e.getButton();
        if (!mouseButtonsPressed.contains(button)) {
            mouseButtonsJustPressed.add(button);
        }
        mouseButtonsPressed.add(button);
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseButtonsPressed.remove(e.getButton());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Not needed - we handle press/release separately
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Not needed
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Not needed
    }

    // ========== MouseMotionListener Implementation ==========

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        // Switch back to mouse control when mouse moves
        usingController = false;
        // Sync controller virtual mouse with real mouse position
        if (controllerManager != null) {
            controllerManager.setVirtualMousePosition(mouseX, mouseY);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        // Switch back to mouse control when mouse moves
        usingController = false;
        // Sync controller virtual mouse with real mouse position
        if (controllerManager != null) {
            controllerManager.setVirtualMousePosition(mouseX, mouseY);
        }
    }
}