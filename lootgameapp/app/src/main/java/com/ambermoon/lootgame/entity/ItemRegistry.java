package com.ambermoon.lootgame.entity;

import android.util.Log;

import com.ambermoon.lootgame.graphics.AssetLoader;

// Melee Weapons
import com.ambermoon.lootgame.entity.items.weapons.melee.WoodenSword;
import com.ambermoon.lootgame.entity.items.weapons.melee.IronSword;
import com.ambermoon.lootgame.entity.items.weapons.melee.SteelSword;
import com.ambermoon.lootgame.entity.items.weapons.melee.FireSword;
import com.ambermoon.lootgame.entity.items.weapons.melee.IceSword;
import com.ambermoon.lootgame.entity.items.weapons.melee.LegendarySword;
import com.ambermoon.lootgame.entity.items.weapons.melee.Dagger;
import com.ambermoon.lootgame.entity.items.weapons.melee.BattleAxe;
import com.ambermoon.lootgame.entity.items.weapons.melee.Mace;
import com.ambermoon.lootgame.entity.items.weapons.melee.CrystalSword;
import com.ambermoon.lootgame.entity.items.weapons.melee.VampiricBlade;
import com.ambermoon.lootgame.entity.items.weapons.melee.PoisonDagger;
import com.ambermoon.lootgame.entity.items.weapons.melee.NecromancersBlade;
import com.ambermoon.lootgame.entity.items.weapons.melee.ElectrifiedKatana;
import com.ambermoon.lootgame.entity.items.weapons.melee.EtherealDragonslayer;
import com.ambermoon.lootgame.entity.items.weapons.melee.SoulboundDagger;
import com.ambermoon.lootgame.entity.items.weapons.melee.VoidBlade;
import com.ambermoon.lootgame.entity.items.weapons.melee.SoulReaver;
import com.ambermoon.lootgame.entity.items.weapons.melee.TimeWarpBlade;
import com.ambermoon.lootgame.entity.items.weapons.melee.Frostmourne;
import com.ambermoon.lootgame.entity.items.weapons.melee.ThunderHammer;
import com.ambermoon.lootgame.entity.items.weapons.melee.ShadowDagger;

// Ranged Weapons
import com.ambermoon.lootgame.entity.items.weapons.ranged.WoodenBow;
import com.ambermoon.lootgame.entity.items.weapons.ranged.Longbow;
import com.ambermoon.lootgame.entity.items.weapons.ranged.Crossbow;
import com.ambermoon.lootgame.entity.items.weapons.ranged.HeavyCrossbow;
import com.ambermoon.lootgame.entity.items.weapons.ranged.MagicWand;
import com.ambermoon.lootgame.entity.items.weapons.ranged.FireStaff;
import com.ambermoon.lootgame.entity.items.weapons.ranged.IceStaff;
import com.ambermoon.lootgame.entity.items.weapons.ranged.ArcaneStaff;
import com.ambermoon.lootgame.entity.items.weapons.ranged.EpicBow;
import com.ambermoon.lootgame.entity.items.weapons.ranged.SummoningRod;
import com.ambermoon.lootgame.entity.items.weapons.ranged.LightningRod;
import com.ambermoon.lootgame.entity.items.weapons.ranged.BlazingRod;
import com.ambermoon.lootgame.entity.items.weapons.ranged.MagicFishingRod;
import com.ambermoon.lootgame.entity.items.weapons.ranged.CelestialBow;
import com.ambermoon.lootgame.entity.items.weapons.ranged.InfinityStaff;
import com.ambermoon.lootgame.entity.items.weapons.ranged.PhoenixBow;

// Throwing Weapons
import com.ambermoon.lootgame.entity.items.weapons.throwing.ThrowingKnife;
import com.ambermoon.lootgame.entity.items.weapons.throwing.ThrowingAxe;
import com.ambermoon.lootgame.entity.items.weapons.throwing.Rock;

