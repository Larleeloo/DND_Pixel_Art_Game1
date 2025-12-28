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
import javax.swing.*;

public class PlayerEntity extends SpriteEntity implements PlayerBase {

    private double velY = 0;
    private final double gravity = 0.5;
    private final double jumpStrength = -10;

    private boolean onGround = false;
    private boolean facingRight = true; // Track direction player is facing
    private int airTime = 0; // Track how long we've been in the air

    // Health system
    private int maxHealth = 100;
    private int currentHealth = 100;
    private double invincibilityTime = 1.0;
    private double invincibilityTimer = 0;

    private Inventory inventory;
    private AudioManager audioManager; // Reference to audio manager

    // Jump animation (optional GIF)
    private ImageIcon jumpAnimatedIcon;
    private Image jumpSpriteImage;
    private int jumpWidth, jumpHeight;

    // Ground height - can be set per level
    private int groundY = 720; // Default to GamePanel.GROUND_Y

    // Block interaction
    private BlockEntity lastBrokenBlock = null;
    private ItemEntity lastDroppedItem = null;

    // Mining targeting system - 8 positions around player (clockwise from top)
    // 0=up, 1=right-upper, 2=right-lower, 3=down, 4=left-lower, 5=left-upper
    private int miningDirection = 1; // Start facing right-upper
    private static final int NUM_DIRECTIONS = 6;
    private BlockEntity targetedBlock = null; // Currently targeted block for crosshair

    public PlayerEntity(int x, int y, String spritePath) {
        super(x, y, spritePath, false);

        // Initialize inventory with 10 slots
        this.inventory = new Inventory(10);
        this.audioManager = null; // Will be set later

        // Optional jump animation
        tryLoadJumpSprite(spritePath.replace(".png", "_jump.gif"));

        System.out.println("Player created at: x=" + x + " y=" + y + " width=" + width + " height=" + height);
        System.out.println("Ground level set at: " + groundY);
    }

