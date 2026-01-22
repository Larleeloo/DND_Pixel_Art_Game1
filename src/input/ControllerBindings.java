package input;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages customizable controller button bindings for the game.
 * Allows players to rebind controller buttons to their preferred controls.
 * Bindings are persisted to a file for future sessions.
 */
public class ControllerBindings {

    private static ControllerBindings instance;

    // Controller button identifiers
    public static final String BUTTON_A = "button_a";
    public static final String BUTTON_B = "button_b";
    public static final String BUTTON_X = "button_x";
    public static final String BUTTON_Y = "button_y";
    public static final String BUTTON_LB = "button_lb";
    public static final String BUTTON_RB = "button_rb";
    public static final String BUTTON_START = "button_start";
    public static final String BUTTON_BACK = "button_back";
    public static final String BUTTON_L3 = "button_l3";
    public static final String BUTTON_R3 = "button_r3";
    public static final String RIGHT_TRIGGER = "right_trigger";
    public static final String LEFT_TRIGGER = "left_trigger";
    public static final String DPAD_UP = "dpad_up";
    public static final String DPAD_DOWN = "dpad_down";
    public static final String DPAD_LEFT = "dpad_left";
    public static final String DPAD_RIGHT = "dpad_right";

    // Action names (same as KeyBindings for consistency)
    public static final String ACTION_JUMP = "jump";
    public static final String ACTION_INTERACT = "interact";
    public static final String ACTION_INVENTORY = "inventory";
    public static final String ACTION_MENU = "menu";
    public static final String ACTION_BACK = "back";
    public static final String ACTION_SPRINT = "sprint";
    public static final String ACTION_ATTACK = "attack";
    public static final String ACTION_HOTBAR_PREV = "hotbar_prev";
    public static final String ACTION_HOTBAR_NEXT = "hotbar_next";

    // Default button-to-action mappings
    private static final Map<String, String> DEFAULT_BINDINGS = new HashMap<>();
    static {
        DEFAULT_BINDINGS.put(BUTTON_A, ACTION_JUMP);
        DEFAULT_BINDINGS.put(BUTTON_B, ACTION_BACK);
        DEFAULT_BINDINGS.put(BUTTON_X, ACTION_INTERACT);
        DEFAULT_BINDINGS.put(BUTTON_Y, ACTION_INVENTORY);
        DEFAULT_BINDINGS.put(BUTTON_LB, ACTION_HOTBAR_PREV);
        DEFAULT_BINDINGS.put(BUTTON_RB, ACTION_HOTBAR_NEXT);
        DEFAULT_BINDINGS.put(BUTTON_START, ACTION_MENU);
        DEFAULT_BINDINGS.put(BUTTON_BACK, ACTION_BACK);
        DEFAULT_BINDINGS.put(BUTTON_L3, ACTION_SPRINT);
        DEFAULT_BINDINGS.put(BUTTON_R3, ACTION_ATTACK);
        DEFAULT_BINDINGS.put(RIGHT_TRIGGER, ACTION_ATTACK);
        DEFAULT_BINDINGS.put(LEFT_TRIGGER, ACTION_INTERACT);
    }

    // Current bindings: button -> action
    private Map<String, String> bindings;

    // File path for saving bindings
    private static final String BINDINGS_FILE = "saves/controller_bindings.dat";

    private ControllerBindings() {
        bindings = new HashMap<>(DEFAULT_BINDINGS);
        loadBindings();
    }

    public static ControllerBindings getInstance() {
        if (instance == null) {
            instance = new ControllerBindings();
        }
        return instance;
    }

    /**
     * Get the action bound to a button.
     * @param button The button identifier (use constants like BUTTON_A)
     * @return The action name, or null if not bound
     */
    public String getAction(String button) {
        return bindings.get(button);
    }

