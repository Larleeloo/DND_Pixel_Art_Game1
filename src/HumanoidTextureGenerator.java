import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Generates pixel art textures for humanoid skeleton bones.
 * Creates textures for various humanoid types: zombie, skeleton, orc, etc.
 *
 * ============================================================================
 * HUMANOID SKELETON STRUCTURE (15 bones):
 * ============================================================================
 *
 * torso           - Main body/chest
 * neck            - Connector between torso and head
 * head            - Head/face
 * arm_upper_left  - Left upper arm (shoulder to elbow)
 * arm_upper_right - Right upper arm
 * arm_lower_left  - Left forearm (elbow to wrist)
 * arm_lower_right - Right forearm
 * hand_left       - Left hand
 * hand_right      - Right hand
 * leg_upper_left  - Left thigh (hip to knee)
 * leg_upper_right - Right thigh
 * leg_lower_left  - Left shin (knee to ankle)
 * leg_lower_right - Right shin
 * foot_left       - Left foot
 * foot_right      - Right foot
 *
 * ============================================================================
 */
public class HumanoidTextureGenerator {

    private static final Color OUTLINE = new Color(40, 30, 30);

    public static void main(String[] args) {
        // Generate textures for all humanoid types
        String[] mobTypes = {"zombie", "skeleton", "goblin", "orc", "bandit", "knight", "mage", "player"};
        for (String type : mobTypes) {
            generateTexturesForMob(type);
        }
        System.out.println("Done generating all humanoid textures!");
    }

    /**
     * Generates all bone textures for a specific humanoid mob type.
     *
     * @param mobType The mob type name (zombie, skeleton, orc, etc.)
     */
    public static void generateTexturesForMob(String mobType) {
        String outputDir = "assets/textures/humanoid/" + mobType.toLowerCase();
        new File(outputDir).mkdirs();

        // Get configuration for this mob type
        HumanoidVariants.VariantType variantType = getVariantType(mobType);
        HumanoidVariants.VariantConfig config = HumanoidVariants.getConfig(variantType);

        System.out.println("Generating textures for " + mobType + " in " + outputDir);

        // Generate all bone textures
        saveImage(generateTorsoImage(32, 32, config), outputDir + "/torso.png");
        saveImage(generateNeckImage(16, 8, config), outputDir + "/neck.png");
        saveImage(generateHeadImage(24, 24, config, variantType), outputDir + "/head.png");

        // Arms
        saveImage(generateArmImage(12, 20, config.clothesColor, true), outputDir + "/arm_upper_left.png");
        saveImage(generateArmImage(12, 20, config.clothesColor, false), outputDir + "/arm_upper_right.png");
        saveImage(generateArmImage(10, 18, config.skinColor, true), outputDir + "/arm_lower_left.png");
        saveImage(generateArmImage(10, 18, config.skinColor, false), outputDir + "/arm_lower_right.png");
        saveImage(generateHandImage(10, 10, config.skinColor), outputDir + "/hand_left.png");
        saveImage(generateHandImage(10, 10, config.skinColor), outputDir + "/hand_right.png");

        // Legs
        saveImage(generateLegImage(14, 24, config.accentColor, true), outputDir + "/leg_upper_left.png");
        saveImage(generateLegImage(14, 24, config.accentColor, false), outputDir + "/leg_upper_right.png");
        saveImage(generateLegImage(12, 22, config.accentColor, true), outputDir + "/leg_lower_left.png");
        saveImage(generateLegImage(12, 22, config.accentColor, false), outputDir + "/leg_lower_right.png");
        saveImage(generateFootImage(16, 8, config.accentColor), outputDir + "/foot_left.png");
        saveImage(generateFootImage(16, 8, config.accentColor), outputDir + "/foot_right.png");

        System.out.println("  Generated 15 bone textures for " + mobType);
    }

    /**
     * Maps a mob type string to a VariantType enum.
     */
    private static HumanoidVariants.VariantType getVariantType(String mobType) {
        switch (mobType.toLowerCase()) {
            case "zombie": return HumanoidVariants.VariantType.ZOMBIE;
            case "skeleton": return HumanoidVariants.VariantType.SKELETON;
            case "goblin": return HumanoidVariants.VariantType.GOBLIN;
            case "orc": return HumanoidVariants.VariantType.ORC;
            case "bandit": return HumanoidVariants.VariantType.BANDIT;
            case "knight": return HumanoidVariants.VariantType.KNIGHT;
            case "mage": return HumanoidVariants.VariantType.MAGE;
            case "player": return HumanoidVariants.VariantType.BANDIT; // Use bandit as human base
            default: return HumanoidVariants.VariantType.ZOMBIE;
        }
    }

    // ==================== Texture Generation ====================

