package com.ambermoongame.block;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.ambermoongame.entity.Entity;

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
 *
 * Equivalent to block/BlockEntity.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.Graphics/Graphics2D -> android.graphics.Canvas
 * - java.awt.Rectangle           -> android.graphics.Rect
 * - java.awt.Color               -> android.graphics.Color / int
 * - java.awt.image.BufferedImage -> android.graphics.Bitmap
 * - g2d.drawImage(src, dx1,dy1,dx2,dy2, sx1,sy1,sx2,sy2, null)
 *   -> canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
 *
 * Dependencies not yet ported (methods commented out):
 * - ItemEntity (entity/item/) - breakBlock return type
 * - AudioManager - breakBlock/onPlace sound playing
 */
public class BlockEntity extends Entity {

    private static final String TAG = "BlockEntity";

    private final BlockType blockType;
    private final int size;
    private Bitmap texture;

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
    private boolean targeted = false;

    // Overlay system
    private BlockOverlay overlay = BlockOverlay.NONE;
    private Bitmap overlayTexture = null;
    private int overlayDamage = 0;

    // Layer-based damage system
    public static final int MAX_LAYERS = 8;
    public static final int LAYER_SIZE = BlockRegistry.BLOCK_SIZE / MAX_LAYERS; // 8 pixels per layer

    private int damageLeft = 0;
    private int damageRight = 0;
    private int damageTop = 0;
    private int damageBottom = 0;

    // Mining direction constants
    public static final int MINE_LEFT = 0;
    public static final int MINE_RIGHT = 1;
    public static final int MINE_UP = 2;
    public static final int MINE_DOWN = 3;

    // Reusable drawing objects
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();
    private final Paint fallbackPaint = new Paint();
    private final Paint fallbackBorderPaint = new Paint();
    private final Paint damagePaint = new Paint();

    /**
     * Creates a new block entity at the specified pixel position.
     */
    public BlockEntity(int x, int y, BlockType blockType) {
        super(x, y);
        this.blockType = blockType;
        this.size = BlockRegistry.BLOCK_SIZE;
        this.texture = BlockRegistry.getInstance().getTexture(blockType);

        this.gridX = x / BlockRegistry.BLOCK_SIZE;
        this.gridY = y / BlockRegistry.BLOCK_SIZE;

        initPaints();
    }

    /**
     * Creates a new block entity at a grid position.
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

        initPaints();
    }

    private void initPaints() {
        fallbackPaint.setColor(Color.MAGENTA);
        fallbackPaint.setStyle(Paint.Style.FILL);

        fallbackBorderPaint.setColor(Color.BLACK);
        fallbackBorderPaint.setStyle(Paint.Style.STROKE);
        fallbackBorderPaint.setStrokeWidth(1);

        damagePaint.setColor(Color.argb(50, 255, 0, 0));
        damagePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public Rect getBounds() {
        int leftOffset = damageLeft * LAYER_SIZE;
        int topOffset = damageTop * LAYER_SIZE;
        int rightReduction = damageRight * LAYER_SIZE;
        int bottomReduction = damageBottom * LAYER_SIZE;

        int newX = x + leftOffset;
        int newY = y + topOffset;
        int newWidth = Math.max(1, size - leftOffset - rightReduction);
        int newHeight = Math.max(1, size - topOffset - bottomReduction);

        return new Rect(newX, newY, newX + newWidth, newY + newHeight);
    }

    /**
     * Gets the full original bounds (ignoring damage).
     */
    public Rect getFullBounds() {
        return new Rect(x, y, x + size, y + size);
    }

