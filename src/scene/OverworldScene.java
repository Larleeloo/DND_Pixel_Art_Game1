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
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import graphics.AssetLoader.ImageAsset;

/**
 * Overworld scene for level selection in an isometric/angled top-down view.
 * Similar to Super Mario World overworld maps, players navigate between level nodes
 * on a tile-based map and can enter levels by pressing a button on a level node.
 */
public class OverworldScene implements Scene {

    // Map dimensions (in tiles)
    private static final int MAP_WIDTH = 20;
    private static final int MAP_HEIGHT = 15;

    // Tile sizes (isometric)
    private static final int TILE_WIDTH = 64;
    private static final int TILE_HEIGHT = 48; // Including depth

    // Player movement speed
    private static final double PLAYER_SPEED = 3.0;

    // Scene state
    private boolean initialized;
    private float animationTime;

    // Player state
    private double playerX, playerY;      // World position
    private int playerTargetNode;         // Node player is moving toward
    private boolean playerMoving;
    private String playerDirection;       // "up", "down", "left", "right"

    // Textures
    private Map<String, BufferedImage> textures;
    private Map<String, AnimatedTexture> animatedTextures;

    // Level nodes
    private ArrayList<LevelNode> levelNodes;
    private int currentNodeIndex;

    // Path connections between nodes
    private ArrayList<PathConnection> pathConnections;

    // UI elements
    private ArrayList<UIButton> buttons;

    // Camera offset for scrolling large maps
    private double cameraX, cameraY;

    // Background
    private BufferedImage backgroundImage;

    /**
     * Represents a level node on the overworld map
     */
    private static class LevelNode {
        int id;
        String name;
        String levelPath;
        double x, y;           // World position
        boolean unlocked;
        boolean completed;
        boolean isBoss;
        ArrayList<Integer> connectedNodes;  // IDs of connected nodes

        LevelNode(int id, String name, String levelPath, double x, double y) {
            this.id = id;
            this.name = name;
            this.levelPath = levelPath;
            this.x = x;
            this.y = y;
            this.unlocked = false;
            this.completed = false;
            this.isBoss = false;
            this.connectedNodes = new ArrayList<>();
        }
    }

    /**
     * Represents a path connection between two nodes
     */
    private static class PathConnection {
        int nodeA, nodeB;
        ArrayList<Point> pathPoints;  // Points along the path for rendering

        PathConnection(int nodeA, int nodeB) {
            this.nodeA = nodeA;
            this.nodeB = nodeB;
            this.pathPoints = new ArrayList<>();
        }
    }

    public OverworldScene() {
        this.initialized = false;
    }

    @Override
    public void init() {
        if (initialized) return;

        System.out.println("OverworldScene: Initializing...");

        textures = new HashMap<>();
        animatedTextures = new HashMap<>();
        levelNodes = new ArrayList<>();
        pathConnections = new ArrayList<>();
        buttons = new ArrayList<>();

        animationTime = 0;
        playerMoving = false;
        playerDirection = "down";
        currentNodeIndex = 0;

        // Load textures
        loadTextures();

        // Create the overworld map layout
        createOverworldLayout();

        // Set initial player position at first node
        if (!levelNodes.isEmpty()) {
            LevelNode startNode = levelNodes.get(0);
            playerX = startNode.x;
            playerY = startNode.y;
            playerTargetNode = 0;
        }

        // Create UI buttons
        createUIButtons();

        // Initialize camera
        updateCamera();

        initialized = true;
        System.out.println("OverworldScene: Initialized with " + levelNodes.size() + " level nodes");
    }

