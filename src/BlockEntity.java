import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Optimized entity class for block-based terrain and obstacles.
 *
 * Key optimizations for blocks:
 * - Square dimensions: Only one size value needed (width == height)
 * - Shared textures: Uses BlockRegistry for cached textures
 * - Grid alignment: Blocks snap to grid positions for consistent rendering
 * - Minimal per-instance data: Only position, type, and optional tint stored
 *
 * Layer-based damage system:
 * - Blocks can be mined from any direction (left, right, top, bottom)
 * - Each layer is 2 base pixels (8 scaled pixels) wide
 * - 8 layers per direction = 16 base pixels = 64 scaled pixels (full block)
 * - Collision bounds shrink as layers are removed
 * - Block fully breaks when any direction reaches 8 layers of damage
 */
public class BlockEntity extends Entity {

    private final BlockType blockType;
    private final int size;
    private BufferedImage texture;

    // Optional color tinting
    private boolean hasTint = false;
    private int tintRed = 255;
    private int tintGreen = 255;
    private int tintBlue = 255;

    // Grid position (in block units, not pixels)
    private int gridX;
    private int gridY;

    // Block state
    private boolean broken = false;
    private boolean targeted = false; // Whether this block is currently being targeted by player

    // Layer-based damage system
    // Each value represents layers removed (0-8), where each layer = 2 base pixels = 8 scaled pixels
    public static final int MAX_LAYERS = 8;
    public static final int LAYER_SIZE = BlockRegistry.BLOCK_SIZE / MAX_LAYERS; // 8 pixels per layer

    private int damageLeft = 0;   // Layers removed from left side
    private int damageRight = 0;  // Layers removed from right side
    private int damageTop = 0;    // Layers removed from top
    private int damageBottom = 0; // Layers removed from bottom

    // Mining direction constants
    public static final int MINE_LEFT = 0;
    public static final int MINE_RIGHT = 1;
    public static final int MINE_UP = 2;
    public static final int MINE_DOWN = 3;

    /**
     * Creates a new block entity at the specified pixel position.
     *
     * @param x Pixel X coordinate
     * @param y Pixel Y coordinate
     * @param blockType The type of block
     */
    public BlockEntity(int x, int y, BlockType blockType) {
        super(x, y);
        this.blockType = blockType;
        this.size = BlockRegistry.BLOCK_SIZE;
        this.texture = BlockRegistry.getInstance().getTexture(blockType);

        // Calculate grid position
        this.gridX = x / BlockRegistry.BLOCK_SIZE;
        this.gridY = y / BlockRegistry.BLOCK_SIZE;
    }

    /**
     * Creates a new block entity at a grid position.
     * Use this for grid-aligned block placement.
     *
     * @param gridX Grid X coordinate (in block units)
     * @param gridY Grid Y coordinate (in block units)
     * @param blockType The type of block
     * @param useGridCoords If true, x/y are treated as grid coordinates
     */
    public BlockEntity(int gridX, int gridY, BlockType blockType, boolean useGridCoords) {
        super(
            useGridCoords ? gridX * BlockRegistry.BLOCK_SIZE : gridX,
            useGridCoords ? gridY * BlockRegistry.BLOCK_SIZE : gridY
        );
        this.blockType = blockType;
        this.size = BlockRegistry.BLOCK_SIZE;
        this.texture = BlockRegistry.getInstance().getTexture(blockType);

        if (useGridCoords) {
            this.gridX = gridX;
            this.gridY = gridY;
        } else {
            this.gridX = gridX / BlockRegistry.BLOCK_SIZE;
            this.gridY = gridY / BlockRegistry.BLOCK_SIZE;
        }
    }

