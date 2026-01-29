package ui;

import core.GamePanel;
import graphics.AnimatedTexture;
import graphics.AssetLoader;
import input.InputManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Overlay for displaying GIF-based cutscenes with dialogue options.
 * Supports full-screen (1920x1080) GIF animations with click-through prompts
 * and multiple choice dialogue options.
 * Pauses the game while active and requires player interaction to progress.
 */
public class CutsceneOverlay {

    private boolean visible;
    private List<CutsceneFrame> frames;
    private int currentFrameIndex;
    private boolean cutsceneComplete;
    private Runnable onComplete;
    private Consumer<Integer> onOptionSelected;

    // Animation state
    private AnimatedTexture currentAnimation;
    private long lastUpdateTime;

    // Custom dialogue box assets
    private BufferedImage dialogueBoxImage;
    private BufferedImage optionBoxImage;
    private BufferedImage optionHoverImage;
    private static final String DIALOGUE_BOX_PATH = "assets/dialogue/dialogue_box.png";
    private static final String OPTION_BOX_PATH = "assets/dialogue/option_box.png";
    private static final String OPTION_HOVER_PATH = "assets/dialogue/option_hover.png";

    // Display settings
    private static final int SCREEN_WIDTH = GamePanel.SCREEN_WIDTH;
    private static final int SCREEN_HEIGHT = GamePanel.SCREEN_HEIGHT;

    // Colors (used when custom assets not available)
    private static final Color OVERLAY_BG = new Color(0, 0, 0, 255);
    private static final Color DIALOGUE_BG = new Color(20, 15, 30, 230);
    private static final Color DIALOGUE_BORDER = new Color(139, 119, 101);
    private static final Color DIALOGUE_BORDER_INNER = new Color(89, 69, 51);
    private static final Color PROMPT_TEXT = new Color(255, 255, 255);
    private static final Color PROMPT_HINT = new Color(180, 180, 180);
    private static final Color OPTION_BG = new Color(40, 35, 50, 220);
    private static final Color OPTION_HOVER_BG = new Color(70, 60, 90, 240);
    private static final Color OPTION_BORDER = new Color(100, 90, 120);
    private static final Color OPTION_TEXT = new Color(255, 255, 255);
    private static final Color OPTION_HOVER_TEXT = new Color(255, 220, 150);

    // Dialogue box dimensions
    private static final int DIALOGUE_HEIGHT = 180;
    private static final int DIALOGUE_MARGIN = 60;
    private static final int DIALOGUE_PADDING = 25;
    private static final int DIALOGUE_BORDER_WIDTH = 4;

    // Option dimensions
    private static final int OPTION_HEIGHT = 50;
    private static final int OPTION_SPACING = 10;
    private static final int OPTION_MARGIN = 100;
    private static final int OPTION_PADDING = 15;

    // Click cooldown to prevent accidental skips
    private long lastClickTime;
    private static final long CLICK_COOLDOWN_MS = 300;

    // Option hover state
    private int hoveredOptionIndex = -1;
    private List<Rectangle> optionBounds = new ArrayList<>();

    // Fade effect
    private float fadeAlpha = 0f;
    private boolean fadingIn = false;
    private boolean fadingOut = false;
    private static final float FADE_SPEED = 0.08f;

    public CutsceneOverlay() {
        this.visible = false;
        this.frames = new ArrayList<>();
        this.currentFrameIndex = 0;
        this.cutsceneComplete = false;
        this.lastUpdateTime = System.currentTimeMillis();
        this.lastClickTime = 0;

        // Try to load custom dialogue assets
        loadDialogueAssets();
    }

