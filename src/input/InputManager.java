package input;

import java.awt.event.*;
import java.util.HashSet;

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

    public boolean isKeyPressed(char c) {
        return pressed.contains(Character.toLowerCase(c));
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

    public boolean isKeyJustPressed(char c) {
        char lower = Character.toLowerCase(c);
        if (justPressed.contains(lower)) {
            justPressed.remove(lower);
            return true;
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
     */
    public boolean isLeftMouseJustPressed() {
        return isMouseButtonJustPressed(MouseEvent.BUTTON1);
    }

    /**
     * Convenience method for right mouse button just pressed.
     */
    public boolean isRightMouseJustPressed() {
        return isMouseButtonJustPressed(MouseEvent.BUTTON3);
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
     */
    public int getMouseX() {
        return mouseX;
    }

    /**
     * Get the current mouse Y position.
     */
    public int getMouseY() {
        return mouseY;
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
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
}