import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Generates pixel art textures for parallax background layers.
 * Run this class directly to generate the textures.
 */
public class ParallaxTextureGenerator {

    private static final Random random = new Random(42); // Fixed seed for reproducibility

    public static void main(String[] args) {
        String outputDir = "assets/parallax/";

        // Ensure directory exists
        new File(outputDir).mkdirs();

        try {
            System.out.println("Generating parallax textures...");

            // Generate sky (large, fills most of screen)
            generateSky(outputDir + "sky.png", 192, 108);

            // Generate building layers (different depths)
            generateBuildingsFar(outputDir + "buildings_far.png", 192, 108);
            generateBuildingsMid(outputDir + "buildings_mid.png", 192, 108);
            generateBuildingsNear(outputDir + "buildings_near.png", 192, 108);

            // Generate foreground (grass, rocks)
            generateForeground(outputDir + "foreground.png", 192, 54);

            System.out.println("All textures generated successfully!");

        } catch (Exception e) {
            System.err.println("Error generating textures: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate a sky texture with gradient and stars/clouds.
     */
    private static void generateSky(String path, int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Sky gradient from dark blue (top) to lighter purple/pink (horizon)
        for (int y = 0; y < height; y++) {
            float ratio = (float) y / height;

            // Top: deep dark blue, Bottom: purple-ish horizon
            int r = (int) (15 + ratio * 60);
            int gr = (int) (10 + ratio * 40);
            int b = (int) (40 + ratio * 80);

            g.setColor(new Color(r, gr, b));
            g.drawLine(0, y, width, y);
        }

        // Add stars (small white/yellow dots in upper portion)
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height * 2 / 3); // Stars in upper 2/3

            // Varying star brightness
            int brightness = 150 + random.nextInt(105);
            boolean isYellow = random.nextInt(5) == 0;

            if (isYellow) {
                g.setColor(new Color(brightness, brightness, brightness - 30));
            } else {
                g.setColor(new Color(brightness, brightness, brightness));
            }

            // Some stars are single pixel, some are 2x2
            if (random.nextInt(3) == 0) {
                g.fillRect(x, y, 2, 2);
            } else {
                g.fillRect(x, y, 1, 1);
            }
        }

        // Add a moon
        int moonX = width * 3 / 4;
        int moonY = height / 5;
        g.setColor(new Color(240, 240, 220));
        g.fillOval(moonX, moonY, 12, 12);
        // Moon crater details
        g.setColor(new Color(200, 200, 180));
        g.fillOval(moonX + 3, moonY + 2, 3, 3);
        g.fillOval(moonX + 6, moonY + 5, 2, 2);

        // Add some wispy clouds near horizon
        g.setColor(new Color(80, 60, 100, 60));
        for (int i = 0; i < 5; i++) {
            int cx = random.nextInt(width);
            int cy = height - 20 + random.nextInt(15);
            int cw = 20 + random.nextInt(30);
            int ch = 3 + random.nextInt(4);
            g.fillOval(cx, cy, cw, ch);
        }

        g.dispose();
        ImageIO.write(img, "PNG", new File(path));
        System.out.println("Generated: " + path);
    }

    /**
     * Generate distant building silhouettes (very far, small, simple shapes).
     */
    private static void generateBuildingsFar(String path, int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Transparent background
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, width, height);
        g.setComposite(AlphaComposite.SrcOver);

        // Dark silhouette color (very dark blue-gray)
        Color buildingColor = new Color(25, 20, 35);
        Color highlightColor = new Color(35, 30, 50);

        int baseY = height - 5;
        int x = 0;

        // Draw simple rectangular buildings as silhouettes
        while (x < width) {
            int buildingWidth = 8 + random.nextInt(15);
            int buildingHeight = 15 + random.nextInt(25);

            // Main building shape
            g.setColor(buildingColor);
            g.fillRect(x, baseY - buildingHeight, buildingWidth, buildingHeight + 5);

            // Occasional spire or antenna
            if (random.nextInt(4) == 0) {
                int spireHeight = 5 + random.nextInt(8);
                int spireX = x + buildingWidth / 2;
                g.fillRect(spireX, baseY - buildingHeight - spireHeight, 2, spireHeight);
            }

            // Very faint window lights (just a few dots)
            g.setColor(new Color(60, 55, 40, 100));
            for (int wy = baseY - buildingHeight + 3; wy < baseY - 3; wy += 5) {
                if (random.nextInt(3) == 0) {
                    g.fillRect(x + 2 + random.nextInt(buildingWidth - 4), wy, 1, 1);
                }
            }

            x += buildingWidth + random.nextInt(5);
        }

        g.dispose();
        ImageIO.write(img, "PNG", new File(path));
        System.out.println("Generated: " + path);
    }

