package entity.mob;
import entity.*;
import entity.player.*;
import block.*;
import animation.*;
import animation.bone.*;
import graphics.*;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Factory class for creating humanoid skeleton variants.
 * Provides different enemy types: Zombie, Skeleton, Goblin, Orc, etc.
 *
 * ============================================================================
 * HUMANOID VARIANT TYPES:
 * ============================================================================
 *
 * ZOMBIE       - Slow, shambling undead with green/gray skin
 * SKELETON     - Bony undead, faster but weaker
 * GOBLIN       - Small, fast, green-skinned
 * ORC          - Large, strong, green-skinned
 * BANDIT       - Human enemy with varied equipment
 * KNIGHT       - Armored human enemy
 * MAGE         - Robed human with magic
 *
 * ============================================================================
 */
public class HumanoidVariants {

    /**
     * Humanoid variant types.
     */
    public enum VariantType {
        ZOMBIE,
        SKELETON,
        GOBLIN,
        ORC,
        BANDIT,
        KNIGHT,
        MAGE
    }

    /**
     * Configuration for humanoid variants.
     */
    public static class VariantConfig {
        public double scaleX, scaleY;
        public Color skinColor;
        public Color clothesColor;
        public Color accentColor;
        public int health;
        public int damage;
        public double speed;
        public double attackRange;
        public double detectionRange;

        public VariantConfig(double scaleX, double scaleY,
                            Color skinColor, Color clothesColor, Color accentColor,
                            int health, int damage, double speed,
                            double attackRange, double detectionRange) {
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.skinColor = skinColor;
            this.clothesColor = clothesColor;
            this.accentColor = accentColor;
            this.health = health;
            this.damage = damage;
            this.speed = speed;
            this.attackRange = attackRange;
            this.detectionRange = detectionRange;
        }
    }

    /**
     * Gets the configuration for a variant type.
     */
    public static VariantConfig getConfig(VariantType type) {
        switch (type) {
            case ZOMBIE:
                return new VariantConfig(
                    1.0, 1.0,
                    new Color(120, 150, 100),   // Green-gray skin
                    new Color(80, 60, 40),      // Tattered brown clothes
                    new Color(50, 40, 30),      // Dark accent
                    50, 8, 40,                  // Slow but durable
                    45, 150
                );

            case SKELETON:
                return new VariantConfig(
                    0.95, 1.0,
                    new Color(230, 220, 200),   // Bone white
                    new Color(60, 50, 40),      // Dark remnants
                    new Color(40, 35, 30),      // Darker accent
                    40, 6, 80,                  // Fast but fragile
                    50, 200
                );

            case GOBLIN:
                return new VariantConfig(
                    0.7, 0.75,
                    new Color(100, 150, 80),    // Green skin
                    new Color(120, 80, 50),     // Brown leather
                    new Color(80, 60, 40),      // Leather accent
                    40, 5, 100,                 // Small and fast
                    35, 180
                );

            case ORC:
                return new VariantConfig(
                    1.3, 1.4,
                    new Color(80, 120, 60),     // Dark green skin
                    new Color(100, 70, 50),     // Brown armor
                    new Color(60, 45, 35),      // Dark accent
                    60, 15, 60,                 // Big and strong
                    60, 200
                );

            case BANDIT:
                return new VariantConfig(
                    1.0, 1.0,
                    new Color(220, 180, 150),   // Human skin
                    new Color(80, 60, 50),      // Dark clothes
                    new Color(150, 100, 70),    // Leather
                    45, 8, 70,                  // Balanced
                    50, 220
                );

            case KNIGHT:
                return new VariantConfig(
                    1.1, 1.1,
                    new Color(200, 160, 130),   // Human skin
                    new Color(150, 150, 160),   // Steel armor
                    new Color(100, 100, 110),   // Dark steel
                    55, 12, 50,                 // Armored, slower
                    55, 180
                );

            case MAGE:
                return new VariantConfig(
                    0.95, 1.0,
                    new Color(200, 180, 160),   // Pale skin
                    new Color(80, 60, 120),     // Purple robes
                    new Color(60, 40, 90),      // Dark purple
                    40, 15, 40,                 // Fragile but dangerous
                    100, 250                    // Long range
                );

            default:
                return new VariantConfig(
                    1.0, 1.0,
                    new Color(255, 200, 150),
                    new Color(100, 100, 100),
                    new Color(60, 60, 60),
                    20, 5, 60,
                    45, 180
                );
        }
    }

    /**
     * Creates a humanoid skeleton with variant-specific colors.
     *
     * @param type The variant type
     * @return A configured skeleton
     */
    public static Skeleton createVariant(VariantType type) {
        VariantConfig config = getConfig(type);
        Skeleton skeleton = Skeleton.createHumanoid();

        // Apply scale
        skeleton.setScale(config.scaleX);

        // Apply colors to bones
        applyVariantColors(skeleton, config);

        return skeleton;
    }

    /**
     * Creates a humanoid skeleton with textures and variant colors.
     *
     * @param type       The variant type
     * @param textureDir Directory containing textures
     * @return A configured skeleton with textures
     */
    public static Skeleton createVariantWithTextures(VariantType type, String textureDir) {
        VariantConfig config = getConfig(type);
        Skeleton skeleton = Skeleton.createHumanoidWithTextures(textureDir);

        // Apply scale
        skeleton.setScale(config.scaleX);

        // Apply tint colors to bones
        applyVariantTints(skeleton, config);

        return skeleton;
    }

