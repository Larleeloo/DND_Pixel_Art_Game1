package entity;
import block.*;
import input.*;
import graphics.*;
import animation.*;
import audio.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.util.Iterator;
import org.w3c.dom.NodeList;

/**
 * Represents a collectible item in the game world.
 * Can be linked to an Item from ItemRegistry for full properties.
 * Supports animated GIF sprites with automatic frame cycling.
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

    // Stack count for stackable items
    private int stackCount = 1;
    private int maxStackSize = 1;

    // Animation support for GIFs (full 32-bit color with per-frame timing)
    private List<BufferedImage> animationFrames;
    private List<Integer> frameDelays; // Per-frame delays in milliseconds
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private int defaultFrameDelay = 100; // Default if not specified in GIF
    private boolean isAnimated = false;

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
            // Set stack properties from linked item
            if (linkedItem.isStackable()) {
                this.maxStackSize = linkedItem.getMaxStackSize();
            }
        } else {
            this.itemName = itemId;
            this.itemType = "unknown";
        }

        // Try to load sprite from assets, fall back to procedural generation
        this.sprite = loadOrGenerateSprite(itemId, itemType, itemName);
        this.width = ICON_SIZE * SCALE;
        this.height = ICON_SIZE * SCALE;
    }

    /**
     * Attempts to load a sprite from assets, falls back to procedural generation.
     * Prefers GIF files (animated) over PNG files.
     * For GIFs, loads all frames for animation support.
     */
    private BufferedImage loadOrGenerateSprite(String itemId, String type, String name) {
        // Try loading from assets/items/{itemId}.gif first (animated), then .png
        if (itemId != null && !itemId.isEmpty()) {
            String[] gifPaths = {
                "assets/items/" + itemId + ".gif",
                "assets/items/" + type + "/" + itemId + ".gif"
            };

            // Try GIF files first (with animation support)
            for (String path : gifPaths) {
                try {
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        List<BufferedImage> frames = loadGifFrames(file);
                        if (frames != null && !frames.isEmpty()) {
                            this.animationFrames = frames;
                            this.isAnimated = frames.size() > 1;
                            this.lastFrameTime = System.currentTimeMillis();
                            return frames.get(0);
                        }
                    }
                } catch (Exception e) {
                    // Continue to next path
                }
            }

            // Try PNG files (static)
            String[] pngPaths = {
                "assets/items/" + itemId + ".png",
                "assets/items/" + type + "/" + itemId + ".png"
            };

            for (String path : pngPaths) {
                try {
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        BufferedImage loaded = ImageIO.read(file);
                        if (loaded != null) {
                            return scaleToIconSize(loaded);
                        }
                    }
                } catch (Exception e) {
                    // Continue to next path
                }
            }
        }

        // Fall back to procedural generation
        return generateItemIcon(type, name);
    }

    /**
     * Loads all frames from an animated GIF file with full 32-bit color support.
     * Reads frame timing from GIF metadata for accurate playback.
     * Properly composites frames to handle transparency and disposal methods.
     */
    private List<BufferedImage> loadGifFrames(java.io.File file) {
        List<BufferedImage> frames = new ArrayList<>();
        List<Integer> delays = new ArrayList<>();

        try {
            ImageInputStream stream = ImageIO.createImageInputStream(file);
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) {
                stream.close();
                return null;
            }

            ImageReader reader = readers.next();
            reader.setInput(stream);

            int numFrames = reader.getNumImages(true);

            // Get dimensions from first frame for the master canvas
            BufferedImage firstFrame = reader.read(0);
            int width = firstFrame.getWidth();
            int height = firstFrame.getHeight();

            // Create a master canvas for compositing (full 32-bit ARGB)
            BufferedImage master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D masterG = master.createGraphics();

            for (int i = 0; i < numFrames; i++) {
                // Read frame image
                BufferedImage frameImage = reader.read(i);

                // Read frame metadata for timing and positioning
                IIOMetadata metadata = reader.getImageMetadata(i);
                int frameDelay = defaultFrameDelay;
                int frameX = 0, frameY = 0;
                String disposalMethod = "none";

                if (metadata != null) {
                    String formatName = metadata.getNativeMetadataFormatName();
                    IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(formatName);

                    // Get graphic control extension for timing
                    NodeList gceNodes = root.getElementsByTagName("GraphicControlExtension");
                    if (gceNodes.getLength() > 0) {
                        IIOMetadataNode gce = (IIOMetadataNode) gceNodes.item(0);
                        String delayStr = gce.getAttribute("delayTime");
                        if (delayStr != null && !delayStr.isEmpty()) {
                            // GIF delay is in 1/100ths of a second
                            int delayCs = Integer.parseInt(delayStr);
                            frameDelay = delayCs * 10; // Convert to milliseconds
                            if (frameDelay < 20) frameDelay = 100; // Browser-like minimum
                        }
                        String disposal = gce.getAttribute("disposalMethod");
                        if (disposal != null) {
                            disposalMethod = disposal;
                        }
                    }

                    // Get image descriptor for position
                    NodeList descNodes = root.getElementsByTagName("ImageDescriptor");
                    if (descNodes.getLength() > 0) {
                        IIOMetadataNode desc = (IIOMetadataNode) descNodes.item(0);
                        String leftStr = desc.getAttribute("imageLeftPosition");
                        String topStr = desc.getAttribute("imageTopPosition");
                        if (leftStr != null && !leftStr.isEmpty()) {
                            frameX = Integer.parseInt(leftStr);
                        }
                        if (topStr != null && !topStr.isEmpty()) {
                            frameY = Integer.parseInt(topStr);
                        }
                    }
                }

                // Draw frame onto master canvas at correct position
                masterG.drawImage(frameImage, frameX, frameY, null);

                // Create a copy of the current composited frame (full 32-bit ARGB)
                BufferedImage compositedFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = compositedFrame.createGraphics();
                g.drawImage(master, 0, 0, null);
                g.dispose();

                frames.add(scaleToIconSize(compositedFrame));
                delays.add(frameDelay);

                // Handle disposal method for next frame
                if ("restoreToBackgroundColor".equals(disposalMethod)) {
                    // Clear the area this frame occupied
                    masterG.setComposite(AlphaComposite.Clear);
                    masterG.fillRect(frameX, frameY, frameImage.getWidth(), frameImage.getHeight());
                    masterG.setComposite(AlphaComposite.SrcOver);
                } else if ("restoreToPrevious".equals(disposalMethod)) {
                    // Restore to previous frame (simplified: just clear)
                    masterG.setComposite(AlphaComposite.Clear);
                    masterG.fillRect(frameX, frameY, frameImage.getWidth(), frameImage.getHeight());
                    masterG.setComposite(AlphaComposite.SrcOver);
                }
                // "none" or "doNotDispose" - leave the frame as-is
            }

            masterG.dispose();
            reader.dispose();
            stream.close();

            // Store the frame delays
            this.frameDelays = delays;

        } catch (Exception e) {
            // Return whatever frames we got, use default timing
            if (frames.isEmpty()) return null;
        }

        return frames;
    }

    /**
     * Scales an image to ICON_SIZE if needed.
     */
    private BufferedImage scaleToIconSize(BufferedImage img) {
        if (img.getWidth() == ICON_SIZE && img.getHeight() == ICON_SIZE) {
            return img;
        }
        BufferedImage scaled = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(img, 0, 0, ICON_SIZE, ICON_SIZE, null);
        g.dispose();
        return scaled;
    }

    /**
     * Updates the animation frame if this item has an animated sprite.
     * Uses per-frame timing from the GIF metadata for accurate playback.
     */
    public void updateAnimation() {
        if (!isAnimated || animationFrames == null || animationFrames.size() <= 1) return;

        long currentTime = System.currentTimeMillis();

        // Get the delay for the current frame (use default if not available)
        int currentDelay = defaultFrameDelay;
        if (frameDelays != null && currentFrame < frameDelays.size()) {
            currentDelay = frameDelays.get(currentFrame);
        }

        if (currentTime - lastFrameTime >= currentDelay) {
            currentFrame = (currentFrame + 1) % animationFrames.size();
            sprite = animationFrames.get(currentFrame);
            lastFrameTime = currentTime;
        }
    }

    /**
     * Generates a colored icon based on item type with unique variations based on name.
     */
    private BufferedImage generateItemIcon(String type, String name) {
        BufferedImage icon = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Get unique colors based on item name for variety
        Color primary = getUniqueItemColor(type, name);
        Color secondary = primary.darker();
        Color outline = Color.BLACK;

        // Draw based on item type
        switch (type.toLowerCase()) {
            case "weapon":
                // Sword shape with unique styling
                drawWeaponIcon(g, primary, secondary, name);
                break;

            case "ranged_weapon":
            case "bow":
                // Bow/crossbow shape
                drawRangedWeaponIcon(g, primary, secondary, name);
                break;

            case "armor":
                // Armor piece
                drawArmorIcon(g, primary, secondary, name);
                break;

            case "potion":
                // Potion bottle with unique liquid color
                drawPotionIcon(g, primary, name);
                break;

            case "food":
                // Food with variety
                drawFoodIcon(g, primary, secondary, name);
                break;

            case "tool":
                // Tool shape
                drawToolIcon(g, primary, secondary, name);
                break;

            case "ammo":
                // Arrow/bolt
                drawAmmoIcon(g, primary, name);
                break;

            case "collectible":
            case "material":
                // Gem/crystal shape
                drawMaterialIcon(g, primary, secondary, name);
                break;

            case "key":
                // Key with unique color
                drawKeyIcon(g, primary, name);
                break;

            case "lantern":
                // Lantern
                drawLanternIcon(g, primary, name);
                break;

            case "throwable":
                // Throwable item
                drawThrowableIcon(g, primary, secondary, name);
                break;

            default:
                // Default square with item initial
                g.setColor(primary);
                g.fillRoundRect(2, 2, 12, 12, 4, 4);
                g.setColor(secondary);
                g.drawRoundRect(2, 2, 12, 12, 4, 4);
                // Draw first letter
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 10));
                String initial = name.length() > 0 ? name.substring(0, 1).toUpperCase() : "?";
                g.drawString(initial, 5, 12);
        }

        g.dispose();
        return icon;
    }

    /**
     * Gets a unique color based on item type and name for variety.
     */
    private Color getUniqueItemColor(String type, String name) {
        Color baseColor = getTypeColor(type);

        // Hash the name to get a consistent variation
        int hash = name.hashCode();
        float hueShift = ((hash & 0xFF) - 128) / 512.0f;  // -0.25 to 0.25

        float[] hsb = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
        hsb[0] = (hsb[0] + hueShift + 1.0f) % 1.0f;  // Shift hue
        hsb[1] = Math.min(1.0f, hsb[1] + ((hash >> 8) & 0xFF) / 1024.0f);  // Adjust saturation
        hsb[2] = Math.min(1.0f, hsb[2] + ((hash >> 16) & 0xFF) / 1024.0f); // Adjust brightness

        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

    private void drawWeaponIcon(Graphics2D g, Color primary, Color secondary, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("axe")) {
            // Axe shape
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 4, 2, 11);  // Handle
            g.setColor(primary);
            g.fillArc(3, 2, 10, 8, 90, 180);  // Blade
            g.setColor(secondary);
            g.drawArc(3, 2, 10, 8, 90, 180);
        } else if (lowerName.contains("mace") || lowerName.contains("hammer")) {
            // Mace shape
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 6, 2, 9);  // Handle
            g.setColor(primary);
            g.fillOval(4, 2, 8, 8);  // Head
            g.setColor(secondary);
            g.drawOval(4, 2, 8, 8);
        } else if (lowerName.contains("dagger") || lowerName.contains("knife")) {
            // Dagger shape
            g.setColor(secondary);
            g.fillRect(7, 3, 2, 7);  // Short blade
            g.setColor(primary);
            int[] tipX = {8, 6, 10};
            int[] tipY = {1, 4, 4};
            g.fillPolygon(tipX, tipY, 3);
            g.setColor(new Color(139, 90, 43));
            g.fillRect(6, 10, 4, 4);  // Handle
        } else {
            // Default sword
            g.setColor(secondary);
            g.fillRect(7, 2, 2, 10);  // Blade
            g.setColor(primary);
            g.fillRect(6, 1, 4, 2);   // Tip
            g.setColor(new Color(139, 90, 43));  // Brown handle
            g.fillRect(6, 12, 4, 3);
            g.setColor(new Color(255, 215, 0));  // Gold guard
            g.fillRect(4, 11, 8, 2);
        }
    }

    private void drawRangedWeaponIcon(Graphics2D g, Color primary, Color secondary, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("crossbow")) {
            // Crossbow shape
            g.setColor(new Color(139, 90, 43));
            g.fillRect(5, 6, 6, 2);   // Stock
            g.fillRect(7, 4, 2, 8);   // Vertical part
            g.setColor(primary);
            g.fillRect(2, 5, 12, 2);  // Arms
            g.setColor(Color.WHITE);
            g.drawLine(2, 6, 14, 6);  // String
        } else if (lowerName.contains("wand") || lowerName.contains("staff")) {
            // Magic wand/staff
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 4, 2, 10);  // Shaft
            g.setColor(primary);
            g.fillOval(5, 2, 6, 6);   // Magic orb
            g.setColor(Color.WHITE);
            g.fillOval(6, 3, 3, 3);   // Sparkle
        } else {
            // Default bow
            g.setColor(new Color(139, 90, 43));
            g.setStroke(new BasicStroke(2));
            g.drawArc(3, 2, 10, 12, 30, 120);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(1));
            g.drawLine(8, 2, 8, 14);
        }
    }

    private void drawArmorIcon(Graphics2D g, Color primary, Color secondary, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("helmet") || lowerName.contains("hat")) {
            // Helmet shape
            g.setColor(secondary);
            g.fillRoundRect(3, 4, 10, 10, 3, 3);
            g.setColor(primary);
            g.fillArc(3, 2, 10, 10, 0, 180);  // Dome
        } else if (lowerName.contains("boot") || lowerName.contains("shoe")) {
            // Boot shape
            g.setColor(secondary);
            g.fillRect(3, 6, 8, 8);
            g.setColor(primary);
            g.fillRoundRect(2, 10, 10, 5, 2, 2);  // Foot
        } else if (lowerName.contains("legging") || lowerName.contains("pant")) {
            // Leggings shape
            g.setColor(secondary);
            g.fillRect(4, 2, 8, 6);  // Waist
            g.setColor(primary);
            g.fillRect(4, 7, 3, 7);   // Left leg
            g.fillRect(9, 7, 3, 7);   // Right leg
        } else {
            // Default chestplate
            g.setColor(secondary);
            g.fillRoundRect(3, 3, 10, 11, 3, 3);
            g.setColor(primary);
            g.fillRoundRect(4, 4, 8, 9, 2, 2);
            g.setColor(secondary);
            g.fillRect(5, 5, 6, 3);  // Neck hole
        }
    }

    private void drawPotionIcon(Graphics2D g, Color primary, String name) {
        // Potion bottle with unique liquid color
        g.setColor(new Color(200, 200, 255, 180));  // Glass
        g.fillOval(4, 6, 8, 8);  // Bottle body
        g.setColor(primary);  // Liquid based on type
        g.fillOval(5, 8, 6, 5);
        g.setColor(new Color(139, 90, 43));  // Cork
        g.fillRect(6, 3, 4, 4);
        // Add sparkle
        g.setColor(new Color(255, 255, 255, 150));
        g.fillOval(6, 8, 2, 2);
    }

    private void drawFoodIcon(Graphics2D g, Color primary, Color secondary, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("bread") || lowerName.contains("loaf")) {
            // Bread shape
            g.setColor(primary);
            g.fillRoundRect(2, 6, 12, 8, 4, 4);
            g.setColor(secondary);
            g.drawRoundRect(2, 6, 12, 8, 4, 4);
        } else if (lowerName.contains("apple") || lowerName.contains("fruit")) {
            // Apple shape
            g.setColor(primary);
            g.fillOval(3, 4, 10, 10);
            g.setColor(new Color(80, 50, 20));
            g.fillRect(7, 2, 2, 3);  // Stem
            g.setColor(new Color(50, 150, 50));
            g.fillOval(8, 1, 4, 3);  // Leaf
        } else if (lowerName.contains("meat") || lowerName.contains("steak")) {
            // Meat shape
            g.setColor(primary);
            g.fillRoundRect(2, 4, 12, 10, 3, 3);
            g.setColor(secondary);
            g.fillRect(4, 6, 8, 2);  // Grill marks
            g.fillRect(4, 10, 8, 2);
        } else {
            // Default food (cheese wedge)
            g.setColor(primary);
            int[] cheeseX = {2, 14, 8};
            int[] cheeseY = {12, 12, 4};
            g.fillPolygon(cheeseX, cheeseY, 3);
            g.setColor(secondary);
            g.fillOval(5, 8, 2, 2);  // Hole
            g.fillOval(9, 9, 2, 2);
        }
    }

    private void drawToolIcon(Graphics2D g, Color primary, Color secondary, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("shovel")) {
            // Shovel shape
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 2, 2, 10);  // Handle
            g.setColor(primary);
            g.fillRoundRect(4, 10, 8, 5, 2, 2);  // Blade
        } else if (lowerName.contains("axe")) {
            // Tool axe
            g.setColor(new Color(139, 90, 43));
            g.fillRect(7, 4, 2, 11);  // Handle
            g.setColor(primary);
            g.fillArc(3, 2, 10, 8, 90, 180);  // Blade
        } else {
            // Default pickaxe
            g.setColor(new Color(139, 90, 43));  // Handle
            g.fillRect(7, 6, 2, 9);
            g.setColor(primary);  // Head
            g.fillRect(3, 3, 10, 4);
            g.setColor(secondary);
            g.fillRect(4, 4, 8, 2);
        }
    }

    private void drawAmmoIcon(Graphics2D g, Color primary, String name) {
        // Arrow/bolt
        g.setColor(new Color(139, 90, 43));  // Shaft
        g.fillRect(7, 4, 2, 10);
        g.setColor(Color.GRAY);  // Tip
        int[] tipX = {8, 5, 11};
        int[] tipY = {2, 5, 5};
        g.fillPolygon(tipX, tipY, 3);
        g.setColor(primary);  // Fletching
        g.fillRect(6, 12, 4, 2);
    }

    private void drawMaterialIcon(Graphics2D g, Color primary, Color secondary, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("ingot") || lowerName.contains("bar")) {
            // Ingot shape
            int[] ingotX = {2, 4, 12, 14, 12, 4};
            int[] ingotY = {10, 6, 6, 10, 14, 14};
            g.setColor(primary);
            g.fillPolygon(ingotX, ingotY, 6);
            g.setColor(secondary);
            g.drawPolygon(ingotX, ingotY, 6);
        } else if (lowerName.contains("crystal") || lowerName.contains("gem") || lowerName.contains("diamond")) {
            // Crystal shape
            int[] gemX = {8, 3, 5, 11, 13};
            int[] gemY = {2, 6, 14, 14, 6};
            g.setColor(primary);
            g.fillPolygon(gemX, gemY, 5);
            g.setColor(new Color(255, 255, 255, 100));
            g.fillRect(5, 5, 3, 4);  // Shine
            g.setColor(secondary);
            g.drawPolygon(gemX, gemY, 5);
        } else {
            // Default ore/chunk
            g.setColor(secondary);
            g.fillRoundRect(3, 4, 10, 10, 3, 3);
            g.setColor(primary);
            g.fillOval(5, 6, 4, 4);
            g.fillOval(8, 9, 3, 3);
        }
    }

    private void drawKeyIcon(Graphics2D g, Color primary, String name) {
        g.setColor(primary);
        g.fillOval(3, 3, 6, 6);  // Handle
        g.fillRect(7, 6, 7, 2);  // Shaft
        g.fillRect(11, 6, 2, 4);  // Teeth
        g.fillRect(13, 6, 2, 3);
        g.setColor(primary.darker());
        g.drawOval(4, 4, 4, 4);  // Handle hole
    }

    private void drawLanternIcon(Graphics2D g, Color primary, String name) {
        g.setColor(new Color(139, 90, 43));  // Frame
        g.fillRect(5, 3, 6, 2);
        g.fillRect(5, 12, 6, 2);
        g.setColor(new Color(255, 200, 100, 200));  // Glow
        g.fillOval(4, 4, 8, 9);
        g.setColor(primary);  // Flame
        g.fillOval(6, 6, 4, 5);
    }

    private void drawThrowableIcon(Graphics2D g, Color primary, Color secondary, String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("knife")) {
            // Throwing knife
            g.setColor(secondary);
            g.fillRect(7, 2, 2, 8);  // Blade
            g.setColor(primary);
            int[] tipX = {8, 6, 10};
            int[] tipY = {1, 3, 3};
            g.fillPolygon(tipX, tipY, 3);
            g.setColor(new Color(139, 90, 43));
            g.fillRect(6, 10, 4, 4);  // Handle
        } else if (lowerName.contains("bomb")) {
            // Bomb
            g.setColor(Color.DARK_GRAY);
            g.fillOval(3, 4, 10, 10);
            g.setColor(new Color(255, 200, 0));
            g.fillRect(7, 2, 2, 4);  // Fuse
            g.setColor(Color.RED);
            g.fillOval(7, 1, 2, 2);  // Spark
        } else {
            // Default rock
            g.setColor(primary);
            g.fillOval(3, 4, 10, 10);
            g.setColor(secondary);
            g.drawOval(3, 4, 10, 10);
        }
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

    // ==================== Stack Methods ====================

    /**
     * Gets the current stack count.
     */
    public int getStackCount() {
        return stackCount;
    }

    /**
     * Sets the stack count.
     */
    public void setStackCount(int count) {
        this.stackCount = Math.max(1, Math.min(count, maxStackSize));
    }

    /**
     * Gets the maximum stack size for this item.
     */
    public int getMaxStackSize() {
        return maxStackSize;
    }

    /**
     * Sets the maximum stack size.
     */
    public void setMaxStackSize(int max) {
        this.maxStackSize = Math.max(1, max);
    }

    /**
     * Checks if this item is stackable.
     */
    public boolean isStackable() {
        return maxStackSize > 1;
    }

    /**
     * Checks if this item can stack with another item.
     * Items can stack if they have the same ID or name and type.
     */
    public boolean canStackWith(ItemEntity other) {
        if (other == null || !isStackable()) return false;
        if (stackCount >= maxStackSize) return false;

        // Match by item ID if both have one
        if (itemId != null && other.itemId != null) {
            return itemId.equals(other.itemId);
        }

        // Fallback to matching by name and type
        return itemName.equals(other.itemName) && itemType.equals(other.itemType);
    }

    /**
     * Adds items from another stack to this stack.
     * @param other The stack to add from
     * @return The number of items that couldn't be added (overflow)
     */
    public int addToStack(ItemEntity other) {
        if (!canStackWith(other)) return other.stackCount;

        int spaceAvailable = maxStackSize - stackCount;
        int toAdd = Math.min(spaceAvailable, other.stackCount);

        stackCount += toAdd;
        other.stackCount -= toAdd;

        return other.stackCount; // Return remaining
    }

    /**
     * Removes one item from the stack.
     * @return true if an item was removed, false if stack is empty
     */
    public boolean decrementStack() {
        if (stackCount > 0) {
            stackCount--;
            return true;
        }
        return false;
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
            // Update animation frame if animated
            updateAnimation();

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
        // Update animation frame when sprite is accessed (for inventory display)
        updateAnimation();
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