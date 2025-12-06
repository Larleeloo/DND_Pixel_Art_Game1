import java.util.HashMap;
import java.util.Map;

/**
 * Defines attributes for block types including sounds and item drops.
 * Uses a registry pattern to map BlockTypes to their attributes.
 */
public class BlockAttributes {

    private static final Map<BlockType, BlockAttributes> registry = new HashMap<>();

    // Sound paths (null means use default or no sound)
    private String breakSound;
    private String placeSound;
    private String stepSound;

    // Item drop configuration
    private String dropItemName;      // Name of item dropped (null = no drop)
    private String dropItemType;      // Type: "collectible", "block", "resource"
    private String dropSpritePath;    // Sprite for dropped item
    private int dropCount;            // How many items drop (default 1)
    private float dropChance;         // Chance to drop (0.0-1.0, default 1.0)

    // Optional: different item dropped with silk touch / special tools
    private String silkTouchDropName;
    private String silkTouchSpritePath;

    static {
        // Initialize all block attributes
        initializeAttributes();
    }

    private BlockAttributes() {
        this.dropCount = 1;
        this.dropChance = 1.0f;
    }

    /**
     * Gets the attributes for a block type.
     */
    public static BlockAttributes get(BlockType type) {
        return registry.getOrDefault(type, createDefault());
    }

    private static BlockAttributes createDefault() {
        BlockAttributes attrs = new BlockAttributes();
        attrs.breakSound = "sounds/block_break.wav";
        attrs.placeSound = "sounds/block_place.wav";
        attrs.stepSound = "sounds/step_stone.wav";
        return attrs;
    }

    /**
     * Initialize attributes for all block types.
     */
    private static void initializeAttributes() {
        // GRASS - drops dirt, plays grass sounds
        register(BlockType.GRASS, new Builder()
            .breakSound("sounds/grass_break.wav")
            .placeSound("sounds/grass_place.wav")
            .stepSound("sounds/step_grass.wav")
            .drops("Dirt", "block", "blocks/dirt.png")
            .silkTouchDrops("Grass Block", "blocks/grass.png")
            .build());

        // DIRT - drops itself
        register(BlockType.DIRT, new Builder()
            .breakSound("sounds/dirt_break.wav")
            .placeSound("sounds/dirt_place.wav")
            .stepSound("sounds/step_gravel.wav")
            .drops("Dirt", "block", "blocks/dirt.png")
            .build());

        // STONE - drops cobblestone
        register(BlockType.STONE, new Builder()
            .breakSound("sounds/stone_break.wav")
            .placeSound("sounds/stone_place.wav")
            .stepSound("sounds/step_stone.wav")
            .drops("Cobblestone", "block", "blocks/cobblestone.png")
            .silkTouchDrops("Stone", "blocks/stone.png")
            .build());

        // COBBLESTONE - drops itself
        register(BlockType.COBBLESTONE, new Builder()
            .breakSound("sounds/stone_break.wav")
            .placeSound("sounds/stone_place.wav")
            .stepSound("sounds/step_stone.wav")
            .drops("Cobblestone", "block", "blocks/cobblestone.png")
            .build());

        // WOOD - drops itself
        register(BlockType.WOOD, new Builder()
            .breakSound("sounds/wood_break.wav")
            .placeSound("sounds/wood_place.wav")
            .stepSound("sounds/step_wood.wav")
            .drops("Wood", "block", "blocks/wood.png")
            .build());

        // LEAVES - chance to drop sapling or apple
        register(BlockType.LEAVES, new Builder()
            .breakSound("sounds/grass_break.wav")
            .placeSound("sounds/grass_place.wav")
            .stepSound("sounds/step_grass.wav")
            .drops("Sapling", "resource", "blocks/leaves.png")
            .dropChance(0.05f) // 5% chance
            .build());

        // BRICK - drops itself
        register(BlockType.BRICK, new Builder()
            .breakSound("sounds/stone_break.wav")
            .placeSound("sounds/stone_place.wav")
            .stepSound("sounds/step_stone.wav")
            .drops("Brick", "block", "blocks/brick.png")
            .build());

        // SAND - drops itself
        register(BlockType.SAND, new Builder()
            .breakSound("sounds/sand_break.wav")
            .placeSound("sounds/sand_place.wav")
            .stepSound("sounds/step_sand.wav")
            .drops("Sand", "block", "blocks/sand.png")
            .build());

        // WATER - no drops
        register(BlockType.WATER, new Builder()
            .breakSound("sounds/water_splash.wav")
            .placeSound("sounds/water_splash.wav")
            .noDrop()
            .build());

        // GLASS - no drops (shatters)
        register(BlockType.GLASS, new Builder()
            .breakSound("sounds/glass_break.wav")
            .placeSound("sounds/stone_place.wav")
            .stepSound("sounds/step_stone.wav")
            .noDrop()
            .silkTouchDrops("Glass", "blocks/glass.png")
            .build());

        // COAL_ORE - drops coal
        register(BlockType.COAL_ORE, new Builder()
            .breakSound("sounds/stone_break.wav")
            .placeSound("sounds/stone_place.wav")
            .stepSound("sounds/step_stone.wav")
            .drops("Coal", "resource", "blocks/coal_ore.png")
            .silkTouchDrops("Coal Ore", "blocks/coal_ore.png")
            .build());

        // IRON_ORE - drops itself (needs smelting)
        register(BlockType.IRON_ORE, new Builder()
            .breakSound("sounds/stone_break.wav")
            .placeSound("sounds/stone_place.wav")
            .stepSound("sounds/step_stone.wav")
            .drops("Iron Ore", "resource", "blocks/iron_ore.png")
            .build());

        // GOLD_ORE - drops itself (needs smelting)
        register(BlockType.GOLD_ORE, new Builder()
            .breakSound("sounds/stone_break.wav")
            .placeSound("sounds/stone_place.wav")
            .stepSound("sounds/step_stone.wav")
            .drops("Gold Ore", "resource", "blocks/gold_ore.png")
            .build());

        // PLATFORM - legacy platform, drops itself
        register(BlockType.PLATFORM, new Builder()
            .breakSound("sounds/wood_break.wav")
            .placeSound("sounds/wood_place.wav")
            .stepSound("sounds/step_wood.wav")
            .drops("Platform", "block", "assets/obstacle.png")
            .build());
    }

