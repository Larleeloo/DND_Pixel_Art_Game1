package com.ambermoongame.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.Log;

/**
 * Represents a single parallax layer in the background system.
 * Each layer has its own scroll speed (depth) that creates a depth illusion.
 * Supports both static images (PNG) and animated GIFs for dynamic backgrounds.
 *
 * Scroll speeds:
 * - 0.0 = Static (doesn't move with camera)
 * - 0.5 = Moves at half camera speed (distant background)
 * - 1.0 = Moves with camera (same as world entities)
 * - > 1.0 = Moves faster than camera (foreground elements)
 */
public class ParallaxLayer {

    private static final String TAG = "ParallaxLayer";

    // Layer name for identification
    private String name;

    // Image data - supports both static and animated images
    private Bitmap image;          // Current frame to display
    private AnimatedTexture animatedTexture;  // For animated GIF backgrounds
    private int imageWidth;
    private int imageHeight;

    // Position offset (base position before parallax)
    private int offsetX;
    private int offsetY;

    // Scroll speed (depth factor)
    private double scrollSpeedX;
    private double scrollSpeedY;

    // Vertical anchor mode - if true, offsetY is measured from bottom of viewport
    private boolean anchorBottom;

    // Scaling
    private double scale;

    // Tiling options
    private boolean tileHorizontal;
    private boolean tileVertical;

    // Opacity (0.0 - 1.0)
    private float opacity;

    // Z-order for sorting (lower = further back)
    private int zOrder;

    // Visibility flag
    private boolean visible;

    // Reusable drawing objects
    private final Paint drawPaint;
    private final Rect srcRect;
    private final Rect dstRect;

    /**
     * Creates a new parallax layer.
     *
     * @param name         Unique name for this layer
     * @param imagePath    Path to the layer image (relative to assets/)
     * @param scrollSpeedX Horizontal scroll speed (0.0 = static, 1.0 = world speed)
     * @param scrollSpeedY Vertical scroll speed (0.0 = static, 1.0 = world speed)
     * @param zOrder       Z-order for rendering (lower = behind)
     */
    public ParallaxLayer(String name, String imagePath, double scrollSpeedX, double scrollSpeedY, int zOrder) {
        this.name = name;
        this.scrollSpeedX = scrollSpeedX;
        this.scrollSpeedY = scrollSpeedY;
        this.zOrder = zOrder;
        this.scale = 1.0;
        this.opacity = 1.0f;
        this.visible = true;
        this.tileHorizontal = false;
        this.tileVertical = false;
        this.offsetX = 0;
        this.offsetY = 0;
        this.anchorBottom = false;

        this.drawPaint = new Paint();
        this.drawPaint.setAntiAlias(false);
        this.drawPaint.setFilterBitmap(false);  // Nearest-neighbor for pixel art
        this.srcRect = new Rect();
        this.dstRect = new Rect();

        loadImage(imagePath);
    }

    /**
     * Creates a parallax layer with uniform scroll speed in both directions.
     */
    public ParallaxLayer(String name, String imagePath, double scrollSpeed, int zOrder) {
        this(name, imagePath, scrollSpeed, scrollSpeed, zOrder);
    }

