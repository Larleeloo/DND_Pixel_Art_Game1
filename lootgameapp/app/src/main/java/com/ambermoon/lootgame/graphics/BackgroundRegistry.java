package com.ambermoon.lootgame.graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of cosmetic backgrounds available in the app.
 * Background images are 64 pixels high by 32 pixels wide (pixel art).
 * They are tiled across the screen with nearest-neighbor scaling.
 */
public class BackgroundRegistry {

    private static final String TAG = "BackgroundRegistry";
    private static final String BACKGROUNDS_BASE_PATH = "backgrounds/";

    public static final int BG_PIXEL_WIDTH = 32;
    public static final int BG_PIXEL_HEIGHT = 64;

    private static final LinkedHashMap<String, BackgroundEntry> entries = new LinkedHashMap<>();
    private static boolean initialized = false;

    public static class BackgroundEntry {
        public final String id;
        public final String displayName;
        public final String assetPath;     // null for solid-color built-ins
        public final int solidColor;       // used when assetPath is null
        public final boolean isBuiltIn;

        private Bitmap cachedBitmap;

        public BackgroundEntry(String id, String displayName, String assetPath) {
            this.id = id;
            this.displayName = displayName;
            this.assetPath = assetPath;
            this.solidColor = 0;
            this.isBuiltIn = false;
        }

        public BackgroundEntry(String id, String displayName, int solidColor) {
            this.id = id;
            this.displayName = displayName;
            this.assetPath = null;
            this.solidColor = solidColor;
            this.isBuiltIn = true;
        }

        /**
         * Loads the background bitmap from assets.
         * Returns null for built-in solid-color backgrounds.
         */
        public Bitmap getBitmap() {
            if (isBuiltIn) return null;
            if (cachedBitmap != null) return cachedBitmap;
            if (assetPath == null) return null;

            AssetLoader.ImageAsset asset = AssetLoader.load(assetPath);
            if (asset != null && asset.bitmap != null) {
                cachedBitmap = asset.bitmap;
            }
            return cachedBitmap;
        }
    }

    public static void initialize() {
        if (initialized) return;

        // Built-in solid-color backgrounds (always available)
        register(new BackgroundEntry("none", "Default", Color.parseColor("#1A1525")));
        register(new BackgroundEntry("dark_void", "Dark Void", Color.parseColor("#0A0A12")));
        register(new BackgroundEntry("midnight_blue", "Midnight Blue", Color.parseColor("#0D1B2A")));
        register(new BackgroundEntry("deep_crimson", "Deep Crimson", Color.parseColor("#1A0A0A")));
        register(new BackgroundEntry("forest_dark", "Forest Dark", Color.parseColor("#0A1A0D")));

        // Scan for image-based backgrounds in the assets folder
        String[] files = AssetLoader.list(BACKGROUNDS_BASE_PATH);
        if (files != null) {
            for (String file : files) {
                if (file.equals(".gitkeep")) continue;
                if (file.endsWith(".png") || file.endsWith(".gif")) {
                    String id = file.substring(0, file.lastIndexOf('.'));
                    String displayName = formatDisplayName(id);
                    String assetPath = BACKGROUNDS_BASE_PATH + file;

                    if (!entries.containsKey(id)) {
                        register(new BackgroundEntry(id, displayName, assetPath));
                    }
                }
            }
        }

        initialized = true;
        Log.d(TAG, "BackgroundRegistry initialized with " + entries.size() + " backgrounds");
    }

    private static void register(BackgroundEntry entry) {
        entries.put(entry.id, entry);
    }

    /**
     * Converts an underscore-separated id to a display name.
     * e.g. "starry_night" becomes "Starry Night"
     */
    private static String formatDisplayName(String id) {
        String[] parts = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) sb.append(part.substring(1));
        }
        return sb.toString();
    }

    public static BackgroundEntry get(String id) {
        initialize();
        if (id == null || id.isEmpty()) return entries.get("none");
        BackgroundEntry entry = entries.get(id);
        return entry != null ? entry : entries.get("none");
    }

    public static List<BackgroundEntry> getAll() {
        initialize();
        return new ArrayList<>(entries.values());
    }

    public static boolean exists(String id) {
        initialize();
        return entries.containsKey(id);
    }

    public static void reset() {
        entries.clear();
        initialized = false;
    }
}
