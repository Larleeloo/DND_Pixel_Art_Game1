package entity.mob.mobs.humanoid;

import entity.mob.SpriteMobEntity;

/**
 * Knight mob - an armored, powerful enemy.
 *
 * Knights are heavily armored warriors with high health and damage.
 * Their armor slows them down but makes them formidable opponents
 * in melee combat. They are often found guarding important locations.
 *
 * Stats:
 * - Health: 55
 * - Damage: 12
 * - Walk Speed: 35
 * - Chase Speed: 50
 *
 * Special Traits:
 * - High health and damage
 * - Slow movement due to armor
 * - Resistant to knockback
 */
public class KnightMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/knight";

    /**
     * Creates a knight at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public KnightMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a knight with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing knight sprites
     */
    public KnightMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures knight-specific stats.
     */
    private void configureStats() {
        // Health (high due to armor)
        this.maxHealth = 55;
        this.currentHealth = maxHealth;

        // Combat (powerful strikes)
        this.attackDamage = 12;
        this.attackRange = 65;
        this.attackCooldown = 1.3;

        // Movement (slow due to armor)
        this.wanderSpeed = 35;
        this.chaseSpeed = 50;

        // Detection
        this.detectionRange = 220;
        this.loseTargetRange = 380;

        // Knights are always hostile
        setHostile(true);
    }

    /**
     * Knights take reduced knockback due to heavy armor.
     */
    @Override
    public void takeDamage(int damage, double knockbackX, double knockbackY) {
        // Reduce knockback by 50% due to heavy armor
        super.takeDamage(damage, knockbackX * 0.5, knockbackY * 0.5);
    }

    /**
     * Gets the mob type identifier.
     * @return "knight"
     */
    public String getMobType() {
        return "knight";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A heavily armored warrior sworn to defend their post.";
    }
}
