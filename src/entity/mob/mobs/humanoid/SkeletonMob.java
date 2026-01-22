package entity.mob.mobs.humanoid;

import entity.mob.SpriteMobEntity;
import entity.ProjectileEntity;

/**
 * Skeleton mob - a fast, ranged undead enemy.
 *
 * Skeletons are faster than zombies and can attack from range with bows.
 * They have lower health but make up for it with superior speed and
 * the ability to engage from a distance.
 *
 * Stats:
 * - Health: 40
 * - Damage: 6 (melee), 8 (ranged)
 * - Walk Speed: 50
 * - Chase Speed: 80
 *
 * Special Traits:
 * - Ranged bow attacks
 * - Fast movement
 * - Lower health than zombies
 */
public class SkeletonMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/skeleton";

    // Whether this skeleton has a bow
    private boolean hasRangedAttack = true;

    /**
     * Creates a skeleton at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public SkeletonMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a skeleton with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing skeleton sprites
     */
    public SkeletonMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures skeleton-specific stats.
     */
    private void configureStats() {
        // Health (lower than zombie)
        this.maxHealth = 40;
        this.currentHealth = maxHealth;

        // Combat
        this.attackDamage = 6;
        this.attackRange = 50;
        this.attackCooldown = 1.0;

        // Movement (faster than zombie)
        this.wanderSpeed = 50;
        this.chaseSpeed = 80;

        // Detection
        this.detectionRange = 300;
        this.loseTargetRange = 500;

        // Configure ranged attack if enabled
        if (hasRangedAttack) {
            setRangedAttack(
                ProjectileEntity.ProjectileType.ARROW,
                8,      // damage
                10.0,   // projectile speed
                2.0,    // cooldown
                250     // preferred attack range
            );
        }

        // Skeletons are always hostile
        setHostile(true);
    }

    /**
     * Enables or disables the skeleton's ranged attack.
     *
     * @param enabled true to enable bow attacks
     */
    public void setRangedAttackEnabled(boolean enabled) {
        this.hasRangedAttack = enabled;
        if (enabled) {
            setRangedAttack(
                ProjectileEntity.ProjectileType.ARROW,
                8, 10.0, 2.0, 250
            );
        } else {
            this.canFireProjectiles = false;
        }
    }

    /**
     * Gets the mob type identifier.
     * @return "skeleton"
     */
    public String getMobType() {
        return "skeleton";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "An animated skeleton archer, fast and deadly from a distance.";
    }
}
