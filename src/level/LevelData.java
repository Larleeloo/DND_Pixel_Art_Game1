package level;
import core.*;
import entity.*;
import entity.player.*;
import entity.mob.*;
import block.*;
import graphics.*;
import animation.*;
import animation.bone.*;

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
    public List<DoorData> doors;  // Interactive doors
    public List<ButtonData> buttons;  // Interactive buttons/switches
    public List<VaultData> vaults;  // Interactive vaults/chests
    public List<MovingBlockData> movingBlocks;  // Moving/animated blocks
    public List<CutsceneData> cutscenes;  // GIF-based cutscenes

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
        doors = new ArrayList<>();
        buttons = new ArrayList<>();
        vaults = new ArrayList<>();
        movingBlocks = new ArrayList<>();
        cutscenes = new ArrayList<>();

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
        public String itemId;  // ItemRegistry ID for creating linked items

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

        public boolean hasItemId() {
            return itemId != null && !itemId.isEmpty();
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

        // Optional overlay (e.g., "GRASS", "SNOW", "ICE", "MOSS", "VINES")
        public String overlay = null;

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

        public BlockData(int x, int y, String blockType, boolean useGridCoords, String overlay) {
            this.x = x;
            this.y = y;
            this.blockType = blockType;
            this.useGridCoords = useGridCoords;
            this.overlay = overlay;
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

        public boolean hasOverlay() {
            return overlay != null && !overlay.isEmpty();
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
         * Sets both horizontal and vertical scroll speeds appropriate for each depth.
         */
        public void applyDepthDefaults() {
            if (depthLevel == null) return;

            switch (depthLevel.toLowerCase()) {
                case "background":
                case "sky":
                    zOrder = -2;
                    scrollSpeedX = 0.1;
                    scrollSpeedY = 0.1;
                    opacity = 1.0;
                    break;
                case "middleground_3":
                case "distant":
                    zOrder = -1;
                    scrollSpeedX = 0.3;
                    scrollSpeedY = 0.3;
                    opacity = 0.8;
                    break;
                case "middleground_2":
                case "mid":
                    zOrder = 0;
                    scrollSpeedX = 0.5;
                    scrollSpeedY = 0.5;
                    opacity = 0.9;
                    break;
                case "middleground_1":
                case "near":
                    zOrder = 1;
                    scrollSpeedX = 0.7;
                    scrollSpeedY = 0.7;
                    opacity = 1.0;
                    break;
                case "foreground":
                    zOrder = 2;
                    scrollSpeedX = 1.2;
                    scrollSpeedY = 1.2;
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
        public String mobType;           // "quadruped", "humanoid", or "sprite_quadruped"
        public String subType;           // Animal type (wolf, horse, etc.) or variant (zombie, skeleton, etc.)
        public String behavior = "hostile"; // "passive", "neutral", "hostile"
        public String textureDir;        // Optional texture directory (bone-based mobs)
        public String spriteDir;         // Sprite directory for GIF-based mobs
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
     * Data class for interactive door entities.
     * Doors can be linked to buttons and trigger actions when used.
     */
    public static class DoorData {
        public int x;
        public int y;
        public int width = 64;
        public int height = 128;
        public String texturePath = "assets/doors/wooden_door.gif";
        public String linkId = "";              // ID for button linking
        public boolean startsOpen = false;      // Initial state
        public boolean locked = false;          // Whether door requires a key
        public String keyItemId = "";           // Item ID of required key
        public String actionType = "none";      // Action on use: none, level_transition, event, teleport
        public String actionTarget = "";        // Target for action (level path, event name, etc.)
        public float animationSpeed = 0.05f;    // Animation speed (0.0-1.0)

        public DoorData() {}

        public DoorData(int x, int y, String linkId) {
            this.x = x;
            this.y = y;
            this.linkId = linkId;
        }

        public DoorData(int x, int y, int width, int height, String texturePath, String linkId) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.texturePath = texturePath;
            this.linkId = linkId;
        }

        public boolean hasKey() {
            return keyItemId != null && !keyItemId.isEmpty();
        }

        public boolean hasAction() {
            return actionType != null && !actionType.equals("none") && !actionType.isEmpty();
        }
    }

    /**
     * Data class for interactive button/switch entities.
     * Buttons can control doors and trigger actions.
     */
    public static class ButtonData {
        public int x;
        public int y;
        public int width = 32;
        public int height = 16;
        public String texturePath = "assets/buttons/stone_button.gif";
        public String linkId = "";                          // This button's ID
        public String[] linkedDoorIds = new String[0];      // IDs of doors to control
        public String buttonType = "toggle";                // toggle, momentary, one_shot, timed
        public boolean activatedByPlayer = true;            // Can player activate
        public boolean activatedByMobs = true;              // Can mobs activate
        public boolean requiresInteraction = true;          // If false, acts as pressure plate
        public int timedDuration = 3000;                    // Duration for timed buttons (ms)
        public String actionType = "none";                  // Action: none, level_transition, event, spawn_entity
        public String actionTarget = "";                    // Target for action
        public float animationSpeed = 0.1f;                 // Animation speed

        public ButtonData() {}

        public ButtonData(int x, int y, String linkId) {
            this.x = x;
            this.y = y;
            this.linkId = linkId;
        }

        public ButtonData(int x, int y, int width, int height, String texturePath, String linkId) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.texturePath = texturePath;
            this.linkId = linkId;
        }

        public boolean hasLinkedDoors() {
            return linkedDoorIds != null && linkedDoorIds.length > 0;
        }

        public boolean hasAction() {
            return actionType != null && !actionType.equals("none") && !actionType.isEmpty();
        }
    }

    /**
     * Data class for interactive vault/chest entities.
     * Vaults provide access to persistent player storage.
     */
    public static class VaultData {
        public int x;
        public int y;
        public int width = 64;
        public int height = 64;
        public String texturePath = "assets/vault/player_vault.gif";
        public String linkId = "";              // ID for identification
        public String vaultType = "PLAYER_VAULT";  // PLAYER_VAULT or STORAGE_CHEST

        public VaultData() {}

        public VaultData(int x, int y, String linkId) {
            this.x = x;
            this.y = y;
            this.linkId = linkId;
        }

        public VaultData(int x, int y, String vaultType, String texturePath) {
            this.x = x;
            this.y = y;
            this.vaultType = vaultType;
            this.texturePath = texturePath;
        }

        public VaultData(int x, int y, int width, int height, String texturePath, String linkId, String vaultType) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.texturePath = texturePath;
            this.linkId = linkId;
            this.vaultType = vaultType;
        }

        public boolean isPlayerVault() {
            return "PLAYER_VAULT".equals(vaultType);
        }
    }

    /**
     * Data class for moving block entities.
     * Moving blocks follow patterns like horizontal, vertical, circular, or custom paths.
     */
    public static class MovingBlockData {
        public int x;
        public int y;
        public String blockType;          // BlockType enum name (e.g., "GRASS", "STONE")
        public boolean useGridCoords;     // If true, x/y are grid positions, not pixels

        // Movement configuration
        public String movementPattern;    // "HORIZONTAL", "VERTICAL", "CIRCULAR", "PATH"
        public int endX;                  // End position X (or center X for circular)
        public int endY;                  // End position Y (or center Y for circular)
        public double speed = 2.0;        // Movement speed in pixels per frame
        public int pauseTime = 30;        // Frames to pause at endpoints

        // Circular movement specific
        public double radius = 100;       // Radius for circular movement

        // Path movement specific (comma-separated x,y pairs)
        public String waypoints = "";     // e.g., "100,200;150,250;200,200"

        // Optional tint
        public int tintRed = -1;
        public int tintGreen = -1;
        public int tintBlue = -1;

        // Optional overlay (e.g., "GRASS", "SNOW", "ICE", "MOSS", "VINES")
        public String overlay = null;

        public MovingBlockData() {
            blockType = "STONE";
            useGridCoords = true;
            movementPattern = "HORIZONTAL";
        }

        public MovingBlockData(int x, int y, String blockType, boolean useGridCoords,
                              String movementPattern, int endX, int endY, double speed) {
            this.x = x;
            this.y = y;
            this.blockType = blockType;
            this.useGridCoords = useGridCoords;
            this.movementPattern = movementPattern;
            this.endX = endX;
            this.endY = endY;
            this.speed = speed;
        }

        public boolean hasTint() {
            return tintRed >= 0 && tintGreen >= 0 && tintBlue >= 0;
        }

        public boolean hasOverlay() {
            return overlay != null && !overlay.isEmpty();
        }

        public boolean hasWaypoints() {
            return waypoints != null && !waypoints.isEmpty();
        }

        /**
         * Parse waypoints string into array of points.
         * Format: "x1,y1;x2,y2;x3,y3"
         */
        public int[][] parseWaypoints() {
            if (!hasWaypoints()) return new int[0][0];

            String[] pairs = waypoints.split(";");
            int[][] result = new int[pairs.length][2];

            for (int i = 0; i < pairs.length; i++) {
                String[] coords = pairs[i].trim().split(",");
                if (coords.length >= 2) {
                    result[i][0] = Integer.parseInt(coords[0].trim());
                    result[i][1] = Integer.parseInt(coords[1].trim());
                }
            }

            return result;
        }
    }

    /**
     * Data class for GIF-based cutscene configuration.
     * Cutscenes are triggered by TriggerEntity with type "cutscene" or on level start.
     * Supports multi-frame cutscenes with click-through prompts.
     */
    public static class CutsceneData {
        public String id;                    // Unique identifier for this cutscene
        public List<CutsceneFrameData> frames = new ArrayList<>(); // Frames in the cutscene
        public boolean playOnLevelStart = false;  // If true, plays when level loads
        public boolean playOnce = true;           // If true, only plays once per session

        public CutsceneData() {}

        public CutsceneData(String id) {
            this.id = id;
        }

        public CutsceneData(String id, boolean playOnLevelStart) {
            this.id = id;
            this.playOnLevelStart = playOnLevelStart;
        }

        public boolean hasFrames() {
            return frames != null && !frames.isEmpty();
        }
    }

    /**
     * Data class for a single frame within a cutscene.
     * Each frame displays a GIF image and optional text prompt.
     */
    public static class CutsceneFrameData {
        public String gifPath;   // Path to the GIF file (1920x1080 recommended)
        public String text;      // Optional text to display as a prompt

        public CutsceneFrameData() {}

        public CutsceneFrameData(String gifPath) {
            this.gifPath = gifPath;
        }

        public CutsceneFrameData(String gifPath, String text) {
            this.gifPath = gifPath;
            this.text = text;
        }

        public boolean hasText() {
            return text != null && !text.isEmpty();
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

        /**
         * Add a door at the specified position.
         * @param x X position
         * @param y Y position
         * @param linkId ID for button linking
         */
        public Builder addDoor(int x, int y, String linkId) {
            DoorData door = new DoorData(x, y, linkId);
            data.doors.add(door);
            return this;
        }

        /**
         * Add a door with custom dimensions and texture.
         */
        public Builder addDoor(int x, int y, int width, int height, String texturePath, String linkId) {
            DoorData door = new DoorData(x, y, width, height, texturePath, linkId);
            data.doors.add(door);
            return this;
        }

        /**
         * Add a locked door that requires a key.
         */
        public Builder addLockedDoor(int x, int y, String linkId, String keyItemId) {
            DoorData door = new DoorData(x, y, linkId);
            door.locked = true;
            door.keyItemId = keyItemId;
            data.doors.add(door);
            return this;
        }

        /**
         * Add a button at the specified position.
         * @param x X position
         * @param y Y position
         * @param linkId This button's ID
         * @param linkedDoorIds IDs of doors to control
         */
        public Builder addButton(int x, int y, String linkId, String... linkedDoorIds) {
            ButtonData button = new ButtonData(x, y, linkId);
            button.linkedDoorIds = linkedDoorIds;
            data.buttons.add(button);
            return this;
        }

        /**
         * Add a button with custom dimensions and texture.
         */
        public Builder addButton(int x, int y, int width, int height, String texturePath,
                                String linkId, String... linkedDoorIds) {
            ButtonData button = new ButtonData(x, y, width, height, texturePath, linkId);
            button.linkedDoorIds = linkedDoorIds;
            data.buttons.add(button);
            return this;
        }

        /**
         * Add a pressure plate (momentary button that activates on contact).
         */
        public Builder addPressurePlate(int x, int y, String linkId, String... linkedDoorIds) {
            ButtonData button = new ButtonData(x, y, linkId);
            button.linkedDoorIds = linkedDoorIds;
            button.buttonType = "momentary";
            button.requiresInteraction = false;
            data.buttons.add(button);
            return this;
        }

        /**
         * Add a moving block with horizontal/vertical movement.
         * @param x Starting X position
         * @param y Starting Y position
         * @param blockType Type of block (e.g., "STONE", "WOOD")
         * @param useGridCoords If true, coordinates are in grid units
         * @param endX End X position
         * @param endY End Y position
         * @param speed Movement speed in pixels per frame
         */
        public Builder addMovingBlock(int x, int y, String blockType, boolean useGridCoords,
                                     int endX, int endY, double speed) {
            MovingBlockData mb = new MovingBlockData(x, y, blockType, useGridCoords,
                    "HORIZONTAL", endX, endY, speed);
            // Auto-detect pattern based on movement direction
            if (Math.abs(endY - y) > Math.abs(endX - x)) {
                mb.movementPattern = "VERTICAL";
            }
            data.movingBlocks.add(mb);
            return this;
        }

        /**
         * Add a circular moving block.
         * @param centerX Center X position
         * @param centerY Center Y position
         * @param blockType Type of block
         * @param useGridCoords If true, coordinates are in grid units
         * @param radius Radius of circular path
         * @param speed Movement speed
         */
        public Builder addCircularMovingBlock(int centerX, int centerY, String blockType,
                                             boolean useGridCoords, double radius, double speed) {
            MovingBlockData mb = new MovingBlockData();
            mb.x = centerX;
            mb.y = centerY;
            mb.blockType = blockType;
            mb.useGridCoords = useGridCoords;
            mb.movementPattern = "CIRCULAR";
            mb.radius = radius;
            mb.speed = speed;
            data.movingBlocks.add(mb);
            return this;
        }

        /**
         * Add a cutscene that plays on level start.
         * @param id Unique ID for the cutscene
         * @param gifPaths Array of GIF file paths for each frame
         */
        public Builder addLevelStartCutscene(String id, String... gifPaths) {
            CutsceneData cutscene = new CutsceneData(id, true);
            for (String path : gifPaths) {
                cutscene.frames.add(new CutsceneFrameData(path));
            }
            data.cutscenes.add(cutscene);
            return this;
        }

        /**
         * Add a cutscene that can be triggered by ID.
         * @param id Unique ID for the cutscene (used in trigger target)
         * @param gifPaths Array of GIF file paths for each frame
         */
        public Builder addCutscene(String id, String... gifPaths) {
            CutsceneData cutscene = new CutsceneData(id);
            for (String path : gifPaths) {
                cutscene.frames.add(new CutsceneFrameData(path));
            }
            data.cutscenes.add(cutscene);
            return this;
        }

        /**
         * Add a cutscene with text prompts for each frame.
         * @param id Unique ID for the cutscene
         * @param playOnStart If true, plays when level loads
         * @param frames Array of CutsceneFrameData
         */
        public Builder addCutsceneWithText(String id, boolean playOnStart, CutsceneFrameData... frames) {
            CutsceneData cutscene = new CutsceneData(id, playOnStart);
            for (CutsceneFrameData frame : frames) {
                cutscene.frames.add(frame);
            }
            data.cutscenes.add(cutscene);
            return this;
        }

        /**
         * Add a trigger that starts a cutscene when entered.
         * @param x X position
         * @param y Y position
         * @param width Trigger width
         * @param height Trigger height
         * @param cutsceneId ID of the cutscene to play
         */
        public Builder addCutsceneTrigger(int x, int y, int width, int height, String cutsceneId) {
            data.triggers.add(new TriggerData(x, y, width, height, "cutscene", cutsceneId));
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
                ", doors=" + doors.size() +
                ", buttons=" + buttons.size() +
                ", vaults=" + vaults.size() +
                ", movingBlocks=" + movingBlocks.size() +
                ", cutscenes=" + cutscenes.size() +
                '}';
    }
}
