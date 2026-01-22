package entity.mob.mobs.special;

import entity.mob.SpriteMobEntity;

/**
 * Spider mob - a fast, venomous predator.
 *
 * Spiders are hostile creatures found in caves and forests. They
 * are fast and can poison their targets with their bites.
 *
 * Stats:
 * - Health: 35
 * - Damage: 8
 * - Walk Speed: 40
 * - Chase Speed: 120
 *
 * Special Traits:
 * - Quadruped body type
 * - Fast movement
 * - Poison attack (applies poisoned status)
 */
public class SpiderMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/spider";

    /**
     * Creates a spider at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public SpiderMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a spider with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing spider sprites
     */
    public SpiderMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures spider-specific stats.
     */
    private void configureStats() {
        // Health (moderate)
        this.maxHealth = 35;
        this.currentHealth = maxHealth;

        // Combat (venomous bite)
        this.attackDamage = 8;
        this.attackRange = 50;
        this.attackCooldown = 1.2;

        // Movement (fast)
        this.wanderSpeed = 40;
        this.chaseSpeed = 120;

        // Detection
        this.detectionRange = 250;
        this.loseTargetRange = 400;

        // Spiders can climb (multi-jump)
        setMaxJumps(2);

        // Spiders are hostile
        setHostile(true);

        // Set body type to quadruped
        setBodyType(MobBodyType.QUADRUPED);
    }

    /**
     * Spider attacks can poison the target.
     */
    @Override
    protected void performAttack() {
        if (target == null) return;

        double dist = getDistanceToTargetFace();

        // Melee attack if in range
        if (attackTimer <= 0 && dist <= attackRange) {
            // Calculate knockback direction
            java.awt.Rectangle playerBounds = target.getBounds();
            double playerCenterX = playerBounds.x + playerBounds.width / 2;
            double knockbackDir = posX < playerCenterX ? 1 : -1;
            target.takeDamage(attackDamage, knockbackDir * 4, -2);

            // Apply poison effect (if target supports it)
            // Future: target.applyStatusEffect(StatusEffect.POISONED, 3.0, 2, 1.0f);

            attackTimer = attackCooldown;
            setAnimationState("attack");
        }
    }

    /**
     * Gets the mob type identifier.
     * @return "spider"
     */
    public String getMobType() {
        return "spider";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A venomous arachnid with deadly fangs.";
    }
}
