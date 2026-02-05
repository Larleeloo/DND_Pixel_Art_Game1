package com.ambermoongame.entity.item;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

import com.ambermoongame.block.BlockEntity;
import com.ambermoongame.entity.Entity;
import com.ambermoongame.graphics.AndroidAssetLoader;
import com.ambermoongame.input.TouchInputManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a collectible item in the game world.
 * Can be linked to an Item from ItemRegistry for full properties.
 * Supports animated sprites via AndroidAssetLoader.ImageAsset.
 * Features Borderlands-style rarity light beams and physics for loot drops.
 *
 * Conversion notes:
 * - java.awt.Image/BufferedImage -> android.graphics.Bitmap
 * - java.awt.Graphics/Graphics2D -> android.graphics.Canvas + Paint
 * - java.awt.Rectangle           -> android.graphics.Rect
 * - java.awt.Color               -> android.graphics.Color (int)
 * - java.awt.Font/FontMetrics    -> Paint.setTextSize()/measureText()
 * - java.awt.RenderingHints      -> (not needed; pixel art scaling via Bitmap.createScaledBitmap)
 * - java.awt.AlphaComposite      -> Paint.setAlpha()
 * - javax.imageio GIF loading    -> AndroidAssetLoader.ImageAsset
 * - java.awt.BasicStroke          -> Paint.setStrokeWidth()
 * - fillPolygon                   -> Canvas.drawPath(Path)
 * - fillOval                      -> Canvas.drawOval(RectF) / drawCircle
 * - fillRoundRect                 -> Canvas.drawRoundRect(RectF)
 * - InputManager                  -> TouchInputManager
 */
public class ItemEntity extends Entity {

    private static final String TAG = "ItemEntity";

    private Bitmap sprite;
    private int width, height;
    private String itemName;
    private String itemType;
    private String itemId;  // Registry ID for linking to Item properties
    private Item linkedItem;  // Linked Item with full properties
    public boolean collected; // Made public so it can be reset when dropped
    private float bobOffset; // For floating animation
    private float bobSpeed;

    // Physics properties for loot dropping
    private double velocityX = 0;
    private double velocityY = 0;
    private double gravity = 0.5;
    private boolean hasPhysics = false;
    private boolean isGrounded = false;
    private int groundY = 720; // Default ground level
    private double bounceMultiplier = 0.6;
    private int bounceCount = 0;
    private int maxBounces = 3;

    // Reference to entity list for block collision detection
    private List<Entity> entityList = null;

    // Rarity light beam properties (Borderlands style)
    private boolean showLightBeam = false;
    private float lightBeamPhase = 0;
    private float lightBeamSpeed = 0.03f;
    private static final Random random = new Random();

    // Color mask fields for tinting items
    private int maskRed = 255;
    private int maskGreen = 255;
    private int maskBlue = 255;
    private boolean hasColorMask = false;
    private Bitmap tintedSprite; // Cached tinted version

    // Stack count for stackable items
    private int stackCount = 1;
    private int maxStackSize = 16;

    // Animation support via AndroidAssetLoader.ImageAsset
    private AndroidAssetLoader.ImageAsset imageAsset;
    private long animationStartTime = System.currentTimeMillis();

    public static final int SCALE = 3;
    private static final int BASE_ICON_SIZE = 16;

    // Original texture dimensions (preserved from source)
    private int textureWidth = BASE_ICON_SIZE;
    private int textureHeight = BASE_ICON_SIZE;

    // Reusable drawing objects (avoid per-frame allocation)
    private final Paint drawPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint glowPaint = new Paint();
    private final Paint beamPaint = new Paint();
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();
    private final RectF ovalRect = new RectF();
    private final Path polyPath = new Path();

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
        AndroidAssetLoader.ImageAsset asset = AndroidAssetLoader.load(spritePath);
        if (asset != null && asset.bitmap != null && asset.width > 1) {
            this.sprite = asset.bitmap;
            this.imageAsset = asset;
            this.textureWidth = asset.width;
            this.textureHeight = asset.height;
            this.width = BASE_ICON_SIZE * SCALE;
            this.height = BASE_ICON_SIZE * SCALE;
        } else {
            this.sprite = generateItemIcon(itemType, itemName);
            this.textureWidth = BASE_ICON_SIZE;
            this.textureHeight = BASE_ICON_SIZE;
            this.width = BASE_ICON_SIZE * SCALE;
            this.height = BASE_ICON_SIZE * SCALE;
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
            if (linkedItem.isStackable()) {
                this.maxStackSize = linkedItem.getMaxStackSize();
            }
        } else {
            this.itemName = itemId;
            this.itemType = "unknown";
        }

