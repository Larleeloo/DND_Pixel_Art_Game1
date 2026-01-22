package entity.mob.mobs.special;

import entity.mob.SpriteMobEntity;

/**
 * Slime mob - a small, bouncy creature.
 *
 * Slimes are simple creatures that bounce toward their targets.
 * They have low health and damage but can split into smaller slimes
 * when defeated (future feature).
 *
 * Stats:
 * - Health: 25
 * - Damage: 3
 * - Walk Speed: 30
 * - Chase Speed: 60
 *
 * Special Traits:
 * - Small body type
 * - Bouncy movement
 * - Can split when defeated (future)
 */
public class SlimeMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/slime";

    // Slime size variant
    public enum SlimeSize {
        SMALL,   // 16x16
        MEDIUM,  // 32x32
        LARGE    // 48x48
    }

    private SlimeSize slimeSize = SlimeSize.MEDIUM;

    /**
     * Creates a slime at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public SlimeMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a slime with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing slime sprites
     */
    public SlimeMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures slime-specific stats.
     */
    private void configureStats() {
        // Health (low)
        this.maxHealth = 25;
        this.currentHealth = maxHealth;

        // Combat (weak contact damage)
        this.attackDamage = 3;
        this.attackRange = 30;
        this.attackCooldown = 1.0;

        // Movement (slow but bouncy)
        this.wanderSpeed = 30;
        this.chaseSpeed = 60;

        // Detection
        this.detectionRange = 150;
        this.loseTargetRange = 250;

        // Slimes are hostile
        setHostile(true);

        // Set body type to small
        setBodyType(MobBodyType.SMALL);
    }

    /**
     * Sets the slime size variant.
     *
     * @param size The slime size
     */
    public void setSlimeSize(SlimeSize size) {
        this.slimeSize = size;
        switch (size) {
            case SMALL:
                this.maxHealth = 15;
                this.attackDamage = 2;
                this.wanderSpeed = 40;
                this.chaseSpeed = 80;
                setBodyType(MobBodyType.SMALL);
                break;
            case LARGE:
                this.maxHealth = 40;
                this.attackDamage = 5;
                this.wanderSpeed = 25;
                this.chaseSpeed = 50;
                setBodyType(MobBodyType.HUMANOID); // Larger
                break;
            case MEDIUM:
            default:
                this.maxHealth = 25;
                this.attackDamage = 3;
                this.wanderSpeed = 30;
                this.chaseSpeed = 60;
                setBodyType(MobBodyType.SMALL);
                break;
        }
        this.currentHealth = this.maxHealth;
    }

    /**
     * Gets the slime size.
     * @return The slime size
     */
    public SlimeSize getSlimeSize() {
        return slimeSize;
    }

    /**
     * Gets the mob type identifier.
     * @return "slime"
     */
    public String getMobType() {
        return "slime";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A gelatinous creature that bounces toward its prey.";
    }
}
