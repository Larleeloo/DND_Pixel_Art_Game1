import java.awt.*;
import java.util.ArrayList;

/**
 * Player entity that uses bone-based skeletal animation.
 * Supports running, jumping, and idle animations with textured bones.
 */
public class PlayerBoneEntity extends Entity implements PlayerBase {

    // Physics
    private double velX = 0;
    private double velY = 0;
    private final double gravity = 0.5;
    private final double jumpStrength = -10;
    private final int moveSpeed = 4;

    // State
    private boolean onGround = false;
    private boolean facingRight = true;
    private int airTime = 0;
    private boolean isMoving = false;

    // Health system
    private int maxHealth = 100;
    private int currentHealth = 100;
    private double invincibilityTime = 1.0;  // Seconds of invincibility after damage
    private double invincibilityTimer = 0;
    private double damageKnockbackX = 0;
    private double damageKnockbackY = 0;

    // Dimensions (collision box) - original size
    private int width = 48;    // Original collision width
    private int height = 109;  // Original collision height

    // Ground level
    private int groundY = 720;

    // Mining targeting system - 6 positions around player (clockwise from top)
    // 0=up, 1=right-upper, 2=right-lower, 3=down, 4=left-lower, 5=left-upper
    private int miningDirection = 1; // Start facing right-upper
    private static final int NUM_DIRECTIONS = 6;
    private BlockEntity targetedBlock = null; // Currently targeted block for crosshair

    // Block interaction
    private BlockEntity lastBrokenBlock = null;
    private ItemEntity lastDroppedItem = null;

    // Skeleton animation system
    private Skeleton skeleton;

    // Animation state names
    public static final String ANIM_IDLE = "idle";
    public static final String ANIM_RUN = "run";
    public static final String ANIM_JUMP = "jump";

    // Rendering - 1.0 for 32x32 base quality (bone sizes at final display size)
    private static final double RENDER_SCALE = 1.0;  // Scale factor for bones

    // Audio
    private AudioManager audioManager;

    // Inventory (optional, can be added)
    private Inventory inventory;

    /**
     * Creates a new bone-animated player.
     * @param x Starting X position
     * @param y Starting Y position
     */
    public PlayerBoneEntity(int x, int y) {
        super(x, y);

        // Use TextureManager to ensure player PNG textures exist (generates defaults if missing)
        String texDir = TextureManager.ensureHumanoidTextures("player");

        // Create the skeleton with default humanoid structure
        skeleton = Skeleton.createHumanoid();
        skeleton.setScale(RENDER_SCALE);

        // Load textures from PNG files so users can customize them
        String[] humanoidBones = {
            "torso", "neck", "head",
            "arm_upper_left", "arm_upper_right",
            "arm_lower_left", "arm_lower_right",
            "hand_left", "hand_right",
            "leg_upper_left", "leg_upper_right",
            "leg_lower_left", "leg_lower_right",
            "foot_left", "foot_right"
        };
        TextureManager.applyTexturesFromDir(skeleton, texDir, humanoidBones);

        // Apply any saved character customization (colors, sizes)
        CharacterCustomizationScene.applyToPlayer(skeleton);

        // Set up default animations
        setupAnimations();

        // Initialize inventory
        this.inventory = new Inventory(10);

        System.out.println("PlayerBoneEntity created at (" + x + ", " + y + ")");
    }

    /**
     * Creates a player with bone textures from a directory.
     * @param x Starting X position
     * @param y Starting Y position
     * @param textureDir Directory containing bone texture PNGs
     */
    public PlayerBoneEntity(int x, int y, String textureDir) {
        super(x, y);

        // Create skeleton with textures
        skeleton = Skeleton.createHumanoidWithTextures(textureDir);
        skeleton.setScale(RENDER_SCALE);

        // Apply any saved character customization (colors, sizes)
        CharacterCustomizationScene.applyToPlayer(skeleton);

        // Set up animations
        setupAnimations();

        // Initialize inventory
        this.inventory = new Inventory(10);

        System.out.println("PlayerBoneEntity created with textures from: " + textureDir);
    }

