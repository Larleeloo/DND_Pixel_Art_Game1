package com.ambermoongame.entity.player;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.ambermoongame.entity.Entity;
import com.ambermoongame.input.TouchInputManager;

import java.util.ArrayList;

/**
 * Interface defining common player functionality.
 * Both SpritePlayerEntity (primary) and any future player types implement this interface.
 * Equivalent to entity/player/PlayerBase.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.Graphics   -> android.graphics.Canvas
 * - java.awt.Rectangle  -> android.graphics.Rect
 * - InputManager         -> TouchInputManager
 *
 * Dependencies not yet ported (methods commented out):
 * - ItemEntity (entity/item/) - dropItem method
 * - Inventory (ui/) - getInventory method
 * - AudioManager - setAudioManager method (Android version exists with different API)
 */
public interface PlayerBase {

    /**
     * Updates the player state each frame.
     * @param input The touch input manager for reading controls
     * @param entities List of all entities for collision detection
     */
    void update(TouchInputManager input, ArrayList<Entity> entities);

    /**
     * Draws the player.
     * @param canvas Canvas to draw on
     */
    void draw(Canvas canvas);

    /**
     * Gets the player's bounding box for collision detection.
     * @return Rect representing the player's bounds
     */
    Rect getBounds();

    // --- Uncomment when Inventory is ported ---
    //
    // /**
    //  * Gets the player's inventory.
    //  */
    // Inventory getInventory();

    // --- Uncomment when AndroidAudioManager integration is finalized ---
    //
    // /**
    //  * Sets the audio manager for sound effects.
    //  */
    // void setAudioManager(AudioManager audioManager);

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

    // --- Uncomment when ItemEntity is ported ---
    //
    // /**
    //  * Drops an item at the player's position.
    //  */
    // void dropItem(ItemEntity item);

    /**
     * Gets the player's X position.
     */
    int getX();

    /**
     * Gets the player's Y position.
     */
    int getY();

    /**
     * Gets the player's current health.
     */
    int getHealth();

    /**
     * Gets the player's maximum health.
     */
    int getMaxHealth();

    /**
     * Applies damage to the player.
     * @param damage Amount of damage
     * @param knockbackX Horizontal knockback force
     * @param knockbackY Vertical knockback force
     */
    void takeDamage(int damage, double knockbackX, double knockbackY);

    /**
     * Checks if the player is invincible (damage immunity frames).
     */
    boolean isInvincible();

    /**
     * Gets the player's current mana.
     */
    int getMana();

    /**
     * Gets the player's maximum mana.
     */
    int getMaxMana();

    /**
     * Gets the player's current stamina.
     */
    int getStamina();

    /**
     * Gets the player's maximum stamina.
     */
    int getMaxStamina();

    /**
     * Applies a push force to the player (from collision with mobs, etc.).
     * @param pushX Horizontal push force
     * @param pushY Vertical push force
     */
    void applyPush(double pushX, double pushY);
}
