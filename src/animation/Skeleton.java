package animation;
import graphics.*;

import java.awt.*;
import java.util.*;

/**
 * Manages a hierarchical skeleton of bones for character animation.
 * Handles bone organization, animation playback, and rendering.
 */
public class Skeleton {

    private Bone rootBone;
    private Map<String, Bone> boneMap;  // Quick lookup by name

    // Rest positions - the default pose without any animation
    // Animation keyframes are applied as OFFSETS to these positions
    private Map<String, double[]> restPositions;  // boneName -> [x, y, rotation]

    // Animation management
    private Map<String, BoneAnimation> animations;
    private BoneAnimation currentAnimation;
    private BoneAnimation nextAnimation;        // For blending
    private double blendTime = 0;
    private double blendDuration = 0.2;         // 0.2 second blend by default

    // Transform
    private double x;
    private double y;
    private double scale = 1.0;
    private boolean flipX = false;              // Mirror horizontally (for facing direction)

    // Rendering options
    private boolean debugDraw = false;

    /**
     * Creates an empty skeleton.
     */
    public Skeleton() {
        this.boneMap = new HashMap<>();
        this.restPositions = new HashMap<>();
        this.animations = new HashMap<>();
    }

    /**
     * Creates a skeleton with a root bone.
     * @param rootBone The root bone of the skeleton
     */
    public Skeleton(Bone rootBone) {
        this();
        setRootBone(rootBone);
    }

    /**
     * Sets the root bone and rebuilds the bone map.
     * @param rootBone The root bone
     */
    public void setRootBone(Bone rootBone) {
        this.rootBone = rootBone;
        rebuildBoneMap();
    }

    /**
     * Gets the root bone.
     * @return Root bone
     */
    public Bone getRootBone() {
        return rootBone;
    }

    /**
     * Rebuilds the bone name lookup map.
     */
    private void rebuildBoneMap() {
        boneMap.clear();
        if (rootBone != null) {
            addBoneToMap(rootBone);
        }
    }

    private void addBoneToMap(Bone bone) {
        boneMap.put(bone.getName(), bone);
        for (Bone child : bone.getChildren()) {
            addBoneToMap(child);
        }
    }

    /**
     * Finds a bone by name.
     * @param name Bone name
     * @return The bone, or null if not found
     */
    public Bone findBone(String name) {
        return boneMap.get(name);
    }

    /**
     * Adds a bone as a child of an existing bone.
     * @param parentName Name of the parent bone
     * @param child The new child bone
     * @return True if successful, false if parent not found
     */
    public boolean addBone(String parentName, Bone child) {
        Bone parent = findBone(parentName);
        if (parent != null) {
            parent.addChild(child);
            boneMap.put(child.getName(), child);
            return true;
        }
        return false;
    }

    /**
     * Gets all bone names in the skeleton.
     * @return Set of bone names
     */
    public Set<String> getBoneNames() {
        return new HashSet<>(boneMap.keySet());
    }

    // ==================== Rest Position Management ====================

    /**
     * Stores the current bone positions as rest positions.
     * Call this AFTER setting up the bone hierarchy and initial positions.
     * Animation keyframes will be applied as OFFSETS to these rest positions.
     */
    public void storeRestPositions() {
        restPositions.clear();
        for (Map.Entry<String, Bone> entry : boneMap.entrySet()) {
            String name = entry.getKey();
            Bone bone = entry.getValue();
            restPositions.put(name, new double[] {
                bone.getLocalX(),
                bone.getLocalY(),
                bone.getRotation()
            });
        }
        System.out.println("Stored rest positions for " + restPositions.size() + " bones");
    }

    /**
     * Gets the rest position for a bone.
     * @param boneName Name of the bone
     * @return Array of [x, y, rotation] or null if not found
     */
    public double[] getRestPosition(String boneName) {
        return restPositions.get(boneName);
    }

    /**
     * Checks if rest positions have been stored.
     * @return True if rest positions are available
     */
    public boolean hasRestPositions() {
        return !restPositions.isEmpty();
    }