    /**
     * Sets up the default animations for the player.
     * Uses 2-part limbs (upper/lower arms and legs).
     */
    private void setupAnimations() {
        // Idle animation - subtle breathing
        BoneAnimation idle = BoneAnimation.createIdleAnimation("torso");
        skeleton.addAnimation(idle);

        // Running animation with 2-part limbs
        BoneAnimation run = BoneAnimation.createRunAnimation();
        skeleton.addAnimation(run);

        // Jump animation with 2-part limbs
        BoneAnimation jump = BoneAnimation.createJumpAnimation();
        skeleton.addAnimation(jump);

        // Start with idle
        skeleton.playAnimation(ANIM_IDLE);
    }

    /**
     * Loads textures for a specific bone.
     * @param boneName Name of the bone
     * @param texturePath Path to PNG or GIF texture
     */
    public void setBoneTexture(String boneName, String texturePath) {
        Bone bone = skeleton.findBone(boneName);
        if (bone != null) {
            bone.loadTexture(texturePath);
        } else {
            System.out.println("Warning: Bone '" + boneName + "' not found");
        }
    }

    /**
     * Gets the skeleton for direct manipulation.
     * @return The player's skeleton
     */
    public Skeleton getSkeleton() {
        return skeleton;
    }

    /**
     * Adds a custom animation to the player.
     * @param animation The animation to add
     */
    public void addAnimation(BoneAnimation animation) {
        skeleton.addAnimation(animation);
    }

    /**
     * NOTE: Blockbench animation import has been moved to devtools/BlockbenchAnimationImporter.java
     *
     * To import Blockbench animations:
     * 1. Run: java devtools/BlockbenchAnimationImporter <path-to-blockbench-file>
     * 2. This will convert the animation to a format that can be loaded at runtime
     *
     * For now, use the built-in animations defined in the skeleton classes.
     * @deprecated Use devtools/BlockbenchAnimationImporter for importing external animations
     */
    @Deprecated
    public int loadAnimationsFromBlockbench(String filePath) {
        System.out.println("WARNING: Blockbench import is not available at runtime.");
        System.out.println("Please use devtools/BlockbenchAnimationImporter to pre-import animations.");
        return 0;
    }

    /**
     * NOTE: Blockbench animation import has been moved to devtools/BlockbenchAnimationImporter.java
     * @deprecated Use devtools/BlockbenchAnimationImporter for importing external animations
     */
    @Deprecated
    public int loadAnimationsFromBlockbench(String filePath, java.util.Map<String, String> boneMappings) {
        System.out.println("WARNING: Blockbench import is not available at runtime.");
        System.out.println("Please use devtools/BlockbenchAnimationImporter to pre-import animations.");
        return 0;
    }

    /**
     * Sets the audio manager for sound effects.
     */
    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    /**
     * Sets the ground Y level.
     */
    public void setGroundY(int groundY) {
        this.groundY = groundY;
    }

