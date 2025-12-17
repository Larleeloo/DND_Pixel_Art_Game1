import java.awt.*;
import java.util.ArrayList;

/**
 * Demonstration scene for the day/night cycle and lighting system.
 * Shows various light sources and allows toggling between day and night.
 */
class LightingDemoScene implements Scene {

    private ArrayList<UIButton> buttons;
    private LightingSystem lightingSystem;
    private boolean initialized;

    // Scene timing
    private long lastUpdateTime;
    private double deltaTime;

    // Visual elements for the demo environment
    private int groundY;

    // Light source references for interactive demo
    private ArrayList<LightSource> torches;
    private LightSource campfire;
    private ArrayList<LightSource> lanterns;
    private LightSource magicOrb;
    private LightSource playerLight;

    // Player position (simple for demo)
    private double playerX, playerY;
    private double playerVelX;
    private boolean playerLightEnabled;

    public LightingDemoScene() {
        this.initialized = false;
    }

    @Override
    public void init() {
        if (initialized) return;

        System.out.println("LightingDemoScene: Initializing...");

        buttons = new ArrayList<>();
        torches = new ArrayList<>();
        lanterns = new ArrayList<>();

        groundY = GamePanel.SCREEN_HEIGHT - 150;

        // Initialize player position
        playerX = GamePanel.SCREEN_WIDTH / 2;
        playerY = groundY - 40;
        playerVelX = 0;
        playerLightEnabled = true;

        // Initialize the lighting system
        lightingSystem = new LightingSystem(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        lightingSystem.setNight(true);  // Start in night mode to show off the lighting
        lightingSystem.setNightDarkness(0.80);  // 80% darkness
        lightingSystem.setAmbientLevel(0.12);   // 12% ambient light

        // Set up light sources
        setupLightSources();

        // Create UI buttons
        createUI();

        lastUpdateTime = System.nanoTime();
        initialized = true;

        System.out.println("LightingDemoScene: Initialized with " + lightingSystem.getLightSources().size() + " light sources");
    }

    /**
     * Set up various light sources across the scene.
     */
    private void setupLightSources() {
        // Torches along the ground (like wall-mounted torches)
        int[] torchPositions = {150, 450, 750, 1050, 1350, 1650};
        for (int tx : torchPositions) {
            LightSource torch = LightingSystem.createTorchLight(tx, groundY - 100);
            torches.add(torch);
            lightingSystem.addLightSource(torch);
        }

        // Campfire in the middle
        campfire = LightingSystem.createCampfireLight(GamePanel.SCREEN_WIDTH / 2, groundY - 30);
        lightingSystem.addLightSource(campfire);

        // Lanterns at higher positions (like hanging lanterns)
        int[] lanternPositions = {300, 900, 1500};
        for (int lx : lanternPositions) {
            LightSource lantern = LightingSystem.createLanternLight(lx, 200);
            lanterns.add(lantern);
            lightingSystem.addLightSource(lantern);
        }

        // Magic orb on the right side
        magicOrb = LightingSystem.createMagicLight(1700, 400);
        lightingSystem.addLightSource(magicOrb);

        // Player's personal light (like a torch they carry)
        playerLight = new LightSource(playerX, playerY, 100, new Color(255, 220, 150));
        playerLight.setFalloffRadius(200);
        playerLight.enableFlicker(0.08, 5.0);
        lightingSystem.addLightSource(playerLight);
    }

    /**
     * Create UI elements for the demo.
     */
    private void createUI() {
        int buttonY = 20;
        int buttonHeight = 50;
        int spacing = 10;

        // Day/Night toggle button
        UIButton dayNightButton = new UIButton(20, buttonY, 180, buttonHeight, "Toggle Day/Night", () -> {
            lightingSystem.toggleDayNight();
            System.out.println("LightingDemoScene: " + (lightingSystem.isNight() ? "Night" : "Day") + " mode");
        });
        dayNightButton.setColors(
                new Color(50, 50, 100, 220),
                new Color(80, 80, 150, 255),
                Color.WHITE
        );
        buttons.add(dayNightButton);

        // Toggle player light
        UIButton playerLightButton = new UIButton(210, buttonY, 160, buttonHeight, "Player Light", () -> {
            playerLightEnabled = !playerLightEnabled;
            playerLight.setEnabled(playerLightEnabled);
            System.out.println("LightingDemoScene: Player light " + (playerLightEnabled ? "on" : "off"));
        });
        playerLightButton.setColors(
                new Color(150, 120, 50, 220),
                new Color(200, 160, 80, 255),
                Color.WHITE
        );
        buttons.add(playerLightButton);

        // Toggle campfire
        UIButton campfireButton = new UIButton(380, buttonY, 140, buttonHeight, "Campfire", () -> {
            campfire.setEnabled(!campfire.isEnabled());
            System.out.println("LightingDemoScene: Campfire " + (campfire.isEnabled() ? "on" : "off"));
        });
        campfireButton.setColors(
                new Color(180, 80, 30, 220),
                new Color(220, 120, 50, 255),
                Color.WHITE
        );
        buttons.add(campfireButton);

        // Toggle torches
        UIButton torchesButton = new UIButton(530, buttonY, 130, buttonHeight, "Torches", () -> {
            boolean newState = !torches.get(0).isEnabled();
            for (LightSource torch : torches) {
                torch.setEnabled(newState);
            }
            System.out.println("LightingDemoScene: Torches " + (newState ? "on" : "off"));
        });
        torchesButton.setColors(
                new Color(200, 100, 50, 220),
                new Color(240, 140, 80, 255),
                Color.WHITE
        );
        buttons.add(torchesButton);

        // Toggle lanterns
        UIButton lanternsButton = new UIButton(670, buttonY, 130, buttonHeight, "Lanterns", () -> {
            boolean newState = !lanterns.get(0).isEnabled();
            for (LightSource lantern : lanterns) {
                lantern.setEnabled(newState);
            }
            System.out.println("LightingDemoScene: Lanterns " + (newState ? "on" : "off"));
        });
        lanternsButton.setColors(
                new Color(200, 180, 80, 220),
                new Color(240, 220, 120, 255),
                new Color(50, 50, 50)
        );
        buttons.add(lanternsButton);

        // Toggle magic orb
        UIButton magicButton = new UIButton(810, buttonY, 130, buttonHeight, "Magic Orb", () -> {
            magicOrb.setEnabled(!magicOrb.isEnabled());
            System.out.println("LightingDemoScene: Magic orb " + (magicOrb.isEnabled() ? "on" : "off"));
        });
        magicButton.setColors(
                new Color(100, 80, 180, 220),
                new Color(140, 120, 220, 255),
                Color.WHITE
        );
        buttons.add(magicButton);

        // Darkness slider label and controls
        UIButton darkerButton = new UIButton(960, buttonY, 100, buttonHeight, "Darker", () -> {
            double current = lightingSystem.isNight() ? 0.80 : 0;
            lightingSystem.setNightDarkness(Math.min(0.95, current + 0.05));
            System.out.println("LightingDemoScene: Darkness increased");
        });
        darkerButton.setColors(
                new Color(30, 30, 50, 220),
                new Color(50, 50, 80, 255),
                Color.WHITE
        );
        buttons.add(darkerButton);

        UIButton lighterButton = new UIButton(1070, buttonY, 100, buttonHeight, "Lighter", () -> {
            double current = lightingSystem.isNight() ? 0.80 : 0;
            lightingSystem.setNightDarkness(Math.max(0.3, current - 0.05));
            System.out.println("LightingDemoScene: Darkness decreased");
        });
        lighterButton.setColors(
                new Color(100, 100, 130, 220),
                new Color(140, 140, 180, 255),
                Color.WHITE
        );
        buttons.add(lighterButton);

        // Menu button (return to main menu)
        UIButton menuButton = new UIButton(GamePanel.SCREEN_WIDTH - 140, buttonY, 120, buttonHeight, "Menu", () -> {
            SceneManager.getInstance().setScene("mainMenu", SceneManager.TRANSITION_FADE);
        });
        menuButton.setColors(
                new Color(50, 150, 50, 220),
                new Color(80, 200, 80, 255),
                Color.WHITE
        );
        buttons.add(menuButton);
    }

    @Override
    public void update(InputManager input) {
        if (!initialized) return;

        // Calculate delta time
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0;
        lastUpdateTime = currentTime;

        // Update lighting system
        lightingSystem.update(deltaTime);

        // Handle player movement (A/D keys)
        playerVelX = 0;
        if (input.isKeyPressed('a') || input.isKeyPressed('A')) {
            playerVelX = -4;
        }
        if (input.isKeyPressed('d') || input.isKeyPressed('D')) {
            playerVelX = 4;
        }

        // Update player position
        playerX += playerVelX;
        playerX = Math.max(50, Math.min(GamePanel.SCREEN_WIDTH - 50, playerX));

        // Update player light position
        playerLight.setPosition(playerX, playerY - 20);
    }

    @Override
    public void draw(Graphics g) {
        if (!initialized) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw sky gradient (changes based on day/night)
        drawSky(g2d);

        // Draw stars (visible at night)
        if (lightingSystem.isNight()) {
            drawStars(g2d);
        }

        // Draw environment
        drawEnvironment(g2d);

        // Draw light source indicators (before lighting overlay)
        drawLightIndicators(g2d);

        // Draw player
        drawPlayer(g2d);

        // Apply lighting overlay (this dims everything and creates light circles)
        lightingSystem.render(g2d);

        // Draw UI (on top of lighting)
        drawUI(g2d);
    }

    /**
     * Draw the sky background.
     */
    private void drawSky(Graphics2D g2d) {
        Color topColor, bottomColor;

        if (lightingSystem.isNight()) {
            topColor = new Color(5, 5, 20);
            bottomColor = new Color(20, 20, 50);
        } else {
            topColor = new Color(100, 150, 220);
            bottomColor = new Color(180, 210, 240);
        }

        GradientPaint gradient = new GradientPaint(
                0, 0, topColor,
                0, groundY, bottomColor
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, GamePanel.SCREEN_WIDTH, groundY);
    }

    /**
     * Draw stars in the night sky.
     */
    private void drawStars(Graphics2D g2d) {
        // Fixed star positions
        int[][] stars = {
                {100, 50}, {250, 120}, {400, 30}, {550, 90}, {700, 60},
                {850, 140}, {1000, 40}, {1150, 110}, {1300, 70}, {1450, 130},
                {1600, 50}, {1750, 100}, {180, 180}, {380, 200}, {580, 160},
                {780, 220}, {980, 190}, {1180, 170}, {1380, 210}, {1580, 180},
                {1780, 200}, {50, 250}, {220, 280}, {450, 240}, {680, 270},
                {910, 250}, {1140, 290}, {1370, 260}, {1600, 280}, {1830, 240}
        };

        double time = lightingSystem.getGameTime();

        for (int[] star : stars) {
            // Twinkle effect
            float alpha = 0.4f + (float)(Math.sin(time * 2 + star[0] * 0.05 + star[1] * 0.03) * 0.3);
            alpha = Math.max(0.1f, Math.min(1.0f, alpha));

            int size = (star[0] + star[1]) % 3 + 1;

            g2d.setColor(new Color(255, 255, 255, (int)(alpha * 200)));
            g2d.fillOval(star[0], star[1], size, size);
        }

        // Draw a moon
        g2d.setColor(new Color(240, 240, 220));
        g2d.fillOval(1700, 80, 80, 80);
        // Moon shadow
        g2d.setColor(new Color(5, 5, 20));
        g2d.fillOval(1720, 70, 60, 60);
    }

    /**
     * Draw the environment (ground, trees, structures).
     */
    private void drawEnvironment(Graphics2D g2d) {
        // Ground
        g2d.setColor(new Color(40, 80, 40));
        g2d.fillRect(0, groundY, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT - groundY);

        // Ground top line (grass)
        g2d.setColor(new Color(60, 120, 60));
        g2d.fillRect(0, groundY, GamePanel.SCREEN_WIDTH, 10);

        // Draw some simple structures to cast shadows

        // Left house
        g2d.setColor(new Color(100, 70, 50));
        g2d.fillRect(80, groundY - 120, 120, 120);
        g2d.setColor(new Color(120, 50, 40));
        int[] xPoints1 = {70, 140, 210};
        int[] yPoints1 = {groundY - 120, groundY - 180, groundY - 120};
        g2d.fillPolygon(xPoints1, yPoints1, 3);

        // Right house
        g2d.setColor(new Color(90, 65, 45));
        g2d.fillRect(1600, groundY - 140, 140, 140);
        g2d.setColor(new Color(110, 45, 35));
        int[] xPoints2 = {1590, 1670, 1750};
        int[] yPoints2 = {groundY - 140, groundY - 210, groundY - 140};
        g2d.fillPolygon(xPoints2, yPoints2, 3);

        // Windows (will glow with light at night)
        g2d.setColor(lightingSystem.isNight() ? new Color(255, 220, 150, 100) : new Color(150, 200, 255, 100));
        g2d.fillRect(110, groundY - 100, 30, 40);
        g2d.fillRect(150, groundY - 100, 30, 40);
        g2d.fillRect(1630, groundY - 120, 35, 45);
        g2d.fillRect(1680, groundY - 120, 35, 45);

        // Trees (silhouettes)
        drawTree(g2d, 350, groundY, 60);
        drawTree(g2d, 600, groundY, 80);
        drawTree(g2d, 1200, groundY, 70);
        drawTree(g2d, 1400, groundY, 90);

        // Torch poles
        g2d.setColor(new Color(80, 60, 40));
        int[] torchPositions = {150, 450, 750, 1050, 1350, 1650};
        for (int tx : torchPositions) {
            g2d.fillRect(tx - 4, groundY - 110, 8, 110);
            // Torch holder
            g2d.setColor(new Color(60, 60, 60));
            g2d.fillRect(tx - 10, groundY - 115, 20, 15);
            g2d.setColor(new Color(80, 60, 40));
        }

        // Campfire logs
        g2d.setColor(new Color(70, 50, 30));
        int cfx = GamePanel.SCREEN_WIDTH / 2;
        g2d.fillOval(cfx - 40, groundY - 15, 80, 20);
        g2d.setColor(new Color(50, 35, 20));
        g2d.fillRect(cfx - 30, groundY - 25, 15, 30);
        g2d.fillRect(cfx + 15, groundY - 25, 15, 30);

        // Lantern poles (tall)
        g2d.setColor(new Color(60, 60, 70));
        int[] lanternPositions = {300, 900, 1500};
        for (int lx : lanternPositions) {
            g2d.fillRect(lx - 3, 190, 6, groundY - 190);
            // Lantern box
            g2d.setColor(new Color(80, 70, 50));
            g2d.fillRect(lx - 15, 185, 30, 35);
            g2d.setColor(new Color(60, 60, 70));
        }

        // Magic orb pedestal
        g2d.setColor(new Color(60, 50, 80));
        g2d.fillRect(1680, groundY - 80, 40, 80);
        g2d.setColor(new Color(80, 70, 100));
        g2d.fillOval(1685, 385, 30, 30);
    }

    /**
     * Draw a simple tree silhouette.
     */
    private void drawTree(Graphics2D g2d, int x, int baseY, int size) {
        // Trunk
        g2d.setColor(new Color(50, 35, 25));
        g2d.fillRect(x - size / 8, baseY - size, size / 4, size);

        // Foliage (triangle)
        g2d.setColor(new Color(30, 60, 30));
        int[] xPoints = {x - size / 2, x, x + size / 2};
        int[] yPoints = {baseY - size, baseY - size * 2, baseY - size};
        g2d.fillPolygon(xPoints, yPoints, 3);

        // Upper foliage
        int[] xPoints2 = {x - size / 3, x, x + size / 3};
        int[] yPoints2 = {baseY - size - size / 2, baseY - size * 2 - size / 3, baseY - size - size / 2};
        g2d.fillPolygon(xPoints2, yPoints2, 3);
    }

    /**
     * Draw visual indicators for light sources.
     */
    private void drawLightIndicators(Graphics2D g2d) {
        double time = lightingSystem.getGameTime();

        // Torch flames
        for (LightSource torch : torches) {
            if (torch.isEnabled()) {
                drawFlame(g2d, (int) torch.getX(), (int) torch.getY() - 10, 15, time, torch);
            }
        }

        // Campfire flames
        if (campfire.isEnabled()) {
            drawFlame(g2d, (int) campfire.getX(), (int) campfire.getY() - 20, 35, time, campfire);
        }

        // Lantern glow
        for (LightSource lantern : lanterns) {
            if (lantern.isEnabled()) {
                g2d.setColor(new Color(255, 240, 180, 200));
                g2d.fillOval((int) lantern.getX() - 8, (int) lantern.getY() - 8, 16, 16);
            }
        }

        // Magic orb
        if (magicOrb.isEnabled()) {
            float pulse = (float) (0.7 + 0.3 * Math.sin(time * 3));
            int orbSize = (int) (25 * pulse);
            g2d.setColor(new Color(150, 150, 255, (int) (200 * pulse)));
            g2d.fillOval((int) magicOrb.getX() - orbSize / 2, (int) magicOrb.getY() - orbSize / 2, orbSize, orbSize);
            // Inner glow
            g2d.setColor(new Color(200, 200, 255, (int) (255 * pulse)));
            g2d.fillOval((int) magicOrb.getX() - orbSize / 4, (int) magicOrb.getY() - orbSize / 4, orbSize / 2, orbSize / 2);
        }
    }

    /**
     * Draw an animated flame effect.
     */
    private void drawFlame(Graphics2D g2d, int x, int y, int size, double time, LightSource light) {
        double flicker = light.getEffectiveIntensity(time);

        // Outer flame (orange)
        g2d.setColor(new Color(255, 150, 50, (int) (200 * flicker)));
        int[] xPoints = {x - size / 2, x, x + size / 2, x};
        int yOffset = (int) (Math.sin(time * 10) * 3);
        int[] yPoints = {y, y - size - yOffset, y, y + size / 4};
        g2d.fillPolygon(xPoints, yPoints, 4);

        // Inner flame (yellow)
        g2d.setColor(new Color(255, 220, 100, (int) (230 * flicker)));
        int innerSize = size * 2 / 3;
        int[] xPoints2 = {x - innerSize / 2, x, x + innerSize / 2, x};
        int[] yPoints2 = {y, y - innerSize - yOffset / 2, y, y + innerSize / 4};
        g2d.fillPolygon(xPoints2, yPoints2, 4);

        // Core (white-yellow)
        g2d.setColor(new Color(255, 255, 200, (int) (255 * flicker)));
        int coreSize = size / 3;
        g2d.fillOval(x - coreSize / 2, y - coreSize / 2, coreSize, coreSize);
    }

    /**
     * Draw the player character.
     */
    private void drawPlayer(Graphics2D g2d) {
        int px = (int) playerX;
        int py = (int) playerY;

        // Simple player representation
        // Body
        g2d.setColor(new Color(100, 100, 180));
        g2d.fillRect(px - 15, py - 40, 30, 40);

        // Head
        g2d.setColor(new Color(220, 180, 150));
        g2d.fillOval(px - 12, py - 60, 24, 24);

        // Torch in hand (if player light is enabled)
        if (playerLightEnabled) {
            g2d.setColor(new Color(80, 60, 40));
            g2d.fillRect(px + 15, py - 50, 5, 30);
            drawFlame(g2d, px + 17, py - 55, 12, lightingSystem.getGameTime(), playerLight);
        }
    }

    /**
     * Draw UI elements.
     */
    private void drawUI(Graphics2D g2d) {
        // Draw buttons
        for (UIButton button : buttons) {
            button.draw(g2d);
        }

        // Draw status text
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        String status = lightingSystem.isNight() ? "NIGHT" : "DAY";
        g2d.drawString("Time: " + status, 20, 100);

        // Instructions
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.drawString("Use A/D to move the player | Toggle light sources with buttons above", 20, GamePanel.SCREEN_HEIGHT - 30);
        g2d.drawString("Light sources illuminate the darkness, creating visible areas at night", 20, GamePanel.SCREEN_HEIGHT - 10);
    }

    @Override
    public void dispose() {
        System.out.println("LightingDemoScene: Disposing...");
        initialized = false;
        buttons = null;
        lightingSystem = null;
        torches = null;
        lanterns = null;
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
        return "Lighting Demo";
    }
}
