package animation.bone;
import graphics.*;

import java.awt.*;
import java.util.*;

/**
 * Manages quadruped (4-legged animal) skeletal animation systems.
 * Supports various animal types: wolf, horse, pig, cow, etc.
 *
 * ============================================================================
 * QUADRUPED BONE HIERARCHY (17 bones + 1 invisible root):
 * ============================================================================
 *
 * root (invisible container)
 * └── body              [1]  - Main body/torso
 *     ├── neck          [2]  - Connects head to body
 *     │   └── head      [3]  - Animal's head
 *     │       ├── ear_left  [4]  - Left ear
 *     │       └── ear_right [5]  - Right ear
 *     ├── tail_base     [6]  - Tail attachment
 *     │   └── tail_tip  [7]  - Tail end
 *     ├── leg_front_left_upper  [8]  - Front left upper leg
 *     │   └── leg_front_left_lower [9]  - Front left lower leg
 *     │       └── paw_front_left [10] - Front left paw/hoof
 *     ├── leg_front_right_upper [11] - Front right upper leg
 *     │   └── leg_front_right_lower [12] - Front right lower leg
 *     │       └── paw_front_right [13] - Front right paw/hoof
 *     ├── leg_back_left_upper [14] - Back left upper leg
 *     │   └── leg_back_left_lower [15] - Back left lower leg
 *     │       └── paw_back_left [16] - Back left paw/hoof
 *     └── leg_back_right_upper [17] - Back right upper leg
 *         └── leg_back_right_lower [18] - Back right lower leg
 *             └── paw_back_right [19] - Back right paw/hoof
 *
 * ============================================================================
 * BLOCKBENCH ANIMATION IMPORT - BONE NAME MAPPINGS:
 * ============================================================================
 *
 * CORE BONES:
 *   - body     (or: Body, torso, Torso, spine)
 *   - neck     (or: Neck, neck_bone)
 *   - head     (or: Head)
 *   - ear_left (or: leftEar, LeftEar, left_ear)
 *   - ear_right (or: rightEar, RightEar, right_ear)
 *
 * TAIL:
 *   - tail_base (or: tail, Tail, tail1)
 *   - tail_tip  (or: tail2, tailTip, TailTip)
 *
 * FRONT LEGS:
 *   - leg_front_left_upper  (or: frontLeftLeg, FrontLeftLeg, front_left_leg)
 *   - leg_front_left_lower  (or: frontLeftForeleg, front_left_lower)
 *   - paw_front_left        (or: frontLeftPaw, front_left_paw)
 *
 * BACK LEGS:
 *   - leg_back_left_upper   (or: backLeftLeg, BackLeftLeg, back_left_leg)
 *   - leg_back_left_lower   (or: backLeftShin, back_left_lower)
 *   - paw_back_left         (or: backLeftPaw, back_left_paw)
 *
 * (Similar mappings for right legs)
 *
 * ============================================================================
 */
public class QuadrupedSkeleton {

    /**
     * Animal type enumeration for different quadruped configurations.
     */
    public enum AnimalType {
        WOLF,       // Medium-sized predator with pointed ears
        DOG,        // Similar to wolf but friendlier appearance
        CAT,        // Small feline with pointed ears
        HORSE,      // Large equine with long legs
        PIG,        // Stocky body, short legs
        COW,        // Large bovine
        SHEEP,      // Fluffy body
        DEER,       // Slender with long legs
        BEAR,       // Large, heavy body
        FOX         // Small canine with bushy tail
    }

    // Animal-specific size configurations
    private static final Map<AnimalType, AnimalConfig> ANIMAL_CONFIGS = new HashMap<>();