    @Override
    public void draw(Canvas canvas) {
        if (broken) return;

        int leftOffset = damageLeft * LAYER_SIZE;
        int topOffset = damageTop * LAYER_SIZE;
        int rightReduction = damageRight * LAYER_SIZE;
        int bottomReduction = damageBottom * LAYER_SIZE;

        int visibleWidth = size - leftOffset - rightReduction;
        int visibleHeight = size - topOffset - bottomReduction;

        if (visibleWidth <= 0 || visibleHeight <= 0) return;

        if (texture != null && !texture.isRecycled()) {
            // Source rectangle (from texture)
            srcRect.set(leftOffset, topOffset, leftOffset + visibleWidth, topOffset + visibleHeight);
            // Destination rectangle (on screen)
            int destX = x + leftOffset;
            int destY = y + topOffset;
            dstRect.set(destX, destY, destX + visibleWidth, destY + visibleHeight);

            canvas.drawBitmap(texture, srcRect, dstRect, null);

            // Draw overlay on top of base texture
            if (overlay != BlockOverlay.NONE && overlayTexture != null && !overlayTexture.isRecycled()) {
                canvas.drawBitmap(overlayTexture, srcRect, dstRect, null);
            }
        } else {
            // Fallback rendering
            int destX = x + leftOffset;
            int destY = y + topOffset;
            canvas.drawRect(destX, destY, destX + visibleWidth, destY + visibleHeight, fallbackPaint);
            canvas.drawRect(destX, destY, destX + visibleWidth, destY + visibleHeight, fallbackBorderPaint);
        }

        // Draw damage indicators only when targeted
        if (hasDamage() && targeted) {
            if (damageLeft > 0) {
                canvas.drawRect(x, y, x + damageLeft * LAYER_SIZE, y + size, damagePaint);
            }
            if (damageRight > 0) {
                canvas.drawRect(x + size - damageRight * LAYER_SIZE, y, x + size, y + size, damagePaint);
            }
            if (damageTop > 0) {
                canvas.drawRect(x, y, x + size, y + damageTop * LAYER_SIZE, damagePaint);
            }
            if (damageBottom > 0) {
                canvas.drawRect(x, y + size - damageBottom * LAYER_SIZE, x + size, y + size, damagePaint);
            }
        }

        // Reset targeted state each frame
        targeted = false;
    }

    /**
     * Mines one layer from the specified direction.
     * If block has an overlay that blocks base mining, overlay is damaged first.
     * @return true if the block was fully broken by this mining action
     */
    public boolean mineLayer(int direction) {
        if (broken) return false;

        // If overlay blocks base mining, damage overlay first
        if (overlay != BlockOverlay.NONE && overlay.blocksBaseMining()) {
            overlayDamage++;
            if (overlayDamage >= overlay.getBreakSteps()) {
                Log.d(TAG, "Overlay " + overlay.getDisplayName() + " removed from block at ("
                        + gridX + "," + gridY + ")");
                overlay = BlockOverlay.NONE;
                overlayTexture = null;
                overlayDamage = 0;
            }
            return false;
        }

        switch (direction) {
            case MINE_LEFT:   damageLeft = Math.min(MAX_LAYERS, damageLeft + 1); break;
            case MINE_RIGHT:  damageRight = Math.min(MAX_LAYERS, damageRight + 1); break;
            case MINE_UP:     damageTop = Math.min(MAX_LAYERS, damageTop + 1); break;
            case MINE_DOWN:   damageBottom = Math.min(MAX_LAYERS, damageBottom + 1); break;
        }

        // Check if block is fully broken
        if (damageLeft >= MAX_LAYERS || damageRight >= MAX_LAYERS ||
            damageTop >= MAX_LAYERS || damageBottom >= MAX_LAYERS) {
            return true;
        }
        if (damageLeft + damageRight >= MAX_LAYERS ||
            damageTop + damageBottom >= MAX_LAYERS) {
            return true;
        }
        return false;
    }

    // --- Uncomment when ItemEntity and AudioManager are ported ---
    //
    // /**
    //  * Breaks this block and returns the item drop (if any).
    //  */
    // public ItemEntity breakBlock(AudioManager audioManager) {
    //     if (broken) return null;
    //     broken = true;
    //     BlockAttributes attrs = getAttributes();
    //     if (audioManager != null && attrs.getBreakSound() != null) {
    //         audioManager.playSound(attrs.getBreakSound());
    //     }
    //     if (attrs.shouldDrop()) {
    //         int dropX = x + size / 4;
    //         int dropY = y + size / 4;
    //         return new ItemEntity(dropX, dropY, attrs.getDropSpritePath(),
    //                               attrs.getDropItemName(), attrs.getDropItemType());
    //     }
    //     return null;
    // }

    /**
     * Breaks this block without item drops or sound.
     * Use when ItemEntity/AudioManager are not yet available.
     */
    public void breakBlock() {
        broken = true;
        Log.d(TAG, "Block " + blockType.name() + " broken at (" + gridX + "," + gridY + ")");
    }

    // ==================== Getters / Setters ====================

