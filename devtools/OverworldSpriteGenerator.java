import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;

/**
 * Generates isometric/angled top-down sprites for the overworld map system.
 * Creates tiles, level nodes, paths, and player icons in an isometric style
 * similar to Super Mario World overworld maps.
 */
public class OverworldSpriteGenerator {

    // Base tile size (isometric diamond shape)
    private static final int TILE_WIDTH = 64;
    private static final int TILE_HEIGHT = 32;

    // Level node size
    private static final int NODE_SIZE = 48;

    // Player icon size
    private static final int PLAYER_SIZE = 32;

    public static void main(String[] args) {
        System.out.println("=== Overworld Sprite Generator ===");

        // Create output directory
        File outputDir = new File("assets/overworld");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
            System.out.println("Created directory: assets/overworld");
        }

        // Generate all overworld assets
        generateIsometricTiles(outputDir);
        generateLevelNodes(outputDir);
        generatePlayerIcon(outputDir);
        generatePathTiles(outputDir);
        generateDecorations(outputDir);
        generateBackground(outputDir);

        System.out.println("\n=== Generation Complete ===");
        System.out.println("Assets saved to: assets/overworld/");
    }

    /**
     * Generate isometric ground tiles (grass, water, sand, mountain, etc.)
     */
    private static void generateIsometricTiles(File outputDir) {
        System.out.println("\nGenerating isometric tiles...");

        // Grass tile
        generateIsometricTile(outputDir, "grass",
            new Color(76, 153, 0),      // Top face (bright green)
            new Color(51, 102, 0),      // Left face (medium green)
            new Color(34, 68, 0));      // Right face (dark green)

        // Water tile
        generateIsometricTile(outputDir, "water",
            new Color(51, 153, 255),    // Top face (light blue)
            new Color(30, 100, 200),    // Left face
            new Color(20, 80, 180));    // Right face

        // Sand tile
        generateIsometricTile(outputDir, "sand",
            new Color(230, 200, 140),   // Top face
            new Color(200, 170, 110),   // Left face
            new Color(180, 150, 90));   // Right face

        // Stone/mountain tile
        generateIsometricTile(outputDir, "stone",
            new Color(140, 140, 150),   // Top face
            new Color(100, 100, 110),   // Left face
            new Color(80, 80, 90));     // Right face

        // Forest tile (dark grass)
        generateIsometricTile(outputDir, "forest",
            new Color(40, 100, 20),     // Top face
            new Color(30, 80, 15),      // Left face
            new Color(20, 60, 10));     // Right face

        // Path/road tile
        generateIsometricTile(outputDir, "path",
            new Color(180, 150, 100),   // Top face (tan/dirt color)
            new Color(150, 120, 80),    // Left face
            new Color(120, 90, 60));    // Right face
    }

    /**
     * Generate a single isometric tile with 3D depth effect
     */
    private static void generateIsometricTile(File outputDir, String name,
            Color topColor, Color leftColor, Color rightColor) {

        int width = TILE_WIDTH;
        int height = TILE_HEIGHT + 16; // Extra height for 3D depth

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int midX = width / 2;
        int topY = 0;
        int midY = TILE_HEIGHT / 2;
        int bottomY = TILE_HEIGHT;
        int depthY = height;

        // Top face (diamond shape)
        int[] topXPoints = {midX, width - 1, midX, 0};
        int[] topYPoints = {topY, midY, bottomY, midY};
        g.setColor(topColor);
        g.fillPolygon(topXPoints, topYPoints, 4);

        // Left face (3D depth)
        int[] leftXPoints = {0, midX, midX, 0};
        int[] leftYPoints = {midY, bottomY, depthY, midY + 16};
        g.setColor(leftColor);
        g.fillPolygon(leftXPoints, leftYPoints, 4);

        // Right face (3D depth)
        int[] rightXPoints = {midX, width - 1, width - 1, midX};
        int[] rightYPoints = {bottomY, midY, midY + 16, depthY};
        g.setColor(rightColor);
        g.fillPolygon(rightXPoints, rightYPoints, 4);

        // Add some texture/noise to top face
        addNoiseToRegion(g, topXPoints, topYPoints, topColor, 0.1f);

        // Draw outline
        g.setColor(new Color(0, 0, 0, 80));
        g.setStroke(new BasicStroke(1));
        g.drawPolygon(topXPoints, topYPoints, 4);
        g.drawPolygon(leftXPoints, leftYPoints, 4);
        g.drawPolygon(rightXPoints, rightYPoints, 4);

        g.dispose();

        saveImage(img, new File(outputDir, "tile_" + name + ".png"));
        System.out.println("  - Generated tile_" + name + ".png");
    }

    /**
     * Add subtle noise texture to a region
     */
    private static void addNoiseToRegion(Graphics2D g, int[] xPoints, int[] yPoints, Color baseColor, float intensity) {
        Polygon poly = new Polygon(xPoints, yPoints, xPoints.length);
        Rectangle bounds = poly.getBounds();

        for (int x = bounds.x; x < bounds.x + bounds.width; x += 2) {
            for (int y = bounds.y; y < bounds.y + bounds.height; y += 2) {
                if (poly.contains(x, y)) {
                    float noise = (float)(Math.random() - 0.5) * intensity;
                    int r = clamp((int)(baseColor.getRed() * (1 + noise)), 0, 255);
                    int gr = clamp((int)(baseColor.getGreen() * (1 + noise)), 0, 255);
                    int b = clamp((int)(baseColor.getBlue() * (1 + noise)), 0, 255);
                    g.setColor(new Color(r, gr, b));
                    g.fillRect(x, y, 2, 2);
                }
            }
        }
    }

    /**
     * Generate level node markers (like the level icons in Mario overworld)
     */
    private static void generateLevelNodes(File outputDir) {
        System.out.println("\nGenerating level nodes...");

        // Available level node (can enter)
        generateLevelNode(outputDir, "level_available",
            new Color(255, 200, 50),    // Gold/yellow
            new Color(200, 150, 30),    // Darker gold
            true);

        // Locked level node
        generateLevelNode(outputDir, "level_locked",
            new Color(100, 100, 100),   // Gray
            new Color(60, 60, 60),      // Dark gray
            false);

        // Completed level node
        generateLevelNode(outputDir, "level_completed",
            new Color(100, 255, 100),   // Green
            new Color(50, 200, 50),     // Dark green
            true);

        // Current level node (pulsing - will be animated in code)
        generateLevelNode(outputDir, "level_current",
            new Color(255, 100, 100),   // Red/orange
            new Color(200, 60, 60),     // Dark red
            true);

        // Boss level node
        generateLevelNode(outputDir, "level_boss",
            new Color(200, 50, 255),    // Purple
            new Color(150, 30, 200),    // Dark purple
            true);
    }

    /**
     * Generate a single level node icon
     */
    private static void generateLevelNode(File outputDir, String name,
            Color mainColor, Color shadowColor, boolean showStar) {

        BufferedImage img = new BufferedImage(NODE_SIZE, NODE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = NODE_SIZE / 2;
        int centerY = NODE_SIZE / 2;
        int radius = NODE_SIZE / 2 - 4;

        // Draw shadow
        g.setColor(new Color(0, 0, 0, 100));
        g.fillOval(centerX - radius + 2, centerY - radius + 4, radius * 2, radius * 2);

        // Draw main circle with gradient
        GradientPaint gradient = new GradientPaint(
            centerX - radius, centerY - radius, mainColor,
            centerX + radius, centerY + radius, shadowColor
        );
        g.setPaint(gradient);
        g.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Draw highlight
        g.setColor(new Color(255, 255, 255, 100));
        g.fillOval(centerX - radius + 4, centerY - radius + 4, radius / 2, radius / 2);

        // Draw border
        g.setColor(new Color(0, 0, 0, 150));
        g.setStroke(new BasicStroke(2));
        g.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Draw star or lock icon inside
        if (showStar) {
            drawStar(g, centerX, centerY, 8, 4, Color.WHITE);
        } else {
            // Draw lock icon
            g.setColor(new Color(40, 40, 40));
            g.fillRoundRect(centerX - 6, centerY - 2, 12, 10, 3, 3);
            g.drawArc(centerX - 4, centerY - 8, 8, 10, 0, 180);
        }

        g.dispose();

        saveImage(img, new File(outputDir, name + ".png"));
        System.out.println("  - Generated " + name + ".png");
    }

    /**
     * Draw a star shape
     */
    private static void drawStar(Graphics2D g, int centerX, int centerY,
            int outerRadius, int innerRadius, Color color) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];

        for (int i = 0; i < 10; i++) {
            double angle = Math.PI * i / 5 - Math.PI / 2;
            int radius = (i % 2 == 0) ? outerRadius : innerRadius;
            xPoints[i] = centerX + (int)(Math.cos(angle) * radius);
            yPoints[i] = centerY + (int)(Math.sin(angle) * radius);
        }

        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, 10);
        g.setColor(new Color(0, 0, 0, 80));
        g.drawPolygon(xPoints, yPoints, 10);
    }

    /**
     * Generate player overworld icon (animated GIF with 4 directions)
     */
    private static void generatePlayerIcon(File outputDir) {
        System.out.println("\nGenerating player icon...");

        // Create a simple animated player icon
        String[] directions = {"down", "up", "left", "right"};

        for (String dir : directions) {
            BufferedImage img = new BufferedImage(PLAYER_SIZE, PLAYER_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int centerX = PLAYER_SIZE / 2;
            int centerY = PLAYER_SIZE / 2;

            // Body (circle)
            g.setColor(new Color(200, 150, 100)); // Skin tone
            g.fillOval(centerX - 10, centerY - 8, 20, 20);

            // Hair
            g.setColor(new Color(80, 50, 30)); // Brown hair
            g.fillArc(centerX - 10, centerY - 10, 20, 16, 0, 180);

            // Eyes based on direction
            g.setColor(Color.BLACK);
            switch (dir) {
                case "down":
                    g.fillOval(centerX - 5, centerY, 3, 3);
                    g.fillOval(centerX + 2, centerY, 3, 3);
                    break;
                case "up":
                    // Eyes not visible from behind
                    break;
                case "left":
                    g.fillOval(centerX - 6, centerY, 3, 3);
                    break;
                case "right":
                    g.fillOval(centerX + 3, centerY, 3, 3);
                    break;
            }

            // Cape/body below
            g.setColor(new Color(150, 50, 50)); // Red cape
            g.fillArc(centerX - 8, centerY + 6, 16, 12, 180, 180);

            // Shadow
            g.setColor(new Color(0, 0, 0, 60));
            g.fillOval(centerX - 8, centerY + 14, 16, 6);

            g.dispose();

            saveImage(img, new File(outputDir, "player_" + dir + ".png"));
            System.out.println("  - Generated player_" + dir + ".png");
        }

        // Generate walking animation frames
        generatePlayerWalkAnimation(outputDir);
    }

    /**
     * Generate walking animation GIF for player
     */
    private static void generatePlayerWalkAnimation(File outputDir) {
        String[] directions = {"down", "up", "left", "right"};

        for (String dir : directions) {
            BufferedImage[] frames = new BufferedImage[4];

            for (int frame = 0; frame < 4; frame++) {
                frames[frame] = new BufferedImage(PLAYER_SIZE, PLAYER_SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = frames[frame].createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int centerX = PLAYER_SIZE / 2;
                int centerY = PLAYER_SIZE / 2;

                // Bob animation
                int bobOffset = (frame == 1 || frame == 3) ? -2 : 0;

                // Body (circle)
                g.setColor(new Color(200, 150, 100));
                g.fillOval(centerX - 10, centerY - 8 + bobOffset, 20, 20);

                // Hair
                g.setColor(new Color(80, 50, 30));
                g.fillArc(centerX - 10, centerY - 10 + bobOffset, 20, 16, 0, 180);

                // Eyes based on direction
                g.setColor(Color.BLACK);
                switch (dir) {
                    case "down":
                        g.fillOval(centerX - 5, centerY + bobOffset, 3, 3);
                        g.fillOval(centerX + 2, centerY + bobOffset, 3, 3);
                        break;
                    case "up":
                        break;
                    case "left":
                        g.fillOval(centerX - 6, centerY + bobOffset, 3, 3);
                        break;
                    case "right":
                        g.fillOval(centerX + 3, centerY + bobOffset, 3, 3);
                        break;
                }

                // Cape
                g.setColor(new Color(150, 50, 50));
                g.fillArc(centerX - 8, centerY + 6 + bobOffset, 16, 12, 180, 180);

                // Legs animation
                g.setColor(new Color(80, 60, 40));
                int legOffset = (frame < 2) ? 3 : -3;
                g.fillOval(centerX - 6 + legOffset, centerY + 12, 4, 6);
                g.fillOval(centerX + 2 - legOffset, centerY + 12, 4, 6);

                // Shadow
                g.setColor(new Color(0, 0, 0, 60));
                g.fillOval(centerX - 8, centerY + 16, 16, 6);

                g.dispose();
            }

            // Save as GIF
            saveAnimatedGif(frames, new File(outputDir, "player_walk_" + dir + ".gif"), 150);
            System.out.println("  - Generated player_walk_" + dir + ".gif");
        }
    }

    /**
     * Generate path/road tiles connecting level nodes
     */
    private static void generatePathTiles(File outputDir) {
        System.out.println("\nGenerating path tiles...");

        // Horizontal path
        generatePathSegment(outputDir, "path_horizontal", true, false);

        // Vertical path
        generatePathSegment(outputDir, "path_vertical", false, true);

        // Intersection
        generatePathSegment(outputDir, "path_cross", true, true);

        // Corner pieces
        generatePathCorner(outputDir, "path_corner_ne", 0);
        generatePathCorner(outputDir, "path_corner_se", 90);
        generatePathCorner(outputDir, "path_corner_sw", 180);
        generatePathCorner(outputDir, "path_corner_nw", 270);
    }

    /**
     * Generate a path segment
     */
    private static void generatePathSegment(File outputDir, String name,
            boolean horizontal, boolean vertical) {

        int size = 32;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int pathWidth = 12;
        int center = size / 2;

        g.setColor(new Color(180, 150, 100));

        if (horizontal) {
            g.fillRect(0, center - pathWidth / 2, size, pathWidth);
        }
        if (vertical) {
            g.fillRect(center - pathWidth / 2, 0, pathWidth, size);
        }

        // Add dotted center line
        g.setColor(new Color(255, 255, 200, 150));
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                    10, new float[]{4, 4}, 0));
        if (horizontal) {
            g.drawLine(0, center, size, center);
        }
        if (vertical) {
            g.drawLine(center, 0, center, size);
        }

        g.dispose();

        saveImage(img, new File(outputDir, name + ".png"));
        System.out.println("  - Generated " + name + ".png");
    }

    /**
     * Generate a path corner piece
     */
    private static void generatePathCorner(File outputDir, String name, int rotation) {
        int size = 32;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Rotate around center
        g.rotate(Math.toRadians(rotation), size / 2.0, size / 2.0);

        int pathWidth = 12;
        int center = size / 2;

        g.setColor(new Color(180, 150, 100));

        // Draw L-shaped corner (default is NE corner)
        g.fillRect(center - pathWidth / 2, 0, pathWidth, center + pathWidth / 2);
        g.fillRect(center - pathWidth / 2, center - pathWidth / 2, center + pathWidth / 2, pathWidth);

        g.dispose();

        saveImage(img, new File(outputDir, name + ".png"));
        System.out.println("  - Generated " + name + ".png");
    }

    /**
     * Generate decorative elements (trees, mountains, castles, etc.)
     */
    private static void generateDecorations(File outputDir) {
        System.out.println("\nGenerating decorations...");

        // Tree
        generateTree(outputDir);

        // Mountain
        generateMountain(outputDir);

        // Castle/fortress
        generateCastle(outputDir);

        // House
        generateHouse(outputDir);

        // Bridge
        generateBridge(outputDir);
    }

    private static void generateTree(File outputDir) {
        int width = 32;
        int height = 48;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Trunk
        g.setColor(new Color(100, 70, 40));
        g.fillRect(width / 2 - 3, height - 20, 6, 20);

        // Foliage (layered circles)
        g.setColor(new Color(30, 100, 30));
        g.fillOval(2, 8, 28, 28);
        g.setColor(new Color(50, 130, 50));
        g.fillOval(6, 4, 20, 24);
        g.setColor(new Color(70, 150, 70));
        g.fillOval(10, 0, 12, 16);

        g.dispose();
        saveImage(img, new File(outputDir, "deco_tree.png"));
        System.out.println("  - Generated deco_tree.png");
    }

    private static void generateMountain(File outputDir) {
        int width = 64;
        int height = 48;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Mountain shape
        int[] xPoints = {0, width / 2, width, width, 0};
        int[] yPoints = {height, 4, height, height, height};

        g.setColor(new Color(100, 100, 110));
        g.fillPolygon(xPoints, yPoints, 5);

        // Snow cap
        int[] snowX = {width / 2 - 8, width / 2, width / 2 + 8};
        int[] snowY = {16, 4, 16};
        g.setColor(Color.WHITE);
        g.fillPolygon(snowX, snowY, 3);

        // Shading
        g.setColor(new Color(80, 80, 90));
        int[] shadeX = {width / 2, width, width};
        int[] shadeY = {4, height, height};
        g.fillPolygon(shadeX, shadeY, 3);

        g.dispose();
        saveImage(img, new File(outputDir, "deco_mountain.png"));
        System.out.println("  - Generated deco_mountain.png");
    }

    private static void generateCastle(File outputDir) {
        int width = 48;
        int height = 56;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Main building
        g.setColor(new Color(140, 140, 150));
        g.fillRect(8, 24, 32, 32);

        // Towers
        g.setColor(new Color(120, 120, 130));
        g.fillRect(4, 16, 12, 40);
        g.fillRect(32, 16, 12, 40);

        // Tower tops (crenellations)
        g.setColor(new Color(100, 100, 110));
        for (int i = 0; i < 3; i++) {
            g.fillRect(4 + i * 4, 12, 3, 6);
            g.fillRect(32 + i * 4, 12, 3, 6);
        }

        // Center tower
        g.setColor(new Color(130, 130, 140));
        g.fillRect(18, 4, 12, 28);

        // Flag
        g.setColor(new Color(200, 50, 50));
        g.fillRect(22, 0, 8, 6);
        g.setColor(new Color(80, 50, 30));
        g.fillRect(21, 0, 2, 12);

        // Door
        g.setColor(new Color(80, 50, 30));
        g.fillRect(20, 44, 8, 12);

        // Windows
        g.setColor(new Color(200, 200, 100));
        g.fillRect(12, 32, 4, 6);
        g.fillRect(32, 32, 4, 6);
        g.fillRect(22, 16, 4, 6);

        g.dispose();
        saveImage(img, new File(outputDir, "deco_castle.png"));
        System.out.println("  - Generated deco_castle.png");
    }

    private static void generateHouse(File outputDir) {
        int width = 32;
        int height = 32;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Walls
        g.setColor(new Color(180, 160, 140));
        g.fillRect(4, 14, 24, 18);

        // Roof
        g.setColor(new Color(150, 80, 60));
        int[] roofX = {2, 16, 30};
        int[] roofY = {14, 2, 14};
        g.fillPolygon(roofX, roofY, 3);

        // Door
        g.setColor(new Color(100, 70, 50));
        g.fillRect(12, 22, 8, 10);

        // Window
        g.setColor(new Color(200, 220, 255));
        g.fillRect(22, 18, 4, 4);

        // Chimney
        g.setColor(new Color(120, 80, 60));
        g.fillRect(22, 2, 4, 8);

        g.dispose();
        saveImage(img, new File(outputDir, "deco_house.png"));
        System.out.println("  - Generated deco_house.png");
    }

    private static void generateBridge(File outputDir) {
        int width = 48;
        int height = 24;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Bridge deck
        g.setColor(new Color(120, 90, 60));
        g.fillRect(0, 8, 48, 10);

        // Railings
        g.setColor(new Color(100, 70, 40));
        g.fillRect(0, 4, 48, 4);

        // Support posts
        g.setColor(new Color(80, 50, 30));
        g.fillRect(4, 4, 3, 16);
        g.fillRect(20, 4, 3, 16);
        g.fillRect(41, 4, 3, 16);

        // Planks (horizontal lines)
        g.setColor(new Color(100, 70, 50));
        for (int i = 0; i < 8; i++) {
            g.drawLine(i * 6, 10, i * 6 + 4, 10);
        }

        g.dispose();
        saveImage(img, new File(outputDir, "deco_bridge.png"));
        System.out.println("  - Generated deco_bridge.png");
    }

    /**
     * Generate a nice overworld background
     */
    private static void generateBackground(File outputDir) {
        System.out.println("\nGenerating background...");

        int width = 256;
        int height = 256;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Sky gradient
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(135, 206, 235),      // Light sky blue
            0, height, new Color(200, 230, 255)   // Lighter at bottom
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, width, height);

        // Add some clouds
        g.setColor(new Color(255, 255, 255, 180));
        for (int i = 0; i < 5; i++) {
            int cloudX = (int)(Math.random() * width);
            int cloudY = (int)(Math.random() * height / 2);
            drawCloud(g, cloudX, cloudY);
        }

        g.dispose();
        saveImage(img, new File(outputDir, "overworld_bg.png"));
        System.out.println("  - Generated overworld_bg.png");
    }

    private static void drawCloud(Graphics2D g, int x, int y) {
        g.fillOval(x, y, 40, 20);
        g.fillOval(x + 15, y - 8, 30, 20);
        g.fillOval(x + 30, y, 35, 18);
    }

    // ===== UTILITY METHODS =====

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void saveImage(BufferedImage img, File file) {
        try {
            ImageIO.write(img, "PNG", file);
        } catch (Exception e) {
            System.err.println("Failed to save " + file.getName() + ": " + e.getMessage());
        }
    }

    private static void saveAnimatedGif(BufferedImage[] frames, File file, int delayMs) {
        try {
            ImageOutputStream output = new FileImageOutputStream(file);
            GifSequenceWriter writer = new GifSequenceWriter(output, frames[0].getType(), delayMs, true);

            for (BufferedImage frame : frames) {
                writer.writeToSequence(frame);
            }

            writer.close();
            output.close();
        } catch (Exception e) {
            System.err.println("Failed to save GIF " + file.getName() + ": " + e.getMessage());
            // Fall back to saving first frame as PNG
            String pngName = file.getName().replace(".gif", ".png");
            saveImage(frames[0], new File(file.getParent(), pngName));
        }
    }

    /**
     * Simple GIF writer helper class
     */
    static class GifSequenceWriter {
        protected ImageWriter gifWriter;
        protected ImageWriteParam imageWriteParam;
        protected IIOMetadata imageMetaData;

        public GifSequenceWriter(ImageOutputStream outputStream, int imageType, int timeBetweenFramesMS, boolean loopContinuously) throws Exception {
            gifWriter = ImageIO.getImageWritersBySuffix("gif").next();
            imageWriteParam = gifWriter.getDefaultWriteParam();
            ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);

            imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);

            String metaFormatName = imageMetaData.getNativeMetadataFormatName();

            javax.imageio.metadata.IIOMetadataNode root = (javax.imageio.metadata.IIOMetadataNode)
                imageMetaData.getAsTree(metaFormatName);

            javax.imageio.metadata.IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
            graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
            graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(timeBetweenFramesMS / 10));
            graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

            javax.imageio.metadata.IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
            javax.imageio.metadata.IIOMetadataNode child = new javax.imageio.metadata.IIOMetadataNode("ApplicationExtension");
            child.setAttribute("applicationID", "NETSCAPE");
            child.setAttribute("authenticationCode", "2.0");

            int loop = loopContinuously ? 0 : 1;
            child.setUserObject(new byte[]{ 0x1, (byte)(loop & 0xFF), (byte)((loop >> 8) & 0xFF)});
            appExtensionsNode.appendChild(child);

            imageMetaData.setFromTree(metaFormatName, root);

            gifWriter.setOutput(outputStream);
            gifWriter.prepareWriteSequence(null);
        }

        public void writeToSequence(BufferedImage img) throws Exception {
            gifWriter.writeToSequence(new IIOImage(img, null, imageMetaData), imageWriteParam);
        }

        public void close() throws Exception {
            gifWriter.endWriteSequence();
        }

        private static javax.imageio.metadata.IIOMetadataNode getNode(javax.imageio.metadata.IIOMetadataNode rootNode, String nodeName) {
            int nNodes = rootNode.getLength();
            for (int i = 0; i < nNodes; i++) {
                if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) == 0) {
                    return((javax.imageio.metadata.IIOMetadataNode) rootNode.item(i));
                }
            }
            javax.imageio.metadata.IIOMetadataNode node = new javax.imageio.metadata.IIOMetadataNode(nodeName);
            rootNode.appendChild(node);
            return(node);
        }
    }
}
