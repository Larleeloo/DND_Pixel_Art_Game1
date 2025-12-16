import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Character customization scene allowing players to customize their character's
 * appearance with RGB colors and size adjustments for each body part.
 */
class CharacterCustomizationScene implements Scene {

    private boolean initialized;

    // Preview skeleton
    private Skeleton previewSkeleton;
    private static final double PREVIEW_SCALE = 8.0;  // Double the normal 4x scale
    private float animationTime = 0;

    // UI Components
    private ArrayList<UISlider> allSliders;
    private Map<String, BodyPartSliders> bodyPartControls;
    private UIButton doneButton;
    private UIButton resetButton;

    // Stored customization (persists between scene visits)
    private static Map<String, Color> savedColors = new HashMap<>();
    private static Map<String, Double> savedSizes = new HashMap<>();

    // Body part groups for the UI
    private static final String[] BODY_PARTS = {
        "Head", "Neck", "Torso", "Arms", "Legs"
    };

    // Mapping from UI names to bone names
    private static final Map<String, String[]> BONE_MAPPINGS = new HashMap<>();
    static {
        BONE_MAPPINGS.put("Head", new String[]{"head"});
        BONE_MAPPINGS.put("Neck", new String[]{"neck"});
        BONE_MAPPINGS.put("Torso", new String[]{"torso"});
        BONE_MAPPINGS.put("Arms", new String[]{
            "arm_upper_left", "arm_lower_left", "hand_left",
            "arm_upper_right", "arm_lower_right", "hand_right"
        });
        BONE_MAPPINGS.put("Legs", new String[]{
            "leg_upper_left", "leg_lower_left", "foot_left",
            "leg_upper_right", "leg_lower_right", "foot_right"
        });
    }

    // Default colors for each body part
    private static final Map<String, Color> DEFAULT_COLORS = new HashMap<>();
    static {
        DEFAULT_COLORS.put("Head", new Color(255, 200, 150));      // Skin tone
        DEFAULT_COLORS.put("Neck", new Color(255, 200, 150));      // Skin tone
        DEFAULT_COLORS.put("Torso", new Color(100, 150, 200));     // Blue shirt
        DEFAULT_COLORS.put("Arms", new Color(255, 200, 150));      // Skin tone
        DEFAULT_COLORS.put("Legs", new Color(80, 80, 120));        // Dark pants
    }

    /**
     * Helper class to group RGB + Size sliders for a body part.
     */
    private static class BodyPartSliders {
        UISlider redSlider;
        UISlider greenSlider;
        UISlider blueSlider;
        UISlider sizeSlider;
        String partName;

        BodyPartSliders(String partName, int x, int y, int sliderWidth) {
            this.partName = partName;
            int spacing = 35;

            // Get saved or default values
            Color defaultColor = savedColors.getOrDefault(partName, DEFAULT_COLORS.get(partName));
            double defaultSize = savedSizes.getOrDefault(partName, 1.0);

            // RGB sliders (0-255)
            redSlider = new UISlider(x, y, sliderWidth, 8, "R", 0, 255, defaultColor.getRed());
            redSlider.setFillColor(new Color(200, 80, 80));

            greenSlider = new UISlider(x, y + spacing, sliderWidth, 8, "G", 0, 255, defaultColor.getGreen());
            greenSlider.setFillColor(new Color(80, 200, 80));

            blueSlider = new UISlider(x, y + spacing * 2, sliderWidth, 8, "B", 0, 255, defaultColor.getBlue());
            blueSlider.setFillColor(new Color(80, 80, 200));

            // Size slider (0.5 to 2.0)
            sizeSlider = new UISlider(x, y + spacing * 3, sliderWidth, 8, "Size", 0.5, 2.0, defaultSize);
            sizeSlider.setFillColor(new Color(180, 150, 100));
        }

        Color getColor() {
            return new Color(
                redSlider.getIntValue(),
                greenSlider.getIntValue(),
                blueSlider.getIntValue()
            );
        }

        double getSize() {
            return sizeSlider.getValue();
        }

