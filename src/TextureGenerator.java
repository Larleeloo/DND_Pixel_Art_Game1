import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Generates pixel art textures for the player skeleton bones.
 * Creates 32x32 base quality textures (2x the internal bone sizes).
 */
public class TextureGenerator {

    // Texture scale multiplier (2x for 32x32 base quality)
    // Bones are now at 32x32 base quality with RENDER_SCALE=1
    private static final int TEXTURE_SCALE = 2;

    // Base colors for different body parts
    private static final Color SKIN_BASE = new Color(255, 200, 150);
    private static final Color SKIN_SHADOW = new Color(220, 160, 120);
    private static final Color SKIN_HIGHLIGHT = new Color(255, 230, 200);

    private static final Color SHIRT_BASE = new Color(100, 150, 200);
    private static final Color SHIRT_SHADOW = new Color(70, 110, 160);
    private static final Color SHIRT_HIGHLIGHT = new Color(140, 180, 220);

    private static final Color PANTS_BASE = new Color(80, 80, 120);
    private static final Color PANTS_SHADOW = new Color(50, 50, 90);
    private static final Color PANTS_HIGHLIGHT = new Color(110, 110, 150);

    private static final Color SHOE_BASE = new Color(60, 40, 20);
    private static final Color SHOE_SHADOW = new Color(40, 25, 10);
    private static final Color SHOE_HIGHLIGHT = new Color(90, 60, 35);

    private static final Color OUTLINE = new Color(40, 30, 30);

    public static void main(String[] args) {
        String outputDir = "assets/textures/player";

        // Ensure output directory exists
        new File(outputDir).mkdirs();

        System.out.println("Generating player bone textures at " + TEXTURE_SCALE + "x scale...");

        // Generate all 15 bone textures
        // Sizes are based on Skeleton.java bone sizes * TEXTURE_SCALE

        // Core bones
        generateTorso(outputDir + "/torso.png", 16 * TEXTURE_SCALE, 16 * TEXTURE_SCALE);
        generateNeck(outputDir + "/neck.png", 8 * TEXTURE_SCALE, 4 * TEXTURE_SCALE);
        generateHead(outputDir + "/head.png", 12 * TEXTURE_SCALE, 10 * TEXTURE_SCALE);

        // Arms
        generateArmUpper(outputDir + "/arm_upper_left.png", 6 * TEXTURE_SCALE, 8 * TEXTURE_SCALE, true);
        generateArmLower(outputDir + "/arm_lower_left.png", 6 * TEXTURE_SCALE, 8 * TEXTURE_SCALE, true);
        generateHand(outputDir + "/hand_left.png", 6 * TEXTURE_SCALE, 4 * TEXTURE_SCALE, true);

        generateArmUpper(outputDir + "/arm_upper_right.png", 6 * TEXTURE_SCALE, 8 * TEXTURE_SCALE, false);
        generateArmLower(outputDir + "/arm_lower_right.png", 6 * TEXTURE_SCALE, 8 * TEXTURE_SCALE, false);
        generateHand(outputDir + "/hand_right.png", 6 * TEXTURE_SCALE, 4 * TEXTURE_SCALE, false);

        // Legs
        generateLegUpper(outputDir + "/leg_upper_left.png", 8 * TEXTURE_SCALE, 10 * TEXTURE_SCALE, true);
        generateLegLower(outputDir + "/leg_lower_left.png", 6 * TEXTURE_SCALE, 10 * TEXTURE_SCALE, true);
        generateFoot(outputDir + "/foot_left.png", 10 * TEXTURE_SCALE, 4 * TEXTURE_SCALE, true);

        generateLegUpper(outputDir + "/leg_upper_right.png", 8 * TEXTURE_SCALE, 10 * TEXTURE_SCALE, false);
        generateLegLower(outputDir + "/leg_lower_right.png", 6 * TEXTURE_SCALE, 10 * TEXTURE_SCALE, false);
        generateFoot(outputDir + "/foot_right.png", 10 * TEXTURE_SCALE, 4 * TEXTURE_SCALE, false);

        System.out.println("Done! Generated 15 bone textures in " + outputDir);
    }

    // ==================== Texture Generators ====================