// Armor
import com.ambermoon.lootgame.entity.items.armor.IronHelmet;
import com.ambermoon.lootgame.entity.items.armor.IronChestplate;
import com.ambermoon.lootgame.entity.items.armor.IronLeggings;
import com.ambermoon.lootgame.entity.items.armor.IronBoots;
import com.ambermoon.lootgame.entity.items.armor.SteelHelmet;
import com.ambermoon.lootgame.entity.items.armor.SteelChestplate;
import com.ambermoon.lootgame.entity.items.armor.SteelShield;
import com.ambermoon.lootgame.entity.items.armor.WizardHat;
import com.ambermoon.lootgame.entity.items.armor.SentinelGauntlets;
import com.ambermoon.lootgame.entity.items.armor.GoldArmorHelmet;
import com.ambermoon.lootgame.entity.items.armor.GoldArmorChest;
import com.ambermoon.lootgame.entity.items.armor.GoldArmorLegs;
import com.ambermoon.lootgame.entity.items.armor.GoldArmorBoots;
import com.ambermoon.lootgame.entity.items.armor.ChainmailArmor;
import com.ambermoon.lootgame.entity.items.armor.DragonScaleArmor;
import com.ambermoon.lootgame.entity.items.armor.FancyBoots;
import com.ambermoon.lootgame.entity.items.armor.PhoenixCrown;
import com.ambermoon.lootgame.entity.items.armor.VoidArmor;
import com.ambermoon.lootgame.entity.items.armor.CelestialRobes;
import com.ambermoon.lootgame.entity.items.armor.TitanGauntlets;

// Tools
import com.ambermoon.lootgame.entity.items.tools.WoodenPickaxe;
import com.ambermoon.lootgame.entity.items.tools.IronPickaxe;
import com.ambermoon.lootgame.entity.items.tools.GoldenPickaxe;
import com.ambermoon.lootgame.entity.items.tools.WoodenAxeTool;
import com.ambermoon.lootgame.entity.items.tools.IronAxeTool;
import com.ambermoon.lootgame.entity.items.tools.WoodenShovel;
import com.ambermoon.lootgame.entity.items.tools.IronShovel;
import com.ambermoon.lootgame.entity.items.tools.Shears;
import com.ambermoon.lootgame.entity.items.tools.FishingRod;
import com.ambermoon.lootgame.entity.items.tools.WalkingStick;

// Food
import com.ambermoon.lootgame.entity.items.food.Bread;
import com.ambermoon.lootgame.entity.items.food.Apple;
import com.ambermoon.lootgame.entity.items.food.CookedMeat;
import com.ambermoon.lootgame.entity.items.food.Cheese;
import com.ambermoon.lootgame.entity.items.food.GoldenApple;
import com.ambermoon.lootgame.entity.items.food.Cake;
import com.ambermoon.lootgame.entity.items.food.Cookies;
import com.ambermoon.lootgame.entity.items.food.Melon;
import com.ambermoon.lootgame.entity.items.food.Pumpkin;
import com.ambermoon.lootgame.entity.items.food.Salmon;
import com.ambermoon.lootgame.entity.items.food.ChickenEgg;

// Potions
import com.ambermoon.lootgame.entity.items.potions.HealthPotion;
import com.ambermoon.lootgame.entity.items.potions.ManaPotion;
import com.ambermoon.lootgame.entity.items.potions.StaminaPotion;
import com.ambermoon.lootgame.entity.items.potions.GreaterHealthPotion;
import com.ambermoon.lootgame.entity.items.potions.StrengthPotion;
import com.ambermoon.lootgame.entity.items.potions.SpeedPotion;
import com.ambermoon.lootgame.entity.items.potions.ExplodingPotion;
import com.ambermoon.lootgame.entity.items.potions.LuckyPotion;
import com.ambermoon.lootgame.entity.items.potions.HoneyPotion;
import com.ambermoon.lootgame.entity.items.potions.BrewedPotion;
import com.ambermoon.lootgame.entity.items.potions.ManaLeaf;
import com.ambermoon.lootgame.entity.items.potions.ElixirOfImmortality;
import com.ambermoon.lootgame.entity.items.potions.PotionOfAscension;
import com.ambermoon.lootgame.entity.items.potions.EssenceOfDragon;

