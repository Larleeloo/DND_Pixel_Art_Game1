package com.ambermoongame.entity.capabilities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.ambermoongame.entity.Entity;

import java.util.ArrayList;

/**
 * Helper class that provides default implementations for block interaction.
 * Can be used as a delegate by any entity implementing BlockInteractionHandler.
 * Equivalent to entity/capabilities/BlockInteractionHelper.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.Graphics2D   -> android.graphics.Canvas
 * - java.awt.Rectangle    -> android.graphics.Rect
 * - java.awt.Color        -> android.graphics.Color / int
 * - java.awt.BasicStroke  -> Paint.setStrokeWidth()
 * - java.awt.Stroke       -> (not needed, Paint handles stroke state)
 * - g2d.fillPolygon()     -> Canvas.drawPath() with Path
 * - g2d.fillOval()        -> Canvas.drawCircle()
 *
 * Dependencies not yet ported (methods commented out):
 * - BlockEntity, BlockType, BlockRegistry (block system)
 * - Item, ItemEntity (item system)
 * - ToolType, Inventory (UI)
 * - AudioManager (audio)
 */
public class BlockInteractionHelper {

    // Mining state
    // private BlockEntity selectedBlock = null;  // Uncomment when BlockEntity is ported
    private int miningDirection = 2;  // 0=up, 1=right, 2=down, 3=left
    private int miningRadius = BlockInteractionHandler.DEFAULT_MINING_RADIUS;
    private int placementRadius = BlockInteractionHandler.DEFAULT_PLACEMENT_RADIUS;

    // Reusable Paint objects
    private final Paint shadowPaint = new Paint();
    private final Paint fillPaint = new Paint();
    private final Paint outlinePaint = new Paint();
    private final Paint dotPaint = new Paint();

    public BlockInteractionHelper() {
        shadowPaint.setColor(Color.argb(150, 0, 0, 0));
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setAntiAlias(true);

        fillPaint.setColor(Color.argb(255, 255, 220, 100));
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);

        outlinePaint.setColor(Color.argb(255, 200, 150, 50));
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(2);
        outlinePaint.setAntiAlias(true);

