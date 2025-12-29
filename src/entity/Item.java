package entity;

import animation.*;
import graphics.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Item represents a game item with properties, categories, and visual representation.
 * Items can be held by entities and rendered as overlays on their sprites.
 *
 * Features:
 * - Multiple item categories (weapon, armor, tool, food, etc.)
 * - Rarity system with color coding
 * - Properties like damage, defense, range, special effects
 * - Animated held item overlays that sync with entity animations
 * - Projectile firing capabilities for ranged weapons
 *
 * Usage:
 *   Item sword = new Item("Iron Sword", ItemCategory.WEAPON);
 *   sword.setDamage(15);
 *   sword.setRarity(ItemRarity.UNCOMMON);
 *   sword.loadHeldAnimation(ActionState.IDLE, "assets/items/sword/idle.gif");
 */
public class Item {

    // ==================== Item Categories ====================

    /**
     * Categories for items determining their primary use.
     */
    public enum ItemCategory {
        WEAPON,         // Swords, axes, maces
        RANGED_WEAPON,  // Bows, crossbows, wands
        TOOL,           // Pickaxes, shovels, axes
        ARMOR,          // Helmets, chestplates, etc.
        CLOTHING,       // Cosmetic clothing
        BLOCK,          // Placeable blocks
        FOOD,           // Consumable food items
        POTION,         // Consumable potions
        MATERIAL,       // Crafting materials
        KEY,            // Keys for doors/chests
        ACCESSORY,      // Rings, amulets
        THROWABLE,      // Throwing items
        OTHER           // Miscellaneous
    }

    /**
     * Rarity tiers with associated colors.
     */
    public enum ItemRarity {
        COMMON(Color.WHITE, "Common"),
        UNCOMMON(new Color(30, 255, 30), "Uncommon"),
        RARE(new Color(30, 100, 255), "Rare"),
        EPIC(new Color(180, 30, 255), "Epic"),
        LEGENDARY(new Color(255, 165, 0), "Legendary"),
        MYTHIC(new Color(0, 255, 255), "Mythic");

        private final Color color;
        private final String displayName;

        ItemRarity(Color color, String displayName) {
            this.color = color;
            this.displayName = displayName;
        }

