package scene;
import core.*;
import entity.*;
import entity.player.*;
import entity.mob.*;
import block.*;
import animation.*;
import graphics.*;
import level.*;
import audio.*;
import input.*;
import ui.*;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Level selection scene that displays available levels.
 * Levels are only loaded when clicked, not at startup.
 */
class LevelSelectionScene implements Scene {

    private ArrayList<LevelEntry> levelEntries;
    private ArrayList<UIButton> buttons;
    private UIButton backButton;
    private boolean initialized;
    private float animationTime;

    // Scroll support for many levels
    private int scrollOffset;
    private int maxVisibleLevels;
    private int totalLevelHeight;

    // Visual constants
    private static final int ENTRY_WIDTH = 600;
    private static final int ENTRY_HEIGHT = 100;
    private static final int ENTRY_SPACING = 20;
    private static final int START_Y = 180;
    private static final int SIDE_MARGIN = 50;

    /**
     * Stores level path and metadata for display.
     */
    private static class LevelEntry {
        String path;
        String name;
        String description;
        Rectangle bounds;
        boolean hovered;

        LevelEntry(String path, String name, String description) {
            this.path = path;
            this.name = name;
            this.description = description;
            this.hovered = false;
        }
    }

    public LevelSelectionScene() {
        this.initialized = false;
    }

    @Override
    public void init() {
        if (initialized) return;

        System.out.println("LevelSelectionScene: Initializing...");

        levelEntries = new ArrayList<>();
        buttons = new ArrayList<>();
        animationTime = 0;
        scrollOffset = 0;

        // Scan for levels and load only metadata (lightweight)
        scanForLevels();

        // Calculate layout
        calculateLayout();

        // Create navigation buttons
        createButtons();

        initialized = true;
        System.out.println("LevelSelectionScene: Initialized with " + levelEntries.size() + " levels");
    }

    /**
     * Scan the levels directory and load only metadata (not full level data).
     */
    private void scanForLevels() {
        File levelsDir = new File("levels");
        if (!levelsDir.exists() || !levelsDir.isDirectory()) {
            System.out.println("LevelSelectionScene: No levels directory found");
            return;
        }

        File[] files = levelsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            System.out.println("LevelSelectionScene: No level files found");
            return;
        }

