package input;

import java.awt.event.KeyEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages customizable key bindings for the game.
 * Allows players to rebind keys to their preferred controls.
 * Bindings are persisted to a file for future sessions.
 */
public class KeyBindings {

    private static KeyBindings instance;

    // Action names used throughout the game
    public static final String MOVE_LEFT = "move_left";
    public static final String MOVE_RIGHT = "move_right";
    public static final String MOVE_UP = "move_up";
    public static final String MOVE_DOWN = "move_down";
    public static final String JUMP = "jump";
    public static final String SPRINT = "sprint";
    public static final String INTERACT = "interact";
    public static final String INVENTORY = "inventory";
    public static final String MENU = "menu";
    public static final String ATTACK = "attack";
    public static final String DEBUG = "debug";

    // Default key bindings
    private static final Map<String, Integer> DEFAULT_BINDINGS = new HashMap<>();
    static {
        DEFAULT_BINDINGS.put(MOVE_LEFT, KeyEvent.VK_A);
        DEFAULT_BINDINGS.put(MOVE_RIGHT, KeyEvent.VK_D);
        DEFAULT_BINDINGS.put(MOVE_UP, KeyEvent.VK_W);
        DEFAULT_BINDINGS.put(MOVE_DOWN, KeyEvent.VK_S);
        DEFAULT_BINDINGS.put(JUMP, KeyEvent.VK_SPACE);
        DEFAULT_BINDINGS.put(SPRINT, KeyEvent.VK_SHIFT);
        DEFAULT_BINDINGS.put(INTERACT, KeyEvent.VK_E);
        DEFAULT_BINDINGS.put(INVENTORY, KeyEvent.VK_I);
        DEFAULT_BINDINGS.put(MENU, KeyEvent.VK_M);
        DEFAULT_BINDINGS.put(ATTACK, KeyEvent.VK_F);
        DEFAULT_BINDINGS.put(DEBUG, KeyEvent.VK_F3);
    }

    // Current key bindings
    private Map<String, Integer> bindings;

    // File path for saving bindings
    private static final String BINDINGS_FILE = "saves/keybindings.dat";

    private KeyBindings() {
        bindings = new HashMap<>(DEFAULT_BINDINGS);
        loadBindings();
    }

    public static KeyBindings getInstance() {
        if (instance == null) {
            instance = new KeyBindings();
        }
        return instance;
    }

    /**
     * Get the key code for an action.
     * @param action The action name (use constants like MOVE_LEFT)
     * @return The key code, or -1 if action not found
     */
    public int getKey(String action) {
        return bindings.getOrDefault(action, -1);
    }

