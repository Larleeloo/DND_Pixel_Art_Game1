package entity;

import animation.ItemAnimationState;
import animation.TriggeredAnimationManager;
import entity.Item.ItemCategory;
import entity.Item.ItemRarity;
import entity.ProjectileEntity.ProjectileType;
import graphics.AnimatedTexture;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * ItemRegistry provides access to predefined item templates.
 * Use this to create new instances of common items.
 *
 * Features:
 * - Predefined weapons, ranged weapons, tools, armor, food, potions
 * - Consistent item stats and properties
 * - Easy creation of item instances
 * - Support for animation folders with multiple states per item
 *
 * Animation Folder Structure:
 *   assets/items/{item_id}/idle.gif
 *   assets/items/{item_id}/draw.gif  (for bows)
 *   assets/items/{item_id}/fire.gif
 *   assets/items/{item_id}/attack.gif
 *
 * Legacy Support:
 *   assets/items/{item_id}.gif (single animation fallback)
 *
 * Usage:
 *   Item sword = ItemRegistry.create("iron_sword");
 *   Item crossbow = ItemRegistry.create("crossbow");
 */
public class ItemRegistry {

    private static final Map<String, Item> templates = new HashMap<>();
    private static final Set<String> itemsWithAnimationFolders = new HashSet<>();
    private static boolean initialized = false;

    // Base paths for assets
    private static final String ITEMS_BASE_PATH = "assets/items/";
    private static final String BLOCKS_BASE_PATH = "assets/textures/blocks/";

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
        // Make bows chargeable: 2 sec charge, NO mana cost (uses arrows), 2x damage
        templates.get("wooden_bow").setChargeable(true, 2.0f, 0, 2.0f);
        templates.get("wooden_bow").setChargeSpeedMultiplier(1.5f);  // Faster arrows when charged

        registerRangedWeapon("longbow", "Longbow", 15, 18.0f, ProjectileType.ARROW,
                ItemRarity.UNCOMMON, "Greater range and power")
                .setAmmoItemName("arrow");
        // Longbow: 2.5 sec charge, NO mana cost (uses arrows), 2.5x damage
        templates.get("longbow").setChargeable(true, 2.5f, 0, 2.5f);
        templates.get("longbow").setChargeSpeedMultiplier(1.8f);  // Even faster arrows

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
        // Fire staff: 3 sec charge, 30 mana, 3x damage, larger projectile
        templates.get("fire_staff").setChargeable(true, 3.0f, 30, 3.0f);
        templates.get("fire_staff").setChargeSizeMultiplier(2.5f);

        registerRangedWeapon("ice_staff", "Staff of Ice", 18, 14.0f, ProjectileType.ICEBALL,
                ItemRarity.RARE, "Freezes enemies");
        templates.get("ice_staff").setSpecialEffect("Slow effect");
        templates.get("ice_staff").setAmmoItemName("mana");
        // Ice staff: 2.5 sec charge, 25 mana, 2.5x damage
        templates.get("ice_staff").setChargeable(true, 2.5f, 25, 2.5f);
        templates.get("ice_staff").setChargeSizeMultiplier(2.0f);

        registerRangedWeapon("arcane_staff", "Arcane Staff", 35, 16.0f, ProjectileType.MAGIC_BOLT,
                ItemRarity.EPIC, "Channels pure arcane energy");
        templates.get("arcane_staff").setCritChance(0.20f);
        templates.get("arcane_staff").setAmmoItemName("mana");
        // Arcane staff: 4 sec charge for massive damage, 40 mana, 4x damage
        templates.get("arcane_staff").setChargeable(true, 4.0f, 40, 4.0f);
        templates.get("arcane_staff").setChargeSizeMultiplier(3.0f);
        templates.get("arcane_staff").setChargeSpeedMultiplier(2.0f);

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
        // Fire arrows: 3 sec burn, 5 damage per tick (0.5s), 1.2x impact damage
        templates.get("fire_arrow").setStatusEffect(
            entity.ProjectileEntity.StatusEffectType.BURNING, 3.0, 5, 1.2f);
        templates.get("fire_arrow").setSpecialEffect("Burns for 3 seconds");

        registerAmmo("ice_arrow", "Ice Arrow", 7, ProjectileType.ARROW,
                ItemRarity.UNCOMMON, "Arrows that slow enemies");
        // Ice arrows: 4 sec slow (60% speed), 3 damage per tick (1s), 1.1x impact damage
        templates.get("ice_arrow").setStatusEffect(
            entity.ProjectileEntity.StatusEffectType.FROZEN, 4.0, 3, 1.1f);
        templates.get("ice_arrow").setSpecialEffect("Slows for 4 seconds");

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