    /**
     * Gets the inventory.
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Updates the player each frame.
     */
    public void update(InputManager input, ArrayList<Entity> entities) {
        // Track previous movement state
        boolean wasMoving = isMoving;
        isMoving = false;

        // Update invincibility timer
        if (invincibilityTimer > 0) {
            invincibilityTimer -= 1.0 / 60.0;  // Assuming 60 FPS
        }

        // Toggle inventory
        if (input.isKeyJustPressed('i')) {
            inventory.toggleOpen();
        }

        // Hotbar selection
        for (char c = '1'; c <= '5'; c++) {
            if (input.isKeyJustPressed(c)) {
                inventory.handleHotbarKey(c);
            }
        }

        // Scroll wheel cycles mining direction through all 6 positions
        int scroll = input.getScrollDirection();
        if (scroll != 0) {
            miningDirection = (miningDirection + scroll + NUM_DIRECTIONS) % NUM_DIRECTIONS;
        }

        // Update targeted block for crosshair display
        updateTargetedBlock(entities);

        // E - mine the currently targeted block
        if (input.isKeyJustPressed('e')) {
            int direction = getMiningDirection();
            tryMineBlock(entities, direction);
        }

        // Horizontal movement
        int newX = x;
        if (input.isKeyPressed('a')) {
            newX -= moveSpeed;
            facingRight = false;
            isMoving = true;
        }
        if (input.isKeyPressed('d')) {
            newX += moveSpeed;
            facingRight = true;
            isMoving = true;
        }

        // Horizontal collision check
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

        // Soft push collision with mobs (gentle push, doesn't block movement)
        Rectangle currentBounds = new Rectangle(x, y, width, height);
        for (Entity e : entities) {
            if (e instanceof MobEntity) {
                MobEntity mob = (MobEntity) e;
                Rectangle mobBounds = mob.getBounds();
                if (currentBounds.intersects(mobBounds)) {
                    // Calculate overlap and push direction
                    int playerCenterX = x + width / 2;
                    int mobCenterX = mobBounds.x + mobBounds.width / 2;

                    // Gentle push away from mob (2-3 pixels per frame)
                    int pushStrength = 2;
                    if (playerCenterX > mobCenterX) {
                        // Player is to the right of mob, push right
                        x += pushStrength;
                    } else {
                        // Player is to the left of mob, push left
                        x -= pushStrength;
                    }
                }
            }
        }

        // Collect items
        Rectangle playerBounds = new Rectangle(x, y, width, height);
        for (Entity e : entities) {
            if (e instanceof ItemEntity) {
                ItemEntity item = (ItemEntity) e;
                if (!item.isCollected() && playerBounds.intersects(e.getBounds())) {
                    item.collect();
                    inventory.addItem(item);
                    if (audioManager != null) {
                        audioManager.playSound("collect");
                    }
                }
            }
        }

        // Check for mob collisions (damage from hostile mobs)
        if (!isInvincible()) {
            for (Entity e : entities) {
                if (e instanceof MobEntity) {
                    MobEntity mob = (MobEntity) e;
                    // Only take damage from hostile mobs that are in chase/attack state
                    if (mob.getState() == MobEntity.AIState.CHASE ||
                        mob.getState() == MobEntity.AIState.ATTACK) {
                        if (playerBounds.intersects(mob.getBounds())) {
                            // Calculate knockback direction (away from mob)
                            double knockbackDir = (x > mob.getX()) ? 8 : -8;
                            takeDamage(mob.getAttackDamage(), knockbackDir, -5);
                            break;  // Only take damage from one mob per frame
                        }
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

        // Calculate new Y
        int newY = y + (int) velY;

        // Vertical collision
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

        // Ground collision
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

        // Update animation state
        updateAnimationState();

        // Update skeleton animation (60 FPS = ~16.67ms per frame)
        skeleton.update(1.0 / 60.0);
    }

    /**
     * Updates which animation should be playing based on player state.
     */
    private void updateAnimationState() {
        // Update facing direction
        skeleton.setFlipX(!facingRight);

        // Determine animation
        if (!onGround && airTime > 3) {
            // In the air - play jump animation
            if (!skeleton.isPlayingAnimation(ANIM_JUMP)) {
                skeleton.transitionTo(ANIM_JUMP, 0.1);
            }
        } else if (isMoving && onGround) {
            // Moving on ground - run animation
            if (!skeleton.isPlayingAnimation(ANIM_RUN)) {
                skeleton.transitionTo(ANIM_RUN, 0.15);
            }
        } else if (onGround) {
            // Standing still - idle animation
            if (!skeleton.isPlayingAnimation(ANIM_IDLE)) {
                skeleton.transitionTo(ANIM_IDLE, 0.2);
            }
        }
    }

    /**
     * Calculates the foot bottom position relative to torso center based on current bone scales.
     * This accounts for the connected scaling of the leg chain.
     *
     * Formula: footBottom = leg_start + leg_upper_height + leg_lower_height + foot_height
     * Where each segment is scaled by the appropriate bone's baseScale.
     *
     * @return The Y offset from torso center to foot bottom in pixels
     */
    private double calculateFootBottomOffset() {
        // Get bone scales (use right leg, left should be the same)
        Bone torso = skeleton.findBone("torso");
        Bone legUpper = skeleton.findBone("leg_upper_right");
        Bone legLower = skeleton.findBone("leg_lower_right");
        Bone foot = skeleton.findBone("foot_right");

        // Default scales if bones not found
        double torsoScale = (torso != null) ? torso.getBaseScaleY() : 1.0;
        double legUpperScale = (legUpper != null) ? legUpper.getBaseScaleY() : 1.0;
        double legLowerScale = (legLower != null) ? legLower.getBaseScaleY() : 1.0;
        double footScale = (foot != null) ? foot.getBaseScaleY() : 1.0;

        // Base values from Skeleton.java (32x32 quality):
        // - leg_upper.localY = 16 (starts at torso bottom)
        // - leg_lower.localY = 20 (= leg_upper height)
        // - foot.localY = 20 (= leg_lower height)
        // - foot height = 8

        // Calculate foot bottom with connected scaling:
        // Each position is scaled by its parent's baseScale
        double legStart = 16 * torsoScale;                    // Where legs attach to torso
        double kneePos = legStart + 20 * legUpperScale;       // leg_upper bottom
        double anklePos = kneePos + 20 * legLowerScale;       // leg_lower bottom
        double footBottom = anklePos + 8 * footScale;         // foot bottom

        return footBottom;
    }

    @Override
    public void draw(Graphics g) {
        // ====== SKELETON POSITIONING ======
        //
        // The skeleton is designed with torso center at (0,0).
        // Foot bottom position varies based on bone scaling.
        //
        // To align feet with hitbox bottom regardless of scale:
        //   footBottom = skeletonY + footBottomOffset
        //   hitboxBottom = y + height
        //   skeletonY = hitboxBottom - footBottomOffset
        //
        // This ensures the player's feet always touch the ground,
        // even when scaled smaller (player appears shorter but grounded).
        //
        double skeletonX = x + width / 2.0;
        double footBottomOffset = calculateFootBottomOffset();
        double skeletonY = y + height - footBottomOffset;  // Align feet with hitbox bottom

        skeleton.setPosition(skeletonX, skeletonY);

        // Draw the skeleton
        skeleton.draw(g);

        // Debug: draw collision box
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));

        if (airTime > 3) {
            g.setColor(Color.ORANGE);
        } else if (isMoving) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.RED);
        }
        g.drawRect(x, y, width, height);

        // Draw crosshair on targeted block
        if (targetedBlock != null && !targetedBlock.isBroken()) {
            Rectangle blockBounds = targetedBlock.getFullBounds();
            int crosshairX = blockBounds.x + blockBounds.width / 2;
            int crosshairY = blockBounds.y + blockBounds.height / 2;
            int dotSize = 6;

            // Draw white dot with black outline for visibility
            g.setColor(Color.BLACK);
            g.fillOval(crosshairX - dotSize/2 - 1, crosshairY - dotSize/2 - 1, dotSize + 2, dotSize + 2);
            g.setColor(Color.WHITE);
            g.fillOval(crosshairX - dotSize/2, crosshairY - dotSize/2, dotSize, dotSize);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Checks if the player is facing right.
     */
    public boolean isFacingRight() {
        return facingRight;
    }

    /**
     * Checks if the player is on the ground.
     */
    public boolean isOnGround() {
        return onGround;
    }

    /**
     * Gets the current air time (frames).
     */
    public int getAirTime() {
        return airTime;
    }

    /**
     * Enables or disables debug drawing for the skeleton.
     */
    public void setDebugDraw(boolean enabled) {
        skeleton.setDebugDraw(enabled);
    }

    /**
     * Sets the collision box dimensions.
     */
    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Drops an item in front of the player.
     */
    public void dropItem(ItemEntity item) {
        int dropOffset = facingRight ? width + 20 : -20 - item.getBounds().width;
        item.x = x + dropOffset;
        item.y = y + (height / 2);
        if (audioManager != null) {
            audioManager.playSound("drop");
        }
    }

    // ==================== Mining Methods ====================

    /**
     * Attempts to mine layers from a block in the specified direction.
     * Mining speed depends on the held tool - effective tools mine multiple layers.
     * After 8 total layers from any direction, the block fully breaks.
     *
     * @param entities List of entities to check for blocks
     * @param direction BlockEntity.MINE_LEFT, MINE_RIGHT, MINE_UP, or MINE_DOWN
     */
    private void tryMineBlock(ArrayList<Entity> entities, int direction) {
        lastBrokenBlock = null;
        lastDroppedItem = null;

        // Calculate the mining area based on direction
        Rectangle mineArea = getMiningArea(direction);

        // Find the nearest block in the mining area
        BlockEntity targetBlock = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity e : entities) {
            if (e instanceof BlockEntity) {
                BlockEntity block = (BlockEntity) e;
                if (!block.isBroken()) {
                    // Use full bounds for mining detection (so we can mine partially damaged blocks)
                    if (mineArea.intersects(block.getFullBounds())) {
                        // Calculate distance to player center
                        double dist = getDistanceToBlock(block, direction);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            targetBlock = block;
                        }
                    }
                }
            }
        }

        // Mine layers from the block if found
        if (targetBlock != null) {
            // Get held tool and calculate layers to mine
            ToolType heldTool = inventory.getHeldToolType();
            int layersToMine = heldTool.getLayersPerMine(targetBlock.getBlockType());

            // Mine multiple layers based on tool effectiveness
            boolean fullyBroken = false;
            for (int i = 0; i < layersToMine && !fullyBroken; i++) {
                fullyBroken = targetBlock.mineLayer(direction);
            }

            // Play a mining sound (quieter than full break)
            if (audioManager != null) {
                audioManager.playSound("drop"); // Use drop as mining tick sound
            }

            // Show tool effectiveness in debug output
            String toolInfo = heldTool == ToolType.HAND ? "bare hands" : heldTool.getDisplayName();
            System.out.println("Player mined " + layersToMine + " layer(s) with " + toolInfo +
                             " from " + targetBlock.getBlockType().name() +
                             " direction=" + getDirectionName(direction) +
                             " damage=[L:" + targetBlock.getDamage(BlockEntity.MINE_LEFT) +
                             ",R:" + targetBlock.getDamage(BlockEntity.MINE_RIGHT) +
                             ",T:" + targetBlock.getDamage(BlockEntity.MINE_UP) +
                             ",B:" + targetBlock.getDamage(BlockEntity.MINE_DOWN) + "]");

            // If fully broken, trigger the full break with sound and item drop
            if (fullyBroken) {
                lastBrokenBlock = targetBlock;
                lastDroppedItem = targetBlock.breakBlock(audioManager);

                System.out.println("Block fully broken: " + targetBlock.getBlockType().name() +
                                 " at grid (" + targetBlock.getGridX() + "," + targetBlock.getGridY() + ")");
            }
        }
    }

