package com.ambermoongame.scene;

import android.graphics.Canvas;

import com.ambermoongame.input.TouchInputManager;

/**
 * Base implementation of AndroidScene providing common functionality.
 * Extend this class for specific scene implementations.
 */
public abstract class BaseScene implements AndroidScene {

    protected String name;
    protected boolean initialized = false;

    public BaseScene(String name) {
        this.name = name;
    }

    @Override
    public void init() {
        initialized = true;
    }

    @Override
    public void dispose() {
        initialized = false;
    }

    @Override
    public void onTouchPressed(int x, int y) {
        // Override in subclass if needed
    }

    @Override
    public void onTouchReleased(int x, int y) {
        // Override in subclass if needed
    }

    @Override
    public void onTouchMoved(int x, int y) {
        // Override in subclass if needed
    }

    @Override
    public boolean onBackPressed() {
        // Default: go to main menu
        AndroidSceneManager.getInstance().setScene("mainMenu", AndroidSceneManager.TRANSITION_FADE);
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    protected boolean isInitialized() {
        return initialized;
    }
}