// Materials
import com.ambermoon.lootgame.entity.items.materials.StringItem;
import com.ambermoon.lootgame.entity.items.materials.IronIngot;
import com.ambermoon.lootgame.entity.items.materials.GoldIngot;
import com.ambermoon.lootgame.entity.items.materials.Diamond;
import com.ambermoon.lootgame.entity.items.materials.MagicCrystalMaterial;
import com.ambermoon.lootgame.entity.items.materials.Scroll;
import com.ambermoon.lootgame.entity.items.materials.IronOre;
import com.ambermoon.lootgame.entity.items.materials.GoldCoins;
import com.ambermoon.lootgame.entity.items.materials.CopperIngots;
import com.ambermoon.lootgame.entity.items.materials.GoldBars;
import com.ambermoon.lootgame.entity.items.materials.IronBars;
import com.ambermoon.lootgame.entity.items.materials.Leather;
import com.ambermoon.lootgame.entity.items.materials.Yarn;
import com.ambermoon.lootgame.entity.items.materials.Flour;
import com.ambermoon.lootgame.entity.items.materials.MysteriousGemstone;
import com.ambermoon.lootgame.entity.items.materials.DragonEgg;
import com.ambermoon.lootgame.entity.items.materials.Bonemeal;
import com.ambermoon.lootgame.entity.items.materials.Ink;
import com.ambermoon.lootgame.entity.items.materials.Planks;
import com.ambermoon.lootgame.entity.items.materials.Rocks;
import com.ambermoon.lootgame.entity.items.materials.Rope;
import com.ambermoon.lootgame.entity.items.materials.Sapling;
import com.ambermoon.lootgame.entity.items.materials.MachineParts;
import com.ambermoon.lootgame.entity.items.materials.Skull;

// Collectibles
import com.ambermoon.lootgame.entity.items.collectibles.Orb;
import com.ambermoon.lootgame.entity.items.collectibles.AncientPottery;
import com.ambermoon.lootgame.entity.items.collectibles.MusicDisc;
import com.ambermoon.lootgame.entity.items.collectibles.MusicPlayer;
import com.ambermoon.lootgame.entity.items.collectibles.Journal;
import com.ambermoon.lootgame.entity.items.collectibles.PaintingWolves;
import com.ambermoon.lootgame.entity.items.collectibles.PaintingDog;
import com.ambermoon.lootgame.entity.items.collectibles.Marbles;
import com.ambermoon.lootgame.entity.items.collectibles.Nametag;
import com.ambermoon.lootgame.entity.items.collectibles.Candle;
import com.ambermoon.lootgame.entity.items.collectibles.MysteriousCandle;
import com.ambermoon.lootgame.entity.items.collectibles.WaterBottle;
import com.ambermoon.lootgame.entity.items.collectibles.Saddle;
import com.ambermoon.lootgame.entity.items.collectibles.Backpack;
import com.ambermoon.lootgame.entity.items.collectibles.PersonalizedBanner;
import com.ambermoon.lootgame.entity.items.collectibles.Frog;
import com.ambermoon.lootgame.entity.items.collectibles.JackOLantern;
import com.ambermoon.lootgame.entity.items.collectibles.Rocket;
import com.ambermoon.lootgame.entity.items.collectibles.WindCharge;
import com.ambermoon.lootgame.entity.items.collectibles.UndeadScroll;
import com.ambermoon.lootgame.entity.items.collectibles.TripWireTrap;
import com.ambermoon.lootgame.entity.items.collectibles.MagicLantern;
import com.ambermoon.lootgame.entity.items.collectibles.Crucible;
import com.ambermoon.lootgame.entity.items.collectibles.Rowboat;
import com.ambermoon.lootgame.entity.items.collectibles.AncientCrown;
import com.ambermoon.lootgame.entity.items.collectibles.DemonHorn;
import com.ambermoon.lootgame.entity.items.collectibles.AngelFeather;
import com.ambermoon.lootgame.entity.items.collectibles.VoidShard;
import com.ambermoon.lootgame.entity.items.collectibles.LuckyCoin;
import com.ambermoon.lootgame.entity.items.collectibles.TreasureMap;
import com.ambermoon.lootgame.entity.items.collectibles.CrystalSummer;
import com.ambermoon.lootgame.entity.items.collectibles.CrystalWinter;
import com.ambermoon.lootgame.entity.items.collectibles.HeartOfEternity;
import com.ambermoon.lootgame.entity.items.collectibles.EyeOfCosmos;
import com.ambermoon.lootgame.entity.items.collectibles.PhilosophersStone;
import com.ambermoon.lootgame.entity.items.collectibles.AnkhOfRebirth;

