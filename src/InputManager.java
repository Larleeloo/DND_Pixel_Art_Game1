import java.awt.event.*;
import java.util.HashSet;

class InputManager implements KeyListener {

    private HashSet<Character> pressed = new HashSet<>();

    public boolean isKeyPressed(char c) {
        return pressed.contains(Character.toLowerCase(c));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("Key pressed: " + e.getKeyChar());
        pressed.add(Character.toLowerCase(e.getKeyChar()));
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressed.remove(Character.toLowerCase(e.getKeyChar()));
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
