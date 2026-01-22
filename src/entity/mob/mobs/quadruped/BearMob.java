package entity.mob.mobs.quadruped;

import entity.mob.SpriteMobEntity;

/**
 * Bear mob - a powerful, neutral creature.
 *
 * Bears are large, powerful animals that are typically neutral until
 * provoked. They have high health and deal significant damage. Found
 * in forests and mountain areas.
 *
 * Stats:
 * - Health: 55
 * - Damage: 12
 * - Walk Speed: 40
 * - Chase Speed: 100
 *
 * Special Traits:
 * - High health pool
 * - Heavy damage
 * - Neutral behavior (attacks when provoked)
 */
public class BearMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/bear";

    /**
     * Creates a bear at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public BearMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a bear with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing bear sprites
     */
    public BearMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures bear-specific stats.
     */
    private void configureStats() {
        // Health (high)
        this.maxHealth = 55;
        this.currentHealth = maxHealth;

        // Combat (powerful swipes)
        this.attackDamage = 12;
        this.attackRange = 80;
        this.attackCooldown = 1.4;

        // Movement (moderate)
        this.wanderSpeed = 40;
        this.chaseSpeed = 100;

        // Detection
        this.detectionRange = 200;
        this.loseTargetRange = 350;

        // Bears are neutral by default
        setHostile(false);
        setAggroRange(100); // Will attack if you get too close

        // Set body type to quadruped
        setBodyType(MobBodyType.QUADRUPED);
    }

    /**
     * Gets the mob type identifier.
     * @return "bear"
     */
    public String getMobType() {
        return "bear";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A massive bear that prefers to be left alone.";
    }
}
