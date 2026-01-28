package entity.mob.mobs.humanoid;

import entity.mob.SpriteMobEntity;
import entity.item.Item;
import entity.item.items.weapons.melee.BattleAxe;
import entity.item.items.weapons.melee.Mace;
import entity.item.items.weapons.melee.ThunderHammer;
import entity.item.items.weapons.throwing.ThrowingAxe;

/**
 * Orc mob - a strong, tank-like enemy.
 *
 * Orcs are powerful warriors with high health and damage. They are
 * slower than most humanoids but hit hard and can take a lot of
 * punishment. They are often found leading goblin groups.
 *
 * Stats:
 * - Health: 60
 * - Damage: 15
 * - Walk Speed: 40
 * - Chase Speed: 60
 *
 * Special Traits:
 * - High health pool
 * - Heavy damage
 * - Slower movement
 */
public class OrcMob extends SpriteMobEntity {

    // Default sprite directory
    private static final String DEFAULT_SPRITE_DIR = "assets/mobs/orc";

    /**
     * Creates an orc at the specified position.
     *
     * @param x X position
     * @param y Y position
     */
    public OrcMob(int x, int y) {
        this(x, y, DEFAULT_SPRITE_DIR);
    }

    /**
     * Creates an orc with custom sprites.
     *
     * @param x X position
     * @param y Y position
     * @param spriteDir Directory containing orc sprites
     */
    public OrcMob(int x, int y, String spriteDir) {
        super(x, y, spriteDir);
        configureStats();
    }

    /**
     * Configures orc-specific stats.
     */
    private void configureStats() {
        // Health (tank)
        this.maxHealth = 60;
        this.currentHealth = maxHealth;

        // Combat (heavy hitter)
        this.attackDamage = 15;
        this.attackRange = 70;
        this.attackCooldown = 1.5;

        // Movement (slow but steady)
        this.wanderSpeed = 40;
        this.chaseSpeed = 60;

        // Detection
        this.detectionRange = 200;
        this.loseTargetRange = 350;

        // Orcs are always hostile
        setHostile(true);

        // ==================== Weapon Usage Configuration ====================

        // Orcs use melee weapons and can throw axes
        setWeaponPreference(WeaponPreference.MELEE_AND_THROWABLE);

        // Initialize inventory with weapons
        initializeOrcInventory();
    }

    /**
     * Initializes the orc's inventory with appropriate heavy weapons.
     * Orcs prefer axes, maces, and hammers.
     */
    private void initializeOrcInventory() {
        Item mainWeapon;

        // Random loadout selection - orcs have heavy weapons
        double roll = Math.random();
        if (roll < 0.05) {
            // 5% chance for legendary thunder hammer
            mainWeapon = new ThunderHammer();
        } else if (roll < 0.5) {
            // 45% chance for battle axe
            mainWeapon = new BattleAxe();
        } else {
            // 50% chance for mace
            mainWeapon = new Mace();
        }

        addToInventory(mainWeapon);
        equipWeapon(mainWeapon);

        // Add throwing axes (1-2)
        int numAxes = 1 + (int)(Math.random() * 2);
        for (int i = 0; i < numAxes; i++) {
            addToInventory(new ThrowingAxe());
        }

        // Configure ranged attack range for throwables
        this.preferredAttackRange = 180;
        this.attackRange = 180;  // Match attackRange so AI enters attack state at range
        this.projectileCooldown = 2.0;  // Slow throw rate
    }

    /**
     * Gets the mob type identifier.
     * @return "orc"
     */
    public String getMobType() {
        return "orc";
    }

    /**
     * Gets a description of this mob.
     * @return Description string
     */
    public String getDescription() {
        return "A hulking green-skinned warrior with immense strength.";
    }
}
