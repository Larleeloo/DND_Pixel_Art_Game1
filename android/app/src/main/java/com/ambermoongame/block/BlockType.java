package com.ambermoongame.block;

/**
 * Class defining all available block types in the game.
 * Uses int constants instead of enum to avoid D8 compiler issues.
 * Each block type has associated properties like texture path,
 * solidity, and display name.
 * Equivalent to block/BlockType.java from the desktop version.
 */
public final class BlockType {
    // Terrain blocks
    public static final int GRASS = 0;
    public static final int DIRT = 1;
    public static final int STONE = 2;
    public static final int COBBLESTONE = 3;

    // Nature blocks
    public static final int WOOD = 4;
    public static final int LEAVES = 5;

    // Special blocks
    public static final int BRICK = 6;
    public static final int SAND = 7;
    public static final int WATER = 8;

    // Decorative blocks
    public static final int GLASS = 9;

    // Ore blocks
    public static final int COAL_ORE = 10;
    public static final int IRON_ORE = 11;
    public static final int GOLD_ORE = 12;

    // Weather/Environment blocks
    public static final int SNOW = 13;
    public static final int ICE = 14;
    public static final int MOSS = 15;
    public static final int VINES = 16;

    // Platform block
    public static final int PLATFORM = 17;

    public static final int COUNT = 18;

    private static final String[] TEXTURE_PATHS = {
        "assets/textures/blocks/grass.png",
        "assets/textures/blocks/dirt.png",
        "assets/textures/blocks/stone.png",
        "assets/textures/blocks/cobblestone.png",
        "assets/textures/blocks/wood.png",
        "assets/textures/blocks/leaves.png",
        "assets/textures/blocks/brick.png",
        "assets/textures/blocks/sand.png",
        "assets/textures/blocks/water.png",
        "assets/textures/blocks/glass.png",
        "assets/textures/blocks/coal_ore.png",
        "assets/textures/blocks/iron_ore.png",
        "assets/textures/blocks/gold_ore.png",
        "assets/textures/blocks/snow.png",
        "assets/textures/blocks/ice.png",
        "assets/textures/blocks/moss.png",
        "assets/textures/blocks/vines.png",
        "assets/obstacle.png"
    };

    private static final boolean[] SOLID = {
        true,   // GRASS
        true,   // DIRT
        true,   // STONE
        true,   // COBBLESTONE
        true,   // WOOD
        false,  // LEAVES
        true,   // BRICK
        true,   // SAND
        false,  // WATER
        false,  // GLASS
        true,   // COAL_ORE
        true,   // IRON_ORE
        true,   // GOLD_ORE
        true,   // SNOW
        false,  // ICE
        true,   // MOSS
        false,  // VINES
        true    // PLATFORM
    };

    private static final String[] DISPLAY_NAMES = {
        "Grass", "Dirt", "Stone", "Cobblestone",
        "Wood", "Leaves", "Brick", "Sand", "Water", "Glass",
        "Coal Ore", "Iron Ore", "Gold Ore",
        "Snow", "Ice", "Moss", "Vines", "Platform"
    };

    private BlockType() {}

    /**
     * Gets the path to this block's texture file.
     * @return Texture file path relative to project root
     */
    public static String getTexturePath(int type) {
        if (type >= 0 && type < COUNT) {
            return TEXTURE_PATHS[type];
        }
        return TEXTURE_PATHS[DIRT]; // default
    }

    /**
     * Whether this block type is solid (blocks player movement).
     * @return true if solid, false if passable
     */
    public static boolean isSolid(int type) {
        if (type >= 0 && type < COUNT) {
            return SOLID[type];
        }
        return true;
    }

    /**
     * Gets the human-readable display name for this block.
     * @return Display name
     */
    public static String getDisplayName(int type) {
        if (type >= 0 && type < COUNT) {
            return DISPLAY_NAMES[type];
        }
        return "Unknown";
    }

    /**
     * Gets the internal name for this block type.
     */
    public static String getName(int type) {
        switch (type) {
            case GRASS: return "GRASS";
            case DIRT: return "DIRT";
            case STONE: return "STONE";
            case COBBLESTONE: return "COBBLESTONE";
            case WOOD: return "WOOD";
            case LEAVES: return "LEAVES";
            case BRICK: return "BRICK";
            case SAND: return "SAND";
            case WATER: return "WATER";
            case GLASS: return "GLASS";
            case COAL_ORE: return "COAL_ORE";
            case IRON_ORE: return "IRON_ORE";
            case GOLD_ORE: return "GOLD_ORE";
            case SNOW: return "SNOW";
            case ICE: return "ICE";
            case MOSS: return "MOSS";
            case VINES: return "VINES";
            case PLATFORM: return "PLATFORM";
            default: return "UNKNOWN";
        }
    }

    /**
     * Find a BlockType by its name (case-insensitive).
     * @param name The name to search for
     * @return The matching BlockType constant, or DIRT as default
     */
    public static int fromName(String name) {
        if (name == null || name.isEmpty()) {
            return DIRT;
        }

        String upperName = name.toUpperCase().replace(" ", "_");
        for (int i = 0; i < COUNT; i++) {
            if (getName(i).equals(upperName)) {
                return i;
            }
        }

        // Also try matching display name
        for (int i = 0; i < COUNT; i++) {
            if (DISPLAY_NAMES[i].equalsIgnoreCase(name)) {
                return i;
            }
        }

        return DIRT; // Default fallback
    }
}
