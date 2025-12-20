import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Manages texture loading and generation for skeletons.
 *
 * This class provides a unified texture system that:
 * 1. Checks for user-customized PNG files in texture directories
 * 2. Generates default textures if they don't exist (and saves them as PNGs)
 * 3. Loads textures from files so users can edit them
 *
 * ============================================================================
 * TEXTURE DIRECTORY STRUCTURE:
 * ============================================================================
 *
 * assets/textures/
 * ├── quadruped/
 * │   ├── wolf/         - Wolf textures (19 bones)
 * │   │   ├── body.png
 * │   │   ├── head.png
 * │   │   ├── neck.png
 * │   │   ├── ear_left.png, ear_right.png
 * │   │   ├── tail_base.png, tail_tip.png
 * │   │   ├── leg_front_left_upper.png, leg_front_left_lower.png, paw_front_left.png
 * │   │   ├── leg_front_right_upper.png, leg_front_right_lower.png, paw_front_right.png
 * │   │   ├── leg_back_left_upper.png, leg_back_left_lower.png, paw_back_left.png
 * │   │   └── leg_back_right_upper.png, leg_back_right_lower.png, paw_back_right.png
 * │   ├── dog/
 * │   ├── cat/
 * │   ├── horse/
 * │   ├── pig/
 * │   ├── cow/
 * │   ├── sheep/
 * │   ├── deer/
 * │   ├── bear/
 * │   └── fox/
 * │
 * └── humanoid/
 *     ├── player/       - Player character textures (15 bones)
 *     │   ├── torso.png
 *     │   ├── neck.png
 *     │   ├── head.png
 *     │   ├── arm_upper_left.png, arm_upper_right.png
 *     │   ├── arm_lower_left.png, arm_lower_right.png
 *     │   ├── hand_left.png, hand_right.png
 *     │   ├── leg_upper_left.png, leg_upper_right.png
 *     │   ├── leg_lower_left.png, leg_lower_right.png
 *     │   └── foot_left.png, foot_right.png
 *     ├── orc/
 *     ├── zombie/
 *     └── skeleton/
 *
 * ============================================================================
 * HOW TO CUSTOMIZE TEXTURES:
 * ============================================================================
 *
 * 1. Run the game once - this generates default PNG textures in assets/textures/
 * 2. Open the PNG files in any image editor (GIMP, Photoshop, Aseprite, etc.)
 * 3. Edit the textures - keep the same dimensions!
 * 4. Save and run the game again - your custom textures will be loaded
 *
 * TIPS:
 * - Keep transparency (alpha channel) for smooth edges
 * - Match the original dimensions for proper scaling
 * - Use a 2x scale for better detail (textures are rendered at 0.5x)
 *
 * ============================================================================
 */
public class TextureManager {

    // Base directory for all textures
    public static final String TEXTURE_BASE_DIR = "assets/textures";

    // Track if we've shown the generation message
    private static boolean hasShownGenerationMessage = false;

    /**
     * Ensures textures exist for a quadruped animal type.
     * If textures don't exist, generates default ones and saves to PNG files.
     *
     * @param type The animal type
     * @return The texture directory path
     */
    public static String ensureQuadrupedTextures(QuadrupedSkeleton.AnimalType type) {
        String textureDir = TEXTURE_BASE_DIR + "/quadruped/" + type.name().toLowerCase();

        // Check if textures already exist
        if (!texturesExist(textureDir, QuadrupedSkeleton.getQuadrupedBoneNames())) {
            if (!hasShownGenerationMessage) {
                System.out.println("=============================================================");
                System.out.println("GENERATING DEFAULT TEXTURES");
                System.out.println("=============================================================");
                System.out.println("Texture files not found - generating defaults.");
                System.out.println("You can edit these PNG files to customize your mobs!");
                System.out.println("Location: " + new File(TEXTURE_BASE_DIR).getAbsolutePath());
                System.out.println("=============================================================");
                hasShownGenerationMessage = true;
            }

            // Generate default textures
            QuadrupedTextureGenerator.generateTexturesForAnimal(type);
        }

        return textureDir;
    }

