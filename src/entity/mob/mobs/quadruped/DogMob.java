package entity.mob.mobs.quadruped;

import entity.mob.SpriteMobEntity;

/**
 * Dog mob - a friendly, domesticated companion.
 *
 * Dogs are passive creatures that can be found in villages and with NPCs.
 * They are fast and can be tamed as companions. They only attack if
 * their owner is threatened.
 *
 * Stats:
 * - Health: 35
 * - Damage: 4
 * - Walk Speed: 70
 * - Chase Speed: 140
 *
 * Special Traits:
 * - Passive behavior
 * - Can be tamed (future feature)
 * - Fast movement
 */
public class DogMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/dog";

    /**
     * Creates a dog at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public DogMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a dog with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing dog sprites
     */
    public DogMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures dog-specific stats.
     */
    private void configureStats() {
        // Health (moderate)
        this.maxHealth = 35;
        this.currentHealth = maxHealth;

        // Combat (weak but fast)
        this.attackDamage = 4;
        this.attackRange = 45;
        this.attackCooldown = 0.6;

        // Movement (very fast)
        this.wanderSpeed = 70;
        this.chaseSpeed = 140;

        // Detection
        this.detectionRange = 0; // Passive - doesn't chase
        this.loseTargetRange = 100;

        // Dogs are passive
        setHostile(false);

        // Set body type to quadruped
        setBodyType(MobBodyType.QUADRUPED);
    }

    /**
     * Gets the mob type identifier.
     * @return "dog"
     */
    public String getMobType() {
        return "dog";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A loyal companion, always happy to see you.";
    }
}
