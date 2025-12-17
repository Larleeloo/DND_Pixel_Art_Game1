import java.awt.*;

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
                    30, 8, 40,                  // Slow but durable
                    45, 150
                );

            case SKELETON:
                return new VariantConfig(
                    0.95, 1.0,
                    new Color(230, 220, 200),   // Bone white
                    new Color(60, 50, 40),      // Dark remnants
                    new Color(40, 35, 30),      // Darker accent
                    15, 6, 80,                  // Fast but fragile
                    50, 200
                );

            case GOBLIN:
                return new VariantConfig(
                    0.7, 0.75,
                    new Color(100, 150, 80),    // Green skin
                    new Color(120, 80, 50),     // Brown leather
                    new Color(80, 60, 40),      // Leather accent
                    12, 5, 100,                 // Small and fast
                    35, 180
                );

            case ORC:
                return new VariantConfig(
                    1.3, 1.4,
                    new Color(80, 120, 60),     // Dark green skin
                    new Color(100, 70, 50),     // Brown armor
                    new Color(60, 45, 35),      // Dark accent
                    50, 15, 60,                 // Big and strong
                    60, 200
                );

            case BANDIT:
                return new VariantConfig(
                    1.0, 1.0,
                    new Color(220, 180, 150),   // Human skin
                    new Color(80, 60, 50),      // Dark clothes
                    new Color(150, 100, 70),    // Leather
                    25, 8, 70,                  // Balanced
                    50, 220
                );

            case KNIGHT:
                return new VariantConfig(
                    1.1, 1.1,
                    new Color(200, 160, 130),   // Human skin
                    new Color(150, 150, 160),   // Steel armor
                    new Color(100, 100, 110),   // Dark steel
                    40, 12, 50,                 // Armored, slower
                    55, 180
                );

            case MAGE:
                return new VariantConfig(
                    0.95, 1.0,
                    new Color(200, 180, 160),   // Pale skin
                    new Color(80, 60, 120),     // Purple robes
                    new Color(60, 40, 90),      // Dark purple
                    20, 15, 40,                 // Fragile but dangerous
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
}
