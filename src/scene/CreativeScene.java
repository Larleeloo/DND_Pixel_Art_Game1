package scene;

import core.*;
import entity.*;
import entity.player.*;
import entity.mob.*;
import block.*;
import graphics.*;
import level.*;
import input.*;
import scene.creative.CreativePaletteManager;
import scene.creative.CreativePaletteManager.PaletteCategory;
import scene.creative.CreativePaletteManager.ItemSortMode;
import scene.creative.CreativePaletteManager.PaletteItem;

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
 * Controls (Edit Mode):
 * - Left click: Place selected entity
 * - Right click: Remove entity at cursor
 * - WASD/Arrow keys: Pan camera
 * - P: Toggle play mode
 * - Ctrl+S: Save level
 * - Tab: Cycle palette categories
 * - W: Configure door (near door)
 * - E: Configure button (near button)
 * - Escape: Exit to main menu
 *
 * Controls (Play Mode):
 * - E: Interact with vault/chest/door
 * - Escape/Tab: Return to edit mode
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

    // Palette manager (extracted for better code organization)
    private CreativePaletteManager paletteManager;

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
        STARTUP,        // Initial dialog: New Level or Load Level
        CONFIRM_EXIT,   // Confirm exit dialog (Yes/No/Cancel)
        SAVE_FILENAME,  // Enter filename dialog
        LOAD_LEVEL,     // Load level dialog
        NEW_LEVEL,      // New level properties dialog
        CONFIG_DOOR,    // Configure door action (W key near door)
        CONFIG_BUTTON   // Configure button action (E key near button)
    }

    // Available creative levels for loading
    private List<String> availableLevels;
    private int selectedLevelIndex = 0;

    // Placed interactive entities (doors, buttons, and vaults)
    private List<PlacedEntity> placedDoors;
    private List<PlacedEntity> placedButtons;
    private List<PlacedEntity> placedVaults;
    private List<PlacedEntity> placedMovingBlocks;

    // For door/button configuration modal
    private PlacedEntity entityBeingConfigured = null;
    private String configInputText = "";

    // Parallax layers currently in use
    private List<ParallaxLayerEntry> parallaxLayers;

    /**
     * Represents an entity placed in the level
     */
    private static class PlacedEntity {
        int x, y;
        int gridX, gridY; // For blocks
        String type; // "block", "item", "mob", "light"
        Object data; // Type-specific data
        BufferedImage icon;
        String overlay; // Block overlay type (e.g., "SNOW", "ICE", "GRASS", "MOSS", "VINES")

        PlacedEntity(int x, int y, String type, Object data, BufferedImage icon) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.data = data;
            this.icon = icon;
            this.overlay = null;
        }

        Rectangle getBounds() {
            int size = type.equals("block") ? GRID_SIZE : 32;
            return new Rectangle(x, y, size, size);
        }

        boolean hasOverlay() {
            return overlay != null && !overlay.isEmpty();
        }
    }

    /**
     * Represents a parallax layer in the level with editable placement properties
     */
    private static class ParallaxLayerEntry {
        String name;
        String imagePath;
        double scrollSpeedX;
        double scrollSpeedY;
        int zOrder;
        double scale;
        double opacity;
        int offsetX;
        int offsetY;
        String positionLabel;
        BufferedImage icon;

        ParallaxLayerEntry(String name, String imagePath, double scrollSpeedX, int zOrder, BufferedImage icon) {
            this.name = name;
            this.imagePath = imagePath;
            this.scrollSpeedX = scrollSpeedX;
            this.scrollSpeedY = scrollSpeedX;  // Default vertical speed matches horizontal for depth consistency
            this.zOrder = zOrder;
            this.scale = 10.0;  // Scale up to cover screen (matches LevelData default)
            this.opacity = 1.0;
            this.offsetX = 0;
            this.offsetY = 0;
            this.positionLabel = "";
            this.icon = icon;
        }

        ParallaxLayerEntry(String name, String imagePath, double scrollSpeedX, double scrollSpeedY, int zOrder,
                          double scale, double opacity, int offsetX, int offsetY,
                          String positionLabel, BufferedImage icon) {
            this.name = name;
            this.imagePath = imagePath;
            this.scrollSpeedX = scrollSpeedX;
            this.scrollSpeedY = scrollSpeedY;
            this.zOrder = zOrder;
            this.scale = scale;
            this.opacity = opacity;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.positionLabel = positionLabel;
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
        placedDoors = new ArrayList<>();
        placedButtons = new ArrayList<>();
        placedVaults = new ArrayList<>();
        placedMovingBlocks = new ArrayList<>();
        parallaxLayers = new ArrayList<>();

        // Initialize palette manager (handles all palette-related functionality)
        paletteManager = new CreativePaletteManager();
        paletteManager.initialize();

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

        initialized = true;

        // Show startup dialog to choose New Level or Load Level
        modalState = ModalState.STARTUP;
        setStatus("Welcome to Creative Mode - Create a new level or load an existing one");
    }

    @Override
    public void update(InputManager input) {
        if (!initialized) init();

        if (isPlayMode && gameScene != null) {
            // In play mode, update the game scene
            gameScene.update(input);

            // Check for Escape or Tab to return to edit mode
            // (E key is now reserved for vault/chest/door interaction in play mode)
            if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE) || input.isKeyJustPressed(KeyEvent.VK_TAB)) {
                exitPlayMode();
            }
            return;
        }

        // Camera movement (don't move down with S when Ctrl is held - that's for save)
        if (input.isKeyPressed(KeyEvent.VK_W) || input.isKeyPressed(KeyEvent.VK_UP)) {
            cameraY -= CAMERA_SPEED;
        }
        if ((input.isKeyPressed(KeyEvent.VK_S) && !input.isKeyPressed(KeyEvent.VK_CONTROL)) || input.isKeyPressed(KeyEvent.VK_DOWN)) {
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
            paletteManager.cycleCategory();
            setStatus("Category: " + paletteManager.getCurrentCategory().getDisplayName());
        }

        // Handle palette scrolling with mouse wheel when mouse is over palette area
        if (mouseX < CreativePaletteManager.PALETTE_WIDTH) {
            int scroll = input.getScrollDirection();
            if (scroll != 0) {
                paletteManager.handleScroll(scroll);
            }
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

        // S to toggle item sort mode (only when in Items category, and not Ctrl+S for save)
        if (input.isKeyJustPressed(KeyEvent.VK_S) && !input.isKeyPressed(KeyEvent.VK_CONTROL)) {
            if (paletteManager.getCurrentCategory() == PaletteCategory.ITEMS) {
                paletteManager.toggleItemSortMode();
                setStatus("Sort: " + paletteManager.getItemSortMode().getDisplayName());
            }
        }

        // Ctrl+S to save
        if (input.isKeyPressed(KeyEvent.VK_CONTROL) && input.isKeyJustPressed(KeyEvent.VK_S)) {
            saveLevel();
        }

        // Ctrl+L or L to load level
        if (input.isKeyJustPressed(KeyEvent.VK_L)) {
            openLoadDialog();
        }

        // W key to configure door near cursor (level transition)
        if (input.isKeyJustPressed(KeyEvent.VK_W)) {
            PlacedEntity nearbyDoor = findNearbyDoor(worldMouseX, worldMouseY);
            if (nearbyDoor != null) {
                entityBeingConfigured = nearbyDoor;
                configInputText = "";
                modalState = ModalState.CONFIG_DOOR;
                setStatus("Configure door level transition");
            }
        }

        // E key to configure button near cursor (action/spawn)
        if (input.isKeyJustPressed(KeyEvent.VK_E)) {
            PlacedEntity nearbyButton = findNearbyButton(worldMouseX, worldMouseY);
            if (nearbyButton != null) {
                entityBeingConfigured = nearbyButton;
                configInputText = "";
                modalState = ModalState.CONFIG_BUTTON;
                setStatus("Configure button action");
            }
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
        if (input.isRightMouseJustPressed() && mouseX >= CreativePaletteManager.PALETTE_WIDTH) {
            removeEntityAt(worldMouseX, worldMouseY);
        }

        // Left-click to place (already handled in onMouseClicked, but also handle here for immediate feedback)
        if (input.isLeftMouseJustPressed() && mouseX >= CreativePaletteManager.PALETTE_WIDTH) {
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
            case STARTUP:
                // N = New Level, L = Load Level, Escape = Use defaults and start
                if (input.isKeyJustPressed(KeyEvent.VK_N)) {
                    modalState = ModalState.NONE;
                    showNewLevelDialog();
                } else if (input.isKeyJustPressed(KeyEvent.VK_L)) {
                    modalState = ModalState.NONE;
                    openLoadDialog();
                } else if (input.isKeyJustPressed(KeyEvent.VK_ESCAPE)) {
                    // Start with default level settings
                    modalState = ModalState.NONE;
                    setStatus("Using default level (60x17 blocks) - Press N for New Level or L to Load");
                }
                break;

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

            case CONFIG_DOOR:
                // Handle text input for door level path
                if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
                    String levelPath = configInputText.trim();
                    if (entityBeingConfigured != null && !levelPath.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> doorData = (Map<String, Object>) entityBeingConfigured.data;
                        doorData.put("actionType", "level_transition");
                        doorData.put("actionTarget", levelPath);
                        setStatus("Door configured to load: " + levelPath);
                    }
                    modalState = ModalState.NONE;
                    entityBeingConfigured = null;
                    configInputText = "";
                } else if (input.isKeyJustPressed(KeyEvent.VK_BACK_SPACE)) {
                    if (configInputText.length() > 0) {
                        configInputText = configInputText.substring(0, configInputText.length() - 1);
                    }
                } else {
                    // Handle character input
                    handleConfigTextInput(input);
                }
                break;

            case CONFIG_BUTTON:
                // Handle text input for button action (spawn mob type)
                if (input.isKeyJustPressed(KeyEvent.VK_ENTER)) {
                    String actionTarget = configInputText.trim();
                    if (entityBeingConfigured != null && !actionTarget.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> buttonData = (Map<String, Object>) entityBeingConfigured.data;
                        buttonData.put("actionType", "spawn_entity");
                        buttonData.put("actionTarget", actionTarget);
                        setStatus("Button configured to spawn: " + actionTarget);
                    }
                    modalState = ModalState.NONE;
                    entityBeingConfigured = null;
                    configInputText = "";
                } else if (input.isKeyJustPressed(KeyEvent.VK_BACK_SPACE)) {
                    if (configInputText.length() > 0) {
                        configInputText = configInputText.substring(0, configInputText.length() - 1);
                    }
                } else {
                    // Handle character input
                    handleConfigTextInput(input);
                }
                break;

            default:
                break;
        }
    }

    /**
     * Handle text input for configuration modals
     */
    private void handleConfigTextInput(InputManager input) {
        // Check for typed characters (a-z, 0-9, underscore, hyphen, dot, slash)
        for (char c = 'a'; c <= 'z'; c++) {
            if (input.isKeyJustPressed(c)) {
                if (configInputText.length() < 60) {
                    configInputText += c;
                }
            }
        }
        for (char c = '0'; c <= '9'; c++) {
            if (input.isKeyJustPressed(c)) {
                if (configInputText.length() < 60) {
                    configInputText += c;
                }
            }
        }
        if (input.isKeyJustPressed(KeyEvent.VK_MINUS)) {
            if (configInputText.length() < 60) {
                configInputText += "-";
            }
        }
        if (input.isKeyJustPressed(KeyEvent.VK_PERIOD)) {
            if (configInputText.length() < 60) {
                configInputText += ".";
            }
        }
        if (input.isKeyJustPressed(KeyEvent.VK_SLASH)) {
            if (configInputText.length() < 60) {
                configInputText += "/";
            }
        }
        if (input.isKeyPressed(KeyEvent.VK_SHIFT) && input.isKeyJustPressed(KeyEvent.VK_MINUS)) {
            if (configInputText.length() < 60) {
                configInputText += "_";
            }
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
     * Find a door near the given world coordinates
     */
    private PlacedEntity findNearbyDoor(int worldX, int worldY) {
        int searchRadius = 100;
        for (PlacedEntity door : placedDoors) {
            Rectangle bounds = new Rectangle(door.x - searchRadius/2, door.y - searchRadius/2,
                64 + searchRadius, 128 + searchRadius);
            if (bounds.contains(worldX, worldY)) {
                return door;
            }
        }
        return null;
    }

    /**
     * Find a button near the given world coordinates
     */
    private PlacedEntity findNearbyButton(int worldX, int worldY) {
        int searchRadius = 80;
        for (PlacedEntity button : placedButtons) {
            Rectangle bounds = new Rectangle(button.x - searchRadius/2, button.y - searchRadius/2,
                32 + searchRadius, 16 + searchRadius);
            if (bounds.contains(worldX, worldY)) {
                return button;
            }
        }
        return null;
    }

    /**
     * Update which entity the mouse is hovering over
     */
    private void updateHoveredEntity() {
        hoveredEntity = null;

        // Don't check if over palette
        if (mouseX < CreativePaletteManager.PALETTE_WIDTH) return;

        // Check blocks
        for (PlacedEntity entity : placedBlocks) {
            if (entity.getBounds().contains(worldMouseX, worldMouseY)) {
                hoveredEntity = entity;
                return;
            }
        }

        // Check moving blocks
        for (PlacedEntity entity : placedMovingBlocks) {
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

        // Check doors
        for (PlacedEntity entity : placedDoors) {
            Rectangle bounds = new Rectangle(entity.x, entity.y, 64, 128);
            if (bounds.contains(worldMouseX, worldMouseY)) {
                hoveredEntity = entity;
                return;
            }
        }

        // Check buttons
        for (PlacedEntity entity : placedButtons) {
            Rectangle bounds = new Rectangle(entity.x, entity.y, 32, 16);
            if (bounds.contains(worldMouseX, worldMouseY)) {
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
            case STARTUP:
                // Title
                g.setFont(new Font("Arial", Font.BOLD, 28));
                String title = "Creative Mode";
                FontMetrics fm = g.getFontMetrics();
                int titleX = dialogX + (dialogWidth - fm.stringWidth(title)) / 2;
                g.drawString(title, titleX, dialogY + 50);

                // Subtitle
                g.setFont(new Font("Arial", Font.PLAIN, 16));
                String subtitle = "Design and build your own levels";
                fm = g.getFontMetrics();
                int subtitleX = dialogX + (dialogWidth - fm.stringWidth(subtitle)) / 2;
                g.setColor(new Color(180, 180, 200));
                g.drawString(subtitle, subtitleX, dialogY + 80);

                // Options
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.setColor(new Color(100, 200, 100));
                g.drawString("[N] New Level", dialogX + 80, dialogY + 120);
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.setColor(new Color(150, 150, 170));
                g.drawString("Configure level size and settings", dialogX + 80, dialogY + 138);

                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.setColor(new Color(100, 150, 255));
                g.drawString("[L] Load Level", dialogX + 280, dialogY + 120);
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.setColor(new Color(150, 150, 170));
                g.drawString("Open an existing level", dialogX + 280, dialogY + 138);

                // Skip option
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.setColor(new Color(120, 120, 140));
                g.drawString("[Esc] Skip - use default settings (60x17 blocks)", dialogX + 100, dialogY + 175);
                break;

            case CONFIRM_EXIT:
                // Title
                g.setFont(new Font("Arial", Font.BOLD, 24));
                title = "Exit Creative Mode?";
                fm = g.getFontMetrics();
                titleX = dialogX + (dialogWidth - fm.stringWidth(title)) / 2;
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

            case CONFIG_DOOR:
                // Title
                g.setFont(new Font("Arial", Font.BOLD, 24));
                title = "Configure Door";
                fm = g.getFontMetrics();
                titleX = dialogX + (dialogWidth - fm.stringWidth(title)) / 2;
                g.drawString(title, titleX, dialogY + 50);

                // Instructions
                g.setFont(new Font("Arial", Font.PLAIN, 14));
                g.drawString("Enter the level path to load when this door is used:", dialogX + 30, dialogY + 85);
                g.drawString("(e.g., levels/loot_game_room.json)", dialogX + 30, dialogY + 102);

                // Text input box
                g.setColor(new Color(30, 34, 42));
                g.fillRoundRect(dialogX + 30, dialogY + 115, dialogWidth - 60, 40, 5, 5);
                g.setColor(new Color(80, 130, 180));
                g.drawRoundRect(dialogX + 30, dialogY + 115, dialogWidth - 60, 40, 5, 5);

                // Input text with cursor
                g.setColor(Color.WHITE);
                g.setFont(new Font("Monospaced", Font.PLAIN, 16));
                displayText = configInputText;
                if ((System.currentTimeMillis() / 500) % 2 == 0) {
                    displayText += "|";
                }
                g.drawString(displayText, dialogX + 40, dialogY + 142);

                // Buttons hint
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.setColor(new Color(100, 200, 100));
                g.drawString("[Enter] Confirm", dialogX + 30, dialogY + 185);
                g.setColor(new Color(150, 150, 200));
                g.drawString("[Esc] Cancel", dialogX + 130, dialogY + 185);
                break;

            case CONFIG_BUTTON:
                // Title
                g.setFont(new Font("Arial", Font.BOLD, 24));
                title = "Configure Button";
                fm = g.getFontMetrics();
                titleX = dialogX + (dialogWidth - fm.stringWidth(title)) / 2;
                g.drawString(title, titleX, dialogY + 50);

                // Instructions
                g.setFont(new Font("Arial", Font.PLAIN, 14));
                g.drawString("Enter mob type to spawn when button is pressed:", dialogX + 30, dialogY + 85);
                g.drawString("(e.g., bear, wolf, zombie, skeleton)", dialogX + 30, dialogY + 102);

                // Text input box
                g.setColor(new Color(30, 34, 42));
                g.fillRoundRect(dialogX + 30, dialogY + 115, dialogWidth - 60, 40, 5, 5);
                g.setColor(new Color(80, 130, 180));
                g.drawRoundRect(dialogX + 30, dialogY + 115, dialogWidth - 60, 40, 5, 5);

                // Input text with cursor
                g.setColor(Color.WHITE);
                g.setFont(new Font("Monospaced", Font.PLAIN, 16));
                displayText = configInputText;
                if ((System.currentTimeMillis() / 500) % 2 == 0) {
                    displayText += "|";
                }
                g.drawString(displayText, dialogX + 40, dialogY + 142);

                // Buttons hint
                g.setFont(new Font("Arial", Font.PLAIN, 12));
                g.setColor(new Color(100, 200, 100));
                g.drawString("[Enter] Confirm", dialogX + 30, dialogY + 185);
                g.setColor(new Color(150, 150, 200));
                g.drawString("[Esc] Cancel", dialogX + 130, dialogY + 185);
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
        while (startX < CreativePaletteManager.PALETTE_WIDTH) {
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
            g.drawLine(CreativePaletteManager.PALETTE_WIDTH, y, GamePanel.SCREEN_WIDTH, y);
        }

        // Draw ground line
        int groundScreenY = levelData.groundY - (int) cameraY;
        if (groundScreenY >= 0 && groundScreenY < GamePanel.SCREEN_HEIGHT) {
            g.setColor(new Color(255, 100, 100, 100));
            g.setStroke(new BasicStroke(2));
            g.drawLine(CreativePaletteManager.PALETTE_WIDTH, groundScreenY, GamePanel.SCREEN_WIDTH, groundScreenY);
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

            if (screenX + GRID_SIZE > CreativePaletteManager.PALETTE_WIDTH && screenX < GamePanel.SCREEN_WIDTH &&
                screenY + GRID_SIZE > 0 && screenY < GamePanel.SCREEN_HEIGHT) {

                if (entity.icon != null) {
                    g.drawImage(entity.icon, screenX, screenY, GRID_SIZE, GRID_SIZE, null);
                }

                // Draw overlay texture if present
                if (entity.hasOverlay()) {
                    BlockOverlay overlay = BlockOverlay.fromName(entity.overlay);
                    if (overlay != BlockOverlay.NONE) {
                        BufferedImage overlayTexture = BlockRegistry.getInstance().getOverlayTexture(overlay);
                        if (overlayTexture != null) {
                            g.drawImage(overlayTexture, screenX, screenY, GRID_SIZE, GRID_SIZE, null);
                        }
                    }
                }

                // Highlight if hovered
                if (entity == hoveredEntity) {
                    g.setColor(new Color(255, 255, 0, 100));
                    g.fillRect(screenX, screenY, GRID_SIZE, GRID_SIZE);
                }
            }
        }

        // Draw moving blocks with movement pattern indicator
        for (PlacedEntity entity : placedMovingBlocks) {
            int screenX = entity.x - (int) cameraX;
            int screenY = entity.y - (int) cameraY;

            if (screenX + GRID_SIZE > CreativePaletteManager.PALETTE_WIDTH && screenX < GamePanel.SCREEN_WIDTH &&
                screenY + GRID_SIZE > 0 && screenY < GamePanel.SCREEN_HEIGHT) {

                if (entity.icon != null) {
                    g.drawImage(entity.icon, screenX, screenY, GRID_SIZE, GRID_SIZE, null);
                }

                // Draw overlay texture if present
                if (entity.hasOverlay()) {
                    BlockOverlay overlay = BlockOverlay.fromName(entity.overlay);
                    if (overlay != BlockOverlay.NONE) {
                        BufferedImage overlayTexture = BlockRegistry.getInstance().getOverlayTexture(overlay);
                        if (overlayTexture != null) {
                            g.drawImage(overlayTexture, screenX, screenY, GRID_SIZE, GRID_SIZE, null);
                        }
                    }
                }

                // Draw movement path preview
                @SuppressWarnings("unchecked")
                Map<String, Object> movingData = (Map<String, Object>) entity.data;
                String pattern = (String) movingData.get("movementPattern");
                int endX = ((Number) movingData.getOrDefault("endX", entity.gridX)).intValue();
                int endY = ((Number) movingData.getOrDefault("endY", entity.gridY)).intValue();

                g.setColor(new Color(255, 255, 0, 100));
                g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{5, 5}, 0));

                int endScreenX = endX * GRID_SIZE - (int) cameraX;
                int endScreenY = endY * GRID_SIZE - (int) cameraY;

                if ("HORIZONTAL".equals(pattern) || "VERTICAL".equals(pattern)) {
                    g.drawLine(screenX + GRID_SIZE / 2, screenY + GRID_SIZE / 2,
                              endScreenX + GRID_SIZE / 2, endScreenY + GRID_SIZE / 2);
                    g.fillOval(endScreenX + GRID_SIZE / 2 - 5, endScreenY + GRID_SIZE / 2 - 5, 10, 10);
                } else if ("CIRCULAR".equals(pattern)) {
                    double radius = ((Number) movingData.getOrDefault("radius", 100.0)).doubleValue();
                    g.drawOval(screenX + GRID_SIZE / 2 - (int) radius, screenY + GRID_SIZE / 2 - (int) radius,
                              (int) (radius * 2), (int) (radius * 2));
                }

                g.setStroke(new BasicStroke(1));

                // Highlight if hovered
                if (entity == hoveredEntity) {
                    g.setColor(new Color(0, 255, 255, 100));
                    g.fillRect(screenX, screenY, GRID_SIZE, GRID_SIZE);
                }
            }
        }

        // Draw items
        for (PlacedEntity entity : placedItems) {
            int screenX = entity.x - (int) cameraX;
            int screenY = entity.y - (int) cameraY;

            if (screenX + 32 > CreativePaletteManager.PALETTE_WIDTH && screenX < GamePanel.SCREEN_WIDTH &&
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

            if (screenX + 48 > CreativePaletteManager.PALETTE_WIDTH && screenX < GamePanel.SCREEN_WIDTH &&
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

            if (screenX + 32 > CreativePaletteManager.PALETTE_WIDTH && screenX < GamePanel.SCREEN_WIDTH &&
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

        // Draw doors
        for (PlacedEntity entity : placedDoors) {
            int screenX = entity.x - (int) cameraX;
            int screenY = entity.y - (int) cameraY;

            if (screenX + 64 > CreativePaletteManager.PALETTE_WIDTH && screenX < GamePanel.SCREEN_WIDTH &&
                screenY + 128 > 0 && screenY < GamePanel.SCREEN_HEIGHT) {

                if (entity.icon != null) {
                    g.drawImage(entity.icon, screenX, screenY, 64, 128, null);
                } else {
                    // Fallback drawing
                    g.setColor(new Color(139, 90, 43));
                    g.fillRect(screenX, screenY, 64, 128);
                    g.setColor(new Color(101, 67, 33));
                    g.drawRect(screenX, screenY, 64, 128);
                }

                if (entity == hoveredEntity) {
                    g.setColor(new Color(255, 255, 0, 100));
                    g.fillRect(screenX, screenY, 64, 128);
                }

                // Draw configuration indicator
                @SuppressWarnings("unchecked")
                Map<String, Object> doorData = (Map<String, Object>) entity.data;
                String actionTarget = (String) doorData.get("actionTarget");
                if (actionTarget != null && !actionTarget.isEmpty()) {
                    g.setColor(new Color(100, 255, 100, 200));
                    g.fillOval(screenX + 54, screenY + 4, 10, 10);
                    g.setFont(new Font("Arial", Font.BOLD, 8));
                    g.setColor(Color.WHITE);
                    g.drawString("L", screenX + 57, screenY + 12);
                }

                // Draw "W" hint if hovered
                if (entity == hoveredEntity) {
                    g.setFont(new Font("Arial", Font.BOLD, 12));
                    g.setColor(Color.YELLOW);
                    g.drawString("Press W to configure", screenX, screenY - 5);
                }
            }
        }

        // Draw buttons
        for (PlacedEntity entity : placedButtons) {
            int screenX = entity.x - (int) cameraX;
            int screenY = entity.y - (int) cameraY;

            if (screenX + 32 > CreativePaletteManager.PALETTE_WIDTH && screenX < GamePanel.SCREEN_WIDTH &&
                screenY + 16 > 0 && screenY < GamePanel.SCREEN_HEIGHT) {

                if (entity.icon != null) {
                    g.drawImage(entity.icon, screenX, screenY, 32, 16, null);
                } else {
                    // Fallback drawing
                    g.setColor(new Color(100, 100, 100));
                    g.fillRoundRect(screenX, screenY, 32, 16, 4, 4);
                }

                if (entity == hoveredEntity) {
                    g.setColor(new Color(255, 255, 0, 100));
                    g.fillRect(screenX, screenY, 32, 16);
                }

                // Draw configuration indicator
                @SuppressWarnings("unchecked")
                Map<String, Object> buttonData = (Map<String, Object>) entity.data;
                String actionTarget = (String) buttonData.get("actionTarget");
                if (actionTarget != null && !actionTarget.isEmpty()) {
                    g.setColor(new Color(100, 255, 100, 200));
                    g.fillOval(screenX + 26, screenY - 4, 8, 8);
                    g.setFont(new Font("Arial", Font.BOLD, 6));
                    g.setColor(Color.WHITE);
                    g.drawString("A", screenX + 28, screenY + 2);
                }

                // Draw "E" hint if hovered
                if (entity == hoveredEntity) {
                    g.setFont(new Font("Arial", Font.BOLD, 12));
                    g.setColor(Color.YELLOW);
                    g.drawString("Press E to configure", screenX, screenY - 5);
                }
            }
        }

        // Draw vaults
        for (PlacedEntity entity : placedVaults) {
            int screenX = entity.x - (int) cameraX;
            int screenY = entity.y - (int) cameraY;

            if (screenX + 64 > CreativePaletteManager.PALETTE_WIDTH && screenX < GamePanel.SCREEN_WIDTH &&
                screenY + 64 > 0 && screenY < GamePanel.SCREEN_HEIGHT) {

                if (entity.icon != null) {
                    g.drawImage(entity.icon, screenX, screenY, 64, 64, null);
                } else {
                    // Fallback drawing - simple chest shape
                    g.setColor(new Color(139, 90, 43));
                    g.fillRoundRect(screenX + 4, screenY + 16, 56, 44, 8, 8);
                    g.setColor(new Color(101, 67, 33));
                    g.fillRoundRect(screenX + 2, screenY + 8, 60, 20, 6, 6);
                    g.setColor(new Color(255, 215, 0));
                    g.fillOval(screenX + 28, screenY + 35, 8, 10);
                }

                if (entity == hoveredEntity) {
                    g.setColor(new Color(255, 215, 0, 100));
                    g.fillRect(screenX, screenY, 64, 64);
                }

                // Draw vault type label
                @SuppressWarnings("unchecked")
                Map<String, Object> vaultData = (Map<String, Object>) entity.data;
                String vaultType = (String) vaultData.get("vaultType");
                g.setFont(new Font("Arial", Font.BOLD, 9));
                g.setColor(Color.WHITE);
                String label = "PLAYER_VAULT".equals(vaultType) ? "P" : "S";
                g.drawString(label, screenX + 28, screenY + 60);
            }
        }
    }

    /**
     * Draw preview of selected item at cursor
     */
    private void drawCursorPreview(Graphics2D g) {
        if (mouseX < CreativePaletteManager.PALETTE_WIDTH) return;

        PaletteItem selected = paletteManager.getSelectedPaletteItem();
        if (selected == null) return;

        int previewX, previewY;
        int size;

        if (paletteManager.getCurrentCategory() == PaletteCategory.BLOCKS) {
            // Snap to grid
            int gridX = (worldMouseX / GRID_SIZE) * GRID_SIZE;
            int gridY = (worldMouseY / GRID_SIZE) * GRID_SIZE;
            previewX = gridX - (int) cameraX;
            previewY = gridY - (int) cameraY;
            size = GRID_SIZE;
        } else {
            previewX = mouseX - 16;
            previewY = mouseY - 16;
            size = paletteManager.getCurrentCategory() == PaletteCategory.MOBS ? 48 : 32;
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
        g.fillRect(0, 0, CreativePaletteManager.PALETTE_WIDTH, GamePanel.SCREEN_HEIGHT);

        // Border
        g.setColor(new Color(60, 64, 72));
        g.drawLine(CreativePaletteManager.PALETTE_WIDTH, 0, CreativePaletteManager.PALETTE_WIDTH, GamePanel.SCREEN_HEIGHT);

        // Category tabs
        int tabY = 10;
        int tabHeight = 30;
        for (PaletteCategory cat : PaletteCategory.values()) {
            boolean isSelected = cat == paletteManager.getCurrentCategory();

            g.setColor(isSelected ? new Color(70, 130, 180) : new Color(50, 54, 62));
            g.fillRoundRect(10, tabY, CreativePaletteManager.PALETTE_WIDTH - 20, tabHeight, 5, 5);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", isSelected ? Font.BOLD : Font.PLAIN, 12));
            FontMetrics fm = g.getFontMetrics();
            int textX = (CreativePaletteManager.PALETTE_WIDTH - fm.stringWidth(cat.getDisplayName())) / 2;
            g.drawString(cat.getDisplayName(), textX, tabY + 20);

            tabY += tabHeight + 5;
        }

        // Draw palette items with scrolling
        List<PaletteItem> currentPalette = paletteManager.getCurrentPalette();
        int startIndex = paletteManager.getPaletteScrollOffset() * CreativePaletteManager.PALETTE_ITEMS_PER_ROW;
        int endIndex = Math.min(startIndex + CreativePaletteManager.PALETTE_VISIBLE_ROWS * CreativePaletteManager.PALETTE_ITEMS_PER_ROW, currentPalette.size());

        int itemY = tabY + 20;
        int itemX = 10;
        int col = 0;

        for (int i = startIndex; i < endIndex; i++) {
            PaletteItem item = currentPalette.get(i);
            boolean isSelected = i == paletteManager.getSelectedPaletteIndex();

            // Background
            g.setColor(isSelected ? new Color(70, 130, 180) : new Color(50, 54, 62));
            g.fillRoundRect(itemX, itemY, CreativePaletteManager.PALETTE_ITEM_SIZE + 8, CreativePaletteManager.PALETTE_ITEM_SIZE + 8, 5, 5);

            // Icon
            if (item.icon != null) {
                g.drawImage(item.icon, itemX + 4, itemY + 4, CreativePaletteManager.PALETTE_ITEM_SIZE, CreativePaletteManager.PALETTE_ITEM_SIZE, null);
            }

            // For parallax items, show checkmark if layer is active
            if (paletteManager.getCurrentCategory() == PaletteCategory.PARALLAX) {
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
                    g.fillOval(itemX + CreativePaletteManager.PALETTE_ITEM_SIZE - 4, itemY + 4, 12, 12);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 10));
                    g.drawString("✓", itemX + CreativePaletteManager.PALETTE_ITEM_SIZE - 1, itemY + 13);
                }
            }

            // Selection border
            if (isSelected) {
                g.setColor(Color.WHITE);
                g.drawRoundRect(itemX, itemY, CreativePaletteManager.PALETTE_ITEM_SIZE + 8, CreativePaletteManager.PALETTE_ITEM_SIZE + 8, 5, 5);
            }

            col++;
            itemX += CreativePaletteManager.PALETTE_ITEM_SIZE + 12;

            if (col >= CreativePaletteManager.PALETTE_ITEMS_PER_ROW) {
                col = 0;
                itemX = 10;
                itemY += CreativePaletteManager.PALETTE_ITEM_SIZE + 12;
            }
        }

        // Draw scroll indicators if needed
        int totalRows = (int) Math.ceil((double) currentPalette.size() / CreativePaletteManager.PALETTE_ITEMS_PER_ROW);
        if (totalRows > CreativePaletteManager.PALETTE_VISIBLE_ROWS) {
            g.setFont(new Font("Arial", Font.BOLD, 14));

            // Scroll up indicator
            if (paletteManager.getPaletteScrollOffset() > 0) {
                g.setColor(new Color(150, 200, 255));
                g.drawString("▲ Scroll Up", 50, tabY + 10);
            }

            // Scroll down indicator
            int maxScroll = Math.max(0, totalRows - CreativePaletteManager.PALETTE_VISIBLE_ROWS);
            if (paletteManager.getPaletteScrollOffset() < maxScroll) {
                g.setColor(new Color(150, 200, 255));
                g.drawString("▼ Scroll Down", 45, GamePanel.SCREEN_HEIGHT - 60);
            }

            // Scroll position indicator
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.setColor(new Color(180, 180, 200));
            String scrollInfo = (paletteManager.getPaletteScrollOffset() + 1) + "-" + Math.min(paletteManager.getPaletteScrollOffset() + CreativePaletteManager.PALETTE_VISIBLE_ROWS, totalRows) + " of " + totalRows + " rows";
            g.drawString(scrollInfo, 10, GamePanel.SCREEN_HEIGHT - 75);
        }

        // Draw selected item name
        if (paletteManager.getSelectedPaletteIndex() < currentPalette.size()) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 11));
            String name = currentPalette.get(paletteManager.getSelectedPaletteIndex()).displayName;
            g.drawString(name, 10, GamePanel.SCREEN_HEIGHT - 40);
        }

        // Draw sort mode indicator for Items category
        if (paletteManager.getCurrentCategory() == PaletteCategory.ITEMS) {
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.setColor(new Color(100, 180, 255));
            String sortLabel = "Sort: " + paletteManager.getItemSortMode().getDisplayName() + " [S]";
            g.drawString(sortLabel, 10, GamePanel.SCREEN_HEIGHT - 20);
        }
    }

    /**
     * Draw the toolbar at the top
     */
    private void drawToolbar(Graphics2D g) {
        // Background
        g.setColor(new Color(40, 44, 52, 200));
        g.fillRect(CreativePaletteManager.PALETTE_WIDTH, 0, GamePanel.SCREEN_WIDTH - CreativePaletteManager.PALETTE_WIDTH, 40);

        // Controls help
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.drawString("WASD: Pan | LClick: Place | RClick: Delete | Tab: Category | G: Grid | P: Play | Ctrl+S: Save | L: Load | Esc: Menu",
            CreativePaletteManager.PALETTE_WIDTH + 10, 25);
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
        g.fillRoundRect(CreativePaletteManager.PALETTE_WIDTH + 10, GamePanel.SCREEN_HEIGHT - 50, 400, 30, 10, 10);

        g.setColor(new Color(255, 255, 255, (int)(255 * alpha)));
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString(statusMessage, CreativePaletteManager.PALETTE_WIDTH + 20, GamePanel.SCREEN_HEIGHT - 30);
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
        if (x < CreativePaletteManager.PALETTE_WIDTH) {
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
        if (isDragging && x >= CreativePaletteManager.PALETTE_WIDTH && paletteManager.getCurrentCategory() == PaletteCategory.BLOCKS) {
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
                paletteManager.setCurrentCategory(cat);
                paletteManager.setSelectedPaletteIndex(0);
                paletteManager.setPaletteScrollOffset(0);
                return;
            }
            tabY += tabHeight + 5;
        }

        // Check palette items (account for scroll offset)
        int itemY = tabY + 20;
        int itemX = 10;
        int col = 0;
        List<PaletteItem> palette = paletteManager.getCurrentPalette();
        int startIndex = paletteManager.getPaletteScrollOffset() * CreativePaletteManager.PALETTE_ITEMS_PER_ROW;
        int endIndex = Math.min(startIndex + CreativePaletteManager.PALETTE_VISIBLE_ROWS * CreativePaletteManager.PALETTE_ITEMS_PER_ROW, palette.size());

        for (int i = startIndex; i < endIndex; i++) {
            Rectangle itemRect = new Rectangle(itemX, itemY, CreativePaletteManager.PALETTE_ITEM_SIZE + 8, CreativePaletteManager.PALETTE_ITEM_SIZE + 8);
            if (itemRect.contains(x, y)) {
                paletteManager.setSelectedPaletteIndex(i);
                return;
            }

            col++;
            itemX += CreativePaletteManager.PALETTE_ITEM_SIZE + 12;

            if (col >= CreativePaletteManager.PALETTE_ITEMS_PER_ROW) {
                col = 0;
                itemX = 10;
                itemY += CreativePaletteManager.PALETTE_ITEM_SIZE + 12;
            }
        }
    }

    /**
     * Place an entity at the current mouse position
     */
    private void placeEntity() {
        PaletteItem selected = paletteManager.getSelectedPaletteItem();
        if (selected == null) return;

        int placeX, placeY;

        switch (paletteManager.getCurrentCategory()) {
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
                BufferedImage blockIcon = paletteManager.getBlockTextures().get(blockType);
                PlacedEntity block = new PlacedEntity(placeX, placeY, "block", blockType, blockIcon);
                block.gridX = placeX / GRID_SIZE;
                block.gridY = placeY / GRID_SIZE;
                placedBlocks.add(block);
                break;

            case MOVING_BLOCKS:
                // Snap to grid
                placeX = (worldMouseX / GRID_SIZE) * GRID_SIZE;
                placeY = (worldMouseY / GRID_SIZE) * GRID_SIZE;

                // Check if moving block already exists at this position
                for (PlacedEntity entity : placedMovingBlocks) {
                    if (entity.x == placeX && entity.y == placeY) {
                        return; // Block already exists
                    }
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> movingData = new HashMap<>((Map<String, Object>) selected.data);
                // Set default end position based on pattern
                String pattern = (String) movingData.get("movementPattern");
                int gridX = placeX / GRID_SIZE;
                int gridY = placeY / GRID_SIZE;
                if ("HORIZONTAL".equals(pattern)) {
                    movingData.put("endX", gridX + 3);  // 3 blocks to the right
                    movingData.put("endY", gridY);
                } else if ("VERTICAL".equals(pattern)) {
                    movingData.put("endX", gridX);
                    movingData.put("endY", gridY + 3);  // 3 blocks down
                } else if ("CIRCULAR".equals(pattern)) {
                    movingData.put("endX", gridX);      // Center X
                    movingData.put("endY", gridY);      // Center Y
                }
                movingData.put("startGridX", gridX);
                movingData.put("startGridY", gridY);

                PlacedEntity movingBlock = new PlacedEntity(placeX, placeY, "moving_block", movingData, selected.icon);
                movingBlock.gridX = gridX;
                movingBlock.gridY = gridY;
                placedMovingBlocks.add(movingBlock);
                setStatus("Placed moving block (" + pattern + ") - Right-click to remove");
                break;

            case OVERLAYS:
                // Snap to grid
                placeX = (worldMouseX / GRID_SIZE) * GRID_SIZE;
                placeY = (worldMouseY / GRID_SIZE) * GRID_SIZE;

                BlockOverlay overlayType = (BlockOverlay) selected.data;

                // Find block at this position and apply overlay
                boolean appliedToBlock = false;
                for (PlacedEntity entity : placedBlocks) {
                    if (entity.x == placeX && entity.y == placeY) {
                        entity.overlay = overlayType.name();
                        appliedToBlock = true;
                        setStatus("Applied " + overlayType.getDisplayName() + " overlay to block");
                        break;
                    }
                }

                // Also check moving blocks
                if (!appliedToBlock) {
                    for (PlacedEntity entity : placedMovingBlocks) {
                        if (entity.x == placeX && entity.y == placeY) {
                            entity.overlay = overlayType.name();
                            appliedToBlock = true;
                            setStatus("Applied " + overlayType.getDisplayName() + " overlay to moving block");
                            break;
                        }
                    }
                }

                if (!appliedToBlock) {
                    setStatus("No block at this position - place a block first");
                }
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

            case INTERACTIVE:
                @SuppressWarnings("unchecked")
                Map<String, Object> interactiveData = (Map<String, Object>) selected.data;
                String interactiveType = (String) interactiveData.get("type");

                if ("door".equals(interactiveType)) {
                    // Place door (snap to grid vertically, free horizontal)
                    placeX = worldMouseX - 32;
                    placeY = (worldMouseY / GRID_SIZE) * GRID_SIZE;

                    Map<String, Object> doorData = new HashMap<>(interactiveData);
                    doorData.put("linkId", "door_" + System.currentTimeMillis());
                    PlacedEntity door = new PlacedEntity(placeX, placeY, "door", doorData, selected.icon);
                    placedDoors.add(door);
                    setStatus("Placed door - Press W near it to configure level transition");
                } else if ("button".equals(interactiveType)) {
                    // Place button
                    placeX = worldMouseX - 16;
                    placeY = worldMouseY - 8;

                    Map<String, Object> buttonData = new HashMap<>(interactiveData);
                    buttonData.put("linkId", "button_" + System.currentTimeMillis());
                    PlacedEntity button = new PlacedEntity(placeX, placeY, "button", buttonData, selected.icon);
                    placedButtons.add(button);
                    setStatus("Placed button - Press E near it to configure action");
                } else if ("vault".equals(interactiveType)) {
                    // Place vault/chest
                    placeX = worldMouseX - 32;
                    placeY = worldMouseY - 32;

                    Map<String, Object> vaultData = new HashMap<>(interactiveData);
                    vaultData.put("linkId", "vault_" + System.currentTimeMillis());
                    PlacedEntity vault = new PlacedEntity(placeX, placeY, "vault", vaultData, selected.icon);
                    placedVaults.add(vault);
                    setStatus("Placed vault - Player can open with E key");
                }
                break;

            case PARALLAX:
                // For parallax, clicking toggles the layer or opens config dialog if active
                @SuppressWarnings("unchecked")
                Map<String, String> parallaxData = (Map<String, String>) selected.data;
                String layerName = parallaxData.get("name");

                // Check if layer already exists
                ParallaxLayerEntry existingLayer = null;
                for (ParallaxLayerEntry layer : parallaxLayers) {
                    if (layer.name.equals(layerName)) {
                        existingLayer = layer;
                        break;
                    }
                }

                if (existingLayer != null) {
                    // Layer exists - show config dialog for editing placement
                    showParallaxConfigDialog(existingLayer, selected.displayName);
                } else {
                    // Add the layer with default properties from palette
                    double scale = Double.parseDouble(parallaxData.getOrDefault("scale", "10.0"));
                    double opacity = Double.parseDouble(parallaxData.getOrDefault("opacity", "1.0"));
                    int offsetX = Integer.parseInt(parallaxData.getOrDefault("offsetX", "0"));
                    int offsetY = Integer.parseInt(parallaxData.getOrDefault("offsetY", "0"));
                    String positionLabel = parallaxData.getOrDefault("positionLabel", "");

                    double scrollSpeed = Double.parseDouble(parallaxData.get("scrollSpeed"));
                    ParallaxLayerEntry newLayer = new ParallaxLayerEntry(
                        layerName,
                        parallaxData.get("path"),
                        scrollSpeed,
                        scrollSpeed,  // scrollSpeedY defaults to match scrollSpeedX
                        Integer.parseInt(parallaxData.get("zOrder")),
                        scale, opacity, offsetX, offsetY, positionLabel,
                        selected.icon
                    );
                    parallaxLayers.add(newLayer);
                    // Sort by z-order
                    parallaxLayers.sort((a, b) -> Integer.compare(a.zOrder, b.zOrder));
                    setStatus("Added parallax layer: " + selected.displayName + " - Click again to configure");
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

        // Check moving blocks
        iter = placedMovingBlocks.iterator();
        while (iter.hasNext()) {
            PlacedEntity entity = iter.next();
            if (entity.getBounds().contains(worldX, worldY)) {
                iter.remove();
                setStatus("Removed moving block");
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

        // Check doors
        iter = placedDoors.iterator();
        while (iter.hasNext()) {
            PlacedEntity entity = iter.next();
            Rectangle bounds = new Rectangle(entity.x, entity.y, 64, 128);
            if (bounds.contains(worldX, worldY)) {
                iter.remove();
                setStatus("Removed door");
                return;
            }
        }

        // Check buttons
        iter = placedButtons.iterator();
        while (iter.hasNext()) {
            PlacedEntity entity = iter.next();
            Rectangle bounds = new Rectangle(entity.x, entity.y, 32, 16);
            if (bounds.contains(worldX, worldY)) {
                iter.remove();
                setStatus("Removed button");
                return;
            }
        }

        // Check vaults
        iter = placedVaults.iterator();
        while (iter.hasNext()) {
            PlacedEntity entity = iter.next();
            Rectangle bounds = new Rectangle(entity.x, entity.y, 64, 64);
            if (bounds.contains(worldX, worldY)) {
                iter.remove();
                setStatus("Removed vault");
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
        levelData.doors.clear();
        levelData.buttons.clear();
        levelData.vaults.clear();
        levelData.movingBlocks.clear();

        // Add blocks
        for (PlacedEntity entity : placedBlocks) {
            LevelData.BlockData blockData = new LevelData.BlockData();
            blockData.x = entity.gridX;
            blockData.y = entity.gridY;
            blockData.blockType = ((BlockType) entity.data).name();
            blockData.useGridCoords = true;
            if (entity.hasOverlay()) {
                blockData.overlay = entity.overlay;
            }
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
            // Frog uses a variant subdirectory for its animations
            String subType = mobInfo.get("subType");
            if ("frog".equals(subType)) {
                mobData.spriteDir = "assets/mobs/frog/purple_frog";
            } else {
                mobData.spriteDir = "assets/mobs/" + subType;
            }
            levelData.mobs.add(mobData);
        }

        // Add lights
        for (PlacedEntity entity : placedLights) {
            LevelData.LightSourceData lightData = new LevelData.LightSourceData(
                entity.x, entity.y, (String) entity.data
            );
            levelData.lightSources.add(lightData);
        }

        // Add parallax layers with full placement properties
        for (ParallaxLayerEntry layer : parallaxLayers) {
            LevelData.ParallaxLayerData parallaxData = new LevelData.ParallaxLayerData();
            parallaxData.name = layer.name;
            parallaxData.imagePath = layer.imagePath;
            parallaxData.scrollSpeedX = layer.scrollSpeedX;
            parallaxData.scrollSpeedY = layer.scrollSpeedY;
            parallaxData.zOrder = layer.zOrder;
            parallaxData.scale = layer.scale;
            parallaxData.opacity = layer.opacity;
            parallaxData.offsetX = layer.offsetX;
            parallaxData.offsetY = layer.offsetY;
            parallaxData.tileHorizontal = true;
            parallaxData.tileVertical = false;  // Only tile horizontally, not vertically
            parallaxData.anchorBottom = true;  // Anchor to bottom for proper positioning
            levelData.parallaxLayers.add(parallaxData);
        }

        // Add doors
        for (PlacedEntity entity : placedDoors) {
            @SuppressWarnings("unchecked")
            Map<String, Object> doorInfo = (Map<String, Object>) entity.data;
            LevelData.DoorData doorData = new LevelData.DoorData();
            doorData.x = entity.x;
            doorData.y = entity.y;
            doorData.width = 64;
            doorData.height = 128;
            doorData.texturePath = (String) doorInfo.get("texturePath");
            doorData.linkId = (String) doorInfo.get("linkId");
            doorData.actionType = (String) doorInfo.getOrDefault("actionType", "none");
            doorData.actionTarget = (String) doorInfo.getOrDefault("actionTarget", "");
            levelData.doors.add(doorData);
        }

        // Add buttons
        for (PlacedEntity entity : placedButtons) {
            @SuppressWarnings("unchecked")
            Map<String, Object> buttonInfo = (Map<String, Object>) entity.data;
            LevelData.ButtonData buttonData = new LevelData.ButtonData();
            buttonData.x = entity.x;
            buttonData.y = entity.y;
            buttonData.width = 32;
            buttonData.height = 16;
            buttonData.texturePath = (String) buttonInfo.get("texturePath");
            buttonData.linkId = (String) buttonInfo.get("linkId");
            buttonData.buttonType = (String) buttonInfo.getOrDefault("buttonType", "toggle");
            buttonData.actionType = (String) buttonInfo.getOrDefault("actionType", "none");
            buttonData.actionTarget = (String) buttonInfo.getOrDefault("actionTarget", "");
            levelData.buttons.add(buttonData);
        }

        // Add vaults
        for (PlacedEntity entity : placedVaults) {
            @SuppressWarnings("unchecked")
            Map<String, Object> vaultInfo = (Map<String, Object>) entity.data;
            LevelData.VaultData vaultData = new LevelData.VaultData();
            vaultData.x = entity.x;
            vaultData.y = entity.y;
            vaultData.width = 64;
            vaultData.height = 64;
            vaultData.texturePath = (String) vaultInfo.get("texturePath");
            vaultData.linkId = (String) vaultInfo.get("linkId");
            vaultData.vaultType = (String) vaultInfo.getOrDefault("vaultType", "PLAYER_VAULT");
            levelData.vaults.add(vaultData);
        }

        // Add moving blocks
        for (PlacedEntity entity : placedMovingBlocks) {
            @SuppressWarnings("unchecked")
            Map<String, Object> movingInfo = (Map<String, Object>) entity.data;
            LevelData.MovingBlockData movingData = new LevelData.MovingBlockData();
            movingData.x = entity.gridX;
            movingData.y = entity.gridY;
            movingData.blockType = ((BlockType) movingInfo.get("blockType")).name();
            movingData.useGridCoords = true;
            movingData.movementPattern = (String) movingInfo.get("movementPattern");
            movingData.endX = (Integer) movingInfo.getOrDefault("endX", entity.gridX + 3);
            movingData.endY = (Integer) movingInfo.getOrDefault("endY", entity.gridY);
            movingData.speed = ((Number) movingInfo.getOrDefault("speed", 2.0)).doubleValue();
            movingData.pauseTime = ((Number) movingInfo.getOrDefault("pauseTime", 30)).intValue();
            movingData.radius = ((Number) movingInfo.getOrDefault("radius", 100.0)).doubleValue();
            if (entity.hasOverlay()) {
                movingData.overlay = entity.overlay;
            }
            levelData.movingBlocks.add(movingData);
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

                // Parallax layers with placement properties
                writer.println("  \"parallaxEnabled\": " + levelData.parallaxEnabled + ",");
                writer.println("  \"parallaxLayers\": [");
                for (int i = 0; i < levelData.parallaxLayers.size(); i++) {
                    LevelData.ParallaxLayerData p = levelData.parallaxLayers.get(i);
                    String comma = (i < levelData.parallaxLayers.size() - 1) ? "," : "";
                    writer.println("    {\"name\": \"" + escapeJson(p.name) +
                        "\", \"imagePath\": \"" + escapeJson(p.imagePath) +
                        "\", \"scrollSpeedX\": " + p.scrollSpeedX +
                        ", \"scrollSpeedY\": " + p.scrollSpeedY +
                        ", \"zOrder\": " + p.zOrder +
                        ", \"scale\": " + p.scale +
                        ", \"opacity\": " + p.opacity +
                        ", \"offsetX\": " + p.offsetX +
                        ", \"offsetY\": " + p.offsetY +
                        ", \"tileHorizontal\": " + p.tileHorizontal +
                        ", \"tileVertical\": " + p.tileVertical +
                        ", \"anchorBottom\": " + p.anchorBottom + "}" + comma);
                }
                writer.println("  ],");
                writer.println();

                // Doors
                writer.println("  \"doors\": [");
                for (int i = 0; i < levelData.doors.size(); i++) {
                    LevelData.DoorData d = levelData.doors.get(i);
                    String comma = (i < levelData.doors.size() - 1) ? "," : "";
                    writer.println("    {\"x\": " + d.x + ", \"y\": " + d.y +
                        ", \"width\": " + d.width + ", \"height\": " + d.height +
                        ", \"texturePath\": \"" + escapeJson(d.texturePath) +
                        "\", \"linkId\": \"" + escapeJson(d.linkId) +
                        "\", \"actionType\": \"" + escapeJson(d.actionType) +
                        "\", \"actionTarget\": \"" + escapeJson(d.actionTarget) + "\"}" + comma);
                }
                writer.println("  ],");
                writer.println();

                // Buttons
                writer.println("  \"buttons\": [");
                for (int i = 0; i < levelData.buttons.size(); i++) {
                    LevelData.ButtonData b = levelData.buttons.get(i);
                    String comma = (i < levelData.buttons.size() - 1) ? "," : "";
                    writer.println("    {\"x\": " + b.x + ", \"y\": " + b.y +
                        ", \"width\": " + b.width + ", \"height\": " + b.height +
                        ", \"texturePath\": \"" + escapeJson(b.texturePath) +
                        "\", \"linkId\": \"" + escapeJson(b.linkId) +
                        "\", \"buttonType\": \"" + escapeJson(b.buttonType) +
                        "\", \"actionType\": \"" + escapeJson(b.actionType) +
                        "\", \"actionTarget\": \"" + escapeJson(b.actionTarget) + "\"}" + comma);
                }
                writer.println("  ],");
                writer.println();

                // Vaults
                writer.println("  \"vaults\": [");
                for (int i = 0; i < levelData.vaults.size(); i++) {
                    LevelData.VaultData v = levelData.vaults.get(i);
                    String comma = (i < levelData.vaults.size() - 1) ? "," : "";
                    writer.println("    {\"x\": " + v.x + ", \"y\": " + v.y +
                        ", \"width\": " + v.width + ", \"height\": " + v.height +
                        ", \"texturePath\": \"" + escapeJson(v.texturePath) +
                        "\", \"linkId\": \"" + escapeJson(v.linkId) +
                        "\", \"vaultType\": \"" + escapeJson(v.vaultType) + "\"}" + comma);
                }
                writer.println("  ],");
                writer.println();

                // Moving Blocks
                writer.println("  \"movingBlocks\": [");
                for (int i = 0; i < levelData.movingBlocks.size(); i++) {
                    LevelData.MovingBlockData mb = levelData.movingBlocks.get(i);
                    String comma = (i < levelData.movingBlocks.size() - 1) ? "," : "";
                    writer.println("    {\"x\": " + mb.x + ", \"y\": " + mb.y +
                        ", \"blockType\": \"" + escapeJson(mb.blockType) +
                        "\", \"useGridCoords\": " + mb.useGridCoords +
                        ", \"movementPattern\": \"" + escapeJson(mb.movementPattern) +
                        "\", \"endX\": " + mb.endX + ", \"endY\": " + mb.endY +
                        ", \"speed\": " + mb.speed + ", \"pauseTime\": " + mb.pauseTime +
                        ", \"radius\": " + mb.radius + "}" + comma);
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

                PlacedEntity entity = new PlacedEntity(px, py, "block", type, paletteManager.getBlockTextures().get(type));
                entity.gridX = b.useGridCoords ? b.x : b.x / GRID_SIZE;
                entity.gridY = b.useGridCoords ? b.y : b.y / GRID_SIZE;
                if (b.hasOverlay()) {
                    entity.overlay = b.overlay;
                }
                placedBlocks.add(entity);
            }

            for (LevelData.ItemData i : levelData.items) {
                BufferedImage icon = paletteManager.createItemIcon(i.itemId != null ? i.itemId : "unknown");
                PlacedEntity entity = new PlacedEntity(i.x, i.y, "item", i.itemId, icon);
                placedItems.add(entity);
            }

            for (LevelData.MobData m : levelData.mobs) {
                BufferedImage icon = paletteManager.createMobIcon(m.subType != null ? m.subType : "zombie");
                Map<String, String> mobData = new HashMap<>();
                mobData.put("mobType", m.mobType);
                mobData.put("subType", m.subType);
                mobData.put("behavior", m.behavior);
                PlacedEntity entity = new PlacedEntity(m.x, m.y, "mob", mobData, icon);
                placedMobs.add(entity);
            }

            for (LevelData.LightSourceData l : levelData.lightSources) {
                BufferedImage icon = paletteManager.createLightIcon(l.lightType);
                PlacedEntity entity = new PlacedEntity(l.x, l.y, "light", l.lightType, icon);
                placedLights.add(entity);
            }

            // Restore parallax layers with full placement properties
            parallaxLayers.clear();
            for (LevelData.ParallaxLayerData p : levelData.parallaxLayers) {
                BufferedImage icon = paletteManager.createParallaxIcon(p.imagePath);
                // Determine position label based on zOrder
                String positionLabel = getPositionLabelForZOrder(p.zOrder);
                ParallaxLayerEntry entry = new ParallaxLayerEntry(
                    p.name, p.imagePath, p.scrollSpeedX, p.scrollSpeedY, p.zOrder,
                    p.scale, p.opacity, p.offsetX, p.offsetY, positionLabel, icon
                );
                parallaxLayers.add(entry);
            }
            // Sort by z-order
            parallaxLayers.sort((a, b) -> Integer.compare(a.zOrder, b.zOrder));

            // Load doors
            placedDoors.clear();
            for (LevelData.DoorData d : levelData.doors) {
                Map<String, Object> doorData = new HashMap<>();
                doorData.put("type", "door");
                doorData.put("texturePath", d.texturePath);
                doorData.put("linkId", d.linkId);
                doorData.put("actionType", d.actionType);
                doorData.put("actionTarget", d.actionTarget);
                BufferedImage icon = paletteManager.createDoorIcon(d.texturePath);
                PlacedEntity entity = new PlacedEntity(d.x, d.y, "door", doorData, icon);
                placedDoors.add(entity);
            }

            // Load buttons
            placedButtons.clear();
            for (LevelData.ButtonData b : levelData.buttons) {
                Map<String, Object> buttonData = new HashMap<>();
                buttonData.put("type", "button");
                buttonData.put("texturePath", b.texturePath);
                buttonData.put("linkId", b.linkId);
                buttonData.put("buttonType", b.buttonType);
                buttonData.put("actionType", b.actionType);
                buttonData.put("actionTarget", b.actionTarget);
                BufferedImage icon = paletteManager.createButtonIcon(b.texturePath);
                PlacedEntity entity = new PlacedEntity(b.x, b.y, "button", buttonData, icon);
                placedButtons.add(entity);
            }

            // Load vaults
            placedVaults.clear();
            for (LevelData.VaultData v : levelData.vaults) {
                Map<String, Object> vaultData = new HashMap<>();
                vaultData.put("type", "vault");
                vaultData.put("texturePath", v.texturePath);
                vaultData.put("linkId", v.linkId);
                vaultData.put("vaultType", v.vaultType);
                BufferedImage icon = paletteManager.createVaultIcon(v.texturePath);
                PlacedEntity entity = new PlacedEntity(v.x, v.y, "vault", vaultData, icon);
                placedVaults.add(entity);
            }

            // Load moving blocks
            placedMovingBlocks.clear();
            for (LevelData.MovingBlockData mb : levelData.movingBlocks) {
                BlockType type = BlockType.fromName(mb.blockType);
                int px = mb.useGridCoords ? mb.x * GRID_SIZE : mb.x;
                int py = mb.useGridCoords ? mb.y * GRID_SIZE : mb.y;

                // Create moving data map
                Map<String, Object> movingData = new HashMap<>();
                movingData.put("blockType", type);
                movingData.put("movementPattern", mb.movementPattern);
                movingData.put("endX", mb.endX);
                movingData.put("endY", mb.endY);
                movingData.put("speed", mb.speed);
                movingData.put("pauseTime", mb.pauseTime);
                movingData.put("radius", mb.radius);

                // Create icon with pattern overlay
                BufferedImage baseIcon = paletteManager.getBlockTextures().get(type);
                BufferedImage icon = paletteManager.createMovingBlockIcon(baseIcon, mb.movementPattern);

                PlacedEntity entity = new PlacedEntity(px, py, "moving_block", movingData, icon);
                entity.gridX = mb.useGridCoords ? mb.x : mb.x / GRID_SIZE;
                entity.gridY = mb.useGridCoords ? mb.y : mb.y / GRID_SIZE;
                if (mb.hasOverlay()) {
                    entity.overlay = mb.overlay;
                }
                placedMovingBlocks.add(entity);
            }

            // Update camera bounds
            camera.setLevelBounds(levelData.levelWidth, levelData.levelHeight);

            setStatus("Loaded level: " + filepath + " (" +
                placedBlocks.size() + " blocks, " +
                placedMovingBlocks.size() + " moving blocks, " +
                placedMobs.size() + " mobs, " +
                placedDoors.size() + " doors, " +
                placedButtons.size() + " buttons, " +
                placedVaults.size() + " vaults)");

        } catch (Exception e) {
            setStatus("Error loading level: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show the new level properties dialog with block-based size selection
     */
    public void showNewLevelDialog() {
        final int BLOCK_SIZE = 64; // Standard block size in pixels

        JDialog dialog = new JDialog((Frame) null, "New Creative Level", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Level name
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Level Name:"), gbc);
        JTextField nameField = new JTextField("My Creative Level", 20);
        gbc.gridx = 1; gbc.gridwidth = 2;
        dialog.add(nameField, gbc);
        gbc.gridwidth = 1;

        // Size presets dropdown
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Size Preset:"), gbc);
        String[] presets = {
            "Custom",
            "Small (30x17 blocks)",
            "Medium (60x17 blocks)",
            "Large (100x17 blocks)",
            "Extra Large (150x25 blocks)",
            "Vertical (30x40 blocks)",
            "Square (50x50 blocks)"
        };
        JComboBox<String> presetCombo = new JComboBox<>(presets);
        presetCombo.setSelectedIndex(2); // Default to Medium
        gbc.gridx = 1; gbc.gridwidth = 2;
        dialog.add(presetCombo, gbc);
        gbc.gridwidth = 1;

        // Width in blocks
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Width (blocks):"), gbc);
        JSpinner widthBlocksSpinner = new JSpinner(new SpinnerNumberModel(60, 10, 500, 5));
        gbc.gridx = 1;
        dialog.add(widthBlocksSpinner, gbc);
        JLabel widthPixelsLabel = new JLabel("= 3840 px");
        widthPixelsLabel.setForeground(new Color(100, 100, 100));
        gbc.gridx = 2;
        dialog.add(widthPixelsLabel, gbc);

        // Height in blocks
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Height (blocks):"), gbc);
        JSpinner heightBlocksSpinner = new JSpinner(new SpinnerNumberModel(17, 10, 200, 5));
        gbc.gridx = 1;
        dialog.add(heightBlocksSpinner, gbc);
        JLabel heightPixelsLabel = new JLabel("= 1088 px");
        heightPixelsLabel.setForeground(new Color(100, 100, 100));
        gbc.gridx = 2;
        dialog.add(heightPixelsLabel, gbc);

        // Update pixel labels when spinners change
        widthBlocksSpinner.addChangeListener(e -> {
            int blocks = (Integer) widthBlocksSpinner.getValue();
            widthPixelsLabel.setText("= " + (blocks * BLOCK_SIZE) + " px");
            presetCombo.setSelectedIndex(0); // Switch to Custom
        });
        heightBlocksSpinner.addChangeListener(e -> {
            int blocks = (Integer) heightBlocksSpinner.getValue();
            heightPixelsLabel.setText("= " + (blocks * BLOCK_SIZE) + " px");
            presetCombo.setSelectedIndex(0); // Switch to Custom
        });

        // Preset selection handler
        presetCombo.addActionListener(e -> {
            int idx = presetCombo.getSelectedIndex();
            switch (idx) {
                case 1: // Small
                    widthBlocksSpinner.setValue(30);
                    heightBlocksSpinner.setValue(17);
                    break;
                case 2: // Medium
                    widthBlocksSpinner.setValue(60);
                    heightBlocksSpinner.setValue(17);
                    break;
                case 3: // Large
                    widthBlocksSpinner.setValue(100);
                    heightBlocksSpinner.setValue(17);
                    break;
                case 4: // Extra Large
                    widthBlocksSpinner.setValue(150);
                    heightBlocksSpinner.setValue(25);
                    break;
                case 5: // Vertical
                    widthBlocksSpinner.setValue(30);
                    heightBlocksSpinner.setValue(40);
                    break;
                case 6: // Square
                    widthBlocksSpinner.setValue(50);
                    heightBlocksSpinner.setValue(50);
                    break;
            }
        });

        // Ground Y (auto-calculated based on height)
        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Ground Y:"), gbc);
        JTextField groundField = new JTextField("920", 10);
        gbc.gridx = 1;
        dialog.add(groundField, gbc);
        JLabel groundHint = new JLabel("(from top)");
        groundHint.setForeground(new Color(100, 100, 100));
        gbc.gridx = 2;
        dialog.add(groundHint, gbc);

        // Auto-update ground Y when height changes
        heightBlocksSpinner.addChangeListener(e -> {
            int heightBlocks = (Integer) heightBlocksSpinner.getValue();
            int heightPx = heightBlocks * BLOCK_SIZE;
            // Ground at 2.5 blocks from bottom
            int suggestedGround = heightPx - (int)(2.5 * BLOCK_SIZE);
            groundField.setText(String.valueOf(suggestedGround));
        });

        // Scrolling enabled
        gbc.gridx = 0; gbc.gridy = 5;
        dialog.add(new JLabel("Enable Scrolling:"), gbc);
        JCheckBox scrollCheck = new JCheckBox("", true);
        gbc.gridx = 1;
        dialog.add(scrollCheck, gbc);

        // Vertical scroll enabled
        gbc.gridx = 0; gbc.gridy = 6;
        dialog.add(new JLabel("Vertical Scrolling:"), gbc);
        JCheckBox vertScrollCheck = new JCheckBox("", true);
        gbc.gridx = 1;
        dialog.add(vertScrollCheck, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton createBtn = new JButton("Create Level");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 3;
        dialog.add(buttonPanel, gbc);

        // Initialize with Medium preset values
        widthBlocksSpinner.setValue(60);
        heightBlocksSpinner.setValue(17);

        createBtn.addActionListener(e -> {
            try {
                int widthBlocks = (Integer) widthBlocksSpinner.getValue();
                int heightBlocks = (Integer) heightBlocksSpinner.getValue();

                levelData.name = nameField.getText();
                levelData.levelWidth = widthBlocks * BLOCK_SIZE;
                levelData.levelHeight = heightBlocks * BLOCK_SIZE;
                levelData.groundY = Integer.parseInt(groundField.getText());
                levelData.scrollingEnabled = scrollCheck.isSelected();
                levelData.verticalScrollEnabled = vertScrollCheck.isSelected();
                levelData.playerSpawnX = 200;
                levelData.playerSpawnY = levelData.groundY - 70;

                camera.setLevelBounds(levelData.levelWidth, levelData.levelHeight);

                // Clear any existing entities
                placedBlocks.clear();
                placedItems.clear();
                placedMobs.clear();
                placedLights.clear();
                placedDoors.clear();
                placedButtons.clear();
                placedVaults.clear();
                parallaxLayers.clear();

                showPropertiesDialog = false;
                dialog.dispose();
                setStatus("Created level: " + levelData.name + " (" + widthBlocks + "x" + heightBlocks + " blocks)");
            } catch (NumberFormatException ex) {
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

    /**
     * Show configuration dialog for editing a parallax layer's placement properties
     */
    /**
     * Get a human-readable position label based on z-order depth
     */
    private String getPositionLabelForZOrder(int zOrder) {
        if (zOrder <= -5) return "Furthest (Sky)";
        if (zOrder == -4) return "Very Far";
        if (zOrder == -3) return "Far";
        if (zOrder == -2) return "Near";
        if (zOrder == -1) return "Behind Player";
        if (zOrder == 0) return "Player Level";
        if (zOrder == 1) return "In Front";
        if (zOrder >= 2) return "Closest (Front)";
        return "Z: " + zOrder;
    }

    /**
     * Show configuration dialog for editing a parallax layer's placement properties
     */
    private void showParallaxConfigDialog(ParallaxLayerEntry layer, String displayName) {
        JDialog dialog = new JDialog((Frame) null, "Configure Parallax Layer", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Layer info header
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel headerLabel = new JLabel(displayName);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dialog.add(headerLabel, gbc);
        gbc.gridwidth = 1;

        // Position label
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Depth Position:"), gbc);
        gbc.gridx = 1;
        String posInfo = layer.positionLabel.isEmpty() ? "Z-Order: " + layer.zOrder : layer.positionLabel + " (Z: " + layer.zOrder + ")";
        dialog.add(new JLabel(posInfo), gbc);

        // Offset X
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Offset X (pixels):"), gbc);
        JTextField offsetXField = new JTextField(String.valueOf(layer.offsetX), 10);
        gbc.gridx = 1;
        dialog.add(offsetXField, gbc);

        // Offset Y
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Offset Y (pixels):"), gbc);
        JTextField offsetYField = new JTextField(String.valueOf(layer.offsetY), 10);
        gbc.gridx = 1;
        dialog.add(offsetYField, gbc);

        // Scroll Speed X
        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Scroll Speed X (0.0-2.0):"), gbc);
        JTextField scrollSpeedXField = new JTextField(String.format("%.2f", layer.scrollSpeedX), 10);
        gbc.gridx = 1;
        dialog.add(scrollSpeedXField, gbc);

        // Scroll Speed Y
        gbc.gridx = 0; gbc.gridy = 5;
        dialog.add(new JLabel("Scroll Speed Y (0.0-2.0):"), gbc);
        JTextField scrollSpeedYField = new JTextField(String.format("%.2f", layer.scrollSpeedY), 10);
        gbc.gridx = 1;
        dialog.add(scrollSpeedYField, gbc);

        // Scale
        gbc.gridx = 0; gbc.gridy = 6;
        dialog.add(new JLabel("Scale:"), gbc);
        JTextField scaleField = new JTextField(String.format("%.1f", layer.scale), 10);
        gbc.gridx = 1;
        dialog.add(scaleField, gbc);

        // Opacity
        gbc.gridx = 0; gbc.gridy = 7;
        dialog.add(new JLabel("Opacity (0.0-1.0):"), gbc);
        JTextField opacityField = new JTextField(String.format("%.2f", layer.opacity), 10);
        gbc.gridx = 1;
        dialog.add(opacityField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        JButton applyBtn = new JButton("Apply");
        JButton removeBtn = new JButton("Remove Layer");
        JButton cancelBtn = new JButton("Cancel");
        removeBtn.setForeground(new Color(180, 50, 50));
        buttonPanel.add(applyBtn);
        buttonPanel.add(removeBtn);
        buttonPanel.add(cancelBtn);

        gbc.gridx = 0; gbc.gridy = 8;
        gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);

        applyBtn.addActionListener(e -> {
            try {
                layer.offsetX = Integer.parseInt(offsetXField.getText().trim());
                layer.offsetY = Integer.parseInt(offsetYField.getText().trim());
                layer.scrollSpeedX = Double.parseDouble(scrollSpeedXField.getText().trim());
                layer.scrollSpeedY = Double.parseDouble(scrollSpeedYField.getText().trim());
                layer.scale = Double.parseDouble(scaleField.getText().trim());
                layer.opacity = Math.max(0.0, Math.min(1.0, Double.parseDouble(opacityField.getText().trim())));
                dialog.dispose();
                setStatus("Updated parallax layer: " + layer.name);
            } catch (NumberFormatException ex) {
                dialog.setTitle("Error: Please enter valid numbers!");
            }
        });

        removeBtn.addActionListener(e -> {
            parallaxLayers.removeIf(l -> l.name.equals(layer.name));
            levelData.parallaxEnabled = !parallaxLayers.isEmpty();
            dialog.dispose();
            setStatus("Removed parallax layer: " + layer.name);
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

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
