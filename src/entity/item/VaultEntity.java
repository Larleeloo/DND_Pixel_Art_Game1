package entity.item;

import entity.Entity;
import graphics.AnimatedTexture;
import graphics.AssetLoader;
import input.InputManager;
import save.SaveManager.SavedItem;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * VaultEntity represents an interactable vault/chest that provides access to
 * the player's persistent storage.
 *
 * Features:
 * - Opens with 'E' key when player is nearby
 * - Provides access to vault inventory (10,000 slots)
 * - Items can be added/removed to/from the vault
 * - Visual feedback with glow effect and opening animation
 * - Works in both LootGameScene and CreativeScene
 *
 * Types:
 * - PLAYER_VAULT: Main player vault with persistent storage
 * - STORAGE_CHEST: Local storage chest (per-level)
 */
public class VaultEntity extends Entity {

    // Vault types
    public enum VaultType {
        PLAYER_VAULT("Player Vault", "assets/vault/player_vault", true, 10000),
        STORAGE_CHEST("Storage Chest", "assets/vault/storage_chest", false, 48),
        LARGE_CHEST("Large Chest", "assets/vault/large_chest", false, 32),
        MEDIUM_CHEST("Medium Chest", "assets/vault/medium_chest", false, 16),
        ANCIENT_POTTERY("Ancient Pottery", "assets/items/ancient_pottery", false, 5);

        private final String displayName;
        private final String textureBasePath;
        private final boolean persistent;
        private final int maxSlots;

        VaultType(String displayName, String textureBasePath, boolean persistent, int maxSlots) {
            this.displayName = displayName;
            this.textureBasePath = textureBasePath;
            this.persistent = persistent;
            this.maxSlots = maxSlots;
        }

        public String getDisplayName() { return displayName; }
        public String getTextureBasePath() { return textureBasePath; }
        public boolean isPersistent() { return persistent; }
        public int getMaxSlots() { return maxSlots; }
    }

    // Dimensions (position is inherited from Entity)
    private int width = 64;
    private int height = 64;

    // Vault type
    private VaultType vaultType;

    // Visual state
    private AnimatedTexture animatedTexture;
    private BufferedImage texture;
    private AnimatedTexture closedTexture;
    private AnimatedTexture openTexture;
    private boolean isOpen = false;
    private boolean isOpening = false;
    private boolean isClosing = false;
    private boolean isInteractable = true;

    // Glow effect
    private float glowIntensity = 0.5f;
    private float glowPhase = 0f;
    private Color glowColor = new Color(255, 215, 0);  // Gold glow

    // Interaction
    private boolean playerNearby = false;
    private static final int INTERACTION_RANGE = 80;

    // Callback for when vault is opened/closed
    private Runnable onOpenCallback;
    private Runnable onCloseCallback;

    // Time tracking for animation
    private long lastUpdateTime = System.currentTimeMillis();

    // Local storage for STORAGE_CHEST type (non-persistent)
    private List<SavedItem> localItems = new ArrayList<>();

    /**
     * Creates a new VaultEntity.
     *
     * @param x X position
     * @param y Y position
     * @param vaultType Type of vault
     */
    public VaultEntity(int x, int y, VaultType vaultType) {
        super(x, y);
        this.vaultType = vaultType;

        loadTexture();
    }

    /**
     * Creates a player vault at the specified position.
     */
    public static VaultEntity createPlayerVault(int x, int y) {
        return new VaultEntity(x, y, VaultType.PLAYER_VAULT);
    }

    /**
     * Creates a storage chest at the specified position (48 slots).
     */
    public static VaultEntity createStorageChest(int x, int y) {
        return new VaultEntity(x, y, VaultType.STORAGE_CHEST);
    }

    /**
     * Creates a large chest at the specified position (32 slots).
     */
    public static VaultEntity createLargeChest(int x, int y) {
        return new VaultEntity(x, y, VaultType.LARGE_CHEST);
    }

