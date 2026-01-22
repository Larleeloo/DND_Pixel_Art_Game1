package entity.mob.mobs.quadruped;

import entity.mob.SpriteMobEntity;

/**
 * Cow mob - a passive farm animal.
 *
 * Cows are passive creatures found on farms. They wander slowly and
 * don't engage in combat. They can be a source of food.
 *
 * Stats:
 * - Health: 40
 * - Damage: 3
 * - Walk Speed: 50
 * - Chase Speed: 80
 *
 * Special Traits:
 * - Passive behavior
 * - Slow movement
 * - Drops food when defeated
 */
public class CowMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/cow";

    /**
     * Creates a cow at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public CowMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a cow with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing cow sprites
     */
    public CowMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures cow-specific stats.
     */
    private void configureStats() {
        // Health (moderate)
        this.maxHealth = 40;
        this.currentHealth = maxHealth;

        // Combat (weak)
        this.attackDamage = 3;
        this.attackRange = 50;
        this.attackCooldown = 2.0;

        // Movement (slow)
        this.wanderSpeed = 50;
        this.chaseSpeed = 80;

        // Detection
        this.detectionRange = 0; // Passive
        this.loseTargetRange = 50;

        // Cows are passive
        setHostile(false);

        // Set body type to quadruped
        setBodyType(MobBodyType.QUADRUPED);
    }

    /**
     * Gets the mob type identifier.
     * @return "cow"
     */
    public String getMobType() {
        return "cow";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A docile farm animal that grazes peacefully.";
    }
}