    /**
     * Attempts to load custom dialogue box assets from assets/dialogue folder.
     */
    private void loadDialogueAssets() {
        try {
            AssetLoader.ImageAsset dialogueAsset = AssetLoader.load(DIALOGUE_BOX_PATH);
            if (dialogueAsset != null && dialogueAsset.staticImage != null) {
                dialogueBoxImage = dialogueAsset.staticImage;
                System.out.println("CutsceneOverlay: Loaded custom dialogue box");
            }

            AssetLoader.ImageAsset optionAsset = AssetLoader.load(OPTION_BOX_PATH);
            if (optionAsset != null && optionAsset.staticImage != null) {
                optionBoxImage = optionAsset.staticImage;
                System.out.println("CutsceneOverlay: Loaded custom option box");
            }

            AssetLoader.ImageAsset hoverAsset = AssetLoader.load(OPTION_HOVER_PATH);
            if (hoverAsset != null && hoverAsset.staticImage != null) {
                optionHoverImage = hoverAsset.staticImage;
                System.out.println("CutsceneOverlay: Loaded custom option hover");
            }
        } catch (Exception e) {
            System.out.println("CutsceneOverlay: Using default dialogue styling (custom assets not found)");
        }
    }

    /**
     * Starts a cutscene with the given frames.
     * @param cutsceneFrames List of frames (GIF paths + optional text + optional options)
     * @param onComplete Callback when cutscene finishes (called with no option selection)
     */
    public void startCutscene(List<CutsceneFrame> cutsceneFrames, Runnable onComplete) {
        startCutscene(cutsceneFrames, onComplete, null);
    }

