package animation.bone;
import graphics.*;

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

    /*
     * ============================================================================
     * ANIMATION FACTORY METHODS FOR 15-BONE SKELETON
     * ============================================================================
     *
     * These factory methods create animations for the standard 15-bone humanoid:
     *
     * BONE LIST (for Blockbench animation):
     *   1.  torso           - Main body/chest
     *   2.  neck            - Neck connector (NEW)
     *   3.  head            - Character's head
     *   4.  arm_upper_left  - Left upper arm (back arm in profile)
     *   5.  arm_lower_left  - Left forearm
     *   6.  hand_left       - Left hand
     *   7.  arm_upper_right - Right upper arm (front arm in profile)
     *   8.  arm_lower_right - Right forearm
     *   9.  hand_right      - Right hand
     *   10. leg_upper_left  - Left thigh (back leg in profile)
     *   11. leg_lower_left  - Left calf/shin
     *   12. foot_left       - Left foot
     *   13. leg_upper_right - Right thigh (front leg in profile)
     *   14. leg_lower_right - Right calf/shin
     *   15. foot_right      - Right foot
     *
     * KEYFRAME FORMAT:
     *   addKeyframe(boneName, time, localX, localY, rotation)
     *   - time: seconds into animation
     *   - localX/localY: position offset from bone's rest position
     *   - rotation: degrees (clockwise positive)
     *
     * IMPORTING FROM BLOCKBENCH:
     *   Use BlockbenchAnimationImporter.importFile("path/to/animation.json")
     *   Blockbench Z-rotation maps to our 2D rotation.
     *   Blockbench Y-position is inverted (-Y) for our coordinate system.
     *
     * ============================================================================
     */

    /**
     * Creates a natural idle breathing animation for profile view.
     * Animates all 15 bones with subtle breathing movement.
     *
     * Duration: 2.5 seconds (looping)
     *
     * @param torsoName Name of the torso/body bone (ignored, uses standard names)
     * @return Idle animation for 15-bone skeleton
     */
    public static BoneAnimation createIdleAnimation(String torsoName) {
        BoneAnimation anim = new BoneAnimation("idle", 2.5, true);

        // === TORSO - subtle breathing bob ===
        anim.addKeyframe("torso", 0.0, 0, 0, 0);
        anim.addKeyframe("torso", 1.25, 0, -1, 0);   // Slight rise (inhale)
        anim.addKeyframe("torso", 2.5, 0, 0, 0);    // Return (exhale)

        // === NECK - follows torso with slight independent movement ===
        anim.addKeyframe("neck", 0.0, 0, 0, 0);
        anim.addKeyframe("neck", 1.25, 0, 0, 1);    // Tiny tilt during inhale
        anim.addKeyframe("neck", 2.5, 0, 0, 0);

        // === HEAD - very subtle movement ===
        anim.addKeyframe("head", 0.0, 0, 0, 0);
        anim.addKeyframe("head", 1.25, 0, 0, -1);   // Counter-tilt to neck
        anim.addKeyframe("head", 2.5, 0, 0, 0);

        // === ARMS - relaxed at sides, slight natural bend ===
        // Right arm (front in profile) - relaxed position
        anim.addKeyframe("arm_upper_right", 0.0, 0, 0, 5);
        anim.addKeyframe("arm_upper_right", 1.25, 0, 0, 5);
        anim.addKeyframe("arm_upper_right", 2.5, 0, 0, 5);
        anim.addKeyframe("arm_lower_right", 0.0, 0, 0, -15);
        anim.addKeyframe("arm_lower_right", 1.25, 0, 0, -15);
        anim.addKeyframe("arm_lower_right", 2.5, 0, 0, -15);
        anim.addKeyframe("hand_right", 0.0, 0, 0, 0);
        anim.addKeyframe("hand_right", 1.25, 0, 0, 0);
        anim.addKeyframe("hand_right", 2.5, 0, 0, 0);

        // Left arm (back in profile) - relaxed position
        anim.addKeyframe("arm_upper_left", 0.0, 0, 0, -5);
        anim.addKeyframe("arm_upper_left", 1.25, 0, 0, -5);
        anim.addKeyframe("arm_upper_left", 2.5, 0, 0, -5);
        anim.addKeyframe("arm_lower_left", 0.0, 0, 0, -15);
        anim.addKeyframe("arm_lower_left", 1.25, 0, 0, -15);
        anim.addKeyframe("arm_lower_left", 2.5, 0, 0, -15);
        anim.addKeyframe("hand_left", 0.0, 0, 0, 0);
        anim.addKeyframe("hand_left", 1.25, 0, 0, 0);
        anim.addKeyframe("hand_left", 2.5, 0, 0, 0);

        // === LEGS - standing position ===
        anim.addKeyframe("leg_upper_right", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_upper_right", 1.25, 0, 0, 0);
        anim.addKeyframe("leg_upper_right", 2.5, 0, 0, 0);
        anim.addKeyframe("leg_lower_right", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_lower_right", 1.25, 0, 0, 0);
        anim.addKeyframe("leg_lower_right", 2.5, 0, 0, 0);
        anim.addKeyframe("foot_right", 0.0, 0, 0, 0);
        anim.addKeyframe("foot_right", 1.25, 0, 0, 0);
        anim.addKeyframe("foot_right", 2.5, 0, 0, 0);

        anim.addKeyframe("leg_upper_left", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_upper_left", 1.25, 0, 0, 0);
        anim.addKeyframe("leg_upper_left", 2.5, 0, 0, 0);
        anim.addKeyframe("leg_lower_left", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_lower_left", 1.25, 0, 0, 0);
        anim.addKeyframe("leg_lower_left", 2.5, 0, 0, 0);
        anim.addKeyframe("foot_left", 0.0, 0, 0, 0);
        anim.addKeyframe("foot_left", 1.25, 0, 0, 0);
        anim.addKeyframe("foot_left", 2.5, 0, 0, 0);

        return anim;
    }

    /**
     * Creates a natural human running animation for 15-bone skeleton in profile view.
     * Implements a proper running gait cycle with contact, push-off, swing phases.
     *
     * Duration: 0.5 seconds per cycle (looping)
     *
     * All 15 bones are animated:
     *   - torso: Forward lean + vertical bob
     *   - neck: Slight stabilization
     *   - head: Counter-rotation for stability
     *   - arms: Opposite swing to legs
     *   - legs: Full gait cycle
     *   - hands/feet: Natural follow-through
     *
     * @return Running animation for 15-bone skeleton
     */
    public static BoneAnimation createRunAnimation() {
        BoneAnimation anim = new BoneAnimation("run", 0.5, true);

        // Human running gait cycle (0.5s per full cycle = 2 steps)
        // Phase timing: 0.0 = right foot contact, 0.25 = left foot contact, 0.5 = cycle repeats

        // === TORSO - slight forward lean with vertical bob ===
        // Body drops at contact, rises during push-off
        anim.addKeyframe("torso", 0.0, 0, 2, 8);      // Right contact - body low, slight lean
        anim.addKeyframe("torso", 0.125, 0, -2, 8);   // Push-off - body rises
        anim.addKeyframe("torso", 0.25, 0, 2, 8);     // Left contact - body low
        anim.addKeyframe("torso", 0.375, 0, -2, 8);   // Push-off - body rises
        anim.addKeyframe("torso", 0.5, 0, 2, 8);      // Cycle complete

        // === NECK - stabilizes head, slight counter to torso bob ===
        anim.addKeyframe("neck", 0.0, 0, 0, -2);      // Counter lean
        anim.addKeyframe("neck", 0.125, 0, 0, -2);
        anim.addKeyframe("neck", 0.25, 0, 0, -2);
        anim.addKeyframe("neck", 0.375, 0, 0, -2);
        anim.addKeyframe("neck", 0.5, 0, 0, -2);

        // === HEAD - stabilizes, slight counter-rotation to torso ===
        anim.addKeyframe("head", 0.0, 0, -1, -3);     // Counter-tilt
        anim.addKeyframe("head", 0.125, 0, 0, -3);
        anim.addKeyframe("head", 0.25, 0, -1, -3);
        anim.addKeyframe("head", 0.375, 0, 0, -3);
        anim.addKeyframe("head", 0.5, 0, -1, -3);

        // === RIGHT LEG (front leg in profile) ===
        // At 0.0: contact phase - leg extended forward
        // At 0.125: mid-stance - leg vertical, supporting weight
        // At 0.25: push-off - leg extends back
        // At 0.375: swing phase - leg swings forward with bent knee
        anim.addKeyframe("leg_upper_right", 0.0, 0, 0, -35);   // Extended forward (contact)
        anim.addKeyframe("leg_upper_right", 0.125, 0, 0, -10); // Vertical (mid-stance)
        anim.addKeyframe("leg_upper_right", 0.25, 0, 0, 30);   // Extended back (push-off)
        anim.addKeyframe("leg_upper_right", 0.375, 0, 0, -15); // Swinging forward
        anim.addKeyframe("leg_upper_right", 0.5, 0, 0, -35);   // Cycle complete

        anim.addKeyframe("leg_lower_right", 0.0, 0, 0, 5);     // Nearly straight at contact
        anim.addKeyframe("leg_lower_right", 0.125, 0, 0, 0);   // Straight at mid-stance
        anim.addKeyframe("leg_lower_right", 0.25, 0, 0, 10);   // Slight bend at push-off
        anim.addKeyframe("leg_lower_right", 0.375, 0, 0, 45);  // Knee bent during swing
        anim.addKeyframe("leg_lower_right", 0.5, 0, 0, 5);     // Extending for contact

        // === LEFT LEG (back leg in profile) - opposite phase ===
        anim.addKeyframe("leg_upper_left", 0.0, 0, 0, 30);     // Extended back (push-off)
        anim.addKeyframe("leg_upper_left", 0.125, 0, 0, -15);  // Swinging forward
        anim.addKeyframe("leg_upper_left", 0.25, 0, 0, -35);   // Extended forward (contact)
        anim.addKeyframe("leg_upper_left", 0.375, 0, 0, -10);  // Vertical (mid-stance)
        anim.addKeyframe("leg_upper_left", 0.5, 0, 0, 30);     // Cycle complete

        anim.addKeyframe("leg_lower_left", 0.0, 0, 0, 10);     // Slight bend at push-off
        anim.addKeyframe("leg_lower_left", 0.125, 0, 0, 45);   // Knee bent during swing
        anim.addKeyframe("leg_lower_left", 0.25, 0, 0, 5);     // Nearly straight at contact
        anim.addKeyframe("leg_lower_left", 0.375, 0, 0, 0);    // Straight at mid-stance
        anim.addKeyframe("leg_lower_left", 0.5, 0, 0, 10);     // Cycle complete

        // === RIGHT ARM (front arm in profile) - swings opposite to right leg ===
        // When right leg is forward, right arm is back
        anim.addKeyframe("arm_upper_right", 0.0, 0, 0, 40);    // Arm back
        anim.addKeyframe("arm_upper_right", 0.125, 0, 0, 15);  // Swinging forward
        anim.addKeyframe("arm_upper_right", 0.25, 0, 0, -35);  // Arm forward
        anim.addKeyframe("arm_upper_right", 0.375, 0, 0, 5);   // Swinging back
        anim.addKeyframe("arm_upper_right", 0.5, 0, 0, 40);    // Cycle complete

        anim.addKeyframe("arm_lower_right", 0.0, 0, 0, -50);   // Elbow bent, arm back
        anim.addKeyframe("arm_lower_right", 0.125, 0, 0, -40); // Transitioning
        anim.addKeyframe("arm_lower_right", 0.25, 0, 0, -30);  // Less bent, arm forward
        anim.addKeyframe("arm_lower_right", 0.375, 0, 0, -45); // Transitioning
        anim.addKeyframe("arm_lower_right", 0.5, 0, 0, -50);   // Cycle complete

        // === LEFT ARM (back arm in profile) - opposite phase ===
        anim.addKeyframe("arm_upper_left", 0.0, 0, 0, -35);    // Arm forward
        anim.addKeyframe("arm_upper_left", 0.125, 0, 0, 5);    // Swinging back
        anim.addKeyframe("arm_upper_left", 0.25, 0, 0, 40);    // Arm back
        anim.addKeyframe("arm_upper_left", 0.375, 0, 0, 15);   // Swinging forward
        anim.addKeyframe("arm_upper_left", 0.5, 0, 0, -35);    // Cycle complete

        anim.addKeyframe("arm_lower_left", 0.0, 0, 0, -30);    // Less bent, arm forward
        anim.addKeyframe("arm_lower_left", 0.125, 0, 0, -45);  // Transitioning
        anim.addKeyframe("arm_lower_left", 0.25, 0, 0, -50);   // Elbow bent, arm back
        anim.addKeyframe("arm_lower_left", 0.375, 0, 0, -40);  // Transitioning
        anim.addKeyframe("arm_lower_left", 0.5, 0, 0, -30);    // Cycle complete

        // === HANDS - follow arm movement with slight wrist rotation ===
        anim.addKeyframe("hand_right", 0.0, 0, 0, 10);         // Wrist bent back
        anim.addKeyframe("hand_right", 0.125, 0, 0, 5);
        anim.addKeyframe("hand_right", 0.25, 0, 0, -5);        // Wrist straight/forward
        anim.addKeyframe("hand_right", 0.375, 0, 0, 0);
        anim.addKeyframe("hand_right", 0.5, 0, 0, 10);

        anim.addKeyframe("hand_left", 0.0, 0, 0, -5);          // Opposite phase
        anim.addKeyframe("hand_left", 0.125, 0, 0, 0);
        anim.addKeyframe("hand_left", 0.25, 0, 0, 10);
        anim.addKeyframe("hand_left", 0.375, 0, 0, 5);
        anim.addKeyframe("hand_left", 0.5, 0, 0, -5);

        // === FEET - ankle rotation for natural foot strike ===
        // Right foot: dorsiflexed during swing, plantarflexed at push-off
        anim.addKeyframe("foot_right", 0.0, 0, 0, 30);         // Heel strike - toes up
        anim.addKeyframe("foot_right", 0.125, 0, 0, 0);        // Flat on ground
        anim.addKeyframe("foot_right", 0.25, 0, 0, -25);       // Push-off - toes down
        anim.addKeyframe("foot_right", 0.375, 0, 0, 20);       // Swing - toes up (dorsiflexed)
        anim.addKeyframe("foot_right", 0.5, 0, 0, 30);         // Cycle complete

        // Left foot: opposite phase
        anim.addKeyframe("foot_left", 0.0, 0, 0, -25);         // Push-off
        anim.addKeyframe("foot_left", 0.125, 0, 0, 20);        // Swing
        anim.addKeyframe("foot_left", 0.25, 0, 0, 30);         // Heel strike
        anim.addKeyframe("foot_left", 0.375, 0, 0, 0);         // Flat
        anim.addKeyframe("foot_left", 0.5, 0, 0, -25);         // Cycle complete

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
     * Creates a natural walking animation for 15-bone skeleton in profile view.
     * Slower and more subtle than running.
     *
     * Duration: 0.8 seconds per cycle (looping)
     *
     * @return Walk animation for 15-bone skeleton
     */
    public static BoneAnimation createWalkAnimation() {
        BoneAnimation anim = new BoneAnimation("walk", 0.8, true);

        // Walking gait - slower, more upright, smaller movements than run
        // Phase timing: 0.0 = right foot contact, 0.4 = left foot contact

        // === TORSO - minimal lean, subtle bob ===
        anim.addKeyframe("torso", 0.0, 0, 1, 3);
        anim.addKeyframe("torso", 0.2, 0, -1, 3);
        anim.addKeyframe("torso", 0.4, 0, 1, 3);
        anim.addKeyframe("torso", 0.6, 0, -1, 3);
        anim.addKeyframe("torso", 0.8, 0, 1, 3);

        // === NECK - subtle stabilization ===
        anim.addKeyframe("neck", 0.0, 0, 0, 0);
        anim.addKeyframe("neck", 0.8, 0, 0, 0);

        // === HEAD - stable ===
        anim.addKeyframe("head", 0.0, 0, 0, 0);
        anim.addKeyframe("head", 0.8, 0, 0, 0);

        // === RIGHT LEG ===
        anim.addKeyframe("leg_upper_right", 0.0, 0, 0, -20);   // Forward contact
        anim.addKeyframe("leg_upper_right", 0.2, 0, 0, 0);     // Vertical
        anim.addKeyframe("leg_upper_right", 0.4, 0, 0, 20);    // Back push-off
        anim.addKeyframe("leg_upper_right", 0.6, 0, 0, -5);    // Swinging forward
        anim.addKeyframe("leg_upper_right", 0.8, 0, 0, -20);   // Cycle complete

        anim.addKeyframe("leg_lower_right", 0.0, 0, 0, 5);
        anim.addKeyframe("leg_lower_right", 0.2, 0, 0, 0);
        anim.addKeyframe("leg_lower_right", 0.4, 0, 0, 5);
        anim.addKeyframe("leg_lower_right", 0.6, 0, 0, 25);    // Knee bent during swing
        anim.addKeyframe("leg_lower_right", 0.8, 0, 0, 5);

        // === LEFT LEG - opposite phase ===
        anim.addKeyframe("leg_upper_left", 0.0, 0, 0, 20);     // Back push-off
        anim.addKeyframe("leg_upper_left", 0.2, 0, 0, -5);     // Swinging forward
        anim.addKeyframe("leg_upper_left", 0.4, 0, 0, -20);    // Forward contact
        anim.addKeyframe("leg_upper_left", 0.6, 0, 0, 0);      // Vertical
        anim.addKeyframe("leg_upper_left", 0.8, 0, 0, 20);     // Cycle complete

        anim.addKeyframe("leg_lower_left", 0.0, 0, 0, 5);
        anim.addKeyframe("leg_lower_left", 0.2, 0, 0, 25);     // Knee bent during swing
        anim.addKeyframe("leg_lower_left", 0.4, 0, 0, 5);
        anim.addKeyframe("leg_lower_left", 0.6, 0, 0, 0);
        anim.addKeyframe("leg_lower_left", 0.8, 0, 0, 5);

        // === RIGHT ARM - opposite to right leg ===
        anim.addKeyframe("arm_upper_right", 0.0, 0, 0, 25);    // Arm back
        anim.addKeyframe("arm_upper_right", 0.2, 0, 0, 5);
        anim.addKeyframe("arm_upper_right", 0.4, 0, 0, -20);   // Arm forward
        anim.addKeyframe("arm_upper_right", 0.6, 0, 0, 0);
        anim.addKeyframe("arm_upper_right", 0.8, 0, 0, 25);

        anim.addKeyframe("arm_lower_right", 0.0, 0, 0, -30);
        anim.addKeyframe("arm_lower_right", 0.4, 0, 0, -20);
        anim.addKeyframe("arm_lower_right", 0.8, 0, 0, -30);

        // === LEFT ARM - opposite phase ===
        anim.addKeyframe("arm_upper_left", 0.0, 0, 0, -20);    // Arm forward
        anim.addKeyframe("arm_upper_left", 0.2, 0, 0, 0);
        anim.addKeyframe("arm_upper_left", 0.4, 0, 0, 25);     // Arm back
        anim.addKeyframe("arm_upper_left", 0.6, 0, 0, 5);
        anim.addKeyframe("arm_upper_left", 0.8, 0, 0, -20);

        anim.addKeyframe("arm_lower_left", 0.0, 0, 0, -20);
        anim.addKeyframe("arm_lower_left", 0.4, 0, 0, -30);
        anim.addKeyframe("arm_lower_left", 0.8, 0, 0, -20);

        // === HANDS - subtle follow-through ===
        anim.addKeyframe("hand_right", 0.0, 0, 0, 5);
        anim.addKeyframe("hand_right", 0.4, 0, 0, -5);
        anim.addKeyframe("hand_right", 0.8, 0, 0, 5);

        anim.addKeyframe("hand_left", 0.0, 0, 0, -5);
        anim.addKeyframe("hand_left", 0.4, 0, 0, 5);
        anim.addKeyframe("hand_left", 0.8, 0, 0, -5);

        // === FEET - ground contact ===
        anim.addKeyframe("foot_right", 0.0, 0, 0, 0);
        anim.addKeyframe("foot_right", 0.6, 0, 0, -15);
        anim.addKeyframe("foot_right", 0.8, 0, 0, 0);

        anim.addKeyframe("foot_left", 0.0, 0, 0, -15);
        anim.addKeyframe("foot_left", 0.2, 0, 0, 0);
        anim.addKeyframe("foot_left", 0.4, 0, 0, 0);
        anim.addKeyframe("foot_left", 0.8, 0, 0, -15);

        return anim;
    }

    /**
     * Creates a natural jump animation for 15-bone skeleton in profile view.
     * Phases: crouch, launch, peak, descent, land
     *
     * Duration: 0.8 seconds (non-looping)
     *
     * All 15 bones are animated:
     *   - torso: Crouch, extend, forward lean
     *   - neck: Stabilization during motion
     *   - head: Counter-rotation, look direction
     *   - arms: Swing for momentum
     *   - legs: Crouch, extend, tuck, land
     *   - hands/feet: Follow-through
     *
     * @return Jump animation for 15-bone skeleton
     */
    public static BoneAnimation createJumpAnimation() {
        BoneAnimation anim = new BoneAnimation("jump", 0.8, false);

        // Jump phases:
        // 0.0-0.15: Crouch preparation
        // 0.15-0.3: Launch (explosive extension)
        // 0.3-0.5: Airborne (peak)
        // 0.5-0.7: Descent
        // 0.7-0.8: Landing

        // === TORSO - crouch, extend, lean slightly forward in air ===
        anim.addKeyframe("torso", 0.0, 0, 0, 0);       // Start
        anim.addKeyframe("torso", 0.15, 0, 6, 5);     // Crouch - body low, slight lean back
        anim.addKeyframe("torso", 0.3, 0, -4, 10);    // Launch - body extends, lean forward
        anim.addKeyframe("torso", 0.5, 0, -6, 12);    // Peak - full extension, forward lean
        anim.addKeyframe("torso", 0.7, 0, -2, 8);     // Descent
        anim.addKeyframe("torso", 0.8, 0, 2, 0);      // Landing

        // === NECK - stabilizes during jump, slight counter-movement ===
        anim.addKeyframe("neck", 0.0, 0, 0, 0);
        anim.addKeyframe("neck", 0.15, 0, 0, -3);     // Counter crouch lean
        anim.addKeyframe("neck", 0.3, 0, 0, -5);      // Counter launch lean
        anim.addKeyframe("neck", 0.5, 0, 0, -6);      // Maximum counter
        anim.addKeyframe("neck", 0.7, 0, 0, -4);      // Relaxing during descent
        anim.addKeyframe("neck", 0.8, 0, 0, 0);       // Landing

        // === HEAD - stabilizes, counters torso motion ===
        anim.addKeyframe("head", 0.0, 0, 0, 0);
        anim.addKeyframe("head", 0.15, 0, -1, -2);    // Look up during crouch
        anim.addKeyframe("head", 0.3, 0, 0, -3);      // Counter lean
        anim.addKeyframe("head", 0.5, 0, 0, -4);      // Look ahead
        anim.addKeyframe("head", 0.7, 0, 0, -2);      // Descending
        anim.addKeyframe("head", 0.8, 0, 0, 0);       // Landing

        // === RIGHT LEG (front) - crouch, extend, tuck, extend for landing ===
        anim.addKeyframe("leg_upper_right", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_upper_right", 0.15, 0, 0, -30);  // Crouch - knee forward
        anim.addKeyframe("leg_upper_right", 0.3, 0, 0, 20);    // Launch - leg extends back
        anim.addKeyframe("leg_upper_right", 0.5, 0, 0, -20);   // Airborne - leg forward
        anim.addKeyframe("leg_upper_right", 0.7, 0, 0, -10);   // Preparing to land
        anim.addKeyframe("leg_upper_right", 0.8, 0, 0, 0);     // Landing

        anim.addKeyframe("leg_lower_right", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_lower_right", 0.15, 0, 0, 50);   // Deep knee bend
        anim.addKeyframe("leg_lower_right", 0.3, 0, 0, 10);    // Extending
        anim.addKeyframe("leg_lower_right", 0.5, 0, 0, 25);    // Slightly bent in air
        anim.addKeyframe("leg_lower_right", 0.7, 0, 0, 15);    // Extending for landing
        anim.addKeyframe("leg_lower_right", 0.8, 0, 0, 0);     // Landing

        // === LEFT LEG (back) - mirrors right but slightly offset ===
        anim.addKeyframe("leg_upper_left", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_upper_left", 0.15, 0, 0, -25);   // Crouch
        anim.addKeyframe("leg_upper_left", 0.3, 0, 0, 25);     // Launch - extends back
        anim.addKeyframe("leg_upper_left", 0.5, 0, 0, 15);     // Airborne - trails behind
        anim.addKeyframe("leg_upper_left", 0.7, 0, 0, 5);      // Coming forward
        anim.addKeyframe("leg_upper_left", 0.8, 0, 0, 0);      // Landing

        anim.addKeyframe("leg_lower_left", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_lower_left", 0.15, 0, 0, 45);    // Crouch bend
        anim.addKeyframe("leg_lower_left", 0.3, 0, 0, 15);     // Extending
        anim.addKeyframe("leg_lower_left", 0.5, 0, 0, 30);     // Bent in air
        anim.addKeyframe("leg_lower_left", 0.7, 0, 0, 10);     // Extending
        anim.addKeyframe("leg_lower_left", 0.8, 0, 0, 0);      // Landing

        // === RIGHT ARM (front) - swings back, then forward ===
        anim.addKeyframe("arm_upper_right", 0.0, 0, 0, 5);
        anim.addKeyframe("arm_upper_right", 0.15, 0, 0, 30);   // Arm back during crouch
        anim.addKeyframe("arm_upper_right", 0.3, 0, 0, -40);   // Swing forward for launch
        anim.addKeyframe("arm_upper_right", 0.5, 0, 0, -30);   // Forward in air
        anim.addKeyframe("arm_upper_right", 0.7, 0, 0, -10);   // Coming down
        anim.addKeyframe("arm_upper_right", 0.8, 0, 0, 5);     // Landing

        anim.addKeyframe("arm_lower_right", 0.0, 0, 0, -15);
        anim.addKeyframe("arm_lower_right", 0.15, 0, 0, -40);  // Bent back
        anim.addKeyframe("arm_lower_right", 0.3, 0, 0, -60);   // Bent forward
        anim.addKeyframe("arm_lower_right", 0.5, 0, 0, -50);   // In air
        anim.addKeyframe("arm_lower_right", 0.7, 0, 0, -30);   // Descending
        anim.addKeyframe("arm_lower_right", 0.8, 0, 0, -15);   // Landing

        // === LEFT ARM (back) - opposite swing ===
        anim.addKeyframe("arm_upper_left", 0.0, 0, 0, -5);
        anim.addKeyframe("arm_upper_left", 0.15, 0, 0, -35);   // Arm forward during crouch
        anim.addKeyframe("arm_upper_left", 0.3, 0, 0, 35);     // Swing back for launch
        anim.addKeyframe("arm_upper_left", 0.5, 0, 0, 25);     // Back in air
        anim.addKeyframe("arm_upper_left", 0.7, 0, 0, 10);     // Coming forward
        anim.addKeyframe("arm_upper_left", 0.8, 0, 0, -5);     // Landing

        anim.addKeyframe("arm_lower_left", 0.0, 0, 0, -15);
        anim.addKeyframe("arm_lower_left", 0.15, 0, 0, -50);   // Bent forward
        anim.addKeyframe("arm_lower_left", 0.3, 0, 0, -30);    // Less bent back
        anim.addKeyframe("arm_lower_left", 0.5, 0, 0, -35);    // In air
        anim.addKeyframe("arm_lower_left", 0.7, 0, 0, -25);    // Descending
        anim.addKeyframe("arm_lower_left", 0.8, 0, 0, -15);    // Landing

        // === HANDS - follow arm movement ===
        anim.addKeyframe("hand_right", 0.0, 0, 0, 0);
        anim.addKeyframe("hand_right", 0.15, 0, 0, 10);        // Wrist back during crouch
        anim.addKeyframe("hand_right", 0.3, 0, 0, -10);        // Wrist forward at launch
        anim.addKeyframe("hand_right", 0.5, 0, 0, -5);         // In air
        anim.addKeyframe("hand_right", 0.7, 0, 0, 0);          // Descending
        anim.addKeyframe("hand_right", 0.8, 0, 0, 0);          // Landing

        anim.addKeyframe("hand_left", 0.0, 0, 0, 0);
        anim.addKeyframe("hand_left", 0.15, 0, 0, -10);        // Opposite wrist
        anim.addKeyframe("hand_left", 0.3, 0, 0, 10);          // Wrist back at launch
        anim.addKeyframe("hand_left", 0.5, 0, 0, 5);           // In air
        anim.addKeyframe("hand_left", 0.7, 0, 0, 0);           // Descending
        anim.addKeyframe("hand_left", 0.8, 0, 0, 0);           // Landing

        // === FEET - ankle movement for jump ===
        anim.addKeyframe("foot_right", 0.0, 0, 0, 0);
        anim.addKeyframe("foot_right", 0.15, 0, 0, 20);        // Heels up during crouch
        anim.addKeyframe("foot_right", 0.3, 0, 0, -30);        // Toes point down at launch
        anim.addKeyframe("foot_right", 0.5, 0, 0, -20);        // Toes down in air
        anim.addKeyframe("foot_right", 0.7, 0, 0, 10);         // Preparing to land
        anim.addKeyframe("foot_right", 0.8, 0, 0, 0);          // Landing

        anim.addKeyframe("foot_left", 0.0, 0, 0, 0);
        anim.addKeyframe("foot_left", 0.15, 0, 0, 20);         // Heels up during crouch
        anim.addKeyframe("foot_left", 0.3, 0, 0, -35);         // Toes point down at launch
        anim.addKeyframe("foot_left", 0.5, 0, 0, -25);         // Toes down in air
        anim.addKeyframe("foot_left", 0.7, 0, 0, 5);           // Preparing to land
        anim.addKeyframe("foot_left", 0.8, 0, 0, 0);           // Landing

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
