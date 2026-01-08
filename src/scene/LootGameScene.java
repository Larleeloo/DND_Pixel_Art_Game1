package scene;

import core.GamePanel;
import entity.*;
import entity.player.*;
import block.*;
import input.*;
import graphics.*;
import audio.*;
import ui.*;
import save.SaveManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * LootGameScene - A fun mini-game where players open treasure chests for random loot!
 *
 * Features:
 * - Flat level 6000 pixels wide with plank floor
 * - Daily chest (center) - Opens once per day, drops 3 items
 * - Monthly chest (right side) - Opens once per month, drops 10 high-rarity items
 * - Items have Borderlands-style rarity beams
 * - Items bounce with physics when dropped
 * - Player inventory saved to JSON
 */
public class LootGameScene implements Scene {

    private static final int LEVEL_WIDTH = 6000;
    private static final int LEVEL_HEIGHT = 1080;
    private static final int GROUND_Y = 750;
    private static final int FLOOR_BLOCK_Y = 11; // Grid Y for floor blocks

    private boolean initialized = false;
    private EntityManager entityManager;
    private Camera camera;
    private SpritePlayerEntity player;

    // Chests
    private LootChestEntity dailyChest;
    private LootChestEntity monthlyChest;

    // Secret room door
    private DoorEntity secretRoomDoor;

    // Player Vault
    private VaultEntity playerVault;

    // UI
    private List<UIButton> buttons;

    // Background
    private Color skyColor = new Color(40, 50, 80);
    private Color horizonColor = new Color(80, 60, 100);

    // Stats display
    private int totalItemsCollected;
    private int legendaryItems;
    private int mythicItems;

    @Override
    public String getName() {
        return "Loot Game";
    }

