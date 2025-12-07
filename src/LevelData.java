import java.util.ArrayList;
import java.util.List;

/**
 * Data class that holds the configuration for a game level.
 * Can be loaded from JSON files via LevelLoader.
 */
class LevelData {

    // Level metadata
    public String name;
    public String description;
    public String backgroundPath;
    public String musicPath;

    // Player spawn point
    public int playerSpawnX;
    public int playerSpawnY;
    public String playerSpritePath;

    // Bone animation settings
    public boolean useBoneAnimation = false;       // If true, use PlayerBoneEntity instead of PlayerEntity
    public String boneTextureDir = "assets/bones"; // Directory containing bone textures

    // Level dimensions (for camera bounds)
    public int levelWidth;
    public int levelHeight;

    // Ground configuration
    public int groundY;

    // Scrolling/camera settings
    public boolean scrollingEnabled = false;
    public boolean tileBackgroundHorizontal = false;
    public boolean tileBackgroundVertical = false;

    // Vertical scrolling settings
    public boolean verticalScrollEnabled = false;
    public int verticalMargin = 0; // Height of black bars at top and bottom (pixels)

    // Lists of entities
    public List<PlatformData> platforms;
    public List<ItemData> items;
    public List<TriggerData> triggers;
    public List<BlockData> blocks;

    // Next level (for level progression)
    public String nextLevel;

    public LevelData() {
        platforms = new ArrayList<>();
        items = new ArrayList<>();
        triggers = new ArrayList<>();
        blocks = new ArrayList<>();

        // Defaults
        name = "Untitled Level";
        description = "";
        backgroundPath = "assets/background.png";
        musicPath = "assets/music.wav";
        playerSpawnX = 100;
        playerSpawnY = 620;
        playerSpritePath = "assets/player.png";
        levelWidth = GamePanel.SCREEN_WIDTH;
        levelHeight = GamePanel.SCREEN_HEIGHT;
        groundY = GamePanel.GROUND_Y;
    }

    /**
     * Data class for platform/obstacle entities.
     */
    public static class PlatformData {
        public int x;
        public int y;
        public String spritePath;
        public boolean solid;

        // Optional color mask (RGB 0-255 each, -1 means no mask)
        public int maskRed = -1;
        public int maskGreen = -1;
        public int maskBlue = -1;

        public PlatformData() {
            spritePath = "assets/obstacle.png";
            solid = true;
        }

        public PlatformData(int x, int y) {
            this();
            this.x = x;
            this.y = y;
        }

        public PlatformData(int x, int y, String spritePath, boolean solid) {
            this.x = x;
            this.y = y;
            this.spritePath = spritePath;
            this.solid = solid;
        }

        public PlatformData(int x, int y, String spritePath, boolean solid, int maskRed, int maskGreen, int maskBlue) {
            this.x = x;
            this.y = y;
            this.spritePath = spritePath;
            this.solid = solid;
            this.maskRed = maskRed;
            this.maskGreen = maskGreen;
            this.maskBlue = maskBlue;
        }

        public boolean hasColorMask() {
            return maskRed >= 0 && maskGreen >= 0 && maskBlue >= 0;
        }
    }

    /**
     * Data class for collectible item entities.
     */
    public static class ItemData {
        public int x;
        public int y;
        public String spritePath;
        public String itemName;
        public String itemType;

        public ItemData() {
            spritePath = "assets/obstacle.png";
            itemType = "collectible";
        }

        public ItemData(int x, int y, String itemName, String itemType) {
            this();
            this.x = x;
            this.y = y;
            this.itemName = itemName;
            this.itemType = itemType;
        }

        public ItemData(int x, int y, String spritePath, String itemName, String itemType) {
            this.x = x;
            this.y = y;
            this.spritePath = spritePath;
            this.itemName = itemName;
            this.itemType = itemType;
        }
    }

    /**
     * Data class for trigger zones (level transitions, events, etc.)
     */
    public static class TriggerData {
        public int x;
        public int y;
        public int width;
        public int height;
        public String type;
        public String target; // e.g., next level path or event name

        public TriggerData() {
            width = 64;
            height = 64;
            type = "level_transition";
        }