    /**
     * Gets the mining area based on the current miningDirection (0-5).
     * 0=up, 1=right-upper, 2=right-lower, 3=down, 4=left-lower, 5=left-upper
     */
    private Rectangle getMiningArea(int damageDir) {
        int reach = BlockRegistry.BLOCK_SIZE;
        int playerCenterX = x + width / 2;

        switch (miningDirection) {
            case 0:  // Up - block above player
                return new Rectangle(playerCenterX - reach/2, y - reach, reach, reach);

            case 1:  // Right-upper - block to right at head level
                return new Rectangle(x + width, y - reach/2, reach, reach);

            case 2:  // Right-lower - block to right at feet level
                return new Rectangle(x + width, y + height - reach/2, reach, reach);

            case 3:  // Down - block below player
                return new Rectangle(playerCenterX - reach/2, y + height, reach, reach);

            case 4:  // Left-lower - block to left at feet level
                return new Rectangle(x - reach, y + height - reach/2, reach, reach);

            case 5:  // Left-upper - block to left at head level
                return new Rectangle(x - reach, y - reach/2, reach, reach);

            default:
                return new Rectangle(x, y, width, height);
        }
    }

    /**
     * Gets the damage direction (which side of block to damage) based on miningDirection.
     * 0=up, 1=right-upper, 2=right-lower, 3=down, 4=left-lower, 5=left-upper
     */
    private int getMiningDirection() {
        switch (miningDirection) {
            case 0:  // Up - damage block's bottom
                return BlockEntity.MINE_DOWN;
            case 1:  // Right-upper - damage block's left side
            case 2:  // Right-lower - damage block's left side
                return BlockEntity.MINE_LEFT;
            case 3:  // Down - damage block's top
                return BlockEntity.MINE_UP;
            case 4:  // Left-lower - damage block's right side
            case 5:  // Left-upper - damage block's right side
                return BlockEntity.MINE_RIGHT;
            default:
                return BlockEntity.MINE_LEFT;
        }
    }

