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
        generateNeck(outputDir + "/neck.png", 10 * TEXTURE_SCALE, 12 * TEXTURE_SCALE, config);
        // Head is horizontal in profile - width > height
        generateHead(outputDir + "/head.png",
                    (int)(24 * config.headScaleX) * TEXTURE_SCALE,
                    (int)(18 * config.headScaleY) * TEXTURE_SCALE, config, type);
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

        // Profile view head - horizontal orientation
        // Skull area (back/top of head) - rounder
        g.setColor(base);
        g.fillRoundRect(w/3, 1, w*2/3 - 1, h - 2, 6, 6);

        // Snout/muzzle (front of head) - tapered
        g.setColor(base);
        int[] snoutX = {0, w/3 + 2, w/3 + 2, 0};
        int[] snoutY = {h/3, 1, h - 2, h*2/3};
        g.fillPolygon(snoutX, snoutY, 4);

        // Snout bridge highlight
        g.setColor(highlight);
        g.fillRect(2, h/3, w/3, 2);

        // Top of head highlight
        g.setColor(highlight);
        g.fillRect(w/2, 1, w/3, 2);

        // Snout underside (lighter muzzle)
        g.setColor(config.secondaryColor);
        g.fillRect(2, h/2, w/4, h/3);

        // Cheek/jaw shadow
        g.setColor(shadow);
        g.fillRect(w/3, h*2/3, w/4, h/4);

        // Eye (positioned on side of head, visible in profile)
        g.setColor(Color.WHITE);
        int eyeSize = Math.max(3, h / 4);
        int eyeX = w/2;
        int eyeY = h/4;
        g.fillOval(eyeX, eyeY, eyeSize + 1, eyeSize);

        // Pupil
        g.setColor(Color.BLACK);
        g.fillOval(eyeX + 1, eyeY + 1, eyeSize - 1, eyeSize - 2);

        // Eye shine
        g.setColor(Color.WHITE);
        g.fillRect(eyeX + eyeSize/2, eyeY + 1, 2, 2);

        // Nose at tip of snout
        g.setColor(config.accentColor);
        g.fillOval(0, h/2 - 2, 4, 5);
        g.setColor(darken(config.accentColor, 0.6));
        g.fillOval(1, h/2 - 1, 2, 3);

        // Mouth line
        g.setColor(shadow);
        g.drawLine(4, h*2/3 - 1, w/4, h*2/3 - 1);

        // Outline
        g.setColor(OUTLINE);
        g.drawRoundRect(w/3, 0, w*2/3 - 1, h - 1, 6, 6);
        g.drawPolygon(snoutX, snoutY, 4);

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

    // ==================== In-Memory Texture Generation ====================

    /**
     * Applies generated textures directly to skeleton bones (no file I/O).
     * This is used at runtime to create textured mobs without pre-generated files.
     *
     * @param skeleton The skeleton to apply textures to
     * @param type The animal type for coloring
     */
    public static void applyTexturesToSkeleton(Skeleton skeleton, QuadrupedSkeleton.AnimalType type) {
        QuadrupedSkeleton.AnimalConfig config = QuadrupedSkeleton.getConfig(type);

        int bodyWidth = (int)(48 * config.bodyScaleX);
        int bodyHeight = (int)(24 * config.bodyScaleY);
        int legLength = (int)(16 * config.legLengthMultiplier);
        int tailLen = (int)(20 * config.tailLength);
        int legWidth = 8;

        // Apply textures to each bone
        applyTexture(skeleton, "body", generateBodyImage(bodyWidth * TEXTURE_SCALE, bodyHeight * TEXTURE_SCALE, config));
        applyTexture(skeleton, "neck", generateNeckImage(10 * TEXTURE_SCALE, 12 * TEXTURE_SCALE, config));
        // Head is horizontal in profile - width > height
        applyTexture(skeleton, "head", generateHeadImage((int)(24 * config.headScaleX) * TEXTURE_SCALE, (int)(18 * config.headScaleY) * TEXTURE_SCALE, config, type));
        applyTexture(skeleton, "ear_left", generateEarImage(6 * TEXTURE_SCALE, 10 * TEXTURE_SCALE, config, true));
        applyTexture(skeleton, "ear_right", generateEarImage(6 * TEXTURE_SCALE, 10 * TEXTURE_SCALE, config, false));

        applyTexture(skeleton, "tail_base", generateTailImage((tailLen/2) * TEXTURE_SCALE, 6 * TEXTURE_SCALE, config, true));
        applyTexture(skeleton, "tail_tip", generateTailImage((tailLen/2) * TEXTURE_SCALE, 4 * TEXTURE_SCALE, config, false));

        applyTexture(skeleton, "leg_front_left_upper", generateLegUpperImage(legWidth * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, true, true));
        applyTexture(skeleton, "leg_front_left_lower", generateLegLowerImage((legWidth - 2) * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, true));
        applyTexture(skeleton, "paw_front_left", generatePawImage(legWidth * TEXTURE_SCALE, 6 * TEXTURE_SCALE, config, true));

        applyTexture(skeleton, "leg_front_right_upper", generateLegUpperImage(legWidth * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, true, false));
        applyTexture(skeleton, "leg_front_right_lower", generateLegLowerImage((legWidth - 2) * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, false));
        applyTexture(skeleton, "paw_front_right", generatePawImage(legWidth * TEXTURE_SCALE, 6 * TEXTURE_SCALE, config, false));

        applyTexture(skeleton, "leg_back_left_upper", generateLegUpperImage((legWidth + 2) * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, false, true));
        applyTexture(skeleton, "leg_back_left_lower", generateLegLowerImage(legWidth * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, true));
        applyTexture(skeleton, "paw_back_left", generatePawImage(legWidth * TEXTURE_SCALE, 6 * TEXTURE_SCALE, config, true));

        applyTexture(skeleton, "leg_back_right_upper", generateLegUpperImage((legWidth + 2) * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, false, false));
        applyTexture(skeleton, "leg_back_right_lower", generateLegLowerImage(legWidth * TEXTURE_SCALE, legLength * TEXTURE_SCALE, config, false));
        applyTexture(skeleton, "paw_back_right", generatePawImage(legWidth * TEXTURE_SCALE, 6 * TEXTURE_SCALE, config, false));
    }

    private static void applyTexture(Skeleton skeleton, String boneName, BufferedImage texture) {
        Bone bone = skeleton.findBone(boneName);
        if (bone != null && texture != null) {
            bone.setTexture(texture);
        }
    }

    // Image generators that return BufferedImage instead of saving to file

    private static BufferedImage generateBodyImage(int w, int h, QuadrupedSkeleton.AnimalConfig config) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        g.setColor(base);
        g.fillRoundRect(1, 1, w - 2, h - 2, 6, 6);
        g.setColor(highlight);
        g.fillRect(4, 1, w - 8, 3);
        g.setColor(shadow);
        g.fillRect(4, h - 4, w - 8, 3);
        g.fillRect(1, 4, 3, h - 8);
        for (int i = 0; i < w - 10; i += 6) {
            g.drawLine(5 + i, 3, 5 + i + 2, 5);
        }
        g.setColor(config.secondaryColor);
        g.fillOval(w/4, h - 6, w/2, 4);
        g.setColor(OUTLINE);
        g.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);
        g.dispose();
        return img;
    }

    private static BufferedImage generateNeckImage(int w, int h, QuadrupedSkeleton.AnimalConfig config) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        g.setColor(base);
        g.fillRect(1, 0, w - 2, h);
        g.setColor(shadow);
        g.fillRect(1, 0, 2, h);
        g.setColor(highlight);
        g.fillRect(w - 3, 0, 2, h);
        g.setColor(OUTLINE);
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 1);
        g.dispose();
        return img;
    }

    private static BufferedImage generateHeadImage(int w, int h, QuadrupedSkeleton.AnimalConfig config, QuadrupedSkeleton.AnimalType type) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Profile view head - horizontal orientation
        // Skull area (back/top of head) - rounder
        g.setColor(base);
        g.fillRoundRect(w/3, 1, w*2/3 - 1, h - 2, 6, 6);

        // Snout/muzzle (front of head) - tapered
        g.setColor(base);
        int[] snoutX = {0, w/3 + 2, w/3 + 2, 0};
        int[] snoutY = {h/3, 1, h - 2, h*2/3};
        g.fillPolygon(snoutX, snoutY, 4);

        // Snout bridge highlight
        g.setColor(highlight);
        g.fillRect(2, h/3, w/3, 2);

        // Top of head highlight
        g.setColor(highlight);
        g.fillRect(w/2, 1, w/3, 2);

        // Snout underside (lighter muzzle)
        g.setColor(config.secondaryColor);
        g.fillRect(2, h/2, w/4, h/3);

        // Cheek/jaw shadow
        g.setColor(shadow);
        g.fillRect(w/3, h*2/3, w/4, h/4);

        // Eye (positioned on side of head, visible in profile)
        g.setColor(Color.WHITE);
        int eyeSize = Math.max(3, h / 4);
        int eyeX = w/2;
        int eyeY = h/4;
        g.fillOval(eyeX, eyeY, eyeSize + 1, eyeSize);

        // Pupil
        g.setColor(Color.BLACK);
        g.fillOval(eyeX + 1, eyeY + 1, eyeSize - 1, eyeSize - 2);

        // Eye shine
        g.setColor(Color.WHITE);
        g.fillRect(eyeX + eyeSize/2, eyeY + 1, 2, 2);

        // Nose at tip of snout
        g.setColor(config.accentColor);
        g.fillOval(0, h/2 - 2, 4, 5);
        g.setColor(darken(config.accentColor, 0.6));
        g.fillOval(1, h/2 - 1, 2, 3);

        // Mouth line
        g.setColor(shadow);
        g.drawLine(4, h*2/3 - 1, w/4, h*2/3 - 1);

        // Outline
        g.setColor(OUTLINE);
        g.drawRoundRect(w/3, 0, w*2/3 - 1, h - 1, 6, 6);
        g.drawPolygon(snoutX, snoutY, 4);

        g.dispose();
        return img;
    }

    private static BufferedImage generateEarImage(int w, int h, QuadrupedSkeleton.AnimalConfig config, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Color base = config.primaryColor;
        Color inner = config.secondaryColor;
        Color shadow = darken(base, 0.7);

        int[] xPoints = {w/2, 0, w - 1};
        int[] yPoints = {0, h - 1, h - 1};
        g.setColor(base);
        g.fillPolygon(xPoints, yPoints, 3);
        int[] innerX = {w/2, w/4, w*3/4};
        int[] innerY = {h/4, h - 3, h - 3};
        g.setColor(inner);
        g.fillPolygon(innerX, innerY, 3);
        g.setColor(shadow);
        if (isLeft) g.drawLine(0, h - 1, w/2, 0);
        else g.drawLine(w - 1, h - 1, w/2, 0);
        g.setColor(OUTLINE);
        g.drawPolygon(xPoints, yPoints, 3);
        g.dispose();
        return img;
    }

    private static BufferedImage generateTailImage(int w, int h, QuadrupedSkeleton.AnimalConfig config, boolean isBase) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        if (isBase) {
            g.setColor(base);
            g.fillOval(0, 0, w, h);
            g.setColor(highlight);
            g.fillRect(0, 1, w/2, 2);
            g.setColor(shadow);
            g.fillRect(0, h - 3, w/2, 2);
            g.setColor(OUTLINE);
            g.drawOval(0, 0, w - 1, h - 1);
        } else {
            int[] xPoints = {0, w - 1, w - 1, 0};
            int[] yPoints = {h/4, 0, h - 1, h*3/4};
            g.setColor(base);
            g.fillPolygon(xPoints, yPoints, 4);
            g.setColor(highlight);
            g.drawLine(0, h/4, w/2, 1);
        }
        g.dispose();
        return img;
    }

    private static BufferedImage generateLegUpperImage(int w, int h, QuadrupedSkeleton.AnimalConfig config, boolean isFront, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        g.setColor(base);
        g.fillRect(1, 0, w - 2, h - 1);
        g.setColor(shadow);
        if (isLeft) g.fillRect(w - 3, 0, 2, h - 1);
        else g.fillRect(1, 0, 2, h - 1);
        g.setColor(highlight);
        if (isLeft) g.fillRect(1, 0, 2, h - 1);
        else g.fillRect(w - 3, 0, 2, h - 1);
        g.setColor(shadow);
        g.fillOval(w/4, h/4, w/2, h/3);
        g.setColor(OUTLINE);
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 1);
        g.drawLine(0, h - 1, w - 1, h - 1);
        g.dispose();
        return img;
    }

    private static BufferedImage generateLegLowerImage(int w, int h, QuadrupedSkeleton.AnimalConfig config, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        g.setColor(base);
        g.fillRect(1, 0, w - 2, h - 1);
        g.setColor(shadow);
        if (isLeft) g.fillRect(w - 3, 0, 2, h - 1);
        else g.fillRect(1, 0, 2, h - 1);
        g.setColor(highlight);
        if (isLeft) g.fillRect(1, 0, 2, h - 1);
        else g.fillRect(w - 3, 0, 2, h - 1);
        g.setColor(OUTLINE);
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 1);
        g.drawLine(0, h - 1, w - 1, h - 1);
        g.dispose();
        return img;
    }

    private static BufferedImage generatePawImage(int w, int h, QuadrupedSkeleton.AnimalConfig config, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Color base = config.accentColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        g.setColor(base);
        g.fillRoundRect(1, 0, w - 2, h - 1, 3, 3);
        g.setColor(highlight);
        g.fillRect(2, 0, w - 4, 2);
        g.setColor(shadow);
        g.fillRect(2, h - 3, w - 4, 2);
        int toeWidth = w / 4;
        for (int i = 1; i < 4; i++) {
            g.drawLine(i * toeWidth, h - 2, i * toeWidth, h - 1);
        }
        g.setColor(OUTLINE);
        g.drawRoundRect(0, 0, w - 1, h - 1, 3, 3);
        g.dispose();
        return img;
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
