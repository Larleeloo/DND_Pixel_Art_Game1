import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;

class GamePanel extends JPanel implements Runnable {

    private Thread gameThread;
    private final int FPS = 60;

    private EntityManager entityManager;
    private InputManager inputManager;

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
        addKeyListener(inputManager);

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

        // Start player on the ground
        entityManager.addEntity(new PlayerEntity(100, 620, "assets/player.png"));

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

        // Draw UI info on top
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Ground Y: " + GROUND_Y, 10, 30);
        g.drawString("Use A/D to move, SPACE to jump", 10, 60);
    }
}