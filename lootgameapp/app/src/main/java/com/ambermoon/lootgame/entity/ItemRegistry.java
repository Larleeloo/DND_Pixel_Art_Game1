package com.ambermoon.lootgame.entity;

import android.util.Log;

import com.ambermoon.lootgame.graphics.AssetLoader;

// Melee Weapons
import com.ambermoon.lootgame.entity.items.weapons.melee.WoodenSword;
import com.ambermoon.lootgame.entity.items.weapons.melee.Club;
import com.ambermoon.lootgame.entity.items.weapons.melee.IronSword;
import com.ambermoon.lootgame.entity.items.weapons.melee.BattleAxe;
import com.ambermoon.lootgame.entity.items.weapons.melee.Mace;
import com.ambermoon.lootgame.entity.items.weapons.melee.Dagger;
import com.ambermoon.lootgame.entity.items.weapons.melee.GoldSword;
import com.ambermoon.lootgame.entity.items.weapons.melee.GoldenHeavyBattleaxe;
import com.ambermoon.lootgame.entity.items.weapons.melee.GoldenMace;
import com.ambermoon.lootgame.entity.items.weapons.melee.Katana;
import com.ambermoon.lootgame.entity.items.weapons.melee.SpectralSword;
import com.ambermoon.lootgame.entity.items.weapons.melee.SpectralAxe;
import com.ambermoon.lootgame.entity.items.weapons.melee.SpectralMace;
import com.ambermoon.lootgame.entity.items.weapons.melee.SoulboundDagger;
import com.ambermoon.lootgame.entity.items.weapons.melee.VampiricBlade;
import com.ambermoon.lootgame.entity.items.weapons.melee.ThunderHammer;
import com.ambermoon.lootgame.entity.items.weapons.melee.VoidBlade;
import com.ambermoon.lootgame.entity.items.weapons.melee.NecromancersBlade;
import com.ambermoon.lootgame.entity.items.weapons.melee.EtherealDragonslayer;
import com.ambermoon.lootgame.entity.items.weapons.melee.ElectrifiedKatana;
import com.ambermoon.lootgame.entity.items.weapons.melee.TimeWarpBlade;

// Ranged Weapons
import com.ambermoon.lootgame.entity.items.weapons.ranged.WoodenBow;
import com.ambermoon.lootgame.entity.items.weapons.ranged.Staff;
import com.ambermoon.lootgame.entity.items.weapons.ranged.MetalBow;
import com.ambermoon.lootgame.entity.items.weapons.ranged.Crossbow;
import com.ambermoon.lootgame.entity.items.weapons.ranged.HeavyCrossbow;
import com.ambermoon.lootgame.entity.items.weapons.ranged.GoldenBow;
import com.ambermoon.lootgame.entity.items.weapons.ranged.IceStaff;
import com.ambermoon.lootgame.entity.items.weapons.ranged.LightningRod;
import com.ambermoon.lootgame.entity.items.weapons.ranged.MagicWand;
import com.ambermoon.lootgame.entity.items.weapons.ranged.MagicFishingRod;
import com.ambermoon.lootgame.entity.items.weapons.ranged.SpectralBow;
import com.ambermoon.lootgame.entity.items.weapons.ranged.Musket;
import com.ambermoon.lootgame.entity.items.weapons.ranged.SummoningRod;
import com.ambermoon.lootgame.entity.items.weapons.ranged.ArcaneStaff;
import com.ambermoon.lootgame.entity.items.weapons.ranged.Cannon;
import com.ambermoon.lootgame.entity.items.weapons.ranged.InfinityStaff;
import com.ambermoon.lootgame.entity.items.weapons.ranged.PhoenixBow;
import com.ambermoon.lootgame.entity.items.weapons.ranged.StaffOf1000Souls;

// Throwing Weapons
import com.ambermoon.lootgame.entity.items.weapons.throwing.ThrowingKnife;
import com.ambermoon.lootgame.entity.items.weapons.throwing.ThrowingAxe;
import com.ambermoon.lootgame.entity.items.weapons.throwing.Rock;

