package com.ambermoongame.block;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.ambermoongame.graphics.AndroidAssetLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for block textures and definitions.
 * Uses singleton pattern to ensure textures are loaded only once
 * and shared across all block entities for memory efficiency.
 * Supports both static textures (PNG) and animated textures (GIF).
 * Equivalent to block/BlockRegistry.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.image.BufferedImage -> android.graphics.Bitmap
 * - java.awt.Graphics2D          -> android.graphics.Canvas
 * - java.awt.Color               -> android.graphics.Color
 * - java.awt.RenderingHints      -> (not needed, Bitmap.createScaledBitmap uses filtering param)
 * - AssetLoader                   -> AndroidAssetLoader
 * - AnimatedTexture               -> AndroidAssetLoader.ImageAsset (handles animation internally)
 *
 * Key change: Desktop used a separate AnimatedTexture class; Android uses
 * AndroidAssetLoader.ImageAsset which already manages frames + delays + getFrame(elapsed).
 * The registry caches the ImageAsset directly for animated blocks.
 */
public class BlockRegistry {

    private static final String TAG = "BlockRegistry";

    private static BlockRegistry instance;

    // Default block size in pixels (before scaling)
    public static final int BASE_BLOCK_SIZE = 16;

    // Scale factor for rendering (matches SpriteEntity.SCALE)
    public static final int BLOCK_SCALE = 4;

    // Final rendered block size
    public static final int BLOCK_SIZE = BASE_BLOCK_SIZE * BLOCK_SCALE; // 64 pixels

    // Cache for loaded and scaled textures (static) - uses int for BlockType
    private final android.util.SparseArray<Bitmap> textureCache;

    // Cache for animated textures (GIF blocks) - stores full ImageAsset for frame cycling
    private final android.util.SparseArray<AndroidAssetLoader.ImageAsset> animatedTextureCache;

    // Cache for tinted texture variants (key: BlockType + color hash)
    private final Map<String, Bitmap> tintedTextureCache;

    // Cache for overlay textures - uses int for BlockOverlay
    private final android.util.SparseArray<Bitmap> overlayTextureCache;

    // Fallback texture for missing assets
    private Bitmap fallbackTexture;

    // Animation timestamp
    private long animationStartTime = System.currentTimeMillis();

    private BlockRegistry() {
        textureCache = new android.util.SparseArray<>();
        animatedTextureCache = new android.util.SparseArray<>();
        tintedTextureCache = new HashMap<>();
        overlayTextureCache = new android.util.SparseArray<>();
        createFallbackTexture();
    }

    /**
     * Gets the singleton instance of BlockRegistry.
     */
    public static synchronized BlockRegistry getInstance() {
        if (instance == null) {
            instance = new BlockRegistry();
        }
        return instance;
    }