        // ==================== LEGENDARY WEAPONS (Discord Bot Items) ====================

        registerMeleeWeapon("necromancers_blade", "Necromancer's Blade", 28, 1.1f, 70,
                ItemRarity.EPIC, "A blade infused with dark energy")
                .setSpecialEffect("Lifesteal on hit");

        Item electrifiedKatana = registerMeleeWeapon("electrified_katana", "Electrified Katana", 32, 1.4f, 75,
                ItemRarity.LEGENDARY, "Crackles with lightning energy");
        electrifiedKatana.setSpecialEffect("Chain lightning on critical hits");
        electrifiedKatana.setCritChance(0.20f);

        Item etherealDragonslayer = registerMeleeWeapon("ethereal_dragonslayer", "Ethereal DragonSlayer Blade", 50, 0.8f, 90,
                ItemRarity.MYTHIC, "Forged to slay ancient dragons");
        etherealDragonslayer.setSpecialEffect("+200% damage to dragons");
        etherealDragonslayer.setCritChance(0.25f);

        registerMeleeWeapon("soulbound_dagger", "Soulbound Dagger", 15, 2.5f, 45,
                ItemRarity.RARE, "Bound to your soul, returns when thrown")
                .setSpecialEffect("Returns to owner");

        // ==================== EPIC RANGED WEAPONS (Discord Bot Items) ====================

        registerRangedWeapon("epic_bow", "Epic Bow", 22, 20.0f, ProjectileType.ARROW,
                ItemRarity.EPIC, "A masterfully crafted bow")
                .setAmmoItemName("arrow");
        templates.get("epic_bow").setChargeable(true, 1.8f, 0, 2.8f);
        templates.get("epic_bow").setChargeSpeedMultiplier(2.0f);
        templates.get("epic_bow").setCritChance(0.15f);

        registerRangedWeapon("summoning_rod", "Summoning Rod", 18, 10.0f, ProjectileType.MAGIC_BOLT,
                ItemRarity.RARE, "Calls forth magical entities")
                .setAmmoItemName("mana");
        templates.get("summoning_rod").setSpecialEffect("Summons spectral allies");
        templates.get("summoning_rod").setChargeable(true, 3.0f, 35, 2.5f);

        registerRangedWeapon("lightning_rod", "Lightning Rod", 30, 25.0f, ProjectileType.MAGIC_BOLT,
                ItemRarity.RARE, "Channels the power of storms")
                .setAmmoItemName("mana");
        templates.get("lightning_rod").setSpecialEffect("Chain lightning effect");
        templates.get("lightning_rod").setChargeable(true, 2.0f, 20, 3.0f);

        registerRangedWeapon("blazing_rod", "Blazing Rod", 25, 14.0f, ProjectileType.FIREBALL,
                ItemRarity.RARE, "Burns with eternal flame")
                .setAmmoItemName("mana");
        templates.get("blazing_rod").setSpecialEffect("Sets enemies ablaze");
        templates.get("blazing_rod").setChargeable(true, 2.5f, 25, 2.5f);

        registerRangedWeapon("magic_fishing_rod", "Magic Fishing Rod", 8, 12.0f, ProjectileType.MAGIC_BOLT,
                ItemRarity.UNCOMMON, "Catches fish and magic alike")
                .setAmmoItemName("mana");
        templates.get("magic_fishing_rod").setSpecialEffect("Pulls items toward you");

        // ==================== SHIELDS & ARMOR (Discord Bot Items) ====================

        registerArmor("steel_shield", "Steel Shield", 12,
                ItemRarity.UNCOMMON, "A sturdy defensive shield")
                .setSpecialEffect("Block incoming projectiles");

        registerArmor("sentinel_gauntlets", "Sentinel Gauntlets", 8,
                ItemRarity.UNCOMMON, "Gauntlets of a watchful guardian")
                .setSpecialEffect("+5% attack speed");

        registerArmor("gold_armor_helmet", "Gold Armor Helmet", 6,
                ItemRarity.UNCOMMON, "Ornate golden headpiece");

        registerArmor("gold_armor_chest", "Gold Armor Chestplate", 12,
                ItemRarity.UNCOMMON, "Gleaming golden protection");

        registerArmor("gold_armor_legs", "Gold Armor Leggings", 9,
                ItemRarity.UNCOMMON, "Royal golden leg guards");

