package com.ambermoongame.scene;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;

import com.ambermoongame.audio.AndroidAudioManager;
import com.ambermoongame.block.BlockEntity;
import com.ambermoongame.block.BlockRegistry;
import com.ambermoongame.block.BlockType;
import com.ambermoongame.entity.Entity;
import com.ambermoongame.entity.EntityManager;
import com.ambermoongame.entity.item.AlchemyTableEntity;
import com.ambermoongame.entity.item.DoorEntity;
import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.item.ItemEntity;
import com.ambermoongame.entity.item.ItemRegistry;
import com.ambermoongame.entity.item.LootChestEntity;
import com.ambermoongame.entity.item.RecipeManager;
import com.ambermoongame.entity.item.VaultEntity;
import com.ambermoongame.entity.player.AbilityScores;
import com.ambermoongame.entity.player.PlayableCharacter;
import com.ambermoongame.entity.player.PlayableCharacterRegistry;
import com.ambermoongame.entity.player.SpritePlayerEntity;
import com.ambermoongame.graphics.Camera;
import com.ambermoongame.input.TouchInputManager;
import com.ambermoongame.ui.AlchemyTableUI;
import com.ambermoongame.ui.Inventory;
import com.ambermoongame.ui.VaultInventory;

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
 * - Player inventory with vault integration
 * - Touch controls for movement and interaction
 *
 * Android port conversion notes:
 * - Graphics2D → Canvas + Paint
 * - java.awt.Color → android.graphics.Color
 * - GradientPaint → LinearGradient (Shader)
 * - Rectangle → Rect
 * - Font/FontMetrics → Paint.setTextSize() + measureText()
 * - InputManager → TouchInputManager
 * - SceneManager → AndroidSceneManager
 */
public class LootGameScene extends BaseScene {

    private static final String TAG = "LootGameScene";

    private static final int LEVEL_WIDTH = 6000;
    private static final int LEVEL_HEIGHT = 1080;
    private static final int GROUND_Y = 750;
    private static final int FLOOR_BLOCK_Y = 11; // Grid Y for floor blocks

    private EntityManager entityManager;
    private Camera camera;
    private SpritePlayerEntity player;
    private Inventory inventory;

    // Chests
    private LootChestEntity dailyChest;
    private LootChestEntity monthlyChest;

    // Secret room door
    private DoorEntity secretRoomDoor;

    // Player Vault
    private VaultEntity playerVault;

    // Alchemy Tables
    private AlchemyTableEntity alchemyTable;
    private AlchemyTableEntity reverseCraftingTable;
    private AlchemyTableUI alchemyUI;
    // --- Uncomment when ReverseCraftingUI is ported ---
    // private ReverseCraftingUI reverseCraftingUI;

    // Background colors
    private int skyColor = Color.rgb(40, 50, 80);
    private int horizonColor = Color.rgb(80, 60, 100);
    private LinearGradient skyGradient;

    // Stats display
    private int totalItemsCollected = 0;
    private int legendaryItems = 0;
    private int mythicItems = 0;

    // Reusable drawing objects
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();

    // Screen dimensions
    private int screenWidth = AndroidSceneManager.SCREEN_WIDTH;
    private int screenHeight = AndroidSceneManager.SCREEN_HEIGHT;

    // Star positions for background
    private int[][] stars = {
        {100, 80}, {300, 150}, {500, 50}, {700, 180}, {900, 100},
        {1100, 60}, {1300, 140}, {1500, 90}, {1700, 170}, {1850, 120},
        {200, 200}, {400, 250}, {600, 220}, {800, 280}, {1000, 240},
        {1200, 260}, {1400, 210}, {1600, 290}, {1800, 230}
    };

