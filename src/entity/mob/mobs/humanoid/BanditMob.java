package entity.mob.mobs.humanoid;

import entity.mob.SpriteMobEntity;

/**
 * Bandit mob - a balanced human enemy.
 *
 * Bandits are common enemies found on roads and in hideouts. They have
 * balanced stats and are dangerous in groups. Some bandits may have
 * ranged weapons.
 *
 * Stats:
 * - Health: 45
 * - Damage: 8
 * - Walk Speed: 50
 * - Chase Speed: 70
 *
 * Special Traits:
 * - Balanced stats
 * - Can be equipped with various weapons
 * - Often found in groups
 */
public class BanditMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/bandit";

    /**
     * Creates a bandit at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public BanditMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a bandit with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing bandit sprites
     */
    public BanditMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures bandit-specific stats.
     */
    private void configureStats() {
        // Health (moderate)
        this.maxHealth = 45;
        this.currentHealth = maxHealth;

        // Combat (balanced)
        this.attackDamage = 8;
        this.attackRange = 55;
        this.attackCooldown = 1.0;

        // Movement (moderate speed)
        this.wanderSpeed = 50;
        this.chaseSpeed = 70;

        // Detection
        this.detectionRange = 280;
        this.loseTargetRange = 450;

        // Bandits are always hostile
        setHostile(true);
    }

    /**
     * Gets the mob type identifier.
     * @return "bandit"
     */
    public String getMobType() {
        return "bandit";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A ruthless outlaw who preys on travelers.";
    }
}