    // ==================== Position and Transform ====================

    /**
     * Sets the world position of the skeleton.
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the X position.
     * @return X coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the Y position.
     * @return Y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the scale factor for rendering.
     * @param scale Scale (1.0 = original size)
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * Gets the current scale.
     * @return Scale factor
     */
    public double getScale() {
        return scale;
    }

    /**
     * Sets whether to flip the skeleton horizontally.
     * Useful for changing facing direction.
     * @param flip True to flip
     */
    public void setFlipX(boolean flip) {
        this.flipX = flip;
    }

    /**
     * Checks if the skeleton is flipped horizontally.
     * @return True if flipped
     */
    public boolean isFlipX() {
        return flipX;
    }

    // ==================== Animation Management ====================

    /**
     * Adds an animation to this skeleton.
     * @param animation The animation to add
     */
    public void addAnimation(BoneAnimation animation) {
        animations.put(animation.getName(), animation);
    }

    /**
     * Gets an animation by name.
     * @param name Animation name
     * @return The animation, or null if not found
     */
    public BoneAnimation getAnimation(String name) {
        return animations.get(name);
    }

    /**
     * Gets all animation names.
     * @return Set of animation names
     */
    public Set<String> getAnimationNames() {
        return new HashSet<>(animations.keySet());
    }

    /**
     * Plays an animation immediately (no blending).
     * @param name Animation name
     */
    public void playAnimation(String name) {
        BoneAnimation anim = animations.get(name);
        if (anim != null) {
            currentAnimation = anim;
            currentAnimation.reset();
            nextAnimation = null;
            blendTime = 0;
        }
    }

    /**
     * Transitions to a new animation with blending.
     * @param name Animation name
     * @param blendDuration Duration of the blend in seconds
     */
    public void transitionTo(String name, double blendDuration) {
        BoneAnimation anim = animations.get(name);
        if (anim != null && anim != currentAnimation) {
            // Don't restart if we're already transitioning to this animation
            if (nextAnimation != null && nextAnimation.getName().equals(name)) {
                return; // Already blending to this animation
            }
            if (currentAnimation == null) {
                playAnimation(name);
            } else {
                nextAnimation = anim;
                nextAnimation.reset();
                this.blendDuration = blendDuration;
                this.blendTime = 0;
            }
        }
    }

    /**
     * Transitions to a new animation with default blend duration.
     * @param name Animation name
     */
    public void transitionTo(String name) {
        transitionTo(name, 0.2);
    }

    /**
     * Gets the currently playing animation.
     * @return Current animation, or null if none
     */
    public BoneAnimation getCurrentAnimation() {
        return currentAnimation;
    }

    /**
     * Checks if a specific animation is currently playing or being transitioned to.
     * @param name Animation name
     * @return True if that animation is playing or we're transitioning to it
     */
    public boolean isPlayingAnimation(String name) {
        // Check if it's the current animation
        if (currentAnimation != null && currentAnimation.getName().equals(name)) {
            return true;
        }
        // Also return true if we're transitioning TO this animation
        if (nextAnimation != null && nextAnimation.getName().equals(name)) {
            return true;
        }
        return false;
    }

    // ==================== Update and Render ====================

    /**
     * Updates the skeleton animation.
     * Call this every frame with the elapsed time.
     * @param deltaTime Time elapsed since last frame (in seconds)
     */
    public void update(double deltaTime) {
        // Update current animation
        if (currentAnimation != null) {
            currentAnimation.update(deltaTime);
            applyAnimation(currentAnimation);
        }

        // Handle animation blending
        if (nextAnimation != null) {
            nextAnimation.update(deltaTime);
            blendTime += deltaTime;

            if (blendTime >= blendDuration) {
                // Blend complete - switch to next animation
                currentAnimation = nextAnimation;
                nextAnimation = null;
                blendTime = 0;
            } else {
                // Blend the animations (simple approach: just use next anim's weights increasingly)
                // For a more sophisticated blend, you'd interpolate bone transforms
                double t = blendTime / blendDuration;
                // Apply next animation with increasing influence
                // (Current anim already applied, so we partially blend next)
                blendAnimations(currentAnimation, nextAnimation, t);
            }
        }
    }

