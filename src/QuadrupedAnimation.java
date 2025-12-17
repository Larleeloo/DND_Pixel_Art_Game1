/**
 * Provides factory methods for creating quadruped (4-legged animal) animations.
 * All animations are designed for the standard quadruped skeleton from QuadrupedSkeleton.
 *
 * ============================================================================
 * QUADRUPED ANIMATION BONE LIST (19 bones):
 * ============================================================================
 *
 *   1.  body                    - Main body/torso
 *   2.  neck                    - Neck connector
 *   3.  head                    - Animal's head
 *   4.  ear_left                - Left ear
 *   5.  ear_right               - Right ear
 *   6.  tail_base               - Tail attachment point
 *   7.  tail_tip                - Tail end
 *   8.  leg_front_left_upper    - Front left upper leg
 *   9.  leg_front_left_lower    - Front left lower leg
 *   10. paw_front_left          - Front left paw
 *   11. leg_front_right_upper   - Front right upper leg
 *   12. leg_front_right_lower   - Front right lower leg
 *   13. paw_front_right         - Front right paw
 *   14. leg_back_left_upper     - Back left upper leg
 *   15. leg_back_left_lower     - Back left lower leg
 *   16. paw_back_left           - Back left paw
 *   17. leg_back_right_upper    - Back right upper leg
 *   18. leg_back_right_lower    - Back right lower leg
 *   19. paw_back_right          - Back right paw
 *
 * ============================================================================
 * GAIT PATTERNS:
 * ============================================================================
 *
 * WALK (4-beat gait): Each leg moves independently
 *   Pattern: back_right → front_right → back_left → front_left
 *
 * TROT (2-beat gait): Diagonal pairs move together
 *   Pattern: (front_left + back_right) alternates with (front_right + back_left)
 *
 * RUN/GALLOP (4-beat gait): Front and back pairs
 *   Pattern: Both front legs together, then both back legs
 *
 * ============================================================================
 */
public class QuadrupedAnimation {

    /**
     * Creates a natural idle breathing animation for quadrupeds.
     * Animates body, head, tail, and ears with subtle movement.
     *
     * Duration: 3.0 seconds (looping)
     *
     * @return Idle animation for quadruped skeleton
     */
    public static BoneAnimation createIdleAnimation() {
        BoneAnimation anim = new BoneAnimation("idle", 3.0, true);

        // === BODY - subtle breathing movement ===
        anim.addKeyframe("body", 0.0, 0, 0, 0);
        anim.addKeyframe("body", 1.5, 0, -1, 0);   // Slight rise (inhale)
        anim.addKeyframe("body", 3.0, 0, 0, 0);   // Return (exhale)

        // === NECK - slight movement with breathing ===
        anim.addKeyframe("neck", 0.0, 0, 0, 0);
        anim.addKeyframe("neck", 1.5, 0, 0, 2);   // Slight lift
        anim.addKeyframe("neck", 3.0, 0, 0, 0);

        // === HEAD - occasional look around ===
        anim.addKeyframe("head", 0.0, 0, 0, 0);
        anim.addKeyframe("head", 0.75, 0, 0, 3);  // Look slightly up
        anim.addKeyframe("head", 1.5, 0, 0, -2);  // Look slightly down
        anim.addKeyframe("head", 2.25, 0, 0, 0);
        anim.addKeyframe("head", 3.0, 0, 0, 0);

        // === EARS - slight twitch ===
        anim.addKeyframe("ear_left", 0.0, 0, 0, 0);
        anim.addKeyframe("ear_left", 0.5, 0, 0, 5);
        anim.addKeyframe("ear_left", 0.7, 0, 0, 0);
        anim.addKeyframe("ear_left", 2.0, 0, 0, -5);
        anim.addKeyframe("ear_left", 2.2, 0, 0, 0);
        anim.addKeyframe("ear_left", 3.0, 0, 0, 0);

        anim.addKeyframe("ear_right", 0.0, 0, 0, 0);
        anim.addKeyframe("ear_right", 0.3, 0, 0, -5);
        anim.addKeyframe("ear_right", 0.5, 0, 0, 0);
        anim.addKeyframe("ear_right", 1.8, 0, 0, 5);
        anim.addKeyframe("ear_right", 2.0, 0, 0, 0);
        anim.addKeyframe("ear_right", 3.0, 0, 0, 0);

        // === TAIL - gentle wag ===
        anim.addKeyframe("tail_base", 0.0, 0, 0, 5);
        anim.addKeyframe("tail_base", 0.75, 0, 0, -5);
        anim.addKeyframe("tail_base", 1.5, 0, 0, 5);
        anim.addKeyframe("tail_base", 2.25, 0, 0, -5);
        anim.addKeyframe("tail_base", 3.0, 0, 0, 5);

        anim.addKeyframe("tail_tip", 0.0, 0, 0, 8);
        anim.addKeyframe("tail_tip", 0.75, 0, 0, -8);
        anim.addKeyframe("tail_tip", 1.5, 0, 0, 8);
        anim.addKeyframe("tail_tip", 2.25, 0, 0, -8);
        anim.addKeyframe("tail_tip", 3.0, 0, 0, 8);

        // === LEGS - standing still with slight weight shifts ===
        // Front legs
        addLegKeyframes(anim, "leg_front_left", 0, 0, 0);
        addLegKeyframes(anim, "leg_front_right", 0, 0, 0);
        // Back legs
        addLegKeyframes(anim, "leg_back_left", 0, 0, 0);
        addLegKeyframes(anim, "leg_back_right", 0, 0, 0);

        return anim;
    }