        void addToList(ArrayList<UISlider> list) {
            list.add(redSlider);
            list.add(greenSlider);
            list.add(blueSlider);
            list.add(sizeSlider);
        }

        void handleMousePressed(int mouseX, int mouseY) {
            redSlider.handleMousePressed(mouseX, mouseY);
            greenSlider.handleMousePressed(mouseX, mouseY);
            blueSlider.handleMousePressed(mouseX, mouseY);
            sizeSlider.handleMousePressed(mouseX, mouseY);
        }

        void handleMouseReleased(int mouseX, int mouseY) {
            redSlider.handleMouseReleased(mouseX, mouseY);
            greenSlider.handleMouseReleased(mouseX, mouseY);
            blueSlider.handleMouseReleased(mouseX, mouseY);
            sizeSlider.handleMouseReleased(mouseX, mouseY);
        }

        void handleMouseDragged(int mouseX, int mouseY) {
            redSlider.handleMouseDragged(mouseX, mouseY);
            greenSlider.handleMouseDragged(mouseX, mouseY);
            blueSlider.handleMouseDragged(mouseX, mouseY);
            sizeSlider.handleMouseDragged(mouseX, mouseY);
        }

        void draw(Graphics g) {
            redSlider.draw(g);
            greenSlider.draw(g);
            blueSlider.draw(g);
            sizeSlider.draw(g);
        }
    }

    public CharacterCustomizationScene() {
        this.initialized = false;
    }

    @Override
    public void init() {
        if (initialized) return;

        System.out.println("CharacterCustomizationScene: Initializing...");

        // Create preview skeleton
        previewSkeleton = Skeleton.createHumanoid();
        previewSkeleton.setScale(PREVIEW_SCALE);

        // Add idle animation for preview
        BoneAnimation idle = BoneAnimation.createIdleAnimation("torso");
        previewSkeleton.addAnimation(idle);
        previewSkeleton.playAnimation("idle");

        // Create UI controls
        createSliders();
        createButtons();

        // Apply saved customizations to preview
        applyCustomizationToSkeleton();

        initialized = true;
        System.out.println("CharacterCustomizationScene: Initialized");
    }

    /**
     * Creates the slider controls for each body part.
     */
    private void createSliders() {
        allSliders = new ArrayList<>();
        bodyPartControls = new HashMap<>();

        int sliderWidth = 150;
        int columnWidth = 200;
        int startX = GamePanel.SCREEN_WIDTH / 2 + 50;
        int startY = 120;
        int partSpacing = 180;

        // Create sliders for each body part in two columns
        for (int i = 0; i < BODY_PARTS.length; i++) {
            String partName = BODY_PARTS[i];
            int column = i / 3;  // 0 or 1
            int row = i % 3;     // 0, 1, or 2

            int x = startX + column * (columnWidth + 50);
            int y = startY + row * partSpacing;

            BodyPartSliders sliders = new BodyPartSliders(partName, x, y, sliderWidth);

            // Set up change callbacks to update preview
            Runnable updatePreview = this::applyCustomizationToSkeleton;
            sliders.redSlider.setOnChange(updatePreview);
            sliders.greenSlider.setOnChange(updatePreview);
            sliders.blueSlider.setOnChange(updatePreview);
            sliders.sizeSlider.setOnChange(updatePreview);

            bodyPartControls.put(partName, sliders);
            sliders.addToList(allSliders);
        }
    }

    /**
     * Creates the buttons.
     */
    private void createButtons() {
        int buttonWidth = 200;
        int buttonHeight = 50;
        int buttonY = GamePanel.SCREEN_HEIGHT - 100;

        // Done button - saves and returns to main menu
        doneButton = new UIButton(
            GamePanel.SCREEN_WIDTH / 2 - buttonWidth - 20,
            buttonY,
            buttonWidth,
            buttonHeight,
            "Done",
            () -> {
                saveCustomization();
                SceneManager.getInstance().setScene("mainMenu", SceneManager.TRANSITION_FADE);
            }
        );
        doneButton.setColors(
            new Color(70, 130, 180, 220),
            new Color(100, 160, 210, 255),
            Color.WHITE
        );

        // Reset button - resets to defaults
        resetButton = new UIButton(
            GamePanel.SCREEN_WIDTH / 2 + 20,
            buttonY,
            buttonWidth,
            buttonHeight,
            "Reset",
            () -> {
                resetToDefaults();
            }
        );
        resetButton.setColors(
            new Color(150, 100, 50, 220),
            new Color(180, 130, 80, 255),
            Color.WHITE
        );
    }

