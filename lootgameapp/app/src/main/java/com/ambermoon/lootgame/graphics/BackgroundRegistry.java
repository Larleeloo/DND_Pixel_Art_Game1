package com.ambermoon.lootgame.graphics;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registry of cosmetic backgrounds available in the app.
 * Background images are 64 pixels high by 32 pixels wide (pixel art).
 * Solid-color backgrounds can be unlocked via chest drops.
 */
public class BackgroundRegistry {

    private static final String TAG = "BackgroundRegistry";
    private static final String BACKGROUNDS_BASE_PATH = "backgrounds/";

    public static final int BG_PIXEL_WIDTH = 32;
    public static final int BG_PIXEL_HEIGHT = 64;

    // Rarity constants (mirrors Item rarity)
    public static final int RARITY_COMMON = 0;
    public static final int RARITY_UNCOMMON = 1;
    public static final int RARITY_RARE = 2;
    public static final int RARITY_EPIC = 3;
    public static final int RARITY_LEGENDARY = 4;
    public static final int RARITY_MYTHIC = 5;

    private static final int[] RARITY_COLORS = {
        Color.WHITE,
        Color.rgb(30, 255, 30),
        Color.rgb(30, 100, 255),
        Color.rgb(180, 30, 255),
        Color.rgb(255, 165, 0),
        Color.rgb(0, 255, 255)
    };

    private static final String[] RARITY_NAMES = {
        "Common", "Uncommon", "Rare", "Epic", "Legendary", "Mythic"
    };

    public static int getRarityColor(int rarity) {
        if (rarity >= 0 && rarity < RARITY_COLORS.length) return RARITY_COLORS[rarity];
        return Color.WHITE;
    }

    public static String getRarityName(int rarity) {
        if (rarity >= 0 && rarity < RARITY_NAMES.length) return RARITY_NAMES[rarity];
        return "Common";
    }

    private static final LinkedHashMap<String, BackgroundEntry> entries = new LinkedHashMap<>();
    private static boolean initialized = false;
    private static final SecureRandom rng = new SecureRandom();

    public static class BackgroundEntry {
        public final String id;
        public final String displayName;
        public final String assetPath;     // null for solid-color backgrounds
        public final int solidColor;       // used when assetPath is null
        public final boolean isSolidColor;
        public final int rarity;           // RARITY_COMMON through RARITY_MYTHIC
        public final boolean alwaysUnlocked;

        private AssetLoader.ImageAsset cachedAsset;

        /** Image-based background from assets folder. */
        public BackgroundEntry(String id, String displayName, String assetPath, int rarity) {
            this.id = id;
            this.displayName = displayName;
            this.assetPath = assetPath;
            this.solidColor = 0;
            this.isSolidColor = false;
            this.rarity = rarity;
            this.alwaysUnlocked = false;
        }

        /** Solid-color background (unlockable). */
        public BackgroundEntry(String id, String displayName, int solidColor, int rarity, boolean alwaysUnlocked) {
            this.id = id;
            this.displayName = displayName;
            this.assetPath = null;
            this.solidColor = solidColor;
            this.isSolidColor = true;
            this.rarity = rarity;
            this.alwaysUnlocked = alwaysUnlocked;
        }

        public AssetLoader.ImageAsset getImageAsset() {
            if (isSolidColor) return null;
            if (cachedAsset != null) return cachedAsset;
            if (assetPath == null) return null;
            cachedAsset = AssetLoader.load(assetPath);
            return cachedAsset;
        }

        public Bitmap getBitmap() {
            AssetLoader.ImageAsset asset = getImageAsset();
            return (asset != null) ? asset.bitmap : null;
        }
    }