// Armor
import com.ambermoon.lootgame.entity.items.armor.LeatherTunic;
import com.ambermoon.lootgame.entity.items.armor.IronChestplate;
import com.ambermoon.lootgame.entity.items.armor.IronBoots;
import com.ambermoon.lootgame.entity.items.armor.SentinelGauntlets;
import com.ambermoon.lootgame.entity.items.armor.IronLeggings;
import com.ambermoon.lootgame.entity.items.armor.ChainmailShirt;
import com.ambermoon.lootgame.entity.items.armor.ChainmailPants;
import com.ambermoon.lootgame.entity.items.armor.WoodenShield;
import com.ambermoon.lootgame.entity.items.armor.SteelShield;
import com.ambermoon.lootgame.entity.items.armor.WizardHat;
import com.ambermoon.lootgame.entity.items.armor.FancyBoots;
import com.ambermoon.lootgame.entity.items.armor.GoldenShield;
import com.ambermoon.lootgame.entity.items.armor.TitanGauntlets;
import com.ambermoon.lootgame.entity.items.armor.CelestialRobes;
import com.ambermoon.lootgame.entity.items.armor.ArchmageRobes;
import com.ambermoon.lootgame.entity.items.armor.ObsidianHelmet;
import com.ambermoon.lootgame.entity.items.armor.ObsidianChestplate;
import com.ambermoon.lootgame.entity.items.armor.ObsidianLeggings;
import com.ambermoon.lootgame.entity.items.armor.ObsidianGauntlets;
import com.ambermoon.lootgame.entity.items.armor.ObsidianBoots;
import com.ambermoon.lootgame.entity.items.armor.VoidShield;
import com.ambermoon.lootgame.entity.items.armor.VoidHelmet;
import com.ambermoon.lootgame.entity.items.armor.VoidChestplate;
import com.ambermoon.lootgame.entity.items.armor.VoidLeggings;
import com.ambermoon.lootgame.entity.items.armor.VoidGauntlets;
import com.ambermoon.lootgame.entity.items.armor.VoidBoots;

// Tools
import com.ambermoon.lootgame.entity.items.tools.WoodenPickaxe;
import com.ambermoon.lootgame.entity.items.tools.WoodenShovel;
import com.ambermoon.lootgame.entity.items.tools.WoodenAxeTool;
import com.ambermoon.lootgame.entity.items.tools.WalkingStick;
import com.ambermoon.lootgame.entity.items.tools.FishingRod;
import com.ambermoon.lootgame.entity.items.tools.IronPickaxe;
import com.ambermoon.lootgame.entity.items.tools.IronShovel;
import com.ambermoon.lootgame.entity.items.tools.DiamondEngravedPickaxe;
import com.ambermoon.lootgame.entity.items.tools.MagicShovel;

// Food
import com.ambermoon.lootgame.entity.items.food.Apple;
import com.ambermoon.lootgame.entity.items.food.Bread;
import com.ambermoon.lootgame.entity.items.food.Berries;
import com.ambermoon.lootgame.entity.items.food.Cheese;
import com.ambermoon.lootgame.entity.items.food.ChickenEgg;
import com.ambermoon.lootgame.entity.items.food.Pumpkin;
import com.ambermoon.lootgame.entity.items.food.Melon;
import com.ambermoon.lootgame.entity.items.food.Chicken;
import com.ambermoon.lootgame.entity.items.food.Fish;
import com.ambermoon.lootgame.entity.items.food.Salmon;
import com.ambermoon.lootgame.entity.items.food.MagicApple;
import com.ambermoon.lootgame.entity.items.food.BerriesOfTheOldGods;

// Potions
import com.ambermoon.lootgame.entity.items.potions.BlankPotion;
import com.ambermoon.lootgame.entity.items.potions.HealthPotion;
import com.ambermoon.lootgame.entity.items.potions.ManaPotion;
import com.ambermoon.lootgame.entity.items.potions.StaminaPotion;
import com.ambermoon.lootgame.entity.items.potions.FullHealthPotion;
import com.ambermoon.lootgame.entity.items.potions.FullManaPotion;
import com.ambermoon.lootgame.entity.items.potions.FullStaminaPotion;
import com.ambermoon.lootgame.entity.items.potions.XPPotion;
import com.ambermoon.lootgame.entity.items.potions.PurplePotion;
import com.ambermoon.lootgame.entity.items.potions.LuckyPotion;
import com.ambermoon.lootgame.entity.items.potions.ElixirOfImmortality;
import com.ambermoon.lootgame.entity.items.potions.VoidPotion;

