package entity.mob.mobs.quadruped;

import entity.mob.SpriteMobEntity;

/**
 * Sheep mob - a passive farm animal.
 *
 * Sheep are passive creatures found on farms and grasslands. They
 * provide wool and food. They tend to stay in groups.
 *
 * Stats:
 * - Health: 35
 * - Damage: 2
 * - Walk Speed: 45
 * - Chase Speed: 70
 *
 * Special Traits:
 * - Passive behavior
 * - Herding behavior (stays near other sheep)
 * - Drops wool when sheared
 */
public class SheepMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/sheep";

    /**
     * Creates a sheep at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public SheepMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a sheep with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing sheep sprites
     */
    public SheepMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures sheep-specific stats.
     */
    private void configureStats() {
        // Health (low-moderate)
        this.maxHealth = 35;
        this.currentHealth = maxHealth;

        // Combat (very weak)
        this.attackDamage = 2;
        this.attackRange = 40;
        this.attackCooldown = 2.0;

        // Movement (moderate)
        this.wanderSpeed = 45;
        this.chaseSpeed = 70;

        // Detection
        this.detectionRange = 0; // Passive
        this.loseTargetRange = 50;

        // Sheep are passive
        setHostile(false);

        // Set body type to quadruped
        setBodyType(MobBodyType.QUADRUPED);
    }

    /**
     * Gets the mob type identifier.
     * @return "sheep"
     */
    public String getMobType() {
        return "sheep";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A fluffy sheep, perfect for shearing.";
    }
}