    /**
     * Generate mid-distance buildings with more detail.
     */
    private static void generateBuildingsMid(String path, int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Transparent background
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, width, height);
        g.setComposite(AlphaComposite.SrcOver);

        // Darker building colors
        Color buildingColor = new Color(35, 30, 45);
        Color darkColor = new Color(25, 22, 35);
        Color windowLit = new Color(255, 230, 150, 200);
        Color windowDark = new Color(20, 18, 30);

        int baseY = height - 3;
        int x = 0;

        while (x < width) {
            int buildingWidth = 12 + random.nextInt(20);
            int buildingHeight = 25 + random.nextInt(40);

            // Main building
            g.setColor(buildingColor);
            g.fillRect(x, baseY - buildingHeight, buildingWidth, buildingHeight + 3);

            // Dark edge on right side
            g.setColor(darkColor);
            g.fillRect(x + buildingWidth - 2, baseY - buildingHeight, 2, buildingHeight + 3);

            // Roof variations
            int roofType = random.nextInt(3);
            if (roofType == 0 && buildingWidth > 10) {
                // Flat roof with small structure
                g.setColor(buildingColor);
                int roofW = buildingWidth / 3;
                int roofH = 4 + random.nextInt(4);
                g.fillRect(x + buildingWidth / 3, baseY - buildingHeight - roofH, roofW, roofH);
            } else if (roofType == 1) {
                // Antenna
                g.setColor(darkColor);
                g.fillRect(x + buildingWidth / 2, baseY - buildingHeight - 6, 1, 6);
            }

            // Windows - grid pattern
            int windowSize = 2;
            int windowSpacingX = 4;
            int windowSpacingY = 5;

            for (int wy = baseY - buildingHeight + 4; wy < baseY - 4; wy += windowSpacingY) {
                for (int wx = x + 3; wx < x + buildingWidth - 3; wx += windowSpacingX) {
                    // Random chance for lit vs dark window
                    if (random.nextInt(4) == 0) {
                        g.setColor(windowLit);
                    } else {
                        g.setColor(windowDark);
                    }
                    g.fillRect(wx, wy, windowSize, windowSize);
                }
            }

            x += buildingWidth + random.nextInt(3);
        }