// Accessories
import com.ambermoon.lootgame.entity.items.accessories.RubySkull;

// Clothing
import com.ambermoon.lootgame.entity.items.clothing.GreenDress;
import com.ambermoon.lootgame.entity.items.clothing.OrangeDress;
import com.ambermoon.lootgame.entity.items.clothing.BlackDress;
import com.ambermoon.lootgame.entity.items.clothing.Hat;
import com.ambermoon.lootgame.entity.items.clothing.Pants;
import com.ambermoon.lootgame.entity.items.clothing.Shirt;
import com.ambermoon.lootgame.entity.items.clothing.ThreePieceSuit;
import com.ambermoon.lootgame.entity.items.clothing.Swimwear;
import com.ambermoon.lootgame.entity.items.clothing.YellowCloak;
import com.ambermoon.lootgame.entity.items.clothing.ChameleonCloak;
import com.ambermoon.lootgame.entity.items.clothing.WitchsHat;
import com.ambermoon.lootgame.entity.items.clothing.GownForgottenVerses;

// Keys
import com.ambermoon.lootgame.entity.items.keys.BronzeKey;
import com.ambermoon.lootgame.entity.items.keys.SilverKey;
import com.ambermoon.lootgame.entity.items.keys.GoldenKey;
import com.ambermoon.lootgame.entity.items.keys.SkeletonKey;

// Blocks
import com.ambermoon.lootgame.entity.items.blocks.DirtBlock;
import com.ambermoon.lootgame.entity.items.blocks.GrassBlock;
import com.ambermoon.lootgame.entity.items.blocks.StoneBlock;
import com.ambermoon.lootgame.entity.items.blocks.CobblestoneBlock;
import com.ambermoon.lootgame.entity.items.blocks.SandBlock;
import com.ambermoon.lootgame.entity.items.blocks.WoodBlock;
import com.ambermoon.lootgame.entity.items.blocks.LeavesBlock;
import com.ambermoon.lootgame.entity.items.blocks.BrickBlock;
import com.ambermoon.lootgame.entity.items.blocks.GlassBlock;
import com.ambermoon.lootgame.entity.items.blocks.WaterBlock;
import com.ambermoon.lootgame.entity.items.blocks.CoalOreBlock;
import com.ambermoon.lootgame.entity.items.blocks.IronOreBlock;
import com.ambermoon.lootgame.entity.items.blocks.GoldOreBlock;
import com.ambermoon.lootgame.entity.items.blocks.SnowBlock;
import com.ambermoon.lootgame.entity.items.blocks.IceBlock;
import com.ambermoon.lootgame.entity.items.blocks.MossBlock;
import com.ambermoon.lootgame.entity.items.blocks.VinesBlock;

// Ammo
import com.ambermoon.lootgame.entity.items.ammo.Arrow;
import com.ambermoon.lootgame.entity.items.ammo.FireArrow;
import com.ambermoon.lootgame.entity.items.ammo.IceArrow;
import com.ambermoon.lootgame.entity.items.ammo.Bolt;
import com.ambermoon.lootgame.entity.items.ammo.HeavyBolt;
import com.ambermoon.lootgame.entity.items.ammo.ManaCrystal;

