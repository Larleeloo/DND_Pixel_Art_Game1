package com.ambermoongame.entity.item;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * MirrorToOtherRealms is a special magical item that fires projectiles
 * based on the currently displayed realm in its animated sprite.
 *
 * Conversion notes:
 * - ProjectileEntity/ProjectileType -> String-based projectile names (pending port)
 * - ProjectileEntity.StatusEffectType -> String-based effect names (pending port)
 * - System.currentTimeMillis() remains unchanged (pure Java timing)
 * - Extends Android-ported Item class
 *
 * The mirror cycles through three distinct realms every 400 milliseconds:
 * - Volcano (0-399ms): Fires fireballs
 * - Forest (400-799ms): Fires arrows
 * - Ocean (800-1199ms): Fires tiny fish
 *
 * Usage:
 * - Fires 3 projectiles per use
 * - Consumes 25 mana per use
 * - Projectile type is determined by the current realm displayed
 */
public class MirrorToOtherRealms extends Item {

    private static final String TAG = "MirrorToOtherRealms";

    /**
     * The three realms displayed by the mirror.
     */
    public enum Realm {
        VOLCANO,
        FOREST,
        OCEAN
    }

    // Timing constants
    private static final long REALM_DURATION_MS = 400;
    private static final long TOTAL_CYCLE_MS = REALM_DURATION_MS * 3;

    // Item configuration
    private static final int MANA_COST = 25;
    private static final int PROJECTILE_COUNT = 3;
    private static final int PROJECTILE_DAMAGE = 15;
    private static final float PROJECTILE_SPEED = 18.0f;

    // Internal state
    private long cycleStartTime;
    private boolean trackingTime = false;

    /**
     * Creates a new Mirror to Other Realms item.
     */
    public MirrorToOtherRealms() {
        super("Mirror to Other Realms", ItemCategory.RANGED_WEAPON);

        setRarity(ItemRarity.EPIC);
        setDescription("A mystical mirror that glimpses into parallel realms. " +
                "Each realm grants different projectile powers.");
        setSpecialEffect("Fires 3 projectiles based on displayed realm");

        // --- Uncomment when ProjectileEntity/ProjectileType is ported ---
        // setRangedWeapon(true, ProjectileType.FIREBALL, PROJECTILE_DAMAGE, PROJECTILE_SPEED);
        setProjectileTypeName("FIREBALL");

        setStackable(false);
        setMaxStackSize(1);

        cycleStartTime = System.currentTimeMillis();
        trackingTime = true;
    }

    @Override
    public Item copy() {
        return new MirrorToOtherRealms();
    }

    /**
     * Gets the current realm based on the internal timer.
     */
    public Realm getCurrentRealm() {
        if (!trackingTime) {
            cycleStartTime = System.currentTimeMillis();
            trackingTime = true;
        }

        long elapsedMs = (System.currentTimeMillis() - cycleStartTime) % TOTAL_CYCLE_MS;

        if (elapsedMs < REALM_DURATION_MS) {
            return Realm.VOLCANO;
        } else if (elapsedMs < REALM_DURATION_MS * 2) {
            return Realm.FOREST;
        } else {
            return Realm.OCEAN;
        }
    }

    /**
     * Gets the projectile type name corresponding to the current realm.
     * Returns a string name instead of ProjectileType enum (pending port).
     */
    public String getCurrentProjectileTypeName() {
        switch (getCurrentRealm()) {
            case VOLCANO:
                return "FIREBALL";
            case FOREST:
                return "ARROW";
            case OCEAN:
                return "FISH";
            default:
                return "MAGIC_BOLT";
        }
    }

    /**
     * Gets the realm-specific projectile speed.
     */
    public float getCurrentProjectileSpeed() {
        switch (getCurrentRealm()) {
            case VOLCANO:
                return 16.0f;
            case FOREST:
                return 22.0f;
            case OCEAN:
                return 14.0f;
            default:
                return PROJECTILE_SPEED;
        }
    }

    /**
     * Gets the realm-specific projectile damage.
     */
    public int getCurrentProjectileDamage() {
        switch (getCurrentRealm()) {
            case VOLCANO:
                return 18;
            case FOREST:
                return 12;
            case OCEAN:
                return 10;
            default:
                return PROJECTILE_DAMAGE;
        }
    }

    /** Gets the number of projectiles fired per use. */
    public int getProjectileCount() {
        return PROJECTILE_COUNT;
    }

    /** Gets the mana cost to use this item. */
    public int getManaCost() {
        return MANA_COST;
    }

