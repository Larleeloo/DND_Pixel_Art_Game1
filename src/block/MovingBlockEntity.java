package block;

import entity.*;
import graphics.*;
import audio.*;
import input.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * A block entity that moves along a path or pattern.
 * Moving blocks can carry the player when standing on top of them.
 *
 * Movement Patterns:
 * - HORIZONTAL: Moves left and right between two points
 * - VERTICAL: Moves up and down between two points
 * - CIRCULAR: Moves in a circular pattern around a center point
 * - PATH: Follows a set of waypoints
 *
 * Configuration:
 * - startX, startY: Initial position
 * - endX, endY: End position for linear movement (or center for circular)
 * - speed: Movement speed in pixels per frame
 * - pauseTime: Time to wait at each endpoint (in frames)
 * - radius: For circular movement, the radius of the circle
 */
public class MovingBlockEntity extends BlockEntity {

    // Movement pattern types
    public enum MovementPattern {
        HORIZONTAL,  // Left-right movement
        VERTICAL,    // Up-down movement
        CIRCULAR,    // Circular path around a point
        PATH         // Custom waypoint path
    }

    // Movement state
    private MovementPattern pattern;
    private double startX, startY;  // Starting position
    private double endX, endY;      // End position (or center for circular)
    private double speed;           // Pixels per frame
    private int pauseTime;          // Frames to pause at endpoints
    private int currentPause;       // Current pause counter
    private boolean movingForward;  // Direction flag for linear patterns

    // Position tracking (double precision for smooth movement)
    private double posX, posY;
    private double prevX, prevY;    // Previous position for velocity calculation

    // Circular movement
    private double angle;           // Current angle for circular movement
    private double radius;          // Radius for circular movement
    private double centerX, centerY; // Center point for circular movement

    // Waypoint path
    private java.util.List<Point> waypoints;
    private int currentWaypointIndex;

    // Entity riding tracking
    private Entity ridingEntity;    // Entity currently riding this block (if any)
    private double riderOffsetX;    // Rider's X offset from block left edge
    private double riderAccumX;     // Accumulated fractional X movement for rider
    private double riderAccumY;     // Accumulated fractional Y movement for rider

    /**
     * Creates a moving block with horizontal movement pattern.
     *
     * @param gridX Starting grid X position
     * @param gridY Starting grid Y position
     * @param blockType The type of block
     * @param endGridX End grid X position
     * @param endGridY End grid Y position
     * @param speed Movement speed (pixels per frame)
     */
    public MovingBlockEntity(int gridX, int gridY, BlockType blockType, boolean useGridCoords,
                             int endGridX, int endGridY, double speed) {
        super(gridX, gridY, blockType, useGridCoords);

        if (useGridCoords) {
            this.startX = gridX * BlockRegistry.BLOCK_SIZE;
            this.startY = gridY * BlockRegistry.BLOCK_SIZE;
            this.endX = endGridX * BlockRegistry.BLOCK_SIZE;
            this.endY = endGridY * BlockRegistry.BLOCK_SIZE;
        } else {
            this.startX = gridX;
            this.startY = gridY;
            this.endX = endGridX;
            this.endY = endGridY;
        }

        this.posX = this.startX;
        this.posY = this.startY;
        this.prevX = this.posX;
        this.prevY = this.posY;
        this.speed = speed;
        this.pauseTime = 30; // Default half-second pause at endpoints
        this.currentPause = 0;
        this.movingForward = true;

        // Determine pattern based on movement direction
        if (Math.abs(endX - startX) > Math.abs(endY - startY)) {
            this.pattern = MovementPattern.HORIZONTAL;
        } else {
            this.pattern = MovementPattern.VERTICAL;
        }

        this.waypoints = new ArrayList<>();
    }

