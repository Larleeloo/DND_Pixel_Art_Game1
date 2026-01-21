package entity.item;

import entity.ProjectileEntity;
import entity.ProjectileEntity.ProjectileType;
import java.util.ArrayList;
import java.util.List;

/**
 * MirrorToOtherRealms is a special magical item that fires projectiles
 * based on the currently displayed realm in its animated sprite.
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
 *
 * The visual sprite and projectile type are synchronized, ensuring
 * players can time their shots based on the mirror's appearance.
 */
public class MirrorToOtherRealms extends Item {

    /**
     * The three realms displayed by the mirror.
     */
    public enum Realm {
        VOLCANO,  // Fires fireballs
        FOREST,   // Fires arrows
        OCEAN     // Fires tiny fish
    }

    // Timing constants
    private static final long REALM_DURATION_MS = 400;  // Each realm displays for 400ms
    private static final long TOTAL_CYCLE_MS = REALM_DURATION_MS * 3;  // Full cycle is 1200ms

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

        // Configure base item properties
        setRarity(ItemRarity.EPIC);
        setDescription("A mystical mirror that glimpses into parallel realms. " +
                "Each realm grants different projectile powers.");
        setSpecialEffect("Fires 3 projectiles based on displayed realm");

        // Configure as ranged weapon (base projectile type will be overridden)
        setRangedWeapon(true, ProjectileType.FIREBALL, PROJECTILE_DAMAGE, PROJECTILE_SPEED);

        // Not chargeable - fires immediately
        setStackable(false);
        setMaxStackSize(1);

