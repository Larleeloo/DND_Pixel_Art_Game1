package com.ambermoongame.block;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.ambermoongame.entity.Entity;
import com.ambermoongame.input.TouchInputManager;

import java.util.ArrayList;
import java.util.List;

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
 * Equivalent to block/MovingBlockEntity.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.Graphics/Graphics2D -> android.graphics.Canvas
 * - java.awt.Rectangle           -> android.graphics.Rect
 * - java.awt.Color               -> android.graphics.Color / int
 * - java.awt.BasicStroke          -> Paint + DashPathEffect
 * - java.awt.Point               -> android.graphics.Point
 * - InputManager                  -> TouchInputManager
 * - g2d.drawLine()               -> canvas.drawLine()
 * - g2d.fillOval()               -> canvas.drawCircle()
 * - g2d.drawOval()               -> canvas.drawOval() with RectF
 */
public class MovingBlockEntity extends BlockEntity {

    // Movement pattern constants (replaced enum to avoid D8 crash)
    public static final int PATTERN_HORIZONTAL = 0;
    public static final int PATTERN_VERTICAL = 1;
    public static final int PATTERN_CIRCULAR = 2;
    public static final int PATTERN_PATH = 3;

    // Movement state
    private int pattern;
    private double startX, startY;
    private double endX, endY;
    private double speed;
    private int pauseTime;
    private int currentPause;
    private boolean movingForward;

    // Position tracking (double precision for smooth movement)
    private double posX, posY;
    private double prevX, prevY;

    // Circular movement
    private double angle;
    private double radius;
    private double centerX, centerY;

    // Waypoint path
    private List<Point> waypoints;
    private int currentWaypointIndex;

    // Entity riding tracking
    private Entity ridingEntity;
    private double riderOffsetX;
    private double riderAccumX;
    private double riderAccumY;

    /**
     * Creates a moving block with linear movement between two positions.
     */
    public MovingBlockEntity(int gridX, int gridY, int blockType, boolean useGridCoords,
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
        this.pauseTime = 30;
        this.currentPause = 0;
        this.movingForward = true;

        if (Math.abs(endX - startX) > Math.abs(endY - startY)) {
            this.pattern = PATTERN_HORIZONTAL;
        } else {
            this.pattern = PATTERN_VERTICAL;
        }

        this.waypoints = new ArrayList<>();
    }

    /**
     * Creates a moving block with specified movement pattern.
     */
    public MovingBlockEntity(int gridX, int gridY, int blockType, boolean useGridCoords,
                             int pattern, double speed) {
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

    public void setEndPosition(int endGridX, int endGridY, boolean useGridCoords) {
        if (useGridCoords) {
            this.endX = endGridX * BlockRegistry.BLOCK_SIZE;
            this.endY = endGridY * BlockRegistry.BLOCK_SIZE;
        } else {
            this.endX = endGridX;
            this.endY = endGridY;
        }
    }

    public void setCircularMovement(int centerGridX, int centerGridY, boolean useGridCoords, double radius) {
        this.pattern = PATTERN_CIRCULAR;
        if (useGridCoords) {
            this.centerX = centerGridX * BlockRegistry.BLOCK_SIZE;
            this.centerY = centerGridY * BlockRegistry.BLOCK_SIZE;
        } else {
            this.centerX = centerGridX;
            this.centerY = centerGridY;
        }
        this.radius = radius;
        this.angle = 0;
        this.posX = centerX + radius;
        this.posY = centerY;
        this.x = (int) posX;
        this.y = (int) posY;
    }

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

        if (pattern != PATTERN_PATH) {
            pattern = PATTERN_PATH;
            currentWaypointIndex = 0;
        }
    }

    public void setPauseTime(int frames) { this.pauseTime = frames; }
    public void setSpeed(double speed) { this.speed = speed; }

    public double getVelocityX() { return posX - prevX; }
    public double getVelocityY() { return posY - prevY; }
    public int getPattern() { return pattern; }
    public double getSpeed() { return speed; }
    public double getStartX() { return startX; }
    public double getStartY() { return startY; }
    public double getEndX() { return endX; }
    public double getEndY() { return endY; }
    public double getCenterXPos() { return centerX; }
    public double getCenterYPos() { return centerY; }
    public double getRadius() { return radius; }
    public int getPauseTime() { return pauseTime; }
    public List<Point> getWaypoints() { return waypoints; }

    @Override
    public void update(TouchInputManager input) {
        if (isBroken()) return;

        prevX = posX;
        prevY = posY;

        if (currentPause > 0) {
            currentPause--;
            return;
        }

        switch (pattern) {
            case PATTERN_HORIZONTAL:
            case PATTERN_VERTICAL:
                updateLinearMovement();
                break;
            case PATTERN_CIRCULAR:
                updateCircularMovement();
                break;
            case PATTERN_PATH:
                updatePathMovement();
                break;
        }

        this.x = (int) posX;
        this.y = (int) posY;
    }

