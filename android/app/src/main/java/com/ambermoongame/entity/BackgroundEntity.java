package com.ambermoongame.entity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.ambermoongame.graphics.AndroidAssetLoader;
import com.ambermoongame.input.TouchInputManager;

/**
 * Entity for rendering background images.
 * Supports tiling/repeating horizontally and vertically for scrolling levels.
 * Equivalent to entity/BackgroundEntity.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.Image           -> android.graphics.Bitmap
 * - java.awt.Graphics/Graphics2D -> android.graphics.Canvas
 * - java.awt.AlphaComposite  -> Paint.setAlpha()
 * - java.awt.Rectangle       -> android.graphics.Rect
 * - AssetLoader              -> AndroidAssetLoader
 * - Camera                    -> uses cameraX/cameraY/viewportWidth/viewportHeight directly
 *                               (will integrate with Android Camera class when ported)
 */
public class BackgroundEntity extends Entity {

    private static final String TAG = "BackgroundEntity";

    private Bitmap image;
    private int width, height;

    // Tiling settings
    private boolean tileHorizontal = false;
    private boolean tileVertical = false;

    // Camera state for tiled drawing (set externally)
    private int cameraX = 0;
    private int cameraY = 0;
    private int viewportWidth = 1920;
    private int viewportHeight = 1080;

    // Drawing paint with alpha
    private Paint alphaPaint;

    // Reusable Rect objects
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    public static final int SCALE = 10;

    public BackgroundEntity(String path) {
        super(0, 0);

        alphaPaint = new Paint();
        alphaPaint.setAlpha(179); // 0.7f * 255 = ~179

        AndroidAssetLoader.ImageAsset asset = AndroidAssetLoader.load(path);
        if (asset != null && asset.bitmap != null) {
            this.image = asset.bitmap;
            this.width = asset.width * SCALE;
            this.height = asset.height * SCALE;
            Log.d(TAG, "Background loaded: " + width + "x" + height);
        } else {
            this.image = null;
            this.width = 1920;
            this.height = 1080;
            Log.w(TAG, "Background not found: " + path);
        }
    }

    /**
     * Sets camera state for tiled rendering.
     * Call this each frame before drawing if tiling is enabled.
     */
    public void setCameraState(int cameraX, int cameraY, int viewportWidth, int viewportHeight) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public void setTileHorizontal(boolean enabled) {
        this.tileHorizontal = enabled;
    }

    public void setTileVertical(boolean enabled) {
        this.tileVertical = enabled;
    }

    public void setTiling(boolean horizontal, boolean vertical) {
        this.tileHorizontal = horizontal;
        this.tileVertical = vertical;
    }

    public int getTileWidth() { return width; }
    public int getTileHeight() { return height; }

    @Override
    public Rect getBounds() {
        return new Rect(0, 0, width, height);
    }

    @Override
    public void update(TouchInputManager input) {
        // Background doesn't update or collide
    }

    @Override
    public void draw(Canvas canvas) {
        if (image == null || image.isRecycled()) return;

        if ((tileHorizontal || tileVertical) && (cameraX != 0 || cameraY != 0)) {
            drawTiled(canvas);
        } else {
            // Simple single background draw
            srcRect.set(0, 0, image.getWidth(), image.getHeight());
            dstRect.set(x, y, x + width, y + height);
            canvas.drawBitmap(image, srcRect, dstRect, alphaPaint);
        }
    }

    /**
     * Draws the background with explicit camera offset applied.
     */
    public void draw(Canvas canvas, int camX, int camY, int vpWidth, int vpHeight) {
        setCameraState(camX, camY, vpWidth, vpHeight);
        draw(canvas);
    }

    /**
     * Draws tiled background across the visible viewport.
     */
    private void drawTiled(Canvas canvas) {
        int startTileX, endTileX, startTileY, endTileY;

        if (tileHorizontal) {
            startTileX = (int) Math.floor((double) cameraX / width);
            endTileX = (int) Math.ceil((double) (cameraX + viewportWidth) / width);
        } else {
            startTileX = 0;
            endTileX = 1;
        }

        if (tileVertical) {
            startTileY = (int) Math.floor((double) cameraY / height);
            endTileY = (int) Math.ceil((double) (cameraY + viewportHeight) / height);
        } else {
            startTileY = 0;
            endTileY = 1;
        }

        srcRect.set(0, 0, image.getWidth(), image.getHeight());

        for (int tileY = startTileY; tileY <= endTileY; tileY++) {
            for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                int drawX = tileX * width;
                int drawY = tileY * height;
                dstRect.set(drawX, drawY, drawX + width, drawY + height);
                canvas.drawBitmap(image, srcRect, dstRect, alphaPaint);
            }
        }
    }

    /**
     * Draws the background with a parallax effect.
     *
     * @param canvas         Canvas to draw on
     * @param camX           Camera X position
     * @param camY           Camera Y position
     * @param vpWidth        Viewport width
     * @param vpHeight       Viewport height
     * @param parallaxRatioX Horizontal parallax ratio (0.0 = static, 1.0 = same as camera)
     * @param parallaxRatioY Vertical parallax ratio (0.0 = static, 1.0 = same as camera)
     */
    public void drawParallax(Canvas canvas, int camX, int camY, int vpWidth, int vpHeight,
                             double parallaxRatioX, double parallaxRatioY) {
        if (image == null || image.isRecycled()) return;

        int parallaxOffsetX = (int) (camX * parallaxRatioX);
        int parallaxOffsetY = (int) (camY * parallaxRatioY);

        srcRect.set(0, 0, image.getWidth(), image.getHeight());

        if (tileHorizontal || tileVertical) {
            int startTileX, endTileX, startTileY, endTileY;

            if (tileHorizontal) {
                startTileX = (int) Math.floor((double) parallaxOffsetX / width);
                endTileX = (int) Math.ceil((double) (parallaxOffsetX + vpWidth) / width);
            } else {
                startTileX = 0;
                endTileX = 1;
            }

            if (tileVertical) {
                startTileY = (int) Math.floor((double) parallaxOffsetY / height);
                endTileY = (int) Math.ceil((double) (parallaxOffsetY + vpHeight) / height);
            } else {
                startTileY = 0;
                endTileY = 1;
            }

            for (int tileY = startTileY; tileY <= endTileY; tileY++) {
                for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                    int drawX = tileX * width - parallaxOffsetX + camX;
                    int drawY = tileY * height - parallaxOffsetY + camY;
                    dstRect.set(drawX, drawY, drawX + width, drawY + height);
                    canvas.drawBitmap(image, srcRect, dstRect, alphaPaint);
                }
            }
        } else {
            int drawX = -parallaxOffsetX + camX;
            int drawY = -parallaxOffsetY + camY;
            dstRect.set(drawX, drawY, drawX + width, drawY + height);
            canvas.drawBitmap(image, srcRect, dstRect, alphaPaint);
        }
    }
}
