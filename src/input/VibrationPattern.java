package input;

/**
 * VibrationPattern defines various haptic feedback patterns for the Xbox controller.
 * Each pattern specifies the intensity (0.0 to 1.0) and duration (milliseconds) of
 * vibration pulses, as well as whether they use the left (low frequency) or right
 * (high frequency) motor, or both.
 *
 * MINOR patterns are subtle feedback for common interactions:
 * - Light pulses for item pickups, UI confirmations, footsteps
 * - Low intensity (0.1-0.3), short duration (50-150ms)
 *
 * GREATER patterns are more pronounced for significant events:
 * - Strong pulses for damage, attacks, jumps, chest opening
 * - Higher intensity (0.4-1.0), longer duration (100-500ms)
 *
 * INTRICATE patterns are complex multi-pulse sequences:
 * - Used for special events like loot chest opening
 * - Combine multiple intensities and durations for unique feel
 *
 * Motor Types:
 * - LEFT_MOTOR: Low frequency rumble (bass), good for impacts
 * - RIGHT_MOTOR: High frequency rumble (treble), good for feedback
 * - BOTH: Simultaneous activation for powerful effects
 */
public enum VibrationPattern {

    // ==================== MINOR PATTERNS ====================
    // Subtle haptic feedback for common interactions

    /**
     * Very light tap for UI selections and confirmations.
     */
    MINOR_UI_CLICK(0.15f, 40, MotorType.RIGHT),

    /**
     * Light pulse for collecting items.
     */
    MINOR_ITEM_PICKUP(0.2f, 80, MotorType.RIGHT),

    /**
     * Gentle feedback for hotbar slot changes.
     */
    MINOR_HOTBAR_SWITCH(0.12f, 50, MotorType.RIGHT),

    /**
     * Soft pulse for footsteps (walking).
     */
    MINOR_FOOTSTEP_WALK(0.08f, 30, MotorType.LEFT),

    /**
     * Slightly stronger pulse for running footsteps.
     */
    MINOR_FOOTSTEP_RUN(0.12f, 40, MotorType.LEFT),

    /**
     * Light feedback for inventory navigation.
     */
    MINOR_INVENTORY_NAVIGATE(0.1f, 35, MotorType.RIGHT),

    /**
     * Subtle pulse for equipping items.
     */
    MINOR_EQUIP_ITEM(0.18f, 60, MotorType.RIGHT),

    /**
     * Light rumble for eating/drinking.
     */
    MINOR_CONSUME_ITEM(0.15f, 100, MotorType.RIGHT),

    /**
     * Gentle feedback for block placement.
     */
    MINOR_BLOCK_PLACE(0.2f, 70, MotorType.LEFT),

    // ==================== GREATER PATTERNS ====================
    // More pronounced feedback for significant events

    /**
     * Strong impact for taking damage.
     */
    GREATER_DAMAGE_TAKEN(0.7f, 200, MotorType.BOTH),

    /**
     * Powerful pulse for critical damage.
     */
    GREATER_CRITICAL_DAMAGE(0.9f, 300, MotorType.BOTH),

    /**
     * Strong feedback for melee attack execution.
     */
    GREATER_MELEE_ATTACK(0.5f, 100, MotorType.LEFT),

    /**
     * Sharp pulse for ranged weapon fire.
     */
    GREATER_RANGED_FIRE(0.4f, 80, MotorType.RIGHT),

    /**
     * Impact feedback for projectile hit.
     */
    GREATER_PROJECTILE_HIT(0.6f, 120, MotorType.BOTH),

    /**
     * Strong rumble for jumping.
     */
    GREATER_JUMP(0.35f, 80, MotorType.LEFT),

    /**
     * Stronger rumble for double jump.
     */
    GREATER_DOUBLE_JUMP(0.45f, 100, MotorType.LEFT),

    /**
     * Powerful rumble for triple jump.
     */
    GREATER_TRIPLE_JUMP(0.55f, 120, MotorType.LEFT),

    /**
     * Impact rumble for landing from a fall.
     */
    GREATER_LAND_IMPACT(0.4f, 100, MotorType.LEFT),

    /**
     * Strong rumble for heavy landing (after long fall).
     */
    GREATER_LAND_HEAVY(0.7f, 180, MotorType.BOTH),

