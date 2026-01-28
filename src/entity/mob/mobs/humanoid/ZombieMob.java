package entity.mob.mobs.humanoid;

import entity.mob.SpriteMobEntity;
import entity.item.Item;
import entity.item.items.weapons.melee.WoodenSword;
import entity.item.items.weapons.melee.IronSword;
import entity.item.items.weapons.melee.Mace;

/**
 * Zombie mob - a slow, persistent undead enemy.
 *
 * Zombies are slow-moving but relentless pursuers. They have moderate health
 * and deal decent damage with their melee attacks. They are typically found
 * in graveyards, dungeons, and at night.
 *
 * Stats:
 * - Health: 50
 * - Damage: 8
 * - Walk Speed: 40
 * - Chase Speed: 60 (slow but persistent)
 *
 * Special Traits:
 * - Slow movement but never gives up chase
 * - Can be burned by fire attacks
 */
public class ZombieMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/zombie";

    /**
     * Creates a zombie at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public ZombieMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a zombie with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing zombie sprites
     */
    public ZombieMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures zombie-specific stats.
     */
    private void configureStats() {
        // Health
        this.maxHealth = 50;
        this.currentHealth = maxHealth;

        // Combat
        this.attackDamage = 8;
        this.attackRange = 60;
        this.attackCooldown = 1.2;

        // Movement (slow but relentless)
        this.wanderSpeed = 40;
        this.chaseSpeed = 60;

        // Detection
        this.detectionRange = 250;
        this.loseTargetRange = 450;

        // Zombies are always hostile
        setHostile(true);

        // ==================== Weapon Usage Configuration ====================

        // Zombies only use melee weapons (no finesse required)
        setWeaponPreference(WeaponPreference.MELEE_ONLY);

        // Initialize inventory with crude weapons (some zombies are unarmed)
        initializeZombieInventory();
    }

    /**
     * Initializes the zombie's inventory with crude melee weapons.
     * Not all zombies carry weapons - some fight with bare hands.
     */
    private void initializeZombieInventory() {
        // Only 60% of zombies carry weapons
        if (Math.random() > 0.6) {
            return;  // Unarmed zombie uses base damage
        }

        Item weapon;

        // Random loadout selection - zombies have crude or scavenged weapons
        double roll = Math.random();
        if (roll < 0.2) {
            // 20% chance for mace (heavier damage)
            weapon = new Mace();
        } else if (roll < 0.5) {
            // 30% chance for iron sword (better weapon)
            weapon = new IronSword();
        } else {
            // 50% chance for wooden sword (basic weapon)
            weapon = new WoodenSword();
        }

        addToInventory(weapon);
        equipWeapon(weapon);
    }

    /**
     * Gets the mob type identifier.
     * @return "zombie"
     */
    public String getMobType() {
        return "zombie";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A shambling undead creature, slow but relentless in pursuit of the living.";
    }
}
