package entity.mob.mobs.humanoid;

import entity.mob.SpriteMobEntity;

/**
 * Orc mob - a strong, tank-like enemy.
 *
 * Orcs are powerful warriors with high health and damage. They are
 * slower than most humanoids but hit hard and can take a lot of
 * punishment. They are often found leading goblin groups.
 *
 * Stats:
 * - Health: 60
 * - Damage: 15
 * - Walk Speed: 40
 * - Chase Speed: 60
 *
 * Special Traits:
 * - High health pool
 * - Heavy damage
 * - Slower movement
 */
public class OrcMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/orc";

    /**
     * Creates an orc at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public OrcMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates an orc with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing orc sprites
     */
    public OrcMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures orc-specific stats.
     */
    private void configureStats() {
        // Health (tank)
        this.maxHealth = 60;
        this.currentHealth = maxHealth;

        // Combat (heavy hitter)
        this.attackDamage = 15;
        this.attackRange = 70;
        this.attackCooldown = 1.5;

        // Movement (slow but steady)
        this.wanderSpeed = 40;
        this.chaseSpeed = 60;

        // Detection
        this.detectionRange = 200;
        this.loseTargetRange = 350;

        // Orcs are always hostile
        setHostile(true);
    }

    /**
     * Gets the mob type identifier.
     * @return "orc"
     */
    public String getMobType() {
        return "orc";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A hulking green-skinned warrior with immense strength.";
    }
}
