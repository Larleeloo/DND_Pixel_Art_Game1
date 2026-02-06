package com.ambermoon.lootgame.entity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.ambermoon.lootgame.graphics.AssetLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * Item represents a game item with properties, categories, and visual representation.
 * Items can be held by entities and rendered as overlays on their sprites.
 *
 * Adapted from the Android port's Item.java for the standalone Loot Game App.
 * - Package: com.ambermoon.lootgame.entity
 * - Uses AssetLoader instead of AndroidAssetLoader
 * - No ProjectileEntity dependency - uses string-based projectile/effect types
 *
 * Features:
 * - Multiple item categories (weapon, armor, tool, food, etc.)
 * - Rarity system with color coding
 * - Properties like damage, defense, range, special effects
 * - Projectile firing capabilities for ranged weapons (string-based type storage)
 *
 * Usage:
 *   Item sword = new Item("Iron Sword", ItemCategory.WEAPON);
 *   sword.setDamage(15);
 *   sword.setRarity(ItemRarity.UNCOMMON);
 */
public class Item {

    private static final String TAG = "Item";

    // ==================== Item Category Constants ====================
    // (Replaced enum with int constants to avoid D8 crash)

    public static final int CATEGORY_WEAPON = 0;
    public static final int CATEGORY_RANGED_WEAPON = 1;
    public static final int CATEGORY_TOOL = 2;
    public static final int CATEGORY_ARMOR = 3;
    public static final int CATEGORY_CLOTHING = 4;
    public static final int CATEGORY_BLOCK = 5;
    public static final int CATEGORY_FOOD = 6;
    public static final int CATEGORY_POTION = 7;
    public static final int CATEGORY_MATERIAL = 8;
    public static final int CATEGORY_KEY = 9;
    public static final int CATEGORY_ACCESSORY = 10;
    public static final int CATEGORY_THROWABLE = 11;
    public static final int CATEGORY_OTHER = 12;
    public static final int CATEGORY_COUNT = 13;

    private static final String[] CATEGORY_NAMES = {
        "WEAPON", "RANGED_WEAPON", "TOOL", "ARMOR", "CLOTHING",
        "BLOCK", "FOOD", "POTION", "MATERIAL", "KEY",
        "ACCESSORY", "THROWABLE", "OTHER"
    };

    public static String getCategoryName(int category) {
        if (category >= 0 && category < CATEGORY_COUNT) return CATEGORY_NAMES[category];
        return "UNKNOWN";
    }

    public static int categoryFromName(String name) {
        if (name == null) return CATEGORY_OTHER;
        for (int i = 0; i < CATEGORY_COUNT; i++) {
            if (CATEGORY_NAMES[i].equalsIgnoreCase(name)) return i;
        }
        return CATEGORY_OTHER;
    }

    /**
     * Helper class for backwards compatibility with enum-like API.
     */
    public static final class ItemCategory {
        private final int value;

        private ItemCategory(int value) { this.value = value; }

        public static ItemCategory fromInt(int value) { return new ItemCategory(value); }
        public int intValue() { return value; }
        public String name() { return getCategoryName(value); }

        public static final ItemCategory WEAPON = new ItemCategory(CATEGORY_WEAPON);
        public static final ItemCategory RANGED_WEAPON = new ItemCategory(CATEGORY_RANGED_WEAPON);
        public static final ItemCategory TOOL = new ItemCategory(CATEGORY_TOOL);
        public static final ItemCategory ARMOR = new ItemCategory(CATEGORY_ARMOR);
        public static final ItemCategory CLOTHING = new ItemCategory(CATEGORY_CLOTHING);
        public static final ItemCategory BLOCK = new ItemCategory(CATEGORY_BLOCK);
        public static final ItemCategory FOOD = new ItemCategory(CATEGORY_FOOD);
        public static final ItemCategory POTION = new ItemCategory(CATEGORY_POTION);
        public static final ItemCategory MATERIAL = new ItemCategory(CATEGORY_MATERIAL);
        public static final ItemCategory KEY = new ItemCategory(CATEGORY_KEY);
        public static final ItemCategory ACCESSORY = new ItemCategory(CATEGORY_ACCESSORY);
        public static final ItemCategory THROWABLE = new ItemCategory(CATEGORY_THROWABLE);
        public static final ItemCategory OTHER = new ItemCategory(CATEGORY_OTHER);
    }

