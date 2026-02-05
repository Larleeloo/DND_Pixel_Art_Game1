package com.ambermoongame.graphics;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.ambermoongame.entity.Entity;

/**
 * Camera class for handling viewport scrolling in the game.
 * The camera follows the player and manages the viewport offset
 * for rendering entities in world space.
 */
public class Camera {

    // Camera position (top-left corner of viewport in world coordinates)
    private double x;
    private double y;

    // Viewport dimensions (screen size)
    private int viewportWidth;
    private int viewportHeight;

    // Level bounds (camera cannot go outside these)
    private int levelWidth;
    private int levelHeight;

    // Target to follow (usually player position)
    private Entity target;

    // Smooth following settings
    private double smoothSpeed = 0.1; // 0.0 = no follow, 1.0 = instant snap
    private boolean smoothingEnabled = true;
    private double maxCameraSpeed = 6.0; // Maximum pixels camera can move per frame

    // Dead zone - camera won't move if target is within this zone from center
    private int deadZoneX = 100;
    private int deadZoneY = 50;

    // Camera bounds enabled
    private boolean boundsEnabled = true;

    // Track if camera has been initialized with a position
    private boolean hasLastPosition = false;

    // Vertical scrolling settings
    private boolean verticalScrollEnabled = false;
    private int verticalMargin = 0; // Black bar height at top and bottom

    // Reusable Rect to avoid per-frame allocation
    private final Rect viewportRect = new Rect();

    /**
     * Creates a new camera with the specified viewport dimensions.
     *
     * @param viewportWidth  Width of the viewport (screen width)
     * @param viewportHeight Height of the viewport (screen height)
     */
    public Camera(int viewportWidth, int viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.levelWidth = viewportWidth;
        this.levelHeight = viewportHeight;
        this.x = 0;
        this.y = 0;
    }

    /**
     * Sets the level bounds that the camera cannot exceed.
     *
     * @param width  Level width in pixels
     * @param height Level height in pixels
     */
    public void setLevelBounds(int width, int height) {
        this.levelWidth = Math.max(viewportWidth, width);
        this.levelHeight = Math.max(viewportHeight, height);
    }

    /**
     * Sets the entity for the camera to follow.
     *
     * @param target The entity to follow (usually the player)
     */
    public void setTarget(Entity target) {
        this.target = target;
    }

    /**
     * Sets the smooth following speed.
     *
     * @param speed Value from 0.0 (no movement) to 1.0 (instant snap)
     */
    public void setSmoothSpeed(double speed) {
        this.smoothSpeed = Math.max(0.0, Math.min(1.0, speed));
    }

    /**
     * Enables or disables smooth camera following.
     *
     * @param enabled True to enable smoothing, false for instant follow
     */
    public void setSmoothingEnabled(boolean enabled) {
        this.smoothingEnabled = enabled;
    }

    /**
     * Sets the dead zone where the camera won't move if target is inside.
     *
     * @param x Horizontal dead zone radius from center
     * @param y Vertical dead zone radius from center
     */
    public void setDeadZone(int x, int y) {
        this.deadZoneX = x;
        this.deadZoneY = y;
    }

    /**
     * Enables or disables camera bounds checking.
     *
     * @param enabled True to keep camera within level bounds
     */
    public void setBoundsEnabled(boolean enabled) {
        this.boundsEnabled = enabled;
    }

    /**
     * Sets the maximum camera speed in pixels per frame.
     *
     * @param speed Maximum pixels the camera can move per frame
     */
    public void setMaxCameraSpeed(double speed) {
        this.maxCameraSpeed = Math.max(1.0, speed);
    }

    /**
     * Enables or disables vertical scrolling.
     *
     * @param enabled True to enable vertical scrolling
     */
    public void setVerticalScrollEnabled(boolean enabled) {
        this.verticalScrollEnabled = enabled;
    }

    /**
     * Sets the vertical margin (black bar height) at top and bottom of screen.
     *
     * @param margin Height in pixels of each black bar (top and bottom)
     */
    public void setVerticalMargin(int margin) {
        this.verticalMargin = Math.max(0, margin);
    }

    /**
     * Gets whether vertical scrolling is enabled.
     */
    public boolean isVerticalScrollEnabled() {
        return verticalScrollEnabled;
    }

    /**
     * Gets the vertical margin (black bar height).
     */
    public int getVerticalMargin() {
        return verticalMargin;
    }