    @Override
    public Rectangle getBounds() {
        // Calculate bounds accounting for layer damage
        int leftOffset = damageLeft * LAYER_SIZE;
        int topOffset = damageTop * LAYER_SIZE;
        int rightReduction = damageRight * LAYER_SIZE;
        int bottomReduction = damageBottom * LAYER_SIZE;

        int newX = x + leftOffset;
        int newY = y + topOffset;
        int newWidth = size - leftOffset - rightReduction;
        int newHeight = size - topOffset - bottomReduction;

        // Ensure minimum size of 1 pixel
        newWidth = Math.max(1, newWidth);
        newHeight = Math.max(1, newHeight);

        return new Rectangle(newX, newY, newWidth, newHeight);
    }

    /**
     * Gets the full original bounds (ignoring damage).
     * Useful for determining block position for mining checks.
     */
    public Rectangle getFullBounds() {
        return new Rectangle(x, y, size, size);
    }

    @Override
    public void draw(Graphics g) {
        if (broken) return; // Don't draw broken blocks

        Graphics2D g2d = (Graphics2D) g;

        // Calculate the visible portion of the block
        int leftOffset = damageLeft * LAYER_SIZE;
        int topOffset = damageTop * LAYER_SIZE;
        int rightReduction = damageRight * LAYER_SIZE;
        int bottomReduction = damageBottom * LAYER_SIZE;

        int visibleWidth = size - leftOffset - rightReduction;
        int visibleHeight = size - topOffset - bottomReduction;

        if (visibleWidth <= 0 || visibleHeight <= 0) return;

        // Draw the visible portion of the texture
        if (texture != null) {
            // Source rectangle (from texture)
            int srcX = leftOffset;
            int srcY = topOffset;
            int srcWidth = visibleWidth;
            int srcHeight = visibleHeight;

            // Destination rectangle (on screen)
            int destX = x + leftOffset;
            int destY = y + topOffset;

            g2d.drawImage(texture,
                destX, destY, destX + visibleWidth, destY + visibleHeight,  // destination
                srcX, srcY, srcX + srcWidth, srcY + srcHeight,              // source
                null);
        } else {
            // Fallback rendering
            g.setColor(Color.MAGENTA);
            g.fillRect(x + leftOffset, y + topOffset, visibleWidth, visibleHeight);
            g.setColor(Color.BLACK);
            g.drawRect(x + leftOffset, y + topOffset, visibleWidth - 1, visibleHeight - 1);
        }

        // Draw damage indicators only when block is currently targeted
        // Red overlay shows mining progress only on the actively targeted block
        if (hasDamage() && targeted) {
            g2d.setColor(new Color(255, 0, 0, 50));
            // Show mined areas
            if (damageLeft > 0) {
                g2d.fillRect(x, y, damageLeft * LAYER_SIZE, size);
            }
            if (damageRight > 0) {
                g2d.fillRect(x + size - damageRight * LAYER_SIZE, y, damageRight * LAYER_SIZE, size);
            }
            if (damageTop > 0) {
                g2d.fillRect(x, y, size, damageTop * LAYER_SIZE);
            }
            if (damageBottom > 0) {
                g2d.fillRect(x, y + size - damageBottom * LAYER_SIZE, size, damageBottom * LAYER_SIZE);
            }
        }

        // Reset targeted state each frame - must be set again by player
        targeted = false;
    }

    /**
     * Mines one layer from the specified direction.
     * Returns true if the block was fully broken by this mining action.
     *
     * @param direction MINE_LEFT, MINE_RIGHT, MINE_UP, or MINE_DOWN
     * @return true if block is now fully broken
     */
    public boolean mineLayer(int direction) {
        if (broken) return false;

        switch (direction) {
            case MINE_LEFT:
                damageLeft = Math.min(MAX_LAYERS, damageLeft + 1);
                break;
            case MINE_RIGHT:
                damageRight = Math.min(MAX_LAYERS, damageRight + 1);
                break;
            case MINE_UP:
                damageTop = Math.min(MAX_LAYERS, damageTop + 1);
                break;
            case MINE_DOWN:
                damageBottom = Math.min(MAX_LAYERS, damageBottom + 1);
                break;
        }

        // Check if block is fully broken (any direction reached max)
        if (damageLeft >= MAX_LAYERS || damageRight >= MAX_LAYERS ||
            damageTop >= MAX_LAYERS || damageBottom >= MAX_LAYERS) {
            return true;
        }

        // Also check if horizontal or vertical damage combined breaks through
        if (damageLeft + damageRight >= MAX_LAYERS ||
            damageTop + damageBottom >= MAX_LAYERS) {
            return true;
        }

        return false;
    }

