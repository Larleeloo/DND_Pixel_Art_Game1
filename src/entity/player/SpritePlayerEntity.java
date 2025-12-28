package entity.player;

import entity.*;
import block.*;
import animation.*;
import audio.*;
import input.*;
import ui.*;
import graphics.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * SpritePlayerEntity is a player implementation that uses sprite/GIF-based animation
 * with support for equipment overlays. This is an alternative to the bone-based
 * PlayerBoneEntity, allowing for simpler GIF animations with layered equipment.
 *
 * Features:
 * - GIF animations for idle, walk, and jump actions
 * - Equipment overlay system for clothing/armor that syncs with base sprite
 * - Automatic action state transitions based on player movement
 * - Full compatibility with existing game systems (inventory, collision, etc.)
 *
 * The animation system expects GIF files with matching frame counts for proper
 * synchronization between the base sprite and equipment overlays.
 *
 * Usage in level JSON:
 *   "useSpriteAnimation": true,
 *   "spriteAnimationDir": "assets/player/sprites"
 *
 * Expected sprite files:
 *   - idle.gif (5 frames recommended)
 *   - walk.gif (5 frames recommended)
 *   - jump.gif (5 frames recommended)
 */
public class SpritePlayerEntity extends Entity implements PlayerBase {

    // Animation system
    private SpriteAnimation spriteAnimation;
    private EquipmentOverlay equipmentOverlay;

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
    private double tripleJumpStrength = -8;

    // Sprint system
    private boolean isSprinting = false;
    private double walkSpeed = 4;
    private double sprintSpeed = 7;
    private double sprintStaminaCost = 0.5; // Stamina per frame while sprinting

    // Projectile system
    private List<ProjectileEntity> activeProjectiles = new ArrayList<>();
    private Item heldItem = null;        // Currently held item
    private boolean isFiring = false;
    private double fireTimer = 0;
    private double fireCooldown = 0.5;   // Seconds between shots
    private double fireDuration = 0.3;   // Animation duration

    // Item usage system
    private boolean isUsingItem = false;
    private double useItemTimer = 0;
    private double useItemDuration = 0.5;

    // Eating system
    private boolean isEating = false;
    private double eatTimer = 0;
    private double eatDuration = 1.5;    // Time to consume food

    // Attack system
    private boolean isAttacking = false;
    private double attackTimer = 0;
    private double attackCooldown = 0.4; // Seconds between attacks
    private double attackDuration = 0.2; // How long attack animation lasts
    private int baseAttackDamage = 5;     // Unarmed damage
    private int baseAttackRange = 40;     // Unarmed range
    private int attackWidth = 40;
    private int attackHeight = 50;

    // Dimensions
    private int width;
    private int height;
    // Scale factor: 2 gives 64x128 from 32x64 base sprites (similar to original 96x128 player)
    private static final int SCALE = 2;

    // Health system
    private int maxHealth = 100;
    private int currentHealth = 100;
    private double invincibilityTime = 1.0;
    private double invincibilityTimer = 0;

    // Mana system
    private int maxMana = 100;
    private int currentMana = 100;
    private double manaRegenRate = 5.0; // Mana per second

    // Stamina system
    private int maxStamina = 100;
    private double currentStaminaFloat = 100.0;  // Use float for smooth draining
    private int currentStamina = 100;
    private double staminaRegenRate = 15.0; // Stamina per second
    private double staminaDrainRate = 20.0; // Stamina drain per second when sprinting

    // Systems
    private Inventory inventory;
    private AudioManager audioManager;

    // Ground level
    private int groundY = 720;

    // Block interaction
    private BlockEntity lastBrokenBlock = null;
    private ItemEntity lastDroppedItem = null;

    // Mining targeting system
    private int miningDirection = 1;
    private static final int NUM_DIRECTIONS = 6;
    private BlockEntity targetedBlock = null;

    // Timing for animation updates
    private long lastUpdateTime;

    /**
     * Creates a SpritePlayerEntity with animations loaded from a directory.
     *
     * @param x Starting X position
     * @param y Starting Y position
     * @param spriteDir Directory containing idle.gif, walk.gif, jump.gif
     */
    public SpritePlayerEntity(int x, int y, String spriteDir) {
        super(x, y);

        // Initialize animation systems
        this.spriteAnimation = new SpriteAnimation();
        this.equipmentOverlay = new EquipmentOverlay();

        // Load animations from directory
        loadAnimations(spriteDir);

        // Set dimensions based on loaded animation
        this.width = spriteAnimation.getBaseWidth() * SCALE;
        this.height = spriteAnimation.getBaseHeight() * SCALE;

        // Initialize inventory
        this.inventory = new Inventory(10);
        this.lastUpdateTime = System.currentTimeMillis();

    }

    /**
     * Creates a SpritePlayerEntity with a pre-configured SpriteAnimation.
     *
     * @param x Starting X position
     * @param y Starting Y position
     * @param animation Pre-configured SpriteAnimation
     */
    public SpritePlayerEntity(int x, int y, SpriteAnimation animation) {
        super(x, y);

        this.spriteAnimation = animation;
        this.equipmentOverlay = new EquipmentOverlay();

        this.width = spriteAnimation.getBaseWidth() * SCALE;
        this.height = spriteAnimation.getBaseHeight() * SCALE;

        this.inventory = new Inventory(10);
        this.lastUpdateTime = System.currentTimeMillis();

    }