    static {
        // Initialize animal configurations
        ANIMAL_CONFIGS.put(AnimalType.WOLF, new AnimalConfig(
            1.0, 1.0,           // Body scale
            0.8, 0.9,           // Head scale
            1.2,                // Leg length multiplier
            0.8,                // Tail length
            new Color(100, 100, 110),   // Primary color (gray)
            new Color(80, 80, 90),      // Secondary color (darker gray)
            new Color(60, 60, 65)       // Accent color (dark)
        ));

        ANIMAL_CONFIGS.put(AnimalType.DOG, new AnimalConfig(
            0.9, 0.9,
            0.85, 0.95,
            1.0,
            0.6,
            new Color(180, 140, 100),   // Brown
            new Color(150, 110, 70),
            new Color(120, 80, 50)
        ));

        ANIMAL_CONFIGS.put(AnimalType.CAT, new AnimalConfig(
            0.7, 0.7,
            0.9, 1.0,
            0.8,
            1.0,
            new Color(255, 180, 100),   // Orange tabby
            new Color(220, 150, 80),
            new Color(60, 40, 30)
        ));

        ANIMAL_CONFIGS.put(AnimalType.HORSE, new AnimalConfig(
            1.5, 1.3,
            0.7, 0.8,
            1.8,
            1.2,
            new Color(140, 100, 70),    // Brown
            new Color(110, 75, 50),
            new Color(80, 50, 30)
        ));

        ANIMAL_CONFIGS.put(AnimalType.PIG, new AnimalConfig(
            1.2, 0.9,
            0.9, 0.9,
            0.6,
            0.3,
            new Color(255, 180, 180),   // Pink
            new Color(230, 150, 150),
            new Color(200, 120, 120)
        ));

        ANIMAL_CONFIGS.put(AnimalType.COW, new AnimalConfig(
            1.6, 1.2,
            0.8, 0.9,
            1.0,
            0.5,
            new Color(240, 240, 240),   // White with spots
            new Color(60, 50, 40),      // Brown spots
            new Color(200, 200, 200)
        ));

        ANIMAL_CONFIGS.put(AnimalType.SHEEP, new AnimalConfig(
            1.1, 1.1,
            0.7, 0.8,
            0.7,
            0.2,
            new Color(250, 250, 250),   // White wool
            new Color(220, 220, 220),
            new Color(80, 60, 50)       // Dark face
        ));

        ANIMAL_CONFIGS.put(AnimalType.DEER, new AnimalConfig(
            1.0, 1.0,
            0.75, 0.85,
            1.5,
            0.3,
            new Color(180, 140, 100),   // Tan
            new Color(150, 110, 70),
            new Color(250, 240, 230)    // White underbelly
        ));

        ANIMAL_CONFIGS.put(AnimalType.BEAR, new AnimalConfig(
            1.6, 1.4,
            1.0, 1.0,
            0.9,
            0.2,
            new Color(100, 70, 50),     // Dark brown
            new Color(70, 50, 35),
            new Color(60, 40, 30)
        ));

        ANIMAL_CONFIGS.put(AnimalType.FOX, new AnimalConfig(
            0.8, 0.75,
            0.85, 0.95,
            1.0,
            1.1,
            new Color(220, 130, 50),    // Orange
            new Color(250, 250, 250),   // White
            new Color(40, 30, 25)       // Black
        ));
    }

    /**
     * Configuration data for different animal types.
     */
    public static class AnimalConfig {
        public double bodyScaleX, bodyScaleY;
        public double headScaleX, headScaleY;
        public double legLengthMultiplier;
        public double tailLength;
        public Color primaryColor;
        public Color secondaryColor;
        public Color accentColor;

        public AnimalConfig(double bodyScaleX, double bodyScaleY,
                           double headScaleX, double headScaleY,
                           double legLengthMultiplier, double tailLength,
                           Color primaryColor, Color secondaryColor, Color accentColor) {
            this.bodyScaleX = bodyScaleX;
            this.bodyScaleY = bodyScaleY;
            this.headScaleX = headScaleX;
            this.headScaleY = headScaleY;
            this.legLengthMultiplier = legLengthMultiplier;
            this.tailLength = tailLength;
            this.primaryColor = primaryColor;
            this.secondaryColor = secondaryColor;
            this.accentColor = accentColor;
        }
    }

    /**
     * Creates a quadruped skeleton for the specified animal type.
     *
     * @param type The type of animal to create
     * @return A new quadruped skeleton
     */
    public static Skeleton createQuadruped(AnimalType type) {
        AnimalConfig config = ANIMAL_CONFIGS.getOrDefault(type, ANIMAL_CONFIGS.get(AnimalType.WOLF));
        return createQuadrupedWithConfig(config);
    }

