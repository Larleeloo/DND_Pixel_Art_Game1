package entity.mob.mobs.special;

import entity.mob.SpriteMobEntity;
import entity.ProjectileEntity;

/**
 * Dragon mob - a powerful boss creature.
 *
 * Dragons are massive, powerful creatures that serve as bosses.
 * They have high health, deal massive damage, and can breathe fire.
 *
 * Stats:
 * - Health: 150
 * - Damage: 25
 * - Walk Speed: 30
 * - Chase Speed: 80
 *
 * Special Traits:
 * - Large body type
 * - Fire breath attack
 * - High health and damage
 * - Boss-level enemy
 */
public class DragonMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/dragon";

    /**
     * Dragon element type.
     */
    public enum DragonElement {
        FIRE,   // Fire breath
        ICE,    // Ice breath
        POISON  // Poison breath
    }

    private DragonElement element = DragonElement.FIRE;

    /**
     * Creates a dragon at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public DragonMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a dragon with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing dragon sprites
     */
    public DragonMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures dragon-specific stats.
     */
    private void configureStats() {
        // Health (boss-level)
        this.maxHealth = 150;
        this.currentHealth = maxHealth;

        // Combat (devastating attacks)
        this.attackDamage = 25;
        this.attackRange = 100;
        this.attackCooldown = 2.0;

        // Movement (slow but imposing)
        this.wanderSpeed = 30;
        this.chaseSpeed = 80;

        // Detection (wide range)
        this.detectionRange = 400;
        this.loseTargetRange = 600;

        // Configure breath attack
        updateBreathAttack();

        // Dragons are always hostile
        setHostile(true);

        // Set body type to large
        setBodyType(MobBodyType.LARGE);
    }

    /**
     * Sets the dragon's element type.
     *
     * @param element The element type
     */
    public void setElement(DragonElement element) {
        this.element = element;
        updateBreathAttack();
    }

    /**
     * Updates the breath attack based on element.
     */
    private void updateBreathAttack() {
        switch (element) {
            case ICE:
                setRangedAttack(
                    ProjectileEntity.ProjectileType.ICEBALL,
                    20,     // damage
                    12.0,   // projectile speed
                    3.0,    // cooldown
                    350     // preferred attack range
                );
                break;
            case POISON:
                setRangedAttack(
                    ProjectileEntity.ProjectileType.MAGIC_BOLT, // Use as poison bolt
                    15,     // damage
                    10.0,   // projectile speed
                    2.5,    // cooldown
                    300     // preferred attack range
                );
                break;
            case FIRE:
            default:
                setRangedAttack(
                    ProjectileEntity.ProjectileType.FIREBALL,
                    25,     // damage
                    10.0,   // projectile speed
                    3.0,    // cooldown
                    400     // preferred attack range
                );
                break;
        }
    }

    /**
     * Dragons take reduced knockback due to their size.
     */
    @Override
    public void takeDamage(int damage, double knockbackX, double knockbackY) {
        // Dragons take minimal knockback
        super.takeDamage(damage, knockbackX * 0.2, knockbackY * 0.2);
    }

    /**
     * Gets the dragon's element.
     * @return The element type
     */
    public DragonElement getElement() {
        return element;
    }

    /**
     * Gets the mob type identifier.
     * @return "dragon"
     */
    public String getMobType() {
        return "dragon";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        switch (element) {
            case ICE:
                return "An ancient frost dragon that freezes all in its path.";
            case POISON:
                return "A corrupted dragon whose breath brings decay.";
            default:
                return "A mighty dragon that commands the power of fire.";
        }
    }
}
