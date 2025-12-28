package entity;

import entity.Item.ItemCategory;
import entity.Item.ItemRarity;
import entity.ProjectileEntity.ProjectileType;

import java.util.HashMap;
import java.util.Map;

/**
 * ItemRegistry provides access to predefined item templates.
 * Use this to create new instances of common items.
 *
 * Features:
 * - Predefined weapons, ranged weapons, tools, armor, food, potions
 * - Consistent item stats and properties
 * - Easy creation of item instances
 *
 * Usage:
 *   Item sword = ItemRegistry.create("iron_sword");
 *   Item crossbow = ItemRegistry.create("crossbow");
 */
public class ItemRegistry {

    private static final Map<String, Item> templates = new HashMap<>();
    private static boolean initialized = false;

    /**
     * Initializes all item templates.
     */
    public static void initialize() {
        if (initialized) return;

        // ==================== MELEE WEAPONS ====================

        registerMeleeWeapon("wooden_sword", "Wooden Sword", 5, 1.2f, 50,
                ItemRarity.COMMON, "A basic training sword");

        registerMeleeWeapon("iron_sword", "Iron Sword", 12, 1.0f, 60,
                ItemRarity.COMMON, "A reliable iron blade");

        registerMeleeWeapon("steel_sword", "Steel Sword", 18, 1.0f, 65,
                ItemRarity.UNCOMMON, "A well-crafted steel blade");

        registerMeleeWeapon("fire_sword", "Flame Blade", 20, 0.9f, 65,
                ItemRarity.RARE, "Burns enemies on hit")
                .setSpecialEffect("Burn damage over time");

        registerMeleeWeapon("ice_sword", "Frost Edge", 18, 0.9f, 65,
                ItemRarity.RARE, "Slows enemies on hit")
                .setSpecialEffect("Slow effect for 2 seconds");

        registerMeleeWeapon("legendary_sword", "Excalibur", 35, 1.2f, 80,
                ItemRarity.LEGENDARY, "The sword of kings")
                .setCritChance(0.15f);

        registerMeleeWeapon("dagger", "Iron Dagger", 8, 2.0f, 40,
                ItemRarity.COMMON, "Quick but short range");

        registerMeleeWeapon("battle_axe", "Battle Axe", 25, 0.7f, 70,
                ItemRarity.UNCOMMON, "Slow but powerful");

        registerMeleeWeapon("mace", "Iron Mace", 15, 0.9f, 55,
                ItemRarity.COMMON, "Good against armored foes");

        // ==================== RANGED WEAPONS ====================

        registerRangedWeapon("wooden_bow", "Wooden Bow", 8, 12.0f, ProjectileType.ARROW,
                ItemRarity.COMMON, "A simple hunting bow")
                .setAmmoItemName("arrow");

        registerRangedWeapon("longbow", "Longbow", 15, 18.0f, ProjectileType.ARROW,
                ItemRarity.UNCOMMON, "Greater range and power")
                .setAmmoItemName("arrow");

        registerRangedWeapon("crossbow", "Crossbow", 20, 20.0f, ProjectileType.BOLT,
                ItemRarity.UNCOMMON, "Powerful but slow to reload")
                .setAmmoItemName("bolt");

        registerRangedWeapon("heavy_crossbow", "Heavy Crossbow", 30, 22.0f, ProjectileType.BOLT,
                ItemRarity.RARE, "Devastating power");
        templates.get("heavy_crossbow").setAttackSpeed(0.5f);
        templates.get("heavy_crossbow").setAmmoItemName("bolt");

        registerRangedWeapon("magic_wand", "Magic Wand", 10, 15.0f, ProjectileType.MAGIC_BOLT,
                ItemRarity.UNCOMMON, "Fires magic bolts")
                .setAmmoItemName("mana"); // Uses mana

        registerRangedWeapon("fire_staff", "Staff of Fire", 25, 12.0f, ProjectileType.FIREBALL,
                ItemRarity.RARE, "Launches explosive fireballs");
        templates.get("fire_staff").setSpecialEffect("Explosion on impact");
        templates.get("fire_staff").setAmmoItemName("mana");

        registerRangedWeapon("ice_staff", "Staff of Ice", 18, 14.0f, ProjectileType.ICEBALL,
                ItemRarity.RARE, "Freezes enemies");
        templates.get("ice_staff").setSpecialEffect("Slow effect");
        templates.get("ice_staff").setAmmoItemName("mana");

        registerRangedWeapon("arcane_staff", "Arcane Staff", 35, 16.0f, ProjectileType.MAGIC_BOLT,
                ItemRarity.EPIC, "Channels pure arcane energy");
        templates.get("arcane_staff").setCritChance(0.20f);
        templates.get("arcane_staff").setAmmoItemName("mana");

        // ==================== THROWING WEAPONS ====================

        registerThrowingWeapon("throwing_knife", "Throwing Knife", 10, 18.0f,
                ItemRarity.COMMON, "Quick and accurate");

        registerThrowingWeapon("throwing_axe", "Throwing Axe", 18, 14.0f,
                ItemRarity.UNCOMMON, "Heavy but powerful");

        registerThrowingWeapon("rock", "Rock", 5, 12.0f,
                ItemRarity.COMMON, "A simple projectile");

        // ==================== AMMO ====================

        registerAmmo("arrow", "Arrow", 5, ProjectileType.ARROW,
                ItemRarity.COMMON, "Standard ammunition for bows");

        registerAmmo("fire_arrow", "Fire Arrow", 8, ProjectileType.ARROW,
                ItemRarity.UNCOMMON, "Arrows that burn on impact");

        registerAmmo("ice_arrow", "Ice Arrow", 7, ProjectileType.ARROW,
                ItemRarity.UNCOMMON, "Arrows that slow enemies");

        registerAmmo("bolt", "Bolt", 8, ProjectileType.BOLT,
                ItemRarity.COMMON, "Standard crossbow ammunition");

        registerAmmo("heavy_bolt", "Heavy Bolt", 12, ProjectileType.BOLT,
                ItemRarity.UNCOMMON, "Heavier bolts for more damage");

        registerAmmo("mana_crystal", "Mana Crystal", 0, null,
                ItemRarity.UNCOMMON, "Powers magic weapons");

        // ==================== THROWABLES (Consumable) ====================

        registerThrowableAmmo("bomb", "Bomb", 40, ProjectileType.BOMB,
                ItemRarity.UNCOMMON, "Explodes on impact");

        registerThrowableAmmo("throwing_potion", "Throwing Potion", 15, ProjectileType.POTION,
                ItemRarity.COMMON, "Explodes in a splash");

        // ==================== TOOLS ====================

        registerTool("wooden_pickaxe", "Wooden Pickaxe", 3,
                ItemRarity.COMMON, "Mines stone slowly");

        registerTool("iron_pickaxe", "Iron Pickaxe", 5,
                ItemRarity.COMMON, "Standard mining tool");

        registerTool("golden_pickaxe", "Golden Pickaxe", 8,
                ItemRarity.UNCOMMON, "Mines quickly but breaks fast");

        registerTool("wooden_axe", "Wooden Axe", 4,
                ItemRarity.COMMON, "Chops wood");

        registerTool("iron_axe", "Iron Axe", 8,
                ItemRarity.COMMON, "Better for chopping");

        registerTool("wooden_shovel", "Wooden Shovel", 2,
                ItemRarity.COMMON, "Digs soft ground");

        registerTool("iron_shovel", "Iron Shovel", 4,
                ItemRarity.COMMON, "Faster digging");

        // ==================== ARMOR ====================

        registerArmor("iron_helmet", "Iron Helmet", 5,
                ItemRarity.COMMON, "Basic head protection");

        registerArmor("iron_chestplate", "Iron Chestplate", 10,
                ItemRarity.COMMON, "Basic chest protection");

        registerArmor("iron_leggings", "Iron Leggings", 7,
                ItemRarity.COMMON, "Basic leg protection");

        registerArmor("iron_boots", "Iron Boots", 4,
                ItemRarity.COMMON, "Basic foot protection");

        registerArmor("steel_helmet", "Steel Helmet", 8,
                ItemRarity.UNCOMMON, "Better head protection");

        registerArmor("steel_chestplate", "Steel Chestplate", 15,
                ItemRarity.UNCOMMON, "Better chest protection");

        registerArmor("wizard_hat", "Wizard Hat", 2,
                ItemRarity.UNCOMMON, "Increases mana regen")
                .setSpecialEffect("+10% mana regeneration");

        // ==================== FOOD ====================

        registerFood("bread", "Bread", 20, 0, 10,
                ItemRarity.COMMON, "A hearty loaf of bread");

        registerFood("apple", "Apple", 10, 0, 5,
                ItemRarity.COMMON, "A fresh apple");

        registerFood("cooked_meat", "Cooked Meat", 40, 0, 20,
                ItemRarity.COMMON, "A filling meal");

        registerFood("cheese", "Cheese", 15, 0, 8,
                ItemRarity.COMMON, "A wedge of cheese");

        registerFood("golden_apple", "Golden Apple", 50, 20, 30,
                ItemRarity.RARE, "A magical fruit")
                .setSpecialEffect("Temporary invincibility");

        // ==================== POTIONS ====================

        registerPotion("health_potion", "Health Potion", 50, 0, 0,
                ItemRarity.COMMON, "Restores 50 health");

        registerPotion("mana_potion", "Mana Potion", 0, 50, 0,
                ItemRarity.COMMON, "Restores 50 mana");

        registerPotion("stamina_potion", "Stamina Potion", 0, 0, 100,
                ItemRarity.COMMON, "Restores all stamina");

        registerPotion("greater_health_potion", "Greater Health Potion", 100, 0, 0,
                ItemRarity.UNCOMMON, "Fully restores health");

        registerPotion("strength_potion", "Strength Potion", 0, 0, 0,
                ItemRarity.UNCOMMON, "Temporarily increases damage")
                .setSpecialEffect("+50% damage for 30 seconds");

        registerPotion("speed_potion", "Speed Potion", 0, 0, 0,
                ItemRarity.UNCOMMON, "Temporarily increases speed")
                .setSpecialEffect("+30% movement speed for 30 seconds");

        registerPotion("exploding_potion", "Exploding Potion", 0, 0, 0,
                ItemRarity.RARE, "Explosive on impact")
                .setAreaEffect(true, 96);

        // ==================== MATERIALS ====================

        registerMaterial("string", "String", ItemRarity.COMMON,
                "Used for crafting bows");

        registerMaterial("iron_ingot", "Iron Ingot", ItemRarity.COMMON,
                "Refined iron for crafting");

        registerMaterial("gold_ingot", "Gold Ingot", ItemRarity.UNCOMMON,
                "Precious metal");

        registerMaterial("diamond", "Diamond", ItemRarity.RARE,
                "A valuable gemstone");

        registerMaterial("magic_crystal", "Magic Crystal", ItemRarity.RARE,
                "Imbued with arcane energy");

        registerMaterial("scroll", "Scroll", ItemRarity.UNCOMMON,
                "Contains ancient knowledge");

        // ==================== KEYS ====================

        registerKey("bronze_key", "Bronze Key", ItemRarity.COMMON,
                "Opens bronze locks");

        registerKey("silver_key", "Silver Key", ItemRarity.UNCOMMON,
                "Opens silver locks");

        registerKey("golden_key", "Golden Key", ItemRarity.RARE,
                "Opens golden locks");

        registerKey("skeleton_key", "Skeleton Key", ItemRarity.EPIC,
                "Opens many locks");

        initialized = true;
    }

