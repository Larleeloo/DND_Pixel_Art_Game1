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
 * DoorEntity represents an interactive door that can be opened/closed.
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
 * - GIF texture animation for opening/closing
 * - Stays on first frame when closed (idle state)
 * - Can be linked to ButtonEntities via linkId
 * - Blocks players and mobs when closed
 * - Supports backend-configurable actions (level transitions, events)
 */
public class DoorEntity extends Entity {

    private static final String TAG = "DoorEntity";
    private static final boolean DEBUG = false; // Set true for debug drawing

    // Door state constants (replaces enum to avoid D8 crash)
    public static final int DOOR_STATE_CLOSED = 0;
    public static final int DOOR_STATE_OPENING = 1;
    public static final int DOOR_STATE_OPEN = 2;
    public static final int DOOR_STATE_CLOSING = 3;

    // Action type constants
    public static final int DOOR_ACTION_NONE = 0;
    public static final int DOOR_ACTION_LEVEL_TRANSITION = 1;
    public static final int DOOR_ACTION_EVENT = 2;
    public static final int DOOR_ACTION_TELEPORT = 3;
    public static final int DOOR_ACTION_SPAWN_ENTITY = 4;

    private static String getStateName(int state) {
        switch (state) {
            case DOOR_STATE_CLOSED: return "CLOSED";
            case DOOR_STATE_OPENING: return "OPENING";
            case DOOR_STATE_OPEN: return "OPEN";
            case DOOR_STATE_CLOSING: return "CLOSING";
            default: return "UNKNOWN";
        }
    }

    private static String getActionTypeName(int type) {
        switch (type) {
            case DOOR_ACTION_NONE: return "NONE";
            case DOOR_ACTION_LEVEL_TRANSITION: return "LEVEL_TRANSITION";
            case DOOR_ACTION_EVENT: return "EVENT";
            case DOOR_ACTION_TELEPORT: return "TELEPORT";
            case DOOR_ACTION_SPAWN_ENTITY: return "SPAWN_ENTITY";
            default: return "UNKNOWN";
        }
    }

    // Dimensions
    private int width;
    private int height;

    // State
    private int state;
    private boolean locked;
    private String requiredKeyId;

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
    private List<String> linkedButtonIds;

    // Actions
    private int actionType;
    private String actionTarget;

    // Visual effects
    private boolean showHighlight;
    private float highlightAlpha;
    private int highlightColor;

    // Interaction
    private boolean playerNearby;
    private Rect interactionZone;
    private int interactionRadius;

    // Sound cues
    private boolean playOpenSound;
    private boolean playCloseSound;

    // Reusable drawing objects
    private final Paint drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    /**
     * Creates a new door entity.
     */
    public DoorEntity(int x, int y, int width, int height, String texturePath) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.state = DOOR_STATE_CLOSED;
        this.locked = false;
        this.requiredKeyId = null;
        this.animationProgress = 0;
        this.animationSpeed = 0.05f;
        this.playingAnimation = false;
        this.lastUpdateTime = System.currentTimeMillis();
        this.linkId = "";
        this.linkedButtonIds = new ArrayList<>();
        this.actionType = DOOR_ACTION_NONE;
        this.actionTarget = "";
        this.showHighlight = false;
        this.highlightAlpha = 0;
        this.highlightColor = Color.rgb(255, 255, 100);
        this.playerNearby = false;
        this.interactionRadius = 50;
        this.interactionZone = new Rect(
            x - interactionRadius, y - interactionRadius,
            x + width + interactionRadius, y + height + interactionRadius);
        this.playOpenSound = false;
        this.playCloseSound = false;

