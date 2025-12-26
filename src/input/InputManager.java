package input;

import java.awt.event.*;
import java.util.HashSet;

public class InputManager implements KeyListener, MouseWheelListener, MouseListener {

    private HashSet<Character> pressed = new HashSet<>();
    private HashSet<Character> justPressed = new HashSet<>();

    // Mouse wheel state: -1 = scroll up, 0 = no scroll, 1 = scroll down
    private int scrollDirection = 0;

    // Mouse button state tracking
    // Button 1 = left click, Button 2 = middle click, Button 3 = right click
    private HashSet<Integer> mouseButtonsPressed = new HashSet<>();
    private HashSet<Integer> mouseButtonsJustPressed = new HashSet<>();
    private int mouseX = 0;
    private int mouseY = 0;

    public boolean isKeyPressed(char c) {
        return pressed.contains(Character.toLowerCase(c));
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
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressed.remove(Character.toLowerCase(e.getKeyChar()));
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Negative rotation = scroll up, Positive = scroll down
        scrollDirection = e.getWheelRotation() < 0 ? -1 : 1;
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
}