import java.awt.*;
import java.util.ArrayList;

/**
 * Scene implementation for actual gameplay.
 * Loads levels from JSON files and manages gameplay logic.
 */
class GameScene implements Scene {

    private String levelPath;
    private LevelData levelData;
    private EntityManager entityManager;
    private ArrayList<UIButton> buttons;
    private ArrayList<TriggerEntity> triggers;
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

    @Override
    public void init() {
        if (initialized) return;

        System.out.println("GameScene: Initializing...");

        try {
            entityManager = new EntityManager();
            buttons = new ArrayList<>();
            triggers = new ArrayList<>();

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
            entityManager.addEntity(new ItemEntity(i.x, i.y, i.spritePath, i.itemName, i.itemType));
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

        // Add player - use bone animation if enabled
        if (levelData.useBoneAnimation) {
            PlayerBoneEntity bonePlayer = new PlayerBoneEntity(
                levelData.playerSpawnX, levelData.playerSpawnY, levelData.boneTextureDir);
            bonePlayer.setGroundY(levelData.groundY);
            AudioManager audio = SceneManager.getInstance().getAudioManager();
            if (audio != null) {
                bonePlayer.setAudioManager(audio);
            }
            player = bonePlayer;
            System.out.println("GameScene: Using bone-animated player from " + levelData.boneTextureDir);
        } else {
            PlayerEntity spritePlayer = new PlayerEntity(
                levelData.playerSpawnX, levelData.playerSpawnY, levelData.playerSpritePath);
            spritePlayer.setGroundY(levelData.groundY);
            AudioManager audio = SceneManager.getInstance().getAudioManager();
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
    }

    /**
     * Create a default level if none is loaded.
     */
    private LevelData createDefaultLevel() {
        return LevelData.builder()
                .name("Default Level")
                .description("A default test level")
                .background("assets/background.png")
                .music("assets/music.wav")
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

        entityManager.updateAll(input);

        // Handle block breaking - check for broken blocks and dropped items
        if (player != null) {
            handleBlockBreaking();
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
     * Works with both PlayerEntity and PlayerBoneEntity.
     */
    private void handleBlockBreaking() {
        BlockEntity brokenBlock = null;
        ItemEntity droppedItem = null;

        // Get broken block from whichever player type we have
        if (player instanceof PlayerEntity) {
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

        // Draw black bars at top and bottom for vertical scrolling
        if (camera.isVerticalScrollEnabled() && camera.getVerticalMargin() > 0) {
            drawBlackBars(g2d);
        }
    }

    /**
     * Draws black bars at the top and bottom of the screen for vertical scrolling.
     * These bars create a letterbox effect and can be used for gameplay elements.
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
        }

        // Draw level name
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        String name = levelData.name != null ? levelData.name : "Unknown Level";
        g2d.drawString(name, 10, 60);

        // Draw controls hint
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("A/D: Move | SPACE: Jump | E/Q/F: Mine (Side/Up/Down) | I: Inventory", 10, 30);

        // Draw camera position debug info for scrolling levels
        if (levelData.scrollingEnabled && camera != null) {
            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            g2d.setColor(new Color(255, 255, 255, 180));
            String camInfo = String.format("Camera: (%.0f, %.0f) | Level: %dx%d",
                    camera.getX(), camera.getY(), levelData.levelWidth, levelData.levelHeight);
            g2d.drawString(camInfo, 10, GamePanel.SCREEN_HEIGHT - 20);
        }
    }

    @Override
    public void dispose() {
        System.out.println("GameScene: Disposing level '" + (levelData != null ? levelData.name : "unknown") + "'");
        initialized = false;
        entityManager = null;
        buttons = null;
        triggers = null;
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
            button.handleClick(x, y);
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
        }

        System.err.println("GameScene: Unknown mobType: " + type);
        return null;
    }
}