// Materials
import com.ambermoon.lootgame.entity.items.materials.Yarn;
import com.ambermoon.lootgame.entity.items.materials.Coal;
import com.ambermoon.lootgame.entity.items.materials.Cobblestone;
import com.ambermoon.lootgame.entity.items.materials.Planks;
import com.ambermoon.lootgame.entity.items.materials.Ice;
import com.ambermoon.lootgame.entity.items.materials.Feather;
import com.ambermoon.lootgame.entity.items.materials.WhiteDye;
import com.ambermoon.lootgame.entity.items.materials.BlackDye;
import com.ambermoon.lootgame.entity.items.materials.RedDye;
import com.ambermoon.lootgame.entity.items.materials.GreenDye;
import com.ambermoon.lootgame.entity.items.materials.PurpleDye;
import com.ambermoon.lootgame.entity.items.materials.OrangeDye;
import com.ambermoon.lootgame.entity.items.materials.BlueDye;
import com.ambermoon.lootgame.entity.items.materials.IronOre;
import com.ambermoon.lootgame.entity.items.materials.Gunpowder;
import com.ambermoon.lootgame.entity.items.materials.Ink;
import com.ambermoon.lootgame.entity.items.materials.Cactus;
import com.ambermoon.lootgame.entity.items.materials.Bonemeal;
import com.ambermoon.lootgame.entity.items.materials.Rhododendron;
import com.ambermoon.lootgame.entity.items.materials.Rose;
import com.ambermoon.lootgame.entity.items.materials.Violet;
import com.ambermoon.lootgame.entity.items.materials.LavaStone;
import com.ambermoon.lootgame.entity.items.materials.Skull;
import com.ambermoon.lootgame.entity.items.materials.Poison;
import com.ambermoon.lootgame.entity.items.materials.Leather;
import com.ambermoon.lootgame.entity.items.materials.BlankScroll;
import com.ambermoon.lootgame.entity.items.materials.CherrySapling;
import com.ambermoon.lootgame.entity.items.materials.WillowSapling;
import com.ambermoon.lootgame.entity.items.materials.PineSapling;
import com.ambermoon.lootgame.entity.items.materials.OakSapling;
import com.ambermoon.lootgame.entity.items.materials.PalmSapling;
import com.ambermoon.lootgame.entity.items.materials.ScrollOfFireball;
import com.ambermoon.lootgame.entity.items.materials.ScrollOfIceCrystal;
import com.ambermoon.lootgame.entity.items.materials.ScrollOfPoison;
import com.ambermoon.lootgame.entity.items.materials.ScrollOfFireRune;
import com.ambermoon.lootgame.entity.items.materials.ScrollOfIceRune;
import com.ambermoon.lootgame.entity.items.materials.ScrollOfPoisonRune;
import com.ambermoon.lootgame.entity.items.materials.Ruby;
import com.ambermoon.lootgame.entity.items.materials.Emerald;
import com.ambermoon.lootgame.entity.items.materials.Sapphire;
import com.ambermoon.lootgame.entity.items.materials.Diamond;
import com.ambermoon.lootgame.entity.items.materials.ObsidianOre;
import com.ambermoon.lootgame.entity.items.materials.MysteriousGemstone;
import com.ambermoon.lootgame.entity.items.materials.VoidScroll;
import com.ambermoon.lootgame.entity.items.materials.VoidRuneScroll;
import com.ambermoon.lootgame.entity.items.materials.VoidStone;
import com.ambermoon.lootgame.entity.items.materials.GoldBars;
import com.ambermoon.lootgame.entity.items.materials.RedDragonEgg;
import com.ambermoon.lootgame.entity.items.materials.GreenDragonEgg;
import com.ambermoon.lootgame.entity.items.materials.BlueDragonEgg;
import com.ambermoon.lootgame.entity.items.materials.BlackDragonEgg;
import com.ambermoon.lootgame.entity.items.materials.WhiteDragonEgg;

// Collectibles & Misc
import com.ambermoon.lootgame.entity.items.collectibles.Orb;
import com.ambermoon.lootgame.entity.items.collectibles.AncientPottery;
import com.ambermoon.lootgame.entity.items.collectibles.MusicDisc;
import com.ambermoon.lootgame.entity.items.collectibles.Journal;
import com.ambermoon.lootgame.entity.items.collectibles.Marbles;
import com.ambermoon.lootgame.entity.items.collectibles.Nametag;
import com.ambermoon.lootgame.entity.items.collectibles.Candle;
import com.ambermoon.lootgame.entity.items.collectibles.MysteriousCandle;
import com.ambermoon.lootgame.entity.items.collectibles.WaterBottle;
import com.ambermoon.lootgame.entity.items.collectibles.Saddle;
import com.ambermoon.lootgame.entity.items.collectibles.Backpack;
import com.ambermoon.lootgame.entity.items.collectibles.Frog;
import com.ambermoon.lootgame.entity.items.collectibles.FrogEgg;
import com.ambermoon.lootgame.entity.items.collectibles.JackOLantern;
import com.ambermoon.lootgame.entity.items.collectibles.Bunny;
import com.ambermoon.lootgame.entity.items.collectibles.UndeadScroll;
import com.ambermoon.lootgame.entity.items.collectibles.TripWireTrap;
import com.ambermoon.lootgame.entity.items.collectibles.Crucible;
import com.ambermoon.lootgame.entity.items.collectibles.AncientCrown;
import com.ambermoon.lootgame.entity.items.collectibles.LuckyCoin;
import com.ambermoon.lootgame.entity.items.collectibles.TreasureMap;
import com.ambermoon.lootgame.entity.items.collectibles.BottleOfHoney;
import com.ambermoon.lootgame.entity.items.collectibles.SpellCastersTome;
import com.ambermoon.lootgame.entity.items.collectibles.LegendaryForge;
import com.ambermoon.lootgame.entity.items.collectibles.HeartOfEternity;
import com.ambermoon.lootgame.entity.items.collectibles.EyeOfCosmos;
import com.ambermoon.lootgame.entity.items.collectibles.MirrorToOtherRealms;
import com.ambermoon.lootgame.entity.items.collectibles.Gramophone;
import com.ambermoon.lootgame.entity.items.collectibles.UndyingStone;

