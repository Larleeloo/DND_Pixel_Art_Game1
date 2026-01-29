package graphics;
import animation.*;
import block.*;

import java.awt.*;
import java.awt.image.BufferedImage;

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

    // Layer name for identification
    private String name;

    // Image data - supports both static and animated images
    private Image image;           // Current frame to display
    private AnimatedTexture animatedTexture;  // For animated GIF backgrounds
    private int imageWidth;
    private int imageHeight;

    // Position offset (base position before parallax)
    private int offsetX;
    private int offsetY;

    // Scroll speed (depth factor)
    // Lower values = further back, moves slower
    // Higher values = closer, moves faster
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

    /**
     * Creates a new parallax layer.
     *
     * @param name         Unique name for this layer
     * @param imagePath    Path to the layer image
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
            AssetLoader.ImageAsset asset = AssetLoader.load(imagePath);
            this.animatedTexture = asset.animatedTexture;
            this.image = asset.staticImage;
            this.imageWidth = asset.width;
            this.imageHeight = asset.height;

            // Auto-start animation for animated GIFs in parallax backgrounds
            if (animatedTexture != null && animatedTexture.isAnimated()) {
                animatedTexture.playForward();
            }

            String animInfo = (animatedTexture != null && animatedTexture.isAnimated())
                ? ", animated (" + animatedTexture.getFrameCount() + " frames)" : "";
            System.out.println("ParallaxLayer '" + name + "' loaded: " + imageWidth + "x" + imageHeight + animInfo);
        } catch (Exception e) {
            System.err.println("ParallaxLayer: Failed to load image '" + imagePath + "' for layer '" + name + "'");
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
            // Update the image reference to current frame
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
        BufferedImage placeholder = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();

        // Create a gradient placeholder
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(100, 100, 150, 100),
                0, imageHeight, new Color(50, 50, 100, 100)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, imageWidth, imageHeight);

        g.setColor(new Color(200, 200, 200, 100));
        g.drawString(name, 10, 20);

        g.dispose();
        this.image = placeholder;
    }

    /**
     * Render this parallax layer.
     *
     * @param g       Graphics context
     * @param camera  Camera for viewport and position information
     */
    public void draw(Graphics2D g, Camera camera) {
        if (!visible || image == null) return;

        // Save old composite for opacity
        Composite oldComposite = g.getComposite();
        if (opacity < 1.0f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        }

        int viewportWidth = camera.getViewportWidth();
        int viewportHeight = camera.getViewportHeight();
        double cameraX = camera.getX();
        double cameraY = camera.getY();

        // Calculate the scaled image dimensions
        int scaledWidth = (int) (imageWidth * scale);
        int scaledHeight = (int) (imageHeight * scale);

        // Calculate parallax offset
        // The layer position moves at a fraction of the camera movement
        double parallaxX = cameraX * scrollSpeedX;
        double parallaxY = cameraY * scrollSpeedY;

        // Calculate base Y position
        // If anchorBottom is true, position the image so its bottom edge aligns
        // with (viewportHeight + offsetY) in screen space
        double baseOffsetY = offsetY;
        if (anchorBottom) {
            // Anchor from bottom: offsetY=0 means image bottom at viewport bottom
            // offsetY=-100 means image bottom is 100px above viewport bottom
            baseOffsetY = viewportHeight - scaledHeight + offsetY;
        }

        // Base draw position (in screen space)
        // We draw relative to camera position, adjusted by parallax
        double drawX = offsetX - parallaxX + cameraX;
        double drawY = baseOffsetY - parallaxY + cameraY;

        if (tileHorizontal || tileVertical) {
            drawTiled(g, camera, drawX, drawY, scaledWidth, scaledHeight);
        } else {
            // Single image draw
            g.drawImage(image, (int) drawX, (int) drawY, scaledWidth, scaledHeight, null);
        }

        // Restore composite
        g.setComposite(oldComposite);
    }

    /**
     * Draw the layer with tiling.
     */
    private void drawTiled(Graphics2D g, Camera camera, double baseX, double baseY,
                           int scaledWidth, int scaledHeight) {
        int viewportWidth = camera.getViewportWidth();
        int viewportHeight = camera.getViewportHeight();
        double cameraX = camera.getX();
        double cameraY = camera.getY();

        // Calculate base Y offset considering anchor mode
        double baseOffsetY = offsetY;
        if (anchorBottom) {
            baseOffsetY = viewportHeight - scaledHeight + offsetY;
        }

        int startTileX, endTileX, startTileY, endTileY;

        if (tileHorizontal && scaledWidth > 0) {
            // Calculate which tiles to draw horizontally
            double effectiveX = cameraX * scrollSpeedX - offsetX;
            startTileX = (int) Math.floor(effectiveX / scaledWidth);
            endTileX = (int) Math.ceil((effectiveX + viewportWidth) / scaledWidth);
        } else {
            startTileX = 0;
            endTileX = 1;
        }

        if (tileVertical && scaledHeight > 0) {
            // Calculate which tiles to draw vertically
            double effectiveY = cameraY * scrollSpeedY - baseOffsetY;
            startTileY = (int) Math.floor(effectiveY / scaledHeight);
            endTileY = (int) Math.ceil((effectiveY + viewportHeight) / scaledHeight);
        } else {
            // Only draw a single tile vertically when not tiling
            startTileY = 0;
            endTileY = 0;
        }

        // Draw all visible tiles
        for (int tileY = startTileY; tileY <= endTileY; tileY++) {
            for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                double tileDrawX = tileX * scaledWidth + offsetX - (cameraX * scrollSpeedX) + cameraX;
                double tileDrawY = tileY * scaledHeight + baseOffsetY - (cameraY * scrollSpeedY) + cameraY;

                g.drawImage(image, (int) tileDrawX, (int) tileDrawY, scaledWidth, scaledHeight, null);
            }
        }
    }

    // ========== Getters and Setters ==========

    public String getName() {
        return name;
    }

    public double getScrollSpeedX() {
        return scrollSpeedX;
    }

    public void setScrollSpeedX(double scrollSpeedX) {
        this.scrollSpeedX = scrollSpeedX;
    }

    public double getScrollSpeedY() {
        return scrollSpeedY;
    }

    public void setScrollSpeedY(double scrollSpeedY) {
        this.scrollSpeedY = scrollSpeedY;
    }

    public void setScrollSpeed(double speedX, double speedY) {
        this.scrollSpeedX = speedX;
        this.scrollSpeedY = speedY;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = Math.max(0.1, scale);
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = Math.max(0, Math.min(1, opacity));
    }

    public int getZOrder() {
        return zOrder;
    }

    public void setZOrder(int zOrder) {
        this.zOrder = zOrder;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isTileHorizontal() {
        return tileHorizontal;
    }

    public void setTileHorizontal(boolean tileHorizontal) {
        this.tileHorizontal = tileHorizontal;
    }

    public boolean isTileVertical() {
        return tileVertical;
    }

    public void setTileVertical(boolean tileVertical) {
        this.tileVertical = tileVertical;
    }

    public void setTiling(boolean horizontal, boolean vertical) {
        this.tileHorizontal = horizontal;
        this.tileVertical = vertical;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public void setOffset(int x, int y) {
        this.offsetX = x;
        this.offsetY = y;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getScaledWidth() {
        return (int) (imageWidth * scale);
    }

    public int getScaledHeight() {
        return (int) (imageHeight * scale);
    }

    public boolean isAnchorBottom() {
        return anchorBottom;
    }

    public void setAnchorBottom(boolean anchorBottom) {
        this.anchorBottom = anchorBottom;
    }
}