    @Override
    public void init() {
        if (initialized) return;

        System.out.println("LootGameScene: Initializing...");

        entityManager = new EntityManager();
        buttons = new ArrayList<>();

        // Initialize item registry
        ItemRegistry.initialize();

        // Create camera
        camera = new Camera(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        camera.setLevelBounds(LEVEL_WIDTH, LEVEL_HEIGHT);
        camera.setSmoothSpeed(0.15);
        camera.setDeadZone(100, 50);

        // Build the level
        buildLevel();

        // Create UI
        createUI();

        // Load stats
        SaveManager save = SaveManager.getInstance();
        totalItemsCollected = save.getTotalItemsCollected();
        legendaryItems = save.getLegendaryItemsFound();
        mythicItems = save.getMythicItemsFound();

        initialized = true;
        System.out.println("LootGameScene: Initialized successfully!");
    }

    /**
     * Builds the loot game level with plank floor and chests.
     */
    private void buildLevel() {
        // Create floor using wood plank blocks
        int blocksAcross = LEVEL_WIDTH / BlockRegistry.BLOCK_SIZE + 1;
        for (int i = 0; i < blocksAcross; i++) {
            // Top layer - planks
            BlockEntity plank = new BlockEntity(i, FLOOR_BLOCK_Y, BlockType.WOOD, true);
            entityManager.addEntity(plank);

            // Second layer - dirt foundation
            BlockEntity dirt1 = new BlockEntity(i, FLOOR_BLOCK_Y + 1, BlockType.DIRT, true);
            entityManager.addEntity(dirt1);

            // Third layer
            BlockEntity dirt2 = new BlockEntity(i, FLOOR_BLOCK_Y + 2, BlockType.DIRT, true);
            entityManager.addEntity(dirt2);
        }

        // Create player in the center-left
        int playerX = LEVEL_WIDTH / 2 - 200;
        int playerY = GROUND_Y - 100;
        player = new SpritePlayerEntity(playerX, playerY, "assets/textures/sprite_player");
        player.setGroundY(GROUND_Y);
        player.setCamera(camera);
        AudioManager audio = SceneManager.getInstance().getAudioManager();
        if (audio != null) {
            player.setAudioManager(audio);
        }
        entityManager.addEntity(player);

        // Set camera to follow player
        camera.setTarget(player);
        camera.snapToTarget();

        // Create Daily Chest (center of level)
        int dailyChestX = LEVEL_WIDTH / 2 - 48;
        int dailyChestY = GROUND_Y - 72;
        dailyChest = new LootChestEntity(dailyChestX, dailyChestY,
            LootChestEntity.ChestType.DAILY, GROUND_Y);
        dailyChest.setEntityList(entityManager.getEntities());
        entityManager.addEntity(dailyChest);

        // Create Monthly Chest (right of center, larger)
        int monthlyChestX = LEVEL_WIDTH / 2 + 300;
        int monthlyChestY = GROUND_Y - 90; // Slightly bigger
        monthlyChest = new LootChestEntity(monthlyChestX, monthlyChestY,
            LootChestEntity.ChestType.MONTHLY, GROUND_Y);
        monthlyChest.setEntityList(entityManager.getEntities());
        entityManager.addEntity(monthlyChest);

        // Create Secret Room Door (on the far left)
        int doorX = 200;
        int doorY = GROUND_Y - 128;
        secretRoomDoor = new DoorEntity(doorX, doorY, 64, 128,
            "assets/doors/iron_door.gif", "secret_room_door");
        secretRoomDoor.setActionType(DoorEntity.ActionType.LEVEL_TRANSITION);
        secretRoomDoor.setActionTarget("levels/loot_game_room.json");
        entityManager.addEntity(secretRoomDoor);

        // Create Player Vault (to the left of daily chest)
        int vaultX = LEVEL_WIDTH / 2 - 200;
        int vaultY = GROUND_Y - 64;
        playerVault = VaultEntity.createPlayerVault(vaultX, vaultY);
        playerVault.setOnOpenCallback(() -> {
            // Open the vault UI when player opens the vault
            if (player != null && player.getInventory() != null) {
                player.getInventory().openVault();
            }
        });
        playerVault.setOnCloseCallback(() -> {
            // Close the vault UI when vault closes
            if (player != null && player.getInventory() != null) {
                player.getInventory().closeVault();
            }
        });
        entityManager.addEntity(playerVault);

        System.out.println("LootGameScene: Level built - " + blocksAcross + " blocks wide, with secret room door and vault");
    }

    /**
     * Creates the UI elements.
     */
    private void createUI() {
        // Back to Menu button
        UIButton menuButton = new UIButton(20, 20, 150, 50, "Back to Menu", () -> {
            SceneManager.getInstance().setScene("mainMenu", SceneManager.TRANSITION_FADE);
        });
        menuButton.setColors(
            new Color(50, 50, 70, 220),
            new Color(70, 70, 100, 240),
            Color.WHITE
        );
        buttons.add(menuButton);

        // Developer Mode toggle button
        UIButton devModeButton = new UIButton(20, 80, 150, 40, getDevModeButtonText(), () -> {
            SaveManager.getInstance().toggleDeveloperMode();
            // Reinitialize to apply changes
            initialized = false;
            init();
        });
        devModeButton.setColors(
            SaveManager.getInstance().isDeveloperMode() ? new Color(80, 150, 80, 220) : new Color(80, 80, 80, 220),
            SaveManager.getInstance().isDeveloperMode() ? new Color(100, 180, 100, 240) : new Color(100, 100, 100, 240),
            Color.WHITE
        );
        buttons.add(devModeButton);

        // Reset button (for testing)
        UIButton resetButton = new UIButton(GamePanel.SCREEN_WIDTH - 170, 20, 150, 50, "Reset Chests", () -> {
            SaveManager.getInstance().resetChestCooldowns();
            // Reinitialize the scene to reset chest states
            initialized = false;
            init();
        });
        resetButton.setColors(
            new Color(150, 50, 50, 220),
            new Color(200, 70, 70, 240),
            Color.WHITE
        );
        buttons.add(resetButton);
    }

    /**
     * Gets the developer mode button text based on current state.
     */
    private String getDevModeButtonText() {
        return "Dev Mode: " + (SaveManager.getInstance().isDeveloperMode() ? "ON" : "OFF");
    }

    @Override
    public void update(InputManager input) {
        if (!initialized) return;

        // Check chest and door interaction BEFORE updating entities to ensure 'E' key isn't consumed
        if (player != null) {
            Rectangle playerBounds = player.getBounds();

            // Update daily chest proximity
            if (dailyChest.isInInteractionZone(playerBounds)) {
                dailyChest.setPlayerNearby(true);
            } else {
                dailyChest.setPlayerNearby(false);
            }

            // Update monthly chest proximity
            if (monthlyChest.isInInteractionZone(playerBounds)) {
                monthlyChest.setPlayerNearby(true);
            } else {
                monthlyChest.setPlayerNearby(false);
            }

            // Update secret room door proximity
            if (secretRoomDoor != null && secretRoomDoor.isInInteractionZone(playerBounds)) {
                secretRoomDoor.setPlayerNearby(true);
            } else if (secretRoomDoor != null) {
                secretRoomDoor.setPlayerNearby(false);
            }

            // Update vault proximity
            if (playerVault != null) {
                Rectangle playerBoundsCheck = player.getBounds();
                playerVault.checkPlayerProximity(player.getX(), player.getY(),
                    playerBoundsCheck.width, playerBoundsCheck.height);
            }

            // Handle 'E' key press for chest/door/vault interaction
            if (input.isKeyJustPressed('e') || input.isKeyJustPressed('E')) {
                // Check if vault is already open - close it
                if (playerVault != null && playerVault.isOpen()) {
                    playerVault.close();
                }
                // Try vault first if player is nearby
                else if (playerVault != null && playerVault.isPlayerNearby()) {
                    playerVault.tryOpen();
                }
                // Try secret room door
                else if (secretRoomDoor != null && secretRoomDoor.isPlayerNearby()) {
                    secretRoomDoor.open();
                    // Execute door action (level transition)
                    if (secretRoomDoor.getActionType() == DoorEntity.ActionType.LEVEL_TRANSITION) {
                        String targetLevel = secretRoomDoor.getActionTarget();
                        if (targetLevel != null && !targetLevel.isEmpty()) {
                            SceneManager.getInstance().loadLevel(targetLevel, SceneManager.TRANSITION_FADE);
                        }
                    }
                }
                // Try to open whichever chest the player is near
                else if (!dailyChest.tryOpen()) {
                    monthlyChest.tryOpen();
                }
            }

            // Update vault entity
            if (playerVault != null) {
                playerVault.update(input);
            }

            // Collect dropped items when player touches them
            collectDroppedItems(dailyChest, playerBounds);
            collectDroppedItems(monthlyChest, playerBounds);
        }

        // Update all entities
        entityManager.updateAll(input);

        // Update camera
        if (camera != null) {
            camera.update();
        }

        // Update stats from save manager
        SaveManager save = SaveManager.getInstance();
        totalItemsCollected = save.getTotalItemsCollected();
        legendaryItems = save.getLegendaryItemsFound();
        mythicItems = save.getMythicItemsFound();

        // ESC to return to menu - transfer items to vault first
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            transferInventoryToVaultOnExit();
            SceneManager.getInstance().setScene("mainMenu", SceneManager.TRANSITION_FADE);
        }
    }

