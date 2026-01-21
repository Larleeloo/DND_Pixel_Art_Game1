package entity.item;

import animation.*;
import animation.ItemAnimationState;
import entity.ProjectileEntity;
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
    private String texturePath;  // Path to the GIF texture file

    // Held item animations (for when item is equipped and held)
    private Map<SpriteAnimation.ActionState, AnimatedTexture> heldAnimations;

    // Triggered animation system (new folder-based structure)
    private String animationFolderPath;  // Path to folder containing animation states
    private Map<ItemAnimationState, AnimatedTexture> triggeredAnimations;
    private ItemAnimationState currentTriggeredState = ItemAnimationState.IDLE;

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

    // ==================== Ability Scaling Properties ====================

    // These flags indicate which ability scores affect this item's usage/damage
    private boolean scalesWithStrength = false;     // Melee weapons - STR affects damage (+/-17%)
    private boolean scalesWithDexterity = false;    // Dexterity items - chance for double use or miss
    private boolean scalesWithIntelligence = false; // Magical items - INT affects damage (+/-15%)
    private int wisdomRequirement = 0;              // Ancient artifacts - minimum WIS to use (0 = no requirement)

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
        this.triggeredAnimations = new HashMap<>();

        // Set defaults based on category
        // Default stack size is 16 for all stackable items (except blocks which stack to 64)
        switch (category) {
            case WEAPON:
            case TOOL:
            case ARMOR:
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
            case KEY:
            case ACCESSORY:
            case THROWABLE:
            case CLOTHING:
            case OTHER:
                this.stackable = true;
                this.maxStackSize = 16;
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
        this.texturePath = original.texturePath;
        this.heldAnimations = new HashMap<>(original.heldAnimations);
        this.animationFolderPath = original.animationFolderPath;
        this.triggeredAnimations = new HashMap<>(original.triggeredAnimations);
        this.currentTriggeredState = original.currentTriggeredState;
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
        this.scalesWithStrength = original.scalesWithStrength;
        this.scalesWithDexterity = original.scalesWithDexterity;
        this.scalesWithIntelligence = original.scalesWithIntelligence;
        this.wisdomRequirement = original.wisdomRequirement;
    }

    /**
     * Creates a copy of this item.
     * Subclasses should override this to return their specific type.
     *
     * @return A new Item instance with the same properties
     */
    public Item copy() {
        return new Item(this);
    }

    // ==================== Animation Loading ====================

    /**
     * Loads the item icon from a file.
     */
    public void loadIcon(String path) {
        this.texturePath = path;  // Store the path
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
     * Supports variable texture sizes (16x16, 32x32, etc.) - textures are scaled
     * to the specified display size while preserving pixel art quality.
     */
    public void drawHeld(Graphics g, int x, int y, int width, int height,
                         boolean facingRight, SpriteAnimation.ActionState state) {
        AnimatedTexture anim = getHeldAnimation(state);
        if (anim == null) return;

        BufferedImage frame = anim.getCurrentFrame();
        if (frame == null) return;

        Graphics2D g2d = (Graphics2D) g;
        // Use nearest-neighbor interpolation to preserve pixel art quality
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                            java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        if (facingRight) {
            g2d.drawImage(frame, x, y, width, height, null);
        } else {
            g2d.drawImage(frame, x + width, y, -width, height, null);
        }
    }

    // ==================== Triggered Animation System ====================

    /**
     * Sets the animation folder path for this item.
     * Animation folders contain multiple GIF files for different states.
     *
     * @param folderPath Path to the animation folder (e.g., "assets/items/bow/")
     */
    public void setAnimationFolderPath(String folderPath) {
        this.animationFolderPath = folderPath;
    }

    /**
     * Gets the animation folder path for this item.
     *
     * @return Folder path, or null if using legacy single-file texture
     */
    public String getAnimationFolderPath() {
        return animationFolderPath;
    }

    /**
     * Checks if this item has an animation folder with multiple states.
     *
     * @return true if the item uses folder-based animations
     */
    public boolean hasAnimationFolder() {
        return animationFolderPath != null;
    }

    /**
     * Loads a triggered animation for a specific state.
     *
     * @param state The animation state
     * @param path Path to the animation GIF
     */
    public void loadTriggeredAnimation(ItemAnimationState state, String path) {
        try {
            AssetLoader.ImageAsset asset = AssetLoader.load(path);
            if (asset.animatedTexture != null) {
                triggeredAnimations.put(state, asset.animatedTexture);
            } else if (asset.staticImage != null) {
                AnimatedTexture singleFrame = new AnimatedTexture(asset.staticImage);
                triggeredAnimations.put(state, singleFrame);
            }
        } catch (Exception e) {
            System.err.println("Item: Failed to load triggered animation: " + path);
        }
    }

    /**
     * Gets the triggered animation for a specific state.
     *
     * @param state The animation state
     * @return The AnimatedTexture, or null if not available
     */
    public AnimatedTexture getTriggeredAnimation(ItemAnimationState state) {
        AnimatedTexture anim = triggeredAnimations.get(state);
        if (anim == null && state != ItemAnimationState.IDLE) {
            // Fallback to idle if specific state not found
            anim = triggeredAnimations.get(ItemAnimationState.IDLE);
        }
        return anim;
    }

    /**
     * Checks if this item has a triggered animation for the given state.
     *
     * @param state The animation state
     * @return true if the animation exists
     */
    public boolean hasTriggeredAnimation(ItemAnimationState state) {
        return triggeredAnimations.containsKey(state);
    }

    /**
     * Gets all loaded triggered animations.
     *
     * @return Map of states to animations
     */
    public Map<ItemAnimationState, AnimatedTexture> getTriggeredAnimations() {
        return triggeredAnimations;
    }

    /**
     * Sets the current triggered animation state.
     * This controls which animation is currently playing.
     *
     * @param state The animation state to set
     */
    public void setTriggeredAnimationState(ItemAnimationState state) {
        if (state != currentTriggeredState) {
            currentTriggeredState = state;
            AnimatedTexture anim = getTriggeredAnimation(state);
            if (anim != null) {
                anim.reset();
            }
        }
    }

    /**
     * Gets the current triggered animation state.
     *
     * @return The current state
     */
    public ItemAnimationState getTriggeredAnimationState() {
        return currentTriggeredState;
    }

    /**
     * Updates the current triggered animation.
     *
     * @param deltaMs Milliseconds since last update
     */
    public void updateTriggeredAnimation(long deltaMs) {
        AnimatedTexture anim = getTriggeredAnimation(currentTriggeredState);
        if (anim != null) {
            anim.update(deltaMs);
        }
    }

    /**
     * Draws the current triggered animation.
     * Supports variable texture sizes (16x16, 32x32, etc.) - textures are scaled
     * to the specified display size while preserving pixel art quality.
     *
     * @param g Graphics context
     * @param x X position
     * @param y Y position
     * @param width Render width
     * @param height Render height
     * @param facingRight Direction facing
     */
    public void drawTriggeredAnimation(Graphics g, int x, int y, int width, int height, boolean facingRight) {
        AnimatedTexture anim = getTriggeredAnimation(currentTriggeredState);
        if (anim == null) return;

        BufferedImage frame = anim.getCurrentFrame();
        if (frame == null) return;

        Graphics2D g2d = (Graphics2D) g;
        // Use nearest-neighbor interpolation to preserve pixel art quality
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                            java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        if (facingRight) {
            g2d.drawImage(frame, x, y, width, height, null);
        } else {
            g2d.drawImage(frame, x + width, y, -width, height, null);
        }
    }

    /**
     * Gets the appropriate triggered animation state based on entity action.
     *
     * @param entityAction The entity's current action
     * @return The appropriate item animation state
     */
    public ItemAnimationState getAppropriateTriggeredState(SpriteAnimation.ActionState entityAction) {
        // Map entity actions to item animation states
        ItemAnimationState mapped = ItemAnimationState.fromEntityAction(entityAction);

        // For ranged weapons, check for charging vs firing
        if (isRangedWeapon && entityAction == SpriteAnimation.ActionState.FIRE) {
            boolean isMagic = projectileType == ProjectileEntity.ProjectileType.MAGIC_BOLT ||
                              projectileType == ProjectileEntity.ProjectileType.FIREBALL ||
                              projectileType == ProjectileEntity.ProjectileType.ICEBALL;

            if (isChargeable) {
                // During charge, use DRAW for bows or CHARGE for magic
                return ItemAnimationState.getChargingState(true, isMagic);
            } else {
                return ItemAnimationState.getFiringState(true, isMagic);
            }
        }

        return mapped;
    }

    /**
     * Triggers the appropriate item animation based on entity state.
     *
     * @param entityAction The entity's current action
     * @param isCharging Whether the weapon is currently charging
     * @param chargePercent Charge percentage (0.0 to 1.0)
     */
    public void triggerAnimationForAction(SpriteAnimation.ActionState entityAction,
                                           boolean isCharging, float chargePercent) {
        ItemAnimationState newState;

        if (isCharging && isChargeable) {
            // Determine if magic or physical ranged
            boolean isMagic = projectileType == ProjectileEntity.ProjectileType.MAGIC_BOLT ||
                              projectileType == ProjectileEntity.ProjectileType.FIREBALL ||
                              projectileType == ProjectileEntity.ProjectileType.ICEBALL;
            newState = ItemAnimationState.getChargingState(isRangedWeapon, isMagic);
        } else {
            newState = getAppropriateTriggeredState(entityAction);
        }

        setTriggeredAnimationState(newState);
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

    // ==================== Ability Scaling Getters/Setters ====================

    /**
     * Checks if this item's damage scales with Strength.
     * Melee weapons typically scale with strength (+/-17% per point).
     */
    public boolean scalesWithStrength() {
        return scalesWithStrength;
    }

    /**
     * Sets whether this item scales with Strength.
     */
    public void setScalesWithStrength(boolean scales) {
        this.scalesWithStrength = scales;
    }

    /**
     * Checks if this item's usage scales with Dexterity.
     * Dexterity items have a chance for double use (above baseline) or miss (below baseline).
     */
    public boolean scalesWithDexterity() {
        return scalesWithDexterity;
    }

    /**
     * Sets whether this item scales with Dexterity.
     */
    public void setScalesWithDexterity(boolean scales) {
        this.scalesWithDexterity = scales;
    }

    /**
     * Checks if this item's damage scales with Intelligence.
     * Magical items typically scale with intelligence (+/-15% per point).
     */
    public boolean scalesWithIntelligence() {
        return scalesWithIntelligence;
    }

    /**
     * Sets whether this item scales with Intelligence.
     */
    public void setScalesWithIntelligence(boolean scales) {
        this.scalesWithIntelligence = scales;
    }

    /**
     * Gets the minimum Wisdom required to use this item.
     * Returns 0 if no wisdom requirement.
     */
    public int getWisdomRequirement() {
        return wisdomRequirement;
    }

    /**
     * Sets the minimum Wisdom required to use this item.
     * Set to 0 for no requirement.
     */
    public void setWisdomRequirement(int requirement) {
        this.wisdomRequirement = Math.max(0, requirement);
    }

    /**
     * Checks if this item requires wisdom to use.
     */
    public boolean requiresWisdom() {
        return wisdomRequirement > 0;
    }

    /**
     * Checks if this item has any ability scaling.
     */
    public boolean hasAbilityScaling() {
        return scalesWithStrength || scalesWithDexterity || scalesWithIntelligence || wisdomRequirement > 0;
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
        // Add ability scaling tags
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
