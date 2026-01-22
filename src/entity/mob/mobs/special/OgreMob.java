package entity.mob.mobs.special;

import entity.mob.SpriteMobEntity;

/**
 * Ogre mob - a large, powerful brute.
 *
 * Ogres are massive, slow creatures with tremendous strength. They
 * can serve as mini-bosses or dangerous elite enemies.
 *
 * Stats:
 * - Health: 100
 * - Damage: 20
 * - Walk Speed: 25
 * - Chase Speed: 50
 *
 * Special Traits:
 * - Large body type
 * - Very high health and damage
 * - Slow movement
 * - Ground pound attack (future)
 */
public class OgreMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/ogre";

    /**
     * Creates an ogre at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public OgreMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates an ogre with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing ogre sprites
     */
    public OgreMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures ogre-specific stats.
     */
    private void configureStats() {
        // Health (mini-boss level)
        this.maxHealth = 100;
        this.currentHealth = maxHealth;

        // Combat (devastating swings)
        this.attackDamage = 20;
        this.attackRange = 90;
        this.attackCooldown = 2.0;

        // Movement (very slow)
        this.wanderSpeed = 25;
        this.chaseSpeed = 50;

        // Detection
        this.detectionRange = 250;
        this.loseTargetRange = 400;

        // Ogres are always hostile
        setHostile(true);

        // Set body type to large
        setBodyType(MobBodyType.LARGE);
    }

    /**
     * Ogres take reduced knockback due to their massive size.
     */
    @Override
    public void takeDamage(int damage, double knockbackX, double knockbackY) {
        // Ogres take minimal knockback
        super.takeDamage(damage, knockbackX * 0.3, knockbackY * 0.3);
    }

    /**
     * Gets the mob type identifier.
     * @return "ogre"
     */
    public String getMobType() {
        return "ogre";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A hulking brute with immense strength and a bad temper.";
    }
}