    /**
     * Applies variant colors to skeleton bones (for placeholder rendering).
     */
    private static void applyVariantColors(Skeleton skeleton, VariantConfig config) {
        // Skin color for exposed parts
        String[] skinBones = {"neck", "head", "arm_lower_left", "arm_lower_right",
                              "hand_left", "hand_right"};
        for (String name : skinBones) {
            Bone bone = skeleton.findBone(name);
            if (bone != null) {
                bone.setPlaceholderColor(config.skinColor);
            }
        }

        // Clothes color for torso and upper limbs
        String[] clothesBones = {"torso", "arm_upper_left", "arm_upper_right"};
        for (String name : clothesBones) {
            Bone bone = skeleton.findBone(name);
            if (bone != null) {
                bone.setPlaceholderColor(config.clothesColor);
            }
        }

        // Accent color for legs and feet
        String[] accentBones = {"leg_upper_left", "leg_upper_right",
                                "leg_lower_left", "leg_lower_right",
                                "foot_left", "foot_right"};
        for (String name : accentBones) {
            Bone bone = skeleton.findBone(name);
            if (bone != null) {
                bone.setPlaceholderColor(config.accentColor);
            }
        }
    }

    /**
     * Applies variant tint colors to textured skeleton bones.
     */
    private static void applyVariantTints(Skeleton skeleton, VariantConfig config) {
        // Apply tints similar to colors
        String[] skinBones = {"neck", "head", "arm_lower_left", "arm_lower_right",
                              "hand_left", "hand_right"};
        for (String name : skinBones) {
            Bone bone = skeleton.findBone(name);
            if (bone != null) {
                bone.setTintColor(config.skinColor);
            }
        }

        String[] clothesBones = {"torso", "arm_upper_left", "arm_upper_right"};
        for (String name : clothesBones) {
            Bone bone = skeleton.findBone(name);
            if (bone != null) {
                bone.setTintColor(config.clothesColor);
            }
        }

        String[] accentBones = {"leg_upper_left", "leg_upper_right",
                                "leg_lower_left", "leg_lower_right",
                                "foot_left", "foot_right"};
        for (String name : accentBones) {
            Bone bone = skeleton.findBone(name);
            if (bone != null) {
                bone.setTintColor(config.accentColor);
            }
        }
    }

    // ==================== In-Memory Texture Generation ====================

    private static final int TEXTURE_SCALE = 2;
    private static final Color OUTLINE = new Color(40, 30, 30);

    /**
     * Applies generated textures directly to skeleton bones (no file I/O).
     * This creates proper pixel art textures at runtime.
     *
     * @param skeleton The skeleton to apply textures to
     * @param type The variant type for coloring
     */
    public static void applyTexturesToSkeleton(Skeleton skeleton, VariantType type) {
        VariantConfig config = getConfig(type);

        // Core body parts
        applyTexture(skeleton, "torso", generateTorsoImage(32, 32, config));
        applyTexture(skeleton, "neck", generateNeckImage(16, 8, config));
        applyTexture(skeleton, "head", generateHeadImage(24, 24, config, type));

        // Arms
        applyTexture(skeleton, "arm_upper_left", generateArmImage(12, 20, config.clothesColor, true));
        applyTexture(skeleton, "arm_upper_right", generateArmImage(12, 20, config.clothesColor, false));
        applyTexture(skeleton, "arm_lower_left", generateArmImage(10, 18, config.skinColor, true));
        applyTexture(skeleton, "arm_lower_right", generateArmImage(10, 18, config.skinColor, false));
        applyTexture(skeleton, "hand_left", generateHandImage(10, 10, config.skinColor));
        applyTexture(skeleton, "hand_right", generateHandImage(10, 10, config.skinColor));

        // Legs
        applyTexture(skeleton, "leg_upper_left", generateLegImage(14, 24, config.accentColor, true));
        applyTexture(skeleton, "leg_upper_right", generateLegImage(14, 24, config.accentColor, false));
        applyTexture(skeleton, "leg_lower_left", generateLegImage(12, 22, config.accentColor, true));
        applyTexture(skeleton, "leg_lower_right", generateLegImage(12, 22, config.accentColor, false));
        applyTexture(skeleton, "foot_left", generateFootImage(16, 8, config.accentColor));
        applyTexture(skeleton, "foot_right", generateFootImage(16, 8, config.accentColor));
    }

    private static void applyTexture(Skeleton skeleton, String boneName, BufferedImage texture) {
        Bone bone = skeleton.findBone(boneName);
        if (bone != null && texture != null) {
            bone.setTexture(texture);
        }
    }

    private static BufferedImage generateTorsoImage(int w, int h, VariantConfig config) {
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

    private static BufferedImage generateNeckImage(int w, int h, VariantConfig config) {
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

    private static BufferedImage generateHeadImage(int w, int h, VariantConfig config, VariantType type) {
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
        g.setColor(type == VariantType.SKELETON ? new Color(200, 50, 50) : Color.BLACK);
        int eyeSize = Math.max(3, w / 6);
        g.fillOval(w/3 - eyeSize/2, h/3, eyeSize, eyeSize);
        g.fillOval(w*2/3 - eyeSize/2, h/3, eyeSize, eyeSize);

        // Eye shine (except skeleton)
        if (type != VariantType.SKELETON) {
            g.setColor(Color.WHITE);
            g.fillRect(w/3, h/3, 1, 1);
            g.fillRect(w*2/3, h/3, 1, 1);
        }

        // Mouth/expression based on type
        g.setColor(type == VariantType.SKELETON ? shadow : darken(base, 0.5));
        if (type == VariantType.SKELETON) {
            // Skeleton teeth
            for (int i = 0; i < 4; i++) {
                g.fillRect(w/4 + i * (w/8), h*2/3, w/10, h/6);
            }
        } else if (type == VariantType.ZOMBIE) {
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
}