    /**
     * Transfers all items from the player's inventory to the vault when leaving.
     */
    private void transferInventoryToVaultOnExit() {
        if (player != null && player.getInventory() != null) {
            Inventory inventory = player.getInventory();
            int itemCount = inventory.getItemCount();
            if (itemCount > 0) {
                int overflow = inventory.transferAllToVault();
                if (overflow == 0) {
                    System.out.println("LootGameScene: Transferred " + itemCount + " items to vault");
                } else {
                    System.out.println("LootGameScene: Transferred items to vault with " + overflow + " overflow");
                }
            }
        }
    }

    /**
     * Collects dropped items from a chest when player touches them.
     */
    private void collectDroppedItems(LootChestEntity chest, Rectangle playerBounds) {
        for (ItemEntity item : chest.getDroppedItems()) {
            if (!item.isCollected() && item.isGrounded()) {
                if (playerBounds.intersects(item.getBounds())) {
                    // Add to player inventory
                    player.getInventory().addItem(item);
                    item.collected = true;

                    // Play collect sound
                    AudioManager audio = SceneManager.getInstance().getAudioManager();
                    if (audio != null) {
                        audio.playSound("collect");
                    }
                }
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!initialized) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw gradient sky background
        drawBackground(g2d);

        // Apply camera transform
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();
        camera.applyTransform(g2d);

        // Draw ground fill below blocks
        g2d.setColor(new Color(60, 40, 30));
        g2d.fillRect(0, GROUND_Y, LEVEL_WIDTH, LEVEL_HEIGHT - GROUND_Y);

        // Draw all entities
        entityManager.drawAll(g2d);

        // Restore transform
        g2d.setTransform(oldTransform);

        // Draw UI (screen space)
        drawUI(g2d);
    }

    /**
     * Draws the gradient background.
     */
    private void drawBackground(Graphics2D g) {
        // Create vertical gradient from sky to horizon
        GradientPaint gradient = new GradientPaint(
            0, 0, skyColor,
            0, LEVEL_HEIGHT / 2, horizonColor
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        // Draw decorative stars
        drawStars(g);

        // Draw title banner
        drawTitleBanner(g);
    }

    /**
     * Draws decorative background stars.
     */
    private void drawStars(Graphics2D g) {
        long time = System.currentTimeMillis();
        g.setColor(Color.WHITE);

        // Static stars with twinkle effect
        int[][] stars = {
            {100, 80}, {300, 150}, {500, 50}, {700, 180}, {900, 100},
            {1100, 60}, {1300, 140}, {1500, 90}, {1700, 170}, {1850, 120},
            {200, 200}, {400, 250}, {600, 220}, {800, 280}, {1000, 240},
            {1200, 260}, {1400, 210}, {1600, 290}, {1800, 230}
        };

        for (int i = 0; i < stars.length; i++) {
            float twinkle = (float)(Math.sin(time * 0.003 + i) * 0.3 + 0.7);
            int alpha = (int)(twinkle * 200);
            g.setColor(new Color(255, 255, 255, alpha));
            int size = 2 + (i % 3);
            g.fillOval(stars[i][0], stars[i][1], size, size);
        }
    }

    /**
     * Draws the title banner at the top.
     */
    private void drawTitleBanner(Graphics2D g) {
        // Title background
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(GamePanel.SCREEN_WIDTH / 2 - 200, 10, 400, 60, 15, 15);

        // Border glow
        float hue = (float)((System.currentTimeMillis() % 3000) / 3000.0);
        Color glowColor = Color.getHSBColor(hue, 0.7f, 1.0f);
        g.setColor(glowColor);
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(GamePanel.SCREEN_WIDTH / 2 - 200, 10, 400, 60, 15, 15);

        // Title text
        g.setFont(new Font("Arial", Font.BOLD, 36));
        String title = "LOOT GAME";
        FontMetrics fm = g.getFontMetrics();
        int textX = GamePanel.SCREEN_WIDTH / 2 - fm.stringWidth(title) / 2;

        // Text shadow
        g.setColor(Color.BLACK);
        g.drawString(title, textX + 2, 52);

        // Rainbow text effect
        for (int i = 0; i < title.length(); i++) {
            float charHue = (hue + i * 0.1f) % 1.0f;
            g.setColor(Color.getHSBColor(charHue, 0.8f, 1.0f));
            g.drawString(String.valueOf(title.charAt(i)),
                textX + fm.stringWidth(title.substring(0, i)), 50);
        }
    }

    /**
     * Draws the UI elements.
     */
    private void drawUI(Graphics2D g) {
        // Draw buttons
        for (UIButton button : buttons) {
            button.draw(g);
        }

        // Draw player inventory
        if (player != null) {
            player.getInventory().draw(g);
            // Draw vault inventory if open
            player.getInventory().drawVault(g);
        }

        // Draw stats panel
        drawStatsPanel(g);

        // Draw controls hint
        drawControlsHint(g);
    }

    /**
     * Draws the statistics panel.
     */
    private void drawStatsPanel(Graphics2D g) {
        int panelX = GamePanel.SCREEN_WIDTH - 250;
        int panelY = 80;
        int panelWidth = 230;
        int panelHeight = 120;

        // Panel background
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);
        g.setColor(new Color(255, 215, 0, 150));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);

        // Stats text
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(new Color(255, 215, 0));
        g.drawString("LOOT STATISTICS", panelX + 50, panelY + 22);

        g.setFont(new Font("Arial", Font.PLAIN, 13));
        g.setColor(Color.WHITE);
        g.drawString("Total Items: " + totalItemsCollected, panelX + 15, panelY + 45);

        g.setColor(Item.ItemRarity.LEGENDARY.getColor());
        g.drawString("Legendary: " + legendaryItems, panelX + 15, panelY + 65);

        g.setColor(Item.ItemRarity.MYTHIC.getColor());
        g.drawString("Mythic: " + mythicItems, panelX + 15, panelY + 85);

        // Chest status
        g.setColor(Color.GRAY);
        SaveManager save = SaveManager.getInstance();
        String dailyStatus = save.canOpenDailyChest() ? "READY!" : SaveManager.formatTimeRemaining(save.getDailyChestTimeRemaining());
        String monthlyStatus = save.canOpenMonthlyChest() ? "READY!" : SaveManager.formatTimeRemaining(save.getMonthlyChestTimeRemaining());

        g.setColor(save.canOpenDailyChest() ? new Color(100, 255, 100) : new Color(255, 100, 100));
        g.drawString("Daily: " + dailyStatus, panelX + 15, panelY + 105);

        g.setColor(save.canOpenMonthlyChest() ? new Color(100, 255, 100) : new Color(255, 100, 100));
        g.drawString("Monthly: " + monthlyStatus, panelX + 120, panelY + 105);
    }