    /**
     * Creates a medium chest at the specified position (16 slots).
     */
    public static VaultEntity createMediumChest(int x, int y) {
        return new VaultEntity(x, y, VaultType.MEDIUM_CHEST);
    }

    /**
     * Creates an ancient pottery container at the specified position (5 slots).
     */
    public static VaultEntity createAncientPottery(int x, int y) {
        return new VaultEntity(x, y, VaultType.ANCIENT_POTTERY);
    }

    /**
     * Loads the GIF texture for opening animation.
     * A single GIF represents the full open/close animation:
     * - First frame = closed state
     * - Last frame = open state
     * - Play forward to open, play reverse to close
     */
    private void loadTexture() {
        String texturePath = vaultType.getTextureBasePath() + ".gif";

        try {
            AssetLoader.ImageAsset asset = AssetLoader.load(texturePath);
            if (asset.animatedTexture != null) {
                animatedTexture = asset.animatedTexture;
            } else if (asset.staticImage != null) {
                // Single static image - create single-frame animation
                List<BufferedImage> frames = new ArrayList<>();
                frames.add(asset.staticImage);
                List<Integer> delays = new ArrayList<>();
                delays.add(100);
                animatedTexture = new AnimatedTexture(frames, delays);
            }
        } catch (Exception e) {
            System.err.println("VaultEntity: Failed to load texture: " + texturePath);
        }

        // Create placeholder if needed
        if (animatedTexture == null) {
            animatedTexture = createPlaceholderTexture();
        }

        // Configure for non-looping playback and start at first frame (closed)
        animatedTexture.setLooping(false);
        animatedTexture.goToStart();
    }
        String basePath = vaultType.getTextureBasePath();

        // Load closed texture
        try {
            AssetLoader.ImageAsset closedAsset = AssetLoader.load(basePath + "_closed.gif");
            if (closedAsset.animatedTexture != null) {
                this.closedTexture = closedAsset.animatedTexture;
            } else if (closedAsset.staticImage != null) {
                this.closedTexture = createSingleFrameTexture(closedAsset.staticImage);
            }
        } catch (Exception e) {
            System.err.println("VaultEntity: Failed to load closed texture: " + basePath + "_closed.gif");
        }

        // Load open texture
        try {
            AssetLoader.ImageAsset openAsset = AssetLoader.load(basePath + "_open.gif");
            if (openAsset.animatedTexture != null) {
                this.openTexture = openAsset.animatedTexture;
            } else if (openAsset.staticImage != null) {
                this.openTexture = createSingleFrameTexture(openAsset.staticImage);
            }
        } catch (Exception e) {
            System.err.println("VaultEntity: Failed to load open texture: " + basePath + "_open.gif");
        }

        // Create placeholder textures if needed
        if (closedTexture == null) {
            closedTexture = createPlaceholderTexture(false);
        }
        if (openTexture == null) {
            openTexture = createPlaceholderTexture(true);
        }