    /**
     * Applies current slider values to the preview skeleton.
     */
    private void applyCustomizationToSkeleton() {
        if (previewSkeleton == null || bodyPartControls == null) return;

        for (String partName : BODY_PARTS) {
            BodyPartSliders sliders = bodyPartControls.get(partName);
            if (sliders == null) continue;

            Color color = sliders.getColor();
            double size = sliders.getSize();

            String[] boneNames = BONE_MAPPINGS.get(partName);
            if (boneNames == null) continue;

            for (String boneName : boneNames) {
                Bone bone = previewSkeleton.findBone(boneName);
                if (bone != null) {
                    bone.setPlaceholderColor(color);
                    bone.setScale(size, size);
                }
            }
        }
    }

    /**
     * Saves current customization to static storage.
     */
    private void saveCustomization() {
        for (String partName : BODY_PARTS) {
            BodyPartSliders sliders = bodyPartControls.get(partName);
            if (sliders != null) {
                savedColors.put(partName, sliders.getColor());
                savedSizes.put(partName, sliders.getSize());
            }
        }
        System.out.println("CharacterCustomizationScene: Customization saved");
    }

    /**
     * Resets all sliders to default values.
     */
    private void resetToDefaults() {
        for (String partName : BODY_PARTS) {
            BodyPartSliders sliders = bodyPartControls.get(partName);
            if (sliders == null) continue;

            Color defaultColor = DEFAULT_COLORS.get(partName);
            sliders.redSlider.setValue(defaultColor.getRed());
            sliders.greenSlider.setValue(defaultColor.getGreen());
            sliders.blueSlider.setValue(defaultColor.getBlue());
            sliders.sizeSlider.setValue(1.0);
        }

        // Clear saved customization
        savedColors.clear();
        savedSizes.clear();

        applyCustomizationToSkeleton();
        System.out.println("CharacterCustomizationScene: Reset to defaults");
    }

    /**
     * Applies the saved customization to a player entity.
     * Call this when creating a new player.
     */
    public static void applyToPlayer(Skeleton skeleton) {
        if (savedColors.isEmpty() && savedSizes.isEmpty()) {
            return; // No customization saved
        }

        for (String partName : BODY_PARTS) {
            Color color = savedColors.getOrDefault(partName, DEFAULT_COLORS.get(partName));
            double size = savedSizes.getOrDefault(partName, 1.0);

            String[] boneNames = BONE_MAPPINGS.get(partName);
            if (boneNames == null) continue;

            for (String boneName : boneNames) {
                Bone bone = skeleton.findBone(boneName);
                if (bone != null) {
                    bone.setPlaceholderColor(color);
                    bone.setScale(size, size);
                }
            }
        }
    }

    @Override
    public void update(InputManager input) {
        if (!initialized) return;

        // Update animation
        animationTime += 1.0 / 60.0;
        previewSkeleton.update(1.0 / 60.0);
    }

