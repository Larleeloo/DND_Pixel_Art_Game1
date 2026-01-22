package entity.mob.mobs.quadruped;

import entity.mob.SpriteMobEntity;

/**
 * Fox mob - a cunning, neutral predator.
 *
 * Foxes are small, fast, and clever. They are neutral creatures that
 * hunt small prey but avoid larger threats. They are found in forests
 * and grasslands.
 *
 * Stats:
 * - Health: 30
 * - Damage: 5
 * - Walk Speed: 80
 * - Chase Speed: 130
 *
 * Special Traits:
 * - Neutral behavior
 * - Very fast and agile
 * - Will steal food items
 */
public class FoxMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/fox";

    /**
     * Creates a fox at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public FoxMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a fox with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing fox sprites
     */
    public FoxMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures fox-specific stats.
     */
    private void configureStats() {
        // Health (low)
        this.maxHealth = 30;
        this.currentHealth = maxHealth;

        // Combat (quick bites)
        this.attackDamage = 5;
        this.attackRange = 40;
        this.attackCooldown = 0.7;

        // Movement (very fast)
        this.wanderSpeed = 80;
        this.chaseSpeed = 130;

        // Detection
        this.detectionRange = 100; // Small detection range
        this.loseTargetRange = 200;

        // Foxes can double jump
        setMaxJumps(2);

        // Foxes are neutral
        setHostile(false);
        setAggroRange(80); // Will attack if provoked

        // Set body type to quadruped
        setBodyType(MobBodyType.QUADRUPED);
    }

    /**
     * Gets the mob type identifier.
     * @return "fox"
     */
    public String getMobType() {
        return "fox";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A clever and quick forest creature with a taste for mischief.";
    }
}
