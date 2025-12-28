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

    // Attack system
    private boolean isAttacking = false;
    private double attackTimer = 0;
    private double attackCooldown = 0.4; // Seconds between attacks
    private double attackDuration = 0.2; // How long attack animation lasts
    private int attackDamage = 10;
    private int attackRange = 60;
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

        System.out.println("SpritePlayerEntity created at: x=" + x + " y=" + y +
                " width=" + width + " height=" + height);
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

        System.out.println("SpritePlayerEntity created with custom animation at: x=" + x + " y=" + y);
    }

    /**
     * Loads animations from a sprite directory.
     *
     * @param spriteDir Directory containing animation GIFs
     */
    private void loadAnimations(String spriteDir) {
        String basePath = spriteDir.endsWith("/") ? spriteDir : spriteDir + "/";

        // Try to load each action state
        spriteAnimation.loadAction(SpriteAnimation.ActionState.IDLE, basePath + "idle.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.WALK, basePath + "walk.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.JUMP, basePath + "jump.gif");

        // Optional: load additional states if they exist
        spriteAnimation.loadAction(SpriteAnimation.ActionState.RUN, basePath + "run.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.FALL, basePath + "fall.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.ATTACK, basePath + "attack.gif");
        spriteAnimation.loadAction(SpriteAnimation.ActionState.HURT, basePath + "hurt.gif");

        System.out.println("SpritePlayerEntity: Loaded animations from " + spriteDir);
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

        System.out.println("SpritePlayerEntity: Equipped " + itemName + " overlay from " + spriteDir);
    }

    @Override
    public void setGroundY(int groundY) {
        this.groundY = groundY;
        System.out.println("SpritePlayerEntity ground level updated to: " + groundY);
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

        // Update invincibility timer
        if (invincibilityTimer > 0) {
            invincibilityTimer -= deltaMs / 1000.0;
        }

        int speed = 4;
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

        // Scroll wheel cycles mining direction
        int scroll = input.getScrollDirection();
        if (scroll != 0) {
            miningDirection = (miningDirection + scroll + NUM_DIRECTIONS) % NUM_DIRECTIONS;
        }

        // Update targeted block
        updateTargetedBlock(entities);

        // E key - mine block
        if (input.isKeyJustPressed('e')) {
            int direction = getMiningDirection();
            tryMineBlock(entities, direction);
        }

        // Left Mouse Click or F key - attack
        if (input.isLeftMouseJustPressed() || input.isKeyJustPressed('f')) {
            if (attack()) {
                // Attack started - check for mob hits
                Rectangle attackBounds = getAttackBounds();
                if (attackBounds != null) {
                    EntityPhysics.checkPlayerAttack(this, attackBounds, attackDamage, 8.0, entities);
                    if (audioManager != null) {
                        audioManager.playSound("attack");
                    }
                }
            }
        }

        // Update attack state
        double deltaSeconds = deltaMs / 1000.0;
        if (attackTimer > 0) {
            attackTimer -= deltaSeconds;
        }
        if (isAttacking && attackTimer <= attackCooldown - attackDuration) {
            isAttacking = false;
        }

        // Apply push forces from collisions
        EntityPhysics.processCollisions(entities, this, deltaSeconds);

        // Horizontal movement
        if (input.isKeyPressed('a')) {
            newX -= speed;
            facingRight = false;
            isMoving = true;
        }
        if (input.isKeyPressed('d')) {
            newX += speed;
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

        // Jumping
        if (input.isKeyPressed(' ') && onGround) {
            velY = jumpStrength;
            onGround = false;

            if (audioManager != null) {
                audioManager.playSound("jump");
            }
        }

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

        // Update animation state based on movement
        updateAnimationState(isMoving);

        // Update animations
        spriteAnimation.update(deltaMs);

        // Sync equipment overlays to base animation frame
        equipmentOverlay.syncToFrame(
                spriteAnimation.getCurrentFrameIndex(),
                spriteAnimation.getState()
        );
    }

    /**
     * Updates the animation state based on player movement.
     */
    private void updateAnimationState(boolean isMoving) {
        SpriteAnimation.ActionState newState;

        if (airTime > 3) {
            // In the air
            if (velY < 0) {
                newState = SpriteAnimation.ActionState.JUMP;
            } else {
                // Falling - use FALL if available, otherwise JUMP
                newState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.FALL)
                        ? SpriteAnimation.ActionState.FALL
                        : SpriteAnimation.ActionState.JUMP;
            }
        } else if (isMoving) {
            newState = SpriteAnimation.ActionState.WALK;
        } else {
            newState = SpriteAnimation.ActionState.IDLE;
        }

        spriteAnimation.setState(newState);
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        SpriteAnimation.ActionState currentState = spriteAnimation.getState();

        // Draw equipment behind (BACK slot - capes, etc.)
        equipmentOverlay.drawBehind(g, x, y, width, height, facingRight, currentState);

        // Draw base sprite
        spriteAnimation.draw(g, x, y, width, height, facingRight);

        // Draw equipment in front (armor, weapons, etc.)
        equipmentOverlay.drawInFront(g, x, y, width, height, facingRight, currentState);

        // Debug: draw bounds
        g.setColor((airTime > 3) ? Color.ORANGE : Color.RED);
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

        System.out.println("SpritePlayer took " + damage + " damage! Health: " + currentHealth + "/" + maxHealth);
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
     * @return Rectangle of attack area, or null if not attacking
     */
    public Rectangle getAttackBounds() {
        if (!isAttacking) return null;

        int attackX;
        if (facingRight) {
            attackX = x + width;
        } else {
            attackX = x - attackRange;
        }

        int attackY = y + (height - attackHeight) / 2;
        return new Rectangle(attackX, attackY, attackRange, attackHeight);
    }

    /**
     * Gets the attack damage value.
     */
    public int getAttackDamage() {
        return attackDamage;
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
}