    /**
     * Applies an animation to the skeleton, using rest positions as the base.
     * Animation keyframe values are treated as OFFSETS from rest positions.
     * @param animation The animation to apply
     */
    private void applyAnimation(BoneAnimation animation) {
        for (String boneName : animation.getAnimatedBoneNames()) {
            Bone bone = findBone(boneName);
            if (bone == null) continue;

            BoneAnimation.Keyframe kf = animation.getInterpolatedKeyframe(boneName);
            if (kf == null) continue;

            // Get rest position (base pose)
            double[] rest = restPositions.get(boneName);
            double restX = rest != null ? rest[0] : 0;
            double restY = rest != null ? rest[1] : 0;
            double restRotation = rest != null ? rest[2] : 0;

            // Apply keyframe as OFFSET to rest position
            bone.setLocalPosition(restX + kf.localX, restY + kf.localY);
            bone.setRotation(restRotation + kf.rotation);
            bone.setScale(kf.scaleX, kf.scaleY);
        }
    }

    /**
     * Blends between two animations.
     * Animation keyframe values are treated as OFFSETS from rest positions.
     * @param from Source animation
     * @param to Target animation
     * @param t Blend factor (0 = from, 1 = to)
     */
    private void blendAnimations(BoneAnimation from, BoneAnimation to, double t) {
        // Get all animated bones from both animations
        Set<String> allBones = new HashSet<>();
        allBones.addAll(from.getAnimatedBoneNames());
        allBones.addAll(to.getAnimatedBoneNames());

        for (String boneName : allBones) {
            Bone bone = findBone(boneName);
            if (bone == null) continue;

            // Get rest position (base pose)
            double[] rest = restPositions.get(boneName);
            double restX = rest != null ? rest[0] : 0;
            double restY = rest != null ? rest[1] : 0;
            double restRotation = rest != null ? rest[2] : 0;

            BoneAnimation.Keyframe fromKf = from.getInterpolatedKeyframe(boneName);
            BoneAnimation.Keyframe toKf = to.getInterpolatedKeyframe(boneName);

            if (fromKf != null && toKf != null) {
                // Blend the keyframe offsets
                double offsetX = lerp(fromKf.localX, toKf.localX, t);
                double offsetY = lerp(fromKf.localY, toKf.localY, t);
                double offsetRotation = lerpAngle(fromKf.rotation, toKf.rotation, t);
                double scaleX = lerp(fromKf.scaleX, toKf.scaleX, t);
                double scaleY = lerp(fromKf.scaleY, toKf.scaleY, t);

                // Apply as offset to rest position
                bone.setLocalPosition(restX + offsetX, restY + offsetY);
                bone.setRotation(restRotation + offsetRotation);
                bone.setScale(scaleX, scaleY);
            } else if (toKf != null) {
                // Only target has keyframe, lerp from rest to target
                double offsetX = lerp(0, toKf.localX, t);
                double offsetY = lerp(0, toKf.localY, t);
                double offsetRotation = lerpAngle(0, toKf.rotation, t);

                bone.setLocalPosition(restX + offsetX, restY + offsetY);
                bone.setRotation(restRotation + offsetRotation);
            }
        }
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private double lerpAngle(double a, double b, double t) {
        a = normalizeAngle(a);
        b = normalizeAngle(b);
        double diff = b - a;
        if (diff > 180) diff -= 360;
        else if (diff < -180) diff += 360;
        return normalizeAngle(a + diff * t);
    }

    private double normalizeAngle(double angle) {
        angle = angle % 360;
        if (angle < 0) angle += 360;
        return angle;
    }

    /**
     * Draws the skeleton.
     * @param g Graphics context
     */
    public void draw(Graphics g) {
        if (rootBone == null) return;

        Graphics2D g2d = (Graphics2D) g;

        // Save current transform
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();

        // Apply flip if needed
        if (flipX) {
            g2d.translate(x, 0);
            g2d.scale(-1, 1);
            g2d.translate(-x, 0);
        }

        // Draw the bone hierarchy
        rootBone.draw(g2d, x, y, scale);

        // Debug drawing
        if (debugDraw) {
            drawDebugInfo(g2d);
        }

        // Restore transform
        g2d.setTransform(oldTransform);
    }

    /**
     * Draws debug information (bone positions, pivots, etc.).
     */
    private void drawDebugInfo(Graphics2D g) {
        // Draw skeleton origin
        g.setColor(Color.GREEN);
        int size = 6;
        g.fillOval((int)x - size/2, (int)y - size/2, size, size);

        // Draw bone pivots
        g.setColor(Color.CYAN);
        for (Bone bone : boneMap.values()) {
            int bx = (int) bone.getWorldX();
            int by = (int) bone.getWorldY();
            g.fillOval(bx - 3, by - 3, 6, 6);

            // Draw bone name
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString(bone.getName(), bx + 5, by - 5);
        }
    }

    /**
     * Enables or disables debug drawing.
     * @param enabled True to enable debug visuals
     */
    public void setDebugDraw(boolean enabled) {
        this.debugDraw = enabled;
    }

    /**
     * Checks if debug drawing is enabled.
     * @return True if debug drawing is on
     */
    public boolean isDebugDraw() {
        return debugDraw;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a 15-bone humanoid skeleton optimized for 2D side-view animation.
     *
     * ============================================================================
     * BONE HIERARCHY (15 visible bones + 1 invisible root):
     * ============================================================================
     *
     * root (invisible container)
     * └── torso              [1]  - Main body/chest
     *     ├── neck           [2]  - Connects head to torso
     *     │   └── head       [3]  - Character's head
     *     ├── arm_upper_left [4]  - Left upper arm (back arm in profile)
     *     │   └── arm_lower_left [5]  - Left forearm
     *     │       └── hand_left  [6]  - Left hand
     *     ├── arm_upper_right [7] - Right upper arm (front arm in profile)
     *     │   └── arm_lower_right [8] - Right forearm
     *     │       └── hand_right [9]  - Right hand
     *     ├── leg_upper_left [10] - Left thigh (back leg in profile)
     *     │   └── leg_lower_left [11] - Left calf/shin
     *     │       └── foot_left [12]  - Left foot
     *     └── leg_upper_right [13] - Right thigh (front leg in profile)
     *         └── leg_lower_right [14] - Right calf/shin
     *             └── foot_right [15]  - Right foot
     *
     * ============================================================================
     * BLOCKBENCH ANIMATION IMPORT - BONE NAME MAPPINGS:
     * ============================================================================
     *
     * When creating animations in Blockbench, use these bone names (or the mapped
     * alternatives listed in BlockbenchAnimationImporter):
     *
     * CORE BONES:
     *   - torso     (or: body, Body, chest, Chest, Torso)
     *   - neck      (or: Neck, neck_bone)
     *   - head      (or: Head)
     *
     * LEFT ARM (back arm in 2D profile view):
     *   - arm_upper_left  (or: leftArm, LeftArm, left_arm, arm_left)
     *   - arm_lower_left  (or: leftForearm, LeftForearm, left_forearm)
     *   - hand_left       (or: leftHand, LeftHand, left_hand)
     *
     * RIGHT ARM (front arm in 2D profile view):
     *   - arm_upper_right (or: rightArm, RightArm, right_arm, arm_right)
     *   - arm_lower_right (or: rightForearm, RightForearm, right_forearm)
     *   - hand_right      (or: rightHand, RightHand, right_hand)
     *
     * LEFT LEG (back leg in 2D profile view):
     *   - leg_upper_left  (or: leftLeg, LeftLeg, left_leg, leftThigh)
     *   - leg_lower_left  (or: leftCalf, LeftCalf, left_calf, leftShin)
     *   - foot_left       (or: leftFoot, LeftFoot, left_foot)
     *
     * RIGHT LEG (front leg in 2D profile view):
     *   - leg_upper_right (or: rightLeg, RightLeg, right_leg, rightThigh)
     *   - leg_lower_right (or: rightCalf, RightCalf, right_calf, rightShin)
     *   - foot_right      (or: rightFoot, RightFoot, right_foot)
     *
     * ============================================================================
     * BLOCKBENCH KEYFRAME FORMAT:
     * ============================================================================
     *
     * Blockbench exports animations in this JSON format:
     *
     * {
     *   "animations": {
     *     "animation.player.walk": {
     *       "loop": true,
     *       "animation_length": 0.5,
     *       "bones": {
     *         "torso": {
     *           "rotation": { "0.0": [0, 0, 5], "0.25": [0, 0, -5] },
     *           "position": { "0.0": [0, 0, 0], "0.25": [0, 2, 0] }
     *         },
     *         "neck": {
     *           "rotation": { "0.0": [0, 0, 0] }
     *         },
     *         ...
     *       }
     *     }
     *   }
     * }
     *
     * COORDINATE MAPPING (Blockbench 3D -> Game 2D):
     *   - Blockbench X position -> Game localX
     *   - Blockbench Y position -> Game localY (inverted: -Y)
     *   - Blockbench Z rotation -> Game 2D rotation
     *
     * PIVOT POINTS (normalized 0.0 to 1.0):
     *   - (0.5, 0.0) = top-center (used for limbs - rotate from joint)
     *   - (0.5, 0.5) = center
     *   - (0.5, 1.0) = bottom-center (used for head - rotate from neck base)
     *
     * ============================================================================
     *
     * @return A new 15-bone humanoid skeleton
     */
    public static Skeleton createHumanoid() {
        Skeleton skeleton = new Skeleton();

        // ====== CREATE ALL 15 BONES + ROOT ======

        // Root container (invisible)
        Bone root = new Bone("root");
        root.setVisible(false);

        // Core bones
        Bone torso = new Bone("torso");
        Bone neck = new Bone("neck");
        Bone head = new Bone("head");

        // Left arm (back arm in profile view) - 3 segments
        Bone armUpperLeft = new Bone("arm_upper_left");
        Bone armLowerLeft = new Bone("arm_lower_left");
        Bone handLeft = new Bone("hand_left");

        // Right arm (front arm in profile view) - 3 segments
        Bone armUpperRight = new Bone("arm_upper_right");
        Bone armLowerRight = new Bone("arm_lower_right");
        Bone handRight = new Bone("hand_right");

        // Left leg (back leg in profile view) - 3 segments
        Bone legUpperLeft = new Bone("leg_upper_left");
        Bone legLowerLeft = new Bone("leg_lower_left");
        Bone footLeft = new Bone("foot_left");

        // Right leg (front leg in profile view) - 3 segments
        Bone legUpperRight = new Bone("leg_upper_right");
        Bone legLowerRight = new Bone("leg_lower_right");
        Bone footRight = new Bone("foot_right");

        // ====== SET UP BONE HIERARCHY ======

        root.addChild(torso);

        // Head chain: torso -> neck -> head
        torso.addChild(neck);
        neck.addChild(head);

        // Arms: torso -> upper -> lower -> hand
        torso.addChild(armUpperLeft);
        armUpperLeft.addChild(armLowerLeft);
        armLowerLeft.addChild(handLeft);

        torso.addChild(armUpperRight);
        armUpperRight.addChild(armLowerRight);
        armLowerRight.addChild(handRight);

        // Legs: torso -> upper -> lower -> foot
        torso.addChild(legUpperLeft);
        legUpperLeft.addChild(legLowerLeft);
        legLowerLeft.addChild(footLeft);

        torso.addChild(legUpperRight);
        legUpperRight.addChild(legLowerRight);
        legLowerRight.addChild(footRight);

        // ====== SET LOCAL POSITIONS (relative to parent) ======
        //
        // COORDINATE SYSTEM:
        // - Positions are in UNSCALED pixels (multiply by RENDER_SCALE=1 for display)
        // - Y-axis: negative = up, positive = down
        // - Each bone's localPosition is relative to its PARENT'S position
        // - 32x32 BASE QUALITY (was RENDER_SCALE=2, now RENDER_SCALE=1 with 2x sizes)
        //
        // PIVOT POINTS determine where the bone's position represents:
        // - Pivot (0.5, 0.0) = top-center: position = TOP of bone, extends DOWN
        // - Pivot (0.5, 1.0) = bottom-center: position = BOTTOM of bone, extends UP
        // - Pivot (0.5, 0.5) = center: position = CENTER of bone
        //
        // BONE CONNECTION RULES:
        // - Child bone position should match parent bone's edge
        // - For pivot-top bones: next bone at position + height
        // - For pivot-bottom bones: next bone at position - height
        // - For pivot-center bones: edges at position ± height/2
        //
        // BONE SIZES (32x32 base quality):
        //   torso: 32x32, neck: 16x8, head: 24x20
        //   arm_upper: 12x16, arm_lower: 12x16, hand: 12x8
        //   leg_upper: 16x20, leg_lower: 12x20, foot: 20x8
        //
        // BONE LAYOUT (calculated from connections):
        //     y=-44 -------- HEAD TOP (head: -24 - 20)
        //     y=-24 -------- head bottom / neck top
        //     y=-16 -------- NECK BOTTOM / TORSO TOP
        //     y=0   -------- TORSO CENTER (anchor point)
        //     y=+16 -------- torso bottom / hip / leg top
        //     y=+36 -------- leg_upper bottom / knee
        //     y=+56 -------- leg_lower bottom / ankle
        //     y=+64 -------- FOOT BOTTOM
        //
        // Total span: -44 to +64 = 108 unscaled = 108 display pixels
        // Fits in 109px hitbox
        //
        // ====================================================================

        torso.setLocalPosition(0, 0);  // Anchor point - torso center

        // === HEAD CHAIN (above torso) ===
        // Torso: size 32x32, pivot center → top at y=-16
        // Neck: size 16x8, pivot bottom → position = neck bottom, extends up
        neck.setLocalPosition(0, -16);            // Neck bottom at torso top

        // Head: size 24x20, pivot bottom → position = head bottom
        // Neck top = neck_pos - neck_height = -16 - 8 = -24
        head.setLocalPosition(0, -8);             // Relative to neck: -24 - (-16) = -8

        // === ARMS (at shoulder level) ===
        // Arms: pivot top → position = arm top, extends down
        // Upper arm at shoulder (inside torso top area)
        armUpperLeft.setLocalPosition(0, -12);    // Shoulder position
        armLowerLeft.setLocalPosition(0, 16);     // arm_upper height = 16
        handLeft.setLocalPosition(0, 16);         // arm_lower height = 16

        armUpperRight.setLocalPosition(0, -12);
        armLowerRight.setLocalPosition(0, 16);
        handRight.setLocalPosition(0, 16);

        // === LEGS (below torso) ===
        // Legs: pivot top → position = leg top, extends down
        // Torso bottom at y=+16
        legUpperLeft.setLocalPosition(0, 16);     // Hip at torso bottom
        legLowerLeft.setLocalPosition(0, 20);     // leg_upper height = 20
        footLeft.setLocalPosition(0, 20);         // leg_lower height = 20

        legUpperRight.setLocalPosition(0, 16);
        legLowerRight.setLocalPosition(0, 20);
        footRight.setLocalPosition(0, 20);

        // ====== SET PIVOT POINTS (rotation origins) ======
        // Format: (x, y) where 0.0-1.0 represents position within bone bounds
        // (0.5, 0.0) = top-center - good for limbs that rotate from their attachment point
        // (0.5, 1.0) = bottom-center - good for head rotating from base of skull

        torso.setPivot(0.5, 0.5);                 // Center (body core)
        neck.setPivot(0.5, 1.0);                  // Bottom (rotates from base)
        head.setPivot(0.5, 1.0);                  // Bottom (rotates from neck joint)

        // Arms - pivot at top (shoulder/elbow/wrist joints)
        armUpperLeft.setPivot(0.5, 0.0);
        armLowerLeft.setPivot(0.5, 0.0);
        handLeft.setPivot(0.5, 0.0);
        armUpperRight.setPivot(0.5, 0.0);
        armLowerRight.setPivot(0.5, 0.0);
        handRight.setPivot(0.5, 0.0);

        // Legs - pivot at top (hip/knee/ankle joints)
        legUpperLeft.setPivot(0.5, 0.0);
        legLowerLeft.setPivot(0.5, 0.0);
        footLeft.setPivot(0.5, 0.0);
        legUpperRight.setPivot(0.5, 0.0);
        legLowerRight.setPivot(0.5, 0.0);
        footRight.setPivot(0.5, 0.0);

        // ====== SET Z-ORDER (drawing order for 2D profile view) ======
        // Lower values = drawn first (behind), higher values = drawn last (in front)
        // This creates proper layering for side-view character

        legUpperLeft.setZOrder(-3);               // Back leg - furthest back
        legLowerLeft.setZOrder(-3);
        footLeft.setZOrder(-3);

        armUpperLeft.setZOrder(-2);               // Back arm - behind torso
        armLowerLeft.setZOrder(-2);
        handLeft.setZOrder(-2);

        legUpperRight.setZOrder(-1);              // Front leg - behind torso but in front of back leg
        legLowerRight.setZOrder(-1);
        footRight.setZOrder(-1);

        torso.setZOrder(0);                       // Body in middle
        neck.setZOrder(0);                        // Neck same layer as torso
        head.setZOrder(1);                        // Head in front of torso

        armUpperRight.setZOrder(2);               // Front arm - in front of everything
        armLowerRight.setZOrder(2);
        handRight.setZOrder(2);

        // ====== SET DEFAULT SIZES (for placeholder rendering) ======
        // These are pixel dimensions at 32x32 BASE QUALITY (RENDER_SCALE=1)
        // Sizes should match the local position offsets for proper bone connections
        // 4x the original resolution for maximum detail
        //
        // BLOCKBENCH CUBE SIZES (use these when creating your model):
        // These are the actual display pixel sizes
        //
        //   BONE              SIZE        NOTES
        //   ----              ----        -----
        //   torso             32 x 32     Main body
        //   neck              16 x 8      Neck connector
        //   head              24 x 20     Character's head
        //   arm_upper_*       12 x 16     Upper arm
        //   arm_lower_*       12 x 16     Forearm
        //   hand_*            12 x 8      Hand
        //   leg_upper_*       16 x 20     Thigh
        //   leg_lower_*       12 x 20     Calf/shin
        //   foot_*            20 x 8      Foot

        torso.setDefaultSize(32, 32);             // Main body (32x32 base quality)
        neck.setDefaultSize(16, 8);               // Neck connector
        head.setDefaultSize(24, 20);              // Head

        // Arm segments - sized to connect properly
        armUpperLeft.setDefaultSize(12, 16);      // Upper arm
        armLowerLeft.setDefaultSize(12, 16);      // Forearm
        handLeft.setDefaultSize(12, 8);           // Hand
        armUpperRight.setDefaultSize(12, 16);
        armLowerRight.setDefaultSize(12, 16);
        handRight.setDefaultSize(12, 8);

        // Leg segments - larger than arms
        legUpperLeft.setDefaultSize(16, 20);      // Thigh
        legLowerLeft.setDefaultSize(12, 20);      // Calf
        footLeft.setDefaultSize(20, 8);           // Foot
        legUpperRight.setDefaultSize(16, 20);
        legLowerRight.setDefaultSize(12, 20);
        footRight.setDefaultSize(20, 8);

        // ====== SET PLACEHOLDER COLORS (for debugging without textures) ======

        torso.setPlaceholderColor(new Color(100, 150, 200));    // Blue shirt
        neck.setPlaceholderColor(new Color(255, 200, 150));     // Skin tone
        head.setPlaceholderColor(new Color(255, 200, 150));     // Skin tone

        armUpperLeft.setPlaceholderColor(new Color(255, 200, 150));   // Skin
        armLowerLeft.setPlaceholderColor(new Color(255, 200, 150));
        handLeft.setPlaceholderColor(new Color(255, 180, 130));       // Slightly darker
        armUpperRight.setPlaceholderColor(new Color(255, 200, 150));
        armLowerRight.setPlaceholderColor(new Color(255, 200, 150));
        handRight.setPlaceholderColor(new Color(255, 180, 130));

        legUpperLeft.setPlaceholderColor(new Color(80, 80, 120));     // Dark pants
        legLowerLeft.setPlaceholderColor(new Color(80, 80, 120));
        footLeft.setPlaceholderColor(new Color(60, 40, 20));          // Brown shoes
        legUpperRight.setPlaceholderColor(new Color(80, 80, 120));
        legLowerRight.setPlaceholderColor(new Color(80, 80, 120));
        footRight.setPlaceholderColor(new Color(60, 40, 20));

        skeleton.setRootBone(root);

        // CRITICAL: Store rest positions BEFORE any animations are applied
        // Animation keyframes will be applied as offsets to these positions
        skeleton.storeRestPositions();

        // Debug: print bone hierarchy
        System.out.println("Skeleton created with 15 bones:");
        for (String boneName : skeleton.getBoneNames()) {
            Bone b = skeleton.findBone(boneName);
            System.out.println("  - " + boneName + " visible=" + b.isVisible() +
                             " size=" + b.getTextureWidth() + "x" + b.getTextureHeight());
        }

        return skeleton;
    }

    /**
     * Creates a humanoid skeleton with textures loaded from a directory.
     *
     * Expected texture files (15 bones):
     *   - torso.png           - Main body/chest
     *   - neck.png            - Neck connector
     *   - head.png            - Character's head
     *   - arm_upper_left.png  - Left upper arm
     *   - arm_lower_left.png  - Left forearm
     *   - hand_left.png       - Left hand
     *   - arm_upper_right.png - Right upper arm
     *   - arm_lower_right.png - Right forearm
     *   - hand_right.png      - Right hand
     *   - leg_upper_left.png  - Left thigh
     *   - leg_lower_left.png  - Left calf
     *   - foot_left.png       - Left foot
     *   - leg_upper_right.png - Right thigh
     *   - leg_lower_right.png - Right calf
     *   - foot_right.png      - Right foot
     *
     * @param textureDir Directory containing the texture files
     * @return A textured humanoid skeleton
     */
    public static Skeleton createHumanoidWithTextures(String textureDir) {
        Skeleton skeleton = createHumanoid();

        // Load textures for all 15 bones
        String[] boneNames = {
            "torso", "neck", "head",
            "arm_upper_left", "arm_lower_left", "hand_left",
            "arm_upper_right", "arm_lower_right", "hand_right",
            "leg_upper_left", "leg_lower_left", "foot_left",
            "leg_upper_right", "leg_lower_right", "foot_right"
        };

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
     * Gets the list of all 15 bone names in this skeleton.
     * Useful for iterating over bones or validating Blockbench imports.
     *
     * @return Array of the 15 bone names
     */
    public static String[] getBoneNameList() {
        return new String[] {
            "torso", "neck", "head",
            "arm_upper_left", "arm_lower_left", "hand_left",
            "arm_upper_right", "arm_lower_right", "hand_right",
            "leg_upper_left", "leg_lower_left", "foot_left",
            "leg_upper_right", "leg_lower_right", "foot_right"
        };
    }
}