        // Try to load sprite from assets, fall back to procedural generation
        this.sprite = loadOrGenerateSprite(itemId, itemType, itemName);
        this.width = BASE_ICON_SIZE * SCALE;
        this.height = BASE_ICON_SIZE * SCALE;
    }

    /**
     * Attempts to load a sprite from assets, falls back to procedural generation.
     * Uses AndroidAssetLoader for asset path resolution.
     */
    private Bitmap loadOrGenerateSprite(String itemId, String type, String name) {
        if (itemId != null && !itemId.isEmpty()) {
            String[] paths = {
                "items/" + itemId + "/idle.gif",  // New folder structure
                "items/" + itemId + ".gif",       // Legacy single file
                "items/" + type + "/" + itemId + ".gif",
                "items/" + itemId + "/idle.png",
                "items/" + itemId + ".png",
                "items/" + type + "/" + itemId + ".png"
            };

            for (String path : paths) {
                if (AndroidAssetLoader.exists(path)) {
                    AndroidAssetLoader.ImageAsset asset = AndroidAssetLoader.load(path);
                    if (asset != null && asset.bitmap != null) {
                        this.imageAsset = asset;
                        this.textureWidth = asset.width;
                        this.textureHeight = asset.height;
                        return asset.bitmap;
                    }
                }
            }
        }

        // Fall back to procedural generation
        this.textureWidth = BASE_ICON_SIZE;
        this.textureHeight = BASE_ICON_SIZE;
        return generateItemIcon(type, name);
    }

    /**
     * Updates the animation frame if this item has an animated sprite.
     * Uses AndroidAssetLoader.ImageAsset for frame cycling.
     */
    public void updateAnimation() {
        if (imageAsset != null && imageAsset.isAnimated) {
            long elapsed = System.currentTimeMillis() - animationStartTime;
            sprite = imageAsset.getFrame(elapsed);
        }
    }

    /**
     * Generates a colored icon based on item type with unique variations based on name.
     * Uses Canvas + Paint instead of Graphics2D.
     */
    private Bitmap generateItemIcon(String type, String name) {
        Bitmap icon = Bitmap.createBitmap(BASE_ICON_SIZE, BASE_ICON_SIZE, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(icon);
        Paint p = new Paint();
        p.setAntiAlias(true);

        int primary = getUniqueItemColor(type, name);
        int secondary = darkenColor(primary);

        switch (type.toLowerCase()) {
            case "weapon":
                drawWeaponIcon(c, p, primary, secondary, name);
                break;
            case "ranged_weapon":
            case "bow":
                drawRangedWeaponIcon(c, p, primary, secondary, name);
                break;
            case "armor":
                drawArmorIcon(c, p, primary, secondary, name);
                break;
            case "potion":
                drawPotionIcon(c, p, primary, name);
                break;
            case "food":
                drawFoodIcon(c, p, primary, secondary, name);
                break;
            case "tool":
                drawToolIcon(c, p, primary, secondary, name);
                break;
            case "ammo":
                drawAmmoIcon(c, p, primary, name);
                break;
            case "collectible":
            case "material":
                drawMaterialIcon(c, p, primary, secondary, name);
                break;
            case "key":
                drawKeyIcon(c, p, primary, name);
                break;
            case "lantern":
                drawLanternIcon(c, p, primary, name);
                break;
            case "throwable":
                drawThrowableIcon(c, p, primary, secondary, name);
                break;
            default:
                // Default square with item initial
                p.setStyle(Paint.Style.FILL);
                p.setColor(primary);
                ovalRect.set(2, 2, 14, 14);
                c.drawRoundRect(ovalRect, 4, 4, p);
                p.setStyle(Paint.Style.STROKE);
                p.setColor(secondary);
                c.drawRoundRect(ovalRect, 4, 4, p);
                // Draw first letter
                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.WHITE);
                p.setTextSize(10);
                p.setTypeface(Typeface.DEFAULT_BOLD);
                String initial = name.length() > 0 ? name.substring(0, 1).toUpperCase() : "?";
                c.drawText(initial, 5, 12, p);
        }

        return icon;
    }

    /**
     * Gets a unique color based on item type and name for variety.
     * Uses Color.colorToHSV/HSVToColor instead of java.awt.Color.RGBtoHSB/getHSBColor.
     */
    private int getUniqueItemColor(String type, String name) {
        int baseColor = getTypeColor(type);

        int hash = name.hashCode();
        float hueShift = ((hash & 0xFF) - 128) / 512.0f;

        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[0] = (hsv[0] + hueShift * 360 + 360) % 360;
        hsv[1] = Math.min(1.0f, hsv[1] + ((hash >> 8) & 0xFF) / 1024.0f);
        hsv[2] = Math.min(1.0f, hsv[2] + ((hash >> 16) & 0xFF) / 1024.0f);

        return Color.HSVToColor(hsv);
    }

    private void drawWeaponIcon(Canvas c, Paint p, int primary, int secondary, String name) {
        String lowerName = name.toLowerCase();
        p.setStyle(Paint.Style.FILL);

        if (lowerName.contains("axe")) {
            p.setColor(Color.rgb(139, 90, 43));
            c.drawRect(7, 4, 9, 15, p);
            p.setColor(primary);
            ovalRect.set(3, 2, 13, 10);
            c.drawArc(ovalRect, 90, 180, true, p);
            p.setStyle(Paint.Style.STROKE);
            p.setColor(secondary);
            c.drawArc(ovalRect, 90, 180, true, p);
        } else if (lowerName.contains("mace") || lowerName.contains("hammer")) {
            p.setColor(Color.rgb(139, 90, 43));
            c.drawRect(7, 6, 9, 15, p);
            p.setColor(primary);
            ovalRect.set(4, 2, 12, 10);
            c.drawOval(ovalRect, p);
            p.setStyle(Paint.Style.STROKE);
            p.setColor(secondary);
            c.drawOval(ovalRect, p);
        } else if (lowerName.contains("dagger") || lowerName.contains("knife")) {
            p.setColor(secondary);
            c.drawRect(7, 3, 9, 10, p);
            p.setColor(primary);
            polyPath.reset();
            polyPath.moveTo(8, 1);
            polyPath.lineTo(6, 4);
            polyPath.lineTo(10, 4);
            polyPath.close();
            c.drawPath(polyPath, p);
            p.setColor(Color.rgb(139, 90, 43));
            c.drawRect(6, 10, 10, 14, p);
        } else {
            // Default sword
            p.setColor(secondary);
            c.drawRect(7, 2, 9, 12, p);
            p.setColor(primary);
            c.drawRect(6, 1, 10, 3, p);
            p.setColor(Color.rgb(139, 90, 43));
            c.drawRect(6, 12, 10, 15, p);
            p.setColor(Color.rgb(255, 215, 0));
            c.drawRect(4, 11, 12, 13, p);
        }
    }

    private void drawRangedWeaponIcon(Canvas c, Paint p, int primary, int secondary, String name) {
        String lowerName = name.toLowerCase();
        p.setStyle(Paint.Style.FILL);

        if (lowerName.contains("crossbow")) {
            p.setColor(Color.rgb(139, 90, 43));
            c.drawRect(5, 6, 11, 8, p);
            c.drawRect(7, 4, 9, 12, p);
            p.setColor(primary);
            c.drawRect(2, 5, 14, 7, p);
            p.setColor(Color.WHITE);
            p.setStrokeWidth(1);
            p.setStyle(Paint.Style.STROKE);
            c.drawLine(2, 6, 14, 6, p);
        } else if (lowerName.contains("wand") || lowerName.contains("staff")) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(139, 90, 43));
            c.drawRect(7, 4, 9, 14, p);
            p.setColor(primary);
            ovalRect.set(5, 2, 11, 8);
            c.drawOval(ovalRect, p);
            p.setColor(Color.WHITE);
            ovalRect.set(6, 3, 9, 6);
            c.drawOval(ovalRect, p);
        } else {
            // Default bow
            p.setColor(Color.rgb(139, 90, 43));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2);
            ovalRect.set(3, 2, 13, 14);
            c.drawArc(ovalRect, -60, 120, false, p);
            p.setColor(Color.WHITE);
            p.setStrokeWidth(1);
            c.drawLine(8, 2, 8, 14, p);
        }
    }

    private void drawArmorIcon(Canvas c, Paint p, int primary, int secondary, String name) {
        String lowerName = name.toLowerCase();
        p.setStyle(Paint.Style.FILL);

        if (lowerName.contains("helmet") || lowerName.contains("hat")) {
            p.setColor(secondary);
            ovalRect.set(3, 4, 13, 14);
            c.drawRoundRect(ovalRect, 3, 3, p);
            p.setColor(primary);
            ovalRect.set(3, 2, 13, 12);
            c.drawArc(ovalRect, 0, -180, true, p);
        } else if (lowerName.contains("boot") || lowerName.contains("shoe")) {
            p.setColor(secondary);
            c.drawRect(3, 6, 11, 14, p);
            p.setColor(primary);
            ovalRect.set(2, 10, 12, 15);
            c.drawRoundRect(ovalRect, 2, 2, p);
        } else if (lowerName.contains("legging") || lowerName.contains("pant")) {
            p.setColor(secondary);
            c.drawRect(4, 2, 12, 8, p);
            p.setColor(primary);
            c.drawRect(4, 7, 7, 14, p);
            c.drawRect(9, 7, 12, 14, p);
        } else {
            // Default chestplate
            p.setColor(secondary);
            ovalRect.set(3, 3, 13, 14);
            c.drawRoundRect(ovalRect, 3, 3, p);
            p.setColor(primary);
            ovalRect.set(4, 4, 12, 13);
            c.drawRoundRect(ovalRect, 2, 2, p);
            p.setColor(secondary);
            c.drawRect(5, 5, 11, 8, p);
        }
    }

    private void drawPotionIcon(Canvas c, Paint p, int primary, String name) {
        p.setStyle(Paint.Style.FILL);
        // Glass
        p.setColor(Color.argb(180, 200, 200, 255));
        ovalRect.set(4, 6, 12, 14);
        c.drawOval(ovalRect, p);
        // Liquid
        p.setColor(primary);
        ovalRect.set(5, 8, 11, 13);
        c.drawOval(ovalRect, p);
        // Cork
        p.setColor(Color.rgb(139, 90, 43));
        c.drawRect(6, 3, 10, 7, p);
        // Sparkle
        p.setColor(Color.argb(150, 255, 255, 255));
        ovalRect.set(6, 8, 8, 10);
        c.drawOval(ovalRect, p);
    }

    private void drawFoodIcon(Canvas c, Paint p, int primary, int secondary, String name) {
        String lowerName = name.toLowerCase();
        p.setStyle(Paint.Style.FILL);

        if (lowerName.contains("bread") || lowerName.contains("loaf")) {
            p.setColor(primary);
            ovalRect.set(2, 6, 14, 14);
            c.drawRoundRect(ovalRect, 4, 4, p);
            p.setStyle(Paint.Style.STROKE);
            p.setColor(secondary);
            c.drawRoundRect(ovalRect, 4, 4, p);
        } else if (lowerName.contains("apple") || lowerName.contains("fruit")) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(primary);
            ovalRect.set(3, 4, 13, 14);
            c.drawOval(ovalRect, p);
            p.setColor(Color.rgb(80, 50, 20));
            c.drawRect(7, 2, 9, 5, p);
            p.setColor(Color.rgb(50, 150, 50));
            ovalRect.set(8, 1, 12, 4);
            c.drawOval(ovalRect, p);
        } else if (lowerName.contains("meat") || lowerName.contains("steak")) {
            p.setColor(primary);
            ovalRect.set(2, 4, 14, 14);
            c.drawRoundRect(ovalRect, 3, 3, p);
            p.setColor(secondary);
            c.drawRect(4, 6, 12, 8, p);
            c.drawRect(4, 10, 12, 12, p);
        } else {
            // Default cheese wedge
            p.setColor(primary);
            polyPath.reset();
            polyPath.moveTo(2, 12);
            polyPath.lineTo(14, 12);
            polyPath.lineTo(8, 4);
            polyPath.close();
            c.drawPath(polyPath, p);
            p.setColor(secondary);
            ovalRect.set(5, 8, 7, 10);
            c.drawOval(ovalRect, p);
            ovalRect.set(9, 9, 11, 11);
            c.drawOval(ovalRect, p);
        }
    }

    private void drawToolIcon(Canvas c, Paint p, int primary, int secondary, String name) {
        String lowerName = name.toLowerCase();
        p.setStyle(Paint.Style.FILL);

        if (lowerName.contains("shovel")) {
            p.setColor(Color.rgb(139, 90, 43));
            c.drawRect(7, 2, 9, 12, p);
            p.setColor(primary);
            ovalRect.set(4, 10, 12, 15);
            c.drawRoundRect(ovalRect, 2, 2, p);
        } else if (lowerName.contains("axe")) {
            p.setColor(Color.rgb(139, 90, 43));
            c.drawRect(7, 4, 9, 15, p);
            p.setColor(primary);
            ovalRect.set(3, 2, 13, 10);
            c.drawArc(ovalRect, 90, 180, true, p);
        } else {
            // Default pickaxe
            p.setColor(Color.rgb(139, 90, 43));
            c.drawRect(7, 6, 9, 15, p);
            p.setColor(primary);
            c.drawRect(3, 3, 13, 7, p);
            p.setColor(secondary);
            c.drawRect(4, 4, 12, 6, p);
        }
    }

    private void drawAmmoIcon(Canvas c, Paint p, int primary, String name) {
        p.setStyle(Paint.Style.FILL);
        // Shaft
        p.setColor(Color.rgb(139, 90, 43));
        c.drawRect(7, 4, 9, 14, p);
        // Tip
        p.setColor(Color.GRAY);
        polyPath.reset();
        polyPath.moveTo(8, 2);
        polyPath.lineTo(5, 5);
        polyPath.lineTo(11, 5);
        polyPath.close();
        c.drawPath(polyPath, p);
        // Fletching
        p.setColor(primary);
        c.drawRect(6, 12, 10, 14, p);
    }

    private void drawMaterialIcon(Canvas c, Paint p, int primary, int secondary, String name) {
        String lowerName = name.toLowerCase();
        p.setStyle(Paint.Style.FILL);

        if (lowerName.contains("ingot") || lowerName.contains("bar")) {
            p.setColor(primary);
            polyPath.reset();
            polyPath.moveTo(2, 10);
            polyPath.lineTo(4, 6);
            polyPath.lineTo(12, 6);
            polyPath.lineTo(14, 10);
            polyPath.lineTo(12, 14);
            polyPath.lineTo(4, 14);
            polyPath.close();
            c.drawPath(polyPath, p);
            p.setStyle(Paint.Style.STROKE);
            p.setColor(secondary);
            c.drawPath(polyPath, p);
        } else if (lowerName.contains("crystal") || lowerName.contains("gem") || lowerName.contains("diamond")) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(primary);
            polyPath.reset();
            polyPath.moveTo(8, 2);
            polyPath.lineTo(3, 6);
            polyPath.lineTo(5, 14);
            polyPath.lineTo(11, 14);
            polyPath.lineTo(13, 6);
            polyPath.close();
            c.drawPath(polyPath, p);
            p.setColor(Color.argb(100, 255, 255, 255));
            c.drawRect(5, 5, 8, 9, p);
            p.setStyle(Paint.Style.STROKE);
            p.setColor(secondary);
            c.drawPath(polyPath, p);
        } else {
            p.setStyle(Paint.Style.FILL);
            p.setColor(secondary);
            ovalRect.set(3, 4, 13, 14);
            c.drawRoundRect(ovalRect, 3, 3, p);
            p.setColor(primary);
            ovalRect.set(5, 6, 9, 10);
            c.drawOval(ovalRect, p);
            ovalRect.set(8, 9, 11, 12);
            c.drawOval(ovalRect, p);
        }
    }

    private void drawKeyIcon(Canvas c, Paint p, int primary, String name) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(primary);
        ovalRect.set(3, 3, 9, 9);
        c.drawOval(ovalRect, p);
        c.drawRect(7, 6, 14, 8, p);
        c.drawRect(11, 6, 13, 10, p);
        c.drawRect(13, 6, 15, 9, p);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(darkenColor(primary));
        ovalRect.set(4, 4, 8, 8);
        c.drawOval(ovalRect, p);
    }

    private void drawLanternIcon(Canvas c, Paint p, int primary, String name) {
        p.setStyle(Paint.Style.FILL);
        // Frame
        p.setColor(Color.rgb(139, 90, 43));
        c.drawRect(5, 3, 11, 5, p);
        c.drawRect(5, 12, 11, 14, p);
        // Glow
        p.setColor(Color.argb(200, 255, 200, 100));
        ovalRect.set(4, 4, 12, 13);
        c.drawOval(ovalRect, p);
        // Flame
        p.setColor(primary);
        ovalRect.set(6, 6, 10, 11);
        c.drawOval(ovalRect, p);
    }

    private void drawThrowableIcon(Canvas c, Paint p, int primary, int secondary, String name) {
        String lowerName = name.toLowerCase();
        p.setStyle(Paint.Style.FILL);

        if (lowerName.contains("knife")) {
            p.setColor(secondary);
            c.drawRect(7, 2, 9, 10, p);
            p.setColor(primary);
            polyPath.reset();
            polyPath.moveTo(8, 1);
            polyPath.lineTo(6, 3);
            polyPath.lineTo(10, 3);
            polyPath.close();
            c.drawPath(polyPath, p);
            p.setColor(Color.rgb(139, 90, 43));
            c.drawRect(6, 10, 10, 14, p);
        } else if (lowerName.contains("bomb")) {
            p.setColor(Color.DKGRAY);
            ovalRect.set(3, 4, 13, 14);
            c.drawOval(ovalRect, p);
            p.setColor(Color.rgb(255, 200, 0));
            c.drawRect(7, 2, 9, 6, p);
            p.setColor(Color.RED);
            ovalRect.set(7, 1, 9, 3);
            c.drawOval(ovalRect, p);
        } else {
            // Default rock
            p.setColor(primary);
            ovalRect.set(3, 4, 13, 14);
            c.drawOval(ovalRect, p);
            p.setStyle(Paint.Style.STROKE);
            p.setColor(secondary);
            c.drawOval(ovalRect, p);
        }
    }

    /**
     * Gets a color based on item type.
     */
    private int getTypeColor(String type) {
        switch (type.toLowerCase()) {
            case "weapon": return Color.rgb(192, 192, 192);
            case "ranged_weapon":
            case "bow": return Color.rgb(139, 90, 43);
            case "armor": return Color.rgb(100, 100, 150);
            case "potion": return Color.rgb(255, 100, 100);
            case "food": return Color.rgb(255, 200, 100);
            case "tool": return Color.rgb(100, 100, 100);
            case "ammo": return Color.rgb(150, 100, 50);
            case "collectible": return Color.rgb(100, 200, 255);
            case "material": return Color.rgb(200, 150, 100);
            case "key": return Color.rgb(255, 215, 0);
            case "lantern": return Color.rgb(255, 200, 50);
            default: return Color.rgb(180, 180, 180);
        }
    }

    /**
     * Darkens a color by reducing RGB values by ~30%.
     * Replacement for java.awt.Color.darker().
     */
    private int darkenColor(int color) {
        int r = (int) (Color.red(color) * 0.7f);
        int g = (int) (Color.green(color) * 0.7f);
        int b = (int) (Color.blue(color) * 0.7f);
        return Color.rgb(r, g, b);
    }

    // ==================== Linked Item Methods ====================

    public Item getLinkedItem() {
        return linkedItem;
    }

    public void setLinkedItem(Item item) {
        this.linkedItem = item;
        if (item != null) {
            this.itemName = item.getName();
            this.itemType = item.getCategory().name().toLowerCase();
        }
    }

    public String getItemId() {
        return itemId;
    }

    // ==================== Stack Methods ====================

    public int getStackCount() {
        return stackCount;
    }

    public void setStackCount(int count) {
        this.stackCount = Math.max(1, Math.min(count, maxStackSize));
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public void setMaxStackSize(int max) {
        this.maxStackSize = Math.max(1, max);
    }

    public boolean isStackable() {
        return maxStackSize > 1;
    }

    /**
     * Checks if this item can stack with another item.
     * Items can stack ONLY if they have the same registry ID.
     */
    public boolean canStackWith(ItemEntity other) {
        if (other == null || !isStackable()) return false;
        if (stackCount >= maxStackSize) return false;

        if (itemId != null && !itemId.isEmpty() && other.itemId != null && !other.itemId.isEmpty()) {
            return itemId.equals(other.itemId);
        }
        return false;
    }

    /**
     * Adds items from another stack to this stack.
     * @return The number of items that couldn't be added (overflow)
     */
    public int addToStack(ItemEntity other) {
        if (!canStackWith(other)) return other.stackCount;

        int spaceAvailable = maxStackSize - stackCount;
        int toAdd = Math.min(spaceAvailable, other.stackCount);

        stackCount += toAdd;
        other.stackCount -= toAdd;

        return other.stackCount;
    }

    /**
     * Removes one item from the stack.
     */
    public boolean decrementStack() {
        if (stackCount > 0) {
            stackCount--;
            return true;
        }
        return false;
    }

    @Override
    public Rect getBounds() {
        if (collected) return new Rect(0, 0, 0, 0);
        return new Rect(x, y + (int) bobOffset, x + width, y + (int) bobOffset + height);
    }

    @Override
    public void update(TouchInputManager input) {
        if (!collected) {
            // Update light beam animation
            lightBeamPhase += lightBeamSpeed;
            if (lightBeamPhase > Math.PI * 2) {
                lightBeamPhase -= Math.PI * 2;
            }

            // Physics update for loot drops
            if (hasPhysics && !isGrounded) {
                velocityY += gravity;

                x += (int) velocityX;
                int newY = y + (int) velocityY;

                // Block collision detection
                int landingY = groundY;
                boolean hitBlock = false;

                if (entityList != null) {
                    Rect futureYBounds = new Rect(x, newY, x + width, newY + height);
                    int itemBottom = y + height;

                    for (Entity e : entityList) {
                        if (e instanceof BlockEntity) {
                            BlockEntity block = (BlockEntity) e;
                            if (block.isSolid() && !block.isBroken()) {
                                Rect blockBounds = block.getBounds();

                                if (Rect.intersects(futureYBounds, blockBounds)) {
                                    int blockTop = blockBounds.top;
                                    if (velocityY > 0 && blockTop >= itemBottom - 4) {
                                        hitBlock = true;
                                        if (blockTop < landingY) {
                                            landingY = blockTop;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Check ground/block collision
                if (newY + height >= landingY) {
                    y = landingY - height;
                    bounceCount++;

                    if (bounceCount >= maxBounces || Math.abs(velocityY) < 2) {
                        isGrounded = true;
                        velocityX = 0;
                        velocityY = 0;
                    } else {
                        velocityY = -velocityY * bounceMultiplier;
                        velocityX *= 0.8;
                    }
                } else {
                    y = newY;
                }

                bobOffset = 0;
            } else {
                bobOffset = (float) (Math.sin(System.currentTimeMillis() * 0.003) * 8);
            }
        }
    }

    // ==================== Physics Methods ====================

    public void enablePhysics(double velX, double velY, int groundLevel) {
        this.hasPhysics = true;
        this.velocityX = velX;
        this.velocityY = velY;
        this.groundY = groundLevel;
        this.isGrounded = false;
        this.bounceCount = 0;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public void setHasPhysics(boolean hasPhysics) {
        this.hasPhysics = hasPhysics;
    }

    public void setGroundY(int groundY) {
        this.groundY = groundY;
    }

    public void setEntityList(List<Entity> entities) {
        this.entityList = entities;
    }

    // ==================== Light Beam Methods ====================

    public void setShowLightBeam(boolean show) {
        this.showLightBeam = show;
    }

    /**
     * Gets the rarity color for the light beam.
     * Returns Android int color instead of java.awt.Color.
     */
    public int getRarityColor() {
        if (linkedItem != null) {
            return linkedItem.getRarity().getColor();
        }
        return Color.WHITE;
    }

    /**
     * Draws the Borderlands-style rarity light beam.
     * Uses Canvas.drawPath() instead of Graphics2D.fillPolygon().
     */
    private void drawLightBeam(Canvas canvas) {
        if (!showLightBeam || collected) return;

        int rarityColor = getRarityColor();
        int beamHeight = height * 3;

        float fluctuation = (float) (Math.sin(lightBeamPhase) * 0.3 + 0.7);
        float glowPulse = (float) (Math.sin(lightBeamPhase * 2) * 0.5 + 0.5);

        int centerX = x + width / 2;
        int beamBottom = y + (int) bobOffset + height;
        int beamTop = beamBottom - beamHeight;

        beamPaint.setStyle(Paint.Style.FILL);

        // Draw multiple layers for glow effect
        for (int layer = 0; layer < 5; layer++) {
            float layerAlpha = (0.15f - layer * 0.025f) * fluctuation;
            int layerWidth = 20 + layer * 15;

            int alpha = (int) (layerAlpha * 255 * glowPulse);
            alpha = Math.max(0, Math.min(255, alpha));
            beamPaint.setColor(Color.argb(alpha, Color.red(rarityColor),
                    Color.green(rarityColor), Color.blue(rarityColor)));

            polyPath.reset();
            polyPath.moveTo(centerX - layerWidth / 2f, beamTop);
            polyPath.lineTo(centerX + layerWidth / 2f, beamTop);
            polyPath.lineTo(centerX + 5, beamBottom);
            polyPath.lineTo(centerX - 5, beamBottom);
            polyPath.close();
            canvas.drawPath(polyPath, beamPaint);
        }

        // Core beam
        int coreWidth = 10;
        int coreAlpha = (int) (180 * fluctuation);
        coreAlpha = Math.max(0, Math.min(255, coreAlpha));
        beamPaint.setColor(Color.argb(coreAlpha,
                Math.min(255, Color.red(rarityColor) + 50),
                Math.min(255, Color.green(rarityColor) + 50),
                Math.min(255, Color.blue(rarityColor) + 50)));

        polyPath.reset();
        polyPath.moveTo(centerX - coreWidth / 2f, beamTop);
        polyPath.lineTo(centerX + coreWidth / 2f, beamTop);
        polyPath.lineTo(centerX + 2, beamBottom);
        polyPath.lineTo(centerX - 2, beamBottom);
        polyPath.close();
        canvas.drawPath(polyPath, beamPaint);

        // Sparkle particles
        drawSparkles(canvas, centerX, beamTop, beamBottom, rarityColor, fluctuation);
    }

    /**
     * Draws sparkle particles along the light beam.
     */
    private void drawSparkles(Canvas canvas, int centerX, int beamTop, int beamBottom,
                               int color, float intensity) {
        long time = System.currentTimeMillis();
        int numSparkles = 5;

        for (int i = 0; i < numSparkles; i++) {
            float sparklePhase = (time * 0.002f + i * 1.2f) % 1.0f;
            int sparkleY = (int) (beamTop + (beamBottom - beamTop) * sparklePhase);
            int sparkleX = centerX + (int) (Math.sin(time * 0.003 + i) * 15);
            int sparkleSize = 3 + (int) (Math.sin(time * 0.005 + i * 2) * 2);

            float alpha = 1.0f - Math.abs(sparklePhase - 0.5f) * 2;
            alpha *= intensity;

            int sparkleAlpha = (int) (alpha * 200);
            sparkleAlpha = Math.max(0, Math.min(255, sparkleAlpha));

            beamPaint.setColor(Color.argb(sparkleAlpha,
                    Math.min(255, Color.red(color) + 100),
                    Math.min(255, Color.green(color) + 100),
                    Math.min(255, Color.blue(color) + 100)));

            ovalRect.set(sparkleX - sparkleSize / 2f, sparkleY - sparkleSize / 2f,
                    sparkleX + sparkleSize / 2f, sparkleY + sparkleSize / 2f);
            canvas.drawOval(ovalRect, beamPaint);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (!collected) {
            updateAnimation();

            // Draw rarity light beam behind item
            if (showLightBeam) {
                drawLightBeam(canvas);
            }

            // Draw glow effect
            int glowColor;
            if (showLightBeam && linkedItem != null) {
                int rarityColor = linkedItem.getRarity().getColor();
                glowColor = Color.argb(100, Color.red(rarityColor),
                        Color.green(rarityColor), Color.blue(rarityColor));
            } else {
                glowColor = Color.argb(100, 255, 255, 100);
            }
            glowPaint.setStyle(Paint.Style.FILL);
            glowPaint.setColor(glowColor);
            ovalRect.set(x - 5, y + bobOffset - 5, x + width + 5, y + bobOffset + height + 5);
            canvas.drawOval(ovalRect, glowPaint);

            // Draw sprite (nearest-neighbor via Bitmap.createScaledBitmap with filter=false)
            Bitmap spriteToDraw = (hasColorMask && tintedSprite != null) ? tintedSprite : sprite;
            if (spriteToDraw != null) {
                srcRect.set(0, 0, spriteToDraw.getWidth(), spriteToDraw.getHeight());
                dstRect.set(x, y + (int) bobOffset, x + width, y + (int) bobOffset + height);
                drawPaint.setFilterBitmap(false);  // Nearest-neighbor for pixel art
                canvas.drawBitmap(spriteToDraw, srcRect, dstRect, drawPaint);
            } else {
                // Fallback yellow square
                drawPaint.setColor(Color.YELLOW);
                drawPaint.setStyle(Paint.Style.FILL);
                canvas.drawRect(x, y + (int) bobOffset, x + width, y + (int) bobOffset + height, drawPaint);
            }

            // Draw item name below
            int textColor = Color.WHITE;
            if (showLightBeam && linkedItem != null) {
                textColor = linkedItem.getRarity().getColor();
            }

            textPaint.setTextSize(12);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            textPaint.setStyle(Paint.Style.FILL);

            float textWidth = textPaint.measureText(itemName);
            float textX = x + (width - textWidth) / 2;

            // Text shadow
            textPaint.setColor(Color.BLACK);
            canvas.drawText(itemName, textX + 1, y + height + bobOffset + 16, textPaint);

            // Text
            textPaint.setColor(textColor);
            canvas.drawText(itemName, textX, y + height + bobOffset + 15, textPaint);

            // Draw rarity label if showing light beam
            if (showLightBeam && linkedItem != null) {
                String rarityName = linkedItem.getRarity().getDisplayName();
                textPaint.setTextSize(10);
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));

                float rarityWidth = textPaint.measureText(rarityName);
                float rarityX = x + (width - rarityWidth) / 2;

                textPaint.setColor(Color.argb(150, 0, 0, 0));
                canvas.drawText(rarityName, rarityX + 1, y + height + bobOffset + 28, textPaint);

                textPaint.setColor(linkedItem.getRarity().getColor());
                canvas.drawText(rarityName, rarityX, y + height + bobOffset + 27, textPaint);
            }
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

    public Bitmap getSprite() {
        updateAnimation();
        return sprite;
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public static int getBaseIconSize() {
        return BASE_ICON_SIZE;
    }

    public double getTextureScaleFactor() {
        if (textureWidth <= 0) return 1.0;
        return (double) BASE_ICON_SIZE / textureWidth;
    }

    // ==================== Color Mask ====================

    /**
     * Applies an RGB color mask to the item sprite.
     * Uses Bitmap.getPixels()/setPixels() for bulk pixel processing.
     */
    public void setColorMask(int red, int green, int blue) {
        this.maskRed = Math.max(0, Math.min(255, red));
        this.maskGreen = Math.max(0, Math.min(255, green));
        this.maskBlue = Math.max(0, Math.min(255, blue));
        this.hasColorMask = true;
        this.tintedSprite = createTintedSprite();
    }

    public void clearColorMask() {
        this.hasColorMask = false;
        this.tintedSprite = null;
        this.maskRed = 255;
        this.maskGreen = 255;
        this.maskBlue = 255;
    }

    public boolean hasColorMask() {
        return hasColorMask;
    }

    public int[] getColorMask() {
        return new int[]{maskRed, maskGreen, maskBlue};
    }

    /**
     * Creates a tinted copy of the sprite using bulk pixel operations.
     */
    private Bitmap createTintedSprite() {
        if (sprite == null) return null;

        int w = sprite.getWidth();
        int h = sprite.getHeight();
        if (w <= 0 || h <= 0) return null;

        // Bulk pixel processing
        int[] pixels = new int[w * h];
        sprite.getPixels(pixels, 0, w, 0, 0, w, h);

        float rFactor = maskRed / 255.0f;
        float gFactor = maskGreen / 255.0f;
        float bFactor = maskBlue / 255.0f;

        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            int alpha = (argb >> 24) & 0xFF;

            if (alpha > 0) {
                int r = Math.min(255, Math.max(0, Math.round(((argb >> 16) & 0xFF) * rFactor)));
                int g = Math.min(255, Math.max(0, Math.round(((argb >> 8) & 0xFF) * gFactor)));
                int b = Math.min(255, Math.max(0, Math.round((argb & 0xFF) * bFactor)));
                pixels[i] = (alpha << 24) | (r << 16) | (g << 8) | b;
            }
        }

        Bitmap tinted = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        tinted.setPixels(pixels, 0, w, 0, 0, w, h);
        return tinted;
    }
}
