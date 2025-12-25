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
import java.util.ArrayList;

/**
 * Main menu scene - the game's title screen.
 * Provides navigation to level selection and other options.
 */
public class MainMenuScene implements Scene {

    private ArrayList<UIButton> buttons;
    private String title;
    private boolean initialized;
    private float titleBob;

    public MainMenuScene() {
        this.title = "The Amber Moon";
        this.initialized = false;
    }

    @Override
    public void init() {
        if (initialized) return;

        System.out.println("MainMenuScene: Initializing...");

        buttons = new ArrayList<>();
        titleBob = 0;

        // Create menu buttons
        createMenuButtons();

        initialized = true;
        System.out.println("MainMenuScene: Initialized");
    }

    /**
     * Create the menu buttons.
     */
    private void createMenuButtons() {
        int centerX = GamePanel.SCREEN_WIDTH / 2;
        int startY = 400;
        int buttonWidth = 300;
        int buttonHeight = 60;
        int spacing = 80;

        // Play button - goes to level selection
        UIButton playButton = new UIButton(
                centerX - buttonWidth / 2,
                startY,
                buttonWidth,
                buttonHeight,
                "Play",
                () -> {
                    System.out.println("MainMenuScene: Opening level selection");
                    SceneManager.getInstance().setScene("levelSelection", SceneManager.TRANSITION_FADE);
                }
        );
        playButton.setColors(
                new Color(70, 130, 180, 220),
                new Color(100, 160, 210, 255),
                Color.WHITE
        );
        buttons.add(playButton);

        // Customize button - opens character customization
        UIButton customizeButton = new UIButton(
                centerX - buttonWidth / 2,
                startY + spacing,
                buttonWidth,
                buttonHeight,
                "Customize Character",
                () -> {
                    System.out.println("MainMenuScene: Opening character customization");
                    SceneManager.getInstance().setScene("characterCustomization", SceneManager.TRANSITION_FADE);
                }
        );
        customizeButton.setColors(
                new Color(150, 100, 180, 220),
                new Color(180, 130, 210, 255),
                Color.WHITE
        );
        buttons.add(customizeButton);

        // Lighting Demo button - opens lighting demonstration
        UIButton lightingDemoButton = new UIButton(
                centerX - buttonWidth / 2,
                startY + spacing * 2,
                buttonWidth,
                buttonHeight,
                "Lighting Demo",
                () -> {
                    System.out.println("MainMenuScene: Opening lighting demo");
                    SceneManager.getInstance().setScene("lightingDemo", SceneManager.TRANSITION_FADE);
                }
        );
        lightingDemoButton.setColors(
                new Color(50, 50, 100, 220),
                new Color(80, 80, 150, 255),
                Color.WHITE
        );
        buttons.add(lightingDemoButton);

        // Exit button
        UIButton exitButton = new UIButton(
                centerX - buttonWidth / 2,
                startY + spacing * 3,
                buttonWidth,
                buttonHeight,
                "Exit Game",
                () -> {
                    AudioManager audio = SceneManager.getInstance().getAudioManager();
                    if (audio != null) audio.dispose();
                    System.exit(0);
                }
        );
        exitButton.setColors(
                new Color(200, 50, 50, 220),
                new Color(255, 80, 80, 255),
                Color.WHITE
        );
        buttons.add(exitButton);
    }

    @Override
    public void update(InputManager input) {
        if (!initialized) return;

        // Animate title
        titleBob += 0.02f;
    }

    @Override
    public void draw(Graphics g) {
        if (!initialized) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw gradient background
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(20, 20, 40),
                0, GamePanel.SCREEN_HEIGHT, new Color(40, 40, 80)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        // Draw decorative stars
        drawStars(g2d);

        // Draw title with shadow and bobbing
        int titleY = 200 + (int)(Math.sin(titleBob) * 10);

        // Shadow
        g2d.setFont(new Font("Serif", Font.BOLD, 72));
        g2d.setColor(new Color(0, 0, 0, 150));
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (GamePanel.SCREEN_WIDTH - fm.stringWidth(title)) / 2;
        g2d.drawString(title, titleX + 4, titleY + 4);

        // Main title with gradient
        GradientPaint titleGradient = new GradientPaint(
                titleX, titleY - 50, new Color(255, 200, 100),
                titleX, titleY + 20, new Color(255, 150, 50)
        );
        g2d.setPaint(titleGradient);
        g2d.drawString(title, titleX, titleY);

        // Subtitle
        g2d.setFont(new Font("SansSerif", Font.ITALIC, 24));
        g2d.setColor(new Color(200, 200, 220));
        String subtitle = "A DND Pixel Art Adventure";
        fm = g2d.getFontMetrics();
        int subtitleX = (GamePanel.SCREEN_WIDTH - fm.stringWidth(subtitle)) / 2;
        g2d.drawString(subtitle, subtitleX, titleY + 50);

        // Draw buttons
        for (UIButton button : buttons) {
            button.draw(g);
        }

        // Draw instructions at bottom
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2d.setColor(new Color(150, 150, 170));
        String instructions = "Press Play to begin your adventure";
        fm = g2d.getFontMetrics();
        int instrX = (GamePanel.SCREEN_WIDTH - fm.stringWidth(instructions)) / 2;
        g2d.drawString(instructions, instrX, GamePanel.SCREEN_HEIGHT - 50);
    }

    /**
     * Draw decorative background stars.
     */
    private void drawStars(Graphics2D g2d) {
        g2d.setColor(new Color(255, 255, 255, 100));

        // Fixed star positions for consistent look
        int[][] stars = {
                {100, 100}, {300, 150}, {500, 80}, {700, 200}, {900, 120},
                {1100, 180}, {1300, 90}, {1500, 160}, {1700, 100}, {1850, 140},
                {150, 300}, {450, 250}, {750, 320}, {1050, 280}, {1350, 310},
                {1650, 260}, {200, 500}, {600, 450}, {1000, 520}, {1400, 480},
                {1800, 450}, {250, 700}, {550, 680}, {850, 720}, {1150, 690},
                {1450, 750}, {1750, 710}
        };

        for (int[] star : stars) {
            int size = (int)(Math.random() * 3) + 1;
            float alpha = 0.3f + (float)(Math.sin(titleBob * 2 + star[0] * 0.01) * 0.3);
            g2d.setColor(new Color(255, 255, 255, (int)(alpha * 255)));
            g2d.fillOval(star[0], star[1], size, size);
        }
    }

    @Override
    public void dispose() {
        System.out.println("MainMenuScene: Disposing...");
        initialized = false;
        buttons = null;
    }

    @Override
    public void onMousePressed(int x, int y) {
        // Not used in menu
    }

    @Override
    public void onMouseReleased(int x, int y) {
        // Not used in menu
    }

    @Override
    public void onMouseDragged(int x, int y) {
        // Not used in menu
    }

    @Override
    public void onMouseMoved(int x, int y) {
        if (buttons != null) {
            for (UIButton button : buttons) {
                button.handleMouseMove(x, y);
            }
        }
    }

    @Override
    public void onMouseClicked(int x, int y) {
        if (buttons != null) {
            for (UIButton button : buttons) {
                button.handleClick(x, y);
            }
        }
    }

    @Override
    public String getName() {
        return "Main Menu";
    }
}