    /**
     * Creates a natural walking animation using a 4-beat gait.
     * Each leg moves independently in sequence.
     *
     * Duration: 1.0 seconds (looping)
     *
     * @return Walk animation for quadruped skeleton
     */
    public static BoneAnimation createWalkAnimation() {
        BoneAnimation anim = new BoneAnimation("walk", 1.0, true);

        // === BODY - subtle vertical bob and slight sway ===
        anim.addKeyframe("body", 0.0, 0, 1, 2);
        anim.addKeyframe("body", 0.25, 0, 0, -1);
        anim.addKeyframe("body", 0.5, 0, 1, 2);
        anim.addKeyframe("body", 0.75, 0, 0, -1);
        anim.addKeyframe("body", 1.0, 0, 1, 2);

        // === NECK - stabilizing counter-movement ===
        anim.addKeyframe("neck", 0.0, 0, 0, -1);
        anim.addKeyframe("neck", 0.25, 0, 0, 1);
        anim.addKeyframe("neck", 0.5, 0, 0, -1);
        anim.addKeyframe("neck", 0.75, 0, 0, 1);
        anim.addKeyframe("neck", 1.0, 0, 0, -1);

        // === HEAD - slight bob ===
        anim.addKeyframe("head", 0.0, 0, 0, 0);
        anim.addKeyframe("head", 0.25, 0, -1, 2);
        anim.addKeyframe("head", 0.5, 0, 0, 0);
        anim.addKeyframe("head", 0.75, 0, -1, 2);
        anim.addKeyframe("head", 1.0, 0, 0, 0);

        // === EARS - slight movement ===
        anim.addKeyframe("ear_left", 0.0, 0, 0, 0);
        anim.addKeyframe("ear_left", 0.5, 0, 0, 3);
        anim.addKeyframe("ear_left", 1.0, 0, 0, 0);

        anim.addKeyframe("ear_right", 0.0, 0, 0, 0);
        anim.addKeyframe("ear_right", 0.5, 0, 0, -3);
        anim.addKeyframe("ear_right", 1.0, 0, 0, 0);

        // === TAIL - following body motion ===
        anim.addKeyframe("tail_base", 0.0, 0, 0, 10);
        anim.addKeyframe("tail_base", 0.25, 0, 0, -5);
        anim.addKeyframe("tail_base", 0.5, 0, 0, 10);
        anim.addKeyframe("tail_base", 0.75, 0, 0, -5);
        anim.addKeyframe("tail_base", 1.0, 0, 0, 10);

        anim.addKeyframe("tail_tip", 0.0, 0, 0, 15);
        anim.addKeyframe("tail_tip", 0.25, 0, 0, -10);
        anim.addKeyframe("tail_tip", 0.5, 0, 0, 15);
        anim.addKeyframe("tail_tip", 0.75, 0, 0, -10);
        anim.addKeyframe("tail_tip", 1.0, 0, 0, 15);

        // === LEGS - 4-beat walk gait ===
        // Phase: 0.0 = back_right contact, 0.25 = front_right, 0.5 = back_left, 0.75 = front_left

        // FRONT LEFT LEG - contact at 0.75
        anim.addKeyframe("leg_front_left_upper", 0.0, 0, 0, 15);    // Swing back
        anim.addKeyframe("leg_front_left_upper", 0.25, 0, 0, 25);   // Push off
        anim.addKeyframe("leg_front_left_upper", 0.5, 0, 0, 0);     // Swing forward
        anim.addKeyframe("leg_front_left_upper", 0.75, 0, 0, -20);  // Contact
        anim.addKeyframe("leg_front_left_upper", 1.0, 0, 0, 15);

        anim.addKeyframe("leg_front_left_lower", 0.0, 0, 0, -10);
        anim.addKeyframe("leg_front_left_lower", 0.25, 0, 0, -5);
        anim.addKeyframe("leg_front_left_lower", 0.5, 0, 0, -25);   // Bent during swing
        anim.addKeyframe("leg_front_left_lower", 0.75, 0, 0, 0);    // Straight at contact
        anim.addKeyframe("leg_front_left_lower", 1.0, 0, 0, -10);

        anim.addKeyframe("paw_front_left", 0.0, 0, 0, 0);
        anim.addKeyframe("paw_front_left", 0.5, 0, 0, 15);          // Lifted during swing
        anim.addKeyframe("paw_front_left", 0.75, 0, 0, 0);          // Flat at contact
        anim.addKeyframe("paw_front_left", 1.0, 0, 0, 0);

        // FRONT RIGHT LEG - contact at 0.25
        anim.addKeyframe("leg_front_right_upper", 0.0, 0, 0, 0);    // Swing forward
        anim.addKeyframe("leg_front_right_upper", 0.25, 0, 0, -20); // Contact
        anim.addKeyframe("leg_front_right_upper", 0.5, 0, 0, 15);   // Swing back
        anim.addKeyframe("leg_front_right_upper", 0.75, 0, 0, 25);  // Push off
        anim.addKeyframe("leg_front_right_upper", 1.0, 0, 0, 0);

        anim.addKeyframe("leg_front_right_lower", 0.0, 0, 0, -25);  // Bent during swing
        anim.addKeyframe("leg_front_right_lower", 0.25, 0, 0, 0);   // Straight at contact
        anim.addKeyframe("leg_front_right_lower", 0.5, 0, 0, -10);
        anim.addKeyframe("leg_front_right_lower", 0.75, 0, 0, -5);
        anim.addKeyframe("leg_front_right_lower", 1.0, 0, 0, -25);

        anim.addKeyframe("paw_front_right", 0.0, 0, 0, 15);
        anim.addKeyframe("paw_front_right", 0.25, 0, 0, 0);
        anim.addKeyframe("paw_front_right", 0.75, 0, 0, 0);
        anim.addKeyframe("paw_front_right", 1.0, 0, 0, 15);

        // BACK LEFT LEG - contact at 0.5
        anim.addKeyframe("leg_back_left_upper", 0.0, 0, 0, 20);     // Push off
        anim.addKeyframe("leg_back_left_upper", 0.25, 0, 0, -5);    // Swing forward
        anim.addKeyframe("leg_back_left_upper", 0.5, 0, 0, -25);    // Contact
        anim.addKeyframe("leg_back_left_upper", 0.75, 0, 0, 10);    // Swing back
        anim.addKeyframe("leg_back_left_upper", 1.0, 0, 0, 20);

        anim.addKeyframe("leg_back_left_lower", 0.0, 0, 0, 5);
        anim.addKeyframe("leg_back_left_lower", 0.25, 0, 0, 30);    // Bent during swing
        anim.addKeyframe("leg_back_left_lower", 0.5, 0, 0, 5);      // Slight bend at contact
        anim.addKeyframe("leg_back_left_lower", 0.75, 0, 0, 0);
        anim.addKeyframe("leg_back_left_lower", 1.0, 0, 0, 5);

        anim.addKeyframe("paw_back_left", 0.0, 0, 0, -10);
        anim.addKeyframe("paw_back_left", 0.25, 0, 0, 10);
        anim.addKeyframe("paw_back_left", 0.5, 0, 0, 0);
        anim.addKeyframe("paw_back_left", 1.0, 0, 0, -10);

        // BACK RIGHT LEG - contact at 0.0
        anim.addKeyframe("leg_back_right_upper", 0.0, 0, 0, -25);   // Contact
        anim.addKeyframe("leg_back_right_upper", 0.25, 0, 0, 10);   // Swing back
        anim.addKeyframe("leg_back_right_upper", 0.5, 0, 0, 20);    // Push off
        anim.addKeyframe("leg_back_right_upper", 0.75, 0, 0, -5);   // Swing forward
        anim.addKeyframe("leg_back_right_upper", 1.0, 0, 0, -25);

        anim.addKeyframe("leg_back_right_lower", 0.0, 0, 0, 5);     // Slight bend at contact
        anim.addKeyframe("leg_back_right_lower", 0.25, 0, 0, 0);
        anim.addKeyframe("leg_back_right_lower", 0.5, 0, 0, 5);
        anim.addKeyframe("leg_back_right_lower", 0.75, 0, 0, 30);   // Bent during swing
        anim.addKeyframe("leg_back_right_lower", 1.0, 0, 0, 5);

        anim.addKeyframe("paw_back_right", 0.0, 0, 0, 0);
        anim.addKeyframe("paw_back_right", 0.5, 0, 0, -10);
        anim.addKeyframe("paw_back_right", 0.75, 0, 0, 10);
        anim.addKeyframe("paw_back_right", 1.0, 0, 0, 0);

        return anim;
    }

