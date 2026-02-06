package com.ambermoon.lootgame.graphics;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and caches assets from Android asset folder.
 * Provides full GIF frame extraction via custom GifDecoder.
 */
public class AssetLoader {
    private static final String TAG = "AssetLoader";
    private static Context appContext;
    private static final Map<String, ImageAsset> cache = new HashMap<>();

    public static class ImageAsset {
        public Bitmap bitmap;           // first frame or static image
        public List<Bitmap> frames;     // all frames for animated GIF
        public List<Integer> delays;    // per-frame delay in ms
        public boolean isAnimated;
        public int width, height;

        public Bitmap getFrame(long elapsedMs) {
            if (!isAnimated || frames == null || frames.size() <= 1) return bitmap;
            long total = 0;
            for (int d : delays) total += d;
            if (total <= 0) return bitmap;
            long pos = elapsedMs % total;
            long accum = 0;
            for (int i = 0; i < frames.size(); i++) {
                accum += delays.get(i);
                if (pos < accum) return frames.get(i);
            }
            return bitmap;
        }
    }

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static ImageAsset load(String path) {
        if (appContext == null || path == null) return null;
        ImageAsset cached = cache.get(path);
        if (cached != null) return cached;

        try {
            InputStream is = appContext.getAssets().open(path);
            ImageAsset asset = new ImageAsset();

            if (path.toLowerCase().endsWith(".gif")) {
                // Full GIF decoding with all frames
                GifDecoder decoder = new GifDecoder();
                GifDecoder.GifResult result = decoder.decode(is);
                is.close();

                if (result.frames.size() > 0) {
                    asset.frames = result.frames;
                    asset.delays = result.delays;
                    asset.bitmap = result.frames.get(0);
                    asset.isAnimated = result.frames.size() > 1;
                    asset.width = result.width;
                    asset.height = result.height;
                } else {
                    // Fallback: try as static image
                    is = appContext.getAssets().open(path);
                    asset.bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    if (asset.bitmap != null) {
                        asset.width = asset.bitmap.getWidth();
                        asset.height = asset.bitmap.getHeight();
                    }
                }
            } else {
                asset.bitmap = BitmapFactory.decodeStream(is);
                is.close();
                if (asset.bitmap != null) {
                    asset.width = asset.bitmap.getWidth();
                    asset.height = asset.bitmap.getHeight();
                }
            }

            cache.put(path, asset);
            return asset;
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean exists(String path) {
        if (appContext == null || path == null) return false;
        try {
            InputStream is = appContext.getAssets().open(path);
            is.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String[] list(String path) {
        if (appContext == null || path == null) return null;
        try {
            String[] files = appContext.getAssets().list(path);
            return (files != null && files.length > 0) ? files : null;
        } catch (IOException e) {
            return null;
        }
    }

    public static void clearCache() {
        cache.clear();
    }
}