    private static void generateTorso(String path, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Fill with shirt base color
        g.setColor(SHIRT_BASE);
        g.fillRect(1, 1, w - 2, h - 2);

        // Add shading on left side and bottom
        g.setColor(SHIRT_SHADOW);
        g.fillRect(1, h - 4, w - 2, 3);  // Bottom shadow
        g.fillRect(1, 1, 3, h - 2);       // Left shadow

        // Add highlight on right side and top
        g.setColor(SHIRT_HIGHLIGHT);
        g.fillRect(w - 4, 1, 3, h - 5);   // Right highlight
        g.fillRect(4, 1, w - 8, 2);       // Top highlight

        // Add some detail - vertical line for shirt center
        g.setColor(SHIRT_SHADOW);
        g.fillRect(w / 2 - 1, 2, 2, h - 4);

        // Add collar area at top
        g.setColor(SKIN_BASE);
        g.fillRect(w / 2 - 3, 0, 6, 3);

        // Outline
        g.setColor(OUTLINE);
        g.drawRect(0, 0, w - 1, h - 1);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateNeck(String path, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Fill with skin color
        g.setColor(SKIN_BASE);
        g.fillRect(1, 0, w - 2, h);

        // Add shading
        g.setColor(SKIN_SHADOW);
        g.fillRect(1, 0, 2, h);  // Left shadow

        // Add highlight
        g.setColor(SKIN_HIGHLIGHT);
        g.fillRect(w - 3, 0, 2, h);  // Right highlight

        // Outline (sides only, no top/bottom for seamless connection)
        g.setColor(OUTLINE);
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 1);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateHead(String path, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Fill with skin color
        g.setColor(SKIN_BASE);
        g.fillRect(1, 1, w - 2, h - 2);

        // Add shading
        g.setColor(SKIN_SHADOW);
        g.fillRect(1, h - 3, w - 2, 2);  // Bottom shadow (jaw)
        g.fillRect(1, 1, 2, h - 2);       // Left shadow

        // Add highlight
        g.setColor(SKIN_HIGHLIGHT);
        g.fillRect(w - 3, 1, 2, h - 4);   // Right highlight
        g.fillRect(3, 1, w - 6, 2);       // Top highlight (forehead)

        // Add hair on top
        Color hairColor = new Color(80, 50, 30);
        Color hairShadow = new Color(50, 30, 15);
        g.setColor(hairColor);
        g.fillRect(1, 0, w - 2, 4);
        g.setColor(hairShadow);
        g.fillRect(1, 3, w - 2, 1);

        // Add simple face features
        Color eyeColor = new Color(60, 40, 30);
        // Eyes
        g.setColor(eyeColor);
        g.fillRect(3, 5, 2, 2);           // Left eye
        g.fillRect(w - 5, 5, 2, 2);       // Right eye

        // Eyebrows
        g.fillRect(2, 4, 3, 1);
        g.fillRect(w - 5, 4, 3, 1);

        // Outline
        g.setColor(OUTLINE);
        g.drawRect(0, 0, w - 1, h - 1);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateArmUpper(String path, int w, int h, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Fill with skin color
        g.setColor(SKIN_BASE);
        g.fillRect(1, 0, w - 2, h - 1);

        // Add shading (depends on which arm)
        g.setColor(SKIN_SHADOW);
        if (isLeft) {
            g.fillRect(w - 3, 0, 2, h - 1);  // Right shadow for back arm
        } else {
            g.fillRect(1, 0, 2, h - 1);       // Left shadow for front arm
        }

        // Add highlight
        g.setColor(SKIN_HIGHLIGHT);
        if (isLeft) {
            g.fillRect(1, 0, 2, h - 1);
        } else {
            g.fillRect(w - 3, 0, 2, h - 1);
        }

        // Add muscle definition
        g.setColor(SKIN_SHADOW);
        g.fillRect(w / 2 - 1, h / 3, 2, h / 3);

        // Outline (no top for shoulder connection)
        g.setColor(OUTLINE);
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 1);
        g.drawLine(0, h - 1, w - 1, h - 1);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateArmLower(String path, int w, int h, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Fill with skin color
        g.setColor(SKIN_BASE);
        g.fillRect(1, 0, w - 2, h - 1);

        // Add shading
        g.setColor(SKIN_SHADOW);
        if (isLeft) {
            g.fillRect(w - 3, 0, 2, h - 1);
        } else {
            g.fillRect(1, 0, 2, h - 1);
        }

        // Add highlight
        g.setColor(SKIN_HIGHLIGHT);
        if (isLeft) {
            g.fillRect(1, 0, 2, h - 1);
        } else {
            g.fillRect(w - 3, 0, 2, h - 1);
        }

        // Outline
        g.setColor(OUTLINE);
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 1);
        g.drawLine(0, h - 1, w - 1, h - 1);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateHand(String path, int w, int h, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Slightly darker skin for hands
        Color handBase = new Color(255, 180, 130);
        Color handShadow = new Color(220, 150, 100);
        Color handHighlight = new Color(255, 210, 170);

        // Fill with hand color
        g.setColor(handBase);
        g.fillRect(1, 0, w - 2, h - 1);

        // Add shading
        g.setColor(handShadow);
        if (isLeft) {
            g.fillRect(w - 3, 0, 2, h - 1);
        } else {
            g.fillRect(1, 0, 2, h - 1);
        }

        // Add highlight
        g.setColor(handHighlight);
        if (isLeft) {
            g.fillRect(1, 0, 2, h - 1);
        } else {
            g.fillRect(w - 3, 0, 2, h - 1);
        }

        // Add finger lines
        g.setColor(handShadow);
        g.drawLine(w / 3, h - 2, w / 3, h - 1);
        g.drawLine(w * 2 / 3, h - 2, w * 2 / 3, h - 1);

        // Outline
        g.setColor(OUTLINE);
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 1);
        g.drawLine(0, h - 1, w - 1, h - 1);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateLegUpper(String path, int w, int h, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Fill with pants color
        g.setColor(PANTS_BASE);
        g.fillRect(1, 0, w - 2, h - 1);

        // Add shading
        g.setColor(PANTS_SHADOW);
        if (isLeft) {
            g.fillRect(w - 3, 0, 2, h - 1);
        } else {
            g.fillRect(1, 0, 2, h - 1);
        }
        g.fillRect(1, h - 3, w - 2, 2);  // Bottom shadow

        // Add highlight
        g.setColor(PANTS_HIGHLIGHT);
        if (isLeft) {
            g.fillRect(1, 0, 2, h - 3);
        } else {
            g.fillRect(w - 3, 0, 2, h - 3);
        }

        // Add seam detail
        g.setColor(PANTS_SHADOW);
        g.drawLine(w / 2, 0, w / 2, h - 1);

        // Outline
        g.setColor(OUTLINE);
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 1);
        g.drawLine(0, h - 1, w - 1, h - 1);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateLegLower(String path, int w, int h, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Fill with pants color
        g.setColor(PANTS_BASE);
        g.fillRect(1, 0, w - 2, h - 1);

        // Add shading
        g.setColor(PANTS_SHADOW);
        if (isLeft) {
            g.fillRect(w - 3, 0, 2, h - 1);
        } else {
            g.fillRect(1, 0, 2, h - 1);
        }
        g.fillRect(1, h - 3, w - 2, 2);  // Bottom shadow (cuff)

        // Add highlight
        g.setColor(PANTS_HIGHLIGHT);
        if (isLeft) {
            g.fillRect(1, 0, 2, h - 3);
        } else {
            g.fillRect(w - 3, 0, 2, h - 3);
        }

        // Outline
        g.setColor(OUTLINE);
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 1);
        g.drawLine(0, h - 1, w - 1, h - 1);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateFoot(String path, int w, int h, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Fill with shoe color
        g.setColor(SHOE_BASE);
        g.fillRect(1, 0, w - 2, h - 1);

        // Add shading
        g.setColor(SHOE_SHADOW);
        g.fillRect(1, h - 2, w - 2, 1);  // Bottom sole
        if (isLeft) {
            g.fillRect(w - 3, 0, 2, h - 1);
        } else {
            g.fillRect(1, 0, 2, h - 1);
        }

        // Add highlight
        g.setColor(SHOE_HIGHLIGHT);
        if (isLeft) {
            g.fillRect(1, 0, 2, h - 2);
        } else {
            g.fillRect(w - 3, 0, 2, h - 2);
        }
        g.fillRect(3, 0, w - 6, 1);  // Top highlight

        // Add sole line
        g.setColor(new Color(30, 20, 10));
        g.drawLine(0, h - 1, w - 1, h - 1);

        // Outline
        g.setColor(OUTLINE);
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 1);
        g.drawLine(0, 0, w - 1, 0);

        g.dispose();
        saveImage(img, path);
    }

    // ==================== Utility Methods ====================

    private static void saveImage(BufferedImage img, String path) {
        try {
            File file = new File(path);
            file.getParentFile().mkdirs();
            ImageIO.write(img, "PNG", file);
            System.out.println("  Created: " + path + " (" + img.getWidth() + "x" + img.getHeight() + ")");
        } catch (Exception e) {
            System.err.println("  Failed to create: " + path + " - " + e.getMessage());
        }
    }
}