    /**
     * Creates a magenta/black checkerboard fallback texture for missing assets.
     */
    private void createFallbackTexture() {
        fallbackTexture = Bitmap.createBitmap(BASE_BLOCK_SIZE, BASE_BLOCK_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(fallbackTexture);
        Paint paint = new Paint();

        int halfSize = BASE_BLOCK_SIZE / 2;

        // Magenta squares
        paint.setColor(Color.MAGENTA);
        canvas.drawRect(0, 0, halfSize, halfSize, paint);
        canvas.drawRect(halfSize, halfSize, BASE_BLOCK_SIZE, BASE_BLOCK_SIZE, paint);

        // Black squares
        paint.setColor(Color.BLACK);
        canvas.drawRect(halfSize, 0, BASE_BLOCK_SIZE, halfSize, paint);
        canvas.drawRect(0, halfSize, halfSize, BASE_BLOCK_SIZE, paint);
    }

    /**
     * Gets the texture for a block type, loading it if necessary.
     * The returned texture is already scaled to BLOCK_SIZE.
     * For animated blocks, returns the current frame.
     *
     * @param type The block type
     * @return Scaled Bitmap for the block
     */
    public Bitmap getTexture(int type) {
        // Check if this is an animated block
        if (animatedTextureCache.get(type) != null) {
            long elapsed = System.currentTimeMillis() - animationStartTime;
            return animatedTextureCache.get(type).getFrame(elapsed);
        }

        if (textureCache.get(type) != null) {
            return textureCache.get(type);
        }

        // Load and cache the texture
        return loadAndCacheTexture(type);
    }

    /**
     * Gets the image asset for animated blocks.
     * Returns null for static textures.
     */
    public AndroidAssetLoader.ImageAsset getAnimatedAsset(int type) {
        if (animatedTextureCache.get(type) == null && textureCache.get(type) == null) {
            loadAndCacheTexture(type);
        }
        return animatedTextureCache.get(type);
    }

    /**
     * Checks if a block type has an animated texture.
     */
    public boolean isAnimated(int type) {
        AndroidAssetLoader.ImageAsset asset = getAnimatedAsset(type);
        return asset != null && asset.isAnimated;
    }

    /**
     * Loads and caches a texture for a block type.
     * Handles both static and animated textures.
     */
    private Bitmap loadAndCacheTexture(int type) {
        AndroidAssetLoader.ImageAsset asset = AndroidAssetLoader.load(BlockType.getTexturePath(type));

        if (asset == null || asset.bitmap == null) {
            Log.w(TAG, "Failed to load texture: " + BlockType.getTexturePath(type) + ", using fallback");
            Bitmap scaled = scaleTexture(fallbackTexture);
            textureCache.put(type, scaled);
            return scaled;
        }

        // Check if this is an animated texture
        if (asset.isAnimated && asset.frames != null && !asset.frames.isEmpty()) {
            // Scale all frames and create a new ImageAsset
            AndroidAssetLoader.ImageAsset scaledAsset = scaleAnimatedAsset(asset);
            animatedTextureCache.put(type, scaledAsset);
            Log.d(TAG, "Loaded animated block texture: " + BlockType.getName(type)
                    + " (" + scaledAsset.frames.size() + " frames)");
            return scaledAsset.bitmap;
        } else {
            // Static texture
            Bitmap scaled = scaleTexture(asset.bitmap);
            textureCache.put(type, scaled);
            return scaled;
        }
    }

    /**
     * Gets a tinted version of a block texture.
     * Tinted textures are cached for reuse.
     */
    public Bitmap getTintedTexture(int type, int red, int green, int blue) {
        String cacheKey = BlockType.getName(type) + "_" + red + "_" + green + "_" + blue;

        if (tintedTextureCache.containsKey(cacheKey)) {
            return tintedTextureCache.get(cacheKey);
        }

        Bitmap baseTexture = getTexture(type);
        Bitmap tinted = applyTint(baseTexture, red, green, blue);
        tintedTextureCache.put(cacheKey, tinted);
        return tinted;
    }

    /**
     * Gets an overlay texture, loading from file or generating if not available.
     */
    public Bitmap getOverlayTexture(int overlay) {
        if (overlay == BlockOverlay.NONE) {
            return null;
        }

        if (overlayTextureCache.get(overlay) != null) {
            return overlayTextureCache.get(overlay);
        }

        // Try to load from file first
        String texturePath = BlockOverlay.getTexturePath(overlay);
        if (texturePath != null) {
            AndroidAssetLoader.ImageAsset asset = AndroidAssetLoader.load(texturePath);
            if (asset != null && asset.bitmap != null) {
                Bitmap scaled = scaleTexture(asset.bitmap);
                overlayTextureCache.put(overlay, scaled);
                Log.d(TAG, "Loaded overlay texture: " + BlockOverlay.getName(overlay));
                return scaled;
            }
        }

        // Generate texture if file not found
        Bitmap generated = BlockOverlay.generateTexture(overlay, BLOCK_SIZE);
        overlayTextureCache.put(overlay, generated);
        Log.d(TAG, "Generated overlay texture: " + BlockOverlay.getName(overlay));
        return generated;
    }

    /**
     * Scales an animated asset - all frames scaled to BLOCK_SIZE.
     */
    private AndroidAssetLoader.ImageAsset scaleAnimatedAsset(AndroidAssetLoader.ImageAsset source) {
        java.util.List<Bitmap> scaledFrames = new java.util.ArrayList<>();
        java.util.List<Integer> delays = new java.util.ArrayList<>();

        for (int i = 0; i < source.frames.size(); i++) {
            scaledFrames.add(scaleTexture(source.frames.get(i)));
        }

        // Copy delays from original
        if (source.delays != null) {
            delays.addAll(source.delays);
        } else {
            // Default delay if none specified
            for (int i = 0; i < source.frames.size(); i++) {
                delays.add(100);
            }
        }

        return new AndroidAssetLoader.ImageAsset(scaledFrames, delays);
    }

    /**
     * Scales a texture to BLOCK_SIZE x BLOCK_SIZE.
     * Uses nearest-neighbor filtering (false param) to preserve pixel art.
     */
    private Bitmap scaleTexture(Bitmap source) {
        if (source == null || source.isRecycled()) {
            return scaleTexture(fallbackTexture);
        }
        // filter=false gives nearest-neighbor interpolation for crisp pixel art
        return Bitmap.createScaledBitmap(source, BLOCK_SIZE, BLOCK_SIZE, false);
    }

    /**
     * Applies a color tint to a bitmap using multiplicative blending.
     */
    private Bitmap applyTint(Bitmap source, int red, int green, int blue) {
        if (source == null || source.isRecycled()) return null;

        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap tinted = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        float rFactor = red / 255.0f;
        float gFactor = green / 255.0f;
        float bFactor = blue / 255.0f;

        // Bulk pixel operation for performance
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            int alpha = (argb >> 24) & 0xFF;

            if (alpha > 0) {
                int r = Math.min(255, Math.round(((argb >> 16) & 0xFF) * rFactor));
                int g = Math.min(255, Math.round(((argb >> 8) & 0xFF) * gFactor));
                int b = Math.min(255, Math.round((argb & 0xFF) * bFactor));
                pixels[i] = (alpha << 24) | (r << 16) | (g << 8) | b;
            }
        }

        tinted.setPixels(pixels, 0, width, 0, 0, width, height);
        return tinted;
    }

