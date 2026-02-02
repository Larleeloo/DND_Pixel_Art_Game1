package com.ambermoongame.graphics;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles loading of image assets for Android.
 * Equivalent to AssetLoader.java from desktop version.
 *
 * Supports:
 * - Static images (PNG, JPG)
 * - Animated GIFs (using Movie class or frame extraction)
 * - Asset caching with simple HashMap
 */
public class AndroidAssetLoader {

    private static final String TAG = "AssetLoader";

    private static Context appContext;
    private static AssetManager assetManager;

    // Simple cache using HashMap (avoids D8 generic class issues)
    private static Map<String, ImageAsset> cache;
    private static int cacheHits = 0;
    private static int cacheMisses = 0;

    /**
     * Initialize the asset loader with application context.
     */
    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
        assetManager = appContext.getAssets();
        cache = new HashMap<String, ImageAsset>();
        cacheHits = 0;
        cacheMisses = 0;
        Log.d(TAG, "AssetLoader initialized");
    }

    /**
     * Represents a loaded image asset.
     */
    public static class ImageAsset {
        public Bitmap bitmap;           // Static image or first frame of GIF
        public Movie movie;             // For legacy GIF support (deprecated in API 28+)
        public List<Bitmap> frames;     // Extracted GIF frames
        public List<Integer> delays;    // Frame delays in ms
        public int width;
        public int height;
        public boolean isAnimated;

        public ImageAsset(Bitmap bitmap) {
            this.bitmap = bitmap;
            this.width = bitmap != null ? bitmap.getWidth() : 0;
            this.height = bitmap != null ? bitmap.getHeight() : 0;
            this.isAnimated = false;
        }

        public ImageAsset(List<Bitmap> frames, List<Integer> delays) {
            this.frames = frames;
            this.delays = delays;
            this.isAnimated = true;
            if (frames != null && !frames.isEmpty()) {
                this.bitmap = frames.get(0);
                this.width = bitmap.getWidth();
                this.height = bitmap.getHeight();
            }
        }

        /**
         * Get the current frame based on elapsed time.
         */
        public Bitmap getFrame(long elapsedMs) {
            if (!isAnimated || frames == null || frames.isEmpty()) {
                return bitmap;
            }

            int totalDuration = 0;
            for (int delay : delays) {
                totalDuration += delay;
            }

            if (totalDuration == 0) return bitmap;

            long cycleTime = elapsedMs % totalDuration;
            int accumulated = 0;

            for (int i = 0; i < frames.size(); i++) {
                accumulated += delays.get(i);
                if (cycleTime < accumulated) {
                    return frames.get(i);
                }
            }

            return bitmap;
        }
    }

    /**
     * Load an image from assets folder.
     * @param path Path relative to assets folder (e.g., "characters/player/idle.gif")
     */
    public static ImageAsset load(String path) {
        if (cache == null) {
            return null;
        }

        // Check cache first
        ImageAsset cached = cache.get(path);
        if (cached != null) {
            cacheHits++;
            return cached;
        }

        cacheMisses++;
        ImageAsset asset = null;

        try {
            if (path.toLowerCase().endsWith(".gif")) {
                asset = loadGif(path);
            } else {
                asset = loadStatic(path);
            }

            if (asset != null) {
                cache.put(path, asset);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to load asset: " + path + " - " + e.getMessage());
        }

        return asset;
    }

    /**
     * Load a static image (PNG, JPG).
     */
    private static ImageAsset loadStatic(String path) throws IOException {
        InputStream is = null;
        try {
            is = assetManager.open(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);

            if (bitmap != null) {
                Log.d(TAG, "Loaded static image: " + path + " (" + bitmap.getWidth() + "x" + bitmap.getHeight() + ")");
                return new ImageAsset(bitmap);
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return null;
    }

    /**
     * Load an animated GIF.
     * Uses frame extraction for better control.
     */
    private static ImageAsset loadGif(String path) throws IOException {
        InputStream is = null;
        try {
            is = assetManager.open(path);

            // Try to decode as regular bitmap first (gets first frame)
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);

            if (bitmap != null) {
                // For now, treat as static - full GIF support requires additional library
                // Consider using android-gif-drawable library for full support
                Log.d(TAG, "Loaded GIF (as static): " + path);
                ImageAsset asset = new ImageAsset(bitmap);
                // Mark as potentially animated for future enhancement
                return asset;
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return null;
    }

    /**
     * Load a bitmap with sampling to reduce memory usage.
     * @param path Asset path
     * @param reqWidth Required width (0 for no scaling)
     * @param reqHeight Required height (0 for no scaling)
     */
    public static Bitmap loadScaled(String path, int reqWidth, int reqHeight) {
        InputStream is = null;
        try {
            // First decode bounds only
            is = assetManager.open(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            is.close();

            // Calculate sample size
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            // Decode with sample size
            is = assetManager.open(path);
            return BitmapFactory.decodeStream(is, null, options);
        } catch (IOException e) {
            Log.w(TAG, "Failed to load scaled: " + path);
            return null;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                // Ignore close exception
            }
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (reqWidth == 0 && reqHeight == 0) {
            return inSampleSize;
        }

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Check if an asset exists.
     */
    public static boolean exists(String path) {
        try {
            InputStream is = assetManager.open(path);
            is.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * List assets in a directory.
     */
    public static String[] list(String path) {
        try {
            return assetManager.list(path);
        } catch (IOException e) {
            return new String[0];
        }
    }

    /**
     * Clear the asset cache.
     */
    public static void clearCache() {
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Get cache statistics.
     */
    public static String getCacheStats() {
        if (cache == null) return "Cache not initialized";
        return "Cache: " + cache.size() + " items, " +
               cacheHits + " hits, " +
               cacheMisses + " misses";
    }
}
