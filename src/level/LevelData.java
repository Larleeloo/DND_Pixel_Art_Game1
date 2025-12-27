package level;
import core.*;
import entity.*;
import entity.player.*;
import entity.mob.*;
import block.*;
import graphics.*;
import animation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class that holds the configuration for a game level.
 * Can be loaded from JSON files via LevelLoader.
 */
public class LevelData {

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
    public String boneTextureDir = "assets/textures/humanoid/player"; // Directory containing bone textures

    // Sprite-based GIF animation settings (alternative to bone animation)
    public boolean useSpriteAnimation = false;     // If true, use SpritePlayerEntity with GIF animations
    public String spriteAnimationDir = "assets/player/sprites"; // Directory containing idle.gif, walk.gif, jump.gif

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

    // Lighting settings
    public boolean nightMode = false;           // If true, level starts in night/darkness mode
    public double nightDarkness = 0.80;         // How dark the night is (0.0 - 1.0)
    public double ambientLight = 0.12;          // Minimum ambient light level (0.0 - 1.0)
    public boolean playerLightEnabled = false;  // Whether player starts with a light
    public double playerLightRadius = 100;      // Player light inner radius
    public double playerLightFalloff = 200;     // Player light falloff radius

    // Lists of entities
    public List<PlatformData> platforms;
    public List<ItemData> items;
    public List<TriggerData> triggers;
    public List<BlockData> blocks;
    public List<LightSourceData> lightSources;  // Static light sources in the level
    public List<ParallaxLayerData> parallaxLayers; // Parallax background layers
    public List<MobData> mobs;  // AI-controlled mobs (creatures/enemies)

    // Parallax settings
    public boolean parallaxEnabled = false;  // If true, use parallax background system

    // Next level (for level progression)
    public String nextLevel;

