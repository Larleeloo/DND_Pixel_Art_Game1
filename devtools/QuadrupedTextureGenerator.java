import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import animation.QuadrupedSkeleton;
import animation.Skeleton;
import animation.Bone;

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
        generateBody(outputDir + "/body.png", bodyWidth * TEXTURE_SCALE, bodyHeight * TEXTURE_SCALE, config, type);
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

    private static void generateBody(String path, int w, int h, QuadrupedSkeleton.AnimalConfig config,
                                     QuadrupedSkeleton.AnimalType type) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);

        // Species-specific body generation
        generateBodyWithType(g, w, h, config, type);

        g.dispose();
        saveImage(img, path);
    }

    private static void generateBodyWithType(Graphics2D g, int w, int h, QuadrupedSkeleton.AnimalConfig config,
                                             QuadrupedSkeleton.AnimalType type) {
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Main body fill - slightly more oval for natural look
        g.setColor(base);
        g.fillOval(0, 0, w, h);

        // Species-specific body features
        switch (type) {
            case COW:
                // Cow spots pattern
                g.setColor(config.secondaryColor);
                g.fillOval(w/6, h/6, w/4, h/3);
                g.fillOval(w/2, h/4, w/5, h/4);
                g.fillOval(w*2/3, h/2, w/6, h/4);
                break;

            case SHEEP:
                // Fluffy wool texture
                g.setColor(shadow);
                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 3; j++) {
                        int bx = w/8 + i * (w/7);
                        int by = h/6 + j * (h/4);
                        g.fillOval(bx, by, w/10, h/6);
                    }
                }
                g.setColor(highlight);
                for (int i = 0; i < 5; i++) {
                    g.fillOval(w/6 + i * (w/6), 2, w/12, h/8);
                }
                break;

            case PIG:
                // Rounder belly emphasis
                g.setColor(brighten(base, 1.1));
                g.fillOval(w/4, h/3, w/2, h*2/3);
                break;

            case BEAR:
                // Bulky shoulder hump
                g.setColor(shadow);
                g.fillOval(w/8, -h/6, w/3, h/2);
                // Thick fur texture
                for (int i = 0; i < w - 12; i += 5) {
                    g.drawLine(6 + i, 2, 6 + i + 2, 6);
                    g.drawLine(6 + i, h - 6, 6 + i + 2, h - 2);
                }
                break;

            case FOX:
                // White chest/belly
                g.setColor(config.secondaryColor);
                g.fillOval(0, h/3, w/3, h*2/3);
                break;

            case WOLF:
            case DOG:
                // Sleeker, more muscular build
                g.setColor(shadow);
                g.fillOval(w/10, h/4, w/4, h/2); // Shoulder
                g.fillOval(w*2/3, h/4, w/4, h/2); // Haunch
                break;

            case CAT:
                // Sleek, lithe body
                g.setColor(highlight);
                g.fillOval(w/3, 0, w/3, h/3); // Back arch highlight
                // Tabby stripes for orange cats
                g.setColor(darken(base, 0.8));
                for (int i = 0; i < 4; i++) {
                    int stripeX = w/4 + i * (w/5);
                    g.fillRect(stripeX, h/6, 3, h*2/3);
                }
                break;

            case HORSE:
            case DEER:
                // Athletic, muscular definition
                g.setColor(shadow);
                g.fillOval(w/12, h/4, w/4, h/2); // Strong shoulder
                g.fillOval(w*2/3, h/5, w/4, h*3/5); // Powerful haunch
                break;

            default:
                // Basic fur texture
                break;
        }

        // Common muscle/shading details
        g.setColor(shadow);
        g.fillOval(2, h/4, w/5, h/2);  // Shoulder shadow
        g.fillOval(w*4/5 - 2, h/4, w/5, h/2);  // Hip shadow

        // Top highlight (spine/back)
        g.setColor(highlight);
        g.fillRect(w/4, 1, w/2, 3);

        // Belly area with secondary color (underbelly) - skip for already processed
        if (type != QuadrupedSkeleton.AnimalType.FOX && type != QuadrupedSkeleton.AnimalType.PIG) {
            g.setColor(config.secondaryColor);
            g.fillOval(w/6, h - h/4, w*2/3, h/5);
        }

        // Outline
        g.setColor(OUTLINE);
        g.drawOval(0, 0, w - 1, h - 1);
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
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);

        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Species-specific head generation
        switch (type) {
            case WOLF:
            case DOG:
            case FOX:
                generateCanineHead(g, w, h, config, type);
                break;
            case CAT:
                generateFelineHead(g, w, h, config);
                break;
            case HORSE:
            case DEER:
                generateEquineHead(g, w, h, config, type);
                break;
            case PIG:
                generatePorcineHead(g, w, h, config);
                break;
            case COW:
                generateBovineHead(g, w, h, config);
                break;
            case SHEEP:
                generateOvineHead(g, w, h, config);
                break;
            case BEAR:
                generateUrsineHead(g, w, h, config);
                break;
            default:
                generateGenericHead(g, w, h, config);
        }

        g.dispose();
        saveImage(img, path);
    }

    // Canine head (wolf, dog, fox) - elongated snout, pointed features
    private static void generateCanineHead(Graphics2D g, int w, int h, QuadrupedSkeleton.AnimalConfig config,
                                           QuadrupedSkeleton.AnimalType type) {
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Skull area - rounder at back
        g.setColor(base);
        g.fillOval(w/3, 0, w*2/3, h);

        // Long snout for canines
        int snoutLength = w/2;
        int snoutHeight = h/2;
        int[] snoutX = {0, snoutLength, snoutLength, 0};
        int[] snoutY = {h/3 + 2, h/6, h - h/6, h*2/3 - 2};
        g.fillPolygon(snoutX, snoutY, 4);

        // Snout bridge highlight
        g.setColor(highlight);
        g.fillRect(2, h/3 + 2, snoutLength - 4, 3);

        // Top of head highlight
        g.setColor(highlight);
        g.fillOval(w/2, 1, w/4, 3);

        // Muzzle underside (lighter)
        g.setColor(config.secondaryColor);
        int muzzleY = h/2;
        g.fillRect(2, muzzleY, snoutLength/2, h/4);

        // Fox-specific white muzzle marking
        if (type == QuadrupedSkeleton.AnimalType.FOX) {
            g.setColor(config.secondaryColor);
            g.fillOval(0, h/3, snoutLength/2 + 4, h/2);
        }

        // Cheek fur
        g.setColor(shadow);
        g.fillOval(w/3 - 2, h/2, w/5, h/3);

        // Eye - almond shaped for canines
        int eyeW = Math.max(5, w/6);
        int eyeH = Math.max(3, h/5);
        int eyeX = w/2 + 2;
        int eyeY = h/4;
        g.setColor(Color.WHITE);
        g.fillOval(eyeX, eyeY, eyeW, eyeH);
        // Pupil
        g.setColor(new Color(60, 40, 20)); // Amber/brown eye
        g.fillOval(eyeX + 2, eyeY + 1, eyeW - 3, eyeH - 2);
        // Eye shine
        g.setColor(Color.WHITE);
        g.fillRect(eyeX + eyeW/2, eyeY + 1, 2, 2);

        // Black nose - prominent
        g.setColor(Color.BLACK);
        g.fillRoundRect(0, h/2 - 3, 6, 7, 3, 3);
        g.setColor(new Color(40, 40, 40));
        g.fillRect(1, h/2 - 1, 2, 3);

        // Mouth line
        g.setColor(OUTLINE);
        g.drawLine(5, h*2/3 - 2, snoutLength/2, h*2/3);

        // Outline
        g.setColor(OUTLINE);
        g.drawOval(w/3, 0, w*2/3 - 1, h - 1);
        g.drawPolygon(snoutX, snoutY, 4);
    }

    // Feline head (cat) - round face, small nose, large eyes
    private static void generateFelineHead(Graphics2D g, int w, int h, QuadrupedSkeleton.AnimalConfig config) {
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Round skull - cats have rounder heads
        g.setColor(base);
        g.fillOval(w/5, 0, w*4/5, h);

        // Short snout/muzzle area
        int snoutW = w/3;
        g.setColor(base);
        g.fillOval(0, h/4, snoutW, h/2);

        // White muzzle area
        g.setColor(config.secondaryColor);
        g.fillOval(2, h/3, snoutW - 4, h/3);

        // Top highlight
        g.setColor(highlight);
        g.fillOval(w/3, 1, w/3, 3);

        // Cheek fluff
        g.setColor(shadow);
        g.fillOval(w/4, h*2/3 - 2, w/4, h/4);

        // Large eye - cats have big eyes relative to head
        int eyeW = Math.max(6, w/4);
        int eyeH = Math.max(5, h/3);
        int eyeX = w/2;
        int eyeY = h/5;
        g.setColor(new Color(220, 220, 100)); // Yellow-green cat eye
        g.fillOval(eyeX, eyeY, eyeW, eyeH);
        // Vertical slit pupil
        g.setColor(Color.BLACK);
        g.fillRect(eyeX + eyeW/2 - 1, eyeY + 1, 3, eyeH - 2);
        // Eye shine
        g.setColor(Color.WHITE);
        g.fillRect(eyeX + eyeW/3, eyeY + 2, 2, 2);

        // Small pink nose - triangle shape
        g.setColor(new Color(255, 150, 150));
        int[] noseX = {4, 1, 7};
        int[] noseY = {h/2 - 2, h/2 + 3, h/2 + 3};
        g.fillPolygon(noseX, noseY, 3);

        // Whisker dots
        g.setColor(shadow);
        g.fillRect(snoutW/2 - 2, h/2 + 2, 2, 2);
        g.fillRect(snoutW/2 + 2, h/2 + 3, 2, 2);

        // Mouth - small 'w' shape for cats
        g.setColor(OUTLINE);
        g.drawLine(4, h/2 + 4, 6, h*2/3 - 2);
        g.drawLine(6, h*2/3 - 2, 8, h/2 + 4);

        // Outline
        g.setColor(OUTLINE);
        g.drawOval(w/5, 0, w*4/5 - 1, h - 1);
        g.drawOval(0, h/4, snoutW, h/2);
    }

    // Equine head (horse, deer) - long face, large nostrils
    private static void generateEquineHead(Graphics2D g, int w, int h, QuadrupedSkeleton.AnimalConfig config,
                                           QuadrupedSkeleton.AnimalType type) {
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Long face profile
        g.setColor(base);
        // Main head shape - elongated
        g.fillRoundRect(0, h/6, w, h*2/3, 8, 8);

        // Forehead bulge
        g.setColor(base);
        g.fillOval(w*2/3, 0, w/3, h/2);

        // Jaw line
        g.setColor(shadow);
        g.fillRect(w/4, h*2/3, w/2, h/4);

        // Muzzle highlight
        g.setColor(highlight);
        g.fillRect(2, h/4, w/3, 4);

        // Top of head highlight
        g.setColor(highlight);
        g.fillRect(w*2/3, 2, w/4, 3);

        // Eye - on side of head
        int eyeW = Math.max(4, w/8);
        int eyeH = Math.max(4, h/4);
        int eyeX = w*2/3;
        int eyeY = h/4;
        g.setColor(new Color(50, 30, 20)); // Dark horse eye
        g.fillOval(eyeX, eyeY, eyeW, eyeH);
        g.setColor(Color.WHITE);
        g.fillRect(eyeX + 1, eyeY + 1, 2, 2);

        // Large nostril
        g.setColor(shadow);
        g.fillOval(2, h/2 - 2, 6, 8);
        g.setColor(darken(shadow, 0.6));
        g.fillOval(3, h/2, 4, 5);

        // Mouth line
        g.setColor(OUTLINE);
        g.drawLine(6, h*2/3, w/3, h*2/3 + 2);

        // Deer-specific: lighter muzzle
        if (type == QuadrupedSkeleton.AnimalType.DEER) {
            g.setColor(config.accentColor);
            g.fillRect(0, h/3, w/4, h/3);
        }

        // Outline
        g.setColor(OUTLINE);
        g.drawRoundRect(0, h/6, w - 1, h*2/3, 8, 8);
        g.drawOval(w*2/3, 0, w/3 - 1, h/2);
    }

    // Porcine head (pig) - flat snout, round nose disc
    private static void generatePorcineHead(Graphics2D g, int w, int h, QuadrupedSkeleton.AnimalConfig config) {
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Round head
        g.setColor(base);
        g.fillOval(w/4, 0, w*3/4, h);

        // Flat snout
        g.setColor(base);
        g.fillRoundRect(0, h/4, w/2, h/2, 4, 4);

        // Top highlight
        g.setColor(highlight);
        g.fillOval(w/2, 2, w/3, 4);

        // Snout disc (pig nose) - distinctive round shape
        g.setColor(darken(base, 0.85));
        g.fillOval(0, h/3, w/3, h/3);
        // Nostrils
        g.setColor(shadow);
        g.fillOval(3, h/2 - 3, 4, 4);
        g.fillOval(3, h/2 + 1, 4, 4);

        // Small eye
        int eyeSize = Math.max(3, h/5);
        g.setColor(Color.BLACK);
        g.fillOval(w*2/3, h/4, eyeSize, eyeSize);
        g.setColor(Color.WHITE);
        g.fillRect(w*2/3 + 1, h/4 + 1, 2, 2);

        // Jowl/cheek
        g.setColor(shadow);
        g.fillOval(w/3, h*2/3 - 4, w/4, h/4);

        // Outline
        g.setColor(OUTLINE);
        g.drawOval(w/4, 0, w*3/4 - 1, h - 1);
        g.drawRoundRect(0, h/4, w/2, h/2, 4, 4);
    }

    // Bovine head (cow) - broad face, large muzzle
    private static void generateBovineHead(Graphics2D g, int w, int h, QuadrupedSkeleton.AnimalConfig config) {
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Broad head
        g.setColor(base);
        g.fillRoundRect(w/4, 0, w*3/4, h, 6, 6);

        // Broad muzzle
        g.setColor(config.secondaryColor);
        g.fillRoundRect(0, h/4, w/2 + 4, h/2, 6, 6);

        // Spots pattern (optional for cows)
        g.setColor(config.secondaryColor);
        g.fillOval(w/2, h/6, w/4, h/4);

        // Top highlight
        g.setColor(highlight);
        g.fillRect(w/2, 2, w/3, 3);

        // Large nostrils
        g.setColor(shadow);
        g.fillOval(4, h/3, 6, 8);
        g.fillOval(4, h/2, 6, 8);

        // Eye
        int eyeSize = Math.max(4, h/5);
        g.setColor(new Color(50, 40, 30));
        g.fillOval(w*2/3, h/4, eyeSize + 2, eyeSize);
        g.setColor(Color.WHITE);
        g.fillRect(w*2/3 + 2, h/4 + 1, 2, 2);

        // Mouth
        g.setColor(OUTLINE);
        g.drawLine(8, h*2/3, w/3, h*2/3 + 2);

        // Outline
        g.setColor(OUTLINE);
        g.drawRoundRect(w/4, 0, w*3/4 - 1, h - 1, 6, 6);
        g.drawRoundRect(0, h/4, w/2 + 4, h/2, 6, 6);
    }

    // Ovine head (sheep) - fluffy, small face
    private static void generateOvineHead(Graphics2D g, int w, int h, QuadrupedSkeleton.AnimalConfig config) {
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);
        Color face = config.accentColor; // Darker face

        // Fluffy wool around head
        g.setColor(base);
        g.fillOval(w/4, -h/6, w*3/4, h + h/3);

        // Small dark face
        g.setColor(face);
        g.fillRoundRect(0, h/4, w/2, h/2, 4, 4);

        // Wool texture bumps
        g.setColor(shadow);
        for (int i = 0; i < 4; i++) {
            g.fillOval(w/2 + i*6, h/6 + (i%2)*4, 6, 6);
        }

        // Highlight on wool
        g.setColor(highlight);
        g.fillOval(w/2 + 4, 4, w/4, h/4);

        // Eye
        int eyeSize = Math.max(3, h/6);
        g.setColor(new Color(60, 50, 40));
        g.fillOval(w/3, h/3, eyeSize, eyeSize);
        g.setColor(Color.WHITE);
        g.fillRect(w/3 + 1, h/3 + 1, 1, 1);

        // Small nose
        g.setColor(Color.BLACK);
        g.fillOval(2, h/2 - 2, 4, 4);

        // Mouth
        g.setColor(OUTLINE);
        g.drawLine(4, h/2 + 3, w/4, h/2 + 4);

        // Outline
        g.setColor(OUTLINE);
        g.drawOval(w/4, -h/6, w*3/4 - 1, h + h/3);
        g.drawRoundRect(0, h/4, w/2, h/2, 4, 4);
    }

    // Ursine head (bear) - round, broad with short muzzle
    private static void generateUrsineHead(Graphics2D g, int w, int h, QuadrupedSkeleton.AnimalConfig config) {
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Massive round head
        g.setColor(base);
        g.fillOval(w/6, 0, w*5/6, h);

        // Broad short muzzle
        g.setColor(darken(base, 0.9));
        g.fillRoundRect(0, h/4, w/2, h/2, 6, 6);

        // Top highlight
        g.setColor(highlight);
        g.fillOval(w/2, 2, w/3, 4);

        // Cheek fur
        g.setColor(shadow);
        g.fillOval(w/3, h/2, w/4, h/3);

        // Eye - small relative to head
        int eyeSize = Math.max(3, h/6);
        g.setColor(Color.BLACK);
        g.fillOval(w*2/3, h/4, eyeSize, eyeSize);
        g.setColor(Color.WHITE);
        g.fillRect(w*2/3 + 1, h/4 + 1, 1, 1);

        // Large black nose
        g.setColor(Color.BLACK);
        g.fillRoundRect(0, h/2 - 4, 8, 10, 4, 4);

        // Nostrils
        g.setColor(new Color(30, 25, 20));
        g.fillOval(2, h/2 - 1, 3, 4);

        // Mouth
        g.setColor(OUTLINE);
        g.drawLine(6, h*2/3, w/3, h*2/3 + 2);

        // Outline
        g.setColor(OUTLINE);
        g.drawOval(w/6, 0, w*5/6 - 1, h - 1);
        g.drawRoundRect(0, h/4, w/2, h/2, 6, 6);
    }

    // Generic fallback head
    private static void generateGenericHead(Graphics2D g, int w, int h, QuadrupedSkeleton.AnimalConfig config) {
        Color base = config.primaryColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        g.setColor(base);
        g.fillRoundRect(w/3, 1, w*2/3 - 1, h - 2, 6, 6);

        int[] snoutX = {0, w/3 + 2, w/3 + 2, 0};
        int[] snoutY = {h/3, 1, h - 2, h*2/3};
        g.fillPolygon(snoutX, snoutY, 4);

        g.setColor(highlight);
        g.fillRect(2, h/3, w/3, 2);
        g.fillRect(w/2, 1, w/3, 2);

        int eyeSize = Math.max(3, h / 4);
        g.setColor(Color.WHITE);
        g.fillOval(w/2, h/4, eyeSize + 1, eyeSize);
        g.setColor(Color.BLACK);
        g.fillOval(w/2 + 1, h/4 + 1, eyeSize - 1, eyeSize - 2);

        g.setColor(config.accentColor);
        g.fillOval(0, h/2 - 2, 4, 5);

        g.setColor(OUTLINE);
        g.drawRoundRect(w/3, 0, w*2/3 - 1, h - 1, 6, 6);
        g.drawPolygon(snoutX, snoutY, 4);
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
        applyTexture(skeleton, "body", generateBodyImage(bodyWidth * TEXTURE_SCALE, bodyHeight * TEXTURE_SCALE, config, type));
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

    private static BufferedImage generateBodyImage(int w, int h, QuadrupedSkeleton.AnimalConfig config,
                                                   QuadrupedSkeleton.AnimalType type) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);

        // Use the same species-specific body generation
        generateBodyWithType(g, w, h, config, type);

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
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);

        // Species-specific head generation (same as file-based)
        switch (type) {
            case WOLF:
            case DOG:
            case FOX:
                generateCanineHead(g, w, h, config, type);
                break;
            case CAT:
                generateFelineHead(g, w, h, config);
                break;
            case HORSE:
            case DEER:
                generateEquineHead(g, w, h, config, type);
                break;
            case PIG:
                generatePorcineHead(g, w, h, config);
                break;
            case COW:
                generateBovineHead(g, w, h, config);
                break;
            case SHEEP:
                generateOvineHead(g, w, h, config);
                break;
            case BEAR:
                generateUrsineHead(g, w, h, config);
                break;
            default:
                generateGenericHead(g, w, h, config);
        }

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