// Accessories
import com.ambermoon.lootgame.entity.items.accessories.Bracelet;
import com.ambermoon.lootgame.entity.items.accessories.RubySkull;
import com.ambermoon.lootgame.entity.items.accessories.SilverNecklace;
import com.ambermoon.lootgame.entity.items.accessories.SilverBracelet;
import com.ambermoon.lootgame.entity.items.accessories.GoldNecklace;
import com.ambermoon.lootgame.entity.items.accessories.GoldBracelet;
import com.ambermoon.lootgame.entity.items.accessories.SilverRubyNecklace;
import com.ambermoon.lootgame.entity.items.accessories.SilverEmeraldNecklace;
import com.ambermoon.lootgame.entity.items.accessories.SilverSapphireNecklace;
import com.ambermoon.lootgame.entity.items.accessories.SilverDiamondNecklace;
import com.ambermoon.lootgame.entity.items.accessories.SilverRubyBracelet;
import com.ambermoon.lootgame.entity.items.accessories.SilverEmeraldBracelet;
import com.ambermoon.lootgame.entity.items.accessories.SilverSapphireBracelet;
import com.ambermoon.lootgame.entity.items.accessories.SilverDiamondBracelet;
import com.ambermoon.lootgame.entity.items.accessories.GoldRubyNecklace;
import com.ambermoon.lootgame.entity.items.accessories.GoldEmeraldNecklace;
import com.ambermoon.lootgame.entity.items.accessories.GoldSapphireNecklace;
import com.ambermoon.lootgame.entity.items.accessories.GoldDiamondNecklace;
import com.ambermoon.lootgame.entity.items.accessories.GoldRubyBracelet;
import com.ambermoon.lootgame.entity.items.accessories.GoldEmeraldBracelet;
import com.ambermoon.lootgame.entity.items.accessories.GoldSapphireBracelet;
import com.ambermoon.lootgame.entity.items.accessories.GoldDiamondBracelet;

// Clothing
import com.ambermoon.lootgame.entity.items.clothing.Hat;
import com.ambermoon.lootgame.entity.items.clothing.Cap;
import com.ambermoon.lootgame.entity.items.clothing.Pants;
import com.ambermoon.lootgame.entity.items.clothing.Shirt;
import com.ambermoon.lootgame.entity.items.clothing.WornShoes;
import com.ambermoon.lootgame.entity.items.clothing.Boots;
import com.ambermoon.lootgame.entity.items.clothing.CollaredShirt;
import com.ambermoon.lootgame.entity.items.clothing.RedShirt;
import com.ambermoon.lootgame.entity.items.clothing.GreenShirt;
import com.ambermoon.lootgame.entity.items.clothing.BlueShirt;
import com.ambermoon.lootgame.entity.items.clothing.PurpleShirt;
import com.ambermoon.lootgame.entity.items.clothing.OrangeShirt;
import com.ambermoon.lootgame.entity.items.clothing.BlackShirt;
import com.ambermoon.lootgame.entity.items.clothing.WhiteDress;
import com.ambermoon.lootgame.entity.items.clothing.RedDress;
import com.ambermoon.lootgame.entity.items.clothing.GreenDress;
import com.ambermoon.lootgame.entity.items.clothing.BlueDress;
import com.ambermoon.lootgame.entity.items.clothing.PurpleDress;
import com.ambermoon.lootgame.entity.items.clothing.OrangeDress;
import com.ambermoon.lootgame.entity.items.clothing.BlackDress;
import com.ambermoon.lootgame.entity.items.clothing.WhiteHat;
import com.ambermoon.lootgame.entity.items.clothing.RedHat;
import com.ambermoon.lootgame.entity.items.clothing.GreenHat;
import com.ambermoon.lootgame.entity.items.clothing.BlueHat;
import com.ambermoon.lootgame.entity.items.clothing.PurpleHat;
import com.ambermoon.lootgame.entity.items.clothing.OrangeHat;
import com.ambermoon.lootgame.entity.items.clothing.BlackHat;
import com.ambermoon.lootgame.entity.items.clothing.WhitePants;
import com.ambermoon.lootgame.entity.items.clothing.RedPants;
import com.ambermoon.lootgame.entity.items.clothing.GreenPants;
import com.ambermoon.lootgame.entity.items.clothing.BluePants;
import com.ambermoon.lootgame.entity.items.clothing.PurplePants;
import com.ambermoon.lootgame.entity.items.clothing.OrangePants;
import com.ambermoon.lootgame.entity.items.clothing.BlackPants;
import com.ambermoon.lootgame.entity.items.clothing.WhiteRobe;
import com.ambermoon.lootgame.entity.items.clothing.RedRobe;
import com.ambermoon.lootgame.entity.items.clothing.GreenRobe;
import com.ambermoon.lootgame.entity.items.clothing.BlueRobe;
import com.ambermoon.lootgame.entity.items.clothing.PurpleRobe;
import com.ambermoon.lootgame.entity.items.clothing.OrangeRobe;
import com.ambermoon.lootgame.entity.items.clothing.BlackRobe;
import com.ambermoon.lootgame.entity.items.clothing.WhiteShoes;
import com.ambermoon.lootgame.entity.items.clothing.RedShoes;
import com.ambermoon.lootgame.entity.items.clothing.GreenShoes;
import com.ambermoon.lootgame.entity.items.clothing.BlueShoes;
import com.ambermoon.lootgame.entity.items.clothing.PurpleShoes;
import com.ambermoon.lootgame.entity.items.clothing.OrangeShoes;
import com.ambermoon.lootgame.entity.items.clothing.BlackShoes;
import com.ambermoon.lootgame.entity.items.clothing.GoldShirt;
import com.ambermoon.lootgame.entity.items.clothing.GoldPants;
import com.ambermoon.lootgame.entity.items.clothing.GoldShoes;
import com.ambermoon.lootgame.entity.items.clothing.GoldHat;
import com.ambermoon.lootgame.entity.items.clothing.GoldDress;
import com.ambermoon.lootgame.entity.items.clothing.GoldRobe;
import com.ambermoon.lootgame.entity.items.clothing.Cape;
import com.ambermoon.lootgame.entity.items.clothing.ThreePieceSuit;
import com.ambermoon.lootgame.entity.items.clothing.WitchsHat;
import com.ambermoon.lootgame.entity.items.clothing.Swimwear;
import com.ambermoon.lootgame.entity.items.clothing.SkeletonCrown;
import com.ambermoon.lootgame.entity.items.clothing.PlatinumCrown;
import com.ambermoon.lootgame.entity.items.clothing.ChameleonCloak;
import com.ambermoon.lootgame.entity.items.clothing.GownForgottenVerses;