    /**
     * Get the key code for an action as a character (for letter keys).
     * @param action The action name
     * @return The character for the key, or '\0' if not a letter key
     */
    public char getKeyChar(String action) {
        int keyCode = getKey(action);
        if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            return Character.toLowerCase((char) keyCode);
        }
        return '\0';
    }

    /**
     * Set a new key binding for an action.
     * @param action The action to rebind
     * @param keyCode The new key code
     */
    public void setKey(String action, int keyCode) {
        if (DEFAULT_BINDINGS.containsKey(action)) {
            // Remove the key from any other action that uses it
            String existingAction = getActionForKey(keyCode);
            if (existingAction != null && !existingAction.equals(action)) {
                // Swap: give the old action this action's current key
                int currentKey = bindings.get(action);
                bindings.put(existingAction, currentKey);
            }
            bindings.put(action, keyCode);
            saveBindings();
        }
    }

    /**
     * Reset a specific action to its default binding.
     * @param action The action to reset
     */
    public void resetToDefault(String action) {
        if (DEFAULT_BINDINGS.containsKey(action)) {
            bindings.put(action, DEFAULT_BINDINGS.get(action));
            saveBindings();
        }
    }

    /**
     * Reset all key bindings to defaults.
     */
    public void resetAllToDefaults() {
        bindings = new HashMap<>(DEFAULT_BINDINGS);
        saveBindings();
    }

    /**
     * Get the action name bound to a specific key.
     * @param keyCode The key code to check
     * @return The action name, or null if no action is bound
     */
    public String getActionForKey(int keyCode) {
        for (Map.Entry<String, Integer> entry : bindings.entrySet()) {
            if (entry.getValue() == keyCode) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get all current bindings.
     * @return A copy of the bindings map
     */
    public Map<String, Integer> getAllBindings() {
        return new HashMap<>(bindings);
    }

    /**
     * Get the display name for a key code.
     * @param keyCode The key code
     * @return Human-readable key name
     */
    public static String getKeyName(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_SPACE: return "Space";
            case KeyEvent.VK_SHIFT: return "Shift";
            case KeyEvent.VK_CONTROL: return "Ctrl";
            case KeyEvent.VK_ALT: return "Alt";
            case KeyEvent.VK_ENTER: return "Enter";
            case KeyEvent.VK_ESCAPE: return "Esc";
            case KeyEvent.VK_TAB: return "Tab";
            case KeyEvent.VK_BACK_SPACE: return "Backspace";
            case KeyEvent.VK_UP: return "Up";
            case KeyEvent.VK_DOWN: return "Down";
            case KeyEvent.VK_LEFT: return "Left";
            case KeyEvent.VK_RIGHT: return "Right";
            case KeyEvent.VK_F1: return "F1";
            case KeyEvent.VK_F2: return "F2";
            case KeyEvent.VK_F3: return "F3";
            case KeyEvent.VK_F4: return "F4";
            case KeyEvent.VK_F5: return "F5";
            case KeyEvent.VK_F6: return "F6";
            case KeyEvent.VK_F7: return "F7";
            case KeyEvent.VK_F8: return "F8";
            case KeyEvent.VK_F9: return "F9";
            case KeyEvent.VK_F10: return "F10";
            case KeyEvent.VK_F11: return "F11";
            case KeyEvent.VK_F12: return "F12";
            default:
                if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
                    return String.valueOf((char) keyCode);
                }
                if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) {
                    return String.valueOf((char) keyCode);
                }
                return KeyEvent.getKeyText(keyCode);
        }
    }

    /**
     * Get the display name for an action.
     * @param action The action constant
     * @return Human-readable action name
     */
    public static String getActionDisplayName(String action) {
        switch (action) {
            case MOVE_LEFT: return "Move Left";
            case MOVE_RIGHT: return "Move Right";
            case MOVE_UP: return "Move Up";
            case MOVE_DOWN: return "Move Down";
            case JUMP: return "Jump";
            case SPRINT: return "Sprint";
            case INTERACT: return "Interact";
            case INVENTORY: return "Inventory";
            case MENU: return "Menu";
            case ATTACK: return "Attack/Equip";
            case DEBUG: return "Debug Mode";
            default: return action;
        }
    }

    /**
     * Check if a key is bound to a specific action.
     * @param action The action to check
     * @param keyCode The key code to check
     * @return true if the key is bound to this action
     */
    public boolean isKeyBoundTo(String action, int keyCode) {
        return bindings.getOrDefault(action, -1) == keyCode;
    }

    /**
     * Check if the given key/char matches the binding for an action.
     * Handles both character keys and special keys.
     */
    public boolean matchesBinding(String action, char keyChar, int keyCode) {
        int boundKey = getKey(action);
        if (boundKey == -1) return false;

        // Check key code first (works for all keys)
        if (keyCode == boundKey) return true;

        // For letter keys, also check the character
        if (boundKey >= KeyEvent.VK_A && boundKey <= KeyEvent.VK_Z) {
            char boundChar = Character.toLowerCase((char) boundKey);
            return Character.toLowerCase(keyChar) == boundChar;
        }

        return false;
    }

    /**
     * Save bindings to file.
     */
    private void saveBindings() {
        try {
            // Ensure directory exists
            File file = new File(BINDINGS_FILE);
            file.getParentFile().mkdirs();

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(bindings);
            }
            System.out.println("KeyBindings: Saved to " + BINDINGS_FILE);
        } catch (IOException e) {
            System.err.println("KeyBindings: Failed to save bindings: " + e.getMessage());
        }
    }

    /**
     * Load bindings from file.
     */
    @SuppressWarnings("unchecked")
    private void loadBindings() {
        File file = new File(BINDINGS_FILE);
        if (!file.exists()) {
            System.out.println("KeyBindings: No saved bindings found, using defaults");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, Integer> loaded = (Map<String, Integer>) ois.readObject();

            // Merge with defaults (in case new actions were added)
            for (String action : DEFAULT_BINDINGS.keySet()) {
                if (loaded.containsKey(action)) {
                    bindings.put(action, loaded.get(action));
                }
            }
            System.out.println("KeyBindings: Loaded from " + BINDINGS_FILE);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("KeyBindings: Failed to load bindings, using defaults: " + e.getMessage());
        }
    }
}
