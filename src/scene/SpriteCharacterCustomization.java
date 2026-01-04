package scene;

import core.*;
import entity.*;
import entity.player.*;
import animation.*;
import graphics.*;
import input.*;
import ui.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Character customization scene for sprite-based animation system.
 * Allows players to equip clothing overlays that sync with their sprite animations.
 *
 * Features:
 * - Preview character with selected equipment
 * - Tabs for each equipment category
 * - Color tinting for equipped items
 * - Hair customization with color options
 * - Skin tone selection
 * - Can be opened during gameplay
 * - Saves selections that persist across levels
 */
public class SpriteCharacterCustomization implements Scene {

    private boolean initialized;

    // Preview animation
    private SpriteAnimation previewAnimation;
    private EquipmentOverlay previewOverlay;
    private long lastUpdateTime;
    private boolean facingRight = true;

    // Equipment categories and items (now includes Hair)
    private static final String[] CATEGORIES = {
        "Hair", "Helmet", "Chest", "Belt", "Legs", "Boots", "Gloves", "Necklace", "Wristwear"
    };

    // Preset skin tone colors
    private static final Color[] SKIN_TONES = {
        new Color(255, 224, 189),  // Light
        new Color(241, 194, 155),  // Fair
        new Color(224, 172, 130),  // Medium light
        new Color(198, 134, 93),   // Medium
        new Color(168, 109, 71),   // Medium dark
        new Color(138, 85, 54),    // Dark
        new Color(100, 60, 40),    // Deep
        null                        // None (no tint)
    };
    private static final String[] SKIN_TONE_NAMES = {
        "Light", "Fair", "Med Light", "Medium", "Med Dark", "Dark", "Deep", "None"
    };

    // Preset hair colors
    private static final Color[] HAIR_COLORS = {
        new Color(40, 30, 20),      // Black
        new Color(80, 50, 30),      // Dark Brown
        new Color(139, 90, 43),     // Brown
        new Color(180, 130, 70),    // Light Brown
        new Color(255, 215, 140),   // Blonde
        new Color(220, 180, 140),   // Dirty Blonde
        new Color(180, 80, 40),     // Auburn
        new Color(200, 60, 30),     // Red
        new Color(100, 100, 110),   // Gray
        new Color(220, 220, 230),   // White/Silver
        new Color(70, 40, 90),      // Purple
        new Color(40, 80, 120),     // Blue
        null                         // None (no tint)
    };
    private static final String[] HAIR_COLOR_NAMES = {
        "Black", "Dk Brown", "Brown", "Lt Brown", "Blonde", "Dirty Bl",
        "Auburn", "Red", "Gray", "White", "Purple", "Blue", "None"
    };

    private Map<String, List<ClothingItem>> availableItems;
    private Map<String, ClothingItem> selectedItems;  // Current selections per category
    private Map<String, Color> itemTints;  // Tint colors per category

    // Saved state (persists between scene visits)
    private static Map<String, ClothingItem> savedSelections = new HashMap<>();
    private static Map<String, Color> savedTints = new HashMap<>();
    private static int savedSkinToneIndex = 7;  // Default to "None" (index 7)
    private static int savedHairColorIndex = 12; // Default to "None"

    // Current selections
    private int selectedSkinToneIndex = 7;
    private int selectedHairColorIndex = 12;

    // UI State
    private int currentCategory = 0;
    private int scrollOffset = 0;

    // UI Components
    private UIButton doneButton;
    private UIButton clearAllButton;
    private UIButton[] categoryTabs;
    private UISlider redSlider;
    private UISlider greenSlider;
    private UISlider blueSlider;

    // Colors
    private static final Color BG_COLOR = new Color(30, 30, 45);
    private static final Color PANEL_COLOR = new Color(40, 40, 55, 220);
    private static final Color SELECTED_COLOR = new Color(70, 130, 180);
    private static final Color HOVER_COLOR = new Color(60, 60, 80);

    // ==================== NEW LAYOUT CONSTANTS ====================
    // Screen is 1920x1080 - utilize the full space

    // Left Panel - Character Preview
    private static final int LEFT_PANEL_X = 60;
    private static final int LEFT_PANEL_Y = 110;
    private static final int LEFT_PANEL_WIDTH = 320;
    private static final int LEFT_PANEL_HEIGHT = 520;

    // Center Panel - Equipment Selection
    private static final int CENTER_PANEL_X = 420;
    private static final int CENTER_PANEL_Y = 110;
    private static final int CENTER_PANEL_WIDTH = 680;

    // Category tabs layout
    private static final int TABS_Y = 115;
    private static final int TAB_WIDTH = 95;
    private static final int TAB_HEIGHT = 38;
    private static final int TABS_PER_ROW = 9;

    // Items grid layout
    private static final int ITEMS_X = 435;
    private static final int ITEMS_Y = 175;
    private static final int ITEM_SIZE = 90;
    private static final int ITEMS_PER_ROW = 6;
    private static final int ITEMS_VISIBLE_ROWS = 4;

    // Right Panel - Character Options (Skin Tone, Hair Color, Tint)
    private static final int RIGHT_PANEL_X = 1140;
    private static final int RIGHT_PANEL_Y = 220;
    private static final int RIGHT_PANEL_WIDTH = 340;