    /**
     * Loads animations from a sprite directory.
     *
     * @param spriteDir Directory containing animation GIFs
     */
    private void loadAnimations(String spriteDir) {
        String basePath = spriteDir.endsWith("/") ? spriteDir : spriteDir + "/";

        // Core movement animations
        spriteAnimation.loadAction(SpriteAnimation.ActionState.IDLE, basePath + "idle.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.WALK, basePath + "walk.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.RUN, basePath + "run.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.SPRINT, basePath + "sprint.gif");

        // Jump animations (single, double, triple)
        spriteAnimation.loadAction(SpriteAnimation.ActionState.JUMP, basePath + "jump.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.DOUBLE_JUMP, basePath + "double_jump.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.TRIPLE_JUMP, basePath + "triple_jump.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.FALL, basePath + "fall.gif");

        // Combat animations
        spriteAnimation.loadAction(SpriteAnimation.ActionState.ATTACK, basePath + "attack.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.FIRE, basePath + "fire.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.BLOCK, basePath + "block.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.CAST, basePath + "cast.gif");

        // Item usage animations
        spriteAnimation.loadAction(SpriteAnimation.ActionState.USE_ITEM, basePath + "use_item.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.EAT, basePath + "eat.gif");

        // Reaction animations
        spriteAnimation.loadAction(SpriteAnimation.ActionState.HURT, basePath + "hurt.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.DEAD, basePath + "dead.gif");

    }

    /**
     * Gets the equipment overlay system for adding clothing/armor.
     *
     * @return EquipmentOverlay instance
     */
    public EquipmentOverlay getEquipmentOverlay() {
        return equipmentOverlay;
    }

    /**
     * Gets the sprite animation system.
     *
     * @return SpriteAnimation instance
     */
    public SpriteAnimation getSpriteAnimation() {
        return spriteAnimation;
    }

    /**
     * Equips a clothing/armor item that will overlay on the sprite.
     *
     * @param slot Equipment slot
     * @param spriteDir Directory containing action GIFs for this equipment
     * @param itemName Display name
     */
    public void equipOverlay(EquipmentOverlay.EquipmentSlot slot, String spriteDir, String itemName) {
        String basePath = spriteDir.endsWith("/") ? spriteDir : spriteDir + "/";

        // Load animations for each action state
        equipmentOverlay.equipItem(slot, SpriteAnimation.ActionState.IDLE, basePath + "idle.gif", itemName);
        equipmentOverlay.equipItem(slot, SpriteAnimation.ActionState.WALK, basePath + "walk.gif", itemName);
        equipmentOverlay.equipItem(slot, SpriteAnimation.ActionState.JUMP, basePath + "jump.gif", itemName);

    }

    @Override
    public void setGroundY(int groundY) {
        this.groundY = groundY;
    }

    @Override
    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @Override
    public void update(InputManager input, ArrayList<Entity> entities) {
        // Calculate delta time for animation
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

        // Regenerate mana and stamina (use float for smooth regeneration)
        if (!isSprinting) {
            currentStaminaFloat = Math.min(maxStamina, currentStaminaFloat + staminaRegenRate * deltaSeconds);
        }
        currentStamina = (int) currentStaminaFloat;
        currentMana = Math.min(maxMana, currentMana + (int)(manaRegenRate * deltaSeconds));

        int newX = x;
        int newY = y;
        boolean isMoving = false;

        // Toggle inventory with 'I' key
        if (input.isKeyJustPressed('i')) {
            inventory.toggleOpen();
        }

        // Number keys 1-5 to select hotbar slots
        for (char c = '1'; c <= '5'; c++) {
            if (input.isKeyJustPressed(c)) {
                inventory.handleHotbarKey(c);
            }
        }

        // Sync held item with inventory selection
        syncHeldItemWithInventory();

        // Scroll wheel - cycles hotbar when inventory closed, scrolls inventory when open
        int scroll = input.getScrollDirection();
        if (scroll != 0) {
            if (inventory.isOpen()) {
                // Scroll through inventory
                inventory.handleScroll(scroll);
            } else {
                // Cycle hotbar selection
                inventory.handleScroll(scroll);
                syncHeldItemWithInventory();
            }
        }

        // Update targeted block
        updateTargetedBlock(entities);

        // E key or Left Mouse Click - mine block (use tool) or fire projectile
        // Also handle inventory auto-equip on left click when inventory is open
        if (input.isLeftMouseJustPressed()) {
            if (inventory.isOpen()) {
                // Try auto-equip in open inventory
                if (inventory.handleLeftClick(input.getMouseX(), input.getMouseY())) {
                    syncHeldItemWithInventory();
                }
            } else if (heldItem != null && heldItem.isRangedWeapon()) {
                // Fire projectile
                fireProjectile(entities);
            } else {
                // Mine block
                int direction = getMiningDirection();
                tryMineBlock(entities, direction);
            }
        }

        // E key always fires or mines (not affected by inventory)
        if (input.isKeyJustPressed('e')) {
            if (heldItem != null && heldItem.isRangedWeapon()) {
                fireProjectile(entities);
            } else {
                int direction = getMiningDirection();
                tryMineBlock(entities, direction);
            }
        }

        // Right Mouse Click or F key - attack or use item
        if (input.isRightMouseJustPressed() || input.isKeyJustPressed('f')) {
            if (heldItem != null && heldItem.isConsumable()) {
                // Start eating/using consumable
                startEating();
            } else if (attack()) {
                // Attack started - check for mob hits
                Rectangle attackBounds = getAttackBounds();
                if (attackBounds != null) {
                    EntityPhysics.checkPlayerAttack(this, attackBounds, getAttackDamage(), 8.0, entities);
                    if (audioManager != null) {
                        audioManager.playSound("attack");
                    }
                }
            }
        }

        // Q key - use item (non-consumable)
        if (input.isKeyJustPressed('q') && heldItem != null && !heldItem.isConsumable()) {
            startUsingItem();
        }

        // Apply push forces from collisions
        EntityPhysics.processCollisions(entities, this, deltaSeconds);

        // Sprinting - hold Shift (drains stamina over time)
        boolean wantsSprint = input.isKeyPressed(java.awt.event.KeyEvent.VK_SHIFT);
        if (wantsSprint && currentStaminaFloat > 0 && (input.isKeyPressed('a') || input.isKeyPressed('d'))) {
            isSprinting = true;
            // Drain stamina while sprinting (using float for smooth drain)
            currentStaminaFloat = Math.max(0, currentStaminaFloat - staminaDrainRate * deltaSeconds);
            currentStamina = (int) currentStaminaFloat;
        } else {
            isSprinting = false;
        }

        // Determine movement speed
        double speed = isSprinting ? sprintSpeed : walkSpeed;

        // Horizontal movement
        if (input.isKeyPressed('a')) {
            newX -= (int)speed;
            facingRight = false;
            isMoving = true;
        }
        if (input.isKeyPressed('d')) {
            newX += (int)speed;
            facingRight = true;
            isMoving = true;
        }

        // Check horizontal collision
        Rectangle futureXBounds = new Rectangle(newX, y, width, height);
        boolean xCollision = false;

        for (Entity e : entities) {
            if (e == this) continue;

            boolean isSolid = false;
            if (e instanceof SpriteEntity && ((SpriteEntity) e).isSolid()) {
                isSolid = true;
            } else if (e instanceof BlockEntity && ((BlockEntity) e).isSolid()) {
                isSolid = true;
            }

            if (isSolid && futureXBounds.intersects(e.getBounds())) {
                xCollision = true;
                break;
            }
        }

        if (!xCollision) {
            x = newX;
        }

        // Apply push forces
        if (Math.abs(pushX) > 0.1) {
            x += (int) pushX;
            pushX *= 0.8; // Decay push force
        } else {
            pushX = 0;
        }

        // Check for item collection
        Rectangle playerBounds = new Rectangle(x, y, width, height);
        for (Entity e : entities) {
            if (e instanceof ItemEntity) {
                ItemEntity item = (ItemEntity) e;
                if (!item.isCollected() && playerBounds.intersects(e.getBounds())) {
                    item.collect();
                    boolean added = inventory.addItem(item);

                    if (audioManager != null) {
                        audioManager.playSound("collect");
                    }
                }
            }
        }

        // Multi-jump system
        handleJumping(input);

        // Apply gravity
        velY += gravity;
        newY = y + (int)velY;

        // Check vertical collision
        Rectangle futureYBounds = new Rectangle(x, newY, width, height);
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
                Rectangle platformBounds = e.getBounds();

                if (futureYBounds.intersects(platformBounds)) {
                    if (velY > 0) {
                        newY = platformBounds.y - height;
                        velY = 0;
                        onGround = true;
                        foundPlatform = true;
                        // Reset jumps on landing
                        jumpsRemaining = maxJumps;
                        currentJumpNumber = 0;
                    } else if (velY < 0) {
                        newY = platformBounds.y + platformBounds.height;
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
            // Reset jumps on landing
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

        // Update projectiles
        updateProjectiles(deltaSeconds, entities);

        // Update animation state based on movement
        updateAnimationState(isMoving);

        // Update animations
        spriteAnimation.update(deltaMs);

        // Sync equipment overlays to base animation frame
        equipmentOverlay.syncToFrame(
                spriteAnimation.getCurrentFrameIndex(),
                spriteAnimation.getState()
        );

        // Sync held item animation
        if (heldItem != null) {
            heldItem.syncHeldAnimations(
                    spriteAnimation.getCurrentFrameIndex(),
                    spriteAnimation.getState()
            );
        }
    }

    /**
     * Handles multi-jump input (single, double, triple jump).
     * Uses isKeyJustPressed for immediate response on key press.
     * Checks both space char and VK_SPACE keyCode for maximum responsiveness.
     */
    private void handleJumping(InputManager input) {
        // Use both space char and VK_SPACE keyCode for reliable detection
        boolean spacePressed = input.isKeyJustPressed(' ') ||
                               input.isKeyJustPressed(java.awt.event.KeyEvent.VK_SPACE);

        if (spacePressed && jumpsRemaining > 0) {
            if (onGround) {
                // First jump from ground
                velY = jumpStrength;
                onGround = false;
                jumpsRemaining--;
                currentJumpNumber = 1;

                if (audioManager != null) {
                    audioManager.playSound("jump");
                }
            } else if (jumpsRemaining > 0) {
                // Air jump (double or triple)
                currentJumpNumber++;

                if (currentJumpNumber == 2) {
                    velY = doubleJumpStrength;
                } else if (currentJumpNumber == 3) {
                    velY = tripleJumpStrength;
                }

                jumpsRemaining--;

                if (audioManager != null) {
                    audioManager.playSound("jump");
                }
            }
        }
    }

    /**
     * Updates action timers for attack, fire, eat, use item.
     */
    private void updateActionTimers(double deltaSeconds) {
        // Attack timer
        if (attackTimer > 0) {
            attackTimer -= deltaSeconds;
        }
        if (isAttacking && attackTimer <= attackCooldown - attackDuration) {
            isAttacking = false;
        }

        // Fire timer
        if (fireTimer > 0) {
            fireTimer -= deltaSeconds;
        }
        if (isFiring && fireTimer <= fireCooldown - fireDuration) {
            isFiring = false;
        }

        // Eat timer
        if (isEating) {
            eatTimer -= deltaSeconds;
            if (eatTimer <= 0) {
                finishEating();
            }
        }

        // Use item timer
        if (isUsingItem) {
            useItemTimer -= deltaSeconds;
            if (useItemTimer <= 0) {
                finishUsingItem();
            }
        }
    }

    /**
     * Fires a projectile from the held ranged weapon.
     * Consumes appropriate ammo or mana based on weapon type.
     */
    private void fireProjectile(ArrayList<Entity> entities) {
        if (heldItem == null || !heldItem.isRangedWeapon()) return;
        if (fireTimer > 0) return; // On cooldown

        // Determine ammo requirements
        String ammoType = heldItem.getAmmoItemName();
        boolean isThrowable = heldItem.getCategory() == Item.ItemCategory.THROWABLE;
        boolean usesMana = "mana".equalsIgnoreCase(ammoType);
        int manaCost = 10;  // Base mana cost for magic weapons

        // Save reference to the item being used for projectile creation
        // (needed because throwables may be removed from inventory before we create the projectile)
        Item itemForProjectile = heldItem;

        // Check and consume resources
        int bonusDamage = 0;

        if (isThrowable) {
            // Throwables consume themselves - remove from current slot
            int currentSlot = inventory.getSelectedSlot();
            ItemEntity removedItem = inventory.removeItemAtSlot(currentSlot);
            if (removedItem == null) {
                // No item to throw
                return;
            }
            // Throwable item was consumed - sync AFTER we've saved the item reference
            syncHeldItemWithInventory();  // Update held item reference (may become null)
        } else if (usesMana) {
            // Magic weapons consume mana
            if (currentMana < manaCost) {
                // Not enough mana - play fail sound or show indicator
                return;
            }
            currentMana -= manaCost;
        } else if (ammoType != null && !ammoType.isEmpty()) {
            // Regular ranged weapons consume ammo from inventory
            ItemEntity consumedAmmo = inventory.consumeAmmo(ammoType);
            if (consumedAmmo == null) {
                // No ammo available - can't fire
                return;
            }
            // Check if ammo provides bonus damage
            Item ammoItem = consumedAmmo.getLinkedItem();
            if (ammoItem != null) {
                bonusDamage = ammoItem.getDamage();
            }
        }

        // Create projectile using the saved item reference
        int projX = facingRight ? x + width : x - 10;
        int projY = y + height / 3;
        double dirX = facingRight ? 1.0 : -1.0;

        ProjectileEntity projectile = itemForProjectile.createProjectile(projX, projY, dirX, 0, true);
        if (projectile != null) {
            // Apply bonus damage from ammo
            if (bonusDamage > 0) {
                projectile.setDamage(projectile.getDamage() + bonusDamage);
            }

            projectile.setSource(this);
            activeProjectiles.add(projectile);
            entities.add(projectile);

            isFiring = true;
            fireTimer = fireCooldown;

            if (audioManager != null) {
                audioManager.playSound("fire");
            }
        }
    }

    /**
     * Updates active projectiles.
     */
    private void updateProjectiles(double deltaSeconds, ArrayList<Entity> entities) {
        Iterator<ProjectileEntity> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            ProjectileEntity proj = iterator.next();
            proj.update(deltaSeconds, entities);

            if (!proj.isActive()) {
                iterator.remove();
                entities.remove(proj);
            }
        }
    }

    /**
     * Starts eating/consuming a held item.
     */
    private void startEating() {
        if (heldItem == null || !heldItem.isConsumable()) return;
        if (isEating) return;

        isEating = true;
        eatTimer = heldItem.getConsumeTime();
        eatDuration = heldItem.getConsumeTime();
    }

    /**
     * Finishes eating and applies effects.
     */
    private void finishEating() {
        if (heldItem == null) return;

        // Apply restoration
        currentHealth = Math.min(maxHealth, currentHealth + heldItem.getHealthRestore());
        currentMana = Math.min(maxMana, currentMana + heldItem.getManaRestore());
        currentStamina = Math.min(maxStamina, currentStamina + heldItem.getStaminaRestore());

        isEating = false;

        // Remove consumed item from inventory
        int currentSlot = inventory.getSelectedSlot();
        inventory.removeItemAtSlot(currentSlot);
        syncHeldItemWithInventory();  // Update held item reference

        if (audioManager != null) {
            audioManager.playSound("eat");
        }
    }

    /**
     * Starts using a non-consumable item.
     */
    private void startUsingItem() {
        if (heldItem == null) return;
        if (isUsingItem) return;

        isUsingItem = true;
        useItemTimer = useItemDuration;
    }

    /**
     * Finishes using an item.
     */
    private void finishUsingItem() {
        isUsingItem = false;
        // TODO: Apply item effects
    }

    /**
     * Updates the animation state based on player movement and actions.
     * Priority: Death > Hurt > Eating > Firing > Using Item > Attack > Jump > Sprint/Run/Walk > Idle
     */
    private void updateAnimationState(boolean isMoving) {
        SpriteAnimation.ActionState newState;

        // Priority-based animation state selection

        // Highest priority: Special actions
        if (isEating) {
            newState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.EAT)
                    ? SpriteAnimation.ActionState.EAT
                    : SpriteAnimation.ActionState.USE_ITEM;
        } else if (isFiring) {
            newState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.FIRE)
                    ? SpriteAnimation.ActionState.FIRE
                    : SpriteAnimation.ActionState.ATTACK;
        } else if (isUsingItem) {
            newState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.USE_ITEM)
                    ? SpriteAnimation.ActionState.USE_ITEM
                    : SpriteAnimation.ActionState.IDLE;
        } else if (isAttacking) {
            newState = SpriteAnimation.ActionState.ATTACK;
        } else if (airTime > 3) {
            // In the air - check for multi-jump animations
            if (velY < 0) {
                // Going up
                if (currentJumpNumber == 3 && spriteAnimation.hasAnimation(SpriteAnimation.ActionState.TRIPLE_JUMP)) {
                    newState = SpriteAnimation.ActionState.TRIPLE_JUMP;
                } else if (currentJumpNumber == 2 && spriteAnimation.hasAnimation(SpriteAnimation.ActionState.DOUBLE_JUMP)) {
                    newState = SpriteAnimation.ActionState.DOUBLE_JUMP;
                } else {
                    newState = SpriteAnimation.ActionState.JUMP;
                }
            } else {
                // Falling - use FALL if available, otherwise JUMP
                newState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.FALL)
                        ? SpriteAnimation.ActionState.FALL
                        : SpriteAnimation.ActionState.JUMP;
            }
        } else if (isMoving) {
            // On ground and moving
            if (isSprinting) {
                // Use SPRINT if available, otherwise RUN, otherwise WALK
                if (spriteAnimation.hasAnimation(SpriteAnimation.ActionState.SPRINT)) {
                    newState = SpriteAnimation.ActionState.SPRINT;
                } else if (spriteAnimation.hasAnimation(SpriteAnimation.ActionState.RUN)) {
                    newState = SpriteAnimation.ActionState.RUN;
                } else {
                    newState = SpriteAnimation.ActionState.WALK;
                }
            } else {
                newState = SpriteAnimation.ActionState.WALK;
            }
        } else {
            newState = SpriteAnimation.ActionState.IDLE;
        }

        spriteAnimation.setState(newState);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        SpriteAnimation.ActionState currentState = spriteAnimation.getState();

        // Invincibility flash effect
        boolean flashHide = invincibilityTimer > 0 && (int)(invincibilityTimer * 10) % 2 == 0;

        if (!flashHide) {
            // Draw equipment behind (BACK slot - capes, etc.)
            equipmentOverlay.drawBehind(g, x, y, width, height, facingRight, currentState);

            // Draw base sprite
            spriteAnimation.draw(g, x, y, width, height, facingRight);

            // Draw equipment in front (armor, weapons, etc.)
            equipmentOverlay.drawInFront(g, x, y, width, height, facingRight, currentState);

            // Draw held item overlay (if animated overlay exists)
            if (heldItem != null) {
                heldItem.drawHeld(g, x, y, width, height, facingRight, currentState);
            }

            // Draw mini held item icon in player's hand
            drawHeldItemIcon(g2d);
        }

        // Debug: draw bounds
        g.setColor((airTime > 3) ? Color.ORANGE : (isSprinting ? Color.CYAN : Color.RED));
        g2d.setStroke(new BasicStroke(2));
        g.drawRect(x, y, width, height);

        // Draw crosshair on targeted block
        if (targetedBlock != null && !targetedBlock.isBroken()) {
            Rectangle blockBounds = targetedBlock.getFullBounds();
            int crosshairX = blockBounds.x + blockBounds.width / 2;
            int crosshairY = blockBounds.y + blockBounds.height / 2;
            int dotSize = 6;

            g.setColor(Color.BLACK);
            g.fillOval(crosshairX - dotSize/2 - 1, crosshairY - dotSize/2 - 1, dotSize + 2, dotSize + 2);
            g.setColor(Color.WHITE);
            g.fillOval(crosshairX - dotSize/2, crosshairY - dotSize/2, dotSize, dotSize);
        }

        // Draw attack hitbox when attacking
        if (isAttacking) {
            Rectangle attackBounds = getAttackBounds();
            if (attackBounds != null) {
                g2d.setColor(new Color(255, 100, 100, 100));
                g2d.fillRect(attackBounds.x, attackBounds.y, attackBounds.width, attackBounds.height);
                g2d.setColor(new Color(255, 50, 50, 200));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(attackBounds.x, attackBounds.y, attackBounds.width, attackBounds.height);
            }
        }

        // Draw eating progress bar
        if (isEating) {
            int barWidth = 40;
            int barHeight = 6;
            int barX = x + (width - barWidth) / 2;
            int barY = y - 15;
            float progress = 1.0f - (float)(eatTimer / eatDuration);

            g2d.setColor(new Color(50, 50, 50));
            g2d.fillRect(barX, barY, barWidth, barHeight);
            g2d.setColor(new Color(100, 200, 100));
            g2d.fillRect(barX, barY, (int)(barWidth * progress), barHeight);
            g2d.setColor(Color.WHITE);
            g2d.drawRect(barX, barY, barWidth, barHeight);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void dropItem(ItemEntity item) {
        int dropOffset = facingRight ? width + 20 : -20 - item.getBounds().width;
        item.x = x + dropOffset;
        item.y = y + (height / 2);

        if (audioManager != null) {
            audioManager.playSound("drop");
        }
    }

    @Override
    public boolean isFacingRight() {
        return facingRight;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    // ==================== Health System ====================

    @Override
    public int getHealth() {
        return currentHealth;
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public boolean isInvincible() {
        return invincibilityTimer > 0;
    }

    @Override
    public void takeDamage(int damage, double knockbackX, double knockbackY) {
        if (invincibilityTimer > 0) {
            return;
        }

        currentHealth -= damage;
        if (currentHealth < 0) currentHealth = 0;

        velY += knockbackY;
        invincibilityTimer = invincibilityTime;

        if (audioManager != null) {
            audioManager.playSound("hurt");
        }
    }

    @Override
    public int getMana() {
        return currentMana;
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public int getStamina() {
        return currentStamina;
    }

    @Override
    public int getMaxStamina() {
        return maxStamina;
    }

    /**
     * Uses mana for an action.
     * @param amount Amount of mana to use
     * @return true if enough mana was available
     */
    public boolean useMana(int amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            return true;
        }
        return false;
    }

    /**
     * Uses stamina for an action.
     * @param amount Amount of stamina to use
     * @return true if enough stamina was available
     */
    public boolean useStamina(int amount) {
        if (currentStamina >= amount) {
            currentStamina -= amount;
            return true;
        }
        return false;
    }

    // ==================== Physics Push System ====================

    /**
     * Applies an external push force to the player (from collisions).
     * @param pushX Horizontal push force
     * @param pushY Vertical push force
     */
    public void applyPush(double pushX, double pushY) {
        this.pushX += pushX;
        this.pushY += pushY;
    }

    // ==================== Attack System ====================

    /**
     * Initiates a player attack.
     * @return true if attack was started, false if on cooldown
     */
    public boolean attack() {
        if (attackTimer <= 0 && !isAttacking) {
            isAttacking = true;
            attackTimer = attackCooldown;
            spriteAnimation.setState(SpriteAnimation.ActionState.ATTACK);
            return true;
        }
        return false;
    }

    /**
     * Checks if the player is currently attacking.
     */
    public boolean isAttacking() {
        return isAttacking;
    }

    /**
     * Gets the current attack hitbox bounds.
     * Uses held weapon's range if available.
     * @return Rectangle of attack area, or null if not attacking
     */
    public Rectangle getAttackBounds() {
        if (!isAttacking) return null;

        int effectiveRange = getEffectiveAttackRange();
        int attackX;
        if (facingRight) {
            attackX = x + width;
        } else {
            attackX = x - effectiveRange;
        }

        int attackY = y + (height - attackHeight) / 2;
        return new Rectangle(attackX, attackY, effectiveRange, attackHeight);
    }

    /**
     * Gets the effective attack damage (from weapon if held, otherwise base).
     */
    public int getAttackDamage() {
        if (heldItem != null && heldItem.getDamage() > 0) {
            return heldItem.getDamage();
        }
        return baseAttackDamage;
    }

    /**
     * Gets the effective attack range (from weapon if held, otherwise base).
     */
    public int getEffectiveAttackRange() {
        if (heldItem != null && heldItem.getRange() > 0) {
            return heldItem.getRange();
        }
        return baseAttackRange;
    }

    // ==================== Block Mining System ====================

    private void tryMineBlock(ArrayList<Entity> entities, int direction) {
        lastBrokenBlock = null;
        lastDroppedItem = null;

        Rectangle mineArea = getMiningArea(direction);

        BlockEntity targetBlock = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity e : entities) {
            if (e instanceof BlockEntity) {
                BlockEntity block = (BlockEntity) e;
                if (!block.isBroken()) {
                    if (mineArea.intersects(block.getFullBounds())) {
                        double dist = getDistanceToBlock(block, direction);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            targetBlock = block;
                        }
                    }
                }
            }
        }

        if (targetBlock != null) {
            ToolType heldTool = inventory.getHeldToolType();
            int layersToMine = heldTool.getLayersPerMine(targetBlock.getBlockType());

            boolean fullyBroken = false;
            for (int i = 0; i < layersToMine && !fullyBroken; i++) {
                fullyBroken = targetBlock.mineLayer(direction);
            }

            if (audioManager != null) {
                audioManager.playSound("drop");
            }

            if (fullyBroken) {
                lastBrokenBlock = targetBlock;
                lastDroppedItem = targetBlock.breakBlock(audioManager);
            }
        }
    }

    private Rectangle getMiningArea(int damageDir) {
        int reach = BlockRegistry.BLOCK_SIZE;
        int playerCenterX = x + width / 2;

        switch (miningDirection) {
            case 0:
                return new Rectangle(playerCenterX - reach/2, y - reach, reach, reach);
            case 1:
                return new Rectangle(x + width, y - reach/2, reach, reach);
            case 2:
                return new Rectangle(x + width, y + height - reach/2, reach, reach);
            case 3:
                return new Rectangle(playerCenterX - reach/2, y + height, reach, reach);
            case 4:
                return new Rectangle(x - reach, y + height - reach/2, reach, reach);
            case 5:
                return new Rectangle(x - reach, y - reach/2, reach, reach);
            default:
                return new Rectangle(x, y, width, height);
        }
    }

    private int getMiningDirection() {
        switch (miningDirection) {
            case 0:
                return BlockEntity.MINE_DOWN;
            case 1:
            case 2:
                return BlockEntity.MINE_LEFT;
            case 3:
                return BlockEntity.MINE_UP;
            case 4:
            case 5:
                return BlockEntity.MINE_RIGHT;
            default:
                return BlockEntity.MINE_LEFT;
        }
    }

    private void updateTargetedBlock(ArrayList<Entity> entities) {
        int direction = getMiningDirection();
        Rectangle mineArea = getMiningArea(direction);

        BlockEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity e : entities) {
            if (e instanceof BlockEntity) {
                BlockEntity block = (BlockEntity) e;
                if (!block.isBroken()) {
                    if (mineArea.intersects(block.getFullBounds())) {
                        double dist = getDistanceToBlock(block, direction);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearest = block;
                        }
                    }
                }
            }
        }

        targetedBlock = nearest;

        if (targetedBlock != null) {
            targetedBlock.setTargeted(true);
        }
    }

    private double getDistanceToBlock(BlockEntity block, int direction) {
        int playerCenterX = x + width / 2;
        int playerCenterY = y + height / 2;
        int blockCenterX = block.x + block.getSize() / 2;
        int blockCenterY = block.y + block.getSize() / 2;

        double dx = blockCenterX - playerCenterX;
        double dy = blockCenterY - playerCenterY;
        return Math.sqrt(dx * dx + dy * dy);
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

    // ==================== Held Item System ====================

    /**
     * Draws a mini icon of the held item near the player's hand.
     */
    private void drawHeldItemIcon(Graphics2D g2d) {
        ItemEntity heldEntity = inventory.getHeldItem();
        if (heldEntity == null) return;

        // Get the item sprite from the inventory slot
        Image itemSprite = heldEntity.getSprite();
        if (itemSprite == null) return;

        // Calculate position near player's hand
        int iconSize = 24; // Mini icon size
        int handOffsetX = facingRight ? width - 8 : -iconSize + 8;
        int handOffsetY = height / 2 - 10;

        // Adjust position based on animation state
        SpriteAnimation.ActionState currentState = spriteAnimation.getState();
        if (currentState == SpriteAnimation.ActionState.ATTACK ||
            currentState == SpriteAnimation.ActionState.FIRE) {
            // Extend arm during attack/fire
            handOffsetX = facingRight ? width + 10 : -iconSize - 10;
        }

        int iconX = x + handOffsetX;
        int iconY = y + handOffsetY;

        // Draw with slight rotation for held appearance
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();

        if (!facingRight) {
            // Flip for left-facing
            g2d.translate(iconX + iconSize/2, iconY + iconSize/2);
            g2d.scale(-1, 1);
            g2d.translate(-iconSize/2, -iconSize/2);
            g2d.drawImage(itemSprite, 0, 0, iconSize, iconSize, null);
        } else {
            g2d.drawImage(itemSprite, iconX, iconY, iconSize, iconSize, null);
        }

        g2d.setTransform(oldTransform);
    }

    /**
     * Syncs the held Item with the currently selected inventory slot.
     * Converts ItemEntity to Item for use in combat/actions.
     */
    private void syncHeldItemWithInventory() {
        ItemEntity selectedEntity = inventory.getHeldItem();
        if (selectedEntity != null) {
            // Check if the ItemEntity has a linked Item
            Item linked = selectedEntity.getLinkedItem();
            if (linked != null) {
                heldItem = linked;
            } else {
                // Try to create an Item based on the ItemEntity's type
                heldItem = createItemFromEntity(selectedEntity);
            }
        } else {
            heldItem = null;
        }
    }

    /**
     * Creates an Item from an ItemEntity by looking up in registry or creating a basic one.
     */
    private Item createItemFromEntity(ItemEntity entity) {
        // Try registry lookup by itemId
        if (entity.getItemId() != null) {
            Item item = ItemRegistry.create(entity.getItemId());
            if (item != null) {
                entity.setLinkedItem(item);
                return item;
            }
        }

        // Try to find by name (convert name to registry id format)
        String name = entity.getItemName().toLowerCase().replace(" ", "_");
        Item item = ItemRegistry.create(name);
        if (item != null) {
            entity.setLinkedItem(item);
            return item;
        }

        // Determine category - check item NAME for ranged weapon keywords first
        String lowerName = entity.getItemName().toLowerCase();
        String type = entity.getItemType().toLowerCase();
        Item.ItemCategory category = Item.ItemCategory.MATERIAL;

        // Detect ranged weapons by name (since JSON might have them typed as "weapon")
        boolean isRangedByName = lowerName.contains("bow") || lowerName.contains("crossbow") ||
                                  lowerName.contains("wand") || lowerName.contains("staff") ||
                                  lowerName.contains("sling");
        boolean isThrowableByName = lowerName.contains("throwing") || lowerName.contains("thrown") ||
                                     lowerName.contains("rock") || lowerName.contains("bomb");

        if (isRangedByName) {
            category = Item.ItemCategory.RANGED_WEAPON;
        } else if (isThrowableByName) {
            category = Item.ItemCategory.THROWABLE;
        } else {
            switch (type) {
                case "weapon": category = Item.ItemCategory.WEAPON; break;
                case "ranged_weapon":
                case "bow": category = Item.ItemCategory.RANGED_WEAPON; break;
                case "armor": category = Item.ItemCategory.ARMOR; break;
                case "potion": category = Item.ItemCategory.POTION; break;
                case "food": category = Item.ItemCategory.FOOD; break;
                case "tool": category = Item.ItemCategory.TOOL; break;
                case "throwable": category = Item.ItemCategory.THROWABLE; break;
            }
        }

        Item basicItem = new Item(entity.getItemName(), category);
        basicItem.setDamage(10);  // Default damage
        basicItem.setRange(60);   // Default range

        // Set ranged weapon properties for bows/crossbows
        if (category == Item.ItemCategory.RANGED_WEAPON) {
            // Determine projectile type from name
            ProjectileEntity.ProjectileType projType = ProjectileEntity.ProjectileType.ARROW;
            if (lowerName.contains("crossbow")) {
                projType = ProjectileEntity.ProjectileType.BOLT;
            } else if (lowerName.contains("wand") || lowerName.contains("staff")) {
                projType = ProjectileEntity.ProjectileType.MAGIC_BOLT;
            } else if (lowerName.contains("fire")) {
                projType = ProjectileEntity.ProjectileType.FIREBALL;
            }
            basicItem.setRangedWeapon(true, projType, 15, 15.0f);
        }

        // Set throwable properties
        if (category == Item.ItemCategory.THROWABLE) {
            ProjectileEntity.ProjectileType projType = ProjectileEntity.ProjectileType.ROCK;
            if (lowerName.contains("knife")) {
                projType = ProjectileEntity.ProjectileType.THROWING_KNIFE;
            } else if (lowerName.contains("axe")) {
                projType = ProjectileEntity.ProjectileType.THROWING_AXE;
            } else if (lowerName.contains("bomb")) {
                projType = ProjectileEntity.ProjectileType.BOMB;
            }
            basicItem.setRangedWeapon(true, projType, 12, 18.0f);
        }

        // Set consumable properties for food/potions
        if (category == Item.ItemCategory.FOOD || category == Item.ItemCategory.POTION) {
            basicItem.setHealthRestore(20);
            basicItem.setConsumeTime(1.0f);
        }

        entity.setLinkedItem(basicItem);
        return basicItem;
    }

    /**
     * Sets the currently held item.
     * The held item will be rendered as an overlay and used for attacks/actions.
     *
     * @param item The item to hold
     */
    public void setHeldItem(Item item) {
        this.heldItem = item;
    }

    /**
     * Gets the currently held item.
     *
     * @return The held item, or null if empty-handed
     */
    public Item getHeldItem() {
        return heldItem;
    }

    /**
     * Checks if the player is currently holding an item.
     */
    public boolean hasHeldItem() {
        return heldItem != null;
    }

    // ==================== Multi-Jump Configuration ====================

    /**
     * Sets the maximum number of jumps (1 = single, 2 = double, 3 = triple).
     *
     * @param maxJumps Maximum jumps allowed (1-3)
     */
    public void setMaxJumps(int maxJumps) {
        this.maxJumps = Math.max(1, Math.min(3, maxJumps));
        this.jumpsRemaining = this.maxJumps;
    }

    /**
     * Gets the maximum number of jumps allowed.
     */
    public int getMaxJumps() {
        return maxJumps;
    }

    /**
     * Gets the remaining jumps available.
     */
    public int getJumpsRemaining() {
        return jumpsRemaining;
    }

    /**
     * Checks if currently sprinting.
     */
    public boolean isSprinting() {
        return isSprinting;
    }

    /**
     * Sets sprint speed.
     */
    public void setSprintSpeed(double speed) {
        this.sprintSpeed = speed;
    }

    /**
     * Gets the active projectiles fired by this player.
     */
    public List<ProjectileEntity> getActiveProjectiles() {
        return activeProjectiles;
    }

    // ==================== Action State Queries ====================

    /**
     * Checks if currently firing a projectile.
     */
    public boolean isFiring() {
        return isFiring;
    }

    /**
     * Checks if currently eating.
     */
    public boolean isEating() {
        return isEating;
    }

    /**
     * Checks if currently using an item.
     */
    public boolean isUsingItem() {
        return isUsingItem;
    }

    /**
     * Gets the current jump number (0 = not jumping, 1 = first, 2 = double, 3 = triple).
     */
    public int getCurrentJumpNumber() {
        return currentJumpNumber;
    }
}
