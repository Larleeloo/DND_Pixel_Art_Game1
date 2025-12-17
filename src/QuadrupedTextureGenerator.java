import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Generates pixel art textures for quadruped (4-legged animal) skeleton bones.
 * Creates textures for various animal types: wolf, dog, cat, horse, pig, cow, etc.
 */
public class QuadrupedTextureGenerator {

    // Texture scale multiplier (2x for better quality)
    private static final int TEXTURE_SCALE = 2;

    private static final Color OUTLINE = new Color(40, 30, 30);

    public static void main(String[] args) {
        // Generate textures for all animal types
        for (QuadrupedSkeleton.AnimalType type : QuadrupedSkeleton.AnimalType.values()) {
            generateTexturesForAnimal(type);
        }
        System.out.println("Done generating all quadruped textures!");
    }

    /**
     * Generates all bone textures for a specific animal type.
     *
     * @param type The animal type to generate textures for
     */
    public static void generateTexturesForAnimal(QuadrupedSkeleton.AnimalType type) {
        String outputDir = "assets/textures/quadruped/" + type.name().toLowerCase();
        new File(outputDir).mkdirs();

        QuadrupedSkeleton.AnimalConfig config = QuadrupedSkeleton.getConfig(type);

        System.out.println("Generating textures for " + type.name() + " in " + outputDir);

        // Generate body parts based on animal configuration
        int bodyWidth = (int)(48 * config.bodyScaleX);
        int bodyHeight = (int)(24 * config.bodyScaleY);
        int legLength = (int)(16 * config.legLengthMultiplier);
        int tailLen = (int)(20 * config.tailLength);

        // Core bones
        generateBody(outputDir + "/body.png", bodyWidth * TEXTURE_SCALE, bodyHeight * TEXTURE_SCALE, config);
        generateNeck(outputDir + "/neck.png", 12 * TEXTURE_SCALE, 14 * TEXTURE_SCALE, config);
        generateHead(outputDir + "/head.png",
                    (int)(20 * config.headScaleX) * TEXTURE_SCALE,
                    (int)(16 * config.headScaleY) * TEXTURE_SCALE, config, type);
        generateEar(outputDir + "/ear_left.png", 6 * TEXTURE_SCALE, 10 * TEXTURE_SCALE, config, true);
        generateEar(outputDir + "/ear_right.png", 6 * TEXTURE_SCALE, 10 * TEXTURE_SCALE, config, false);

        // Tail
        generateTail(outputDir + "/tail_base.png", (tailLen/2) * TEXTURE_SCALE, 6 * TEXTURE_SCALE, config, true);
        generateTail(outputDir + "/tail_tip.png", (tailLen/2) * TEXTURE_SCALE, 4 * TEXTURE_SCALE, config, false);

        // Legs
        int legWidth = 8;
        generateLegUpper(outputDir + "/leg_front_left_upper.png", legWidth * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, true, true);
        generateLegLower(outputDir + "/leg_front_left_lower.png", (legWidth - 2) * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, true);
        generatePaw(outputDir + "/paw_front_left.png", legWidth * TEXTURE_SCALE, 6 * TEXTURE_SCALE, config, true);

        generateLegUpper(outputDir + "/leg_front_right_upper.png", legWidth * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, true, false);
        generateLegLower(outputDir + "/leg_front_right_lower.png", (legWidth - 2) * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, false);
        generatePaw(outputDir + "/paw_front_right.png", legWidth * TEXTURE_SCALE, 6 * TEXTURE_SCALE, config, false);

        generateLegUpper(outputDir + "/leg_back_left_upper.png", (legWidth + 2) * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, false, true);
        generateLegLower(outputDir + "/leg_back_left_lower.png", legWidth * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, true);
        generatePaw(outputDir + "/paw_back_left.png", legWidth * TEXTURE_SCALE, 6 * TEXTURE_SCALE, config, true);

        generateLegUpper(outputDir + "/leg_back_right_upper.png", (legWidth + 2) * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, false, false);
        generateLegLower(outputDir + "/leg_back_right_lower.png", legWidth * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, false);
        generatePaw(outputDir + "/paw_back_right.png", legWidth * TEXTURE_SCALE, 6 * TEXTURE_SCALE, config, false);

        System.out.println("  Generated 19 bone textures for " + type.name());
    }

