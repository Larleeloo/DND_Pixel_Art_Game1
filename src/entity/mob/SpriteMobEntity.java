package entity.mob;
import entity.*;
import entity.player.*;
import block.*;
import animation.*;
import graphics.*;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Base class for sprite-based AI-controlled mobs.
 * Uses SpriteAnimation for GIF-based animations instead of bone-based Skeleton.
 *
 * This is an alternative to the bone-based MobEntity that supports:
 * - GIF animations for idle, walk, run, attack, hurt, death actions
 * - Proper hitbox collision detection
 * - AI state machine for mob behavior
 *
 * Expected sprite files in mob directory:
 *   - idle.gif (standing/breathing animation)
 *   - walk.gif (walking animation)
 *   - run.gif (optional, running/chasing animation)
 *   - attack.gif (optional, attack animation)
 *   - hurt.gif (optional, damage reaction)
 *   - death.gif (optional, death animation)
 *
 * Usage in level JSON:
 *   "mobType": "sprite_wolf",
 *   "spriteDir": "assets/mobs/wolf/sprites"
 */
public class SpriteMobEntity extends MobEntity {

    // Sprite animation system
    protected SpriteAnimation spriteAnimation;
    protected String spriteDir;

    // Animation state
    protected String currentAnimState = "idle";
    protected long lastUpdateTime;

    // Visual dimensions (from sprite)
    protected int spriteWidth;
    protected int spriteHeight;
    protected static final int SCALE = 2;

    // Mob type for different body shapes
    public enum MobBodyType {
        HUMANOID,   // 32x64 base (64x128 scaled) - bipedal creatures
        QUADRUPED,  // 64x64 base (128x128 scaled) - four-legged creatures
        SMALL,      // 16x16 base (32x32 scaled) - tiny creatures
        LARGE       // 64x96 base (128x192 scaled) - large creatures
    }
    protected MobBodyType bodyType = MobBodyType.HUMANOID;

    // Multi-jump system
    protected int maxJumps = 1;           // Default to single jump
    protected int jumpsRemaining = 1;
    protected int currentJumpNumber = 0;
    protected double doubleJumpStrength = -8;
    protected double tripleJumpStrength = -7;

    // Sprint system
    protected boolean isSprinting = false;
    protected double sprintSpeed = 0;      // Set by subclasses

    // Ranged attack system
    protected boolean canFireProjectiles = false;
    protected ProjectileEntity.ProjectileType projectileType;
    protected int projectileDamage = 5;
    protected double projectileSpeed = 12.0;
    protected double projectileCooldown = 2.0;
    protected double projectileTimer = 0;
    protected double preferredAttackRange = 200; // Range to start firing
    protected List<ProjectileEntity> activeProjectiles = new ArrayList<>();

    // Eating animation (for herbivore mobs)
    protected boolean isEating = false;
    protected double eatTimer = 0;

    // Mob inventory and equipment system
    protected List<Item> inventory = new ArrayList<>();
    protected int maxInventorySize = 5;
    protected Item equippedWeapon = null;
    protected Item equippedArmor = null;
    protected EquipmentOverlay equipmentOverlay;
    protected boolean isHumanoid = false;  // Whether this mob can equip humanoid items