    /**
     * Creates a running animation using a bounding/gallop gait.
     * Front and back leg pairs move together.
     *
     * Duration: 0.4 seconds (looping)
     *
     * @return Run animation for quadruped skeleton
     */
    public static BoneAnimation createRunAnimation() {
        BoneAnimation anim = new BoneAnimation("run", 0.4, true);

        // === BODY - significant bob and forward lean ===
        anim.addKeyframe("body", 0.0, 0, 4, 8);     // Low point, strong lean
        anim.addKeyframe("body", 0.1, 0, -2, 6);    // Rising
        anim.addKeyframe("body", 0.2, 0, 4, 8);     // Low point
        anim.addKeyframe("body", 0.3, 0, -2, 6);    // Rising
        anim.addKeyframe("body", 0.4, 0, 4, 8);

        // === NECK - counter-balance ===
        anim.addKeyframe("neck", 0.0, 0, 0, -4);
        anim.addKeyframe("neck", 0.1, 0, 0, -2);
        anim.addKeyframe("neck", 0.2, 0, 0, -4);
        anim.addKeyframe("neck", 0.3, 0, 0, -2);
        anim.addKeyframe("neck", 0.4, 0, 0, -4);

        // === HEAD - forward thrust ===
        anim.addKeyframe("head", 0.0, 0, -2, -3);
        anim.addKeyframe("head", 0.1, 0, 0, 0);
        anim.addKeyframe("head", 0.2, 0, -2, -3);
        anim.addKeyframe("head", 0.3, 0, 0, 0);
        anim.addKeyframe("head", 0.4, 0, -2, -3);

        // === EARS - flattened against head ===
        anim.addKeyframe("ear_left", 0.0, 0, 0, -20);
        anim.addKeyframe("ear_left", 0.4, 0, 0, -20);

        anim.addKeyframe("ear_right", 0.0, 0, 0, -20);
        anim.addKeyframe("ear_right", 0.4, 0, 0, -20);

        // === TAIL - streaming behind ===
        anim.addKeyframe("tail_base", 0.0, 0, 0, 30);
        anim.addKeyframe("tail_base", 0.1, 0, 0, 20);
        anim.addKeyframe("tail_base", 0.2, 0, 0, 30);
        anim.addKeyframe("tail_base", 0.3, 0, 0, 20);
        anim.addKeyframe("tail_base", 0.4, 0, 0, 30);

        anim.addKeyframe("tail_tip", 0.0, 0, 0, 20);
        anim.addKeyframe("tail_tip", 0.1, 0, 0, 10);
        anim.addKeyframe("tail_tip", 0.2, 0, 0, 20);
        anim.addKeyframe("tail_tip", 0.3, 0, 0, 10);
        anim.addKeyframe("tail_tip", 0.4, 0, 0, 20);

        // === LEGS - Gallop pattern ===
        // Phase 0.0-0.2: Back legs push, front legs reach
        // Phase 0.2-0.4: Front legs push, back legs swing

        // FRONT LEFT LEG
        anim.addKeyframe("leg_front_left_upper", 0.0, 0, 0, -35);   // Extended forward
        anim.addKeyframe("leg_front_left_upper", 0.1, 0, 0, -20);   // Contact
        anim.addKeyframe("leg_front_left_upper", 0.2, 0, 0, 30);    // Push back
        anim.addKeyframe("leg_front_left_upper", 0.3, 0, 0, -10);   // Swing forward
        anim.addKeyframe("leg_front_left_upper", 0.4, 0, 0, -35);

        anim.addKeyframe("leg_front_left_lower", 0.0, 0, 0, 10);
        anim.addKeyframe("leg_front_left_lower", 0.1, 0, 0, 0);
        anim.addKeyframe("leg_front_left_lower", 0.2, 0, 0, -20);
        anim.addKeyframe("leg_front_left_lower", 0.3, 0, 0, -35);   // Folded during swing
        anim.addKeyframe("leg_front_left_lower", 0.4, 0, 0, 10);

        anim.addKeyframe("paw_front_left", 0.0, 0, 0, 10);
        anim.addKeyframe("paw_front_left", 0.1, 0, 0, 0);
        anim.addKeyframe("paw_front_left", 0.2, 0, 0, -15);
        anim.addKeyframe("paw_front_left", 0.3, 0, 0, 20);
        anim.addKeyframe("paw_front_left", 0.4, 0, 0, 10);

        // FRONT RIGHT LEG - slightly offset
        anim.addKeyframe("leg_front_right_upper", 0.0, 0, 0, -30);
        anim.addKeyframe("leg_front_right_upper", 0.1, 0, 0, -15);
        anim.addKeyframe("leg_front_right_upper", 0.2, 0, 0, 35);
        anim.addKeyframe("leg_front_right_upper", 0.3, 0, 0, -5);
        anim.addKeyframe("leg_front_right_upper", 0.4, 0, 0, -30);

        anim.addKeyframe("leg_front_right_lower", 0.0, 0, 0, 5);
        anim.addKeyframe("leg_front_right_lower", 0.1, 0, 0, 0);
        anim.addKeyframe("leg_front_right_lower", 0.2, 0, 0, -15);
        anim.addKeyframe("leg_front_right_lower", 0.3, 0, 0, -30);
        anim.addKeyframe("leg_front_right_lower", 0.4, 0, 0, 5);

        anim.addKeyframe("paw_front_right", 0.0, 0, 0, 5);
        anim.addKeyframe("paw_front_right", 0.1, 0, 0, 0);
        anim.addKeyframe("paw_front_right", 0.2, 0, 0, -10);
        anim.addKeyframe("paw_front_right", 0.3, 0, 0, 15);
        anim.addKeyframe("paw_front_right", 0.4, 0, 0, 5);

        // BACK LEFT LEG
        anim.addKeyframe("leg_back_left_upper", 0.0, 0, 0, 40);     // Extended back (push)
        anim.addKeyframe("leg_back_left_upper", 0.1, 0, 0, 10);     // Swing forward
        anim.addKeyframe("leg_back_left_upper", 0.2, 0, 0, -30);    // Contact
        anim.addKeyframe("leg_back_left_upper", 0.3, 0, 0, 20);     // Push back
        anim.addKeyframe("leg_back_left_upper", 0.4, 0, 0, 40);

        anim.addKeyframe("leg_back_left_lower", 0.0, 0, 0, -10);
        anim.addKeyframe("leg_back_left_lower", 0.1, 0, 0, 40);     // Folded during swing
        anim.addKeyframe("leg_back_left_lower", 0.2, 0, 0, 10);
        anim.addKeyframe("leg_back_left_lower", 0.3, 0, 0, 0);
        anim.addKeyframe("leg_back_left_lower", 0.4, 0, 0, -10);

        anim.addKeyframe("paw_back_left", 0.0, 0, 0, -20);
        anim.addKeyframe("paw_back_left", 0.1, 0, 0, 10);
        anim.addKeyframe("paw_back_left", 0.2, 0, 0, 5);
        anim.addKeyframe("paw_back_left", 0.3, 0, 0, 0);
        anim.addKeyframe("paw_back_left", 0.4, 0, 0, -20);

        // BACK RIGHT LEG - slightly offset
        anim.addKeyframe("leg_back_right_upper", 0.0, 0, 0, 35);
        anim.addKeyframe("leg_back_right_upper", 0.1, 0, 0, 5);
        anim.addKeyframe("leg_back_right_upper", 0.2, 0, 0, -25);
        anim.addKeyframe("leg_back_right_upper", 0.3, 0, 0, 25);
        anim.addKeyframe("leg_back_right_upper", 0.4, 0, 0, 35);

        anim.addKeyframe("leg_back_right_lower", 0.0, 0, 0, -5);
        anim.addKeyframe("leg_back_right_lower", 0.1, 0, 0, 35);
        anim.addKeyframe("leg_back_right_lower", 0.2, 0, 0, 5);
        anim.addKeyframe("leg_back_right_lower", 0.3, 0, 0, 0);
        anim.addKeyframe("leg_back_right_lower", 0.4, 0, 0, -5);

        anim.addKeyframe("paw_back_right", 0.0, 0, 0, -15);
        anim.addKeyframe("paw_back_right", 0.1, 0, 0, 5);
        anim.addKeyframe("paw_back_right", 0.2, 0, 0, 0);
        anim.addKeyframe("paw_back_right", 0.3, 0, 0, 0);
        anim.addKeyframe("paw_back_right", 0.4, 0, 0, -15);

        return anim;
    }

