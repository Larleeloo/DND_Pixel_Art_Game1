import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Main menu scene with level selection.
 */
class MainMenuScene implements Scene {

    private ArrayList<UIButton> buttons;
    private ArrayList<String> levelPaths;
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
        levelPaths = new ArrayList<>();
        titleBob = 0;

        // Scan for level files
        scanForLevels();

        // Create menu buttons
        createMenuButtons();

        initialized = true;
        System.out.println("MainMenuScene: Initialized with " + levelPaths.size() + " levels");
    }

    /**
     * Scan the levels directory for JSON files.
     */
    private void scanForLevels() {
        // Check for levels directory
        File levelsDir = new File("levels");
        if (levelsDir.exists() && levelsDir.isDirectory()) {
            File[] files = levelsDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    levelPaths.add(file.getPath());
                    System.out.println("MainMenuScene: Found level " + file.getPath());
                }
            }
        }

        // Always add a default level option if no levels found
        if (levelPaths.isEmpty()) {
            System.out.println("MainMenuScene: No level files found, will use default level");
        }
    }

    /**
     * Create the menu buttons.
     */
    private void createMenuButtons() {
        int centerX = GamePanel.SCREEN_WIDTH / 2;
        int startY = 350;
        int buttonWidth = 300;
        int buttonHeight = 60;
        int spacing = 80;

        // If we have level files, create a button for each
        if (!levelPaths.isEmpty()) {
            for (int i = 0; i < levelPaths.size(); i++) {
                final String path = levelPaths.get(i);
                String levelName = getLevelName(path, i + 1);

                UIButton levelButton = new UIButton(
                        centerX - buttonWidth / 2,
                        startY + (i * spacing),
                        buttonWidth,
                        buttonHeight,
                        levelName,
                        () -> {
                            System.out.println("Loading level: " + path);
                            SceneManager.getInstance().loadLevel(path, SceneManager.TRANSITION_FADE);
                        }
                );
                levelButton.setColors(
                        new Color(70, 130, 180, 220),
                        new Color(100, 160, 210, 255),
                        Color.WHITE
                );
                buttons.add(levelButton);
            }
        } else {
            // No level files found - create a "Start Game" button
            UIButton startButton = new UIButton(
                    centerX - buttonWidth / 2,
                    startY,
                    buttonWidth,
                    buttonHeight,
                    "Start Game",
                    () -> {
                        // Create a default GameScene
                        GameScene defaultGame = new GameScene((LevelData) null);
                        SceneManager.getInstance().addScene("defaultGame", defaultGame);
                        SceneManager.getInstance().setScene("defaultGame", SceneManager.TRANSITION_FADE);
                    }
            );
            startButton.setColors(
                    new Color(70, 130, 180, 220),
                    new Color(100, 160, 210, 255),
                    Color.WHITE
            );
            buttons.add(startButton);
        }

        // Exit button (always at bottom)
        int exitY = startY + Math.max(levelPaths.size(), 1) * spacing + 40;
        UIButton exitButton = new UIButton(
                centerX - buttonWidth / 2,
                exitY,
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

    /**
     * Try to get a nice name for the level from its file path or content.
     */
    private String getLevelName(String path, int fallbackNumber) {
        // Try to load just the name from the file
        LevelData data = LevelLoader.load(path);
        if (data != null && data.name != null && !data.name.equals("Untitled Level")) {
            return data.name;
        }

        // Fall back to filename without extension
        File file = new File(path);
        String name = file.getName();
        if (name.endsWith(".json")) {
            name = name.substring(0, name.length() - 5);
        }
        // Convert underscores/hyphens to spaces and capitalize
        name = name.replace("_", " ").replace("-", " ");
        return capitalizeWords(name);
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
        String instructions = "Select a level to begin your adventure";
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
        levelPaths = null;
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
