package com.ambermoongame.entity.item;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.ambermoongame.entity.Entity;
import com.ambermoongame.graphics.AndroidAssetLoader;
import com.ambermoongame.input.TouchInputManager;

import java.util.ArrayList;
import java.util.List;

/**
 * ButtonEntity represents an interactive button/switch that can trigger doors and actions.
 *
 * Conversion notes:
 * - AnimatedTexture              -> Manual frame management via ImageAsset.frames
 * - java.awt.Color               -> android.graphics.Color (int)
 * - Graphics/Graphics2D          -> Canvas + Paint
 * - BufferedImage                 -> Bitmap
 * - Rectangle                    -> Rect
 * - BasicStroke                  -> Paint.setStrokeWidth()
 * - Font/FontMetrics             -> Paint.setTextSize()/measureText()
 * - InputManager                 -> TouchInputManager
 * - System.out/err.println       -> Log.d()/Log.e()
 * - System.getProperty("debug")  -> BuildConfig.DEBUG or false
 *
 * Features:
 * - GIF texture animation when pressed
 * - Stays on first frame when idle (unpressed)
 * - Can be linked to DoorEntities via linkIds
 * - Supports player and mob activation
 * - Multiple button types: toggle, momentary (pressure plate), one-shot, timed
 */
public class ButtonEntity extends Entity {

    private static final String TAG = "ButtonEntity";
    private static final boolean DEBUG = false;

    // Button type constants (replaces enum to avoid D8 crash)
    public static final int BUTTON_TYPE_TOGGLE = 0;
    public static final int BUTTON_TYPE_MOMENTARY = 1;
    public static final int BUTTON_TYPE_ONE_SHOT = 2;
    public static final int BUTTON_TYPE_TIMED = 3;

    // Button state constants
    public static final int BUTTON_STATE_IDLE = 0;
    public static final int BUTTON_STATE_PRESSING = 1;
    public static final int BUTTON_STATE_PRESSED = 2;
    public static final int BUTTON_STATE_RELEASING = 3;
    public static final int BUTTON_STATE_DISABLED = 4;

    // Action type constants
    public static final int ACTION_TYPE_NONE = 0;
    public static final int ACTION_TYPE_LEVEL_TRANSITION = 1;
    public static final int ACTION_TYPE_EVENT = 2;
    public static final int ACTION_TYPE_SPAWN_ENTITY = 3;
    public static final int ACTION_TYPE_PLAY_SOUND = 4;

    private static String getButtonTypeName(int type) {
        switch (type) {
            case BUTTON_TYPE_TOGGLE: return "TOGGLE";
            case BUTTON_TYPE_MOMENTARY: return "MOMENTARY";
            case BUTTON_TYPE_ONE_SHOT: return "ONE_SHOT";
            case BUTTON_TYPE_TIMED: return "TIMED";
            default: return "UNKNOWN";
        }
    }

    private static String getStateName(int state) {
        switch (state) {
            case BUTTON_STATE_IDLE: return "IDLE";
            case BUTTON_STATE_PRESSING: return "PRESSING";
            case BUTTON_STATE_PRESSED: return "PRESSED";
            case BUTTON_STATE_RELEASING: return "RELEASING";
            case BUTTON_STATE_DISABLED: return "DISABLED";
            default: return "UNKNOWN";
        }
    }

    private static String getActionTypeName(int type) {
        switch (type) {
            case ACTION_TYPE_NONE: return "NONE";
            case ACTION_TYPE_LEVEL_TRANSITION: return "LEVEL_TRANSITION";
            case ACTION_TYPE_EVENT: return "EVENT";
            case ACTION_TYPE_SPAWN_ENTITY: return "SPAWN_ENTITY";
            case ACTION_TYPE_PLAY_SOUND: return "PLAY_SOUND";
            default: return "UNKNOWN";
        }
    }

    // Dimensions
    private int width;
    private int height;

    // State
    private int state;
    private int buttonType;
    private boolean activated;
    private long activationTime;
    private int timedDuration;

