package com.ambermoongame.graphics;

import android.util.Log;

/**
 * Manages texture loading and generation for skeletons.
 *
 * This class provides a unified texture system that:
 * 1. Checks for texture files in the Android assets directory
 * 2. Loads textures via AndroidAssetLoader
 *
 * ============================================================================
 * TEXTURE DIRECTORY STRUCTURE (in assets/):
 * ============================================================================
 *
 * textures/
 * +-- quadruped/
 * |   +-- wolf/         - Wolf textures (19 bones)
 * |   +-- dog/
 * |   +-- cat/
 * |   +-- horse/
 * |   +-- pig/
 * |   +-- cow/
 * |   +-- sheep/
 * |   +-- deer/
 * |   +-- bear/
 * |   +-- fox/
 * |
 * +-- humanoid/
 *     +-- player/       - Player character textures (15 bones)
 *     +-- orc/
 *     +-- zombie/
 *     +-- skeleton/
 *
 * ============================================================================
 */
public class TextureManager {

    private static final String TAG = "TextureManager";

    // Base directory for all textures (relative to assets/)
    public static final String TEXTURE_BASE_DIR = "textures";

    // Track if we've shown the generation message
    private static boolean hasShownWarningMessage = false;

    /**
     * Ensures textures exist for a quadruped animal type.
     * On Android, checks if the asset directory exists via AndroidAssetLoader.
     *
     * @param animalType The animal type name (e.g., "wolf", "dog")
     * @return The texture directory path (relative to assets/)
     */
    public static String ensureQuadrupedTextures(String animalType) {
        String textureDir = TEXTURE_BASE_DIR + "/quadruped/" + animalType.toLowerCase();

        String[] quadrupedBoneNames = {
            "body", "head", "neck",
            "ear_left", "ear_right",
            "tail_base", "tail_tip",
            "leg_front_left_upper", "leg_front_left_lower", "paw_front_left",
            "leg_front_right_upper", "leg_front_right_lower", "paw_front_right",
            "leg_back_left_upper", "leg_back_left_lower", "paw_back_left",
            "leg_back_right_upper", "leg_back_right_lower", "paw_back_right"
        };

        if (!texturesExist(textureDir, quadrupedBoneNames)) {
            if (!hasShownWarningMessage) {
                Log.w(TAG, "Missing textures for quadruped: " + animalType);
                Log.w(TAG, "Expected location: assets/" + textureDir);
                hasShownWarningMessage = true;
            }
        }

        return textureDir;
    }

    /**
     * Ensures textures exist for a humanoid mob type.
     * On Android, checks if the asset directory exists via AndroidAssetLoader.
     *
     * @param mobType The humanoid mob type name (e.g., "orc", "zombie", "skeleton", "player")
     * @return The texture directory path (relative to assets/)
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

        if (!texturesExist(textureDir, humanoidBones)) {
            if (!hasShownWarningMessage) {
                Log.w(TAG, "Missing textures for humanoid: " + mobType);
                Log.w(TAG, "Expected location: assets/" + textureDir);
                hasShownWarningMessage = true;
            }
        }

        return textureDir;
    }

    // --- Uncomment when bone animation system (Skeleton, Bone) is ported ---
    // /**
    //  * Applies textures from a directory to a skeleton.
    //  * Supports both PNG and GIF texture files (GIF for animations).
    //  *
    //  * @param skeleton The skeleton to apply textures to
    //  * @param textureDir The directory containing texture files (relative to assets/)
    //  * @param boneNames Array of bone names to load textures for
    //  */
    // public static void applyTexturesFromDir(Skeleton skeleton, String textureDir, String[] boneNames) {
    //     for (String name : boneNames) {
    //         Bone bone = skeleton.findBone(name);
    //         if (bone != null) {
    //             String gifPath = textureDir + "/" + name + ".gif";
    //             String pngPath = textureDir + "/" + name + ".png";
    //
    //             if (AndroidAssetLoader.exists(gifPath)) {
    //                 bone.loadTexture(gifPath);
    //             } else if (AndroidAssetLoader.exists(pngPath)) {
    //                 bone.loadTexture(pngPath);
    //             }
    //         }
    //     }
    // }

    /**
     * Checks if texture files exist for the given bone names.
     * Uses AndroidAssetLoader to check the assets directory.
     *
     * @param textureDir The texture directory (relative to assets/)
     * @param boneNames The bone names to check
     * @return true if at least one texture file exists
     */
    private static boolean texturesExist(String textureDir, String[] boneNames) {
        // Check if at least one texture file exists (PNG or GIF)
        for (String name : boneNames) {
            String pngPath = textureDir + "/" + name + ".png";
            String gifPath = textureDir + "/" + name + ".gif";
            if (AndroidAssetLoader.exists(pngPath) || AndroidAssetLoader.exists(gifPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads a texture from the assets directory.
     * Convenience method that wraps AndroidAssetLoader.
     *
     * @param path Path relative to assets/ (e.g., "textures/humanoid/player/torso.png")
     * @return The loaded ImageAsset, or null if not found
     */
    public static AndroidAssetLoader.ImageAsset loadTexture(String path) {
        return AndroidAssetLoader.load(path);
    }
}