    /**
     * Gets the current damage for a direction.
     */
    public int getDamage(int direction) {
        switch (direction) {
            case MINE_LEFT: return damageLeft;
            case MINE_RIGHT: return damageRight;
            case MINE_UP: return damageTop;
            case MINE_DOWN: return damageBottom;
            default: return 0;
        }
    }

    /**
     * Checks if this block has any damage.
     */
    public boolean hasDamage() {
        return damageLeft > 0 || damageRight > 0 || damageTop > 0 || damageBottom > 0;
    }

    /**
     * Sets whether this block is currently targeted by the player.
     * Targeted blocks show their damage overlay (red highlight).
     * This state resets each frame and must be set again by the player.
     */
    public void setTargeted(boolean targeted) {
        this.targeted = targeted;
    }

    /**
     * Checks if this block is currently targeted.
     */
    public boolean isTargeted() {
        return targeted;
    }

    /**
     * Gets total damage layers (for display/debug purposes).
     */
    public int getTotalDamage() {
        return damageLeft + damageRight + damageTop + damageBottom;
    }

    /**
     * Gets the remaining width of the block after damage.
     */
    public int getRemainingWidth() {
        return size - (damageLeft + damageRight) * LAYER_SIZE;
    }

    /**
     * Gets the remaining height of the block after damage.
     */
    public int getRemainingHeight() {
        return size - (damageTop + damageBottom) * LAYER_SIZE;
    }

    /**
     * Checks if this block is solid (blocks player movement).
     * A broken block is never solid, even if its type normally is.
     * @return true if the block is solid and not broken
     */
    public boolean isSolid() {
        return !broken && blockType.isSolid();
    }

    /**
     * Gets the block type.
     * @return The BlockType enum value
     */
    public BlockType getBlockType() {
        return blockType;
    }

    /**
     * Gets the block size (width and height are equal).
     * @return Block size in pixels
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the grid X coordinate.
     * @return Grid X position in block units
     */
    public int getGridX() {
        return gridX;
    }

    /**
     * Gets the grid Y coordinate.
     * @return Grid Y position in block units
     */
    public int getGridY() {
        return gridY;
    }

    /**
     * Applies a color tint to this block.
     * The tinted texture is cached by BlockRegistry for efficiency.
     *
     * @param red Red component (0-255)
     * @param green Green component (0-255)
     * @param blue Blue component (0-255)
     */
    public void setTint(int red, int green, int blue) {
        this.tintRed = Math.max(0, Math.min(255, red));
        this.tintGreen = Math.max(0, Math.min(255, green));
        this.tintBlue = Math.max(0, Math.min(255, blue));
        this.hasTint = true;

        // Get tinted texture from registry (cached)
        this.texture = BlockRegistry.getInstance().getTintedTexture(
            blockType, tintRed, tintGreen, tintBlue
        );
    }

    /**
     * Removes the color tint, restoring original texture.
     */
    public void clearTint() {
        this.hasTint = false;
        this.tintRed = 255;
        this.tintGreen = 255;
        this.tintBlue = 255;
        this.texture = BlockRegistry.getInstance().getTexture(blockType);
    }

    /**
     * Checks if this block has a color tint applied.
     * @return true if tinted
     */
    public boolean hasTint() {
        return hasTint;
    }

    /**
     * Gets the current tint color.
     * @return int array with [red, green, blue] values
     */
    public int[] getTint() {
        return new int[] { tintRed, tintGreen, tintBlue };
    }

    /**
     * Moves this block to a new grid position.
     *
     * @param newGridX New grid X coordinate
     * @param newGridY New grid Y coordinate
     */
    public void setGridPosition(int newGridX, int newGridY) {
        this.gridX = newGridX;
        this.gridY = newGridY;
        this.x = newGridX * BlockRegistry.BLOCK_SIZE;
        this.y = newGridY * BlockRegistry.BLOCK_SIZE;
    }