    /**
     * Draws controls hint at the bottom.
     */
    private void drawControlsHint(Graphics2D g) {
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(255, 255, 255, 180));

        String controls = "A/D: Move | SPACE: Jump | E: Open Chest/Door | Walk to items to collect | ESC: Menu";
        FontMetrics fm = g.getFontMetrics();
        int textX = GamePanel.SCREEN_WIDTH / 2 - fm.stringWidth(controls) / 2;
        int textY = GamePanel.SCREEN_HEIGHT - 20;

        // Background
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(textX - 15, textY - 15, fm.stringWidth(controls) + 30, 25, 8, 8);

        // Text
        g.setColor(new Color(255, 255, 255, 200));
        g.drawString(controls, textX, textY);
    }

    @Override
    public void dispose() {
        initialized = false;
        entityManager = null;
        buttons = null;
        player = null;
        dailyChest = null;
        monthlyChest = null;
        secretRoomDoor = null;
        camera = null;
    }

    @Override
    public void onMousePressed(int x, int y) {
        if (player != null) {
            Inventory inventory = player.getInventory();

            // Check if pressing on vault inventory first (for drag start)
            if (inventory.isVaultOpen()) {
                VaultInventory vault = inventory.getVaultInventory();
                if (vault.containsPoint(x, y)) {
                    vault.handleMousePressed(x, y);
                    return;  // Press handled by vault
                }
            }

            // Handle inventory drag start
            inventory.handleMousePressed(x, y);
        }
    }

    @Override
    public void onMouseReleased(int x, int y) {
        if (player != null) {
            Inventory inventory = player.getInventory();

            // Check if releasing a vault drag
            if (inventory.isVaultOpen()) {
                VaultInventory vault = inventory.getVaultInventory();
                if (vault.isDragging()) {
                    vault.handleMouseReleased(x, y);
                    return;
                }
            }

            // Handle inventory drag release (may drop item)
            ItemEntity droppedItem = inventory.handleMouseReleased(x, y);
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
            Inventory inventory = player.getInventory();

            // Forward drag to vault if it's dragging
            if (inventory.isVaultOpen()) {
                VaultInventory vault = inventory.getVaultInventory();
                if (vault.isDragging()) {
                    vault.handleMouseDragged(x, y);
                    return;
                }
            }

            // Forward to inventory
            inventory.handleMouseDragged(x, y);
        }
    }

    @Override
    public void onMouseMoved(int x, int y) {
        // Update button hover states
        for (UIButton button : buttons) {
            button.handleMouseMove(x, y);
        }

        // Update vault inventory mouse position for tooltips
        if (player != null && player.getInventory().isVaultOpen()) {
            player.getInventory().updateVaultMousePosition(x, y);
        }
    }

    @Override
    public void onMouseClicked(int x, int y) {
        // Handle button clicks first
        for (UIButton button : buttons) {
            if (button.handleClick(x, y)) {
                return;  // Button handled the click
            }
        }

        if (player != null) {
            Inventory inventory = player.getInventory();

            // Check if clicking on vault inventory
            if (inventory.isVaultOpen() && inventory.handleVaultClick(x, y, false)) {
                return;  // Click handled by vault
            }

            // Check if clicking on player inventory
            if (inventory.isOpen() && inventory.handleLeftClick(x, y)) {
                return;  // Click handled by inventory
            }

            // Get camera offset for world coordinate translation
            int cameraOffsetX = camera != null ? (int) camera.getX() : 0;
            int cameraOffsetY = camera != null ? (int) camera.getY() : 0;

            // Check if clicking on vault (left-click to open/close)
            if (playerVault != null && playerVault.handleClick(x, y, cameraOffsetX, cameraOffsetY)) {
                return;  // Click handled by vault entity
            }

            // Check if clicking on daily chest (left-click to open)
            if (dailyChest != null && dailyChest.isPlayerNearby() &&
                dailyChest.handleClick(x, y, cameraOffsetX, cameraOffsetY)) {
                return;  // Click handled by daily chest
            }

            // Check if clicking on monthly chest (left-click to open)
            if (monthlyChest != null && monthlyChest.isPlayerNearby() &&
                monthlyChest.handleClick(x, y, cameraOffsetX, cameraOffsetY)) {
                return;  // Click handled by monthly chest
            }
        }
    }

    /**
     * Handles right-click events (for taking single items from vault).
     */
    public void onRightClick(int x, int y) {
        if (player != null) {
            Inventory inventory = player.getInventory();

            // Check if right-clicking on vault inventory (take single item)
            if (inventory.isVaultOpen() && inventory.handleVaultClick(x, y, true)) {
                return;  // Click handled by vault
            }
        }
    }
}
