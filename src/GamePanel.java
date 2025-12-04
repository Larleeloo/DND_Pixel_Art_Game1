import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

class GamePanel extends JPanel implements Runnable {

    private Thread gameThread;
    private final int FPS = 60;

    private EntityManager entityManager;
    private InputManager inputManager;
    private ArrayList<UIButton> buttons;
    private AudioManager audioManager;

    public static final int SCREEN_WIDTH = 1920;  // Landscape width
    public static final int SCREEN_HEIGHT = 1080; // Landscape height
    public static final int GROUND_Y = 720; // Lower third (2/3 down the screen)

    public GamePanel() {

        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.DARK_GRAY); // Dark gray so ground line shows
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                requestFocusInWindow();
            }
        });

        entityManager = new EntityManager();
        inputManager = new InputManager();
        buttons = new ArrayList<>();
        audioManager = new AudioManager();

        addKeyListener(inputManager);

        // Load audio files
        // Background music (use .wav files for best compatibility)
        audioManager.loadMusic("assets/music.wav");

        // Sound effects
        audioManager.loadSound("jump", "assets/jump.wav");
        audioManager.loadSound("collect", "assets/collect.wav");
        audioManager.loadSound("drop", "assets/drop.wav");

        // Start background music
        audioManager.playMusic();

        // Add mouse listener for buttons
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for (UIButton button : buttons) {
                    button.handleMouseMove(e.getX(), e.getY());
                }
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                for (UIButton button : buttons) {
                    button.handleClick(e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // Handle inventory drag start
                PlayerEntity player = getPlayer();
                if (player != null) {
                    player.getInventory().handleMousePressed(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // Handle inventory dragging
                PlayerEntity player = getPlayer();
                if (player != null) {
                    player.getInventory().handleMouseDragged(e.getX(), e.getY());
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Handle inventory drop
                PlayerEntity player = getPlayer();
                if (player != null) {
                    ItemEntity droppedItem = player.getInventory().handleMouseReleased(e.getX(), e.getY());
                    if (droppedItem != null) {
                        // Drop the item into the world
                        droppedItem.collected = false; // Make it collectible again
                        player.dropItem(droppedItem);
                        entityManager.addEntity(droppedItem);
                    }
                }
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        // Add background back in
        entityManager.addEntity(new BackgroundEntity("assets/background.png"));

        // Create a series of platforms to climb - much closer together!
        // Each platform is about 100-120 pixels apart horizontally
        // and only 40-50 pixels higher vertically (easily jumpable)

        // Platform 1 - First step
        entityManager.addEntity(new SpriteEntity(250, 670, "assets/obstacle.png", true));

        // Platform 2 - Second step
        entityManager.addEntity(new SpriteEntity(400, 630, "assets/obstacle.png", true));

        // Platform 3 - Third step
        entityManager.addEntity(new SpriteEntity(550, 590, "assets/obstacle.png", true));

        // Platform 4 - Fourth step
        entityManager.addEntity(new SpriteEntity(700, 550, "assets/obstacle.png", true));

        // Platform 5 - Fifth step
        entityManager.addEntity(new SpriteEntity(850, 510, "assets/obstacle.png", true));

        // Platform 6 - Sixth step
        entityManager.addEntity(new SpriteEntity(1000, 470, "assets/obstacle.png", true));

        // Platform 7 - Near the top
        entityManager.addEntity(new SpriteEntity(1150, 430, "assets/obstacle.png", true));

        // Platform 8 - Top platform!
        entityManager.addEntity(new SpriteEntity(1300, 390, "assets/obstacle.png", true));

        // Add collectible items scattered around the platforms!
        // Place items ABOVE platforms so they're reachable
        entityManager.addEntity(new ItemEntity(200, 650, "assets/obstacle.png", "Key", "key"));
        entityManager.addEntity(new ItemEntity(350, 620, "assets/obstacle.png", "Gem", "collectible"));
        entityManager.addEntity(new ItemEntity(500, 580, "assets/obstacle.png", "Coin", "collectible"));
        entityManager.addEntity(new ItemEntity(650, 540, "assets/obstacle.png", "Star", "collectible"));
        entityManager.addEntity(new ItemEntity(800, 500, "assets/obstacle.png", "Crystal", "collectible"));
        entityManager.addEntity(new ItemEntity(950, 460, "assets/obstacle.png", "Ruby", "collectible"));
        entityManager.addEntity(new ItemEntity(1100, 420, "assets/obstacle.png", "Diamond", "collectible"));
        entityManager.addEntity(new ItemEntity(1350, 350, "assets/obstacle.png", "Trophy", "special"));

        // Start player on the ground
        PlayerEntity player = new PlayerEntity(100, 620, "assets/player.png");
        player.setAudioManager(audioManager);
        entityManager.addEntity(player);

        // Create UI buttons
        // Exit button in top right corner
        UIButton exitButton = new UIButton(SCREEN_WIDTH - 150, 20, 120, 50, "Exit", () -> {
            System.out.println("Exit button clicked!");
            audioManager.dispose();
            System.exit(0);
        });
        exitButton.setColors(
                new Color(200, 50, 50, 200),  // Normal: Red
                new Color(255, 80, 80, 230),  // Hover: Brighter red
                Color.WHITE                    // Text: White
        );
        buttons.add(exitButton);

        // Music toggle button
        UIButton musicButton = new UIButton(SCREEN_WIDTH - 290, 20, 120, 50, "Music", () -> {
            audioManager.toggleMusic();
        });
        musicButton.setColors(
                new Color(50, 100, 200, 200),  // Normal: Blue
                new Color(80, 130, 255, 230),  // Hover: Brighter blue
                Color.WHITE                     // Text: White
        );
        buttons.add(musicButton);

        System.out.println("GamePanel initialized. Ground at y=" + GROUND_Y);
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
        entityManager.updateAll(inputManager);
    }

    private PlayerEntity getPlayer() {
        // Helper method to find the player
        return entityManager.getPlayer();
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        // Draw ground area (lower third of screen)
        g2d.setColor(new Color(34, 139, 34, 100)); // Semi-transparent green
        g2d.fillRect(0, GROUND_Y, SCREEN_WIDTH, SCREEN_HEIGHT - GROUND_Y);

        // Draw ground line at top of ground area
        g2d.setStroke(new BasicStroke(4));
        g2d.setColor(new Color(34, 139, 34)); // Solid green
        g2d.drawLine(0, GROUND_Y, SCREEN_WIDTH, GROUND_Y);

        // Now draw all entities
        entityManager.drawAll(g);

        // Draw UI buttons on top
        for (UIButton button : buttons) {
            button.draw(g);
        }

        // Draw UI info
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Use A/D to move, SPACE to jump, I for inventory", 10, 30);
    }
}