    /**
     * Creates an attack/bite animation.
     * Quick lunge forward with head strike.
     *
     * Duration: 0.5 seconds (non-looping)
     *
     * @return Attack animation for quadruped skeleton
     */
    public static BoneAnimation createAttackAnimation() {
        BoneAnimation anim = new BoneAnimation("attack", 0.5, false);

        // === BODY - lunge forward ===
        anim.addKeyframe("body", 0.0, 0, 0, 0);
        anim.addKeyframe("body", 0.1, 0, 2, -5);     // Crouch preparation
        anim.addKeyframe("body", 0.25, -10, -3, 15); // Lunge forward
        anim.addKeyframe("body", 0.35, -5, 0, 10);   // Extended
        anim.addKeyframe("body", 0.5, 0, 0, 0);      // Return

        // === NECK - extend forward ===
        anim.addKeyframe("neck", 0.0, 0, 0, 0);
        anim.addKeyframe("neck", 0.1, 0, 0, 5);      // Pull back
        anim.addKeyframe("neck", 0.25, -5, -2, -20); // Thrust forward
        anim.addKeyframe("neck", 0.35, -3, 0, -15);
        anim.addKeyframe("neck", 0.5, 0, 0, 0);

        // === HEAD - strike motion ===
        anim.addKeyframe("head", 0.0, 0, 0, 0);
        anim.addKeyframe("head", 0.1, 0, 0, 10);     // Pull back
        anim.addKeyframe("head", 0.2, 0, 0, -15);    // Quick strike
        anim.addKeyframe("head", 0.3, 0, 0, -25);    // Full extension
        anim.addKeyframe("head", 0.4, 0, 0, -10);
        anim.addKeyframe("head", 0.5, 0, 0, 0);

        // === EARS - alert position ===
        anim.addKeyframe("ear_left", 0.0, 0, 0, 0);
        anim.addKeyframe("ear_left", 0.1, 0, 0, -15);
        anim.addKeyframe("ear_left", 0.5, 0, 0, 0);

        anim.addKeyframe("ear_right", 0.0, 0, 0, 0);
        anim.addKeyframe("ear_right", 0.1, 0, 0, -15);
        anim.addKeyframe("ear_right", 0.5, 0, 0, 0);

        // === TAIL - raised aggressively ===
        anim.addKeyframe("tail_base", 0.0, 0, 0, 0);
        anim.addKeyframe("tail_base", 0.1, 0, 0, -20);
        anim.addKeyframe("tail_base", 0.3, 0, 0, -25);
        anim.addKeyframe("tail_base", 0.5, 0, 0, 0);

        anim.addKeyframe("tail_tip", 0.0, 0, 0, 0);
        anim.addKeyframe("tail_tip", 0.1, 0, 0, -10);
        anim.addKeyframe("tail_tip", 0.3, 0, 0, -15);
        anim.addKeyframe("tail_tip", 0.5, 0, 0, 0);

        // === FRONT LEGS - lunge forward ===
        anim.addKeyframe("leg_front_left_upper", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_front_left_upper", 0.1, 0, 0, 20);   // Crouch
        anim.addKeyframe("leg_front_left_upper", 0.25, 0, 0, -30); // Extended
        anim.addKeyframe("leg_front_left_upper", 0.5, 0, 0, 0);

        anim.addKeyframe("leg_front_left_lower", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_front_left_lower", 0.1, 0, 0, -30);
        anim.addKeyframe("leg_front_left_lower", 0.25, 0, 0, 0);
        anim.addKeyframe("leg_front_left_lower", 0.5, 0, 0, 0);

        anim.addKeyframe("leg_front_right_upper", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_front_right_upper", 0.1, 0, 0, 20);
        anim.addKeyframe("leg_front_right_upper", 0.25, 0, 0, -25);
        anim.addKeyframe("leg_front_right_upper", 0.5, 0, 0, 0);

        anim.addKeyframe("leg_front_right_lower", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_front_right_lower", 0.1, 0, 0, -25);
        anim.addKeyframe("leg_front_right_lower", 0.25, 0, 0, 0);
        anim.addKeyframe("leg_front_right_lower", 0.5, 0, 0, 0);

        // === BACK LEGS - push off ===
        anim.addKeyframe("leg_back_left_upper", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_back_left_upper", 0.1, 0, 0, -20);
        anim.addKeyframe("leg_back_left_upper", 0.25, 0, 0, 35);   // Push
        anim.addKeyframe("leg_back_left_upper", 0.5, 0, 0, 0);

        anim.addKeyframe("leg_back_left_lower", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_back_left_lower", 0.1, 0, 0, 20);
        anim.addKeyframe("leg_back_left_lower", 0.25, 0, 0, -10);
        anim.addKeyframe("leg_back_left_lower", 0.5, 0, 0, 0);

        anim.addKeyframe("leg_back_right_upper", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_back_right_upper", 0.1, 0, 0, -20);
        anim.addKeyframe("leg_back_right_upper", 0.25, 0, 0, 30);
        anim.addKeyframe("leg_back_right_upper", 0.5, 0, 0, 0);

        anim.addKeyframe("leg_back_right_lower", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_back_right_lower", 0.1, 0, 0, 20);
        anim.addKeyframe("leg_back_right_lower", 0.25, 0, 0, -10);
        anim.addKeyframe("leg_back_right_lower", 0.5, 0, 0, 0);

        // Paws (simplified)
        addPawKeyframes(anim, 0.0, 0, 0.5);

        return anim;
    }

    /**
     * Creates a hurt/damage reaction animation.
     *
     * Duration: 0.3 seconds (non-looping)
     *
     * @return Hurt animation for quadruped skeleton
     */
    public static BoneAnimation createHurtAnimation() {
        BoneAnimation anim = new BoneAnimation("hurt", 0.3, false);

        // Body recoil
        anim.addKeyframe("body", 0.0, 0, 0, 0);
        anim.addKeyframe("body", 0.1, 3, 2, -10);   // Recoil back
        anim.addKeyframe("body", 0.2, 2, 1, -5);
        anim.addKeyframe("body", 0.3, 0, 0, 0);

        // Head thrown back
        anim.addKeyframe("head", 0.0, 0, 0, 0);
        anim.addKeyframe("head", 0.1, 0, -2, 20);
        anim.addKeyframe("head", 0.3, 0, 0, 0);

        anim.addKeyframe("neck", 0.0, 0, 0, 0);
        anim.addKeyframe("neck", 0.1, 0, 0, 15);
        anim.addKeyframe("neck", 0.3, 0, 0, 0);

        // Ears flatten
        anim.addKeyframe("ear_left", 0.0, 0, 0, 0);
        anim.addKeyframe("ear_left", 0.1, 0, 0, -25);
        anim.addKeyframe("ear_left", 0.3, 0, 0, 0);

        anim.addKeyframe("ear_right", 0.0, 0, 0, 0);
        anim.addKeyframe("ear_right", 0.1, 0, 0, -25);
        anim.addKeyframe("ear_right", 0.3, 0, 0, 0);

        // Tail drops
        anim.addKeyframe("tail_base", 0.0, 0, 0, 0);
        anim.addKeyframe("tail_base", 0.1, 0, 0, 30);
        anim.addKeyframe("tail_base", 0.3, 0, 0, 0);

        anim.addKeyframe("tail_tip", 0.0, 0, 0, 0);
        anim.addKeyframe("tail_tip", 0.1, 0, 0, 20);
        anim.addKeyframe("tail_tip", 0.3, 0, 0, 0);

        // Legs buckle slightly
        String[] legs = {"leg_front_left", "leg_front_right", "leg_back_left", "leg_back_right"};
        for (String leg : legs) {
            anim.addKeyframe(leg + "_upper", 0.0, 0, 0, 0);
            anim.addKeyframe(leg + "_upper", 0.1, 0, 0, 10);
            anim.addKeyframe(leg + "_upper", 0.3, 0, 0, 0);

            anim.addKeyframe(leg + "_lower", 0.0, 0, 0, 0);
            anim.addKeyframe(leg + "_lower", 0.1, 0, 0, -15);
            anim.addKeyframe(leg + "_lower", 0.3, 0, 0, 0);
        }

        return anim;
    }

    /**
     * Creates a death animation.
     *
     * Duration: 1.0 seconds (non-looping)
     *
     * @return Death animation for quadruped skeleton
     */
    public static BoneAnimation createDeathAnimation() {
        BoneAnimation anim = new BoneAnimation("death", 1.0, false);

        // Body falls to side
        anim.addKeyframe("body", 0.0, 0, 0, 0);
        anim.addKeyframe("body", 0.3, 2, 5, -20);
        anim.addKeyframe("body", 0.6, 5, 15, -45);
        anim.addKeyframe("body", 1.0, 8, 20, -90);   // On side

        // Head drops
        anim.addKeyframe("head", 0.0, 0, 0, 0);
        anim.addKeyframe("head", 0.3, 0, 0, 30);
        anim.addKeyframe("head", 1.0, 0, 5, 45);

        anim.addKeyframe("neck", 0.0, 0, 0, 0);
        anim.addKeyframe("neck", 0.3, 0, 0, 20);
        anim.addKeyframe("neck", 1.0, 0, 0, 30);

        // Ears droop
        anim.addKeyframe("ear_left", 0.0, 0, 0, 0);
        anim.addKeyframe("ear_left", 1.0, 0, 0, 45);

        anim.addKeyframe("ear_right", 0.0, 0, 0, 0);
        anim.addKeyframe("ear_right", 1.0, 0, 0, -30);

        // Tail limp
        anim.addKeyframe("tail_base", 0.0, 0, 0, 0);
        anim.addKeyframe("tail_base", 1.0, 0, 0, 60);

        anim.addKeyframe("tail_tip", 0.0, 0, 0, 0);
        anim.addKeyframe("tail_tip", 1.0, 0, 0, 30);

        // Legs splay out
        anim.addKeyframe("leg_front_left_upper", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_front_left_upper", 1.0, 0, 0, 45);
        anim.addKeyframe("leg_front_left_lower", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_front_left_lower", 1.0, 0, 0, -30);

        anim.addKeyframe("leg_front_right_upper", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_front_right_upper", 1.0, 0, 0, -60);
        anim.addKeyframe("leg_front_right_lower", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_front_right_lower", 1.0, 0, 0, 20);

        anim.addKeyframe("leg_back_left_upper", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_back_left_upper", 1.0, 0, 0, 30);
        anim.addKeyframe("leg_back_left_lower", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_back_left_lower", 1.0, 0, 0, -20);

        anim.addKeyframe("leg_back_right_upper", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_back_right_upper", 1.0, 0, 0, -45);
        anim.addKeyframe("leg_back_right_lower", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_back_right_lower", 1.0, 0, 0, 15);

        return anim;
    }

    // ==================== Helper Methods ====================

    /**
     * Adds static leg keyframes (for idle standing).
     */
    private static void addLegKeyframes(BoneAnimation anim, String legPrefix,
                                        double rotation, double lower, double paw) {
        anim.addKeyframe(legPrefix + "_upper", 0.0, 0, 0, rotation);
        anim.addKeyframe(legPrefix + "_upper", 3.0, 0, 0, rotation);

        anim.addKeyframe(legPrefix + "_lower", 0.0, 0, 0, lower);
        anim.addKeyframe(legPrefix + "_lower", 3.0, 0, 0, lower);

        anim.addKeyframe("paw_" + legPrefix.substring(4), 0.0, 0, 0, paw);
        anim.addKeyframe("paw_" + legPrefix.substring(4), 3.0, 0, 0, paw);
    }

    /**
     * Adds simple paw keyframes for all paws.
     */
    private static void addPawKeyframes(BoneAnimation anim, double start, double mid, double end) {
        String[] paws = {"paw_front_left", "paw_front_right", "paw_back_left", "paw_back_right"};
        for (String paw : paws) {
            anim.addKeyframe(paw, 0.0, 0, 0, 0);
            anim.addKeyframe(paw, 0.25, 0, 0, 0);
            anim.addKeyframe(paw, 0.5, 0, 0, 0);
        }
    }

    /**
     * Adds all standard quadruped animations to a skeleton.
     *
     * @param skeleton The quadruped skeleton to add animations to
     */
    public static void addAllAnimations(Skeleton skeleton) {
        skeleton.addAnimation(createIdleAnimation());
        skeleton.addAnimation(createWalkAnimation());
        skeleton.addAnimation(createRunAnimation());
        skeleton.addAnimation(createAttackAnimation());
        skeleton.addAnimation(createHurtAnimation());
        skeleton.addAnimation(createDeathAnimation());
    }
}
