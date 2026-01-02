package scene;

import core.*;
import entity.*;
import entity.player.*;
import entity.mob.*;
import block.*;
import graphics.*;
import level.*;
import input.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Creative Mode Scene - A level editor for creating and testing game levels.
 *
 * Features:
 * - Drag and drop placement of blocks, items, and mobs
 * - Grid snapping for blocks (64x64 grid)
 * - Free placement for items and mobs
 * - Play/Edit mode toggle for testing
 * - Save levels to JSON files
 * - Level properties configuration
 *
 * Controls:
 * - Left click: Place selected entity
 * - Right click: Remove entity at cursor
 * - WASD/Arrow keys: Pan camera
 * - P: Toggle play mode
 * - S: Save level
 * - E: Edit mode (return from play)
 * - Escape: Return to main menu
 * - Tab: Cycle palette categories
 * - Mouse wheel: Zoom (when implemented)
 */
public class CreativeScene implements Scene {

    // Scene state
    private boolean initialized = false;
    private boolean isPlayMode = false;

    // Level data
    private LevelData levelData;
    private String currentLevelPath;

    // Camera
    private Camera camera;
    private float cameraX = 0;
    private float cameraY = 0;
    private static final float CAMERA_SPEED = 10f;

    // Entity management for edit mode
    private List<PlacedEntity> placedBlocks;
    private List<PlacedEntity> placedItems;
    private List<PlacedEntity> placedMobs;
    private List<PlacedEntity> placedLights;

    // For play mode
    private GameScene gameScene;

    // Palette
    private PaletteCategory currentCategory = PaletteCategory.BLOCKS;
    private int selectedPaletteIndex = 0;
    private int paletteScrollOffset = 0;
    private static final int PALETTE_WIDTH = 200;
    private static final int PALETTE_ITEM_SIZE = 48;
    private static final int PALETTE_ITEMS_PER_ROW = 3;

    // Grid
    private static final int GRID_SIZE = BlockRegistry.BLOCK_SIZE; // 64 pixels
    private boolean showGrid = true;

    // Mouse state
    private int mouseX, mouseY;
    private int worldMouseX, worldMouseY;
    private boolean isDragging = false;
    private PlacedEntity draggedEntity = null;
    private PlacedEntity hoveredEntity = null;

    // UI state
    private boolean showPropertiesDialog = true;
    private boolean showSaveDialog = false;
    private String statusMessage = "";
    private long statusMessageTime = 0;

    // Modal dialog state (replaces JOptionPane to avoid threading issues)
    private ModalState modalState = ModalState.NONE;
    private String modalInputText = "";
    private boolean saveAfterExit = false; // Flag for exit confirmation flow

    /**
     * Modal dialog states for in-game dialogs
     */
    private enum ModalState {
        NONE,           // No modal open
        CONFIRM_EXIT,   // Confirm exit dialog (Yes/No/Cancel)
        SAVE_FILENAME,  // Enter filename dialog
        LOAD_LEVEL,     // Load level dialog
        NEW_LEVEL       // New level properties dialog
    }

    // Available creative levels for loading
    private List<String> availableLevels;
    private int selectedLevelIndex = 0;

    // Block textures cache
    private Map<BlockType, BufferedImage> blockTextures;

    // Palette data
    private List<PaletteItem> blockPalette;
    private List<PaletteItem> itemPalette;
    private List<PaletteItem> mobPalette;
    private List<PaletteItem> lightPalette;
    private List<PaletteItem> parallaxPalette;

    // Parallax layers currently in use
    private List<ParallaxLayerEntry> parallaxLayers;

    /**
     * Categories in the palette
     */
    public enum PaletteCategory {
        BLOCKS("Blocks"),
        ITEMS("Items"),
        MOBS("Mobs"),
        LIGHTS("Lights"),
        PARALLAX("Parallax");

        private final String displayName;

        PaletteCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Represents an item in the palette
     */
    private static class PaletteItem {
        String id;
        String displayName;
        BufferedImage icon;
        Object data; // BlockType, String itemId, or mob data

        PaletteItem(String id, String displayName, BufferedImage icon, Object data) {
            this.id = id;
            this.displayName = displayName;
            this.icon = icon;
            this.data = data;
        }
    }

    /**
     * Represents an entity placed in the level
     */
    private static class PlacedEntity {
        int x, y;
        int gridX, gridY; // For blocks
        String type; // "block", "item", "mob", "light"
        Object data; // Type-specific data
        BufferedImage icon;

        PlacedEntity(int x, int y, String type, Object data, BufferedImage icon) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.data = data;
            this.icon = icon;
        }

        Rectangle getBounds() {
            int size = type.equals("block") ? GRID_SIZE : 32;
            return new Rectangle(x, y, size, size);
        }
    }

    /**
     * Represents a parallax layer in the level
     */
    private static class ParallaxLayerEntry {
        String name;
        String imagePath;
        double scrollSpeedX;
        int zOrder;
        double scale;
        double opacity;
        BufferedImage icon;

        ParallaxLayerEntry(String name, String imagePath, double scrollSpeedX, int zOrder, BufferedImage icon) {
            this.name = name;
            this.imagePath = imagePath;
            this.scrollSpeedX = scrollSpeedX;
            this.zOrder = zOrder;
            this.scale = 10.0;  // Scale up to cover screen (matches LevelData default)
            this.opacity = 1.0;
            this.icon = icon;
        }
    }

