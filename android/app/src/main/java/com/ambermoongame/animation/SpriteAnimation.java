package com.ambermoongame.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.ambermoongame.graphics.AndroidAssetLoader;
import com.ambermoongame.graphics.AnimatedTexture;

import java.util.HashMap;
import java.util.Map;

/**
 * SpriteAnimation manages GIF-based animations for different action states.
 * This is the core component of the sprite/GIF-based animation system that
 * replaces bone-based animation while keeping bone functionality intact.
 *
 * Features:
 * - Multiple action states (IDLE, WALK, JUMP, etc.)
 * - Automatic frame synchronization for overlays
 * - Direction flipping (left/right facing)
 * - Smooth transitions between states
 *
 * Usage:
 *   SpriteAnimation anim = new SpriteAnimation();
 *   anim.loadAction(ActionState.IDLE, "player/idle.gif");
 *   anim.loadAction(ActionState.WALK, "player/walk.gif");
 *   anim.setState(ActionState.WALK);
 *   anim.update(deltaMs);
 *   anim.draw(canvas, x, y, width, height, facingRight);
 */
public class SpriteAnimation {

    /**
     * Enumeration of animation action states.
     * Extended to support projectile firing, item usage, eating, sprinting, multi-jumps,
     * and status effects (burning, frozen, poisoned).
     * Animations should support 5-15 frames for smooth, clear motion.
     *
     * States:
     * - IDLE: Standing/breathing animation (5-10 frames)
     * - WALK: Walking animation (5-8 frames)
     * - RUN: Running animation (6-10 frames)
     * - SPRINT: Fast sprint animation (6-10 frames)
     * - JUMP: Single jump (5-8 frames)
     * - DOUBLE_JUMP: Double jump with flip/spin (8-12 frames)
     * - TRIPLE_JUMP: Triple jump with more dramatic spin (10-15 frames)
     * - FALL: Falling animation (5-8 frames)
     * - ATTACK: Melee attack swing (8-12 frames)
     * - FIRE: Projectile firing (8-12 frames)
     * - USE_ITEM: General item usage (6-10 frames)
     * - EAT: Eating food/potions (10-15 frames)
     * - HURT: Taking damage reaction (5-8 frames)
     * - DEAD: Death animation (10-15 frames)
     * - BLOCK: Blocking with shield (5-8 frames)
     * - CAST: Casting spell (10-15 frames)
     * - BURNING: On fire status effect (8-12 frames, looping)
     * - FROZEN: Frozen/slowed status effect (5-8 frames)
     * - POISONED: Poisoned status effect (6-10 frames)
     */
    public enum ActionState {
        IDLE,
        WALK,
        RUN,
        SPRINT,
        JUMP,
        DOUBLE_JUMP,
        TRIPLE_JUMP,
        FALL,
        ATTACK,
        FIRE,
        USE_ITEM,
        EAT,
        HURT,
        DEAD,
        BLOCK,
        CAST,
        BURNING,
        FROZEN,
        POISONED
    }

    // Animation textures for each action state
    private final Map<ActionState, AnimatedTexture> animations;

    // Current animation state
    private ActionState currentState;
    private ActionState previousState;

    // Timing and synchronization
    private long stateStartTime;
    private int currentFrameIndex;
    private int totalFrameCount;

    // Dimensions (original unscaled)
    private int baseWidth;
    private int baseHeight;

    // Tinting support (Android color int)
    private int tintColor;
    private boolean hasTint;

    // Reusable drawing objects to avoid per-frame allocation
    private final Paint drawPaint;
    private final Rect srcRect;
    private final Rect dstRect;

    /**
     * Creates a new SpriteAnimation system.
     */
    public SpriteAnimation() {
        this.animations = new HashMap<>();
        this.currentState = ActionState.IDLE;
        this.previousState = ActionState.IDLE;
        this.stateStartTime = System.currentTimeMillis();
        this.currentFrameIndex = 0;
        this.totalFrameCount = 1;
        this.baseWidth = 32;
        this.baseHeight = 64;
        this.tintColor = 0;
        this.hasTint = false;

        this.drawPaint = new Paint();
        this.drawPaint.setAntiAlias(false);  // Pixel art - no anti-aliasing
        this.drawPaint.setFilterBitmap(false);  // Nearest-neighbor scaling
        this.srcRect = new Rect();
        this.dstRect = new Rect();
    }

