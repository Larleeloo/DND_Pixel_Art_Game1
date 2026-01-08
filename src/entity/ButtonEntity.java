package entity;

import graphics.*;
import input.InputManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * ButtonEntity represents an interactive button/switch that can trigger doors and actions.
 *
 * Features:
 * - GIF texture animation when pressed
 * - Stays on first frame when idle (unpressed)
 * - Can be linked to DoorEntities via linkIds
 * - Supports player and mob activation
 * - Multiple button types: toggle, momentary (pressure plate), one-shot
 * - Backend-configurable actions (level events, spawns, etc.)
 *
 * JSON Configuration:
 * {
 *   "x": 100, "y": 300,
 *   "width": 32, "height": 16,
 *   "texturePath": "assets/buttons/stone_button.gif",
 *   "linkId": "button1",
 *   "linkedDoorIds": ["door1", "door2"],
 *   "buttonType": "toggle",
 *   "activatedByMobs": true,
 *   "actionType": "none",
 *   "actionTarget": ""
 * }
 */
public class ButtonEntity extends Entity {

    // Button types
    public enum ButtonType {
        TOGGLE,         // Press to toggle on/off, stays in state
        MOMENTARY,      // Like a pressure plate - active while pressed
        ONE_SHOT,       // Can only be activated once
        TIMED           // Stays active for a duration then resets
    }

    // Button states
    public enum ButtonState {
        IDLE,           // Not pressed, showing first frame
        PRESSING,       // Animation playing forward
        PRESSED,        // Fully pressed, showing last frame (for TOGGLE)
        RELEASING,      // Animation playing backward (for MOMENTARY)
        DISABLED        // Cannot be activated (for ONE_SHOT after use)
    }

    // Action types
    public enum ActionType {
        NONE,               // No action, just triggers linked doors
        LEVEL_TRANSITION,   // Load a new level
        EVENT,              // Trigger a custom event
        SPAWN_ENTITY,       // Spawn an entity at a location
        PLAY_SOUND          // Play a sound effect
    }

    // Dimensions
    private int width;
    private int height;

    // State
    private ButtonState state;
    private ButtonType buttonType;
    private boolean activated;
    private long activationTime;
    private int timedDuration;      // Duration for TIMED type (ms)

    // Animation
    private AnimatedTexture texture;
    private float animationProgress;
    private float animationSpeed;
    private boolean playingAnimation;
    private long lastUpdateTime;

    // Linking system
    private String linkId;                    // This button's ID
    private List<String> linkedDoorIds;       // IDs of doors to control
    private List<String> linkedButtonIds;     // IDs of other buttons to sync with

    // Activation settings
    private boolean activatedByPlayer;
    private boolean activatedByMobs;
    private boolean requiresInteraction;      // If false, can be triggered by walking on it

    // Actions
    private ActionType actionType;
    private String actionTarget;

    // Visual effects
    private Color glowColor;
    private float glowIntensity;
    private boolean showGlow;

    // Interaction
    private Rectangle activationZone;
    private boolean entityOnButton;           // For pressure plate behavior
    private int entitiesOnButton;             // Count for multi-entity plates
    private boolean playerNearby;             // For interaction prompt

    // Sound
    private boolean playPressSound;
    private boolean playReleaseSound;

    /**
     * Creates a new button entity.
     *
     * @param x           X position
     * @param y           Y position
     * @param width       Button width in pixels
     * @param height      Button height in pixels
     * @param texturePath Path to the button GIF texture
     */
    public ButtonEntity(int x, int y, int width, int height, String texturePath) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.state = ButtonState.IDLE;
        this.buttonType = ButtonType.TOGGLE;
        this.activated = false;
        this.activationTime = 0;
        this.timedDuration = 3000;  // 3 seconds default for TIMED type
        this.animationProgress = 0;
        this.animationSpeed = 0.1f;  // Press animation in ~10 frames
        this.playingAnimation = false;
        this.lastUpdateTime = System.currentTimeMillis();
        this.linkId = "";
        this.linkedDoorIds = new ArrayList<>();
        this.linkedButtonIds = new ArrayList<>();
        this.activatedByPlayer = true;
        this.activatedByMobs = true;
        this.requiresInteraction = true;
        this.actionType = ActionType.NONE;
        this.actionTarget = "";
        this.glowColor = new Color(100, 200, 255);
        this.glowIntensity = 0;
        this.showGlow = false;
        this.activationZone = new Rectangle(x - 8, y - 8, width + 16, height + 16);
        this.entityOnButton = false;
        this.entitiesOnButton = 0;
        this.playerNearby = false;
        this.playPressSound = false;
        this.playReleaseSound = false;