    private static void register(BlockType type, BlockAttributes attrs) {
        registry.put(type, attrs);
    }

    // --- Getters ---

    public String getBreakSound() {
        return breakSound;
    }

    public String getPlaceSound() {
        return placeSound;
    }

    public String getStepSound() {
        return stepSound;
    }

    public String getDropItemName() {
        return dropItemName;
    }

    public String getDropItemType() {
        return dropItemType;
    }

    public String getDropSpritePath() {
        return dropSpritePath;
    }

    public int getDropCount() {
        return dropCount;
    }

    public float getDropChance() {
        return dropChance;
    }

    public boolean hasItemDrop() {
        return dropItemName != null && dropChance > 0;
    }

    public String getSilkTouchDropName() {
        return silkTouchDropName;
    }

    public String getSilkTouchSpritePath() {
        return silkTouchSpritePath;
    }

    public boolean hasSilkTouchDrop() {
        return silkTouchDropName != null;
    }

    /**
     * Determines if an item should drop based on drop chance.
     */
    public boolean shouldDrop() {
        if (!hasItemDrop()) return false;
        if (dropChance >= 1.0f) return true;
        return Math.random() < dropChance;
    }

    // --- Builder for fluent attribute creation ---

    public static class Builder {
        private BlockAttributes attrs = new BlockAttributes();

        public Builder breakSound(String path) {
            attrs.breakSound = path;
            return this;
        }

        public Builder placeSound(String path) {
            attrs.placeSound = path;
            return this;
        }

        public Builder stepSound(String path) {
            attrs.stepSound = path;
            return this;
        }

        public Builder drops(String itemName, String itemType, String spritePath) {
            attrs.dropItemName = itemName;
            attrs.dropItemType = itemType;
            attrs.dropSpritePath = spritePath;
            return this;
        }

        public Builder dropCount(int count) {
            attrs.dropCount = count;
            return this;
        }

        public Builder dropChance(float chance) {
            attrs.dropChance = Math.max(0, Math.min(1, chance));
            return this;
        }

        public Builder noDrop() {
            attrs.dropItemName = null;
            attrs.dropChance = 0;
            return this;
        }

        public Builder silkTouchDrops(String itemName, String spritePath) {
            attrs.silkTouchDropName = itemName;
            attrs.silkTouchSpritePath = spritePath;
            return this;
        }

        public BlockAttributes build() {
            return attrs;
        }
    }
}
