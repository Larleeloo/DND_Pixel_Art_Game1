package com.ambermoongame.entity.player;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.ambermoongame.entity.Entity;
import com.ambermoongame.entity.MeleeAttackHitbox;
import com.ambermoongame.entity.SpriteEntity;
import com.ambermoongame.entity.capabilities.BlockInteractionHandler;
import com.ambermoongame.entity.capabilities.BlockInteractionHelper;
import com.ambermoongame.entity.capabilities.CombatCapable;
import com.ambermoongame.entity.capabilities.ResourceManager;
import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.item.ItemEntity;
import com.ambermoongame.entity.item.ItemRegistry;
import com.ambermoongame.entity.item.MirrorToOtherRealms;
import com.ambermoongame.entity.player.AbilityScores;
import com.ambermoongame.entity.player.AbilityScores.DexterityResult;
import com.ambermoongame.block.BlockEntity;
import com.ambermoongame.graphics.AndroidAssetLoader;
import com.ambermoongame.input.TouchInputManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * SpritePlayerEntity is a player implementation that uses sprite/GIF-based animation
 * with support for equipment overlays. This is the primary player class for the Android port.
 *
 * Conversion notes:
 * - java.awt.Graphics/Graphics2D  -> android.graphics.Canvas + Paint
 * - java.awt.Rectangle            -> android.graphics.Rect
 * - java.awt.Color                -> android.graphics.Color (int)
 * - java.awt.Font/FontMetrics     -> Paint.setTextSize()/measureText()
 * - java.awt.BasicStroke          -> Paint.setStrokeWidth()
 * - java.awt.RenderingHints       -> Paint flags (FILTER_BITMAP_FLAG = false for pixel art)
 * - java.awt.geom.AffineTransform -> Canvas.scale()/Canvas.translate()
 * - java.awt.geom.Arc2D           -> Canvas.drawArc(RectF, ...)
 * - InputManager                  -> TouchInputManager
 * - System.out/err.println        -> Log.d()/Log.e()
 *
 * Dependencies not yet ported (commented out):
 * - SpriteAnimation / EquipmentOverlay (Tier 7: Animation System)
 * - ProjectileEntity (separate entity port)
 * - Inventory (Tier 9: UI Components)
 * - AudioManager (Android audio system)
 * - Camera (Tier 8: Graphics & Camera)
 * - ControllerManager / VibrationPattern (input system)
 * - TriggeredAnimationManager / ParticleAnimationState (Tier 7)
 * - SpriteCharacterCustomization (scene system)
 *
 * Features:
 * - GIF animations for idle, walk, and jump actions (pending SpriteAnimation port)
 * - Equipment overlay system for clothing/armor (pending EquipmentOverlay port)
 * - Full physics with gravity, multi-jump, and collision
 * - Block interaction (mining and placement) via BlockInteractionHandler
 * - Resource management (mana, stamina) via ResourceManager
 * - Combat capabilities via CombatCapable
 * - DnD ability score modifiers for damage, health, dexterity effects
 * - Sprint system with stamina drain
 * - Charged shot system for ranged weapons
 */