        registerArmor("gold_armor_boots", "Gold Armor Boots", 5,
                ItemRarity.UNCOMMON, "Shimmering golden footwear");

        registerArmor("chainmail_armor", "Chainmail Armor", 8,
                ItemRarity.COMMON, "Flexible chain protection");

        registerArmor("dragon_scale_armor", "Dragon Scale Armor", 25,
                ItemRarity.LEGENDARY, "Forged from dragon scales")
                .setSpecialEffect("+50% fire resistance");

        registerArmor("fancy_boots", "Fancy Boots", 3,
                ItemRarity.UNCOMMON, "Stylish and somewhat protective")
                .setSpecialEffect("+10% movement speed");

        // ==================== CLOTHING (Discord Bot Items) ====================

        registerClothing("green_dress", "Green Dress", ItemRarity.COMMON,
                "An elegant green gown");

        registerClothing("orange_dress", "Orange Dress", ItemRarity.COMMON,
                "A vibrant orange dress");

        registerClothing("black_dress", "Black Dress", ItemRarity.UNCOMMON,
                "A sophisticated dark dress");

        registerClothing("hat", "Hat", ItemRarity.COMMON,
                "A simple but stylish hat");

        registerClothing("pants", "Pants", ItemRarity.COMMON,
                "Basic comfortable trousers");

        registerClothing("shirt", "Shirt", ItemRarity.COMMON,
                "A simple cloth shirt");

        registerClothing("three_piece_suit", "3-Piece Suit", ItemRarity.UNCOMMON,
                "Formal attire for special occasions");

        registerClothing("swimwear", "Swimwear", ItemRarity.COMMON,
                "Perfect for beach adventures");

        registerClothing("yellow_cloak", "Yellow Cloak", ItemRarity.UNCOMMON,
                "A bright flowing cloak");

        registerClothing("chameleon_cloak", "Chameleon Cloak", ItemRarity.RARE,
                "Changes color to blend in")
                .setSpecialEffect("Partial invisibility");

        registerClothing("witchs_hat", "Witch's Hat", ItemRarity.UNCOMMON,
                "A pointy magical hat")
                .setSpecialEffect("+5% magic damage");

        registerClothing("gown_forgotten_verses", "Gown of Forgotten Verses", ItemRarity.EPIC,
                "Whispers ancient incantations")
                .setSpecialEffect("+20% mana regeneration");

        // ==================== POTIONS (Discord Bot Items) ====================

        registerPotion("lucky_potion", "Lucky Potion", 0, 0, 0,
                ItemRarity.RARE, "Increases your fortune")
                .setSpecialEffect("+25% drop rate for 60 seconds");

        registerPotion("honey_potion", "Honey Potion", 25, 10, 20,
                ItemRarity.COMMON, "Sweet and restorative");

        registerPotion("brewed_potion", "Brewed Potion", 30, 15, 0,
                ItemRarity.COMMON, "A carefully brewed elixir");

        registerMaterial("mana_leaf", "Mana Leaf", ItemRarity.UNCOMMON,
                "Restores mana when consumed");
        templates.get("mana_leaf").setManaRestore(30);
        templates.get("mana_leaf").setConsumeTime(1.0f);
        templates.get("mana_leaf").setCategory(ItemCategory.POTION);

        // ==================== FOOD (Discord Bot Items) ====================

        registerFood("cake", "Cake", 35, 5, 15,
                ItemRarity.UNCOMMON, "A delicious layered cake");

        registerFood("cookies", "Cookies", 12, 0, 8,
                ItemRarity.COMMON, "Freshly baked cookies");

        registerFood("melon", "Melon", 18, 0, 10,
                ItemRarity.COMMON, "A juicy refreshing melon");

        registerFood("pumpkin", "Pumpkin", 15, 0, 5,
                ItemRarity.COMMON, "A seasonal gourd");

        registerFood("salmon", "Salmon", 25, 0, 12,
                ItemRarity.COMMON, "Fresh caught fish");

        registerFood("chicken_egg", "Chicken Egg", 8, 0, 5,
                ItemRarity.COMMON, "A nutritious egg");

        // ==================== MATERIALS & ORES (Discord Bot Items) ====================

        registerMaterial("iron_ore", "Iron Ore", ItemRarity.COMMON,
                "Raw iron ready for smelting");

        registerMaterial("gold_coins", "Gold Coins", ItemRarity.COMMON,
                "Currency of the realm");
        templates.get("gold_coins").setStackable(true);
        templates.get("gold_coins").setMaxStackSize(64);

