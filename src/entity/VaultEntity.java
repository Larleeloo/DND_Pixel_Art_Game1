package entity;

import graphics.AnimatedTexture;
import graphics.AssetLoader;
import input.InputManager;

import java.awt.*;
import java.awt.image.BufferedImage;

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
        PLAYER_VAULT("Player Vault", "assets/vault/player_vault.gif", true),
        STORAGE_CHEST("Storage Chest", "assets/vault/storage_chest.gif", false);

        private final String displayName;
        private final String texturePath;
        private final boolean persistent;

        VaultType(String displayName, String texturePath, boolean persistent) {
            this.displayName = displayName;
            this.texturePath = texturePath;
            this.persistent = persistent;
        }

        public String getDisplayName() { return displayName; }
        public String getTexturePath() { return texturePath; }
        public boolean isPersistent() { return persistent; }
    }

    // Dimensions (position is inherited from Entity)
    private int width = 64;
    private int height = 64;

    // Vault type
    private VaultType vaultType;

    // Visual state
    private BufferedImage texture;
    private AnimatedTexture animatedTexture;
    private boolean isOpen = false;
    private boolean isInteractable = true;

    // Animation state
    private float openProgress = 0f;  // 0 = closed, 1 = fully open
    private static final float OPEN_SPEED = 3.0f;

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
     * Creates a storage chest at the specified position.
     */
    public static VaultEntity createStorageChest(int x, int y) {
        return new VaultEntity(x, y, VaultType.STORAGE_CHEST);
    }

    private void loadTexture() {
        try {
            AssetLoader.ImageAsset asset = AssetLoader.load(vaultType.getTexturePath());
            if (asset.animatedTexture != null) {
                this.animatedTexture = asset.animatedTexture;
                this.texture = animatedTexture.getCurrentFrame();
            } else if (asset.staticImage != null) {
                this.texture = asset.staticImage;
            }
        } catch (Exception e) {
            System.err.println("VaultEntity: Failed to load texture: " + vaultType.getTexturePath());
            createPlaceholderTexture();
        }

        if (texture == null) {
            createPlaceholderTexture();
        }
    }

    private void createPlaceholderTexture() {
        texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = texture.createGraphics();

        // Draw a simple vault/chest shape
        g.setColor(new Color(139, 90, 43));  // Brown for wood
        g.fillRoundRect(4, 16, 56, 44, 8, 8);

        // Draw lid
        g.setColor(new Color(101, 67, 33));  // Darker brown
        g.fillRoundRect(2, 8, 60, 20, 6, 6);

        // Draw metal bands
        g.setColor(new Color(169, 169, 169));  // Silver
        g.fillRect(8, 20, 4, 40);
        g.fillRect(52, 20, 4, 40);
        g.fillRect(28, 20, 8, 40);

        // Draw lock
        g.setColor(new Color(255, 215, 0));  // Gold
        g.fillOval(28, 35, 8, 10);

        g.dispose();
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

        // Update open/close animation
        if (isOpen && openProgress < 1f) {
            openProgress = Math.min(1f, openProgress + delta * OPEN_SPEED);
        } else if (!isOpen && openProgress > 0f) {
            openProgress = Math.max(0f, openProgress - delta * OPEN_SPEED);
        }

        // Update animated texture
        if (animatedTexture != null) {
            animatedTexture.update(deltaMs);
            texture = animatedTexture.getCurrentFrame();
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

        // Draw vault with optional open animation
        if (texture != null) {
            // Apply slight rotation/scaling when opening
            if (openProgress > 0) {
                drawOpenVault(g2d);
            } else {
                g2d.drawImage(texture, x, y, width, height, null);
            }
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

    private void drawOpenVault(Graphics2D g2d) {
        // Draw base of vault (stays in place)
        int lidHeight = height / 3;

        // Draw body
        g2d.drawImage(texture, x, y + lidHeight, width, height - lidHeight,
                      0, lidHeight, texture.getWidth(), texture.getHeight(), null);

        // Draw lid with rotation
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();

        // Rotate lid around top-back pivot
        double angle = -openProgress * Math.PI / 4;  // Open to 45 degrees
        int pivotX = x + width / 2;
        int pivotY = y + 5;

        g2d.rotate(angle, pivotX, pivotY);
        g2d.drawImage(texture, x, y, width, lidHeight,
                      0, 0, texture.getWidth(), lidHeight, null);

        g2d.setTransform(oldTransform);
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
     * Opens the vault.
     */
    public void open() {
        isOpen = true;
        if (onOpenCallback != null) {
            onOpenCallback.run();
        }
        System.out.println("VaultEntity: Vault opened");
    }

    /**
     * Closes the vault.
     */
    public void close() {
        isOpen = false;
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
        System.out.println("VaultEntity: Vault closed");
    }

    /**
     * Toggles the vault open/closed state.
     */
    public void toggle() {
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

        playerNearby = distance <= INTERACTION_RANGE;

        // Auto-close if player moves away while open
        if (isOpen && !playerNearby) {
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
}
