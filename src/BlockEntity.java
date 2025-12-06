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
 * This class is designed for Minecraft-style block placement where
 * all blocks are uniform squares arranged on a grid.
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
        return new Rectangle(x, y, size, size);
    }

    @Override
    public void draw(Graphics g) {
        if (texture != null) {
            g.drawImage(texture, x, y, null);
        } else {
            // Fallback rendering
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, size, size);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, size - 1, size - 1);
        }
    }

    /**
     * Checks if this block is solid (blocks player movement).
     * @return true if the block is solid
     */
    public boolean isSolid() {
        return blockType.isSolid();
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

    @Override
    public String toString() {
        return "BlockEntity{" +
                "type=" + blockType.name() +
                ", grid=(" + gridX + "," + gridY + ")" +
                ", pixel=(" + x + "," + y + ")" +
                ", solid=" + isSolid() +
                "}";
    }
}
