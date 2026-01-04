package entity.capabilities;

import block.*;
import entity.*;
import graphics.*;
import input.*;
import audio.*;
import ui.*;

import java.awt.*;
import java.util.ArrayList;

/**
 * Interface for entities that can interact with blocks (mining and placement).
 * Provides a shared contract for block interactions that can be implemented by
 * players, companions, NPCs, or other entities that need to mine or place blocks.
 *
 * The block interaction system uses a click-to-select model:
 * - Left-click on a block to select it for mining
 * - Arrow keys to change mining direction
 * - Click again or press action key to mine
 * - Right-click on empty space to place blocks (if holding a block item)
 */
public interface BlockInteractionHandler {

    /** Default radius (in blocks) for mining operations */
    int DEFAULT_MINING_RADIUS = 3;

    /** Default radius (in blocks) for placement operations */
    int DEFAULT_PLACEMENT_RADIUS = 3;

    // ==================== Mining System ====================

    /**
     * Gets the currently selected block for mining.
     * @return The selected BlockEntity, or null if no block is selected
     */
    BlockEntity getSelectedBlock();

    /**
     * Selects a block for mining.
     * @param block The block to select, or null to deselect
     */
    void selectBlock(BlockEntity block);

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
     * @return Maximum distance for mining operations
     */
    default int getMiningRadius() {
        return DEFAULT_MINING_RADIUS;
    }

    /**
     * Checks if a block is within mining range of this entity.
     * @param block The block to check
     * @return true if block is within mining range
     */
    boolean isBlockInRange(BlockEntity block);

    /**
     * Validates that the selected block is still valid (exists, in range, not broken).
     * Should be called each update frame.
     */
    void validateSelectedBlock();

    /**
     * Attempts to mine the currently selected block.
     * @param entities The list of all entities (for adding dropped items)
     * @return true if mining was successful
     */
    boolean mineSelectedBlock(ArrayList<Entity> entities);

    /**
     * Handles a click on blocks - selecting or mining based on state.
     * @param entities The list of all entities
     * @param worldX Click X position in world coordinates
     * @param worldY Click Y position in world coordinates
     * @return true if a block was interacted with
     */
    boolean handleBlockClick(ArrayList<Entity> entities, int worldX, int worldY);

    // ==================== Placement System ====================

    /**
     * Gets the placement radius in blocks.
     * @return Maximum distance for placement operations
     */
    default int getPlacementRadius() {
        return DEFAULT_PLACEMENT_RADIUS;
    }

    /**
     * Checks if a position is valid for block placement.
     * @param worldX X position in world coordinates
     * @param worldY Y position in world coordinates
     * @param entities List of all entities for collision checking
     * @return true if a block can be placed at this position
     */
    boolean canPlaceBlockAt(int worldX, int worldY, ArrayList<Entity> entities);

    /**
     * Attempts to place a block at the specified position.
     * @param entities The list of all entities
     * @param worldX World X coordinate
     * @param worldY World Y coordinate
     * @return true if placement was successful
     */
    boolean tryPlaceBlock(ArrayList<Entity> entities, int worldX, int worldY);

    // ==================== Tool Support ====================

    /**
     * Gets the tool type currently being used for mining.
     * @return The ToolType, or HAND if no tool is equipped
     */
    ToolType getEquippedToolType();

    /**
     * Gets the number of layers to mine per action based on the equipped tool.
     * @param blockType The type of block being mined
     * @return Number of layers to remove per mining action
     */
    default int getLayersPerMine(BlockType blockType) {
        return getEquippedToolType().getLayersPerMine(blockType);
    }

    // ==================== Resource Access ====================

    /**
     * Gets the entity's inventory (for consuming/placing blocks).
     * @return The inventory, or null if entity has no inventory
     */
    Inventory getInventory();

    /**
     * Gets the audio manager for playing sounds.
     * @return The AudioManager, or null if unavailable
     */
    AudioManager getAudioManager();

    /**
     * Gets the camera for coordinate conversion.
     * @return The Camera, or null if unavailable
     */
    Camera getCamera();

    // ==================== Position Access ====================

    /**
     * Gets the entity's center X position for range calculations.
     * @return Center X in world coordinates
     */
    int getCenterX();

    /**
     * Gets the entity's center Y position for range calculations.
     * @return Center Y in world coordinates
     */
    int getCenterY();

    // ==================== Visual Feedback ====================

    /**
     * Draws the mining direction arrow on the selected block.
     * @param g2d Graphics context
     * @param centerX Block center X
     * @param centerY Block center Y
     * @param direction Mining direction
     */
    void drawMiningArrow(Graphics2D g2d, int centerX, int centerY, int direction);

    /**
     * Draws the selection highlight around the selected block.
     * @param g2d Graphics context
     * @param blockBounds The bounds of the selected block
     */
    default void drawSelectionHighlight(Graphics2D g2d, Rectangle blockBounds) {
        // Default implementation - yellow highlight
        g2d.setColor(new Color(255, 255, 100, 80));
        g2d.fillRect(blockBounds.x, blockBounds.y, blockBounds.width, blockBounds.height);
        g2d.setColor(new Color(255, 255, 100, 200));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(blockBounds.x, blockBounds.y, blockBounds.width, blockBounds.height);
    }

    /**
     * Draws a placement preview at the specified grid position.
     * @param g2d Graphics context
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     * @param canPlace Whether placement is valid at this position
     */
    default void drawPlacementPreview(Graphics2D g2d, int gridX, int gridY, boolean canPlace) {
        int pixelX = BlockEntity.gridToPixel(gridX);
        int pixelY = BlockEntity.gridToPixel(gridY);
        int size = BlockRegistry.BLOCK_SIZE;

        // Draw preview with color based on validity
        Color fillColor = canPlace ? new Color(100, 255, 100, 60) : new Color(255, 100, 100, 60);
        Color borderColor = canPlace ? new Color(100, 255, 100, 180) : new Color(255, 100, 100, 180);

        g2d.setColor(fillColor);
        g2d.fillRect(pixelX, pixelY, size, size);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(pixelX, pixelY, size, size);
    }
}
