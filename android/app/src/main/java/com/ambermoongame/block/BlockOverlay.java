package com.ambermoongame.block;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Enum defining overlay types that can be applied on top of base blocks.
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
 *
 * Conversion notes:
 * - java.awt.image.BufferedImage -> android.graphics.Bitmap
 * - java.awt.Graphics2D          -> android.graphics.Canvas
 * - java.awt.Color               -> android.graphics.Color (int)
 * - java.awt.RenderingHints      -> Paint.setAntiAlias()
 * - java.awt.BasicStroke          -> Paint.setStrokeWidth()
 * - g2d.fillRect()               -> canvas.drawRect()
 * - g2d.fillOval()               -> canvas.drawCircle()
 * - g2d.drawLine()               -> canvas.drawLine()
 */
public enum BlockOverlay {
    NONE(null, "None", 0, false),
    GRASS("assets/textures/blocks/overlays/grass_overlay.png", "Grass", 1, true),
    SNOW("assets/textures/blocks/overlays/snow_overlay.png", "Snow", 2, true),
    ICE("assets/textures/blocks/overlays/ice_overlay.png", "Ice", 3, true),
    MOSS("assets/textures/blocks/overlays/moss_overlay.png", "Moss", 1, true),
    VINES("assets/textures/blocks/overlays/vines_overlay.png", "Vines", 1, true);

    private final String texturePath;
    private final String displayName;
    private final int breakSteps;  // Number of hits required to remove overlay
    private final boolean blocksBaseMining;  // Must overlay be removed before mining base?

    BlockOverlay(String texturePath, String displayName, int breakSteps, boolean blocksBaseMining) {
        this.texturePath = texturePath;
        this.displayName = displayName;
        this.breakSteps = breakSteps;
        this.blocksBaseMining = blocksBaseMining;
    }

    public String getTexturePath() { return texturePath; }
    public String getDisplayName() { return displayName; }
    public int getBreakSteps() { return breakSteps; }
    public boolean blocksBaseMining() { return blocksBaseMining; }

    /**
     * Find an overlay type by its name (case-insensitive).
     * @param name The name to search for
     * @return The matching BlockOverlay, or NONE as default
     */
    public static BlockOverlay fromName(String name) {
        if (name == null || name.isEmpty()) {
            return NONE;
        }

        String upperName = name.toUpperCase().replace(" ", "_");
        for (BlockOverlay overlay : values()) {
            if (overlay.name().equals(upperName)) {
                return overlay;
            }
        }

        for (BlockOverlay overlay : values()) {
            if (overlay.displayName.equalsIgnoreCase(name)) {
                return overlay;
            }
        }

        return NONE;
    }

    /**
     * Generates a procedural overlay texture if the file doesn't exist.
     * Creates a semi-transparent texture programmatically.
     *
     * @param size The size of the texture to generate (width and height)
     * @return Generated Bitmap
     */
    public Bitmap generateTexture(int size) {
        Bitmap texture = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(texture);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        switch (this) {
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

    private void drawGrassOverlay(Canvas canvas, Paint paint, int size) {
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

    private void drawSnowOverlay(Canvas canvas, Paint paint, int size) {
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

    private void drawIceOverlay(Canvas canvas, Paint paint, int size) {
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

    private void drawMossOverlay(Canvas canvas, Paint paint, int size) {
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

    private void drawVinesOverlay(Canvas canvas, Paint paint, int size) {
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
