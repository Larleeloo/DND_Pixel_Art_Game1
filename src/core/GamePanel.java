package core;
import scene.*;
import entity.*;
import entity.player.*;
import input.*;
import audio.*;
import graphics.*;

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

        addKeyListener(inputManager);
        addMouseWheelListener(inputManager);
        addMouseListener(inputManager);  // For left-click mining

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

    private void update() {
        sceneManager.update(inputManager);
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
    }
}
