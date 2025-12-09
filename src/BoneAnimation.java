import java.util.*;

/**
 * Manages keyframe-based animations for bones.
 * Supports smooth interpolation between keyframes for position, rotation, and scale.
 */
public class BoneAnimation {

    private String name;
    private double duration;       // Total animation duration in seconds
    private boolean looping;       // Whether animation loops
    private double currentTime;    // Current playback time

    // Keyframes stored per bone name
    private Map<String, List<Keyframe>> boneKeyframes;

    /**
     * Represents a single keyframe for a bone.
     */
    public static class Keyframe {
        public double time;        // Time in seconds
        public double localX;      // Local X position
        public double localY;      // Local Y position
        public double rotation;    // Rotation in degrees
        public double scaleX;      // X scale
        public double scaleY;      // Y scale

        public Keyframe(double time, double localX, double localY, double rotation) {
            this(time, localX, localY, rotation, 1.0, 1.0);
        }

        public Keyframe(double time, double localX, double localY, double rotation,
                        double scaleX, double scaleY) {
            this.time = time;
            this.localX = localX;
            this.localY = localY;
            this.rotation = rotation;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
        }

        /**
         * Creates a copy of this keyframe at a different time.
         */
        public Keyframe copyAt(double newTime) {
            return new Keyframe(newTime, localX, localY, rotation, scaleX, scaleY);
        }
    }

    /**
     * Creates a new animation.
     * @param name Animation name (e.g., "idle", "run", "jump")
     * @param duration Total duration in seconds
     * @param looping Whether the animation loops
     */
    public BoneAnimation(String name, double duration, boolean looping) {
        this.name = name;
        this.duration = duration;
        this.looping = looping;
        this.currentTime = 0;
        this.boneKeyframes = new HashMap<>();
    }

    /**
     * Adds a keyframe for a specific bone.
     * @param boneName Name of the bone
     * @param keyframe The keyframe to add
     */
    public void addKeyframe(String boneName, Keyframe keyframe) {
        boneKeyframes.computeIfAbsent(boneName, k -> new ArrayList<>()).add(keyframe);
        // Keep keyframes sorted by time
        boneKeyframes.get(boneName).sort(Comparator.comparingDouble(k -> k.time));
    }

    /**
     * Convenience method to add a keyframe with position and rotation.
     */
    public void addKeyframe(String boneName, double time, double x, double y, double rotation) {
        addKeyframe(boneName, new Keyframe(time, x, y, rotation));
    }

    /**
     * Gets the animation name.
     * @return Animation name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the animation duration.
     * @return Duration in seconds
     */
    public double getDuration() {
        return duration;
    }

    /**
     * Checks if the animation is looping.
     * @return True if looping
     */
    public boolean isLooping() {
        return looping;
    }

    /**
     * Gets the current playback time.
     * @return Current time in seconds
     */
    public double getCurrentTime() {
        return currentTime;
    }

    /**
     * Sets the current playback time.
     * @param time Time in seconds
     */
    public void setCurrentTime(double time) {
        if (looping && duration > 0) {
            this.currentTime = time % duration;
            if (this.currentTime < 0) {
                this.currentTime += duration;
            }
        } else {
            this.currentTime = Math.max(0, Math.min(time, duration));
        }
    }

    /**
     * Updates the animation by advancing time.
     * @param deltaTime Time elapsed since last update (in seconds)
     * @return True if the animation is still playing, false if finished (non-looping)
     */
    public boolean update(double deltaTime) {
        currentTime += deltaTime;

        if (looping) {
            if (duration > 0) {
                currentTime = currentTime % duration;
            }
            return true;
        } else {
            if (currentTime >= duration) {
                currentTime = duration;
                return false;
            }
            return true;
        }
    }

    /**
     * Resets the animation to the beginning.
     */
    public void reset() {
        currentTime = 0;
    }

    /**
     * Gets the interpolated transform for a bone at the current time.
     * @param boneName Name of the bone
     * @return Interpolated keyframe values, or null if no keyframes exist
     */
    public Keyframe getInterpolatedKeyframe(String boneName) {
        List<Keyframe> keyframes = boneKeyframes.get(boneName);
        if (keyframes == null || keyframes.isEmpty()) {
            return null;
        }

        // Find the two keyframes to interpolate between
        Keyframe before = null;
        Keyframe after = null;

        for (Keyframe kf : keyframes) {
            if (kf.time <= currentTime) {
                before = kf;
            } else {
                after = kf;
                break;
            }
        }

        // Handle edge cases
        if (before == null && after == null) {
            return null;
        }
        if (before == null) {
            return after.copyAt(currentTime);
        }
        if (after == null) {
            if (looping && !keyframes.isEmpty()) {
                // Wrap around to first keyframe
                after = keyframes.get(0);
                // Calculate t for looping interpolation
                double segmentDuration = (duration - before.time) + after.time;
                if (segmentDuration > 0) {
                    double t = (currentTime - before.time) / segmentDuration;
                    return interpolate(before, after, t);
                }
            }
            return before.copyAt(currentTime);
        }

        // Interpolate between before and after
        double segmentDuration = after.time - before.time;
        if (segmentDuration <= 0) {
            return before.copyAt(currentTime);
        }

        double t = (currentTime - before.time) / segmentDuration;
        return interpolate(before, after, t);
    }

