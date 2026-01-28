package entity.mob.mobs.humanoid;

import entity.mob.SpriteMobEntity;
import entity.item.Item;
import entity.item.items.weapons.melee.Dagger;
import entity.item.items.weapons.melee.PoisonDagger;
import entity.item.items.weapons.throwing.ThrowingKnife;
import entity.item.items.weapons.throwing.Rock;

/**
 * Goblin mob - a fast, weak enemy that attacks in groups.
 *
 * Goblins are small, agile creatures that rely on speed to overwhelm
 * their targets. They deal moderate damage but have low health.
 * They are often found in camps and caves.
 *
 * Stats:
 * - Health: 40
 * - Damage: 5
 * - Walk Speed: 60
 * - Chase Speed: 100 (very fast)
 *
 * Special Traits:
 * - Very fast movement
 * - Lower health
 * - Quick attack cooldown
 */
public class GoblinMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/goblin";

    /**
     * Creates a goblin at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public GoblinMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates a goblin with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing goblin sprites
     */
    public GoblinMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures goblin-specific stats.
     */
    private void configureStats() {
        // Health (weak)
        this.maxHealth = 40;
        this.currentHealth = maxHealth;

        // Combat (quick attacks)
        this.attackDamage = 5;
        this.attackRange = 45;
        this.attackCooldown = 0.8;

        // Movement (very fast)
        this.wanderSpeed = 60;
        this.chaseSpeed = 100;

        // Detection
        this.detectionRange = 250;
        this.loseTargetRange = 400;

        // Goblins can double jump to catch prey
        setMaxJumps(2);

        // Goblins are always hostile
        setHostile(true);

        // ==================== Weapon Usage Configuration ====================

        // Goblins use daggers and throwables
        setWeaponPreference(WeaponPreference.MELEE_AND_THROWABLE);

        // Initialize inventory with weapons
        initializeGoblinInventory();
    }

    /**
     * Initializes the goblin's inventory with light weapons and throwables.
     * Goblins prefer quick daggers and throwing rocks/knives.
     */
    private void initializeGoblinInventory() {
        Item mainWeapon;

        // Random loadout selection - goblins have light weapons
        double roll = Math.random();
        if (roll < 0.2) {
            // 20% chance for poison dagger
            mainWeapon = new PoisonDagger();
        } else {
            // 80% chance for regular dagger
            mainWeapon = new Dagger();
        }

        addToInventory(mainWeapon);
        equipWeapon(mainWeapon);

        // Add throwables (mix of rocks and throwing knives)
        if (Math.random() < 0.4) {
            // 40% chance for throwing knives (1-2)
            int numKnives = 1 + (int)(Math.random() * 2);
            for (int i = 0; i < numKnives; i++) {
                addToInventory(new ThrowingKnife());
            }
        } else {
            // 60% chance for rocks (2-4)
            int numRocks = 2 + (int)(Math.random() * 3);
            for (int i = 0; i < numRocks; i++) {
                addToInventory(new Rock());
            }
        }

        // Configure ranged attack range for throwables
        this.preferredAttackRange = 150;
        this.projectileCooldown = 1.0;  // Fast throw rate
    }

    /**
     * Gets the mob type identifier.
     * @return "goblin"
     */
    public String getMobType() {
        return "goblin";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A small, cunning creature that attacks with surprising speed.";
    }
}
