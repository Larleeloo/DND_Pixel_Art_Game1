package com.ambermoongame.ui;

import com.ambermoongame.block.BlockType;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines tool types and their mining effectiveness against different block types.
 * Each tool has speed multipliers for different block categories.
 */
public enum ToolType {
    HAND("Hand", 1.0f),
    PICKAXE("Pickaxe", 1.0f),
    SHOVEL("Shovel", 1.0f),
    AXE("Axe", 1.0f);

    private final String displayName;
    private final float baseDurability;
    private final Map<BlockType, Float> speedMultipliers;

    ToolType(String displayName, float baseDurability) {
        this.displayName = displayName;
        this.baseDurability = baseDurability;
        this.speedMultipliers = new HashMap<>();
        initializeMultipliers();
    }

    private void initializeMultipliers() {
        switch (this) {
            case PICKAXE:
                speedMultipliers.put(BlockType.STONE, 4.0f);
                speedMultipliers.put(BlockType.COBBLESTONE, 4.0f);
                speedMultipliers.put(BlockType.BRICK, 4.0f);
                speedMultipliers.put(BlockType.COAL_ORE, 4.0f);
                speedMultipliers.put(BlockType.IRON_ORE, 4.0f);
                speedMultipliers.put(BlockType.GOLD_ORE, 4.0f);
                break;

            case SHOVEL:
                speedMultipliers.put(BlockType.DIRT, 4.0f);
                speedMultipliers.put(BlockType.GRASS, 4.0f);
                speedMultipliers.put(BlockType.SAND, 4.0f);
                break;

            case AXE:
                speedMultipliers.put(BlockType.WOOD, 4.0f);
                speedMultipliers.put(BlockType.LEAVES, 4.0f);
                speedMultipliers.put(BlockType.PLATFORM, 2.0f);
                break;

            case HAND:
            default:
                break;
        }
    }

    public float getSpeedMultiplier(BlockType blockType) {
        return speedMultipliers.getOrDefault(blockType, 1.0f);
    }

    public int getLayersPerMine(BlockType blockType) {
        float multiplier = getSpeedMultiplier(blockType);
        return Math.max(1, Math.min(4, (int) multiplier));
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getBaseDurability() {
        return baseDurability;
    }

    public static ToolType fromItemType(String itemType) {
        if (itemType == null) return HAND;

        String lower = itemType.toLowerCase();
        if (lower.contains("pickaxe")) return PICKAXE;
        if (lower.contains("shovel")) return SHOVEL;
        if (lower.contains("axe")) return AXE;

        return HAND;
    }

    public boolean isEffectiveAgainst(BlockType blockType) {
        return speedMultipliers.containsKey(blockType);
    }
}
