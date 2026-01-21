package entity.capabilities;

import block.*;
import entity.*;
import entity.item.Item;
import entity.item.ItemEntity;
import audio.*;
import ui.*;

import java.awt.*;
import java.util.ArrayList;

/**
 * Helper class that provides default implementations for block interaction.
 * Can be used as a delegate by any entity implementing BlockInteractionHandler.
 *
 * This helper handles:
 * - Block selection and mining state
 * - Range calculations
 * - Block placement validation
 * - Visual feedback (arrows, highlights)
 */
public class BlockInteractionHelper {

    // Mining state
    private BlockEntity selectedBlock = null;
    private int miningDirection = 2;  // 0=up, 1=right, 2=down, 3=left
    private int miningRadius = BlockInteractionHandler.DEFAULT_MINING_RADIUS;
    private int placementRadius = BlockInteractionHandler.DEFAULT_PLACEMENT_RADIUS;

    // References
    private BlockEntity lastBrokenBlock = null;
    private ItemEntity lastDroppedItem = null;

    /**
     * Gets the currently selected block.
     */
    public BlockEntity getSelectedBlock() {
        return selectedBlock;
    }

    /**
     * Selects a block for mining.
     */
    public void selectBlock(BlockEntity block) {
        if (selectedBlock != null && selectedBlock != block) {
            selectedBlock.setTargeted(false);
        }
        selectedBlock = block;
        if (block != null) {
            block.setTargeted(true);
        }
    }

    /**
     * Deselects the currently selected block.
     */
    public void deselectBlock() {
        if (selectedBlock != null) {
            selectedBlock.setTargeted(false);
            selectedBlock = null;
        }
    }

    /**
     * Gets the current mining direction.
     */
    public int getMiningDirection() {
        return miningDirection;
    }

    /**
     * Sets the mining direction.
     */
    public void setMiningDirection(int direction) {
        this.miningDirection = Math.max(0, Math.min(3, direction));
    }

    /**
     * Gets the mining radius.
     */
    public int getMiningRadius() {
        return miningRadius;
    }

    /**
     * Sets the mining radius.
     */
    public void setMiningRadius(int radius) {
        this.miningRadius = radius;
    }

    /**
     * Gets the placement radius.
     */
    public int getPlacementRadius() {
        return placementRadius;
    }

    /**
     * Sets the placement radius.
     */
    public void setPlacementRadius(int radius) {
        this.placementRadius = radius;
    }

    /**
     * Checks if a block is within range of the entity.
     */
    public boolean isBlockInRange(BlockEntity block, int entityCenterX, int entityCenterY) {
        if (block == null) return false;

        int blockCenterX = block.x + block.getSize() / 2;
        int blockCenterY = block.y + block.getSize() / 2;

        double distance = Math.sqrt(
            Math.pow(blockCenterX - entityCenterX, 2) +
            Math.pow(blockCenterY - entityCenterY, 2)
        );

        double distanceInBlocks = distance / BlockRegistry.BLOCK_SIZE;
        return distanceInBlocks <= miningRadius;
    }

    /**
     * Validates the selected block is still valid.
     */
    public void validateSelectedBlock(int entityCenterX, int entityCenterY) {
        if (selectedBlock != null) {
            if (selectedBlock.isBroken() || !isBlockInRange(selectedBlock, entityCenterX, entityCenterY)) {
                deselectBlock();
            }
        }
    }

    /**
     * Handles a click for block selection/mining.
     * @return true if a block was interacted with
     */
    public boolean handleBlockClick(ArrayList<Entity> entities, int worldX, int worldY,
                                    int entityCenterX, int entityCenterY,
                                    ToolType toolType, AudioManager audioManager) {
        // Find the block that was clicked on
        BlockEntity clickedBlock = findBlockAt(entities, worldX, worldY);

        if (clickedBlock != null) {
            if (isBlockInRange(clickedBlock, entityCenterX, entityCenterY)) {
                if (selectedBlock == clickedBlock) {
                    // Clicking on already-selected block -> mine it
                    mineSelectedBlock(entities, toolType, audioManager);
                    return true;
                } else {
                    // Clicking on a new block -> select it
                    selectBlock(clickedBlock);
                    return true;
                }
            } else {
                // Block is out of range - deselect
                deselectBlock();
            }
        } else {
            // Clicked on empty space - deselect
            deselectBlock();
        }

        return false;
    }