        g.dispose();
        ImageIO.write(img, "PNG", new File(path));
        System.out.println("Generated: " + path);
    }

    /**
     * Generate near buildings with the most detail.
     */
    private static void generateBuildingsNear(String path, int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Transparent background
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, width, height);
        g.setComposite(AlphaComposite.SrcOver);

        // Building colors - more variation
        Color[] buildingColors = {
            new Color(45, 40, 55),
            new Color(50, 45, 60),
            new Color(40, 38, 50),
            new Color(55, 50, 65)
        };
        Color darkColor = new Color(30, 28, 40);
        Color lightEdge = new Color(65, 60, 75);
        Color windowLit = new Color(255, 220, 120);
        Color windowWarm = new Color(255, 180, 100);
        Color windowDark = new Color(25, 22, 35);

        int baseY = height;
        int x = 0;

        while (x < width) {
            int buildingWidth = 18 + random.nextInt(25);
            int buildingHeight = 40 + random.nextInt(50);

            Color mainColor = buildingColors[random.nextInt(buildingColors.length)];

            // Main building
            g.setColor(mainColor);
            g.fillRect(x, baseY - buildingHeight, buildingWidth, buildingHeight);

            // Light edge on left
            g.setColor(lightEdge);
            g.fillRect(x, baseY - buildingHeight, 2, buildingHeight);

            // Dark edge on right
            g.setColor(darkColor);
            g.fillRect(x + buildingWidth - 2, baseY - buildingHeight, 2, buildingHeight);

            // Roof details
            int roofType = random.nextInt(4);
            if (roofType == 0 && buildingWidth > 15) {
                // Water tower style
                g.setColor(darkColor);
                int towerW = 8;
                int towerH = 10;
                int towerX = x + (buildingWidth - towerW) / 2;
                g.fillRect(towerX, baseY - buildingHeight - towerH, towerW, towerH);
                g.fillRect(towerX + 3, baseY - buildingHeight - towerH - 3, 2, 3);
            } else if (roofType == 1) {
                // AC units
                g.setColor(new Color(50, 48, 60));
                g.fillRect(x + 3, baseY - buildingHeight - 3, 4, 3);
                g.fillRect(x + buildingWidth - 7, baseY - buildingHeight - 4, 5, 4);
            } else if (roofType == 2) {
                // Antenna array
                g.setColor(darkColor);
                g.fillRect(x + buildingWidth / 2 - 1, baseY - buildingHeight - 8, 1, 8);
                g.fillRect(x + buildingWidth / 2 + 2, baseY - buildingHeight - 5, 1, 5);
            }

            // Windows - larger, more detailed
            int windowW = 3;
            int windowH = 4;
            int windowSpacingX = 5;
            int windowSpacingY = 6;

            for (int wy = baseY - buildingHeight + 5; wy < baseY - 5; wy += windowSpacingY) {
                for (int wx = x + 4; wx < x + buildingWidth - 4; wx += windowSpacingX) {
                    int windowType = random.nextInt(6);
                    if (windowType == 0) {
                        g.setColor(windowLit);
                    } else if (windowType == 1) {
                        g.setColor(windowWarm);
                    } else {
                        g.setColor(windowDark);
                    }
                    g.fillRect(wx, wy, windowW, windowH);

                    // Window frame
                    g.setColor(darkColor);
                    g.drawRect(wx, wy, windowW, windowH);
                }
            }

            // Door at bottom for some buildings
            if (random.nextInt(3) == 0) {
                g.setColor(darkColor);
                int doorW = 4;
                int doorH = 7;
                g.fillRect(x + buildingWidth / 2 - doorW / 2, baseY - doorH, doorW, doorH);
            }

            x += buildingWidth + random.nextInt(2);
        }

        g.dispose();
        ImageIO.write(img, "PNG", new File(path));
        System.out.println("Generated: " + path);
    }

    /**
     * Generate foreground elements (grass, rocks, vegetation).
     */
    private static void generateForeground(String path, int width, int height) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Transparent background
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, width, height);
        g.setComposite(AlphaComposite.SrcOver);

        // Grass and plant colors
        Color grassDark = new Color(20, 40, 25, 200);
        Color grassMid = new Color(30, 55, 35, 180);
        Color grassLight = new Color(40, 70, 45, 160);
        Color[] grassColors = {grassDark, grassMid, grassLight};

        // Ground line with grass tufts
        int groundY = height - 10;

        // Draw grass blades across the bottom
        for (int x = 0; x < width; x += 2) {
            int numBlades = 1 + random.nextInt(3);
            for (int b = 0; b < numBlades; b++) {
                Color gc = grassColors[random.nextInt(grassColors.length)];
                g.setColor(gc);

                int bladeHeight = 5 + random.nextInt(12);
                int bladeX = x + random.nextInt(3);
                int sway = random.nextInt(3) - 1; // -1, 0, or 1

                // Draw blade as a line from bottom up
                for (int by = 0; by < bladeHeight; by++) {
                    int bx = bladeX + (by > bladeHeight / 2 ? sway : 0);
                    g.fillRect(bx, groundY - by, 1, 1);
                }
            }
        }

        // Add some taller plants/weeds
        for (int i = 0; i < 15; i++) {
            int px = random.nextInt(width);
            int plantHeight = 10 + random.nextInt(15);
            Color plantColor = new Color(25 + random.nextInt(20), 50 + random.nextInt(30), 30 + random.nextInt(15), 180);
            g.setColor(plantColor);

            // Stem
            for (int py = 0; py < plantHeight; py++) {
                g.fillRect(px, groundY - py, 1, 1);
            }

            // Leaves/fronds
            int leafY = groundY - plantHeight + 3;
            g.fillRect(px - 1, leafY, 1, 2);
            g.fillRect(px + 1, leafY - 1, 1, 2);
            g.fillRect(px - 2, leafY + 2, 1, 1);
            g.fillRect(px + 2, leafY + 1, 1, 1);
        }

        // Add some small rocks
        Color rockDark = new Color(40, 38, 45, 200);
        Color rockLight = new Color(60, 58, 65, 180);

        for (int i = 0; i < 8; i++) {
            int rx = random.nextInt(width);
            int rw = 3 + random.nextInt(5);
            int rh = 2 + random.nextInt(3);

            g.setColor(rockDark);
            g.fillOval(rx, groundY - rh + 2, rw, rh);
            g.setColor(rockLight);
            g.fillRect(rx + 1, groundY - rh + 2, rw / 2, 1);
        }

        // Add some fireflies/particles (glowing dots)
        for (int i = 0; i < 12; i++) {
            int fx = random.nextInt(width);
            int fy = random.nextInt(height - 15) + 5;

            g.setColor(new Color(200, 255, 150, 100 + random.nextInt(80)));
            g.fillRect(fx, fy, 1, 1);
        }

        g.dispose();
        ImageIO.write(img, "PNG", new File(path));
        System.out.println("Generated: " + path);
    }
}