// Throwables
import com.ambermoon.lootgame.entity.items.throwables.Bomb;
import com.ambermoon.lootgame.entity.items.throwables.ThrowingPotion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ItemRegistry provides access to predefined item templates.
 * Use this to create new instances of common items.
 *
 * Adapted from the Android port's ItemRegistry.java for the standalone Loot Game App.
 * - Package: com.ambermoon.lootgame.entity
 * - Uses AssetLoader instead of AndroidAssetLoader
 * - All item registrations are active (uncommented)
 *
 * Animation Folder Structure (Android assets):
 *   items/{item_id}/idle.gif
 *   items/{item_id}/draw.gif  (for bows)
 *   items/{item_id}/fire.gif
 *   items/{item_id}/attack.gif
 *
 * Legacy Support:
 *   items/{item_id}.gif (single animation fallback)
 *
 * Usage:
 *   Item sword = ItemRegistry.create("iron_sword");
 *   Item crossbow = ItemRegistry.create("crossbow");
 */
public class ItemRegistry {

    private static final String TAG = "ItemRegistry";

    private static final Map<String, Item> templates = new HashMap<>();
    private static final Set<String> itemsWithAnimationFolders = new HashSet<>();
    private static boolean initialized = false;

    // Base paths for assets (relative to Android assets folder)
    private static final String ITEMS_BASE_PATH = "items/";
    private static final String BLOCKS_BASE_PATH = "textures/blocks/";

    /**
     * Helper method to register an item template with its registry ID.
     */
    private static void register(String id, Item item) {
        item.setRegistryId(id);
        templates.put(id, item);
    }