    @Override
    public void init() {
        if (initialized) return;

        // Initialize lists
        placedBlocks = new ArrayList<>();
        placedItems = new ArrayList<>();
        placedMobs = new ArrayList<>();
        placedLights = new ArrayList<>();
        parallaxLayers = new ArrayList<>();
        blockTextures = new HashMap<>();

        // Initialize palettes
        initializePalettes();

        // Create default level data
        levelData = new LevelData();
        levelData.name = "New Creative Level";
        levelData.levelWidth = 3840; // 60 blocks wide
        levelData.levelHeight = 1080; // 17 blocks tall
        levelData.groundY = 920;
        levelData.playerSpawnX = 200;
        levelData.playerSpawnY = 850;
        levelData.scrollingEnabled = true;
        levelData.verticalScrollEnabled = true;
        levelData.tileBackgroundHorizontal = true;

        // Use the proper sprite animation player (not demo player)
        levelData.useSpriteAnimation = true;
        levelData.spriteAnimationDir = "assets/player/sprites";
        levelData.useBoneAnimation = false;

        // Initialize camera
        camera = new Camera(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        camera.setLevelBounds(levelData.levelWidth, levelData.levelHeight);
        camera.setSmoothSpeed(1.0); // Instant response for editing

        // Preload block textures
        BlockRegistry.getInstance().preloadAllTextures();
        for (BlockType type : BlockType.values()) {
            blockTextures.put(type, BlockRegistry.getInstance().getTexture(type));
        }

        initialized = true;
        setStatus("Creative Mode - Press Tab to change category, P to play, S to save");
    }

    /**
     * Initialize the palette with available blocks, items, and mobs
     */
    private void initializePalettes() {
        // Block palette
        blockPalette = new ArrayList<>();
        for (BlockType type : BlockType.values()) {
            BufferedImage icon = BlockRegistry.getInstance().getTexture(type);
            if (icon != null) {
                // Scale down for palette
                BufferedImage scaledIcon = scaleImage(icon, PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE);
                blockPalette.add(new PaletteItem(type.name(), type.getDisplayName(), scaledIcon, type));
            }
        }

        // Item palette - common items from ItemRegistry
        itemPalette = new ArrayList<>();
        String[] commonItems = {
            "wooden_sword", "iron_sword", "steel_sword", "fire_sword", "ice_sword",
            "wooden_bow", "longbow", "crossbow", "magic_wand", "fire_staff",
            "health_potion", "mana_potion", "greater_health_potion",
            "arrow", "fire_arrow", "ice_arrow", "bolt",
            "throwing_knife", "throwing_axe", "bomb",
            "apple", "bread", "cooked_meat", "cake",
            "gold_coins", "iron_ore", "leather"
        };

        for (String itemId : commonItems) {
            BufferedImage icon = createItemIcon(itemId);
            String displayName = itemId.replace("_", " ");
            displayName = Character.toUpperCase(displayName.charAt(0)) + displayName.substring(1);
            itemPalette.add(new PaletteItem(itemId, displayName, icon, itemId));
        }

        // Mob palette
        mobPalette = new ArrayList<>();
        String[][] mobTypes = {
            {"zombie", "Zombie", "sprite_humanoid"},
            {"skeleton", "Skeleton", "sprite_humanoid"},
            {"goblin", "Goblin", "sprite_humanoid"},
            {"orc", "Orc", "sprite_humanoid"},
            {"bandit", "Bandit", "sprite_humanoid"},
            {"knight", "Knight", "sprite_humanoid"},
            {"mage", "Mage", "sprite_humanoid"},
            {"wolf", "Wolf", "sprite_quadruped"},
            {"bear", "Bear", "sprite_quadruped"},
            {"pig", "Pig", "sprite_quadruped"},
            {"cow", "Cow", "sprite_quadruped"},
            {"sheep", "Sheep", "sprite_quadruped"}
        };

        for (String[] mob : mobTypes) {
            BufferedImage icon = createMobIcon(mob[0]);
            Map<String, String> mobData = new HashMap<>();
            mobData.put("subType", mob[0]);
            mobData.put("mobType", mob[2]);
            mobData.put("behavior", mob[0].equals("pig") || mob[0].equals("cow") || mob[0].equals("sheep") ? "passive" : "hostile");
            mobPalette.add(new PaletteItem(mob[0], mob[1], icon, mobData));
        }

        // Light palette
        lightPalette = new ArrayList<>();
        String[] lightTypes = {"torch", "campfire", "lantern", "magic", "crystal"};
        for (String lightType : lightTypes) {
            BufferedImage icon = createLightIcon(lightType);
            String displayName = Character.toUpperCase(lightType.charAt(0)) + lightType.substring(1);
            lightPalette.add(new PaletteItem(lightType, displayName, icon, lightType));
        }

        // Parallax palette - available background/foreground layers
        parallaxPalette = new ArrayList<>();
        String[][] parallaxOptions = {
            {"sky", "Sky Background", "assets/parallax/sky.png", "-2", "0.1"},
            {"buildings_far", "Distant Buildings", "assets/parallax/buildings_far.png", "-1", "0.3"},
            {"buildings_mid", "Mid Buildings", "assets/parallax/buildings_mid.png", "0", "0.5"},
            {"buildings_near", "Near Buildings", "assets/parallax/buildings_near.png", "1", "0.7"},
            {"foreground", "Foreground", "assets/parallax/foreground.png", "2", "1.2"},
            {"background", "Main Background", "assets/background.png", "-2", "0.2"}
        };

        for (String[] option : parallaxOptions) {
            BufferedImage icon = createParallaxIcon(option[2]);
            Map<String, String> data = new HashMap<>();
            data.put("name", option[0]);
            data.put("path", option[2]);
            data.put("zOrder", option[3]);
            data.put("scrollSpeed", option[4]);
            parallaxPalette.add(new PaletteItem(option[0], option[1], icon, data));
        }
    }

    /**
     * Scale an image to specified dimensions
     */
    private BufferedImage scaleImage(BufferedImage source, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return scaled;
    }

    /**
     * Create an icon for items - tries to load actual sprite first, falls back to placeholder
     */
    private BufferedImage createItemIcon(String itemId) {
        // Try to load actual item sprite from assets
        String[] extensions = {".gif", ".png"};
        for (String ext : extensions) {
            String path = "assets/items/" + itemId + ext;
            AssetLoader.ImageAsset asset = AssetLoader.load(path);
            if (asset != null && asset.staticImage != null) {
                return scaleImage(asset.staticImage, PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE);
            }
        }

        // Fallback to placeholder icon
        BufferedImage icon = new BufferedImage(PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Color based on item type
        Color color = Color.GRAY;
        if (itemId.contains("sword") || itemId.contains("axe") || itemId.contains("knife")) {
            color = new Color(150, 150, 180); // Steel
        } else if (itemId.contains("bow") || itemId.contains("crossbow")) {
            color = new Color(139, 90, 43); // Brown
        } else if (itemId.contains("staff") || itemId.contains("wand") || itemId.contains("magic")) {
            color = new Color(138, 43, 226); // Purple
        } else if (itemId.contains("potion")) {
            color = itemId.contains("health") ? new Color(255, 50, 50) : new Color(50, 50, 255);
        } else if (itemId.contains("arrow") || itemId.contains("bolt")) {
            color = new Color(139, 119, 101); // Tan
        } else if (itemId.contains("food") || itemId.contains("apple") || itemId.contains("bread") ||
                   itemId.contains("meat") || itemId.contains("cake")) {
            color = new Color(210, 180, 140); // Food tan
        } else if (itemId.contains("gold")) {
            color = new Color(255, 215, 0); // Gold
        } else if (itemId.contains("fire")) {
            color = new Color(255, 100, 0); // Orange
        } else if (itemId.contains("ice")) {
            color = new Color(100, 200, 255); // Ice blue
        }

        g.setColor(color);
        g.fillRoundRect(4, 4, PALETTE_ITEM_SIZE - 8, PALETTE_ITEM_SIZE - 8, 8, 8);
        g.setColor(Color.BLACK);
        g.drawRoundRect(4, 4, PALETTE_ITEM_SIZE - 8, PALETTE_ITEM_SIZE - 8, 8, 8);

        // Draw first letter
        g.setFont(new Font("Arial", Font.BOLD, 16));
        String letter = itemId.substring(0, 1).toUpperCase();
        FontMetrics fm = g.getFontMetrics();
        int textX = (PALETTE_ITEM_SIZE - fm.stringWidth(letter)) / 2;
        int textY = (PALETTE_ITEM_SIZE + fm.getAscent() - fm.getDescent()) / 2;
        g.setColor(Color.WHITE);
        g.drawString(letter, textX, textY);

        g.dispose();
        return icon;
    }

    /**
     * Create an icon for mobs - tries to load actual sprite first, falls back to placeholder
     */
    private BufferedImage createMobIcon(String mobType) {
        // Try to load actual mob sprite from assets - check multiple paths
        String[] paths = {
            "assets/mobs/" + mobType + "/idle.gif",
            "assets/mobs/" + mobType + "/sprites/idle.gif",
            "assets/mobs/" + mobType + "/idle.png"
        };

        for (String path : paths) {
            AssetLoader.ImageAsset asset = AssetLoader.load(path);
            if (asset != null && asset.staticImage != null) {
                return scaleImage(asset.staticImage, PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE);
            }
        }

        // Fallback to placeholder icon
        BufferedImage icon = new BufferedImage(PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Color based on mob type
        Color color;
        switch (mobType) {
            case "zombie": color = new Color(100, 150, 100); break;
            case "skeleton": color = new Color(220, 220, 200); break;
            case "goblin": color = new Color(50, 150, 50); break;
            case "orc": color = new Color(100, 120, 80); break;
            case "bandit": color = new Color(120, 80, 60); break;
            case "knight": color = new Color(180, 180, 200); break;
            case "mage": color = new Color(100, 50, 150); break;
            case "wolf": color = new Color(100, 100, 100); break;
            case "bear": color = new Color(139, 90, 43); break;
            case "pig": color = new Color(255, 180, 180); break;
            case "cow": color = new Color(100, 80, 60); break;
            case "sheep": color = new Color(240, 240, 240); break;
            default: color = Color.GRAY;
        }

        // Draw creature silhouette
        g.setColor(color);
        if (mobType.equals("wolf") || mobType.equals("bear") || mobType.equals("pig") ||
            mobType.equals("cow") || mobType.equals("sheep")) {
            // Quadruped - oval body
            g.fillOval(6, 14, PALETTE_ITEM_SIZE - 12, PALETTE_ITEM_SIZE - 22);
            g.fillOval(8, 8, 16, 14); // Head
        } else {
            // Humanoid - stick figure
            g.fillOval(16, 4, 16, 16); // Head
            g.fillRect(20, 20, 8, 16); // Body
            g.fillRect(14, 36, 6, 8); // Left leg
            g.fillRect(28, 36, 6, 8); // Right leg
        }

        g.setColor(Color.BLACK);
        g.drawOval(16, 4, 16, 16);

        g.dispose();
        return icon;
    }

    /**
     * Create a placeholder icon for lights
     */
    private BufferedImage createLightIcon(String lightType) {
        BufferedImage icon = new BufferedImage(PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw light glow
        Color glowColor;
        switch (lightType) {
            case "torch": glowColor = new Color(255, 200, 100); break;
            case "campfire": glowColor = new Color(255, 150, 50); break;
            case "lantern": glowColor = new Color(255, 240, 180); break;
            case "magic": glowColor = new Color(150, 150, 255); break;
            case "crystal": glowColor = new Color(100, 255, 200); break;
            default: glowColor = Color.YELLOW;
        }

        // Outer glow
        g.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 50));
        g.fillOval(4, 4, PALETTE_ITEM_SIZE - 8, PALETTE_ITEM_SIZE - 8);

        // Inner glow
        g.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 150));
        g.fillOval(12, 12, PALETTE_ITEM_SIZE - 24, PALETTE_ITEM_SIZE - 24);

        // Center
        g.setColor(glowColor);
        g.fillOval(18, 18, 12, 12);

        g.dispose();
        return icon;
    }

