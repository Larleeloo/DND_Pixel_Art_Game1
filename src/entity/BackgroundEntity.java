package entity;
import block.*;
import input.*;
import graphics.*;
import animation.*;
import audio.*;

import java.awt.*;

/**
 * Entity for rendering background images.
 * Supports tiling/repeating horizontally and vertically for scrolling levels.
 */
class BackgroundEntity extends Entity {

    private Image image;
    private int width, height;

    // Tiling settings
    private boolean tileHorizontal = false;
    private boolean tileVertical = false;

    // Optional camera reference for tiled drawing
    private Camera camera;

    public static final int SCALE = 10; // 10x scaling for backgrounds

    public BackgroundEntity(String path) {
        super(0, 0);

        AssetLoader.ImageAsset asset = AssetLoader.load(path);
        this.image = asset.staticImage;

        this.width  = asset.width  * SCALE;
        this.height = asset.height * SCALE;

        System.out.println("Background loaded: " + width + "x" + height);
    }

    /**
     * Sets the camera reference for tiled rendering.
     *
     * @param camera The camera to use for viewport calculations
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    /**
     * Enables or disables horizontal tiling (repeating).
     *
     * @param enabled True to tile horizontally
     */
    public void setTileHorizontal(boolean enabled) {
        this.tileHorizontal = enabled;
    }

    /**
     * Enables or disables vertical tiling (repeating).
     *
     * @param enabled True to tile vertically
     */
    public void setTileVertical(boolean enabled) {
        this.tileVertical = enabled;
    }

    /**
     * Convenience method to enable both horizontal and vertical tiling.
     */
    public void setTiling(boolean horizontal, boolean vertical) {
        this.tileHorizontal = horizontal;
        this.tileVertical = vertical;
    }

    /**
     * Gets the width of one tile (scaled background width).
     */
    public int getTileWidth() {
        return width;
    }

    /**
     * Gets the height of one tile (scaled background height).
     */
    public int getTileHeight() {
        return height;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(0, 0, width, height);
    }

    @Override
    public void update(InputManager input) {
        // Background doesn't update or collide
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

        if ((tileHorizontal || tileVertical) && camera != null) {
            drawTiled(g2d);
        } else {
            // Simple single background draw (no tiling)
            g.drawImage(image, x, y, width, height, null);
        }

        g2d.setComposite(oldComposite);
    }

    /**
     * Draws the background with camera offset applied.
     * Use this when drawing manually with camera transforms.
     *
     * @param g      Graphics context
     * @param camera Camera for viewport calculations
     */
    public void draw(Graphics g, Camera camera) {
        this.camera = camera;
        draw(g);
    }

    /**
     * Draws tiled background across the visible viewport.
     */
    private void drawTiled(Graphics2D g2d) {
        // Get camera viewport bounds
        int camX = (int) camera.getX();
        int camY = (int) camera.getY();
        int viewWidth = camera.getViewportWidth();
        int viewHeight = camera.getViewportHeight();

        // Calculate which tiles we need to draw
        int startTileX, endTileX, startTileY, endTileY;

        if (tileHorizontal) {
            // Calculate horizontal tile range
            startTileX = (int) Math.floor((double) camX / width);
            endTileX = (int) Math.ceil((double) (camX + viewWidth) / width);
        } else {
            startTileX = 0;
            endTileX = 1;
        }

        if (tileVertical) {
            // Calculate vertical tile range
            startTileY = (int) Math.floor((double) camY / height);
            endTileY = (int) Math.ceil((double) (camY + viewHeight) / height);
        } else {
            startTileY = 0;
            endTileY = 1;
        }

        // Draw all visible tiles
        for (int tileY = startTileY; tileY <= endTileY; tileY++) {
            for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                int drawX = tileX * width;
                int drawY = tileY * height;

                g2d.drawImage(image, drawX, drawY, width, height, null);
            }
        }
    }

    /**
     * Draws the background with a parallax effect.
     * The background moves at a fraction of the camera movement for depth.
     *
     * @param g             Graphics context
     * @param camera        Camera for viewport calculations
     * @param parallaxRatioX Horizontal parallax ratio (0.0 = static, 1.0 = same as camera)
     * @param parallaxRatioY Vertical parallax ratio (0.0 = static, 1.0 = same as camera)
     */
    public void drawParallax(Graphics g, Camera camera, double parallaxRatioX, double parallaxRatioY) {
        Graphics2D g2d = (Graphics2D) g;
        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

        int camX = (int) camera.getX();
        int camY = (int) camera.getY();
        int viewWidth = camera.getViewportWidth();
        int viewHeight = camera.getViewportHeight();

        // Calculate parallax offset
        int parallaxOffsetX = (int) (camX * parallaxRatioX);
        int parallaxOffsetY = (int) (camY * parallaxRatioY);

        if (tileHorizontal || tileVertical) {
            // Calculate tile positions with parallax
            int startTileX, endTileX, startTileY, endTileY;

            if (tileHorizontal) {
                startTileX = (int) Math.floor((double) parallaxOffsetX / width);
                endTileX = (int) Math.ceil((double) (parallaxOffsetX + viewWidth) / width);
            } else {
                startTileX = 0;
                endTileX = 1;
            }

            if (tileVertical) {
                startTileY = (int) Math.floor((double) parallaxOffsetY / height);
                endTileY = (int) Math.ceil((double) (parallaxOffsetY + viewHeight) / height);
            } else {
                startTileY = 0;
                endTileY = 1;
            }

            // Draw tiles with parallax offset
            for (int tileY = startTileY; tileY <= endTileY; tileY++) {
                for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                    int drawX = tileX * width - parallaxOffsetX + camX;
                    int drawY = tileY * height - parallaxOffsetY + camY;

                    g2d.drawImage(image, drawX, drawY, width, height, null);
                }
            }
        } else {
            // Single background with parallax offset
            int drawX = -parallaxOffsetX + camX;
            int drawY = -parallaxOffsetY + camY;
            g2d.drawImage(image, drawX, drawY, width, height, null);
        }

        g2d.setComposite(oldComposite);
    }
}
