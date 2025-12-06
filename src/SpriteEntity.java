import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Simplified SpriteEntity for rollback version.
 * - Handles static sprite (PNG) only, optional GIF for jump.
 * - Keeps width/height scaled by SCALE.
 * - Supports RGB color masking for tinting sprites.
 */
class SpriteEntity extends Entity {

    protected Image sprite;       // image to draw
    protected ImageIcon animatedIcon; // optional animated GIF
    protected int width, height;
    private boolean solid;

    // Color mask fields for tinting sprites
    private int maskRed = 255;
    private int maskGreen = 255;
    private int maskBlue = 255;
    private boolean hasColorMask = false;
    private BufferedImage tintedSprite; // Cached tinted version

    public static final int SCALE = 4;

    public SpriteEntity(int x, int y, String spritePath, boolean solid) {
        super(x, y);
        this.solid = solid;

        AssetLoader.ImageAsset asset = AssetLoader.load(spritePath);
        this.sprite = asset.staticImage;
        //this.animatedIcon = asset.animatedIcon; // optional GIF

        this.width = Math.max(1, asset.width) * SCALE;
        this.height = Math.max(1, asset.height) * SCALE;

        // Debug
        System.out.println("Loaded sprite \"" + spritePath + "\" -> w=" + asset.width + " h=" + asset.height
                + " scaled -> " + this.width + "x" + this.height
                + " animated=" + (this.animatedIcon != null));
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void draw(Graphics g) {
        // Use tinted version if color mask is applied
        Image spriteToDraw = (hasColorMask && tintedSprite != null) ? tintedSprite : sprite;
        if (spriteToDraw != null) {
            g.drawImage(spriteToDraw, x, y, width, height, null);
        } else {
            // placeholder so we see something
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, width, height);
        }
    }

    public boolean isSolid() {
        return solid;
    }

    /**
     * Applies an RGB color mask to the sprite.
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
     * Checks if this sprite currently has a color mask applied.
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
