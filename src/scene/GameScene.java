package scene;
import core.*;
import entity.*;
import entity.player.*;
import entity.mob.*;
import entity.mob.old.*;  // Deprecated bone-based mobs (HumanoidMobEntity, QuadrupedMobEntity)
import block.*;
import animation.*;
import animation.bone.*;
import graphics.*;
import level.*;
import audio.*;
import input.*;
import ui.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Scene implementation for actual gameplay.
 * Loads levels from JSON files and manages gameplay logic.
 */
public class GameScene implements Scene {

    private String levelPath;
    private LevelData levelData;
    private EntityManager entityManager;
    private ArrayList<UIButton> buttons;
    private ArrayList<TriggerEntity> triggers;
    private ArrayList<DoorEntity> doors;
    private ArrayList<ButtonEntity> interactiveButtons;
    private Map<String, DoorEntity> doorsByLinkId;
    private Map<String, ButtonEntity> buttonsByLinkId;
    private PlayerBase player;
    private boolean initialized;

    // Camera for scrolling levels
    private Camera camera;
    private BackgroundEntity background;

    // Parallax background system
    private ParallaxBackground parallaxBackground;

    // Lighting system
    private LightingSystem lightingSystem;
    private LightSource playerLight;
    private boolean playerHasLantern = false;

    // Timing for lighting updates
    private long lastUpdateTime;

    // Debug mode
    private boolean debugMode = false;
    private long frameCount = 0;
    private long lastFpsTime = System.nanoTime();
    private int currentFps = 0;

    public GameScene(String levelPath) {
        this.levelPath = levelPath;
        this.initialized = false;
    }

    /**
     * Create a GameScene from existing LevelData (for programmatic level creation).
     */
    public GameScene(LevelData levelData) {
        this.levelData = levelData;
        this.levelPath = null;
        this.initialized = false;
    }

    /**
     * Default constructor - creates an empty GameScene.
     * Use setLevelData() to set the level data before calling init().
     */
    public GameScene() {
        this.levelPath = null;
        this.levelData = null;
        this.initialized = false;
    }

    /**
     * Set the level data for this scene.
     * Must be called before init() if using the default constructor.
     * @param levelData The level data to use
     */
    public void setLevelData(LevelData levelData) {
        this.levelData = levelData;
        this.levelPath = null;
        // Reset initialized flag so init() will reinitialize with new data
        if (this.initialized) {
            this.initialized = false;
        }
    }

    @Override
    public void init() {
        if (initialized) return;

        System.out.println("GameScene: Initializing...");

        try {
            entityManager = new EntityManager();
            buttons = new ArrayList<>();
            triggers = new ArrayList<>();
            doors = new ArrayList<>();
            interactiveButtons = new ArrayList<>();
            doorsByLinkId = new HashMap<>();
            buttonsByLinkId = new HashMap<>();

            // Load level data if we have a path
            if (levelPath != null && levelData == null) {
                System.out.println("GameScene: Loading level from: " + levelPath);
                levelData = LevelLoader.load(levelPath);
            }

            if (levelData == null) {
                System.err.println("GameScene: No level data! Creating default level.");
                levelData = createDefaultLevel();
            }

            // Build the level
            buildLevel();

            // Create UI buttons
            createUI();

            initialized = true;
            System.out.println("GameScene: Initialized level '" + levelData.name + "'");
        } catch (Exception e) {
            System.err.println("GameScene: Error during initialization: " + e.getMessage());
            e.printStackTrace();

            // Ensure we have at least a default level
            if (levelData == null) {
                System.err.println("GameScene: Creating fallback default level due to error");
                try {
                    levelData = createDefaultLevel();
                } catch (Exception fallbackError) {
                    System.err.println("GameScene: Even default level creation failed!");
                    fallbackError.printStackTrace();
                }
            }

            // Initialize minimal required components if they failed
            if (entityManager == null) entityManager = new EntityManager();
            if (buttons == null) buttons = new ArrayList<>();
            if (triggers == null) triggers = new ArrayList<>();
            if (doors == null) doors = new ArrayList<>();
            if (interactiveButtons == null) interactiveButtons = new ArrayList<>();
            if (doorsByLinkId == null) doorsByLinkId = new HashMap<>();
            if (buttonsByLinkId == null) buttonsByLinkId = new HashMap<>();

            // Mark as initialized anyway so we can at least draw something
            initialized = true;
            System.err.println("GameScene: Initialized with errors - may not display correctly");
        }
    }