    /**
     * Creates a moving block with specified movement pattern.
     */
    public MovingBlockEntity(int gridX, int gridY, BlockType blockType, boolean useGridCoords,
                             MovementPattern pattern, double speed) {
        super(gridX, gridY, blockType, useGridCoords);

        if (useGridCoords) {
            this.startX = gridX * BlockRegistry.BLOCK_SIZE;
            this.startY = gridY * BlockRegistry.BLOCK_SIZE;
        } else {
            this.startX = gridX;
            this.startY = gridY;
        }

        this.posX = this.startX;
        this.posY = this.startY;
        this.prevX = this.posX;
        this.prevY = this.posY;
        this.pattern = pattern;
        this.speed = speed;
        this.pauseTime = 30;
        this.currentPause = 0;
        this.movingForward = true;
        this.endX = this.startX;
        this.endY = this.startY;
        this.waypoints = new ArrayList<>();
    }

    /**
     * Set the end position for linear movement.
     */
    public void setEndPosition(int endGridX, int endGridY, boolean useGridCoords) {
        if (useGridCoords) {
            this.endX = endGridX * BlockRegistry.BLOCK_SIZE;
            this.endY = endGridY * BlockRegistry.BLOCK_SIZE;
        } else {
            this.endX = endGridX;
            this.endY = endGridY;
        }
    }

    /**
     * Configure circular movement.
     */
    public void setCircularMovement(int centerGridX, int centerGridY, boolean useGridCoords, double radius) {
        this.pattern = MovementPattern.CIRCULAR;
        if (useGridCoords) {
            this.centerX = centerGridX * BlockRegistry.BLOCK_SIZE;
            this.centerY = centerGridY * BlockRegistry.BLOCK_SIZE;
        } else {
            this.centerX = centerGridX;
            this.centerY = centerGridY;
        }
        this.radius = radius;
        this.angle = 0;

        // Set initial position on circle
        this.posX = centerX + radius;
        this.posY = centerY;
        this.x = (int) posX;
        this.y = (int) posY;
    }

    /**
     * Add a waypoint for path-based movement.
     */
    public void addWaypoint(int gridX, int gridY, boolean useGridCoords) {
        int px, py;
        if (useGridCoords) {
            px = gridX * BlockRegistry.BLOCK_SIZE;
            py = gridY * BlockRegistry.BLOCK_SIZE;
        } else {
            px = gridX;
            py = gridY;
        }
        waypoints.add(new Point(px, py));

        if (pattern != MovementPattern.PATH) {
            pattern = MovementPattern.PATH;
            currentWaypointIndex = 0;
        }
    }

    /**
     * Set pause time at endpoints (in frames, 60 frames = 1 second).
     */
    public void setPauseTime(int frames) {
        this.pauseTime = frames;
    }

    /**
     * Set movement speed.
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * Get the movement velocity (for carrying entities).
     */
    public double getVelocityX() {
        return posX - prevX;
    }

    public double getVelocityY() {
        return posY - prevY;
    }

    /**
     * Get the movement pattern.
     */
    public MovementPattern getPattern() {
        return pattern;
    }

    /**
     * Get movement speed.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Get start position.
     */
    public double getStartX() { return startX; }
    public double getStartY() { return startY; }

    /**
     * Get end position.
     */
    public double getEndX() { return endX; }
    public double getEndY() { return endY; }

    /**
     * Get circular movement parameters.
     */
    public double getCenterX() { return centerX; }
    public double getCenterY() { return centerY; }
    public double getRadius() { return radius; }

    /**
     * Get pause time.
     */
    public int getPauseTime() { return pauseTime; }

    /**
     * Get waypoints for PATH pattern.
     */
    public java.util.List<Point> getWaypoints() {
        return waypoints;
    }

    @Override
    public void update(InputManager input) {
        if (isBroken()) return;

        // Store previous position for velocity calculation
        prevX = posX;
        prevY = posY;

        // Handle pause at endpoints
        if (currentPause > 0) {
            currentPause--;
            return;
        }

        // Update position based on pattern
        switch (pattern) {
            case HORIZONTAL:
            case VERTICAL:
                updateLinearMovement();
                break;
            case CIRCULAR:
                updateCircularMovement();
                break;
            case PATH:
                updatePathMovement();
                break;
        }

        // Update integer position for rendering and collision
        this.x = (int) posX;
        this.y = (int) posY;
    }

