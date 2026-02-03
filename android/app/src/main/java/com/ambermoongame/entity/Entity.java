package com.ambermoongame.entity;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.ambermoongame.input.TouchInputManager;

/**
 * Abstract base class for all game entities.
 * Equivalent to entity/Entity.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.Rectangle -> android.graphics.Rect
 * - java.awt.Graphics  -> android.graphics.Canvas
 * - InputManager        -> TouchInputManager
 */
public abstract class Entity {

    public int x, y;

    public Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract Rect getBounds();

    public abstract void draw(Canvas canvas);

    public void update(TouchInputManager input) {}
}
