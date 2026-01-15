package entity;

import graphics.AnimatedTexture;
import graphics.AssetLoader;
import input.InputManager;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * AlchemyTableEntity represents an interactable alchemy/crafting table.
 *
 * Features:
 * - Opens with 'E' key when player is nearby
 * - Provides access to alchemy UI for combining items
 * - Visual feedback with glow effect
 * - Supports both alchemy (combining) and reverse crafting (deconstruction) modes
 *
 * Usage in LootGameScene:
 *   AlchemyTableEntity alchemyTable = new AlchemyTableEntity(x, y, false);
 *   alchemyTable.setOnOpenCallback(() -> openAlchemyUI());
 */
public class AlchemyTableEntity extends Entity {

    // Dimensions
    private int width = 64;
    private int height = 64;

    // Visual state
    private BufferedImage texture;
    private AnimatedTexture animatedTexture;
    private boolean isOpen = false;
    private boolean isInteractable = true;

    // Mode: false = alchemy (combine), true = reverse (deconstruct)
    private boolean reverseMode;

    // Glow effect
    private float glowIntensity = 0.5f;
    private float glowPhase = 0f;
    private Color glowColor;

    // Interaction
    private boolean playerNearby = false;
    private static final int INTERACTION_RANGE = 80;

    // Callbacks
    private Runnable onOpenCallback;
    private Runnable onCloseCallback;

    // Time tracking
    private long lastUpdateTime = System.currentTimeMillis();

    /**
     * Creates an alchemy table entity.
     *
     * @param x X position
     * @param y Y position
     * @param reverseMode false for alchemy (combine items), true for reverse crafting (deconstruct)
     */
    public AlchemyTableEntity(int x, int y, boolean reverseMode) {
        super(x, y);
        this.reverseMode = reverseMode;

        // Different colors for alchemy vs reverse crafting
        if (reverseMode) {
            this.glowColor = new Color(200, 100, 255);  // Purple for reverse crafting
        } else {
            this.glowColor = new Color(100, 255, 150);  // Green for alchemy
        }

        loadTexture();
    }

    /**
     * Creates a standard alchemy table.
     */
    public static AlchemyTableEntity createAlchemyTable(int x, int y) {
        return new AlchemyTableEntity(x, y, false);
    }

    /**
     * Creates a reverse crafting table.
     */
    public static AlchemyTableEntity createReverseCraftingTable(int x, int y) {
        return new AlchemyTableEntity(x, y, true);
    }

    private void loadTexture() {
        String texturePath = reverseMode
            ? "assets/alchemy/reverse_crafting_table.gif"
            : "assets/alchemy/alchemy_table.gif";

        try {
            AssetLoader.ImageAsset asset = AssetLoader.load(texturePath);
            if (asset.animatedTexture != null) {
                this.animatedTexture = asset.animatedTexture;
                this.texture = animatedTexture.getCurrentFrame();
            } else if (asset.staticImage != null) {
                this.texture = asset.staticImage;
            }
        } catch (Exception e) {
            System.err.println("AlchemyTableEntity: Failed to load texture: " + texturePath);
            createPlaceholderTexture();
        }

        if (texture == null) {
            createPlaceholderTexture();
        }
    }

    private void createPlaceholderTexture() {
        texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = texture.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (reverseMode) {
            // Reverse crafting table - anvil-like shape
            // Base
            g.setColor(new Color(60, 60, 70));
            g.fillRect(8, 48, 48, 12);

            // Legs
            g.setColor(new Color(50, 50, 60));
            g.fillRect(12, 40, 8, 12);
            g.fillRect(44, 40, 8, 12);

            // Body
            g.setColor(new Color(80, 80, 90));
            g.fillRect(4, 24, 56, 20);

            // Top surface (anvil horn)
            g.setColor(new Color(100, 100, 110));
            g.fillRoundRect(0, 16, 64, 12, 4, 4);

            // Highlight
            g.setColor(new Color(140, 140, 150));
            g.fillRect(4, 18, 56, 3);

            // Purple glow accents
            g.setColor(new Color(180, 100, 220, 150));
            g.fillOval(26, 28, 12, 8);

        } else {
            // Alchemy table - cauldron/potion table shape
            // Table legs
            g.setColor(new Color(101, 67, 33));
            g.fillRect(8, 48, 8, 14);
            g.fillRect(48, 48, 8, 14);

            // Table surface
            g.setColor(new Color(139, 90, 43));
            g.fillRoundRect(2, 40, 60, 12, 4, 4);

            // Cauldron/bowl
            g.setColor(new Color(60, 60, 60));
            g.fillOval(16, 20, 32, 24);

            // Cauldron inner
            g.setColor(new Color(40, 40, 40));
            g.fillOval(20, 24, 24, 16);

            // Bubbling potion (green glow)
            g.setColor(new Color(80, 200, 120, 200));
            g.fillOval(24, 28, 16, 10);

            // Bubbles
            g.setColor(new Color(150, 255, 180, 200));
            g.fillOval(28, 26, 6, 6);
            g.fillOval(36, 30, 4, 4);

            // Book/scroll on table
            g.setColor(new Color(200, 180, 140));
            g.fillRect(2, 44, 12, 6);
        }

        g.dispose();
    }