    /**
     * Update linear (horizontal or vertical) movement.
     */
    private void updateLinearMovement() {
        double targetX = movingForward ? endX : startX;
        double targetY = movingForward ? endY : startY;

        double dx = targetX - posX;
        double dy = targetY - posY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= speed) {
            // Reached endpoint
            posX = targetX;
            posY = targetY;
            movingForward = !movingForward;
            currentPause = pauseTime;
        } else {
            // Move toward target
            double ratio = speed / distance;
            posX += dx * ratio;
            posY += dy * ratio;
        }
    }

    /**
     * Update circular movement.
     */
    private void updateCircularMovement() {
        // Convert linear speed to angular speed
        double angularSpeed = speed / radius;
        angle += angularSpeed;

        if (angle >= Math.PI * 2) {
            angle -= Math.PI * 2;
        }

        posX = centerX + Math.cos(angle) * radius;
        posY = centerY + Math.sin(angle) * radius;
    }

    /**
     * Update waypoint path movement.
     */
    private void updatePathMovement() {
        if (waypoints.isEmpty()) return;

        Point target = waypoints.get(currentWaypointIndex);
        double dx = target.x - posX;
        double dy = target.y - posY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= speed) {
            // Reached waypoint
            posX = target.x;
            posY = target.y;
            currentWaypointIndex++;

            if (currentWaypointIndex >= waypoints.size()) {
                // Loop back to start
                currentWaypointIndex = 0;
            }
            currentPause = pauseTime;
        } else {
            // Move toward waypoint
            double ratio = speed / distance;
            posX += dx * ratio;
            posY += dy * ratio;
        }
    }

    @Override
    public void draw(Graphics g) {
        // Draw the block using parent implementation
        super.draw(g);

        // Optionally draw movement path indicator in debug mode
        // This is handled by the scene if debug mode is enabled
    }

    /**
     * Draw debug visualization of the movement path.
     */
    public void drawDebugPath(Graphics2D g, int cameraX, int cameraY) {
        g.setColor(new Color(255, 255, 0, 100));
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                    0, new float[]{5, 5}, 0));

        int sx = (int) startX - cameraX;
        int sy = (int) startY - cameraY;

        switch (pattern) {
            case HORIZONTAL:
            case VERTICAL:
                int ex = (int) endX - cameraX;
                int ey = (int) endY - cameraY;
                g.drawLine(sx + getSize() / 2, sy + getSize() / 2,
                          ex + getSize() / 2, ey + getSize() / 2);
                // Draw endpoint markers
                g.fillOval(sx + getSize() / 2 - 5, sy + getSize() / 2 - 5, 10, 10);
                g.fillOval(ex + getSize() / 2 - 5, ey + getSize() / 2 - 5, 10, 10);
                break;

            case CIRCULAR:
                int cx = (int) centerX - cameraX;
                int cy = (int) centerY - cameraY;
                g.drawOval(cx - (int) radius, cy - (int) radius,
                          (int) (radius * 2), (int) (radius * 2));
                // Draw center point
                g.fillOval(cx - 5, cy - 5, 10, 10);
                break;

            case PATH:
                if (!waypoints.isEmpty()) {
                    Point prev = new Point((int) startX, (int) startY);
                    for (Point wp : waypoints) {
                        g.drawLine(prev.x - cameraX + getSize() / 2,
                                  prev.y - cameraY + getSize() / 2,
                                  wp.x - cameraX + getSize() / 2,
                                  wp.y - cameraY + getSize() / 2);
                        g.fillOval(wp.x - cameraX + getSize() / 2 - 5,
                                  wp.y - cameraY + getSize() / 2 - 5, 10, 10);
                        prev = wp;
                    }
                    // Connect back to start
                    g.drawLine(prev.x - cameraX + getSize() / 2,
                              prev.y - cameraY + getSize() / 2,
                              (int) startX - cameraX + getSize() / 2,
                              (int) startY - cameraY + getSize() / 2);
                }
                break;
        }

        g.setStroke(new BasicStroke(1));
    }

    /**
     * Check if a given entity is standing on top of this moving block.
     * Uses a tolerance that accounts for the block's movement speed.
     */
    public boolean isEntityOnTop(Entity entity) {
        Rectangle entityBounds = entity.getBounds();
        Rectangle blockBounds = getBounds();

        // Check if entity's bottom is within tolerance of block's top
        int entityBottom = entityBounds.y + entityBounds.height;
        int blockTop = blockBounds.y;

        // Use larger tolerance for faster blocks and circular movement
        // Circular blocks need extra tolerance since they move in all directions
        int tolerance = (int) Math.max(8, speed * 2 + 4);
        if (pattern == MovementPattern.CIRCULAR) {
            tolerance = (int) Math.max(12, speed * 3 + 6);
        }

        // Entity must be within tolerance of block top (can be slightly above or below)
        int verticalDiff = entityBottom - blockTop;
        if (verticalDiff >= -tolerance && verticalDiff <= tolerance) {
            // Check horizontal overlap
            int entityLeft = entityBounds.x;
            int entityRight = entityBounds.x + entityBounds.width;
            int blockLeft = blockBounds.x;
            int blockRight = blockBounds.x + blockBounds.width;

            return entityRight > blockLeft && entityLeft < blockRight;
        }
        return false;
    }

    /**
     * Apply this block's movement to an entity riding on top.
     * Uses accumulated fractional movement for smooth riding.
     * Call this from the scene's update loop for entities standing on moving blocks.
     */
    public void applyMovementToRider(Entity rider) {
        if (rider == null) return;

        double velX = getVelocityX();
        double velY = getVelocityY();

        // Track if this is a new rider or continuing rider
        if (ridingEntity != rider) {
            ridingEntity = rider;
            riderAccumX = 0;
            riderAccumY = 0;
            // Store rider's offset from block for maintaining relative position
            riderOffsetX = rider.x - this.x;
        }

        // Accumulate fractional movement
        riderAccumX += velX;
        riderAccumY += velY;

        // Apply integer portion of accumulated movement
        int moveX = (int) riderAccumX;
        int moveY = (int) riderAccumY;

        if (moveX != 0 || moveY != 0) {
            rider.x += moveX;
            rider.y += moveY;
            riderAccumX -= moveX;
            riderAccumY -= moveY;
        }

        // For circular and fast movement, snap rider to stay exactly on block top
        // This prevents drift from accumulated rounding errors
        Rectangle blockBounds = getBounds();
        Rectangle riderBounds = rider.getBounds();
        int expectedY = blockBounds.y - riderBounds.height;
        int actualY = rider.y;

        // If rider has drifted more than a few pixels vertically, correct it
        if (Math.abs(actualY - expectedY) > 3) {
            rider.y = expectedY;
            riderAccumY = 0;
        }
    }

    /**
     * Clear the riding entity reference (call when entity stops riding).
     */
    public void clearRider() {
        ridingEntity = null;
        riderAccumX = 0;
        riderAccumY = 0;
    }

    /**
     * Get the current riding entity.
     */
    public Entity getRidingEntity() {
        return ridingEntity;
    }

    @Override
    public String toString() {
        return "MovingBlockEntity{" +
                "type=" + getBlockType().name() +
                ", pattern=" + pattern +
                ", pos=(" + (int) posX + "," + (int) posY + ")" +
                ", start=(" + (int) startX + "," + (int) startY + ")" +
                ", end=(" + (int) endX + "," + (int) endY + ")" +
                ", speed=" + speed +
                "}";
    }
}