    /**
     * Finds a block at the specified world coordinates.
     */
    public BlockEntity findBlockAt(ArrayList<Entity> entities, int worldX, int worldY) {
        for (Entity e : entities) {
            if (e instanceof BlockEntity) {
                BlockEntity block = (BlockEntity) e;
                if (!block.isBroken()) {
                    Rectangle bounds = block.getFullBounds();
                    if (bounds.contains(worldX, worldY)) {
                        return block;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Mines the currently selected block.
     * @return true if block was fully broken
     */
    public boolean mineSelectedBlock(ArrayList<Entity> entities, ToolType toolType, AudioManager audioManager) {
        if (selectedBlock == null || selectedBlock.isBroken()) {
            deselectBlock();
            return false;
        }

        lastBrokenBlock = null;
        lastDroppedItem = null;

        // Convert mining direction to block damage direction
        int blockDamageDir = getBlockDamageDirection();

        int layersToMine = toolType.getLayersPerMine(selectedBlock.getBlockType());

        boolean fullyBroken = false;
        for (int i = 0; i < layersToMine && !fullyBroken; i++) {
            fullyBroken = selectedBlock.mineLayer(blockDamageDir);
        }

        if (audioManager != null) {
            audioManager.playSound("drop");
        }

        if (fullyBroken) {
            lastBrokenBlock = selectedBlock;
            lastDroppedItem = selectedBlock.breakBlock(audioManager);
            if (lastDroppedItem != null) {
                entities.add(lastDroppedItem);
            }
            deselectBlock();
            return true;
        }

        return false;
    }

    /**
     * Converts mining direction to block damage direction.
     */
    private int getBlockDamageDirection() {
        switch (miningDirection) {
            case 0: return BlockEntity.MINE_UP;
            case 1: return BlockEntity.MINE_RIGHT;
            case 2: return BlockEntity.MINE_DOWN;
            case 3: return BlockEntity.MINE_LEFT;
            default: return BlockEntity.MINE_DOWN;
        }
    }

    /**
     * Checks if a position is valid for block placement.
     */
    public boolean canPlaceBlockAt(int worldX, int worldY, ArrayList<Entity> entities,
                                   int entityCenterX, int entityCenterY,
                                   Rectangle entityBounds) {
        // Convert to grid coordinates
        int gridX = BlockEntity.pixelToGrid(worldX);
        int gridY = BlockEntity.pixelToGrid(worldY);
        int pixelX = BlockEntity.gridToPixel(gridX);
        int pixelY = BlockEntity.gridToPixel(gridY);

        // Check if within placement radius
        int blockCenterX = pixelX + BlockRegistry.BLOCK_SIZE / 2;
        int blockCenterY = pixelY + BlockRegistry.BLOCK_SIZE / 2;

        double distance = Math.sqrt(
            Math.pow(blockCenterX - entityCenterX, 2) +
            Math.pow(blockCenterY - entityCenterY, 2)
        );

        double distanceInBlocks = distance / BlockRegistry.BLOCK_SIZE;
        if (distanceInBlocks > placementRadius) {
            return false;
        }

        // Check for existing blocks
        Rectangle newBlockBounds = new Rectangle(pixelX, pixelY, BlockRegistry.BLOCK_SIZE, BlockRegistry.BLOCK_SIZE);
        for (Entity e : entities) {
            if (e instanceof BlockEntity) {
                BlockEntity block = (BlockEntity) e;
                if (!block.isBroken() && newBlockBounds.intersects(block.getFullBounds())) {
                    return false;
                }
            }
        }

        // Check if overlapping with entity
        if (entityBounds != null && newBlockBounds.intersects(entityBounds)) {
            return false;
        }

        return true;
    }

    /**
     * Attempts to place a block at the specified position.
     * @return true if placement was successful
     */
    public boolean tryPlaceBlock(ArrayList<Entity> entities, int worldX, int worldY,
                                 Item heldItem, Inventory inventory, AudioManager audioManager,
                                 int entityCenterX, int entityCenterY, Rectangle entityBounds) {
        if (heldItem == null || heldItem.getCategory() != Item.ItemCategory.BLOCK) {
            return false;
        }

        if (!canPlaceBlockAt(worldX, worldY, entities, entityCenterX, entityCenterY, entityBounds)) {
            return false;
        }

        // Convert to grid coordinates
        int gridX = BlockEntity.pixelToGrid(worldX);
        int gridY = BlockEntity.pixelToGrid(worldY);

        // Determine block type from held item
        BlockType blockType = getBlockTypeFromItem(heldItem);
        if (blockType == null) {
            blockType = BlockType.DIRT;
        }

        // Create and add the new block
        BlockEntity newBlock = new BlockEntity(gridX, gridY, blockType, true);
        newBlock.onPlace(audioManager);
        entities.add(newBlock);

        // Consume the block from inventory
        if (inventory != null) {
            int currentSlot = inventory.getSelectedSlot();
            inventory.removeItemAtSlot(currentSlot);
        }

        return true;
    }

    /**
     * Converts a held item to a block type.
     */
    public BlockType getBlockTypeFromItem(Item item) {
        if (item == null) return null;

        String name = item.getName().toLowerCase();

        if (name.contains("grass")) return BlockType.GRASS;
        if (name.contains("dirt")) return BlockType.DIRT;
        if (name.contains("stone") && !name.contains("cobble")) return BlockType.STONE;
        if (name.contains("cobble")) return BlockType.COBBLESTONE;
        if (name.contains("wood") || name.contains("plank")) return BlockType.WOOD;
        if (name.contains("leaves") || name.contains("leaf")) return BlockType.LEAVES;
        if (name.contains("brick")) return BlockType.BRICK;
        if (name.contains("sand")) return BlockType.SAND;
        if (name.contains("glass")) return BlockType.GLASS;
        if (name.contains("coal")) return BlockType.COAL_ORE;
        if (name.contains("iron") && name.contains("ore")) return BlockType.IRON_ORE;
        if (name.contains("gold") && name.contains("ore")) return BlockType.GOLD_ORE;
        if (name.contains("water")) return BlockType.WATER;

        return BlockType.DIRT;
    }

    /**
     * Draws the mining direction arrow.
     */
    public void drawMiningArrow(Graphics2D g2d, int centerX, int centerY, int direction) {
        int arrowSize = 20;
        int arrowWidth = 12;

        Stroke originalStroke = g2d.getStroke();

        int[] xPoints = new int[3];
        int[] yPoints = new int[3];

        switch (direction) {
            case 0:  // Up
                xPoints[0] = centerX;
                yPoints[0] = centerY - arrowSize / 2;
                xPoints[1] = centerX - arrowWidth / 2;
                yPoints[1] = centerY - arrowSize / 2 - arrowSize;
                xPoints[2] = centerX + arrowWidth / 2;
                yPoints[2] = centerY - arrowSize / 2 - arrowSize;
                break;
            case 1:  // Right
                xPoints[0] = centerX + arrowSize / 2;
                yPoints[0] = centerY;
                xPoints[1] = centerX + arrowSize / 2 + arrowSize;
                yPoints[1] = centerY - arrowWidth / 2;
                xPoints[2] = centerX + arrowSize / 2 + arrowSize;
                yPoints[2] = centerY + arrowWidth / 2;
                break;
            case 2:  // Down
                xPoints[0] = centerX;
                yPoints[0] = centerY + arrowSize / 2;
                xPoints[1] = centerX - arrowWidth / 2;
                yPoints[1] = centerY + arrowSize / 2 + arrowSize;
                xPoints[2] = centerX + arrowWidth / 2;
                yPoints[2] = centerY + arrowSize / 2 + arrowSize;
                break;
            case 3:  // Left
                xPoints[0] = centerX - arrowSize / 2;
                yPoints[0] = centerY;
                xPoints[1] = centerX - arrowSize / 2 - arrowSize;
                yPoints[1] = centerY - arrowWidth / 2;
                xPoints[2] = centerX - arrowSize / 2 - arrowSize;
                yPoints[2] = centerY + arrowWidth / 2;
                break;
        }

        // Shadow
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillPolygon(new int[]{xPoints[0] + 2, xPoints[1] + 2, xPoints[2] + 2},
                       new int[]{yPoints[0] + 2, yPoints[1] + 2, yPoints[2] + 2}, 3);

        // Fill
        g2d.setColor(new Color(255, 220, 100));
        g2d.fillPolygon(xPoints, yPoints, 3);

        // Outline
        g2d.setColor(new Color(200, 150, 50));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(xPoints, yPoints, 3);

        // Center dot
        int dotSize = 6;
        g2d.setColor(Color.BLACK);
        g2d.fillOval(centerX - dotSize/2 - 1, centerY - dotSize/2 - 1, dotSize + 2, dotSize + 2);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(centerX - dotSize/2, centerY - dotSize/2, dotSize, dotSize);

        g2d.setStroke(originalStroke);
    }

    /**
     * Gets the last broken block (and clears the reference).
     */
    public BlockEntity getLastBrokenBlock() {
        BlockEntity block = lastBrokenBlock;
        lastBrokenBlock = null;
        return block;
    }

    /**
     * Gets the last dropped item (and clears the reference).
     */
    public ItemEntity getLastDroppedItem() {
        ItemEntity item = lastDroppedItem;
        lastDroppedItem = null;
        return item;
    }
}