        registerMaterial("copper_ingots", "Copper Ingots", ItemRarity.COMMON,
                "Refined copper metal");

        registerMaterial("gold_bars", "Gold Bars", ItemRarity.UNCOMMON,
                "Refined gold, highly valuable");

        registerMaterial("iron_bars", "Iron Bars", ItemRarity.COMMON,
                "Refined iron metal");

        registerMaterial("leather", "Leather", ItemRarity.COMMON,
                "Tanned animal hide");

        registerMaterial("yarn", "Yarn", ItemRarity.COMMON,
                "Spun wool or fiber");

        registerMaterial("flour", "Flour", ItemRarity.COMMON,
                "Ground grain for baking");

        registerMaterial("mysterious_gemstone", "Mysterious Gemstone", ItemRarity.RARE,
                "Pulses with unknown energy");

        registerMaterial("dragon_egg", "Dragon Egg", ItemRarity.LEGENDARY,
                "A dormant dragon embryo");
        templates.get("dragon_egg").setStackable(false);

        registerMaterial("bonemite", "Bonemeal", ItemRarity.COMMON,
                "Ground bones for fertilizer");

        registerMaterial("ink", "Ink", ItemRarity.COMMON,
                "Used for writing and enchanting");

        registerMaterial("planks", "Planks", ItemRarity.COMMON,
                "Cut wooden boards");

        registerMaterial("rocks", "Rocks", ItemRarity.COMMON,
                "Simple stones");

        registerMaterial("rope", "Rope", ItemRarity.COMMON,
                "Strong fiber cord");

        registerMaterial("sapling", "Sapling", ItemRarity.COMMON,
                "A young tree ready to plant");

        registerMaterial("machine_parts", "Machine Parts", ItemRarity.UNCOMMON,
                "Gears and components");

        registerMaterial("skull", "Skull", ItemRarity.UNCOMMON,
                "A grim reminder of mortality");

        // ==================== TOOLS (Discord Bot Items) ====================

        registerTool("shears", "Shears", 2,
                ItemRarity.COMMON, "For cutting wool and plants");

        registerTool("fishing_rod", "Fishing Rod", 1,
                ItemRarity.COMMON, "For catching fish");

        registerTool("walking_stick", "Walking Stick", 3,
                ItemRarity.COMMON, "Helps with long journeys")
                .setSpecialEffect("+5% movement speed");

        // ==================== COLLECTIBLES & MISC (Discord Bot Items) ====================

        registerCollectible("orb", "Orb", ItemRarity.UNCOMMON,
                "A mysterious glowing sphere");

        registerCollectible("ancient_pottery", "Ancient Pottery", ItemRarity.UNCOMMON,
                "A relic from ancient times");

        registerCollectible("music_disc", "Music Disc", ItemRarity.UNCOMMON,
                "Plays enchanting melodies");

        registerCollectible("music_player", "Music Player", ItemRarity.RARE,
                "A magical music box");

        registerCollectible("journal", "Journal", ItemRarity.COMMON,
                "A book for recording thoughts");

        registerCollectible("painting_wolves", "Painting of 3 Wolves", ItemRarity.UNCOMMON,
                "A beautiful wolf painting");

        registerCollectible("painting_dog", "Painting of a Dog", ItemRarity.UNCOMMON,
                "A loyal companion portrait");

        registerCollectible("marbles", "Marbles", ItemRarity.COMMON,
                "Colorful glass spheres");

        registerCollectible("nametag", "Nametag", ItemRarity.UNCOMMON,
                "Name anything you want");

        registerCollectible("candle", "Candle", ItemRarity.COMMON,
                "Provides light in darkness");

        registerCollectible("mysterious_candle", "Mysterious Candle", ItemRarity.RARE,
                "Burns with an ethereal flame");

        registerCollectible("water_bottle", "Water Bottle", ItemRarity.COMMON,
                "Clean drinking water");

        registerCollectible("saddle", "Saddle", ItemRarity.UNCOMMON,
                "For riding mounts");

        registerCollectible("backpack", "Backpack", ItemRarity.UNCOMMON,
                "Increases carrying capacity")
                .setSpecialEffect("+8 inventory slots");

        registerCollectible("personalized_banner", "Personalized Banner", ItemRarity.UNCOMMON,
                "Your personal emblem");

        registerCollectible("frog", "Frog", ItemRarity.COMMON,
                "A small amphibian friend");

        registerCollectible("jack_o_lantern", "Jack-O-Lantern", ItemRarity.UNCOMMON,
                "A carved pumpkin that glows");

