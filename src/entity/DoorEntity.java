package entity;

import graphics.*;
import input.InputManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * DoorEntity represents an interactive door that can be opened/closed.
 *
 * Features:
 * - GIF texture animation for opening/closing
 * - Stays on first frame when closed (idle state)
 * - Can be linked to ButtonEntities via linkId
 * - Blocks players and mobs when closed
 * - Supports backend-configurable actions (level transitions, events)
 *
 * JSON Configuration:
 * {
 *   "x": 100, "y": 200,
 *   "width": 64, "height": 128,
 *   "texturePath": "assets/doors/wooden_door.gif",
 *   "linkId": "door1",
 *   "startsOpen": false,
 *   "requiresKey": false,
 *   "keyItemId": "iron_key",
 *   "actionType": "none",
 *   "actionTarget": ""
 * }
 */
public class DoorEntity extends Entity {

    // Door states
    public enum DoorState {
        CLOSED,
        OPENING,
        OPEN,
        CLOSING
    }

    // Action types that can be triggered when door is used
    public enum ActionType {
        NONE,               // No action, just opens/closes
        LEVEL_TRANSITION,   // Load a new level
        EVENT,              // Trigger a custom event
        TELEPORT,           // Teleport player to coordinates
        SPAWN_ENTITY        // Spawn an entity
    }

    // Dimensions
    private int width;
    private int height;

    // State
    private DoorState state;
    private boolean locked;
    private String requiredKeyId;

    // Animation
    private AnimatedTexture texture;
    private AnimatedTexture closedTexture;   // Texture for closed state (if different)
    private AnimatedTexture openTexture;     // Texture for open state (if different)
    private float animationProgress;         // 0.0 to 1.0
    private float animationSpeed;
    private boolean playingAnimation;
    private long lastUpdateTime;

    // Linking system
    private String linkId;                   // ID for button/trigger linking
    private List<String> linkedButtonIds;    // IDs of buttons that control this door

    // Actions
    private ActionType actionType;
    private String actionTarget;             // Level path, event name, or coordinates

    // Visual effects
    private boolean showHighlight;
    private float highlightAlpha;
    private Color highlightColor;

    // Interaction
    private boolean playerNearby;
    private Rectangle interactionZone;
    private int interactionRadius;

    // Sound cues (can be hooked to AudioManager)
    private boolean playOpenSound;
    private boolean playCloseSound;

    /**
     * Creates a new door entity.
     *
     * @param x           X position
     * @param y           Y position
     * @param width       Door width in pixels
     * @param height      Door height in pixels
     * @param texturePath Path to the door GIF texture
     */
    public DoorEntity(int x, int y, int width, int height, String texturePath) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.state = DoorState.CLOSED;
        this.locked = false;
        this.requiredKeyId = null;
        this.animationProgress = 0;
        this.animationSpeed = 0.05f;  // Opens in ~20 frames
        this.playingAnimation = false;
        this.lastUpdateTime = System.currentTimeMillis();
        this.linkId = "";
        this.linkedButtonIds = new ArrayList<>();
        this.actionType = ActionType.NONE;
        this.actionTarget = "";
        this.showHighlight = false;
        this.highlightAlpha = 0;
        this.highlightColor = new Color(255, 255, 100);
        this.playerNearby = false;
        this.interactionRadius = 50;
        this.interactionZone = new Rectangle(x - interactionRadius, y - interactionRadius,
                width + interactionRadius * 2, height + interactionRadius * 2);
        this.playOpenSound = false;
        this.playCloseSound = false;

