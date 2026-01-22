package core;
import scene.*;
import entity.*;
import entity.player.*;
import input.*;
import audio.*;
import graphics.*;
import save.SaveManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Main game panel that handles the game loop and rendering.
 * Uses SceneManager for scene-based game state management.
 */
public class GamePanel extends JPanel implements Runnable {

    private Thread gameThread;
    private final int FPS = 60;

    private SceneManager sceneManager;
    private InputManager inputManager;
    private AudioManager audioManager;

    public static final int SCREEN_WIDTH = 1920;  // Landscape width
    public static final int SCREEN_HEIGHT = 1080; // Landscape height
    public static final int GROUND_Y = 720; // Lower third (2/3 down the screen)

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.DARK_GRAY);
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                requestFocusInWindow();
            }
        });

        // Initialize managers
        inputManager = new InputManager();
        audioManager = new AudioManager();
        sceneManager = SceneManager.getInstance();
        sceneManager.setAudioManager(audioManager);
        sceneManager.setInputManager(inputManager);

        addKeyListener(inputManager);
        addMouseWheelListener(inputManager);
        addMouseListener(inputManager);  // For left-click mining
        addMouseMotionListener(inputManager);  // For aim tracking

        // Initialize Xbox controller support
        inputManager.initializeController();

        // Load audio files
        audioManager.loadMusic("sounds/music.wav");
        audioManager.loadSound("jump", "sounds/jump.wav");
        audioManager.loadSound("collect", "sounds/collect.wav");
        audioManager.loadSound("drop", "sounds/drop.wav");

        // Set up mouse handling - forward all events to SceneManager
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                sceneManager.onMouseMoved(e.getX(), e.getY());
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                sceneManager.onMouseClicked(e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                sceneManager.onMousePressed(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                sceneManager.onMouseDragged(e.getX(), e.getY());
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                sceneManager.onMouseReleased(e.getX(), e.getY());
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        // Register scenes
        sceneManager.addScene("mainMenu", new MainMenuScene());
        sceneManager.addScene("levelSelection", new LevelSelectionScene());
        sceneManager.addScene("spriteCustomization", new SpriteCharacterCustomization());
        sceneManager.addScene("overworld", new OverworldScene());
        sceneManager.addScene("creative", new CreativeScene());
        sceneManager.addScene("lootGame", new LootGameScene());

        // Start with main menu
        sceneManager.setScene("mainMenu");

        // Start background music
        audioManager.playMusic();
        audioManager.setMusicVolume(0.7f);
        audioManager.setSFXVolume(0.8f);

        System.out.println("GamePanel initialized with SceneManager. Ground at y=" + GROUND_Y);
    }

    public void startGameLoop() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        System.out.println("GamePanel added, requesting focus...");
        requestFocusInWindow();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            while (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    // Track if controller was clicking last frame (for drag detection)
    private boolean controllerWasClicking = false;
    // Track last controller mouse position for movement detection
    private int lastControllerMouseX = -1;
    private int lastControllerMouseY = -1;

    private void update() {
        // Poll Xbox controller for input
        inputManager.pollController();

        // Handle controller mouse events for UI/menu navigation
        handleControllerMouseEvents();

        sceneManager.update(inputManager);
    }

    /**
     * Handles controller right stick as mouse movement and right trigger as clicks.
     * This allows the controller to navigate menus, trigger hover effects, and drag items.
     */
    private void handleControllerMouseEvents() {
        if (!inputManager.isUsingController()) {
            controllerWasClicking = false;
            lastControllerMouseX = -1;
            lastControllerMouseY = -1;
            return;
        }

        int mouseX = inputManager.getMouseX();
        int mouseY = inputManager.getMouseY();

        // Check if mouse position changed → trigger mouse move for hover effects
        if (mouseX != lastControllerMouseX || mouseY != lastControllerMouseY) {
            sceneManager.onMouseMoved(mouseX, mouseY);
            lastControllerMouseX = mouseX;
            lastControllerMouseY = mouseY;
        }

        // Right trigger just pressed → mouse press and click
        if (inputManager.isControllerClickJustPressed()) {
            sceneManager.onMousePressed(mouseX, mouseY);
            sceneManager.onMouseClicked(mouseX, mouseY);
            controllerWasClicking = true;
        }
        // Right trigger held → mouse drag (if position changed)
        else if (inputManager.isControllerClickHeld() && controllerWasClicking) {
            sceneManager.onMouseDragged(mouseX, mouseY);
        }
        // Right trigger just released → mouse release
        else if (inputManager.isControllerClickJustReleased()) {
            sceneManager.onMouseReleased(mouseX, mouseY);
            controllerWasClicking = false;
        }
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        sceneManager.draw(g);

        // Draw controller cursor when using controller
        drawControllerCursor(g);
    }

    /**
     * Draws a visible cursor when using the Xbox controller.
     * The cursor appears at the virtual mouse position controlled by the right stick.
     */
    private void drawControllerCursor(Graphics g) {
        if (!inputManager.isUsingController()) {
            return;
        }

        int cursorX = inputManager.getMouseX();
        int cursorY = inputManager.getMouseY();

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Cursor size
        int outerSize = 24;
        int innerSize = 8;
        int crosshairLength = 12;
        int crosshairGap = 6;

        // Draw outer circle (white with black outline for visibility)
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(Color.BLACK);
        g2d.drawOval(cursorX - outerSize/2, cursorY - outerSize/2, outerSize, outerSize);

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.WHITE);
        g2d.drawOval(cursorX - outerSize/2, cursorY - outerSize/2, outerSize, outerSize);

        // Draw center dot
        g2d.setColor(Color.BLACK);
        g2d.fillOval(cursorX - innerSize/2 - 1, cursorY - innerSize/2 - 1, innerSize + 2, innerSize + 2);
        g2d.setColor(new Color(255, 200, 50)); // Golden yellow center
        g2d.fillOval(cursorX - innerSize/2, cursorY - innerSize/2, innerSize, innerSize);

        // Draw crosshair lines (with gap in center)
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(Color.BLACK);
        // Top line
        g2d.drawLine(cursorX, cursorY - crosshairGap - crosshairLength, cursorX, cursorY - crosshairGap);
        // Bottom line
        g2d.drawLine(cursorX, cursorY + crosshairGap, cursorX, cursorY + crosshairGap + crosshairLength);
        // Left line
        g2d.drawLine(cursorX - crosshairGap - crosshairLength, cursorY, cursorX - crosshairGap, cursorY);
        // Right line
        g2d.drawLine(cursorX + crosshairGap, cursorY, cursorX + crosshairGap + crosshairLength, cursorY);

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.WHITE);
        // Top line
        g2d.drawLine(cursorX, cursorY - crosshairGap - crosshairLength, cursorX, cursorY - crosshairGap);
        // Bottom line
        g2d.drawLine(cursorX, cursorY + crosshairGap, cursorX, cursorY + crosshairGap + crosshairLength);
        // Left line
        g2d.drawLine(cursorX - crosshairGap - crosshairLength, cursorY, cursorX - crosshairGap, cursorY);
        // Right line
        g2d.drawLine(cursorX + crosshairGap, cursorY, cursorX + crosshairGap + crosshairLength, cursorY);
    }
}
