package entity.mob.mobs.quadruped;

import entity.mob.SpriteMobEntity;

/**
 * Pig mob - a passive farm animal.
 *
 * Pigs are passive creatures found on farms. They are slower and
 * smaller than cows. They can be a source of food.
 *
 * Stats:
 * - Health: 30
 * - Damage: 2
 * - Walk Speed: 40
 * - Chase Speed: 60
 *
 * Special Traits:
 * - Passive behavior
 * - Low health
 * - Drops food when defeated
 */
public class PigMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/pig";

    /**
     * Creates a pig at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public PigMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a pig with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing pig sprites
     */
    public PigMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures pig-specific stats.
     */
    private void configureStats() {
        // Health (low)
        this.maxHealth = 30;
        this.currentHealth = maxHealth;

        // Combat (very weak)
        this.attackDamage = 2;
        this.attackRange = 40;
        this.attackCooldown = 2.0;

        // Movement (slow)
        this.wanderSpeed = 40;
        this.chaseSpeed = 60;

        // Detection
        this.detectionRange = 0; // Passive
        this.loseTargetRange = 50;

        // Pigs are passive
        setHostile(false);

        // Set body type to quadruped
        setBodyType(MobBodyType.QUADRUPED);
    }

    /**
     * Gets the mob type identifier.
     * @return "pig"
     */
    public String getMobType() {
        return "pig";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A plump farm pig, happily rooting around.";
    }
}
