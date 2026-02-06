package com.ambermoongame.ui;

import android.util.SparseArray;

import com.ambermoongame.block.BlockType;

/**
 * Defines tool types and their mining effectiveness against different block types.
 * Uses int constants instead of enum to avoid D8 compiler issues.
 * Each tool has speed multipliers for different block categories.
 */
public final class ToolType {
    public static final int HAND = 0;
    public static final int PICKAXE = 1;
    public static final int SHOVEL = 2;
    public static final int AXE = 3;
    public static final int COUNT = 4;

    private static final String[] DISPLAY_NAMES = {"Hand", "Pickaxe", "Shovel", "Axe"};
    private static final float[] BASE_DURABILITIES = {1.0f, 1.0f, 1.0f, 1.0f};

    // Speed multipliers per tool type and block type
    // Stored as SparseArray[toolType] -> SparseArray[blockType] -> multiplier
    private static final SparseArray<SparseArray<Float>> SPEED_MULTIPLIERS = new SparseArray<>();

    static {
        // Initialize PICKAXE multipliers
        SparseArray<Float> pickaxeMults = new SparseArray<>();
        pickaxeMults.put(BlockType.STONE, 4.0f);
        pickaxeMults.put(BlockType.COBBLESTONE, 4.0f);
        pickaxeMults.put(BlockType.BRICK, 4.0f);
        pickaxeMults.put(BlockType.COAL_ORE, 4.0f);
        pickaxeMults.put(BlockType.IRON_ORE, 4.0f);
        pickaxeMults.put(BlockType.GOLD_ORE, 4.0f);
        SPEED_MULTIPLIERS.put(PICKAXE, pickaxeMults);

        // Initialize SHOVEL multipliers
        SparseArray<Float> shovelMults = new SparseArray<>();
        shovelMults.put(BlockType.DIRT, 4.0f);
        shovelMults.put(BlockType.GRASS, 4.0f);
        shovelMults.put(BlockType.SAND, 4.0f);
        SPEED_MULTIPLIERS.put(SHOVEL, shovelMults);

        // Initialize AXE multipliers
        SparseArray<Float> axeMults = new SparseArray<>();
        axeMults.put(BlockType.WOOD, 4.0f);
        axeMults.put(BlockType.LEAVES, 4.0f);
        axeMults.put(BlockType.PLATFORM, 2.0f);
        SPEED_MULTIPLIERS.put(AXE, axeMults);
    }

    private ToolType() {}

    public static float getSpeedMultiplier(int toolType, int blockType) {
        SparseArray<Float> toolMults = SPEED_MULTIPLIERS.get(toolType);
        if (toolMults != null) {
            Float mult = toolMults.get(blockType);
            if (mult != null) {
                return mult;
            }
        }
        return 1.0f;
    }

    public static int getLayersPerMine(int toolType, int blockType) {
        float multiplier = getSpeedMultiplier(toolType, blockType);
        return Math.max(1, Math.min(4, (int) multiplier));
    }

    public static String getDisplayName(int toolType) {
        if (toolType >= 0 && toolType < COUNT) {
            return DISPLAY_NAMES[toolType];
        }
        return "Unknown";
    }

    public static float getBaseDurability(int toolType) {
        if (toolType >= 0 && toolType < COUNT) {
            return BASE_DURABILITIES[toolType];
        }
        return 1.0f;
    }

    public static String getName(int toolType) {
        switch (toolType) {
            case HAND: return "HAND";
            case PICKAXE: return "PICKAXE";
            case SHOVEL: return "SHOVEL";
            case AXE: return "AXE";
            default: return "UNKNOWN";
        }
    }

    public static int fromItemType(String itemType) {
        if (itemType == null) return HAND;

        String lower = itemType.toLowerCase();
        if (lower.contains("pickaxe")) return PICKAXE;
        if (lower.contains("shovel")) return SHOVEL;
        if (lower.contains("axe")) return AXE;

        return HAND;
    }

    public static boolean isEffectiveAgainst(int toolType, int blockType) {
        SparseArray<Float> toolMults = SPEED_MULTIPLIERS.get(toolType);
        if (toolMults != null) {
            return toolMults.get(blockType) != null;
        }
        return false;
    }
}
