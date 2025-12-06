import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

class PlayerEntity extends SpriteEntity {

    private double velY = 0;
    private final double gravity = 0.5;
    private final double jumpStrength = -10;

    private boolean onGround = false;
    private boolean facingRight = true; // Track direction player is facing
    private int airTime = 0; // Track how long we've been in the air

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

        // Directional mining:
        // E - mine horizontally (based on facing direction)
        // Q - mine upward
        // F - mine downward
        if (input.isKeyJustPressed('e')) {
            int direction = facingRight ? BlockEntity.MINE_RIGHT : BlockEntity.MINE_LEFT;
            tryMineBlock(entities, direction);
        }
        if (input.isKeyJustPressed('q')) {
            tryMineBlock(entities, BlockEntity.MINE_UP);
        }
        if (input.isKeyJustPressed('f')) {
            tryMineBlock(entities, BlockEntity.MINE_DOWN);
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
     * Attempts to mine one layer from a block in the specified direction.
     * Mining removes 2 base pixels (8 scaled pixels) per action.
     * After 8 mining actions from any direction, the block fully breaks.
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

        // Mine one layer from the block if found
        if (targetBlock != null) {
            boolean fullyBroken = targetBlock.mineLayer(direction);

            // Play a mining sound (quieter than full break)
            if (audioManager != null) {
                audioManager.playSound("drop"); // Use drop as mining tick sound
            }

            System.out.println("Player mined layer from " + targetBlock.getBlockType().name() +
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
     * Gets the mining area rectangle based on direction.
     */
    private Rectangle getMiningArea(int direction) {
        int reach = BlockRegistry.BLOCK_SIZE;

        switch (direction) {
            case BlockEntity.MINE_LEFT:
                // Check area to the left of player
                return new Rectangle(x - reach, y, reach, height);

            case BlockEntity.MINE_RIGHT:
                // Check area to the right of player
                return new Rectangle(x + width, y, reach, height);

            case BlockEntity.MINE_UP:
                // Check area above player
                return new Rectangle(x - reach/2, y - reach, width + reach, reach);

            case BlockEntity.MINE_DOWN:
                // Check area below player
                return new Rectangle(x - reach/2, y + height, width + reach, reach);

            default:
                return new Rectangle(x, y, width, height);
        }
    }

    /**
     * Calculates distance to a block for mining priority.
     */
    private double getDistanceToBlock(BlockEntity block, int direction) {
        int playerCenterX = x + width / 2;
        int playerCenterY = y + height / 2;
        int blockCenterX = block.x + block.getSize() / 2;
        int blockCenterY = block.y + block.getSize() / 2;

        // For horizontal mining, prioritize by X distance
        if (direction == BlockEntity.MINE_LEFT || direction == BlockEntity.MINE_RIGHT) {
            return Math.abs(blockCenterX - playerCenterX);
        }
        // For vertical mining, prioritize by Y distance
        return Math.abs(blockCenterY - playerCenterY);
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
}