    public LootGameScene() {
        super("LootGameScene");
        strokePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void init() {
        super.init();

        Log.d(TAG, "Initializing...");

        entityManager = new EntityManager();

        // Initialize item registry and recipe manager
        ItemRegistry.initialize();
        RecipeManager.initialize();

        // Create sky gradient
        skyGradient = new LinearGradient(
            0, 0, 0, LEVEL_HEIGHT / 2f,
            skyColor, horizonColor,
            Shader.TileMode.CLAMP
        );

        // Create camera
        camera = new Camera(screenWidth, screenHeight);
        camera.setLevelBounds(LEVEL_WIDTH, LEVEL_HEIGHT);
        camera.setSmoothSpeed(0.15);
        camera.setDeadZone(100, 50);

        // Build the level
        buildLevel();

        // Create inventory
        inventory = new Inventory();
        inventory.setScreenSize(screenWidth, screenHeight);

        // --- Uncomment when SaveManager is ported ---
        // SaveManager save = SaveManager.getInstance();
        // totalItemsCollected = save.getTotalItemsCollected();
        // legendaryItems = save.getLegendaryItemsFound();
        // mythicItems = save.getMythicItemsFound();

        Log.d(TAG, "Initialized successfully!");
    }

    /**
     * Builds the loot game level with plank floor and chests.
     */
    private void buildLevel() {
        // Create floor using wood plank blocks
        int blockSize = BlockRegistry.BLOCK_SIZE;
        int blocksAcross = LEVEL_WIDTH / blockSize + 1;

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

        // Create player in the center-left using Merlin character
        int playerX = LEVEL_WIDTH / 2 - 200;
        int playerY = GROUND_Y - 100;

        // Get Merlin character from registry (Loot Game uses Merlin with baseline stats)
        PlayableCharacter merlin = PlayableCharacterRegistry.getInstance().getMerlin();
        String spritePath = merlin != null ? merlin.getSpritePath() : "assets/player/sprites";

        player = new SpritePlayerEntity(playerX, playerY, spritePath);

        // Set Merlin's baseline ability scores (all 5)
        if (merlin != null) {
            player.setAbilityScores(merlin.getBaseAbilityScores());
        } else {
            // Fallback to baseline if Merlin not found
            player.setAbilityScores(new AbilityScores());
        }

        player.setGroundY(GROUND_Y);
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
        int monthlyChestY = GROUND_Y - 90;
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
            if (inventory != null) {
                inventory.openVault();
            }
        });
        playerVault.setOnCloseCallback(() -> {
            if (inventory != null) {
                inventory.closeVault();
            }
        });
        entityManager.addEntity(playerVault);

        // Create Alchemy Table (to the right of daily chest)
        int alchemyTableX = LEVEL_WIDTH / 2 + 100;
        int alchemyTableY = GROUND_Y - 64;
        alchemyTable = AlchemyTableEntity.createAlchemyTable(alchemyTableX, alchemyTableY);
        alchemyTable.setOnOpenCallback(() -> openAlchemyUI(false));
        alchemyTable.setOnCloseCallback(() -> closeAlchemyUI(false));
        entityManager.addEntity(alchemyTable);

        // Create Reverse Crafting Table (further right)
        int reverseCraftingX = LEVEL_WIDTH / 2 + 200;
        int reverseCraftingY = GROUND_Y - 64;
        reverseCraftingTable = AlchemyTableEntity.createReverseCraftingTable(reverseCraftingX, reverseCraftingY);
        reverseCraftingTable.setOnOpenCallback(() -> openAlchemyUI(true));
        reverseCraftingTable.setOnCloseCallback(() -> closeAlchemyUI(true));
        entityManager.addEntity(reverseCraftingTable);

        // Create Alchemy UIs
        alchemyUI = new AlchemyTableUI(false);
        alchemyUI.setScreenSize(screenWidth, screenHeight);
        alchemyUI.setItemProducedCallback((itemId, count) -> {
            if (inventory != null) {
                ItemEntity item = new ItemEntity(0, 0, itemId);
                item.setStackCount(count);
                Item linked = ItemRegistry.create(itemId);
                if (linked != null) {
                    item.setLinkedItem(linked);
                }
                inventory.addItemAtCursorSlot(item);
                Log.d(TAG, "Crafted " + count + "x " + itemId);
            }
        });

        // --- Uncomment when ReverseCraftingUI is ported ---
        // reverseCraftingUI = new ReverseCraftingUI();
        // reverseCraftingUI.setScreenSize(screenWidth, screenHeight);