    /**
     * Sets the ground Y level for this player.
     * @param groundY The Y coordinate of the ground
     */
    public void setGroundY(int groundY) {
        this.groundY = groundY;
        System.out.println("Player ground level updated to: " + groundY);
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    private void tryLoadJumpSprite(String path) {
        try {
            AssetLoader.ImageAsset jumpAsset = AssetLoader.load(path);

            // Store both animated and static versions
            jumpAnimatedIcon = jumpAsset.animatedIcon;
            jumpSpriteImage = jumpAsset.staticImage;
            jumpWidth = jumpAsset.width * SCALE;
            jumpHeight = jumpAsset.height * SCALE;

            System.out.println("Loaded jump sprite: " + path +
                    " (animated=" + (jumpAnimatedIcon != null) + ")");
        } catch (Exception e) {
            System.out.println("Jump animation not found: " + path + " - " + e.getMessage());
        }
    }

    public void update(InputManager input, ArrayList<Entity> entities) {
        int speed = 4;
        int newX = x;
        int newY = y;

        // Toggle inventory with 'I' key - use justPressed to prevent rapid toggling
        if (input.isKeyJustPressed('i')) {
            inventory.toggleOpen();
        }

        // Number keys 1-5 to select hotbar slots
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

        // E key or Left Mouse Click - mine the currently targeted block
        if (input.isKeyJustPressed('e') || input.isLeftMouseJustPressed()) {
            int direction = getMiningDirection();
            tryMineBlock(entities, direction);
        }

        // Horizontal movement
        if (input.isKeyPressed('a')) {
            newX -= speed;
            facingRight = false;
        }
        if (input.isKeyPressed('d')) {
            newX += speed;
            facingRight = true;
        }

        // Check horizontal collision with obstacles (SpriteEntity and BlockEntity)
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

        // Only apply horizontal movement if no collision
        if (!xCollision) {
            x = newX;
        }

        // Check for item collection
        Rectangle playerBounds = new Rectangle(x, y, width, height);
        for (Entity e : entities) {
            if (e instanceof ItemEntity) {
                ItemEntity item = (ItemEntity) e;
                if (!item.isCollected() && playerBounds.intersects(e.getBounds())) {
                    System.out.println("Collecting item: " + item.getItemName() + " at position (" + item.x + ", " + item.y + ")");
                    item.collect();
                    boolean added = inventory.addItem(item);
                    System.out.println("Item added to inventory: " + added);

                    // Play collect sound
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

            // Play jump sound
            if (audioManager != null) {
                audioManager.playSound("jump");
            }
        }

        // Apply gravity
        velY += gravity;

        // Calculate new Y position
        newY = y + (int)velY;

        // Check vertical collision with obstacles (SpriteEntity and BlockEntity)
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
                        // Falling down - land on top of platform
                        newY = platformBounds.y - height;
                        velY = 0;
                        onGround = true;
                        foundPlatform = true;
                    } else if (velY < 0) {
                        // Jumping up - hit head on bottom of platform
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
            // Only in air if not on platform and not on ground
            onGround = false;
        }

        // Apply vertical movement
        y = newY;

        // Track air time for animation stability
        if (onGround) {
            airTime = 0;
        } else {
            airTime++;
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Determine what to draw - only use jump animation if been in air for at least 3 frames
        boolean useJumpAnimation = (airTime > 3) && (jumpAnimatedIcon != null || jumpSpriteImage != null);

        if (useJumpAnimation) {
            // Draw jump animation
            if (jumpAnimatedIcon != null) {
                // Use animated GIF - scale it properly
                Image jumpImage = jumpAnimatedIcon.getImage();
                g.drawImage(jumpImage, x, y, jumpWidth, jumpHeight, null);
            } else if (jumpSpriteImage != null) {
                // Fallback to static jump image
                g.drawImage(jumpSpriteImage, x, y, jumpWidth, jumpHeight, null);
            }
        } else {
            // Draw normal sprite
            if (sprite != null) {
                g.drawImage(sprite, x, y, width, height, null);
            } else {
                // Fallback placeholder
                g.setColor(Color.YELLOW);
                g.fillRect(x, y, width, height);
            }
        }

        // Debug: draw bounds - use airTime for stable coloring
        g.setColor((airTime > 3) ? Color.ORANGE : Color.RED);
        g2d.setStroke(new BasicStroke(2));
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

        // Note: Inventory is now drawn in GameScene.drawUI() to stay fixed on screen
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void dropItem(ItemEntity item) {
        // Drop item in front of player based on direction they're facing
        int dropOffset = facingRight ? width + 20 : -20 - item.getBounds().width;
        item.x = x + dropOffset;
        item.y = y + (height / 2); // Drop at player's mid-height
        System.out.println("Dropped item: " + item.getItemName() + " at (" + item.x + ", " + item.y + ")");

        // Play drop sound
        if (audioManager != null) {
            audioManager.playSound("drop");
        }
    }

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

        // Mark the targeted block so it shows its damage overlay
        if (targetedBlock != null) {
            targetedBlock.setTargeted(true);
        }
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

    /**
     * Check if the player is facing right.
     */
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

        System.out.println("Player took " + damage + " damage! Health: " + currentHealth + "/" + maxHealth);
    }

    @Override
    public int getMana() {
        return 100; // Default full mana for basic player
    }

    @Override
    public int getMaxMana() {
        return 100;
    }

    @Override
    public int getStamina() {
        return 100; // Default full stamina for basic player
    }

    @Override
    public int getMaxStamina() {
        return 100;
    }

    /**
     * Applies an external push force to the player (from collisions).
     * @param pushX Horizontal push force
     * @param pushY Vertical push force
     */
    public void applyPush(double pushX, double pushY) {
        x += (int) pushX;
        velY += pushY;
    }
}