    /**
     * Load the image from the given path.
     * Supports both static images (PNG, JPG) and animated GIFs.
     */
    private void loadImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            createPlaceholderImage();
            return;
        }

        try {
            AndroidAssetLoader.ImageAsset asset = AndroidAssetLoader.load(imagePath);
            if (asset != null) {
                this.image = asset.bitmap;
                this.imageWidth = asset.width;
                this.imageHeight = asset.height;

                // Create AnimatedTexture if the asset has multiple frames
                if (asset.isAnimated && asset.frames != null && !asset.frames.isEmpty()) {
                    this.animatedTexture = AnimatedTexture.fromImageAsset(asset);
                    if (animatedTexture != null && animatedTexture.isAnimated()) {
                        animatedTexture.playForward();
                    }
                }

                String animInfo = (animatedTexture != null && animatedTexture.isAnimated())
                    ? ", animated (" + animatedTexture.getFrameCount() + " frames)" : "";
                Log.d(TAG, "Layer '" + name + "' loaded: " + imageWidth + "x" + imageHeight + animInfo);
            } else {
                Log.w(TAG, "Failed to load image '" + imagePath + "' for layer '" + name + "'");
                createPlaceholderImage();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to load image '" + imagePath + "' for layer '" + name + "': " + e.getMessage());
            createPlaceholderImage();
        }
    }

    /**
     * Updates the animated texture if present.
     * Call this every frame to advance GIF animation.
     * @param deltaMs Time elapsed since last update in milliseconds
     */
    public void update(long deltaMs) {
        if (animatedTexture != null && animatedTexture.isAnimated()) {
            animatedTexture.update(deltaMs);
            image = animatedTexture.getCurrentFrame();
        }
    }

    /**
     * Checks if this layer has an animated texture.
     * @return true if the layer uses an animated GIF
     */
    public boolean isAnimated() {
        return animatedTexture != null && animatedTexture.isAnimated();
    }

    /**
     * Create a placeholder image if loading fails.
     */
    private void createPlaceholderImage() {
        this.imageWidth = 192;
        this.imageHeight = 108;
        Bitmap placeholder = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(placeholder);

        // Create a gradient placeholder
        Paint gradientPaint = new Paint();
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, imageHeight,
                Color.argb(100, 100, 100, 150),
                Color.argb(100, 50, 50, 100),
                Shader.TileMode.CLAMP
        );
        gradientPaint.setShader(gradient);
        canvas.drawRect(0, 0, imageWidth, imageHeight, gradientPaint);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.argb(100, 200, 200, 200));
        textPaint.setTextSize(14);
        canvas.drawText(name, 10, 20, textPaint);

        this.image = placeholder;
    }

    /**
     * Render this parallax layer.
     *
     * @param canvas Canvas to draw on
     * @param camera Camera for viewport and position information
     */
    public void draw(Canvas canvas, Camera camera) {
        if (!visible || image == null) return;

        int viewportWidth = camera.getViewportWidth();
        int viewportHeight = camera.getViewportHeight();
        double cameraX = camera.getX();
        double cameraY = camera.getY();

        // Calculate the scaled image dimensions
        int scaledWidth = (int) (imageWidth * scale);
        int scaledHeight = (int) (imageHeight * scale);

        // Calculate parallax offset
        double parallaxX = cameraX * scrollSpeedX;
        double parallaxY = cameraY * scrollSpeedY;

        // Calculate base Y position
        double baseOffsetY = offsetY;
        if (anchorBottom) {
            baseOffsetY = viewportHeight - scaledHeight + offsetY;
        }

        // Base draw position (in screen space)
        double drawX = offsetX - parallaxX + cameraX;
        double drawY = baseOffsetY - parallaxY + cameraY;

        // Set opacity
        int alphaBackup = drawPaint.getAlpha();
        if (opacity < 1.0f) {
            drawPaint.setAlpha((int) (opacity * 255));
        }

        if (tileHorizontal || tileVertical) {
            drawTiled(canvas, camera, drawX, drawY, scaledWidth, scaledHeight);
        } else {
            // Single image draw
            srcRect.set(0, 0, image.getWidth(), image.getHeight());
            dstRect.set((int) drawX, (int) drawY, (int) drawX + scaledWidth, (int) drawY + scaledHeight);
            canvas.drawBitmap(image, srcRect, dstRect, drawPaint);
        }

        // Restore opacity
        drawPaint.setAlpha(alphaBackup);
    }

    /**
     * Draw the layer with tiling.
     */
    private void drawTiled(Canvas canvas, Camera camera, double baseX, double baseY,
                           int scaledWidth, int scaledHeight) {
        int viewportWidth = camera.getViewportWidth();
        int viewportHeight = camera.getViewportHeight();
        double cameraX = camera.getX();
        double cameraY = camera.getY();

        double baseOffsetY = offsetY;
        if (anchorBottom) {
            baseOffsetY = viewportHeight - scaledHeight + offsetY;
        }

        int startTileX, endTileX, startTileY, endTileY;

        if (tileHorizontal && scaledWidth > 0) {
            double effectiveX = cameraX * scrollSpeedX - offsetX;
            startTileX = (int) Math.floor(effectiveX / scaledWidth);
            endTileX = (int) Math.ceil((effectiveX + viewportWidth) / scaledWidth);
        } else {
            startTileX = 0;
            endTileX = 1;
        }

        if (tileVertical && scaledHeight > 0) {
            double effectiveY = cameraY * scrollSpeedY - baseOffsetY;
            startTileY = (int) Math.floor(effectiveY / scaledHeight);
            endTileY = (int) Math.ceil((effectiveY + viewportHeight) / scaledHeight);
        } else {
            startTileY = 0;
            endTileY = 0;
        }

        srcRect.set(0, 0, image.getWidth(), image.getHeight());

        for (int tileY = startTileY; tileY <= endTileY; tileY++) {
            for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                double tileDrawX = tileX * scaledWidth + offsetX - (cameraX * scrollSpeedX) + cameraX;
                double tileDrawY = tileY * scaledHeight + baseOffsetY - (cameraY * scrollSpeedY) + cameraY;

                dstRect.set((int) tileDrawX, (int) tileDrawY,
                           (int) tileDrawX + scaledWidth, (int) tileDrawY + scaledHeight);
                canvas.drawBitmap(image, srcRect, dstRect, drawPaint);
            }
        }
    }

    // ========== Getters and Setters ==========

    public String getName() { return name; }

    public double getScrollSpeedX() { return scrollSpeedX; }
    public void setScrollSpeedX(double scrollSpeedX) { this.scrollSpeedX = scrollSpeedX; }

    public double getScrollSpeedY() { return scrollSpeedY; }
    public void setScrollSpeedY(double scrollSpeedY) { this.scrollSpeedY = scrollSpeedY; }

    public void setScrollSpeed(double speedX, double speedY) {
        this.scrollSpeedX = speedX;
        this.scrollSpeedY = speedY;
    }

    public double getScale() { return scale; }
    public void setScale(double scale) { this.scale = Math.max(0.1, scale); }

    public float getOpacity() { return opacity; }
    public void setOpacity(float opacity) { this.opacity = Math.max(0, Math.min(1, opacity)); }

    public int getZOrder() { return zOrder; }
    public void setZOrder(int zOrder) { this.zOrder = zOrder; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isTileHorizontal() { return tileHorizontal; }
    public void setTileHorizontal(boolean tileHorizontal) { this.tileHorizontal = tileHorizontal; }

    public boolean isTileVertical() { return tileVertical; }
    public void setTileVertical(boolean tileVertical) { this.tileVertical = tileVertical; }

    public void setTiling(boolean horizontal, boolean vertical) {
        this.tileHorizontal = horizontal;
        this.tileVertical = vertical;
    }

    public int getOffsetX() { return offsetX; }
    public void setOffsetX(int offsetX) { this.offsetX = offsetX; }

    public int getOffsetY() { return offsetY; }
    public void setOffsetY(int offsetY) { this.offsetY = offsetY; }

    public void setOffset(int x, int y) {
        this.offsetX = x;
        this.offsetY = y;
    }

    public int getImageWidth() { return imageWidth; }
    public int getImageHeight() { return imageHeight; }
    public int getScaledWidth() { return (int) (imageWidth * scale); }
    public int getScaledHeight() { return (int) (imageHeight * scale); }

    public boolean isAnchorBottom() { return anchorBottom; }
    public void setAnchorBottom(boolean anchorBottom) { this.anchorBottom = anchorBottom; }
}