        registerCollectible("rocket", "Rocket", ItemRarity.UNCOMMON,
                "Launches into the sky");

        registerCollectible("wind_charge", "Wind Charge", ItemRarity.UNCOMMON,
                "A gust of captured wind");

        registerCollectible("undead_scroll", "Undead Scroll", ItemRarity.RARE,
                "Contains necromantic knowledge");

        registerCollectible("trip_wire_trap", "Trip-Wire Trap", ItemRarity.UNCOMMON,
                "Catches unsuspecting foes");

        registerCollectible("magic_lantern", "Magic Lantern", ItemRarity.UNCOMMON,
                "Never runs out of light");

        registerCollectible("crucible", "Crucible", ItemRarity.UNCOMMON,
                "For melting and mixing metals");

        // Mirror to Other Realms - special ranged weapon with realm-cycling projectiles
        registerMirrorToOtherRealms();

        registerCollectible("rowboat", "Rowboat", ItemRarity.UNCOMMON,
                "For crossing water");

        // Companions have been moved to player character alternates in SpriteCharacterCustomization
        // See CompanionRegistry for companion definitions

        // ==================== BLOCKS (Placeable) ====================

        registerBlock("dirt_block", "Dirt Block", ItemRarity.COMMON,
                "A block of dirt");

        registerBlock("grass_block", "Grass Block", ItemRarity.COMMON,
                "A grass-covered dirt block");

        registerBlock("stone_block", "Stone Block", ItemRarity.COMMON,
                "A solid stone block");

        registerBlock("cobblestone_block", "Cobblestone Block", ItemRarity.COMMON,
                "A rough cobblestone block");

        registerBlock("wood_block", "Wood Block", ItemRarity.COMMON,
                "A wooden block");

        registerBlock("brick_block", "Brick Block", ItemRarity.COMMON,
                "A brick block");

        registerBlock("sand_block", "Sand Block", ItemRarity.COMMON,
                "A block of sand");

        registerBlock("glass_block", "Glass Block", ItemRarity.UNCOMMON,
                "A transparent glass block");

        registerBlock("leaves_block", "Leaves Block", ItemRarity.COMMON,
                "A cluster of leaves");

        // ==================== SPECIAL CRYSTAL EDITIONS ====================

        registerCollectible("crystal_summer", "Crystal (Summer Edition)", ItemRarity.LEGENDARY,
                "A crystal infused with summer's warmth")
                .setSpecialEffect("Fire damage immunity");

        registerCollectible("crystal_winter", "Crystal (Winter Edition)", ItemRarity.LEGENDARY,
                "A crystal infused with winter's chill")
                .setSpecialEffect("Ice damage immunity");

        // ==================== MYTHIC WEAPONS (Ultimate Loot) ====================

        Item voidBlade = registerMeleeWeapon("void_blade", "Void Blade", 65, 1.3f, 85,
                ItemRarity.MYTHIC, "Forged in the heart of a black hole");
        voidBlade.setSpecialEffect("Absorbs enemy souls");
        voidBlade.setCritChance(0.30f);

        Item celestialBow = registerRangedWeapon("celestial_bow", "Celestial Bow", 45, 30.0f, ProjectileType.ARROW,
                ItemRarity.MYTHIC, "Fires arrows of pure starlight");
        celestialBow.setAmmoItemName("arrow");
        celestialBow.setChargeable(true, 1.5f, 0, 4.0f);
        celestialBow.setChargeSpeedMultiplier(3.0f);
        celestialBow.setCritChance(0.25f);

        Item infinityStaff = registerRangedWeapon("infinity_staff", "Staff of Infinity", 55, 20.0f, ProjectileType.MAGIC_BOLT,
                ItemRarity.MYTHIC, "Contains the power of infinity");
        infinityStaff.setAmmoItemName("mana");
        infinityStaff.setChargeable(true, 5.0f, 50, 5.0f);
        infinityStaff.setChargeSizeMultiplier(4.0f);
        infinityStaff.setSpecialEffect("Reality-bending damage");

        Item soulReaver = registerMeleeWeapon("soul_reaver", "Soul Reaver", 45, 1.0f, 75,
                ItemRarity.MYTHIC, "Devours the souls of the fallen");
        soulReaver.setSpecialEffect("100% lifesteal on kill");
        soulReaver.setCritChance(0.20f);

        Item timeWarp = registerMeleeWeapon("time_warp_blade", "Chrono Blade", 40, 2.0f, 70,
                ItemRarity.MYTHIC, "Bends time around its wielder");
        timeWarp.setSpecialEffect("Slows time on hit");