    /**
     * Loads an animation for a specific action state from a GIF file.
     * If the file doesn't exist, creates a placeholder animation.
     *
     * @param state The action state this animation represents
     * @param gifPath Path to the GIF file (relative to assets)
     * @return true if loaded successfully
     */
    public boolean loadAction(ActionState state, String gifPath) {
        try {
            if (!AndroidAssetLoader.exists(gifPath)) {
                // Create placeholder animation for missing file
                createPlaceholderAnimation(state);
                return true;
            }

            AndroidAssetLoader.ImageAsset asset = AndroidAssetLoader.load(gifPath);
            if (asset != null) {
                AnimatedTexture anim = AnimatedTexture.fromImageAsset(asset);
                if (anim != null) {
                    animations.put(state, anim);

                    // Update base dimensions from first loaded animation
                    if (animations.size() == 1) {
                        baseWidth = asset.width;
                        baseHeight = asset.height;
                    }
                    return true;
                }
            }

            // Create placeholder for failed load
            createPlaceholderAnimation(state);
            return true;
        } catch (Exception e) {
            createPlaceholderAnimation(state);
            return true;
        }
    }

    /**
     * Creates a colored placeholder animation for a missing action state.
     * The color is based on the action type for visual distinction.
     */
    private void createPlaceholderAnimation(ActionState state) {
        // Skip if already has animation
        if (animations.containsKey(state)) return;

        // Use existing animation dimensions or defaults
        int w = baseWidth > 0 ? baseWidth : 32;
        int h = baseHeight > 0 ? baseHeight : 64;

        // Create a colored placeholder frame based on action type
        int color = getPlaceholderColor(state);
        Bitmap frame = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(frame);

        // Draw body shape
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        RectF bodyRect = new RectF(w / 4f, h / 4f, w * 3 / 4f, h * 3 / 4f);
        canvas.drawRoundRect(bodyRect, 8, 8, paint);

        // Draw head
        float headCx = w / 2f;
        float headCy = 2 + h / 8f;
        float headR = w / 6f;
        canvas.drawCircle(headCx, headCy, headR, paint);

        // Draw action indicator
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(8, h / 8f));
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        String label = state.name().substring(0, Math.min(4, state.name().length()));
        canvas.drawText(label, 2, h - 4, paint);

