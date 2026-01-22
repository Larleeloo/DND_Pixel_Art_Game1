package entity.mob.mobs.special;

import entity.mob.SpriteMobEntity;

/**
 * Troll mob - a large, regenerating creature.
 *
 * Trolls are similar to ogres but have the ability to regenerate
 * health over time. Fire damage stops their regeneration.
 *
 * Stats:
 * - Health: 80
 * - Damage: 18
 * - Walk Speed: 30
 * - Chase Speed: 55
 *
 * Special Traits:
 * - Large body type
 * - Health regeneration
 * - Weak to fire (stops regen)
 */
public class TrollMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/troll";

    // Regeneration rate (health per second)
    private double regenRate = 2.0;
    private double regenTimer = 0;

    /**
     * Creates a troll at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public TrollMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a troll with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing troll sprites
     */
    public TrollMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures troll-specific stats.
     */
    private void configureStats() {
        // Health (high)
        this.maxHealth = 80;
        this.currentHealth = maxHealth;

        // Combat (strong attacks)
        this.attackDamage = 18;
        this.attackRange = 85;
        this.attackCooldown = 1.8;

        // Movement (moderate-slow)
        this.wanderSpeed = 30;
        this.chaseSpeed = 55;

        // Detection
        this.detectionRange = 220;
        this.loseTargetRange = 380;

        // Trolls are always hostile
        setHostile(true);

        // Set body type to large
        setBodyType(MobBodyType.LARGE);
    }

    /**
     * Update with regeneration logic.
     */
    @Override
    public void update(double deltaTime, java.util.List<entity.Entity> entities) {
        super.update(deltaTime, entities);

        // Regenerate health if not burning
        if (currentState != AIState.DEAD && getActiveEffect() != StatusEffect.BURNING) {
            regenTimer += deltaTime;
            if (regenTimer >= 1.0) {
                regenTimer -= 1.0;
                int healAmount = (int) regenRate;
                currentHealth = Math.min(maxHealth, currentHealth + healAmount);
            }
        }
    }

    /**
     * Trolls take reduced knockback.
     */
    @Override
    public void takeDamage(int damage, double knockbackX, double knockbackY) {
        super.takeDamage(damage, knockbackX * 0.35, knockbackY * 0.35);
    }

    /**
     * Sets the regeneration rate.
     * @param rate Health per second
     */
    public void setRegenRate(double rate) {
        this.regenRate = rate;
    }

    /**
     * Gets the mob type identifier.
     * @return "troll"
     */
    public String getMobType() {
        return "troll";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A fearsome creature that regenerates wounds unless burned.";
    }
}