    private void updateLinearMovement() {
        double targetX = movingForward ? endX : startX;
        double targetY = movingForward ? endY : startY;

        double dx = targetX - posX;
        double dy = targetY - posY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= speed) {
            posX = targetX;
            posY = targetY;
            movingForward = !movingForward;
            currentPause = pauseTime;
        } else {
            double ratio = speed / distance;
            posX += dx * ratio;
            posY += dy * ratio;
        }
    }

    private void updateCircularMovement() {
        double angularSpeed = speed / radius;
        angle += angularSpeed;
        if (angle >= Math.PI * 2) angle -= Math.PI * 2;

        posX = centerX + Math.cos(angle) * radius;
        posY = centerY + Math.sin(angle) * radius;
    }

    private void updatePathMovement() {
        if (waypoints.isEmpty()) return;

        Point target = waypoints.get(currentWaypointIndex);
        double dx = target.x - posX;
        double dy = target.y - posY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= speed) {
            posX = target.x;
            posY = target.y;
            currentWaypointIndex++;
            if (currentWaypointIndex >= waypoints.size()) {
                currentWaypointIndex = 0;
            }
            currentPause = pauseTime;
        } else {
            double ratio = speed / distance;
            posX += dx * ratio;
            posY += dy * ratio;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    /**
     * Draw debug visualization of the movement path.
     */
    public void drawDebugPath(Canvas canvas, int cameraX, int cameraY) {
        Paint pathPaint = new Paint();
        pathPaint.setColor(Color.argb(100, 255, 255, 0));
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(2);
        pathPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
        pathPaint.setAntiAlias(true);

        Paint dotPaint = new Paint();
        dotPaint.setColor(Color.argb(100, 255, 255, 0));
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);

        int sx = (int) startX - cameraX;
        int sy = (int) startY - cameraY;
        int halfSize = getSize() / 2;

        switch (pattern) {
            case PATTERN_HORIZONTAL:
            case PATTERN_VERTICAL:
                int ex = (int) endX - cameraX;
                int ey = (int) endY - cameraY;
                canvas.drawLine(sx + halfSize, sy + halfSize, ex + halfSize, ey + halfSize, pathPaint);
                canvas.drawCircle(sx + halfSize, sy + halfSize, 5, dotPaint);
                canvas.drawCircle(ex + halfSize, ey + halfSize, 5, dotPaint);
                break;

            case PATTERN_CIRCULAR:
                int cx = (int) centerX - cameraX;
                int cy = (int) centerY - cameraY;
                canvas.drawCircle(cx, cy, (float) radius, pathPaint);
                canvas.drawCircle(cx, cy, 5, dotPaint);
                break;

            case PATTERN_PATH:
                if (!waypoints.isEmpty()) {
                    Point prev = new Point((int) startX, (int) startY);
                    for (Point wp : waypoints) {
                        canvas.drawLine(
                            prev.x - cameraX + halfSize, prev.y - cameraY + halfSize,
                            wp.x - cameraX + halfSize, wp.y - cameraY + halfSize,
                            pathPaint);
                        canvas.drawCircle(wp.x - cameraX + halfSize, wp.y - cameraY + halfSize, 5, dotPaint);
                        prev = wp;
                    }
                    // Connect back to start
                    canvas.drawLine(
                        prev.x - cameraX + halfSize, prev.y - cameraY + halfSize,
                        (int) startX - cameraX + halfSize, (int) startY - cameraY + halfSize,
                        pathPaint);
                }
                break;
        }
    }

    /**
     * Check if an entity is standing on top of this moving block.
     */
    public boolean isEntityOnTop(Entity entity) {
        Rect entityBounds = entity.getBounds();
        Rect blockBounds = getBounds();

        int entityBottom = entityBounds.bottom;
        int blockTop = blockBounds.top;

        int tolerance = (int) Math.max(8, speed * 2 + 4);
        if (pattern == PATTERN_CIRCULAR) {
            tolerance = (int) Math.max(12, speed * 3 + 6);
        }

        int verticalDiff = entityBottom - blockTop;
        if (verticalDiff >= -tolerance && verticalDiff <= tolerance) {
            return entityBounds.right > blockBounds.left && entityBounds.left < blockBounds.right;
        }
        return false;
    }

    /**
     * Apply this block's movement to an entity riding on top.
     */
    public void applyMovementToRider(Entity rider) {
        if (rider == null) return;

        double velX = getVelocityX();
        double velY = getVelocityY();

        if (ridingEntity != rider) {
            ridingEntity = rider;
            riderAccumX = 0;
            riderAccumY = 0;
            riderOffsetX = rider.x - this.x;
        }

        riderAccumX += velX;
        riderAccumY += velY;

        int moveX = (int) riderAccumX;
        int moveY = (int) riderAccumY;

        if (moveX != 0 || moveY != 0) {
            rider.x += moveX;
            rider.y += moveY;
            riderAccumX -= moveX;
            riderAccumY -= moveY;
        }

        // Snap correction for circular/fast movement
        Rect blockBounds = getBounds();
        Rect riderBounds = rider.getBounds();
        int expectedY = blockBounds.top - riderBounds.height();
        if (Math.abs(rider.y - expectedY) > 3) {
            rider.y = expectedY;
            riderAccumY = 0;
        }
    }

    public void clearRider() {
        ridingEntity = null;
        riderAccumX = 0;
        riderAccumY = 0;
    }

    public Entity getRidingEntity() { return ridingEntity; }

    private static String getPatternName(int pattern) {
        switch (pattern) {
            case PATTERN_HORIZONTAL: return "HORIZONTAL";
            case PATTERN_VERTICAL: return "VERTICAL";
            case PATTERN_CIRCULAR: return "CIRCULAR";
            case PATTERN_PATH: return "PATH";
            default: return "UNKNOWN";
        }
    }

    @Override
    public String toString() {
        return "MovingBlockEntity{type=" + BlockType.getName(getBlockType())
                + ", pattern=" + getPatternName(pattern)
                + ", pos=(" + (int) posX + "," + (int) posY + ")"
                + ", start=(" + (int) startX + "," + (int) startY + ")"
                + ", end=(" + (int) endX + "," + (int) endY + ")"
                + ", speed=" + speed + "}";
    }
}