        AnimatedTexture placeholder = new AnimatedTexture(frame);
        animations.put(state, placeholder);
    }

    /**
     * Gets a distinct color for each action state placeholder.
     */
    private int getPlaceholderColor(ActionState state) {
        switch (state) {
            case IDLE:        return Color.rgb(100, 100, 200);       // Blue
            case WALK:        return Color.rgb(100, 200, 100);       // Green
            case RUN:         return Color.rgb(200, 200, 100);       // Yellow
            case SPRINT:      return Color.rgb(255, 200, 50);        // Orange
            case JUMP:        return Color.rgb(100, 200, 200);       // Cyan
            case DOUBLE_JUMP: return Color.rgb(50, 220, 220);        // Light cyan
            case TRIPLE_JUMP: return Color.rgb(0, 255, 255);         // Bright cyan
            case FALL:        return Color.rgb(150, 150, 200);       // Light blue
            case ATTACK:      return Color.rgb(200, 50, 50);         // Red
            case FIRE:        return Color.rgb(255, 100, 50);        // Orange-red
            case USE_ITEM:    return Color.rgb(200, 150, 100);       // Tan
            case EAT:         return Color.rgb(200, 150, 50);        // Gold
            case HURT:        return Color.rgb(255, 50, 50);         // Bright red
            case DEAD:        return Color.rgb(80, 80, 80);          // Dark gray
            case BLOCK:       return Color.rgb(150, 150, 150);       // Gray
            case CAST:        return Color.rgb(150, 50, 200);        // Purple
            case BURNING:     return Color.rgb(255, 100, 0);         // Orange-red (fire)
            case FROZEN:      return Color.rgb(100, 200, 255);       // Light blue (ice)
            case POISONED:    return Color.rgb(100, 200, 50);        // Green (poison)
            default:          return Color.rgb(180, 180, 180);       // Light gray
        }
    }

    /**
     * Ensures all basic animations exist, creating placeholders for missing ones.
     */
    public void ensureBasicAnimations() {
        ActionState[] basicStates = {
            ActionState.IDLE, ActionState.WALK, ActionState.RUN,
            ActionState.JUMP, ActionState.ATTACK, ActionState.HURT
        };

        for (ActionState state : basicStates) {
            if (!animations.containsKey(state)) {
                createPlaceholderAnimation(state);
            }
        }
    }

    /**
     * Sets an animation directly for a specific action state.
     *
     * @param state The action state
     * @param texture The animated texture to use
     */
    public void setAction(ActionState state, AnimatedTexture texture) {
        if (texture != null) {
            animations.put(state, texture);

            // Update base dimensions from first loaded animation
            if (animations.size() == 1) {
                baseWidth = texture.getWidth();
                baseHeight = texture.getHeight();
            }
        }
    }

    /**
     * Sets the current animation state.
     *
     * @param state The new action state
     */
    public void setState(ActionState state) {
        if (currentState != state && animations.containsKey(state)) {
            previousState = currentState;
            currentState = state;
            stateStartTime = System.currentTimeMillis();

            // Reset the new animation
            AnimatedTexture anim = animations.get(currentState);
            if (anim != null) {
                anim.reset();
            }
        }
    }

    /**
     * Gets the current animation state.
     *
     * @return Current action state
     */
    public ActionState getState() {
        return currentState;
    }

    /**
     * Updates the animation based on elapsed time.
     *
     * @param deltaMs Time elapsed since last update in milliseconds
     */
    public void update(long deltaMs) {
        AnimatedTexture anim = animations.get(currentState);
        if (anim != null) {
            anim.update(deltaMs);
            currentFrameIndex = anim.getCurrentFrameIndex();
            totalFrameCount = anim.getFrameCount();
        }
    }

    /**
     * Gets the current frame index for synchronization with overlays.
     *
     * @return Current frame index
     */
    public int getCurrentFrameIndex() {
        return currentFrameIndex;
    }

    /**
     * Gets the total frame count for the current animation.
     *
     * @return Total number of frames
     */
    public int getTotalFrameCount() {
        return totalFrameCount;
    }

    /**
     * Gets the current AnimatedTexture for the current state.
     *
     * @return AnimatedTexture or null if not loaded
     */
    public AnimatedTexture getCurrentAnimation() {
        return animations.get(currentState);
    }

    /**
     * Sets a tint color for the sprite.
     *
     * @param color Tint color (Android color int), use 0 to clear
     */
    public void setTint(int color) {
        this.tintColor = color;
        this.hasTint = (color != 0);
    }

    /**
     * Clears the tint color.
     */
    public void clearTint() {
        this.tintColor = 0;
        this.hasTint = false;
    }

    /**
     * Gets the current tint color.
     *
     * @return Tint color (Android color int), or 0 if no tint
     */
    public int getTint() {
        return tintColor;
    }

    /**
     * Checks if a tint is currently applied.
     *
     * @return true if tinted
     */
    public boolean hasTint() {
        return hasTint;
    }

    /**
     * Gets the current frame as a Bitmap.
     *
     * @return Current frame bitmap, or null if no animation loaded
     */
    public Bitmap getCurrentFrame() {
        AnimatedTexture anim = animations.get(currentState);
        if (anim != null) {
            if (hasTint) {
                return anim.getCurrentFrame(tintColor);
            }
            return anim.getCurrentFrame();
        }
        return null;
    }

    /**
     * Draws the current animation frame.
     *
     * @param canvas Canvas to draw on
     * @param x X position
     * @param y Y position
     * @param width Scaled width
     * @param height Scaled height
     * @param facingRight True if facing right, false for left (will flip)
     */
    public void draw(Canvas canvas, int x, int y, int width, int height, boolean facingRight) {
        Bitmap frame = getCurrentFrame();
        if (frame == null) {
            // Draw placeholder
            drawPaint.setColor(Color.MAGENTA);
            canvas.drawRect(x, y, x + width, y + height, drawPaint);
            return;
        }

        srcRect.set(0, 0, frame.getWidth(), frame.getHeight());
        dstRect.set(x, y, x + width, y + height);

        if (facingRight) {
            // Normal draw
            canvas.drawBitmap(frame, srcRect, dstRect, drawPaint);
        } else {
            // Flip horizontally using canvas transform
            canvas.save();
            float centerX = x + width / 2f;
            canvas.scale(-1, 1, centerX, 0);
            canvas.drawBitmap(frame, srcRect, dstRect, drawPaint);
            canvas.restore();
        }
    }

    /**
     * Gets the base (unscaled) width of the sprite.
     *
     * @return Base width in pixels
     */
    public int getBaseWidth() {
        return baseWidth;
    }

    /**
     * Gets the base (unscaled) height of the sprite.
     *
     * @return Base height in pixels
     */
    public int getBaseHeight() {
        return baseHeight;
    }

    /**
     * Checks if an animation is loaded for the given state.
     *
     * @param state Action state to check
     * @return true if animation exists
     */
    public boolean hasAnimation(ActionState state) {
        return animations.containsKey(state);
    }

    /**
     * Gets the animation duration for a specific state.
     *
     * @param state Action state
     * @return Duration in milliseconds, or 0 if not loaded
     */
    public int getAnimationDuration(ActionState state) {
        AnimatedTexture anim = animations.get(state);
        if (anim != null) {
            return anim.getTotalDuration();
        }
        return 0;
    }

    /**
     * Checks if the current animation has finished (for non-looping animations).
     *
     * @return true if animation is paused/finished
     */
    public boolean isAnimationFinished() {
        AnimatedTexture anim = animations.get(currentState);
        if (anim != null) {
            return anim.isPaused();
        }
        return true;
    }

    /**
     * Clears all loaded animations and recycles their bitmaps.
     */
    public void clear() {
        for (AnimatedTexture anim : animations.values()) {
            if (anim != null) {
                anim.recycle();
            }
        }
        animations.clear();
        currentState = ActionState.IDLE;
        previousState = ActionState.IDLE;
    }
}