    /**
     * Updates the currently targeted block based on mining direction.
     * This is used for crosshair display.
     */
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
    }

    /**
     * Calculates distance to a block for mining priority.
     * Uses true 2D distance to find block closest to player center.
     */
    private double getDistanceToBlock(BlockEntity block, int direction) {
        int playerCenterX = x + width / 2;
        int playerCenterY = y + height / 2;
        int blockCenterX = block.x + block.getSize() / 2;
        int blockCenterY = block.y + block.getSize() / 2;

        // Use 2D distance for all directions to find block closest to player center
        double dx = blockCenterX - playerCenterX;
        double dy = blockCenterY - playerCenterY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Gets a readable name for a mining direction.
     */
    private String getDirectionName(int direction) {
        switch (direction) {
            case BlockEntity.MINE_LEFT: return "LEFT";
            case BlockEntity.MINE_RIGHT: return "RIGHT";
            case BlockEntity.MINE_UP: return "UP";
            case BlockEntity.MINE_DOWN: return "DOWN";
            default: return "UNKNOWN";
        }
    }

    /**
     * Gets the last block broken by the player (if any).
     * Returns null if no block was broken since last check.
     * Clears after being retrieved.
     */
    public BlockEntity getLastBrokenBlock() {
        BlockEntity block = lastBrokenBlock;
        lastBrokenBlock = null;
        return block;
    }

    /**
     * Gets the item dropped from the last broken block (if any).
     * Returns null if no item was dropped.
     * Clears after being retrieved.
     */
    public ItemEntity getLastDroppedItem() {
        ItemEntity item = lastDroppedItem;
        lastDroppedItem = null;
        return item;
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
            return; // Still invincible
        }

        currentHealth -= damage;
        if (currentHealth < 0) currentHealth = 0;

        // Apply knockback
        damageKnockbackX = knockbackX;
        damageKnockbackY = knockbackY;
        velX += knockbackX;
        velY += knockbackY;

        // Start invincibility frames
        invincibilityTimer = invincibilityTime;

        // Play hurt sound
        if (audioManager != null) {
            audioManager.playSound("hurt");
        }

        System.out.println("Player took " + damage + " damage! Health: " + currentHealth + "/" + maxHealth);
    }
}
