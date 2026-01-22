package entity.mob.mobs.quadruped;

import entity.mob.SpriteMobEntity;

/**
 * Cat mob - a small, passive feline.
 *
 * Cats are passive creatures found in villages and homes. They are
 * agile and quick but don't engage in combat unless cornered.
 *
 * Stats:
 * - Health: 25
 * - Damage: 3
 * - Walk Speed: 50
 * - Chase Speed: 120
 *
 * Special Traits:
 * - Passive behavior
 * - Very agile (can double jump)
 * - Flees when attacked
 */
public class CatMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/cat";

    /**
     * Creates a cat at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public CatMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a cat with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing cat sprites
     */
    public CatMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures cat-specific stats.
     */
    private void configureStats() {
        // Health (low)
        this.maxHealth = 25;
        this.currentHealth = maxHealth;

        // Combat (weak)
        this.attackDamage = 3;
        this.attackRange = 35;
        this.attackCooldown = 0.5;

        // Movement (agile)
        this.wanderSpeed = 50;
        this.chaseSpeed = 120;

        // Detection
        this.detectionRange = 0; // Passive
        this.loseTargetRange = 80;

        // Cats can double jump
        setMaxJumps(2);

        // Cats are passive
        setHostile(false);

        // Set body type to quadruped (small)
        setBodyType(MobBodyType.QUADRUPED);
    }

    /**
     * Cats flee when damaged instead of fighting back.
     */
    @Override
    public void takeDamage(int damage, double knockbackX, double knockbackY) {
        super.takeDamage(damage, knockbackX, knockbackY);
        // Trigger flee behavior after being hit
        if (currentHealth > 0) {
            changeState(AIState.FLEE);
        }
    }

    /**
     * Gets the mob type identifier.
     * @return "cat"
     */
    public String getMobType() {
        return "cat";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A curious feline that prefers to observe from a distance.";
    }
}
