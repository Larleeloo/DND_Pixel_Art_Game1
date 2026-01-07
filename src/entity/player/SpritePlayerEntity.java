package entity.player;

import entity.*;
import entity.capabilities.*;
import block.*;
import animation.*;
import animation.ItemAnimationState;
import animation.ParticleAnimationState;
import animation.TriggeredAnimationManager;
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
 * - Block interaction (mining and placement) via BlockInteractionHandler
 * - Resource management (mana, stamina) via ResourceManager
 * - Combat capabilities via CombatCapable
 *
 * Block Interaction System:
 * - Left-click on blocks to select them for mining
 * - Arrow keys to change mining direction
 * - Click again or press E to mine the selected block
 * - Right-click on empty space to place blocks (when holding a block item)
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
public class SpritePlayerEntity extends Entity implements PlayerBase,
        BlockInteractionHandler, ResourceManager, CombatCapable {

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
    private double tripleJumpStrength = -9;  // Same as double for consistent feel

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

    // Aim system for mouse-directed projectiles
    private Camera camera;               // Reference for screen-to-world conversion
    private double aimAngle = 0;         // Current aim angle in radians
    private double aimDirX = 1.0;        // Normalized aim direction X
    private double aimDirY = 0;          // Normalized aim direction Y
    private int aimTargetWorldX = 0;     // Mouse position in world coordinates
    private int aimTargetWorldY = 0;
    private static final int AIM_INDICATOR_LENGTH = 60;  // Length of aim line
    private static final int AIM_INDICATOR_WIDTH = 3;    // Thickness of aim line

    // Charged shot system
    private boolean isCharging = false;       // Currently charging a shot
    private double chargeTimer = 0;           // Current charge time in seconds
    private double chargePercent = 0;         // Current charge percentage (0.0 to 1.0)
    private boolean chargeReady = false;      // Minimum charge reached for valid shot
    private static final int CHARGE_BAR_WIDTH = 50;   // Width of charge bar
    private static final int CHARGE_BAR_HEIGHT = 8;   // Height of charge bar

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

    // Triggered animation system
    private TriggeredAnimationManager triggeredAnimManager;
    private double sprintParticleTimer = 0;
    private double jumpParticleTimer = 0;
    private boolean wasOnGround = true;
    private static final double SPRINT_PARTICLE_INTERVAL = 0.15; // Seconds between sprint particles
    private static final double JUMP_PARTICLE_COOLDOWN = 0.3; // Cooldown after landing before particles

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

        // Initialize triggered animation manager
        this.triggeredAnimManager = TriggeredAnimationManager.getInstance();
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

        // Initialize triggered animation manager
        this.triggeredAnimManager = TriggeredAnimationManager.getInstance();
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
     * Sets the skin tone tint for the base player sprite.
     *
     * @param skinTone Color to tint the base sprite, or null for no tint
     */
    public void setSkinTone(Color skinTone) {
        if (spriteAnimation != null) {
            spriteAnimation.setTint(skinTone);
        }
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

    /**
     * Sets the camera reference for screen-to-world coordinate conversion.
     * Required for mouse-aimed projectiles.
     *
     * @param camera The game camera
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    /**
     * Updates the aim direction based on mouse position.
     * Calculates angle from player center to mouse cursor in world coordinates.
     */
    private void updateAimDirection(InputManager input) {
        if (camera == null) {
            // No camera - use facing direction
            aimDirX = facingRight ? 1.0 : -1.0;
            aimDirY = 0;
            aimAngle = facingRight ? 0 : Math.PI;
            return;
        }

        // Get mouse position in screen coordinates
        int mouseScreenX = input.getMouseX();
        int mouseScreenY = input.getMouseY();

        // Convert to world coordinates
        aimTargetWorldX = camera.screenToWorldX(mouseScreenX);
        aimTargetWorldY = camera.screenToWorldY(mouseScreenY);

        // Calculate player center (where projectiles originate)
        int playerCenterX = x + width / 2;
        int playerCenterY = y + height / 3;  // Aim from upper body

        // Calculate direction vector
        double dx = aimTargetWorldX - playerCenterX;
        double dy = aimTargetWorldY - playerCenterY;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length > 0) {
            aimDirX = dx / length;
            aimDirY = dy / length;
            aimAngle = Math.atan2(dy, dx);

            // Update facing direction based on aim (left/right of player)
            if (dx > 0) {
                facingRight = true;
            } else if (dx < 0) {
                facingRight = false;
            }
        } else {
            // Mouse is on player - use facing direction
            aimDirX = facingRight ? 1.0 : -1.0;
            aimDirY = 0;
            aimAngle = facingRight ? 0 : Math.PI;
        }
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

        // Update aim direction based on mouse position (for ranged weapons)
        updateAimDirection(input);

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

        // Arrow keys change mining direction (only applies to selected block)
        if (input.isKeyJustPressed(java.awt.event.KeyEvent.VK_UP)) {
            blockHelper.setMiningDirection(0);  // Mine from above
        } else if (input.isKeyJustPressed(java.awt.event.KeyEvent.VK_RIGHT)) {
            blockHelper.setMiningDirection(1);  // Mine from right
        } else if (input.isKeyJustPressed(java.awt.event.KeyEvent.VK_DOWN)) {
            blockHelper.setMiningDirection(2);  // Mine from below
        } else if (input.isKeyJustPressed(java.awt.event.KeyEvent.VK_LEFT)) {
            blockHelper.setMiningDirection(3);  // Mine from left
        }

        // Validate selected block is still valid (not broken, still in range)
        validateSelectedBlock();

        // Update block placement preview when holding a block item
        updatePlacementPreview(input, entities);

        // Handle charged shot system for ranged weapons
        boolean leftMouseHeld = input.isMouseButtonPressed(java.awt.event.MouseEvent.BUTTON1);
        boolean leftMouseJustPressed = input.isLeftMouseJustPressed();

        // Check if UI consumed the click (e.g., clicking on a UI button)
        boolean clickConsumedByUI = input.isClickConsumedByUI();

        // Left Mouse Click - click on blocks to select/mine them or fire projectiles
        // Blocks work like UI elements - click to select, arrow keys to choose direction, click again to mine
        // Skip if click was consumed by UI elements
        if (leftMouseJustPressed && !clickConsumedByUI) {
            if (inventory.isOpen()) {
                // Try auto-equip in open inventory
                if (inventory.handleLeftClick(input.getMouseX(), input.getMouseY())) {
                    syncHeldItemWithInventory();
                }
            } else if (heldItem != null && heldItem.isRangedWeapon()) {
                if (heldItem.isChargeable()) {
                    // Start charging for chargeable weapons
                    startCharging();
                } else {
                    // Fire immediately for non-chargeable weapons
                    fireProjectile(entities);
                }
            } else {
                // Click-to-select block mining system
                handleBlockClick(entities, input);
            }
        }

        // Update charging while left mouse is held
        if (isCharging && leftMouseHeld) {
            updateCharging(deltaSeconds);
        }

        // Fire charged shot when mouse is released
        if (isCharging && !leftMouseHeld) {
            fireChargedProjectile(entities);
        }

        // E key - fires ranged weapon or mines selected block
        if (input.isKeyJustPressed('e')) {
            if (heldItem != null && heldItem.isRangedWeapon()) {
                fireProjectile(entities);
            } else if (blockHelper.getSelectedBlock() != null) {
                // Mine the selected block from the current direction
                mineSelectedBlock(entities);
            }
        }

        // Right Mouse Click - place block, attack, or use item
        // Priority: Block placement > Consumable > Attack
        if (input.isRightMouseJustPressed() && !clickConsumedByUI) {
            boolean actionTaken = false;

            // First priority: Place block if holding a block item
            if (heldItem != null && heldItem.getCategory() == Item.ItemCategory.BLOCK) {
                if (camera != null) {
                    int worldX = camera.screenToWorldX(input.getMouseX());
                    int worldY = camera.screenToWorldY(input.getMouseY());
                    actionTaken = tryPlaceBlock(entities, worldX, worldY);
                }
            }

            // Second priority: Consume item
            if (!actionTaken && heldItem != null && heldItem.isConsumable()) {
                startEating();
                actionTaken = true;
            }

            // Third priority: Attack
            if (!actionTaken && attack()) {
                Rectangle attackBounds = getAttackBounds();
                if (attackBounds != null) {
                    EntityPhysics.checkPlayerAttack(this, attackBounds, getAttackDamage(), 8.0, entities);
                    if (audioManager != null) {
                        audioManager.playSound("attack");
                    }
                }
            }
        }

        // F key - attack only (separate from right-click for dedicated combat key)
        if (input.isKeyJustPressed('f')) {
            if (heldItem != null && heldItem.isConsumable()) {
                startEating();
            } else if (attack()) {
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

            // Update triggered item animations
            heldItem.triggerAnimationForAction(
                spriteAnimation.getState(),
                isCharging,
                (float) chargePercent
            );
            heldItem.updateTriggeredAnimation(deltaMs);
        }

        // Update triggered animation manager
        triggeredAnimManager.update(deltaMs);

        // Trigger sprint particles
        updateSprintParticles(deltaSeconds, isMoving);

        // Trigger jump/land particles
        updateJumpParticles(deltaSeconds);
    }

    /**
     * Updates sprint particle effects.
     */
    private void updateSprintParticles(double deltaSeconds, boolean isMoving) {
        if (isSprinting && isMoving && onGround) {
            sprintParticleTimer += deltaSeconds;
            if (sprintParticleTimer >= SPRINT_PARTICLE_INTERVAL) {
                sprintParticleTimer = 0;

                // Spawn sprint particle behind player
                int particleX = facingRight ? x - 10 : x + width;
                int particleY = y + height - 20;

                triggeredAnimManager.triggerParticle(
                    ParticleAnimationState.SPRINT_LINES,
                    particleX, particleY, 30, 20, null, 0.25, false
                );

                // Also spawn dust at feet
                triggeredAnimManager.triggerParticle(
                    ParticleAnimationState.RUN_DUST,
                    x + width / 2 - 10, y + height - 10, 20, 15, null, 0.3, false
                );
            }
        } else {
            sprintParticleTimer = 0;
        }
    }

    /**
     * Updates jump and landing particle effects.
     */
    private void updateJumpParticles(double deltaSeconds) {
        // Update cooldown timer
        if (jumpParticleTimer > 0) {
            jumpParticleTimer -= deltaSeconds;
        }

        // Landing effect - transition from air to ground
        if (onGround && !wasOnGround && jumpParticleTimer <= 0) {
            // Trigger landing dust
            triggeredAnimManager.triggerLandDust(x + width / 2, y + height);
            jumpParticleTimer = JUMP_PARTICLE_COOLDOWN;
        }

        // Track ground state for next frame
        wasOnGround = onGround;
    }

    /**
     * Handles multi-jump input (single, double, triple jump).
     * Uses isKeyJustPressed for immediate response on key press.
     * Checks both space char and VK_SPACE keyCode for maximum responsiveness.
     */
    private void handleJumping(InputManager input) {
        // Check both space char and VK_SPACE keyCode for reliable detection
        // IMPORTANT: Must consume BOTH to prevent double-triggering since they're tracked separately
        boolean spaceCharPressed = input.isKeyJustPressed(' ');
        boolean spaceKeyPressed = input.isKeyJustPressed(java.awt.event.KeyEvent.VK_SPACE);
        boolean spacePressed = spaceCharPressed || spaceKeyPressed;

        if (spacePressed && jumpsRemaining > 0) {
            if (onGround) {
                // First jump from ground
                velY = jumpStrength;
                onGround = false;
                jumpsRemaining--;
                currentJumpNumber = 1;

                // Trigger jump dust particles
                triggeredAnimManager.triggerJumpDust(x + width / 2, y + height);

                if (audioManager != null) {
                    audioManager.playSound("jump");
                }
            } else if (jumpsRemaining > 0) {
                // Air jump (double or triple)
                // Handle edge case: if player fell off ledge without jumping first,
                // currentJumpNumber is 0 - treat this as consuming the first jump
                if (currentJumpNumber == 0) {
                    // Fell off a ledge - first air press gives a weaker "recovery" jump
                    currentJumpNumber = 1;
                    velY = doubleJumpStrength;  // Use double jump strength as recovery
                } else {
                    // Normal air jump sequence
                    currentJumpNumber++;
                    if (currentJumpNumber == 2) {
                        velY = doubleJumpStrength;
                    } else if (currentJumpNumber == 3) {
                        velY = tripleJumpStrength;
                    }
                }

                // Trigger multi-jump magic effect
                triggeredAnimManager.triggerParticle(
                    ParticleAnimationState.MULTI_JUMP,
                    x + width / 2 - 20, y + height / 2 - 10,
                    40, 20, null, 0.4, false
                );

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
        Item consumedAmmoItem = null;  // For applying status effects from special arrows

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
            // Check if ammo provides bonus damage and status effects
            consumedAmmoItem = consumedAmmo.getLinkedItem();
            if (consumedAmmoItem != null) {
                bonusDamage = consumedAmmoItem.getDamage();
            }
        }

        // Create projectile using the saved item reference
        // Position projectile at player's upper body, offset in aim direction
        int playerCenterX = x + width / 2;
        int playerCenterY = y + height / 3;
        int spawnOffset = 20;  // Distance from player center to spawn projectile
        int projX = playerCenterX + (int)(aimDirX * spawnOffset);
        int projY = playerCenterY + (int)(aimDirY * spawnOffset);

        // Use calculated aim direction for projectile trajectory
        ProjectileEntity projectile = itemForProjectile.createProjectile(projX, projY, aimDirX, aimDirY, true);
        if (projectile != null) {
            // Apply bonus damage from ammo
            if (bonusDamage > 0) {
                projectile.setDamage(projectile.getDamage() + bonusDamage);
            }

            // Apply status effect from special ammo (fire/ice arrows)
            if (consumedAmmoItem != null && consumedAmmoItem.hasStatusEffect()) {
                projectile.setStatusEffect(
                    consumedAmmoItem.getStatusEffectType(),
                    consumedAmmoItem.getStatusEffectDuration(),
                    consumedAmmoItem.getStatusEffectDamagePerTick(),
                    consumedAmmoItem.getStatusEffectDamageMultiplier()
                );
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

    // ==================== Charged Shot System ====================

    /**
     * Starts charging a shot for a chargeable weapon.
     */
    private void startCharging() {
        if (heldItem == null || !heldItem.isChargeable()) return;
        if (fireTimer > 0) return; // On cooldown

        isCharging = true;
        chargeTimer = 0;
        chargePercent = 0;
        chargeReady = false;
    }

    /**
     * Updates the charge timer while mouse is held.
     */
    private void updateCharging(double deltaSeconds) {
        if (!isCharging || heldItem == null) return;

        chargeTimer += deltaSeconds;
        chargePercent = heldItem.getChargePercent((float)chargeTimer);

        // Check if minimum charge reached
        if (chargeTimer >= heldItem.getMinChargeTime()) {
            chargeReady = true;
        }

        // Cap at max charge
        if (chargeTimer > heldItem.getMaxChargeTime()) {
            chargeTimer = heldItem.getMaxChargeTime();
            chargePercent = 1.0;
        }
    }

    /**
     * Cancels the current charge without firing.
     */
    private void cancelCharge() {
        isCharging = false;
        chargeTimer = 0;
        chargePercent = 0;
        chargeReady = false;
    }

    /**
     * Fires a charged projectile with damage/speed scaled by charge level.
     * Handles both mana-based weapons (staffs) and ammo-based weapons (bows) differently:
     * - Mana weapons: consume mana, scale projectile size
     * - Ammo weapons: consume arrows/bolts, arrows do NOT scale in size
     */
    private void fireChargedProjectile(ArrayList<Entity> entities) {
        if (!isCharging || heldItem == null) {
            cancelCharge();
            return;
        }

        // Check if minimum charge was reached
        if (!chargeReady) {
            cancelCharge();
            return;
        }

        // Determine if this weapon uses mana or physical ammo
        String ammoType = heldItem.getAmmoItemName();
        boolean usesMana = "mana".equalsIgnoreCase(ammoType);

        // Check and consume resources based on weapon type
        int bonusDamage = 0;
        Item consumedAmmoItem = null;  // For applying status effects from special arrows

        if (usesMana) {
            // Magic weapons consume mana based on charge level
            int manaCost = heldItem.getManaCostForCharge((float)chargePercent);
            if (currentMana < manaCost) {
                cancelCharge();
                return;
            }
            currentMana -= manaCost;
        } else if (ammoType != null && !ammoType.isEmpty()) {
            // Physical weapons (bows) consume ammo from inventory
            ItemEntity consumedAmmo = inventory.consumeAmmo(ammoType);
            if (consumedAmmo == null) {
                // No ammo available - can't fire
                cancelCharge();
                return;
            }
            // Check if ammo provides bonus damage and status effects
            consumedAmmoItem = consumedAmmo.getLinkedItem();
            if (consumedAmmoItem != null) {
                bonusDamage = consumedAmmoItem.getDamage();
            }
        }

        // Calculate damage and speed multipliers
        float damageMultiplier = heldItem.getDamageMultiplierForCharge((float)chargePercent);
        float speedMultiplier = heldItem.getSpeedMultiplierForCharge((float)chargePercent);

        // Create projectile
        int playerCenterX = x + width / 2;
        int playerCenterY = y + height / 3;
        int spawnOffset = 20;
        int projX = playerCenterX + (int)(aimDirX * spawnOffset);
        int projY = playerCenterY + (int)(aimDirY * spawnOffset);

        // Calculate modified velocity
        double baseSpeed = heldItem.getProjectileSpeed();
        double chargedSpeed = baseSpeed * speedMultiplier;
        double velX = aimDirX * chargedSpeed;
        double velY = aimDirY * chargedSpeed;

        // Create the projectile directly with modified stats
        ProjectileEntity projectile = new ProjectileEntity(
            projX, projY,
            heldItem.getProjectileType(),
            (int)(heldItem.getProjectileDamage() * damageMultiplier),
            velX, velY,
            true  // fromPlayer
        );

        if (projectile != null) {
            // Apply bonus damage from ammo
            if (bonusDamage > 0) {
                projectile.setDamage(projectile.getDamage() + bonusDamage);
            }

            // Only scale projectile size for magic weapons, NOT for arrows/bolts
            if (usesMana) {
                float sizeMultiplier = 1.0f + (heldItem.getChargeSizeMultiplier() - 1.0f) * (float)chargePercent;
                projectile.setScale(sizeMultiplier);
            }
            // Arrows/bolts stay at normal size regardless of charge

            // Apply status effect from special ammo (fire/ice arrows)
            if (consumedAmmoItem != null && consumedAmmoItem.hasStatusEffect()) {
                projectile.setStatusEffect(
                    consumedAmmoItem.getStatusEffectType(),
                    consumedAmmoItem.getStatusEffectDuration(),
                    consumedAmmoItem.getStatusEffectDamagePerTick(),
                    consumedAmmoItem.getStatusEffectDamageMultiplier()
                );
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

        // Reset charging state
        cancelCharge();
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

            // Draw equipment in front (armor, etc.) - excludes held items
            equipmentOverlay.drawInFront(g, x, y, width, height, facingRight, currentState);

            // Draw held item at hand position (animated GIF)
            drawHeldItemAtHand(g2d, currentState);
        }

        // Debug: draw bounds
        g.setColor((airTime > 3) ? Color.ORANGE : (isSprinting ? Color.CYAN : Color.RED));
        g2d.setStroke(new BasicStroke(2));
        g.drawRect(x, y, width, height);

        // Draw selection highlight and directional arrow on selected block
        BlockEntity selectedBlock = blockHelper.getSelectedBlock();
        if (selectedBlock != null && !selectedBlock.isBroken()) {
            Rectangle blockBounds = selectedBlock.getFullBounds();
            int centerX = blockBounds.x + blockBounds.width / 2;
            int centerY = blockBounds.y + blockBounds.height / 2;

            // Draw selection highlight around the block
            drawSelectionHighlight(g2d, blockBounds);

            // Draw the directional arrow showing mining direction
            drawMiningArrow(g2d, centerX, centerY, blockHelper.getMiningDirection());
        }

        // Draw block placement preview when holding a block item
        if (showPlacementPreview) {
            drawPlacementPreview(g2d, previewGridX, previewGridY, previewCanPlace);
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

        // Draw charge bar when charging a shot
        if (isCharging) {
            drawChargeBar(g2d);
        }

        // Draw aim indicator when holding a ranged weapon
        if (heldItem != null && heldItem.isRangedWeapon()) {
            drawAimIndicator(g2d);
        }

        // Draw particles from the triggered animation manager
        triggeredAnimManager.drawParticles(g);
    }

    /**
     * Draws the aim indicator showing projectile trajectory direction.
     * Shows a line from the player toward the mouse cursor with a crosshair.
     */
    private void drawAimIndicator(Graphics2D g2d) {
        // Calculate start position (where projectiles spawn)
        int startX = x + width / 2;
        int startY = y + height / 3;

        // Calculate end position of aim line
        int endX = startX + (int)(aimDirX * AIM_INDICATOR_LENGTH);
        int endY = startY + (int)(aimDirY * AIM_INDICATOR_LENGTH);

        // Save original stroke
        Stroke originalStroke = g2d.getStroke();

        // Draw outer glow/shadow for visibility
        g2d.setStroke(new BasicStroke(AIM_INDICATOR_WIDTH + 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.drawLine(startX, startY, endX, endY);

        // Draw main aim line with gradient effect based on fire readiness
        boolean canFire = fireTimer <= 0;
        Color aimColor = canFire ? new Color(255, 200, 50, 200) : new Color(150, 150, 150, 150);
        g2d.setStroke(new BasicStroke(AIM_INDICATOR_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(aimColor);
        g2d.drawLine(startX, startY, endX, endY);

        // Draw crosshair at aim target
        int crosshairSize = 8;
        int crosshairX = aimTargetWorldX;
        int crosshairY = aimTargetWorldY;

        // Only draw crosshair if camera is available (world coordinates are valid)
        if (camera != null) {
            // Outer ring (black shadow)
            g2d.setStroke(new BasicStroke(2));
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.drawOval(crosshairX - crosshairSize - 1, crosshairY - crosshairSize - 1,
                        crosshairSize * 2 + 2, crosshairSize * 2 + 2);

            // Inner crosshair
            g2d.setColor(canFire ? new Color(255, 100, 50) : new Color(100, 100, 100));
            g2d.drawOval(crosshairX - crosshairSize, crosshairY - crosshairSize,
                        crosshairSize * 2, crosshairSize * 2);

            // Cross lines
            g2d.drawLine(crosshairX - crosshairSize - 3, crosshairY, crosshairX - 3, crosshairY);
            g2d.drawLine(crosshairX + 3, crosshairY, crosshairX + crosshairSize + 3, crosshairY);
            g2d.drawLine(crosshairX, crosshairY - crosshairSize - 3, crosshairX, crosshairY - 3);
            g2d.drawLine(crosshairX, crosshairY + 3, crosshairX, crosshairY + crosshairSize + 3);

            // Center dot
            g2d.setColor(canFire ? Color.RED : Color.GRAY);
            g2d.fillOval(crosshairX - 2, crosshairY - 2, 4, 4);
        }

        // Restore original stroke
        g2d.setStroke(originalStroke);
    }

    /**
     * Draws the charge bar above the player showing charge progress.
     * The bar fills from left to right as the shot charges.
     * Color changes from yellow to orange to red as charge increases.
     */
    private void drawChargeBar(Graphics2D g2d) {
        int barX = x + (width - CHARGE_BAR_WIDTH) / 2;
        int barY = y - 20;

        // Background (dark gray)
        g2d.setColor(new Color(40, 40, 40, 200));
        g2d.fillRect(barX - 1, barY - 1, CHARGE_BAR_WIDTH + 2, CHARGE_BAR_HEIGHT + 2);

        // Unfilled portion (darker)
        g2d.setColor(new Color(60, 60, 60));
        g2d.fillRect(barX, barY, CHARGE_BAR_WIDTH, CHARGE_BAR_HEIGHT);

        // Calculate fill width
        int fillWidth = (int)(CHARGE_BAR_WIDTH * chargePercent);

        // Color gradient: Yellow -> Orange -> Red based on charge
        Color chargeColor;
        if (chargePercent < 0.5) {
            // Yellow to orange (0% to 50%)
            float t = (float)(chargePercent * 2);
            chargeColor = new Color(
                255,
                (int)(255 - 55 * t),  // 255 -> 200
                (int)(50 - 50 * t)    // 50 -> 0
            );
        } else {
            // Orange to red (50% to 100%)
            float t = (float)((chargePercent - 0.5) * 2);
            chargeColor = new Color(
                255,
                (int)(200 - 150 * t),  // 200 -> 50
                0
            );
        }

        // Draw filled portion
        g2d.setColor(chargeColor);
        g2d.fillRect(barX, barY, fillWidth, CHARGE_BAR_HEIGHT);

        // Glow effect at full charge
        if (chargePercent >= 1.0) {
            g2d.setColor(new Color(255, 255, 200, 100));
            g2d.fillRect(barX - 2, barY - 2, CHARGE_BAR_WIDTH + 4, CHARGE_BAR_HEIGHT + 4);
        }

        // Border
        g2d.setColor(chargeReady ? Color.WHITE : new Color(150, 150, 150));
        g2d.drawRect(barX, barY, CHARGE_BAR_WIDTH, CHARGE_BAR_HEIGHT);

        // Show charge percentage text for feedback
        if (chargePercent > 0.1) {
            int percent = (int)(chargePercent * 100);
            String text = percent + "%";
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = barX + (CHARGE_BAR_WIDTH - fm.stringWidth(text)) / 2;
            int textY = barY + CHARGE_BAR_HEIGHT + 12;

            // Text shadow
            g2d.setColor(Color.BLACK);
            g2d.drawString(text, textX + 1, textY + 1);

            // Text
            g2d.setColor(chargeReady ? Color.WHITE : Color.GRAY);
            g2d.drawString(text, textX, textY);
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

    // ==================== BlockInteractionHandler Implementation ====================

    @Override
    public BlockEntity getSelectedBlock() {
        return blockHelper.getSelectedBlock();
    }

    @Override
    public void selectBlock(BlockEntity block) {
        blockHelper.selectBlock(block);
    }

    @Override
    public void deselectBlock() {
        blockHelper.deselectBlock();
    }

    @Override
    public int getMiningDirection() {
        return blockHelper.getMiningDirection();
    }

    @Override
    public void setMiningDirection(int direction) {
        blockHelper.setMiningDirection(direction);
    }

    @Override
    public boolean isBlockInRange(BlockEntity block) {
        return blockHelper.isBlockInRange(block, getCenterX(), getCenterY());
    }

    @Override
    public void validateSelectedBlock() {
        blockHelper.validateSelectedBlock(getCenterX(), getCenterY());
    }

    @Override
    public boolean mineSelectedBlock(ArrayList<Entity> entities) {
        boolean broken = blockHelper.mineSelectedBlock(entities, getEquippedToolType(), audioManager);
        if (broken) {
            lastBrokenBlock = blockHelper.getLastBrokenBlock();
            lastDroppedItem = blockHelper.getLastDroppedItem();
        }
        return broken;
    }

    @Override
    public boolean handleBlockClick(ArrayList<Entity> entities, int worldX, int worldY) {
        return blockHelper.handleBlockClick(entities, worldX, worldY,
                getCenterX(), getCenterY(), getEquippedToolType(), audioManager);
    }

    /**
     * Handles left-click on blocks using screen coordinates from InputManager.
     */
    private void handleBlockClick(ArrayList<Entity> entities, InputManager input) {
        if (camera == null) return;

        int worldX = camera.screenToWorldX(input.getMouseX());
        int worldY = camera.screenToWorldY(input.getMouseY());
        handleBlockClick(entities, worldX, worldY);
    }

    @Override
    public boolean canPlaceBlockAt(int worldX, int worldY, ArrayList<Entity> entities) {
        return blockHelper.canPlaceBlockAt(worldX, worldY, entities,
                getCenterX(), getCenterY(), getBounds());
    }

    @Override
    public boolean tryPlaceBlock(ArrayList<Entity> entities, int worldX, int worldY) {
        boolean placed = blockHelper.tryPlaceBlock(entities, worldX, worldY,
                heldItem, inventory, audioManager,
                getCenterX(), getCenterY(), getBounds());
        if (placed) {
            syncHeldItemWithInventory();
            System.out.println("Placed block at world (" + worldX + "," + worldY + ")");
        }
        return placed;
    }

    @Override
    public ToolType getEquippedToolType() {
        return inventory.getHeldToolType();
    }

    @Override
    public AudioManager getAudioManager() {
        return audioManager;
    }

    @Override
    public Camera getCamera() {
        return camera;
    }

    @Override
    public int getCenterX() {
        return x + width / 2;
    }

    @Override
    public int getCenterY() {
        return y + height / 2;
    }

    @Override
    public void drawMiningArrow(Graphics2D g2d, int centerX, int centerY, int direction) {
        blockHelper.drawMiningArrow(g2d, centerX, centerY, direction);
    }

    /**
     * Updates the block placement preview when holding a block item.
     */
    private void updatePlacementPreview(InputManager input, ArrayList<Entity> entities) {
        if (camera != null && heldItem != null && heldItem.getCategory() == Item.ItemCategory.BLOCK) {
            int worldX = camera.screenToWorldX(input.getMouseX());
            int worldY = camera.screenToWorldY(input.getMouseY());
            previewGridX = BlockEntity.pixelToGrid(worldX);
            previewGridY = BlockEntity.pixelToGrid(worldY);
            previewCanPlace = canPlaceBlockAt(worldX, worldY, entities);
            showPlacementPreview = true;
        } else {
            showPlacementPreview = false;
        }
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
    public double getManaRegenRate() {
        return manaRegenRate;
    }

    @Override
    public void setManaRegenRate(double rate) {
        this.manaRegenRate = rate;
    }

    @Override
    public void setStamina(int stamina) {
        this.currentStamina = Math.max(0, Math.min(maxStamina, stamina));
        this.currentStaminaFloat = currentStamina;
    }

    @Override
    public double getStaminaRegenRate() {
        return staminaRegenRate;
    }

    @Override
    public void setStaminaRegenRate(double rate) {
        this.staminaRegenRate = rate;
    }

    @Override
    public double getStaminaDrainRate() {
        return staminaDrainRate;
    }

    @Override
    public void setStaminaDrainRate(double rate) {
        this.staminaDrainRate = rate;
    }

    @Override
    public void setHealth(int health) {
        this.currentHealth = Math.max(0, Math.min(maxHealth, health));
    }

    @Override
    public void updateResourceRegeneration(double deltaSeconds) {
        // Mana regeneration
        currentMana = Math.min(maxMana, currentMana + (int)(manaRegenRate * deltaSeconds));

        // Stamina regeneration (only when not sprinting)
        if (!isSprinting) {
            currentStaminaFloat = Math.min(maxStamina, currentStaminaFloat + staminaRegenRate * deltaSeconds);
            currentStamina = (int) currentStaminaFloat;
        }
    }

    // ==================== CombatCapable Implementation ====================

    @Override
    public int getBaseAttackDamage() {
        return baseAttackDamage;
    }

    @Override
    public void setBaseAttackDamage(int damage) {
        this.baseAttackDamage = damage;
    }

    @Override
    public int getAttackRange() {
        return getEffectiveAttackRange();
    }

    @Override
    public int getBaseAttackRange() {
        return baseAttackRange;
    }

    @Override
    public void setBaseAttackRange(int range) {
        this.baseAttackRange = range;
    }

    @Override
    public double getAttackCooldown() {
        return attackCooldown;
    }

    @Override
    public void setAttackCooldown(double cooldown) {
        this.attackCooldown = cooldown;
    }

    @Override
    public double getInvincibilityDuration() {
        return invincibilityTime;
    }

    @Override
    public void setInvincibilityDuration(double duration) {
        this.invincibilityTime = duration;
    }

    @Override
    public boolean canFireRanged() {
        return heldItem != null && heldItem.isRangedWeapon() && fireTimer <= 0;
    }

    @Override
    public boolean fireProjectile(ArrayList<Entity> entities, double dirX, double dirY) {
        if (!canFireRanged()) return false;
        // Store current aim direction and restore after
        double oldAimDirX = aimDirX;
        double oldAimDirY = aimDirY;
        aimDirX = dirX;
        aimDirY = dirY;
        fireProjectile(entities);
        aimDirX = oldAimDirX;
        aimDirY = oldAimDirY;
        return true;
    }

    @Override
    public void updateCombatTimers(double deltaSeconds) {
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

        // Invincibility timer
        if (invincibilityTimer > 0) {
            invincibilityTimer -= deltaSeconds;
        }
    }

    // ==================== Held Item System ====================

    /**
     * Draws the held item at the player's hand position using animated GIF textures.
     * Items are drawn at hand position, not as full-sprite overlays.
     *
     * @param g2d Graphics context
     * @param currentState Current player animation state
     */
    private void drawHeldItemAtHand(Graphics2D g2d, SpriteAnimation.ActionState currentState) {
        ItemEntity heldEntity = inventory.getHeldItem();
        if (heldEntity == null) return;

        // Get the animated sprite frame from ItemEntity (uses GIF animation)
        Image itemSprite = heldEntity.getSprite();
        if (itemSprite == null) return;

        // Item size at hand (larger than old mini icon)
        int itemSize = 32;

        // Calculate hand position based on player facing and animation state
        int handOffsetX;
        int handOffsetY = height / 2 - itemSize / 2;  // Centered vertically at mid-body

        // Base hand position
        if (facingRight) {
            handOffsetX = width - 12;  // Right side of player
        } else {
            handOffsetX = -itemSize + 12;  // Left side of player
        }

        // Adjust position based on animation state for natural movement
        switch (currentState) {
            case ATTACK:
                // Extend arm forward during attack
                handOffsetX = facingRight ? width + 8 : -itemSize - 8;
                handOffsetY = height / 2 - itemSize / 2 - 4;  // Slightly raised
                break;
            case FIRE:
                // Extended arm for ranged attack
                handOffsetX = facingRight ? width + 4 : -itemSize - 4;
                break;
            case USE_ITEM:
            case EAT:
                // Bring item closer to face
                handOffsetX = facingRight ? width / 2 : -itemSize / 2;
                handOffsetY = height / 3 - itemSize / 2;  // Raised to face level
                break;
            case RUN:
            case SPRINT:
                // Slight bob during running
                int runBob = (int)(Math.sin(System.currentTimeMillis() * 0.01) * 2);
                handOffsetY += runBob;
                break;
            case JUMP:
            case DOUBLE_JUMP:
            case TRIPLE_JUMP:
            case FALL:
                // Arm raised during jump/fall
                handOffsetY = height / 3 - itemSize / 2;
                break;
            default:
                // IDLE, WALK - use base positions
                break;
        }

        int itemX = x + handOffsetX;
        int itemY = y + handOffsetY;

        // Save transform for restoration
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();

        // Apply rotation based on state for natural held appearance
        double rotation = 0;
        if (currentState == SpriteAnimation.ActionState.ATTACK) {
            // Swing rotation during attack
            rotation = facingRight ? Math.toRadians(-30) : Math.toRadians(30);
        } else if (heldItem != null && heldItem.isRangedWeapon()) {
            // Slight angle for bows/crossbows pointing toward aim
            rotation = facingRight ? Math.toRadians(-15) : Math.toRadians(15);
        }

        // Draw the item
        if (!facingRight) {
            // Flip horizontally for left-facing
            g2d.translate(itemX + itemSize / 2.0, itemY + itemSize / 2.0);
            g2d.rotate(rotation);
            g2d.scale(-1, 1);
            g2d.translate(-itemSize / 2.0, -itemSize / 2.0);
            g2d.drawImage(itemSprite, 0, 0, itemSize, itemSize, null);
        } else {
            g2d.translate(itemX + itemSize / 2.0, itemY + itemSize / 2.0);
            g2d.rotate(rotation);
            g2d.translate(-itemSize / 2.0, -itemSize / 2.0);
            g2d.drawImage(itemSprite, 0, 0, itemSize, itemSize, null);
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

        // Detect block items by name (materials that can be placed)
        boolean isBlockByName = lowerName.contains("dirt") || lowerName.contains("grass") ||
                                 lowerName.contains("stone") || lowerName.contains("cobble") ||
                                 lowerName.contains("wood") || lowerName.contains("plank") ||
                                 lowerName.contains("brick") || lowerName.contains("sand") ||
                                 lowerName.contains("glass") || lowerName.contains("leaves") ||
                                 lowerName.contains("ore") || lowerName.contains("block");

        if (isRangedByName) {
            category = Item.ItemCategory.RANGED_WEAPON;
        } else if (isThrowableByName) {
            category = Item.ItemCategory.THROWABLE;
        } else if (isBlockByName) {
            category = Item.ItemCategory.BLOCK;
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
                case "block": category = Item.ItemCategory.BLOCK; break;
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