    // ==================== Item Rarity Constants ====================

    public static final int RARITY_COMMON = 0;
    public static final int RARITY_UNCOMMON = 1;
    public static final int RARITY_RARE = 2;
    public static final int RARITY_EPIC = 3;
    public static final int RARITY_LEGENDARY = 4;
    public static final int RARITY_MYTHIC = 5;
    public static final int RARITY_COUNT = 6;

    private static final int[] RARITY_COLORS = {
        Color.WHITE,
        Color.rgb(30, 255, 30),
        Color.rgb(30, 100, 255),
        Color.rgb(180, 30, 255),
        Color.rgb(255, 165, 0),
        Color.rgb(0, 255, 255)
    };

    private static final String[] RARITY_DISPLAY_NAMES = {
        "Common", "Uncommon", "Rare", "Epic", "Legendary", "Mythic"
    };

    public static int getRarityColor(int rarity) {
        if (rarity >= 0 && rarity < RARITY_COUNT) return RARITY_COLORS[rarity];
        return Color.WHITE;
    }

    public static String getRarityDisplayName(int rarity) {
        if (rarity >= 0 && rarity < RARITY_COUNT) return RARITY_DISPLAY_NAMES[rarity];
        return "Common";
    }

    /**
     * Helper class for backwards compatibility with enum-like API.
     */
    public static final class ItemRarity {
        private final int value;

        private ItemRarity(int value) { this.value = value; }

        public static ItemRarity fromInt(int value) { return new ItemRarity(value); }
        public int intValue() { return value; }
        public int ordinal() { return value; }
        public int getColor() { return getRarityColor(value); }
        public String getDisplayName() { return getRarityDisplayName(value); }

        public static final ItemRarity COMMON = new ItemRarity(RARITY_COMMON);
        public static final ItemRarity UNCOMMON = new ItemRarity(RARITY_UNCOMMON);
        public static final ItemRarity RARE = new ItemRarity(RARITY_RARE);
        public static final ItemRarity EPIC = new ItemRarity(RARITY_EPIC);
        public static final ItemRarity LEGENDARY = new ItemRarity(RARITY_LEGENDARY);
        public static final ItemRarity MYTHIC = new ItemRarity(RARITY_MYTHIC);
    }

    // ==================== Core Properties ====================

    private String name;
    private String description;
    private ItemCategory category;
    private ItemRarity rarity;
    private boolean stackable;
    private int maxStackSize;
    private String registryId;  // Registry ID for item lookup (e.g., "throwing_knife")

    // Item icon for inventory display
    private Bitmap icon;
    private AssetLoader.ImageAsset iconAnimation;
    private String texturePath;  // Path to the GIF texture file

    // Triggered animation system (new folder-based structure)
    private String animationFolderPath;  // Path to folder containing animation states

    // ==================== Combat Properties ====================

    private int damage = 0;
    private int defense = 0;
    private float attackSpeed = 1.0f;    // Attacks per second
    private int range = 60;              // Attack/effect range in pixels
    private float critChance = 0.05f;    // 5% default crit chance
    private float critMultiplier = 1.5f; // 150% damage on crit

    // ==================== Ranged Weapon Properties ====================

    private boolean isRangedWeapon = false;
    private String projectileTypeName;   // Stored as string (no ProjectileEntity dependency)
    private int projectileDamage = 0;
    private float projectileSpeed = 15.0f;
    private int ammoCapacity = 1;
    private int currentAmmo = 0;
    private String ammoItemName;  // Required ammo item name

    // ==================== Charged Shot Properties ====================

    private boolean isChargeable = false;
    private float maxChargeTime = 2.0f;
    private float minChargeTime = 0.3f;
    private int chargeManaCost = 20;
    private float chargeDamageMultiplier = 3.0f;
    private float chargeSpeedMultiplier = 1.5f;
    private float chargeSizeMultiplier = 2.0f;

    // ==================== Consumable Properties ====================

    private boolean isConsumable = false;
    private int healthRestore = 0;
    private int manaRestore = 0;
    private int staminaRestore = 0;
    private float consumeTime = 1.0f;

    // ==================== Special Effects ====================

    private String specialEffect;
    private boolean hasAreaEffect = false;
    private int areaEffectRadius = 0;