// Keys
import com.ambermoon.lootgame.entity.items.keys.BronzeKey;
import com.ambermoon.lootgame.entity.items.keys.SilverKey;
import com.ambermoon.lootgame.entity.items.keys.GoldenKey;
import com.ambermoon.lootgame.entity.items.keys.SkeletonKey;
import com.ambermoon.lootgame.entity.items.keys.VoidKey;
import com.ambermoon.lootgame.entity.items.keys.OpalKey;
import com.ambermoon.lootgame.entity.items.keys.LithosGodKey;
import com.ambermoon.lootgame.entity.items.keys.ManaGodKey;
import com.ambermoon.lootgame.entity.items.keys.DomineGodKey;

// Ammo
import com.ambermoon.lootgame.entity.items.ammo.Arrow;
import com.ambermoon.lootgame.entity.items.ammo.Bolt;
import com.ambermoon.lootgame.entity.items.ammo.FireArrow;
import com.ambermoon.lootgame.entity.items.ammo.IceArrow;
import com.ambermoon.lootgame.entity.items.ammo.PoisonArrow;
import com.ambermoon.lootgame.entity.items.ammo.HeavyBolt;
import com.ambermoon.lootgame.entity.items.ammo.CannonBall;
import com.ambermoon.lootgame.entity.items.ammo.ExplosiveArrow;
import com.ambermoon.lootgame.entity.items.ammo.ExplosiveBolt;

