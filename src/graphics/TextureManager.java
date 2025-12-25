package graphics;
import animation.*;
import block.*;

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
     * If textures don't exist, logs a warning - textures should be pre-generated using devtools.
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
                System.out.println("WARNING: MISSING TEXTURES");
                System.out.println("=============================================================");
                System.out.println("Texture files not found for: " + type.name().toLowerCase());
                System.out.println("Please run the texture generators in the devtools/ directory.");
                System.out.println("Example: java devtools/QuadrupedTextureGenerator");
                System.out.println("Location: " + new File(TEXTURE_BASE_DIR).getAbsolutePath());
                System.out.println("=============================================================");
                hasShownGenerationMessage = true;
            }
        }

        return textureDir;
    }

    /**
     * Ensures textures exist for a humanoid mob type.
     * If textures don't exist, logs a warning - textures should be pre-generated using devtools.
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
                System.out.println("WARNING: MISSING TEXTURES");
                System.out.println("=============================================================");
                System.out.println("Texture files not found for: " + mobType.toLowerCase());
                System.out.println("Please run the texture generators in the devtools/ directory.");
                System.out.println("Example: java devtools/HumanoidTextureGenerator");
                System.out.println("Location: " + new File(TEXTURE_BASE_DIR).getAbsolutePath());
                System.out.println("=============================================================");
                hasShownGenerationMessage = true;
            }
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
     * Prints instructions for regenerating textures using devtools.
     * Texture generation has been moved to the devtools directory.
     */
    public static void printRegenerationInstructions() {
        System.out.println("=============================================================");
        System.out.println("TEXTURE GENERATION INSTRUCTIONS");
        System.out.println("=============================================================");
        System.out.println("Texture generators have been moved to the devtools/ directory.");
        System.out.println("");
        System.out.println("To regenerate textures, run the following from the project root:");
        System.out.println("  java devtools/QuadrupedTextureGenerator  - for animal textures");
        System.out.println("  java devtools/HumanoidTextureGenerator   - for humanoid textures");
        System.out.println("  java devtools/BlockTextureGenerator      - for block textures");
        System.out.println("  java devtools/ParallaxTextureGenerator   - for background textures");
        System.out.println("");
        System.out.println("Textures will be generated in: " + new File(TEXTURE_BASE_DIR).getAbsolutePath());
        System.out.println("=============================================================");
    }

    /**
     * Command-line utility - now just prints instructions.
     */
    public static void main(String[] args) {
        System.out.println("TextureManager - Runtime Texture Loader");
        System.out.println();
        System.out.println("This class loads textures at runtime from " + TEXTURE_BASE_DIR);
        System.out.println();
        printRegenerationInstructions();
    }
}
