package animation;

import graphics.AnimatedTexture;
import graphics.AssetLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
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
 *   anim.loadAction(ActionState.IDLE, "assets/player/idle.gif");
 *   anim.loadAction(ActionState.WALK, "assets/player/walk.gif");
 *   anim.setState(ActionState.WALK);
 *   anim.update(deltaMs);
 *   anim.draw(g, x, y, width, height, facingRight);
 */
public class SpriteAnimation {

    /**
     * Enumeration of animation action states.
     * Extended to support projectile firing, item usage, eating, sprinting, and multi-jumps.
     * Animations should support 5-15 frames for smooth, clear motion.
     */
    public enum ActionState {
        IDLE,           // Standing/breathing animation (5-10 frames)
        WALK,           // Walking animation (5-8 frames)
        RUN,            // Running animation (6-10 frames)
        SPRINT,         // Fast sprint animation (6-10 frames)
        JUMP,           // Single jump (5-8 frames)
        DOUBLE_JUMP,    // Double jump with flip/spin (8-12 frames)
        TRIPLE_JUMP,    // Triple jump with more dramatic spin (10-15 frames)
        FALL,           // Falling animation (5-8 frames)
        ATTACK,         // Melee attack swing (8-12 frames)
        FIRE,           // Projectile firing (bow/crossbow/wand) (8-12 frames)
        USE_ITEM,       // General item usage (6-10 frames)
        EAT,            // Eating food/potions (10-15 frames)
        HURT,           // Taking damage reaction (5-8 frames)
        DEAD,           // Death animation (10-15 frames)
        BLOCK,          // Blocking with shield (5-8 frames)
        CAST            // Casting spell (10-15 frames)
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

    // Tinting support
    private Color tintColor;

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
        this.tintColor = null;
    }

    /**
     * Loads an animation for a specific action state from a GIF file.
     * If the file doesn't exist, creates a placeholder animation.
     *
     * @param state The action state this animation represents
     * @param gifPath Path to the GIF file
     * @return true if loaded successfully
     */
    public boolean loadAction(ActionState state, String gifPath) {
        try {
            java.io.File file = new java.io.File(gifPath);
            if (!file.exists()) {
                // Create placeholder animation for missing file
                createPlaceholderAnimation(state);
                return true;
            }

            AssetLoader.ImageAsset asset = AssetLoader.load(gifPath);
            if (asset.animatedTexture != null) {
                animations.put(state, asset.animatedTexture);

                // Update base dimensions from first loaded animation
                if (animations.size() == 1) {
                    baseWidth = asset.width;
                    baseHeight = asset.height;
                }
                return true;
            } else if (asset.staticImage != null) {
                // Create single-frame animated texture from static image
                AnimatedTexture staticAnim = new AnimatedTexture(asset.staticImage);
                animations.put(state, staticAnim);
                return true;
            } else {
                // Create placeholder for failed load
                createPlaceholderAnimation(state);
                return true;
            }
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
        Color color = getPlaceholderColor(state);
        BufferedImage frame = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = frame.createGraphics();

        // Draw body shape
        g.setColor(color);
        g.fillRoundRect(w/4, h/4, w/2, h*3/4 - h/4, 8, 8);

        // Draw head
        g.fillOval(w/3, 2, w/3, h/4);

        // Draw action indicator
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, Math.max(8, h/8)));
        String label = state.name().substring(0, Math.min(4, state.name().length()));
        g.drawString(label, 2, h - 4);

        g.dispose();

        AnimatedTexture placeholder = new AnimatedTexture(frame);
        animations.put(state, placeholder);
    }

    /**
     * Gets a distinct color for each action state placeholder.
     */
    private Color getPlaceholderColor(ActionState state) {
        switch (state) {
            case IDLE: return new Color(100, 100, 200);       // Blue
            case WALK: return new Color(100, 200, 100);       // Green
            case RUN: return new Color(200, 200, 100);        // Yellow
            case SPRINT: return new Color(255, 200, 50);      // Orange
            case JUMP: return new Color(100, 200, 200);       // Cyan
            case DOUBLE_JUMP: return new Color(50, 220, 220); // Light cyan
            case TRIPLE_JUMP: return new Color(0, 255, 255);  // Bright cyan
            case FALL: return new Color(150, 150, 200);       // Light blue
            case ATTACK: return new Color(200, 50, 50);       // Red
            case FIRE: return new Color(255, 100, 50);        // Orange-red
            case USE_ITEM: return new Color(200, 150, 100);   // Tan
            case EAT: return new Color(200, 150, 50);         // Gold
            case HURT: return new Color(255, 50, 50);         // Bright red
            case DEAD: return new Color(80, 80, 80);          // Dark gray
            case BLOCK: return new Color(150, 150, 150);      // Gray
            case CAST: return new Color(150, 50, 200);        // Purple
            default: return new Color(180, 180, 180);         // Light gray
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

            System.out.println("SpriteAnimation: State changed from " + previousState + " to " + currentState);
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
     * @param color Tint color (null to clear)
     */
    public void setTint(Color color) {
        this.tintColor = color;
    }

    /**
     * Gets the current tint color.
     *
     * @return Tint color or null
     */
    public Color getTint() {
        return tintColor;
    }

    /**
     * Gets the current frame as a BufferedImage.
     *
     * @return Current frame image, or null if no animation loaded
     */
    public BufferedImage getCurrentFrame() {
        AnimatedTexture anim = animations.get(currentState);
        if (anim != null) {
            if (tintColor != null) {
                return anim.getCurrentFrame(tintColor);
            }
            return anim.getCurrentFrame();
        }
        return null;
    }

    /**
     * Draws the current animation frame.
     *
     * @param g Graphics context
     * @param x X position
     * @param y Y position
     * @param width Scaled width
     * @param height Scaled height
     * @param facingRight True if facing right, false for left (will flip)
     */
    public void draw(Graphics g, int x, int y, int width, int height, boolean facingRight) {
        BufferedImage frame = getCurrentFrame();
        if (frame == null) {
            // Draw placeholder
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, width, height);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        if (facingRight) {
            // Normal draw
            g2d.drawImage(frame, x, y, width, height, null);
        } else {
            // Flip horizontally
            g2d.drawImage(frame, x + width, y, -width, height, null);
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
     * Clears all loaded animations.
     */
    public void clear() {
        animations.clear();
        currentState = ActionState.IDLE;
        previousState = ActionState.IDLE;
    }
}