    // ==================== Texture Generators ====================

    private static void generateBody(String path, int w, int h, QuadrupedSkeleton.AnimalConfig config) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Main body fill
        g.setColor(base);
        g.fillRoundRect(1, 1, w - 2, h - 2, 6, 6);

        // Top highlight (back)
        g.setColor(highlight);
        g.fillRect(4, 1, w - 8, 3);

        // Bottom shadow (belly)
        g.setColor(shadow);
        g.fillRect(4, h - 4, w - 8, 3);

        // Side shading
        g.fillRect(1, 4, 3, h - 8);

        // Add some fur texture lines
        g.setColor(shadow);
        for (int i = 0; i < w - 10; i += 6) {
            g.drawLine(5 + i, 3, 5 + i + 2, 5);
        }

        // Belly area with secondary color
        g.setColor(config.secondaryColor);
        g.fillOval(w/4, h - 6, w/2, 4);

        // Outline
        g.setColor(OUTLINE);
        g.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateNeck(String path, int w, int h, QuadrupedSkeleton.AnimalConfig config) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Main fill
        g.setColor(base);
        g.fillRect(1, 0, w - 2, h);

        // Side shading
        g.setColor(shadow);
        g.fillRect(1, 0, 2, h);

        // Highlight
        g.setColor(highlight);
        g.fillRect(w - 3, 0, 2, h);

        // Fur lines
        g.setColor(shadow);
        for (int i = 0; i < h - 4; i += 4) {
            g.drawLine(2, i, 4, i + 2);
        }