    /**
     * Create an icon for parallax layers - loads and scales down the actual image
     */
    private BufferedImage createParallaxIcon(String imagePath) {
        // Try to load the actual parallax image
        AssetLoader.ImageAsset asset = AssetLoader.load(imagePath);
        if (asset != null && asset.staticImage != null) {
            return scaleImage(asset.staticImage, PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE);
        }

        // Fallback to a gradient placeholder
        BufferedImage icon = new BufferedImage(PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Create a gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(100, 150, 200),
            0, PALETTE_ITEM_SIZE, new Color(50, 80, 120)
        );
        g.setPaint(gradient);
        g.fillRoundRect(2, 2, PALETTE_ITEM_SIZE - 4, PALETTE_ITEM_SIZE - 4, 6, 6);

        // Draw layer lines to indicate parallax
        g.setColor(new Color(255, 255, 255, 100));
        for (int i = 10; i < PALETTE_ITEM_SIZE - 10; i += 8) {
            g.drawLine(4, i, PALETTE_ITEM_SIZE - 4, i);
        }

        g.setColor(Color.BLACK);
        g.drawRoundRect(2, 2, PALETTE_ITEM_SIZE - 4, PALETTE_ITEM_SIZE - 4, 6, 6);

        g.dispose();
        return icon;
    }

    @Override
    public void update(InputManager input) {
        if (!initialized) init();

        if (isPlayMode && gameScene != null) {
            // In play mode, update the game scene
            gameScene.update(input);

            // Check for E key to return to edit mode
            if (input.isKeyJustPressed(KeyEvent.VK_E)) {
                exitPlayMode();
            }
            return;
        }

        // Camera movement
        if (input.isKeyPressed(KeyEvent.VK_W) || input.isKeyPressed(KeyEvent.VK_UP)) {
            cameraY -= CAMERA_SPEED;
        }
        if (input.isKeyPressed(KeyEvent.VK_S) || input.isKeyPressed(KeyEvent.VK_DOWN)) {
            cameraY += CAMERA_SPEED;
        }
        if (input.isKeyPressed(KeyEvent.VK_A) || input.isKeyPressed(KeyEvent.VK_LEFT)) {
            cameraX -= CAMERA_SPEED;
        }
        if (input.isKeyPressed(KeyEvent.VK_D) || input.isKeyPressed(KeyEvent.VK_RIGHT)) {
            cameraX += CAMERA_SPEED;
        }

        // Clamp camera to level bounds
        cameraX = Math.max(0, Math.min(cameraX, levelData.levelWidth - GamePanel.SCREEN_WIDTH));
        cameraY = Math.max(0, Math.min(cameraY, levelData.levelHeight - GamePanel.SCREEN_HEIGHT));

        camera.setPosition((int) cameraX, (int) cameraY);

        // Tab to cycle categories
        if (input.isKeyJustPressed(KeyEvent.VK_TAB)) {
            PaletteCategory[] categories = PaletteCategory.values();
            int nextIndex = (currentCategory.ordinal() + 1) % categories.length;
            currentCategory = categories[nextIndex];
            selectedPaletteIndex = 0;
            paletteScrollOffset = 0;
            setStatus("Category: " + currentCategory.getDisplayName());
        }

        // P to toggle play mode
        if (input.isKeyJustPressed(KeyEvent.VK_P)) {
            enterPlayMode();
        }

        // G to toggle grid
        if (input.isKeyJustPressed(KeyEvent.VK_G)) {
            showGrid = !showGrid;
            setStatus("Grid: " + (showGrid ? "ON" : "OFF"));
        }

        // Ctrl+S to save
        if (input.isKeyPressed(KeyEvent.VK_CONTROL) && input.isKeyJustPressed(KeyEvent.VK_S)) {
            saveLevel();
        }

        // Ctrl+L or L to load level
        if (input.isKeyJustPressed(KeyEvent.VK_L)) {
            openLoadDialog();
        }

        // Escape to return to menu (show confirmation dialog)
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            if (modalState == ModalState.NONE) {
                modalState = ModalState.CONFIRM_EXIT;
            } else {
                // Escape closes any open modal
                modalState = ModalState.NONE;
                modalInputText = "";
            }
        }

        // Handle modal dialog input
        if (modalState != ModalState.NONE) {
            handleModalInput(input);
            return; // Don't process other input while modal is open
        }

        // Update world mouse position
        worldMouseX = mouseX + (int) cameraX;
        worldMouseY = mouseY + (int) cameraY;

        // Right-click to delete entity at cursor
        if (input.isRightMouseJustPressed() && mouseX >= PALETTE_WIDTH) {
            removeEntityAt(worldMouseX, worldMouseY);
        }

        // Left-click to place (already handled in onMouseClicked, but also handle here for immediate feedback)
        if (input.isLeftMouseJustPressed() && mouseX >= PALETTE_WIDTH) {
            placeEntity();
        }