    /**
     * Initializes all item templates.
     * All item classes are registered here.
     */
    public static void initialize() {
        if (initialized) return;

        // ==================== MELEE WEAPONS ====================
        register("wooden_sword", new WoodenSword());
        register("iron_sword", new IronSword());
        register("steel_sword", new SteelSword());
        register("fire_sword", new FireSword());
        register("ice_sword", new IceSword());
        register("legendary_sword", new LegendarySword());
        register("dagger", new Dagger());
        register("battle_axe", new BattleAxe());
        register("mace", new Mace());
        register("crystal_sword", new CrystalSword());
        register("vampiric_blade", new VampiricBlade());
        register("poison_dagger", new PoisonDagger());
        register("necromancers_blade", new NecromancersBlade());
        register("electrified_katana", new ElectrifiedKatana());
        register("ethereal_dragonslayer", new EtherealDragonslayer());
        register("soulbound_dagger", new SoulboundDagger());
        register("void_blade", new VoidBlade());
        register("soul_reaver", new SoulReaver());
        register("time_warp_blade", new TimeWarpBlade());
        register("frostmourne", new Frostmourne());
        register("thunder_hammer", new ThunderHammer());
        register("shadow_dagger", new ShadowDagger());

        // ==================== RANGED WEAPONS ====================
        register("wooden_bow", new WoodenBow());
        register("longbow", new Longbow());
        register("crossbow", new Crossbow());
        register("heavy_crossbow", new HeavyCrossbow());
        register("magic_wand", new MagicWand());
        register("fire_staff", new FireStaff());
        register("ice_staff", new IceStaff());
        register("arcane_staff", new ArcaneStaff());
        register("epic_bow", new EpicBow());
        register("summoning_rod", new SummoningRod());
        register("lightning_rod", new LightningRod());
        register("blazing_rod", new BlazingRod());
        register("magic_fishing_rod", new MagicFishingRod());
        register("celestial_bow", new CelestialBow());
        register("infinity_staff", new InfinityStaff());
        register("phoenix_bow", new PhoenixBow());

        // ==================== THROWING WEAPONS ====================
        register("throwing_knife", new ThrowingKnife());
        register("throwing_axe", new ThrowingAxe());
        register("rock", new Rock());

        // ==================== ARMOR ====================
        register("iron_helmet", new IronHelmet());
        register("iron_chestplate", new IronChestplate());
        register("iron_leggings", new IronLeggings());
        register("iron_boots", new IronBoots());
        register("steel_helmet", new SteelHelmet());
        register("steel_chestplate", new SteelChestplate());
        register("steel_shield", new SteelShield());
        register("wizard_hat", new WizardHat());
        register("sentinel_gauntlets", new SentinelGauntlets());
        register("gold_armor_helmet", new GoldArmorHelmet());
        register("gold_armor_chest", new GoldArmorChest());
        register("gold_armor_legs", new GoldArmorLegs());
        register("gold_armor_boots", new GoldArmorBoots());
        register("chainmail_armor", new ChainmailArmor());
        register("dragon_scale_armor", new DragonScaleArmor());
        register("fancy_boots", new FancyBoots());
        register("phoenix_crown", new PhoenixCrown());
        register("void_armor", new VoidArmor());
        register("celestial_robes", new CelestialRobes());
        register("titan_gauntlets", new TitanGauntlets());

        // ==================== TOOLS ====================
        register("wooden_pickaxe", new WoodenPickaxe());
        register("iron_pickaxe", new IronPickaxe());
        register("golden_pickaxe", new GoldenPickaxe());
        register("wooden_axe", new WoodenAxeTool());
        register("iron_axe", new IronAxeTool());
        register("wooden_shovel", new WoodenShovel());
        register("iron_shovel", new IronShovel());
        register("shears", new Shears());
        register("fishing_rod", new FishingRod());
        register("walking_stick", new WalkingStick());

        // ==================== FOOD ====================
        register("bread", new Bread());
        register("apple", new Apple());
        register("cooked_meat", new CookedMeat());
        register("cheese", new Cheese());
        register("golden_apple", new GoldenApple());
        register("cake", new Cake());
        register("cookies", new Cookies());
        register("melon", new Melon());
        register("pumpkin", new Pumpkin());
        register("salmon", new Salmon());
        register("chicken_egg", new ChickenEgg());

        // ==================== POTIONS ====================
        register("health_potion", new HealthPotion());
        register("mana_potion", new ManaPotion());
        register("stamina_potion", new StaminaPotion());
        register("greater_health_potion", new GreaterHealthPotion());
        register("strength_potion", new StrengthPotion());
        register("speed_potion", new SpeedPotion());
        register("exploding_potion", new ExplodingPotion());
        register("lucky_potion", new LuckyPotion());
        register("honey_potion", new HoneyPotion());
        register("brewed_potion", new BrewedPotion());
        register("mana_leaf", new ManaLeaf());
        register("elixir_of_immortality", new ElixirOfImmortality());
        register("potion_of_ascension", new PotionOfAscension());
        register("essence_of_dragon", new EssenceOfDragon());

        // ==================== MATERIALS ====================
        register("string", new StringItem());
        register("iron_ingot", new IronIngot());
        register("gold_ingot", new GoldIngot());
        register("diamond", new Diamond());
        register("magic_crystal", new MagicCrystalMaterial());
        register("scroll", new Scroll());
        register("iron_ore", new IronOre());
        register("gold_coins", new GoldCoins());
        register("copper_ingots", new CopperIngots());
        register("gold_bars", new GoldBars());
        register("iron_bars", new IronBars());
        register("leather", new Leather());
        register("yarn", new Yarn());
        register("flour", new Flour());
        register("mysterious_gemstone", new MysteriousGemstone());
        register("dragon_egg", new DragonEgg());
        register("bonemeal", new Bonemeal());
        register("ink", new Ink());
        register("planks", new Planks());
        register("rocks", new Rocks());
        register("rope", new Rope());
        register("sapling", new Sapling());
        register("machine_parts", new MachineParts());
        register("skull", new Skull());

        // ==================== COLLECTIBLES & MISC ====================
        register("orb", new Orb());
        register("ancient_pottery", new AncientPottery());
        register("music_disc", new MusicDisc());
        register("music_player", new MusicPlayer());
        register("journal", new Journal());
        register("painting_wolves", new PaintingWolves());
        register("painting_dog", new PaintingDog());
        register("marbles", new Marbles());
        register("nametag", new Nametag());
        register("candle", new Candle());
        register("mysterious_candle", new MysteriousCandle());
        register("water_bottle", new WaterBottle());
        register("saddle", new Saddle());
        register("backpack", new Backpack());
        register("personalized_banner", new PersonalizedBanner());
        register("frog", new Frog());
        register("jack_o_lantern", new JackOLantern());
        register("rocket", new Rocket());
        register("wind_charge", new WindCharge());
        register("undead_scroll", new UndeadScroll());
        register("trip_wire_trap", new TripWireTrap());
        register("magic_lantern", new MagicLantern());
        register("crucible", new Crucible());
        register("rowboat", new Rowboat());
        register("ancient_crown", new AncientCrown());
        register("demon_horn", new DemonHorn());
        register("angel_feather", new AngelFeather());
        register("void_shard", new VoidShard());
        register("lucky_coin", new LuckyCoin());
        register("treasure_map", new TreasureMap());
        register("crystal_summer", new CrystalSummer());
        register("crystal_winter", new CrystalWinter());
        register("heart_of_eternity", new HeartOfEternity());
        register("eye_of_cosmos", new EyeOfCosmos());
        register("philosophers_stone", new PhilosophersStone());
        register("ankh_of_rebirth", new AnkhOfRebirth());

        // ==================== ACCESSORIES ====================
        register("ruby_skull", new RubySkull());

        // ==================== CLOTHING ====================
        register("green_dress", new GreenDress());
        register("orange_dress", new OrangeDress());
        register("black_dress", new BlackDress());
        register("hat", new Hat());
        register("pants", new Pants());
        register("shirt", new Shirt());
        register("three_piece_suit", new ThreePieceSuit());
        register("swimwear", new Swimwear());
        register("yellow_cloak", new YellowCloak());
        register("chameleon_cloak", new ChameleonCloak());
        register("witchs_hat", new WitchsHat());
        register("gown_forgotten_verses", new GownForgottenVerses());

        // ==================== KEYS ====================
        register("bronze_key", new BronzeKey());
        register("silver_key", new SilverKey());
        register("golden_key", new GoldenKey());
        register("skeleton_key", new SkeletonKey());

        // ==================== BLOCKS (Placeable) ====================
        register("dirt_block", new DirtBlock());
        register("grass_block", new GrassBlock());
        register("stone_block", new StoneBlock());
        register("cobblestone_block", new CobblestoneBlock());
        register("sand_block", new SandBlock());
        register("wood_block", new WoodBlock());
        register("leaves_block", new LeavesBlock());
        register("brick_block", new BrickBlock());
        register("glass_block", new GlassBlock());
        register("water_block", new WaterBlock());
        register("coal_ore_block", new CoalOreBlock());
        register("iron_ore_block", new IronOreBlock());
        register("gold_ore_block", new GoldOreBlock());
        register("snow_block", new SnowBlock());
        register("ice_block", new IceBlock());
        register("moss_block", new MossBlock());
        register("vines_block", new VinesBlock());

        // ==================== AMMO ====================
        register("arrow", new Arrow());
        register("fire_arrow", new FireArrow());
        register("ice_arrow", new IceArrow());
        register("bolt", new Bolt());
        register("heavy_bolt", new HeavyBolt());
        register("mana_crystal", new ManaCrystal());

        // ==================== THROWABLES ====================
        register("bomb", new Bomb());
        register("throwing_potion", new ThrowingPotion());

        // Load all item textures
        loadAllItemTextures();

        initialized = true;
        Log.d(TAG, "ItemRegistry initialized with " + templates.size() + " items");
    }