    /**
     * Starts a cutscene with the given frames and option callback.
     * @param cutsceneFrames List of frames (GIF paths + optional text + optional options)
     * @param onComplete Callback when cutscene finishes
     * @param onOptionSelected Callback when an option is selected (receives option index)
     */
    public void startCutscene(List<CutsceneFrame> cutsceneFrames, Runnable onComplete, Consumer<Integer> onOptionSelected) {
        if (cutsceneFrames == null || cutsceneFrames.isEmpty()) {
            System.out.println("CutsceneOverlay: No frames provided, skipping cutscene");
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        this.frames = new ArrayList<>(cutsceneFrames);
        this.currentFrameIndex = 0;
        this.cutsceneComplete = false;
        this.onComplete = onComplete;
        this.onOptionSelected = onOptionSelected;
        this.visible = true;
        this.fadeAlpha = 0f;
        this.fadingIn = true;
        this.fadingOut = false;
        this.lastUpdateTime = System.currentTimeMillis();
        this.lastClickTime = System.currentTimeMillis(); // Prevent immediate skip
        this.hoveredOptionIndex = -1;
        this.optionBounds.clear();

        loadCurrentFrame();

        System.out.println("CutsceneOverlay: Starting cutscene with " + frames.size() + " frames");
    }

    /**
     * Starts a single-frame cutscene (convenience method).
     */
    public void startCutscene(String gifPath, String text, Runnable onComplete) {
        List<CutsceneFrame> singleFrame = new ArrayList<>();
        singleFrame.add(new CutsceneFrame(gifPath, text));
        startCutscene(singleFrame, onComplete);
    }

    /**
     * Loads the GIF for the current frame.
     */
    private void loadCurrentFrame() {
        if (currentFrameIndex >= frames.size()) {
            return;
        }

        CutsceneFrame frame = frames.get(currentFrameIndex);
        String gifPath = frame.getGifPath();

        if (gifPath != null && !gifPath.isEmpty()) {
            AssetLoader.ImageAsset asset = AssetLoader.load(gifPath);
            if (asset != null && asset.animatedTexture != null) {
                currentAnimation = asset.animatedTexture;
                currentAnimation.setLooping(true);
                currentAnimation.reset();
                System.out.println("CutsceneOverlay: Loaded GIF - " + gifPath +
                        " (" + currentAnimation.getWidth() + "x" + currentAnimation.getHeight() +
                        ", " + currentAnimation.getFrameCount() + " frames)");
            } else {
                currentAnimation = null;
                System.out.println("CutsceneOverlay: Failed to load GIF - " + gifPath);
            }
        } else {
            currentAnimation = null;
        }

        // Clear option bounds for new frame
        optionBounds.clear();
        hoveredOptionIndex = -1;
    }

    /**
     * Advances to the next frame or completes the cutscene.
     */
    private void advanceFrame() {
        // Don't advance if current frame has options - must select an option
        if (currentFrameIndex < frames.size()) {
            CutsceneFrame currentFrame = frames.get(currentFrameIndex);
            if (currentFrame.hasOptions()) {
                return; // Must click an option to advance
            }
        }

        currentFrameIndex++;

        if (currentFrameIndex >= frames.size()) {
            // Start fade out
            fadingOut = true;
            fadingIn = false;
        } else {
            loadCurrentFrame();
        }
    }

    /**
     * Selects an option and advances the cutscene.
     */
    private void selectOption(int optionIndex) {
        if (currentFrameIndex >= frames.size()) return;

        CutsceneFrame currentFrame = frames.get(currentFrameIndex);
        if (!currentFrame.hasOptions() || optionIndex < 0 || optionIndex >= currentFrame.getOptions().size()) {
            return;
        }

        DialogueOption option = currentFrame.getOptions().get(optionIndex);
        System.out.println("CutsceneOverlay: Selected option " + optionIndex + ": " + option.getText());

        // Call option's callback if present
        if (option.getOnSelect() != null) {
            option.getOnSelect().run();
        }

        // Call global option callback if present
        if (onOptionSelected != null) {
            onOptionSelected.accept(optionIndex);
        }

        // Advance to next frame
        currentFrameIndex++;
        if (currentFrameIndex >= frames.size()) {
            fadingOut = true;
            fadingIn = false;
        } else {
            loadCurrentFrame();
        }
    }

    /**
     * Completes the cutscene and hides the overlay.
     */
    private void completeCutscene() {
        cutsceneComplete = true;
        visible = false;
        currentAnimation = null;
        frames.clear();
        optionBounds.clear();

        System.out.println("CutsceneOverlay: Cutscene complete");

        if (onComplete != null) {
            onComplete.run();
        }
    }

    /**
     * Updates the cutscene animation.
     * @param deltaMs Time since last update in milliseconds
     */
    public void update(long deltaMs) {
        if (!visible) return;

        // Handle fade effects
        if (fadingIn) {
            fadeAlpha += FADE_SPEED;
            if (fadeAlpha >= 1f) {
                fadeAlpha = 1f;
                fadingIn = false;
            }
        } else if (fadingOut) {
            fadeAlpha -= FADE_SPEED;
            if (fadeAlpha <= 0f) {
                fadeAlpha = 0f;
                fadingOut = false;
                completeCutscene();
                return;
            }
        }

        // Update animation
        if (currentAnimation != null) {
            currentAnimation.update(deltaMs);
        }
    }

    /**
     * Convenience update method that calculates deltaMs internally.
     */
    public void update() {
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;
        update(deltaMs);
    }

    /**
     * Draws the cutscene overlay.
     */
    public void draw(Graphics g) {
        if (!visible) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Apply fade
        Composite originalComposite = g2d.getComposite();
        if (fadeAlpha < 1f) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
        }

        // Draw black background
        g2d.setColor(OVERLAY_BG);
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        // Draw the GIF frame
        if (currentAnimation != null) {
            BufferedImage frame = currentAnimation.getCurrentFrame();
            if (frame != null) {
                drawScaledImage(g2d, frame);
            }
        }

        // Draw dialogue box and content
        if (currentFrameIndex < frames.size()) {
            CutsceneFrame currentFrame = frames.get(currentFrameIndex);

            // Draw dialogue box with text
            if (currentFrame.getText() != null && !currentFrame.getText().isEmpty()) {
                drawDialogueBox(g2d, currentFrame);
            }

            // Draw options if present
            if (currentFrame.hasOptions()) {
                drawOptions(g2d, currentFrame);
            } else if (currentFrame.getText() == null || currentFrame.getText().isEmpty()) {
                // Draw minimal hint only if no text and no options
                drawContinueHint(g2d);
            }
        }

        // Restore composite
        g2d.setComposite(originalComposite);
    }