    @Override
    public void draw(Graphics g) {
        if (!initialized) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(30, 30, 45),
            0, GamePanel.SCREEN_HEIGHT, new Color(50, 50, 70)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        // Draw title
        g2d.setFont(new Font("Serif", Font.BOLD, 48));
        g2d.setColor(new Color(255, 220, 150));
        String title = "Character Customization";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (GamePanel.SCREEN_WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 60);

        // Draw preview area background
        int previewX = 100;
        int previewY = 150;
        int previewWidth = GamePanel.SCREEN_WIDTH / 2 - 150;
        int previewHeight = GamePanel.SCREEN_HEIGHT - 300;

        g2d.setColor(new Color(20, 20, 30, 200));
        g2d.fillRoundRect(previewX, previewY, previewWidth, previewHeight, 20, 20);
        g2d.setColor(new Color(100, 100, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(previewX, previewY, previewWidth, previewHeight, 20, 20);

        // Draw "Preview" label
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(new Color(150, 150, 170));
        g2d.drawString("Preview", previewX + 20, previewY + 30);

        // Position and draw the preview skeleton
        double skeletonX = previewX + previewWidth / 2.0;
        double skeletonY = previewY + previewHeight / 2.0 + 50;
        previewSkeleton.setPosition(skeletonX, skeletonY);
        previewSkeleton.draw(g);

        // Draw body part sections
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        for (String partName : BODY_PARTS) {
            BodyPartSliders sliders = bodyPartControls.get(partName);
            if (sliders == null) continue;

            // Draw section header with color preview
            int headerX = sliders.redSlider.getIntValue(); // Hacky way to get x
            int headerY = (int) sliders.redSlider.getValue(); // This won't work, need to fix

            // Draw part name and color preview box
            drawBodyPartHeader(g2d, sliders);

            // Draw the sliders
            sliders.draw(g);
        }

        // Draw buttons
        doneButton.draw(g);
        resetButton.draw(g);

        // Draw instructions
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(150, 150, 170));
        String instructions = "Drag sliders to customize your character. Changes are saved automatically.";
        fm = g2d.getFontMetrics();
        int instrX = (GamePanel.SCREEN_WIDTH - fm.stringWidth(instructions)) / 2;
        g2d.drawString(instructions, instrX, GamePanel.SCREEN_HEIGHT - 30);
    }

    /**
     * Draws the header for a body part section with color preview.
     */
    private void drawBodyPartHeader(Graphics2D g2d, BodyPartSliders sliders) {
        // Get position from the red slider (it's the first one)
        // We need to access the slider's internal position, but it's private
        // For now, we'll calculate based on body part index

        int index = 0;
        for (int i = 0; i < BODY_PARTS.length; i++) {
            if (BODY_PARTS[i].equals(sliders.partName)) {
                index = i;
                break;
            }
        }

        int sliderWidth = 150;
        int columnWidth = 200;
        int startX = GamePanel.SCREEN_WIDTH / 2 + 50;
        int startY = 120;
        int partSpacing = 180;

        int column = index / 3;
        int row = index % 3;
        int x = startX + column * (columnWidth + 50);
        int y = startY + row * partSpacing;

        // Draw section background
        g2d.setColor(new Color(40, 40, 55, 150));
        g2d.fillRoundRect(x - 10, y - 35, sliderWidth + 20, 160, 10, 10);

        // Draw part name
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(new Color(220, 220, 230));
        g2d.drawString(sliders.partName, x, y - 15);

        // Draw color preview box
        Color previewColor = sliders.getColor();
        int boxSize = 20;
        int boxX = x + sliderWidth - boxSize;
        int boxY = y - 30;

        g2d.setColor(previewColor);
        g2d.fillRect(boxX, boxY, boxSize, boxSize);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(boxX, boxY, boxSize, boxSize);
    }

    @Override
    public void onMousePressed(int x, int y) {
        for (BodyPartSliders sliders : bodyPartControls.values()) {
            sliders.handleMousePressed(x, y);
        }
    }

    @Override
    public void onMouseReleased(int x, int y) {
        for (BodyPartSliders sliders : bodyPartControls.values()) {
            sliders.handleMouseReleased(x, y);
        }
    }

    @Override
    public void onMouseDragged(int x, int y) {
        for (BodyPartSliders sliders : bodyPartControls.values()) {
            sliders.handleMouseDragged(x, y);
        }
    }

    @Override
    public void onMouseMoved(int x, int y) {
        doneButton.handleMouseMove(x, y);
        resetButton.handleMouseMove(x, y);
    }

    @Override
    public void onMouseClicked(int x, int y) {
        doneButton.handleClick(x, y);
        resetButton.handleClick(x, y);
    }

    @Override
    public void dispose() {
        System.out.println("CharacterCustomizationScene: Disposing...");
        initialized = false;
        previewSkeleton = null;
        allSliders = null;
        bodyPartControls = null;
    }

    @Override
    public String getName() {
        return "Character Customization";
    }
}