        public TriggerData(int x, int y, int width, int height, String type, String target) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
            this.target = target;
        }
    }

    /**
     * Data class for block entities (Minecraft-style blocks).
     * Blocks are always square and can be placed using either pixel or grid coordinates.
     */
    public static class BlockData {
        public int x;
        public int y;
        public String blockType;      // BlockType enum name (e.g., "GRASS", "STONE")
        public boolean useGridCoords; // If true, x/y are grid positions, not pixels

        // Optional color tint (RGB 0-255 each, -1 means no tint)
        public int tintRed = -1;
        public int tintGreen = -1;
        public int tintBlue = -1;

        public BlockData() {
            blockType = "DIRT";
            useGridCoords = false;
        }

        public BlockData(int x, int y, String blockType) {
            this.x = x;
            this.y = y;
            this.blockType = blockType;
            this.useGridCoords = false;
        }

        public BlockData(int x, int y, String blockType, boolean useGridCoords) {
            this.x = x;
            this.y = y;
            this.blockType = blockType;
            this.useGridCoords = useGridCoords;
        }

        public BlockData(int x, int y, String blockType, boolean useGridCoords,
                         int tintRed, int tintGreen, int tintBlue) {
            this.x = x;
            this.y = y;
            this.blockType = blockType;
            this.useGridCoords = useGridCoords;
            this.tintRed = tintRed;
            this.tintGreen = tintGreen;
            this.tintBlue = tintBlue;
        }

        public boolean hasTint() {
            return tintRed >= 0 && tintGreen >= 0 && tintBlue >= 0;
        }
    }

    /**
     * Create a builder for easier level creation.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for convenient level creation in code.
     */
    public static class Builder {
        private LevelData data;

        public Builder() {
            data = new LevelData();
        }

        public Builder name(String name) {
            data.name = name;
            return this;
        }

        public Builder description(String description) {
            data.description = description;
            return this;
        }

        public Builder background(String path) {
            data.backgroundPath = path;
            return this;
        }

        public Builder music(String path) {
            data.musicPath = path;
            return this;
        }

        public Builder playerSpawn(int x, int y) {
            data.playerSpawnX = x;
            data.playerSpawnY = y;
            return this;
        }

        public Builder playerSprite(String path) {
            data.playerSpritePath = path;
            return this;
        }

        /**
         * Enable bone-based skeletal animation for the player.
         * @param enabled True to use bone animation
         * @return This builder
         */
        public Builder useBoneAnimation(boolean enabled) {
            data.useBoneAnimation = enabled;
            return this;
        }

        /**
         * Set the directory containing bone texture PNGs.
         * @param dir Directory path (e.g., "assets/bones")
         * @return This builder
         */
        public Builder boneTextureDir(String dir) {
            data.boneTextureDir = dir;
            return this;
        }

        public Builder dimensions(int width, int height) {
            data.levelWidth = width;
            data.levelHeight = height;
            return this;
        }

        public Builder groundY(int y) {
            data.groundY = y;
            return this;
        }

        public Builder scrollingEnabled(boolean enabled) {
            data.scrollingEnabled = enabled;
            return this;
        }

        public Builder tileBackground(boolean horizontal, boolean vertical) {
            data.tileBackgroundHorizontal = horizontal;
            data.tileBackgroundVertical = vertical;
            return this;
        }

        public Builder verticalScrollEnabled(boolean enabled) {
            data.verticalScrollEnabled = enabled;
            return this;
        }

        public Builder verticalMargin(int margin) {
            data.verticalMargin = margin;
            return this;
        }

        public Builder addPlatform(int x, int y) {
            data.platforms.add(new PlatformData(x, y));
            return this;
        }

        public Builder addPlatform(int x, int y, String sprite, boolean solid) {
            data.platforms.add(new PlatformData(x, y, sprite, solid));
            return this;
        }

        public Builder addPlatform(int x, int y, String sprite, boolean solid, int maskRed, int maskGreen, int maskBlue) {
            data.platforms.add(new PlatformData(x, y, sprite, solid, maskRed, maskGreen, maskBlue));
            return this;
        }

        public Builder addItem(int x, int y, String name, String type) {
            data.items.add(new ItemData(x, y, name, type));
            return this;
        }

        public Builder addItem(int x, int y, String sprite, String name, String type) {
            data.items.add(new ItemData(x, y, sprite, name, type));
            return this;
        }

        public Builder addTrigger(int x, int y, int width, int height, String type, String target) {
            data.triggers.add(new TriggerData(x, y, width, height, type, target));
            return this;
        }

        public Builder nextLevel(String path) {
            data.nextLevel = path;
            return this;
        }

        /**
         * Add a block at pixel coordinates.
         */
        public Builder addBlock(int x, int y, String blockType) {
            data.blocks.add(new BlockData(x, y, blockType));
            return this;
        }

        /**
         * Add a block with grid/pixel coordinate mode.
         */
        public Builder addBlock(int x, int y, String blockType, boolean useGridCoords) {
            data.blocks.add(new BlockData(x, y, blockType, useGridCoords));
            return this;
        }

        /**
         * Add a tinted block.
         */
        public Builder addBlock(int x, int y, String blockType, boolean useGridCoords,
                               int tintRed, int tintGreen, int tintBlue) {
            data.blocks.add(new BlockData(x, y, blockType, useGridCoords, tintRed, tintGreen, tintBlue));
            return this;
        }

        /**
         * Add a row of blocks (useful for floors).
         * @param startX Starting X coordinate
         * @param y Y coordinate
         * @param count Number of blocks
         * @param blockType Type of block
         * @param useGridCoords If true, coordinates are in grid units
         */
        public Builder addBlockRow(int startX, int y, int count, String blockType, boolean useGridCoords) {
            for (int i = 0; i < count; i++) {
                data.blocks.add(new BlockData(startX + i, y, blockType, useGridCoords));
            }
            return this;
        }

        /**
         * Add a column of blocks (useful for walls).
         */
        public Builder addBlockColumn(int x, int startY, int count, String blockType, boolean useGridCoords) {
            for (int i = 0; i < count; i++) {
                data.blocks.add(new BlockData(x, startY + i, blockType, useGridCoords));
            }
            return this;
        }

        /**
         * Add a filled rectangle of blocks.
         */
        public Builder addBlockRect(int startX, int startY, int width, int height,
                                   String blockType, boolean useGridCoords) {
            for (int dy = 0; dy < height; dy++) {
                for (int dx = 0; dx < width; dx++) {
                    data.blocks.add(new BlockData(startX + dx, startY + dy, blockType, useGridCoords));
                }
            }
            return this;
        }

        public LevelData build() {
            return data;
        }
    }

    @Override
    public String toString() {
        return "LevelData{" +
                "name='" + name + '\'' +
                ", platforms=" + platforms.size() +
                ", items=" + items.size() +
                ", triggers=" + triggers.size() +
                ", blocks=" + blocks.size() +
                '}';
    }
}
