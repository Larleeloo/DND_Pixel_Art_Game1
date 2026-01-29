package entity.item;

import animation.ItemAnimationState;
import animation.TriggeredAnimationManager;
import entity.item.Item.ItemCategory;
import entity.item.Item.ItemRarity;
import entity.ProjectileEntity.ProjectileType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

// Import all item classes
import entity.item.items.weapons.melee.*;
import entity.item.items.weapons.ranged.*;
import entity.item.items.weapons.throwing.*;
import entity.item.items.ammo.*;
import entity.item.items.throwables.*;
import entity.item.items.tools.*;
import entity.item.items.armor.*;
import entity.item.items.food.*;
import entity.item.items.potions.*;
import entity.item.items.materials.*;
import entity.item.items.keys.*;
import entity.item.items.clothing.*;
import entity.item.items.collectibles.*;
import entity.item.items.accessories.*;
import entity.item.items.blocks.*;

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
     * Each item is now instantiated from its dedicated class file.
     */
    public static void initialize() {
        if (initialized) return;

        // ==================== MELEE WEAPONS ====================

        templates.put("wooden_sword", new WoodenSword());
        templates.put("iron_sword", new IronSword());
        templates.put("steel_sword", new SteelSword());
        templates.put("fire_sword", new FireSword());
        templates.put("ice_sword", new IceSword());
        templates.put("legendary_sword", new LegendarySword());
        templates.put("dagger", new Dagger());
        templates.put("battle_axe", new BattleAxe());
        templates.put("mace", new Mace());

        // ==================== RANGED WEAPONS ====================

        templates.put("wooden_bow", new WoodenBow());
        templates.put("longbow", new Longbow());
        templates.put("crossbow", new Crossbow());
        templates.put("heavy_crossbow", new HeavyCrossbow());
        templates.put("magic_wand", new MagicWand());
        templates.put("fire_staff", new FireStaff());
        templates.put("ice_staff", new IceStaff());
        templates.put("arcane_staff", new ArcaneStaff());

        // ==================== THROWING WEAPONS ====================

        templates.put("throwing_knife", new ThrowingKnife());
        templates.put("throwing_axe", new ThrowingAxe());
        templates.put("rock", new Rock());

        // ==================== AMMO ====================

        templates.put("arrow", new Arrow());
        templates.put("fire_arrow", new FireArrow());
        templates.put("ice_arrow", new IceArrow());
        templates.put("bolt", new Bolt());
        templates.put("heavy_bolt", new HeavyBolt());
        templates.put("mana_crystal", new ManaCrystal());

        // ==================== THROWABLES (Consumable) ====================

        templates.put("bomb", new Bomb());
        templates.put("throwing_potion", new ThrowingPotion());

        // ==================== TOOLS ====================

        templates.put("wooden_pickaxe", new WoodenPickaxe());
        templates.put("iron_pickaxe", new IronPickaxe());
        templates.put("golden_pickaxe", new GoldenPickaxe());
        templates.put("wooden_axe", new WoodenAxeTool());
        templates.put("iron_axe", new IronAxeTool());
        templates.put("wooden_shovel", new WoodenShovel());
        templates.put("iron_shovel", new IronShovel());

        // ==================== ARMOR ====================

        templates.put("iron_helmet", new IronHelmet());
        templates.put("iron_chestplate", new IronChestplate());
        templates.put("iron_leggings", new IronLeggings());
        templates.put("iron_boots", new IronBoots());
        templates.put("steel_helmet", new SteelHelmet());
        templates.put("steel_chestplate", new SteelChestplate());
        templates.put("wizard_hat", new WizardHat());

        // ==================== FOOD ====================

        templates.put("bread", new Bread());
        templates.put("apple", new Apple());
        templates.put("cooked_meat", new CookedMeat());
        templates.put("cheese", new Cheese());
        templates.put("golden_apple", new GoldenApple());

        // ==================== POTIONS ====================

        templates.put("health_potion", new HealthPotion());
        templates.put("mana_potion", new ManaPotion());
        templates.put("stamina_potion", new StaminaPotion());
        templates.put("greater_health_potion", new GreaterHealthPotion());
        templates.put("strength_potion", new StrengthPotion());
        templates.put("speed_potion", new SpeedPotion());
        templates.put("exploding_potion", new ExplodingPotion());

        // ==================== MATERIALS ====================

        templates.put("string", new StringItem());
        templates.put("iron_ingot", new IronIngot());
        templates.put("gold_ingot", new GoldIngot());
        templates.put("diamond", new Diamond());
        templates.put("magic_crystal", new MagicCrystalMaterial());
        templates.put("scroll", new Scroll());

        // ==================== KEYS ====================

        templates.put("bronze_key", new BronzeKey());
        templates.put("silver_key", new SilverKey());
        templates.put("golden_key", new GoldenKey());
        templates.put("skeleton_key", new SkeletonKey());

        // ==================== LEGENDARY WEAPONS (Discord Bot Items) ====================

        templates.put("necromancers_blade", new NecromancersBlade());
        templates.put("electrified_katana", new ElectrifiedKatana());
        templates.put("ethereal_dragonslayer", new EtherealDragonslayer());
        templates.put("soulbound_dagger", new SoulboundDagger());

        // ==================== EPIC RANGED WEAPONS (Discord Bot Items) ====================

        templates.put("epic_bow", new EpicBow());
        templates.put("summoning_rod", new SummoningRod());
        templates.put("lightning_rod", new LightningRod());
        templates.put("blazing_rod", new BlazingRod());
        templates.put("magic_fishing_rod", new MagicFishingRod());

        // ==================== SHIELDS & ARMOR (Discord Bot Items) ====================

        templates.put("steel_shield", new SteelShield());
        templates.put("sentinel_gauntlets", new SentinelGauntlets());
        templates.put("gold_armor_helmet", new GoldArmorHelmet());
        templates.put("gold_armor_chest", new GoldArmorChest());
        templates.put("gold_armor_legs", new GoldArmorLegs());
        templates.put("gold_armor_boots", new GoldArmorBoots());
        templates.put("chainmail_armor", new ChainmailArmor());
        templates.put("dragon_scale_armor", new DragonScaleArmor());
        templates.put("fancy_boots", new FancyBoots());

        // ==================== CLOTHING (Discord Bot Items) ====================

        templates.put("green_dress", new GreenDress());
        templates.put("orange_dress", new OrangeDress());
        templates.put("black_dress", new BlackDress());
        templates.put("hat", new Hat());
        templates.put("pants", new Pants());
        templates.put("shirt", new Shirt());
        templates.put("three_piece_suit", new ThreePieceSuit());
        templates.put("swimwear", new Swimwear());
        templates.put("yellow_cloak", new YellowCloak());
        templates.put("chameleon_cloak", new ChameleonCloak());
        templates.put("witchs_hat", new WitchsHat());
        templates.put("gown_forgotten_verses", new GownForgottenVerses());

        // ==================== POTIONS (Discord Bot Items) ====================

        templates.put("lucky_potion", new LuckyPotion());
        templates.put("honey_potion", new HoneyPotion());
        templates.put("brewed_potion", new BrewedPotion());
        templates.put("mana_leaf", new ManaLeaf());

        // ==================== FOOD (Discord Bot Items) ====================

        templates.put("cake", new Cake());
        templates.put("cookies", new Cookies());
        templates.put("melon", new Melon());
        templates.put("pumpkin", new Pumpkin());
        templates.put("salmon", new Salmon());
        templates.put("chicken_egg", new ChickenEgg());

        // ==================== MATERIALS & ORES (Discord Bot Items) ====================

        templates.put("iron_ore", new IronOre());
        templates.put("gold_coins", new GoldCoins());
        templates.put("copper_ingots", new CopperIngots());
        templates.put("gold_bars", new GoldBars());
        templates.put("iron_bars", new IronBars());
        templates.put("leather", new Leather());
        templates.put("yarn", new Yarn());
        templates.put("flour", new Flour());
        templates.put("mysterious_gemstone", new MysteriousGemstone());
        templates.put("dragon_egg", new DragonEgg());
        templates.put("bonemite", new Bonemeal());
        templates.put("ink", new Ink());
        templates.put("planks", new Planks());
        templates.put("rocks", new Rocks());
        templates.put("rope", new Rope());
        templates.put("sapling", new Sapling());
        templates.put("machine_parts", new MachineParts());
        templates.put("skull", new Skull());

        // The Ruby Skull - special accessory that grants unlimited jumps when held
        templates.put("ruby_skull", new RubySkull());

        // ==================== TOOLS (Discord Bot Items) ====================

        templates.put("shears", new Shears());
        templates.put("fishing_rod", new FishingRod());
        templates.put("walking_stick", new WalkingStick());

        // ==================== COLLECTIBLES & MISC (Discord Bot Items) ====================

        templates.put("orb", new Orb());
        templates.put("ancient_pottery", new AncientPottery());
        templates.put("music_disc", new MusicDisc());
        templates.put("music_player", new MusicPlayer());
        templates.put("journal", new Journal());
        templates.put("painting_wolves", new PaintingWolves());
        templates.put("painting_dog", new PaintingDog());
        templates.put("marbles", new Marbles());
        templates.put("nametag", new Nametag());
        templates.put("candle", new Candle());
        templates.put("mysterious_candle", new MysteriousCandle());
        templates.put("water_bottle", new WaterBottle());
        templates.put("saddle", new Saddle());
        templates.put("backpack", new Backpack());
        templates.put("personalized_banner", new PersonalizedBanner());
        templates.put("frog", new Frog());
        templates.put("jack_o_lantern", new JackOLantern());
        templates.put("rocket", new Rocket());
        templates.put("wind_charge", new WindCharge());
        templates.put("undead_scroll", new UndeadScroll());
        templates.put("trip_wire_trap", new TripWireTrap());
        templates.put("magic_lantern", new MagicLantern());
        templates.put("crucible", new Crucible());

        // Mirror to Other Realms - special ranged weapon with realm-cycling projectiles
        templates.put("mirror_realms", new MirrorToOtherRealms());

        templates.put("rowboat", new Rowboat());

        // ==================== BLOCKS (Placeable) ====================

        // Terrain blocks
        templates.put("dirt_block", new DirtBlock());
        templates.put("grass_block", new GrassBlock());
        templates.put("stone_block", new StoneBlock());
        templates.put("cobblestone_block", new CobblestoneBlock());
        templates.put("sand_block", new SandBlock());

        // Nature blocks
        templates.put("wood_block", new WoodBlock());
        templates.put("leaves_block", new LeavesBlock());

        // Special blocks
        templates.put("brick_block", new BrickBlock());
        templates.put("glass_block", new GlassBlock());
        templates.put("water_block", new WaterBlock());

        // Ore blocks
        templates.put("coal_ore_block", new CoalOreBlock());
        templates.put("iron_ore_block", new IronOreBlock());
        templates.put("gold_ore_block", new GoldOreBlock());

        // Weather/Environment blocks (block masks)
        templates.put("snow_block", new SnowBlock());
        templates.put("ice_block", new IceBlock());
        templates.put("moss_block", new MossBlock());
        templates.put("vines_block", new VinesBlock());

        // ==================== SPECIAL CRYSTAL EDITIONS ====================

        templates.put("crystal_summer", new CrystalSummer());
        templates.put("crystal_winter", new CrystalWinter());

        // ==================== MYTHIC WEAPONS (Ultimate Loot) ====================
        // Mythic weapons require high Wisdom to wield (ancient artifact restrictions)

        templates.put("void_blade", new VoidBlade());
        templates.put("celestial_bow", new CelestialBow());
        templates.put("infinity_staff", new InfinityStaff());
        templates.put("soul_reaver", new SoulReaver());
        templates.put("time_warp_blade", new TimeWarpBlade());

        // ==================== LEGENDARY WEAPONS ====================
        // Legendary weapons require moderate Wisdom to wield

        templates.put("phoenix_bow", new PhoenixBow());
        templates.put("frostmourne", new Frostmourne());
        templates.put("thunder_hammer", new ThunderHammer());
        templates.put("shadow_dagger", new ShadowDagger());

        // ==================== EPIC ARMOR ====================

        templates.put("phoenix_crown", new PhoenixCrown());
        templates.put("void_armor", new VoidArmor());
        templates.put("celestial_robes", new CelestialRobes());
        templates.put("titan_gauntlets", new TitanGauntlets());

        // ==================== MYTHIC ACCESSORIES ====================
        // Mythic accessories are ancient artifacts requiring high Wisdom

        templates.put("heart_of_eternity", new HeartOfEternity());
        templates.put("eye_of_cosmos", new EyeOfCosmos());
        templates.put("philosophers_stone", new PhilosophersStone());
        templates.put("ankh_of_rebirth", new AnkhOfRebirth());

        // ==================== LEGENDARY POTIONS ====================

        templates.put("elixir_of_immortality", new ElixirOfImmortality());
        templates.put("potion_of_ascension", new PotionOfAscension());
        templates.put("essence_of_dragon", new EssenceOfDragon());

        // ==================== EPIC COLLECTIBLES ====================

        templates.put("ancient_crown", new AncientCrown());
        templates.put("demon_horn", new DemonHorn());
        templates.put("angel_feather", new AngelFeather());
        templates.put("void_shard", new VoidShard());
        templates.put("lucky_coin", new LuckyCoin());
        templates.put("treasure_map", new TreasureMap());

        // ==================== RARE WEAPONS ====================

        templates.put("crystal_sword", new CrystalSword());
        templates.put("vampiric_blade", new VampiricBlade());
        templates.put("poison_dagger", new PoisonDagger());

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

    // ==================== Registration Helpers (Legacy - kept for reference) ====================
    // These methods are no longer used but kept for documentation purposes.
    // All items are now instantiated from their dedicated class files.

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
            // Use copy() to preserve subclass types (e.g., MirrorToOtherRealms)
            Item copy = template.copy();
            // Set the registry ID so throwables can track their source for drops
            copy.setRegistryId(id);
            return copy;
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