    /**
     * Load all overworld textures
     */
    private void loadTextures() {
        String[] tileNames = {"grass", "water", "sand", "stone", "forest", "path"};
        for (String name : tileNames) {
            loadTexture("tile_" + name, "assets/overworld/tile_" + name + ".png");
        }

        // Level node textures
        loadTexture("level_available", "assets/overworld/level_available.png");
        loadTexture("level_locked", "assets/overworld/level_locked.png");
        loadTexture("level_completed", "assets/overworld/level_completed.png");
        loadTexture("level_current", "assets/overworld/level_current.png");
        loadTexture("level_boss", "assets/overworld/level_boss.png");

        // Player textures
        loadTexture("player_down", "assets/overworld/player_down.png");
        loadTexture("player_up", "assets/overworld/player_up.png");
        loadTexture("player_left", "assets/overworld/player_left.png");
        loadTexture("player_right", "assets/overworld/player_right.png");

        // Animated player textures
        loadAnimatedTexture("player_walk_down", "assets/overworld/player_walk_down.gif");
        loadAnimatedTexture("player_walk_up", "assets/overworld/player_walk_up.gif");
        loadAnimatedTexture("player_walk_left", "assets/overworld/player_walk_left.gif");
        loadAnimatedTexture("player_walk_right", "assets/overworld/player_walk_right.gif");

        // Path textures
        loadTexture("path_horizontal", "assets/overworld/path_horizontal.png");
        loadTexture("path_vertical", "assets/overworld/path_vertical.png");
        loadTexture("path_cross", "assets/overworld/path_cross.png");

        // Decorations
        loadTexture("deco_tree", "assets/overworld/deco_tree.png");
        loadTexture("deco_mountain", "assets/overworld/deco_mountain.png");
        loadTexture("deco_castle", "assets/overworld/deco_castle.png");
        loadTexture("deco_house", "assets/overworld/deco_house.png");
        loadTexture("deco_bridge", "assets/overworld/deco_bridge.png");

        // Background
        loadTexture("overworld_bg", "assets/overworld/overworld_bg.png");
        backgroundImage = textures.get("overworld_bg");
    }