    // Preview dimensions
    private static final int PREVIEW_SCALE = 4;
    private static final int CHAR_WIDTH = 32 * PREVIEW_SCALE;
    private static final int CHAR_HEIGHT = 64 * PREVIEW_SCALE;

    // Reference to calling scene for overlay mode
    private Scene callingScene;
    private boolean isOverlay = false;

    /**
     * Clothing item data class.
     */
    public static class ClothingItem {
        public String name;
        public String displayName;
        public String directory;
        public EquipmentOverlay.EquipmentSlot slot;
        public EquipmentOverlay.EquipmentSlot secondarySlot; // For hair (front + back)
        public BufferedImage previewImage;

        public ClothingItem(String name, String displayName, String directory,
                           EquipmentOverlay.EquipmentSlot slot) {
            this.name = name;
            this.displayName = displayName;
            this.directory = directory;
            this.slot = slot;
            this.secondarySlot = null;
        }

        public ClothingItem(String name, String displayName, String directory,
                           EquipmentOverlay.EquipmentSlot slot, EquipmentOverlay.EquipmentSlot secondarySlot) {
            this.name = name;
            this.displayName = displayName;
            this.directory = directory;
            this.slot = slot;
            this.secondarySlot = secondarySlot;
        }
    }

    public SpriteCharacterCustomization() {
        this.initialized = false;
    }

    /**
     * Set this scene to operate as an overlay on top of another scene.
     */
    public void setOverlayMode(Scene callingScene) {
        this.callingScene = callingScene;
        this.isOverlay = true;
    }

    @Override
    public void init() {
        if (initialized) return;

        System.out.println("SpriteCharacterCustomization: Initializing...");

        // Initialize preview animation
        previewAnimation = new SpriteAnimation();
        previewAnimation.loadAction(SpriteAnimation.ActionState.IDLE, "assets/player/sprites/idle.gif");
        previewAnimation.loadAction(SpriteAnimation.ActionState.WALK, "assets/player/sprites/walk.gif");
        previewAnimation.loadAction(SpriteAnimation.ActionState.JUMP, "assets/player/sprites/jump.gif");
        previewAnimation.setState(SpriteAnimation.ActionState.WALK);

        previewOverlay = new EquipmentOverlay();

        // Load available items
        loadAvailableItems();

        // Initialize selections from saved state
        selectedItems = new HashMap<>(savedSelections);
        itemTints = new HashMap<>(savedTints);
        selectedSkinToneIndex = savedSkinToneIndex;
        selectedHairColorIndex = savedHairColorIndex;

        // Apply saved selections to preview
        applySelectionsToPreview();
        applySkinToneToPreview();
        applyHairColorToPreview();

        // Create UI
        createUI();

        lastUpdateTime = System.currentTimeMillis();
        initialized = true;

        System.out.println("SpriteCharacterCustomization: Initialized with " +
                          getTotalItemCount() + " available items");
    }

    /**
     * Scans the clothing directory and loads available items.
     */
    private void loadAvailableItems() {
        availableItems = new HashMap<>();

        // Map category names to slot types and directories
        Map<String, Object[]> categoryConfig = new LinkedHashMap<>();
        // Hair uses both HAIR_FRONT and HAIR_BACK slots
        categoryConfig.put("Hair", new Object[]{EquipmentOverlay.EquipmentSlot.HAIR_FRONT, "hair", EquipmentOverlay.EquipmentSlot.HAIR_BACK});
        categoryConfig.put("Helmet", new Object[]{EquipmentOverlay.EquipmentSlot.HELMET, "helmet", null});
        categoryConfig.put("Chest", new Object[]{EquipmentOverlay.EquipmentSlot.CHEST, "chest", null});
        categoryConfig.put("Belt", new Object[]{EquipmentOverlay.EquipmentSlot.BELT, "belt", null});
        categoryConfig.put("Legs", new Object[]{EquipmentOverlay.EquipmentSlot.LEGS, "legs", null});
        categoryConfig.put("Boots", new Object[]{EquipmentOverlay.EquipmentSlot.BOOTS, "boots", null});
        categoryConfig.put("Gloves", new Object[]{EquipmentOverlay.EquipmentSlot.GLOVES, "gloves", null});
        categoryConfig.put("Necklace", new Object[]{EquipmentOverlay.EquipmentSlot.NECKLACE, "necklace", null});
        categoryConfig.put("Wristwear", new Object[]{EquipmentOverlay.EquipmentSlot.WRISTWEAR, "wristwear", null});

        for (String category : CATEGORIES) {
            Object[] config = categoryConfig.get(category);
            EquipmentOverlay.EquipmentSlot slot = (EquipmentOverlay.EquipmentSlot) config[0];
            String dirName = (String) config[1];
            EquipmentOverlay.EquipmentSlot secondarySlot = (EquipmentOverlay.EquipmentSlot) config[2];

            List<ClothingItem> items = new ArrayList<>();

            // Add "None" option
            items.add(new ClothingItem("none", "None", null, slot, secondarySlot));

            // Scan directory for items
            File dir = new File("assets/clothing/" + dirName);
            if (dir.exists() && dir.isDirectory()) {
                File[] subdirs = dir.listFiles(File::isDirectory);
                if (subdirs != null) {
                    Arrays.sort(subdirs); // Sort alphabetically
                    for (File subdir : subdirs) {
                        String itemName = subdir.getName();
                        String displayName = formatDisplayName(itemName);
                        String itemDir = subdir.getPath();

                        ClothingItem item = new ClothingItem(itemName, displayName, itemDir, slot, secondarySlot);

                        // Try to load preview image from idle.gif first frame
                        try {
                            AssetLoader.ImageAsset asset = AssetLoader.load(itemDir + "/idle.gif");
                            if (asset.staticImage != null) {
                                item.previewImage = asset.staticImage;
                            }
                        } catch (Exception e) {
                            // No preview available
                        }

                        items.add(item);
                    }
                }
            }

            availableItems.put(category, items);
        }
    }