    /**
     * Creates a sprite-based mob entity.
     *
     * @param x Initial X position
     * @param y Initial Y position
     * @param spriteDir Directory containing animation GIFs
     */
    public SpriteMobEntity(int x, int y, String spriteDir) {
        super(x, y);
        this.spriteDir = spriteDir;

        // Detect body type from directory name first
        detectBodyTypeFromDir(spriteDir);

        // Initialize sprite animation
        this.spriteAnimation = new SpriteAnimation();
        loadAnimations(spriteDir);

        // Set dimensions based on body type (if sprite loading failed or has wrong dimensions)
        int baseWidth = spriteAnimation.getBaseWidth();
        int baseHeight = spriteAnimation.getBaseHeight();

        // If sprite loaded with valid dimensions, use them; otherwise use body type defaults
        if (baseWidth > 0 && baseHeight > 0) {
            this.spriteWidth = baseWidth * SCALE;
            this.spriteHeight = baseHeight * SCALE;
        } else {
            // Use body type defaults
            applyBodyTypeDimensions();
        }

        // Set hitbox based on sprite size (slightly smaller for better gameplay feel)
        this.hitboxWidth = (int)(spriteWidth * 0.8);
        this.hitboxHeight = (int)(spriteHeight * 0.9);
        this.hitboxOffsetX = -hitboxWidth / 2;
        this.hitboxOffsetY = -hitboxHeight;

        // Larger sprites need larger attack range
        this.attackRange = Math.max(60, spriteWidth);

        // Initialize equipment overlay for humanoid mobs
        this.equipmentOverlay = new EquipmentOverlay();

        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Detects body type from sprite directory name.
     * Looks for keywords like "quadruped", "wolf", "horse", etc.
     */
    private void detectBodyTypeFromDir(String dir) {
        if (dir == null) {
            bodyType = MobBodyType.HUMANOID;
            isHumanoid = true;
            return;
        }

        String lowerDir = dir.toLowerCase();

        // Quadruped animals
        if (lowerDir.contains("quadruped") || lowerDir.contains("wolf") ||
            lowerDir.contains("dog") || lowerDir.contains("cat") ||
            lowerDir.contains("horse") || lowerDir.contains("cow") ||
            lowerDir.contains("pig") || lowerDir.contains("sheep") ||
            lowerDir.contains("bear") || lowerDir.contains("deer") ||
            lowerDir.contains("fox") || lowerDir.contains("lion") ||
            lowerDir.contains("tiger") || lowerDir.contains("spider")) {
            bodyType = MobBodyType.QUADRUPED;
            isHumanoid = false;
        }
        // Small creatures
        else if (lowerDir.contains("slime") || lowerDir.contains("bug") ||
                 lowerDir.contains("bat") || lowerDir.contains("small") ||
                 lowerDir.contains("tiny") || lowerDir.contains("rat")) {
            bodyType = MobBodyType.SMALL;
            isHumanoid = false;
        }
        // Large creatures
        else if (lowerDir.contains("giant") || lowerDir.contains("boss") ||
                 lowerDir.contains("dragon") || lowerDir.contains("large") ||
                 lowerDir.contains("ogre") || lowerDir.contains("troll")) {
            bodyType = MobBodyType.LARGE;
            isHumanoid = false;
        }
        // Default to humanoid
        else {
            bodyType = MobBodyType.HUMANOID;
            isHumanoid = true;
        }
    }

    /**
     * Applies dimensions based on body type.
     */
    private void applyBodyTypeDimensions() {
        switch (bodyType) {
            case QUADRUPED:
                this.spriteWidth = 64 * SCALE;
                this.spriteHeight = 64 * SCALE;
                break;
            case SMALL:
                this.spriteWidth = 16 * SCALE;
                this.spriteHeight = 16 * SCALE;
                break;
            case LARGE:
                this.spriteWidth = 64 * SCALE;
                this.spriteHeight = 96 * SCALE;
                break;
            case HUMANOID:
            default:
                this.spriteWidth = 32 * SCALE;
                this.spriteHeight = 64 * SCALE;
                break;
        }
    }

    /**
     * Loads animations from the sprite directory.
     *
     * @param dir Directory containing animation GIFs
     */
    protected void loadAnimations(String dir) {
        // Map of file names to action states (extended for new animations)
        java.util.Map<String, SpriteAnimation.ActionState> actionMap = new java.util.HashMap<>();
        actionMap.put("idle", SpriteAnimation.ActionState.IDLE);
        actionMap.put("walk", SpriteAnimation.ActionState.WALK);
        actionMap.put("run", SpriteAnimation.ActionState.RUN);
        actionMap.put("sprint", SpriteAnimation.ActionState.SPRINT);
        actionMap.put("jump", SpriteAnimation.ActionState.JUMP);
        actionMap.put("double_jump", SpriteAnimation.ActionState.DOUBLE_JUMP);
        actionMap.put("triple_jump", SpriteAnimation.ActionState.TRIPLE_JUMP);
        actionMap.put("fall", SpriteAnimation.ActionState.FALL);
        actionMap.put("attack", SpriteAnimation.ActionState.ATTACK);
        actionMap.put("fire", SpriteAnimation.ActionState.FIRE);
        actionMap.put("cast", SpriteAnimation.ActionState.CAST);
        actionMap.put("eat", SpriteAnimation.ActionState.EAT);
        actionMap.put("hurt", SpriteAnimation.ActionState.HURT);
        actionMap.put("death", SpriteAnimation.ActionState.DEAD);

        // Load all animation states - loadAction creates placeholders for missing files
        for (java.util.Map.Entry<String, SpriteAnimation.ActionState> entry : actionMap.entrySet()) {
            String path = dir + "/" + entry.getKey() + ".gif";
            spriteAnimation.loadAction(entry.getValue(), path);
        }

        // Ensure basic animations have placeholders even if directory doesn't exist
        spriteAnimation.ensureBasicAnimations();

        // Fallback: if still no valid dimensions, create placeholder
        if (spriteAnimation.getBaseWidth() <= 0) {
            createPlaceholderSprite();
        }
    }

    /**
     * Creates a placeholder sprite for testing based on body type and mob name.
     */
    protected void createPlaceholderSprite() {
        // Determine dimensions based on body type
        int w, h;
        switch (bodyType) {
            case QUADRUPED:
                w = 64; h = 64;
                break;
            case SMALL:
                w = 16; h = 16;
                break;
            case LARGE:
                w = 64; h = 96;
                break;
            case HUMANOID:
            default:
                w = 32; h = 64;
                break;
        }

        // Generate unique color based on mob's sprite directory name
        Color bodyColor = generateMobColor();

        java.awt.image.BufferedImage placeholder = new java.awt.image.BufferedImage(
            w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                           java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw based on body type
        if (bodyType == MobBodyType.QUADRUPED) {
            // Four-legged creature shape
            Color darkerColor = bodyColor.darker();

            // Body (horizontal oval)
            g.setColor(bodyColor);
            g.fillOval(8, 16, 48, 32);

            // Head
            g.fillOval(50, 8, 14, 20);

            // Legs
            g.setColor(darkerColor);
            g.fillRect(12, 44, 8, 16);
            g.fillRect(28, 44, 8, 16);
            g.fillRect(40, 44, 8, 16);

            // Tail
            g.setColor(bodyColor);
            g.fillRect(0, 24, 12, 6);

            // Eyes
            g.setColor(Color.WHITE);
            g.fillOval(54, 12, 6, 6);
            g.setColor(Color.BLACK);
            g.fillOval(56, 14, 3, 3);
        } else if (bodyType == MobBodyType.SMALL) {
            // Tiny creature (slime, bug, etc.)
            g.setColor(bodyColor);
            g.fillOval(2, 4, 12, 10);

            // Eyes
            g.setColor(Color.WHITE);
            g.fillOval(3, 6, 4, 4);
            g.fillOval(9, 6, 4, 4);
            g.setColor(Color.BLACK);
            g.fillOval(4, 7, 2, 2);
            g.fillOval(10, 7, 2, 2);
        } else if (bodyType == MobBodyType.LARGE) {
            // Large creature
            Color darkerColor = bodyColor.darker();

            // Body
            g.setColor(bodyColor);
            g.fillRoundRect(8, 20, 48, 60, 12, 12);

            // Head
            g.fillOval(12, 4, 40, 24);

            // Arms
            g.setColor(darkerColor);
            g.fillRect(0, 30, 12, 30);
            g.fillRect(52, 30, 12, 30);

            // Legs
            g.fillRect(16, 76, 14, 20);
            g.fillRect(34, 76, 14, 20);

            // Eyes
            g.setColor(Color.WHITE);
            g.fillOval(18, 8, 12, 12);
            g.fillOval(34, 8, 12, 12);
            g.setColor(Color.RED);
            g.fillOval(22, 11, 5, 5);
            g.fillOval(38, 11, 5, 5);
        } else {
            // Humanoid (default)
            Color darkerColor = bodyColor.darker();

            // Body
            g.setColor(bodyColor);
            g.fillRoundRect(w/4, h/4, w/2, h*3/4 - h/4, 8, 8);

            // Head
            g.fillOval(w/3, 2, w/3, h/4);

            // Arms
            g.setColor(darkerColor);
            g.fillRect(w/4 - 6, h/4 + 4, 6, h/3);
            g.fillRect(w*3/4, h/4 + 4, 6, h/3);

            // Legs
            g.fillRect(w/3, h*3/4 - 4, 6, h/4);
            g.fillRect(w*2/3 - 6, h*3/4 - 4, 6, h/4);

            // Eyes
            g.setColor(Color.WHITE);
            g.fillOval(w/3 + 2, h/8, 4, 4);
            g.fillOval(w*2/3 - 6, h/8, 4, 4);
        }

        g.dispose();

        java.util.List<java.awt.image.BufferedImage> frames = new java.util.ArrayList<>();
        frames.add(placeholder);
        java.util.List<Integer> delays = new java.util.ArrayList<>();
        delays.add(100);
        AnimatedTexture anim = new AnimatedTexture(frames, delays);
        spriteAnimation.setAction(SpriteAnimation.ActionState.IDLE, anim);

        this.spriteWidth = w * SCALE;
        this.spriteHeight = h * SCALE;
    }

    /**
     * Generates a unique color for this mob based on its sprite directory.
     */
    protected Color generateMobColor() {
        // Hash the sprite directory to get a consistent color
        int hash = spriteDir != null ? spriteDir.hashCode() : 0;

        // Generate HSB color from hash for good color variety
        float hue = (hash & 0xFF) / 255.0f;
        float saturation = 0.5f + ((hash >> 8) & 0xFF) / 510.0f; // 0.5 to 1.0
        float brightness = 0.5f + ((hash >> 16) & 0xFF) / 510.0f; // 0.5 to 1.0

        return Color.getHSBColor(hue, saturation, brightness);
    }

    // ==================== Override Abstract Methods ====================

    @Override
    protected void createSkeleton() {
        // Not used for sprite-based mobs - skeleton is null
        this.skeleton = null;
    }

    @Override
    protected void setupAnimations() {
        // Animations are loaded in loadAnimations() instead
    }

    @Override
    protected void performAttack() {
        if (target == null) return;

        double dist = getDistanceToTargetFace();

        // Check for ranged attack first
        if (canFireProjectiles && dist <= preferredAttackRange && projectileTimer <= 0) {
            fireProjectile();
            return;
        }

        // Melee attack if in range
        if (attackTimer <= 0 && dist <= attackRange) {
            // Calculate knockback direction based on mob position relative to player
            Rectangle playerBounds = target.getBounds();
            double playerCenterX = playerBounds.x + playerBounds.width / 2;
            double knockbackDir = posX < playerCenterX ? 1 : -1;
            target.takeDamage(attackDamage, knockbackDir * 5, -3);
            attackTimer = attackCooldown;
            setAnimationState("attack");
        }
    }

    /**
     * Fires a projectile at the target.
     */
    protected void fireProjectile() {
        if (target == null || projectileType == null) return;

        // Calculate direction to target
        Rectangle targetBounds = target.getBounds();
        double targetCenterX = targetBounds.x + targetBounds.width / 2;
        double targetCenterY = targetBounds.y + targetBounds.height / 2;

        double dx = targetCenterX - posX;
        double dy = targetCenterY - (posY - spriteHeight / 2);
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length > 0) {
            dx /= length;
            dy /= length;
        }

        // Create projectile
        int projX = (int)posX;
        int projY = (int)(posY - spriteHeight / 2);

        ProjectileEntity projectile = new ProjectileEntity(
            projX, projY, projectileType, projectileDamage,
            dx * projectileSpeed, dy * projectileSpeed, false
        );
        projectile.setSource(this);
        activeProjectiles.add(projectile);

        projectileTimer = projectileCooldown;
        setAnimationState("fire");
    }

    /**
     * Configures this mob for ranged attacks.
     */
    public void setRangedAttack(ProjectileEntity.ProjectileType type, int damage, double speed, double cooldown, double range) {
        this.canFireProjectiles = true;
        this.projectileType = type;
        this.projectileDamage = damage;
        this.projectileSpeed = speed;
        this.projectileCooldown = cooldown;
        this.preferredAttackRange = range;
    }

    /**
     * Sets the maximum number of jumps for this mob.
     */
    public void setMaxJumps(int jumps) {
        this.maxJumps = Math.max(1, Math.min(3, jumps));
        this.jumpsRemaining = this.maxJumps;
    }

    /**
     * Sets the sprint speed for this mob.
     */
    public void setSprintSpeed(double speed) {
        this.sprintSpeed = speed;
    }

    // ==================== Inventory and Equipment ====================

    /**
     * Sets whether this mob is humanoid (can equip humanoid items like armor, weapons).
     */
    public void setHumanoid(boolean humanoid) {
        this.isHumanoid = humanoid;
    }

    /**
     * Sets the body type for this mob, adjusting dimensions accordingly.
     * Call this before or after constructor to update dimensions.
     */
    public void setBodyType(MobBodyType type) {
        this.bodyType = type;

        // Set dimensions based on body type
        switch (type) {
            case QUADRUPED:
                // 64x64 base, scaled to 128x128
                this.spriteWidth = 64 * SCALE;
                this.spriteHeight = 64 * SCALE;
                this.hitboxWidth = (int)(spriteWidth * 0.8);
                this.hitboxHeight = (int)(spriteHeight * 0.7);
                this.hitboxOffsetX = -hitboxWidth / 2;
                this.hitboxOffsetY = -hitboxHeight;
                this.isHumanoid = false;
                break;
            case SMALL:
                // 16x16 base, scaled to 32x32
                this.spriteWidth = 16 * SCALE;
                this.spriteHeight = 16 * SCALE;
                this.hitboxWidth = (int)(spriteWidth * 0.8);
                this.hitboxHeight = (int)(spriteHeight * 0.9);
                this.hitboxOffsetX = -hitboxWidth / 2;
                this.hitboxOffsetY = -hitboxHeight;
                this.isHumanoid = false;
                break;
            case LARGE:
                // 64x96 base, scaled to 128x192
                this.spriteWidth = 64 * SCALE;
                this.spriteHeight = 96 * SCALE;
                this.hitboxWidth = (int)(spriteWidth * 0.7);
                this.hitboxHeight = (int)(spriteHeight * 0.8);
                this.hitboxOffsetX = -hitboxWidth / 2;
                this.hitboxOffsetY = -hitboxHeight;
                this.isHumanoid = false;
                break;
            case HUMANOID:
            default:
                // 32x64 base, scaled to 64x128
                this.spriteWidth = 32 * SCALE;
                this.spriteHeight = 64 * SCALE;
                this.hitboxWidth = (int)(spriteWidth * 0.8);
                this.hitboxHeight = (int)(spriteHeight * 0.9);
                this.hitboxOffsetX = -hitboxWidth / 2;
                this.hitboxOffsetY = -hitboxHeight;
                this.isHumanoid = true;
                break;
        }

        // Adjust attack range based on size
        this.attackRange = Math.max(60, spriteWidth);
    }

    /**
     * Gets the current body type.
     */
    public MobBodyType getBodyType() {
        return bodyType;
    }

    /**
     * Adds an item to this mob's inventory.
     * @return true if added successfully, false if inventory full
     */
    public boolean addToInventory(Item item) {
        if (inventory.size() < maxInventorySize) {
            inventory.add(item);
            return true;
        }
        return false;
    }

    /**
     * Removes an item from inventory.
     */
    public boolean removeFromInventory(Item item) {
        return inventory.remove(item);
    }

    /**
     * Gets the mob's inventory.
     */
    public List<Item> getInventory() {
        return new ArrayList<>(inventory);
    }

    /**
     * Equips a weapon from inventory (humanoid mobs only).
     * @param item The weapon to equip
     * @return true if equipped successfully
     */
    public boolean equipWeapon(Item item) {
        if (!isHumanoid) return false;
        if (item.getCategory() != Item.ItemCategory.WEAPON &&
            item.getCategory() != Item.ItemCategory.RANGED_WEAPON) return false;

        // Unequip current weapon
        if (equippedWeapon != null) {
            inventory.add(equippedWeapon);
        }

        equippedWeapon = item;
        inventory.remove(item);

        // Update attack damage based on weapon
        this.attackDamage = item.getDamage();
        this.attackRange = item.getRange();

        // If ranged weapon, configure ranged attack
        if (item.isRangedWeapon()) {
            setRangedAttack(
                item.getProjectileType(),
                item.getDamage(),
                item.getProjectileSpeed(),
                1.5,  // Cooldown
                item.getRange()
            );
        }

        return true;
    }

    /**
     * Equips armor from inventory (humanoid mobs only).
     * @param item The armor to equip
     * @return true if equipped successfully
     */
    public boolean equipArmor(Item item) {
        if (!isHumanoid) return false;
        if (item.getCategory() != Item.ItemCategory.ARMOR) return false;

        // Unequip current armor
        if (equippedArmor != null) {
            inventory.add(equippedArmor);
        }

        equippedArmor = item;
        inventory.remove(item);

        return true;
    }

    /**
     * Gets the equipped weapon.
     */
    public Item getEquippedWeapon() {
        return equippedWeapon;
    }

    /**
     * Gets the equipped armor.
     */
    public Item getEquippedArmor() {
        return equippedArmor;
    }

    /**
     * Gets the defense value from equipped armor.
     */
    public int getDefense() {
        if (equippedArmor != null) {
            return equippedArmor.getDefense();
        }
        return 0;
    }

    /**
     * Equips an overlay item (like clothing/armor visual).
     * @param slot Equipment slot enum
     * @param spriteDir Directory containing overlay sprites
     * @param itemName Name of the item
     */
    public void equipOverlay(EquipmentOverlay.EquipmentSlot slot, String spriteDir, String itemName) {
        if (!isHumanoid) return;

        String basePath = spriteDir + "/";
        equipmentOverlay.equipItem(slot, SpriteAnimation.ActionState.IDLE, basePath + "idle.gif", itemName);
        equipmentOverlay.equipItem(slot, SpriteAnimation.ActionState.WALK, basePath + "walk.gif", itemName);
        equipmentOverlay.equipItem(slot, SpriteAnimation.ActionState.RUN, basePath + "run.gif", itemName);
        equipmentOverlay.equipItem(slot, SpriteAnimation.ActionState.ATTACK, basePath + "attack.gif", itemName);
    }

    /**
     * Equips an overlay item (like clothing/armor visual) using slot name string.
     * @param slotName Equipment slot name (helmet, chest, legs, boots, weapon, back, gloves, necklace, accessory)
     * @param spriteDir Directory containing overlay sprites
     * @param itemName Name of the item
     */
    public void equipOverlay(String slotName, String spriteDir, String itemName) {
        if (!isHumanoid) return;

        // Convert string to EquipmentSlot enum
        EquipmentOverlay.EquipmentSlot slot;
        switch (slotName.toLowerCase()) {
            case "helmet":
            case "head":
                slot = EquipmentOverlay.EquipmentSlot.HELMET;
                break;
            case "chest":
            case "body":
            case "torso":
                slot = EquipmentOverlay.EquipmentSlot.CHEST;
                break;
            case "legs":
            case "pants":
                slot = EquipmentOverlay.EquipmentSlot.LEGS;
                break;
            case "boots":
            case "feet":
            case "shoes":
                slot = EquipmentOverlay.EquipmentSlot.BOOTS;
                break;
            case "weapon":
            case "hand":
                slot = EquipmentOverlay.EquipmentSlot.WEAPON;
                break;
            case "back":
            case "cape":
            case "backpack":
                slot = EquipmentOverlay.EquipmentSlot.BACK;
                break;
            case "gloves":
            case "hands":
                slot = EquipmentOverlay.EquipmentSlot.GLOVES;
                break;
            case "necklace":
            case "neck":
                slot = EquipmentOverlay.EquipmentSlot.NECKLACE;
                break;
            case "wrist":
            case "wristwear":
            case "bracelet":
                slot = EquipmentOverlay.EquipmentSlot.WRISTWEAR;
                break;
            case "accessory":
            default:
                slot = EquipmentOverlay.EquipmentSlot.ACCESSORY;
                break;
        }

        equipOverlay(slot, spriteDir, itemName);
    }

    // ==================== Animation State ====================

    /**
     * Sets the current animation state.
     *
     * @param state Animation state name (idle, walk, run, sprint, attack, fire, hurt, death, eat)
     */
    protected void setAnimationState(String state) {
        if (!state.equals(currentAnimState)) {
            currentAnimState = state;
            SpriteAnimation.ActionState actionState = SpriteAnimation.ActionState.IDLE;
            switch (state) {
                case "walk":
                    actionState = SpriteAnimation.ActionState.WALK;
                    break;
                case "run":
                case "chase":
                    actionState = SpriteAnimation.ActionState.RUN;
                    break;
                case "sprint":
                    actionState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.SPRINT)
                            ? SpriteAnimation.ActionState.SPRINT
                            : SpriteAnimation.ActionState.RUN;
                    break;
                case "jump":
                    actionState = SpriteAnimation.ActionState.JUMP;
                    break;
                case "double_jump":
                    actionState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.DOUBLE_JUMP)
                            ? SpriteAnimation.ActionState.DOUBLE_JUMP
                            : SpriteAnimation.ActionState.JUMP;
                    break;
                case "fall":
                    actionState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.FALL)
                            ? SpriteAnimation.ActionState.FALL
                            : SpriteAnimation.ActionState.JUMP;
                    break;
                case "attack":
                    actionState = SpriteAnimation.ActionState.ATTACK;
                    break;
                case "fire":
                    actionState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.FIRE)
                            ? SpriteAnimation.ActionState.FIRE
                            : SpriteAnimation.ActionState.ATTACK;
                    break;
                case "cast":
                    actionState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.CAST)
                            ? SpriteAnimation.ActionState.CAST
                            : SpriteAnimation.ActionState.ATTACK;
                    break;
                case "eat":
                    actionState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.EAT)
                            ? SpriteAnimation.ActionState.EAT
                            : SpriteAnimation.ActionState.IDLE;
                    break;
                case "hurt":
                    actionState = SpriteAnimation.ActionState.HURT;
                    break;
                case "death":
                    actionState = SpriteAnimation.ActionState.DEAD;
                    break;
                default:
                    actionState = SpriteAnimation.ActionState.IDLE;
            }
            spriteAnimation.setState(actionState);
        }
    }

    // ==================== Update and Draw ====================

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        // Update timers
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Update projectile timer
        if (projectileTimer > 0) {
            projectileTimer -= deltaTime;
        }

        // Update eating timer
        if (isEating) {
            eatTimer -= deltaTime;
            if (eatTimer <= 0) {
                isEating = false;
            }
        }

        // Determine if sprinting (when chasing and has sprint speed set)
        isSprinting = (currentState == AIState.CHASE && sprintSpeed > 0);

        // Update sprite animation
        spriteAnimation.update(elapsed);

        // Update animation state based on AI state
        updateAnimationFromAIState();

        // Call parent update for AI and physics
        super.update(deltaTime, entities);

        // Update projectiles
        updateProjectiles(deltaTime, entities);

        // Sync entity position with mob position
        this.x = (int)posX;
        this.y = (int)posY;
    }

    /**
     * Updates active projectiles.
     */
    protected void updateProjectiles(double deltaTime, List<Entity> entities) {
        Iterator<ProjectileEntity> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            ProjectileEntity proj = iterator.next();
            proj.update(deltaTime, entities);

            if (!proj.isActive()) {
                iterator.remove();
                entities.remove(proj);
            }
        }
    }

    /**
     * Gets active projectiles for drawing.
     */
    public List<ProjectileEntity> getActiveProjectiles() {
        return activeProjectiles;
    }

    /**
     * Updates the animation state based on current AI state.
     */
    protected void updateAnimationFromAIState() {
        // Priority: eating > firing > special states
        if (isEating) {
            setAnimationState("eat");
            return;
        }

        switch (currentState) {
            case IDLE:
                setAnimationState("idle");
                break;
            case WANDER:
                setAnimationState("walk");
                break;
            case CHASE:
                // Use sprint if available and configured, otherwise run
                if (isSprinting && spriteAnimation.hasAnimation(SpriteAnimation.ActionState.SPRINT)) {
                    setAnimationState("sprint");
                } else {
                    setAnimationState("run");
                }
                break;
            case ATTACK:
                // Check if we're in ranged or melee mode
                if (canFireProjectiles && projectileTimer > projectileCooldown - 0.3) {
                    setAnimationState("fire");
                } else {
                    setAnimationState("attack");
                }
                break;
            case FLEE:
                setAnimationState("run");
                break;
            case HURT:
                setAnimationState("hurt");
                break;
            case DEAD:
                setAnimationState("death");
                break;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (currentState == AIState.DEAD && stateTimer > 2.0) {
            return; // Don't draw if dead and animation complete
        }

        Graphics2D g2d = (Graphics2D) g;

        // Get current sprite frame
        java.awt.image.BufferedImage frame = spriteAnimation.getCurrentFrame();
        if (frame == null) return;

        // Calculate draw position
        int drawX = (int)posX - spriteWidth / 2;
        int drawY = (int)posY - spriteHeight;

        // Apply flip if facing left
        if (!facingRight) {
            g2d.drawImage(frame,
                drawX + spriteWidth, drawY, -spriteWidth, spriteHeight,
                null);
        } else {
            g2d.drawImage(frame,
                drawX, drawY, spriteWidth, spriteHeight,
                null);
        }

        // Draw invincibility flash effect
        if (invincibilityTimer > 0 && (int)(invincibilityTimer * 10) % 2 == 0) {
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillRect(drawX, drawY, spriteWidth, spriteHeight);
        }

        // Draw hitbox in debug mode
        if (debugDraw) {
            g2d.setColor(new Color(255, 0, 0, 100));
            Rectangle hitbox = getBounds();
            g2d.fillRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
            g2d.setColor(Color.RED);
            g2d.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);

            // Draw position marker
            g2d.setColor(Color.YELLOW);
            g2d.fillOval((int)posX - 3, (int)posY - 3, 6, 6);

            // Draw state text
            g2d.setColor(Color.WHITE);
            g2d.drawString(currentState.name(), (int)posX - 20, (int)posY - spriteHeight - 10);
            g2d.drawString("HP: " + currentHealth + "/" + maxHealth, (int)posX - 30, (int)posY - spriteHeight - 25);
        }

        // Draw health bar
        drawHealthBar(g2d);
    }

    /**
     * Draws a health bar above the mob.
     */
    protected void drawHealthBar(Graphics2D g2d) {
        // Always show health bar for mobs
        int barWidth = Math.max(hitboxWidth, 60);
        int barHeight = 6;
        int barX = (int)posX - barWidth / 2;
        int barY = (int)posY - spriteHeight - 12;

        // Background (dark red for lost health)
        g2d.setColor(new Color(100, 20, 20));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        // Health fill (bright red/green based on health)
        if (maxHealth > 0 && currentHealth > 0) {
            double healthPercent = (double)currentHealth / maxHealth;
            int fillWidth = (int)(barWidth * healthPercent);
            fillWidth = Math.max(fillWidth, 1); // At least 1 pixel if alive

            // Color gradient: green when healthy, yellow when hurt, red when critical
            Color healthColor;
            if (healthPercent > 0.6) {
                healthColor = new Color(50, 220, 50); // Green
            } else if (healthPercent > 0.3) {
                healthColor = new Color(220, 200, 50); // Yellow
            } else {
                healthColor = new Color(220, 50, 50); // Red
            }
            g2d.setColor(healthColor);
            g2d.fillRect(barX, barY, fillWidth, barHeight);
        }

        // Border
        g2d.setColor(Color.BLACK);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }

    // ==================== Hitbox Collision ====================

    @Override
    public Rectangle getBounds() {
        return new Rectangle(
            (int)posX + hitboxOffsetX,
            (int)posY + hitboxOffsetY,
            hitboxWidth,
            hitboxHeight
        );
    }

    /**
     * Gets the visual bounds for camera culling.
     */
    @Override
    public Rectangle getVisualBounds() {
        return new Rectangle(
            (int)posX - spriteWidth / 2 - 10,
            (int)posY - spriteHeight - 10,
            spriteWidth + 20,
            spriteHeight + 20
        );
    }

    /**
     * Sets the hitbox size.
     *
     * @param width Hitbox width
     * @param height Hitbox height
     */
    public void setHitboxSize(int width, int height) {
        this.hitboxWidth = width;
        this.hitboxHeight = height;
        this.hitboxOffsetX = -width / 2;
        this.hitboxOffsetY = -height;
    }

    /**
     * Checks collision with a block.
     *
     * @param block Block to check collision with
     * @return true if colliding
     */
    public boolean collidesWith(BlockEntity block) {
        if (block == null || !block.isSolid()) return false;
        return getBounds().intersects(block.getBounds());
    }

    /**
     * Checks collision with another entity.
     *
     * @param entity Entity to check collision with
     * @return true if colliding
     */
    public boolean collidesWith(Entity entity) {
        if (entity == null || entity == this) return false;
        return getBounds().intersects(entity.getBounds());
    }

    // ==================== Accessors ====================

    /**
     * Gets the sprite width.
     */
    public int getSpriteWidth() {
        return spriteWidth;
    }

    /**
     * Gets the sprite height.
     */
    public int getSpriteHeight() {
        return spriteHeight;
    }

    /**
     * Enables or disables debug drawing.
     */
    public void setDebugDraw(boolean debug) {
        this.debugDraw = debug;
    }

    @Override
    public String toString() {
        return "SpriteMobEntity{" +
                "pos=(" + (int)posX + "," + (int)posY + ")" +
                ", state=" + currentState +
                ", health=" + currentHealth + "/" + maxHealth +
                ", facing=" + (facingRight ? "R" : "L") +
                ", hitbox=" + hitboxWidth + "x" + hitboxHeight +
                "}";
    }
}