    // Status effect properties (stored as strings, no ProjectileEntity dependency)
    private String statusEffectTypeName = "NONE";
    private double statusEffectDuration = 0;
    private int statusEffectDamagePerTick = 0;
    private float statusEffectDamageMultiplier = 1.0f;

    // ==================== Ability Scaling Properties ====================

    private boolean scalesWithStrength = false;
    private boolean scalesWithDexterity = false;
    private boolean scalesWithIntelligence = false;
    private int wisdomRequirement = 0;

    // Reusable Paint for drawing
    private static final Paint drawPaint = new Paint();
    private static final Rect srcRect = new Rect();
    private static final Rect dstRect = new Rect();

    // ==================== Constructors ====================

    /**
     * Creates a new item with a name and category.
     */
    public Item(String name, ItemCategory category) {
        this.name = name;
        this.category = category;
        this.rarity = ItemRarity.COMMON;
        this.description = "";

        // Set defaults based on category
        switch (category.intValue()) {
            case CATEGORY_WEAPON:
            case CATEGORY_TOOL:
            case CATEGORY_ARMOR:
                this.stackable = false;
                this.maxStackSize = 1;
                break;
            case CATEGORY_RANGED_WEAPON:
                this.stackable = false;
                this.maxStackSize = 1;
                this.isRangedWeapon = true;
                break;
            case CATEGORY_FOOD:
            case CATEGORY_POTION:
                this.stackable = true;
                this.maxStackSize = 16;
                this.isConsumable = true;
                break;
            case CATEGORY_MATERIAL:
            case CATEGORY_KEY:
            case CATEGORY_ACCESSORY:
            case CATEGORY_THROWABLE:
            case CATEGORY_CLOTHING:
            case CATEGORY_OTHER:
                this.stackable = true;
                this.maxStackSize = 16;
                break;
            case CATEGORY_BLOCK:
                this.stackable = true;
                this.maxStackSize = 64;
                break;
            default:
                this.stackable = true;
                this.maxStackSize = 16;
        }
    }

    /**
     * Creates a copy of an item.
     */
    public Item(Item original) {
        this.name = original.name;
        this.description = original.description;
        this.category = original.category;
        this.rarity = original.rarity;
        this.stackable = original.stackable;
        this.maxStackSize = original.maxStackSize;
        this.registryId = original.registryId;
        this.icon = original.icon;
        this.iconAnimation = original.iconAnimation;
        this.texturePath = original.texturePath;
        this.animationFolderPath = original.animationFolderPath;
        this.damage = original.damage;
        this.defense = original.defense;
        this.attackSpeed = original.attackSpeed;
        this.range = original.range;
        this.critChance = original.critChance;
        this.critMultiplier = original.critMultiplier;
        this.isRangedWeapon = original.isRangedWeapon;
        this.projectileTypeName = original.projectileTypeName;
        this.projectileDamage = original.projectileDamage;
        this.projectileSpeed = original.projectileSpeed;
        this.ammoCapacity = original.ammoCapacity;
        this.currentAmmo = original.currentAmmo;
        this.ammoItemName = original.ammoItemName;
        this.isChargeable = original.isChargeable;
        this.maxChargeTime = original.maxChargeTime;
        this.minChargeTime = original.minChargeTime;
        this.chargeManaCost = original.chargeManaCost;
        this.chargeDamageMultiplier = original.chargeDamageMultiplier;
        this.chargeSpeedMultiplier = original.chargeSpeedMultiplier;
        this.chargeSizeMultiplier = original.chargeSizeMultiplier;
        this.isConsumable = original.isConsumable;
        this.healthRestore = original.healthRestore;
        this.manaRestore = original.manaRestore;
        this.staminaRestore = original.staminaRestore;
        this.consumeTime = original.consumeTime;
        this.specialEffect = original.specialEffect;
        this.hasAreaEffect = original.hasAreaEffect;
        this.areaEffectRadius = original.areaEffectRadius;
        this.statusEffectTypeName = original.statusEffectTypeName;
        this.statusEffectDuration = original.statusEffectDuration;
        this.statusEffectDamagePerTick = original.statusEffectDamagePerTick;
        this.statusEffectDamageMultiplier = original.statusEffectDamageMultiplier;
        this.scalesWithStrength = original.scalesWithStrength;
        this.scalesWithDexterity = original.scalesWithDexterity;
        this.scalesWithIntelligence = original.scalesWithIntelligence;
        this.wisdomRequirement = original.wisdomRequirement;
    }