    /**
     * Formats an item name for display (e.g., "leather_boots" -> "Leather Boots").
     */
    private String formatDisplayName(String name) {
        String[] parts = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) sb.append(" ");
            if (part.length() > 0) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    /**
     * Creates UI components.
     */
    private void createUI() {
        int buttonWidth = 160;
        int buttonHeight = 50;

        // Done button - bottom right
        doneButton = new UIButton(
            GamePanel.SCREEN_WIDTH - buttonWidth - 40,
            GamePanel.SCREEN_HEIGHT - buttonHeight - 40,
            buttonWidth, buttonHeight,
            "Done",
            this::onDoneClicked
        );
        doneButton.setColors(
            new Color(70, 130, 180, 220),
            new Color(100, 160, 210, 255),
            Color.WHITE
        );

        // Clear All button - next to Done
        clearAllButton = new UIButton(
            GamePanel.SCREEN_WIDTH - buttonWidth * 2 - 60,
            GamePanel.SCREEN_HEIGHT - buttonHeight - 40,
            buttonWidth, buttonHeight,
            "Clear All",
            this::onClearAllClicked
        );
        clearAllButton.setColors(
            new Color(150, 80, 80, 220),
            new Color(180, 100, 100, 255),
            Color.WHITE
        );

        // Category tabs - single row across the center panel
        categoryTabs = new UIButton[CATEGORIES.length];
        int tabStartX = CENTER_PANEL_X;

        for (int i = 0; i < CATEGORIES.length; i++) {
            final int index = i;
            int col = i % TABS_PER_ROW;
            int row = i / TABS_PER_ROW;

            categoryTabs[i] = new UIButton(
                tabStartX + col * (TAB_WIDTH + 5),
                TABS_Y + row * (TAB_HEIGHT + 5),
                TAB_WIDTH, TAB_HEIGHT,
                CATEGORIES[i],
                () -> selectCategory(index)
            );
            updateTabColors(i);
        }

        // Color tint sliders - in the right panel
        int sliderX = RIGHT_PANEL_X + 20;
        int sliderY = RIGHT_PANEL_Y + 380;
        int sliderWidth = RIGHT_PANEL_WIDTH - 80;

        redSlider = new UISlider(sliderX, sliderY, sliderWidth, 10, "R", 0, 255, 255);
        redSlider.setFillColor(new Color(200, 80, 80));
        redSlider.setOnChange(this::onTintChanged);

        greenSlider = new UISlider(sliderX, sliderY + 35, sliderWidth, 10, "G", 0, 255, 255);
        greenSlider.setFillColor(new Color(80, 200, 80));
        greenSlider.setOnChange(this::onTintChanged);

        blueSlider = new UISlider(sliderX, sliderY + 70, sliderWidth, 10, "B", 0, 255, 255);
        blueSlider.setFillColor(new Color(80, 80, 200));
        blueSlider.setOnChange(this::onTintChanged);
    }

    /**
     * Updates tab button colors based on selection state.
     */
    private void updateTabColors(int index) {
        if (index == currentCategory) {
            categoryTabs[index].setColors(
                SELECTED_COLOR,
                SELECTED_COLOR.brighter(),
                Color.WHITE
            );
        } else {
            categoryTabs[index].setColors(
                new Color(60, 60, 75, 220),
                HOVER_COLOR,
                new Color(200, 200, 210)
            );
        }
    }

    /**
     * Selects a category tab.
     */
    private void selectCategory(int index) {
        currentCategory = index;
        scrollOffset = 0;
        for (int i = 0; i < categoryTabs.length; i++) {
            updateTabColors(i);
        }

        // Update tint sliders to show current category's tint
        String category = CATEGORIES[currentCategory];
        Color tint = itemTints.getOrDefault(category, Color.WHITE);
        redSlider.setValue(tint.getRed());
        greenSlider.setValue(tint.getGreen());
        blueSlider.setValue(tint.getBlue());
    }

