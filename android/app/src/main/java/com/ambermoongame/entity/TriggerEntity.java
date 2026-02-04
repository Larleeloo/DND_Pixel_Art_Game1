package com.ambermoongame.entity;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.ambermoongame.input.TouchInputManager;

/**
 * Invisible trigger zone that fires events when the player enters.
 * Used for level transitions, checkpoints, cutscenes, etc.
 *
 * Conversion notes:
 * - java.awt.Rectangle        -> android.graphics.Rect
 * - java.awt.Color            -> android.graphics.Color (int)
 * - Graphics/Graphics2D       -> Canvas + Paint
 * - Font                      -> Paint.setTextSize()
 * - System.out.println        -> Log.d()
 * - System.getProperty("debug") -> DEBUG constant
 * - SceneManager              -> commented out (pending port)
 */
public class TriggerEntity extends Entity {

    private static final String TAG = "TriggerEntity";
    private static final boolean DEBUG = false;

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
    public static final String TYPE_CUTSCENE = "cutscene";

    // Reusable drawing objects
    private final Paint drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public TriggerEntity(int x, int y, int width, int height, String type, String target) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.triggerType = type;
        this.target = target;
        this.triggered = false;
        this.repeatable = false;
    }

    @Override
    public Rect getBounds() {
        return new Rect(x, y, x + width, y + height);
    }

    @Override
    public void draw(Canvas canvas) {
        // Debug drawing - only show in debug mode
        if (DEBUG) {
            drawPaint.setColor(Color.argb(50, 255, 0, 255));
            canvas.drawRect(x, y, x + width, y + height, drawPaint);

            drawPaint.setColor(Color.argb(150, 255, 0, 255));
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeWidth(1);
            canvas.drawRect(x, y, x + width, y + height, drawPaint);
            drawPaint.setStyle(Paint.Style.FILL);

            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(10);
            canvas.drawText(triggerType + " -> " + target, x + 2, y + 12, textPaint);
        }
    }

    /**
     * Check if a player intersects with this trigger.
     * @param playerBounds The player's bounding box
     * @return true if triggered
     */
    public boolean checkTrigger(Rect playerBounds) {
        if (triggered && !repeatable) {
            return false;
        }

        if (Rect.intersects(getBounds(), playerBounds)) {
            triggered = true;
            return true;
        }
        return false;
    }

    /**
     * Execute the trigger action.
     */
    public void execute() {
        Log.d(TAG, "Executing " + triggerType + " -> " + target);

        switch (triggerType) {
            case TYPE_LEVEL_TRANSITION:
                // --- Uncomment when SceneManager is ported ---
                // if (target == null || target.isEmpty() ||
                //     target.equalsIgnoreCase("menu") ||
                //     target.equalsIgnoreCase("mainMenu")) {
                //     SceneManager.getInstance().setScene("mainMenu", SceneManager.TRANSITION_FADE);
                // } else {
                //     SceneManager.getInstance().loadLevel(target, SceneManager.TRANSITION_FADE);
                // }
                Log.d(TAG, "Level transition: " + target);
                break;

            case TYPE_CHECKPOINT:
                Log.d(TAG, "Checkpoint reached: " + target);
                break;

            case TYPE_EVENT:
                Log.d(TAG, "Event triggered: " + target);
                break;

            case TYPE_CUTSCENE:
                Log.d(TAG, "Cutscene triggered: " + target);
                break;

            default:
                Log.d(TAG, "Unknown trigger type: " + triggerType);
        }
    }

    /** Check if this is a cutscene trigger. */
    public boolean isCutsceneTrigger() {
        return TYPE_CUTSCENE.equals(triggerType);
    }

    public String getTriggerType() { return triggerType; }
    public String getTarget() { return target; }
    public boolean isTriggered() { return triggered; }
    public void reset() { triggered = false; }
    public void setRepeatable(boolean repeatable) { this.repeatable = repeatable; }
}