    /**
     * Build entities from level data.
     */
    private void buildLevel() {
        // Create camera
        camera = new Camera(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        camera.setLevelBounds(levelData.levelWidth, levelData.levelHeight);
        camera.setSmoothSpeed(0.25); // Smooth follow - camera catches up to player responsively
        camera.setMaxCameraSpeed(6.0); // Cap camera speed above player speed (4 px/frame) for smooth following
        camera.setDeadZone(0, 0); // No dead zone - always keep player centered

        // Configure vertical scrolling if enabled
        if (levelData.verticalScrollEnabled) {
            camera.setVerticalScrollEnabled(true);
            camera.setVerticalMargin(levelData.verticalMargin);
        }

        // Set up parallax background if enabled
        if (levelData.parallaxEnabled && levelData.parallaxLayers.size() > 0) {
            setupParallaxBackground();
        } else {
            // Use the simple background entity (legacy mode)
            background = new BackgroundEntity(levelData.backgroundPath);

            // Configure background tiling for scrolling levels
            if (levelData.scrollingEnabled) {
                background.setTiling(levelData.tileBackgroundHorizontal, levelData.tileBackgroundVertical);
                background.setCamera(camera);
            }

            entityManager.addEntity(background);
        }

        // Add blocks (new block-based terrain system)
        for (LevelData.BlockData b : levelData.blocks) {
            BlockType blockType = BlockType.fromName(b.blockType);
            BlockEntity block = new BlockEntity(b.x, b.y, blockType, b.useGridCoords);
            // Apply overlay if specified (GRASS, SNOW, ICE, MOSS, VINES)
            if (b.hasOverlay()) {
                BlockOverlay overlay = BlockOverlay.fromName(b.overlay);
                block.setOverlay(overlay);
            }
            // Apply tint if specified
            if (b.hasTint()) {
                block.setTint(b.tintRed, b.tintGreen, b.tintBlue);
            }
            entityManager.addEntity(block);
        }

        // Add platforms (legacy platform system for backwards compatibility)
        for (LevelData.PlatformData p : levelData.platforms) {
            SpriteEntity platform = new SpriteEntity(p.x, p.y, p.spritePath, p.solid);
            // Apply color mask if specified
            if (p.hasColorMask()) {
                platform.setColorMask(p.maskRed, p.maskGreen, p.maskBlue);
            }
            entityManager.addEntity(platform);
        }

        // Add items
        for (LevelData.ItemData i : levelData.items) {
            ItemEntity itemEntity;
            if (i.hasItemId()) {
                // Use ItemRegistry to create item with full properties
                itemEntity = new ItemEntity(i.x, i.y, i.itemId);
            } else {
                // Legacy support for items without registry ID
                itemEntity = new ItemEntity(i.x, i.y, i.spritePath, i.itemName, i.itemType);
            }
            entityManager.addEntity(itemEntity);
        }

        // Add triggers
        for (LevelData.TriggerData t : levelData.triggers) {
            TriggerEntity trigger = new TriggerEntity(t.x, t.y, t.width, t.height, t.type, t.target);
            triggers.add(trigger);
            entityManager.addEntity(trigger);
        }

        // Add mobs (AI-controlled creatures/enemies)
        int mobsAdded = 0;
        for (LevelData.MobData m : levelData.mobs) {
            MobEntity mob = createMobFromData(m);
            if (mob != null) {
                // Set ground level from level data
                mob.setGroundY(levelData.groundY);

                // Set wander bounds - default to spawn position +/- 200 if not specified
                if (m.wanderMinX != 0 || m.wanderMaxX != 0) {
                    mob.setWanderBounds(m.wanderMinX, m.wanderMaxX);
                } else {
                    // Keep mobs near their spawn point
                    mob.setWanderBounds(m.x - 200, m.x + 200);
                }

                // Enable debug drawing if specified (or for first mob for testing)
                if (m.debugDraw || mobsAdded == 0) {
                    mob.setDebugDraw(true);
                }
                entityManager.addEntity(mob);
                mobsAdded++;
            }
        }
        System.out.println("GameScene: Added " + mobsAdded + " mobs to level");

        // Add doors (interactive door entities)
        int doorsAdded = 0;
        for (LevelData.DoorData d : levelData.doors) {
            DoorEntity door = new DoorEntity(d.x, d.y, d.width, d.height, d.texturePath, d.linkId);

            // Configure door properties
            if (d.startsOpen) {
                door.setOpen();
            }
            if (d.locked) {
                door.setLocked(true);
                door.setRequiredKeyId(d.keyItemId);
            }
            if (d.hasAction()) {
                door.setActionType(DoorEntity.ActionType.valueOf(d.actionType.toUpperCase()));
                door.setActionTarget(d.actionTarget);
            }
            door.setAnimationSpeed(d.animationSpeed);

            // Track door for interaction
            doors.add(door);
            if (d.linkId != null && !d.linkId.isEmpty()) {
                doorsByLinkId.put(d.linkId, door);
            }
            entityManager.addEntity(door);
            doorsAdded++;
        }
        System.out.println("GameScene: Added " + doorsAdded + " doors to level");

        // Add buttons (interactive button/switch entities)
        int buttonsAdded = 0;
        for (LevelData.ButtonData b : levelData.buttons) {
            ButtonEntity button = new ButtonEntity(b.x, b.y, b.width, b.height, b.texturePath, b.linkId);

            // Configure button properties
            try {
                button.setButtonType(ButtonEntity.ButtonType.valueOf(b.buttonType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.err.println("GameScene: Unknown button type: " + b.buttonType);
            }
            button.setActivatedByPlayer(b.activatedByPlayer);
            button.setActivatedByMobs(b.activatedByMobs);
            button.setRequiresInteraction(b.requiresInteraction);
            button.setTimedDuration(b.timedDuration);
            button.setAnimationSpeed(b.animationSpeed);

            // Set up linked doors
            if (b.hasLinkedDoors()) {
                for (String doorId : b.linkedDoorIds) {
                    button.addLinkedDoor(doorId);
                }
            }

            // Configure action if specified
            if (b.hasAction()) {
                try {
                    button.setActionType(ButtonEntity.ActionType.valueOf(b.actionType.toUpperCase()));
                    button.setActionTarget(b.actionTarget);
                } catch (IllegalArgumentException e) {
                    System.err.println("GameScene: Unknown button action type: " + b.actionType);
                }
            }

            // Track button for interaction
            interactiveButtons.add(button);
            if (b.linkId != null && !b.linkId.isEmpty()) {
                buttonsByLinkId.put(b.linkId, button);
            }
            entityManager.addEntity(button);
            buttonsAdded++;
        }
        System.out.println("GameScene: Added " + buttonsAdded + " buttons to level");

        // Add vaults (interactive vault/chest entities)
        int vaultsAdded = 0;
        for (LevelData.VaultData v : levelData.vaults) {
            VaultEntity.VaultType vaultType = "STORAGE_CHEST".equals(v.vaultType)
                ? VaultEntity.VaultType.STORAGE_CHEST
                : VaultEntity.VaultType.PLAYER_VAULT;

            VaultEntity vault = new VaultEntity(v.x, v.y, vaultType);
            entityManager.addEntity(vault);
            vaultsAdded++;
        }
        System.out.println("GameScene: Added " + vaultsAdded + " vaults to level");

        // Add player - choose animation system based on level settings
        // Priority: useSpriteAnimation > useBoneAnimation > default (PlayerEntity)
        AudioManager audio = SceneManager.getInstance().getAudioManager();

        if (levelData.useSpriteAnimation) {
            // Use sprite/GIF-based animation with equipment overlay support
            SpritePlayerEntity spriteAnimPlayer = new SpritePlayerEntity(
                levelData.playerSpawnX, levelData.playerSpawnY, levelData.spriteAnimationDir);
            spriteAnimPlayer.setGroundY(levelData.groundY);
            if (audio != null) {
                spriteAnimPlayer.setAudioManager(audio);
            }
            // Apply saved character customization (clothing overlays)
            SpriteCharacterCustomization.applyToPlayer(spriteAnimPlayer);
            // Set camera reference for mouse-aimed projectiles
            spriteAnimPlayer.setCamera(camera);
            player = spriteAnimPlayer;
            System.out.println("GameScene: Using sprite-animated player from " + levelData.spriteAnimationDir);
        } else if (levelData.useBoneAnimation) {
            // Use bone-based skeletal animation
            PlayerBoneEntity bonePlayer = new PlayerBoneEntity(
                levelData.playerSpawnX, levelData.playerSpawnY, levelData.boneTextureDir);
            bonePlayer.setGroundY(levelData.groundY);
            if (audio != null) {
                bonePlayer.setAudioManager(audio);
            }
            player = bonePlayer;
            System.out.println("GameScene: Using bone-animated player from " + levelData.boneTextureDir);
        } else {
            // Use default sprite-based player (simple PNG)
            PlayerEntity spritePlayer = new PlayerEntity(
                levelData.playerSpawnX, levelData.playerSpawnY, levelData.playerSpritePath);
            spritePlayer.setGroundY(levelData.groundY);
            if (audio != null) {
                spritePlayer.setAudioManager(audio);
            }
            player = spritePlayer;
        }
        entityManager.addEntity((Entity) player);

        // Set camera to follow player and snap to initial position
        camera.setTarget((Entity) player);
        camera.snapToTarget();

        // Initialize lighting system
        setupLighting();
    }

    /**
     * Set up the lighting system based on level data.
     */
    private void setupLighting() {
        lightingSystem = new LightingSystem(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        // Configure lighting from level data
        lightingSystem.setNight(levelData.nightMode);
        lightingSystem.setNightDarkness(levelData.nightDarkness);
        lightingSystem.setAmbientLevel(levelData.ambientLight);

        // Create player light (initially disabled unless level specifies otherwise)
        Rectangle playerBounds = player.getBounds();
        double playerCenterX = playerBounds.getCenterX();
        double playerCenterY = playerBounds.getCenterY();

        playerLight = new LightSource(playerCenterX, playerCenterY - 20,
                levelData.playerLightRadius, new Color(255, 220, 150));
        playerLight.setFalloffRadius(levelData.playerLightFalloff);
        playerLight.enableFlicker(0.08, 5.0);
        playerLight.setEnabled(levelData.playerLightEnabled);
        playerHasLantern = levelData.playerLightEnabled;
        lightingSystem.addLightSource(playerLight);

        // Add light sources from level data
        if (levelData.lightSources != null) {
            for (LevelData.LightSourceData lsd : levelData.lightSources) {
                LightSource light = new LightSource(lsd.x, lsd.y, lsd.radius,
                        new Color(lsd.colorRed, lsd.colorGreen, lsd.colorBlue));
                light.setFalloffRadius(lsd.falloffRadius);
                light.setIntensity(lsd.intensity);
                if (lsd.flicker) {
                    light.enableFlicker(lsd.flickerAmount, lsd.flickerSpeed);
                }
                lightingSystem.addLightSource(light);
            }
        }

        lastUpdateTime = System.nanoTime();

        if (levelData.nightMode) {
            System.out.println("GameScene: Lighting system enabled - Night mode with " +
                    lightingSystem.getLightSources().size() + " light sources");
        }
    }

    /**
     * Set up the parallax background system from level data.
     */
    private void setupParallaxBackground() {
        parallaxBackground = new ParallaxBackground();

        for (LevelData.ParallaxLayerData pld : levelData.parallaxLayers) {
            ParallaxLayer layer = new ParallaxLayer(
                    pld.name,
                    pld.imagePath,
                    pld.scrollSpeedX,
                    pld.scrollSpeedY,
                    pld.zOrder
            );

            layer.setScale(pld.scale);
            layer.setOpacity((float) pld.opacity);
            layer.setTiling(pld.tileHorizontal, pld.tileVertical);
            layer.setOffset(pld.offsetX, pld.offsetY);
            layer.setAnchorBottom(pld.anchorBottom);

            parallaxBackground.addLayer(layer);
        }

        System.out.println("GameScene: Parallax background enabled with " +
                parallaxBackground.getLayerCount() + " layers");
    }

    /**
     * Create UI elements.
     */
    private void createUI() {
        // Exit button
        UIButton exitButton = new UIButton(GamePanel.SCREEN_WIDTH - 150, 20, 120, 50, "Exit", () -> {
            AudioManager audio = SceneManager.getInstance().getAudioManager();
            if (audio != null) audio.dispose();
            System.exit(0);
        });
        exitButton.setColors(
                new Color(200, 50, 50, 200),
                new Color(255, 80, 80, 230),
                Color.WHITE
        );
        buttons.add(exitButton);

        // Music toggle button
        UIButton musicButton = new UIButton(GamePanel.SCREEN_WIDTH - 290, 20, 120, 50, "Music", () -> {
            AudioManager audio = SceneManager.getInstance().getAudioManager();
            if (audio != null) audio.toggleMusic();
        });
        musicButton.setColors(
                new Color(50, 100, 200, 200),
                new Color(80, 130, 255, 230),
                Color.WHITE
        );
        buttons.add(musicButton);

        // Menu button (to return to main menu)
        UIButton menuButton = new UIButton(GamePanel.SCREEN_WIDTH - 430, 20, 120, 50, "Menu", () -> {
            SceneManager.getInstance().setScene("mainMenu", SceneManager.TRANSITION_FADE);
        });
        menuButton.setColors(
                new Color(50, 150, 50, 200),
                new Color(80, 200, 80, 230),
                Color.WHITE
        );
        buttons.add(menuButton);

        // Day/Night toggle button (for debugging)
        UIButton dayNightButton = new UIButton(GamePanel.SCREEN_WIDTH - 570, 20, 120, 50, "Day/Night", () -> {
            if (lightingSystem != null) {
                lightingSystem.toggleDayNight();
                String mode = lightingSystem.isNight() ? "Night" : "Day";
                System.out.println("GameScene: Toggled to " + mode + " mode");
            }
        });
        dayNightButton.setColors(
                new Color(100, 50, 150, 200),
                new Color(140, 80, 200, 230),
                Color.WHITE
        );
        buttons.add(dayNightButton);

        // Customize button (opens character customization for sprite-based players)
        if (levelData.useSpriteAnimation) {
            UIButton customizeButton = new UIButton(GamePanel.SCREEN_WIDTH - 710, 20, 120, 50, "Customize", () -> {
                openCharacterCustomization();
            });
            customizeButton.setColors(
                    new Color(180, 120, 50, 200),
                    new Color(220, 160, 80, 230),
                    Color.WHITE
            );
            buttons.add(customizeButton);
        }

        // Debug toggle button (F3 key also toggles)
        UIButton debugButton = new UIButton(GamePanel.SCREEN_WIDTH - 850, 20, 120, 50, "Debug (F3)", () -> {
            toggleDebugMode();
        });
        debugButton.setColors(
                new Color(80, 80, 80, 200),
                new Color(120, 120, 120, 230),
                Color.WHITE
        );
        buttons.add(debugButton);
    }

    /**
     * Opens the character customization scene.
     * The current level state is preserved while customizing.
     */
    private void openCharacterCustomization() {
        // Store current level path for return
        currentLevelPath = this.levelPath;
        SceneManager.getInstance().setScene("spriteCustomization", SceneManager.TRANSITION_FADE);
    }

    /**
     * Toggles debug mode on/off.
     * Debug mode shows hitboxes, entity positions, performance stats, etc.
     */
    private void toggleDebugMode() {
        debugMode = !debugMode;

        // Enable/disable debug drawing on all mobs
        if (entityManager != null) {
            for (Entity entity : entityManager.getEntities()) {
                if (entity instanceof MobEntity) {
                    ((MobEntity) entity).setDebugDraw(debugMode);
                }
            }
        }
    }

    /**
     * Returns whether debug mode is currently enabled.
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    // Store level path for returning from customization
    private static String currentLevelPath = null;

    /**
     * Gets the current level path (used when returning from customization).
     */
    public static String getCurrentLevelPath() {
        return currentLevelPath;
    }

    /**
     * Create a default level if none is loaded.
     */
    private LevelData createDefaultLevel() {
        return LevelData.builder()
                .name("Default Level")
                .description("A default test level")
                .background("assets/background.png")
                .music("sounds/music.wav")
                .playerSpawn(100, 620)
                .groundY(720)
                // Platforms
                .addPlatform(250, 670)
                .addPlatform(400, 630)
                .addPlatform(550, 590)
                .addPlatform(700, 550)
                .addPlatform(850, 510)
                .addPlatform(1000, 470)
                .addPlatform(1150, 430)
                .addPlatform(1300, 390)
                // Items
                .addItem(200, 650, "Key", "key")
                .addItem(350, 620, "Gem", "collectible")
                .addItem(500, 580, "Coin", "collectible")
                .addItem(650, 540, "Star", "collectible")
                .addItem(800, 500, "Crystal", "collectible")
                .addItem(950, 460, "Ruby", "collectible")
                .addItem(1100, 420, "Diamond", "collectible")
                .addItem(1350, 350, "Trophy", "special")
                .build();
    }

    @Override
    public void update(InputManager input) {
        if (!initialized) return;

        // Calculate delta time for lighting updates
        long currentTime = System.nanoTime();
        double deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0;
        lastUpdateTime = currentTime;

        // Handle door and button interactions FIRST (before entities consume E key)
        handleDoorButtonInteractions(input);

        entityManager.updateAll(input);

        // Handle block breaking - check for broken blocks and dropped items
        if (player != null) {
            handleBlockBreaking();
        }

        // C key opens character customization (for sprite-based players)
        if (levelData.useSpriteAnimation && input.isKeyJustPressed('c')) {
            openCharacterCustomization();
        }

        // F3 key toggles debug mode
        if (input.isKeyJustPressed(KeyEvent.VK_F3)) {
            toggleDebugMode();
        }

        // Update FPS counter
        frameCount++;
        long now = System.nanoTime();
        if (now - lastFpsTime >= 1_000_000_000) {
            currentFps = (int) frameCount;
            frameCount = 0;
            lastFpsTime = now;
        }

        // Update camera to follow player
        if (camera != null && levelData.scrollingEnabled) {
            camera.update();
        }

        // Check triggers
        if (player != null) {
            Rectangle playerBounds = player.getBounds();
            for (TriggerEntity trigger : triggers) {
                if (trigger.checkTrigger(playerBounds)) {
                    trigger.execute();
                }
            }
        }

        // Update lighting system
        if (lightingSystem != null) {
            lightingSystem.update(deltaTime);

            // Update player light position to follow player
            if (playerLight != null && player != null) {
                Rectangle bounds = player.getBounds();
                playerLight.setPosition(bounds.getCenterX(), bounds.getCenterY() - 20);
            }

            // Check if player collected a lantern
            checkLanternCollection();
        }
    }

    /**
     * Check if the player has collected a lantern item and enable their light.
     */
    private void checkLanternCollection() {
        if (playerHasLantern || player == null) return;

        // Check player's inventory for a lantern
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getItemCount(); i++) {
            ItemEntity item = inventory.getHeldItem();
            if (item != null && item.getItemName().toLowerCase().contains("lantern")) {
                playerHasLantern = true;
                playerLight.setEnabled(true);
                System.out.println("GameScene: Player found a lantern! Light enabled.");
                break;
            }
        }

        // Also check all items in inventory (not just held)
        // We need to iterate through the items - check via held item cycling
        int originalSlot = inventory.getSelectedSlot();
        for (int i = 0; i < 5; i++) { // Check first 5 slots (hotbar)
            inventory.setSelectedSlot(i);
            ItemEntity item = inventory.getHeldItem();
            if (item != null && item.getItemName().toLowerCase().contains("lantern")) {
                playerHasLantern = true;
                playerLight.setEnabled(true);
                System.out.println("GameScene: Player found a lantern! Light enabled.");
                break;
            }
        }
        inventory.setSelectedSlot(originalSlot);
    }

    /**
     * Handles block breaking - removes broken blocks and adds dropped items.
     * Works with PlayerEntity, PlayerBoneEntity, and SpritePlayerEntity.
     */
    private void handleBlockBreaking() {
        BlockEntity brokenBlock = null;
        ItemEntity droppedItem = null;

        // Get broken block from whichever player type we have
        if (player instanceof SpritePlayerEntity) {
            SpritePlayerEntity spriteAnimPlayer = (SpritePlayerEntity) player;
            brokenBlock = spriteAnimPlayer.getLastBrokenBlock();
            if (brokenBlock != null) {
                droppedItem = spriteAnimPlayer.getLastDroppedItem();
            }
        } else if (player instanceof PlayerEntity) {
            PlayerEntity spritePlayer = (PlayerEntity) player;
            brokenBlock = spritePlayer.getLastBrokenBlock();
            if (brokenBlock != null) {
                droppedItem = spritePlayer.getLastDroppedItem();
            }
        } else if (player instanceof PlayerBoneEntity) {
            PlayerBoneEntity bonePlayer = (PlayerBoneEntity) player;
            brokenBlock = bonePlayer.getLastBrokenBlock();
            if (brokenBlock != null) {
                droppedItem = bonePlayer.getLastDroppedItem();
            }
        }

        // Handle the broken block
        if (brokenBlock != null) {
            // Remove the broken block from the entity manager
            entityManager.removeEntity(brokenBlock);

            // Add the dropped item if any
            if (droppedItem != null) {
                entityManager.addEntity(droppedItem);
            }
        }
    }

    /**
     * Handles door and button interactions.
     * Includes player E key interactions, button-to-door linking, and pressure plate mechanics.
     */
    private void handleDoorButtonInteractions(InputManager input) {
        if (player == null) return;

        Rectangle playerBounds = player.getBounds();

        // Update door proximity detection
        for (DoorEntity door : doors) {
            boolean wasNearby = door.isPlayerNearby();
            boolean isNearby = door.isInInteractionZone(playerBounds);
            door.setPlayerNearby(isNearby);
        }

        // Update button proximity detection (for interaction prompts)
        for (ButtonEntity button : interactiveButtons) {
            if (button.requiresInteraction()) {
                boolean isNearby = button.isInActivationZone(playerBounds);
                button.setPlayerNearby(isNearby);
            }
        }

        // Handle E key interactions with doors and buttons
        if (input.isKeyJustPressed('e') || input.isKeyJustPressed('E')) {
            System.out.println("GameScene: E key pressed, checking " + doors.size() + " doors and " + interactiveButtons.size() + " buttons");

            // Try to interact with nearby doors
            for (DoorEntity door : doors) {
                System.out.println("GameScene: Checking door " + door.getLinkId() + ", isPlayerNearby=" + door.isPlayerNearby());
                if (door.isPlayerNearby()) {
                    // Check if door is locked and player has key
                    if (door.isLocked()) {
                        String keyId = door.getRequiredKeyId();
                        if (playerHasKey(keyId)) {
                            door.tryUnlock(keyId);
                            removeKeyFromInventory(keyId);
                            door.toggle();
                            executeDoorAction(door);
                        } else {
                            System.out.println("GameScene: Door requires key: " + keyId);
                        }
                    } else {
                        door.toggle();
                        executeDoorAction(door);
                    }
                    break;  // Only interact with one door at a time
                }
            }

            // Try to interact with nearby buttons (that require interaction)
            for (ButtonEntity button : interactiveButtons) {
                if (button.requiresInteraction() && button.isInActivationZone(playerBounds)) {
                    if (button.activate(true)) {
                        handleButtonActivation(button);
                    }
                    break;  // Only interact with one button at a time
                }
            }
        }

        // Handle pressure plate (non-interaction) buttons
        for (ButtonEntity button : interactiveButtons) {
            if (!button.requiresInteraction()) {
                boolean wasOnButton = button.isActivated();
                boolean isOnButton = button.isEntityOnButton(playerBounds);

                if (isOnButton && !wasOnButton) {
                    button.onEntityEnter(true);
                    if (button.isActivated()) {
                        handleButtonActivation(button);
                    }
                } else if (!isOnButton && wasOnButton) {
                    button.onEntityExit();
                    if (!button.isActivated()) {
                        handleButtonDeactivation(button);
                    }
                }
            }
        }

        // Check if mobs activate pressure plates
        for (Entity entity : entityManager.getEntities()) {
            if (entity instanceof MobEntity) {
                MobEntity mob = (MobEntity) entity;
                Rectangle mobBounds = mob.getBounds();

                for (ButtonEntity button : interactiveButtons) {
                    if (!button.requiresInteraction() && button.isActivatedByMobs()) {
                        if (button.isEntityOnButton(mobBounds)) {
                            if (!button.isActivated()) {
                                button.onEntityEnter(false);
                                if (button.isActivated()) {
                                    handleButtonActivation(button);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Handles button activation - triggers linked doors and actions.
     */
    private void handleButtonActivation(ButtonEntity button) {
        System.out.println("GameScene: Button " + button.getLinkId() + " activated");

        // Toggle linked doors
        for (String doorId : button.getLinkedDoorIds()) {
            DoorEntity linkedDoor = doorsByLinkId.get(doorId);
            if (linkedDoor != null) {
                linkedDoor.toggle();
                System.out.println("GameScene: Toggled door " + doorId);
            }
        }

        // Execute button action if configured
        executeButtonAction(button);
    }

    /**
     * Handles button deactivation (for momentary buttons).
     */
    private void handleButtonDeactivation(ButtonEntity button) {
        System.out.println("GameScene: Button " + button.getLinkId() + " deactivated");

        // For momentary buttons, close linked doors when released
        if (button.getButtonType() == ButtonEntity.ButtonType.MOMENTARY) {
            for (String doorId : button.getLinkedDoorIds()) {
                DoorEntity linkedDoor = doorsByLinkId.get(doorId);
                if (linkedDoor != null && linkedDoor.isOpen()) {
                    linkedDoor.close();
                }
            }
        }
    }

    /**
     * Executes door-specific actions (level transitions, events, etc.).
     */
    private void executeDoorAction(DoorEntity door) {
        System.out.println("GameScene: executeDoorAction called for door " + door.getLinkId() +
            ", actionType=" + door.getActionType() + ", actionTarget=" + door.getActionTarget());

        if (door.getActionType() == DoorEntity.ActionType.NONE) {
            System.out.println("GameScene: Door action is NONE, returning");
            return;
        }

        switch (door.getActionType()) {
            case LEVEL_TRANSITION:
                String target = door.getActionTarget();
                if (target != null && !target.isEmpty()) {
                    // Check if it's a special scene transition (scene:sceneName)
                    if (target.startsWith("scene:")) {
                        String sceneName = target.substring(6); // Remove "scene:" prefix
                        SceneManager.getInstance().setScene(sceneName, SceneManager.TRANSITION_FADE);
                    } else {
                        SceneManager.getInstance().loadLevel(target, SceneManager.TRANSITION_FADE);
                    }
                }
                break;

            case EVENT:
                System.out.println("GameScene: Door event triggered: " + door.getActionTarget());
                // Custom event handling can be added here
                break;

            case TELEPORT:
                // Parse coordinates from actionTarget (format: "x,y")
                try {
                    String[] coords = door.getActionTarget().split(",");
                    if (coords.length >= 2) {
                        int teleportX = Integer.parseInt(coords[0].trim());
                        int teleportY = Integer.parseInt(coords[1].trim());
                        player.getBounds().setLocation(teleportX, teleportY);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("GameScene: Invalid teleport coordinates: " + door.getActionTarget());
                }
                break;

            case SPAWN_ENTITY:
                System.out.println("GameScene: Spawn entity triggered: " + door.getActionTarget());
                // Entity spawning can be implemented here
                break;
        }
    }

    /**
     * Executes button-specific actions.
     */
    private void executeButtonAction(ButtonEntity button) {
        if (button.getActionType() == ButtonEntity.ActionType.NONE) return;

        switch (button.getActionType()) {
            case LEVEL_TRANSITION:
                String target = button.getActionTarget();
                if (target != null && !target.isEmpty()) {
                    // Check if it's a special scene transition (scene:sceneName)
                    if (target.startsWith("scene:")) {
                        String sceneName = target.substring(6); // Remove "scene:" prefix
                        SceneManager.getInstance().setScene(sceneName, SceneManager.TRANSITION_FADE);
                    } else {
                        SceneManager.getInstance().loadLevel(target, SceneManager.TRANSITION_FADE);
                    }
                }
                break;

            case EVENT:
                System.out.println("GameScene: Button event triggered: " + button.getActionTarget());
                break;

            case SPAWN_ENTITY:
                String mobType = button.getActionTarget();
                if (mobType != null && !mobType.isEmpty()) {
                    // Spawn mob near the button
                    int spawnX = (int)(button.getBounds().getCenterX());
                    int spawnY = (int)(button.getBounds().getY()) - 100;
                    SpriteMobEntity spawnedMob = createSpawnedMob(mobType, spawnX, spawnY);
                    if (spawnedMob != null) {
                        entityManager.addEntity(spawnedMob);
                        System.out.println("GameScene: Spawned " + mobType + " at (" + spawnX + ", " + spawnY + ")");
                    }
                }
                break;

            case PLAY_SOUND:
                AudioManager audio = SceneManager.getInstance().getAudioManager();
                if (audio != null && button.getActionTarget() != null) {
                    audio.playSoundFromPath(button.getActionTarget());
                }
                break;
        }
    }

    /**
     * Checks if the player has a specific key item in their inventory.
     */
    private boolean playerHasKey(String keyId) {
        if (keyId == null || keyId.isEmpty()) return true;

        Inventory inventory = player.getInventory();
        int originalSlot = inventory.getSelectedSlot();

        for (int i = 0; i < inventory.getMaxSlots(); i++) {
            inventory.setSelectedSlot(i);
            ItemEntity item = inventory.getHeldItem();
            if (item != null) {
                Item linkedItem = item.getLinkedItem();
                if (linkedItem != null && keyId.equals(linkedItem.getName())) {
                    inventory.setSelectedSlot(originalSlot);
                    return true;
                }
                // Also check by item name as fallback
                if (item.getItemName() != null &&
                    item.getItemName().toLowerCase().contains(keyId.toLowerCase())) {
                    inventory.setSelectedSlot(originalSlot);
                    return true;
                }
            }
        }

        inventory.setSelectedSlot(originalSlot);
        return false;
    }

    /**
     * Removes a key item from the player's inventory after use.
     */
    private void removeKeyFromInventory(String keyId) {
        if (keyId == null || keyId.isEmpty()) return;

        Inventory inventory = player.getInventory();
        int originalSlot = inventory.getSelectedSlot();

        for (int i = 0; i < inventory.getMaxSlots(); i++) {
            inventory.setSelectedSlot(i);
            ItemEntity item = inventory.getHeldItem();
            if (item != null) {
                Item linkedItem = item.getLinkedItem();
                boolean matches = false;

                if (linkedItem != null && keyId.equals(linkedItem.getName())) {
                    matches = true;
                } else if (item.getItemName() != null &&
                           item.getItemName().toLowerCase().contains(keyId.toLowerCase())) {
                    matches = true;
                }

                if (matches) {
                    inventory.removeItemAtSlot(i);
                    System.out.println("GameScene: Used key: " + keyId);
                    break;
                }
            }
        }

        inventory.setSelectedSlot(originalSlot);
    }

    @Override
    public void draw(Graphics g) {
        if (!initialized) {
            System.err.println("GameScene: Cannot draw - scene not initialized");
            return;
        }

        if (levelData == null) {
            System.err.println("GameScene: Cannot draw - levelData is null");
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("ERROR: Level data failed to load!", 50, 50);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        // Use camera-based rendering for scrolling levels
        if (levelData.scrollingEnabled && camera != null) {
            drawWithCamera(g2d);
        } else {
            drawWithoutCamera(g2d);
        }

        // Draw UI elements (always in screen space, not affected by camera)
        drawUI(g2d);
    }

    /**
     * Draws the scene with camera transformation for scrolling levels.
     */
    private void drawWithCamera(Graphics2D g2d) {
        // Save original transform
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();

        // Draw parallax background layers (behind everything)
        // These are drawn with camera transform applied since they handle parallax internally
        if (parallaxBackground != null) {
            camera.applyTransform(g2d);
            parallaxBackground.drawBackground(g2d, camera);  // z < 0
            parallaxBackground.drawMiddleground(g2d, camera); // z = 0
            g2d.setTransform(oldTransform);
        }

        // Apply camera transformation
        camera.applyTransform(g2d);

        // Draw legacy background if using old system
        if (background != null && parallaxBackground == null) {
            background.draw(g2d, camera);
        }

        // Draw ground area (extends across the level width)
        g2d.setColor(new Color(34, 139, 34, 100));
        g2d.fillRect(0, levelData.groundY, levelData.levelWidth, GamePanel.SCREEN_HEIGHT);

        // Draw ground line (extends across the level width)
        g2d.setStroke(new BasicStroke(4));
        g2d.setColor(new Color(34, 139, 34));
        g2d.drawLine(0, levelData.groundY, levelData.levelWidth, levelData.groundY);

        // Draw all entities with camera (handles background tiling)
        if (entityManager != null) {
            if (parallaxBackground != null) {
                // Don't draw background again if using parallax
                entityManager.drawAll(g2d, camera);
            } else {
                entityManager.drawAllWithBackground(g2d, camera, background);
            }
        }

        // Draw parallax foreground layers (in front of game entities)
        if (parallaxBackground != null) {
            parallaxBackground.drawForeground(g2d, camera);  // z > 0
        }

        // Restore original transform for UI
        g2d.setTransform(oldTransform);

        // Apply lighting overlay (after world, before UI)
        if (lightingSystem != null && lightingSystem.isNight()) {
            lightingSystem.render(g2d, camera.getX(), camera.getY());
        }

        // Draw black bars at top and bottom for vertical scrolling (only when no parallax)
        if (camera.isVerticalScrollEnabled() && camera.getVerticalMargin() > 0 && parallaxBackground == null) {
            drawBlackBars(g2d);
        }
    }

    /**
     * Draws black bars at the top and bottom of the screen for vertical scrolling.
     * These bars create a letterbox effect. Only used when parallax is disabled.
     */
    private void drawBlackBars(Graphics2D g2d) {
        int margin = camera.getVerticalMargin();
        g2d.setColor(Color.BLACK);

        // Top black bar
        g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, margin);

        // Bottom black bar
        g2d.fillRect(0, GamePanel.SCREEN_HEIGHT - margin, GamePanel.SCREEN_WIDTH, margin);
    }

    /**
     * Draws the scene without camera transformation (original behavior).
     */
    private void drawWithoutCamera(Graphics2D g2d) {
        // Draw ground area
        g2d.setColor(new Color(34, 139, 34, 100));
        g2d.fillRect(0, levelData.groundY, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT - levelData.groundY);

        // Draw ground line
        g2d.setStroke(new BasicStroke(4));
        g2d.setColor(new Color(34, 139, 34));
        g2d.drawLine(0, levelData.groundY, GamePanel.SCREEN_WIDTH, levelData.groundY);

        // Draw entities
        if (entityManager != null) {
            entityManager.drawAll(g2d);
        }

        // Apply lighting overlay (after world, before UI)
        if (lightingSystem != null && lightingSystem.isNight()) {
            lightingSystem.render(g2d);
        }
    }

    /**
     * Draws UI elements that are always in screen space.
     */
    private void drawUI(Graphics2D g2d) {
        // Draw UI buttons
        if (buttons != null) {
            for (UIButton button : buttons) {
                button.draw(g2d);
            }
        }

        // Draw inventory UI (in screen space, not affected by camera)
        if (player != null) {
            player.getInventory().draw(g2d);

            // Draw player status bars (health, mana, stamina) above hotbar
            PlayerStatusBar.draw(g2d, player, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        }

        // Draw level name
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        String name = levelData.name != null ? levelData.name : "Unknown Level";
        g2d.drawString(name, 10, 60);

        // Draw controls hint
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("A/D: Move | SPACE: Jump | LMB/E: Mine | Scroll: Aim | I: Inventory", 10, 30);

        // Draw camera position debug info for scrolling levels
        if (levelData.scrollingEnabled && camera != null) {
            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            g2d.setColor(new Color(255, 255, 255, 180));
            String camInfo = String.format("Camera: (%.0f, %.0f) | Level: %dx%d",
                    camera.getX(), camera.getY(), levelData.levelWidth, levelData.levelHeight);
            g2d.drawString(camInfo, 10, GamePanel.SCREEN_HEIGHT - 20);
        }

        // Draw debug overlay if debug mode is enabled
        if (debugMode) {
            drawDebugOverlay(g2d);
        }
    }

    /**
     * Draws debug information overlay when debug mode is enabled.
     */
    private void drawDebugOverlay(Graphics2D g2d) {
        int debugX = 10;
        int debugY = 100;
        int lineHeight = 18;

        // Semi-transparent background for debug panel
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(5, debugY - 15, 350, 220);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.setColor(Color.CYAN);
        g2d.drawString("=== DEBUG MODE (F3 to toggle) ===", debugX, debugY);
        debugY += lineHeight;

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2d.setColor(Color.WHITE);

        // FPS
        g2d.drawString(String.format("FPS: %d", currentFps), debugX, debugY);
        debugY += lineHeight;

        // Player info
        if (player != null) {
            Rectangle bounds = player.getBounds();
            g2d.drawString(String.format("Player Pos: (%.0f, %.0f)", bounds.getX(), bounds.getY()), debugX, debugY);
            debugY += lineHeight;
            g2d.drawString(String.format("Player Health: %d/%d", player.getHealth(), player.getMaxHealth()), debugX, debugY);
            debugY += lineHeight;
            g2d.drawString(String.format("Player Mana: %d/%d", player.getMana(), player.getMaxMana()), debugX, debugY);
            debugY += lineHeight;
            g2d.drawString(String.format("Player Stamina: %d/%d", player.getStamina(), player.getMaxStamina()), debugX, debugY);
            debugY += lineHeight;
        }

        // Entity count
        if (entityManager != null) {
            int totalEntities = entityManager.getEntities().size();
            int mobCount = 0;
            int blockCount = 0;
            int itemCount = 0;
            for (Entity e : entityManager.getEntities()) {
                if (e instanceof MobEntity) mobCount++;
                else if (e instanceof BlockEntity) blockCount++;
                else if (e instanceof ItemEntity) itemCount++;
            }
            g2d.drawString(String.format("Entities: %d (Mobs: %d, Blocks: %d, Items: %d)",
                    totalEntities, mobCount, blockCount, itemCount), debugX, debugY);
            debugY += lineHeight;
        }

        // Lighting info
        if (lightingSystem != null) {
            g2d.drawString(String.format("Lighting: %s | Lights: %d",
                    lightingSystem.isNight() ? "NIGHT" : "DAY",
                    lightingSystem.getLightSources().size()), debugX, debugY);
            debugY += lineHeight;
        }

        // Camera info
        if (camera != null) {
            g2d.drawString(String.format("Camera: (%.0f, %.0f) | Zoom: %.2f",
                    camera.getX(), camera.getY(), 1.0), debugX, debugY);
            debugY += lineHeight;
        }

        // Level info
        g2d.drawString(String.format("Level: %s (%dx%d)",
                levelData.name, levelData.levelWidth, levelData.levelHeight), debugX, debugY);
        debugY += lineHeight;

        // Controls hint
        g2d.setColor(Color.YELLOW);
        g2d.drawString("SHIFT+A/D: Sprint | SPACE x3: Triple Jump", debugX, debugY);
    }

    @Override
    public void dispose() {
        System.out.println("GameScene: Disposing level '" + (levelData != null ? levelData.name : "unknown") + "'");
        initialized = false;
        entityManager = null;
        buttons = null;
        triggers = null;
        doors = null;
        interactiveButtons = null;
        doorsByLinkId = null;
        buttonsByLinkId = null;
        player = null;
        levelData = null;
        camera = null;
        background = null;
        parallaxBackground = null;
        lightingSystem = null;
        playerLight = null;
        playerHasLantern = false;
    }

    @Override
    public void onMousePressed(int x, int y) {
        if (player != null) {
            player.getInventory().handleMousePressed(x, y);
        }
    }

    @Override
    public void onMouseReleased(int x, int y) {
        if (player != null) {
            ItemEntity droppedItem = player.getInventory().handleMouseReleased(x, y);
            if (droppedItem != null) {
                droppedItem.collected = false;
                player.dropItem(droppedItem);
                entityManager.addEntity(droppedItem);
            }
        }
    }

    @Override
    public void onMouseDragged(int x, int y) {
        if (player != null) {
            player.getInventory().handleMouseDragged(x, y);
        }
    }

    @Override
    public void onMouseMoved(int x, int y) {
        for (UIButton button : buttons) {
            button.handleMouseMove(x, y);
        }
    }

    @Override
    public void onMouseClicked(int x, int y) {
        for (UIButton button : buttons) {
            if (button.handleClick(x, y)) {
                // UI button handled the click - consume it so game logic doesn't also respond
                InputManager input = SceneManager.getInstance().getInputManager();
                if (input != null) {
                    input.consumeClick();
                }
                return;  // Stop processing after first button handles click
            }
        }
    }

    @Override
    public String getName() {
        return levelData != null ? levelData.name : "GameScene";
    }

    public LevelData getLevelData() {
        return levelData;
    }

    public PlayerBase getPlayer() {
        return player;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Gets the camera for this scene.
     *
     * @return Camera instance, or null if not initialized
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * Creates a sprite mob entity for button-triggered spawns.
     *
     * @param mobType The type of mob to spawn (e.g., "bear", "wolf", "zombie")
     * @param x Spawn X position
     * @param y Spawn Y position
     * @return A SpriteMobEntity instance, or null if type is unknown
     */
    private SpriteMobEntity createSpawnedMob(String mobType, int x, int y) {
        if (mobType == null) return null;

        String type = mobType.toLowerCase();

        // Try to create a sprite-based mob
        String spriteDir = "assets/mobs/" + type;

        // Create mob (body type is auto-detected from sprite directory)
        SpriteMobEntity mob = new SpriteMobEntity(x, y, spriteDir);
        mob.setGroundY(levelData.groundY);

        System.out.println("GameScene: Created spawned " + type + " mob at (" + x + ", " + y + ")");
        return mob;
    }

    /**
     * Creates a mob entity from level data.
     *
     * @param m The mob data from level JSON
     * @return A MobEntity instance, or null if type is unknown
     */
    private MobEntity createMobFromData(LevelData.MobData m) {
        if (m.mobType == null) return null;

        String type = m.mobType.toLowerCase();
        String subType = m.subType != null ? m.subType.toLowerCase() : "";

        if (type.equals("quadruped")) {
            // Parse animal type
            QuadrupedSkeleton.AnimalType animalType;
            try {
                animalType = QuadrupedSkeleton.AnimalType.valueOf(subType.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("GameScene: Unknown quadruped subType: " + subType);
                return null;
            }

            // Parse behavior type
            QuadrupedMobEntity.BehaviorType behavior = QuadrupedMobEntity.BehaviorType.PASSIVE;
            if (m.behavior != null) {
                switch (m.behavior.toLowerCase()) {
                    case "hostile":
                        behavior = QuadrupedMobEntity.BehaviorType.HOSTILE;
                        break;
                    case "neutral":
                        behavior = QuadrupedMobEntity.BehaviorType.NEUTRAL;
                        break;
                    case "passive":
                    default:
                        behavior = QuadrupedMobEntity.BehaviorType.PASSIVE;
                        break;
                }
            }

            // Create quadruped with or without textures
            if (m.textureDir != null && !m.textureDir.isEmpty()) {
                return new QuadrupedMobEntity(m.x, m.y, animalType, behavior, m.textureDir);
            } else {
                return new QuadrupedMobEntity(m.x, m.y, animalType, behavior);
            }

        } else if (type.equals("humanoid")) {
            // Parse humanoid variant type
            HumanoidVariants.VariantType variantType;
            try {
                variantType = HumanoidVariants.VariantType.valueOf(subType.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("GameScene: Unknown humanoid subType: " + subType);
                return null;
            }

            // Create humanoid with or without textures
            if (m.textureDir != null && !m.textureDir.isEmpty()) {
                return new HumanoidMobEntity(m.x, m.y, variantType, m.textureDir);
            } else {
                return new HumanoidMobEntity(m.x, m.y, variantType);
            }

        } else if (type.equals("sprite_quadruped")) {
            // Sprite-based quadruped mob using GIF animations
            if (m.spriteDir == null || m.spriteDir.isEmpty()) {
                System.err.println("GameScene: sprite_quadruped requires spriteDir");
                return null;
            }

            SpriteMobEntity mob = new SpriteMobEntity(m.x, m.y, m.spriteDir);

            // Set behavior based on level data
            if (m.behavior != null) {
                switch (m.behavior.toLowerCase()) {
                    case "hostile":
                        mob.setHostile(true);
                        mob.setAggroRange(300);
                        break;
                    case "neutral":
                        mob.setHostile(false);
                        mob.setAggroRange(150); // Will aggro if attacked
                        break;
                    case "passive":
                    default:
                        mob.setHostile(false);
                        mob.setAggroRange(0);
                        break;
                }
            }

            // Enable debug drawing if requested
            if (m.debugDraw) {
                mob.setDebugDraw(true);
            }

            System.out.println("GameScene: Created sprite_quadruped mob at (" + m.x + "," + m.y + ") with sprites from " + m.spriteDir);
            return mob;

        } else if (type.equals("sprite_humanoid")) {
            // Sprite-based humanoid mob using GIF animations
            if (m.spriteDir == null || m.spriteDir.isEmpty()) {
                System.err.println("GameScene: sprite_humanoid requires spriteDir");
                return null;
            }

            SpriteMobEntity mob = new SpriteMobEntity(m.x, m.y, m.spriteDir);

            // Set behavior based on level data
            if (m.behavior != null) {
                switch (m.behavior.toLowerCase()) {
                    case "hostile":
                        mob.setHostile(true);
                        mob.setAggroRange(350);
                        break;
                    case "neutral":
                        mob.setHostile(false);
                        mob.setAggroRange(200);
                        break;
                    case "passive":
                    default:
                        mob.setHostile(false);
                        mob.setAggroRange(0);
                        break;
                }
            }

            // Enable debug drawing if requested
            if (m.debugDraw) {
                mob.setDebugDraw(true);
            }

            System.out.println("GameScene: Created sprite_humanoid mob at (" + m.x + "," + m.y + ") with sprites from " + m.spriteDir);
            return mob;
        }

        System.err.println("GameScene: Unknown mobType: " + type);
        return null;
    }
}
