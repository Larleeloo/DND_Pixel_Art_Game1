package entity.mob;
import entity.*;
import entity.item.Item;
import entity.item.ItemEntity;
import entity.item.ItemRegistry;
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
    // Base sizes: humanoid=32x64, quadruped=64x64
    // With SCALE=2: humanoid=64x128 (matches player), quadruped=128x128
    protected int spriteWidth;
    protected int spriteHeight;
    protected static final int SCALE = 2;  // Match player scale (2x)

    // Mob type for different body shapes (base dimensions, scaled 2x for final size)
    public enum MobBodyType {
        HUMANOID,   // 32x64 base → 64x128 final (matches player size)
        QUADRUPED,  // 64x64 base → 128x128 final (double width of humanoid)
        SMALL,      // 16x16 base → 32x32 final
        LARGE       // 64x96 base → 128x192 final
    }
    protected MobBodyType bodyType = MobBodyType.HUMANOID;

    // Multi-jump system
    protected int maxJumps = 1;           // Default to single jump
    protected int jumpsRemaining = 1;
    protected int currentJumpNumber = 0;
    protected double doubleJumpStrength = -8;
    protected double tripleJumpStrength = -7;

    // Track if mob is currently jumping/falling for animations
    protected boolean isJumping = false;
    protected boolean isFalling = false;

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

    // Items dropped on death (to be added to game world)
    protected List<ItemEntity> pendingDroppedItems = new ArrayList<>();
    protected boolean hasDroppedItems = false;

    // ==================== Mana System for Spell-casting Mobs ====================

    protected int maxMana = 100;
    protected int currentMana = 100;
    protected double manaRegenRate = 5.0;  // Mana per second
    protected double manaRegenAccumulator = 0.0;  // For fractional mana regen

    // ==================== Weapon Usage AI System ====================

    // Weapon type preferences for AI weapon selection
    public enum WeaponPreference {
        MELEE_ONLY,           // Only uses melee weapons (zombies, knights)
        RANGED_ONLY,          // Only uses ranged weapons (skeletons, mages)
        MELEE_AND_THROWABLE,  // Uses melee and throwables (bandits, goblins)
        MAGIC_ONLY,           // Only uses magic weapons (mages)
        ANY                   // Uses any available weapon
    }

    protected WeaponPreference weaponPreference = WeaponPreference.ANY;
    protected double weaponSwitchCooldown = 0;  // Prevents rapid weapon switching
    protected static final double WEAPON_SWITCH_DELAY = 1.0;  // Seconds between weapon switches

    // Mana cost for magic weapon attacks
    protected int magicAttackManaCost = 10;

    // ==================== Status Effect System ====================

    /**
     * Status effects that can be applied to mobs from special arrows/magic.
     */
    public enum StatusEffect {
        NONE,
        BURNING,    // Fire damage over time
        FROZEN,     // Slowed movement, ice damage
        POISONED    // Poison damage over time
    }

    // Current active status effect
    protected StatusEffect activeEffect = StatusEffect.NONE;
    protected double effectTimer = 0;           // Duration remaining
    protected double effectDamageTimer = 0;     // Timer for damage ticks
    protected int effectDamagePerTick = 0;      // Damage per tick
    protected double effectTickInterval = 0.5;  // Seconds between damage ticks
    protected double effectSlowMultiplier = 1.0; // Movement speed multiplier (1.0 = normal)

    // Effect visual properties
    protected Color effectTintColor = null;     // Tint overlay color
    protected float effectTintAlpha = 0.4f;     // Tint transparency

    // Particle overlay GIFs for status effects
    protected static AnimatedTexture fireParticles = null;
    protected static AnimatedTexture iceParticles = null;
    protected static AnimatedTexture poisonParticles = null;
    protected static boolean particlesLoaded = false;

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

        // Apply body type dimensions using loaded sprite dimensions
        // This matches how the player calculates its size
        applyDimensionsFromLoadedSprite();

        // Initialize equipment overlay for humanoid mobs
        this.equipmentOverlay = new EquipmentOverlay();

        // Load particle overlay GIFs (once for all mobs)
        loadParticleOverlays();

        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Loads the particle overlay GIFs for status effects.
     * Only loads once (static), shared across all SpriteMobEntity instances.
     */
    protected static void loadParticleOverlays() {
        if (particlesLoaded) return;
        particlesLoaded = true;

        try {
            // Load fire particles
            AssetLoader.ImageAsset fireAsset = AssetLoader.load("assets/particles/fire_particles.gif");
            if (fireAsset.animatedTexture != null) {
                fireParticles = fireAsset.animatedTexture;
            }

            // Load ice particles
            AssetLoader.ImageAsset iceAsset = AssetLoader.load("assets/particles/ice_particles.gif");
            if (iceAsset.animatedTexture != null) {
                iceParticles = iceAsset.animatedTexture;
            }

            // Load poison particles
            AssetLoader.ImageAsset poisonAsset = AssetLoader.load("assets/particles/poison_particles.gif");
            if (poisonAsset.animatedTexture != null) {
                poisonParticles = poisonAsset.animatedTexture;
            }
        } catch (Exception e) {
            // Fallback to procedural particles if loading fails
            System.err.println("Warning: Could not load particle GIFs: " + e.getMessage());
        }
    }

    /**
     * Applies dimensions from loaded sprite animation, matching player calculation.
     * Uses actual GIF dimensions * SCALE, with hitbox matching full visual size.
     */
    private void applyDimensionsFromLoadedSprite() {
        int baseWidth = spriteAnimation.getBaseWidth();
        int baseHeight = spriteAnimation.getBaseHeight();

        // If valid dimensions loaded from GIF, use them exactly like player does
        if (baseWidth > 0 && baseHeight > 0) {
            this.spriteWidth = baseWidth * SCALE;
            this.spriteHeight = baseHeight * SCALE;

            // For humanoid mobs, use full visual size as hitbox (like player)
            // For other body types, use proportional hitbox
            if (bodyType == MobBodyType.HUMANOID) {
                // Match player: hitbox = full visual size
                this.hitboxWidth = spriteWidth;
                this.hitboxHeight = spriteHeight;
            } else {
                // Non-humanoids use proportional hitbox
                this.hitboxWidth = (int)(spriteWidth * 0.8);
                this.hitboxHeight = (int)(spriteHeight * 0.85);
            }

            this.hitboxOffsetX = -hitboxWidth / 2;
            this.hitboxOffsetY = -hitboxHeight;
            this.attackRange = Math.max(60, spriteWidth);
        } else {
            // Fallback to body type defaults if no valid sprite loaded
            setBodyType(bodyType);
        }
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
                 lowerDir.contains("tiny") || lowerDir.contains("rat") ||
                 lowerDir.contains("frog") || lowerDir.contains("toad")) {
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

        // Set stats based on detected mob type
        configureMobStats(lowerDir);
    }

    /**
     * Configures mob stats (health, damage, speed) based on detected mob type from sprite directory.
     * This applies to sprite-based mobs to give them appropriate stats.
     */
    private void configureMobStats(String lowerDir) {
        // Humanoid mob types
        if (lowerDir.contains("zombie")) {
            maxHealth = 50;
            currentHealth = maxHealth;
            attackDamage = 8;
            wanderSpeed = 40;
            chaseSpeed = 60;
        } else if (lowerDir.contains("skeleton")) {
            maxHealth = 40;
            currentHealth = maxHealth;
            attackDamage = 6;
            wanderSpeed = 50;
            chaseSpeed = 80;
        } else if (lowerDir.contains("goblin")) {
            maxHealth = 40;
            currentHealth = maxHealth;
            attackDamage = 5;
            wanderSpeed = 60;
            chaseSpeed = 100;
        } else if (lowerDir.contains("orc")) {
            maxHealth = 60;
            currentHealth = maxHealth;
            attackDamage = 15;
            wanderSpeed = 40;
            chaseSpeed = 60;
        } else if (lowerDir.contains("bandit")) {
            maxHealth = 45;
            currentHealth = maxHealth;
            attackDamage = 8;
            wanderSpeed = 50;
            chaseSpeed = 70;
        } else if (lowerDir.contains("knight")) {
            maxHealth = 55;
            currentHealth = maxHealth;
            attackDamage = 12;
            wanderSpeed = 35;
            chaseSpeed = 50;
        } else if (lowerDir.contains("mage")) {
            maxHealth = 40;
            currentHealth = maxHealth;
            attackDamage = 15;
            wanderSpeed = 30;
            chaseSpeed = 40;
        }
        // Quadruped mob types
        else if (lowerDir.contains("wolf")) {
            maxHealth = 45;
            currentHealth = maxHealth;
            attackDamage = 6;
            wanderSpeed = 60;
            chaseSpeed = 150;
        } else if (lowerDir.contains("bear")) {
            maxHealth = 55;
            currentHealth = maxHealth;
            attackDamage = 12;
            wanderSpeed = 40;
            chaseSpeed = 100;
        } else if (lowerDir.contains("dog")) {
            maxHealth = 35;
            currentHealth = maxHealth;
            attackDamage = 4;
            wanderSpeed = 70;
            chaseSpeed = 140;
        } else if (lowerDir.contains("lion") || lowerDir.contains("tiger")) {
            maxHealth = 50;
            currentHealth = maxHealth;
            attackDamage = 10;
            wanderSpeed = 50;
            chaseSpeed = 180;
        } else if (lowerDir.contains("spider")) {
            maxHealth = 35;
            currentHealth = maxHealth;
            attackDamage = 8;
            wanderSpeed = 40;
            chaseSpeed = 120;
        }
        // Large boss creatures
        else if (lowerDir.contains("dragon")) {
            maxHealth = 150;
            currentHealth = maxHealth;
            attackDamage = 25;
            wanderSpeed = 30;
            chaseSpeed = 80;
        } else if (lowerDir.contains("ogre") || lowerDir.contains("troll")) {
            maxHealth = 100;
            currentHealth = maxHealth;
            attackDamage = 20;
            wanderSpeed = 25;
            chaseSpeed = 50;
        } else if (lowerDir.contains("giant") || lowerDir.contains("boss")) {
            maxHealth = 120;
            currentHealth = maxHealth;
            attackDamage = 18;
            wanderSpeed = 20;
            chaseSpeed = 40;
        }
        // Small creatures
        else if (lowerDir.contains("slime")) {
            maxHealth = 25;
            currentHealth = maxHealth;
            attackDamage = 3;
            wanderSpeed = 30;
            chaseSpeed = 60;
        } else if (lowerDir.contains("bat")) {
            maxHealth = 20;
            currentHealth = maxHealth;
            attackDamage = 4;
            wanderSpeed = 50;
            chaseSpeed = 120;
        } else if (lowerDir.contains("rat")) {
            maxHealth = 15;
            currentHealth = maxHealth;
            attackDamage = 2;
            wanderSpeed = 40;
            chaseSpeed = 80;
        }
        // Default humanoid stats
        else if (bodyType == MobBodyType.HUMANOID) {
            maxHealth = 45;
            currentHealth = maxHealth;
            attackDamage = 8;
            wanderSpeed = 50;
            chaseSpeed = 70;
        }
        // Default for other body types
        else {
            maxHealth = 40;
            currentHealth = maxHealth;
        }
    }

    /**
     * Applies dimensions based on body type (with SCALE applied).
     * Humanoid: 32x64 base → 64x128 scaled
     * Quadruped: 64x64 base → 128x128 scaled
     */
    private void applyBodyTypeDimensions() {
        switch (bodyType) {
            case QUADRUPED:
                this.spriteWidth = 64 * SCALE;   // 128px wide
                this.spriteHeight = 64 * SCALE;  // 128px tall
                break;
            case SMALL:
                this.spriteWidth = 16 * SCALE;   // 32px
                this.spriteHeight = 16 * SCALE;
                break;
            case LARGE:
                this.spriteWidth = 64 * SCALE;   // 128px wide
                this.spriteHeight = 96 * SCALE;  // 192px tall
                break;
            case HUMANOID:
            default:
                this.spriteWidth = 32 * SCALE;   // 64px wide (matches player)
                this.spriteHeight = 64 * SCALE;  // 128px tall (matches player)
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

        // Status effect animations
        actionMap.put("burning", SpriteAnimation.ActionState.BURNING);
        actionMap.put("frozen", SpriteAnimation.ActionState.FROZEN);
        actionMap.put("poisoned", SpriteAnimation.ActionState.POISONED);

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
     * Uses scaled dimensions to match final display size.
     */
    protected void createPlaceholderSprite() {
        // Determine dimensions based on body type (scaled sizes)
        int w, h;
        switch (bodyType) {
            case QUADRUPED:
                w = 64 * SCALE; h = 64 * SCALE;  // 128x128 - wide four-legged
                break;
            case SMALL:
                w = 16 * SCALE; h = 16 * SCALE;  // 32x32
                break;
            case LARGE:
                w = 64 * SCALE; h = 96 * SCALE;  // 128x192
                break;
            case HUMANOID:
            default:
                w = 32 * SCALE; h = 64 * SCALE;  // 64x128 - matches player
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

        // Set scaled dimensions
        this.spriteWidth = w;
        this.spriteHeight = h;
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

        // Humanoid mobs with weapons use the weapon-based attack system
        if (isHumanoid && (equippedWeapon != null || !inventory.isEmpty())) {
            performWeaponAttack();
            return;
        }

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
     * Also updates attackRange to allow AI to enter attack state at range.
     */
    public void setRangedAttack(ProjectileEntity.ProjectileType type, int damage, double speed, double cooldown, double range) {
        this.canFireProjectiles = true;
        this.projectileType = type;
        this.projectileDamage = damage;
        this.projectileSpeed = speed;
        this.projectileCooldown = cooldown;
        this.preferredAttackRange = range;
        // Also update attackRange so AI enters attack state at the right distance
        this.attackRange = range;
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

    /**
     * Override to implement multi-jump support for sprite mobs.
     * Tracks jump number for proper animation selection.
     */
    @Override
    protected void tryObstacleJump() {
        // Can jump if blocked, on ground (or have remaining air jumps), and cooldown expired
        if (blockedByObstacle && obstacleJumpCooldown <= 0) {
            if (onGround) {
                // First jump from ground
                velocityY = jumpStrength;
                onGround = false;
                currentJumpNumber = 1;
                jumpsRemaining = maxJumps - 1;
                obstacleJumpCooldown = OBSTACLE_JUMP_COOLDOWN;
            } else if (jumpsRemaining > 0 && maxJumps > 1) {
                // Air jump (double/triple jump)
                currentJumpNumber++;
                jumpsRemaining--;

                // Use appropriate jump strength for multi-jumps
                if (currentJumpNumber == 2) {
                    velocityY = doubleJumpStrength;
                } else if (currentJumpNumber >= 3) {
                    velocityY = tripleJumpStrength;
                }
                obstacleJumpCooldown = OBSTACLE_JUMP_COOLDOWN;
            }
        }
        // Reset blocked flag each frame (will be set again if still blocked)
        blockedByObstacle = false;
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
                // 64x64 base → 128x128 scaled - wide four-legged creatures
                this.spriteWidth = 64 * SCALE;
                this.spriteHeight = 64 * SCALE;
                this.hitboxWidth = (int)(spriteWidth * 0.8);  // ~102px wide
                this.hitboxHeight = (int)(spriteHeight * 0.7); // ~90px tall
                this.hitboxOffsetX = -hitboxWidth / 2;
                this.hitboxOffsetY = -hitboxHeight;
                this.isHumanoid = false;
                break;
            case SMALL:
                // 16x16 base → 32x32 scaled - tiny creatures
                this.spriteWidth = 16 * SCALE;
                this.spriteHeight = 16 * SCALE;
                this.hitboxWidth = (int)(spriteWidth * 0.8);
                this.hitboxHeight = (int)(spriteHeight * 0.9);
                this.hitboxOffsetX = -hitboxWidth / 2;
                this.hitboxOffsetY = -hitboxHeight;
                this.isHumanoid = false;
                break;
            case LARGE:
                // 64x96 base → 128x192 scaled - large boss creatures
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
                // 32x64 base → 64x128 scaled - matches player size exactly
                this.spriteWidth = 32 * SCALE;
                this.spriteHeight = 64 * SCALE;
                // Use full visual size as hitbox (matching player behavior)
                this.hitboxWidth = spriteWidth;    // 64px (same as player)
                this.hitboxHeight = spriteHeight;  // 128px (same as player)
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
     * Drops all items from inventory and equipment when the mob dies.
     * Creates ItemEntity objects that can be picked up by the player.
     */
    protected void dropAllItems() {
        if (hasDroppedItems) return;  // Only drop once
        hasDroppedItems = true;

        // Drop equipped weapon
        if (equippedWeapon != null) {
            ItemEntity dropped = createDroppedItem(equippedWeapon);
            if (dropped != null) {
                pendingDroppedItems.add(dropped);
            }
            equippedWeapon = null;
        }

        // Drop equipped armor
        if (equippedArmor != null) {
            ItemEntity dropped = createDroppedItem(equippedArmor);
            if (dropped != null) {
                pendingDroppedItems.add(dropped);
            }
            equippedArmor = null;
        }

        // Drop all inventory items
        for (Item item : inventory) {
            ItemEntity dropped = createDroppedItem(item);
            if (dropped != null) {
                pendingDroppedItems.add(dropped);
            }
        }
        inventory.clear();
    }

    /**
     * Creates an ItemEntity from an Item for dropping.
     * Adds random scatter to drop position.
     */
    private ItemEntity createDroppedItem(Item item) {
        if (item == null) return null;

        // Find registry ID by item name
        String registryId = ItemRegistry.findIdByName(item.getName());

        // Random scatter offset
        int scatterX = (int)(Math.random() * 60 - 30);  // -30 to +30 pixels
        int dropX = (int)posX + scatterX;
        int dropY = (int)posY - 20;  // Slightly above ground

        if (registryId != null) {
            return new ItemEntity(dropX, dropY, registryId);
        } else {
            // Fallback: create with name and type
            return new ItemEntity(dropX, dropY, null, item.getName(),
                item.getCategory().name().toLowerCase());
        }
    }

    /**
     * Gets and clears any pending dropped items.
     * Call this from GameScene to add dropped items to the world.
     */
    public List<ItemEntity> collectDroppedItems() {
        List<ItemEntity> items = new ArrayList<>(pendingDroppedItems);
        pendingDroppedItems.clear();
        return items;
    }

    /**
     * Checks if there are pending dropped items to collect.
     */
    public boolean hasPendingDroppedItems() {
        return !pendingDroppedItems.isEmpty();
    }

    /**
     * Override to drop items when entering DEAD state.
     */
    @Override
    protected void changeState(AIState newState) {
        super.changeState(newState);

        // Drop items when dying
        if (newState == AIState.DEAD) {
            dropAllItems();
        }
    }

    /**
     * Equips a weapon from inventory (humanoid mobs only).
     * @param item The weapon to equip
     * @return true if equipped successfully
     */
    public boolean equipWeapon(Item item) {
        if (!isHumanoid) return false;
        if (item.getCategory() != Item.ItemCategory.WEAPON &&
            item.getCategory() != Item.ItemCategory.RANGED_WEAPON &&
            item.getCategory() != Item.ItemCategory.THROWABLE) return false;

        // Unequip current weapon
        if (equippedWeapon != null) {
            inventory.add(equippedWeapon);
        }

        equippedWeapon = item;
        inventory.remove(item);

        // Update attack damage based on weapon
        this.attackDamage = item.getDamage();

        // For ranged/throwable weapons, use proper attack range
        // Default range (60) is melee range - ranged weapons need larger range
        if (item.isRangedWeapon() || item.getCategory() == Item.ItemCategory.THROWABLE) {
            // Use item range if it's been explicitly set higher than default,
            // otherwise use a reasonable ranged attack range
            int effectiveRange = item.getRange() > 80 ? item.getRange() : 250;
            this.attackRange = effectiveRange;

            // Configure ranged attack
            setRangedAttack(
                item.getProjectileType(),
                item.getProjectileDamage() > 0 ? item.getProjectileDamage() : item.getDamage(),
                item.getProjectileSpeed(),
                1.5,  // Cooldown
                effectiveRange
            );
        } else {
            // Melee weapon - use item's range directly
            this.attackRange = item.getRange();
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

    // ==================== Mana System Methods ====================

    /**
     * Gets the current mana.
     */
    public int getMana() {
        return currentMana;
    }

    /**
     * Gets the maximum mana.
     */
    public int getMaxMana() {
        return maxMana;
    }

    /**
     * Sets the current mana (clamped to 0-maxMana).
     */
    public void setMana(int mana) {
        this.currentMana = Math.max(0, Math.min(maxMana, mana));
    }

    /**
     * Sets the maximum mana.
     */
    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
        if (currentMana > maxMana) {
            currentMana = maxMana;
        }
    }

    /**
     * Uses mana for an action.
     * @return true if mana was consumed successfully
     */
    public boolean useMana(int amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            return true;
        }
        return false;
    }

    /**
     * Checks if the mob has enough mana.
     */
    public boolean hasMana(int amount) {
        return currentMana >= amount;
    }

    /**
     * Restores mana.
     */
    public void restoreMana(int amount) {
        currentMana = Math.min(maxMana, currentMana + amount);
    }

    /**
     * Gets the mana regeneration rate.
     */
    public double getManaRegenRate() {
        return manaRegenRate;
    }

    /**
     * Sets the mana regeneration rate.
     */
    public void setManaRegenRate(double rate) {
        this.manaRegenRate = rate;
    }

    /**
     * Updates mana regeneration based on elapsed time.
     */
    protected void updateManaRegeneration(double deltaTime) {
        if (currentMana < maxMana) {
            manaRegenAccumulator += manaRegenRate * deltaTime;
            if (manaRegenAccumulator >= 1.0) {
                int manaToAdd = (int) manaRegenAccumulator;
                currentMana = Math.min(maxMana, currentMana + manaToAdd);
                manaRegenAccumulator -= manaToAdd;
            }
        }
    }

    // ==================== Weapon Usage AI Methods ====================

    /**
     * Sets the weapon preference for this mob.
     */
    public void setWeaponPreference(WeaponPreference preference) {
        this.weaponPreference = preference;
    }

    /**
     * Gets the weapon preference.
     */
    public WeaponPreference getWeaponPreference() {
        return weaponPreference;
    }

    /**
     * Sets the mana cost for magic attacks.
     */
    public void setMagicAttackManaCost(int cost) {
        this.magicAttackManaCost = cost;
    }

    /**
     * Checks if an item matches the mob's weapon preference.
     */
    protected boolean isPreferredWeaponType(Item item) {
        if (item == null) return false;

        Item.ItemCategory category = item.getCategory();
        boolean isMelee = category == Item.ItemCategory.WEAPON;
        boolean isRanged = category == Item.ItemCategory.RANGED_WEAPON;
        boolean isThrowable = category == Item.ItemCategory.THROWABLE;
        boolean isMagic = item.scalesWithIntelligence();

        switch (weaponPreference) {
            case MELEE_ONLY:
                return isMelee;
            case RANGED_ONLY:
                return isRanged;
            case MELEE_AND_THROWABLE:
                return isMelee || isThrowable;
            case MAGIC_ONLY:
                return isMagic && (isRanged || item.getProjectileType() != null);
            case ANY:
            default:
                return isMelee || isRanged || isThrowable;
        }
    }

    /**
     * Selects the best weapon from inventory based on situation.
     * Called periodically during combat to optimize weapon choice.
     *
     * @param distanceToTarget Distance to the target
     */
    protected void selectBestWeapon(double distanceToTarget) {
        if (weaponSwitchCooldown > 0) return;
        if (!isHumanoid) return;

        Item bestWeapon = null;
        int bestScore = -1;

        // Check equipped weapon first
        if (equippedWeapon != null && isPreferredWeaponType(equippedWeapon)) {
            bestScore = scoreWeapon(equippedWeapon, distanceToTarget);
            bestWeapon = equippedWeapon;
        }

        // Check inventory for better weapons
        for (Item item : inventory) {
            if (!isPreferredWeaponType(item)) continue;

            int score = scoreWeapon(item, distanceToTarget);
            if (score > bestScore) {
                bestScore = score;
                bestWeapon = item;
            }
        }

        // Equip the best weapon if different from current
        if (bestWeapon != null && bestWeapon != equippedWeapon) {
            equipWeapon(bestWeapon);
            weaponSwitchCooldown = WEAPON_SWITCH_DELAY;
        }
    }

    /**
     * Scores a weapon based on current combat situation.
     * Higher scores are better.
     */
    protected int scoreWeapon(Item weapon, double distanceToTarget) {
        if (weapon == null) return 0;

        int score = weapon.getDamage();

        // Ranged weapons score higher at distance
        if (weapon.isRangedWeapon() || weapon.getCategory() == Item.ItemCategory.THROWABLE) {
            if (distanceToTarget > attackRange) {
                score += 50;  // Bonus for being able to attack at range
            }
            // Check if we have enough mana for magic weapons
            if (weapon.scalesWithIntelligence() && !hasMana(magicAttackManaCost)) {
                score -= 100;  // Penalty if we can't use the weapon
            }
        } else {
            // Melee weapons score higher at close range
            if (distanceToTarget <= weapon.getRange()) {
                score += 30;
            }
        }

        // Rarity bonus
        score += weapon.getRarity().ordinal() * 5;

        return score;
    }

    /**
     * Uses a throwable item from inventory.
     * Removes the item after throwing if it's stackable.
     *
     * @param throwable The throwable item to use
     * @return true if the item was thrown successfully
     */
    protected boolean useThrowableItem(Item throwable) {
        if (throwable == null || target == null) return false;
        if (!throwable.isRangedWeapon()) return false;

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

        // Create projectile from the throwable item
        int projX = (int)posX;
        int projY = (int)(posY - spriteHeight / 2);

        ProjectileEntity projectile = throwable.createProjectile(
            projX, projY, dx, dy, false
        );

        if (projectile != null) {
            projectile.setSource(this);
            activeProjectiles.add(projectile);

            // Consume the throwable item
            removeFromInventory(throwable);

            setAnimationState("fire");
            return true;
        }

        return false;
    }

    /**
     * Uses a magic weapon, consuming mana.
     *
     * @param weapon The magic weapon to use
     * @return true if the attack was successful
     */
    protected boolean useMagicWeapon(Item weapon) {
        if (weapon == null || target == null) return false;
        if (!weapon.isRangedWeapon()) return false;

        // Determine mana cost
        int manaCost = weapon.getChargeManaCost() > 0 ? weapon.getChargeManaCost() / 3 : magicAttackManaCost;

        // Check if we have enough mana
        if (!useMana(manaCost)) {
            return false;
        }

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

        // Create projectile from the weapon
        int projX = (int)posX;
        int projY = (int)(posY - spriteHeight / 2);

        ProjectileEntity projectile = weapon.createProjectile(
            projX, projY, dx, dy, false
        );

        if (projectile != null) {
            projectile.setSource(this);
            activeProjectiles.add(projectile);

            setAnimationState("cast");
            return true;
        }

        return false;
    }

    /**
     * Uses the currently equipped melee weapon to attack.
     */
    protected void useMeleeWeapon() {
        if (target == null || equippedWeapon == null) return;

        double dist = getDistanceToTargetFace();
        int weaponRange = equippedWeapon.getRange();

        if (dist <= weaponRange) {
            Rectangle playerBounds = target.getBounds();
            double playerCenterX = playerBounds.x + playerBounds.width / 2;
            double knockbackDir = posX < playerCenterX ? 1 : -1;

            int damage = equippedWeapon.getDamage();
            target.takeDamage(damage, knockbackDir * 5, -3);
            attackTimer = 1.0 / Math.max(0.1f, equippedWeapon.getAttackSpeed());
            setAnimationState("attack");
        }
    }

    /**
     * Performs a weapon-based attack using equipped or inventory weapons.
     * Called when the mob is in attack state and attack timer is ready.
     *
     * Subclasses can override this to customize attack behavior.
     */
    protected void performWeaponAttack() {
        if (target == null) return;

        double dist = getDistanceToTargetFace();

        // First, try to select the best weapon for the situation
        selectBestWeapon(dist);

        // If we have an equipped weapon, use it
        if (equippedWeapon != null) {
            if (equippedWeapon.isRangedWeapon()) {
                // Check cooldown for all ranged attacks
                if (projectileTimer > 0) {
                    return;  // Still on cooldown
                }

                // Ranged weapon attack
                if (equippedWeapon.scalesWithIntelligence()) {
                    // Magic weapon - uses mana
                    if (useMagicWeapon(equippedWeapon)) {
                        projectileTimer = projectileCooldown;
                        return;
                    }
                } else if (equippedWeapon.getCategory() == Item.ItemCategory.THROWABLE) {
                    // Throwable - consumes item
                    if (useThrowableItem(equippedWeapon)) {
                        projectileTimer = projectileCooldown;
                        // Find next throwable in inventory
                        for (Item item : inventory) {
                            if (item.getCategory() == Item.ItemCategory.THROWABLE) {
                                equipWeapon(item);
                                break;
                            }
                        }
                        return;
                    }
                } else {
                    // Regular ranged weapon (bow, crossbow)
                    if (dist <= preferredAttackRange) {
                        fireProjectileFromWeapon(equippedWeapon);
                        projectileTimer = projectileCooldown;
                        return;
                    }
                }
            } else {
                // Melee weapon attack
                if (dist <= equippedWeapon.getRange() && attackTimer <= 0) {
                    useMeleeWeapon();
                    return;
                }
            }
        }

        // Check inventory for throwables if we don't have a ranged weapon
        if (dist > attackRange) {
            for (Item item : new ArrayList<>(inventory)) {
                if (item.getCategory() == Item.ItemCategory.THROWABLE) {
                    if (useThrowableItem(item)) {
                        projectileTimer = projectileCooldown;
                        return;
                    }
                }
            }
        }

        // Fall back to default ranged attack if configured
        if (canFireProjectiles && dist <= preferredAttackRange && projectileTimer <= 0) {
            fireProjectile();
            return;
        }

        // Fall back to basic melee attack
        if (attackTimer <= 0 && dist <= attackRange) {
            Rectangle playerBounds = target.getBounds();
            double playerCenterX = playerBounds.x + playerBounds.width / 2;
            double knockbackDir = posX < playerCenterX ? 1 : -1;
            target.takeDamage(attackDamage, knockbackDir * 5, -3);
            attackTimer = attackCooldown;
            setAnimationState("attack");
        }
    }

    /**
     * Fires a projectile using the specified ranged weapon.
     */
    protected void fireProjectileFromWeapon(Item weapon) {
        if (weapon == null || target == null || !weapon.isRangedWeapon()) return;

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

        int projX = (int)posX;
        int projY = (int)(posY - spriteHeight / 2);

        ProjectileEntity projectile = weapon.createProjectile(projX, projY, dx, dy, false);
        if (projectile != null) {
            projectile.setSource(this);
            activeProjectiles.add(projectile);
            setAnimationState("fire");
        }
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

    // ==================== Status Effect Methods ====================

    /**
     * Applies a status effect to this mob.
     *
     * @param effect The type of status effect
     * @param duration Duration in seconds
     * @param damagePerTick Damage dealt per tick (for DoT effects)
     * @param damageMultiplier Multiplier for arrow/weapon damage
     */
    public void applyStatusEffect(StatusEffect effect, double duration, int damagePerTick, float damageMultiplier) {
        if (currentState == AIState.DEAD) return;  // Can't affect dead mobs

        this.activeEffect = effect;
        this.effectTimer = duration;
        this.effectDamageTimer = 0;
        this.effectDamagePerTick = damagePerTick;

        // Configure effect-specific properties
        switch (effect) {
            case BURNING:
                this.effectTickInterval = 0.5;      // Burn damage every 0.5s
                this.effectSlowMultiplier = 1.0;    // No slow from fire
                this.effectTintColor = new Color(255, 100, 0);  // Orange-red
                this.effectTintAlpha = 0.35f;
                // Try to use burning animation if available
                if (spriteAnimation.hasAnimation(SpriteAnimation.ActionState.BURNING)) {
                    spriteAnimation.setTint(null);  // Use animation instead of tint
                }
                break;

            case FROZEN:
                this.effectTickInterval = 1.0;      // Ice damage every 1s
                this.effectSlowMultiplier = 0.4;    // 60% speed reduction
                this.effectTintColor = new Color(100, 200, 255);  // Ice blue
                this.effectTintAlpha = 0.45f;
                // Try to use frozen animation if available
                if (spriteAnimation.hasAnimation(SpriteAnimation.ActionState.FROZEN)) {
                    spriteAnimation.setTint(null);
                }
                break;

            case POISONED:
                this.effectTickInterval = 0.75;     // Poison damage every 0.75s
                this.effectSlowMultiplier = 0.85;   // 15% speed reduction
                this.effectTintColor = new Color(100, 200, 50);  // Green
                this.effectTintAlpha = 0.3f;
                break;

            case NONE:
            default:
                clearStatusEffect();
                break;
        }
    }

    /**
     * Clears the current status effect.
     */
    public void clearStatusEffect() {
        this.activeEffect = StatusEffect.NONE;
        this.effectTimer = 0;
        this.effectDamageTimer = 0;
        this.effectDamagePerTick = 0;
        this.effectSlowMultiplier = 1.0;
        this.effectTintColor = null;
        spriteAnimation.setTint(null);
    }

    /**
     * Updates the status effect, dealing damage and applying slow.
     */
    protected void updateStatusEffect(double deltaTime) {
        if (activeEffect == StatusEffect.NONE || effectTimer <= 0) {
            if (activeEffect != StatusEffect.NONE) {
                clearStatusEffect();
            }
            return;
        }

        // Decrease effect timer
        effectTimer -= deltaTime;

        // Handle damage ticks
        if (effectDamagePerTick > 0) {
            effectDamageTimer += deltaTime;
            if (effectDamageTimer >= effectTickInterval) {
                effectDamageTimer -= effectTickInterval;
                // Deal effect damage (no knockback)
                takeDamage(effectDamagePerTick, 0, 0);
            }
        }

        // Check if effect expired
        if (effectTimer <= 0) {
            clearStatusEffect();
        }
    }

    /**
     * Gets the current status effect.
     */
    public StatusEffect getActiveEffect() {
        return activeEffect;
    }

    /**
     * Checks if the mob has an active status effect.
     */
    public boolean hasStatusEffect() {
        return activeEffect != StatusEffect.NONE && effectTimer > 0;
    }

    /**
     * Gets the current speed multiplier from status effects.
     */
    public double getEffectSpeedMultiplier() {
        return effectSlowMultiplier;
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

        // Update mana regeneration
        updateManaRegeneration(deltaTime);

        // Update weapon switch cooldown
        if (weaponSwitchCooldown > 0) {
            weaponSwitchCooldown -= deltaTime;
        }

        // Determine if sprinting (when chasing and has sprint speed set)
        isSprinting = (currentState == AIState.CHASE && sprintSpeed > 0);

        // Track jump/fall state before physics update
        boolean wasOnGround = onGround;

        // Update sprite animation
        spriteAnimation.update(elapsed);

        // Update status effects (burning, frozen, etc.)
        updateStatusEffect(deltaTime);

        // Call parent update for AI and physics
        super.update(deltaTime, entities);

        // Update jumping/falling state after physics
        if (!onGround) {
            if (velocityY < 0) {
                isJumping = true;
                isFalling = false;
            } else {
                isJumping = false;
                isFalling = true;
            }
        } else {
            // Landed - reset jump state
            if (wasOnGround == false) {
                jumpsRemaining = maxJumps;
                currentJumpNumber = 0;
            }
            isJumping = false;
            isFalling = false;
        }

        // Update animation state based on AI state (after physics so we know if jumping)
        updateAnimationFromAIState();

        // Update projectiles
        updateProjectiles(deltaTime, entities);

        // Check for nearby items to pick up (humanoid mobs only, not when dead)
        // Dead mobs should not re-collect dropped items
        if (isHumanoid && entities != null && currentState != AIState.DEAD) {
            checkForItemPickup(entities);
        }

        // Sync entity position with mob position
        this.x = (int)posX;
        this.y = (int)posY;
    }

    /**
     * Checks for nearby items and picks them up if possible.
     */
    protected void checkForItemPickup(List<Entity> entities) {
        if (inventory.size() >= maxInventorySize) return;

        Rectangle pickupRange = new Rectangle(
            (int)posX - 40,
            (int)posY - 60,
            80,
            80
        );

        for (Entity e : entities) {
            if (e instanceof ItemEntity) {
                ItemEntity item = (ItemEntity) e;
                if (!item.isCollected() && pickupRange.intersects(item.getBounds())) {
                    // Try to pick up the item
                    Item linkedItem = item.getLinkedItem();
                    if (linkedItem == null) {
                        // Create a basic item from the entity
                        linkedItem = createItemFromEntity(item);
                    }

                    if (linkedItem != null && canEquipItem(linkedItem)) {
                        // Add to inventory or equip
                        if (addToInventory(linkedItem)) {
                            item.collect();

                            // Auto-equip if slot is empty
                            if (linkedItem.getCategory() == Item.ItemCategory.WEAPON ||
                                linkedItem.getCategory() == Item.ItemCategory.RANGED_WEAPON) {
                                if (equippedWeapon == null) {
                                    equipWeapon(linkedItem);
                                }
                            } else if (linkedItem.getCategory() == Item.ItemCategory.ARMOR) {
                                if (equippedArmor == null) {
                                    equipArmor(linkedItem);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates an Item from an ItemEntity.
     */
    protected Item createItemFromEntity(ItemEntity entity) {
        String name = entity.getItemName().toLowerCase().replace(" ", "_");
        Item item = ItemRegistry.create(name);
        if (item != null) {
            entity.setLinkedItem(item);
            return item;
        }

        // Create basic item
        Item.ItemCategory category = Item.ItemCategory.MATERIAL;
        String type = entity.getItemType().toLowerCase();
        switch (type) {
            case "weapon": category = Item.ItemCategory.WEAPON; break;
            case "ranged_weapon":
            case "bow": category = Item.ItemCategory.RANGED_WEAPON; break;
            case "armor": category = Item.ItemCategory.ARMOR; break;
            case "food": category = Item.ItemCategory.FOOD; break;
        }

        Item basicItem = new Item(entity.getItemName(), category);
        basicItem.setDamage(8);
        entity.setLinkedItem(basicItem);
        return basicItem;
    }

    /**
     * Checks if this mob can equip the given item.
     */
    protected boolean canEquipItem(Item item) {
        if (!isHumanoid) {
            // Quadrupeds can only use certain items
            return item.getCategory() == Item.ItemCategory.FOOD ||
                   item.getCategory() == Item.ItemCategory.MATERIAL;
        }
        return true;
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

        // Jumping/falling takes priority over ground animations
        if (isJumping) {
            if (currentJumpNumber == 2 && spriteAnimation.hasAnimation(SpriteAnimation.ActionState.DOUBLE_JUMP)) {
                setAnimationState("double_jump");
            } else if (currentJumpNumber == 3 && spriteAnimation.hasAnimation(SpriteAnimation.ActionState.TRIPLE_JUMP)) {
                setAnimationState("triple_jump");
            } else {
                setAnimationState("jump");
            }
            return;
        }
        if (isFalling) {
            setAnimationState("fall");
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

        // Draw status effect overlay
        if (activeEffect != StatusEffect.NONE && effectTintColor != null) {
            // Pulsing effect based on timer
            float pulse = (float)(0.8 + 0.2 * Math.sin(effectTimer * 8));
            int alpha = (int)(effectTintAlpha * 255 * pulse);
            g2d.setColor(new Color(
                effectTintColor.getRed(),
                effectTintColor.getGreen(),
                effectTintColor.getBlue(),
                Math.min(255, alpha)
            ));
            g2d.fillRect(drawX, drawY, spriteWidth, spriteHeight);

            // Draw effect particles
            drawStatusEffectParticles(g2d, drawX, drawY);
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

    /**
     * Draws particle effects for status effects using GIF overlays.
     * Falls back to procedural drawing if GIF not available.
     */
    protected void drawStatusEffectParticles(Graphics2D g2d, int drawX, int drawY) {
        AnimatedTexture particles = null;

        switch (activeEffect) {
            case BURNING:
                particles = fireParticles;
                break;
            case FROZEN:
                particles = iceParticles;
                break;
            case POISONED:
                particles = poisonParticles;
                break;
            default:
                return;
        }

        // If we have a particle GIF, draw it as overlay
        if (particles != null) {
            // Update particle animation
            long elapsed = System.currentTimeMillis() % 1000;
            particles.update(elapsed);

            java.awt.image.BufferedImage particleFrame = particles.getCurrentFrame();
            if (particleFrame != null) {
                // Draw particle overlay scaled to mob size
                // Use semi-transparency for overlay effect
                java.awt.Composite originalComposite = g2d.getComposite();
                g2d.setComposite(java.awt.AlphaComposite.getInstance(
                    java.awt.AlphaComposite.SRC_OVER, 0.85f));

                // Scale and position particle overlay to match mob sprite
                g2d.drawImage(particleFrame,
                    drawX, drawY, spriteWidth, spriteHeight, null);

                g2d.setComposite(originalComposite);
            }
        } else {
            // Fallback to procedural particles if GIF not loaded
            drawProceduralParticles(g2d, drawX, drawY);
        }
    }

    /**
     * Fallback procedural particle drawing for status effects.
     */
    protected void drawProceduralParticles(Graphics2D g2d, int drawX, int drawY) {
        double animTime = System.currentTimeMillis() / 100.0;

        switch (activeEffect) {
            case BURNING:
                // Fire sparks rising upward
                for (int i = 0; i < 6; i++) {
                    double offset = (animTime + i * 1.5) % 4.0;
                    int sparkX = drawX + (int)((Math.sin(animTime * 0.5 + i * 1.2) + 1) * spriteWidth / 2);
                    int sparkY = drawY + spriteHeight - (int)(offset * spriteHeight / 3);
                    int sparkSize = 3 + (int)(Math.random() * 3);

                    int red = 255;
                    int green = 150 + (int)(Math.random() * 100);
                    int alpha = (int)(200 * (1.0 - offset / 4.0));
                    g2d.setColor(new Color(red, green, 0, Math.max(0, alpha)));
                    g2d.fillOval(sparkX, sparkY, sparkSize, sparkSize);
                }
                break;

            case FROZEN:
                // Ice crystals / snowflakes
                for (int i = 0; i < 5; i++) {
                    double offset = (animTime * 0.3 + i * 0.8) % 3.0;
                    int iceX = drawX + (int)((Math.sin(animTime * 0.3 + i) + 1) * spriteWidth / 2);
                    int iceY = drawY + (int)(offset * spriteHeight / 2);
                    int iceSize = 4 + (int)(Math.random() * 3);

                    int alpha = (int)(180 * (1.0 - offset / 3.0));
                    g2d.setColor(new Color(200, 230, 255, Math.max(0, alpha)));
                    g2d.drawLine(iceX, iceY - iceSize/2, iceX, iceY + iceSize/2);
                    g2d.drawLine(iceX - iceSize/2, iceY, iceX + iceSize/2, iceY);
                    g2d.drawLine(iceX - iceSize/3, iceY - iceSize/3, iceX + iceSize/3, iceY + iceSize/3);
                }
                break;

            case POISONED:
                // Poison bubbles rising
                for (int i = 0; i < 4; i++) {
                    double offset = (animTime * 0.4 + i * 1.0) % 3.5;
                    int bubbleX = drawX + (int)((Math.sin(animTime * 0.4 + i * 1.5) + 1) * spriteWidth / 2);
                    int bubbleY = drawY + spriteHeight - (int)(offset * spriteHeight / 2.5);
                    int bubbleSize = 4 + (int)(Math.sin(animTime + i) * 2);

                    int alpha = (int)(150 * (1.0 - offset / 3.5));
                    g2d.setColor(new Color(100, 200, 50, Math.max(0, alpha)));
                    g2d.fillOval(bubbleX, bubbleY, bubbleSize, bubbleSize);
                    g2d.setColor(new Color(150, 255, 100, Math.max(0, alpha / 2)));
                    g2d.drawOval(bubbleX, bubbleY, bubbleSize, bubbleSize);
                }
                break;

            default:
                break;
        }
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
