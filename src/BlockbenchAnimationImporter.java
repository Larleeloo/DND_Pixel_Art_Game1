import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * Imports animations from Blockbench JSON export format.
 *
 * Blockbench is a popular 3D modeling tool that can export animations in JSON format.
 * This class parses the animation data and converts it to BoneAnimation objects.
 *
 * Supported Blockbench export format:
 * - .json files exported from Blockbench with animations
 * - Supports position, rotation, and scale keyframes
 * - Handles both simple keyframes and easing curves
 *
 * Usage:
 *   BlockbenchAnimationImporter importer = new BlockbenchAnimationImporter();
 *   List<BoneAnimation> animations = importer.importFile("path/to/model.animation.json");
 *   for (BoneAnimation anim : animations) {
 *       skeleton.addAnimation(anim);
 *   }
 */
public class BlockbenchAnimationImporter {

    // Bone name mapping from Blockbench names to our skeleton bone names
    private Map<String, String> boneNameMapping;

    /**
     * Creates a new importer with default bone name mappings.
     */
    public BlockbenchAnimationImporter() {
        boneNameMapping = new HashMap<>();
        setupDefaultMappings();
    }

    /**
     * Sets up default bone name mappings from common Blockbench naming conventions
     * to our skeleton bone names.
     */
    private void setupDefaultMappings() {
        // Common mappings - Blockbench uses various naming conventions
        // Torso/Body
        boneNameMapping.put("body", "torso");
        boneNameMapping.put("Body", "torso");
        boneNameMapping.put("chest", "torso");
        boneNameMapping.put("Chest", "torso");
        boneNameMapping.put("torso", "torso");
        boneNameMapping.put("Torso", "torso");

        // Head
        boneNameMapping.put("head", "head");
        boneNameMapping.put("Head", "head");

        // Left arm (2-part)
        boneNameMapping.put("leftArm", "arm_upper_left");
        boneNameMapping.put("LeftArm", "arm_upper_left");
        boneNameMapping.put("left_arm", "arm_upper_left");
        boneNameMapping.put("arm_left", "arm_upper_left");
        boneNameMapping.put("ArmLeft", "arm_upper_left");
        boneNameMapping.put("leftForearm", "arm_lower_left");
        boneNameMapping.put("LeftForearm", "arm_lower_left");
        boneNameMapping.put("left_forearm", "arm_lower_left");
        boneNameMapping.put("forearm_left", "arm_lower_left");
        boneNameMapping.put("leftLowerArm", "arm_lower_left");

        // Right arm (2-part)
        boneNameMapping.put("rightArm", "arm_upper_right");
        boneNameMapping.put("RightArm", "arm_upper_right");
        boneNameMapping.put("right_arm", "arm_upper_right");
        boneNameMapping.put("arm_right", "arm_upper_right");
        boneNameMapping.put("ArmRight", "arm_upper_right");
        boneNameMapping.put("rightForearm", "arm_lower_right");
        boneNameMapping.put("RightForearm", "arm_lower_right");
        boneNameMapping.put("right_forearm", "arm_lower_right");
        boneNameMapping.put("forearm_right", "arm_lower_right");
        boneNameMapping.put("rightLowerArm", "arm_lower_right");

        // Left leg (2-part)
        boneNameMapping.put("leftLeg", "leg_upper_left");
        boneNameMapping.put("LeftLeg", "leg_upper_left");
        boneNameMapping.put("left_leg", "leg_upper_left");
        boneNameMapping.put("leg_left", "leg_upper_left");
        boneNameMapping.put("LegLeft", "leg_upper_left");
        boneNameMapping.put("leftThigh", "leg_upper_left");
        boneNameMapping.put("leftCalf", "leg_lower_left");
        boneNameMapping.put("LeftCalf", "leg_lower_left");
        boneNameMapping.put("left_calf", "leg_lower_left");
        boneNameMapping.put("calf_left", "leg_lower_left");
        boneNameMapping.put("leftLowerLeg", "leg_lower_left");
        boneNameMapping.put("leftShin", "leg_lower_left");

        // Right leg (2-part)
        boneNameMapping.put("rightLeg", "leg_upper_right");
        boneNameMapping.put("RightLeg", "leg_upper_right");
        boneNameMapping.put("right_leg", "leg_upper_right");
        boneNameMapping.put("leg_right", "leg_upper_right");
        boneNameMapping.put("LegRight", "leg_upper_right");
        boneNameMapping.put("rightThigh", "leg_upper_right");
        boneNameMapping.put("rightCalf", "leg_lower_right");
        boneNameMapping.put("RightCalf", "leg_lower_right");
        boneNameMapping.put("right_calf", "leg_lower_right");
        boneNameMapping.put("calf_right", "leg_lower_right");
        boneNameMapping.put("rightLowerLeg", "leg_lower_right");
        boneNameMapping.put("rightShin", "leg_lower_right");

        // Direct mappings for our bone names
        boneNameMapping.put("arm_upper_left", "arm_upper_left");
        boneNameMapping.put("arm_lower_left", "arm_lower_left");
        boneNameMapping.put("arm_upper_right", "arm_upper_right");
        boneNameMapping.put("arm_lower_right", "arm_lower_right");
        boneNameMapping.put("leg_upper_left", "leg_upper_left");
        boneNameMapping.put("leg_lower_left", "leg_lower_left");
        boneNameMapping.put("leg_upper_right", "leg_upper_right");
        boneNameMapping.put("leg_lower_right", "leg_lower_right");
    }