public class SpritePlayerEntity extends Entity implements PlayerBase,
        BlockInteractionHandler, ResourceManager, CombatCapable {

    private static final String TAG = "SpritePlayerEntity";

    // --- Uncomment when SpriteAnimation/EquipmentOverlay are ported (Tier 7) ---
    // private SpriteAnimation spriteAnimation;
    // private EquipmentOverlay equipmentOverlay;

    // Temporary placeholder for sprite rendering before animation system is ported
    private Bitmap currentSprite;
    private AndroidAssetLoader.ImageAsset idleAsset;
    private AndroidAssetLoader.ImageAsset walkAsset;
    private AndroidAssetLoader.ImageAsset jumpAsset;
    private long animStartTime = System.currentTimeMillis();
    private String currentAnimState = "idle";

    // Physics
    private double velX = 0;
    private double velY = 0;
    private double pushX = 0; // External push force
    private double pushY = 0;
    private final double gravity = 0.5;
    private final double jumpStrength = -10;
    private boolean onGround = false;
    private boolean facingRight = true;
    private int airTime = 0;

    // Multi-jump system
    private int maxJumps = 3;           // Allows up to triple jump
    private int jumpsRemaining = 3;
    private int currentJumpNumber = 0;  // 0 = not jumping, 1 = first jump, 2 = double, 3 = triple
    private double doubleJumpStrength = -9;
    private double tripleJumpStrength = -9;

    // Sprint system
    private boolean isSprinting = false;
    private double walkSpeed = 4;
    private double sprintSpeed = 7;
    private double sprintStaminaCost = 0.5;

    // Projectile system
    // --- Uncomment when ProjectileEntity is ported ---
    // private List<ProjectileEntity> activeProjectiles = new ArrayList<>();
    private Item heldItem = null;
    private boolean isFiring = false;
    private double fireTimer = 0;
    private double fireCooldown = 0.5;
    private double fireDuration = 0.3;

    // Aim system for touch-directed projectiles
    // --- Uncomment when Camera is ported (Tier 8) ---
    // private Camera camera;
    private double aimAngle = 0;
    private double aimDirX = 1.0;
    private double aimDirY = 0;
    private int aimTargetWorldX = 0;
    private int aimTargetWorldY = 0;
    private static final int AIM_INDICATOR_LENGTH = 60;
    private static final int AIM_INDICATOR_WIDTH = 3;

    // Charged shot system
    private boolean isCharging = false;
    private double chargeTimer = 0;
    private double chargePercent = 0;
    private boolean chargeReady = false;
    private static final int CHARGE_BAR_WIDTH = 50;
    private static final int CHARGE_BAR_HEIGHT = 8;

    // Item usage system
    private boolean isUsingItem = false;
    private double useItemTimer = 0;
    private double useItemDuration = 0.5;

    // Eating system
    private boolean isEating = false;
    private double eatTimer = 0;
    private double eatDuration = 1.5;

    // Attack system
    private boolean isAttacking = false;
    private double attackTimer = 0;
    private double attackCooldown = 0.4;
    private double attackDuration = 0.2;
    private int baseAttackDamage = 5;
    private int baseAttackRange = 40;
    private int attackWidth = 40;
    private int attackHeight = 50;

    // Dynamic attack direction system (360-degree touch-following)
    private double attackDirX = 1.0;
    private double attackDirY = 0;
    private double attackAngle = 0;
    private MeleeAttackHitbox currentAttackHitbox = null;
    private boolean showMeleeAimIndicator = false;

    // Dimensions
    private int width;
    private int height;
    private static final int SCALE = 2;

    // Health system
    private int maxHealth = 100;
    private int currentHealth = 100;
    private double invincibilityTime = 1.0;
    private double invincibilityTimer = 0;

    // Mana system
    private int maxMana = 100;
    private int currentMana = 100;
    private double manaRegenRate = 5.0;

    // Stamina system
    private int maxStamina = 100;
    private double currentStaminaFloat = 100.0;
    private int currentStamina = 100;
    private double staminaRegenRate = 15.0;
    private double staminaDrainRate = 20.0;

    // Systems
    // --- Uncomment when Inventory is ported (Tier 9) ---
    // private Inventory inventory;
    // --- Uncomment when AudioManager integration is finalized ---
    // private AudioManager audioManager;

    // Ground level
    private int groundY = 720;

    // Ability scores system
    private AbilityScores abilityScores;
    private int baseMaxHealth = 100;
    private int baseMaxMana = 100;
    private int baseMaxStamina = 100;

    // Block interaction - uses helper for shared functionality
    private BlockInteractionHelper blockHelper = new BlockInteractionHelper();
    private BlockEntity lastBrokenBlock = null;
    private ItemEntity lastDroppedItem = null;

    // Block placement preview state
    private boolean showPlacementPreview = false;
    private int previewGridX = 0;
    private int previewGridY = 0;
    private boolean previewCanPlace = false;

    // Timing for animation updates
    private long lastUpdateTime;

    // --- Uncomment when TriggeredAnimationManager is ported (Tier 7) ---
    // private TriggeredAnimationManager triggeredAnimManager;
    private double sprintParticleTimer = 0;
    private double jumpParticleTimer = 0;
    private boolean wasOnGround = true;
    private static final double SPRINT_PARTICLE_INTERVAL = 0.15;
    private static final double JUMP_PARTICLE_COOLDOWN = 0.3;

    // Reusable drawing objects
    private final Paint drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();

    /**
     * Creates a SpritePlayerEntity with animations loaded from a directory.
     *
     * @param x Starting X position
     * @param y Starting Y position
     * @param spriteDir Directory containing idle.gif, walk.gif, jump.gif
     */
    public SpritePlayerEntity(int x, int y, String spriteDir) {
        super(x, y);

        // Load animations from directory (simplified until SpriteAnimation is ported)
        loadAnimations(spriteDir);

        // Set dimensions based on loaded animation
        if (idleAsset != null && idleAsset.bitmap != null) {
            this.width = idleAsset.width * SCALE;
            this.height = idleAsset.height * SCALE;
        } else {
            // Default dimensions (32x64 base * 2 = 64x128)
            this.width = 64;
            this.height = 128;
        }

        // --- Uncomment when Inventory is ported ---
        // this.inventory = new Inventory(10);

        this.lastUpdateTime = System.currentTimeMillis();

        // --- Uncomment when TriggeredAnimationManager is ported ---
        // this.triggeredAnimManager = TriggeredAnimationManager.getInstance();

        // Initialize ability scores from selected character
        initializeAbilityScores();
    }

    /**
     * Initializes ability scores from the currently selected character.
     * Applies constitution modifier to max health.
     */
    private void initializeAbilityScores() {
        // Get ability scores from selected character
        this.abilityScores = PlayableCharacterRegistry.getSelectedAbilityScores();

        // Apply constitution modifier to max health
        applyConstitutionModifiers();
    }

    /**
     * Applies constitution-based modifiers to health and other stats.
     */
    private void applyConstitutionModifiers() {
        if (abilityScores == null) return;

        double healthMod = abilityScores.getHealthModifier();
        this.maxHealth = (int) Math.max(1, baseMaxHealth * healthMod);
        this.currentHealth = Math.min(currentHealth, maxHealth);

        Log.d(TAG, "Applied constitution modifier (" +
            abilityScores.getConstitution() + ") - MaxHealth: " + maxHealth);
    }

    /** Sets ability scores directly (used for Loot Game and testing). */
    public void setAbilityScores(AbilityScores scores) {
        this.abilityScores = scores;
        applyConstitutionModifiers();
    }

    /** Gets the current ability scores. */
    public AbilityScores getAbilityScores() {
        return abilityScores;
    }

    /**
     * Loads animations from a sprite directory.
     * Simplified version until SpriteAnimation system is ported (Tier 7).
     * Loads idle, walk, and jump GIFs as ImageAssets.
     */
    private void loadAnimations(String spriteDir) {
        String basePath = spriteDir.endsWith("/") ? spriteDir : spriteDir + "/";

        // Load core animations as ImageAssets
        idleAsset = AndroidAssetLoader.load(basePath + "idle.gif");
        walkAsset = AndroidAssetLoader.load(basePath + "walk.gif");
        jumpAsset = AndroidAssetLoader.load(basePath + "jump.gif");

        // Set current sprite from idle
        if (idleAsset != null) {
            currentSprite = idleAsset.bitmap;
        }

        Log.d(TAG, "Loaded animations from " + spriteDir +
            " (idle:" + (idleAsset != null) +
            ", walk:" + (walkAsset != null) +
            ", jump:" + (jumpAsset != null) + ")");
    }

    /** Sets the skin tone tint for the base player sprite. */
    public void setSkinTone(int skinToneColor) {
        // --- Uncomment when SpriteAnimation is ported ---
        // if (spriteAnimation != null) {
        //     spriteAnimation.setTint(skinToneColor);
        // }
    }

    @Override
    public void setGroundY(int groundY) {
        this.groundY = groundY;
    }

    // --- Uncomment when AudioManager integration is finalized ---
    // public void setAudioManager(AudioManager audioManager) {
    //     this.audioManager = audioManager;
    // }

    // --- Uncomment when Camera is ported (Tier 8) ---
    // public void setCamera(Camera camera) {
    //     this.camera = camera;
    // }

    /**
     * Updates the aim direction based on touch position.
     * On Android, aim direction is set externally from touch events
     * rather than polling mouse position each frame.
     */
    public void setAimDirection(double dirX, double dirY) {
        double length = Math.sqrt(dirX * dirX + dirY * dirY);
        if (length > 0) {
            aimDirX = dirX / length;
            aimDirY = dirY / length;
            aimAngle = Math.atan2(dirY, dirX);

            if (dirX > 0) {
                facingRight = true;
            } else if (dirX < 0) {
                facingRight = false;
            }
        }
    }

    /**
     * Sets the aim target in world coordinates (from touch-to-world conversion).
     */
    public void setAimTarget(int worldX, int worldY) {
        aimTargetWorldX = worldX;
        aimTargetWorldY = worldY;

        int playerCenterX = x + width / 2;
        int playerCenterY = y + height / 3;

        double dx = worldX - playerCenterX;
        double dy = worldY - playerCenterY;
        setAimDirection(dx, dy);
    }

    @Override
    public void update(TouchInputManager input, ArrayList<Entity> entities) {
        long currentTime = System.currentTimeMillis();
        long deltaMs = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;
        double deltaSeconds = deltaMs / 1000.0;

        // Update invincibility timer
        if (invincibilityTimer > 0) {
            invincibilityTimer -= deltaSeconds;
        }

        // Update action timers
        updateActionTimers(deltaSeconds);

        // Regenerate mana and stamina
        if (!isSprinting) {
            currentStaminaFloat = Math.min(maxStamina, currentStaminaFloat + staminaRegenRate * deltaSeconds);
        }
        currentStamina = (int) currentStaminaFloat;
        currentMana = Math.min(maxMana, currentMana + (int)(manaRegenRate * deltaSeconds));

        int newX = x;
        int newY = y;
        boolean isMoving = false;

        // --- Touch-based input handling ---
        // Movement is handled via virtual joystick or touch areas
        // The TouchInputManager provides left/right/jump/sprint state

        // Sprinting (touch sprint button drains stamina)
        boolean wantsSprint = input.isSprintActive();
        boolean movingHorizontally = input.isMoveLeftActive() || input.isMoveRightActive();

        if (wantsSprint && currentStaminaFloat > 0 && movingHorizontally) {
            isSprinting = true;
            currentStaminaFloat = Math.max(0, currentStaminaFloat - staminaDrainRate * deltaSeconds);
            currentStamina = (int) currentStaminaFloat;
        } else {
            isSprinting = false;
        }

        double speed = isSprinting ? sprintSpeed : walkSpeed;

        // Horizontal movement
        if (input.isMoveLeftActive()) {
            newX -= (int)speed;
            facingRight = false;
            isMoving = true;
        }
        if (input.isMoveRightActive()) {
            newX += (int)speed;
            facingRight = true;
            isMoving = true;
        }

        // Check horizontal collision
        Rect futureXBounds = new Rect(newX, y, newX + width, y + height);
        boolean xCollision = false;

        for (Entity e : entities) {
            if (e == this) continue;

            boolean isSolid = false;
            if (e instanceof SpriteEntity && ((SpriteEntity) e).isSolid()) {
                isSolid = true;
            } else if (e instanceof BlockEntity && ((BlockEntity) e).isSolid()) {
                isSolid = true;
            }

            if (isSolid && Rect.intersects(futureXBounds, e.getBounds())) {
                xCollision = true;
                break;
            }
        }

        if (!xCollision) {
            x = newX;
        }

        // Apply push forces (with collision check)
        if (Math.abs(pushX) > 0.1) {
            int pushAmount = (int) pushX;
            Rect pushedBounds = new Rect(x + pushAmount, y, x + pushAmount + width, y + height);
            boolean pushBlocked = false;

            for (Entity e : entities) {
                if (e == this) continue;
                if (e instanceof BlockEntity) {
                    BlockEntity block = (BlockEntity) e;
                    if (block.isSolid() && !block.isBroken() && Rect.intersects(pushedBounds, block.getBounds())) {
                        pushBlocked = true;
                        break;
                    }
                }
            }

            if (!pushBlocked) {
                x += pushAmount;
            }
            pushX *= 0.8;
        } else {
            pushX = 0;
        }

        // Check for item collection
        Rect playerBounds = new Rect(x, y, x + width, y + height);
        for (Entity e : entities) {
            if (e instanceof ItemEntity) {
                ItemEntity item = (ItemEntity) e;
                if (!item.isCollected() && Rect.intersects(playerBounds, e.getBounds())) {
                    item.collect();
                    // --- Uncomment when Inventory is ported ---
                    // boolean added = inventory.addItem(item);
                    // if (audioManager != null) {
                    //     audioManager.playSound("collect");
                    // }
                }
            }
        }

        // Multi-jump system
        handleJumping(input);

        // Apply gravity
        velY += gravity;
        newY = y + (int)velY;

        // Check vertical collision
        Rect futureYBounds = new Rect(x, newY, x + width, newY + height);
        boolean foundPlatform = false;

        for (Entity e : entities) {
            if (e == this) continue;

            boolean isSolid = false;
            if (e instanceof SpriteEntity && ((SpriteEntity) e).isSolid()) {
                isSolid = true;
            } else if (e instanceof BlockEntity && ((BlockEntity) e).isSolid()) {
                isSolid = true;
            }

            if (isSolid) {
                Rect platformBounds = e.getBounds();

                if (Rect.intersects(futureYBounds, platformBounds)) {
                    if (velY > 0) {
                        newY = platformBounds.top - height;
                        velY = 0;
                        onGround = true;
                        foundPlatform = true;
                        jumpsRemaining = maxJumps;
                        currentJumpNumber = 0;
                    } else if (velY < 0) {
                        newY = platformBounds.bottom;
                        velY = 0;
                    }
                    break;
                }
            }
        }

        // Check ground collision
        if (newY + height >= groundY) {
            newY = groundY - height;
            velY = 0;
            onGround = true;
            jumpsRemaining = maxJumps;
            currentJumpNumber = 0;
        } else if (!foundPlatform) {
            onGround = false;
        }

        y = newY;

        // Track air time
        if (onGround) {
            airTime = 0;
        } else {
            airTime++;
        }

        // --- Uncomment when ProjectileEntity is ported ---
        // updateProjectiles(deltaSeconds, entities);

        // Update animation state
        updateAnimationState(isMoving);

        // Update current sprite frame from ImageAsset
        updateSpriteFrame();

        // Track ground state for particles
        wasOnGround = onGround;
    }

    // Overload for Entity.update(TouchInputManager) base compatibility
    @Override
    public void update(TouchInputManager input) {
        // This overload exists for Entity base class compatibility.
        // The primary update method is update(TouchInputManager, ArrayList<Entity>).
    }

    /**
     * Updates the current sprite frame from the active ImageAsset.
     */
    private void updateSpriteFrame() {
        long elapsed = System.currentTimeMillis() - animStartTime;
        AndroidAssetLoader.ImageAsset activeAsset = null;

        switch (currentAnimState) {
            case "walk":
            case "run":
            case "sprint":
                activeAsset = walkAsset;
                break;
            case "jump":
            case "double_jump":
            case "triple_jump":
            case "fall":
                activeAsset = jumpAsset;
                break;
            default:
                activeAsset = idleAsset;
                break;
        }

        if (activeAsset != null) {
            Bitmap frame = activeAsset.getFrame(elapsed);
            if (frame != null) {
                currentSprite = frame;
            }
        }
    }

    /**
     * Handles multi-jump input.
     * Uses isJumpJustPressed for immediate response on touch.
     */
    private void handleJumping(TouchInputManager input) {
        boolean jumpPressed = input.isJumpJustPressed();

        // Check if holding The Ruby Skull for unlimited jumps
        boolean hasUnlimitedJumps = heldItem != null &&
            "The Ruby Skull".equals(heldItem.getName());

        if (jumpPressed && (jumpsRemaining > 0 || hasUnlimitedJumps)) {
            if (onGround) {
                velY = jumpStrength;
                onGround = false;
                if (!hasUnlimitedJumps) {
                    jumpsRemaining--;
                }
                currentJumpNumber = 1;

                // --- Uncomment when TriggeredAnimationManager is ported ---
                // triggeredAnimManager.triggerJumpDust(x + width / 2, y + height);

                // --- Uncomment when ControllerManager is ported ---
                // ControllerManager controller = ControllerManager.getInstance();
                // if (controller.isVibrationSupported()) {
                //     controller.vibrate(VibrationPattern.GREATER_JUMP);
                // }

                // --- Uncomment when AudioManager is ported ---
                // if (audioManager != null) {
                //     audioManager.playSound("jump");
                // }
            } else if (jumpsRemaining > 0 || hasUnlimitedJumps) {
                if (currentJumpNumber == 0) {
                    currentJumpNumber = hasUnlimitedJumps ? 3 : 1;
                    velY = doubleJumpStrength;
                } else {
                    if (hasUnlimitedJumps) {
                        currentJumpNumber = 3;
                        velY = tripleJumpStrength;
                    } else {
                        currentJumpNumber++;
                        if (currentJumpNumber == 2) {
                            velY = doubleJumpStrength;
                        } else if (currentJumpNumber == 3) {
                            velY = tripleJumpStrength;
                        }
                    }
                }

                // --- Uncomment when TriggeredAnimationManager is ported ---
                // triggeredAnimManager.triggerParticle(
                //     ParticleAnimationState.MULTI_JUMP,
                //     x + width / 2 - 20, y + height / 2 - 10,
                //     40, 20, null, 0.4, false
                // );

                if (!hasUnlimitedJumps) {
                    jumpsRemaining--;
                }

                // --- Uncomment when AudioManager is ported ---
                // if (audioManager != null) {
                //     audioManager.playSound("jump");
                // }
            }
        }
    }

    /**
     * Updates action timers for attack, fire, eat, use item.
     */
    private void updateActionTimers(double deltaSeconds) {
        if (attackTimer > 0) {
            attackTimer -= deltaSeconds;
        }
        if (isAttacking && attackTimer <= attackCooldown - attackDuration) {
            isAttacking = false;
            currentAttackHitbox = null;
        }

        if (fireTimer > 0) {
            fireTimer -= deltaSeconds;
        }
        if (isFiring && fireTimer <= fireCooldown - fireDuration) {
            isFiring = false;
        }

        if (isEating) {
            eatTimer -= deltaSeconds;
            if (eatTimer <= 0) {
                finishEating();
            }
        }

        if (isUsingItem) {
            useItemTimer -= deltaSeconds;
            if (useItemTimer <= 0) {
                finishUsingItem();
            }
        }
    }

    // --- Uncomment when ProjectileEntity is ported ---
    // private void fireProjectile(ArrayList<Entity> entities) { ... }
    // private void fireMirrorProjectiles(ArrayList<Entity> entities, MirrorToOtherRealms mirror) { ... }
    // private void startCharging() { ... }
    // private void updateCharging(double deltaSeconds) { ... }
    // private void cancelCharge() { ... }
    // private void fireChargedProjectile(ArrayList<Entity> entities) { ... }
    // private void updateProjectiles(double deltaSeconds, ArrayList<Entity> entities) { ... }

    /** Starts eating/consuming a held item. */
    private void startEating() {
        if (heldItem == null || !heldItem.isConsumable()) return;
        if (isEating) return;

        isEating = true;
        eatTimer = heldItem.getConsumeTime();
        eatDuration = heldItem.getConsumeTime();
    }

    /** Finishes eating and applies effects. */
    private void finishEating() {
        if (heldItem == null) return;

        currentHealth = Math.min(maxHealth, currentHealth + heldItem.getHealthRestore());
        currentMana = Math.min(maxMana, currentMana + heldItem.getManaRestore());
        currentStamina = Math.min(maxStamina, currentStamina + heldItem.getStaminaRestore());

        isEating = false;

        // --- Uncomment when Inventory is ported ---
        // int currentSlot = inventory.getSelectedSlot();
        // inventory.removeItemAtSlot(currentSlot);
        // syncHeldItemWithInventory();
        //
        // if (audioManager != null) {
        //     audioManager.playSound("eat");
        // }
    }

    /** Starts using a non-consumable item. */
    private void startUsingItem() {
        if (heldItem == null) return;
        if (isUsingItem) return;
        isUsingItem = true;
        useItemTimer = useItemDuration;
    }

    /** Finishes using an item. */
    private void finishUsingItem() {
        isUsingItem = false;
    }

    /**
     * Updates the animation state based on player movement and actions.
     * Simplified version until SpriteAnimation system is ported.
     */
    private void updateAnimationState(boolean isMoving) {
        String newState;

        if (isEating) {
            newState = "eat";
        } else if (isFiring) {
            newState = "fire";
        } else if (isUsingItem) {
            newState = "use_item";
        } else if (isAttacking) {
            newState = "attack";
        } else if (airTime > 3) {
            if (velY < 0) {
                if (currentJumpNumber == 3) {
                    newState = "triple_jump";
                } else if (currentJumpNumber == 2) {
                    newState = "double_jump";
                } else {
                    newState = "jump";
                }
            } else {
                newState = "fall";
            }
        } else if (isMoving) {
            if (isSprinting) {
                newState = "sprint";
            } else {
                newState = "walk";
            }
        } else {
            newState = "idle";
        }

        if (!newState.equals(currentAnimState)) {
            currentAnimState = newState;
            animStartTime = System.currentTimeMillis();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        // Invincibility flash effect
        boolean flashHide = invincibilityTimer > 0 && (int)(invincibilityTimer * 10) % 2 == 0;

        if (!flashHide) {
            // --- Uncomment when EquipmentOverlay is ported ---
            // equipmentOverlay.drawBehind(canvas, x, y, width, height, facingRight, currentAnimState);

            // Draw base sprite
            drawSprite(canvas);

            // --- Uncomment when EquipmentOverlay is ported ---
            // equipmentOverlay.drawInFront(canvas, x, y, width, height, facingRight, currentAnimState);

            // --- Uncomment when Inventory/held item drawing is ported ---
            // drawHeldItemAtHand(canvas);
        }

        // Debug: draw bounds
        int boundColor;
        if (airTime > 3) {
            boundColor = Color.rgb(255, 165, 0); // Orange
        } else if (isSprinting) {
            boundColor = Color.CYAN;
        } else {
            boundColor = Color.RED;
        }
        drawPaint.setColor(boundColor);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(2);
        canvas.drawRect(x, y, x + width, y + height, drawPaint);
        drawPaint.setStyle(Paint.Style.FILL);

        // Draw attack hitbox when attacking
        if (isAttacking) {
            MeleeAttackHitbox meleeHitbox = getMeleeAttackHitbox();
            if (meleeHitbox != null) {
                meleeHitbox.draw(canvas,
                    Color.argb(80, 255, 100, 100),
                    Color.argb(200, 255, 50, 50)
                );
            } else {
                Rect attackBounds = getAttackBounds();
                if (attackBounds != null) {
                    drawPaint.setColor(Color.argb(100, 255, 100, 100));
                    canvas.drawRect(attackBounds, drawPaint);
                    drawPaint.setColor(Color.argb(200, 255, 50, 50));
                    drawPaint.setStyle(Paint.Style.STROKE);
                    drawPaint.setStrokeWidth(2);
                    canvas.drawRect(attackBounds, drawPaint);
                    drawPaint.setStyle(Paint.Style.FILL);
                }
            }
        }

        // Draw eating progress bar
        if (isEating) {
            int barWidth = 40;
            int barHeight = 6;
            int barX = x + (width - barWidth) / 2;
            int barY = y - 15;
            float progress = 1.0f - (float)(eatTimer / eatDuration);

            drawPaint.setColor(Color.rgb(50, 50, 50));
            canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, drawPaint);
            drawPaint.setColor(Color.rgb(100, 200, 100));
            canvas.drawRect(barX, barY, barX + (int)(barWidth * progress), barY + barHeight, drawPaint);
            drawPaint.setColor(Color.WHITE);
            drawPaint.setStyle(Paint.Style.STROKE);
            drawPaint.setStrokeWidth(1);
            canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, drawPaint);
            drawPaint.setStyle(Paint.Style.FILL);
        }

        // Draw charge bar when charging a shot
        if (isCharging) {
            drawChargeBar(canvas);
        }

        // Draw aim indicator when holding a ranged weapon
        if (heldItem != null && heldItem.isRangedWeapon()) {
            drawAimIndicator(canvas);
        }

        // --- Uncomment when TriggeredAnimationManager is ported ---
        // triggeredAnimManager.drawParticles(canvas);
    }

    /**
     * Draws the player sprite with facing direction support.
     */
    private void drawSprite(Canvas canvas) {
        if (currentSprite == null) {
            // Fallback: draw a colored rectangle
            drawPaint.setColor(Color.rgb(100, 150, 255));
            canvas.drawRect(x, y, x + width, y + height, drawPaint);
            return;
        }

        srcRect.set(0, 0, currentSprite.getWidth(), currentSprite.getHeight());

        if (!facingRight) {
            // Flip horizontally by using negative scale
            canvas.save();
            canvas.scale(-1, 1, x + width / 2f, y + height / 2f);
            dstRect.set(x, y, x + width, y + height);
            canvas.drawBitmap(currentSprite, srcRect, dstRect, null);
            canvas.restore();
        } else {
            dstRect.set(x, y, x + width, y + height);
            canvas.drawBitmap(currentSprite, srcRect, dstRect, null);
        }
    }

    /**
     * Draws the aim indicator showing projectile trajectory direction.
     */
    private void drawAimIndicator(Canvas canvas) {
        int startX = x + width / 2;
        int startY = y + height / 3;

        int endX = startX + (int)(aimDirX * AIM_INDICATOR_LENGTH);
        int endY = startY + (int)(aimDirY * AIM_INDICATOR_LENGTH);

        boolean canFire = fireTimer <= 0;

        // Outer shadow
        drawPaint.setColor(Color.argb(120, 0, 0, 0));
        drawPaint.setStrokeWidth(AIM_INDICATOR_WIDTH + 2);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(startX, startY, endX, endY, drawPaint);

        // Main aim line
        int aimColor = canFire ? Color.argb(200, 255, 200, 50) : Color.argb(150, 150, 150, 150);
        drawPaint.setColor(aimColor);
        drawPaint.setStrokeWidth(AIM_INDICATOR_WIDTH);
        canvas.drawLine(startX, startY, endX, endY, drawPaint);
    }

    /**
     * Draws the charge bar above the player.
     */
    private void drawChargeBar(Canvas canvas) {
        int barX = x + (width - CHARGE_BAR_WIDTH) / 2;
        int barY = y - 20;

        // Background
        drawPaint.setColor(Color.argb(200, 40, 40, 40));
        canvas.drawRect(barX - 1, barY - 1, barX + CHARGE_BAR_WIDTH + 1, barY + CHARGE_BAR_HEIGHT + 1, drawPaint);

        // Unfilled
        drawPaint.setColor(Color.rgb(60, 60, 60));
        canvas.drawRect(barX, barY, barX + CHARGE_BAR_WIDTH, barY + CHARGE_BAR_HEIGHT, drawPaint);

        // Filled portion with color gradient
        int fillWidth = (int)(CHARGE_BAR_WIDTH * chargePercent);
        int chargeColor;
        if (chargePercent < 0.5) {
            float t = (float)(chargePercent * 2);
            chargeColor = Color.rgb(255, (int)(255 - 55 * t), (int)(50 - 50 * t));
        } else {
            float t = (float)((chargePercent - 0.5) * 2);
            chargeColor = Color.rgb(255, (int)(200 - 150 * t), 0);
        }
        drawPaint.setColor(chargeColor);
        canvas.drawRect(barX, barY, barX + fillWidth, barY + CHARGE_BAR_HEIGHT, drawPaint);

        // Glow at full charge
        if (chargePercent >= 1.0) {
            drawPaint.setColor(Color.argb(100, 255, 255, 200));
            canvas.drawRect(barX - 2, barY - 2, barX + CHARGE_BAR_WIDTH + 2, barY + CHARGE_BAR_HEIGHT + 2, drawPaint);
        }

        // Border
        drawPaint.setColor(chargeReady ? Color.WHITE : Color.rgb(150, 150, 150));
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeWidth(1);
        canvas.drawRect(barX, barY, barX + CHARGE_BAR_WIDTH, barY + CHARGE_BAR_HEIGHT, drawPaint);
        drawPaint.setStyle(Paint.Style.FILL);

        // Percentage text
        if (chargePercent > 0.1) {
            int percent = (int)(chargePercent * 100);
            String text = percent + "%";
            textPaint.setTextSize(10);
            textPaint.setFakeBoldText(true);
            float textWidth = textPaint.measureText(text);
            int textX = (int)(barX + (CHARGE_BAR_WIDTH - textWidth) / 2);
            int textY = barY + CHARGE_BAR_HEIGHT + 12;

            textPaint.setColor(Color.BLACK);
            canvas.drawText(text, textX + 1, textY + 1, textPaint);
            textPaint.setColor(chargeReady ? Color.WHITE : Color.GRAY);
            canvas.drawText(text, textX, textY, textPaint);
        }
    }

    @Override
    public Rect getBounds() {
        return new Rect(x, y, x + width, y + height);
    }

    // --- Uncomment when Inventory is ported ---
    // public Inventory getInventory() {
    //     return inventory;
    // }

    // --- Uncomment when ItemEntity.dropItem needs to work ---
    // public void dropItem(ItemEntity item) {
    //     int dropOffset = facingRight ? width + 20 : -20 - item.getBounds().width();
    //     item.x = x + dropOffset;
    //     item.y = y + (height / 2);
    //     if (audioManager != null) {
    //         audioManager.playSound("drop");
    //     }
    // }

    @Override
    public boolean isFacingRight() {
        return facingRight;
    }

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    // ==================== Health System ====================

    @Override
    public int getHealth() { return currentHealth; }

    @Override
    public int getMaxHealth() { return maxHealth; }

    @Override
    public boolean isInvincible() { return invincibilityTimer > 0; }

    @Override
    public void takeDamage(int damage, double knockbackX, double knockbackY) {
        if (invincibilityTimer > 0) return;

        currentHealth -= damage;
        if (currentHealth < 0) currentHealth = 0;

        velY += knockbackY;
        invincibilityTimer = invincibilityTime;

        // --- Uncomment when ControllerManager is ported ---
        // ControllerManager controller = ControllerManager.getInstance();
        // if (controller.isVibrationSupported()) {
        //     if (damage > maxHealth / 4) {
        //         controller.vibrate(VibrationPattern.GREATER_CRITICAL_DAMAGE);
        //     } else {
        //         controller.vibrate(VibrationPattern.GREATER_DAMAGE_TAKEN);
        //     }
        //     if (currentHealth <= 0) {
        //         controller.vibrate(VibrationPattern.PLAYER_DEATH);
        //     }
        // }

        // --- Uncomment when AudioManager is ported ---
        // if (audioManager != null) {
        //     audioManager.playSound("hurt");
        // }
    }

    @Override
    public int getMana() { return currentMana; }

    @Override
    public int getMaxMana() { return maxMana; }

    @Override
    public int getStamina() { return currentStamina; }

    @Override
    public int getMaxStamina() { return maxStamina; }

    @Override
    public boolean useMana(int amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            return true;
        }
        return false;
    }

    public boolean useStamina(int amount) {
        if (currentStamina >= amount) {
            currentStamina -= amount;
            return true;
        }
        return false;
    }

    // ==================== Ability Score Methods ====================

    public DexterityResult calculateItemUsageDexterity() {
        if (abilityScores == null) {
            return new DexterityResult(true, true, false);
        }
        return abilityScores.calculateDexterityEffect();
    }

    public int useItemWithDexterity(boolean consumeResources, int resourceAmount, boolean isMana) {
        DexterityResult result = calculateItemUsageDexterity();

        if (consumeResources && result.consumesResources()) {
            if (isMana) {
                if (!useMana(resourceAmount)) return 0;
            } else {
                if (!useStamina(resourceAmount)) return 0;
            }
        }

        if (!result.isSuccess()) {
            Log.d(TAG, "Dexterity check failed! (DEX: " +
                (abilityScores != null ? abilityScores.getDexterity() : 5) + ")");
            return 0;
        }

        if (result.isDoubleUse()) {
            Log.d(TAG, "Dexterity bonus! Double use without extra cost! (DEX: " +
                (abilityScores != null ? abilityScores.getDexterity() : 5) + ")");
            return 2;
        }

        return 1;
    }

    public boolean canUseAncientArtifact(int requiredWisdom) {
        if (abilityScores == null) {
            return AbilityScores.BASELINE >= requiredWisdom;
        }
        return abilityScores.canUseAncientArtifact(requiredWisdom);
    }

    public double getStatusEffectDurationModifier() {
        if (abilityScores == null) return 1.0;
        return abilityScores.getStatusEffectResistanceModifier();
    }

    public double getCarryingCapacityModifier() {
        if (abilityScores == null) return 1.0;
        return abilityScores.getTotalCarryingCapacityModifier();
    }

    public double getDoubleUseChance() {
        if (abilityScores == null) return 0.0;
        return abilityScores.getDoubleUseChance();
    }

    public double getMissChance() {
        if (abilityScores == null) return 0.0;
        return abilityScores.getMissChance();
    }

    // ==================== Physics Push System ====================

    @Override
    public void applyPush(double pushX, double pushY) {
        this.pushX += pushX;
        this.pushY += pushY;
    }

    // ==================== Attack System ====================

    @Override
    public boolean attack() {
        if (attackTimer <= 0 && !isAttacking) {
            isAttacking = true;

            attackDirX = aimDirX;
            attackDirY = aimDirY;
            attackAngle = aimAngle;

            int effectiveRange = getEffectiveAttackRange();
            float weaponAttackSpeed = getEffectiveAttackSpeed();

            attackCooldown = 1.0 / Math.max(0.1, weaponAttackSpeed);
            attackDuration = Math.min(attackCooldown * 0.5, 0.3);
            attackTimer = attackCooldown;

            int attackOriginX = x + width / 2 + (int)(attackDirX * width / 4);
            int attackOriginY = y + height / 2 + (int)(attackDirY * height / 4);

            currentAttackHitbox = MeleeAttackHitbox.fromWeaponSpeed(
                attackOriginX, attackOriginY,
                attackAngle, effectiveRange, weaponAttackSpeed
            );

            // --- Uncomment when ControllerManager is ported ---
            // ControllerManager controller = ControllerManager.getInstance();
            // if (controller.isVibrationSupported()) {
            //     controller.vibrate(VibrationPattern.GREATER_MELEE_ATTACK);
            // }

            return true;
        }
        return false;
    }

    public float getEffectiveAttackSpeed() {
        if (heldItem != null && heldItem.getAttackSpeed() > 0) {
            return heldItem.getAttackSpeed();
        }
        return 1.0f;
    }

    public void setShowMeleeAimIndicator(boolean show) {
        this.showMeleeAimIndicator = show;
    }

    public boolean isShowMeleeAimIndicator() {
        return showMeleeAimIndicator;
    }

    @Override
    public boolean isAttacking() { return isAttacking; }

    @Override
    public Rect getAttackBounds() {
        if (!isAttacking) return null;

        if (currentAttackHitbox != null) {
            int attackOriginX = x + width / 2 + (int)(attackDirX * width / 4);
            int attackOriginY = y + height / 2 + (int)(attackDirY * height / 4);
            currentAttackHitbox.updateOrigin(attackOriginX, attackOriginY);
            return currentAttackHitbox.getBoundingBox();
        }

        int effectiveRange = getEffectiveAttackRange();
        int attackX = facingRight ? x + width : x - effectiveRange;
        int attackY = y + (height - attackHeight) / 2;
        return new Rect(attackX, attackY, attackX + effectiveRange, attackY + attackHeight);
    }

    public MeleeAttackHitbox getMeleeAttackHitbox() {
        if (!isAttacking || currentAttackHitbox == null) return null;

        int attackOriginX = x + width / 2 + (int)(attackDirX * width / 4);
        int attackOriginY = y + height / 2 + (int)(attackDirY * height / 4);
        currentAttackHitbox.updateOrigin(attackOriginX, attackOriginY);
        return currentAttackHitbox;
    }

    public double getAttackDirX() { return attackDirX; }
    public double getAttackDirY() { return attackDirY; }

    @Override
    public int getAttackDamage() {
        int baseDamage = baseAttackDamage;

        if (heldItem != null && heldItem.getDamage() > 0) {
            baseDamage = heldItem.getDamage();
            if (abilityScores != null) {
                if (isMagicalWeapon(heldItem)) {
                    baseDamage = (int) Math.max(1, baseDamage * abilityScores.getMagicalDamageModifier());
                } else if (isMeleeWeapon(heldItem)) {
                    baseDamage = (int) Math.max(1, baseDamage * abilityScores.getMeleeDamageModifier());
                }
            }
        } else if (abilityScores != null) {
            baseDamage = (int) Math.max(1, baseAttackDamage * abilityScores.getMeleeDamageModifier());
        }

        return baseDamage;
    }

    private boolean isMagicalWeapon(Item item) {
        if (item == null) return false;
        String name = item.getName().toLowerCase();
        return name.contains("staff") || name.contains("wand") ||
               name.contains("rod") || name.contains("scepter") ||
               name.contains("magic") || name.contains("arcane");
    }

    private boolean isMeleeWeapon(Item item) {
        if (item == null) return false;
        String name = item.getName().toLowerCase();
        return name.contains("sword") || name.contains("axe") ||
               name.contains("mace") || name.contains("hammer") ||
               name.contains("dagger") || name.contains("blade") ||
               name.contains("club") || name.contains("spear") ||
               item.getCategory() == Item.ItemCategory.WEAPON;
    }

    public int getEffectiveAttackRange() {
        if (heldItem != null && heldItem.getRange() > 0) {
            return heldItem.getRange();
        }
        return baseAttackRange;
    }

    // ==================== BlockInteractionHandler Implementation ====================

    // --- Uncomment when BlockEntity is fully integrated ---
    // public BlockEntity getSelectedBlock() { return blockHelper.getSelectedBlock(); }
    // public void selectBlock(BlockEntity block) { blockHelper.selectBlock(block); }

    @Override
    public void deselectBlock() { blockHelper.deselectBlock(); }

    @Override
    public int getMiningDirection() { return blockHelper.getMiningDirection(); }

    @Override
    public void setMiningDirection(int direction) { blockHelper.setMiningDirection(direction); }

    @Override
    public int getCenterX() { return x + width / 2; }

    @Override
    public int getCenterY() { return y + height / 2; }

    @Override
    public void drawMiningArrow(Canvas canvas, int centerX, int centerY, int direction) {
        blockHelper.drawMiningArrow(canvas, centerX, centerY, direction);
    }

    public BlockEntity getLastBrokenBlock() {
        BlockEntity block = lastBrokenBlock;
        lastBrokenBlock = null;
        return block;
    }

    public ItemEntity getLastDroppedItem() {
        ItemEntity item = lastDroppedItem;
        lastDroppedItem = null;
        return item;
    }

    // ==================== ResourceManager Implementation ====================

    @Override
    public void setMana(int mana) {
        this.currentMana = Math.max(0, Math.min(maxMana, mana));
    }

    @Override
    public double getManaRegenRate() { return manaRegenRate; }

    @Override
    public void setManaRegenRate(double rate) { this.manaRegenRate = rate; }

    @Override
    public void setStamina(int stamina) {
        this.currentStamina = Math.max(0, Math.min(maxStamina, stamina));
        this.currentStaminaFloat = currentStamina;
    }

    @Override
    public double getStaminaRegenRate() { return staminaRegenRate; }

    @Override
    public void setStaminaRegenRate(double rate) { this.staminaRegenRate = rate; }

    @Override
    public double getStaminaDrainRate() { return staminaDrainRate; }

    @Override
    public void setStaminaDrainRate(double rate) { this.staminaDrainRate = rate; }

    @Override
    public void setHealth(int health) {
        this.currentHealth = Math.max(0, Math.min(maxHealth, health));
    }

    @Override
    public void updateResourceRegeneration(double deltaSeconds) {
        currentMana = Math.min(maxMana, currentMana + (int)(manaRegenRate * deltaSeconds));
        if (!isSprinting) {
            currentStaminaFloat = Math.min(maxStamina, currentStaminaFloat + staminaRegenRate * deltaSeconds);
            currentStamina = (int) currentStaminaFloat;
        }
    }

    // ==================== CombatCapable Implementation ====================

    @Override
    public int getBaseAttackDamage() { return baseAttackDamage; }

    @Override
    public void setBaseAttackDamage(int damage) { this.baseAttackDamage = damage; }

    @Override
    public int getAttackRange() { return getEffectiveAttackRange(); }

    @Override
    public int getBaseAttackRange() { return baseAttackRange; }

    @Override
    public void setBaseAttackRange(int range) { this.baseAttackRange = range; }

    @Override
    public double getAttackCooldown() { return attackCooldown; }

    @Override
    public void setAttackCooldown(double cooldown) { this.attackCooldown = cooldown; }

    @Override
    public double getInvincibilityDuration() { return invincibilityTime; }

    @Override
    public void setInvincibilityDuration(double duration) { this.invincibilityTime = duration; }

    @Override
    public boolean canFireRanged() {
        return heldItem != null && heldItem.isRangedWeapon() && fireTimer <= 0;
    }

    @Override
    public boolean isFiring() { return isFiring; }

    @Override
    public void updateCombatTimers(double deltaSeconds) {
        if (attackTimer > 0) attackTimer -= deltaSeconds;
        if (isAttacking && attackTimer <= attackCooldown - attackDuration) {
            isAttacking = false;
            currentAttackHitbox = null;
        }
        if (fireTimer > 0) fireTimer -= deltaSeconds;
        if (isFiring && fireTimer <= fireCooldown - fireDuration) {
            isFiring = false;
        }
        if (invincibilityTimer > 0) invincibilityTimer -= deltaSeconds;
    }

    // ==================== Held Item System ====================

    public void setHeldItem(Item item) { this.heldItem = item; }
    public Item getHeldItem() { return heldItem; }
    public boolean hasHeldItem() { return heldItem != null; }

    // ==================== Multi-Jump Configuration ====================

    public void setMaxJumps(int maxJumps) {
        this.maxJumps = Math.max(1, Math.min(3, maxJumps));
        this.jumpsRemaining = this.maxJumps;
    }

    public int getMaxJumps() { return maxJumps; }
    public int getJumpsRemaining() { return jumpsRemaining; }
    public boolean isSprinting() { return isSprinting; }
    public void setSprintSpeed(double speed) { this.sprintSpeed = speed; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // --- Uncomment when ProjectileEntity is ported ---
    // public List<ProjectileEntity> getActiveProjectiles() { return activeProjectiles; }

    // ==================== Action State Queries ====================

    public boolean isEating() { return isEating; }
    public boolean isUsingItem() { return isUsingItem; }
    public int getCurrentJumpNumber() { return currentJumpNumber; }
    public boolean isOnGround() { return onGround; }
    public String getCurrentAnimState() { return currentAnimState; }
}