    /**
     * Preloads all block textures for faster runtime performance.
     * Call this during game initialization.
     */
    public void preloadAllTextures() {
        Log.d(TAG, "Preloading all block textures...");
        for (int type = 0; type < BlockType.COUNT; type++) {
            getTexture(type);
        }
        Log.d(TAG, "Preloaded " + textureCache.size() + " textures");
    }

    /**
     * Clears all cached textures to free memory.
     * Call when switching levels or during cleanup.
     */
    public void clearCache() {
        // Recycle bitmaps to free native memory
        for (int i = 0; i < textureCache.size(); i++) {
            Bitmap bmp = textureCache.valueAt(i);
            if (bmp != null && !bmp.isRecycled()) bmp.recycle();
        }
        for (Bitmap bmp : tintedTextureCache.values()) {
            if (bmp != null && !bmp.isRecycled()) bmp.recycle();
        }
        for (int i = 0; i < overlayTextureCache.size(); i++) {
            Bitmap bmp = overlayTextureCache.valueAt(i);
            if (bmp != null && !bmp.isRecycled()) bmp.recycle();
        }

        textureCache.clear();
        animatedTextureCache.clear();
        tintedTextureCache.clear();
        overlayTextureCache.clear();
        Log.d(TAG, "Cache cleared");
    }

    /**
     * Gets the standard block size (scaled).
     */
    public int getBlockSize() {
        return BLOCK_SIZE;
    }
}