        Log.d(TAG, "Level built - " + blocksAcross + " blocks wide, with vault and alchemy tables");
    }

    /**
     * Opens the alchemy UI.
     */
    private void openAlchemyUI(boolean reverseMode) {
        if (reverseMode) {
            // --- Uncomment when ReverseCraftingUI is ported ---
            // reverseCraftingUI.open(screenWidth / 2, screenHeight / 2);
        } else {
            alchemyUI.open(screenWidth / 2, screenHeight / 2);
        }
    }

    /**
     * Closes the alchemy UI.
     */
    private void closeAlchemyUI(boolean reverseMode) {
        if (reverseMode) {
            // --- Uncomment when ReverseCraftingUI is ported ---
            // reverseCraftingUI.close();
        } else {
            alchemyUI.close();
        }
    }

    @Override
    public void update(TouchInputManager input) {
        if (!initialized) return;

        // Check chest and door interaction
        if (player != null) {
            Rect playerBounds = player.getBounds();

            // Update daily chest proximity
            dailyChest.setPlayerNearby(dailyChest.isInInteractionZone(playerBounds));

            // Update monthly chest proximity
            monthlyChest.setPlayerNearby(monthlyChest.isInInteractionZone(playerBounds));

            // Update secret room door proximity
            if (secretRoomDoor != null) {
                secretRoomDoor.setPlayerNearby(secretRoomDoor.isInInteractionZone(playerBounds));
            }

            // Update vault proximity
            if (playerVault != null) {
                playerVault.checkPlayerProximity(player.getX(), player.getY(),
                    playerBounds.width(), playerBounds.height());
            }

            // Update alchemy table proximity
            if (alchemyTable != null) {
                alchemyTable.checkPlayerProximity(player.getX(), player.getY(),
                    playerBounds.width(), playerBounds.height());
            }
            if (reverseCraftingTable != null) {
                reverseCraftingTable.checkPlayerProximity(player.getX(), player.getY(),
                    playerBounds.width(), playerBounds.height());
            }

            // Handle 'E' key / interact button for chest/door/vault/alchemy interaction
            if (input.isKeyJustPressed('e')) {
                handleInteraction(input);
            }

            // Update player aim direction based on touch
            int touchX = input.getMouseX();
            int touchY = input.getMouseY();
            int worldX = camera.screenToWorldX(touchX);
            int worldY = camera.screenToWorldY(touchY);
            player.setAimTarget(worldX, worldY);

            // Collect dropped items when player touches them
            collectDroppedItems(dailyChest, playerBounds);
            collectDroppedItems(monthlyChest, playerBounds);
        }

        // Update player with full entity list
        if (player != null) {
            player.update(input, entityManager.getEntities());
        }

        // Update all entities
        entityManager.updateAll(input);

        // Update camera
        if (camera != null) {
            camera.update();
        }

        // Update alchemy UI
        if (alchemyUI != null && alchemyUI.isOpen()) {
            alchemyUI.update(input.getMouseX(), input.getMouseY());
        }
    }

    /**
     * Handles interaction with nearby objects.
     */
    private void handleInteraction(TouchInputManager input) {
        // Check if any alchemy UI is open - close it
        if (alchemyUI != null && alchemyUI.isOpen()) {
            alchemyTable.close();
        }
        // --- Uncomment when ReverseCraftingUI is ported ---
        // else if (reverseCraftingUI != null && reverseCraftingUI.isOpen()) {
        //     reverseCraftingTable.close();
        // }
        // Check if vault is already open - close it
        else if (playerVault != null && playerVault.isOpen()) {
            playerVault.close();
        }
        // Try alchemy table if player is nearby
        else if (alchemyTable != null && alchemyTable.isPlayerNearby()) {
            alchemyTable.tryOpen();
        }
        // Try reverse crafting table if player is nearby
        else if (reverseCraftingTable != null && reverseCraftingTable.isPlayerNearby()) {
            reverseCraftingTable.tryOpen();
        }
        // Try vault if player is nearby
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
                    AndroidSceneManager.getInstance().loadLevel(targetLevel, AndroidSceneManager.TRANSITION_FADE);
                }
            }
        }
        // Try to open whichever chest the player is near
        else if (!dailyChest.tryOpen()) {
            monthlyChest.tryOpen();
        }
    }

    /**
     * Collects dropped items from a chest when player touches them.
     */
    private void collectDroppedItems(LootChestEntity chest, Rect playerBounds) {
        for (ItemEntity item : chest.getDroppedItems()) {
            if (!item.isCollected() && item.isGrounded()) {
                if (Rect.intersects(playerBounds, item.getBounds())) {
                    // Add to player inventory
                    if (inventory != null) {
                        inventory.addItem(item);
                    }
                    item.collect();

                    // Update stats based on rarity
                    if (item.getLinkedItem() != null) {
                        totalItemsCollected++;
                        Item.ItemRarity rarity = item.getLinkedItem().getRarity();
                        if (rarity == Item.ItemRarity.LEGENDARY) {
                            legendaryItems++;
                        } else if (rarity == Item.ItemRarity.MYTHIC) {
                            mythicItems++;
                        }
                    }

                    // Play collect sound
                    AndroidAudioManager audio = AndroidSceneManager.getInstance().getAudioManager();
                    if (audio != null) {
                        audio.playSound("collect");
                    }
                }
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (!initialized) return;

        // Draw gradient sky background
        drawBackground(canvas);

        // Save canvas state before camera transform
        canvas.save();

        // Apply camera transform
        camera.applyTransform(canvas);

        // Draw ground fill below blocks
        fillPaint.setColor(Color.rgb(60, 40, 30));
        canvas.drawRect(0, GROUND_Y, LEVEL_WIDTH, LEVEL_HEIGHT, fillPaint);

        // Draw all entities
        entityManager.drawAll(canvas);

        // Restore canvas state (remove camera transform)
        canvas.restore();

        // Draw UI (screen space)
        drawUI(canvas);
    }

    /**
     * Draws the gradient background.
     */
    private void drawBackground(Canvas canvas) {
        // Draw sky gradient
        fillPaint.setShader(skyGradient);
        canvas.drawRect(0, 0, screenWidth, screenHeight, fillPaint);
        fillPaint.setShader(null);

        // Draw decorative stars
        drawStars(canvas);

        // Draw title banner
        drawTitleBanner(canvas);
    }

    /**
     * Draws decorative background stars.
     */
    private void drawStars(Canvas canvas) {
        long time = System.currentTimeMillis();

        for (int i = 0; i < stars.length; i++) {
            float twinkle = (float)(Math.sin(time * 0.003 + i) * 0.3 + 0.7);
            int alpha = (int)(twinkle * 200);
            fillPaint.setColor(Color.argb(alpha, 255, 255, 255));
            int size = 2 + (i % 3);
            canvas.drawCircle(stars[i][0], stars[i][1], size / 2f, fillPaint);
        }
    }

    /**
     * Draws the title banner at the top.
     */
    private void drawTitleBanner(Canvas canvas) {
        // Title background
        fillPaint.setColor(Color.argb(150, 0, 0, 0));
        rectF.set(screenWidth / 2f - 200, 10, screenWidth / 2f + 200, 70);
        canvas.drawRoundRect(rectF, 15, 15, fillPaint);

        // Border glow (cycling hue)
        float hue = (float)((System.currentTimeMillis() % 3000) / 3000.0) * 360f;
        int glowColor = Color.HSVToColor(new float[]{hue, 0.7f, 1.0f});
        strokePaint.setColor(glowColor);
        strokePaint.setStrokeWidth(3);
        canvas.drawRoundRect(rectF, 15, 15, strokePaint);

        // Title text with rainbow effect
        String title = "LOOT GAME";
        textPaint.setTextSize(36);
        textPaint.setFakeBoldText(true);
        float textWidth = textPaint.measureText(title);
        float textX = screenWidth / 2f - textWidth / 2f;
        float textY = 52;

        // Text shadow
        textPaint.setColor(Color.BLACK);
        canvas.drawText(title, textX + 2, textY + 2, textPaint);

        // Rainbow text effect
        float charX = textX;
        for (int i = 0; i < title.length(); i++) {
            float charHue = (hue + i * 36f) % 360f;
            textPaint.setColor(Color.HSVToColor(new float[]{charHue, 0.8f, 1.0f}));
            String ch = String.valueOf(title.charAt(i));
            canvas.drawText(ch, charX, textY, textPaint);
            charX += textPaint.measureText(ch);
        }
    }

    /**
     * Draws the UI elements.
     */
    private void drawUI(Canvas canvas) {
        // Draw player inventory
        if (inventory != null) {
            inventory.draw(canvas);
            // Draw vault inventory if open
            inventory.drawVault(canvas);
            // Draw dragged item overlays on top of all UI
            inventory.drawAllDraggedItemOverlays(canvas);
        }

        // Draw alchemy UI
        if (alchemyUI != null && alchemyUI.isOpen()) {
            alchemyUI.draw(canvas);
        }
        // --- Uncomment when ReverseCraftingUI is ported ---
        // if (reverseCraftingUI != null && reverseCraftingUI.isOpen()) {
        //     reverseCraftingUI.draw(canvas);
        // }

        // Draw stats panel
        drawStatsPanel(canvas);

        // Draw controls hint
        drawControlsHint(canvas);

        // Draw back button hint
        drawBackButton(canvas);
    }

    /**
     * Draws the statistics panel.
     */
    private void drawStatsPanel(Canvas canvas) {
        int panelX = screenWidth - 250;
        int panelY = 80;
        int panelWidth = 230;
        int panelHeight = 120;

        // Panel background
        fillPaint.setColor(Color.argb(180, 0, 0, 0));
        rectF.set(panelX, panelY, panelX + panelWidth, panelY + panelHeight);
        canvas.drawRoundRect(rectF, 10, 10, fillPaint);

        // Gold border
        strokePaint.setColor(Color.argb(150, 255, 215, 0));
        strokePaint.setStrokeWidth(2);
        canvas.drawRoundRect(rectF, 10, 10, strokePaint);

        // Stats text
        textPaint.setTextSize(14);
        textPaint.setFakeBoldText(true);
        textPaint.setColor(Color.rgb(255, 215, 0));
        canvas.drawText("LOOT STATISTICS", panelX + 50, panelY + 22, textPaint);

        textPaint.setFakeBoldText(false);
        textPaint.setTextSize(13);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("Total Items: " + totalItemsCollected, panelX + 15, panelY + 45, textPaint);

        textPaint.setColor(Item.ItemRarity.LEGENDARY.getColor());
        canvas.drawText("Legendary: " + legendaryItems, panelX + 15, panelY + 65, textPaint);

        textPaint.setColor(Item.ItemRarity.MYTHIC.getColor());
        canvas.drawText("Mythic: " + mythicItems, panelX + 15, panelY + 85, textPaint);

        // Chest status
        // --- Uncomment when SaveManager is ported ---
        // SaveManager save = SaveManager.getInstance();
        // String dailyStatus = save.canOpenDailyChest() ? "READY!" : SaveManager.formatTimeRemaining(save.getDailyChestTimeRemaining());
        // String monthlyStatus = save.canOpenMonthlyChest() ? "READY!" : SaveManager.formatTimeRemaining(save.getMonthlyChestTimeRemaining());
        String dailyStatus = dailyChest.canOpen() ? "READY!" : "Cooldown";
        String monthlyStatus = monthlyChest.canOpen() ? "READY!" : "Cooldown";

        textPaint.setColor(dailyChest.canOpen() ? Color.rgb(100, 255, 100) : Color.rgb(255, 100, 100));
        canvas.drawText("Daily: " + dailyStatus, panelX + 15, panelY + 105, textPaint);

        textPaint.setColor(monthlyChest.canOpen() ? Color.rgb(100, 255, 100) : Color.rgb(255, 100, 100));
        canvas.drawText("Monthly: " + monthlyStatus, panelX + 120, panelY + 105, textPaint);
    }

    /**
     * Draws controls hint at the bottom.
     */
    private void drawControlsHint(Canvas canvas) {
        String controls = "D-Pad: Move | Jump: Jump | E: Interact | Walk to items to collect";
        textPaint.setTextSize(14);
        textPaint.setFakeBoldText(false);
        float textWidth = textPaint.measureText(controls);
        float textX = screenWidth / 2f - textWidth / 2f;
        float textY = screenHeight - 20;

        // Background
        fillPaint.setColor(Color.argb(150, 0, 0, 0));
        rectF.set(textX - 15, textY - 15, textX + textWidth + 15, textY + 7);
        canvas.drawRoundRect(rectF, 8, 8, fillPaint);

        // Text
        textPaint.setColor(Color.argb(200, 255, 255, 255));
        canvas.drawText(controls, textX, textY, textPaint);
    }

    /**
     * Draws the back button in top-left corner.
     */
    private void drawBackButton(Canvas canvas) {
        // Button background
        fillPaint.setColor(Color.argb(220, 50, 50, 70));
        rectF.set(20, 20, 170, 70);
        canvas.drawRoundRect(rectF, 10, 10, fillPaint);

        // Button text
        textPaint.setTextSize(18);
        textPaint.setFakeBoldText(true);
        textPaint.setColor(Color.WHITE);
        canvas.drawText("< Back", 50, 52, textPaint);
    }

    @Override
    public void onTouchPressed(int x, int y) {
        // Check back button
        if (x >= 20 && x <= 170 && y >= 20 && y <= 70) {
            AndroidSceneManager.getInstance().setScene("mainMenu", AndroidSceneManager.TRANSITION_FADE);
            return;
        }

        // Check alchemy UI
        if (alchemyUI != null && alchemyUI.isOpen() && alchemyUI.containsPoint(x, y)) {
            alchemyUI.handleMousePressed(x, y);
            return;
        }

        // Handle inventory drag start
        if (inventory != null) {
            if (inventory.isVaultOpen()) {
                VaultInventory vault = inventory.getVaultInventory();
                if (vault != null && vault.containsPoint(x, y)) {
                    vault.handleMousePressed(x, y);
                    return;
                }
            }
            inventory.handleMousePressed(x, y);
        }

        // Check chest clicks (in world space)
        if (camera != null) {
            int worldX = camera.screenToWorldX(x);
            int worldY = camera.screenToWorldY(y);

            if (dailyChest.isPlayerNearby()) {
                dailyChest.handleClick(x, y, (int)camera.getX(), (int)camera.getY());
            }
            if (monthlyChest.isPlayerNearby()) {
                monthlyChest.handleClick(x, y, (int)camera.getX(), (int)camera.getY());
            }
        }
    }

    @Override
    public void onTouchReleased(int x, int y) {
        // Handle alchemy UI release
        if (alchemyUI != null && alchemyUI.isOpen() && alchemyUI.isDragging()) {
            ItemEntity droppedItem = alchemyUI.handleMouseReleased(x, y);
            if (droppedItem != null && inventory != null) {
                inventory.addItem(droppedItem);
            }
            return;
        }

        // Handle inventory drag release
        if (inventory != null) {
            if (inventory.isVaultOpen()) {
                VaultInventory vault = inventory.getVaultInventory();
                if (vault != null && vault.isDragging()) {
                    vault.handleMouseReleased(x, y);
                    return;
                }
            }

            // Check if releasing on alchemy UI
            if (inventory.isDragging() && alchemyUI != null && alchemyUI.isOpen() && alchemyUI.containsPoint(x, y)) {
                ItemEntity draggedItem = inventory.getDraggedItem();
                if (draggedItem != null) {
                    String itemId = draggedItem.getItemId();
                    if (itemId == null || itemId.isEmpty()) {
                        itemId = ItemRegistry.findIdByName(draggedItem.getItemName());
                    }
                    if (itemId != null && alchemyUI.addItem(itemId, draggedItem.getStackCount())) {
                        inventory.consumeDraggedItem();
                        return;
                    }
                }
            }

            ItemEntity droppedItem = inventory.handleMouseReleased(x, y);
            if (droppedItem != null && player != null) {
                // Drop item into world
                droppedItem.setCollected(false);
                int dropX = player.getX() + (player.isFacingRight() ? player.getWidth() + 20 : -40);
                droppedItem.setPosition(dropX, player.getY() + player.getHeight() / 2);
                droppedItem.enablePhysics(player.isFacingRight() ? 3 : -3, -5, GROUND_Y);
                droppedItem.setEntityList(entityManager.getEntities());
                entityManager.addEntity(droppedItem);
            }
        }
    }

    @Override
    public void onTouchMoved(int x, int y) {
        // Forward drag to alchemy UI
        if (alchemyUI != null && alchemyUI.isOpen() && alchemyUI.isDragging()) {
            alchemyUI.handleMouseDragged(x, y);
            return;
        }

        // Update inventory mouse position
        if (inventory != null) {
            inventory.updateMousePosition(x, y);
            if (inventory.isVaultOpen()) {
                inventory.updateVaultMousePosition(x, y);
                VaultInventory vault = inventory.getVaultInventory();
                if (vault != null && vault.isDragging()) {
                    vault.handleMouseDragged(x, y);
                    return;
                }
            }
            inventory.handleMouseDragged(x, y);
        }
    }

    @Override
    public boolean onBackPressed() {
        // Transfer inventory to vault on exit
        // --- Uncomment when vault transfer is implemented ---
        // if (inventory != null) {
        //     int itemCount = inventory.getItemCount();
        //     if (itemCount > 0) {
        //         inventory.transferAllToVault();
        //     }
        // }
        AndroidSceneManager.getInstance().setScene("mainMenu", AndroidSceneManager.TRANSITION_FADE);
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();
        entityManager = null;
        camera = null;
        player = null;
        inventory = null;
        dailyChest = null;
        monthlyChest = null;
        secretRoomDoor = null;
        playerVault = null;
        alchemyTable = null;
        reverseCraftingTable = null;
        alchemyUI = null;
    }
}
