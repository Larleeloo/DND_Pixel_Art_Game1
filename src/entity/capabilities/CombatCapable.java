package entity.capabilities;

import entity.*;

import java.awt.*;
import java.util.ArrayList;

/**
 * Interface for entities that can engage in combat.
 * Provides a unified contract for attack and defense capabilities that can be
 * implemented by players, mobs, companions, or any entity that participates in combat.
 *
 * Combat capabilities include:
 * - Melee attacks with configurable damage and range
 * - Ranged attacks via projectiles
 * - Damage reception with knockback
 * - Invincibility frames after taking damage
 */
public interface CombatCapable {

    // ==================== Attack System ====================

    /**
     * Initiates an attack.
     * @return true if attack was started, false if on cooldown or otherwise unavailable
     */
    boolean attack();

    /**
     * Checks if the entity is currently attacking.
     * @return true if in attack animation/state
     */
    boolean isAttacking();

    /**
     * Gets the attack damage (base damage + weapon damage).
     * @return Total attack damage
     */
    int getAttackDamage();

    /**
     * Gets the base (unarmed) attack damage.
     * @return Base damage without weapons
     */
    int getBaseAttackDamage();

    /**
     * Sets the base attack damage.
     * @param damage New base damage value
     */
    void setBaseAttackDamage(int damage);

    /**
     * Gets the attack range (base range + weapon range).
     * @return Total attack range in pixels
     */
    int getAttackRange();

    /**
     * Gets the base (unarmed) attack range.
     * @return Base range without weapons
     */
    int getBaseAttackRange();

    /**
     * Sets the base attack range.
     * @param range New base range in pixels
     */
    void setBaseAttackRange(int range);

    /**
     * Gets the attack cooldown duration.
     * @return Seconds between attacks
     */
    double getAttackCooldown();

    /**
     * Sets the attack cooldown duration.
     * @param cooldown Seconds between attacks
     */
    void setAttackCooldown(double cooldown);

    /**
     * Gets the attack hitbox bounds during an attack.
     * @return Rectangle representing the attack area, or null if not attacking
     */
    Rectangle getAttackBounds();

    // ==================== Damage Reception ====================

    /**
     * Applies damage to this entity with knockback.
     * @param damage Amount of damage to take
     * @param knockbackX Horizontal knockback force
     * @param knockbackY Vertical knockback force
     */
    void takeDamage(int damage, double knockbackX, double knockbackY);

    /**
     * Checks if the entity is currently invincible (damage immunity frames).
     * @return true if currently invincible
     */
    boolean isInvincible();

    /**
     * Gets the invincibility duration after taking damage.
     * @return Duration in seconds
     */
    double getInvincibilityDuration();

    /**
     * Sets the invincibility duration after taking damage.
     * @param duration Duration in seconds
     */
    void setInvincibilityDuration(double duration);

    // ==================== Ranged Combat ====================

    /**
     * Checks if the entity can fire ranged attacks.
     * @return true if ranged attacks are available
     */
    boolean canFireRanged();

    /**
     * Fires a projectile in the specified direction.
     * @param entities List of all entities (for adding the projectile)
     * @param dirX Normalized X direction
     * @param dirY Normalized Y direction
     * @return true if projectile was fired
     */
    boolean fireProjectile(ArrayList<Entity> entities, double dirX, double dirY);

    /**
     * Gets the active projectiles fired by this entity.
     * @return List of active projectiles
     */
    java.util.List<ProjectileEntity> getActiveProjectiles();

    /**
     * Checks if the entity is currently firing.
     * @return true if in fire animation/state
     */
    boolean isFiring();

    // ==================== Combat State ====================

    /**
     * Gets the direction the entity is facing.
     * @return true if facing right, false if facing left
     */
    boolean isFacingRight();

    /**
     * Updates combat-related timers (attack cooldown, invincibility, etc.).
     * @param deltaSeconds Time elapsed since last update
     */
    void updateCombatTimers(double deltaSeconds);

    /**
     * Checks if this entity can attack another entity.
     * @param target The potential target
     * @return true if the target is a valid attack target
     */
    default boolean canAttack(Entity target) {
        // Default: can attack anything except self
        return target != null && target != this;
    }

    /**
     * Calculates damage with modifiers (critical hits, armor, etc.).
     * @param baseDamage The base damage before modifiers
     * @return Modified damage value
     */
    default int calculateDamage(int baseDamage) {
        // Default: no modifiers
        return baseDamage;
    }
}
