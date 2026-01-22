package entity.mob.mobs.quadruped;

import entity.mob.SpriteMobEntity;

/**
 * Horse mob - a fast, rideable animal.
 *
 * Horses are passive creatures found in stables and wild grasslands.
 * They are very fast and can potentially be tamed as mounts.
 *
 * Stats:
 * - Health: 50
 * - Damage: 4
 * - Walk Speed: 60
 * - Chase Speed: 120
 *
 * Special Traits:
 * - Passive behavior
 * - Very fast movement
 * - Can be tamed as mount (future feature)
 */
public class HorseMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/horse";

    /**
     * Creates a horse at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public HorseMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a horse with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing horse sprites
     */
    public HorseMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures horse-specific stats.
     */
    private void configureStats() {
        // Health (moderate)
        this.maxHealth = 50;
        this.currentHealth = maxHealth;

        // Combat (hooves kick)
        this.attackDamage = 4;
        this.attackRange = 60;
        this.attackCooldown = 1.5;

        // Movement (fast)
        this.wanderSpeed = 60;
        this.chaseSpeed = 120;

        // Detection
        this.detectionRange = 0; // Passive
        this.loseTargetRange = 80;

        // Horses are passive
        setHostile(false);

        // Set body type to quadruped
        setBodyType(MobBodyType.QUADRUPED);
    }

    /**
     * Gets the mob type identifier.
     * @return "horse"
     */
    public String getMobType() {
        return "horse";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A noble steed, swift and graceful.";
    }
}