    // --- Uncomment when ProjectileEntity is ported ---
    // /**
    //  * Creates projectiles for the current realm.
    //  * Fires 3 projectiles in a spread pattern.
    //  */
    // public List<ProjectileEntity> createProjectiles(int x, int y, double baseDirX, double baseDirY, boolean fromPlayer) {
    //     List<ProjectileEntity> projectiles = new ArrayList<>();
    //
    //     ProjectileType type = getCurrentProjectileType();
    //     int damage = getCurrentProjectileDamage();
    //     float speed = getCurrentProjectileSpeed();
    //
    //     double spreadAngle = Math.toRadians(10);
    //     double baseAngle = Math.atan2(baseDirY, baseDirX);
    //
    //     for (int i = 0; i < PROJECTILE_COUNT; i++) {
    //         double angleOffset = (i - 1) * spreadAngle;
    //         double angle = baseAngle + angleOffset;
    //
    //         double dirX = Math.cos(angle);
    //         double dirY = Math.sin(angle);
    //
    //         double velX = dirX * speed;
    //         double velY = dirY * speed;
    //
    //         ProjectileEntity projectile = new ProjectileEntity(x, y, type, damage, velX, velY, fromPlayer);
    //         applyRealmEffects(projectile);
    //         projectiles.add(projectile);
    //     }
    //
    //     return projectiles;
    // }

    // --- Uncomment when ProjectileEntity is ported ---
    // private void applyRealmEffects(ProjectileEntity projectile) {
    //     switch (getCurrentRealm()) {
    //         case VOLCANO:
    //             projectile.setStatusEffect(
    //                 ProjectileEntity.StatusEffectType.BURNING,
    //                 2.0, 4, 1.0f
    //             );
    //             projectile.setExplosive(false, 0);
    //             break;
    //
    //         case FOREST:
    //             projectile.setPiercing(true, 1);
    //             break;
    //
    //         case OCEAN:
    //             projectile.setStatusEffect(
    //                 ProjectileEntity.StatusEffectType.FROZEN,
    //                 1.5, 2, 1.0f
    //             );
    //             break;
    //     }
    // }

    /**
     * Calculates projectile velocities for the spread pattern.
     * Useful for scene code that creates projectiles externally.
     *
     * @param baseDirX Base direction X (normalized)
     * @param baseDirY Base direction Y (normalized)
     * @return List of [velX, velY] pairs for each projectile
     */
    public List<double[]> calculateProjectileVelocities(double baseDirX, double baseDirY) {
        List<double[]> velocities = new ArrayList<>();
        float speed = getCurrentProjectileSpeed();
        double spreadAngle = Math.toRadians(10);
        double baseAngle = Math.atan2(baseDirY, baseDirX);

        for (int i = 0; i < PROJECTILE_COUNT; i++) {
            double angleOffset = (i - 1) * spreadAngle;
            double angle = baseAngle + angleOffset;
            double velX = Math.cos(angle) * speed;
            double velY = Math.sin(angle) * speed;
            velocities.add(new double[]{velX, velY});
        }

        return velocities;
    }

    /** Resets the realm cycle timer. */
    public void resetCycle() {
        cycleStartTime = System.currentTimeMillis();
        trackingTime = true;
    }

    /**
     * Synchronizes the internal timer with an animation frame.
     */
    public void syncWithFrame(int frameIndex) {
        long offset = frameIndex * REALM_DURATION_MS;
        cycleStartTime = System.currentTimeMillis() - offset;
    }

    /** Gets the current realm index (0, 1, or 2). */
    public int getRealmIndex() {
        return getCurrentRealm().ordinal();
    }

    /** Gets the time remaining in the current realm display (milliseconds). */
    public long getTimeRemainingInRealm() {
        long elapsedMs = (System.currentTimeMillis() - cycleStartTime) % TOTAL_CYCLE_MS;
        long timeInCurrentRealm = elapsedMs % REALM_DURATION_MS;
        return REALM_DURATION_MS - timeInCurrentRealm;
    }

    /** Gets the realm duration in milliseconds. */
    public static long getRealmDurationMs() {
        return REALM_DURATION_MS;
    }

    /** Gets the total cycle duration in milliseconds. */
    public static long getTotalCycleMs() {
        return TOTAL_CYCLE_MS;
    }

    @Override
    public String toString() {
        return "MirrorToOtherRealms{" +
                "currentRealm=" + getCurrentRealm() +
                ", projectileType=" + getCurrentProjectileTypeName() +
                ", manaCost=" + MANA_COST +
                ", projectileCount=" + PROJECTILE_COUNT +
                "}";
    }
}
