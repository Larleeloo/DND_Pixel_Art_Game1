package com.ambermoongame.entity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.ambermoongame.graphics.AndroidAssetLoader;

/**
 * SpriteEntity supports both static sprites (PNG) and animated sprites (GIF).
 * - Handles static sprite (PNG) and animated GIF with frame cycling.
 * - Keeps width/height scaled by SCALE.
 * - Supports RGB color masking for tinting sprites.
 * - Animated sprites automatically cycle through frames when update() is called.
 * Equivalent to entity/SpriteEntity.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.Image/BufferedImage -> android.graphics.Bitmap
 * - java.awt.Graphics            -> android.graphics.Canvas
 * - java.awt.Rectangle           -> android.graphics.Rect
 * - java.awt.Color               -> android.graphics.Color (int)
 * - AssetLoader                   -> AndroidAssetLoader
 * - ImageIcon (legacy GIF)        -> removed (not needed on Android)
 * - AnimatedTexture               -> AndroidAssetLoader.ImageAsset (handles animation)
 */
public class SpriteEntity extends Entity {

    private static final String TAG = "SpriteEntity";

    protected Bitmap sprite;                      // Current frame to draw
    protected AndroidAssetLoader.ImageAsset imageAsset;  // For animated sprite support
    protected int width, height;
    private boolean solid;

    // Color mask fields for tinting sprites
    private int maskRed = 255;
    private int maskGreen = 255;
    private int maskBlue = 255;
    private boolean hasColorMask = false;
    private Bitmap tintedSprite; // Cached tinted version

    // Paint for drawing
    private Paint drawPaint = new Paint();
    private Paint placeholderPaint = new Paint();

    // Timestamp for animation frame selection
    private long animationStartTime = System.currentTimeMillis();

    public static final int SCALE = 4;

    // Reusable Rect objects to reduce allocation
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    public SpriteEntity(int x, int y, String spritePath, boolean solid) {
        super(x, y);
        this.solid = solid;

        placeholderPaint.setColor(Color.MAGENTA);

        imageAsset = AndroidAssetLoader.load(spritePath);
        if (imageAsset != null && imageAsset.bitmap != null) {
            this.sprite = imageAsset.bitmap;
            this.width = Math.max(1, imageAsset.width) * SCALE;
            this.height = Math.max(1, imageAsset.height) * SCALE;

            String animInfo = imageAsset.isAnimated
                ? ", animated" : "";
            Log.d(TAG, "Loaded sprite \"" + spritePath + "\" -> w=" + imageAsset.width
                    + " h=" + imageAsset.height + " scaled -> " + width + "x" + height + animInfo);
        } else {
            // Asset not found - use placeholder dimensions
            this.sprite = null;
            this.width = 32 * SCALE;
            this.height = 32 * SCALE;
            Log.w(TAG, "Asset not found: " + spritePath + ", using placeholder");
        }
    }

    /**
     * Updates the animated sprite if present.
     * Call this every frame to advance GIF animation.
     * @param deltaMs Time elapsed since last update in milliseconds
     */
    public void updateAnimation(long deltaMs) {
        if (imageAsset != null && imageAsset.isAnimated) {
            long elapsed = System.currentTimeMillis() - animationStartTime;
            Bitmap currentFrame = imageAsset.getFrame(elapsed);
            if (currentFrame != null && currentFrame != sprite) {
                sprite = currentFrame;
                // Invalidate tinted cache when frame changes
                if (hasColorMask) {
                    tintedSprite = createTintedSprite();
                }
            }
        }
    }

    /**
     * Checks if this sprite is animated.
     * @return true if the sprite has multiple frames
     */
    public boolean isAnimated() {
        return imageAsset != null && imageAsset.isAnimated;
    }

    /**
     * Gets the image asset if available.
     * @return ImageAsset or null if not loaded
     */
    public AndroidAssetLoader.ImageAsset getImageAsset() {
        return imageAsset;
    }

    @Override
    public Rect getBounds() {
        return new Rect(x, y, x + width, y + height);
    }

    @Override
    public void draw(Canvas canvas) {
        Bitmap spriteToDraw = (hasColorMask && tintedSprite != null) ? tintedSprite : sprite;
        if (spriteToDraw != null && !spriteToDraw.isRecycled()) {
            srcRect.set(0, 0, spriteToDraw.getWidth(), spriteToDraw.getHeight());
            dstRect.set(x, y, x + width, y + height);
            canvas.drawBitmap(spriteToDraw, srcRect, dstRect, drawPaint);
        } else {
            // Placeholder so we see something
            canvas.drawRect(x, y, x + width, y + height, placeholderPaint);
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
        this.maskRed = Math.max(0, Math.min(255, red));
        this.maskGreen = Math.max(0, Math.min(255, green));
        this.maskBlue = Math.max(0, Math.min(255, blue));
        this.hasColorMask = true;
        this.tintedSprite = createTintedSprite();
    }

    /**
     * Clears the color mask, restoring the original sprite appearance.
     */
    public void clearColorMask() {
        this.hasColorMask = false;
        if (tintedSprite != null && tintedSprite != sprite) {
            tintedSprite.recycle();
        }
        this.tintedSprite = null;
        this.maskRed = 255;
        this.maskGreen = 255;
        this.maskBlue = 255;
    }

    /**
     * Checks if this sprite currently has a color mask applied.
     */
    public boolean hasColorMask() {
        return hasColorMask;
    }

    /**
     * Gets the current color mask as an array [red, green, blue].
     */
    public int[] getColorMask() {
        return new int[] { maskRed, maskGreen, maskBlue };
    }

    /**
     * Creates a tinted copy of the sprite by applying the color mask
     * to all non-transparent pixels using multiplicative blending.
     *
     * @return Bitmap with the color mask applied
     */
    private Bitmap createTintedSprite() {
        if (sprite == null || sprite.isRecycled()) {
            return null;
        }

        int origWidth = sprite.getWidth();
        int origHeight = sprite.getHeight();

        if (origWidth <= 0 || origHeight <= 0) {
            return null;
        }

        // Create mutable copy
        Bitmap tinted = Bitmap.createBitmap(origWidth, origHeight, Bitmap.Config.ARGB_8888);

        // Normalize mask values
        float rFactor = maskRed / 255.0f;
        float gFactor = maskGreen / 255.0f;
        float bFactor = maskBlue / 255.0f;

        // Read all pixels at once for performance
        int[] pixels = new int[origWidth * origHeight];
        sprite.getPixels(pixels, 0, origWidth, 0, 0, origWidth, origHeight);

        // Process each pixel
        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            int alpha = (argb >> 24) & 0xFF;

            if (alpha > 0) {
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;

                red = Math.round(red * rFactor);
                green = Math.round(green * gFactor);
                blue = Math.round(blue * bFactor);

                red = Math.min(255, Math.max(0, red));
                green = Math.min(255, Math.max(0, green));
                blue = Math.min(255, Math.max(0, blue));

                pixels[i] = (alpha << 24) | (red << 16) | (green << 8) | blue;
            }
        }

        tinted.setPixels(pixels, 0, origWidth, 0, 0, origWidth, origHeight);
        return tinted;
    }
}