        public Color getColor() {
            return color;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ==================== Core Properties ====================

    private String name;
    private String description;
    private ItemCategory category;
    private ItemRarity rarity;
    private boolean stackable;
    private int maxStackSize;

    // Item icon for inventory display
    private BufferedImage icon;
    private AnimatedTexture iconAnimation;

    // Held item animations (for when item is equipped and held)
    private Map<SpriteAnimation.ActionState, AnimatedTexture> heldAnimations;

    // ==================== Combat Properties ====================

    private int damage = 0;
    private int defense = 0;
    private float attackSpeed = 1.0f;    // Attacks per second
    private int range = 60;              // Attack/effect range in pixels
    private float critChance = 0.05f;    // 5% default crit chance
    private float critMultiplier = 1.5f; // 150% damage on crit

    // ==================== Ranged Weapon Properties ====================

    private boolean isRangedWeapon = false;
    private ProjectileEntity.ProjectileType projectileType;
    private int projectileDamage = 0;
    private float projectileSpeed = 15.0f;
    private int ammoCapacity = 1;
    private int currentAmmo = 0;
    private String ammoItemName;  // Required ammo item name

    // ==================== Charged Shot Properties ====================

    private boolean isChargeable = false;         // Can this weapon charge shots?
    private float maxChargeTime = 2.0f;           // Maximum charge time in seconds (1-5)
    private float minChargeTime = 0.3f;           // Minimum charge for a valid shot
    private int chargeManaCost = 20;              // Mana cost at full charge
    private float chargeDamageMultiplier = 3.0f;  // Damage multiplier at full charge (1x to 3x)
    private float chargeSpeedMultiplier = 1.5f;   // Speed multiplier at full charge
    private float chargeSizeMultiplier = 2.0f;    // Projectile size at full charge

    // ==================== Consumable Properties ====================

    private boolean isConsumable = false;
    private int healthRestore = 0;
    private int manaRestore = 0;
    private int staminaRestore = 0;
    private float consumeTime = 1.0f;  // Seconds to consume

    // ==================== Special Effects ====================

    private String specialEffect;  // Description of special effect
    private boolean hasAreaEffect = false;
    private int areaEffectRadius = 0;

    // Status effect properties (for special ammo like fire/ice arrows)
    private ProjectileEntity.StatusEffectType statusEffectType = ProjectileEntity.StatusEffectType.NONE;
    private double statusEffectDuration = 0;     // Duration in seconds
    private int statusEffectDamagePerTick = 0;   // Damage per tick
    private float statusEffectDamageMultiplier = 1.0f;  // Impact damage multiplier

    // ==================== Constructors ====================

    /**
     * Creates a new item with a name and category.
     */
    public Item(String name, ItemCategory category) {
        this.name = name;
        this.category = category;
        this.rarity = ItemRarity.COMMON;
        this.description = "";
        this.heldAnimations = new HashMap<>();

        // Set defaults based on category
        switch (category) {
            case WEAPON:
            case TOOL:
                this.stackable = false;
                this.maxStackSize = 1;
                break;
            case RANGED_WEAPON:
                this.stackable = false;
                this.maxStackSize = 1;
                this.isRangedWeapon = true;
                break;
            case FOOD:
            case POTION:
                this.stackable = true;
                this.maxStackSize = 16;
                this.isConsumable = true;
                break;
            case MATERIAL:
                this.stackable = true;
                this.maxStackSize = 64;
                break;
            case BLOCK:
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
        this.icon = original.icon;
        this.iconAnimation = original.iconAnimation;
        this.heldAnimations = new HashMap<>(original.heldAnimations);
        this.damage = original.damage;
        this.defense = original.defense;
        this.attackSpeed = original.attackSpeed;
        this.range = original.range;
        this.critChance = original.critChance;
        this.critMultiplier = original.critMultiplier;
        this.isRangedWeapon = original.isRangedWeapon;
        this.projectileType = original.projectileType;
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
        this.statusEffectType = original.statusEffectType;
        this.statusEffectDuration = original.statusEffectDuration;
        this.statusEffectDamagePerTick = original.statusEffectDamagePerTick;
        this.statusEffectDamageMultiplier = original.statusEffectDamageMultiplier;
    }

    // ==================== Animation Loading ====================

    /**
     * Loads the item icon from a file.
     */
    public void loadIcon(String path) {
        try {
            AssetLoader.ImageAsset asset = AssetLoader.load(path);
            if (asset.animatedTexture != null) {
                this.iconAnimation = asset.animatedTexture;
                this.icon = asset.animatedTexture.getCurrentFrame();
            } else if (asset.staticImage != null) {
                this.icon = asset.staticImage;
            }
        } catch (Exception e) {
            System.err.println("Item: Failed to load icon: " + path);
        }
    }

    /**
     * Loads a held animation for a specific action state.
     * Held animations are drawn over the entity when the item is equipped.
     */
    public void loadHeldAnimation(SpriteAnimation.ActionState state, String path) {
        try {
            AssetLoader.ImageAsset asset = AssetLoader.load(path);
            if (asset.animatedTexture != null) {
                heldAnimations.put(state, asset.animatedTexture);
            } else if (asset.staticImage != null) {
                AnimatedTexture singleFrame = new AnimatedTexture(asset.staticImage);
                heldAnimations.put(state, singleFrame);
            }
        } catch (Exception e) {
            System.err.println("Item: Failed to load held animation: " + path);
        }
    }

    /**
     * Loads all held animations from a directory.
     * Expects files named: idle.gif, walk.gif, run.gif, attack.gif, fire.gif, etc.
     */
    public void loadHeldAnimationsFromDir(String dir) {
        String basePath = dir.endsWith("/") ? dir : dir + "/";

        // Map of file names to action states
        Map<String, SpriteAnimation.ActionState> stateMap = new HashMap<>();
        stateMap.put("idle", SpriteAnimation.ActionState.IDLE);
        stateMap.put("walk", SpriteAnimation.ActionState.WALK);
        stateMap.put("run", SpriteAnimation.ActionState.RUN);
        stateMap.put("sprint", SpriteAnimation.ActionState.SPRINT);
        stateMap.put("jump", SpriteAnimation.ActionState.JUMP);
        stateMap.put("double_jump", SpriteAnimation.ActionState.DOUBLE_JUMP);
        stateMap.put("triple_jump", SpriteAnimation.ActionState.TRIPLE_JUMP);
        stateMap.put("fall", SpriteAnimation.ActionState.FALL);
        stateMap.put("attack", SpriteAnimation.ActionState.ATTACK);
        stateMap.put("fire", SpriteAnimation.ActionState.FIRE);
        stateMap.put("use_item", SpriteAnimation.ActionState.USE_ITEM);
        stateMap.put("eat", SpriteAnimation.ActionState.EAT);
        stateMap.put("hurt", SpriteAnimation.ActionState.HURT);
        stateMap.put("block", SpriteAnimation.ActionState.BLOCK);
        stateMap.put("cast", SpriteAnimation.ActionState.CAST);

        for (Map.Entry<String, SpriteAnimation.ActionState> entry : stateMap.entrySet()) {
            String path = basePath + entry.getKey() + ".gif";
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                loadHeldAnimation(entry.getValue(), path);
            }
        }
    }

    /**
     * Gets the held animation for a specific action state.
     * Falls back to IDLE if the specific state doesn't exist.
     */
    public AnimatedTexture getHeldAnimation(SpriteAnimation.ActionState state) {
        AnimatedTexture anim = heldAnimations.get(state);
        if (anim == null) {
            anim = heldAnimations.get(SpriteAnimation.ActionState.IDLE);
        }
        return anim;
    }

    /**
     * Checks if this item has a held animation for the given state.
     */
    public boolean hasHeldAnimation(SpriteAnimation.ActionState state) {
        return heldAnimations.containsKey(state);
    }

    /**
     * Syncs all held animations to a specific frame index.
     */
    public void syncHeldAnimations(int frameIndex, SpriteAnimation.ActionState state) {
        AnimatedTexture anim = getHeldAnimation(state);
        if (anim != null && anim.getFrameCount() > 0) {
            int mappedFrame = frameIndex % anim.getFrameCount();
            anim.setCurrentFrameIndex(mappedFrame);
        }
    }

    /**
     * Draws the held item overlay.
     */
    public void drawHeld(Graphics g, int x, int y, int width, int height,
                         boolean facingRight, SpriteAnimation.ActionState state) {
        AnimatedTexture anim = getHeldAnimation(state);
        if (anim == null) return;

        BufferedImage frame = anim.getCurrentFrame();
        if (frame == null) return;

        Graphics2D g2d = (Graphics2D) g;
        if (facingRight) {
            g2d.drawImage(frame, x, y, width, height, null);
        } else {
            g2d.drawImage(frame, x + width, y, -width, height, null);
        }
    }

    // ==================== Projectile Firing ====================

    /**
     * Creates a projectile fired from this weapon.
     *
     * @param x Starting X position
     * @param y Starting Y position
     * @param dirX Direction X (-1 to 1)
     * @param dirY Direction Y (-1 to 1)
     * @param fromPlayer True if fired by player
     * @return The created projectile, or null if not a ranged weapon
     */
    public ProjectileEntity createProjectile(int x, int y, double dirX, double dirY, boolean fromPlayer) {
        if (!isRangedWeapon || projectileType == null) {
            return null;
        }

        // Calculate velocity
        double velX = dirX * projectileSpeed;
        double velY = dirY * projectileSpeed;

        ProjectileEntity projectile = new ProjectileEntity(
            x, y, projectileType, projectileDamage, velX, velY, fromPlayer
        );

        // Apply status effect if this item has one
        if (statusEffectType != ProjectileEntity.StatusEffectType.NONE) {
            projectile.setStatusEffect(statusEffectType, statusEffectDuration,
                                        statusEffectDamagePerTick, statusEffectDamageMultiplier);
        }

        return projectile;
    }

    // ==================== Getters and Setters ====================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public BufferedImage getIcon() {
        if (iconAnimation != null) {
            return iconAnimation.getCurrentFrame();
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

    public void setRangedWeapon(boolean rangedWeapon, ProjectileEntity.ProjectileType projectileType,
                                 int projectileDamage, float projectileSpeed) {
        this.isRangedWeapon = rangedWeapon;
        this.projectileType = projectileType;
        this.projectileDamage = projectileDamage;
        this.projectileSpeed = projectileSpeed;
    }

    public ProjectileEntity.ProjectileType getProjectileType() {
        return projectileType;
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
     *
     * @param chargeable Whether this weapon can charge
     * @param maxChargeTime Maximum charge time in seconds (1-5)
     * @param chargeManaCost Mana cost at full charge
     * @param damageMultiplier Damage multiplier at full charge (e.g., 3.0 = 3x damage)
     */
    public void setChargeable(boolean chargeable, float maxChargeTime, int chargeManaCost, float damageMultiplier) {
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
     *
     * @param chargeTime Current charge time in seconds
     * @return Charge percentage from 0.0 to 1.0
     */
    public float getChargePercent(float chargeTime) {
        if (!isChargeable || maxChargeTime <= 0) return 0;
        return Math.min(1.0f, chargeTime / maxChargeTime);
    }

    /**
     * Calculates the mana cost based on charge level.
     * Scales linearly from base mana cost to full charge cost.
     *
     * @param chargePercent Charge percentage (0.0 to 1.0)
     * @return Mana cost for this charge level
     */
    public int getManaCostForCharge(float chargePercent) {
        // Base cost is 20% of full charge cost, scales up to full
        int baseCost = Math.max(1, chargeManaCost / 5);
        return baseCost + (int)((chargeManaCost - baseCost) * chargePercent);
    }

    /**
     * Calculates the damage multiplier based on charge level.
     *
     * @param chargePercent Charge percentage (0.0 to 1.0)
     * @return Damage multiplier (1.0 to chargeDamageMultiplier)
     */
    public float getDamageMultiplierForCharge(float chargePercent) {
        return 1.0f + (chargeDamageMultiplier - 1.0f) * chargePercent;
    }

    /**
     * Calculates the speed multiplier based on charge level.
     *
     * @param chargePercent Charge percentage (0.0 to 1.0)
     * @return Speed multiplier (1.0 to chargeSpeedMultiplier)
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
     *
     * @param effectType Type of status effect (BURNING, FROZEN, POISONED)
     * @param duration Duration in seconds
     * @param damagePerTick Damage per tick for DoT effects
     * @param damageMultiplier Multiplier for impact damage
     */
    public void setStatusEffect(ProjectileEntity.StatusEffectType effectType, double duration,
                                  int damagePerTick, float damageMultiplier) {
        this.statusEffectType = effectType;
        this.statusEffectDuration = duration;
        this.statusEffectDamagePerTick = damagePerTick;
        this.statusEffectDamageMultiplier = damageMultiplier;
    }

    public ProjectileEntity.StatusEffectType getStatusEffectType() {
        return statusEffectType;
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
        return statusEffectType != ProjectileEntity.StatusEffectType.NONE;
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
            sb.append("Range: ").append((int)(projectileSpeed * 10)).append("\n");
        }
        if (isConsumable) {
            if (healthRestore > 0) sb.append("Restores ").append(healthRestore).append(" Health\n");
            if (manaRestore > 0) sb.append("Restores ").append(manaRestore).append(" Mana\n");
            if (staminaRestore > 0) sb.append("Restores ").append(staminaRestore).append(" Stamina\n");
        }
        if (specialEffect != null && !specialEffect.isEmpty()) {
            sb.append("Effect: ").append(specialEffect).append("\n");
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
