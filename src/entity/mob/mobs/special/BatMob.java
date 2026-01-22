package entity.mob.mobs.special;

import entity.mob.SpriteMobEntity;

/**
 * Bat mob - a small, flying creature.
 *
 * Bats are small, fast creatures found in caves and dungeons. They
 * swoop down to attack and are hard to hit due to their erratic
 * flight patterns.
 *
 * Stats:
 * - Health: 20
 * - Damage: 4
 * - Walk Speed: 50
 * - Chase Speed: 120
 *
 * Special Traits:
 * - Small body type
 * - Flying movement (ignores gravity - future)
 * - Erratic movement pattern
 */
public class BatMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/bat";

    /**
     * Creates a bat at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public BatMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a bat with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing bat sprites
     */
    public BatMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures bat-specific stats.
     */
    private void configureStats() {
        // Health (very low)
        this.maxHealth = 20;
        this.currentHealth = maxHealth;

        // Combat (quick bites)
        this.attackDamage = 4;
        this.attackRange = 35;
        this.attackCooldown = 0.8;

        // Movement (fast, erratic)
        this.wanderSpeed = 50;
        this.chaseSpeed = 120;

        // Detection
        this.detectionRange = 200;
        this.loseTargetRange = 350;

        // Bats can multi-jump to simulate flying
        setMaxJumps(3);

        // Bats are hostile
        setHostile(true);

        // Set body type to small
        setBodyType(MobBodyType.SMALL);
    }

    /**
     * Gets the mob type identifier.
     * @return "bat"
     */
    public String getMobType() {
        return "bat";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A nocturnal creature that swoops down from the darkness.";
    }
}