    // Frame-based animation (replaces AnimatedTexture)
    private List<Bitmap> frames;
    private List<Integer> frameDelays;
    private int currentFrameIndex = 0;
    private float animationProgress;
    private float animationSpeed;
    private boolean playingAnimation;
    private long lastUpdateTime;

    // Linking system
    private String linkId;
    private List<String> linkedDoorIds;
    private List<String> linkedButtonIds;

    // Activation settings
    private boolean activatedByPlayer;
    private boolean activatedByMobs;
    private boolean requiresInteraction;

    // Actions
    private int actionType;
    private String actionTarget;

    // Visual effects
    private int glowColor;
    private float glowIntensity;
    private boolean showGlow;

    // Interaction
    private Rect activationZone;
    private boolean entityOnButton;
    private int entitiesOnButton;
    private boolean playerNearby;

    // Sound flags
    private boolean playPressSound;
    private boolean playReleaseSound;

    // Reusable drawing objects
    private final Paint drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    /**
     * Creates a new button entity.
     */
    public ButtonEntity(int x, int y, int width, int height, String texturePath) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.state = BUTTON_STATE_IDLE;
        this.buttonType = BUTTON_TYPE_TOGGLE;
        this.activated = false;
        this.activationTime = 0;
        this.timedDuration = 3000;
        this.animationProgress = 0;
        this.animationSpeed = 0.1f;
        this.playingAnimation = false;
        this.lastUpdateTime = System.currentTimeMillis();
        this.linkId = "";
        this.linkedDoorIds = new ArrayList<>();
        this.linkedButtonIds = new ArrayList<>();
        this.activatedByPlayer = true;
        this.activatedByMobs = true;
        this.requiresInteraction = true;
        this.actionType = ACTION_TYPE_NONE;
        this.actionTarget = "";
        this.glowColor = Color.rgb(100, 200, 255);
        this.glowIntensity = 0;
        this.showGlow = false;
        this.activationZone = new Rect(x - 8, y - 8, x + width + 8, y + height + 8);
        this.entityOnButton = false;
        this.entitiesOnButton = 0;
        this.playerNearby = false;
        this.playPressSound = false;
        this.playReleaseSound = false;

