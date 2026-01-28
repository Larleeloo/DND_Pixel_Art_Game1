package entity.mob.mobs.humanoid;

import entity.mob.SpriteMobEntity;
import entity.item.Item;
import entity.item.items.weapons.melee.IronSword;
import entity.item.items.weapons.melee.Dagger;
import entity.item.items.weapons.melee.SteelSword;
import entity.item.items.weapons.throwing.ThrowingKnife;
import entity.item.items.weapons.throwing.ThrowingAxe;

/**
 * Bandit mob - a balanced human enemy.
 *
 * Bandits are common enemies found on roads and in hideouts. They have
 * balanced stats and are dangerous in groups. Some bandits may have
 * ranged weapons.
 *
 * Stats:
 * - Health: 45
 * - Damage: 8
 * - Walk Speed: 50
 * - Chase Speed: 70
 *
 * Special Traits:
 * - Balanced stats
 * - Can be equipped with various weapons
 * - Often found in groups
 */
public class BanditMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/bandit";

    /**
     * Creates a bandit at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public BanditMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a bandit with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing bandit sprites
     */
    public BanditMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures bandit-specific stats.
     */
    private void configureStats() {
        // Health (moderate)
        this.maxHealth = 45;
        this.currentHealth = maxHealth;

        // Combat (balanced)
        this.attackDamage = 8;
        this.attackRange = 55;
        this.attackCooldown = 1.0;

        // Movement (moderate speed)
        this.wanderSpeed = 50;
        this.chaseSpeed = 70;

        // Detection
        this.detectionRange = 280;
        this.loseTargetRange = 450;

        // Bandits are always hostile
        setHostile(true);

        // ==================== Weapon Usage Configuration ====================

        // Bandits use melee weapons and throwables
        setWeaponPreference(WeaponPreference.MELEE_AND_THROWABLE);

        // Initialize inventory with weapons
        initializeBanditInventory();
    }

    /**
     * Initializes the bandit's inventory with appropriate weapons.
     * Bandits carry melee weapons and some throwables.
     */
    private void initializeBanditInventory() {
        // Random loadout selection
        double roll = Math.random();

        Item mainWeapon;
        if (roll < 0.3) {
            // 30% chance for steel sword
            mainWeapon = new SteelSword();
        } else if (roll < 0.6) {
            // 30% chance for iron sword
            mainWeapon = new IronSword();
        } else {
            // 40% chance for dagger
            mainWeapon = new Dagger();
        }

        addToInventory(mainWeapon);
        equipWeapon(mainWeapon);

        // Add throwables (2-4 throwing knives or 1-2 throwing axes)
        if (Math.random() < 0.6) {
            // 60% chance for throwing knives
            int numKnives = 2 + (int)(Math.random() * 3);  // 2-4 knives
            for (int i = 0; i < numKnives; i++) {
                addToInventory(new ThrowingKnife());
            }
        } else {
            // 40% chance for throwing axes
            int numAxes = 1 + (int)(Math.random() * 2);  // 1-2 axes
            for (int i = 0; i < numAxes; i++) {
                addToInventory(new ThrowingAxe());
            }
        }

        // Configure ranged attack range for throwables
        this.preferredAttackRange = 200;
        this.attackRange = 200;  // Match attackRange so AI enters attack state at range
        this.projectileCooldown = 1.5;
    }

    /**
     * Gets the mob type identifier.
     * @return "bandit"
     */
    public String getMobType() {
        return "bandit";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A ruthless outlaw who preys on travelers.";
    }
}