        // Outline (sides only)
        g.setColor(OUTLINE);
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 1);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateHead(String path, int w, int h, QuadrupedSkeleton.AnimalConfig config,
                                     QuadrupedSkeleton.AnimalType type) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Main head shape
        g.setColor(base);
        g.fillRoundRect(1, 1, w - 2, h - 2, 4, 4);

        // Snout/muzzle area (front of head)
        g.setColor(config.secondaryColor);
        g.fillOval(0, h/3, w/2, h/2);

        // Top highlight
        g.setColor(highlight);
        g.fillRect(3, 1, w - 6, 2);

        // Side shadow
        g.setColor(shadow);
        g.fillRect(1, 3, 2, h - 6);

        // Eyes
        g.setColor(Color.BLACK);
        int eyeSize = Math.max(2, w / 8);
        g.fillOval(w/2 - 1, h/4, eyeSize, eyeSize);

        // Eye shine
        g.setColor(Color.WHITE);
        g.fillRect(w/2, h/4, 1, 1);

        // Nose
        g.setColor(config.accentColor);
        g.fillOval(1, h/2 - 1, 3, 3);

        // Outline
        g.setColor(OUTLINE);
        g.drawRoundRect(0, 0, w - 1, h - 1, 4, 4);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateEar(String path, int w, int h, QuadrupedSkeleton.AnimalConfig config, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color base = config.primaryColor;
        Color inner = config.secondaryColor;
        Color shadow = darken(base, 0.7);

        // Triangular ear shape
        int[] xPoints = {w/2, 0, w - 1};
        int[] yPoints = {0, h - 1, h - 1};
        g.setColor(base);
        g.fillPolygon(xPoints, yPoints, 3);

        // Inner ear
        int[] innerX = {w/2, w/4, w*3/4};
        int[] innerY = {h/4, h - 3, h - 3};
        g.setColor(inner);
        g.fillPolygon(innerX, innerY, 3);

        // Shadow on one side
        g.setColor(shadow);
        if (isLeft) {
            g.drawLine(0, h - 1, w/2, 0);
        } else {
            g.drawLine(w - 1, h - 1, w/2, 0);
        }

        // Outline
        g.setColor(OUTLINE);
        g.drawPolygon(xPoints, yPoints, 3);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateTail(String path, int w, int h, QuadrupedSkeleton.AnimalConfig config, boolean isBase) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Tapered tail shape
        if (isBase) {
            // Thicker at base
            g.setColor(base);
            g.fillOval(0, 0, w, h);

            g.setColor(highlight);
            g.fillRect(0, 1, w/2, 2);

            g.setColor(shadow);
            g.fillRect(0, h - 3, w/2, 2);
        } else {
            // Tapered tip
            int[] xPoints = {0, w - 1, w - 1, 0};
            int[] yPoints = {h/4, 0, h - 1, h*3/4};
            g.setColor(base);
            g.fillPolygon(xPoints, yPoints, 4);

            g.setColor(highlight);
            g.drawLine(0, h/4, w/2, 1);
        }

        // Outline
        g.setColor(OUTLINE);
        if (isBase) {
            g.drawOval(0, 0, w - 1, h - 1);
        }

        g.dispose();
        saveImage(img, path);
    }

    private static void generateLegUpper(String path, int w, int h, QuadrupedSkeleton.AnimalConfig config,
                                         boolean isFront, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Main leg fill
        g.setColor(base);
        g.fillRect(1, 0, w - 2, h - 1);

        // Muscle definition
        g.setColor(shadow);
        if (isLeft) {
            g.fillRect(w - 3, 0, 2, h - 1);
        } else {
            g.fillRect(1, 0, 2, h - 1);
        }

        // Highlight
        g.setColor(highlight);
        if (isLeft) {
            g.fillRect(1, 0, 2, h - 1);
        } else {
            g.fillRect(w - 3, 0, 2, h - 1);
        }

        // Muscle detail
        g.setColor(shadow);
        g.fillOval(w/4, h/4, w/2, h/3);

        // Outline
        g.setColor(OUTLINE);
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 1);
        g.drawLine(0, h - 1, w - 1, h - 1);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateLegLower(String path, int w, int h, QuadrupedSkeleton.AnimalConfig config, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Main leg fill
        g.setColor(base);
        g.fillRect(1, 0, w - 2, h - 1);

        // Side shading
        g.setColor(shadow);
        if (isLeft) {
            g.fillRect(w - 3, 0, 2, h - 1);
        } else {
            g.fillRect(1, 0, 2, h - 1);
        }

        // Highlight
        g.setColor(highlight);
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

    private static void generatePaw(String path, int w, int h, QuadrupedSkeleton.AnimalConfig config, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color base = config.accentColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Paw pad area
        g.setColor(base);
        g.fillRoundRect(1, 0, w - 2, h - 1, 3, 3);

        // Highlight
        g.setColor(highlight);
        g.fillRect(2, 0, w - 4, 2);

        // Shadow (bottom)
        g.setColor(shadow);
        g.fillRect(2, h - 3, w - 4, 2);

        // Toe lines
        g.setColor(shadow);
        int toeWidth = w / 4;
        for (int i = 1; i < 4; i++) {
            g.drawLine(i * toeWidth, h - 2, i * toeWidth, h - 1);
        }

        // Outline
        g.setColor(OUTLINE);
        g.drawRoundRect(0, 0, w - 1, h - 1, 3, 3);

        g.dispose();
        saveImage(img, path);
    }

    // ==================== Utility Methods ====================

    private static Color darken(Color c, double factor) {
        return new Color(
            Math.max(0, (int)(c.getRed() * factor)),
            Math.max(0, (int)(c.getGreen() * factor)),
            Math.max(0, (int)(c.getBlue() * factor))
        );
    }

    private static Color brighten(Color c, double factor) {
        return new Color(
            Math.min(255, (int)(c.getRed() * factor)),
            Math.min(255, (int)(c.getGreen() * factor)),
            Math.min(255, (int)(c.getBlue() * factor))
        );
    }

    private static void saveImage(BufferedImage img, String path) {
        try {
            File file = new File(path);
            file.getParentFile().mkdirs();
            ImageIO.write(img, "PNG", file);
            System.out.println("    Created: " + path);
        } catch (Exception e) {
            System.err.println("    Failed to create: " + path + " - " + e.getMessage());
        }
    }
}