    /**
     * Manually registers an item template.
     *
     * @param id Unique item ID (e.g., "iron_sword")
     * @param item Item template instance
     */
    public static void registerItem(String id, Item item) {
        item.setRegistryId(id);
        templates.put(id, item);
    }

    /**
     * Loads textures for all registered items using AssetLoader.
     */
    private static void loadAllItemTextures() {
        int loaded = 0;
        int loadedWithAnimations = 0;
        int missing = 0;

        for (Map.Entry<String, Item> entry : templates.entrySet()) {
            String id = entry.getKey();
            Item item = entry.getValue();

            // Skip blocks - they use different textures
            if (item.getCategory() == Item.ItemCategory.BLOCK) {
                loadBlockTextures(id, item);
                continue;
            }

            // Check for animation folder structure
            String folderPath = ITEMS_BASE_PATH + id;
            String[] folderContents = AssetLoader.list(folderPath);

            if (folderContents != null && folderContents.length > 0) {
                // Folder structure exists - load animations
                boolean loadedAny = loadItemAnimationFolder(id, item, folderPath, folderContents);
                if (loadedAny) {
                    itemsWithAnimationFolders.add(id);
                    loadedWithAnimations++;
                    loaded++;
                    continue;
                }
            }

            // Fallback: try legacy single file structure
            String texturePath = ITEMS_BASE_PATH + id + ".gif";
            if (AssetLoader.exists(texturePath)) {
                item.loadIcon(texturePath);
                item.setAnimationFolderPath(null);
                loaded++;
            } else {
                // Try PNG as fallback
                String pngPath = ITEMS_BASE_PATH + id + ".png";
                if (AssetLoader.exists(pngPath)) {
                    item.loadIcon(pngPath);
                    item.setTexturePath(texturePath);
                    item.setAnimationFolderPath(null);
                    loaded++;
                } else {
                    item.setTexturePath(texturePath);
                    item.setAnimationFolderPath(folderPath);
                    missing++;
                }
            }
        }

        Log.d(TAG, "Loaded " + loaded + " item textures (" +
                loadedWithAnimations + " with animation folders), " + missing + " missing");
    }

