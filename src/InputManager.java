import java.awt.event.*;
import java.util.HashSet;

class InputManager implements KeyListener {

    private HashSet<Character> pressed = new HashSet<>();
    private HashSet<Character> justPressed = new HashSet<>();

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
}