    /**
     * Get the button bound to an action.
     * @param action The action to look up
     * @return The button identifier, or null if not bound
     */
    public String getButtonForAction(String action) {
        for (Map.Entry<String, String> entry : bindings.entrySet()) {
            if (action.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get all buttons bound to an action (some actions may have multiple buttons).
     * @param action The action to look up
     * @return Array of button identifiers
     */
    public String[] getButtonsForAction(String action) {
        return bindings.entrySet().stream()
            .filter(e -> action.equals(e.getValue()))
            .map(Map.Entry::getKey)
            .toArray(String[]::new);
    }

    /**
     * Check if a button is bound to a specific action.
     * @param button The button to check
     * @param action The action to check
     * @return true if the button is bound to the action
     */
    public boolean isButtonBoundTo(String button, String action) {
        return action.equals(bindings.get(button));
    }

    /**
     * Set a new action binding for a button.
     * @param button The button to rebind
     * @param action The action to bind to
     */
    public void setBinding(String button, String action) {
        if (DEFAULT_BINDINGS.containsKey(button)) {
            // Find any other button bound to this action and swap
            String existingButton = getButtonForAction(action);
            if (existingButton != null && !existingButton.equals(button)) {
                // Swap: give the old button this button's current action
                String currentAction = bindings.get(button);
                if (currentAction != null) {
                    bindings.put(existingButton, currentAction);
                }
            }
            bindings.put(button, action);
            saveBindings();
        }
    }

    /**
     * Reset a specific button to its default binding.
     * @param button The button to reset
     */
    public void resetToDefault(String button) {
        if (DEFAULT_BINDINGS.containsKey(button)) {
            bindings.put(button, DEFAULT_BINDINGS.get(button));
            saveBindings();
        }
    }

    /**
     * Reset all bindings to defaults.
     */
    public void resetAllToDefaults() {
        bindings = new HashMap<>(DEFAULT_BINDINGS);
        saveBindings();
    }

    /**
     * Get all current bindings.
     * @return A copy of the bindings map
     */
    public Map<String, String> getAllBindings() {
        return new HashMap<>(bindings);
    }

    /**
     * Get the display name for a button.
     * @param button The button identifier
     * @return Human-readable button name
     */
    public static String getButtonDisplayName(String button) {
        switch (button) {
            case BUTTON_A: return "A Button";
            case BUTTON_B: return "B Button";
            case BUTTON_X: return "X Button";
            case BUTTON_Y: return "Y Button";
            case BUTTON_LB: return "LB (Left Bumper)";
            case BUTTON_RB: return "RB (Right Bumper)";
            case BUTTON_START: return "Start";
            case BUTTON_BACK: return "Back/Select";
            case BUTTON_L3: return "L3 (Left Stick Click)";
            case BUTTON_R3: return "R3 (Right Stick Click)";
            case RIGHT_TRIGGER: return "RT (Right Trigger)";
            case LEFT_TRIGGER: return "LT (Left Trigger)";
            case DPAD_UP: return "D-Pad Up";
            case DPAD_DOWN: return "D-Pad Down";
            case DPAD_LEFT: return "D-Pad Left";
            case DPAD_RIGHT: return "D-Pad Right";
            default: return button;
        }
    }

    /**
     * Get a short display name for a button (for compact UI).
     * @param button The button identifier
     * @return Short button name
     */
    public static String getButtonShortName(String button) {
        switch (button) {
            case BUTTON_A: return "A";
            case BUTTON_B: return "B";
            case BUTTON_X: return "X";
            case BUTTON_Y: return "Y";
            case BUTTON_LB: return "LB";
            case BUTTON_RB: return "RB";
            case BUTTON_START: return "Start";
            case BUTTON_BACK: return "Back";
            case BUTTON_L3: return "L3";
            case BUTTON_R3: return "R3";
            case RIGHT_TRIGGER: return "RT";
            case LEFT_TRIGGER: return "LT";
            case DPAD_UP: return "D-Up";
            case DPAD_DOWN: return "D-Down";
            case DPAD_LEFT: return "D-Left";
            case DPAD_RIGHT: return "D-Right";
            default: return button;
        }
    }

    /**
     * Get the display name for an action.
     * @param action The action constant
     * @return Human-readable action name
     */
    public static String getActionDisplayName(String action) {
        switch (action) {
            case ACTION_JUMP: return "Jump";
            case ACTION_INTERACT: return "Interact/Mine";
            case ACTION_INVENTORY: return "Inventory";
            case ACTION_MENU: return "Menu/Settings";
            case ACTION_BACK: return "Back/Cancel";
            case ACTION_SPRINT: return "Sprint";
            case ACTION_ATTACK: return "Attack/Click";
            case ACTION_HOTBAR_PREV: return "Hotbar Previous";
            case ACTION_HOTBAR_NEXT: return "Hotbar Next";
            default: return action;
        }
    }

    /**
     * Get all available actions that can be bound.
     * @return Array of action identifiers
     */
    public static String[] getAllActions() {
        return new String[] {
            ACTION_JUMP, ACTION_INTERACT, ACTION_INVENTORY, ACTION_MENU,
            ACTION_BACK, ACTION_SPRINT, ACTION_ATTACK,
            ACTION_HOTBAR_PREV, ACTION_HOTBAR_NEXT
        };
    }

    /**
     * Get all rebindable buttons.
     * @return Array of button identifiers
     */
    public static String[] getAllButtons() {
        return new String[] {
            BUTTON_A, BUTTON_B, BUTTON_X, BUTTON_Y,
            BUTTON_LB, BUTTON_RB, BUTTON_START, BUTTON_BACK,
            BUTTON_L3, BUTTON_R3, RIGHT_TRIGGER, LEFT_TRIGGER
        };
    }

    /**
     * Save bindings to file.
     */
    private void saveBindings() {
        try {
            File file = new File(BINDINGS_FILE);
            file.getParentFile().mkdirs();

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(bindings);
            }
            System.out.println("ControllerBindings: Saved to " + BINDINGS_FILE);
        } catch (IOException e) {
            System.err.println("ControllerBindings: Failed to save bindings: " + e.getMessage());
        }
    }

    /**
     * Load bindings from file.
     */
    @SuppressWarnings("unchecked")
    private void loadBindings() {
        File file = new File(BINDINGS_FILE);
        if (!file.exists()) {
            System.out.println("ControllerBindings: No saved bindings found, using defaults");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, String> loaded = (Map<String, String>) ois.readObject();

            // Merge with defaults (in case new buttons were added)
            for (String button : DEFAULT_BINDINGS.keySet()) {
                if (loaded.containsKey(button)) {
                    bindings.put(button, loaded.get(button));
                }
            }
            System.out.println("ControllerBindings: Loaded from " + BINDINGS_FILE);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("ControllerBindings: Failed to load bindings, using defaults: " + e.getMessage());
        }
    }
}