    /**
     * Applies current selections to the preview overlay.
     */
    private void applySelectionsToPreview() {
        previewOverlay.clearAll();

        for (Map.Entry<String, ClothingItem> entry : selectedItems.entrySet()) {
            ClothingItem item = entry.getValue();
            if (item.directory != null) {
                // Load and equip the item
                for (SpriteAnimation.ActionState state : new SpriteAnimation.ActionState[]{
                    SpriteAnimation.ActionState.IDLE,
                    SpriteAnimation.ActionState.WALK,
                    SpriteAnimation.ActionState.JUMP
                }) {
                    String gifPath = item.directory + "/" + state.name().toLowerCase() + ".gif";
                    previewOverlay.equipItem(item.slot, state, gifPath, item.displayName);

                    // Also equip to secondary slot if present (for hair back layer)
                    if (item.secondarySlot != null) {
                        String backGifPath = item.directory + "/" + state.name().toLowerCase() + "_back.gif";
                        File backFile = new File(backGifPath);
                        if (backFile.exists()) {
                            previewOverlay.equipItem(item.secondarySlot, state, backGifPath, item.displayName + " (back)");
                        }
                    }
                }

                // Apply tint if set
                Color tint = itemTints.get(entry.getKey());
                if (tint != null && !tint.equals(Color.WHITE)) {
                    previewOverlay.setItemTint(item.slot, tint);
                    if (item.secondarySlot != null) {
                        previewOverlay.setItemTint(item.secondarySlot, tint);
                    }
                }
            }
        }

        // Apply hair color to hair items
        applyHairColorToPreview();
    }

    /**
     * Applies the current skin tone to the preview animation.
     */
    private void applySkinToneToPreview() {
        Color skinTone = SKIN_TONES[selectedSkinToneIndex];
        previewAnimation.setTint(skinTone);
    }

    /**
     * Applies the current hair color to any equipped hair.
     */
    private void applyHairColorToPreview() {
        Color hairColor = HAIR_COLORS[selectedHairColorIndex];
        if (hairColor != null) {
            previewOverlay.setItemTint(EquipmentOverlay.EquipmentSlot.HAIR_FRONT, hairColor);
            previewOverlay.setItemTint(EquipmentOverlay.EquipmentSlot.HAIR_BACK, hairColor);
        }
    }

    /**
     * Selects a skin tone by index.
     */
    private void selectSkinTone(int index) {
        if (index >= 0 && index < SKIN_TONES.length) {
            selectedSkinToneIndex = index;
            applySkinToneToPreview();
            System.out.println("Selected skin tone: " + SKIN_TONE_NAMES[index]);
        }
    }

    /**
     * Selects a hair color by index.
     */
    private void selectHairColor(int index) {
        if (index >= 0 && index < HAIR_COLORS.length) {
            selectedHairColorIndex = index;
            applyHairColorToPreview();
            System.out.println("Selected hair color: " + HAIR_COLOR_NAMES[index]);
        }
    }

    /**
     * Called when Done button is clicked.
     */
    private void onDoneClicked() {
        // Save current selections
        savedSelections = new HashMap<>(selectedItems);
        savedTints = new HashMap<>(itemTints);
        savedSkinToneIndex = selectedSkinToneIndex;
        savedHairColorIndex = selectedHairColorIndex;

        System.out.println("SpriteCharacterCustomization: Saved " + selectedItems.size() + " items" +
                           " with skin tone: " + SKIN_TONE_NAMES[selectedSkinToneIndex] +
                           " and hair color: " + HAIR_COLOR_NAMES[selectedHairColorIndex]);

        // Return to previous scene
        if (isOverlay && callingScene != null) {
            SceneManager.getInstance().setScene(
                getSceneNameFor(callingScene),
                SceneManager.TRANSITION_NONE
            );
        } else {
            // Check if we came from a game level
            String levelPath = GameScene.getCurrentLevelPath();
            if (levelPath != null) {
                // Return to the level and apply customization
                SceneManager.getInstance().loadLevel(levelPath, SceneManager.TRANSITION_FADE);
            } else {
                SceneManager.getInstance().setScene("mainMenu", SceneManager.TRANSITION_FADE);
            }
        }
    }

    /**
     * Called when Clear All button is clicked.
     */
    private void onClearAllClicked() {
        selectedItems.clear();
        itemTints.clear();
        selectedSkinToneIndex = 7;  // Reset to "None"
        selectedHairColorIndex = 12; // Reset to "None"
        applySelectionsToPreview();
        applySkinToneToPreview();
    }

    /**
     * Called when tint sliders change.
     */
    private void onTintChanged() {
        String category = CATEGORIES[currentCategory];
        Color newTint = new Color(
            redSlider.getIntValue(),
            greenSlider.getIntValue(),
            blueSlider.getIntValue()
        );
        itemTints.put(category, newTint);

        // Apply to preview
        ClothingItem item = selectedItems.get(category);
        if (item != null && item.directory != null) {
            previewOverlay.setItemTint(item.slot, newTint);
            if (item.secondarySlot != null) {
                previewOverlay.setItemTint(item.secondarySlot, newTint);
            }
        }
    }

    /**
     * Gets the scene name for a scene instance.
     */
    private String getSceneNameFor(Scene scene) {
        if (scene instanceof GameScene) {
            return "game";
        }
        return "mainMenu";
    }

    /**
     * Gets total count of available items.
     */
    private int getTotalItemCount() {
        int count = 0;
        for (List<ClothingItem> items : availableItems.values()) {
            count += items.size();
        }
        return count;
    }