    /**
     * Ensures textures exist for a humanoid mob type.
     * If textures don't exist, generates default ones and saves to PNG files.
     *
     * @param mobType The humanoid mob type name (e.g., "orc", "zombie", "skeleton", "player")
     * @return The texture directory path
     */
    public static String ensureHumanoidTextures(String mobType) {
        String textureDir = TEXTURE_BASE_DIR + "/humanoid/" + mobType.toLowerCase();

        String[] humanoidBones = {
            "torso", "neck", "head",
            "arm_upper_left", "arm_upper_right",
            "arm_lower_left", "arm_lower_right",
            "hand_left", "hand_right",
            "leg_upper_left", "leg_upper_right",
            "leg_lower_left", "leg_lower_right",
            "foot_left", "foot_right"
        };

        // Check if textures already exist
        if (!texturesExist(textureDir, humanoidBones)) {
            if (!hasShownGenerationMessage) {
                System.out.println("=============================================================");
                System.out.println("GENERATING DEFAULT TEXTURES");
                System.out.println("=============================================================");
                System.out.println("Texture files not found - generating defaults.");
                System.out.println("You can edit these PNG files to customize your mobs!");
                System.out.println("Location: " + new File(TEXTURE_BASE_DIR).getAbsolutePath());
                System.out.println("=============================================================");
                hasShownGenerationMessage = true;
            }

            // Generate default textures using HumanoidTextureGenerator
            HumanoidTextureGenerator.generateTexturesForMob(mobType);
        }

        return textureDir;
    }

    /**
     * Applies textures from a directory to a skeleton.
     * Falls back to generated textures if files are missing.
     *
     * @param skeleton The skeleton to apply textures to
     * @param textureDir The directory containing texture PNG files
     * @param boneNames Array of bone names to load textures for
     */
    public static void applyTexturesFromDir(Skeleton skeleton, String textureDir, String[] boneNames) {
        for (String name : boneNames) {
            Bone bone = skeleton.findBone(name);
            if (bone != null) {
                String path = textureDir + "/" + name + ".png";
                File file = new File(path);

                if (file.exists()) {
                    bone.loadTexture(path);
                }
                // If file doesn't exist, bone will use placeholder color
            }
        }
    }

    /**
     * Checks if texture files exist for the given bone names.
     *
     * @param textureDir The texture directory
     * @param boneNames The bone names to check
     * @return true if at least one texture file exists
     */
    private static boolean texturesExist(String textureDir, String[] boneNames) {
        File dir = new File(textureDir);
        if (!dir.exists()) {
            return false;
        }

        // Check if at least the body/torso texture exists
        for (String name : boneNames) {
            File file = new File(textureDir + "/" + name + ".png");
            if (file.exists()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Regenerates all default textures, overwriting any existing files.
     * Useful if you want to reset to defaults.
     */
    public static void regenerateAllDefaults() {
        System.out.println("Regenerating all default textures...");

        // Quadrupeds
        for (QuadrupedSkeleton.AnimalType type : QuadrupedSkeleton.AnimalType.values()) {
            QuadrupedTextureGenerator.generateTexturesForAnimal(type);
        }

        // Humanoids
        String[] humanoidTypes = {"orc", "zombie", "skeleton", "player"};
        for (String mobType : humanoidTypes) {
            HumanoidTextureGenerator.generateTexturesForMob(mobType);
        }

        System.out.println("Done! All textures regenerated in: " + new File(TEXTURE_BASE_DIR).getAbsolutePath());
    }

    /**
     * Command-line utility to generate all textures.
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--regenerate")) {
            regenerateAllDefaults();
        } else {
            System.out.println("TextureManager - Texture Generation Utility");
            System.out.println("Usage: java TextureManager [--regenerate]");
            System.out.println();
            System.out.println("Options:");
            System.out.println("  --regenerate   Regenerate all default textures (overwrites existing)");
            System.out.println();
            System.out.println("Textures are automatically generated when mobs are created.");
            System.out.println("Edit the PNG files in " + TEXTURE_BASE_DIR + " to customize!");
        }
    }
}