        dotPaint.setAntiAlias(true);
    }

    // --- Uncomment when BlockEntity is ported ---
    //
    // public BlockEntity getSelectedBlock() {
    //     return selectedBlock;
    // }
    //
    // public void selectBlock(BlockEntity block) {
    //     if (selectedBlock != null && selectedBlock != block) {
    //         selectedBlock.setTargeted(false);
    //     }
    //     selectedBlock = block;
    //     if (block != null) {
    //         block.setTargeted(true);
    //     }
    // }

    /**
     * Deselects the currently selected block.
     */
    public void deselectBlock() {
        // Uncomment when BlockEntity is ported:
        // if (selectedBlock != null) {
        //     selectedBlock.setTargeted(false);
        //     selectedBlock = null;
        // }
    }

    public int getMiningDirection() {
        return miningDirection;
    }

    public void setMiningDirection(int direction) {
        this.miningDirection = Math.max(0, Math.min(3, direction));
    }

    public int getMiningRadius() {
        return miningRadius;
    }

    public void setMiningRadius(int radius) {
        this.miningRadius = radius;
    }

    public int getPlacementRadius() {
        return placementRadius;
    }

    public void setPlacementRadius(int radius) {
        this.placementRadius = radius;
    }

    // --- Uncomment when BlockEntity, BlockRegistry are ported ---
    //
    // public boolean isBlockInRange(BlockEntity block, int entityCenterX, int entityCenterY) {
    //     if (block == null) return false;
    //     int blockCenterX = block.x + block.getSize() / 2;
    //     int blockCenterY = block.y + block.getSize() / 2;
    //     double distance = Math.sqrt(
    //         Math.pow(blockCenterX - entityCenterX, 2) +
    //         Math.pow(blockCenterY - entityCenterY, 2)
    //     );
    //     double distanceInBlocks = distance / BlockRegistry.BLOCK_SIZE;
    //     return distanceInBlocks <= miningRadius;
    // }
    //
    // public void validateSelectedBlock(int entityCenterX, int entityCenterY) {
    //     if (selectedBlock != null) {
    //         if (selectedBlock.isBroken() || !isBlockInRange(selectedBlock, entityCenterX, entityCenterY)) {
    //             deselectBlock();
    //         }
    //     }
    // }
    //
    // public boolean handleBlockClick(ArrayList<Entity> entities, int worldX, int worldY,
    //                                 int entityCenterX, int entityCenterY,
    //                                 ToolType toolType, AudioManager audioManager) {
    //     BlockEntity clickedBlock = findBlockAt(entities, worldX, worldY);
    //     if (clickedBlock != null) {
    //         if (isBlockInRange(clickedBlock, entityCenterX, entityCenterY)) {
    //             if (selectedBlock == clickedBlock) {
    //                 mineSelectedBlock(entities, toolType, audioManager);
    //                 return true;
    //             } else {
    //                 selectBlock(clickedBlock);
    //                 return true;
    //             }
    //         } else {
    //             deselectBlock();
    //         }
    //     } else {
    //         deselectBlock();
    //     }
    //     return false;
    // }
    //
    // public BlockEntity findBlockAt(ArrayList<Entity> entities, int worldX, int worldY) {
    //     for (Entity e : entities) {
    //         if (e instanceof BlockEntity) {
    //             BlockEntity block = (BlockEntity) e;
    //             if (!block.isBroken()) {
    //                 Rect bounds = block.getFullBounds();
    //                 if (bounds.contains(worldX, worldY)) {
    //                     return block;
    //                 }
    //             }
    //         }
    //     }
    //     return null;
    // }
    //
    // public boolean mineSelectedBlock(ArrayList<Entity> entities, ToolType toolType, AudioManager audioManager) {
    //     if (selectedBlock == null || selectedBlock.isBroken()) {
    //         deselectBlock();
    //         return false;
    //     }
    //     int blockDamageDir = getBlockDamageDirection();
    //     int layersToMine = toolType.getLayersPerMine(selectedBlock.getBlockType());
    //     boolean fullyBroken = false;
    //     for (int i = 0; i < layersToMine && !fullyBroken; i++) {
    //         fullyBroken = selectedBlock.mineLayer(blockDamageDir);
    //     }
    //     if (audioManager != null) {
    //         audioManager.playSound("drop");
    //     }
    //     if (fullyBroken) {
    //         ItemEntity lastDroppedItem = selectedBlock.breakBlock(audioManager);
    //         if (lastDroppedItem != null) {
    //             entities.add(lastDroppedItem);
    //         }
    //         deselectBlock();
    //         return true;
    //     }
    //     return false;
    // }

    /**
     * Converts mining direction to block damage direction.
     */
    private int getBlockDamageDirection() {
        // BlockEntity constants: MINE_UP=0, MINE_RIGHT=1, MINE_DOWN=2, MINE_LEFT=3
        switch (miningDirection) {
            case 0: return 0; // MINE_UP
            case 1: return 1; // MINE_RIGHT
            case 2: return 2; // MINE_DOWN
            case 3: return 3; // MINE_LEFT
            default: return 2; // MINE_DOWN
        }
    }

    // --- Uncomment when BlockEntity, BlockRegistry, Item, Inventory are ported ---
    //
    // public boolean canPlaceBlockAt(int worldX, int worldY, ArrayList<Entity> entities,
    //                                int entityCenterX, int entityCenterY,
    //                                Rect entityBounds) { ... }
    //
    // public boolean tryPlaceBlock(ArrayList<Entity> entities, int worldX, int worldY,
    //                              Item heldItem, Inventory inventory, AudioManager audioManager,
    //                              int entityCenterX, int entityCenterY, Rect entityBounds) { ... }
    //
    // public BlockType getBlockTypeFromItem(Item item) { ... }

    /**
     * Draws the mining direction arrow.
     * Converted from Graphics2D.fillPolygon to Canvas.drawPath.
     */
    public void drawMiningArrow(Canvas canvas, int centerX, int centerY, int direction) {
        int arrowSize = 20;
        int arrowWidth = 12;

        float[] xPoints = new float[3];
        float[] yPoints = new float[3];

        switch (direction) {
            case 0:  // Up
                xPoints[0] = centerX;
                yPoints[0] = centerY - arrowSize / 2f;
                xPoints[1] = centerX - arrowWidth / 2f;
                yPoints[1] = centerY - arrowSize / 2f - arrowSize;
                xPoints[2] = centerX + arrowWidth / 2f;
                yPoints[2] = centerY - arrowSize / 2f - arrowSize;
                break;
            case 1:  // Right
                xPoints[0] = centerX + arrowSize / 2f;
                yPoints[0] = centerY;
                xPoints[1] = centerX + arrowSize / 2f + arrowSize;
                yPoints[1] = centerY - arrowWidth / 2f;
                xPoints[2] = centerX + arrowSize / 2f + arrowSize;
                yPoints[2] = centerY + arrowWidth / 2f;
                break;
            case 2:  // Down
                xPoints[0] = centerX;
                yPoints[0] = centerY + arrowSize / 2f;
                xPoints[1] = centerX - arrowWidth / 2f;
                yPoints[1] = centerY + arrowSize / 2f + arrowSize;
                xPoints[2] = centerX + arrowWidth / 2f;
                yPoints[2] = centerY + arrowSize / 2f + arrowSize;
                break;
            case 3:  // Left
                xPoints[0] = centerX - arrowSize / 2f;
                yPoints[0] = centerY;
                xPoints[1] = centerX - arrowSize / 2f - arrowSize;
                yPoints[1] = centerY - arrowWidth / 2f;
                xPoints[2] = centerX - arrowSize / 2f - arrowSize;
                yPoints[2] = centerY + arrowWidth / 2f;
                break;
        }

        // Build path for the triangle
        Path arrowPath = new Path();
        arrowPath.moveTo(xPoints[0], yPoints[0]);
        arrowPath.lineTo(xPoints[1], yPoints[1]);
        arrowPath.lineTo(xPoints[2], yPoints[2]);
        arrowPath.close();

        // Shadow (offset by +2, +2)
        Path shadowPath = new Path();
        shadowPath.moveTo(xPoints[0] + 2, yPoints[0] + 2);
        shadowPath.lineTo(xPoints[1] + 2, yPoints[1] + 2);
        shadowPath.lineTo(xPoints[2] + 2, yPoints[2] + 2);
        shadowPath.close();
        canvas.drawPath(shadowPath, shadowPaint);

        // Fill
        canvas.drawPath(arrowPath, fillPaint);

        // Outline
        canvas.drawPath(arrowPath, outlinePaint);

        // Center dot
        int dotSize = 6;
        dotPaint.setColor(Color.BLACK);
        dotPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, (dotSize + 2) / 2f, dotPaint);
        dotPaint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerY, dotSize / 2f, dotPaint);
    }

    // --- Uncomment when BlockEntity, ItemEntity are ported ---
    //
    // public BlockEntity getLastBrokenBlock() {
    //     BlockEntity block = lastBrokenBlock;
    //     lastBrokenBlock = null;
    //     return block;
    // }
    //
    // public ItemEntity getLastDroppedItem() {
    //     ItemEntity item = lastDroppedItem;
    //     lastDroppedItem = null;
    //     return item;
    // }
}