        // Load texture
        loadTexture(texturePath);
    }

    /**
     * Creates a button with a link ID.
     */
    public ButtonEntity(int x, int y, int width, int height, String texturePath, String linkId) {
        this(x, y, width, height, texturePath);
        this.linkId = linkId;
    }

    /**
     * Load the button texture from file.
     */
    private void loadTexture(String path) {
        AssetLoader.ImageAsset asset = AssetLoader.load(path);
        if (asset != null && asset.animatedTexture != null) {
            this.texture = asset.animatedTexture;
            // Pause animation - we'll control it manually
            this.texture.pause();
            this.texture.setCurrentFrameIndex(0);
            System.out.println("ButtonEntity: Loaded texture " + path +
                    " (" + texture.getFrameCount() + " frames)");
        } else {
            System.err.println("ButtonEntity: Failed to load texture " + path);
            createFallbackTexture();
        }
    }

    /**
     * Creates a fallback texture when no GIF is available.
     */
    private void createFallbackTexture() {
        BufferedImage fallback = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fallback.createGraphics();

        // Draw a simple button shape
        g.setColor(new Color(100, 100, 100));
        g.fillRoundRect(0, 0, width, height, 6, 6);

        // Highlight
        g.setColor(new Color(150, 150, 150));
        g.fillRoundRect(2, 2, width - 4, height / 2, 4, 4);

        g.dispose();
        this.texture = new AnimatedTexture(fallback);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void update(InputManager input) {
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Reset sound flags
        playPressSound = false;
        playReleaseSound = false;

        // Update animation
        if (playingAnimation) {
            updateAnimation(deltaMs);
        }

        // Handle timed button auto-release
        if (buttonType == ButtonType.TIMED && activated) {
            if (currentTime - activationTime >= timedDuration) {
                release();
            }
        }

        // Handle momentary button (pressure plate) behavior
        if (buttonType == ButtonType.MOMENTARY && !requiresInteraction) {
            if (!entityOnButton && activated) {
                release();
            }
            // Reset entity tracking for next frame
            entityOnButton = false;
            entitiesOnButton = 0;
        }

        // Update glow effect
        updateGlow(deltaMs);
    }

    /**
     * Updates the button press/release animation.
     */
    private void updateAnimation(long deltaMs) {
        if (texture == null || texture.getFrameCount() <= 1) {
            playingAnimation = false;
            return;
        }

        int frameCount = texture.getFrameCount();

        if (state == ButtonState.PRESSING) {
            animationProgress += animationSpeed;
            if (animationProgress >= 1.0f) {
                animationProgress = 1.0f;
                state = ButtonState.PRESSED;
                playingAnimation = false;
                texture.setCurrentFrameIndex(frameCount - 1);
            } else {
                int targetFrame = Math.min((int) (animationProgress * frameCount), frameCount - 1);
                texture.setCurrentFrameIndex(targetFrame);
            }
        } else if (state == ButtonState.RELEASING) {
            animationProgress -= animationSpeed;
            if (animationProgress <= 0) {
                animationProgress = 0;
                state = ButtonState.IDLE;
                playingAnimation = false;
                texture.setCurrentFrameIndex(0);
            } else {
                int targetFrame = Math.max((int) (animationProgress * frameCount), 0);
                texture.setCurrentFrameIndex(targetFrame);
            }
        }
    }

    /**
     * Updates the glow effect.
     */
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
        if (state == ButtonState.DISABLED) {
            return false;
        }

        // Check if this entity type can activate the button
        if (isPlayer && !activatedByPlayer) {
            return false;
        }
        if (!isPlayer && !activatedByMobs) {
            return false;
        }

        return press();
    }

    /**
     * Presses the button (called by player interaction or entity collision).
     * @return True if button was pressed
     */
    public boolean press() {
        if (state == ButtonState.DISABLED) {
            return false;
        }

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
                if (!activated && state != ButtonState.DISABLED) {
                    boolean result = activateButton();
                    if (result) {
                        state = ButtonState.DISABLED;
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

    /**
     * Internal method to activate the button.
     */
    private boolean activateButton() {
        if (activated && buttonType != ButtonType.MOMENTARY) {
            return false;
        }

        activated = true;
        activationTime = System.currentTimeMillis();
        state = ButtonState.PRESSING;
        playingAnimation = true;
        playPressSound = true;
        System.out.println("ButtonEntity: Button " + linkId + " pressed");
        return true;
    }

    /**
     * Releases the button (for toggle/momentary types).
     * @return True if button was released
     */
    public boolean release() {
        if (!activated) {
            return false;
        }

        activated = false;
        state = ButtonState.RELEASING;
        playingAnimation = true;
        playReleaseSound = true;
        System.out.println("ButtonEntity: Button " + linkId + " released");
        return true;
    }

    /**
     * Called when an entity is standing on this button (for pressure plates).
     */
    public void onEntityEnter(boolean isPlayer) {
        entityOnButton = true;
        entitiesOnButton++;

        if (!requiresInteraction) {
            activate(isPlayer);
        }
    }

    /**
     * Called when an entity leaves this button (for pressure plates).
     */
    public void onEntityExit() {
        entitiesOnButton = Math.max(0, entitiesOnButton - 1);
        if (entitiesOnButton == 0) {
            entityOnButton = false;
        }
    }

    /**
     * Checks if an entity's bounds intersect with the activation zone.
     */
    public boolean isInActivationZone(Rectangle entityBounds) {
        return activationZone.intersects(entityBounds);
    }

    /**
     * Checks if an entity is standing directly on the button (for pressure plates).
     */
    public boolean isEntityOnButton(Rectangle entityBounds) {
        // Check if entity is standing on top of the button
        Rectangle buttonTop = new Rectangle(x, y - 8, width, 16);
        return buttonTop.intersects(entityBounds);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Draw glow effect
        if (glowIntensity > 0) {
            int glowSize = (int) (10 + glowIntensity * 15);
            for (int i = 0; i < 3; i++) {
                float alpha = (0.15f - i * 0.04f) * glowIntensity;
                int size = glowSize + i * 8;
                g2d.setColor(new Color(
                        glowColor.getRed(),
                        glowColor.getGreen(),
                        glowColor.getBlue(),
                        (int) (alpha * 255)));
                g2d.fillOval(x + width / 2 - size / 2, y + height / 2 - size / 2, size, size);
            }
        }

        // Draw the button texture
        if (texture != null) {
            BufferedImage frame = texture.getCurrentFrame();
            if (frame != null) {
                g2d.drawImage(frame, x, y, width, height, null);
            }
        } else {
            drawFallback(g2d);
        }

        // Draw state indicator for disabled buttons
        if (state == ButtonState.DISABLED) {
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.fillRect(x, y, width, height);
        }

        // Draw interaction prompt if player is nearby and button requires interaction
        if (playerNearby && requiresInteraction && state != ButtonState.DISABLED) {
            drawInteractionPrompt(g2d);
        }

        // Debug drawing
        if (System.getProperty("debug") != null) {
            drawDebug(g2d);
        }
    }

    /**
     * Draws the "Press E" interaction prompt above the button.
     */
    private void drawInteractionPrompt(Graphics2D g) {
        String prompt = "Press E";
        Font font = new Font("Arial", Font.BOLD, 12);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int textWidth = fm.stringWidth(prompt);
        int textHeight = fm.getHeight();
        int promptX = x + width / 2 - textWidth / 2;
        int promptY = y - 20;

        // Background
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(promptX - 6, promptY - textHeight + 2, textWidth + 12, textHeight + 4, 8, 8);

        // Border
        g.setColor(new Color(255, 200, 100));
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(promptX - 6, promptY - textHeight + 2, textWidth + 12, textHeight + 4, 8, 8);

        // Text
        g.setColor(Color.WHITE);
        g.drawString(prompt, promptX, promptY);
    }

    /**
     * Draws a fallback button when no texture is loaded.
     */
    private void drawFallback(Graphics2D g) {
        // Button base
        int pressOffset = activated ? 4 : 0;
        g.setColor(new Color(60, 60, 60));
        g.fillRoundRect(x, y + pressOffset, width, height - pressOffset, 6, 6);

        // Button top
        Color topColor = activated ? new Color(100, 100, 100) : new Color(140, 140, 140);
        g.setColor(topColor);
        g.fillRoundRect(x + 2, y + 2 + pressOffset, width - 4, height - 4 - pressOffset, 4, 4);

        // Highlight
        if (!activated) {
            g.setColor(new Color(180, 180, 180));
            g.fillRoundRect(x + 4, y + 4, width - 8, (height - 8) / 2, 3, 3);
        }
    }

    /**
     * Debug drawing showing bounds and state.
     */
    private void drawDebug(Graphics2D g) {
        // Activation zone
        g.setColor(new Color(0, 255, 255, 50));
        g.fillRect(activationZone.x, activationZone.y,
                activationZone.width, activationZone.height);

        // Button bounds
        g.setColor(new Color(255, 255, 0, 100));
        g.fillRect(x, y, width, height);

        // State text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString("State: " + state, x, y - 25);
        g.drawString("Link: " + linkId, x, y - 15);
        g.drawString("Type: " + buttonType, x, y - 5);
    }

    // Getters and Setters

    public ButtonState getState() {
        return state;
    }

    public boolean isActivated() {
        return activated;
    }

    public ButtonType getButtonType() {
        return buttonType;
    }

    public void setButtonType(ButtonType type) {
        this.buttonType = type;
        // Configure defaults based on type
        if (type == ButtonType.MOMENTARY) {
            this.requiresInteraction = false;  // Pressure plate behavior
        }
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public List<String> getLinkedDoorIds() {
        return linkedDoorIds;
    }

    public void addLinkedDoor(String doorId) {
        if (!linkedDoorIds.contains(doorId)) {
            linkedDoorIds.add(doorId);
        }
    }

    public void setLinkedDoorIds(List<String> doorIds) {
        this.linkedDoorIds = new ArrayList<>(doorIds);
    }

    public List<String> getLinkedButtonIds() {
        return linkedButtonIds;
    }

    public void addLinkedButton(String buttonId) {
        if (!linkedButtonIds.contains(buttonId)) {
            linkedButtonIds.add(buttonId);
        }
    }

    public boolean isActivatedByPlayer() {
        return activatedByPlayer;
    }

    public void setActivatedByPlayer(boolean activatedByPlayer) {
        this.activatedByPlayer = activatedByPlayer;
    }

    public boolean isActivatedByMobs() {
        return activatedByMobs;
    }

    public void setActivatedByMobs(boolean activatedByMobs) {
        this.activatedByMobs = activatedByMobs;
    }

    public boolean requiresInteraction() {
        return requiresInteraction;
    }

    public void setRequiresInteraction(boolean requiresInteraction) {
        this.requiresInteraction = requiresInteraction;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getActionTarget() {
        return actionTarget;
    }

    public void setActionTarget(String actionTarget) {
        this.actionTarget = actionTarget;
    }

    public void setTimedDuration(int durationMs) {
        this.timedDuration = durationMs;
    }

    public int getTimedDuration() {
        return timedDuration;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setGlowColor(Color color) {
        this.glowColor = color;
    }

    public void setAnimationSpeed(float speed) {
        this.animationSpeed = speed;
    }

    public boolean shouldPlayPressSound() {
        return playPressSound;
    }

    public boolean shouldPlayReleaseSound() {
        return playReleaseSound;
    }

    public boolean isPlayerNearby() {
        return playerNearby;
    }

    public void setPlayerNearby(boolean nearby) {
        this.playerNearby = nearby;
    }

    /**
     * Resets a one-shot button to be usable again.
     */
    public void reset() {
        if (state == ButtonState.DISABLED) {
            state = ButtonState.IDLE;
            activated = false;
            animationProgress = 0;
            if (texture != null) {
                texture.setCurrentFrameIndex(0);
            }
        }
    }
}
