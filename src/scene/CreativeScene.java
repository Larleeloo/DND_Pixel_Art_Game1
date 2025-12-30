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

    // Block textures cache
    private Map<BlockType, BufferedImage> blockTextures;

    // Palette data
    private List<PaletteItem> blockPalette;
    private List<PaletteItem> itemPalette;
    private List<PaletteItem> mobPalette;
    private List<PaletteItem> lightPalette;

    /**
     * Categories in the palette
     */
    public enum PaletteCategory {
        BLOCKS("Blocks"),
        ITEMS("Items"),
        MOBS("Mobs"),
        LIGHTS("Lights");

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

    @Override
    public void init() {
        if (initialized) return;

        // Initialize lists
        placedBlocks = new ArrayList<>();
        placedItems = new ArrayList<>();
        placedMobs = new ArrayList<>();
        placedLights = new ArrayList<>();
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
     * Create a placeholder icon for items
     */
    private BufferedImage createItemIcon(String itemId) {
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
     * Create a placeholder icon for mobs
     */
    private BufferedImage createMobIcon(String mobType) {
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

        // Escape to return to menu
        if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
            // Ask to save first
            int result = JOptionPane.showConfirmDialog(null,
                "Save before exiting?", "Save Level",
                JOptionPane.YES_NO_CANCEL_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                saveLevel();
                SceneManager.getInstance().setScene("mainMenu", SceneManager.TRANSITION_FADE);
            } else if (result == JOptionPane.NO_OPTION) {
                SceneManager.getInstance().setScene("mainMenu", SceneManager.TRANSITION_FADE);
            }
            // Cancel does nothing
        }

        // Update world mouse position
        worldMouseX = mouseX + (int) cameraX;
        worldMouseY = mouseY + (int) cameraY;

        // Update hovered entity
        updateHoveredEntity();
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
    }

    /**
     * Draw the grid overlay
     */
    private void drawGrid(Graphics2D g) {
        g.setColor(new Color(100, 100, 100, 50));

        int startX = -(int) cameraX % GRID_SIZE;
        int startY = -(int) cameraY % GRID_SIZE;

        // Vertical lines
        for (int x = startX + PALETTE_WIDTH; x < GamePanel.SCREEN_WIDTH; x += GRID_SIZE) {
            g.drawLine(x, 0, x, GamePanel.SCREEN_HEIGHT);
        }

        // Horizontal lines
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
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("WASD: Pan | Click: Place | Right-Click: Delete | Tab: Category | G: Grid | P: Play | Ctrl+S: Save | Esc: Menu",
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
        g.fillRect(GamePanel.SCREEN_WIDTH - 200, 50, 190, 100);

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

        // Left click in canvas area
        if (x >= PALETTE_WIDTH) {
            // Check for right click (remove) - handled via button check
            // For now, assume left click places
            placeEntity();
        }
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
    }

    /**
     * Save the level to a JSON file
     */
    private void saveLevel() {
        // Build the level data
        buildLevelData();

        // Get filename from user
        String filename = JOptionPane.showInputDialog(null,
            "Enter level filename (without .json):",
            "Save Level",
            JOptionPane.PLAIN_MESSAGE);

        if (filename == null || filename.trim().isEmpty()) {
            setStatus("Save cancelled");
            return;
        }

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
                writer.println("  \"parallaxLayers\": []");

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
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for dimensions.");
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