    // ==================== Registration Helpers ====================

    private static Item registerMeleeWeapon(String id, String name, int damage, float speed,
                                             int range, ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.WEAPON);
        item.setDamage(damage);
        item.setAttackSpeed(speed);
        item.setRange(range);
        item.setRarity(rarity);
        item.setDescription(desc);
        templates.put(id, item);
        return item;
    }

    private static Item registerRangedWeapon(String id, String name, int damage, float projSpeed,
                                              ProjectileType projType, ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.RANGED_WEAPON);
        item.setRangedWeapon(true, projType, damage, projSpeed);
        item.setRarity(rarity);
        item.setDescription(desc);
        item.setAmmoItemName("arrow"); // Default ammo
        templates.put(id, item);
        return item;
    }

    private static Item registerThrowingWeapon(String id, String name, int damage, float speed,
                                                ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.THROWABLE);
        ProjectileType type = id.contains("knife") ? ProjectileType.THROWING_KNIFE :
                              id.contains("axe") ? ProjectileType.THROWING_AXE :
                              ProjectileType.ROCK;
        item.setRangedWeapon(true, type, damage, speed);
        item.setRarity(rarity);
        item.setDescription(desc);
        item.setStackable(true);
        item.setMaxStackSize(16);
        templates.put(id, item);
        return item;
    }

    private static Item registerTool(String id, String name, int damage,
                                      ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.TOOL);
        item.setDamage(damage);
        item.setRarity(rarity);
        item.setDescription(desc);
        templates.put(id, item);
        return item;
    }

    private static Item registerArmor(String id, String name, int defense,
                                       ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.ARMOR);
        item.setDefense(defense);
        item.setRarity(rarity);
        item.setDescription(desc);
        templates.put(id, item);
        return item;
    }

    private static Item registerFood(String id, String name, int health, int mana, int stamina,
                                       ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.FOOD);
        item.setHealthRestore(health);
        item.setManaRestore(mana);
        item.setStaminaRestore(stamina);
        item.setConsumeTime(1.5f);
        item.setRarity(rarity);
        item.setDescription(desc);
        templates.put(id, item);
        return item;
    }

    private static Item registerPotion(String id, String name, int health, int mana, int stamina,
                                         ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.POTION);
        item.setHealthRestore(health);
        item.setManaRestore(mana);
        item.setStaminaRestore(stamina);
        item.setConsumeTime(0.5f);
        item.setRarity(rarity);
        item.setDescription(desc);
        templates.put(id, item);
        return item;
    }

    private static Item registerMaterial(String id, String name, ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.MATERIAL);
        item.setRarity(rarity);
        item.setDescription(desc);
        templates.put(id, item);
        return item;
    }

    private static Item registerKey(String id, String name, ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.KEY);
        item.setRarity(rarity);
        item.setDescription(desc);
        item.setStackable(false);
        templates.put(id, item);
        return item;
    }

    private static Item registerAmmo(String id, String name, int bonusDamage, ProjectileType projType,
                                       ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.MATERIAL);  // Ammo is a stackable material
        item.setDamage(bonusDamage);  // Bonus damage added to weapon damage
        item.setRarity(rarity);
        item.setDescription(desc);
        item.setStackable(true);
        item.setMaxStackSize(16);  // Arrows/bolts stack up to 16
        // Store projectile type info in the ammo item for reference
        if (projType != null) {
            item.setRangedWeapon(false, projType, bonusDamage, 0);
        }
        templates.put(id, item);
        return item;
    }

    private static Item registerThrowableAmmo(String id, String name, int damage, ProjectileType projType,
                                               ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.THROWABLE);
        item.setRangedWeapon(true, projType, damage, 14.0f);  // Throwables are self-consuming ranged weapons
        item.setRarity(rarity);
        item.setDescription(desc);
        item.setStackable(true);
        item.setMaxStackSize(16);
        templates.put(id, item);
        return item;
    }

    // ==================== Public API ====================

    /**
     * Creates a new instance of an item by ID.
     *
     * @param id Item ID (e.g., "iron_sword", "crossbow")
     * @return New Item instance, or null if not found
     */
    public static Item create(String id) {
        initialize();
        Item template = templates.get(id);
        if (template != null) {
            return new Item(template);
        }
        System.err.println("ItemRegistry: Unknown item ID: " + id);
        return null;
    }

    /**
     * Gets all registered item IDs.
     */
    public static java.util.Set<String> getAllItemIds() {
        initialize();
        return templates.keySet();
    }

    /**
     * Gets the template item (do not modify!).
     */
    public static Item getTemplate(String id) {
        initialize();
        return templates.get(id);
    }

    /**
     * Checks if an item ID exists.
     */
    public static boolean exists(String id) {
        initialize();
        return templates.containsKey(id);
    }
}
