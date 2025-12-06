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
    private PlayerEntity player;
    private boolean initialized;

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
        // Add background
        entityManager.addEntity(new BackgroundEntity(levelData.backgroundPath));

        // Add platforms
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

        // Add player
        player = new PlayerEntity(levelData.playerSpawnX, levelData.playerSpawnY, levelData.playerSpritePath);
        AudioManager audio = SceneManager.getInstance().getAudioManager();
        if (audio != null) {
            player.setAudioManager(audio);
        }
        entityManager.addEntity(player);
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

        entityManager.updateAll(input);

        // Check triggers
        if (player != null) {
            Rectangle playerBounds = player.getBounds();
            for (TriggerEntity trigger : triggers) {
                if (trigger.checkTrigger(playerBounds)) {
                    trigger.execute();
                }
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

        // Draw ground area
        g2d.setColor(new Color(34, 139, 34, 100));
        g2d.fillRect(0, levelData.groundY, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT - levelData.groundY);

        // Draw ground line
        g2d.setStroke(new BasicStroke(4));
        g2d.setColor(new Color(34, 139, 34));
        g2d.drawLine(0, levelData.groundY, GamePanel.SCREEN_WIDTH, levelData.groundY);

        // Draw entities
        if (entityManager != null) {
            entityManager.drawAll(g);
        }

        // Draw UI buttons
        if (buttons != null) {
            for (UIButton button : buttons) {
                button.draw(g);
            }
        }

        // Draw level name
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String name = levelData.name != null ? levelData.name : "Unknown Level";
        g.drawString(name, 10, 60);

        // Draw controls hint
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("A/D: Move | SPACE: Jump | I: Inventory", 10, 30);
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

    public PlayerEntity getPlayer() {
        return player;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