        texture = closedTexture.getCurrentFrame();
    }

    /**
     * Creates a single-frame AnimatedTexture from a static image.
     */
    private AnimatedTexture createSingleFrameTexture(BufferedImage image) {
        List<BufferedImage> frames = new ArrayList<>();
        frames.add(image);
        List<Integer> delays = new ArrayList<>();
        delays.add(1000);
        return new AnimatedTexture(frames, delays);
    }

    private AnimatedTexture createPlaceholderTexture(boolean isOpenState) {
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();

        // Draw a simple vault/chest shape
        g.setColor(new Color(139, 90, 43));  // Brown for wood
        g.fillRoundRect(4, 16, 56, 44, 8, 8);

        // Draw lid (position depends on state)
        if (isOpenState) {
            // Open lid - rotated back
            java.awt.geom.AffineTransform oldTransform = g.getTransform();
            g.rotate(Math.toRadians(-45), 32, 16);
            g.setColor(new Color(101, 67, 33));  // Darker brown
            g.fillRoundRect(2, 8, 60, 20, 6, 6);
            g.setTransform(oldTransform);
        } else {
            // Closed lid
            g.setColor(new Color(101, 67, 33));  // Darker brown
            g.fillRoundRect(2, 8, 60, 20, 6, 6);
        }

        // Draw metal bands
        g.setColor(new Color(169, 169, 169));  // Silver
        g.fillRect(8, 20, 4, 40);
        g.fillRect(52, 20, 4, 40);
        g.fillRect(28, 20, 8, 40);

    /**
     * Creates a placeholder animated texture when GIF file is not available.
     * Creates multiple frames showing the lid opening animation.
     */
    private AnimatedTexture createPlaceholderTexture() {
        List<BufferedImage> frames = new ArrayList<>();
        List<Integer> delays = new ArrayList<>();

        // Create frames for opening animation (8 frames, 0 to 45 degrees)
        int frameCount = 8;
        for (int f = 0; f < frameCount; f++) {
            float lidAngle = (f / (float)(frameCount - 1)) * 45; // 0 to 45 degrees

            BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();

            // Draw a simple vault/chest shape
            g.setColor(new Color(139, 90, 43));  // Brown for wood
            g.fillRoundRect(4, 16, 56, 44, 8, 8);

            // Draw lid with rotation based on frame
            java.awt.geom.AffineTransform oldTransform = g.getTransform();
            g.rotate(Math.toRadians(-lidAngle), 32, 16);
            g.setColor(new Color(101, 67, 33));  // Darker brown
            g.fillRoundRect(2, 8, 60, 20, 6, 6);
            g.setTransform(oldTransform);

            // Draw metal bands
            g.setColor(new Color(169, 169, 169));  // Silver
            g.fillRect(8, 20, 4, 40);
            g.fillRect(52, 20, 4, 40);
            g.fillRect(28, 20, 8, 40);

            // Draw lock
            g.setColor(new Color(255, 215, 0));  // Gold
            g.fillOval(28, 35, 8, 10);

            g.dispose();

            frames.add(frame);
            delays.add(50); // 50ms per frame
        }

        g.dispose();

        List<BufferedImage> frames = new ArrayList<>();
        frames.add(placeholder);
        List<Integer> delays = new ArrayList<>();
        delays.add(1000);
        return new AnimatedTexture(frames, delays);
    }

    @Override
    public void update(InputManager input) {
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        float delta = deltaMs / 1000f;

        // Update glow effect
        glowPhase += delta * 2;
        glowIntensity = 0.4f + 0.2f * (float) Math.sin(glowPhase);

        // Update the animated texture
        if (animatedTexture != null) {
            animatedTexture.update(deltaMs);

            // Check if opening animation completed (reached last frame)
            if (isOpening && animatedTexture.isAtEnd() && animatedTexture.isPaused()) {
                isOpening = false;
                isOpen = true;
            }

            // Check if closing animation completed (reached first frame)
            if (isClosing && animatedTexture.isAtStart() && animatedTexture.isPaused()) {
                isClosing = false;
                isOpen = false;
        // Update animated texture based on state and get current frame
        if (isOpen) {
            if (openTexture != null) {
                openTexture.update(deltaMs);
                texture = openTexture.getCurrentFrame();
            }
        } else {
            if (closedTexture != null) {
                closedTexture.update(deltaMs);
                texture = closedTexture.getCurrentFrame();
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Draw glow effect if player is nearby
        if (playerNearby || isOpen) {
            drawGlowEffect(g2d);
        }

        // Draw interaction prompt if nearby and not open
        if (playerNearby && !isOpen) {
            drawInteractionPrompt(g2d);
        }

        // Draw vault texture (GIF-based with directional playback)
        if (animatedTexture != null) {
            g2d.drawImage(animatedTexture.getCurrentFrame(), x, y, width, height, null);
        // Draw vault texture (GIF-based, no programmatic animation)
        if (texture != null) {
            g2d.drawImage(texture, x, y, width, height, null);
        }

        // Draw "OPEN" text when vault is open
        if (isOpen) {
            g2d.setColor(new Color(0, 255, 0, 200));
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String text = "VAULT OPEN";
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            g2d.drawString(text, x + (width - textWidth) / 2, y - 5);
        }
    }

    private void drawGlowEffect(Graphics2D g2d) {
        // Draw multiple layers of glow
        int glowRadius = (int) (20 * glowIntensity);
        for (int i = glowRadius; i > 0; i -= 4) {
            float alpha = 0.1f * (1 - (float) i / glowRadius);
            g2d.setColor(new Color(
                glowColor.getRed() / 255f,
                glowColor.getGreen() / 255f,
                glowColor.getBlue() / 255f,
                alpha * (isOpen ? 1.5f : 1f)
            ));
            g2d.fillOval(
                x - i + width / 2,
                y - i + height / 2,
                width + i * 2 - width,
                height + i * 2 - height
            );
        }
    }

    private void drawInteractionPrompt(Graphics2D g2d) {
        // Draw "Press E" prompt
        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String text = "[E] Open Vault";
        int textWidth = g2d.getFontMetrics().stringWidth(text);

        // Background
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(x + (width - textWidth) / 2 - 8, y - 28, textWidth + 16, 22, 8, 8);

        // Text
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, x + (width - textWidth) / 2, y - 12);
    }

    /**
     * Attempts to open the vault when player presses E.
     * Returns true if the vault was opened.
     */
    public boolean tryOpen() {
        if (!isInteractable || !playerNearby) return false;

        if (!isOpen) {
            open();
            return true;
        }
        return false;
    }

    /**
     * Handles a mouse click at the given screen coordinates.
     * Opens the vault if clicked while player is nearby.
     *
     * @param clickX Click X position (screen coordinates)
     * @param clickY Click Y position (screen coordinates)
     * @param cameraOffsetX Camera X offset for coordinate translation
     * @param cameraOffsetY Camera Y offset for coordinate translation
     * @return true if the click was handled (vault opened or closed)
     */
    public boolean handleClick(int clickX, int clickY, int cameraOffsetX, int cameraOffsetY) {
        // Translate screen coordinates to world coordinates
        int worldX = clickX + cameraOffsetX;
        int worldY = clickY + cameraOffsetY;

        // Check if click is within vault bounds
        Rectangle bounds = new Rectangle(x, y, width, height);
        if (bounds.contains(worldX, worldY)) {
            if (playerNearby && isInteractable) {
                toggle();
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a screen position is within the vault bounds.
     *
     * @param screenX Screen X position
     * @param screenY Screen Y position
     * @param cameraOffsetX Camera X offset
     * @param cameraOffsetY Camera Y offset
     * @return true if the position is within vault bounds
     */
    public boolean containsPoint(int screenX, int screenY, int cameraOffsetX, int cameraOffsetY) {
        int worldX = screenX + cameraOffsetX;
        int worldY = screenY + cameraOffsetY;
        return new Rectangle(x, y, width, height).contains(worldX, worldY);
    }

    /**
     * Opens the vault.
     * Plays the GIF animation forward.
     */
    public void open() {
        if (isOpen || isOpening) return;

        isOpening = true;
        isClosing = false;

        // Start playing the animation forward
        if (animatedTexture != null) {
            animatedTexture.playForward();
        }

        if (onOpenCallback != null) {
            onOpenCallback.run();
        }
        System.out.println("VaultEntity: Vault opening");
    }

    /**
     * Closes the vault.
     * Plays the GIF animation in reverse.
     */
    public void close() {
        if (!isOpen || isClosing) return;

        isClosing = true;
        isOpening = false;

        // Start playing the animation in reverse
        if (animatedTexture != null) {
            animatedTexture.playReverse();
        }

        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
        System.out.println("VaultEntity: Vault closing");
    }

    /**
     * Toggles the vault open/closed state.
     */
    public void toggle() {
        if (isOpening || isClosing) {
            // Don't toggle while animation is in progress
            return;
        }
        if (isOpen) {
            close();
        } else {
            open();
        }
    }

    /**
     * Checks if the player is within interaction range.
     */
    public void checkPlayerProximity(int playerX, int playerY, int playerWidth, int playerHeight) {
        int playerCenterX = playerX + playerWidth / 2;
        int playerCenterY = playerY + playerHeight / 2;
        int vaultCenterX = x + width / 2;
        int vaultCenterY = y + height / 2;

        double distance = Math.sqrt(
            Math.pow(playerCenterX - vaultCenterX, 2) +
            Math.pow(playerCenterY - vaultCenterY, 2)
        );

        boolean wasNearby = playerNearby;
        playerNearby = distance <= INTERACTION_RANGE;

        // Auto-close if player moves away while open (and not already closing)
        if ((isOpen || isOpening) && wasNearby && !playerNearby && !isClosing) {
            isOpening = false;
            isOpen = true; // Force to open state so close() can work
            close();
        }
    }

    // Getters and setters

    public int getX() { return x; }

    public int getY() { return y; }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public void setPosition(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    public VaultType getVaultType() { return vaultType; }

    public boolean isOpen() { return isOpen; }

    public boolean isPlayerNearby() { return playerNearby; }

    public boolean isPersistent() { return vaultType.isPersistent(); }

    public void setOnOpenCallback(Runnable callback) {
        this.onOpenCallback = callback;
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    public void setGlowColor(Color color) {
        this.glowColor = color;
    }

    public void setInteractable(boolean interactable) {
        this.isInteractable = interactable;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // ==================== Local Storage Methods (for STORAGE_CHEST) ====================

    /**
     * Gets the local items list (for STORAGE_CHEST type).
     */
    public List<SavedItem> getLocalItems() {
        return new ArrayList<>(localItems);
    }

    /**
     * Adds an item to local storage (for STORAGE_CHEST type).
     *
     * @param itemId Item registry ID
     * @param count Number of items
     * @return Number of items that couldn't be added (overflow)
     */
    public int addLocalItem(String itemId, int count) {
        if (!vaultType.isPersistent() && itemId != null && !itemId.isEmpty() && count > 0) {
            int remaining = count;
            int stackSize = 16;

            // Try to stack with existing items
            for (SavedItem item : localItems) {
                if (item.itemId.equals(itemId) && item.stackCount < stackSize) {
                    int space = stackSize - item.stackCount;
                    int toAdd = Math.min(space, remaining);
                    item.stackCount += toAdd;
                    remaining -= toAdd;
                    if (remaining == 0) return 0;
                }
            }

            // Add new stacks
            while (remaining > 0 && localItems.size() < vaultType.getMaxSlots()) {
                int toAdd = Math.min(stackSize, remaining);
                localItems.add(new SavedItem(itemId, toAdd));
                remaining -= toAdd;
            }

            return remaining;
        }
        return count;
    }

    /**
     * Removes an item from local storage (for STORAGE_CHEST type).
     *
     * @param slotIndex Slot index
     * @param count Number to remove (-1 for entire stack)
     * @return The removed item info, or null if invalid
     */
    public SavedItem removeLocalItem(int slotIndex, int count) {
        if (!vaultType.isPersistent() && slotIndex >= 0 && slotIndex < localItems.size()) {
            SavedItem item = localItems.get(slotIndex);
            if (count < 0 || count >= item.stackCount) {
                localItems.remove(slotIndex);
                return item;
            } else {
                item.stackCount -= count;
                return new SavedItem(item.itemId, count);
            }
        }
        return null;
    }

    /**
     * Clears all local items (for STORAGE_CHEST type).
     */
    public void clearLocalItems() {
        localItems.clear();
    }

    /**
     * Gets the local storage slot count.
     */
    public int getLocalSlotCount() {
        return localItems.size();
    }

    /**
     * Gets the maximum local storage slots.
     */
    public int getMaxLocalSlots() {
        return vaultType.getMaxSlots();
    }
}