    private void loadTexture(String name, String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                textures.put(name, img);
            } else {
                System.out.println("OverworldScene: Texture not found: " + path);
                // Create placeholder texture
                textures.put(name, createPlaceholderTexture(64, 48, name));
            }
        } catch (Exception e) {
            System.out.println("OverworldScene: Failed to load texture: " + path);
            textures.put(name, createPlaceholderTexture(64, 48, name));
        }
    }

    private void loadAnimatedTexture(String name, String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                ImageAsset asset = AssetLoader.load(path);
                if (asset.animatedTexture != null) {
                    animatedTextures.put(name, asset.animatedTexture);
                } else if (asset.staticImage != null) {
                    textures.put(name, asset.staticImage);
                }
            } else {
                // Use static texture as fallback
                String staticName = name.replace("_walk", "");
                textures.put(name, textures.get(staticName));
            }
        } catch (Exception e) {
            System.out.println("OverworldScene: Failed to load animated texture: " + path);
        }
    }

    private BufferedImage createPlaceholderTexture(int width, int height, String name) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(100, 100, 100, 200));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString(name, 4, height / 2);
        g.dispose();
        return img;
    }

    /**
     * Create the overworld layout with level nodes and paths
     */
    private void createOverworldLayout() {
        // Create a simple overworld with a path of levels
        // This represents "World 1" with the projectiles demo as the first level

        // Starting node (just a marker, no level)
        LevelNode start = new LevelNode(0, "Start", null, 200, 500);
        start.unlocked = true;
        start.completed = true;
        levelNodes.add(start);

        // Level 1 - Projectiles Demo (the main level to load)
        LevelNode level1 = new LevelNode(1, "Projectiles Demo", "levels/level_projectiles_demo.json", 400, 450);
        level1.unlocked = true;
        level1.completed = false;
        level1.connectedNodes.add(0);  // Connected to start
        levelNodes.add(level1);

        // Add some locked future levels for visual interest
        LevelNode level2 = new LevelNode(2, "Forest Path", null, 600, 400);
        level2.unlocked = false;
        level2.connectedNodes.add(1);
        levelNodes.add(level2);

        LevelNode level3 = new LevelNode(3, "Mountain Pass", null, 750, 350);
        level3.unlocked = false;
        level3.connectedNodes.add(2);
        levelNodes.add(level3);

        LevelNode level4 = new LevelNode(4, "Castle Siege", null, 900, 400);
        level4.unlocked = false;
        level4.isBoss = true;
        level4.connectedNodes.add(3);
        levelNodes.add(level4);

        // Connect nodes both ways
        start.connectedNodes.add(1);

        // Create path connections
        for (LevelNode node : levelNodes) {
            for (int connectedId : node.connectedNodes) {
                // Only add if we haven't already (avoid duplicates)
                boolean exists = false;
                for (PathConnection pc : pathConnections) {
                    if ((pc.nodeA == node.id && pc.nodeB == connectedId) ||
                        (pc.nodeA == connectedId && pc.nodeB == node.id)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    PathConnection path = new PathConnection(node.id, connectedId);
                    // Add intermediate points for curved paths
                    LevelNode targetNode = getLevelNodeById(connectedId);
                    if (targetNode != null) {
                        int steps = 5;
                        for (int i = 0; i <= steps; i++) {
                            double t = (double) i / steps;
                            int px = (int)(node.x + (targetNode.x - node.x) * t);
                            int py = (int)(node.y + (targetNode.y - node.y) * t);
                            path.pathPoints.add(new Point(px, py));
                        }
                    }
                    pathConnections.add(path);
                }
            }
        }
    }

    private LevelNode getLevelNodeById(int id) {
        for (LevelNode node : levelNodes) {
            if (node.id == id) return node;
        }
        return null;
    }

    /**
     * Create UI buttons
     */
    private void createUIButtons() {
        // Back button
        UIButton backButton = new UIButton(
            50, GamePanel.SCREEN_HEIGHT - 80,
            150, 50,
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

        animationTime += 0.05f;

        // Update animated textures (assuming ~60 FPS, so ~16ms per frame)
        for (AnimatedTexture tex : animatedTextures.values()) {
            tex.update(16);
        }

        // Handle player input for node navigation
        handlePlayerInput(input);

        // Update player movement
        updatePlayerMovement();

        // Update camera to follow player
        updateCamera();

        // Check if player pressed Enter/Space to enter a level
        if (input.isKeyJustPressed(' ') || input.isKeyJustPressed('\n')) {
            enterCurrentLevel();
        }
    }

    /**
     * Handle keyboard input for navigating between nodes
     */
    private void handlePlayerInput(InputManager input) {
        if (playerMoving) return;  // Can't change direction while moving

        LevelNode currentNode = levelNodes.get(currentNodeIndex);

        // Find connected nodes in each direction
        LevelNode upNode = null, downNode = null, leftNode = null, rightNode = null;

        for (int connectedId : currentNode.connectedNodes) {
            LevelNode connected = getLevelNodeById(connectedId);
            if (connected == null || !connected.unlocked) continue;

            double dx = connected.x - currentNode.x;
            double dy = connected.y - currentNode.y;

            // Determine primary direction
            if (Math.abs(dx) > Math.abs(dy)) {
                if (dx > 0 && rightNode == null) rightNode = connected;
                else if (dx < 0 && leftNode == null) leftNode = connected;
            } else {
                if (dy > 0 && downNode == null) downNode = connected;
                else if (dy < 0 && upNode == null) upNode = connected;
            }
        }

        // Handle directional input
        if ((input.isKeyPressed('w') || input.isKeyPressed('W')) && upNode != null) {
            startMovingToNode(upNode.id);
            playerDirection = "up";
        } else if ((input.isKeyPressed('s') || input.isKeyPressed('S')) && downNode != null) {
            startMovingToNode(downNode.id);
            playerDirection = "down";
        } else if ((input.isKeyPressed('a') || input.isKeyPressed('A')) && leftNode != null) {
            startMovingToNode(leftNode.id);
            playerDirection = "left";
        } else if ((input.isKeyPressed('d') || input.isKeyPressed('D')) && rightNode != null) {
            startMovingToNode(rightNode.id);
            playerDirection = "right";
        }
    }

    /**
     * Start moving player toward a target node
     */
    private void startMovingToNode(int nodeId) {
        playerTargetNode = nodeId;
        playerMoving = true;
    }

    /**
     * Update player position as they move between nodes
     */
    private void updatePlayerMovement() {
        if (!playerMoving) return;

        LevelNode targetNode = getLevelNodeById(playerTargetNode);
        if (targetNode == null) {
            playerMoving = false;
            return;
        }

        double dx = targetNode.x - playerX;
        double dy = targetNode.y - playerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < PLAYER_SPEED) {
            // Arrived at node
            playerX = targetNode.x;
            playerY = targetNode.y;
            currentNodeIndex = playerTargetNode;
            playerMoving = false;
        } else {
            // Move toward node
            double moveX = (dx / distance) * PLAYER_SPEED;
            double moveY = (dy / distance) * PLAYER_SPEED;
            playerX += moveX;
            playerY += moveY;

            // Update direction based on movement
            if (Math.abs(moveX) > Math.abs(moveY)) {
                playerDirection = moveX > 0 ? "right" : "left";
            } else {
                playerDirection = moveY > 0 ? "down" : "up";
            }
        }
    }

    /**
     * Update camera position to follow player
     */
    private void updateCamera() {
        double targetCameraX = playerX - GamePanel.SCREEN_WIDTH / 2.0;
        double targetCameraY = playerY - GamePanel.SCREEN_HEIGHT / 2.0;

        // Smooth camera following
        cameraX += (targetCameraX - cameraX) * 0.1;
        cameraY += (targetCameraY - cameraY) * 0.1;

        // Clamp camera to map bounds
        int mapPixelWidth = MAP_WIDTH * TILE_WIDTH;
        int mapPixelHeight = MAP_HEIGHT * TILE_HEIGHT;

        cameraX = Math.max(0, Math.min(cameraX, mapPixelWidth - GamePanel.SCREEN_WIDTH));
        cameraY = Math.max(0, Math.min(cameraY, mapPixelHeight - GamePanel.SCREEN_HEIGHT));

        // If map is smaller than screen, center it
        if (mapPixelWidth < GamePanel.SCREEN_WIDTH) {
            cameraX = (mapPixelWidth - GamePanel.SCREEN_WIDTH) / 2.0;
        }
        if (mapPixelHeight < GamePanel.SCREEN_HEIGHT) {
            cameraY = (mapPixelHeight - GamePanel.SCREEN_HEIGHT) / 2.0;
        }
    }

    /**
     * Enter the level at the current node
     */
    private void enterCurrentLevel() {
        LevelNode currentNode = levelNodes.get(currentNodeIndex);
        if (currentNode.levelPath != null && !currentNode.levelPath.isEmpty()) {
            System.out.println("OverworldScene: Entering level - " + currentNode.name);
            SceneManager.getInstance().loadLevel(currentNode.levelPath, SceneManager.TRANSITION_FADE);
        } else {
            System.out.println("OverworldScene: No level at this node");
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!initialized) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        drawBackground(g2d);

        // Apply camera transform
        g2d.translate(-cameraX, -cameraY);

        // Draw ground tiles
        drawGroundTiles(g2d);

        // Draw path connections
        drawPaths(g2d);

        // Draw decorations
        drawDecorations(g2d);

        // Draw level nodes
        drawLevelNodes(g2d);

        // Draw player
        drawPlayer(g2d);

        // Reset transform for UI
        g2d.translate(cameraX, cameraY);

        // Draw UI elements
        drawUI(g2d);
    }

    /**
     * Draw tiled background
     */
    private void drawBackground(Graphics2D g) {
        if (backgroundImage != null) {
            // Tile the background
            int bgWidth = backgroundImage.getWidth();
            int bgHeight = backgroundImage.getHeight();
            for (int x = 0; x < GamePanel.SCREEN_WIDTH; x += bgWidth) {
                for (int y = 0; y < GamePanel.SCREEN_HEIGHT; y += bgHeight) {
                    g.drawImage(backgroundImage, x, y, null);
                }
            }
        } else {
            // Fallback gradient
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(135, 206, 235),
                0, GamePanel.SCREEN_HEIGHT, new Color(200, 230, 255)
            );
            g.setPaint(gradient);
            g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);
        }
    }

    /**
     * Draw isometric ground tiles
     */
    private void drawGroundTiles(Graphics2D g) {
        // Create a simple ground pattern
        BufferedImage grassTile = textures.get("tile_grass");
        BufferedImage waterTile = textures.get("tile_water");
        BufferedImage forestTile = textures.get("tile_forest");

        if (grassTile == null) return;

        // Draw tiles in a grid pattern covering the play area
        int startX = -100;
        int startY = 200;
        int endX = 1200;
        int endY = 700;

        for (int x = startX; x < endX; x += TILE_WIDTH / 2) {
            for (int y = startY; y < endY; y += TILE_HEIGHT - 16) {
                // Stagger rows for isometric effect
                int offsetX = ((y / (TILE_HEIGHT - 16)) % 2 == 0) ? 0 : TILE_WIDTH / 4;

                // Determine tile type based on position (simple pattern)
                BufferedImage tile = grassTile;
                int tileX = x / TILE_WIDTH;
                int tileY = y / TILE_HEIGHT;

                // Add some variety
                if ((tileX + tileY) % 7 == 0) {
                    tile = forestTile != null ? forestTile : grassTile;
                }
                if (tileY < 3 || tileY > 10) {
                    // Water at edges
                    tile = waterTile != null ? waterTile : grassTile;
                }

                g.drawImage(tile, x + offsetX, y, null);
            }
        }
    }

    /**
     * Draw paths between level nodes
     */
    private void drawPaths(Graphics2D g) {
        g.setColor(new Color(180, 150, 100));
        g.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (PathConnection path : pathConnections) {
            LevelNode nodeA = getLevelNodeById(path.nodeA);
            LevelNode nodeB = getLevelNodeById(path.nodeB);
            if (nodeA == null || nodeB == null) continue;

            // Draw path line
            g.drawLine((int) nodeA.x, (int) nodeA.y, (int) nodeB.x, (int) nodeB.y);
        }

        // Draw dotted center line
        g.setColor(new Color(255, 255, 200, 150));
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                    10, new float[]{6, 6}, 0));

        for (PathConnection path : pathConnections) {
            LevelNode nodeA = getLevelNodeById(path.nodeA);
            LevelNode nodeB = getLevelNodeById(path.nodeB);
            if (nodeA == null || nodeB == null) continue;

            g.drawLine((int) nodeA.x, (int) nodeA.y, (int) nodeB.x, (int) nodeB.y);
        }
    }

    /**
     * Draw decorative elements
     */
    private void drawDecorations(Graphics2D g) {
        BufferedImage tree = textures.get("deco_tree");
        BufferedImage mountain = textures.get("deco_mountain");
        BufferedImage castle = textures.get("deco_castle");

        // Draw trees scattered around
        if (tree != null) {
            int[] treeX = {100, 300, 550, 700, 850, 1000};
            int[] treeY = {350, 300, 280, 320, 250, 300};
            for (int i = 0; i < treeX.length; i++) {
                g.drawImage(tree, treeX[i], treeY[i], null);
            }
        }

        // Draw mountains in background
        if (mountain != null) {
            g.drawImage(mountain, 50, 220, null);
            g.drawImage(mountain, 500, 200, null);
            g.drawImage(mountain, 800, 180, null);
        }

        // Draw castle at boss level
        if (castle != null) {
            LevelNode bossNode = null;
            for (LevelNode node : levelNodes) {
                if (node.isBoss) {
                    bossNode = node;
                    break;
                }
            }
            if (bossNode != null) {
                g.drawImage(castle, (int) bossNode.x - 24, (int) bossNode.y - 70, null);
            }
        }
    }

    /**
     * Draw level node markers
     */
    private void drawLevelNodes(Graphics2D g) {
        for (int i = 0; i < levelNodes.size(); i++) {
            LevelNode node = levelNodes.get(i);

            // Determine which texture to use
            String textureName;
            if (i == currentNodeIndex) {
                textureName = "level_current";
            } else if (node.isBoss) {
                textureName = "level_boss";
            } else if (node.completed) {
                textureName = "level_completed";
            } else if (node.unlocked) {
                textureName = "level_available";
            } else {
                textureName = "level_locked";
            }

            BufferedImage nodeTexture = textures.get(textureName);
            if (nodeTexture != null) {
                // Pulsing effect for current node
                double scale = 1.0;
                if (i == currentNodeIndex) {
                    scale = 1.0 + Math.sin(animationTime * 3) * 0.1;
                }

                int drawWidth = (int) (nodeTexture.getWidth() * scale);
                int drawHeight = (int) (nodeTexture.getHeight() * scale);
                int drawX = (int) node.x - drawWidth / 2;
                int drawY = (int) node.y - drawHeight / 2;

                g.drawImage(nodeTexture, drawX, drawY, drawWidth, drawHeight, null);
            } else {
                // Fallback circle
                g.setColor(node.unlocked ? new Color(255, 200, 50) : new Color(100, 100, 100));
                g.fillOval((int) node.x - 20, (int) node.y - 20, 40, 40);
            }

            // Draw level name below node
            if (node.unlocked && node.levelPath != null) {
                g.setFont(new Font("SansSerif", Font.BOLD, 14));
                g.setColor(new Color(0, 0, 0, 150));
                FontMetrics fm = g.getFontMetrics();
                int textX = (int) node.x - fm.stringWidth(node.name) / 2;
                g.drawString(node.name, textX + 1, (int) node.y + 36);
                g.setColor(Color.WHITE);
                g.drawString(node.name, textX, (int) node.y + 35);
            }
        }
    }

    /**
     * Draw the player character
     */
    private void drawPlayer(Graphics2D g) {
        // Determine which texture to use
        String textureName;
        if (playerMoving) {
            textureName = "player_walk_" + playerDirection;
        } else {
            textureName = "player_" + playerDirection;
        }

        BufferedImage playerImage = null;

        // Check animated textures first
        if (playerMoving && animatedTextures.containsKey(textureName)) {
            AnimatedTexture animTex = animatedTextures.get(textureName);
            playerImage = animTex.getCurrentFrame();
        }

        // Fall back to static texture
        if (playerImage == null) {
            playerImage = textures.get(textureName);
        }
        if (playerImage == null) {
            playerImage = textures.get("player_down");
        }

        if (playerImage != null) {
            // Draw shadow
            g.setColor(new Color(0, 0, 0, 60));
            g.fillOval((int) playerX - 12, (int) playerY + 8, 24, 10);

            // Draw player (scaled up 2x)
            int drawWidth = playerImage.getWidth() * 2;
            int drawHeight = playerImage.getHeight() * 2;
            g.drawImage(playerImage,
                        (int) playerX - drawWidth / 2,
                        (int) playerY - drawHeight / 2 - 16, // Offset up so feet are at position
                        drawWidth, drawHeight, null);
        } else {
            // Fallback: draw simple circle
            g.setColor(new Color(200, 100, 100));
            g.fillOval((int) playerX - 15, (int) playerY - 15, 30, 30);
        }
    }

    /**
     * Draw UI elements
     */
    private void drawUI(Graphics2D g) {
        // Draw title
        g.setFont(new Font("Serif", Font.BOLD, 42));
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString("World 1 - The Beginning", 53, 53);
        g.setColor(new Color(255, 220, 150));
        g.drawString("World 1 - The Beginning", 50, 50);

        // Draw current level info box
        LevelNode currentNode = levelNodes.get(currentNodeIndex);
        if (currentNode.levelPath != null) {
            // Info box background
            int boxX = GamePanel.SCREEN_WIDTH - 350;
            int boxY = 20;
            int boxWidth = 330;
            int boxHeight = 100;

            g.setColor(new Color(0, 0, 0, 180));
            g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);
            g.setColor(new Color(255, 255, 255, 100));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);

            // Level name
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            g.setColor(new Color(255, 220, 150));
            g.drawString(currentNode.name, boxX + 15, boxY + 30);

            // Instructions
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.setColor(new Color(200, 200, 220));
            g.drawString("Press SPACE or ENTER to enter level", boxX + 15, boxY + 55);
            g.drawString("Use WASD to navigate", boxX + 15, boxY + 75);
        }

        // Draw navigation hints at bottom
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(new Color(255, 255, 255, 200));
        String hint = "WASD: Move  |  SPACE: Enter Level  |  ESC: Back to Menu";
        FontMetrics fm = g.getFontMetrics();
        int hintX = (GamePanel.SCREEN_WIDTH - fm.stringWidth(hint)) / 2;
        g.drawString(hint, hintX, GamePanel.SCREEN_HEIGHT - 30);

        // Draw buttons
        for (UIButton button : buttons) {
            button.draw(g);
        }
    }

    @Override
    public void dispose() {
        System.out.println("OverworldScene: Disposing...");
        initialized = false;
        textures.clear();
        animatedTextures.clear();
        levelNodes.clear();
        pathConnections.clear();
        buttons.clear();
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

        for (UIButton button : buttons) {
            button.handleMouseMove(x, y);
        }
    }

    @Override
    public void onMouseClicked(int x, int y) {
        if (!initialized) return;

        // Check button clicks
        for (UIButton button : buttons) {
            button.handleClick(x, y);
        }

        // Check if clicking on a level node (with camera offset)
        double worldX = x + cameraX;
        double worldY = y + cameraY;

        for (int i = 0; i < levelNodes.size(); i++) {
            LevelNode node = levelNodes.get(i);
            if (!node.unlocked) continue;

            double dx = worldX - node.x;
            double dy = worldY - node.y;
            if (dx * dx + dy * dy < 30 * 30) {
                // Clicked on this node
                if (i == currentNodeIndex && node.levelPath != null) {
                    // Already at this node, enter level
                    enterCurrentLevel();
                } else if (isNodeReachable(i)) {
                    // Move to this node
                    startMovingToNode(i);
                }
                break;
            }
        }
    }

    /**
     * Check if a node is directly reachable from current position
     */
    private boolean isNodeReachable(int nodeId) {
        LevelNode currentNode = levelNodes.get(currentNodeIndex);
        return currentNode.connectedNodes.contains(nodeId);
    }

    @Override
    public String getName() {
        return "Overworld";
    }
}
