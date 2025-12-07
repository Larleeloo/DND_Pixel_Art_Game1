import java.awt.*;
import java.util.ArrayList;

/**
 * Interface defining common player functionality.
 * Both PlayerEntity and PlayerBoneEntity implement this interface.
 */
public interface PlayerBase {

    /**
     * Updates the player state each frame.
     * @param input The input manager for reading controls
     * @param entities List of all entities for collision detection
     */
    void update(InputManager input, ArrayList<Entity> entities);

    /**
     * Draws the player.
     * @param g Graphics context
     */
    void draw(Graphics g);

    /**
     * Gets the player's bounding box for collision detection.
     * @return Rectangle representing the player's bounds
     */
    Rectangle getBounds();

    /**
     * Gets the player's inventory.
     * @return The player's inventory
     */
    Inventory getInventory();

    /**
     * Sets the audio manager for sound effects.
     * @param audioManager The audio manager
     */
    void setAudioManager(AudioManager audioManager);

    /**
     * Sets the ground Y level for this player.
     * @param groundY The Y coordinate of the ground
     */
    void setGroundY(int groundY);

    /**
     * Checks if the player is facing right.
     * @return true if facing right, false if facing left
     */
    boolean isFacingRight();

    /**
     * Drops an item at the player's position.
     * @param item The item to drop
     */
    void dropItem(ItemEntity item);

    /**
     * Gets the player's X position.
     * @return X coordinate
     */
    int getX();

    /**
     * Gets the player's Y position.
     * @return Y coordinate
     */
    int getY();
}
