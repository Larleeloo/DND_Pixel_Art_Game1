package entity.mob.mobs.quadruped;

import entity.mob.SpriteMobEntity;

/**
 * Wolf mob - a fast, hostile pack predator.
 *
 * Wolves are fast and aggressive. They hunt in packs and are particularly
 * dangerous at night. They have moderate health but can chase down most
 * targets.
 *
 * Stats:
 * - Health: 45
 * - Damage: 6
 * - Walk Speed: 60
 * - Chase Speed: 150 (very fast)
 *
 * Special Traits:
 * - Very fast chase speed
 * - Pack behavior (future: aggro nearby wolves when attacked)
 * - Nocturnal hunter
 */
public class WolfMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/wolf";

    /**
     * Creates a wolf at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public WolfMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a wolf with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing wolf sprites
     */
    public WolfMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures wolf-specific stats.
     */
    private void configureStats() {
        // Health
        this.maxHealth = 45;
        this.currentHealth = maxHealth;

        // Combat (quick bites)
        this.attackDamage = 6;
        this.attackRange = 55;
        this.attackCooldown = 0.8;

        // Movement (very fast)
        this.wanderSpeed = 60;
        this.chaseSpeed = 150;

        // Detection (keen senses)
        this.detectionRange = 350;
        this.loseTargetRange = 500;

        // Wolves are hostile
        setHostile(true);

        // Set body type to quadruped
        setBodyType(MobBodyType.QUADRUPED);
    }

    /**
     * Gets the mob type identifier.
     * @return "wolf"
     */
    public String getMobType() {
        return "wolf";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A fierce predator that hunts in packs.";
    }
}
