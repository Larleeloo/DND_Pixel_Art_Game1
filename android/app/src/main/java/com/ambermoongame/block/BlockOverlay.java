package com.ambermoongame.block;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Class defining overlay types that can be applied on top of base blocks.
 * Uses int constants instead of enum to avoid D8 compiler issues.
 *
 * Overlays are semi-transparent textures rendered on top of base block textures.
 * They can be used for:
 * - GRASS: Grass growing on top of dirt
 * - SNOW: Snow covering any block
 * - ICE: Frozen overlay with transparency
 * - MOSS: Moss growing on stone
 * - VINES: Vines on walls
 *
 * Overlays must be removed/broken before the base block can be mined.
 * Equivalent to block/BlockOverlay.java from the desktop version.
 */
public final class BlockOverlay {
    public static final int NONE = 0;
    public static final int GRASS = 1;
    public static final int SNOW = 2;
    public static final int ICE = 3;
    public static final int MOSS = 4;
    public static final int VINES = 5;
    public static final int COUNT = 6;

    private static final String[] TEXTURE_PATHS = {
        null,
        "assets/textures/blocks/overlays/grass_overlay.png",
        "assets/textures/blocks/overlays/snow_overlay.png",
        "assets/textures/blocks/overlays/ice_overlay.png",
        "assets/textures/blocks/overlays/moss_overlay.png",
        "assets/textures/blocks/overlays/vines_overlay.png"
    };

    private static final String[] DISPLAY_NAMES = {
        "None", "Grass", "Snow", "Ice", "Moss", "Vines"
    };

    private static final int[] BREAK_STEPS = {
        0, 1, 2, 3, 1, 1
    };

    private static final boolean[] BLOCKS_BASE_MINING = {
        false, true, true, true, true, true
    };

    private BlockOverlay() {}

    public static String getTexturePath(int overlay) {
        if (overlay >= 0 && overlay < COUNT) {
            return TEXTURE_PATHS[overlay];
        }
        return null;
    }

    public static String getDisplayName(int overlay) {
        if (overlay >= 0 && overlay < COUNT) {
            return DISPLAY_NAMES[overlay];
        }
        return "Unknown";
    }

    public static int getBreakSteps(int overlay) {
        if (overlay >= 0 && overlay < COUNT) {
            return BREAK_STEPS[overlay];
        }
        return 0;
    }

    public static boolean blocksBaseMining(int overlay) {
        if (overlay >= 0 && overlay < COUNT) {
            return BLOCKS_BASE_MINING[overlay];
        }
        return false;
    }

    public static String getName(int overlay) {
        switch (overlay) {
            case NONE: return "NONE";
            case GRASS: return "GRASS";
            case SNOW: return "SNOW";
            case ICE: return "ICE";
            case MOSS: return "MOSS";
            case VINES: return "VINES";
            default: return "UNKNOWN";
        }
    }

    /**
     * Find an overlay type by its name (case-insensitive).
     * @param name The name to search for
     * @return The matching overlay constant, or NONE as default
     */
    public static int fromName(String name) {
        if (name == null || name.isEmpty()) {
            return NONE;
        }

        String upperName = name.toUpperCase().replace(" ", "_");
        for (int i = 0; i < COUNT; i++) {
            if (getName(i).equals(upperName)) {
                return i;
            }
        }

        for (int i = 0; i < COUNT; i++) {
            if (DISPLAY_NAMES[i].equalsIgnoreCase(name)) {
                return i;
            }
        }

        return NONE;
    }

