import java.awt.*;

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

    // Dead zone - camera won't move if target is within this zone from center
    private int deadZoneX = 100;
    private int deadZoneY = 50;

    // Camera bounds enabled
    private boolean boundsEnabled = true;

    // Track if camera has been initialized with a position
    private boolean hasLastPosition = false;

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
     * Updates the camera position to follow the target.
     * Should be called every frame.
     */
    public void update() {
        if (target == null) {
            return;
        }

        Rectangle targetBounds = target.getBounds();
        double targetCenterX = targetBounds.x + targetBounds.width / 2.0;
        double targetCenterY = targetBounds.y + targetBounds.height / 2.0;

        // Calculate where we want the camera to be (target centered in viewport)
        double desiredX = targetCenterX - viewportWidth / 2.0;
        double desiredY = targetCenterY - viewportHeight / 2.0;

        // Clamp desired position to bounds first (prevents camera from trying to go outside level)
        if (boundsEnabled) {
            if (desiredX < 0) desiredX = 0;
            if (desiredY < 0) desiredY = 0;
            if (desiredX > levelWidth - viewportWidth) {
                desiredX = levelWidth - viewportWidth;
            }
            if (desiredY > levelHeight - viewportHeight) {
                desiredY = levelHeight - viewportHeight;
            }
        }

        // If we don't have a previous position yet, snap to target
        if (!hasLastPosition) {
            x = desiredX;
            y = desiredY;
            hasLastPosition = true;
        } else if (smoothingEnabled && smoothSpeed < 1.0) {
            // Smooth lerp toward desired position
            // This creates a smooth, lagging camera that always trends toward centering
            x = lerp(x, desiredX, smoothSpeed);
            y = lerp(y, desiredY, smoothSpeed);
        } else {
            // Instant follow (smoothSpeed = 1.0)
            x = desiredX;
            y = desiredY;
        }

        // Final bounds check (ensures camera never goes outside level)
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

        Rectangle targetBounds = target.getBounds();
        double targetCenterX = targetBounds.x + targetBounds.width / 2.0;
        double targetCenterY = targetBounds.y + targetBounds.height / 2.0;

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
        // Don't let camera go past left/top edge
        if (x < 0) x = 0;
        if (y < 0) y = 0;

        // Don't let camera go past right/bottom edge
        if (x > levelWidth - viewportWidth) {
            x = levelWidth - viewportWidth;
        }
        if (y > levelHeight - viewportHeight) {
            y = levelHeight - viewportHeight;
        }
    }

    /**
     * Linear interpolation helper.
     */
    private double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }

    // ========== Coordinate Conversion Methods ==========

    /**
     * Converts a world X coordinate to screen X coordinate.
     *
     * @param worldX X position in world space
     * @return X position in screen space
     */
    public int worldToScreenX(int worldX) {
        return worldX - (int) x;
    }

    /**
     * Converts a world Y coordinate to screen Y coordinate.
     *
     * @param worldY Y position in world space
     * @return Y position in screen space
     */
    public int worldToScreenY(int worldY) {
        return worldY - (int) y;
    }

    /**
     * Converts a world position to screen position.
     *
     * @param worldPoint Point in world space
     * @return Point in screen space
     */
    public Point worldToScreen(Point worldPoint) {
        return new Point(
            worldPoint.x - (int) x,
            worldPoint.y - (int) y
        );
    }

    /**
     * Converts a screen X coordinate to world X coordinate.
     *
     * @param screenX X position in screen space
     * @return X position in world space
     */
    public int screenToWorldX(int screenX) {
        return screenX + (int) x;
    }

    /**
     * Converts a screen Y coordinate to world Y coordinate.
     *
     * @param screenY Y position in screen space
     * @return Y position in world space
     */
    public int screenToWorldY(int screenY) {
        return screenY + (int) y;
    }

    /**
     * Converts a screen position to world position.
     *
     * @param screenPoint Point in screen space
     * @return Point in world space
     */
    public Point screenToWorld(Point screenPoint) {
        return new Point(
            screenPoint.x + (int) x,
            screenPoint.y + (int) y
        );
    }

    /**
     * Applies the camera transform to a Graphics2D context.
     * Call this before drawing world entities.
     *
     * @param g2d Graphics2D context to transform
     */
    public void applyTransform(Graphics2D g2d) {
        g2d.translate(-(int) x, -(int) y);
    }

    /**
     * Removes the camera transform from a Graphics2D context.
     * Call this after drawing world entities, before drawing UI.
     *
     * @param g2d Graphics2D context to reset
     */
    public void removeTransform(Graphics2D g2d) {
        g2d.translate((int) x, (int) y);
    }

    /**
     * Checks if a rectangle in world space is visible in the viewport.
     *
     * @param bounds Rectangle in world coordinates
     * @return True if any part of the rectangle is visible
     */
    public boolean isVisible(Rectangle bounds) {
        Rectangle viewport = new Rectangle((int) x, (int) y, viewportWidth, viewportHeight);
        return viewport.intersects(bounds);
    }

    // ========== Getters ==========

    /**
     * Gets the camera X position (left edge of viewport in world space).
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the camera Y position (top edge of viewport in world space).
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the viewport width.
     */
    public int getViewportWidth() {
        return viewportWidth;
    }

    /**
     * Gets the viewport height.
     */
    public int getViewportHeight() {
        return viewportHeight;
    }

    /**
     * Gets the level width.
     */
    public int getLevelWidth() {
        return levelWidth;
    }

    /**
     * Gets the level height.
     */
    public int getLevelHeight() {
        return levelHeight;
    }

    /**
     * Gets the viewport rectangle in world coordinates.
     */
    public Rectangle getViewportBounds() {
        return new Rectangle((int) x, (int) y, viewportWidth, viewportHeight);
    }
}