    /**
     * Loads animations from an item's animation folder.
     */
    private static boolean loadItemAnimationFolder(String id, Item item,
                                                     String folderPath, String[] contents) {
        boolean loadedAny = false;

        // Look for idle.gif first (this becomes the icon)
        String idlePath = folderPath + "/idle.gif";
        if (AssetLoader.exists(idlePath)) {
            item.loadIcon(idlePath);
            loadedAny = true;
        }

        // Set the animation folder path
        item.setAnimationFolderPath(folderPath);

        // If no idle was found but folder has GIFs, use first one
        if (!AssetLoader.exists(idlePath) && contents != null) {
            for (String file : contents) {
                if (file.endsWith(".gif")) {
                    item.loadIcon(folderPath + "/" + file);
                    loadedAny = true;
                    break;
                }
            }
        }

        return loadedAny;
    }

    /**
     * Loads textures for a block item.
     */
    private static void loadBlockTextures(String id, Item item) {
        String blockId = id.replace("_block", "");

        // Check for animation folder
        String folderPath = BLOCKS_BASE_PATH + blockId;
        String[] folderContents = AssetLoader.list(folderPath);

        if (folderContents != null && folderContents.length > 0) {
            item.setAnimationFolderPath(folderPath);

            String idlePath = folderPath + "/idle.gif";
            if (AssetLoader.exists(idlePath)) {
                item.loadIcon(idlePath);
            } else {
                // Try any .gif or .png in folder
                for (String file : folderContents) {
                    if (file.endsWith(".gif") || file.endsWith(".png")) {
                        item.loadIcon(folderPath + "/" + file);
                        break;
                    }
                }
            }
        } else {
            // Fallback: look for single texture file
            String[] possiblePaths = {
                    BLOCKS_BASE_PATH + blockId + ".gif",
                    BLOCKS_BASE_PATH + blockId + ".png",
                    "textures/blocks/" + blockId + ".gif",
                    "textures/blocks/" + blockId + ".png"
            };

            for (String path : possiblePaths) {
                if (AssetLoader.exists(path)) {
                    item.loadIcon(path);
                    break;
                }
            }
        }
    }

    /**
     * Checks if an item has an animation folder with multiple states.
     */
    public static boolean hasAnimationFolder(String itemId) {
        initialize();
        return itemsWithAnimationFolders.contains(itemId);
    }

    /**
     * Gets the animation folder path for an item.
     */
    public static String getAnimationFolderPath(String itemId) {
        initialize();
        Item template = templates.get(itemId);
        return template != null ? template.getAnimationFolderPath() : null;
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
            Item copy = template.copy();
            copy.setRegistryId(id);
            return copy;
        }
        Log.w(TAG, "Unknown item ID: " + id);
        return null;
    }

    /**
     * Gets all registered item IDs.
     */
    public static Set<String> getAllItemIds() {
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
     *
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

    /**
     * Clears all templates and resets initialization state.
     * Useful for testing or reloading.
     */
    public static void reset() {
        templates.clear();
        itemsWithAnimationFolders.clear();
        initialized = false;
    }
}