    /**
     * Creates a quadruped skeleton with custom configuration.
     *
     * @param config The animal configuration
     * @return A new quadruped skeleton
     */
    public static Skeleton createQuadrupedWithConfig(AnimalConfig config) {
        Skeleton skeleton = new Skeleton();

        // ====== CREATE ALL BONES ======

        // Root container (invisible)
        Bone root = new Bone("root");
        root.setVisible(false);

        // Core bones
        Bone body = new Bone("body");
        Bone neck = new Bone("neck");
        Bone head = new Bone("head");
        Bone earLeft = new Bone("ear_left");
        Bone earRight = new Bone("ear_right");

        // Tail
        Bone tailBase = new Bone("tail_base");
        Bone tailTip = new Bone("tail_tip");

        // Front legs
        Bone legFrontLeftUpper = new Bone("leg_front_left_upper");
        Bone legFrontLeftLower = new Bone("leg_front_left_lower");
        Bone pawFrontLeft = new Bone("paw_front_left");

        Bone legFrontRightUpper = new Bone("leg_front_right_upper");
        Bone legFrontRightLower = new Bone("leg_front_right_lower");
        Bone pawFrontRight = new Bone("paw_front_right");

        // Back legs
        Bone legBackLeftUpper = new Bone("leg_back_left_upper");
        Bone legBackLeftLower = new Bone("leg_back_left_lower");
        Bone pawBackLeft = new Bone("paw_back_left");

        Bone legBackRightUpper = new Bone("leg_back_right_upper");
        Bone legBackRightLower = new Bone("leg_back_right_lower");
        Bone pawBackRight = new Bone("paw_back_right");

        // ====== SET UP BONE HIERARCHY ======

        root.addChild(body);

        // Head chain
        body.addChild(neck);
        neck.addChild(head);
        head.addChild(earLeft);
        head.addChild(earRight);

        // Tail
        body.addChild(tailBase);
        tailBase.addChild(tailTip);

        // Front legs
        body.addChild(legFrontLeftUpper);
        legFrontLeftUpper.addChild(legFrontLeftLower);
        legFrontLeftLower.addChild(pawFrontLeft);

        body.addChild(legFrontRightUpper);
        legFrontRightUpper.addChild(legFrontRightLower);
        legFrontRightLower.addChild(pawFrontRight);

        // Back legs
        body.addChild(legBackLeftUpper);
        legBackLeftUpper.addChild(legBackLeftLower);
        legBackLeftLower.addChild(pawBackLeft);

        body.addChild(legBackRightUpper);
        legBackRightUpper.addChild(legBackRightLower);
        legBackRightLower.addChild(pawBackRight);

        // ====== SET LOCAL POSITIONS ======
        // Base sizes (will be multiplied by config values)
        int bodyWidth = (int)(48 * config.bodyScaleX);
        int bodyHeight = (int)(24 * config.bodyScaleY);
        int legLength = (int)(16 * config.legLengthMultiplier);
        int tailLen = (int)(20 * config.tailLength);

        body.setLocalPosition(0, 0);  // Anchor point

        // Neck and head - at front of body, positioned for profile view
        // Neck attaches at front-top of body
        neck.setLocalPosition(-bodyWidth/2 - 2, -bodyHeight/4);
        // Head extends forward (left in profile) from neck - horizontal orientation
        // Negative X makes it extend forward, small Y offset for natural look
        head.setLocalPosition(-14, -4);

        // Ears on top of head (adjusted for horizontal head)
        earLeft.setLocalPosition(2, -12);
        earRight.setLocalPosition(6, -12);

        // Tail - at back of body
        tailBase.setLocalPosition(bodyWidth/2 - 2, -2);
        tailTip.setLocalPosition(tailLen/2, 0);

        // Leg offset for visual separation (right legs slightly forward in side view)
        int legSeparation = 8;

        // Front legs - moved outward toward front edge of body
        int frontLegX = -bodyWidth/2 + 8;  // Closer to front edge
        int backLegX = bodyWidth/2 - 8;    // Closer to back edge
        int legAttachY = bodyHeight/2;     // Bottom of body

        // Left leg (far side, behind)
        legFrontLeftUpper.setLocalPosition(frontLegX + legSeparation/2, legAttachY);
        legFrontLeftLower.setLocalPosition(0, legLength);
        pawFrontLeft.setLocalPosition(0, legLength);

        // Right leg (near side, in front) - offset slightly toward viewer
        legFrontRightUpper.setLocalPosition(frontLegX - legSeparation/2, legAttachY);
        legFrontRightLower.setLocalPosition(0, legLength);
        pawFrontRight.setLocalPosition(0, legLength);

        // Back legs - moved outward toward back edge of body
        // Left leg (far side, behind)
        legBackLeftUpper.setLocalPosition(backLegX + legSeparation/2, legAttachY);
        legBackLeftLower.setLocalPosition(0, legLength);
        pawBackLeft.setLocalPosition(0, legLength);

        // Right leg (near side, in front) - offset slightly toward viewer
        legBackRightUpper.setLocalPosition(backLegX - legSeparation/2, legAttachY);
        legBackRightLower.setLocalPosition(0, legLength);
        pawBackRight.setLocalPosition(0, legLength);

        // ====== SET PIVOT POINTS ======
        body.setPivot(0.5, 0.5);
        neck.setPivot(0.5, 1.0);    // Pivot at base
        head.setPivot(1.0, 0.5);    // Pivot at right side (neck connection) - head extends left

        earLeft.setPivot(0.5, 1.0);
        earRight.setPivot(0.5, 1.0);

        tailBase.setPivot(0.0, 0.5); // Pivot at body connection
        tailTip.setPivot(0.0, 0.5);

        // Legs pivot at top (hip/shoulder/knee/ankle)
        legFrontLeftUpper.setPivot(0.5, 0.0);
        legFrontLeftLower.setPivot(0.5, 0.0);
        pawFrontLeft.setPivot(0.5, 0.0);

        legFrontRightUpper.setPivot(0.5, 0.0);
        legFrontRightLower.setPivot(0.5, 0.0);
        pawFrontRight.setPivot(0.5, 0.0);

        legBackLeftUpper.setPivot(0.5, 0.0);
        legBackLeftLower.setPivot(0.5, 0.0);
        pawBackLeft.setPivot(0.5, 0.0);

        legBackRightUpper.setPivot(0.5, 0.0);
        legBackRightLower.setPivot(0.5, 0.0);
        pawBackRight.setPivot(0.5, 0.0);

        // ====== SET Z-ORDER ======
        // Back legs behind body
        legBackLeftUpper.setZOrder(-3);
        legBackLeftLower.setZOrder(-3);
        pawBackLeft.setZOrder(-3);

        legFrontLeftUpper.setZOrder(-2);
        legFrontLeftLower.setZOrder(-2);
        pawFrontLeft.setZOrder(-2);

        // Body in middle
        body.setZOrder(0);
        tailBase.setZOrder(-1);
        tailTip.setZOrder(-1);

        // Front right leg and head in front
        legBackRightUpper.setZOrder(1);
        legBackRightLower.setZOrder(1);
        pawBackRight.setZOrder(1);

        legFrontRightUpper.setZOrder(2);
        legFrontRightLower.setZOrder(2);
        pawFrontRight.setZOrder(2);

        neck.setZOrder(1);
        head.setZOrder(2);
        earLeft.setZOrder(1);
        earRight.setZOrder(3);

        // ====== SET DEFAULT SIZES ======
        body.setDefaultSize(bodyWidth, bodyHeight);
        neck.setDefaultSize(10, 12);
        // Head is horizontal in profile view - width > height
        // Dimensions swapped so face extends forward
        head.setDefaultSize((int)(24 * config.headScaleX), (int)(18 * config.headScaleY));
        earLeft.setDefaultSize(6, 10);
        earRight.setDefaultSize(6, 10);

        tailBase.setDefaultSize(tailLen/2, 6);
        tailTip.setDefaultSize(tailLen/2, 4);

        int legWidth = 8;
        legFrontLeftUpper.setDefaultSize(legWidth, legLength);
        legFrontLeftLower.setDefaultSize(legWidth - 2, legLength);
        pawFrontLeft.setDefaultSize(legWidth, 6);

        legFrontRightUpper.setDefaultSize(legWidth, legLength);
        legFrontRightLower.setDefaultSize(legWidth - 2, legLength);
        pawFrontRight.setDefaultSize(legWidth, 6);

        legBackLeftUpper.setDefaultSize(legWidth + 2, legLength);
        legBackLeftLower.setDefaultSize(legWidth, legLength);
        pawBackLeft.setDefaultSize(legWidth, 6);

        legBackRightUpper.setDefaultSize(legWidth + 2, legLength);
        legBackRightLower.setDefaultSize(legWidth, legLength);
        pawBackRight.setDefaultSize(legWidth, 6);

        // ====== SET PLACEHOLDER COLORS ======
        body.setPlaceholderColor(config.primaryColor);
        neck.setPlaceholderColor(config.primaryColor);
        head.setPlaceholderColor(config.primaryColor);
        earLeft.setPlaceholderColor(config.secondaryColor);
        earRight.setPlaceholderColor(config.secondaryColor);

        tailBase.setPlaceholderColor(config.primaryColor);
        tailTip.setPlaceholderColor(config.secondaryColor);

        legFrontLeftUpper.setPlaceholderColor(config.primaryColor);
        legFrontLeftLower.setPlaceholderColor(config.primaryColor);
        pawFrontLeft.setPlaceholderColor(config.accentColor);

        legFrontRightUpper.setPlaceholderColor(config.primaryColor);
        legFrontRightLower.setPlaceholderColor(config.primaryColor);
        pawFrontRight.setPlaceholderColor(config.accentColor);

        legBackLeftUpper.setPlaceholderColor(config.primaryColor);
        legBackLeftLower.setPlaceholderColor(config.primaryColor);
        pawBackLeft.setPlaceholderColor(config.accentColor);

        legBackRightUpper.setPlaceholderColor(config.primaryColor);
        legBackRightLower.setPlaceholderColor(config.primaryColor);
        pawBackRight.setPlaceholderColor(config.accentColor);

        skeleton.setRootBone(root);
        skeleton.storeRestPositions();

        System.out.println("Quadruped skeleton created with 19 bones");
        return skeleton;
    }