        loadTexture(texturePath);
    }

    /** Creates a door with a link ID for button connections. */
    public DoorEntity(int x, int y, int width, int height, String texturePath, String linkId) {
        this(x, y, width, height, texturePath);
        this.linkId = linkId;
    }

    /**
     * Load the door texture from file.
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

        // Door body (brown wood)
        p.setColor(Color.rgb(139, 90, 43));
        c.drawRect(0, 0, width, height, p);

        // Door frame
        p.setColor(Color.rgb(101, 67, 33));
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(4);
        c.drawRect(2, 2, width - 2, height - 2, p);
        p.setStyle(Paint.Style.FILL);

        // Door handle
        p.setColor(Color.rgb(255, 215, 0));
        RectF handleOval = new RectF(width - 20, height / 2f - 5, width - 8, height / 2f + 7);
        c.drawOval(handleOval, p);

        frames = new ArrayList<>();
        frames.add(fallback);
        frameDelays = new ArrayList<>();
        frameDelays.add(100);
    }

    @Override
    public Rect getBounds() {
        return new Rect(x, y, x + width, y + height);
    }

    /** Gets the collision bounds - only solid when door is closed. */
    public Rect getCollisionBounds() {
        if (state == DOOR_STATE_CLOSED || state == DOOR_STATE_CLOSING) {
            return new Rect(x, y, x + width, y + height);
        }
        return new Rect(x, y, x + 4, y + height);
    }

    /** Checks if this door blocks movement. */
    public boolean isSolid() {
        return state == DOOR_STATE_CLOSED || state == DOOR_STATE_CLOSING;
    }

    /** Checks if a rect intersects with this door's solid area. */
    public boolean blocksMovement(Rect entityBounds) {
        if (!isSolid()) return false;
        return Rect.intersects(getBounds(), entityBounds);
    }

    @Override
    public void update(TouchInputManager input) {
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        if (playingAnimation) {
            updateAnimation(deltaMs);
        }

        updateHighlight(deltaMs);

        playOpenSound = false;
        playCloseSound = false;
    }

    /**
     * Updates the door opening/closing animation using manual frame control.
     */
    private void updateAnimation(long deltaMs) {
        if (frames == null || frames.size() <= 1) {
            playingAnimation = false;
            return;
        }

        int frameCount = frames.size();

        if (state == DOOR_STATE_OPENING) {
            animationProgress += animationSpeed;
            if (animationProgress >= 1.0f) {
                animationProgress = 1.0f;
                state = DOOR_STATE_OPEN;
                playingAnimation = false;
                currentFrameIndex = frameCount - 1;
            } else {
                currentFrameIndex = Math.min((int)(animationProgress * frameCount), frameCount - 1);
            }
        } else if (state == DOOR_STATE_CLOSING) {
            animationProgress -= animationSpeed;
            if (animationProgress <= 0) {
                animationProgress = 0;
                state = DOOR_STATE_CLOSED;
                playingAnimation = false;
                currentFrameIndex = 0;
            } else {
                currentFrameIndex = Math.max((int)(animationProgress * frameCount), 0);
            }
        }
    }

    private void updateHighlight(long deltaMs) {
        if (showHighlight && playerNearby) {
            highlightAlpha = Math.min(highlightAlpha + 0.05f, 0.5f);
        } else {
            highlightAlpha = Math.max(highlightAlpha - 0.03f, 0);
        }
    }

    /**
     * Opens the door (starts opening animation).
     * @return true if door started opening, false if already open or locked
     */
    public boolean open() {
        if (state == DOOR_STATE_OPEN || state == DOOR_STATE_OPENING) return false;
        if (locked) {
            Log.d(TAG, "Door is locked!");
            return false;
        }

        state = DOOR_STATE_OPENING;
        playingAnimation = true;
        playOpenSound = true;
        Log.d(TAG, "Opening door " + linkId);
        return true;
    }

    /**
     * Closes the door (starts closing animation).
     * @return true if door started closing, false if already closed
     */
    public boolean close() {
        if (state == DOOR_STATE_CLOSED || state == DOOR_STATE_CLOSING) return false;

        state = DOOR_STATE_CLOSING;
        playingAnimation = true;
        playCloseSound = true;
        Log.d(TAG, "Closing door " + linkId);
        return true;
    }

    /** Toggles the door open/closed state. */
    public boolean toggle() {
        if (state == DOOR_STATE_CLOSED || state == DOOR_STATE_CLOSING) {
            return open();
        } else {
            return close();
        }
    }

    /**
     * Attempts to unlock the door with the given key item ID.
     */
    public boolean tryUnlock(String keyId) {
        if (!locked) return true;
        if (requiredKeyId == null || requiredKeyId.isEmpty()) {
            locked = false;
            return true;
        }
        if (requiredKeyId.equals(keyId)) {
            locked = false;
            Log.d(TAG, "Door " + linkId + " unlocked with " + keyId);
            return true;
        }
        return false;
    }

    /** Instantly sets the door to open state (no animation). */
    public void setOpen() {
        state = DOOR_STATE_OPEN;
        animationProgress = 1.0f;
        playingAnimation = false;
        if (frames != null && frames.size() > 1) {
            currentFrameIndex = frames.size() - 1;
        }
    }

    /** Instantly sets the door to closed state (no animation). */
    public void setClosed() {
        state = DOOR_STATE_CLOSED;
        animationProgress = 0;
        playingAnimation = false;
        currentFrameIndex = 0;
    }

    @Override
    public void draw(Canvas canvas) {
        // Draw highlight glow if near player
        if (highlightAlpha > 0) {
            int a = (int)(highlightAlpha * 255);
            drawPaint.setColor(Color.argb(a, Color.red(highlightColor),
                Color.green(highlightColor), Color.blue(highlightColor)));
            canvas.drawRect(x - 4, y - 4, x + width + 4, y + height + 4, drawPaint);
        }

        // Draw the door texture frame
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

        // Draw lock indicator if locked
        if (locked) {
            drawLockIndicator(canvas);
        }

        // Draw interaction prompt if player is nearby
        if (playerNearby && !locked) {
            drawInteractionPrompt(canvas);
        }

        // Debug drawing
        if (DEBUG) {
            drawDebug(canvas);
        }
    }

    private void drawFallback(Canvas canvas) {
        int doorAlpha = (state == DOOR_STATE_OPEN || state == DOOR_STATE_OPENING) ? 150 : 255;

        drawPaint.setColor(Color.argb(doorAlpha, 139, 90, 43));
        canvas.drawRect(x, y, x + width, y + height, drawPaint);

        drawPaint.setColor(Color.rgb(101, 67, 33));
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(3);
        canvas.drawRect(x + 1, y + 1, x + width - 1, y + height - 1, drawPaint);
        drawPaint.setStyle(Paint.Style.FILL);

        drawPaint.setColor(Color.YELLOW);
        int handleX = (state == DOOR_STATE_OPEN) ? x + 8 : x + width - 18;
        RectF handleOval = new RectF(handleX, y + height / 2f - 5, handleX + 10, y + height / 2f + 5);
        canvas.drawOval(handleOval, drawPaint);
    }

    private void drawLockIndicator(Canvas canvas) {
        int lockX = x + width / 2 - 8;
        int lockY = y + height / 2 - 10;

        // Lock body
        drawPaint.setColor(Color.rgb(80, 80, 80));
        canvas.drawRect(lockX, lockY + 6, lockX + 16, lockY + 18, drawPaint);

        // Lock shackle
        drawPaint.setColor(Color.rgb(100, 100, 100));
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(3);
        rectF.set(lockX + 3, lockY, lockX + 13, lockY + 12);
        canvas.drawArc(rectF, 180, 180, false, drawPaint);
        drawPaint.setStyle(Paint.Style.FILL);

        // Keyhole
        drawPaint.setColor(Color.BLACK);
        RectF keyholeOval = new RectF(lockX + 6, lockY + 9, lockX + 10, lockY + 13);
        canvas.drawOval(keyholeOval, drawPaint);
        canvas.drawRect(lockX + 7, lockY + 12, lockX + 9, lockY + 16, drawPaint);
    }

    private void drawInteractionPrompt(Canvas canvas) {
        String prompt = (state == DOOR_STATE_CLOSED) ? "Tap to Open" : "Tap to Close";
        textPaint.setTextSize(12);
        textPaint.setFakeBoldText(true);
        float textWidth = textPaint.measureText(prompt);

        float promptX = x + width / 2f - textWidth / 2f;
        float promptY = y - 15;

        // Background
        drawPaint.setColor(Color.argb(180, 0, 0, 0));
        RectF bgRect = new RectF(promptX - 5, promptY - 12, promptX + textWidth + 5, promptY + 4);
        canvas.drawRoundRect(bgRect, 6, 6, drawPaint);

        // Text
        textPaint.setColor(Color.WHITE);
        canvas.drawText(prompt, promptX, promptY, textPaint);
    }

    private void drawDebug(Canvas canvas) {
        // Collision bounds
        Rect collision = getCollisionBounds();
        drawPaint.setColor(Color.argb(100, 255, 0, 0));
        canvas.drawRect(collision, drawPaint);

        // Interaction zone
        drawPaint.setColor(Color.argb(50, 0, 255, 0));
        canvas.drawRect(interactionZone, drawPaint);

        // State text
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(10);
        canvas.drawText("State: " + getStateName(state), x, y - 25, textPaint);
        canvas.drawText("Link: " + linkId, x, y - 15, textPaint);
    }

    // Getters and Setters

    public int getState() { return state; }
    public boolean isOpen() { return state == DOOR_STATE_OPEN; }
    public boolean isClosed() { return state == DOOR_STATE_CLOSED; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public String getRequiredKeyId() { return requiredKeyId; }
    public void setRequiredKeyId(String keyId) {
        this.requiredKeyId = keyId;
        this.locked = (keyId != null && !keyId.isEmpty());
    }

    public String getLinkId() { return linkId; }
    public void setLinkId(String linkId) { this.linkId = linkId; }
    public List<String> getLinkedButtonIds() { return linkedButtonIds; }
    public void addLinkedButton(String buttonId) {
        if (!linkedButtonIds.contains(buttonId)) {
            linkedButtonIds.add(buttonId);
        }
    }

    public int getActionType() { return actionType; }
    public void setActionType(int actionType) { this.actionType = actionType; }
    public String getActionTarget() { return actionTarget; }
    public void setActionTarget(String actionTarget) { this.actionTarget = actionTarget; }
    public boolean isPlayerNearby() { return playerNearby; }
    public void setPlayerNearby(boolean nearby) {
        this.playerNearby = nearby;
        this.showHighlight = nearby;
    }

    public boolean isInInteractionZone(Rect bounds) {
        return Rect.intersects(interactionZone, bounds);
    }

    public void setAnimationSpeed(float speed) { this.animationSpeed = speed; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean shouldPlayOpenSound() { return playOpenSound; }
    public boolean shouldPlayCloseSound() { return playCloseSound; }

    public void setStartsOpen(boolean startsOpen) {
        if (startsOpen) {
            setOpen();
        } else {
            setClosed();
        }
    }
}