        // ==================== LEGENDARY WEAPONS ====================

        Item phoenixBow = registerRangedWeapon("phoenix_bow", "Phoenix Bow", 35, 22.0f, ProjectileType.ARROW,
                ItemRarity.LEGENDARY, "Rises from the ashes");
        phoenixBow.setAmmoItemName("arrow");
        phoenixBow.setChargeable(true, 2.0f, 0, 3.5f);
        phoenixBow.setSpecialEffect("Fire arrows that resurrect");

        Item frostmourne = registerMeleeWeapon("frostmourne", "Frostmourne", 42, 0.9f, 80,
                ItemRarity.LEGENDARY, "Hungers for souls");
        frostmourne.setSpecialEffect("Freezes enemies solid");
        frostmourne.setCritChance(0.18f);

        Item thunderHammer = registerMeleeWeapon("thunder_hammer", "Thunder Hammer", 55, 0.6f, 90,
                ItemRarity.LEGENDARY, "Strikes with the fury of storms");
        thunderHammer.setSpecialEffect("Chain lightning on hit");
        thunderHammer.setCritChance(0.12f);

        Item shadowDagger = registerMeleeWeapon("shadow_dagger", "Shadow Dagger", 28, 3.0f, 40,
                ItemRarity.LEGENDARY, "Strikes from the shadows");
        shadowDagger.setSpecialEffect("Invisible for 2 seconds after backstab");
        shadowDagger.setCritChance(0.35f);

        // ==================== EPIC ARMOR ====================

        registerArmor("phoenix_crown", "Phoenix Crown", 15,
                ItemRarity.EPIC, "Blazes with eternal fire")
                .setSpecialEffect("+100% fire resistance, auto-revive once");

        registerArmor("void_armor", "Void Armor", 35,
                ItemRarity.LEGENDARY, "Woven from the fabric of space")
                .setSpecialEffect("+30% damage reduction, +15% evasion");

        registerArmor("celestial_robes", "Celestial Robes", 20,
                ItemRarity.MYTHIC, "Garments of the gods")
                .setSpecialEffect("+50% mana, +30% magic damage");

        registerArmor("titan_gauntlets", "Titan Gauntlets", 18,
                ItemRarity.LEGENDARY, "Worn by ancient giants")
                .setSpecialEffect("+40% melee damage, ground slam ability");

        // ==================== MYTHIC ACCESSORIES ====================

        registerCollectible("heart_of_eternity", "Heart of Eternity", ItemRarity.MYTHIC,
                "The crystallized essence of time itself")
                .setSpecialEffect("Immortality for 10 seconds per day");

        registerCollectible("eye_of_cosmos", "Eye of the Cosmos", ItemRarity.MYTHIC,
                "See all that was, is, and will be")
                .setSpecialEffect("Reveal all secrets and treasures");

        registerCollectible("philosophers_stone", "Philosopher's Stone", ItemRarity.MYTHIC,
                "The legendary alchemical creation")
                .setSpecialEffect("Transmute any material to gold");

        registerCollectible("ankh_of_rebirth", "Ankh of Rebirth", ItemRarity.LEGENDARY,
                "Symbol of eternal life")
                .setSpecialEffect("Auto-revive with full health");

        // ==================== LEGENDARY POTIONS ====================

        registerPotion("elixir_of_immortality", "Elixir of Immortality", 0, 0, 0,
                ItemRarity.MYTHIC, "Grants temporary invincibility")
                .setSpecialEffect("Immune to all damage for 30 seconds");

        registerPotion("potion_of_ascension", "Potion of Ascension", 100, 100, 100,
                ItemRarity.LEGENDARY, "Transcend mortal limits")
                .setSpecialEffect("Double all stats for 60 seconds");

        registerPotion("essence_of_dragon", "Essence of Dragon", 0, 50, 0,
                ItemRarity.LEGENDARY, "Contains a dragon's power")
                .setSpecialEffect("Breathe fire for 30 seconds");

        // ==================== EPIC COLLECTIBLES ====================

        registerCollectible("ancient_crown", "Ancient Crown", ItemRarity.EPIC,
                "A crown worn by forgotten kings")
                .setSpecialEffect("+20% to all stats");

        registerCollectible("demon_horn", "Demon Horn", ItemRarity.EPIC,
                "Torn from a greater demon")
                .setSpecialEffect("+25% dark damage");