    /**
     * Linearly interpolates between two keyframes.
     * Uses smooth interpolation for rotation to handle angle wrapping.
     */
    private Keyframe interpolate(Keyframe a, Keyframe b, double t) {
        // Clamp t to 0-1
        t = Math.max(0, Math.min(1, t));

        // Smooth easing (ease in-out)
        double smoothT = smoothstep(t);

        // Linear interpolation for position and scale
        double x = lerp(a.localX, b.localX, smoothT);
        double y = lerp(a.localY, b.localY, smoothT);
        double scaleX = lerp(a.scaleX, b.scaleX, smoothT);
        double scaleY = lerp(a.scaleY, b.scaleY, smoothT);

        // Angle interpolation (handles wrapping around 360 degrees)
        double rotation = lerpAngle(a.rotation, b.rotation, smoothT);

        return new Keyframe(currentTime, x, y, rotation, scaleX, scaleY);
    }

    /**
     * Linear interpolation.
     */
    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /**
     * Angle interpolation that takes the shortest path.
     */
    private double lerpAngle(double a, double b, double t) {
        // Normalize angles to 0-360
        a = normalizeAngle(a);
        b = normalizeAngle(b);

        // Find shortest path
        double diff = b - a;
        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }

        return normalizeAngle(a + diff * t);
    }

    /**
     * Normalizes angle to 0-360 range.
     */
    private double normalizeAngle(double angle) {
        angle = angle % 360;
        if (angle < 0) angle += 360;
        return angle;
    }

    /**
     * Smooth step function for easing.
     */
    private double smoothstep(double t) {
        return t * t * (3 - 2 * t);
    }

    /**
     * Applies the current animation state to a skeleton.
     * @param skeleton The skeleton to animate
     */
    public void applyToSkeleton(Skeleton skeleton) {
        for (String boneName : boneKeyframes.keySet()) {
            Bone bone = skeleton.findBone(boneName);
            if (bone != null) {
                Keyframe kf = getInterpolatedKeyframe(boneName);
                if (kf != null) {
                    bone.setLocalPosition(kf.localX, kf.localY);
                    bone.setRotation(kf.rotation);
                    bone.setScale(kf.scaleX, kf.scaleY);
                }
            }
        }
    }

    /**
     * Gets all bone names that have keyframes in this animation.
     * @return Set of bone names
     */
    public Set<String> getAnimatedBoneNames() {
        return new HashSet<>(boneKeyframes.keySet());
    }

    /**
     * Checks if a bone has any keyframes in this animation.
     * @param boneName Name of the bone
     * @return True if the bone has keyframes
     */
    public boolean hasKeyframesFor(String boneName) {
        List<Keyframe> keyframes = boneKeyframes.get(boneName);
        return keyframes != null && !keyframes.isEmpty();
    }

    // ==================== Static Factory Methods for Common Animations ====================

    /**
     * Creates a simple idle breathing animation.
     * @param torsoName Name of the torso/body bone to animate
     * @return Idle animation
     */
    public static BoneAnimation createIdleAnimation(String torsoName) {
        BoneAnimation anim = new BoneAnimation("idle", 2.0, true);
        // Subtle up/down bob
        anim.addKeyframe(torsoName, 0.0, 0, 0, 0);
        anim.addKeyframe(torsoName, 1.0, 0, -2, 0);  // Slight rise
        anim.addKeyframe(torsoName, 2.0, 0, 0, 0);
        return anim;
    }

    /**
     * Creates a basic running animation for 2-part limbs.
     * Uses the default bone names: arm_upper_left, arm_lower_left, etc.
     * @return Running animation for 2-part skeleton
     */
    public static BoneAnimation createRunAnimation() {
        BoneAnimation anim = new BoneAnimation("run", 0.4, true);

        // Left leg - upper swings, lower bends at knee
        anim.addKeyframe("leg_upper_left", 0.0, 0, 0, -25);   // Forward
        anim.addKeyframe("leg_upper_left", 0.2, 0, 0, 25);    // Back
        anim.addKeyframe("leg_upper_left", 0.4, 0, 0, -25);   // Forward again
        anim.addKeyframe("leg_lower_left", 0.0, 0, 0, 30);    // Bent back
        anim.addKeyframe("leg_lower_left", 0.2, 0, 0, 0);     // Straight
        anim.addKeyframe("leg_lower_left", 0.4, 0, 0, 30);    // Bent back

        // Right leg - opposite phase
        anim.addKeyframe("leg_upper_right", 0.0, 0, 0, 25);   // Back
        anim.addKeyframe("leg_upper_right", 0.2, 0, 0, -25);  // Forward
        anim.addKeyframe("leg_upper_right", 0.4, 0, 0, 25);   // Back again
        anim.addKeyframe("leg_lower_right", 0.0, 0, 0, 0);    // Straight
        anim.addKeyframe("leg_lower_right", 0.2, 0, 0, 30);   // Bent back
        anim.addKeyframe("leg_lower_right", 0.4, 0, 0, 0);    // Straight

        // Left arm - swings opposite to left leg
        anim.addKeyframe("arm_upper_left", 0.0, 0, 0, 25);    // Back
        anim.addKeyframe("arm_upper_left", 0.2, 0, 0, -25);   // Forward
        anim.addKeyframe("arm_upper_left", 0.4, 0, 0, 25);    // Back again
        anim.addKeyframe("arm_lower_left", 0.0, 0, 0, -20);   // Slight bend
        anim.addKeyframe("arm_lower_left", 0.2, 0, 0, -30);   // More bend forward
        anim.addKeyframe("arm_lower_left", 0.4, 0, 0, -20);   // Slight bend

        // Right arm - swings opposite to right leg
        anim.addKeyframe("arm_upper_right", 0.0, 0, 0, -25);  // Forward
        anim.addKeyframe("arm_upper_right", 0.2, 0, 0, 25);   // Back
        anim.addKeyframe("arm_upper_right", 0.4, 0, 0, -25);  // Forward again
        anim.addKeyframe("arm_lower_right", 0.0, 0, 0, -30);  // More bend forward
        anim.addKeyframe("arm_lower_right", 0.2, 0, 0, -20);  // Slight bend
        anim.addKeyframe("arm_lower_right", 0.4, 0, 0, -30);  // More bend forward

        return anim;
    }

    /**
     * Creates a basic running animation (legacy version for single-part limbs).
     * @param legLeftName Left leg bone name
     * @param legRightName Right leg bone name
     * @param armLeftName Left arm bone name
     * @param armRightName Right arm bone name
     * @return Running animation
     */
    public static BoneAnimation createRunAnimation(String legLeftName, String legRightName,
                                                   String armLeftName, String armRightName) {
        BoneAnimation anim = new BoneAnimation("run", 0.4, true);

        // Left leg swings forward when right leg swings back
        anim.addKeyframe(legLeftName, 0.0, 0, 0, -30);   // Forward
        anim.addKeyframe(legLeftName, 0.2, 0, 0, 30);    // Back
        anim.addKeyframe(legLeftName, 0.4, 0, 0, -30);   // Forward again

        anim.addKeyframe(legRightName, 0.0, 0, 0, 30);   // Back
        anim.addKeyframe(legRightName, 0.2, 0, 0, -30);  // Forward
        anim.addKeyframe(legRightName, 0.4, 0, 0, 30);   // Back again

        // Arms swing opposite to legs
        anim.addKeyframe(armLeftName, 0.0, 0, 0, 30);    // Back
        anim.addKeyframe(armLeftName, 0.2, 0, 0, -30);   // Forward
        anim.addKeyframe(armLeftName, 0.4, 0, 0, 30);    // Back again

        anim.addKeyframe(armRightName, 0.0, 0, 0, -30);  // Forward
        anim.addKeyframe(armRightName, 0.2, 0, 0, 30);   // Back
        anim.addKeyframe(armRightName, 0.4, 0, 0, -30);  // Forward again

        return anim;
    }

    /**
     * Creates a jump animation for 2-part limbs.
     * Uses the default bone names.
     * @return Jump animation for 2-part skeleton
     */
    public static BoneAnimation createJumpAnimation() {
        BoneAnimation anim = new BoneAnimation("jump", 0.6, false);

        // Crouch, then extend
        anim.addKeyframe("torso", 0.0, 0, 0, 0);
        anim.addKeyframe("torso", 0.1, 0, 4, 0);       // Crouch
        anim.addKeyframe("torso", 0.3, 0, -8, -5);     // Jump up, slight lean
        anim.addKeyframe("torso", 0.6, 0, 0, 0);       // Return

        // Left leg - crouch then extend
        anim.addKeyframe("leg_upper_left", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_upper_left", 0.1, 0, 0, -20);  // Crouch - thigh forward
        anim.addKeyframe("leg_upper_left", 0.3, 0, 0, 15);   // Extended back
        anim.addKeyframe("leg_upper_left", 0.6, 0, 0, 0);
        anim.addKeyframe("leg_lower_left", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_lower_left", 0.1, 0, 0, 40);   // Knee bent for crouch
        anim.addKeyframe("leg_lower_left", 0.3, 0, 0, -10);  // Leg extended
        anim.addKeyframe("leg_lower_left", 0.6, 0, 0, 0);

        // Right leg - same as left
        anim.addKeyframe("leg_upper_right", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_upper_right", 0.1, 0, 0, -20);
        anim.addKeyframe("leg_upper_right", 0.3, 0, 0, 15);
        anim.addKeyframe("leg_upper_right", 0.6, 0, 0, 0);
        anim.addKeyframe("leg_lower_right", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_lower_right", 0.1, 0, 0, 40);
        anim.addKeyframe("leg_lower_right", 0.3, 0, 0, -10);
        anim.addKeyframe("leg_lower_right", 0.6, 0, 0, 0);

        // Left arm - goes up during jump
        anim.addKeyframe("arm_upper_left", 0.0, 0, 0, 0);
        anim.addKeyframe("arm_upper_left", 0.1, 0, 0, -15);
        anim.addKeyframe("arm_upper_left", 0.3, 0, 0, -50);  // Arm up
        anim.addKeyframe("arm_upper_left", 0.6, 0, 0, 0);
        anim.addKeyframe("arm_lower_left", 0.0, 0, 0, 0);
        anim.addKeyframe("arm_lower_left", 0.1, 0, 0, -20);
        anim.addKeyframe("arm_lower_left", 0.3, 0, 0, -30);  // Bent at elbow
        anim.addKeyframe("arm_lower_left", 0.6, 0, 0, 0);

        // Right arm - goes up during jump
        anim.addKeyframe("arm_upper_right", 0.0, 0, 0, 0);
        anim.addKeyframe("arm_upper_right", 0.1, 0, 0, 15);
        anim.addKeyframe("arm_upper_right", 0.3, 0, 0, 50);   // Arm up
        anim.addKeyframe("arm_upper_right", 0.6, 0, 0, 0);
        anim.addKeyframe("arm_lower_right", 0.0, 0, 0, 0);
        anim.addKeyframe("arm_lower_right", 0.1, 0, 0, 20);
        anim.addKeyframe("arm_lower_right", 0.3, 0, 0, 30);   // Bent at elbow
        anim.addKeyframe("arm_lower_right", 0.6, 0, 0, 0);

        return anim;
    }

    /**
     * Creates a jump animation (legacy version for single-part limbs).
     * @param torsoName Torso bone name
     * @param legLeftName Left leg bone name
     * @param legRightName Right leg bone name
     * @param armLeftName Left arm bone name
     * @param armRightName Right arm bone name
     * @return Jump animation
     */
    public static BoneAnimation createJumpAnimation(String torsoName,
                                                    String legLeftName, String legRightName,
                                                    String armLeftName, String armRightName) {
        BoneAnimation anim = new BoneAnimation("jump", 0.6, false);

        // Crouch, then extend
        anim.addKeyframe(torsoName, 0.0, 0, 0, 0);
        anim.addKeyframe(torsoName, 0.1, 0, 4, 0);       // Crouch
        anim.addKeyframe(torsoName, 0.3, 0, -8, -5);     // Jump up, slight lean
        anim.addKeyframe(torsoName, 0.6, 0, 0, 0);       // Return

        // Legs tuck up during jump
        anim.addKeyframe(legLeftName, 0.0, 0, 0, 0);
        anim.addKeyframe(legLeftName, 0.1, 0, 0, 20);    // Bend for crouch
        anim.addKeyframe(legLeftName, 0.3, 0, 0, -20);   // Extend
        anim.addKeyframe(legLeftName, 0.6, 0, 0, 0);

        anim.addKeyframe(legRightName, 0.0, 0, 0, 0);
        anim.addKeyframe(legRightName, 0.1, 0, 0, 20);
        anim.addKeyframe(legRightName, 0.3, 0, 0, -20);
        anim.addKeyframe(legRightName, 0.6, 0, 0, 0);

        // Arms go up during jump
        anim.addKeyframe(armLeftName, 0.0, 0, 0, 0);
        anim.addKeyframe(armLeftName, 0.1, 0, 0, -20);
        anim.addKeyframe(armLeftName, 0.3, 0, 0, -60);   // Arms up
        anim.addKeyframe(armLeftName, 0.6, 0, 0, 0);

        anim.addKeyframe(armRightName, 0.0, 0, 0, 0);
        anim.addKeyframe(armRightName, 0.1, 0, 0, 20);
        anim.addKeyframe(armRightName, 0.3, 0, 0, 60);
        anim.addKeyframe(armRightName, 0.6, 0, 0, 0);

        return anim;
    }
}