    @Override
    public void update(InputManager input) {
        if (!initialized) return;

        // Update animation timing
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Update preview animation
        previewAnimation.update(deltaMs);
        previewOverlay.syncToFrame(
            previewAnimation.getCurrentFrameIndex(),
            previewAnimation.getState()
        );

        // Toggle animation state with space
        if (input.isKeyJustPressed(' ')) {
            SpriteAnimation.ActionState current = previewAnimation.getState();
            if (current == SpriteAnimation.ActionState.IDLE) {
                previewAnimation.setState(SpriteAnimation.ActionState.WALK);
            } else if (current == SpriteAnimation.ActionState.WALK) {
                previewAnimation.setState(SpriteAnimation.ActionState.JUMP);
            } else {
                previewAnimation.setState(SpriteAnimation.ActionState.IDLE);
            }
        }

        // Toggle facing direction with A/D
        if (input.isKeyJustPressed('a')) facingRight = false;
        if (input.isKeyJustPressed('d')) facingRight = true;

        // ESC to close
        if (input.isKeyJustPressed('\u001B')) { // ESC key
            onDoneClicked();
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!initialized) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        if (isOverlay) {
            g2d.setColor(new Color(0, 0, 0, 180));
        } else {
            GradientPaint gradient = new GradientPaint(
                0, 0, BG_COLOR,
                0, GamePanel.SCREEN_HEIGHT, new Color(50, 50, 70)
            );
            g2d.setPaint(gradient);
        }
        g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        // Draw title
        g2d.setFont(new Font("Serif", Font.BOLD, 48));
        g2d.setColor(new Color(255, 220, 150));
        String title = "Character Customization";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (GamePanel.SCREEN_WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 60);

        // Draw subtitle
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.setColor(new Color(150, 150, 170));
        String subtitle = "Press SPACE to change animation  |  A/D to flip direction  |  ESC to save and exit";
        fm = g2d.getFontMetrics();
        g2d.drawString(subtitle, (GamePanel.SCREEN_WIDTH - fm.stringWidth(subtitle)) / 2, 90);

        // Draw left panel (Preview)
        drawPreviewPanel(g2d);

        // Draw category tabs
        for (UIButton tab : categoryTabs) {
            tab.draw(g);
        }

        // Draw center panel (Items grid)
        drawItemsGrid(g2d);

        // Draw right panel (Skin tone, Hair color, Tint controls)
        drawRightPanel(g2d);

        // Draw buttons
        doneButton.draw(g);
        clearAllButton.draw(g);
    }

