import java.awt.event.*;
import java.util.HashSet;

class InputManager implements KeyListener, MouseWheelListener {

    private HashSet<Character> pressed = new HashSet<>();
    private HashSet<Character> justPressed = new HashSet<>();

    // Mouse wheel state: -1 = scroll up, 0 = no scroll, 1 = scroll down
    private int scrollDirection = 0;

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
}