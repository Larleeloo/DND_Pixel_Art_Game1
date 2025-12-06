import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Utility class to generate placeholder block textures.
 * Creates simple 16x16 pixel art textures for all block types.
 *
 * Run this as a standalone program to regenerate all textures,
 * or call generateMissingTextures() to only create missing ones.
 */
public class BlockTextureGenerator {

    private static final int SIZE = 16; // 16x16 base size for pixel art

    public static void main(String[] args) {
        System.out.println("BlockTextureGenerator: Generating all block textures...");
        generateAllTextures();
        System.out.println("BlockTextureGenerator: Done!");
    }

    /**
     * Generates all block textures, overwriting existing ones.
     */
    public static void generateAllTextures() {
        ensureBlocksDirectory();

        // Generate each block type
        generateGrass();
        generateDirt();
        generateStone();
        generateCobblestone();
        generateWood();
        generateLeaves();
        generateBrick();
        generateSand();
        generateWater();
        generateGlass();
        generateCoalOre();
        generateIronOre();
        generateGoldOre();
    }

    /**
     * Generates only missing block textures.
     */
    public static void generateMissingTextures() {
        ensureBlocksDirectory();

        for (BlockType type : BlockType.values()) {
            File file = new File(type.getTexturePath());
            if (!file.exists()) {
                System.out.println("BlockTextureGenerator: Creating missing texture for " + type.name());
                generateTextureForType(type);
            }
        }
    }

    private static void ensureBlocksDirectory() {
        File dir = new File("blocks");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private static void generateTextureForType(BlockType type) {
        switch (type) {
            case GRASS: generateGrass(); break;
            case DIRT: generateDirt(); break;
            case STONE: generateStone(); break;
            case COBBLESTONE: generateCobblestone(); break;
            case WOOD: generateWood(); break;
            case LEAVES: generateLeaves(); break;
            case BRICK: generateBrick(); break;
            case SAND: generateSand(); break;
            case WATER: generateWater(); break;
            case GLASS: generateGlass(); break;
            case COAL_ORE: generateCoalOre(); break;
            case IRON_ORE: generateIronOre(); break;
            case GOLD_ORE: generateGoldOre(); break;
            default: break; // PLATFORM uses existing obstacle.png
        }
    }

    private static void saveTexture(BufferedImage img, String filename) {
        try {
            File file = new File("blocks/" + filename);
            ImageIO.write(img, "PNG", file);
            System.out.println("  Created: " + file.getPath());
        } catch (Exception e) {
            System.err.println("  Failed to create " + filename + ": " + e.getMessage());
        }
    }

    // --- Texture Generators ---

    private static void generateGrass() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);