    @Override
    public void update(InputManager input) {
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        float delta = deltaMs / 1000f;

        // Update glow effect
        glowPhase += delta * 2.5f;
        glowIntensity = 0.4f + 0.25f * (float) Math.sin(glowPhase);

        // Update animated texture
        if (animatedTexture != null) {
            animatedTexture.update(deltaMs);
            texture = animatedTexture.getCurrentFrame();
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Draw glow effect if player is nearby or table is open
        if (playerNearby || isOpen) {
            drawGlowEffect(g2d);
        }

        // Draw interaction prompt if nearby and not open
        if (playerNearby && !isOpen) {
            drawInteractionPrompt(g2d);
        }

        // Draw table
        if (texture != null) {
            g2d.drawImage(texture, x, y, width, height, null);
        }

        // Draw "IN USE" text when open
        if (isOpen) {
            g2d.setColor(glowColor);
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            String text = "IN USE";
            int textWidth = g2d.getFontMetrics().stringWidth(text);
            g2d.drawString(text, x + (width - textWidth) / 2, y - 5);
        }
    }

    private void drawGlowEffect(Graphics2D g2d) {
        int glowRadius = (int) (25 * glowIntensity);
        for (int i = glowRadius; i > 0; i -= 4) {
            float alpha = 0.12f * (1 - (float) i / glowRadius);
            g2d.setColor(new Color(
                glowColor.getRed() / 255f,
                glowColor.getGreen() / 255f,
                glowColor.getBlue() / 255f,
                alpha * (isOpen ? 1.5f : 1f)
            ));
            g2d.fillOval(
                x + width / 2 - i,
                y + height / 2 - i,
                i * 2,
                i * 2
            );
        }
    }

    private void drawInteractionPrompt(Graphics2D g2d) {
        String text = reverseMode ? "[E] Deconstruct" : "[E] Craft";
        g2d.setFont(new Font("Arial", Font.BOLD, 13));
        int textWidth = g2d.getFontMetrics().stringWidth(text);

        // Background
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(x + (width - textWidth) / 2 - 10, y - 30, textWidth + 20, 24, 8, 8);

        // Border
        g2d.setColor(glowColor);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(x + (width - textWidth) / 2 - 10, y - 30, textWidth + 20, 24, 8, 8);

        // Text
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, x + (width - textWidth) / 2, y - 12);
    }

    /**
     * Attempts to open the alchemy table.
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
     * Opens the alchemy table.
     */
    public void open() {
        isOpen = true;
        if (onOpenCallback != null) {
            onOpenCallback.run();
        }
        System.out.println("AlchemyTableEntity: " + (reverseMode ? "Reverse crafting" : "Alchemy") + " table opened");
    }

    /**
     * Closes the alchemy table.
     */
    public void close() {
        isOpen = false;
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
        System.out.println("AlchemyTableEntity: " + (reverseMode ? "Reverse crafting" : "Alchemy") + " table closed");
    }

    /**
     * Toggles the open state.
     */
    public void toggle() {
        if (isOpen) {
            close();
        } else {
            open();
        }
    }

    /**
     * Checks if player is within interaction range.
     */
    public void checkPlayerProximity(int playerX, int playerY, int playerWidth, int playerHeight) {
        int playerCenterX = playerX + playerWidth / 2;
        int playerCenterY = playerY + playerHeight / 2;
        int tableCenterX = x + width / 2;
        int tableCenterY = y + height / 2;

        double distance = Math.sqrt(
            Math.pow(playerCenterX - tableCenterX, 2) +
            Math.pow(playerCenterY - tableCenterY, 2)
        );

        playerNearby = distance <= INTERACTION_RANGE;

        // Auto-close if player moves away
        if (isOpen && !playerNearby) {
            close();
        }
    }

    /**
     * Checks if a point is in the interaction zone.
     */
    public boolean isInInteractionZone(Rectangle playerBounds) {
        Rectangle interactionZone = new Rectangle(
            x - INTERACTION_RANGE / 2,
            y - INTERACTION_RANGE / 2,
            width + INTERACTION_RANGE,
            height + INTERACTION_RANGE
        );
        return interactionZone.intersects(playerBounds);
    }

    /**
     * Handles a mouse click at the given screen coordinates.
     */
    public boolean handleClick(int clickX, int clickY, int cameraOffsetX, int cameraOffsetY) {
        int worldX = clickX + cameraOffsetX;
        int worldY = clickY + cameraOffsetY;

        Rectangle bounds = new Rectangle(x, y, width, height);
        if (bounds.contains(worldX, worldY)) {
            if (playerNearby && isInteractable) {
                toggle();
                return true;
            }
        }
        return false;
    }

    // Getters and setters

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean isOpen() { return isOpen; }
    public boolean isPlayerNearby() { return playerNearby; }
    public boolean isReverseMode() { return reverseMode; }

    public void setPlayerNearby(boolean nearby) {
        this.playerNearby = nearby;
        if (isOpen && !nearby) {
            close();
        }
    }

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