// Throwables
import com.ambermoon.lootgame.entity.items.throwables.Bomb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ItemRegistry provides access to predefined item templates.
 * Use this to create new instances of common items.
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
 *   Item katana = ItemRegistry.create("katana");
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

        // ==================== COMMON ITEMS ====================

        // Common - Materials
        register("yarn", new Yarn());
        register("coal", new Coal());
        register("cobblestone", new Cobblestone());
        register("planks", new Planks());
        register("ice", new Ice());
        register("feather", new Feather());
        register("iron_ore", new IronOre());
        register("gunpowder", new Gunpowder());
        register("ink", new Ink());
        register("cactus", new Cactus());
        register("bonemeal", new Bonemeal());
        register("rhododendron", new Rhododendron());
        register("rose", new Rose());
        register("violet", new Violet());
        register("white_dye", new WhiteDye());
        register("black_dye", new BlackDye());
        register("red_dye", new RedDye());
        register("green_dye", new GreenDye());
        register("purple_dye", new PurpleDye());
        register("orange_dye", new OrangeDye());
        register("blue_dye", new BlueDye());

        // Common - Food
        register("apple", new Apple());
        register("bread", new Bread());
        register("cheese_wheel", new Cheese());
        register("berries", new Berries());
        register("chicken_egg", new ChickenEgg());
        register("pumpkin", new Pumpkin());

        // Common - Potions
        register("blank_potion", new BlankPotion());

        // Common - Weapons
        register("wooden_sword", new WoodenSword());
        register("club", new Club());
        register("bow", new WoodenBow());
        register("staff", new Staff());
        register("rock", new Rock());

        // Common - Tools
        register("carpenters_axe", new WoodenAxeTool());
        register("wooden_shovel", new WoodenShovel());
        register("wooden_pickaxe", new WoodenPickaxe());
        register("walking_stick", new WalkingStick());
        register("fishing_rod", new FishingRod());

        // Common - Clothing
        register("hat", new Hat());
        register("cap", new Cap());
        register("pants", new Pants());
        register("work_shirt", new Shirt());
        register("worn_shoes", new WornShoes());
        register("boots", new Boots());

        // Common - Armor
        register("leather_tunic", new LeatherTunic());

        // Common - Accessories
        register("bracelet", new Bracelet());

        // Common - Keys
        register("bronze_key", new BronzeKey());

        // Common - Collectibles
        register("water_bottle", new WaterBottle());
        register("nametag", new Nametag());
        register("candle", new Candle());
        register("marbles", new Marbles());

        // Common - Ammo
        register("arrow", new Arrow());
        register("bolt", new Bolt());

        // ==================== UNCOMMON ITEMS ====================

        // Uncommon - Food
        register("melon", new Melon());
        register("chicken", new Chicken());
        register("fish", new Fish());
        register("cooked_salmon", new Salmon());

        // Uncommon - Potions
        register("mana_potion", new ManaPotion());
        register("health_potion", new HealthPotion());
        register("stamina_potion", new StaminaPotion());

        // Uncommon - Materials
        register("lava_stone", new LavaStone());
        register("skull", new Skull());
        register("poison", new Poison());
        register("leather", new Leather());
        register("blank_scroll", new BlankScroll());

        // Uncommon - Weapons
        register("iron_sword", new IronSword());
        register("battle_axe", new BattleAxe());
        register("mace", new Mace());
        register("daggers", new Dagger());
        register("metal_bow", new MetalBow());
        register("crossbow", new Crossbow());
        register("throwing_axe", new ThrowingAxe());
        register("throwing_knife", new ThrowingKnife());

        // Uncommon - Armor
        register("iron_chestplate", new IronChestplate());
        register("iron_boots", new IronBoots());
        register("sentinel_gauntlets", new SentinelGauntlets());
        register("iron_leggings", new IronLeggings());
        register("chainmail_shirt", new ChainmailShirt());
        register("chainmail_pants", new ChainmailPants());
        register("wooden_shield", new WoodenShield());

        // Uncommon - Tools
        register("iron_pickaxe", new IronPickaxe());
        register("iron_shovel", new IronShovel());

        // Uncommon - Keys
        register("silver_key", new SilverKey());

        // Uncommon - Ammo
        register("heavy_bolt", new HeavyBolt());
        register("fire_arrow", new FireArrow());
        register("ice_arrow", new IceArrow());
        register("poison_arrow", new PoisonArrow());
        register("cannon_ball", new CannonBall());

        // Uncommon - Accessories
        register("silver_necklace", new SilverNecklace());
        register("silver_bracelet", new SilverBracelet());

        // Uncommon - Clothing
        register("collared_shirt", new CollaredShirt());
        register("red_shirt", new RedShirt());
        register("green_shirt", new GreenShirt());
        register("blue_shirt", new BlueShirt());
        register("purple_shirt", new PurpleShirt());
        register("orange_shirt", new OrangeShirt());
        register("black_shirt", new BlackShirt());
        register("white_dress", new WhiteDress());
        register("red_dress", new RedDress());
        register("green_dress", new GreenDress());
        register("blue_dress", new BlueDress());
        register("purple_dress", new PurpleDress());
        register("orange_dress", new OrangeDress());
        register("black_dress", new BlackDress());
        register("white_hat", new WhiteHat());
        register("red_hat", new RedHat());
        register("green_hat", new GreenHat());
        register("blue_hat", new BlueHat());
        register("purple_hat", new PurpleHat());
        register("orange_hat", new OrangeHat());
        register("black_hat", new BlackHat());
        register("white_pants", new WhitePants());
        register("red_pants", new RedPants());
        register("green_pants", new GreenPants());
        register("blue_pants", new BluePants());
        register("purple_pants", new PurplePants());
        register("orange_pants", new OrangePants());
        register("black_pants", new BlackPants());
        register("white_robe", new WhiteRobe());
        register("red_robe", new RedRobe());
        register("green_robe", new GreenRobe());
        register("blue_robe", new BlueRobe());
        register("purple_robe", new PurpleRobe());
        register("orange_robe", new OrangeRobe());
        register("black_robe", new BlackRobe());
        register("white_shoes", new WhiteShoes());
        register("red_shoes", new RedShoes());
        register("green_shoes", new GreenShoes());
        register("blue_shoes", new BlueShoes());
        register("purple_shoes", new PurpleShoes());
        register("orange_shoes", new OrangeShoes());
        register("black_shoes", new BlackShoes());
        register("swimwear", new Swimwear());

        // Uncommon - Collectibles
        register("journal", new Journal());
        register("jack_o_lantern", new JackOLantern());
        register("frog_egg", new FrogEgg());
        register("music_disc", new MusicDisc());
        register("crucible", new Crucible());
        register("bottle_of_honey", new BottleOfHoney());

        // ==================== RARE ITEMS ====================

        // Rare - Collectibles
        register("frog", new Frog());
        register("bunny", new Bunny());
        register("orb", new Orb());
        register("ancient_pottery", new AncientPottery());
        register("saddle", new Saddle());
        register("backpack", new Backpack());
        register("trip_wire_trap", new TripWireTrap());

        // Rare - Materials
        register("cherry_sapling", new CherrySapling());
        register("willow_sapling", new WillowSapling());
        register("pine_sapling", new PineSapling());
        register("oak_sapling", new OakSapling());
        register("palm_sapling", new PalmSapling());
        register("scroll_of_fireball", new ScrollOfFireball());
        register("scroll_of_ice_crystal", new ScrollOfIceCrystal());
        register("scroll_of_poison", new ScrollOfPoison());
        register("scroll_of_fire_rune", new ScrollOfFireRune());
        register("scroll_of_ice_rune", new ScrollOfIceRune());
        register("scroll_of_poison_rune", new ScrollOfPoisonRune());
        register("ruby", new Ruby());
        register("emerald", new Emerald());
        register("sapphire", new Sapphire());
        register("diamond", new Diamond());
        register("obsidian_ore", new ObsidianOre());

        // Rare - Weapons
        register("gold_sword", new GoldSword());
        register("golden_heavy_battleaxe", new GoldenHeavyBattleaxe());
        register("golden_mace", new GoldenMace());
        register("golden_bow", new GoldenBow());
        register("katana", new Katana());
        register("ice_staff", new IceStaff());
        register("heavy_crossbow", new HeavyCrossbow());
        register("lightning_rod", new LightningRod());
        register("magic_wand", new MagicWand());
        register("magic_fishing_rod", new MagicFishingRod());

        // Rare - Potions
        register("full_health_potion", new FullHealthPotion());
        register("full_mana_potion", new FullManaPotion());
        register("full_stamina_potion", new FullStaminaPotion());
        register("xp_potion", new XPPotion());
        register("purple_potion", new PurplePotion());

        // Rare - Armor
        register("steel_shield", new SteelShield());
        register("wizard_hat", new WizardHat());
        register("fancy_boots", new FancyBoots());

        // Rare - Clothing
        register("gold_shirt", new GoldShirt());
        register("gold_pants", new GoldPants());
        register("gold_shoes", new GoldShoes());
        register("gold_hat", new GoldHat());
        register("gold_dress", new GoldDress());
        register("gold_robe", new GoldRobe());
        register("cape", new Cape());
        register("three_piece_suit", new ThreePieceSuit());
        register("witchs_hat", new WitchsHat());

        // Rare - Accessories
        register("gold_necklace", new GoldNecklace());
        register("gold_bracelet", new GoldBracelet());
        register("silver_ruby_necklace", new SilverRubyNecklace());
        register("silver_emerald_necklace", new SilverEmeraldNecklace());
        register("silver_sapphire_necklace", new SilverSapphireNecklace());
        register("silver_diamond_necklace", new SilverDiamondNecklace());
        register("silver_ruby_bracelet", new SilverRubyBracelet());
        register("silver_emerald_bracelet", new SilverEmeraldBracelet());
        register("silver_sapphire_bracelet", new SilverSapphireBracelet());
        register("silver_diamond_bracelet", new SilverDiamondBracelet());

        // Rare - Keys
        register("golden_key", new GoldenKey());

        // Rare - Ammo
        register("explosive_arrow", new ExplosiveArrow());

        // Rare - Throwables
        register("bomb", new Bomb());

        // ==================== EPIC ITEMS ====================

        // Epic - Collectibles
        register("treasure_map", new TreasureMap());
        register("mysterious_candle", new MysteriousCandle());
        register("spell_casters_tome", new SpellCastersTome());
        register("lucky_coin", new LuckyCoin());
        register("legendary_forge", new LegendaryForge());
        register("undead_scroll", new UndeadScroll());
        register("crown", new AncientCrown());

        // Epic - Materials
        register("magic_gemstone", new MysteriousGemstone());
        register("void_scroll", new VoidScroll());
        register("void_rune_scroll", new VoidRuneScroll());
        register("void_stone", new VoidStone());
        register("gold_bar", new GoldBars());

        // Epic - Weapons
        register("soulbound_dagger", new SoulboundDagger());
        register("spectral_bow", new SpectralBow());
        register("spectral_sword", new SpectralSword());
        register("spectral_axe", new SpectralAxe());
        register("spectral_mace", new SpectralMace());
        register("vampiric_dagger", new VampiricBlade());
        register("thunder_hammer", new ThunderHammer());
        register("musket", new Musket());
        register("summoning_rod", new SummoningRod());

        // Epic - Armor
        register("golden_shield", new GoldenShield());
        register("titan_gauntlets", new TitanGauntlets());
        register("celestial_robes", new CelestialRobes());
        register("archmage_robes", new ArchmageRobes());
        register("obsidian_helmet", new ObsidianHelmet());
        register("obsidian_chestplate", new ObsidianChestplate());
        register("obsidian_leggings", new ObsidianLeggings());
        register("obsidian_gauntlets", new ObsidianGauntlets());
        register("obsidian_boots", new ObsidianBoots());

        // Epic - Tools
        register("diamond_engraved_pickaxe", new DiamondEngravedPickaxe());
        register("magic_shovel", new MagicShovel());

        // Epic - Potions
        register("lucky_potion", new LuckyPotion());

        // Epic - Keys
        register("skeleton_key", new SkeletonKey());
        register("void_key", new VoidKey());

        // Epic - Accessories
        register("ruby_skull", new RubySkull());
        register("gold_ruby_necklace", new GoldRubyNecklace());
        register("gold_emerald_necklace", new GoldEmeraldNecklace());
        register("gold_sapphire_necklace", new GoldSapphireNecklace());
        register("gold_diamond_necklace", new GoldDiamondNecklace());
        register("gold_ruby_bracelet", new GoldRubyBracelet());
        register("gold_emerald_bracelet", new GoldEmeraldBracelet());
        register("gold_sapphire_bracelet", new GoldSapphireBracelet());
        register("gold_diamond_bracelet", new GoldDiamondBracelet());

        // Epic - Ammo
        register("explosive_bolt", new ExplosiveBolt());

        // Epic - Food
        register("magic_apple", new MagicApple());
        register("berries_of_the_old_gods", new BerriesOfTheOldGods());

        // ==================== LEGENDARY ITEMS ====================

        // Legendary - Collectibles
        register("mirror_to_other_realms", new MirrorToOtherRealms());
        register("gramophone", new Gramophone());
        register("undying_stone", new UndyingStone());

        // Legendary - Weapons
        register("arcane_staff", new ArcaneStaff());
        register("void_blade", new VoidBlade());
        register("necromancers_blade", new NecromancersBlade());
        register("ethereal_dragonslayer", new EtherealDragonslayer());
        register("electrified_katana", new ElectrifiedKatana());
        register("cannon", new Cannon());
        register("infinity_staff", new InfinityStaff());
        register("phoenix_bow", new PhoenixBow());

        // Legendary - Armor
        register("void_shield", new VoidShield());
        register("void_helmet", new VoidHelmet());
        register("void_chestplate", new VoidChestplate());
        register("void_leggings", new VoidLeggings());
        register("void_gauntlets", new VoidGauntlets());
        register("void_boots", new VoidBoots());

        // Legendary - Potions
        register("elixir_of_immortality", new ElixirOfImmortality());
        register("void_potion", new VoidPotion());

        // Legendary - Materials
        register("red_dragon_egg", new RedDragonEgg());
        register("green_dragon_egg", new GreenDragonEgg());
        register("blue_dragon_egg", new BlueDragonEgg());
        register("black_dragon_egg", new BlackDragonEgg());
        register("white_dragon_egg", new WhiteDragonEgg());

        // Legendary - Keys
        register("opal_key", new OpalKey());

        // Legendary - Clothing
        register("skeleton_crown", new SkeletonCrown());
        register("platinum_crown", new PlatinumCrown());
        register("chameleon_cloak", new ChameleonCloak());

        // ==================== MYTHIC ITEMS ====================

        register("time_warp_blade", new TimeWarpBlade());
        register("staff_of_1000_souls", new StaffOf1000Souls());
        register("eye_of_the_cosmos", new EyeOfCosmos());
        register("heart_of_eternity", new HeartOfEternity());
        register("gown_of_forgotten_verses", new GownForgottenVerses());
        register("lithos_god_key", new LithosGodKey());
        register("mana_god_key", new ManaGodKey());
        register("domine_god_key", new DomineGodKey());

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
     * @param id Item ID (e.g., "iron_sword", "katana")
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