        loadTexture(texturePath);
    }

    /** Creates a button with a link ID. */
    public ButtonEntity(int x, int y, int width, int height, String texturePath, String linkId) {
        this(x, y, width, height, texturePath);
        this.linkId = linkId;
    }

    /**
     * Load the button texture from file.
     * Extracts frames from ImageAsset for manual frame control.
     */
    private void loadTexture(String path) {
        AndroidAssetLoader.ImageAsset asset = AndroidAssetLoader.load(path);
        if (asset != null) {
            if (asset.isAnimated && asset.frames != null && !asset.frames.isEmpty()) {
                frames = new ArrayList<>(asset.frames);
                frameDelays = new ArrayList<>(asset.delays);
            } else if (asset.bitmap != null) {
                frames = new ArrayList<>();
                frames.add(asset.bitmap);
                frameDelays = new ArrayList<>();
                frameDelays.add(100);
            }
        }

        if (frames != null && !frames.isEmpty()) {
            currentFrameIndex = 0;
            Log.d(TAG, "Loaded texture " + path + " (" + frames.size() + " frames)");
        } else {
            Log.e(TAG, "Failed to load texture " + path);
            createFallbackTexture();
        }
    }

    /**
     * Creates a fallback texture when no GIF is available.
     */
    private void createFallbackTexture() {
        Bitmap fallback = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(fallback);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Simple button shape
        p.setColor(Color.rgb(100, 100, 100));
        RectF buttonRect = new RectF(0, 0, width, height);
        c.drawRoundRect(buttonRect, 6, 6, p);

        // Highlight
        p.setColor(Color.rgb(150, 150, 150));
        RectF hlRect = new RectF(2, 2, width - 2, height / 2f);
        c.drawRoundRect(hlRect, 4, 4, p);

        frames = new ArrayList<>();
        frames.add(fallback);
        frameDelays = new ArrayList<>();
        frameDelays.add(100);
    }

    @Override
    public Rect getBounds() {
        return new Rect(x, y, x + width, y + height);
    }

    @Override
    public void update(TouchInputManager input) {
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        playPressSound = false;
        playReleaseSound = false;

        if (playingAnimation) {
            updateAnimation(deltaMs);
        }

        // Handle timed button auto-release
        if (buttonType == BUTTON_TYPE_TIMED && activated) {
            if (currentTime - activationTime >= timedDuration) {
                release();
            }
        }

        // Handle momentary button (pressure plate) behavior
        if (buttonType == BUTTON_TYPE_MOMENTARY && !requiresInteraction) {
            if (!entityOnButton && activated) {
                release();
            }
            entityOnButton = false;
            entitiesOnButton = 0;
        }

        updateGlow(deltaMs);
    }

    /**
     * Updates the button press/release animation using manual frame control.
     */
    private void updateAnimation(long deltaMs) {
        if (frames == null || frames.size() <= 1) {
            playingAnimation = false;
            return;
        }

        int frameCount = frames.size();

        if (state == BUTTON_STATE_PRESSING) {
            animationProgress += animationSpeed;
            if (animationProgress >= 1.0f) {
                animationProgress = 1.0f;
                state = BUTTON_STATE_PRESSED;
                playingAnimation = false;
                currentFrameIndex = frameCount - 1;
            } else {
                currentFrameIndex = Math.min((int)(animationProgress * frameCount), frameCount - 1);
            }
        } else if (state == BUTTON_STATE_RELEASING) {
            animationProgress -= animationSpeed;
            if (animationProgress <= 0) {
                animationProgress = 0;
                state = BUTTON_STATE_IDLE;
                playingAnimation = false;
                currentFrameIndex = 0;
            } else {
                currentFrameIndex = Math.max((int)(animationProgress * frameCount), 0);
            }
        }
    }

    private void updateGlow(long deltaMs) {
        if (showGlow || activated) {
            glowIntensity = Math.min(glowIntensity + 0.05f, 0.6f);
        } else {
            glowIntensity = Math.max(glowIntensity - 0.03f, 0);
        }
    }

    /**
     * Attempts to activate the button.
     * @param isPlayer True if activated by player, false if by mob
     * @return True if successfully activated
     */
    public boolean activate(boolean isPlayer) {
        if (state == BUTTON_STATE_DISABLED) return false;
        if (isPlayer && !activatedByPlayer) return false;
        if (!isPlayer && !activatedByMobs) return false;
        return press();
    }

    /**
     * Presses the button (called by player interaction or entity collision).
     */
    public boolean press() {
        if (state == BUTTON_STATE_DISABLED) return false;

        switch (buttonType) {
            case TOGGLE:
                if (activated) {
                    return release();
                } else {
                    return activateButton();
                }

            case MOMENTARY:
                if (!activated) {
                    return activateButton();
                }
                return false;

            case ONE_SHOT:
                if (!activated && state != BUTTON_STATE_DISABLED) {
                    boolean result = activateButton();
                    if (result) {
                        state = BUTTON_STATE_DISABLED;
                    }
                    return result;
                }
                return false;

            case TIMED:
                if (!activated) {
                    return activateButton();
                }
                return false;

            default:
                return false;
        }
    }

    private boolean activateButton() {
        if (activated && buttonType != BUTTON_TYPE_MOMENTARY) return false;

        activated = true;
        activationTime = System.currentTimeMillis();
        state = BUTTON_STATE_PRESSING;
        playingAnimation = true;
        playPressSound = true;
        Log.d(TAG, "Button " + linkId + " pressed");
        return true;
    }

    /** Releases the button (for toggle/momentary types). */
    public boolean release() {
        if (!activated) return false;

        activated = false;
        state = BUTTON_STATE_RELEASING;
        playingAnimation = true;
        playReleaseSound = true;
        Log.d(TAG, "Button " + linkId + " released");
        return true;
    }

    /** Called when an entity is standing on this button (for pressure plates). */
    public void onEntityEnter(boolean isPlayer) {
        entityOnButton = true;
        entitiesOnButton++;
        if (!requiresInteraction) {
            activate(isPlayer);
        }
    }

    /** Called when an entity leaves this button (for pressure plates). */
    public void onEntityExit() {
        entitiesOnButton = Math.max(0, entitiesOnButton - 1);
        if (entitiesOnButton == 0) {
            entityOnButton = false;
        }
    }

    /** Checks if an entity's bounds intersect with the activation zone. */
    public boolean isInActivationZone(Rect entityBounds) {
        return Rect.intersects(activationZone, entityBounds);
    }

    /** Checks if an entity is standing directly on the button (for pressure plates). */
    public boolean isEntityOnButton(Rect entityBounds) {
        Rect buttonTop = new Rect(x, y - 8, x + width, y + 8);
        return Rect.intersects(buttonTop, entityBounds);
    }

    @Override
    public void draw(Canvas canvas) {
        // Draw glow effect
        if (glowIntensity > 0) {
            int glowSize = (int)(10 + glowIntensity * 15);
            int r = Color.red(glowColor);
            int g = Color.green(glowColor);
            int b = Color.blue(glowColor);

            for (int i = 0; i < 3; i++) {
                float alpha = (0.15f - i * 0.04f) * glowIntensity;
                int a = Math.max(0, Math.min(255, (int)(alpha * 255)));
                int size = glowSize + i * 8;
                drawPaint.setColor(Color.argb(a, r, g, b));
                rectF.set(x + width / 2f - size / 2f, y + height / 2f - size / 2f,
                          x + width / 2f + size / 2f, y + height / 2f + size / 2f);
                canvas.drawOval(rectF, drawPaint);
            }
        }

        // Draw the button texture frame
        if (frames != null && !frames.isEmpty()) {
            int idx = Math.max(0, Math.min(currentFrameIndex, frames.size() - 1));
            Bitmap frame = frames.get(idx);
            if (frame != null) {
                srcRect.set(0, 0, frame.getWidth(), frame.getHeight());
                dstRect.set(x, y, x + width, y + height);
                canvas.drawBitmap(frame, srcRect, dstRect, null);
            }
        } else {
            drawFallback(canvas);
        }

        // Draw state indicator for disabled buttons
        if (state == BUTTON_STATE_DISABLED) {
            drawPaint.setColor(Color.argb(100, 0, 0, 0));
            canvas.drawRect(x, y, x + width, y + height, drawPaint);
        }

        // Draw interaction prompt if player is nearby
        if (playerNearby && requiresInteraction && state != BUTTON_STATE_DISABLED) {
            drawInteractionPrompt(canvas);
        }

        if (DEBUG) {
            drawDebug(canvas);
        }
    }

    private void drawInteractionPrompt(Canvas canvas) {
        String prompt = "Tap";
        textPaint.setTextSize(12);
        textPaint.setFakeBoldText(true);
        float textWidth = textPaint.measureText(prompt);

        float promptX = x + width / 2f - textWidth / 2f;
        float promptY = y - 20;
        float textHeight = 14;

        // Background
        drawPaint.setColor(Color.argb(180, 0, 0, 0));
        RectF bgRect = new RectF(promptX - 6, promptY - textHeight + 2,
            promptX + textWidth + 6, promptY + 6);
        canvas.drawRoundRect(bgRect, 8, 8, drawPaint);

        // Border
        drawPaint.setColor(Color.rgb(255, 200, 100));
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(1);
        canvas.drawRoundRect(bgRect, 8, 8, drawPaint);
        drawPaint.setStyle(Paint.Style.FILL);

        // Text
        textPaint.setColor(Color.WHITE);
        canvas.drawText(prompt, promptX, promptY, textPaint);
    }

    private void drawFallback(Canvas canvas) {
        int pressOffset = activated ? 4 : 0;

        drawPaint.setColor(Color.rgb(60, 60, 60));
        RectF baseRect = new RectF(x, y + pressOffset, x + width, y + height);
        canvas.drawRoundRect(baseRect, 6, 6, drawPaint);

        int topColor = activated ? Color.rgb(100, 100, 100) : Color.rgb(140, 140, 140);
        drawPaint.setColor(topColor);
        RectF topRect = new RectF(x + 2, y + 2 + pressOffset, x + width - 2, y + height - 2);
        canvas.drawRoundRect(topRect, 4, 4, drawPaint);

        if (!activated) {
            drawPaint.setColor(Color.rgb(180, 180, 180));
            RectF hlRect = new RectF(x + 4, y + 4, x + width - 4, y + (height - 8) / 2f + 4);
            canvas.drawRoundRect(hlRect, 3, 3, drawPaint);
        }
    }

    private void drawDebug(Canvas canvas) {
        // Activation zone
        drawPaint.setColor(Color.argb(50, 0, 255, 255));
        canvas.drawRect(activationZone, drawPaint);

        // Button bounds
        drawPaint.setColor(Color.argb(100, 255, 255, 0));
        canvas.drawRect(x, y, x + width, y + height, drawPaint);

        // State text
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(10);
        canvas.drawText("State: " + getStateName(state), x, y - 25, textPaint);
        canvas.drawText("Link: " + linkId, x, y - 15, textPaint);
        canvas.drawText("Type: " + getButtonTypeName(buttonType), x, y - 5, textPaint);
    }

    // Getters and Setters

    public int getState() { return state; }
    public boolean isActivated() { return activated; }
    public int getButtonType() { return buttonType; }

    public void setButtonType(int type) {
        this.buttonType = type;
        if (type == BUTTON_TYPE_MOMENTARY) {
            this.requiresInteraction = false;
        }
    }

    public String getLinkId() { return linkId; }
    public void setLinkId(String linkId) { this.linkId = linkId; }
    public List<String> getLinkedDoorIds() { return linkedDoorIds; }

    public void addLinkedDoor(String doorId) {
        if (!linkedDoorIds.contains(doorId)) {
            linkedDoorIds.add(doorId);
        }
    }

    public void setLinkedDoorIds(List<String> doorIds) {
        this.linkedDoorIds = new ArrayList<>(doorIds);
    }

    public List<String> getLinkedButtonIds() { return linkedButtonIds; }

    public void addLinkedButton(String buttonId) {
        if (!linkedButtonIds.contains(buttonId)) {
            linkedButtonIds.add(buttonId);
        }
    }

    public boolean isActivatedByPlayer() { return activatedByPlayer; }
    public void setActivatedByPlayer(boolean v) { this.activatedByPlayer = v; }
    public boolean isActivatedByMobs() { return activatedByMobs; }
    public void setActivatedByMobs(boolean v) { this.activatedByMobs = v; }
    public boolean requiresInteraction() { return requiresInteraction; }
    public void setRequiresInteraction(boolean v) { this.requiresInteraction = v; }
    public int getActionType() { return actionType; }
    public void setActionType(int v) { this.actionType = v; }
    public String getActionTarget() { return actionTarget; }
    public void setActionTarget(String v) { this.actionTarget = v; }
    public void setTimedDuration(int durationMs) { this.timedDuration = durationMs; }
    public int getTimedDuration() { return timedDuration; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public void setGlowColor(int color) { this.glowColor = color; }
    public void setAnimationSpeed(float speed) { this.animationSpeed = speed; }
    public boolean shouldPlayPressSound() { return playPressSound; }
    public boolean shouldPlayReleaseSound() { return playReleaseSound; }
    public boolean isPlayerNearby() { return playerNearby; }
    public void setPlayerNearby(boolean nearby) { this.playerNearby = nearby; }

    /** Resets a one-shot button to be usable again. */
    public void reset() {
        if (state == BUTTON_STATE_DISABLED) {
            state = BUTTON_STATE_IDLE;
            activated = false;
            animationProgress = 0;
            currentFrameIndex = 0;
        }
    }
}