        // Load texture
        loadTexture(texturePath);
    }

    /**
     * Creates a door with a link ID for button connections.
     */
    public DoorEntity(int x, int y, int width, int height, String texturePath, String linkId) {
        this(x, y, width, height, texturePath);
        this.linkId = linkId;
    }

    /**
     * Load the door texture from file.
     */
    private void loadTexture(String path) {
        AssetLoader.ImageAsset asset = AssetLoader.load(path);
        if (asset != null && asset.animatedTexture != null) {
            this.texture = asset.animatedTexture;
            // Pause animation - we'll control it manually
            this.texture.pause();
            this.texture.setCurrentFrameIndex(0);
            System.out.println("DoorEntity: Loaded texture " + path +
                    " (" + texture.getFrameCount() + " frames)");
        } else {
            System.err.println("DoorEntity: Failed to load texture " + path);
            // Create a fallback texture
            createFallbackTexture();
        }
    }

    /**
     * Creates a fallback texture when no GIF is available.
     */
    private void createFallbackTexture() {
        BufferedImage fallback = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fallback.createGraphics();

        // Draw a simple door shape
        g.setColor(new Color(139, 90, 43));  // Brown wood color
        g.fillRect(0, 0, width, height);

        // Door frame
        g.setColor(new Color(101, 67, 33));
        g.setStroke(new BasicStroke(4));
        g.drawRect(2, 2, width - 4, height - 4);

        // Door handle
        g.setColor(new Color(255, 215, 0));
        g.fillOval(width - 20, height / 2 - 5, 12, 12);

        g.dispose();
        this.texture = new AnimatedTexture(fallback);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Gets the collision bounds - only solid when door is closed.
     */
    public Rectangle getCollisionBounds() {
        if (state == DoorState.CLOSED || state == DoorState.CLOSING) {
            return new Rectangle(x, y, width, height);
        }
        // When open, return a thin rectangle (or empty)
        return new Rectangle(x, y, 4, height);
    }

    /**
     * Checks if this door blocks movement.
     */
    public boolean isSolid() {
        return state == DoorState.CLOSED || state == DoorState.CLOSING;
    }

    /**
     * Checks if a point/rectangle intersects with this door's solid area.
     */
    public boolean blocksMovement(Rectangle entityBounds) {
        if (!isSolid()) return false;
        return getBounds().intersects(entityBounds);
    }

    @Override
    public void update(InputManager input) {
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Update animation
        if (playingAnimation) {
            updateAnimation(deltaMs);
        }

        // Update highlight effect
        updateHighlight(deltaMs);

        // Reset sound flags
        playOpenSound = false;
        playCloseSound = false;
    }

    /**
     * Updates the door opening/closing animation.
     */
    private void updateAnimation(long deltaMs) {
        if (texture == null || texture.getFrameCount() <= 1) {
            // No animation frames, just snap to state
            playingAnimation = false;
            return;
        }

        int frameCount = texture.getFrameCount();
        float frameProgress = (float) texture.getCurrentFrameIndex() / (frameCount - 1);

        if (state == DoorState.OPENING) {
            // Advance through frames
            animationProgress += animationSpeed;
            if (animationProgress >= 1.0f) {
                animationProgress = 1.0f;
                state = DoorState.OPEN;
                playingAnimation = false;
                texture.setCurrentFrameIndex(frameCount - 1);
            } else {
                int targetFrame = Math.min((int) (animationProgress * frameCount), frameCount - 1);
                texture.setCurrentFrameIndex(targetFrame);
            }
        } else if (state == DoorState.CLOSING) {
            // Reverse through frames
            animationProgress -= animationSpeed;
            if (animationProgress <= 0) {
                animationProgress = 0;
                state = DoorState.CLOSED;
                playingAnimation = false;
                texture.setCurrentFrameIndex(0);
            } else {
                int targetFrame = Math.max((int) (animationProgress * frameCount), 0);
                texture.setCurrentFrameIndex(targetFrame);
            }
        }
    }

    /**
     * Updates the highlight glow effect.
     */
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
        if (state == DoorState.OPEN || state == DoorState.OPENING) {
            return false;
        }
        if (locked) {
            System.out.println("DoorEntity: Door is locked!");
            return false;
        }

        state = DoorState.OPENING;
        playingAnimation = true;
        playOpenSound = true;
        System.out.println("DoorEntity: Opening door " + linkId);
        return true;
    }

    /**
     * Closes the door (starts closing animation).
     * @return true if door started closing, false if already closed
     */
    public boolean close() {
        if (state == DoorState.CLOSED || state == DoorState.CLOSING) {
            return false;
        }

        state = DoorState.CLOSING;
        playingAnimation = true;
        playCloseSound = true;
        System.out.println("DoorEntity: Closing door " + linkId);
        return true;
    }

    /**
     * Toggles the door open/closed state.
     * @return true if state was changed
     */
    public boolean toggle() {
        if (state == DoorState.CLOSED || state == DoorState.CLOSING) {
            return open();
        } else {
            return close();
        }
    }

    /**
     * Attempts to unlock the door with the given key item ID.
     * @param keyId The item ID of the key being used
     * @return true if unlocked successfully
     */
    public boolean tryUnlock(String keyId) {
        if (!locked) return true;
        if (requiredKeyId == null || requiredKeyId.isEmpty()) {
            locked = false;
            return true;
        }
        if (requiredKeyId.equals(keyId)) {
            locked = false;
            System.out.println("DoorEntity: Door " + linkId + " unlocked with " + keyId);
            return true;
        }
        return false;
    }

    /**
     * Instantly sets the door to open state (no animation).
     */
    public void setOpen() {
        state = DoorState.OPEN;
        animationProgress = 1.0f;
        playingAnimation = false;
        if (texture != null && texture.getFrameCount() > 1) {
            texture.setCurrentFrameIndex(texture.getFrameCount() - 1);
        }
    }

    /**
     * Instantly sets the door to closed state (no animation).
     */
    public void setClosed() {
        state = DoorState.CLOSED;
        animationProgress = 0;
        playingAnimation = false;
        if (texture != null) {
            texture.setCurrentFrameIndex(0);
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Draw highlight glow if near player
        if (highlightAlpha > 0) {
            g2d.setColor(new Color(
                    highlightColor.getRed(),
                    highlightColor.getGreen(),
                    highlightColor.getBlue(),
                    (int) (highlightAlpha * 255)));
            g2d.fillRect(x - 4, y - 4, width + 8, height + 8);
        }

        // Draw the door texture
        if (texture != null) {
            BufferedImage frame = texture.getCurrentFrame();
            if (frame != null) {
                g2d.drawImage(frame, x, y, width, height, null);
            }
        } else {
            // Fallback drawing
            drawFallback(g2d);
        }

        // Draw lock indicator if locked
        if (locked) {
            drawLockIndicator(g2d);
        }

        // Draw interaction prompt if player is nearby
        if (playerNearby && !locked) {
            drawInteractionPrompt(g2d);
        }

        // Debug drawing
        if (System.getProperty("debug") != null) {
            drawDebug(g2d);
        }
    }

    /**
     * Draws a fallback door when no texture is loaded.
     */
    private void drawFallback(Graphics2D g) {
        // Door body
        Color doorColor = (state == DoorState.OPEN || state == DoorState.OPENING)
                ? new Color(139, 90, 43, 150)   // Semi-transparent when open
                : new Color(139, 90, 43);       // Solid when closed
        g.setColor(doorColor);
        g.fillRect(x, y, width, height);

        // Frame
        g.setColor(new Color(101, 67, 33));
        g.setStroke(new BasicStroke(3));
        g.drawRect(x + 1, y + 1, width - 2, height - 2);

        // Handle
        g.setColor(Color.YELLOW);
        int handleX = (state == DoorState.OPEN) ? x + 8 : x + width - 18;
        g.fillOval(handleX, y + height / 2 - 5, 10, 10);
    }

    /**
     * Draws a lock indicator icon.
     */
    private void drawLockIndicator(Graphics2D g) {
        int lockX = x + width / 2 - 8;
        int lockY = y + height / 2 - 10;

        // Lock body
        g.setColor(new Color(80, 80, 80));
        g.fillRect(lockX, lockY + 6, 16, 12);

        // Lock shackle
        g.setColor(new Color(100, 100, 100));
        g.setStroke(new BasicStroke(3));
        g.drawArc(lockX + 3, lockY, 10, 12, 0, 180);

        // Keyhole
        g.setColor(Color.BLACK);
        g.fillOval(lockX + 6, lockY + 9, 4, 4);
        g.fillRect(lockX + 7, lockY + 12, 2, 4);
    }

    /**
     * Draws interaction prompt when player is near.
     */
    private void drawInteractionPrompt(Graphics2D g) {
        String prompt = (state == DoorState.CLOSED) ? "Press [E] to Open" : "Press [E] to Close";
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(prompt);

        int promptX = x + width / 2 - textWidth / 2;
        int promptY = y - 15;

        // Background
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(promptX - 5, promptY - 12, textWidth + 10, 16, 6, 6);

        // Text
        g.setColor(Color.WHITE);
        g.drawString(prompt, promptX, promptY);
    }

    /**
     * Debug drawing showing bounds and state.
     */
    private void drawDebug(Graphics2D g) {
        // Collision bounds
        g.setColor(new Color(255, 0, 0, 100));
        Rectangle collision = getCollisionBounds();
        g.fillRect(collision.x, collision.y, collision.width, collision.height);

        // Interaction zone
        g.setColor(new Color(0, 255, 0, 50));
        g.fillRect(interactionZone.x, interactionZone.y,
                interactionZone.width, interactionZone.height);

        // State text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString("State: " + state, x, y - 25);
        g.drawString("Link: " + linkId, x, y - 15);
    }

    // Getters and Setters

    public DoorState getState() {
        return state;
    }

    public boolean isOpen() {
        return state == DoorState.OPEN;
    }

    public boolean isClosed() {
        return state == DoorState.CLOSED;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getRequiredKeyId() {
        return requiredKeyId;
    }

    public void setRequiredKeyId(String keyId) {
        this.requiredKeyId = keyId;
        this.locked = (keyId != null && !keyId.isEmpty());
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public List<String> getLinkedButtonIds() {
        return linkedButtonIds;
    }

    public void addLinkedButton(String buttonId) {
        if (!linkedButtonIds.contains(buttonId)) {
            linkedButtonIds.add(buttonId);
        }
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

    public boolean isPlayerNearby() {
        return playerNearby;
    }

    public void setPlayerNearby(boolean nearby) {
        this.playerNearby = nearby;
        this.showHighlight = nearby;
    }

    /**
     * Checks if a point is within the interaction zone.
     */
    public boolean isInInteractionZone(Rectangle bounds) {
        return interactionZone.intersects(bounds);
    }

    public void setAnimationSpeed(float speed) {
        this.animationSpeed = speed;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean shouldPlayOpenSound() {
        return playOpenSound;
    }

    public boolean shouldPlayCloseSound() {
        return playCloseSound;
    }

    /**
     * Sets the door to start in an open state.
     */
    public void setStartsOpen(boolean startsOpen) {
        if (startsOpen) {
            setOpen();
        } else {
            setClosed();
        }
    }
}