    /**
     * Generates a procedural overlay texture if the file doesn't exist.
     * Creates a semi-transparent texture programmatically.
     *
     * @param overlay The overlay type constant
     * @param size The size of the texture to generate (width and height)
     * @return Generated Bitmap
     */
    public static Bitmap generateTexture(int overlay, int size) {
        Bitmap texture = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(texture);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        switch (overlay) {
            case GRASS:
                drawGrassOverlay(canvas, paint, size);
                break;

            case SNOW:
                drawSnowOverlay(canvas, paint, size);
                break;

            case ICE:
                drawIceOverlay(canvas, paint, size);
                break;

            case MOSS:
                drawMossOverlay(canvas, paint, size);
                break;

            case VINES:
                drawVinesOverlay(canvas, paint, size);
                break;

            default:
                // Transparent for NONE - bitmap is already transparent
                break;
        }

        return texture;
    }

    private static void drawGrassOverlay(Canvas canvas, Paint paint, int size) {
        // Green grass tufts on top portion
        paint.setColor(Color.argb(200, 60, 180, 60));
        paint.setStyle(Paint.Style.FILL);
        int bladeWidth = size / 8;
        int bladeHeight = size / 3;
        for (int i = 0; i < 8; i++) {
            int x = i * bladeWidth;
            int h = bladeHeight + (int)(Math.random() * bladeHeight / 2);
            canvas.drawRect(x, 0, x + bladeWidth, h, paint);
        }
        // Darker green highlights
        paint.setColor(Color.argb(180, 40, 140, 40));
        for (int i = 0; i < 4; i++) {
            int x = (int)(Math.random() * size);
            int h = (int)(Math.random() * bladeHeight);
            canvas.drawRect(x, 0, x + bladeWidth / 2, h, paint);
        }
    }

    private static void drawSnowOverlay(Canvas canvas, Paint paint, int size) {
        // Snow layer on top
        paint.setColor(Color.argb(220, 255, 255, 255));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, size, size / 2, paint);

        // Uneven snow edge
        paint.setColor(Color.argb(180, 255, 255, 255));
        for (int x = 0; x < size; x += 4) {
            int h = (int)(Math.random() * size / 4);
            canvas.drawRect(x, size / 2, x + 4, size / 2 + h, paint);
        }

        // Snow sparkles
        paint.setColor(Color.argb(255, 255, 255, 255));
        for (int i = 0; i < 5; i++) {
            int x = (int)(Math.random() * size);
            int y = (int)(Math.random() * size / 2);
            canvas.drawRect(x, y, x + 1, y + 1, paint);
        }
    }

    private static void drawIceOverlay(Canvas canvas, Paint paint, int size) {
        // Semi-transparent ice coating entire block
        paint.setColor(Color.argb(100, 180, 220, 255));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, size, size, paint);

        // Ice cracks
        paint.setColor(Color.argb(150, 200, 240, 255));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        for (int i = 0; i < 3; i++) {
            int x1 = (int)(Math.random() * size);
            int y1 = (int)(Math.random() * size);
            int x2 = x1 + (int)(Math.random() * size / 2) - size / 4;
            int y2 = y1 + (int)(Math.random() * size / 2) - size / 4;
            canvas.drawLine(x1, y1, x2, y2, paint);
        }

        // Ice shine
        paint.setColor(Color.argb(80, 255, 255, 255));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(size / 4f, size / 4f, size / 2f, size / 4f + 2, paint);
    }

    private static void drawMossOverlay(Canvas canvas, Paint paint, int size) {
        // Green moss patches
        paint.setColor(Color.argb(180, 80, 120, 60));
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 6; i++) {
            int x = (int)(Math.random() * size);
            int y = (int)(Math.random() * size);
            int r = size / 6 + (int)(Math.random() * size / 6);
            canvas.drawCircle(x, y, r / 2f, paint);
        }
    }

    private static void drawVinesOverlay(Canvas canvas, Paint paint, int size) {
        // Hanging vines
        paint.setColor(Color.argb(200, 40, 100, 40));
        paint.setStyle(Paint.Style.FILL);
        int vineWidth = size / 6;
        for (int i = 0; i < 4; i++) {
            int x = (int)(Math.random() * size);
            int length = size / 2 + (int)(Math.random() * size / 2);
            canvas.drawRect(x, 0, x + vineWidth, length, paint);
        }
    }
}