        registerCollectible("angel_feather", "Angel Feather", ItemRarity.EPIC,
                "A feather from divine wings")
                .setSpecialEffect("+25% holy damage, slow fall");

        registerCollectible("void_shard", "Void Shard", ItemRarity.LEGENDARY,
                "A fragment of the void itself")
                .setSpecialEffect("Phase through walls briefly");

        registerCollectible("lucky_coin", "Lucky Coin", ItemRarity.RARE,
                "Fortune favors the brave")
                .setSpecialEffect("+15% drop rate");

        registerCollectible("treasure_map", "Ancient Treasure Map", ItemRarity.RARE,
                "Marks the location of hidden riches")
                .setSpecialEffect("Reveals secret areas");

        // ==================== RARE WEAPONS ====================

        registerMeleeWeapon("crystal_sword", "Crystal Sword", 22, 1.1f, 65,
                ItemRarity.RARE, "Made of pure crystal")
                .setSpecialEffect("Reflects magic");

        registerMeleeWeapon("vampiric_blade", "Vampiric Blade", 20, 1.0f, 60,
                ItemRarity.RARE, "Thirsts for blood")
                .setSpecialEffect("10% lifesteal");

        registerMeleeWeapon("poison_dagger", "Poison Dagger", 12, 2.2f, 45,
                ItemRarity.RARE, "Coated in deadly venom")
                .setSpecialEffect("Poison damage over time");

        // Load all item textures
        loadAllItemTextures();