    public int getDamage(int direction) {
        switch (direction) {
            case MINE_LEFT: return damageLeft;
            case MINE_RIGHT: return damageRight;
            case MINE_UP: return damageTop;
            case MINE_DOWN: return damageBottom;
            default: return 0;
        }
    }

    public boolean hasDamage() {
        return damageLeft > 0 || damageRight > 0 || damageTop > 0 || damageBottom > 0;
    }

    public void setTargeted(boolean targeted) { this.targeted = targeted; }
    public boolean isTargeted() { return targeted; }

    public int getTotalDamage() { return damageLeft + damageRight + damageTop + damageBottom; }
    public int getRemainingWidth() { return size - (damageLeft + damageRight) * LAYER_SIZE; }
    public int getRemainingHeight() { return size - (damageTop + damageBottom) * LAYER_SIZE; }

    public boolean isSolid() { return !broken && blockType.isSolid(); }
    public BlockType getBlockType() { return blockType; }
    public int getSize() { return size; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public boolean isBroken() { return broken; }
    public BlockAttributes getAttributes() { return BlockAttributes.get(blockType); }

    public String getBreakSound() { return getAttributes().getBreakSound(); }
    public String getPlaceSound() { return getAttributes().getPlaceSound(); }
    public String getStepSound() { return getAttributes().getStepSound(); }

    // ==================== Tinting ====================

    public void setTint(int red, int green, int blue) {
        this.tintRed = Math.max(0, Math.min(255, red));
        this.tintGreen = Math.max(0, Math.min(255, green));
        this.tintBlue = Math.max(0, Math.min(255, blue));
        this.hasTint = true;
        this.texture = BlockRegistry.getInstance().getTintedTexture(blockType, tintRed, tintGreen, tintBlue);
    }

    public void clearTint() {
        this.hasTint = false;
        this.tintRed = 255;
        this.tintGreen = 255;
        this.tintBlue = 255;
        this.texture = BlockRegistry.getInstance().getTexture(blockType);
    }

    public boolean hasTint() { return hasTint; }
    public int[] getTint() { return new int[] { tintRed, tintGreen, tintBlue }; }

    // ==================== Overlay ====================

    public void setOverlay(BlockOverlay overlay) {
        this.overlay = overlay != null ? overlay : BlockOverlay.NONE;
        this.overlayDamage = 0;

        if (this.overlay != BlockOverlay.NONE) {
            this.overlayTexture = BlockRegistry.getInstance().getOverlayTexture(this.overlay);
            if (this.overlayTexture == null) {
                this.overlayTexture = this.overlay.generateTexture(size);
            }
        } else {
            this.overlayTexture = null;
        }
    }

    public BlockOverlay getOverlay() { return overlay; }
    public boolean hasOverlay() { return overlay != BlockOverlay.NONE; }

    public void removeOverlay() {
        this.overlay = BlockOverlay.NONE;
        this.overlayTexture = null;
        this.overlayDamage = 0;
    }

    public int getOverlayDamage() { return overlayDamage; }
    public boolean isOverlayBlockingMining() { return overlay != BlockOverlay.NONE && overlay.blocksBaseMining(); }

    // ==================== Grid Utilities ====================

    public void setGridPosition(int newGridX, int newGridY) {
        this.gridX = newGridX;
        this.gridY = newGridY;
        this.x = newGridX * BlockRegistry.BLOCK_SIZE;
        this.y = newGridY * BlockRegistry.BLOCK_SIZE;
    }

    public static int pixelToGrid(int pixelPos) { return pixelPos / BlockRegistry.BLOCK_SIZE; }
    public static int gridToPixel(int gridPos) { return gridPos * BlockRegistry.BLOCK_SIZE; }
    public static int snapToGrid(int pixelPos) { return (pixelPos / BlockRegistry.BLOCK_SIZE) * BlockRegistry.BLOCK_SIZE; }

    @Override
    public String toString() {
        return "BlockEntity{type=" + blockType.name()
                + ", grid=(" + gridX + "," + gridY + ")"
                + ", pixel=(" + x + "," + y + ")"
                + ", solid=" + isSolid()
                + ", broken=" + broken
                + ", damage=[L:" + damageLeft + ",R:" + damageRight
                + ",T:" + damageTop + ",B:" + damageBottom + "]"
                + "}";
    }
}