    /**
     * Converts a pixel position to grid coordinates.
     *
     * @param pixelPos Pixel coordinate
     * @return Grid coordinate
     */
    public static int pixelToGrid(int pixelPos) {
        return pixelPos / BlockRegistry.BLOCK_SIZE;
    }

    /**
     * Converts a grid position to pixel coordinates.
     *
     * @param gridPos Grid coordinate
     * @return Pixel coordinate
     */
    public static int gridToPixel(int gridPos) {
        return gridPos * BlockRegistry.BLOCK_SIZE;
    }

    /**
     * Snaps a pixel coordinate to the nearest grid position.
     *
     * @param pixelPos Pixel coordinate
     * @return Snapped pixel coordinate (aligned to grid)
     */
    public static int snapToGrid(int pixelPos) {
        return (pixelPos / BlockRegistry.BLOCK_SIZE) * BlockRegistry.BLOCK_SIZE;
    }

    /**
     * Gets the attributes for this block type (sounds, drops, etc.).
     * @return BlockAttributes for this block's type
     */
    public BlockAttributes getAttributes() {
        return BlockAttributes.get(blockType);
    }

    /**
     * Checks if this block has been broken.
     * @return true if broken
     */
    public boolean isBroken() {
        return broken;
    }

    /**
     * Breaks this block and returns the item drop (if any).
     * Call this when the player breaks a block.
     *
     * @param audioManager AudioManager to play break sound (can be null)
     * @return ItemEntity to spawn, or null if no drop
     */
    public ItemEntity breakBlock(AudioManager audioManager) {
        if (broken) {
            return null; // Already broken
        }

        broken = true;
        BlockAttributes attrs = getAttributes();

        // Play break sound
        if (audioManager != null && attrs.getBreakSound() != null) {
            audioManager.playSound(attrs.getBreakSound());
        }

        // Create item drop if applicable
        if (attrs.shouldDrop()) {
            // Drop item at block center
            int dropX = x + size / 4;
            int dropY = y + size / 4;

            ItemEntity drop = new ItemEntity(
                dropX, dropY,
                attrs.getDropSpritePath(),
                attrs.getDropItemName(),
                attrs.getDropItemType()
            );

            System.out.println("Block " + blockType.name() + " broken at (" + gridX + "," + gridY +
                             ") - dropped " + attrs.getDropItemName());
            return drop;
        }

        System.out.println("Block " + blockType.name() + " broken at (" + gridX + "," + gridY + ") - no drop");
        return null;
    }

    /**
     * Called when this block is placed. Plays placement sound.
     *
     * @param audioManager AudioManager to play place sound (can be null)
     */
    public void onPlace(AudioManager audioManager) {
        BlockAttributes attrs = getAttributes();
        if (audioManager != null && attrs.getPlaceSound() != null) {
            audioManager.playSound(attrs.getPlaceSound());
        }
    }

    /**
     * Gets the break sound path for this block.
     * @return Sound file path or null
     */
    public String getBreakSound() {
        return getAttributes().getBreakSound();
    }

    /**
     * Gets the place sound path for this block.
     * @return Sound file path or null
     */
    public String getPlaceSound() {
        return getAttributes().getPlaceSound();
    }

    /**
     * Gets the step sound path for this block.
     * @return Sound file path or null
     */
    public String getStepSound() {
        return getAttributes().getStepSound();
    }

    @Override
    public String toString() {
        return "BlockEntity{" +
                "type=" + blockType.name() +
                ", grid=(" + gridX + "," + gridY + ")" +
                ", pixel=(" + x + "," + y + ")" +
                ", solid=" + isSolid() +
                ", broken=" + broken +
                ", damage=[L:" + damageLeft + ",R:" + damageRight +
                ",T:" + damageTop + ",B:" + damageBottom + "]" +
                "}";
    }
}
