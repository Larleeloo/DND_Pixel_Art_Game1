import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Represents a collectible item in the game world
 */
class ItemEntity extends Entity {

    private Image sprite;
    private int width, height;
    private String itemName;
    private String itemType;
    public boolean collected; // Made public so it can be reset when dropped
    private float bobOffset; // For floating animation
    private float bobSpeed;

    // Color mask fields for tinting items
    private int maskRed = 255;
    private int maskGreen = 255;
    private int maskBlue = 255;
    private boolean hasColorMask = false;
    private BufferedImage tintedSprite; // Cached tinted version

    public static final int SCALE = 3;

    public ItemEntity(int x, int y, String spritePath, String itemName, String itemType) {
        super(x, y);
        this.itemName = itemName;
        this.itemType = itemType;
        this.collected = false;
        this.bobOffset = 0;
        this.bobSpeed = 0.05f;

        AssetLoader.ImageAsset asset = AssetLoader.load(spritePath);
        this.sprite = asset.staticImage;
        this.width = Math.max(1, asset.width) * SCALE;
        this.height = Math.max(1, asset.height) * SCALE;

        System.out.println("Item created: " + itemName + " (" + itemType + ")");
    }

    @Override
    public Rectangle getBounds() {
        if (collected) return new Rectangle(0, 0, 0, 0);
        return new Rectangle(x, y + (int)bobOffset, width, height);
    }

    @Override
    public void update(InputManager input) {
        if (!collected) {
            // Bobbing animation - use milliseconds for smooth animation
            bobOffset = (float)(Math.sin(System.currentTimeMillis() * 0.003) * 8);
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!collected) {
            Graphics2D g2d = (Graphics2D) g;

            // Draw glow effect
            g2d.setColor(new Color(255, 255, 100, 100));
            g2d.fillOval(x - 5, y + (int)bobOffset - 5, width + 10, height + 10);

            // Draw sprite (use tinted version if color mask is applied)
            Image spriteToDraw = (hasColorMask && tintedSprite != null) ? tintedSprite : sprite;
            if (spriteToDraw != null) {
                g.drawImage(spriteToDraw, x, y + (int)bobOffset, width, height, null);
            } else {
                // Fallback
                g.setColor(Color.YELLOW);
                g.fillRect(x, y + (int)bobOffset, width, height);
            }

            // Draw item name below
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            FontMetrics fm = g.getFontMetrics();
            int textX = x + (width - fm.stringWidth(itemName)) / 2;
            g.drawString(itemName, textX, y + height + (int)bobOffset + 15);
        }
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        this.collected = true;
        System.out.println("Collected: " + itemName);
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemType() {
        return itemType;
    }

    public Image getSprite() {
        return sprite;
    }

    /**
     * Applies an RGB color mask to the item sprite.
     * The mask shades only non-transparent pixels with the specified color.
     * Values are 8-bit (0-255) for red, green, and blue channels.
     *
     * @param red   Red channel value (0-255)
     * @param green Green channel value (0-255)
     * @param blue  Blue channel value (0-255)
     */
    public void setColorMask(int red, int green, int blue) {
        // Clamp values to valid 8-bit range
        this.maskRed = Math.max(0, Math.min(255, red));
        this.maskGreen = Math.max(0, Math.min(255, green));
        this.maskBlue = Math.max(0, Math.min(255, blue));
        this.hasColorMask = true;

        // Generate the tinted sprite
        this.tintedSprite = createTintedSprite();
    }

    /**
     * Clears the color mask, restoring the original sprite appearance.
     */
    public void clearColorMask() {
        this.hasColorMask = false;
        this.tintedSprite = null;
        this.maskRed = 255;
        this.maskGreen = 255;
        this.maskBlue = 255;
    }

    /**
     * Checks if this item currently has a color mask applied.
     *
     * @return true if a color mask is active, false otherwise
     */
    public boolean hasColorMask() {
        return hasColorMask;
    }

    /**
     * Gets the current color mask as an array [red, green, blue].
     *
     * @return int array with RGB values (0-255 each)
     */
    public int[] getColorMask() {
        return new int[] { maskRed, maskGreen, maskBlue };
    }

    /**
     * Creates a tinted copy of the sprite by applying the color mask
     * to all non-transparent pixels using multiplicative blending.
     *
     * @return BufferedImage with the color mask applied
     */
    private BufferedImage createTintedSprite() {
        if (sprite == null) {
            return null;
        }

        // Get original dimensions
        int origWidth = sprite.getWidth(null);
        int origHeight = sprite.getHeight(null);

        if (origWidth <= 0 || origHeight <= 0) {
            return null;
        }

        // Convert sprite to BufferedImage if needed
        BufferedImage sourceImage;
        if (sprite instanceof BufferedImage) {
            sourceImage = (BufferedImage) sprite;
        } else {
            // Draw the image into a new BufferedImage
            sourceImage = new BufferedImage(origWidth, origHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = sourceImage.createGraphics();
            g.drawImage(sprite, 0, 0, null);
            g.dispose();
        }

        // Create output image
        BufferedImage tinted = new BufferedImage(origWidth, origHeight, BufferedImage.TYPE_INT_ARGB);

        // Normalize mask values to 0.0-1.0 range for multiplication
        float rFactor = maskRed / 255.0f;
        float gFactor = maskGreen / 255.0f;
        float bFactor = maskBlue / 255.0f;

        // Process each pixel
        for (int py = 0; py < origHeight; py++) {
            for (int px = 0; px < origWidth; px++) {
                int argb = sourceImage.getRGB(px, py);

                // Extract components
                int alpha = (argb >> 24) & 0xFF;
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;

                // Only modify non-transparent pixels
                if (alpha > 0) {
                    // Apply multiplicative color mask
                    red = Math.round(red * rFactor);
                    green = Math.round(green * gFactor);
                    blue = Math.round(blue * bFactor);

                    // Clamp to valid range
                    red = Math.min(255, Math.max(0, red));
                    green = Math.min(255, Math.max(0, green));
                    blue = Math.min(255, Math.max(0, blue));
                }

                // Reconstruct ARGB value
                int newArgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                tinted.setRGB(px, py, newArgb);
            }
        }

        return tinted;
    }
}