    /**
     * Powerful rumble for blocking with shield.
     */
    GREATER_BLOCK_SHIELD(0.5f, 150, MotorType.BOTH),

    /**
     * Impact for breaking blocks while mining.
     */
    GREATER_BLOCK_BREAK(0.4f, 90, MotorType.LEFT),

    /**
     * Strong pulse for killing an enemy.
     */
    GREATER_ENEMY_KILLED(0.55f, 150, MotorType.BOTH),

    /**
     * Powerful rumble for explosions.
     */
    GREATER_EXPLOSION(0.9f, 250, MotorType.BOTH),

    /**
     * Strong feedback for status effect application (burning, freezing).
     */
    GREATER_STATUS_EFFECT(0.45f, 180, MotorType.RIGHT),

    /**
     * Powerful pulse for level up or achievement.
     */
    GREATER_LEVEL_UP(0.6f, 200, MotorType.BOTH),

    /**
     * Strong rumble for door opening.
     */
    GREATER_DOOR_OPEN(0.35f, 120, MotorType.LEFT),

    // ==================== INTRICATE PATTERNS ====================
    // Complex multi-pulse sequences for special events

    /**
     * Intricate pattern for daily loot chest opening.
     * Creates anticipation with building pulses followed by a satisfying burst.
     */
    LOOT_CHEST_DAILY(new VibrationStep[] {
        // Build-up phase: Escalating small pulses
        new VibrationStep(0.15f, 60, MotorType.RIGHT),   // Soft start
        new VibrationStep(0.0f, 80, MotorType.BOTH),     // Pause
        new VibrationStep(0.2f, 70, MotorType.RIGHT),    // Slightly stronger
        new VibrationStep(0.0f, 70, MotorType.BOTH),     // Pause
        new VibrationStep(0.3f, 80, MotorType.BOTH),     // Building
        new VibrationStep(0.0f, 60, MotorType.BOTH),     // Short pause
        // Climax: Strong opening burst
        new VibrationStep(0.6f, 150, MotorType.BOTH),    // Main opening burst
        new VibrationStep(0.3f, 100, MotorType.LEFT),    // Echo rumble
        // Resolution: Light confirmation
        new VibrationStep(0.15f, 80, MotorType.RIGHT)    // Soft finish
    }),

    /**
     * Intricate pattern for monthly loot chest opening.
     * Even more dramatic than daily, with longer build-up and stronger burst.
     */
    LOOT_CHEST_MONTHLY(new VibrationStep[] {
        // Phase 1: Initial awakening
        new VibrationStep(0.1f, 40, MotorType.RIGHT),    // Whisper
        new VibrationStep(0.0f, 100, MotorType.BOTH),    // Silence
        new VibrationStep(0.15f, 50, MotorType.RIGHT),   // Gentle start
        new VibrationStep(0.0f, 80, MotorType.BOTH),     // Pause

        // Phase 2: Building anticipation
        new VibrationStep(0.2f, 60, MotorType.LEFT),     // Low rumble
        new VibrationStep(0.1f, 40, MotorType.RIGHT),    // High accent
        new VibrationStep(0.3f, 70, MotorType.BOTH),     // Combined
        new VibrationStep(0.0f, 60, MotorType.BOTH),     // Brief pause
        new VibrationStep(0.35f, 80, MotorType.BOTH),    // Growing
        new VibrationStep(0.0f, 50, MotorType.BOTH),     // Quick pause

        // Phase 3: Pre-climax tension
        new VibrationStep(0.45f, 90, MotorType.LEFT),    // Deep rumble
        new VibrationStep(0.4f, 60, MotorType.RIGHT),    // High tension
        new VibrationStep(0.55f, 100, MotorType.BOTH),   // Peak build
        new VibrationStep(0.0f, 40, MotorType.BOTH),     // Dramatic pause

        // Phase 4: Grand opening burst
        new VibrationStep(0.9f, 200, MotorType.BOTH),    // MASSIVE burst
        new VibrationStep(0.6f, 120, MotorType.LEFT),    // Rolling thunder
        new VibrationStep(0.4f, 100, MotorType.RIGHT),   // Sparkling

        // Phase 5: Magical resolution
        new VibrationStep(0.25f, 80, MotorType.BOTH),    // Settling
        new VibrationStep(0.15f, 60, MotorType.RIGHT),   // Magic shimmer
        new VibrationStep(0.1f, 100, MotorType.RIGHT)    // Fade to wonder
    }),