    /**
     * Draws the GIF image scaled to fit the screen while maintaining aspect ratio.
     */
    private void drawScaledImage(Graphics2D g2d, BufferedImage image) {
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        // Calculate scale to fit screen (cover mode - fill screen, may crop)
        double scaleX = (double) SCREEN_WIDTH / imgWidth;
        double scaleY = (double) SCREEN_HEIGHT / imgHeight;
        double scale = Math.max(scaleX, scaleY); // Cover mode

        int drawWidth = (int) (imgWidth * scale);
        int drawHeight = (int) (imgHeight * scale);

        // Center the image
        int drawX = (SCREEN_WIDTH - drawWidth) / 2;
        int drawY = (SCREEN_HEIGHT - drawHeight) / 2;

        g2d.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
    }

    /**
     * Draws the dialogue box with text.
     */
    private void drawDialogueBox(Graphics2D g2d, CutsceneFrame frame) {
        int boxY = SCREEN_HEIGHT - DIALOGUE_HEIGHT - DIALOGUE_MARGIN;
        int boxWidth = SCREEN_WIDTH - (DIALOGUE_MARGIN * 2);
        int boxX = DIALOGUE_MARGIN;

        // Adjust box height if there are options
        int actualHeight = DIALOGUE_HEIGHT;
        if (frame.hasOptions()) {
            // Make dialogue box smaller to leave room for options
            actualHeight = 120;
            boxY = SCREEN_HEIGHT - actualHeight - DIALOGUE_MARGIN -
                   (frame.getOptions().size() * (OPTION_HEIGHT + OPTION_SPACING)) - 20;
        }

        // Draw custom dialogue box image or fallback to programmatic drawing
        if (dialogueBoxImage != null) {
            g2d.drawImage(dialogueBoxImage, boxX, boxY, boxWidth, actualHeight, null);
        } else {
            drawDefaultDialogueBox(g2d, boxX, boxY, boxWidth, actualHeight);
        }

        // Draw speaker name if present
        int textStartY = boxY + DIALOGUE_PADDING;
        if (frame.getSpeakerName() != null && !frame.getSpeakerName().isEmpty()) {
            g2d.setColor(OPTION_HOVER_TEXT);
            g2d.setFont(new Font("Serif", Font.BOLD, 22));
            g2d.drawString(frame.getSpeakerName(), boxX + DIALOGUE_PADDING, textStartY + 5);
            textStartY += 30;
        }

        // Draw text
        g2d.setColor(PROMPT_TEXT);
        g2d.setFont(new Font("Serif", Font.PLAIN, 20));
        FontMetrics fm = g2d.getFontMetrics();

        // Word wrap the text
        List<String> lines = wrapText(frame.getText(), boxWidth - (DIALOGUE_PADDING * 2), fm);
        int textY = textStartY + fm.getAscent();
        for (String line : lines) {
            g2d.drawString(line, boxX + DIALOGUE_PADDING, textY);
            textY += fm.getHeight();
        }

        // Draw continue hint if no options
        if (!frame.hasOptions()) {
            g2d.setColor(PROMPT_HINT);
            g2d.setFont(new Font("Serif", Font.ITALIC, 14));
            String hint = "Click to continue...";
            fm = g2d.getFontMetrics();
            int hintX = boxX + boxWidth - DIALOGUE_PADDING - fm.stringWidth(hint);
            int hintY = boxY + actualHeight - DIALOGUE_PADDING + 5;
            g2d.drawString(hint, hintX, hintY);

            // Draw frame indicator
            String frameIndicator = (currentFrameIndex + 1) + " / " + frames.size();
            g2d.setFont(new Font("Serif", Font.PLAIN, 12));
            fm = g2d.getFontMetrics();
            g2d.drawString(frameIndicator, boxX + DIALOGUE_PADDING, hintY);
        }
    }