        initialized = true;
    }

    /**
     * Loads GIF textures for all registered items.
     *
     * New folder structure (preferred):
     *   assets/items/{item_id}/idle.gif (icon/default)
     *   assets/items/{item_id}/draw.gif (bow charging)
     *   assets/items/{item_id}/fire.gif (projectile release)
     *   etc.
     *
     * Legacy structure (fallback):
     *   assets/items/{item_id}.gif
     */
    private static void loadAllItemTextures() {
        int loaded = 0;
        int loadedWithAnimations = 0;
        int missing = 0;

        for (Map.Entry<String, Item> entry : templates.entrySet()) {
            String id = entry.getKey();
            Item item = entry.getValue();

            // Skip blocks - they use different textures
            if (item.getCategory() == ItemCategory.BLOCK) {
                loadBlockTextures(id, item);
                continue;
            }

            // First, check for animation folder structure
            String folderPath = ITEMS_BASE_PATH + id;
            File folder = new File(folderPath);

            if (folder.exists() && folder.isDirectory()) {
                // New folder structure - load animations
                boolean loadedAny = loadItemAnimationFolder(id, item, folderPath);
                if (loadedAny) {
                    itemsWithAnimationFolders.add(id);
                    loadedWithAnimations++;
                    loaded++;
                    continue;
                }
            }

            // Fallback: try legacy single file structure
            String texturePath = ITEMS_BASE_PATH + id + ".gif";
            File textureFile = new File(texturePath);

            if (textureFile.exists()) {
                item.loadIcon(texturePath);
                item.setAnimationFolderPath(null); // No folder, single file
                loaded++;
            } else {
                // Try PNG as fallback
                String pngPath = ITEMS_BASE_PATH + id + ".png";
                File pngFile = new File(pngPath);
                if (pngFile.exists()) {
                    item.loadIcon(pngPath);
                    item.setTexturePath(texturePath);  // Still record intended GIF path
                    item.setAnimationFolderPath(null);
                    loaded++;
                } else {
                    // Record the expected path even if file is missing
                    item.setTexturePath(texturePath);
                    item.setAnimationFolderPath(folderPath); // Expected folder path
                    missing++;
                }
            }
        }

        System.out.println("ItemRegistry: Loaded " + loaded + " item textures (" +
            loadedWithAnimations + " with animation folders), " + missing + " missing");
    }

    /**
     * Loads animations from an item's animation folder.
     *
     * @param id Item registry ID
     * @param item Item to load animations for
     * @param folderPath Path to the animation folder
     * @return true if any animations were loaded
     */
    private static boolean loadItemAnimationFolder(String id, Item item, String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }

        boolean loadedAny = false;

        // Look for idle.gif first (this becomes the icon)
        String idlePath = folderPath + "/idle.gif";
        File idleFile = new File(idlePath);
        if (idleFile.exists()) {
            item.loadIcon(idlePath);
            loadedAny = true;
        }

        // Set the animation folder path for the Item
        item.setAnimationFolderPath(folderPath);

        // Load all animation states
        for (ItemAnimationState state : ItemAnimationState.values()) {
            String statePath = state.getFilePath(folderPath);
            File stateFile = new File(statePath);
            if (stateFile.exists()) {
                item.loadTriggeredAnimation(state, statePath);
                loadedAny = true;
            }
        }

        // If no idle was found but folder exists, use first available animation as icon
        if (!idleFile.exists() && loadedAny) {
            File[] gifs = folder.listFiles((dir, name) -> name.endsWith(".gif"));
            if (gifs != null && gifs.length > 0) {
                item.loadIcon(gifs[0].getPath());
            }
        }

        return loadedAny;
    }

    /**
     * Loads textures for a block item.
     *
     * @param id Block item registry ID
     * @param item Block item
     */
    private static void loadBlockTextures(String id, Item item) {
        // Remove "_block" suffix for folder lookup if present
        String blockId = id.replace("_block", "");

        // Check for animation folder
        String folderPath = BLOCKS_BASE_PATH + blockId;
        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            item.setAnimationFolderPath(folderPath);

            // Load idle/default texture
            String idlePath = folderPath + "/idle.gif";
            File idleFile = new File(idlePath);
            if (idleFile.exists()) {
                item.loadIcon(idlePath);
            } else {
                // Try any .gif or .png in folder
                File[] files = folder.listFiles((dir, name) ->
                    name.endsWith(".gif") || name.endsWith(".png"));
                if (files != null && files.length > 0) {
                    item.loadIcon(files[0].getPath());
                }
            }
        } else {
            // Fallback: look for single texture file
            String[] possiblePaths = {
                BLOCKS_BASE_PATH + blockId + ".gif",
                BLOCKS_BASE_PATH + blockId + ".png",
                "assets/textures/blocks/" + blockId + ".gif",
                "assets/textures/blocks/" + blockId + ".png"
            };

            for (String path : possiblePaths) {
                File file = new File(path);
                if (file.exists()) {
                    item.loadIcon(path);
                    break;
                }
            }
        }
    }

    /**
     * Checks if an item has an animation folder with multiple states.
     *
     * @param itemId Item registry ID
     * @return true if the item has animation folder
     */
    public static boolean hasAnimationFolder(String itemId) {
        initialize();
        return itemsWithAnimationFolders.contains(itemId);
    }

    /**
     * Gets the animation folder path for an item.
     *
     * @param itemId Item registry ID
     * @return Folder path, or null if item uses legacy single file
     */
    public static String getAnimationFolderPath(String itemId) {
        initialize();
        Item template = templates.get(itemId);
        return template != null ? template.getAnimationFolderPath() : null;
    }

    /**
     * Preloads triggered animations for items that need them.
     * Call this after initialization for better performance.
     */
    public static void preloadTriggeredAnimations() {
        initialize();
        TriggeredAnimationManager manager = TriggeredAnimationManager.getInstance();

        for (String itemId : itemsWithAnimationFolders) {
            manager.loadItemAnimations(itemId);
        }

        System.out.println("ItemRegistry: Preloaded animations for " +
            itemsWithAnimationFolders.size() + " items");
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

    private static Item registerClothing(String id, String name, ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.CLOTHING);
        item.setRarity(rarity);
        item.setDescription(desc);
        item.setStackable(false);
        templates.put(id, item);
        return item;
    }

    private static Item registerCollectible(String id, String name, ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.OTHER);
        item.setRarity(rarity);
        item.setDescription(desc);
        item.setStackable(true);
        item.setMaxStackSize(16);
        templates.put(id, item);
        return item;
    }

    // registerCompanion removed - companions are now player character alternates
    // See CompanionRegistry for companion definitions

    private static Item registerBlock(String id, String name, ItemRarity rarity, String desc) {
        Item item = new Item(name, ItemCategory.BLOCK);
        item.setRarity(rarity);
        item.setDescription(desc);
        item.setStackable(true);
        item.setMaxStackSize(64);
        templates.put(id, item);
        return item;
    }

    /**
     * Registers the Mirror to Other Realms special item.
     * This is a unique ranged weapon that cycles through realms and fires
     * different projectile types (fireball, arrow, fish).
     */
    private static void registerMirrorToOtherRealms() {
        MirrorToOtherRealms mirror = new MirrorToOtherRealms();
        templates.put("mirror_realms", mirror);
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

    /**
     * Finds an item ID by item name.
     * @return The registry ID or null if not found
     */
    public static String findIdByName(String name) {
        if (name == null) return null;
        initialize();
        for (Map.Entry<String, Item> entry : templates.entrySet()) {
            if (entry.getValue().getName().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
