package entity;
import block.*;
import input.*;
import graphics.*;
import animation.*;
import audio.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

/**
 * Represents a collectible item in the game world.
 * Can be linked to an Item from ItemRegistry for full properties.
 */
public class ItemEntity extends Entity {

    private Image sprite;
    private int width, height;
    private String itemName;
    private String itemType;
    private String itemId;  // Registry ID for linking to Item properties
    private Item linkedItem;  // Linked Item with full properties
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
    private static final int ICON_SIZE = 16;  // Base icon size

    public ItemEntity(int x, int y, String spritePath, String itemName, String itemType) {
        super(x, y);
        this.itemName = itemName;
        this.itemType = itemType;
        this.itemId = null;
        this.linkedItem = null;
        this.collected = false;
        this.bobOffset = 0;
        this.bobSpeed = 0.05f;

        // Try to load sprite, fall back to generated icon
        AssetLoader.ImageAsset asset = AssetLoader.load(spritePath);
        if (asset.staticImage != null && asset.width > 1) {
            this.sprite = asset.staticImage;
            this.width = Math.max(1, asset.width) * SCALE;
            this.height = Math.max(1, asset.height) * SCALE;
        } else {
            // Generate a colored icon based on item type
            this.sprite = generateItemIcon(itemType, itemName);
            this.width = ICON_SIZE * SCALE;
            this.height = ICON_SIZE * SCALE;
        }
    }

    /**
     * Creates an ItemEntity linked to an ItemRegistry entry.
     */
    public ItemEntity(int x, int y, String itemId) {
        super(x, y);
        this.itemId = itemId;
        this.collected = false;
        this.bobOffset = 0;
        this.bobSpeed = 0.05f;

        // Get item from registry
        this.linkedItem = ItemRegistry.create(itemId);
        if (linkedItem != null) {
            this.itemName = linkedItem.getName();
            this.itemType = linkedItem.getCategory().name().toLowerCase();
        } else {
            this.itemName = itemId;
            this.itemType = "unknown";
        }

        // Generate colored icon based on item type and rarity
        this.sprite = generateItemIcon(itemType, itemName);
        this.width = ICON_SIZE * SCALE;
        this.height = ICON_SIZE * SCALE;
    }

