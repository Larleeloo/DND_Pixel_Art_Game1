package entity.mob.mobs.quadruped;

import entity.mob.SpriteMobEntity;

/**
 * Deer mob - a skittish forest creature.
 *
 * Deer are passive creatures found in forests. They are fast and
 * flee at the first sign of danger. They are a source of food and
 * materials.
 *
 * Stats:
 * - Health: 40
 * - Damage: 3
 * - Walk Speed: 70
 * - Chase Speed: 110
 *
 * Special Traits:
 * - Passive behavior
 * - Flees immediately when approached
 * - Fast movement
 */
public class DeerMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/deer";

    /**
     * Creates a deer at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public DeerMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a deer with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing deer sprites
     */
    public DeerMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures deer-specific stats.
     */
    private void configureStats() {
        // Health (moderate)
        this.maxHealth = 40;
        this.currentHealth = maxHealth;

        // Combat (weak - antlers)
        this.attackDamage = 3;
        this.attackRange = 50;
        this.attackCooldown = 1.5;

        // Movement (fast)
        this.wanderSpeed = 70;
        this.chaseSpeed = 110;

        // Detection - deer are very aware of their surroundings
        this.detectionRange = 150; // Detect player to flee
        this.loseTargetRange = 300;

        // Deer are passive but aware
        setHostile(false);

        // Set body type to quadruped
        setBodyType(MobBodyType.QUADRUPED);
    }

    /**
     * Deer flee when they detect the player.
     */
    @Override
    protected void checkStateTransitions() {
        // Check if player is nearby
        if (target != null && getDistanceToTarget() < detectionRange) {
            // Flee instead of chase
            if (currentState != AIState.FLEE) {
                changeState(AIState.FLEE);
            }
        } else if (currentState == AIState.FLEE && getDistanceToTarget() > loseTargetRange) {
            changeState(AIState.WANDER);
        }
    }

    /**
     * Deer flee when damaged.
     */
    @Override
    public void takeDamage(int damage, double knockbackX, double knockbackY) {
        super.takeDamage(damage, knockbackX, knockbackY);
        if (currentHealth > 0) {
            changeState(AIState.FLEE);
        }
    }

    /**
     * Gets the mob type identifier.
     * @return "deer"
     */
    public String getMobType() {
        return "deer";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A graceful forest dweller, always alert for danger.";
    }
}