    /**
     * Updates the camera position to follow the target.
     * Should be called every frame.
     */
    public void update() {
        if (target == null) {
            return;
        }

        Rect targetBounds = target.getBounds();
        double targetCenterX = targetBounds.left + targetBounds.width() / 2.0;
        double targetCenterY = targetBounds.top + targetBounds.height() / 2.0;

        // Calculate where we want the camera to be (target centered in viewport)
        double desiredX = targetCenterX - viewportWidth / 2.0;
        double desiredY;

        if (verticalScrollEnabled && verticalMargin > 0) {
            int visibleAreaHeight = viewportHeight - 2 * verticalMargin;
            desiredY = targetCenterY - verticalMargin - visibleAreaHeight / 2.0;
        } else {
            desiredY = targetCenterY - viewportHeight / 2.0;
        }

        // Clamp desired position to bounds
        if (boundsEnabled) {
            if (desiredX < 0) desiredX = 0;
            if (desiredX > levelWidth - viewportWidth) {
                desiredX = levelWidth - viewportWidth;
            }

            if (verticalScrollEnabled && verticalMargin > 0) {
                double minY = -viewportHeight;
                double maxY = levelHeight;
                if (desiredY < minY) desiredY = minY;
                if (desiredY > maxY) desiredY = maxY;
            } else {
                if (desiredY < 0) desiredY = 0;
                if (desiredY > levelHeight - viewportHeight) {
                    desiredY = levelHeight - viewportHeight;
                }
            }
        }

        // If we don't have a previous position yet, snap to target
        if (!hasLastPosition) {
            x = desiredX;
            y = desiredY;
            hasLastPosition = true;
        } else if (smoothingEnabled && smoothSpeed < 1.0) {
            double deltaX = desiredX - x;
            double deltaY = desiredY - y;

            double moveX = deltaX * smoothSpeed;
            double moveY = deltaY * smoothSpeed;

            if (Math.abs(moveX) > maxCameraSpeed) {
                moveX = maxCameraSpeed * Math.signum(moveX);
            }
            if (Math.abs(moveY) > maxCameraSpeed) {
                moveY = maxCameraSpeed * Math.signum(moveY);
            }

            x += moveX;
            y += moveY;
        } else {
            x = desiredX;
            y = desiredY;
        }

        if (boundsEnabled) {
            clampToBounds();
        }
    }

    /**
     * Immediately centers the camera on the target without smoothing.
     */
    public void snapToTarget() {
        if (target == null) {
            return;
        }

        Rect targetBounds = target.getBounds();
        double targetCenterX = targetBounds.left + targetBounds.width() / 2.0;
        double targetCenterY = targetBounds.top + targetBounds.height() / 2.0;

        x = targetCenterX - viewportWidth / 2.0;
        y = targetCenterY - viewportHeight / 2.0;

        hasLastPosition = true;

        if (boundsEnabled) {
            clampToBounds();
        }
    }

    /**
     * Sets the camera position directly.
     *
     * @param x Camera X position (left edge of viewport)
     * @param y Camera Y position (top edge of viewport)
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        if (boundsEnabled) {
            clampToBounds();
        }
    }

    /**
     * Clamps the camera position to stay within level bounds.
     */
    private void clampToBounds() {
        if (x < 0) x = 0;
        if (x > levelWidth - viewportWidth) {
            x = levelWidth - viewportWidth;
        }

        if (verticalScrollEnabled && verticalMargin > 0) {
            double minY = -viewportHeight;
            double maxY = levelHeight;
            if (y < minY) y = minY;
            if (y > maxY) y = maxY;
        } else {
            if (y < 0) y = 0;
            if (y > levelHeight - viewportHeight) {
                y = levelHeight - viewportHeight;
            }
        }
    }

    // ========== Coordinate Conversion Methods ==========

    /**
     * Converts a world X coordinate to screen X coordinate.
     */
    public int worldToScreenX(int worldX) {
        return worldX - (int) x;
    }

    /**
     * Converts a world Y coordinate to screen Y coordinate.
     */
    public int worldToScreenY(int worldY) {
        return worldY - (int) y;
    }

    /**
     * Converts a screen X coordinate to world X coordinate.
     */
    public int screenToWorldX(int screenX) {
        return screenX + (int) x;
    }

    /**
     * Converts a screen Y coordinate to world Y coordinate.
     */
    public int screenToWorldY(int screenY) {
        return screenY + (int) y;
    }

    /**
     * Applies the camera transform to a Canvas.
     * Call this before drawing world entities.
     *
     * @param canvas Canvas to transform
     */
    public void applyTransform(Canvas canvas) {
        canvas.translate(-(int) x, -(int) y);
    }

    /**
     * Removes the camera transform from a Canvas.
     * Call this after drawing world entities, before drawing UI.
     *
     * @param canvas Canvas to reset
     */
    public void removeTransform(Canvas canvas) {
        canvas.translate((int) x, (int) y);
    }

    /**
     * Checks if a rectangle in world space is visible in the viewport.
     *
     * @param bounds Rect in world coordinates
     * @return True if any part of the rectangle is visible
     */
    public boolean isVisible(Rect bounds) {
        viewportRect.set((int) x, (int) y, (int) x + viewportWidth, (int) y + viewportHeight);
        return Rect.intersects(viewportRect, bounds);
    }

    // ========== Getters ==========

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getViewportWidth() {
        return viewportWidth;
    }

    public int getViewportHeight() {
        return viewportHeight;
    }

    public int getLevelWidth() {
        return levelWidth;
    }

    public int getLevelHeight() {
        return levelHeight;
    }

    /**
     * Gets the viewport rectangle in world coordinates.
     */
    public Rect getViewportBounds() {
        return new Rect((int) x, (int) y, (int) x + viewportWidth, (int) y + viewportHeight);
    }
}