    /**
     * Generates a colored icon based on item type.
     */
    private BufferedImage generateItemIcon(String type, String name) {
        BufferedImage icon = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Get colors based on item type
        Color primary = getTypeColor(type);
        Color secondary = primary.darker();
        Color outline = Color.BLACK;

        // Draw based on item type
        switch (type.toLowerCase()) {
            case "weapon":
                // Sword shape
                g.setColor(secondary);
                g.fillRect(7, 2, 2, 10);  // Blade
                g.setColor(primary);
                g.fillRect(6, 2, 4, 2);   // Tip
                g.setColor(new Color(139, 90, 43));  // Brown handle
                g.fillRect(6, 12, 4, 3);
                g.setColor(new Color(255, 215, 0));  // Gold guard
                g.fillRect(4, 11, 8, 2);
                break;

            case "ranged_weapon":
            case "bow":
                // Bow shape
                g.setColor(new Color(139, 90, 43));  // Brown wood
                g.drawArc(4, 2, 8, 12, 30, 120);
                g.setColor(Color.WHITE);  // String
                g.drawLine(8, 2, 8, 14);
                break;

            case "armor":
                // Chestplate shape
                g.setColor(secondary);
                g.fillRoundRect(3, 3, 10, 11, 3, 3);
                g.setColor(primary);
                g.fillRoundRect(4, 4, 8, 9, 2, 2);
                g.setColor(secondary);
                g.fillRect(5, 5, 6, 3);  // Neck hole
                break;

            case "potion":
                // Potion bottle
                g.setColor(new Color(200, 200, 255));  // Glass
                g.fillOval(4, 6, 8, 8);  // Bottle body
                g.setColor(primary);  // Liquid
                g.fillOval(5, 8, 6, 5);
                g.setColor(new Color(139, 90, 43));  // Cork
                g.fillRect(6, 3, 4, 4);
                break;

            case "food":
                // Food (circular with bite mark)
                g.setColor(primary);
                g.fillOval(3, 4, 10, 10);
                g.setColor(secondary);
                g.fillOval(10, 4, 4, 4);  // Bite mark
                break;

            case "tool":
                // Pickaxe shape
                g.setColor(new Color(139, 90, 43));  // Handle
                g.fillRect(7, 6, 2, 9);
                g.setColor(primary);  // Head
                g.fillRect(3, 3, 10, 4);
                g.setColor(secondary);
                g.fillRect(4, 4, 8, 2);
                break;

            case "ammo":
                // Arrow
                g.setColor(new Color(139, 90, 43));  // Shaft
                g.fillRect(7, 4, 2, 10);
                g.setColor(Color.GRAY);  // Tip
                int[] tipX = {8, 5, 11};
                int[] tipY = {2, 5, 5};
                g.fillPolygon(tipX, tipY, 3);
                g.setColor(Color.RED);  // Fletching
                g.fillRect(6, 12, 4, 2);
                break;

            case "collectible":
            case "material":
                // Gem/crystal shape
                g.setColor(primary);
                int[] gemX = {8, 3, 5, 11, 13};
                int[] gemY = {2, 6, 14, 14, 6};
                g.fillPolygon(gemX, gemY, 5);
                g.setColor(secondary);
                g.drawPolygon(gemX, gemY, 5);
                break;

            case "key":
                // Key shape
                g.setColor(new Color(255, 215, 0));  // Gold
                g.fillOval(3, 3, 6, 6);  // Handle
                g.fillRect(7, 6, 7, 2);  // Shaft
                g.fillRect(11, 6, 2, 4);  // Teeth
                g.fillRect(13, 6, 2, 3);
                g.setColor(new Color(180, 150, 0));
                g.drawOval(4, 4, 4, 4);
                break;

            case "lantern":
                // Lantern
                g.setColor(new Color(139, 90, 43));  // Frame
                g.fillRect(5, 3, 6, 2);
                g.fillRect(5, 12, 6, 2);
                g.setColor(new Color(255, 200, 100, 200));  // Glow
                g.fillOval(4, 4, 8, 9);
                g.setColor(new Color(255, 255, 100));  // Flame
                g.fillOval(6, 6, 4, 5);
                break;

            default:
                // Default square
                g.setColor(primary);
                g.fillRoundRect(2, 2, 12, 12, 4, 4);
                g.setColor(secondary);
                g.drawRoundRect(2, 2, 12, 12, 4, 4);
        }

        // Draw outline for visibility
        g.setColor(outline);
        g.setStroke(new BasicStroke(0.5f));

        g.dispose();
        return icon;
    }

    /**
     * Gets a color based on item type.
     */
    private Color getTypeColor(String type) {
        switch (type.toLowerCase()) {
            case "weapon": return new Color(192, 192, 192);  // Silver
            case "ranged_weapon":
            case "bow": return new Color(139, 90, 43);  // Brown
            case "armor": return new Color(100, 100, 150);  // Steel blue
            case "potion": return new Color(255, 100, 100);  // Red
            case "food": return new Color(255, 200, 100);  // Orange/tan
            case "tool": return new Color(100, 100, 100);  // Gray
            case "ammo": return new Color(150, 100, 50);  // Brown
            case "collectible": return new Color(100, 200, 255);  // Light blue
            case "material": return new Color(200, 150, 100);  // Tan
            case "key": return new Color(255, 215, 0);  // Gold
            case "lantern": return new Color(255, 200, 50);  // Yellow
            default: return new Color(180, 180, 180);  // Light gray
        }
    }

    /**
     * Gets the linked Item with full properties.
     */
    public Item getLinkedItem() {
        return linkedItem;
    }

    /**
     * Sets the linked Item.
     */
    public void setLinkedItem(Item item) {
        this.linkedItem = item;
        if (item != null) {
            this.itemName = item.getName();
            this.itemType = item.getCategory().name().toLowerCase();
        }
    }

    /**
     * Gets the ItemRegistry ID.
     */
    public String getItemId() {
        return itemId;
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