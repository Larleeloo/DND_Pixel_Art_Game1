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

    // Level dimensions (for camera bounds)
    public int levelWidth;
    public int levelHeight;

    // Ground configuration
    public int groundY;

    // Scrolling/camera settings
    public boolean scrollingEnabled = false;
    public boolean tileBackgroundHorizontal = false;
    public boolean tileBackgroundVertical = false;

    // Lists of entities
    public List<PlatformData> platforms;
    public List<ItemData> items;
    public List<TriggerData> triggers;

    // Next level (for level progression)
    public String nextLevel;

    public LevelData() {
        platforms = new ArrayList<>();
        items = new ArrayList<>();
        triggers = new ArrayList<>();

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
                '}';
    }
}