    /**
     * Intricate pattern for collecting rare/legendary items.
     * Celebratory pattern with triumphant pulses.
     */
    LOOT_LEGENDARY_ITEM(new VibrationStep[] {
        new VibrationStep(0.4f, 80, MotorType.BOTH),     // Initial impact
        new VibrationStep(0.0f, 50, MotorType.BOTH),     // Pause
        new VibrationStep(0.5f, 100, MotorType.BOTH),    // Confirmation
        new VibrationStep(0.3f, 80, MotorType.RIGHT),    // Sparkle
        new VibrationStep(0.2f, 100, MotorType.RIGHT)    // Glow
    }),

    /**
     * Intricate pattern for mythic item discovery.
     * Even more impressive than legendary.
     */
    LOOT_MYTHIC_ITEM(new VibrationStep[] {
        new VibrationStep(0.3f, 60, MotorType.RIGHT),    // Shimmer
        new VibrationStep(0.5f, 100, MotorType.BOTH),    // Wave
        new VibrationStep(0.0f, 40, MotorType.BOTH),     // Pause
        new VibrationStep(0.7f, 150, MotorType.BOTH),    // Cosmic impact
        new VibrationStep(0.5f, 100, MotorType.LEFT),    // Deep resonance
        new VibrationStep(0.3f, 80, MotorType.RIGHT),    // Star dust
        new VibrationStep(0.4f, 120, MotorType.BOTH),    // Magic pulse
        new VibrationStep(0.2f, 150, MotorType.RIGHT)    // Ethereal fade
    }),

    /**
     * Pattern for player death - dramatic and final.
     */
    PLAYER_DEATH(new VibrationStep[] {
        new VibrationStep(1.0f, 300, MotorType.BOTH),    // Massive hit
        new VibrationStep(0.6f, 200, MotorType.LEFT),    // Fading
        new VibrationStep(0.3f, 200, MotorType.LEFT),    // Weakening
        new VibrationStep(0.1f, 300, MotorType.LEFT)     // Final fade
    }),

    /**
     * Pattern for boss encounter start.
     */
    BOSS_ENCOUNTER(new VibrationStep[] {
        new VibrationStep(0.3f, 100, MotorType.LEFT),    // Rumble
        new VibrationStep(0.0f, 100, MotorType.BOTH),    // Pause
        new VibrationStep(0.4f, 120, MotorType.LEFT),    // Growing
        new VibrationStep(0.0f, 100, MotorType.BOTH),    // Pause
        new VibrationStep(0.6f, 150, MotorType.BOTH),    // Building
        new VibrationStep(0.0f, 100, MotorType.BOTH),    // Dramatic pause
        new VibrationStep(0.8f, 300, MotorType.BOTH)     // BOSS ROAR
    }),

    /**
     * Pattern for vault/chest unlocking (without loot drop).
     */
    VAULT_UNLOCK(new VibrationStep[] {
        new VibrationStep(0.2f, 50, MotorType.RIGHT),    // Key turn 1
        new VibrationStep(0.0f, 80, MotorType.BOTH),     // Pause
        new VibrationStep(0.25f, 60, MotorType.RIGHT),   // Key turn 2
        new VibrationStep(0.0f, 100, MotorType.BOTH),    // Pause
        new VibrationStep(0.4f, 120, MotorType.LEFT),    // Lock mechanism
        new VibrationStep(0.2f, 80, MotorType.BOTH)      // Door swing
    }),

    /**
     * Pattern for alchemy crafting success.
     */
    ALCHEMY_SUCCESS(new VibrationStep[] {
        new VibrationStep(0.2f, 60, MotorType.RIGHT),    // Bubble
        new VibrationStep(0.15f, 50, MotorType.RIGHT),   // Bubble
        new VibrationStep(0.25f, 70, MotorType.RIGHT),   // Bigger bubble
        new VibrationStep(0.0f, 60, MotorType.BOTH),     // Pause
        new VibrationStep(0.5f, 150, MotorType.BOTH),    // Transformation
        new VibrationStep(0.2f, 100, MotorType.RIGHT)    // Magic settle
    });