    public LevelData() {
        platforms = new ArrayList<>();
        items = new ArrayList<>();
        triggers = new ArrayList<>();
        blocks = new ArrayList<>();
        lightSources = new ArrayList<>();
        parallaxLayers = new ArrayList<>();
        mobs = new ArrayList<>();

        // Defaults
        name = "Untitled Level";
        description = "";
        backgroundPath = "assets/background.png";
        musicPath = "sounds/music.wav";
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
     * Data class for light source entities in a level.
     */
    public static class LightSourceData {
        public int x;
        public int y;
        public double radius = 80;           // Inner radius of full brightness
        public double falloffRadius = 180;   // Outer radius where light fades to zero
        public int colorRed = 255;           // Light color RGB
        public int colorGreen = 200;
        public int colorBlue = 100;
        public double intensity = 1.0;       // Light intensity (0.0-1.0)
        public String lightType = "torch";   // Type: torch, campfire, lantern, magic
        public boolean flicker = false;      // Whether light flickers
        public double flickerAmount = 0.15;  // How much intensity varies when flickering
        public double flickerSpeed = 8.0;    // Speed of flicker effect

        public LightSourceData() {}

        public LightSourceData(int x, int y, String lightType) {
            this.x = x;
            this.y = y;
            this.lightType = lightType;
            applyTypeDefaults();
        }

        /**
         * Apply default values based on light type.
         */
        public void applyTypeDefaults() {
            switch (lightType.toLowerCase()) {
                case "torch":
                    radius = 80;
                    falloffRadius = 180;
                    colorRed = 255; colorGreen = 200; colorBlue = 100;
                    flicker = true;
                    flickerAmount = 0.15;
                    flickerSpeed = 8.0;
                    break;
                case "campfire":
                    radius = 120;
                    falloffRadius = 280;
                    colorRed = 255; colorGreen = 150; colorBlue = 50;
                    flicker = true;
                    flickerAmount = 0.25;
                    flickerSpeed = 6.0;
                    break;
                case "lantern":
                    radius = 100;
                    falloffRadius = 200;
                    colorRed = 255; colorGreen = 240; colorBlue = 180;
                    flicker = false;
                    break;
                case "magic":
                    radius = 90;
                    falloffRadius = 180;
                    colorRed = 150; colorGreen = 150; colorBlue = 255;
                    flicker = true;
                    flickerAmount = 0.1;
                    flickerSpeed = 3.0;
                    break;
                case "crystal":
                    radius = 60;
                    falloffRadius = 120;
                    colorRed = 100; colorGreen = 255; colorBlue = 200;
                    flicker = true;
                    flickerAmount = 0.08;
                    flickerSpeed = 2.0;
                    break;
            }
        }
    }

    /**
     * Data class for parallax background layers.
     * Layers are sorted by zOrder (lower = further back).
     */
    public static class ParallaxLayerData {
        public String name;              // Layer identifier
        public String imagePath;         // Path to layer image
        public double scrollSpeedX = 0.5; // Horizontal scroll speed (0.0 = static, 1.0 = world speed)
        public double scrollSpeedY = 0.0; // Vertical scroll speed
        public int zOrder = 0;           // Z-order for depth sorting (lower = further back)
        public double scale = 10.0;      // Image scale factor
        public double opacity = 1.0;     // Opacity (0.0 - 1.0)
        public boolean tileHorizontal = true;  // Whether to tile horizontally
        public boolean tileVertical = false;   // Whether to tile vertically
        public int offsetX = 0;          // Base X offset
        public int offsetY = 0;          // Base Y offset
        public boolean anchorBottom = false; // If true, offsetY is from bottom of viewport

        // Depth level presets (for convenience)
        public String depthLevel = null; // "background", "middleground_3", "middleground_2", "middleground_1", "foreground"

        public ParallaxLayerData() {}

        public ParallaxLayerData(String name, String imagePath, double scrollSpeedX, int zOrder) {
            this.name = name;
            this.imagePath = imagePath;
            this.scrollSpeedX = scrollSpeedX;
            this.zOrder = zOrder;
        }

        /**
         * Apply default values based on depth level preset.
         */
        public void applyDepthDefaults() {
            if (depthLevel == null) return;

            switch (depthLevel.toLowerCase()) {
                case "background":
                case "sky":
                    zOrder = -2;
                    scrollSpeedX = 0.1;
                    opacity = 1.0;
                    break;
                case "middleground_3":
                case "distant":
                    zOrder = -1;
                    scrollSpeedX = 0.3;
                    opacity = 0.8;
                    break;
                case "middleground_2":
                case "mid":
                    zOrder = 0;
                    scrollSpeedX = 0.5;
                    opacity = 0.9;
                    break;
                case "middleground_1":
                case "near":
                    zOrder = 1;
                    scrollSpeedX = 0.7;
                    opacity = 1.0;
                    break;
                case "foreground":
                    zOrder = 2;
                    scrollSpeedX = 1.2;
                    opacity = 0.7;
                    break;
            }
        }
    }

    /**
     * Data class for mob entities (AI-controlled creatures/enemies).
     * Supports both quadruped (4-legged) and humanoid mob types.
     */
    public static class MobData {
        public int x;
        public int y;
        public String mobType;           // "quadruped" or "humanoid"
        public String subType;           // Animal type (wolf, horse, etc.) or variant (zombie, skeleton, etc.)
        public String behavior = "hostile"; // "passive", "neutral", "hostile"
        public String textureDir;        // Optional texture directory
        public double wanderMinX = -1;   // Wander bounds (-1 = use default)
        public double wanderMaxX = -1;
        public boolean debugDraw = false;

        public MobData() {}

        public MobData(int x, int y, String mobType, String subType) {
            this.x = x;
            this.y = y;
            this.mobType = mobType;
            this.subType = subType;
        }

        public MobData(int x, int y, String mobType, String subType, String behavior) {
            this(x, y, mobType, subType);
            this.behavior = behavior;
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
         * @param dir Directory path (e.g., "assets/textures/humanoid/player")
         * @return This builder
         */
        public Builder boneTextureDir(String dir) {
            data.boneTextureDir = dir;
            return this;
        }

        /**
         * Enable sprite/GIF-based animation for the player.
         * This provides layered GIF animations with equipment overlay support.
         * @param enabled True to use sprite animation
         * @return This builder
         */
        public Builder useSpriteAnimation(boolean enabled) {
            data.useSpriteAnimation = enabled;
            return this;
        }

        /**
         * Set the directory containing sprite animation GIFs.
         * Expected files: idle.gif, walk.gif, jump.gif
         * @param dir Directory path (e.g., "assets/player/sprites")
         * @return This builder
         */
        public Builder spriteAnimationDir(String dir) {
            data.spriteAnimationDir = dir;
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

        /**
         * Enable the parallax background system.
         */
        public Builder parallaxEnabled(boolean enabled) {
            data.parallaxEnabled = enabled;
            return this;
        }

        /**
         * Add a parallax layer with full configuration.
         */
        public Builder addParallaxLayer(String name, String imagePath, double scrollSpeedX,
                                       int zOrder, double scale, double opacity) {
            ParallaxLayerData layer = new ParallaxLayerData();
            layer.name = name;
            layer.imagePath = imagePath;
            layer.scrollSpeedX = scrollSpeedX;
            layer.zOrder = zOrder;
            layer.scale = scale;
            layer.opacity = opacity;
            data.parallaxLayers.add(layer);
            return this;
        }

        /**
         * Add a parallax layer using a depth preset.
         * Presets: "background", "middleground_3", "middleground_2", "middleground_1", "foreground"
         */
        public Builder addParallaxLayer(String name, String imagePath, String depthLevel) {
            ParallaxLayerData layer = new ParallaxLayerData();
            layer.name = name;
            layer.imagePath = imagePath;
            layer.depthLevel = depthLevel;
            layer.applyDepthDefaults();
            data.parallaxLayers.add(layer);
            return this;
        }

        /**
         * Add a simple parallax layer with default settings.
         */
        public Builder addParallaxLayer(String name, String imagePath, double scrollSpeedX, int zOrder) {
            ParallaxLayerData layer = new ParallaxLayerData(name, imagePath, scrollSpeedX, zOrder);
            data.parallaxLayers.add(layer);
            return this;
        }

        /**
         * Add a quadruped (4-legged animal) mob.
         * @param x X position
         * @param y Y position
         * @param animalType Animal type: wolf, dog, cat, horse, pig, cow, sheep, deer, bear, fox
         * @param behavior Behavior type: passive, neutral, hostile
         */
        public Builder addQuadrupedMob(int x, int y, String animalType, String behavior) {
            MobData mob = new MobData(x, y, "quadruped", animalType, behavior);
            data.mobs.add(mob);
            return this;
        }

        /**
         * Add a humanoid mob.
         * @param x X position
         * @param y Y position
         * @param variantType Variant type: zombie, skeleton, goblin, orc, bandit, knight, mage
         */
        public Builder addHumanoidMob(int x, int y, String variantType) {
            MobData mob = new MobData(x, y, "humanoid", variantType, "hostile");
            data.mobs.add(mob);
            return this;
        }

        /**
         * Add a mob with full configuration.
         */
        public Builder addMob(int x, int y, String mobType, String subType, String behavior) {
            MobData mob = new MobData(x, y, mobType, subType, behavior);
            data.mobs.add(mob);
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
                ", mobs=" + mobs.size() +
                '}';
    }
}