    private static BufferedImage generateTorsoImage(int w, int h, HumanoidVariants.VariantConfig config) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color base = config.clothesColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Main body fill
        g.setColor(base);
        g.fillRoundRect(1, 1, w - 2, h - 2, 4, 4);

        // Highlight on top/front
        g.setColor(highlight);
        g.fillRect(w/4, 1, w/2, 3);

        // Shadow on sides
        g.setColor(shadow);
        g.fillRect(1, 3, 3, h - 6);
        g.fillRect(w - 4, 3, 3, h - 6);

        // Belt/waist detail
        g.setColor(darken(base, 0.5));
        g.fillRect(2, h - 5, w - 4, 3);

        // Outline
        g.setColor(OUTLINE);
        g.drawRoundRect(0, 0, w - 1, h - 1, 4, 4);

        g.dispose();
        return img;
    }

    private static BufferedImage generateNeckImage(int w, int h, HumanoidVariants.VariantConfig config) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color base = config.skinColor;
        Color shadow = darken(base, 0.8);

        g.setColor(base);
        g.fillRect(1, 0, w - 2, h);

        g.setColor(shadow);
        g.fillRect(1, 0, 2, h);

        g.setColor(OUTLINE);
        g.drawLine(0, 0, 0, h - 1);
        g.drawLine(w - 1, 0, w - 1, h - 1);

        g.dispose();
        return img;
    }

    private static BufferedImage generateHeadImage(int w, int h, HumanoidVariants.VariantConfig config,
                                                    HumanoidVariants.VariantType type) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color base = config.skinColor;
        Color shadow = darken(base, 0.7);
        Color highlight = brighten(base, 1.2);

        // Head shape
        g.setColor(base);
        g.fillRoundRect(1, 1, w - 2, h - 2, 6, 6);

        // Highlight
        g.setColor(highlight);
        g.fillRect(w/4, 1, w/2, 3);

        // Shadow
        g.setColor(shadow);
        g.fillRect(1, h/3, 3, h/2);

        // Eyes
        g.setColor(type == HumanoidVariants.VariantType.SKELETON ? new Color(200, 50, 50) : Color.BLACK);
        int eyeSize = Math.max(3, w / 6);
        g.fillOval(w/3 - eyeSize/2, h/3, eyeSize, eyeSize);
        g.fillOval(w*2/3 - eyeSize/2, h/3, eyeSize, eyeSize);

        // Eye shine (except skeleton)
        if (type != HumanoidVariants.VariantType.SKELETON) {
            g.setColor(Color.WHITE);
            g.fillRect(w/3, h/3, 1, 1);
            g.fillRect(w*2/3, h/3, 1, 1);
        }

        // Mouth/expression based on type
        g.setColor(type == HumanoidVariants.VariantType.SKELETON ? shadow : darken(base, 0.5));
        if (type == HumanoidVariants.VariantType.SKELETON) {
            // Skeleton teeth
            for (int i = 0; i < 4; i++) {
                g.fillRect(w/4 + i * (w/8), h*2/3, w/10, h/6);
            }
        } else if (type == HumanoidVariants.VariantType.ZOMBIE) {
            // Zombie grimace
            g.drawLine(w/3, h*2/3, w*2/3, h*2/3 + 2);
        }

        // Outline
        g.setColor(OUTLINE);
        g.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);

        g.dispose();
        return img;
    }

    private static BufferedImage generateArmImage(int w, int h, Color color, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color shadow = darken(color, 0.7);
        Color highlight = brighten(color, 1.2);

        g.setColor(color);
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

    private static BufferedImage generateHandImage(int w, int h, Color skinColor) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color shadow = darken(skinColor, 0.8);

        g.setColor(skinColor);
        g.fillRoundRect(1, 1, w - 2, h - 2, 3, 3);

        // Finger lines
        g.setColor(shadow);
        for (int i = 1; i < 4; i++) {
            g.drawLine(i * w/4, h - 3, i * w/4, h - 1);
        }

        g.setColor(OUTLINE);
        g.drawRoundRect(0, 0, w - 1, h - 1, 3, 3);

        g.dispose();
        return img;
    }

    private static BufferedImage generateLegImage(int w, int h, Color color, boolean isLeft) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color shadow = darken(color, 0.7);
        Color highlight = brighten(color, 1.2);

        g.setColor(color);
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

    private static BufferedImage generateFootImage(int w, int h, Color color) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        Color shadow = darken(color, 0.6);
        Color highlight = brighten(color, 1.1);

        g.setColor(color);
        g.fillRoundRect(1, 1, w - 2, h - 2, 3, 2);

        g.setColor(highlight);
        g.fillRect(2, 1, w - 4, 2);

        g.setColor(shadow);
        g.fillRect(2, h - 3, w - 4, 2);

        g.setColor(OUTLINE);
        g.drawRoundRect(0, 0, w - 1, h - 1, 3, 2);

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
