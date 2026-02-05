package com.ambermoongame.entity.capabilities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.ambermoongame.entity.Entity;

import java.util.ArrayList;

/**
 * Interface for entities that can interact with blocks (mining and placement).
 * Provides a shared contract for block interactions that can be implemented by
 * players, companions, NPCs, or other entities that need to mine or place blocks.
 * Equivalent to entity/capabilities/BlockInteractionHandler.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.Graphics2D   -> android.graphics.Canvas
 * - java.awt.Rectangle    -> android.graphics.Rect
 * - java.awt.Color        -> android.graphics.Color / int
 * - java.awt.BasicStroke  -> Paint.setStrokeWidth()
 *
 * Dependencies not yet ported (methods commented out):
 * - BlockEntity, BlockType, BlockRegistry (block system)
 * - ToolType, Inventory (UI)
 * - AudioManager (audio - Android version exists but with different API)
 * - Camera (graphics)
 */
public interface BlockInteractionHandler {

    /** Default radius (in blocks) for mining operations */
    int DEFAULT_MINING_RADIUS = 3;

    /** Default radius (in blocks) for placement operations */
    int DEFAULT_PLACEMENT_RADIUS = 3;

    // ==================== Mining System ====================

    // --- Uncomment when BlockEntity is ported ---
    //
    // /**
    //  * Gets the currently selected block for mining.
    //  */
    // BlockEntity getSelectedBlock();
    //
    // /**
    //  * Selects a block for mining.
    //  */
    // void selectBlock(BlockEntity block);

    /**
     * Deselects the currently selected block.
     */
    void deselectBlock();

    /**
     * Gets the current mining direction.
     * @return Direction constant (0=up, 1=right, 2=down, 3=left)
     */
    int getMiningDirection();

    /**
     * Sets the mining direction.
     * @param direction Direction constant (0=up, 1=right, 2=down, 3=left)
     */
    void setMiningDirection(int direction);

    /**
     * Gets the mining radius in blocks.
     */
    default int getMiningRadius() {
        return DEFAULT_MINING_RADIUS;
    }

    // --- Uncomment when BlockEntity is ported ---
    //
    // /**
    //  * Checks if a block is within mining range of this entity.
    //  */
    // boolean isBlockInRange(BlockEntity block);
    //
    // /**
    //  * Validates that the selected block is still valid (exists, in range, not broken).
    //  */
    // void validateSelectedBlock();
    //
    // /**
    //  * Attempts to mine the currently selected block.
    //  */
    // boolean mineSelectedBlock(ArrayList<Entity> entities);
    //
    // /**
    //  * Handles a click on blocks - selecting or mining based on state.
    //  */
    // boolean handleBlockClick(ArrayList<Entity> entities, int worldX, int worldY);

    // ==================== Placement System ====================

    /**
     * Gets the placement radius in blocks.
     */
    default int getPlacementRadius() {
        return DEFAULT_PLACEMENT_RADIUS;
    }

    // --- Uncomment when BlockEntity, BlockRegistry are ported ---
    //
    // boolean canPlaceBlockAt(int worldX, int worldY, ArrayList<Entity> entities);
    // boolean tryPlaceBlock(ArrayList<Entity> entities, int worldX, int worldY);

    // ==================== Tool Support ====================

    // --- Uncomment when ToolType is ported ---
    //
    // /**
    //  * Gets the tool type currently being used for mining.
    //  */
    // ToolType getEquippedToolType();
    //
    // /**
    //  * Gets the number of layers to mine per action based on the equipped tool.
    //  */
    // default int getLayersPerMine(BlockType blockType) {
    //     return getEquippedToolType().getLayersPerMine(blockType);
    // }

    // ==================== Resource Access ====================

    // --- Uncomment when Inventory, Camera are ported ---
    //
    // Inventory getInventory();
    // Camera getCamera();

    // ==================== Position Access ====================

    /**
     * Gets the entity's center X position for range calculations.
     */
    int getCenterX();

    /**
     * Gets the entity's center Y position for range calculations.
     */
    int getCenterY();

    // ==================== Visual Feedback ====================

    /**
     * Draws the mining direction arrow on the selected block.
     */
    void drawMiningArrow(Canvas canvas, int centerX, int centerY, int direction);

    /**
     * Draws the selection highlight around the selected block.
     */
    default void drawSelectionHighlight(Canvas canvas, Rect blockBounds) {
        // Fill - yellow with low alpha
        Paint fillPaint = new Paint();
        fillPaint.setColor(Color.argb(80, 255, 255, 100));
        fillPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(blockBounds, fillPaint);

        // Border - yellow with high alpha
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.argb(200, 255, 255, 100));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        canvas.drawRect(blockBounds, borderPaint);
    }

    // --- Uncomment when BlockEntity, BlockRegistry are ported ---
    //
    // /**
    //  * Draws a placement preview at the specified grid position.
    //  */
    // default void drawPlacementPreview(Canvas canvas, int gridX, int gridY, boolean canPlace) {
    //     int pixelX = BlockEntity.gridToPixel(gridX);
    //     int pixelY = BlockEntity.gridToPixel(gridY);
    //     int size = BlockRegistry.BLOCK_SIZE;
    //
    //     int fillColor = canPlace ? Color.argb(60, 100, 255, 100) : Color.argb(60, 255, 100, 100);
    //     int borderColor = canPlace ? Color.argb(180, 100, 255, 100) : Color.argb(180, 255, 100, 100);
    //
    //     Paint fillPaint = new Paint();
    //     fillPaint.setColor(fillColor);
    //     fillPaint.setStyle(Paint.Style.FILL);
    //     canvas.drawRect(pixelX, pixelY, pixelX + size, pixelY + size, fillPaint);
    //
    //     Paint borderPaint = new Paint();
    //     borderPaint.setColor(borderColor);
    //     borderPaint.setStyle(Paint.Style.STROKE);
    //     borderPaint.setStrokeWidth(2);
    //     canvas.drawRect(pixelX, pixelY, pixelX + size, pixelY + size, borderPaint);
    // }
}