        // Update hovered entity
        updateHoveredEntity();
    }

    /**
     * Handle input for modal dialogs
     */
    private void handleModalInput(InputManager input) {
        switch (modalState) {
            case CONFIRM_EXIT:
                // Y = Yes (save and exit), N = No (exit without save), Escape = Cancel
                if (input.isKeyJustPressed(KeyEvent.VK_Y)) {
                    modalState = ModalState.NONE;
                    saveAfterExit = true;
                    // Trigger save flow, which will exit after save completes
                    modalState = ModalState.SAVE_FILENAME;
                    modalInputText = "";
                } else if (input.isKeyJustPressed(KeyEvent.VK_N)) {
                    modalState = ModalState.NONE;
                    SceneManager.getInstance().setScene("mainMenu", SceneManager.TRANSITION_FADE);
                }
                // Escape already handled above, closes the modal
                break;

            case SAVE_FILENAME:
                // Handle text input for filename
                // Check for Enter to confirm
                if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
                    String filename = modalInputText.trim();
                    modalState = ModalState.NONE;
                    if (!filename.isEmpty()) {
                        performSave(filename);
                        // If we were saving before exit, now exit
                        if (saveAfterExit) {
                            saveAfterExit = false;
                            SceneManager.getInstance().setScene("mainMenu", SceneManager.TRANSITION_FADE);
                        }
                    } else {
                        setStatus("Save cancelled - no filename entered");
                        saveAfterExit = false;
                    }
                }
                // Backspace to delete
                else if (input.isKeyJustPressed(KeyEvent.VK_BACK_SPACE)) {
                    if (modalInputText.length() > 0) {
                        modalInputText = modalInputText.substring(0, modalInputText.length() - 1);
                    }
                }
                // Handle regular character input
                else {
                    // Check for typed characters (a-z, 0-9, underscore, hyphen)
                    for (char c = 'a'; c <= 'z'; c++) {
                        if (input.isKeyJustPressed(c)) {
                            if (modalInputText.length() < 30) {
                                modalInputText += c;
                            }
                        }
                    }
                    for (char c = '0'; c <= '9'; c++) {
                        if (input.isKeyJustPressed(c)) {
                            if (modalInputText.length() < 30) {
                                modalInputText += c;
                            }
                        }
                    }
                    if (input.isKeyJustPressed(KeyEvent.VK_MINUS)) {
                        if (modalInputText.length() < 30) {
                            modalInputText += "-";
                        }
                    }
                    if (input.isKeyJustPressed(KeyEvent.VK_UNDERSCORE) ||
                        (input.isKeyPressed(KeyEvent.VK_SHIFT) && input.isKeyJustPressed(KeyEvent.VK_MINUS))) {
                        if (modalInputText.length() < 30) {
                            modalInputText += "_";
                        }
                    }
                }
                break;

            case LOAD_LEVEL:
                // Up/Down to navigate, Enter to load, Escape to cancel
                if (availableLevels != null && !availableLevels.isEmpty()) {
                    if (input.isKeyJustPressed(KeyEvent.VK_UP)) {
                        selectedLevelIndex = Math.max(0, selectedLevelIndex - 1);
                    } else if (input.isKeyJustPressed(KeyEvent.VK_DOWN)) {
                        selectedLevelIndex = Math.min(availableLevels.size() - 1, selectedLevelIndex + 1);
                    } else if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
                        String selectedPath = availableLevels.get(selectedLevelIndex);
                        modalState = ModalState.NONE;
                        loadLevel(selectedPath);
                    }
                }
                break;

            case NEW_LEVEL:
                // For now, just close on Escape (already handled)
                break;

            default:
                break;
        }
    }

    /**
     * Open the load level dialog
     */
    private void openLoadDialog() {
        // Scan for available creative levels
        availableLevels = new ArrayList<>();
        File levelsDir = new File("levels");
        if (levelsDir.exists() && levelsDir.isDirectory()) {
            File[] files = levelsDir.listFiles((dir, name) ->
                name.startsWith("creative_") && name.endsWith(".json"));
            if (files != null) {
                for (File f : files) {
                    availableLevels.add(f.getPath());
                }
            }
            // Also add other levels for reference
            files = levelsDir.listFiles((dir, name) ->
                name.endsWith(".json") && !name.startsWith("creative_"));
            if (files != null) {
                for (File f : files) {
                    availableLevels.add(f.getPath());
                }
            }
        }

        if (availableLevels.isEmpty()) {
            setStatus("No levels found to load");
            return;
        }

        selectedLevelIndex = 0;
        modalState = ModalState.LOAD_LEVEL;
    }

    /**
     * Update which entity the mouse is hovering over
     */
    private void updateHoveredEntity() {
        hoveredEntity = null;

        // Don't check if over palette
        if (mouseX < PALETTE_WIDTH) return;

        // Check blocks
        for (PlacedEntity entity : placedBlocks) {
            if (entity.getBounds().contains(worldMouseX, worldMouseY)) {
                hoveredEntity = entity;
                return;
            }
        }

        // Check items
        for (PlacedEntity entity : placedItems) {
            if (entity.getBounds().contains(worldMouseX, worldMouseY)) {
                hoveredEntity = entity;
                return;
            }
        }

        // Check mobs
        for (PlacedEntity entity : placedMobs) {
            if (entity.getBounds().contains(worldMouseX, worldMouseY)) {
                hoveredEntity = entity;
                return;
            }
        }

        // Check lights
        for (PlacedEntity entity : placedLights) {
            if (entity.getBounds().contains(worldMouseX, worldMouseY)) {
                hoveredEntity = entity;
                return;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (isPlayMode && gameScene != null) {
            // In play mode, draw the game scene
            gameScene.draw(g);

            // Draw play mode indicator
            g2.setColor(new Color(0, 255, 0, 100));
            g2.fillRect(GamePanel.SCREEN_WIDTH - 150, 10, 140, 30);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString("PLAY MODE (E=Edit)", GamePanel.SCREEN_WIDTH - 145, 30);
            return;
        }

        // Clear background
        g2.setColor(new Color(40, 44, 52));
        g2.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        // Draw grid
        if (showGrid) {
            drawGrid(g2);
        }

        // Draw placed entities
        drawPlacedEntities(g2);

        // Draw cursor preview
        drawCursorPreview(g2);

        // Draw palette
        drawPalette(g2);

        // Draw toolbar
        drawToolbar(g2);

        // Draw status message
        drawStatusMessage(g2);

        // Draw level info
        drawLevelInfo(g2);

        // Draw modal dialogs on top of everything
        if (modalState != ModalState.NONE) {
            drawModalDialog(g2);
        }
    }

    /**
     * Draw modal dialog overlay
     */
    private void drawModalDialog(Graphics2D g) {
        // Dim the background
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        // Dialog box dimensions
        int dialogWidth = 500;
        int dialogHeight = 200;
        int dialogX = (GamePanel.SCREEN_WIDTH - dialogWidth) / 2;
        int dialogY = (GamePanel.SCREEN_HEIGHT - dialogHeight) / 2;

        // Dialog background
        g.setColor(new Color(50, 54, 62));
        g.fillRoundRect(dialogX, dialogY, dialogWidth, dialogHeight, 15, 15);

        // Dialog border
        g.setColor(new Color(100, 104, 112));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(dialogX, dialogY, dialogWidth, dialogHeight, 15, 15);
        g.setStroke(new BasicStroke(1));

        g.setColor(Color.WHITE);

        switch (modalState) {
            case CONFIRM_EXIT:
                // Title
                g.setFont(new Font("Arial", Font.BOLD, 24));
                String title = "Exit Creative Mode?";
                FontMetrics fm = g.getFontMetrics();
                int titleX = dialogX + (dialogWidth - fm.stringWidth(title)) / 2;
                g.drawString(title, titleX, dialogY + 50);

                // Message
                g.setFont(new Font("Arial", Font.PLAIN, 16));
                String msg = "Do you want to save before exiting?";
                fm = g.getFontMetrics();
                int msgX = dialogX + (dialogWidth - fm.stringWidth(msg)) / 2;
                g.drawString(msg, msgX, dialogY + 90);

                // Buttons hint
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.setColor(new Color(100, 200, 100));
                g.drawString("[Y] Yes - Save & Exit", dialogX + 50, dialogY + 140);
                g.setColor(new Color(200, 100, 100));
                g.drawString("[N] No - Exit without saving", dialogX + 50, dialogY + 165);
                g.setColor(new Color(150, 150, 200));
                g.drawString("[Esc] Cancel", dialogX + 320, dialogY + 165);
                break;

            case SAVE_FILENAME:
                // Title
                g.setFont(new Font("Arial", Font.BOLD, 24));
                title = "Save Level";
                fm = g.getFontMetrics();
                titleX = dialogX + (dialogWidth - fm.stringWidth(title)) / 2;
                g.drawString(title, titleX, dialogY + 50);

                // Instructions
                g.setFont(new Font("Arial", Font.PLAIN, 14));
                g.drawString("Enter filename (letters, numbers, - and _ only):", dialogX + 30, dialogY + 85);

                // Text input box
                g.setColor(new Color(30, 34, 42));
                g.fillRoundRect(dialogX + 30, dialogY + 100, dialogWidth - 60, 40, 5, 5);
                g.setColor(new Color(80, 130, 180));
                g.drawRoundRect(dialogX + 30, dialogY + 100, dialogWidth - 60, 40, 5, 5);

                // Input text with cursor
                g.setColor(Color.WHITE);
                g.setFont(new Font("Monospaced", Font.PLAIN, 18));
                String displayText = modalInputText;
                // Blinking cursor
                if ((System.currentTimeMillis() / 500) % 2 == 0) {
                    displayText += "|";
                }
                g.drawString(displayText, dialogX + 40, dialogY + 128);

                // Hint
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.setColor(new Color(150, 150, 150));
                g.drawString("File will be saved as: levels/creative_" +
                    (modalInputText.isEmpty() ? "<filename>" : modalInputText) + ".json",
                    dialogX + 30, dialogY + 165);

                // Buttons hint
                g.setColor(new Color(100, 200, 100));
                g.drawString("[Enter] Save", dialogX + 30, dialogY + 185);
                g.setColor(new Color(150, 150, 200));
                g.drawString("[Esc] Cancel", dialogX + 120, dialogY + 185);
                break;

            case LOAD_LEVEL:
                // Title
                g.setFont(new Font("Arial", Font.BOLD, 24));
                title = "Load Level";
                fm = g.getFontMetrics();
                titleX = dialogX + (dialogWidth - fm.stringWidth(title)) / 2;
                g.drawString(title, titleX, dialogY + 40);

                // Make dialog taller for list
                int loadDialogHeight = 300;
                g.setColor(new Color(50, 54, 62));
                g.fillRoundRect(dialogX, dialogY, dialogWidth, loadDialogHeight, 15, 15);
                g.setColor(new Color(100, 104, 112));
                g.drawRoundRect(dialogX, dialogY, dialogWidth, loadDialogHeight, 15, 15);

                // Redraw title
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString(title, titleX, dialogY + 40);

                // List background
                g.setColor(new Color(30, 34, 42));
                g.fillRoundRect(dialogX + 20, dialogY + 55, dialogWidth - 40, 190, 5, 5);

                // Draw level list
                g.setFont(new Font("Monospaced", Font.PLAIN, 14));
                if (availableLevels != null) {
                    int listY = dialogY + 75;
                    int maxVisible = 8;
                    int startIdx = Math.max(0, selectedLevelIndex - maxVisible / 2);
                    startIdx = Math.min(startIdx, Math.max(0, availableLevels.size() - maxVisible));

                    for (int i = startIdx; i < Math.min(startIdx + maxVisible, availableLevels.size()); i++) {
                        String levelPath = availableLevels.get(i);
                        String displayName = new File(levelPath).getName();

                        if (i == selectedLevelIndex) {
                            g.setColor(new Color(70, 130, 180));
                            g.fillRoundRect(dialogX + 25, listY - 14, dialogWidth - 50, 20, 3, 3);
                            g.setColor(Color.WHITE);
                        } else {
                            g.setColor(new Color(180, 180, 180));
                        }
                        g.drawString(displayName, dialogX + 30, listY);
                        listY += 22;
                    }
                }

                // Controls hint
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.setColor(new Color(100, 200, 100));
                g.drawString("[Up/Down] Select", dialogX + 30, dialogY + 265);
                g.drawString("[Enter] Load", dialogX + 150, dialogY + 265);
                g.setColor(new Color(150, 150, 200));
                g.drawString("[Esc] Cancel", dialogX + 250, dialogY + 265);
                break;

            default:
                break;
        }
    }

    /**
     * Draw the grid overlay
     */
    private void drawGrid(Graphics2D g) {
        g.setColor(new Color(100, 100, 100, 50));

        // Calculate grid offset to align with world coordinates
        // Grid lines should appear at screen positions where world coordinate is multiple of GRID_SIZE
        int gridOffsetX = -((int) cameraX % GRID_SIZE);
        int gridOffsetY = -((int) cameraY % GRID_SIZE);

        // Adjust for negative camera positions
        if (gridOffsetX > 0) gridOffsetX -= GRID_SIZE;
        if (gridOffsetY > 0) gridOffsetY -= GRID_SIZE;

        // Find first grid line that appears after the palette
        int startX = gridOffsetX;
        while (startX < PALETTE_WIDTH) {
            startX += GRID_SIZE;
        }

        // Vertical lines - aligned with world grid
        for (int x = startX; x < GamePanel.SCREEN_WIDTH; x += GRID_SIZE) {
            g.drawLine(x, 0, x, GamePanel.SCREEN_HEIGHT);
        }

        // Horizontal lines
        int startY = gridOffsetY;
        while (startY < 0) {
            startY += GRID_SIZE;
        }
        for (int y = startY; y < GamePanel.SCREEN_HEIGHT; y += GRID_SIZE) {
            g.drawLine(PALETTE_WIDTH, y, GamePanel.SCREEN_WIDTH, y);
        }

        // Draw ground line
        int groundScreenY = levelData.groundY - (int) cameraY;
        if (groundScreenY >= 0 && groundScreenY < GamePanel.SCREEN_HEIGHT) {
            g.setColor(new Color(255, 100, 100, 100));
            g.setStroke(new BasicStroke(2));
            g.drawLine(PALETTE_WIDTH, groundScreenY, GamePanel.SCREEN_WIDTH, groundScreenY);
            g.setStroke(new BasicStroke(1));
        }
    }

    /**
     * Draw all placed entities
     */
    private void drawPlacedEntities(Graphics2D g) {
        // Draw blocks
        for (PlacedEntity entity : placedBlocks) {
            int screenX = entity.x - (int) cameraX;
            int screenY = entity.y - (int) cameraY;

            if (screenX + GRID_SIZE > PALETTE_WIDTH && screenX < GamePanel.SCREEN_WIDTH &&
                screenY + GRID_SIZE > 0 && screenY < GamePanel.SCREEN_HEIGHT) {

                if (entity.icon != null) {
                    g.drawImage(entity.icon, screenX, screenY, GRID_SIZE, GRID_SIZE, null);
                }

                // Highlight if hovered
                if (entity == hoveredEntity) {
                    g.setColor(new Color(255, 255, 0, 100));
                    g.fillRect(screenX, screenY, GRID_SIZE, GRID_SIZE);
                }
            }
        }

        // Draw items
        for (PlacedEntity entity : placedItems) {
            int screenX = entity.x - (int) cameraX;
            int screenY = entity.y - (int) cameraY;

            if (screenX + 32 > PALETTE_WIDTH && screenX < GamePanel.SCREEN_WIDTH &&
                screenY + 32 > 0 && screenY < GamePanel.SCREEN_HEIGHT) {

                if (entity.icon != null) {
                    g.drawImage(entity.icon, screenX, screenY, 32, 32, null);
                }

                if (entity == hoveredEntity) {
                    g.setColor(new Color(255, 255, 0, 100));
                    g.fillRect(screenX, screenY, 32, 32);
                }
            }
        }

        // Draw mobs
        for (PlacedEntity entity : placedMobs) {
            int screenX = entity.x - (int) cameraX;
            int screenY = entity.y - (int) cameraY;

            if (screenX + 48 > PALETTE_WIDTH && screenX < GamePanel.SCREEN_WIDTH &&
                screenY + 48 > 0 && screenY < GamePanel.SCREEN_HEIGHT) {

                if (entity.icon != null) {
                    g.drawImage(entity.icon, screenX, screenY, 48, 48, null);
                }

                if (entity == hoveredEntity) {
                    g.setColor(new Color(255, 255, 0, 100));
                    g.fillRect(screenX, screenY, 48, 48);
                }
            }
        }

        // Draw lights
        for (PlacedEntity entity : placedLights) {
            int screenX = entity.x - (int) cameraX;
            int screenY = entity.y - (int) cameraY;

            if (screenX + 32 > PALETTE_WIDTH && screenX < GamePanel.SCREEN_WIDTH &&
                screenY + 32 > 0 && screenY < GamePanel.SCREEN_HEIGHT) {

                if (entity.icon != null) {
                    g.drawImage(entity.icon, screenX, screenY, 32, 32, null);
                }

                if (entity == hoveredEntity) {
                    g.setColor(new Color(255, 255, 0, 100));
                    g.fillRect(screenX, screenY, 32, 32);
                }
            }
        }
    }

    /**
     * Draw preview of selected item at cursor
     */
    private void drawCursorPreview(Graphics2D g) {
        if (mouseX < PALETTE_WIDTH) return;

        PaletteItem selected = getSelectedPaletteItem();
        if (selected == null) return;

        int previewX, previewY;
        int size;

        if (currentCategory == PaletteCategory.BLOCKS) {
            // Snap to grid
            int gridX = (worldMouseX / GRID_SIZE) * GRID_SIZE;
            int gridY = (worldMouseY / GRID_SIZE) * GRID_SIZE;
            previewX = gridX - (int) cameraX;
            previewY = gridY - (int) cameraY;
            size = GRID_SIZE;
        } else {
            previewX = mouseX - 16;
            previewY = mouseY - 16;
            size = currentCategory == PaletteCategory.MOBS ? 48 : 32;
        }

        // Draw semi-transparent preview
        if (selected.icon != null) {
            Composite oldComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.drawImage(selected.icon, previewX, previewY, size, size, null);
            g.setComposite(oldComposite);
        }
    }

    /**
     * Draw the palette panel
     */
    private void drawPalette(Graphics2D g) {
        // Background
        g.setColor(new Color(30, 34, 42));
        g.fillRect(0, 0, PALETTE_WIDTH, GamePanel.SCREEN_HEIGHT);

        // Border
        g.setColor(new Color(60, 64, 72));
        g.drawLine(PALETTE_WIDTH, 0, PALETTE_WIDTH, GamePanel.SCREEN_HEIGHT);

        // Category tabs
        int tabY = 10;
        int tabHeight = 30;
        for (PaletteCategory cat : PaletteCategory.values()) {
            boolean isSelected = cat == currentCategory;

            g.setColor(isSelected ? new Color(70, 130, 180) : new Color(50, 54, 62));
            g.fillRoundRect(10, tabY, PALETTE_WIDTH - 20, tabHeight, 5, 5);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", isSelected ? Font.BOLD : Font.PLAIN, 12));
            FontMetrics fm = g.getFontMetrics();
            int textX = (PALETTE_WIDTH - fm.stringWidth(cat.getDisplayName())) / 2;
            g.drawString(cat.getDisplayName(), textX, tabY + 20);

            tabY += tabHeight + 5;
        }

        // Draw palette items
        List<PaletteItem> currentPalette = getCurrentPalette();
        int itemY = tabY + 20;
        int itemX = 10;
        int col = 0;

        for (int i = 0; i < currentPalette.size(); i++) {
            PaletteItem item = currentPalette.get(i);
            boolean isSelected = i == selectedPaletteIndex;

            // Background
            g.setColor(isSelected ? new Color(70, 130, 180) : new Color(50, 54, 62));
            g.fillRoundRect(itemX, itemY, PALETTE_ITEM_SIZE + 8, PALETTE_ITEM_SIZE + 8, 5, 5);

            // Icon
            if (item.icon != null) {
                g.drawImage(item.icon, itemX + 4, itemY + 4, PALETTE_ITEM_SIZE, PALETTE_ITEM_SIZE, null);
            }

            // For parallax items, show checkmark if layer is active
            if (currentCategory == PaletteCategory.PARALLAX) {
                boolean isActive = false;
                for (ParallaxLayerEntry layer : parallaxLayers) {
                    if (layer.name.equals(item.id)) {
                        isActive = true;
                        break;
                    }
                }
                if (isActive) {
                    // Draw green checkmark overlay
                    g.setColor(new Color(0, 255, 0, 180));
                    g.fillOval(itemX + PALETTE_ITEM_SIZE - 4, itemY + 4, 12, 12);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 10));
                    g.drawString("✓", itemX + PALETTE_ITEM_SIZE - 1, itemY + 13);
                }
            }

            // Selection border
            if (isSelected) {
                g.setColor(Color.WHITE);
                g.drawRoundRect(itemX, itemY, PALETTE_ITEM_SIZE + 8, PALETTE_ITEM_SIZE + 8, 5, 5);
            }

            col++;
            itemX += PALETTE_ITEM_SIZE + 12;

            if (col >= PALETTE_ITEMS_PER_ROW) {
                col = 0;
                itemX = 10;
                itemY += PALETTE_ITEM_SIZE + 12;
            }
        }

        // Draw selected item name
        if (selectedPaletteIndex < currentPalette.size()) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 11));
            String name = currentPalette.get(selectedPaletteIndex).displayName;
            g.drawString(name, 10, GamePanel.SCREEN_HEIGHT - 40);
        }
    }

    /**
     * Draw the toolbar at the top
     */
    private void drawToolbar(Graphics2D g) {
        // Background
        g.setColor(new Color(40, 44, 52, 200));
        g.fillRect(PALETTE_WIDTH, 0, GamePanel.SCREEN_WIDTH - PALETTE_WIDTH, 40);

        // Controls help
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.drawString("WASD: Pan | LClick: Place | RClick: Delete | Tab: Category | G: Grid | P: Play | Ctrl+S: Save | L: Load | Esc: Menu",
            PALETTE_WIDTH + 10, 25);
    }

    /**
     * Draw the status message
     */
    private void drawStatusMessage(Graphics2D g) {
        if (statusMessage.isEmpty()) return;

        long elapsed = System.currentTimeMillis() - statusMessageTime;
        if (elapsed > 3000) {
            statusMessage = "";
            return;
        }

        // Fade out
        float alpha = Math.min(1f, (3000 - elapsed) / 1000f);

        g.setColor(new Color(0, 0, 0, (int)(200 * alpha)));
        g.fillRoundRect(PALETTE_WIDTH + 10, GamePanel.SCREEN_HEIGHT - 50, 400, 30, 10, 10);

        g.setColor(new Color(255, 255, 255, (int)(255 * alpha)));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString(statusMessage, PALETTE_WIDTH + 20, GamePanel.SCREEN_HEIGHT - 30);
    }

    /**
     * Draw level information
     */
    private void drawLevelInfo(Graphics2D g) {
        g.setColor(new Color(40, 44, 52, 200));
        g.fillRect(GamePanel.SCREEN_WIDTH - 200, 50, 190, 120);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 11));

        int y = 70;
        g.drawString("Level: " + levelData.name, GamePanel.SCREEN_WIDTH - 190, y);
        y += 18;
        g.drawString("Size: " + levelData.levelWidth + " x " + levelData.levelHeight, GamePanel.SCREEN_WIDTH - 190, y);
        y += 18;
        g.drawString("Blocks: " + placedBlocks.size(), GamePanel.SCREEN_WIDTH - 190, y);
        y += 18;
        g.drawString("Items: " + placedItems.size() + " Mobs: " + placedMobs.size(), GamePanel.SCREEN_WIDTH - 190, y);
        y += 18;
        g.drawString("Parallax: " + parallaxLayers.size() + " layers", GamePanel.SCREEN_WIDTH - 190, y);
        y += 18;
        g.drawString("Pos: " + (int)cameraX + ", " + (int)cameraY, GamePanel.SCREEN_WIDTH - 190, y);
    }

    /**
     * Get the currently active palette
     */
    private List<PaletteItem> getCurrentPalette() {
        switch (currentCategory) {
            case BLOCKS: return blockPalette;
            case ITEMS: return itemPalette;
            case MOBS: return mobPalette;
            case LIGHTS: return lightPalette;
            case PARALLAX: return parallaxPalette;
            default: return blockPalette;
        }
    }

    /**
     * Get the currently selected palette item
     */
    private PaletteItem getSelectedPaletteItem() {
        List<PaletteItem> palette = getCurrentPalette();
        if (selectedPaletteIndex >= 0 && selectedPaletteIndex < palette.size()) {
            return palette.get(selectedPaletteIndex);
        }
        return null;
    }

    /**
     * Set a status message to display
     */
    private void setStatus(String message) {
        this.statusMessage = message;
        this.statusMessageTime = System.currentTimeMillis();
    }

    @Override
    public void onMousePressed(int x, int y) {
        if (isPlayMode && gameScene != null) {
            gameScene.onMousePressed(x, y);
            return;
        }

        mouseX = x;
        mouseY = y;

        // Check palette clicks
        if (x < PALETTE_WIDTH) {
            handlePaletteClick(x, y);
            return;
        }

        isDragging = true;
    }

    @Override
    public void onMouseReleased(int x, int y) {
        if (isPlayMode && gameScene != null) {
            gameScene.onMouseReleased(x, y);
            return;
        }

        mouseX = x;
        mouseY = y;
        isDragging = false;
        draggedEntity = null;
    }

    @Override
    public void onMouseDragged(int x, int y) {
        if (isPlayMode && gameScene != null) {
            gameScene.onMouseDragged(x, y);
            return;
        }

        mouseX = x;
        mouseY = y;
        worldMouseX = x + (int) cameraX;
        worldMouseY = y + (int) cameraY;

        // Continuous placement while dragging (for blocks)
        if (isDragging && x >= PALETTE_WIDTH && currentCategory == PaletteCategory.BLOCKS) {
            placeEntity();
        }
    }

    @Override
    public void onMouseMoved(int x, int y) {
        if (isPlayMode && gameScene != null) {
            gameScene.onMouseMoved(x, y);
            return;
        }

        mouseX = x;
        mouseY = y;
        worldMouseX = x + (int) cameraX;
        worldMouseY = y + (int) cameraY;
    }

    @Override
    public void onMouseClicked(int x, int y) {
        if (isPlayMode && gameScene != null) {
            gameScene.onMouseClicked(x, y);
            return;
        }

        mouseX = x;
        mouseY = y;

        // Note: Entity placement is handled in update() via InputManager
        // which properly distinguishes left vs right mouse button.
        // This method is only used for forwarding to play mode.
    }

    /**
     * Handle clicks in the palette area
     */
    private void handlePaletteClick(int x, int y) {
        // Check category tabs
        int tabY = 10;
        int tabHeight = 30;
        for (PaletteCategory cat : PaletteCategory.values()) {
            if (y >= tabY && y < tabY + tabHeight) {
                currentCategory = cat;
                selectedPaletteIndex = 0;
                paletteScrollOffset = 0;
                return;
            }
            tabY += tabHeight + 5;
        }

        // Check palette items
        int itemY = tabY + 20;
        int itemX = 10;
        int col = 0;
        List<PaletteItem> palette = getCurrentPalette();

        for (int i = 0; i < palette.size(); i++) {
            Rectangle itemRect = new Rectangle(itemX, itemY, PALETTE_ITEM_SIZE + 8, PALETTE_ITEM_SIZE + 8);
            if (itemRect.contains(x, y)) {
                selectedPaletteIndex = i;
                return;
            }

            col++;
            itemX += PALETTE_ITEM_SIZE + 12;

            if (col >= PALETTE_ITEMS_PER_ROW) {
                col = 0;
                itemX = 10;
                itemY += PALETTE_ITEM_SIZE + 12;
            }
        }
    }

    /**
     * Place an entity at the current mouse position
     */
    private void placeEntity() {
        PaletteItem selected = getSelectedPaletteItem();
        if (selected == null) return;

        int placeX, placeY;

        switch (currentCategory) {
            case BLOCKS:
                // Snap to grid
                placeX = (worldMouseX / GRID_SIZE) * GRID_SIZE;
                placeY = (worldMouseY / GRID_SIZE) * GRID_SIZE;

                // Check if block already exists at this position
                for (PlacedEntity entity : placedBlocks) {
                    if (entity.x == placeX && entity.y == placeY) {
                        return; // Block already exists
                    }
                }

                BlockType blockType = (BlockType) selected.data;
                BufferedImage blockIcon = blockTextures.get(blockType);
                PlacedEntity block = new PlacedEntity(placeX, placeY, "block", blockType, blockIcon);
                block.gridX = placeX / GRID_SIZE;
                block.gridY = placeY / GRID_SIZE;
                placedBlocks.add(block);
                break;

            case ITEMS:
                placeX = worldMouseX - 16;
                placeY = worldMouseY - 16;
                String itemId = (String) selected.data;
                PlacedEntity item = new PlacedEntity(placeX, placeY, "item", itemId, selected.icon);
                placedItems.add(item);
                setStatus("Placed item: " + selected.displayName);
                break;

            case MOBS:
                placeX = worldMouseX - 24;
                placeY = worldMouseY - 24;
                @SuppressWarnings("unchecked")
                Map<String, String> mobData = (Map<String, String>) selected.data;
                PlacedEntity mob = new PlacedEntity(placeX, placeY, "mob", new HashMap<>(mobData), selected.icon);
                placedMobs.add(mob);
                setStatus("Placed mob: " + selected.displayName);
                break;

            case LIGHTS:
                placeX = worldMouseX - 16;
                placeY = worldMouseY - 16;
                String lightType = (String) selected.data;
                PlacedEntity light = new PlacedEntity(placeX, placeY, "light", lightType, selected.icon);
                placedLights.add(light);
                setStatus("Placed light: " + selected.displayName);
                break;

            case PARALLAX:
                // For parallax, clicking adds/removes the layer to the level
                @SuppressWarnings("unchecked")
                Map<String, String> parallaxData = (Map<String, String>) selected.data;
                String layerName = parallaxData.get("name");

                // Check if layer already exists
                boolean exists = false;
                for (ParallaxLayerEntry layer : parallaxLayers) {
                    if (layer.name.equals(layerName)) {
                        exists = true;
                        break;
                    }
                }

                if (exists) {
                    // Remove the layer
                    parallaxLayers.removeIf(l -> l.name.equals(layerName));
                    setStatus("Removed parallax layer: " + selected.displayName);
                } else {
                    // Add the layer
                    ParallaxLayerEntry newLayer = new ParallaxLayerEntry(
                        layerName,
                        parallaxData.get("path"),
                        Double.parseDouble(parallaxData.get("scrollSpeed")),
                        Integer.parseInt(parallaxData.get("zOrder")),
                        selected.icon
                    );
                    parallaxLayers.add(newLayer);
                    // Sort by z-order
                    parallaxLayers.sort((a, b) -> Integer.compare(a.zOrder, b.zOrder));
                    setStatus("Added parallax layer: " + selected.displayName);
                }
                levelData.parallaxEnabled = !parallaxLayers.isEmpty();
                break;
        }
    }

    /**
     * Remove entity at the given position
     */
    public void removeEntityAt(int worldX, int worldY) {
        // Check all entity lists
        Iterator<PlacedEntity> iter = placedBlocks.iterator();
        while (iter.hasNext()) {
            PlacedEntity entity = iter.next();
            if (entity.getBounds().contains(worldX, worldY)) {
                iter.remove();
                setStatus("Removed block");
                return;
            }
        }

        iter = placedItems.iterator();
        while (iter.hasNext()) {
            PlacedEntity entity = iter.next();
            if (entity.getBounds().contains(worldX, worldY)) {
                iter.remove();
                setStatus("Removed item");
                return;
            }
        }

        iter = placedMobs.iterator();
        while (iter.hasNext()) {
            PlacedEntity entity = iter.next();
            if (entity.getBounds().contains(worldX, worldY)) {
                iter.remove();
                setStatus("Removed mob");
                return;
            }
        }

        iter = placedLights.iterator();
        while (iter.hasNext()) {
            PlacedEntity entity = iter.next();
            if (entity.getBounds().contains(worldX, worldY)) {
                iter.remove();
                setStatus("Removed light");
                return;
            }
        }
    }

    /**
     * Enter play mode to test the level
     */
    private void enterPlayMode() {
        setStatus("Entering play mode...");

        // Build level data from placed entities
        buildLevelData();

        // Create a temporary game scene
        gameScene = new GameScene();
        gameScene.setLevelData(levelData);
        gameScene.init();

        isPlayMode = true;
        setStatus("Play mode active - Press E to return to editing");
    }

    /**
     * Exit play mode and return to editing
     */
    private void exitPlayMode() {
        if (gameScene != null) {
            gameScene.dispose();
            gameScene = null;
        }
        isPlayMode = false;
        setStatus("Returned to edit mode");
    }

    /**
     * Build LevelData from placed entities
     */
    private void buildLevelData() {
        levelData.blocks.clear();
        levelData.items.clear();
        levelData.mobs.clear();
        levelData.lightSources.clear();
        levelData.parallaxLayers.clear();

        // Add blocks
        for (PlacedEntity entity : placedBlocks) {
            LevelData.BlockData blockData = new LevelData.BlockData();
            blockData.x = entity.gridX;
            blockData.y = entity.gridY;
            blockData.blockType = ((BlockType) entity.data).name();
            blockData.useGridCoords = true;
            levelData.blocks.add(blockData);
        }

        // Add items
        for (PlacedEntity entity : placedItems) {
            LevelData.ItemData itemData = new LevelData.ItemData();
            itemData.x = entity.x;
            itemData.y = entity.y;
            itemData.itemId = (String) entity.data;
            levelData.items.add(itemData);
        }

        // Add mobs
        for (PlacedEntity entity : placedMobs) {
            @SuppressWarnings("unchecked")
            Map<String, String> mobInfo = (Map<String, String>) entity.data;
            LevelData.MobData mobData = new LevelData.MobData();
            mobData.x = entity.x;
            mobData.y = entity.y;
            mobData.mobType = mobInfo.get("mobType");
            mobData.subType = mobInfo.get("subType");
            mobData.behavior = mobInfo.get("behavior");
            mobData.spriteDir = "assets/mobs/" + mobInfo.get("subType");
            levelData.mobs.add(mobData);
        }

        // Add lights
        for (PlacedEntity entity : placedLights) {
            LevelData.LightSourceData lightData = new LevelData.LightSourceData(
                entity.x, entity.y, (String) entity.data
            );
            levelData.lightSources.add(lightData);
        }

        // Add parallax layers
        for (ParallaxLayerEntry layer : parallaxLayers) {
            LevelData.ParallaxLayerData parallaxData = new LevelData.ParallaxLayerData();
            parallaxData.name = layer.name;
            parallaxData.imagePath = layer.imagePath;
            parallaxData.scrollSpeedX = layer.scrollSpeedX;
            parallaxData.zOrder = layer.zOrder;
            parallaxData.scale = layer.scale;  // Should be 10.0 for proper sizing
            parallaxData.opacity = layer.opacity;
            parallaxData.tileHorizontal = true;
            parallaxData.anchorBottom = true;  // Anchor to bottom for proper positioning
            levelData.parallaxLayers.add(parallaxData);
        }
    }

    /**
     * Save the level to a JSON file
     * Opens the filename input modal if no filename provided
     */
    private void saveLevel() {
        // Show filename input modal
        modalState = ModalState.SAVE_FILENAME;
        modalInputText = "";
        setStatus("Enter filename and press Enter to save");
    }

    /**
     * Actually perform the save with the given filename
     */
    private void performSave(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            setStatus("Save cancelled");
            return;
        }

        // Build the level data
        buildLevelData();

        // Ensure filename is valid
        filename = filename.replaceAll("[^a-zA-Z0-9_-]", "_");
        String filepath = "levels/creative_" + filename + ".json";

        try {
            File file = new File(filepath);
            file.getParentFile().mkdirs();

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("{");
                writer.println("  \"name\": \"" + escapeJson(levelData.name) + "\",");
                writer.println("  \"description\": \"Creative mode level\",");
                writer.println();

                // Player settings
                writer.println("  \"useSpriteAnimation\": true,");
                writer.println("  \"spriteAnimationDir\": \"assets/player/sprites\",");
                writer.println();

                // Background
                writer.println("  \"backgroundPath\": \"" + escapeJson(levelData.backgroundPath) + "\",");
                writer.println("  \"musicPath\": \"" + escapeJson(levelData.musicPath) + "\",");
                writer.println();

                // Spawn
                writer.println("  \"playerSpawnX\": " + levelData.playerSpawnX + ",");
                writer.println("  \"playerSpawnY\": " + levelData.playerSpawnY + ",");
                writer.println();

                // Dimensions
                writer.println("  \"levelWidth\": " + levelData.levelWidth + ",");
                writer.println("  \"levelHeight\": " + levelData.levelHeight + ",");
                writer.println("  \"groundY\": " + levelData.groundY + ",");
                writer.println();

                // Scrolling
                writer.println("  \"scrollingEnabled\": " + levelData.scrollingEnabled + ",");
                writer.println("  \"tileBackgroundHorizontal\": " + levelData.tileBackgroundHorizontal + ",");
                writer.println("  \"tileBackgroundVertical\": false,");
                writer.println("  \"verticalScrollEnabled\": " + levelData.verticalScrollEnabled + ",");
                writer.println();

                // Lighting
                writer.println("  \"nightMode\": false,");
                writer.println();

                // Blocks
                writer.println("  \"blocks\": [");
                for (int i = 0; i < levelData.blocks.size(); i++) {
                    LevelData.BlockData b = levelData.blocks.get(i);
                    String comma = (i < levelData.blocks.size() - 1) ? "," : "";
                    writer.println("    {\"x\": " + b.x + ", \"y\": " + b.y +
                        ", \"blockType\": \"" + b.blockType + "\", \"useGridCoords\": true}" + comma);
                }
                writer.println("  ],");
                writer.println();

                // Items
                writer.println("  \"items\": [");
                for (int i = 0; i < levelData.items.size(); i++) {
                    LevelData.ItemData item = levelData.items.get(i);
                    String comma = (i < levelData.items.size() - 1) ? "," : "";
                    writer.println("    {\"x\": " + item.x + ", \"y\": " + item.y +
                        ", \"itemId\": \"" + escapeJson(item.itemId) + "\"}" + comma);
                }
                writer.println("  ],");
                writer.println();

                // Mobs
                writer.println("  \"mobs\": [");
                for (int i = 0; i < levelData.mobs.size(); i++) {
                    LevelData.MobData m = levelData.mobs.get(i);
                    String comma = (i < levelData.mobs.size() - 1) ? "," : "";
                    writer.println("    {\"x\": " + m.x + ", \"y\": " + m.y +
                        ", \"mobType\": \"" + escapeJson(m.mobType) +
                        "\", \"spriteDir\": \"" + escapeJson(m.spriteDir) +
                        "\", \"behavior\": \"" + escapeJson(m.behavior) + "\"}" + comma);
                }
                writer.println("  ],");
                writer.println();

                // Light sources
                writer.println("  \"lightSources\": [");
                for (int i = 0; i < levelData.lightSources.size(); i++) {
                    LevelData.LightSourceData l = levelData.lightSources.get(i);
                    String comma = (i < levelData.lightSources.size() - 1) ? "," : "";
                    writer.println("    {\"x\": " + l.x + ", \"y\": " + l.y +
                        ", \"lightType\": \"" + escapeJson(l.lightType) + "\"}" + comma);
                }
                writer.println("  ],");
                writer.println();

                // Empty arrays
                writer.println("  \"platforms\": [],");
                writer.println("  \"triggers\": [],");
                writer.println();

                // Parallax layers
                writer.println("  \"parallaxEnabled\": " + levelData.parallaxEnabled + ",");
                writer.println("  \"parallaxLayers\": [");
                for (int i = 0; i < levelData.parallaxLayers.size(); i++) {
                    LevelData.ParallaxLayerData p = levelData.parallaxLayers.get(i);
                    String comma = (i < levelData.parallaxLayers.size() - 1) ? "," : "";
                    writer.println("    {\"name\": \"" + escapeJson(p.name) +
                        "\", \"imagePath\": \"" + escapeJson(p.imagePath) +
                        "\", \"scrollSpeedX\": " + p.scrollSpeedX +
                        ", \"zOrder\": " + p.zOrder +
                        ", \"scale\": " + p.scale +
                        ", \"opacity\": " + p.opacity +
                        ", \"tileHorizontal\": " + p.tileHorizontal +
                        ", \"anchorBottom\": " + p.anchorBottom + "}" + comma);
                }
                writer.println("  ]");

                writer.println("}");
            }

            currentLevelPath = filepath;
            setStatus("Level saved to: " + filepath);

        } catch (IOException e) {
            setStatus("Error saving level: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Escape special characters for JSON
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * Load a level from JSON file
     */
    public void loadLevel(String filepath) {
        try {
            levelData = LevelLoader.load(filepath);
            currentLevelPath = filepath;

            // Clear placed entities
            placedBlocks.clear();
            placedItems.clear();
            placedMobs.clear();
            placedLights.clear();

            // Convert level data to placed entities
            for (LevelData.BlockData b : levelData.blocks) {
                BlockType type = BlockType.fromName(b.blockType);
                int px = b.useGridCoords ? b.x * GRID_SIZE : b.x;
                int py = b.useGridCoords ? b.y * GRID_SIZE : b.y;

                PlacedEntity entity = new PlacedEntity(px, py, "block", type, blockTextures.get(type));
                entity.gridX = b.useGridCoords ? b.x : b.x / GRID_SIZE;
                entity.gridY = b.useGridCoords ? b.y : b.y / GRID_SIZE;
                placedBlocks.add(entity);
            }

            for (LevelData.ItemData i : levelData.items) {
                BufferedImage icon = createItemIcon(i.itemId != null ? i.itemId : "unknown");
                PlacedEntity entity = new PlacedEntity(i.x, i.y, "item", i.itemId, icon);
                placedItems.add(entity);
            }

            for (LevelData.MobData m : levelData.mobs) {
                BufferedImage icon = createMobIcon(m.subType != null ? m.subType : "zombie");
                Map<String, String> mobData = new HashMap<>();
                mobData.put("mobType", m.mobType);
                mobData.put("subType", m.subType);
                mobData.put("behavior", m.behavior);
                PlacedEntity entity = new PlacedEntity(m.x, m.y, "mob", mobData, icon);
                placedMobs.add(entity);
            }

            for (LevelData.LightSourceData l : levelData.lightSources) {
                BufferedImage icon = createLightIcon(l.lightType);
                PlacedEntity entity = new PlacedEntity(l.x, l.y, "light", l.lightType, icon);
                placedLights.add(entity);
            }

            // Restore parallax layers
            parallaxLayers.clear();
            for (LevelData.ParallaxLayerData p : levelData.parallaxLayers) {
                BufferedImage icon = createParallaxIcon(p.imagePath);
                ParallaxLayerEntry entry = new ParallaxLayerEntry(
                    p.name, p.imagePath, p.scrollSpeedX, p.zOrder, icon
                );
                entry.scale = p.scale;
                entry.opacity = p.opacity;
                parallaxLayers.add(entry);
            }
            // Sort by z-order
            parallaxLayers.sort((a, b) -> Integer.compare(a.zOrder, b.zOrder));

            // Update camera bounds
            camera.setLevelBounds(levelData.levelWidth, levelData.levelHeight);

            setStatus("Loaded level: " + filepath);

        } catch (Exception e) {
            setStatus("Error loading level: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show the new level properties dialog
     */
    public void showNewLevelDialog() {
        JDialog dialog = new JDialog((Frame) null, "New Creative Level", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Level name
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Level Name:"), gbc);
        JTextField nameField = new JTextField("My Creative Level", 20);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        // Width
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Width (pixels):"), gbc);
        JTextField widthField = new JTextField("3840", 10);
        gbc.gridx = 1;
        dialog.add(widthField, gbc);

        // Height
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Height (pixels):"), gbc);
        JTextField heightField = new JTextField("1080", 10);
        gbc.gridx = 1;
        dialog.add(heightField, gbc);

        // Ground Y
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Ground Y:"), gbc);
        JTextField groundField = new JTextField("920", 10);
        gbc.gridx = 1;
        dialog.add(groundField, gbc);

        // Scrolling enabled
        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Enable Scrolling:"), gbc);
        JCheckBox scrollCheck = new JCheckBox("", true);
        gbc.gridx = 1;
        dialog.add(scrollCheck, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton createBtn = new JButton("Create");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        createBtn.addActionListener(e -> {
            try {
                levelData.name = nameField.getText();
                levelData.levelWidth = Integer.parseInt(widthField.getText());
                levelData.levelHeight = Integer.parseInt(heightField.getText());
                levelData.groundY = Integer.parseInt(groundField.getText());
                levelData.scrollingEnabled = scrollCheck.isSelected();
                levelData.playerSpawnX = 200;
                levelData.playerSpawnY = levelData.groundY - 70;

                camera.setLevelBounds(levelData.levelWidth, levelData.levelHeight);

                // Clear any existing entities
                placedBlocks.clear();
                placedItems.clear();
                placedMobs.clear();
                placedLights.clear();

                showPropertiesDialog = false;
                dialog.dispose();
                setStatus("Created new level: " + levelData.name);
            } catch (NumberFormatException ex) {
                // Reset fields to defaults and show error in status
                widthField.setText("3840");
                heightField.setText("1080");
                groundField.setText("920");
                // Show error via dialog title change (avoid JOptionPane)
                dialog.setTitle("Error: Please enter valid numbers!");
            }
        });

        cancelBtn.addActionListener(e -> {
            showPropertiesDialog = false;
            dialog.dispose();
        });

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    @Override
    public void dispose() {
        if (gameScene != null) {
            gameScene.dispose();
            gameScene = null;
        }
        initialized = false;
    }

    @Override
    public String getName() {
        return "creative";
    }

    /**
     * Set the level data directly (for loading existing levels)
     */
    public void setLevelData(LevelData data) {
        this.levelData = data;
    }

    /**
     * Get current level data
     */
    public LevelData getLevelData() {
        buildLevelData();
        return levelData;
    }
}
