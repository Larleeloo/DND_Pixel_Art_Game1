/**
 * Enum defining all available block types in the game.
 * Each block type has associated properties like texture path,
 * solidity, and display name.
 */
public enum BlockType {
    // Terrain blocks
    GRASS("blocks/grass.png", true, "Grass"),
    DIRT("blocks/dirt.png", true, "Dirt"),
    STONE("blocks/stone.png", true, "Stone"),
    COBBLESTONE("blocks/cobblestone.png", true, "Cobblestone"),

    // Nature blocks
    WOOD("blocks/wood.png", true, "Wood"),
    LEAVES("blocks/leaves.png", false, "Leaves"),

    // Special blocks
    BRICK("blocks/brick.png", true, "Brick"),
    SAND("blocks/sand.png", true, "Sand"),
    WATER("blocks/water.png", false, "Water"),

    // Decorative blocks
    GLASS("blocks/glass.png", false, "Glass"),

    // Ore blocks
    COAL_ORE("blocks/coal_ore.png", true, "Coal Ore"),
    IRON_ORE("blocks/iron_ore.png", true, "Iron Ore"),
    GOLD_ORE("blocks/gold_ore.png", true, "Gold Ore"),

    // Platform block (for backwards compatibility with obstacle.png)
    PLATFORM("assets/obstacle.png", true, "Platform");

    private final String texturePath;
    private final boolean solid;
    private final String displayName;

    BlockType(String texturePath, boolean solid, String displayName) {
        this.texturePath = texturePath;
        this.solid = solid;
        this.displayName = displayName;
    }

    /**
     * Gets the path to this block's texture file.
     * @return Texture file path relative to project root
     */
    public String getTexturePath() {
        return texturePath;
    }

    /**
     * Whether this block type is solid (blocks player movement).
     * @return true if solid, false if passable
     */
    public boolean isSolid() {
        return solid;
    }

    /**
     * Gets the human-readable display name for this block.
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Find a BlockType by its name (case-insensitive).
     * @param name The name to search for
     * @return The matching BlockType, or DIRT as default
     */
    public static BlockType fromName(String name) {
        if (name == null || name.isEmpty()) {
            return DIRT;
        }

        String upperName = name.toUpperCase().replace(" ", "_");
        for (BlockType type : values()) {
            if (type.name().equals(upperName)) {
                return type;
            }
        }

        // Also try matching display name
        for (BlockType type : values()) {
            if (type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }

        return DIRT; // Default fallback
    }
}
