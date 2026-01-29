package ui;

import core.GamePanel;
import graphics.AnimatedTexture;
import graphics.AssetLoader;
import input.InputManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Overlay for displaying GIF-based cutscenes.
 * Supports full-screen (1920x1080) GIF animations with click-through prompts.
 * Pauses the game while active and requires player interaction to progress.
 */
public class CutsceneOverlay {

    private boolean visible;
    private List<CutsceneFrame> frames;
    private int currentFrameIndex;
    private boolean cutsceneComplete;
    private Runnable onComplete;

    // Animation state
    private AnimatedTexture currentAnimation;
    private long lastUpdateTime;

    // Display settings
    private static final int SCREEN_WIDTH = GamePanel.SCREEN_WIDTH;
    private static final int SCREEN_HEIGHT = GamePanel.SCREEN_HEIGHT;

    // Colors
    private static final Color OVERLAY_BG = new Color(0, 0, 0, 255);
    private static final Color PROMPT_BG = new Color(0, 0, 0, 180);
    private static final Color PROMPT_TEXT = new Color(255, 255, 255);
    private static final Color PROMPT_HINT = new Color(180, 180, 180);

    // Prompt box dimensions
    private static final int PROMPT_HEIGHT = 120;
    private static final int PROMPT_MARGIN = 40;
    private static final int PROMPT_PADDING = 20;

    // Click cooldown to prevent accidental skips
    private long lastClickTime;
    private static final long CLICK_COOLDOWN_MS = 300;

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
    }

    /**
     * Starts a cutscene with the given frames.
     * @param cutsceneFrames List of frames (GIF paths + optional text)
     * @param onComplete Callback when cutscene finishes
     */
    public void startCutscene(List<CutsceneFrame> cutsceneFrames, Runnable onComplete) {
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
        this.visible = true;
        this.fadeAlpha = 0f;
        this.fadingIn = true;
        this.fadingOut = false;
        this.lastUpdateTime = System.currentTimeMillis();
        this.lastClickTime = System.currentTimeMillis(); // Prevent immediate skip

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
    }

    /**
     * Advances to the next frame or completes the cutscene.
     */
    private void advanceFrame() {
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
     * Completes the cutscene and hides the overlay.
     */
    private void completeCutscene() {
        cutsceneComplete = true;
        visible = false;
        currentAnimation = null;
        frames.clear();

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

        // Draw text prompt if present
        if (currentFrameIndex < frames.size()) {
            CutsceneFrame currentFrame = frames.get(currentFrameIndex);
            if (currentFrame.getText() != null && !currentFrame.getText().isEmpty()) {
                drawPromptBox(g2d, currentFrame.getText());
            } else {
                // Draw minimal "click to continue" hint
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
     * Draws the text prompt box at the bottom of the screen.
     */
    private void drawPromptBox(Graphics2D g2d, String text) {
        int boxY = SCREEN_HEIGHT - PROMPT_HEIGHT - PROMPT_MARGIN;
        int boxWidth = SCREEN_WIDTH - (PROMPT_MARGIN * 2);

        // Draw semi-transparent background
        g2d.setColor(PROMPT_BG);
        g2d.fillRoundRect(PROMPT_MARGIN, boxY, boxWidth, PROMPT_HEIGHT, 15, 15);

        // Draw border
        g2d.setColor(new Color(100, 100, 100, 150));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(PROMPT_MARGIN, boxY, boxWidth, PROMPT_HEIGHT, 15, 15);

        // Draw text
        g2d.setColor(PROMPT_TEXT);
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        FontMetrics fm = g2d.getFontMetrics();

        // Word wrap the text
        List<String> lines = wrapText(text, boxWidth - (PROMPT_PADDING * 2), fm);
        int textY = boxY + PROMPT_PADDING + fm.getAscent();
        for (String line : lines) {
            g2d.drawString(line, PROMPT_MARGIN + PROMPT_PADDING, textY);
            textY += fm.getHeight();
        }

        // Draw "Click to continue" hint
        g2d.setColor(PROMPT_HINT);
        g2d.setFont(new Font("Arial", Font.ITALIC, 16));
        String hint = "Click to continue...";
        fm = g2d.getFontMetrics();
        int hintX = PROMPT_MARGIN + boxWidth - PROMPT_PADDING - fm.stringWidth(hint);
        int hintY = boxY + PROMPT_HEIGHT - PROMPT_PADDING;
        g2d.drawString(hint, hintX, hintY);

        // Draw frame indicator
        String frameIndicator = (currentFrameIndex + 1) + " / " + frames.size();
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        fm = g2d.getFontMetrics();
        g2d.drawString(frameIndicator, PROMPT_MARGIN + PROMPT_PADDING, hintY);
    }

    /**
     * Draws a minimal "click to continue" hint when there's no text.
     */
    private void drawContinueHint(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 180));
        g2d.setFont(new Font("Arial", Font.ITALIC, 20));
        String hint = "Click to continue...";
        FontMetrics fm = g2d.getFontMetrics();
        int hintX = (SCREEN_WIDTH - fm.stringWidth(hint)) / 2;
        int hintY = SCREEN_HEIGHT - 50;
        g2d.drawString(hint, hintX, hintY);

        // Draw frame indicator
        if (frames.size() > 1) {
            String frameIndicator = (currentFrameIndex + 1) + " / " + frames.size();
            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
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
     * Handles key press events.
     * @return true if event was consumed
     */
    public boolean handleKeyPressed(int keyCode) {
        if (!visible || fadingIn || fadingOut) return visible;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < CLICK_COOLDOWN_MS) {
            return true;
        }

        // Space, Enter, or Escape advances the cutscene
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
    }

    /**
     * Represents a single frame in a cutscene.
     */
    public static class CutsceneFrame {
        private String gifPath;
        private String text;

        public CutsceneFrame(String gifPath, String text) {
            this.gifPath = gifPath;
            this.text = text;
        }

        public CutsceneFrame(String gifPath) {
            this(gifPath, null);
        }

        public String getGifPath() {
            return gifPath;
        }

        public String getText() {
            return text;
        }

        public void setGifPath(String gifPath) {
            this.gifPath = gifPath;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