        // Sort files alphabetically for consistent ordering
        java.util.Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        for (File file : files) {
            String path = file.getPath();

            // Use lightweight metadata loading - doesn't parse full level!
            LevelLoader.LevelMetadata metadata = LevelLoader.loadMetadataOnly(path);

            String name;
            String description;

            if (metadata != null) {
                name = metadata.getName();
                description = metadata.getDescription();
            } else {
                // Fall back to filename
                name = formatFilename(file.getName());
                description = "";
            }

            levelEntries.add(new LevelEntry(path, name, description));
            System.out.println("LevelSelectionScene: Found level - " + name);
        }
    }

    /**
     * Format a filename into a display name.
     */
    private String formatFilename(String filename) {
        if (filename.endsWith(".json")) {
            filename = filename.substring(0, filename.length() - 5);
        }
        // Convert underscores/hyphens to spaces and capitalize
        filename = filename.replace("_", " ").replace("-", " ");
        return capitalizeWords(filename);
    }

    private String capitalizeWords(String str) {
        StringBuilder result = new StringBuilder();
        String[] words = str.split(" ");
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }
        return result.toString().trim();
    }

    /**
     * Calculate the layout for level entries.
     */
    private void calculateLayout() {
        int centerX = GamePanel.SCREEN_WIDTH / 2;
        int y = START_Y;

        for (LevelEntry entry : levelEntries) {
            entry.bounds = new Rectangle(
                    centerX - ENTRY_WIDTH / 2,
                    y,
                    ENTRY_WIDTH,
                    ENTRY_HEIGHT
            );
            y += ENTRY_HEIGHT + ENTRY_SPACING;
        }

        totalLevelHeight = y - START_Y;
        maxVisibleLevels = (GamePanel.SCREEN_HEIGHT - START_Y - 100) / (ENTRY_HEIGHT + ENTRY_SPACING);
    }

    /**
     * Create navigation buttons.
     */
    private void createButtons() {
        // Back button
        backButton = new UIButton(
                SIDE_MARGIN,
                GamePanel.SCREEN_HEIGHT - 80,
                150,
                50,
                "Back",
                () -> SceneManager.getInstance().setScene("mainMenu", SceneManager.TRANSITION_FADE)
        );
        backButton.setColors(
                new Color(100, 100, 120, 220),
                new Color(130, 130, 150, 255),
                Color.WHITE
        );
        buttons.add(backButton);
    }

    @Override
    public void update(InputManager input) {
        if (!initialized) return;

        animationTime += 0.02f;

        // Handle scroll with W/S keys or mouse wheel
        int scrollAmount = ENTRY_HEIGHT + ENTRY_SPACING;
        int maxScroll = Math.max(0, totalLevelHeight - (GamePanel.SCREEN_HEIGHT - START_Y - 100));

        if (input.isKeyJustPressed('w')) {
            scrollOffset = Math.max(0, scrollOffset - scrollAmount);
        }
        if (input.isKeyJustPressed('s')) {
            scrollOffset = Math.min(maxScroll, scrollOffset + scrollAmount);
        }

        // Handle mouse wheel scroll
        int scrollDir = input.getScrollDirection();
        if (scrollDir != 0) {
            scrollOffset += scrollDir * scrollAmount;
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!initialized) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw gradient background
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(25, 25, 50),
                0, GamePanel.SCREEN_HEIGHT, new Color(50, 40, 70)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        // Draw decorative elements
        drawDecorations(g2d);

        // Draw title
        drawTitle(g2d);

        // Set clip for level entries area
        Shape oldClip = g2d.getClip();
        g2d.setClip(0, START_Y - 10, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT - START_Y - 70);

        // Draw level entries with scroll offset
        drawLevelEntries(g2d);

        // Restore clip
        g2d.setClip(oldClip);

        // Draw scroll indicators if needed
        drawScrollIndicators(g2d);

        // Draw buttons
        for (UIButton button : buttons) {
            button.draw(g2d);
        }

        // Draw instructions
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2d.setColor(new Color(150, 150, 170));
        String instructions = "Click a level to play  |  W/S or scroll to navigate";
        FontMetrics fm = g2d.getFontMetrics();
        int instrX = (GamePanel.SCREEN_WIDTH - fm.stringWidth(instructions)) / 2;
        g2d.drawString(instructions, instrX, GamePanel.SCREEN_HEIGHT - 30);
    }

    /**
     * Draw the title with animation.
     */
    private void drawTitle(Graphics2D g2d) {
        String title = "Select Level";
        int titleY = 100 + (int)(Math.sin(animationTime) * 5);

        // Shadow
        g2d.setFont(new Font("Serif", Font.BOLD, 56));
        g2d.setColor(new Color(0, 0, 0, 150));
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (GamePanel.SCREEN_WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX + 3, titleY + 3);

        // Main title
        GradientPaint titleGradient = new GradientPaint(
                titleX, titleY - 40, new Color(200, 180, 255),
                titleX, titleY + 10, new Color(150, 130, 200)
        );
        g2d.setPaint(titleGradient);
        g2d.drawString(title, titleX, titleY);
    }

    /**
     * Draw level entry cards.
     */
    private void drawLevelEntries(Graphics2D g2d) {
        for (int i = 0; i < levelEntries.size(); i++) {
            LevelEntry entry = levelEntries.get(i);

            // Calculate display position with scroll
            int displayY = entry.bounds.y - scrollOffset;

            // Skip if off-screen
            if (displayY + ENTRY_HEIGHT < START_Y - 10 || displayY > GamePanel.SCREEN_HEIGHT - 70) {
                continue;
            }

            // Draw entry card
            drawLevelCard(g2d, entry, entry.bounds.x, displayY, i);
        }
    }

    /**
     * Draw a single level card.
     */
    private void drawLevelCard(Graphics2D g2d, LevelEntry entry, int x, int y, int index) {
        // Card background
        Color bgColor = entry.hovered
                ? new Color(80, 100, 140, 230)
                : new Color(60, 70, 100, 200);

        g2d.setColor(bgColor);
        g2d.fillRoundRect(x, y, ENTRY_WIDTH, ENTRY_HEIGHT, 15, 15);

        // Card border
        Color borderColor = entry.hovered
                ? new Color(150, 180, 255, 255)
                : new Color(100, 120, 160, 180);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(entry.hovered ? 3 : 2));
        g2d.drawRoundRect(x, y, ENTRY_WIDTH, ENTRY_HEIGHT, 15, 15);

        // Level number badge
        g2d.setColor(new Color(100, 130, 180, 255));
        g2d.fillRoundRect(x + 15, y + 20, 50, 50, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
        String numStr = String.valueOf(index + 1);
        FontMetrics fm = g2d.getFontMetrics();
        int numX = x + 15 + (50 - fm.stringWidth(numStr)) / 2;
        int numY = y + 20 + (50 + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(numStr, numX, numY);

        // Level name
        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2d.setColor(Color.WHITE);
        g2d.drawString(entry.name, x + 80, y + 40);

        // Level description
        if (entry.description != null && !entry.description.isEmpty()) {
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2d.setColor(new Color(180, 180, 200));

            // Truncate if too long
            String desc = entry.description;
            fm = g2d.getFontMetrics();
            int maxWidth = ENTRY_WIDTH - 100;
            if (fm.stringWidth(desc) > maxWidth) {
                while (fm.stringWidth(desc + "...") > maxWidth && desc.length() > 0) {
                    desc = desc.substring(0, desc.length() - 1);
                }
                desc += "...";
            }
            g2d.drawString(desc, x + 80, y + 65);
        }

        // Play indicator on hover
        if (entry.hovered) {
            g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2d.setColor(new Color(100, 255, 150));
            String playText = "Click to Play";
            fm = g2d.getFontMetrics();
            g2d.drawString(playText, x + ENTRY_WIDTH - fm.stringWidth(playText) - 20, y + ENTRY_HEIGHT - 15);
        }
    }

    /**
     * Draw scroll indicators if content is scrollable.
     */
    private void drawScrollIndicators(Graphics2D g2d) {
        int maxScroll = Math.max(0, totalLevelHeight - (GamePanel.SCREEN_HEIGHT - START_Y - 100));

        if (maxScroll <= 0) return; // No scrolling needed

        // Up indicator
        if (scrollOffset > 0) {
            g2d.setColor(new Color(200, 200, 220, 200));
            int[] xPoints = {GamePanel.SCREEN_WIDTH / 2 - 15, GamePanel.SCREEN_WIDTH / 2, GamePanel.SCREEN_WIDTH / 2 + 15};
            int[] yPoints = {START_Y - 5, START_Y - 20, START_Y - 5};
            g2d.fillPolygon(xPoints, yPoints, 3);
        }

        // Down indicator
        if (scrollOffset < maxScroll) {
            g2d.setColor(new Color(200, 200, 220, 200));
            int bottomY = GamePanel.SCREEN_HEIGHT - 90;
            int[] xPoints = {GamePanel.SCREEN_WIDTH / 2 - 15, GamePanel.SCREEN_WIDTH / 2, GamePanel.SCREEN_WIDTH / 2 + 15};
            int[] yPoints = {bottomY, bottomY + 15, bottomY};
            g2d.fillPolygon(xPoints, yPoints, 3);
        }
    }

    /**
     * Draw decorative background elements.
     */
    private void drawDecorations(Graphics2D g2d) {
        // Floating particles
        g2d.setColor(new Color(255, 255, 255, 40));
        for (int i = 0; i < 20; i++) {
            double offset = animationTime * 0.5 + i * 0.3;
            int x = (int)((Math.sin(offset + i * 0.7) + 1) * GamePanel.SCREEN_WIDTH / 2);
            int y = (int)((Math.cos(offset * 0.8 + i * 0.5) + 1) * GamePanel.SCREEN_HEIGHT / 2);
            int size = 2 + (i % 3);
            g2d.fillOval(x, y, size, size);
        }
    }

    @Override
    public void dispose() {
        System.out.println("LevelSelectionScene: Disposing...");
        initialized = false;
        levelEntries = null;
        buttons = null;
    }

    @Override
    public void onMousePressed(int x, int y) {
        // Not used
    }

    @Override
    public void onMouseReleased(int x, int y) {
        // Not used
    }

    @Override
    public void onMouseDragged(int x, int y) {
        // Not used
    }

    @Override
    public void onMouseMoved(int x, int y) {
        if (!initialized) return;

        // Update button hover states
        for (UIButton button : buttons) {
            button.handleMouseMove(x, y);
        }

        // Update level entry hover states
        for (LevelEntry entry : levelEntries) {
            int displayY = entry.bounds.y - scrollOffset;
            Rectangle displayBounds = new Rectangle(entry.bounds.x, displayY, ENTRY_WIDTH, ENTRY_HEIGHT);
            entry.hovered = displayBounds.contains(x, y);
        }
    }

    @Override
    public void onMouseClicked(int x, int y) {
        if (!initialized) return;

        // Check button clicks
        for (UIButton button : buttons) {
            button.handleClick(x, y);
        }

        // Check level entry clicks
        for (LevelEntry entry : levelEntries) {
            int displayY = entry.bounds.y - scrollOffset;
            Rectangle displayBounds = new Rectangle(entry.bounds.x, displayY, ENTRY_WIDTH, ENTRY_HEIGHT);

            if (displayBounds.contains(x, y)) {
                System.out.println("LevelSelectionScene: Loading level - " + entry.name);
                // NOW the level is actually loaded (on-demand, not at startup)
                SceneManager.getInstance().loadLevel(entry.path, SceneManager.TRANSITION_FADE);
                return;
            }
        }
    }

    @Override
    public String getName() {
        return "Level Selection";
    }
}