        // Top layer: grass (green)
        Color grassDark = new Color(34, 139, 34);
        Color grassLight = new Color(50, 180, 50);
        // Bottom layer: dirt (brown)
        Color dirtDark = new Color(139, 90, 43);
        Color dirtLight = new Color(160, 110, 60);

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                Color c;
                if (y < 4) {
                    // Grass top
                    c = ((x + y) % 3 == 0) ? grassLight : grassDark;
                } else if (y < 6) {
                    // Transition
                    c = ((x + y) % 2 == 0) ? grassDark : dirtLight;
                } else {
                    // Dirt
                    c = ((x + y) % 3 == 0) ? dirtLight : dirtDark;
                }
                img.setRGB(x, y, c.getRGB());
            }
        }
        saveTexture(img, "grass.png");
    }

    private static void generateDirt() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Color dirtDark = new Color(139, 90, 43);
        Color dirtLight = new Color(160, 110, 60);
        Color dirtMid = new Color(150, 100, 50);

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int pattern = (x * 7 + y * 11) % 5;
                Color c = pattern == 0 ? dirtLight : (pattern < 3 ? dirtMid : dirtDark);
                img.setRGB(x, y, c.getRGB());
            }
        }
        saveTexture(img, "dirt.png");
    }

    private static void generateStone() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Color stoneDark = new Color(100, 100, 100);
        Color stoneLight = new Color(140, 140, 140);
        Color stoneMid = new Color(120, 120, 120);

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int pattern = (x * 3 + y * 5) % 7;
                Color c = pattern == 0 ? stoneDark : (pattern < 4 ? stoneMid : stoneLight);
                img.setRGB(x, y, c.getRGB());
            }
        }
        saveTexture(img, "stone.png");
    }

    private static void generateCobblestone() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Color dark = new Color(80, 80, 80);
        Color light = new Color(150, 150, 150);
        Color mid = new Color(115, 115, 115);

        // Create cobblestone pattern with "cracks"
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                boolean isCrack = (x == 4 || x == 11 || y == 5 || y == 10);
                int pattern = (x * 5 + y * 3) % 4;
                Color c;
                if (isCrack) {
                    c = dark;
                } else {
                    c = pattern == 0 ? light : mid;
                }
                img.setRGB(x, y, c.getRGB());
            }
        }
        saveTexture(img, "cobblestone.png");
    }

    private static void generateWood() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Color woodDark = new Color(101, 67, 33);
        Color woodLight = new Color(150, 111, 51);
        Color woodMid = new Color(130, 90, 40);

        // Vertical wood grain pattern
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int grainY = (y + x / 4) % 4;
                Color c = grainY == 0 ? woodDark : (grainY < 2 ? woodMid : woodLight);
                img.setRGB(x, y, c.getRGB());
            }
        }
        saveTexture(img, "wood.png");
    }

    private static void generateLeaves() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Color leafDark = new Color(34, 100, 34);
        Color leafLight = new Color(50, 150, 50);
        Color leafMid = new Color(40, 125, 40);

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                // Create leafy pattern with some transparency
                int pattern = (x * 7 + y * 11) % 8;
                if (pattern == 0) {
                    img.setRGB(x, y, 0); // Transparent gap
                } else {
                    Color c = pattern < 3 ? leafDark : (pattern < 6 ? leafMid : leafLight);
                    img.setRGB(x, y, c.getRGB());
                }
            }
        }
        saveTexture(img, "leaves.png");
    }

    private static void generateBrick() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Color brickRed = new Color(150, 50, 50);
        Color brickLight = new Color(180, 70, 70);
        Color mortar = new Color(180, 180, 170);

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                boolean isMortar = (y % 4 == 3) || ((y / 4) % 2 == 0 ? x == 7 : x == 15 || x == 0);
                if (isMortar) {
                    img.setRGB(x, y, mortar.getRGB());
                } else {
                    Color c = ((x + y) % 3 == 0) ? brickLight : brickRed;
                    img.setRGB(x, y, c.getRGB());
                }
            }
        }
        saveTexture(img, "brick.png");
    }

    private static void generateSand() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Color sandLight = new Color(237, 201, 175);
        Color sandDark = new Color(210, 180, 140);
        Color sandMid = new Color(220, 190, 150);

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int pattern = (x * 11 + y * 7) % 5;
                Color c = pattern == 0 ? sandDark : (pattern < 3 ? sandMid : sandLight);
                img.setRGB(x, y, c.getRGB());
            }
        }
        saveTexture(img, "sand.png");
    }

    private static void generateWater() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Color waterDeep = new Color(30, 100, 200, 200); // Semi-transparent
        Color waterLight = new Color(80, 150, 230, 180);

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int wave = (x + y * 2) % 5;
                Color c = wave < 2 ? waterDeep : waterLight;
                img.setRGB(x, y, c.getRGB());
            }
        }
        saveTexture(img, "water.png");
    }

    private static void generateGlass() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Color glassEdge = new Color(200, 220, 255, 150);
        Color glassMid = new Color(220, 240, 255, 80);

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                boolean isEdge = x == 0 || x == 15 || y == 0 || y == 15;
                Color c = isEdge ? glassEdge : glassMid;
                img.setRGB(x, y, c.getRGB());
            }
        }
        saveTexture(img, "glass.png");
    }

    private static void generateCoalOre() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Color stoneDark = new Color(100, 100, 100);
        Color stoneLight = new Color(140, 140, 140);
        Color coal = new Color(30, 30, 30);

        // Stone base with coal spots
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                boolean isCoal = isOreSpot(x, y, 0);
                if (isCoal) {
                    img.setRGB(x, y, coal.getRGB());
                } else {
                    Color c = ((x + y) % 3 == 0) ? stoneLight : stoneDark;
                    img.setRGB(x, y, c.getRGB());
                }
            }
        }
        saveTexture(img, "coal_ore.png");
    }

    private static void generateIronOre() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Color stoneDark = new Color(100, 100, 100);
        Color stoneLight = new Color(140, 140, 140);
        Color iron = new Color(200, 180, 150);

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                boolean isOre = isOreSpot(x, y, 1);
                if (isOre) {
                    img.setRGB(x, y, iron.getRGB());
                } else {
                    Color c = ((x + y) % 3 == 0) ? stoneLight : stoneDark;
                    img.setRGB(x, y, c.getRGB());
                }
            }
        }
        saveTexture(img, "iron_ore.png");
    }

    private static void generateGoldOre() {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Color stoneDark = new Color(100, 100, 100);
        Color stoneLight = new Color(140, 140, 140);
        Color gold = new Color(255, 215, 0);

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                boolean isOre = isOreSpot(x, y, 2);
                if (isOre) {
                    img.setRGB(x, y, gold.getRGB());
                } else {
                    Color c = ((x + y) % 3 == 0) ? stoneLight : stoneDark;
                    img.setRGB(x, y, c.getRGB());
                }
            }
        }
        saveTexture(img, "gold_ore.png");
    }

    /**
     * Determines if a position should be an ore spot.
     * Uses different offsets for different ore types.
     */
    private static boolean isOreSpot(int x, int y, int oreType) {
        // Create irregular ore patterns
        int[][] patterns = {
            // Coal (more common)
            {3, 4}, {4, 4}, {5, 4}, {4, 5}, {11, 10}, {12, 10}, {11, 11}, {6, 12}, {7, 12}
        };
        int[][] ironPatterns = {
            {4, 3}, {5, 3}, {4, 4}, {10, 11}, {11, 11}, {10, 12}
        };
        int[][] goldPatterns = {
            {7, 7}, {8, 7}, {7, 8}, {12, 4}, {13, 4}
        };

        int[][] toCheck = oreType == 0 ? patterns : (oreType == 1 ? ironPatterns : goldPatterns);

        for (int[] pos : toCheck) {
            if (x == pos[0] && y == pos[1]) return true;
        }
        return false;
    }
}
