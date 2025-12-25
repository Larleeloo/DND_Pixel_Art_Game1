package scene;
import core.*;
import entity.*;
import entity.player.*;
import entity.mob.*;
import block.*;
import animation.*;
import graphics.*;
import level.*;
import audio.*;
import input.*;
import ui.*;

import java.awt.*;

/**
 * Interface for game scenes.
 * Scenes represent different states of the game (menu, gameplay, etc.)
 */
public interface Scene {

    /**
     * Called when the scene is first loaded.
     * Initialize entities, load assets, etc.
     */
    void init();

    /**
     * Called every frame to update game logic.
     * @param input The input manager for handling player input
     */
    void update(InputManager input);

    /**
     * Called every frame to render the scene.
     * @param g The graphics context
     */
    void draw(Graphics g);

    /**
     * Called when the scene is being exited.
     * Clean up resources, save state, etc.
     */
    void dispose();

    /**
     * Handle mouse pressed events.
     * @param x Mouse x coordinate
     * @param y Mouse y coordinate
     */
    void onMousePressed(int x, int y);

    /**
     * Handle mouse released events.
     * @param x Mouse x coordinate
     * @param y Mouse y coordinate
     */
    void onMouseReleased(int x, int y);

    /**
     * Handle mouse dragged events.
     * @param x Mouse x coordinate
     * @param y Mouse y coordinate
     */
    void onMouseDragged(int x, int y);

    /**
     * Handle mouse moved events.
     * @param x Mouse x coordinate
     * @param y Mouse y coordinate
     */
    void onMouseMoved(int x, int y);

    /**
     * Handle mouse clicked events.
     * @param x Mouse x coordinate
     * @param y Mouse y coordinate
     */
    void onMouseClicked(int x, int y);

    /**
     * Get the name of this scene (for debugging/display).
     * @return Scene name
     */
    String getName();
}