    /**
     * Draws the character preview panel.
     */
    private void drawPreviewPanel(Graphics2D g2d) {
        // Panel background
        g2d.setColor(PANEL_COLOR);
        g2d.fillRoundRect(LEFT_PANEL_X, LEFT_PANEL_Y, LEFT_PANEL_WIDTH, LEFT_PANEL_HEIGHT, 15, 15);
        g2d.setColor(new Color(100, 100, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(LEFT_PANEL_X, LEFT_PANEL_Y, LEFT_PANEL_WIDTH, LEFT_PANEL_HEIGHT, 15, 15);

        // Label
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(new Color(200, 200, 210));
        g2d.drawString("Preview", LEFT_PANEL_X + 20, LEFT_PANEL_Y + 30);

        // Current animation state
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(150, 150, 170));
        String stateText = previewAnimation.getState().toString();
        g2d.drawString(stateText, LEFT_PANEL_X + LEFT_PANEL_WIDTH - 70, LEFT_PANEL_Y + 30);

        // Calculate character position (centered in preview area)
        int charX = LEFT_PANEL_X + (LEFT_PANEL_WIDTH - CHAR_WIDTH) / 2;
        int charY = LEFT_PANEL_Y + 50 + (LEFT_PANEL_HEIGHT - 100 - CHAR_HEIGHT) / 2;

        SpriteAnimation.ActionState state = previewAnimation.getState();

        // Draw equipment behind
        previewOverlay.drawBehind(g2d, charX, charY, CHAR_WIDTH, CHAR_HEIGHT, facingRight, state);

        // Draw base character
        previewAnimation.draw(g2d, charX, charY, CHAR_WIDTH, CHAR_HEIGHT, facingRight);

        // Draw equipment in front
        previewOverlay.drawInFront(g2d, charX, charY, CHAR_WIDTH, CHAR_HEIGHT, facingRight, state);

        // Draw direction indicator
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(new Color(120, 120, 140));
        String dirText = facingRight ? "Facing: Right →" : "← Facing: Left";
        g2d.drawString(dirText, LEFT_PANEL_X + 20, LEFT_PANEL_Y + LEFT_PANEL_HEIGHT - 15);
    }

    /**
     * Draws the items grid for the current category.
     */
    private void drawItemsGrid(Graphics2D g2d) {
        String category = CATEGORIES[currentCategory];
        List<ClothingItem> items = availableItems.get(category);
        if (items == null) return;

        ClothingItem selected = selectedItems.get(category);

        // Calculate grid dimensions
        int gridWidth = ITEMS_PER_ROW * (ITEM_SIZE + 10) + 20;
        int gridHeight = ITEMS_VISIBLE_ROWS * (ITEM_SIZE + 10) + 20;

        // Background panel
        g2d.setColor(PANEL_COLOR);
        g2d.fillRoundRect(ITEMS_X - 20, ITEMS_Y - 10, gridWidth, gridHeight, 10, 10);
        g2d.setColor(new Color(100, 100, 120));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(ITEMS_X - 20, ITEMS_Y - 10, gridWidth, gridHeight, 10, 10);

        // Category label
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(new Color(200, 200, 210));
        g2d.drawString(category + " Options", ITEMS_X - 10, ITEMS_Y + 5);

        // Draw items
        int startY = ITEMS_Y + 20;
        for (int i = 0; i < items.size() && i < ITEMS_PER_ROW * ITEMS_VISIBLE_ROWS; i++) {
            ClothingItem item = items.get(i);
            int row = i / ITEMS_PER_ROW;
            int col = i % ITEMS_PER_ROW;

            int x = ITEMS_X + col * (ITEM_SIZE + 10);
            int y = startY + row * (ITEM_SIZE + 10);

            // Item background
            boolean isSelected = (selected != null && selected.name.equals(item.name)) ||
                                (selected == null && item.name.equals("none"));

            g2d.setColor(isSelected ? SELECTED_COLOR : new Color(50, 50, 65));
            g2d.fillRoundRect(x, y, ITEM_SIZE, ITEM_SIZE, 8, 8);

            // Item border
            g2d.setColor(isSelected ? SELECTED_COLOR.brighter() : new Color(80, 80, 95));
            g2d.setStroke(new BasicStroke(isSelected ? 2 : 1));
            g2d.drawRoundRect(x, y, ITEM_SIZE, ITEM_SIZE, 8, 8);

            // Draw item preview or placeholder
            if (item.previewImage != null) {
                int imgSize = ITEM_SIZE - 20;
                g2d.drawImage(item.previewImage,
                             x + 10, y + 5,
                             imgSize, imgSize, null);
            } else if (item.name.equals("none")) {
                // Draw X for none
                g2d.setColor(new Color(150, 100, 100));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(x + 25, y + 20, x + ITEM_SIZE - 25, y + ITEM_SIZE - 25);
                g2d.drawLine(x + ITEM_SIZE - 25, y + 20, x + 25, y + ITEM_SIZE - 25);
            } else {
                // Placeholder box
                g2d.setColor(new Color(80, 80, 100));
                g2d.fillRect(x + 20, y + 15, ITEM_SIZE - 40, ITEM_SIZE - 35);
            }

            // Item name
            g2d.setFont(new Font("Arial", Font.PLAIN, 11));
            g2d.setColor(Color.WHITE);
            String displayName = item.displayName;
            if (displayName.length() > 10) {
                displayName = displayName.substring(0, 9) + "..";
            }
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (ITEM_SIZE - fm.stringWidth(displayName)) / 2;
            g2d.drawString(displayName, textX, y + ITEM_SIZE - 5);
        }
    }

    /**
     * Draws the right panel with skin tone, hair color, and tint controls.
     */
    private void drawRightPanel(Graphics2D g2d) {
        // Panel background
        g2d.setColor(PANEL_COLOR);
        g2d.fillRoundRect(RIGHT_PANEL_X, RIGHT_PANEL_Y, RIGHT_PANEL_WIDTH, 500, 15, 15);
        g2d.setColor(new Color(100, 100, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(RIGHT_PANEL_X, RIGHT_PANEL_Y, RIGHT_PANEL_WIDTH, 500, 15, 15);

        // Draw skin tone selector
        drawSkinToneSelector(g2d, RIGHT_PANEL_X + 20, RIGHT_PANEL_Y + 20);

        // Draw hair color selector
        drawHairColorSelector(g2d, RIGHT_PANEL_X + 20, RIGHT_PANEL_Y + 180);

        // Draw tint controls
        drawTintControls(g2d);
    }

    /**
     * Draws the skin tone selection panel.
     */
    private void drawSkinToneSelector(Graphics2D g2d, int panelX, int panelY) {
        int swatchSize = 32;
        int padding = 6;

        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(new Color(200, 200, 210));
        g2d.drawString("Skin Tone", panelX, panelY);

        // Draw skin tone swatches in 2 rows of 4
        int startY = panelY + 15;
        for (int i = 0; i < SKIN_TONES.length; i++) {
            int row = i / 4;
            int col = i % 4;
            int x = panelX + col * (swatchSize + padding);
            int y = startY + row * (swatchSize + padding + 18);

            // Swatch background (for "None" option)
            if (SKIN_TONES[i] == null) {
                g2d.setColor(new Color(60, 60, 75));
                g2d.fillRoundRect(x, y, swatchSize, swatchSize, 5, 5);
                // Draw X for none
                g2d.setColor(new Color(150, 100, 100));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(x + 8, y + 8, x + swatchSize - 8, y + swatchSize - 8);
                g2d.drawLine(x + swatchSize - 8, y + 8, x + 8, y + swatchSize - 8);
            } else {
                g2d.setColor(SKIN_TONES[i]);
                g2d.fillRoundRect(x, y, swatchSize, swatchSize, 5, 5);
            }

            // Selection border
            if (i == selectedSkinToneIndex) {
                g2d.setColor(SELECTED_COLOR.brighter());
                g2d.setStroke(new BasicStroke(3));
            } else {
                g2d.setColor(new Color(80, 80, 95));
                g2d.setStroke(new BasicStroke(1));
            }
            g2d.drawRoundRect(x, y, swatchSize, swatchSize, 5, 5);

            // Label below swatch
            g2d.setFont(new Font("Arial", Font.PLAIN, 9));
            g2d.setColor(new Color(180, 180, 190));
            String name = SKIN_TONE_NAMES[i];
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (swatchSize - fm.stringWidth(name)) / 2;
            g2d.drawString(name, textX, y + swatchSize + 12);
        }
    }

    /**
     * Draws the hair color selection panel.
     */
    private void drawHairColorSelector(Graphics2D g2d, int panelX, int panelY) {
        int swatchSize = 28;
        int padding = 5;

        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(new Color(200, 200, 210));
        g2d.drawString("Hair Color", panelX, panelY);

        // Draw hair color swatches in rows
        int startY = panelY + 15;
        int swatchesPerRow = 5;
        for (int i = 0; i < HAIR_COLORS.length; i++) {
            int row = i / swatchesPerRow;
            int col = i % swatchesPerRow;
            int x = panelX + col * (swatchSize + padding);
            int y = startY + row * (swatchSize + padding + 14);

            // Swatch background
            if (HAIR_COLORS[i] == null) {
                g2d.setColor(new Color(60, 60, 75));
                g2d.fillRoundRect(x, y, swatchSize, swatchSize, 4, 4);
                // Draw X for none
                g2d.setColor(new Color(150, 100, 100));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawLine(x + 6, y + 6, x + swatchSize - 6, y + swatchSize - 6);
                g2d.drawLine(x + swatchSize - 6, y + 6, x + 6, y + swatchSize - 6);
            } else {
                g2d.setColor(HAIR_COLORS[i]);
                g2d.fillRoundRect(x, y, swatchSize, swatchSize, 4, 4);
            }

            // Selection border
            if (i == selectedHairColorIndex) {
                g2d.setColor(SELECTED_COLOR.brighter());
                g2d.setStroke(new BasicStroke(2.5f));
            } else {
                g2d.setColor(new Color(80, 80, 95));
                g2d.setStroke(new BasicStroke(1));
            }
            g2d.drawRoundRect(x, y, swatchSize, swatchSize, 4, 4);

            // Label below swatch
            g2d.setFont(new Font("Arial", Font.PLAIN, 8));
            g2d.setColor(new Color(160, 160, 170));
            String name = HAIR_COLOR_NAMES[i];
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (swatchSize - fm.stringWidth(name)) / 2;
            g2d.drawString(name, textX, y + swatchSize + 10);
        }
    }

    /**
     * Draws the color tint controls.
     */
    private void drawTintControls(Graphics2D g2d) {
        int y = RIGHT_PANEL_Y + 360;

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(new Color(200, 200, 210));
        g2d.drawString("Item Color Tint", RIGHT_PANEL_X + 20, y);

        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.setColor(new Color(150, 150, 170));
        g2d.drawString("(Applies to selected " + CATEGORIES[currentCategory] + ")", RIGHT_PANEL_X + 20, y + 18);

        redSlider.draw(g2d);
        greenSlider.draw(g2d);
        blueSlider.draw(g2d);

        // Color preview box
        Color currentTint = new Color(
            redSlider.getIntValue(),
            greenSlider.getIntValue(),
            blueSlider.getIntValue()
        );
        int boxX = RIGHT_PANEL_X + RIGHT_PANEL_WIDTH - 45;
        int boxY = y + 40;
        g2d.setColor(currentTint);
        g2d.fillRoundRect(boxX, boxY, 40, 60, 5, 5);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(boxX, boxY, 40, 60, 5, 5);
    }

    @Override
    public void onMousePressed(int x, int y) {
        redSlider.handleMousePressed(x, y);
        greenSlider.handleMousePressed(x, y);
        blueSlider.handleMousePressed(x, y);
    }

    @Override
    public void onMouseReleased(int x, int y) {
        redSlider.handleMouseReleased(x, y);
        greenSlider.handleMouseReleased(x, y);
        blueSlider.handleMouseReleased(x, y);
    }

    @Override
    public void onMouseDragged(int x, int y) {
        redSlider.handleMouseDragged(x, y);
        greenSlider.handleMouseDragged(x, y);
        blueSlider.handleMouseDragged(x, y);
    }

    @Override
    public void onMouseMoved(int x, int y) {
        doneButton.handleMouseMove(x, y);
        clearAllButton.handleMouseMove(x, y);
        for (UIButton tab : categoryTabs) {
            tab.handleMouseMove(x, y);
        }
    }

    @Override
    public void onMouseClicked(int x, int y) {
        // Check buttons
        doneButton.handleClick(x, y);
        clearAllButton.handleClick(x, y);

        for (UIButton tab : categoryTabs) {
            tab.handleClick(x, y);
        }

        // Check skin tone clicks
        int skinPanelX = RIGHT_PANEL_X + 20;
        int skinPanelY = RIGHT_PANEL_Y + 35;
        int swatchSize = 32;
        int padding = 6;

        for (int i = 0; i < SKIN_TONES.length; i++) {
            int row = i / 4;
            int col = i % 4;
            int swatchX = skinPanelX + col * (swatchSize + padding);
            int swatchY = skinPanelY + row * (swatchSize + padding + 18);

            if (x >= swatchX && x < swatchX + swatchSize &&
                y >= swatchY && y < swatchY + swatchSize) {
                selectSkinTone(i);
                return;
            }
        }

        // Check hair color clicks
        int hairPanelX = RIGHT_PANEL_X + 20;
        int hairPanelY = RIGHT_PANEL_Y + 195;
        int hairSwatchSize = 28;
        int hairPadding = 5;
        int swatchesPerRow = 5;

        for (int i = 0; i < HAIR_COLORS.length; i++) {
            int row = i / swatchesPerRow;
            int col = i % swatchesPerRow;
            int swatchX = hairPanelX + col * (hairSwatchSize + hairPadding);
            int swatchY = hairPanelY + row * (hairSwatchSize + hairPadding + 14);

            if (x >= swatchX && x < swatchX + hairSwatchSize &&
                y >= swatchY && y < swatchY + hairSwatchSize) {
                selectHairColor(i);
                return;
            }
        }

        // Check item clicks
        String category = CATEGORIES[currentCategory];
        List<ClothingItem> items = availableItems.get(category);
        if (items == null) return;

        int startY = ITEMS_Y + 20;
        for (int i = 0; i < items.size() && i < ITEMS_PER_ROW * ITEMS_VISIBLE_ROWS; i++) {
            int row = i / ITEMS_PER_ROW;
            int col = i % ITEMS_PER_ROW;

            int itemX = ITEMS_X + col * (ITEM_SIZE + 10);
            int itemY = startY + row * (ITEM_SIZE + 10);

            if (x >= itemX && x < itemX + ITEM_SIZE &&
                y >= itemY && y < itemY + ITEM_SIZE) {

                ClothingItem item = items.get(i);

                if (item.name.equals("none")) {
                    // Remove selection
                    selectedItems.remove(category);
                    itemTints.remove(category);
                } else {
                    // Select this item
                    selectedItems.put(category, item);
                }

                applySelectionsToPreview();
                System.out.println("Selected: " + item.displayName + " in " + category);
                break;
            }
        }
    }

    @Override
    public void dispose() {
        System.out.println("SpriteCharacterCustomization: Disposing...");
        initialized = false;
    }

    @Override
    public String getName() {
        return "Sprite Character Customization";
    }

    // ==================== STATIC METHODS FOR APPLYING TO PLAYER ====================

    /**
     * Applies the saved customization to a SpritePlayerEntity.
     */
    public static void applyToPlayer(SpritePlayerEntity player) {
        // Apply skin tone first
        if (savedSkinToneIndex >= 0 && savedSkinToneIndex < SKIN_TONES.length) {
            Color skinTone = SKIN_TONES[savedSkinToneIndex];
            player.setSkinTone(skinTone);
            if (skinTone != null) {
                System.out.println("SpriteCharacterCustomization: Applied skin tone: " +
                                   SKIN_TONE_NAMES[savedSkinToneIndex]);
            }
        }

        if (savedSelections.isEmpty()) {
            System.out.println("SpriteCharacterCustomization: No saved clothing selections to apply");
            return;
        }

        EquipmentOverlay overlay = player.getEquipmentOverlay();
        overlay.clearAll();

        for (Map.Entry<String, ClothingItem> entry : savedSelections.entrySet()) {
            ClothingItem item = entry.getValue();
            if (item.directory != null) {
                // Load and equip the item
                for (SpriteAnimation.ActionState state : new SpriteAnimation.ActionState[]{
                    SpriteAnimation.ActionState.IDLE,
                    SpriteAnimation.ActionState.WALK,
                    SpriteAnimation.ActionState.JUMP
                }) {
                    String gifPath = item.directory + "/" + state.name().toLowerCase() + ".gif";
                    overlay.equipItem(item.slot, state, gifPath, item.displayName);

                    // Also equip to secondary slot if present (for hair back layer)
                    if (item.secondarySlot != null) {
                        String backGifPath = item.directory + "/" + state.name().toLowerCase() + "_back.gif";
                        File backFile = new File(backGifPath);
                        if (backFile.exists()) {
                            overlay.equipItem(item.secondarySlot, state, backGifPath, item.displayName + " (back)");
                        }
                    }
                }

                // Apply tint if set
                Color tint = savedTints.get(entry.getKey());
                if (tint != null && !tint.equals(Color.WHITE)) {
                    overlay.setItemTint(item.slot, tint);
                    if (item.secondarySlot != null) {
                        overlay.setItemTint(item.secondarySlot, tint);
                    }
                }
            }
        }

        // Apply hair color
        if (savedHairColorIndex >= 0 && savedHairColorIndex < HAIR_COLORS.length) {
            Color hairColor = HAIR_COLORS[savedHairColorIndex];
            if (hairColor != null) {
                overlay.setItemTint(EquipmentOverlay.EquipmentSlot.HAIR_FRONT, hairColor);
                overlay.setItemTint(EquipmentOverlay.EquipmentSlot.HAIR_BACK, hairColor);
                System.out.println("SpriteCharacterCustomization: Applied hair color: " +
                                   HAIR_COLOR_NAMES[savedHairColorIndex]);
            }
        }

        System.out.println("SpriteCharacterCustomization: Applied " +
                          savedSelections.size() + " items to player");
    }

    /**
     * Returns whether there are saved customizations.
     */
    public static boolean hasSavedCustomization() {
        return !savedSelections.isEmpty() || savedSkinToneIndex != 7 || savedHairColorIndex != 12;
    }
}