    /**
     * Creates a copy of this item.
     * Subclasses should override this to return their specific type.
     */
    public Item copy() {
        return new Item(this);
    }

    // ==================== Animation Loading ====================

    /**
     * Loads the item icon from an asset path.
     * Uses AssetLoader instead of AndroidAssetLoader.
     */
    public void loadIcon(String path) {
        this.texturePath = path;
        try {
            AssetLoader.ImageAsset asset = AssetLoader.load(path);
            if (asset != null) {
                if (asset.isAnimated) {
                    this.iconAnimation = asset;
                    this.icon = asset.bitmap;  // First frame
                } else {
                    this.icon = asset.bitmap;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to load icon: " + path);
        }
    }

    /**
     * Sets the texture path without loading (for reference).
     */
    public void setTexturePath(String path) {
        this.texturePath = path;
    }

    /**
     * Gets the texture path for this item.
     */
    public String getTexturePath() {
        return texturePath;
    }

    // ==================== Triggered Animation System ====================

    /**
     * Sets the animation folder path for this item.
     */
    public void setAnimationFolderPath(String folderPath) {
        this.animationFolderPath = folderPath;
    }

    /**
     * Gets the animation folder path for this item.
     */
    public String getAnimationFolderPath() {
        return animationFolderPath;
    }

    /**
     * Checks if this item has an animation folder with multiple states.
     */
    public boolean hasAnimationFolder() {
        return animationFolderPath != null;
    }

    // ==================== Getters and Setters ====================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistryId() {
        return registryId;
    }

    public void setRegistryId(String id) {
        this.registryId = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public ItemRarity getRarity() {
        return rarity;
    }

    public void setRarity(ItemRarity rarity) {
        this.rarity = rarity;
    }

    public boolean isStackable() {
        return stackable;
    }

    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
    }

    /**
     * Gets the current icon frame.
     * If the icon has animation, returns the current animation frame.
     */
    public Bitmap getIcon() {
        if (iconAnimation != null && iconAnimation.isAnimated) {
            long elapsed = System.currentTimeMillis();
            return iconAnimation.getFrame(elapsed);
        }
        return icon;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public float getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(float attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public float getCritChance() {
        return critChance;
    }

    public void setCritChance(float critChance) {
        this.critChance = critChance;
    }

    public float getCritMultiplier() {
        return critMultiplier;
    }

    public void setCritMultiplier(float critMultiplier) {
        this.critMultiplier = critMultiplier;
    }

    public boolean isRangedWeapon() {
        return isRangedWeapon;
    }

    /**
     * Configures this item as a ranged weapon.
     * Uses string-based projectile type names (no ProjectileEntity dependency).
     */
    public void setRangedWeapon(boolean rangedWeapon, String projectileTypeName,
                                 int projectileDamage, float projectileSpeed) {
        this.isRangedWeapon = rangedWeapon;
        this.projectileTypeName = projectileTypeName;
        this.projectileDamage = projectileDamage;
        this.projectileSpeed = projectileSpeed;
    }

    /**
     * Configures this item as a ranged weapon using projectile type constant.
     * Converts int constant to string name for storage (no ProjectileEntity dependency).
     */
    public void setRangedWeapon(boolean rangedWeapon, int projectileType,
                                 int projectileDamage, float projectileSpeed) {
        this.isRangedWeapon = rangedWeapon;
        this.projectileTypeName = "PROJECTILE_" + projectileType;
        this.projectileDamage = projectileDamage;
        this.projectileSpeed = projectileSpeed;
    }

    public String getProjectileTypeName() {
        return projectileTypeName;
    }

    public void setProjectileTypeName(String projectileTypeName) {
        this.projectileTypeName = projectileTypeName;
    }

    public int getProjectileDamage() {
        return projectileDamage;
    }

    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    public int getAmmoCapacity() {
        return ammoCapacity;
    }

    public void setAmmoCapacity(int ammoCapacity) {
        this.ammoCapacity = ammoCapacity;
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }

    public void setCurrentAmmo(int currentAmmo) {
        this.currentAmmo = currentAmmo;
    }

    public String getAmmoItemName() {
        return ammoItemName;
    }

    public void setAmmoItemName(String ammoItemName) {
        this.ammoItemName = ammoItemName;
    }

    // ==================== Charged Shot Getters/Setters ====================

    public boolean isChargeable() {
        return isChargeable;
    }

    /**
     * Configures this weapon for charged shots.
     */
    public void setChargeable(boolean chargeable, float maxChargeTime,
                               int chargeManaCost, float damageMultiplier) {
        this.isChargeable = chargeable;
        this.maxChargeTime = Math.max(1.0f, Math.min(5.0f, maxChargeTime));
        this.chargeManaCost = chargeManaCost;
        this.chargeDamageMultiplier = damageMultiplier;
    }

    public float getMaxChargeTime() {
        return maxChargeTime;
    }

    public void setMaxChargeTime(float maxChargeTime) {
        this.maxChargeTime = Math.max(1.0f, Math.min(5.0f, maxChargeTime));
    }

    public float getMinChargeTime() {
        return minChargeTime;
    }

    public void setMinChargeTime(float minChargeTime) {
        this.minChargeTime = minChargeTime;
    }

    public int getChargeManaCost() {
        return chargeManaCost;
    }

    public void setChargeManaCost(int chargeManaCost) {
        this.chargeManaCost = chargeManaCost;
    }

    public float getChargeDamageMultiplier() {
        return chargeDamageMultiplier;
    }

    public void setChargeDamageMultiplier(float chargeDamageMultiplier) {
        this.chargeDamageMultiplier = chargeDamageMultiplier;
    }

    public float getChargeSpeedMultiplier() {
        return chargeSpeedMultiplier;
    }

    public void setChargeSpeedMultiplier(float chargeSpeedMultiplier) {
        this.chargeSpeedMultiplier = chargeSpeedMultiplier;
    }

    public float getChargeSizeMultiplier() {
        return chargeSizeMultiplier;
    }

    public void setChargeSizeMultiplier(float chargeSizeMultiplier) {
        this.chargeSizeMultiplier = chargeSizeMultiplier;
    }

    /**
     * Calculates the charge percentage based on charge time.
     */
    public float getChargePercent(float chargeTime) {
        if (!isChargeable || maxChargeTime <= 0) return 0;
        return Math.min(1.0f, chargeTime / maxChargeTime);
    }

    /**
     * Calculates the mana cost based on charge level.
     */
    public int getManaCostForCharge(float chargePercent) {
        int baseCost = Math.max(1, chargeManaCost / 5);
        return baseCost + (int) ((chargeManaCost - baseCost) * chargePercent);
    }

    /**
     * Calculates the damage multiplier based on charge level.
     */
    public float getDamageMultiplierForCharge(float chargePercent) {
        return 1.0f + (chargeDamageMultiplier - 1.0f) * chargePercent;
    }

    /**
     * Calculates the speed multiplier based on charge level.
     */
    public float getSpeedMultiplierForCharge(float chargePercent) {
        return 1.0f + (chargeSpeedMultiplier - 1.0f) * chargePercent;
    }

    public boolean isConsumable() {
        return isConsumable;
    }

    public void setConsumable(boolean consumable) {
        this.isConsumable = consumable;
    }

    public int getHealthRestore() {
        return healthRestore;
    }

    public void setHealthRestore(int healthRestore) {
        this.healthRestore = healthRestore;
    }

    public int getManaRestore() {
        return manaRestore;
    }

    public void setManaRestore(int manaRestore) {
        this.manaRestore = manaRestore;
    }

    public int getStaminaRestore() {
        return staminaRestore;
    }

    public void setStaminaRestore(int staminaRestore) {
        this.staminaRestore = staminaRestore;
    }

    public float getConsumeTime() {
        return consumeTime;
    }

    public void setConsumeTime(float consumeTime) {
        this.consumeTime = consumeTime;
    }

    public String getSpecialEffect() {
        return specialEffect;
    }

    public void setSpecialEffect(String specialEffect) {
        this.specialEffect = specialEffect;
    }

    public boolean hasAreaEffect() {
        return hasAreaEffect;
    }

    public void setAreaEffect(boolean hasAreaEffect, int radius) {
        this.hasAreaEffect = hasAreaEffect;
        this.areaEffectRadius = radius;
    }

    public int getAreaEffectRadius() {
        return areaEffectRadius;
    }

    // ==================== Status Effect Properties ====================

    /**
     * Sets the status effect this item applies on hit.
     * Uses string type names (no ProjectileEntity dependency).
     */
    public void setStatusEffect(String effectTypeName, double duration,
                                  int damagePerTick, float damageMultiplier) {
        this.statusEffectTypeName = effectTypeName;
        this.statusEffectDuration = duration;
        this.statusEffectDamagePerTick = damagePerTick;
        this.statusEffectDamageMultiplier = damageMultiplier;
    }

    /**
     * Sets the status effect using status effect int constant.
     * Converts int constant to string name for storage (no ProjectileEntity dependency).
     */
    public void setStatusEffect(int effectType, double duration,
                                  int damagePerTick, float damageMultiplier) {
        this.statusEffectTypeName = "EFFECT_" + effectType;
        this.statusEffectDuration = duration;
        this.statusEffectDamagePerTick = damagePerTick;
        this.statusEffectDamageMultiplier = damageMultiplier;
    }

    public String getStatusEffectTypeName() {
        return statusEffectTypeName;
    }

    public double getStatusEffectDuration() {
        return statusEffectDuration;
    }

    public int getStatusEffectDamagePerTick() {
        return statusEffectDamagePerTick;
    }

    public float getStatusEffectDamageMultiplier() {
        return statusEffectDamageMultiplier;
    }

    public boolean hasStatusEffect() {
        return !"NONE".equals(statusEffectTypeName);
    }

    // ==================== Ability Scaling Getters/Setters ====================

    public boolean scalesWithStrength() {
        return scalesWithStrength;
    }

    public void setScalesWithStrength(boolean scales) {
        this.scalesWithStrength = scales;
    }

    public boolean scalesWithDexterity() {
        return scalesWithDexterity;
    }

    public void setScalesWithDexterity(boolean scales) {
        this.scalesWithDexterity = scales;
    }

    public boolean scalesWithIntelligence() {
        return scalesWithIntelligence;
    }

    public void setScalesWithIntelligence(boolean scales) {
        this.scalesWithIntelligence = scales;
    }

    public int getWisdomRequirement() {
        return wisdomRequirement;
    }

    public void setWisdomRequirement(int requirement) {
        this.wisdomRequirement = Math.max(0, requirement);
    }

    public boolean requiresWisdom() {
        return wisdomRequirement > 0;
    }

    public boolean hasAbilityScaling() {
        return scalesWithStrength || scalesWithDexterity ||
               scalesWithIntelligence || wisdomRequirement > 0;
    }

    /**
     * Gets a formatted string of ability tags for this item.
     */
    public String getAbilityTags() {
        StringBuilder tags = new StringBuilder();
        if (scalesWithStrength) {
            if (tags.length() > 0) tags.append(", ");
            tags.append("[STR]");
        }
        if (scalesWithDexterity) {
            if (tags.length() > 0) tags.append(", ");
            tags.append("[DEX]");
        }
        if (scalesWithIntelligence) {
            if (tags.length() > 0) tags.append(", ");
            tags.append("[INT]");
        }
        if (wisdomRequirement > 0) {
            if (tags.length() > 0) tags.append(", ");
            tags.append("[WIS:" + wisdomRequirement + "]");
        }
        return tags.toString();
    }

    /**
     * Gets a formatted tooltip string for inventory display.
     */
    public String getTooltip() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n");
        sb.append(rarity.getDisplayName()).append(" ").append(category.name()).append("\n");

        if (damage > 0) sb.append("Damage: ").append(damage).append("\n");
        if (defense > 0) sb.append("Defense: ").append(defense).append("\n");
        if (isRangedWeapon) {
            sb.append("Projectile Damage: ").append(projectileDamage).append("\n");
            sb.append("Range: ").append((int) (projectileSpeed * 10)).append("\n");
        }
        if (isConsumable) {
            if (healthRestore > 0) sb.append("Restores ").append(healthRestore).append(" Health\n");
            if (manaRestore > 0) sb.append("Restores ").append(manaRestore).append(" Mana\n");
            if (staminaRestore > 0) sb.append("Restores ").append(staminaRestore).append(" Stamina\n");
        }
        if (specialEffect != null && !specialEffect.isEmpty()) {
            sb.append("Effect: ").append(specialEffect).append("\n");
        }
        if (hasAbilityScaling()) {
            sb.append("Scales: ").append(getAbilityTags()).append("\n");
        }
        if (description != null && !description.isEmpty()) {
            sb.append("\n").append(description);
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", category=" + category +
                ", rarity=" + rarity +
                "}";
    }
}