    /**
     * Creates a quadruped skeleton with textures from a directory.
     *
     * @param type       The animal type
     * @param textureDir Directory containing texture files
     * @return A textured quadruped skeleton
     */
    public static Skeleton createQuadrupedWithTextures(AnimalType type, String textureDir) {
        Skeleton skeleton = createQuadruped(type);

        String[] boneNames = getQuadrupedBoneNames();
        for (String name : boneNames) {
            Bone bone = skeleton.findBone(name);
            if (bone != null) {
                String path = textureDir + "/" + name + ".png";
                bone.loadTexture(path);
            }
        }

        return skeleton;
    }

    /**
     * Gets all bone names for quadruped skeletons.
     *
     * @return Array of bone names
     */
    public static String[] getQuadrupedBoneNames() {
        return new String[] {
            "body", "neck", "head", "ear_left", "ear_right",
            "tail_base", "tail_tip",
            "leg_front_left_upper", "leg_front_left_lower", "paw_front_left",
            "leg_front_right_upper", "leg_front_right_lower", "paw_front_right",
            "leg_back_left_upper", "leg_back_left_lower", "paw_back_left",
            "leg_back_right_upper", "leg_back_right_lower", "paw_back_right"
        };
    }

    /**
     * Gets the animal configuration for a given type.
     *
     * @param type The animal type
     * @return The configuration, or wolf config if not found
     */
    public static AnimalConfig getConfig(AnimalType type) {
        return ANIMAL_CONFIGS.getOrDefault(type, ANIMAL_CONFIGS.get(AnimalType.WOLF));
    }
}
