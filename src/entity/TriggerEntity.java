package entity;
import block.*;
import input.*;
import graphics.*;
import animation.*;
import audio.*;

import java.awt.*;

/**
 * Invisible trigger zone that fires events when the player enters.
 * Used for level transitions, checkpoints, cutscenes, etc.
 */
class TriggerEntity extends Entity {

    private int width;
    private int height;
    private String triggerType;
    private String target;
    private boolean triggered;
    private boolean repeatable;

    // Trigger types
    public static final String TYPE_LEVEL_TRANSITION = "level_transition";
    public static final String TYPE_CHECKPOINT = "checkpoint";
    public static final String TYPE_EVENT = "event";

    public TriggerEntity(int x, int y, int width, int height, String type, String target) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.triggerType = type;
        this.target = target;
        this.triggered = false;
        this.repeatable = false; // One-time trigger by default
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void draw(Graphics g) {
        // Debug drawing - only show in debug mode
        if (System.getProperty("debug") != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(255, 0, 255, 50));
            g2d.fillRect(x, y, width, height);
            g2d.setColor(new Color(255, 0, 255, 150));
            g2d.drawRect(x, y, width, height);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString(triggerType + " -> " + target, x + 2, y + 12);
        }
    }

    /**
     * Check if a player intersects with this trigger.
     * @param playerBounds The player's bounding box
     * @return true if triggered
     */
    public boolean checkTrigger(Rectangle playerBounds) {
        if (triggered && !repeatable) {
            return false;
        }

        if (getBounds().intersects(playerBounds)) {
            triggered = true;
            return true;
        }
        return false;
    }

    /**
     * Execute the trigger action.
     */
    public void execute() {
        System.out.println("TriggerEntity: Executing " + triggerType + " -> " + target);

        switch (triggerType) {
            case TYPE_LEVEL_TRANSITION:
                // Load the next level
                SceneManager.getInstance().loadLevel(target, SceneManager.TRANSITION_FADE);
                break;

            case TYPE_CHECKPOINT:
                // Save checkpoint (could store in player or game state)
                System.out.println("Checkpoint reached: " + target);
                break;

            case TYPE_EVENT:
                // Custom event handling
                System.out.println("Event triggered: " + target);
                break;

            default:
                System.out.println("Unknown trigger type: " + triggerType);
        }
    }

    public String getTriggerType() {
        return triggerType;
    }

    public String getTarget() {
        return target;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void reset() {
        triggered = false;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }
}
