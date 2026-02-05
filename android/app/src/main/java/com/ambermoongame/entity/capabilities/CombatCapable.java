package com.ambermoongame.entity.capabilities;

import android.graphics.Rect;

import com.ambermoongame.entity.Entity;
import com.ambermoongame.entity.ProjectileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for entities that can engage in combat.
 * Provides a unified contract for attack and defense capabilities that can be
 * implemented by players, mobs, companions, or any entity that participates in combat.
 * Equivalent to entity/capabilities/CombatCapable.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.Rectangle -> android.graphics.Rect
 *
 * Dependencies not yet ported:
 * - ProjectileEntity (referenced in fireProjectile/getActiveProjectiles)
 *   Methods are present but will not compile until ProjectileEntity is ported.
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
     */
    boolean isAttacking();

    /**
     * Gets the attack damage (base damage + weapon damage).
     */
    int getAttackDamage();

    /**
     * Gets the base (unarmed) attack damage.
     */
    int getBaseAttackDamage();

    /**
     * Sets the base attack damage.
     */
    void setBaseAttackDamage(int damage);

    /**
     * Gets the attack range (base range + weapon range).
     */
    int getAttackRange();

    /**
     * Gets the base (unarmed) attack range.
     */
    int getBaseAttackRange();

    /**
     * Sets the base attack range.
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
     * @return Rect representing the attack area, or null if not attacking
     */
    Rect getAttackBounds();

    // ==================== Damage Reception ====================

    /**
     * Applies damage to this entity with knockback.
     */
    void takeDamage(int damage, double knockbackX, double knockbackY);

    /**
     * Checks if the entity is currently invincible (damage immunity frames).
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
     */
    boolean canFireRanged();

    // --- Uncomment when ProjectileEntity is ported ---
    //
    // /**
    //  * Fires a projectile in the specified direction.
    //  * @param entities List of all entities (for adding the projectile)
    //  * @param dirX Normalized X direction
    //  * @param dirY Normalized Y direction
    //  * @return true if projectile was fired
    //  */
    // boolean fireProjectile(ArrayList<Entity> entities, double dirX, double dirY);
    //
    // /**
    //  * Gets the active projectiles fired by this entity.
    //  */
    // List<ProjectileEntity> getActiveProjectiles();

    /**
     * Checks if the entity is currently firing.
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
     */
    default boolean canAttack(Entity target) {
        return target != null && target != this;
    }

    /**
     * Calculates damage with modifiers (critical hits, armor, etc.).
     * @param baseDamage The base damage before modifiers
     * @return Modified damage value
     */
    default int calculateDamage(int baseDamage) {
        return baseDamage;
    }
}
