package entity.mob.mobs.humanoid;

import entity.mob.SpriteMobEntity;
import entity.ProjectileEntity;
import entity.item.Item;
import entity.item.items.weapons.ranged.MagicWand;
import entity.item.items.weapons.ranged.FireStaff;
import entity.item.items.weapons.ranged.IceStaff;
import entity.item.items.weapons.ranged.ArcaneStaff;

/**
 * Mage mob - a ranged spellcaster enemy.
 *
 * Mages are powerful ranged attackers that cast magical projectiles.
 * They have low health but high damage output. They prefer to keep
 * their distance and attack from range.
 *
 * Stats:
 * - Health: 40
 * - Damage: 15 (magic)
 * - Walk Speed: 30
 * - Chase Speed: 40
 *
 * Special Traits:
 * - Ranged magic attacks
 * - High damage
 * - Low health and slow movement
 * - Can cast fireballs or ice bolts
 */
public class MageMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/mage";

    /**
     * Magic type that this mage uses.
     */
    public enum MagicType {
        FIRE,   // Fireballs that burn
        ICE,    // Ice bolts that freeze
        ARCANE  // Standard magic bolts
    }

    private MagicType magicType = MagicType.ARCANE;

    /**
     * Creates a mage at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public MageMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a mage with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing mage sprites
     */
    public MageMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures mage-specific stats.
     */
    private void configureStats() {
        // Health (fragile)
        this.maxHealth = 40;
        this.currentHealth = maxHealth;

        // Melee combat (weak)
        this.attackDamage = 5;
        this.attackRange = 40;
        this.attackCooldown = 1.5;

        // Movement (slow, prefers range)
        this.wanderSpeed = 30;
        this.chaseSpeed = 40;

        // Detection (high - mages are perceptive)
        this.detectionRange = 350;
        this.loseTargetRange = 500;

        // Configure ranged magic attack
        updateMagicType();

        // Mages are always hostile
        setHostile(true);

        // ==================== Weapon Usage Configuration ====================

        // Mages prefer magic weapons only
        setWeaponPreference(WeaponPreference.MAGIC_ONLY);

        // Configure mana for spellcasting
        setMaxMana(150);
        setMana(150);
        setManaRegenRate(8.0);  // Fast mana regen for frequent casting
        setMagicAttackManaCost(15);

        // Initialize inventory with appropriate magic weapon
        initializeMageInventory();
    }

    /**
     * Initializes the mage's inventory with magic weapons based on magic type.
     * Note: updateMagicType() already configures the ranged attack settings correctly,
     * so we just need to add the weapon to inventory for drops on death and visuals.
     */
    private void initializeMageInventory() {
        Item weapon;

        switch (magicType) {
            case FIRE:
                weapon = new FireStaff();
                break;
            case ICE:
                weapon = new IceStaff();
                break;
            case ARCANE:
            default:
                // Random chance for better weapon
                if (Math.random() < 0.3) {
                    weapon = new ArcaneStaff();
                } else {
                    weapon = new MagicWand();
                }
                break;
        }

        // Add weapon to inventory for drops on death
        addToInventory(weapon);

        // Set as equipped weapon directly (don't use equipWeapon() which would
        // override the attack range configured by updateMagicType())
        equippedWeapon = weapon;
        inventory.remove(weapon);

        // Restore the proper attack range from magic type configuration
        // (equipWeapon would have overridden this, so we don't call it)
        updateMagicType();
    }

    /**
     * Sets the type of magic this mage uses.
     *
     * @param type The magic type
     */
    public void setMagicType(MagicType type) {
        this.magicType = type;
        updateMagicType();
    }

    /**
     * Updates the projectile configuration based on magic type.
     */
    private void updateMagicType() {
        switch (magicType) {
            case FIRE:
                setRangedAttack(
                    ProjectileEntity.ProjectileType.FIREBALL,
                    15,     // damage
                    8.0,    // projectile speed
                    2.5,    // cooldown
                    280     // preferred attack range
                );
                break;
            case ICE:
                setRangedAttack(
                    ProjectileEntity.ProjectileType.ICEBALL,
                    12,     // damage (lower but freezes)
                    9.0,    // projectile speed
                    2.2,    // cooldown
                    260     // preferred attack range
                );
                break;
            case ARCANE:
            default:
                setRangedAttack(
                    ProjectileEntity.ProjectileType.MAGIC_BOLT,
                    15,     // damage
                    10.0,   // projectile speed
                    2.0,    // cooldown
                    300     // preferred attack range
                );
                break;
        }
    }

    /**
     * Gets the current magic type.
     * @return The magic type
     */
    public MagicType getMagicType() {
        return magicType;
    }

    /**
     * Gets the mob type identifier.
     * @return "mage"
     */
    public String getMobType() {
        return "mage";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        switch (magicType) {
            case FIRE:
                return "A spellcaster who commands the destructive power of fire.";
            case ICE:
                return "A frost mage who freezes enemies in their tracks.";
            default:
                return "A mysterious spellcaster wielding arcane magic.";
        }
    }
}