    /**
     * Draws default dialogue box with programmatic styling.
     */
    private void drawDefaultDialogueBox(Graphics2D g2d, int x, int y, int width, int height) {
        // Outer border
        g2d.setColor(DIALOGUE_BORDER);
        g2d.fillRoundRect(x, y, width, height, 10, 10);

        // Inner border
        g2d.setColor(DIALOGUE_BORDER_INNER);
        g2d.fillRoundRect(x + DIALOGUE_BORDER_WIDTH, y + DIALOGUE_BORDER_WIDTH,
                width - DIALOGUE_BORDER_WIDTH * 2, height - DIALOGUE_BORDER_WIDTH * 2, 8, 8);

        // Background
        g2d.setColor(DIALOGUE_BG);
        g2d.fillRoundRect(x + DIALOGUE_BORDER_WIDTH * 2, y + DIALOGUE_BORDER_WIDTH * 2,
                width - DIALOGUE_BORDER_WIDTH * 4, height - DIALOGUE_BORDER_WIDTH * 4, 6, 6);

        // Decorative corners
        g2d.setColor(new Color(180, 160, 140, 100));
        int cornerSize = 15;
        // Top-left
        g2d.fillRect(x + 8, y + 8, cornerSize, 3);
        g2d.fillRect(x + 8, y + 8, 3, cornerSize);
        // Top-right
        g2d.fillRect(x + width - 8 - cornerSize, y + 8, cornerSize, 3);
        g2d.fillRect(x + width - 11, y + 8, 3, cornerSize);
        // Bottom-left
        g2d.fillRect(x + 8, y + height - 11, cornerSize, 3);
        g2d.fillRect(x + 8, y + height - 8 - cornerSize, 3, cornerSize);
        // Bottom-right
        g2d.fillRect(x + width - 8 - cornerSize, y + height - 11, cornerSize, 3);
        g2d.fillRect(x + width - 11, y + height - 8 - cornerSize, 3, cornerSize);
    }

    /**
     * Draws dialogue options.
     */
    private void drawOptions(Graphics2D g2d, CutsceneFrame frame) {
        List<DialogueOption> options = frame.getOptions();
        int optionWidth = SCREEN_WIDTH - (OPTION_MARGIN * 2);
        int startY = SCREEN_HEIGHT - DIALOGUE_MARGIN - (options.size() * (OPTION_HEIGHT + OPTION_SPACING));

        optionBounds.clear();

        for (int i = 0; i < options.size(); i++) {
            DialogueOption option = options.get(i);
            int optionY = startY + i * (OPTION_HEIGHT + OPTION_SPACING);
            Rectangle bounds = new Rectangle(OPTION_MARGIN, optionY, optionWidth, OPTION_HEIGHT);
            optionBounds.add(bounds);

            boolean isHovered = (i == hoveredOptionIndex);

            // Draw option background
            if (isHovered && optionHoverImage != null) {
                g2d.drawImage(optionHoverImage, bounds.x, bounds.y, bounds.width, bounds.height, null);
            } else if (!isHovered && optionBoxImage != null) {
                g2d.drawImage(optionBoxImage, bounds.x, bounds.y, bounds.width, bounds.height, null);
            } else {
                drawDefaultOptionBox(g2d, bounds, isHovered);
            }

            // Draw option number
            g2d.setColor(isHovered ? OPTION_HOVER_TEXT : new Color(180, 170, 160));
            g2d.setFont(new Font("Serif", Font.BOLD, 18));
            String numberText = (i + 1) + ".";
            g2d.drawString(numberText, bounds.x + OPTION_PADDING, bounds.y + bounds.height / 2 + 6);

            // Draw option text
            g2d.setColor(isHovered ? OPTION_HOVER_TEXT : OPTION_TEXT);
            g2d.setFont(new Font("Serif", Font.PLAIN, 18));
            g2d.drawString(option.getText(), bounds.x + OPTION_PADDING + 30, bounds.y + bounds.height / 2 + 6);

            // Draw hover indicator
            if (isHovered) {
                g2d.setColor(OPTION_HOVER_TEXT);
                g2d.setFont(new Font("Serif", Font.BOLD, 20));
                g2d.drawString(">", bounds.x + 8, bounds.y + bounds.height / 2 + 7);
            }
        }

        // Draw hint
        g2d.setColor(PROMPT_HINT);
        g2d.setFont(new Font("Serif", Font.ITALIC, 14));
        String hint = "Click an option or press 1-" + options.size();
        FontMetrics fm = g2d.getFontMetrics();
        int hintX = (SCREEN_WIDTH - fm.stringWidth(hint)) / 2;
        g2d.drawString(hint, hintX, SCREEN_HEIGHT - 20);
    }

