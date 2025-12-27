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

    // Equipment categories and items
    private static final String[] CATEGORIES = {
        "Helmet", "Chest", "Legs", "Boots", "Gloves", "Necklace", "Wristwear"
    };

    private Map<String, List<ClothingItem>> availableItems;
    private Map<String, ClothingItem> selectedItems;  // Current selections per category
    private Map<String, Color> itemTints;  // Tint colors per category

    // Saved state (persists between scene visits)
    private static Map<String, ClothingItem> savedSelections = new HashMap<>();
    private static Map<String, Color> savedTints = new HashMap<>();

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

    // Layout constants
    private static final int PREVIEW_X = 80;
    private static final int PREVIEW_Y = 150;
    private static final int PREVIEW_WIDTH = 200;
    private static final int PREVIEW_HEIGHT = 350;

    private static final int ITEMS_X = 320;
    private static final int ITEMS_Y = 180;
    private static final int ITEM_SIZE = 80;
    private static final int ITEMS_PER_ROW = 4;

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
        public BufferedImage previewImage;

        public ClothingItem(String name, String displayName, String directory,
                           EquipmentOverlay.EquipmentSlot slot) {
            this.name = name;
            this.displayName = displayName;
            this.directory = directory;
            this.slot = slot;
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

        // Apply saved selections to preview
        applySelectionsToPreview();

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
        categoryConfig.put("Helmet", new Object[]{EquipmentOverlay.EquipmentSlot.HELMET, "helmet"});
        categoryConfig.put("Chest", new Object[]{EquipmentOverlay.EquipmentSlot.CHEST, "chest"});
        categoryConfig.put("Legs", new Object[]{EquipmentOverlay.EquipmentSlot.LEGS, "legs"});
        categoryConfig.put("Boots", new Object[]{EquipmentOverlay.EquipmentSlot.BOOTS, "boots"});
        categoryConfig.put("Gloves", new Object[]{EquipmentOverlay.EquipmentSlot.GLOVES, "gloves"});
        categoryConfig.put("Necklace", new Object[]{EquipmentOverlay.EquipmentSlot.NECKLACE, "necklace"});
        categoryConfig.put("Wristwear", new Object[]{EquipmentOverlay.EquipmentSlot.WRISTWEAR, "wristwear"});

        for (String category : CATEGORIES) {
            Object[] config = categoryConfig.get(category);
            EquipmentOverlay.EquipmentSlot slot = (EquipmentOverlay.EquipmentSlot) config[0];
            String dirName = (String) config[1];

            List<ClothingItem> items = new ArrayList<>();

            // Add "None" option
            items.add(new ClothingItem("none", "None", null, slot));

            // Scan directory for items
            File dir = new File("assets/clothing/" + dirName);
            if (dir.exists() && dir.isDirectory()) {
                File[] subdirs = dir.listFiles(File::isDirectory);
                if (subdirs != null) {
                    for (File subdir : subdirs) {
                        String itemName = subdir.getName();
                        String displayName = formatDisplayName(itemName);
                        String itemDir = subdir.getPath();

                        ClothingItem item = new ClothingItem(itemName, displayName, itemDir, slot);

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
            sb.append(Character.toUpperCase(part.charAt(0)));
            sb.append(part.substring(1));
        }
        return sb.toString();
    }

    /**
     * Creates UI components.
     */
    private void createUI() {
        int buttonWidth = 150;
        int buttonHeight = 45;

        // Done button
        doneButton = new UIButton(
            GamePanel.SCREEN_WIDTH - buttonWidth - 30,
            GamePanel.SCREEN_HEIGHT - buttonHeight - 30,
            buttonWidth, buttonHeight,
            "Done",
            this::onDoneClicked
        );
        doneButton.setColors(
            new Color(70, 130, 180, 220),
            new Color(100, 160, 210, 255),
            Color.WHITE
        );

        // Clear All button
        clearAllButton = new UIButton(
            GamePanel.SCREEN_WIDTH - buttonWidth * 2 - 50,
            GamePanel.SCREEN_HEIGHT - buttonHeight - 30,
            buttonWidth, buttonHeight,
            "Clear All",
            this::onClearAllClicked
        );
        clearAllButton.setColors(
            new Color(150, 80, 80, 220),
            new Color(180, 100, 100, 255),
            Color.WHITE
        );

        // Category tabs
        categoryTabs = new UIButton[CATEGORIES.length];
        int tabWidth = 100;
        int tabHeight = 35;
        int tabStartX = ITEMS_X;
        int tabY = 130;

        for (int i = 0; i < CATEGORIES.length; i++) {
            final int index = i;
            categoryTabs[i] = new UIButton(
                tabStartX + i * (tabWidth + 5), tabY,
                tabWidth, tabHeight,
                CATEGORIES[i],
                () -> selectCategory(index)
            );
            updateTabColors(i);
        }

        // Color tint sliders
        int sliderX = PREVIEW_X;
        int sliderY = PREVIEW_Y + PREVIEW_HEIGHT + 20;
        int sliderWidth = PREVIEW_WIDTH;

        redSlider = new UISlider(sliderX, sliderY, sliderWidth, 8, "R", 0, 255, 255);
        redSlider.setFillColor(new Color(200, 80, 80));
        redSlider.setOnChange(this::onTintChanged);

        greenSlider = new UISlider(sliderX, sliderY + 30, sliderWidth, 8, "G", 0, 255, 255);
        greenSlider.setFillColor(new Color(80, 200, 80));
        greenSlider.setOnChange(this::onTintChanged);

        blueSlider = new UISlider(sliderX, sliderY + 60, sliderWidth, 8, "B", 0, 255, 255);
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
                }

                // Apply tint if set
                Color tint = itemTints.get(entry.getKey());
                if (tint != null && !tint.equals(Color.WHITE)) {
                    previewOverlay.setItemTint(item.slot, tint);
                }
            }
        }
    }

    /**
     * Called when Done button is clicked.
     */
    private void onDoneClicked() {
        // Save current selections
        savedSelections = new HashMap<>(selectedItems);
        savedTints = new HashMap<>(itemTints);

        System.out.println("SpriteCharacterCustomization: Saved " + selectedItems.size() + " items");

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
        applySelectionsToPreview();
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
        }
    }

    /**
     * Gets the scene name for a scene instance.
     */
    private String getSceneNameFor(Scene scene) {
        // Try to find the scene name
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

        // Draw background (semi-transparent if overlay)
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
        g2d.setFont(new Font("Serif", Font.BOLD, 42));
        g2d.setColor(new Color(255, 220, 150));
        String title = "Character Customization";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (GamePanel.SCREEN_WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 55);

        // Draw subtitle
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(150, 150, 170));
        String subtitle = "Press SPACE to change animation, A/D to flip direction";
        fm = g2d.getFontMetrics();
        g2d.drawString(subtitle, (GamePanel.SCREEN_WIDTH - fm.stringWidth(subtitle)) / 2, 80);

        // Draw preview panel
        drawPreviewPanel(g2d);

        // Draw category tabs
        for (UIButton tab : categoryTabs) {
            tab.draw(g);
        }

        // Draw items grid
        drawItemsGrid(g2d);

        // Draw tint controls
        drawTintControls(g2d);

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
        g2d.fillRoundRect(PREVIEW_X - 20, PREVIEW_Y - 40, PREVIEW_WIDTH + 40, PREVIEW_HEIGHT + 50, 15, 15);
        g2d.setColor(new Color(100, 100, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(PREVIEW_X - 20, PREVIEW_Y - 40, PREVIEW_WIDTH + 40, PREVIEW_HEIGHT + 50, 15, 15);

        // Label
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(new Color(200, 200, 210));
        g2d.drawString("Preview", PREVIEW_X, PREVIEW_Y - 15);

        // Current animation state
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(new Color(150, 150, 170));
        g2d.drawString(previewAnimation.getState().toString(), PREVIEW_X + PREVIEW_WIDTH - 50, PREVIEW_Y - 15);

        // Draw character preview (scaled up)
        int previewScale = 3;
        int charWidth = 32 * previewScale;
        int charHeight = 64 * previewScale;
        int charX = PREVIEW_X + (PREVIEW_WIDTH - charWidth) / 2;
        int charY = PREVIEW_Y + (PREVIEW_HEIGHT - charHeight) / 2;

        SpriteAnimation.ActionState state = previewAnimation.getState();

        // Draw equipment behind
        previewOverlay.drawBehind(g2d, charX, charY, charWidth, charHeight, facingRight, state);

        // Draw base character
        previewAnimation.draw(g2d, charX, charY, charWidth, charHeight, facingRight);

        // Draw equipment in front
        previewOverlay.drawInFront(g2d, charX, charY, charWidth, charHeight, facingRight, state);
    }

    /**
     * Draws the items grid for the current category.
     */
    private void drawItemsGrid(Graphics2D g2d) {
        String category = CATEGORIES[currentCategory];
        List<ClothingItem> items = availableItems.get(category);
        if (items == null) return;

        ClothingItem selected = selectedItems.get(category);

        int gridWidth = ITEMS_PER_ROW * (ITEM_SIZE + 10);
        int gridHeight = ((items.size() + ITEMS_PER_ROW - 1) / ITEMS_PER_ROW) * (ITEM_SIZE + 10);

        // Background panel
        g2d.setColor(PANEL_COLOR);
        g2d.fillRoundRect(ITEMS_X - 15, ITEMS_Y - 15, gridWidth + 30,
                         Math.min(gridHeight + 30, 320), 10, 10);

        // Draw items
        for (int i = 0; i < items.size(); i++) {
            ClothingItem item = items.get(i);
            int row = i / ITEMS_PER_ROW;
            int col = i % ITEMS_PER_ROW;

            int x = ITEMS_X + col * (ITEM_SIZE + 10);
            int y = ITEMS_Y + row * (ITEM_SIZE + 10);

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
                // Scale and draw preview
                int imgSize = ITEM_SIZE - 16;
                g2d.drawImage(item.previewImage,
                             x + 8, y + 4,
                             imgSize, imgSize, null);
            } else if (item.name.equals("none")) {
                // Draw X for none
                g2d.setColor(new Color(150, 100, 100));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(x + 20, y + 20, x + ITEM_SIZE - 20, y + ITEM_SIZE - 20);
                g2d.drawLine(x + ITEM_SIZE - 20, y + 20, x + 20, y + ITEM_SIZE - 20);
            } else {
                // Placeholder box
                g2d.setColor(new Color(80, 80, 100));
                g2d.fillRect(x + 15, y + 10, ITEM_SIZE - 30, ITEM_SIZE - 30);
            }

            // Item name
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.setColor(Color.WHITE);
            String displayName = item.displayName;
            if (displayName.length() > 12) {
                displayName = displayName.substring(0, 10) + "...";
            }
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (ITEM_SIZE - fm.stringWidth(displayName)) / 2;
            g2d.drawString(displayName, textX, y + ITEM_SIZE - 5);
        }
    }

    /**
     * Draws the color tint controls.
     */
    private void drawTintControls(Graphics2D g2d) {
        int y = PREVIEW_Y + PREVIEW_HEIGHT + 10;

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(new Color(180, 180, 190));
        g2d.drawString("Color Tint:", PREVIEW_X, y);

        redSlider.draw(g2d);
        greenSlider.draw(g2d);
        blueSlider.draw(g2d);

        // Color preview box
        Color currentTint = new Color(
            redSlider.getIntValue(),
            greenSlider.getIntValue(),
            blueSlider.getIntValue()
        );
        int boxX = PREVIEW_X + PREVIEW_WIDTH - 30;
        int boxY = y + 30;
        g2d.setColor(currentTint);
        g2d.fillRect(boxX, boxY, 25, 50);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(boxX, boxY, 25, 50);
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

        // Check item clicks
        String category = CATEGORIES[currentCategory];
        List<ClothingItem> items = availableItems.get(category);
        if (items == null) return;

        for (int i = 0; i < items.size(); i++) {
            int row = i / ITEMS_PER_ROW;
            int col = i % ITEMS_PER_ROW;

            int itemX = ITEMS_X + col * (ITEM_SIZE + 10);
            int itemY = ITEMS_Y + row * (ITEM_SIZE + 10);

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
        if (savedSelections.isEmpty()) {
            System.out.println("SpriteCharacterCustomization: No saved selections to apply");
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
                }

                // Apply tint if set
                Color tint = savedTints.get(entry.getKey());
                if (tint != null && !tint.equals(Color.WHITE)) {
                    overlay.setItemTint(item.slot, tint);
                }
            }
        }

        System.out.println("SpriteCharacterCustomization: Applied " +
                          savedSelections.size() + " items to player");
    }

    /**
     * Returns whether there are saved customizations.
     */
    public static boolean hasSavedCustomization() {
        return !savedSelections.isEmpty();
    }
}