    /**
     * Adds or overrides a bone name mapping.
     * @param blockbenchName The name used in Blockbench
     * @param skeletonName The name used in our Skeleton
     */
    public void addBoneMapping(String blockbenchName, String skeletonName) {
        boneNameMapping.put(blockbenchName, skeletonName);
    }

    /**
     * Maps a Blockbench bone name to our skeleton bone name.
     * @param blockbenchName The name from Blockbench
     * @return The mapped name, or the original if no mapping exists
     */
    private String mapBoneName(String blockbenchName) {
        String mapped = boneNameMapping.get(blockbenchName);
        return mapped != null ? mapped : blockbenchName;
    }

    /**
     * Imports all animations from a Blockbench JSON file.
     * @param filePath Path to the .json file
     * @return List of BoneAnimation objects
     */
    public List<BoneAnimation> importFile(String filePath) {
        List<BoneAnimation> animations = new ArrayList<>();

        try {
            String json = readFile(filePath);
            animations = parseAnimations(json);
            System.out.println("BlockbenchAnimationImporter: Loaded " + animations.size() +
                             " animations from " + filePath);
        } catch (Exception e) {
            System.err.println("BlockbenchAnimationImporter: Error loading " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }

        return animations;
    }

    /**
     * Imports animations from a JSON string.
     * @param json The JSON content
     * @return List of BoneAnimation objects
     */
    public List<BoneAnimation> importFromString(String json) {
        return parseAnimations(json);
    }

    /**
     * Reads a file into a string.
     */
    private String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * Parses animations from Blockbench JSON format.
     * Blockbench format structure:
     * {
     *   "animations": {
     *     "animation.model.walk": {
     *       "loop": true,
     *       "animation_length": 1.0,
     *       "bones": {
     *         "body": {
     *           "rotation": {
     *             "0.0": [0, 0, 0],
     *             "0.5": [5, 0, 0]
     *           },
     *           "position": { ... },
     *           "scale": { ... }
     *         }
     *       }
     *     }
     *   }
     * }
     */
    private List<BoneAnimation> parseAnimations(String json) {
        List<BoneAnimation> animations = new ArrayList<>();

        // Find the animations object
        String animationsBlock = extractBlock(json, "\"animations\"");
        if (animationsBlock == null) {
            // Try alternative format where animations are at root level
            animationsBlock = json;
        }

        // Find each animation
        Pattern animPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{");
        Matcher animMatcher = animPattern.matcher(animationsBlock);

        int searchStart = 0;
        while (animMatcher.find(searchStart)) {
            String animName = animMatcher.group(1);

            // Skip non-animation keys
            if (animName.equals("animations") || animName.equals("bones") ||
                animName.equals("rotation") || animName.equals("position") ||
                animName.equals("scale") || animName.equals("format_version") ||
                animName.equals("geometry") || animName.equals("textures")) {
                searchStart = animMatcher.end();
                continue;
            }

            // Extract the animation block
            int blockStart = animMatcher.end() - 1; // Include the {
            String animBlock = extractBracedContent(animationsBlock, blockStart);

            if (animBlock != null) {
                BoneAnimation anim = parseAnimation(animName, animBlock);
                if (anim != null) {
                    animations.add(anim);
                }
            }

            searchStart = animMatcher.end();
        }

        return animations;
    }

    /**
     * Parses a single animation from its JSON block.
     */
    private BoneAnimation parseAnimation(String fullName, String animBlock) {
        // Extract animation name (remove prefix like "animation.model.")
        String name = fullName;
        if (fullName.contains(".")) {
            String[] parts = fullName.split("\\.");
            name = parts[parts.length - 1];
        }

        // Get loop setting
        boolean loop = animBlock.contains("\"loop\"") &&
                       (animBlock.contains("\"loop\": true") || animBlock.contains("\"loop\":true"));

        // Get animation length
        double duration = 1.0;
        Pattern lengthPattern = Pattern.compile("\"animation_length\"\\s*:\\s*([\\d.]+)");
        Matcher lengthMatcher = lengthPattern.matcher(animBlock);
        if (lengthMatcher.find()) {
            try {
                duration = Double.parseDouble(lengthMatcher.group(1));
            } catch (NumberFormatException e) {
                // Use default
            }
        }

        BoneAnimation animation = new BoneAnimation(name, duration, loop);

        // Parse bones
        String bonesBlock = extractBlock(animBlock, "\"bones\"");
        if (bonesBlock != null) {
            parseBones(bonesBlock, animation);
        }

        System.out.println("BlockbenchAnimationImporter: Parsed animation '" + name +
                         "' (duration=" + duration + "s, loop=" + loop + ")");

        return animation;
    }

    /**
     * Parses bone keyframes from the bones block.
     */
    private void parseBones(String bonesBlock, BoneAnimation animation) {
        // Find each bone
        Pattern bonePattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\{");
        Matcher boneMatcher = bonePattern.matcher(bonesBlock);

        int searchStart = 0;
        while (boneMatcher.find(searchStart)) {
            String blockbenchBoneName = boneMatcher.group(1);

            // Skip property names
            if (blockbenchBoneName.equals("bones") || blockbenchBoneName.equals("rotation") ||
                blockbenchBoneName.equals("position") || blockbenchBoneName.equals("scale")) {
                searchStart = boneMatcher.end();
                continue;
            }

            // Map to our bone name
            String boneName = mapBoneName(blockbenchBoneName);

            // Extract bone block
            int blockStart = boneMatcher.end() - 1;
            String boneBlock = extractBracedContent(bonesBlock, blockStart);

            if (boneBlock != null) {
                parseBoneKeyframes(boneName, boneBlock, animation);
            }

            searchStart = boneMatcher.end();
        }
    }

    /**
     * Parses keyframes for a single bone.
     */
    private void parseBoneKeyframes(String boneName, String boneBlock, BoneAnimation animation) {
        // Collect all keyframe times and their values
        Map<Double, double[]> positionKeyframes = new HashMap<>();
        Map<Double, double[]> rotationKeyframes = new HashMap<>();
        Map<Double, double[]> scaleKeyframes = new HashMap<>();

        // Parse position keyframes
        String posBlock = extractBlock(boneBlock, "\"position\"");
        if (posBlock != null) {
            positionKeyframes = parseKeyframeValues(posBlock);
        }

        // Parse rotation keyframes
        String rotBlock = extractBlock(boneBlock, "\"rotation\"");
        if (rotBlock != null) {
            rotationKeyframes = parseKeyframeValues(rotBlock);
        }

        // Parse scale keyframes
        String scaleBlock = extractBlock(boneBlock, "\"scale\"");
        if (scaleBlock != null) {
            scaleKeyframes = parseKeyframeValues(scaleBlock);
        }

        // Collect all unique times
        Set<Double> allTimes = new TreeSet<>();
        allTimes.addAll(positionKeyframes.keySet());
        allTimes.addAll(rotationKeyframes.keySet());
        allTimes.addAll(scaleKeyframes.keySet());

        // Create keyframes for each time
        for (double time : allTimes) {
            double[] pos = positionKeyframes.getOrDefault(time, new double[]{0, 0, 0});
            double[] rot = rotationKeyframes.getOrDefault(time, new double[]{0, 0, 0});
            double[] scale = scaleKeyframes.getOrDefault(time, new double[]{1, 1, 1});

            // Blockbench uses [x, y, z] format
            // Our system uses 2D (x, y) and rotation around Z axis
            // Convert: use X for localX, Y for localY, Z rotation for rotation
            double localX = pos[0];
            double localY = -pos[1];  // Invert Y (Blockbench uses different Y direction)
            double rotation = rot[2];  // Z rotation becomes our 2D rotation
            double scaleX = scale[0];
            double scaleY = scale[1];

            // Handle case where scale is uniform (single value)
            if (scale.length == 1) {
                scaleX = scale[0];
                scaleY = scale[0];
            }

            animation.addKeyframe(boneName, new BoneAnimation.Keyframe(
                time, localX, localY, rotation, scaleX, scaleY
            ));
        }
    }

    /**
     * Parses keyframe time-value pairs from a block.
     * Format: "0.0": [x, y, z] or "0.0": {"vector": [x, y, z]}
     */
    private Map<Double, double[]> parseKeyframeValues(String block) {
        Map<Double, double[]> keyframes = new HashMap<>();

        // Pattern for time: [values] format
        Pattern simplePattern = Pattern.compile("\"([\\d.]+)\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher simpleMatcher = simplePattern.matcher(block);

        while (simpleMatcher.find()) {
            try {
                double time = Double.parseDouble(simpleMatcher.group(1));
                String valuesStr = simpleMatcher.group(2);
                double[] values = parseValues(valuesStr);
                keyframes.put(time, values);
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }

        // Also handle single value format: "0.0": 5.0
        Pattern singlePattern = Pattern.compile("\"([\\d.]+)\"\\s*:\\s*(-?[\\d.]+)(?![\\d.])");
        Matcher singleMatcher = singlePattern.matcher(block);

        while (singleMatcher.find()) {
            try {
                double time = Double.parseDouble(singleMatcher.group(1));
                double value = Double.parseDouble(singleMatcher.group(2));
                keyframes.put(time, new double[]{value, value, value});
            } catch (NumberFormatException e) {
                // Skip invalid entries
            }
        }

        return keyframes;
    }

    /**
     * Parses comma-separated values into an array.
     */
    private double[] parseValues(String valuesStr) {
        String[] parts = valuesStr.split(",");
        double[] values = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                values[i] = Double.parseDouble(parts[i].trim());
            } catch (NumberFormatException e) {
                values[i] = 0;
            }
        }
        return values;
    }

    /**
     * Extracts a JSON block that starts after a key.
     */
    private String extractBlock(String json, String key) {
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) return null;

        // Find the opening brace after the key
        int braceIndex = json.indexOf('{', keyIndex + key.length());
        if (braceIndex < 0) return null;

        return extractBracedContent(json, braceIndex);
    }

    /**
     * Extracts content within braces, handling nesting.
     */
    private String extractBracedContent(String json, int start) {
        if (start < 0 || start >= json.length() || json.charAt(start) != '{') {
            return null;
        }

        int depth = 0;
        int end = start;

        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    end = i + 1;
                    break;
                }
            }
        }