    public static void initialize() {
        if (initialized) return;

        // === ALWAYS UNLOCKED ===
        register(new BackgroundEntry("none", "Default", Color.parseColor("#1A1525"), RARITY_COMMON, true));

        // === COMMON (8 backgrounds) ===
        register(new BackgroundEntry("obsidian_night", "Obsidian Night", Color.parseColor("#08080E"), RARITY_COMMON, false));
        register(new BackgroundEntry("charcoal", "Charcoal", Color.parseColor("#1C1C1C"), RARITY_COMMON, false));
        register(new BackgroundEntry("slate_gray", "Slate Gray", Color.parseColor("#2C2C3A"), RARITY_COMMON, false));
        register(new BackgroundEntry("dark_olive", "Dark Olive", Color.parseColor("#1A1A0A"), RARITY_COMMON, false));
        register(new BackgroundEntry("dusty_brown", "Dusty Brown", Color.parseColor("#1A120A"), RARITY_COMMON, false));
        register(new BackgroundEntry("deep_clay", "Deep Clay", Color.parseColor("#1E140E"), RARITY_COMMON, false));
        register(new BackgroundEntry("iron_dark", "Iron Dark", Color.parseColor("#18181E"), RARITY_COMMON, false));
        register(new BackgroundEntry("cave_stone", "Cave Stone", Color.parseColor("#22201C"), RARITY_COMMON, false));

        // === UNCOMMON (8 backgrounds) ===
        register(new BackgroundEntry("royal_purple", "Royal Purple", Color.parseColor("#1A0A2A"), RARITY_UNCOMMON, false));
        register(new BackgroundEntry("ocean_depths", "Ocean Depths", Color.parseColor("#0A1A2A"), RARITY_UNCOMMON, false));
        register(new BackgroundEntry("ember_glow", "Ember Glow", Color.parseColor("#2A1A0A"), RARITY_UNCOMMON, false));
        register(new BackgroundEntry("pine_shadow", "Pine Shadow", Color.parseColor("#0A2A1A"), RARITY_UNCOMMON, false));
        register(new BackgroundEntry("twilight_violet", "Twilight Violet", Color.parseColor("#200A28"), RARITY_UNCOMMON, false));
        register(new BackgroundEntry("storm_cloud", "Storm Cloud", Color.parseColor("#1A1A2A"), RARITY_UNCOMMON, false));
        register(new BackgroundEntry("copper_dusk", "Copper Dusk", Color.parseColor("#2A1A10"), RARITY_UNCOMMON, false));
        register(new BackgroundEntry("moss_stone", "Moss Stone", Color.parseColor("#1A2A1A"), RARITY_UNCOMMON, false));

        // === RARE (6 backgrounds) ===
        register(new BackgroundEntry("sapphire_abyss", "Sapphire Abyss", Color.parseColor("#0A0A3A"), RARITY_RARE, false));
        register(new BackgroundEntry("ruby_depths", "Ruby Depths", Color.parseColor("#3A0A0A"), RARITY_RARE, false));
        register(new BackgroundEntry("emerald_cavern", "Emerald Cavern", Color.parseColor("#0A3A0A"), RARITY_RARE, false));
        register(new BackgroundEntry("amber_glow", "Amber Glow", Color.parseColor("#3A2A0A"), RARITY_RARE, false));
        register(new BackgroundEntry("amethyst_haze", "Amethyst Haze", Color.parseColor("#2A0A3A"), RARITY_RARE, false));
        register(new BackgroundEntry("teal_shadow", "Teal Shadow", Color.parseColor("#0A3A3A"), RARITY_RARE, false));

        // === EPIC (5 backgrounds) ===
        register(new BackgroundEntry("dragonfire", "Dragonfire", Color.parseColor("#4A1A00"), RARITY_EPIC, false));
        register(new BackgroundEntry("void_walker", "Void Walker", Color.parseColor("#0A0020"), RARITY_EPIC, false));
        register(new BackgroundEntry("enchanted_forest", "Enchanted Forest", Color.parseColor("#003A1A"), RARITY_EPIC, false));
        register(new BackgroundEntry("blood_moon", "Blood Moon", Color.parseColor("#3A0014"), RARITY_EPIC, false));
        register(new BackgroundEntry("frozen_tundra", "Frozen Tundra", Color.parseColor("#0A2A3A"), RARITY_EPIC, false));

        // === LEGENDARY (3 backgrounds) ===
        register(new BackgroundEntry("phoenix_flame", "Phoenix Flame", Color.parseColor("#5A2A00"), RARITY_LEGENDARY, false));
        register(new BackgroundEntry("abyssal_dark", "Abyssal Dark", Color.parseColor("#000020"), RARITY_LEGENDARY, false));
        register(new BackgroundEntry("celestial_gold", "Celestial Gold", Color.parseColor("#3A3A00"), RARITY_LEGENDARY, false));

        // === MYTHIC (2 backgrounds) ===
        register(new BackgroundEntry("astral_plane", "Astral Plane", Color.parseColor("#1A0A3A"), RARITY_MYTHIC, false));
        register(new BackgroundEntry("primordial_chaos", "Primordial Chaos", Color.parseColor("#2A0A1A"), RARITY_MYTHIC, false));

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
                        register(new BackgroundEntry(id, displayName, assetPath, RARITY_COMMON));
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

    /**
     * Returns all backgrounds that are locked (not yet unlocked by the player).
     */
    public static List<BackgroundEntry> getLocked(Set<String> unlockedIds) {
        initialize();
        List<BackgroundEntry> locked = new ArrayList<>();
        for (BackgroundEntry entry : entries.values()) {
            if (!entry.alwaysUnlocked && !unlockedIds.contains(entry.id)) {
                locked.add(entry);
            }
        }
        return locked;
    }

    /**
     * Checks whether a background is unlocked for the player.
     */
    public static boolean isUnlocked(String id, Set<String> unlockedIds) {
        initialize();
        BackgroundEntry entry = entries.get(id);
        if (entry == null) return false;
        return entry.alwaysUnlocked || unlockedIds.contains(id);
    }

    /**
     * Roll for a random background drop from a chest.
     * Returns a BackgroundEntry if a new (not yet unlocked) background was dropped, or null.
     *
     * @param unlockedIds  set of already-unlocked background IDs
     * @param rarityBoost  multiplier for higher-rarity drops (1.0 = daily, 2.5 = monthly)
     * @param dropChance   base probability of getting any background (0.0 to 1.0)
     */
    public static BackgroundEntry rollChestDrop(Set<String> unlockedIds, float rarityBoost, float dropChance) {
        initialize();

        // First check if a background drops at all
        if (rng.nextFloat() > dropChance) return null;

        // Get locked backgrounds only
        List<BackgroundEntry> locked = getLocked(unlockedIds);
        if (locked.isEmpty()) return null;

        // Roll a rarity tier using weighted distribution, boosted by rarityBoost
        // Base weights: Common=100, Uncommon=50, Rare=25, Epic=10, Legendary=3, Mythic=1
        float[] baseWeights = {100f, 50f, 25f, 10f, 3f, 1f};
        float[] weights = new float[baseWeights.length];
        for (int i = 0; i < weights.length; i++) {
            // Boost higher rarities: multiply weight of uncommon+ by rarityBoost
            weights[i] = (i == 0) ? baseWeights[i] : baseWeights[i] * rarityBoost;
        }

        float totalWeight = 0;
        for (float w : weights) totalWeight += w;

        float roll = rng.nextFloat() * totalWeight;
        int targetRarity = 0;
        float accum = 0;
        for (int i = 0; i < weights.length; i++) {
            accum += weights[i];
            if (roll < accum) {
                targetRarity = i;
                break;
            }
        }

        // Find locked backgrounds at the rolled rarity
        List<BackgroundEntry> candidates = new ArrayList<>();
        for (BackgroundEntry entry : locked) {
            if (entry.rarity == targetRarity) {
                candidates.add(entry);
            }
        }

        // If no candidates at this rarity, try the nearest lower rarity
        if (candidates.isEmpty()) {
            for (int r = targetRarity - 1; r >= 0; r--) {
                for (BackgroundEntry entry : locked) {
                    if (entry.rarity == r) candidates.add(entry);
                }
                if (!candidates.isEmpty()) break;
            }
        }

        // Still empty? Try higher rarities
        if (candidates.isEmpty()) {
            for (int r = targetRarity + 1; r < RARITY_NAMES.length; r++) {
                for (BackgroundEntry entry : locked) {
                    if (entry.rarity == r) candidates.add(entry);
                }
                if (!candidates.isEmpty()) break;
            }
        }

        if (candidates.isEmpty()) return null;

        // Pick a random one from the candidates
        return candidates.get(rng.nextInt(candidates.size()));
    }

    public static void reset() {
        entries.clear();
        initialized = false;
    }
}