    /**
     * Draws default option box with programmatic styling.
     */
    private void drawDefaultOptionBox(Graphics2D g2d, Rectangle bounds, boolean isHovered) {
        // Border
        g2d.setColor(isHovered ? new Color(150, 130, 100) : OPTION_BORDER);
        g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);

        // Background
        g2d.setColor(isHovered ? OPTION_HOVER_BG : OPTION_BG);
        g2d.fillRoundRect(bounds.x + 2, bounds.y + 2, bounds.width - 4, bounds.height - 4, 6, 6);

        // Hover glow effect
        if (isHovered) {
            g2d.setColor(new Color(255, 220, 150, 30));
            g2d.fillRoundRect(bounds.x + 4, bounds.y + 4, bounds.width - 8, bounds.height - 8, 4, 4);
        }
    }

    /**
     * Draws a minimal "click to continue" hint when there's no text.
     */
    private void drawContinueHint(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.setFont(new Font("Serif", Font.ITALIC, 20));
        String hint = "Click to continue...";
        FontMetrics fm = g2d.getFontMetrics();
        int hintX = (SCREEN_WIDTH - fm.stringWidth(hint)) / 2;
        int hintY = SCREEN_HEIGHT - 50;
        g2d.drawString(hint, hintX, hintY);

        // Draw frame indicator
        if (frames.size() > 1) {
            String frameIndicator = (currentFrameIndex + 1) + " / " + frames.size();
            g2d.setFont(new Font("Serif", Font.PLAIN, 16));
            fm = g2d.getFontMetrics();
            g2d.drawString(frameIndicator, (SCREEN_WIDTH - fm.stringWidth(frameIndicator)) / 2, hintY + 25);
        }
    }

    /**
     * Word wraps text to fit within a given width.
     */
    private List<String> wrapText(String text, int maxWidth, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() > 0 ?
                    currentLine + " " + word : word;

            if (fm.stringWidth(testLine) <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * Handles mouse click events.
     * @return true if event was consumed
     */
    public boolean handleMouseClicked(int x, int y) {
        if (!visible || fadingIn || fadingOut) return visible;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_COOLDOWN_MS) {
            return true; // Still consume the event
        }
        lastClickTime = currentTime;

        // Check if clicked on an option
        for (int i = 0; i < optionBounds.size(); i++) {
            if (optionBounds.get(i).contains(x, y)) {
                selectOption(i);
                return true;
            }
        }

        // No option clicked - advance frame if no options present
        advanceFrame();
        return true;
    }

    /**
     * Handles mouse press events.
     * @return true if event was consumed (overlay is visible)
     */
    public boolean handleMousePressed(int x, int y) {
        return visible;
    }

    /**
     * Handles mouse release events.
     * @return true if event was consumed
     */
    public boolean handleMouseReleased(int x, int y) {
        return visible;
    }

    /**
     * Handles mouse move events for option hover effects.
     */
    public void handleMouseMoved(int x, int y) {
        if (!visible) return;

        hoveredOptionIndex = -1;
        for (int i = 0; i < optionBounds.size(); i++) {
            if (optionBounds.get(i).contains(x, y)) {
                hoveredOptionIndex = i;
                break;
            }
        }
    }

    /**
     * Handles key press events.
     * @return true if event was consumed
     */
    public boolean handleKeyPressed(int keyCode) {
        if (!visible || fadingIn || fadingOut) return visible;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_COOLDOWN_MS) {
            return true;
        }

        // Check for number keys to select options
        if (currentFrameIndex < frames.size()) {
            CutsceneFrame currentFrame = frames.get(currentFrameIndex);
            if (currentFrame.hasOptions()) {
                int optionIndex = -1;
                if (keyCode >= java.awt.event.KeyEvent.VK_1 && keyCode <= java.awt.event.KeyEvent.VK_9) {
                    optionIndex = keyCode - java.awt.event.KeyEvent.VK_1;
                } else if (keyCode >= java.awt.event.KeyEvent.VK_NUMPAD1 && keyCode <= java.awt.event.KeyEvent.VK_NUMPAD9) {
                    optionIndex = keyCode - java.awt.event.KeyEvent.VK_NUMPAD1;
                }

                if (optionIndex >= 0 && optionIndex < currentFrame.getOptions().size()) {
                    lastClickTime = currentTime;
                    selectOption(optionIndex);
                    return true;
                }
            }
        }

        // Space, Enter, or Escape advances the cutscene (if no options)
        if (keyCode == java.awt.event.KeyEvent.VK_SPACE ||
            keyCode == java.awt.event.KeyEvent.VK_ENTER ||
            keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
            lastClickTime = currentTime;
            advanceFrame();
            return true;
        }

        return visible;
    }

    /**
     * Checks if the cutscene overlay is currently visible.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Checks if a cutscene is currently playing (visible and not complete).
     */
    public boolean isPlaying() {
        return visible && !cutsceneComplete;
    }

    /**
     * Skips the current cutscene immediately.
     */
    public void skip() {
        if (!visible) return;
        fadingOut = true;
        fadingIn = false;
    }

    /**
     * Hides the cutscene overlay without triggering completion callback.
     */
    public void hide() {
        visible = false;
        cutsceneComplete = true;
        currentAnimation = null;
        frames.clear();
        optionBounds.clear();
    }

    /**
     * Represents a dialogue option that can be selected.
     */
    public static class DialogueOption {
        private String text;
        private Runnable onSelect;

        public DialogueOption(String text) {
            this.text = text;
            this.onSelect = null;
        }

        public DialogueOption(String text, Runnable onSelect) {
            this.text = text;
            this.onSelect = onSelect;
        }

        public String getText() {
            return text;
        }

        public Runnable getOnSelect() {
            return onSelect;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setOnSelect(Runnable onSelect) {
            this.onSelect = onSelect;
        }
    }

    /**
     * Represents a single frame in a cutscene.
     */
    public static class CutsceneFrame {
        private String gifPath;
        private String text;
        private String speakerName;
        private List<DialogueOption> options;

        public CutsceneFrame(String gifPath, String text) {
            this.gifPath = gifPath;
            this.text = text;
            this.speakerName = null;
            this.options = new ArrayList<>();
        }

        public CutsceneFrame(String gifPath) {
            this(gifPath, null);
        }

        public CutsceneFrame(String gifPath, String text, String speakerName) {
            this.gifPath = gifPath;
            this.text = text;
            this.speakerName = speakerName;
            this.options = new ArrayList<>();
        }

        /**
         * Creates a frame with dialogue options.
         */
        public CutsceneFrame(String gifPath, String text, String speakerName, List<DialogueOption> options) {
            this.gifPath = gifPath;
            this.text = text;
            this.speakerName = speakerName;
            this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
        }

        public String getGifPath() {
            return gifPath;
        }

        public String getText() {
            return text;
        }

        public String getSpeakerName() {
            return speakerName;
        }

        public List<DialogueOption> getOptions() {
            return options;
        }

        public boolean hasOptions() {
            return options != null && !options.isEmpty();
        }

        public void setGifPath(String gifPath) {
            this.gifPath = gifPath;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setSpeakerName(String speakerName) {
            this.speakerName = speakerName;
        }

        public void setOptions(List<DialogueOption> options) {
            this.options = options;
        }

        /**
         * Adds an option to this frame.
         */
        public CutsceneFrame addOption(String text) {
            this.options.add(new DialogueOption(text));
            return this;
        }

        /**
         * Adds an option with a callback to this frame.
         */
        public CutsceneFrame addOption(String text, Runnable onSelect) {
            this.options.add(new DialogueOption(text, onSelect));
            return this;
        }
    }
}