        if (depth != 0) return null;
        return json.substring(start, end);
    }

    /**
     * Utility method to import a single animation by name from a file.
     * @param filePath Path to the JSON file
     * @param animationName Name of the animation to import
     * @return The animation, or null if not found
     */
    public BoneAnimation importAnimation(String filePath, String animationName) {
        List<BoneAnimation> animations = importFile(filePath);
        for (BoneAnimation anim : animations) {
            if (anim.getName().equals(animationName)) {
                return anim;
            }
        }
        return null;
    }

    /**
     * Creates a sample Blockbench animation JSON string for testing.
     * @return Sample JSON string
     */
    public static String createSampleAnimationJson() {
        return "{\n" +
               "  \"format_version\": \"1.8.0\",\n" +
               "  \"animations\": {\n" +
               "    \"animation.player.walk\": {\n" +
               "      \"loop\": true,\n" +
               "      \"animation_length\": 0.5,\n" +
               "      \"bones\": {\n" +
               "        \"leftLeg\": {\n" +
               "          \"rotation\": {\n" +
               "            \"0.0\": [30, 0, 0],\n" +
               "            \"0.25\": [-30, 0, 0],\n" +
               "            \"0.5\": [30, 0, 0]\n" +
               "          }\n" +
               "        },\n" +
               "        \"rightLeg\": {\n" +
               "          \"rotation\": {\n" +
               "            \"0.0\": [-30, 0, 0],\n" +
               "            \"0.25\": [30, 0, 0],\n" +
               "            \"0.5\": [-30, 0, 0]\n" +
               "          }\n" +
               "        },\n" +
               "        \"leftArm\": {\n" +
               "          \"rotation\": {\n" +
               "            \"0.0\": [-20, 0, 0],\n" +
               "            \"0.25\": [20, 0, 0],\n" +
               "            \"0.5\": [-20, 0, 0]\n" +
               "          }\n" +
               "        },\n" +
               "        \"rightArm\": {\n" +
               "          \"rotation\": {\n" +
               "            \"0.0\": [20, 0, 0],\n" +
               "            \"0.25\": [-20, 0, 0],\n" +
               "            \"0.5\": [20, 0, 0]\n" +
               "          }\n" +
               "        }\n" +
               "      }\n" +
               "    },\n" +
               "    \"animation.player.jump\": {\n" +
               "      \"loop\": false,\n" +
               "      \"animation_length\": 0.6,\n" +
               "      \"bones\": {\n" +
               "        \"body\": {\n" +
               "          \"position\": {\n" +
               "            \"0.0\": [0, 0, 0],\n" +
               "            \"0.2\": [0, -4, 0],\n" +
               "            \"0.4\": [0, 8, 0],\n" +
               "            \"0.6\": [0, 0, 0]\n" +
               "          }\n" +
               "        },\n" +
               "        \"leftArm\": {\n" +
               "          \"rotation\": {\n" +
               "            \"0.0\": [0, 0, 0],\n" +
               "            \"0.3\": [-60, 0, 0],\n" +
               "            \"0.6\": [0, 0, 0]\n" +
               "          }\n" +
               "        },\n" +
               "        \"rightArm\": {\n" +
               "          \"rotation\": {\n" +
               "            \"0.0\": [0, 0, 0],\n" +
               "            \"0.3\": [-60, 0, 0],\n" +
               "            \"0.6\": [0, 0, 0]\n" +
               "          }\n" +
               "        }\n" +
               "      }\n" +
               "    }\n" +
               "  }\n" +
               "}";
    }

    /**
     * Main method for testing the importer.
     */
    public static void main(String[] args) {
        BlockbenchAnimationImporter importer = new BlockbenchAnimationImporter();

        // Test with sample JSON
        String sampleJson = createSampleAnimationJson();
        System.out.println("Testing Blockbench Animation Importer");
        System.out.println("======================================");
        System.out.println("Sample JSON:\n" + sampleJson);
        System.out.println("\nParsing animations...\n");

        List<BoneAnimation> animations = importer.importFromString(sampleJson);

        System.out.println("\nImported " + animations.size() + " animations:");
        for (BoneAnimation anim : animations) {
            System.out.println("  - " + anim.getName() +
                             " (duration=" + anim.getDuration() + "s, loop=" + anim.isLooping() + ")");
            System.out.println("    Animated bones: " + anim.getAnimatedBoneNames());
        }
    }
}
