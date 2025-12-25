package ui;
import entity.*;
import block.*;
import audio.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines tool types and their mining effectiveness against different block types.
 * Each tool has speed multipliers for different block categories.
 */
public enum ToolType {
    // No tool - base mining speed
    HAND("Hand", 1.0f),

    // Pickaxe - effective against stone, ores
    PICKAXE("Pickaxe", 1.0f),

    // Shovel - effective against dirt, sand, grass
    SHOVEL("Shovel", 1.0f),

    // Axe - effective against wood, leaves
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
                // Pickaxe is effective against stone-type blocks
                speedMultipliers.put(BlockType.STONE, 4.0f);
                speedMultipliers.put(BlockType.COBBLESTONE, 4.0f);
                speedMultipliers.put(BlockType.BRICK, 4.0f);
                speedMultipliers.put(BlockType.COAL_ORE, 4.0f);
                speedMultipliers.put(BlockType.IRON_ORE, 4.0f);
                speedMultipliers.put(BlockType.GOLD_ORE, 4.0f);
                break;

            case SHOVEL:
                // Shovel is effective against soft blocks
                speedMultipliers.put(BlockType.DIRT, 4.0f);
                speedMultipliers.put(BlockType.GRASS, 4.0f);
                speedMultipliers.put(BlockType.SAND, 4.0f);
                break;

            case AXE:
                // Axe is effective against wood-type blocks
                speedMultipliers.put(BlockType.WOOD, 4.0f);
                speedMultipliers.put(BlockType.LEAVES, 4.0f);
                speedMultipliers.put(BlockType.PLATFORM, 2.0f);
                break;

            case HAND:
            default:
                // Hand has no bonuses (1.0x for everything)
                break;
        }
    }

    /**
     * Gets the mining speed multiplier for a specific block type.
     * @param blockType The block being mined
     * @return Speed multiplier (1.0 = normal, higher = faster)
     */
    public float getSpeedMultiplier(BlockType blockType) {
        return speedMultipliers.getOrDefault(blockType, 1.0f);
    }

    /**
     * Gets the number of layers mined per action for a specific block type.
     * Base is 1 layer, tools can mine multiple layers at once.
     * @param blockType The block being mined
     * @return Number of layers to mine (1-4)
     */
    public int getLayersPerMine(BlockType blockType) {
        float multiplier = getSpeedMultiplier(blockType);
        // Convert multiplier to layers: 1x = 1 layer, 4x = 4 layers
        return Math.max(1, Math.min(4, (int) multiplier));
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getBaseDurability() {
        return baseDurability;
    }

    /**
     * Determines the tool type from an item type string.
     * @param itemType The item's type string (e.g., "pickaxe", "shovel")
     * @return The corresponding ToolType, or HAND if not a tool
     */
    public static ToolType fromItemType(String itemType) {
        if (itemType == null) return HAND;

        String lower = itemType.toLowerCase();
        if (lower.contains("pickaxe")) return PICKAXE;
        if (lower.contains("shovel")) return SHOVEL;
        if (lower.contains("axe")) return AXE;

        return HAND;
    }

    /**
     * Checks if this tool type is effective against the given block.
     * @param blockType The block to check
     * @return true if this tool has a speed bonus for this block
     */
    public boolean isEffectiveAgainst(BlockType blockType) {
        return speedMultipliers.containsKey(blockType);
    }
}