    // ==================== MOTOR TYPES ====================

    /**
     * Specifies which controller motor(s) to use for vibration.
     */
    public enum MotorType {
        LEFT,   // Low frequency motor (bass rumble)
        RIGHT,  // High frequency motor (fine vibration)
        BOTH    // Both motors simultaneously
    }

    // ==================== VIBRATION STEP ====================

    /**
     * A single step in a vibration sequence.
     * Used for complex multi-pulse patterns.
     */
    public static class VibrationStep {
        public final float intensity;
        public final int durationMs;
        public final MotorType motor;

        public VibrationStep(float intensity, int durationMs, MotorType motor) {
            this.intensity = Math.max(0.0f, Math.min(1.0f, intensity));
            this.durationMs = Math.max(0, durationMs);
            this.motor = motor;
        }
    }

    // ==================== PATTERN PROPERTIES ====================

    // Simple pattern properties (for single-pulse patterns)
    private final float intensity;
    private final int durationMs;
    private final MotorType motor;

    // Complex pattern properties (for multi-pulse sequences)
    private final VibrationStep[] steps;
    private final boolean isComplex;

    // ==================== CONSTRUCTORS ====================

    /**
     * Creates a simple single-pulse vibration pattern.
     */
    VibrationPattern(float intensity, int durationMs, MotorType motor) {
        this.intensity = Math.max(0.0f, Math.min(1.0f, intensity));
        this.durationMs = Math.max(0, durationMs);
        this.motor = motor;
        this.steps = null;
        this.isComplex = false;
    }

    /**
     * Creates a complex multi-pulse vibration pattern.
     */
    VibrationPattern(VibrationStep[] steps) {
        this.steps = steps;
        this.isComplex = true;
        // Calculate total duration and average intensity for simple accessors
        this.durationMs = calculateTotalDuration();
        this.intensity = calculateAverageIntensity();
        this.motor = MotorType.BOTH; // Default for complex patterns
    }

    // ==================== ACCESSORS ====================

    /**
     * Gets the vibration intensity (0.0 to 1.0).
     * For complex patterns, returns the average intensity.
     */
    public float getIntensity() {
        return intensity;
    }

    /**
     * Gets the duration in milliseconds.
     * For complex patterns, returns the total duration.
     */
    public int getDurationMs() {
        return durationMs;
    }

    /**
     * Gets the motor type to use.
     * For complex patterns, each step may use different motors.
     */
    public MotorType getMotor() {
        return motor;
    }

    /**
     * Checks if this is a complex multi-pulse pattern.
     */
    public boolean isComplex() {
        return isComplex;
    }

    /**
     * Gets the vibration steps for complex patterns.
     * Returns null for simple patterns.
     */
    public VibrationStep[] getSteps() {
        return steps;
    }

    /**
     * Gets the number of steps in this pattern.
     */
    public int getStepCount() {
        return isComplex ? steps.length : 1;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Calculates the total duration of a complex pattern.
     */
    private int calculateTotalDuration() {
        if (steps == null) return 0;
        int total = 0;
        for (VibrationStep step : steps) {
            total += step.durationMs;
        }
        return total;
    }

    /**
     * Calculates the average intensity of a complex pattern.
     */
    private float calculateAverageIntensity() {
        if (steps == null || steps.length == 0) return 0;
        float sum = 0;
        int nonZeroCount = 0;
        for (VibrationStep step : steps) {
            if (step.intensity > 0) {
                sum += step.intensity;
                nonZeroCount++;
            }
        }
        return nonZeroCount > 0 ? sum / nonZeroCount : 0;
    }

    /**
     * Gets a description of this vibration pattern.
     */
    public String getDescription() {
        if (isComplex) {
            return String.format("%s: %d steps, %dms total, avg intensity %.2f",
                name(), steps.length, durationMs, intensity);
        } else {
            return String.format("%s: %.2f intensity, %dms, %s motor",
                name(), intensity, durationMs, motor.name());
        }
    }
}