        // Initialize cycle timer
        cycleStartTime = System.currentTimeMillis();
        trackingTime = true;
    }

    /**
     * Creates a copy of this MirrorToOtherRealms item.
     * Overrides Item.copy() to return the correct subclass type.
     *
     * @return A new MirrorToOtherRealms instance
     */
    @Override
    public Item copy() {
        return new MirrorToOtherRealms();
    }

    /**
     * Gets the current realm based on the internal timer.
     * This should be synchronized with the sprite animation frames.
     *
     * @return The current realm being displayed
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
     * Gets the projectile type corresponding to the current realm.
     *
     * @return ProjectileType for the current realm
     */
    public ProjectileType getCurrentProjectileType() {
        switch (getCurrentRealm()) {
            case VOLCANO:
                return ProjectileType.FIREBALL;
            case FOREST:
                return ProjectileType.ARROW;
            case OCEAN:
                return ProjectileType.FISH;
            default:
                return ProjectileType.MAGIC_BOLT;
        }
    }

    /**
     * Gets the realm-specific projectile speed.
     * Different projectiles have slightly different speeds for variety.
     *
     * @return Projectile speed for current realm
     */
    public float getCurrentProjectileSpeed() {
        switch (getCurrentRealm()) {
            case VOLCANO:
                return 16.0f;  // Fireballs are medium speed
            case FOREST:
                return 22.0f;  // Arrows are fast
            case OCEAN:
                return 14.0f;  // Fish swim a bit slower
            default:
                return PROJECTILE_SPEED;
        }
    }

    /**
     * Gets the realm-specific projectile damage.
     *
     * @return Projectile damage for current realm
     */
    public int getCurrentProjectileDamage() {
        switch (getCurrentRealm()) {
            case VOLCANO:
                return 18;  // Fireballs hit hard
            case FOREST:
                return 12;  // Arrows are balanced
            case OCEAN:
                return 10;  // Fish do less damage but fire 3
            default:
                return PROJECTILE_DAMAGE;
        }
    }

    /**
     * Gets the number of projectiles fired per use.
     *
     * @return Number of projectiles (always 3)
     */
    public int getProjectileCount() {
        return PROJECTILE_COUNT;
    }

    /**
     * Gets the mana cost to use this item.
     *
     * @return Mana cost (always 25)
     */
    public int getManaCost() {
        return MANA_COST;
    }

    /**
     * Creates projectiles for the current realm.
     * Fires 3 projectiles in a spread pattern.
     *
     * @param x Starting X position
     * @param y Starting Y position
     * @param baseDirX Base direction X (normalized)
     * @param baseDirY Base direction Y (normalized)
     * @param fromPlayer True if fired by player
     * @return List of created projectiles
     */
    public List<ProjectileEntity> createProjectiles(int x, int y, double baseDirX, double baseDirY, boolean fromPlayer) {
        List<ProjectileEntity> projectiles = new ArrayList<>();

        ProjectileType type = getCurrentProjectileType();
        int damage = getCurrentProjectileDamage();
        float speed = getCurrentProjectileSpeed();

        // Calculate spread angle for 3 projectiles
        // Center projectile goes straight, others spread by ~10 degrees
        double spreadAngle = Math.toRadians(10);
        double baseAngle = Math.atan2(baseDirY, baseDirX);

        for (int i = 0; i < PROJECTILE_COUNT; i++) {
            // Calculate angle offset: -1, 0, +1 for spread
            double angleOffset = (i - 1) * spreadAngle;
            double angle = baseAngle + angleOffset;

            double dirX = Math.cos(angle);
            double dirY = Math.sin(angle);

            double velX = dirX * speed;
            double velY = dirY * speed;

            ProjectileEntity projectile = new ProjectileEntity(x, y, type, damage, velX, velY, fromPlayer);

            // Apply realm-specific effects
            applyRealmEffects(projectile);

            projectiles.add(projectile);
        }

        return projectiles;
    }

    /**
     * Applies special effects based on the current realm.
     *
     * @param projectile The projectile to modify
     */
    private void applyRealmEffects(ProjectileEntity projectile) {
        switch (getCurrentRealm()) {
            case VOLCANO:
                // Fireballs have burning effect
                projectile.setStatusEffect(
                    ProjectileEntity.StatusEffectType.BURNING,
                    2.0,  // 2 second burn
                    4,    // 4 damage per tick
                    1.0f  // Normal impact damage
                );
                projectile.setExplosive(false, 0);  // Mirror fireballs don't explode
                break;

            case FOREST:
                // Arrows have no special effect but pierce once
                projectile.setPiercing(true, 1);
                break;

            case OCEAN:
                // Fish have a slight slow effect (cold water)
                projectile.setStatusEffect(
                    ProjectileEntity.StatusEffectType.FROZEN,
                    1.5,  // 1.5 second slow
                    2,    // 2 damage per tick
                    1.0f  // Normal impact damage
                );
                break;
        }
    }

    /**
     * Resets the realm cycle timer.
     * Useful when the item is first equipped or after loading.
     */
    public void resetCycle() {
        cycleStartTime = System.currentTimeMillis();
        trackingTime = true;
    }

    /**
     * Synchronizes the internal timer with an animation frame.
     * Call this when the item's sprite animation resets to ensure
     * visual and logical synchronization.
     *
     * @param frameIndex Current animation frame (0, 1, or 2 for 3-frame cycle)
     */
    public void syncWithFrame(int frameIndex) {
        // Each frame represents a realm, so set the cycle start accordingly
        long offset = frameIndex * REALM_DURATION_MS;
        cycleStartTime = System.currentTimeMillis() - offset;
    }

    /**
     * Gets the current realm index (0, 1, or 2).
     * Useful for syncing with 3-frame animations.
     *
     * @return 0 for Volcano, 1 for Forest, 2 for Ocean
     */
    public int getRealmIndex() {
        return getCurrentRealm().ordinal();
    }

    /**
     * Gets the time remaining in the current realm display (milliseconds).
     *
     * @return Milliseconds until realm changes
     */
    public long getTimeRemainingInRealm() {
        long elapsedMs = (System.currentTimeMillis() - cycleStartTime) % TOTAL_CYCLE_MS;
        long timeInCurrentRealm = elapsedMs % REALM_DURATION_MS;
        return REALM_DURATION_MS - timeInCurrentRealm;
    }

    /**
     * Gets the realm duration in milliseconds.
     *
     * @return 400 (each realm lasts 400ms)
     */
    public static long getRealmDurationMs() {
        return REALM_DURATION_MS;
    }

    /**
     * Gets the total cycle duration in milliseconds.
     *
     * @return 1200 (full cycle through all 3 realms)
     */
    public static long getTotalCycleMs() {
        return TOTAL_CYCLE_MS;
    }

    @Override
    public String toString() {
        return "MirrorToOtherRealms{" +
                "currentRealm=" + getCurrentRealm() +
                ", projectileType=" + getCurrentProjectileType() +
                ", manaCost=" + MANA_COST +
                ", projectileCount=" + PROJECTILE_COUNT +
                "}";
    }
}
