package com.ambermoongame.scene;

import android.graphics.Canvas;

import com.ambermoongame.input.TouchInputManager;

/**
 * Interface for Android game scenes.
 * Equivalent to Scene.java from the desktop version, but uses Android Canvas
 * instead of java.awt.Graphics.
 *
 * Scenes represent different states of the game (menu, gameplay, etc.)
 */
public interface AndroidScene {

    /**
     * Called when the scene is first loaded.
     * Initialize entities, load assets, etc.
     */
    void init();

    /**
     * Called every frame to update game logic.
     * @param input The touch input manager for handling player input
     */
    void update(TouchInputManager input);

    /**
     * Called every frame to render the scene.
     * @param canvas The Android canvas to draw on
     */
    void draw(Canvas canvas);

    /**
     * Called when the scene is being exited.
     * Clean up resources, save state, etc.
     */
    void dispose();

    /**
     * Handle touch pressed events.
     * @param x Touch x coordinate (in game coordinates)
     * @param y Touch y coordinate (in game coordinates)
     */
    void onTouchPressed(int x, int y);

    /**
     * Handle touch released events.
     * @param x Touch x coordinate
     * @param y Touch y coordinate
     */
    void onTouchReleased(int x, int y);

    /**
     * Handle touch dragged/moved events.
     * @param x Touch x coordinate
     * @param y Touch y coordinate
     */
    void onTouchMoved(int x, int y);

    /**
     * Handle back button press.
     * @return true if handled, false to allow default behavior
     */
    boolean onBackPressed();

    /**
     * Get the name of this scene (for debugging/display).
     * @return Scene name
     */
    String getName();
}
