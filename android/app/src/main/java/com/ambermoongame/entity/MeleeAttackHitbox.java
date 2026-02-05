package com.ambermoongame.entity;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Represents a melee attack hitbox that supports 360-degree directional attacks.
 * The hitbox is an arc/sector shape that extends from the attacker toward the target direction.
 * Equivalent to entity/MeleeAttackHitbox.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.Rectangle      -> android.graphics.Rect
 * - java.awt.Graphics2D     -> android.graphics.Canvas
 * - java.awt.Color           -> int (Paint.setColor)
 * - java.awt.geom.Arc2D      -> Canvas.drawArc() with RectF
 * - java.awt.BasicStroke      -> Paint.setStrokeWidth() + DashPathEffect
 * - Rectangle.intersects()    -> Rect.intersects(Rect, Rect)
 * - Rectangle.contains()     -> Rect.contains(x, y)
 * - Rectangle.getCenterX()   -> rect.centerX()
 */
public class MeleeAttackHitbox {

    // Origin of the attack (typically player center or hand position)
    private int originX;
    private int originY;

    // Direction of the attack in radians
    private double angle;

    // Attack range (distance the weapon can reach)
    private int range;

    // Arc width in radians (how wide the attack sweep is)
    private double arcWidth;

    private static final double MIN_ARC_WIDTH = Math.PI / 6;     // 30 degrees
    private static final double MAX_ARC_WIDTH = Math.PI * 2 / 3; // 120 degrees
    private static final double DEFAULT_ARC_WIDTH = Math.PI / 2;  // 90 degrees

    /**
     * Creates a melee attack hitbox.
     *
     * @param originX  X coordinate of attack origin
     * @param originY  Y coordinate of attack origin
     * @param angle    Direction angle in radians (0 = right, PI/2 = down, PI = left, -PI/2 = up)
     * @param range    Distance the attack can reach in pixels
     * @param arcWidth Angular width of the attack arc in radians
     */
    public MeleeAttackHitbox(int originX, int originY, double angle, int range, double arcWidth) {
        this.originX = originX;
        this.originY = originY;
        this.angle = angle;
        this.range = range;
        this.arcWidth = Math.max(MIN_ARC_WIDTH, Math.min(MAX_ARC_WIDTH, arcWidth));
    }

    /**
     * Creates a melee attack hitbox with default arc width.
     */
    public MeleeAttackHitbox(int originX, int originY, double angle, int range) {
        this(originX, originY, angle, range, DEFAULT_ARC_WIDTH);
    }

    /**
     * Creates a melee attack hitbox based on weapon attack speed.
     * Faster weapons have narrower arcs, slower weapons have wider sweeps.
     */
    public static MeleeAttackHitbox fromWeaponSpeed(int originX, int originY, double angle,
                                                     int range, float attackSpeed) {
        double arcWidth;
        if (attackSpeed >= 2.0f) {
            arcWidth = MIN_ARC_WIDTH + (Math.PI / 6) * Math.max(0, (2.5f - attackSpeed) / 0.5f);
        } else if (attackSpeed >= 1.0f) {
            arcWidth = Math.PI / 3 + (Math.PI / 6) * (2.0f - attackSpeed);
        } else {
            arcWidth = Math.PI / 2 + (Math.PI / 6) * Math.min(1.0f, (1.0f - attackSpeed) / 0.5f);
        }
        return new MeleeAttackHitbox(originX, originY, angle, range, arcWidth);
    }

    /**
     * Checks if a point is within the attack hitbox.
     */
    public boolean containsPoint(int px, int py) {
        double dx = px - originX;
        double dy = py - originY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > range) {
            return false;
        }

        double pointAngle = Math.atan2(dy, dx);
        double angleDiff = normalizeAngle(pointAngle - angle);

        return Math.abs(angleDiff) <= arcWidth / 2;
    }

    /**
     * Checks if a rectangle (entity bounds) intersects with the attack hitbox.
     * Uses multiple sample points on the rectangle for more accurate detection.
     *
     * @param bounds The Rect to check
     * @return true if any part of the rectangle is within the attack arc
     */
    public boolean intersects(Rect bounds) {
        if (bounds == null) return false;

        // Quick bounding box check first
        int maxDist = range + Math.max(bounds.width(), bounds.height());
        if (Math.abs(bounds.centerX() - originX) > maxDist ||
            Math.abs(bounds.centerY() - originY) > maxDist) {
            return false;
        }

        // Check center point
        if (containsPoint(bounds.centerX(), bounds.centerY())) {
            return true;
        }

        // Check corners
        if (containsPoint(bounds.left, bounds.top)) return true;
        if (containsPoint(bounds.right, bounds.top)) return true;
        if (containsPoint(bounds.left, bounds.bottom)) return true;
        if (containsPoint(bounds.right, bounds.bottom)) return true;

        // Check edge midpoints
        int midX = bounds.left + bounds.width() / 2;
        int midY = bounds.top + bounds.height() / 2;
        if (containsPoint(midX, bounds.top)) return true;
        if (containsPoint(midX, bounds.bottom)) return true;
        if (containsPoint(bounds.left, midY)) return true;
        if (containsPoint(bounds.right, midY)) return true;

        // Check arc edges against rectangle
        return checkArcIntersectsRectangle(bounds);
    }

    /**
     * Additional check for arc edges intersecting with rectangle edges.
     */
    private boolean checkArcIntersectsRectangle(Rect bounds) {
        double leftEdgeAngle = angle - arcWidth / 2;
        double rightEdgeAngle = angle + arcWidth / 2;

        for (int i = 1; i <= 3; i++) {
            double dist = range * i / 4.0;

            int lx = originX + (int)(Math.cos(leftEdgeAngle) * dist);
            int ly = originY + (int)(Math.sin(leftEdgeAngle) * dist);
            if (bounds.contains(lx, ly)) return true;

            int rx = originX + (int)(Math.cos(rightEdgeAngle) * dist);
            int ry = originY + (int)(Math.sin(rightEdgeAngle) * dist);
            if (bounds.contains(rx, ry)) return true;
        }

        for (int i = -2; i <= 2; i++) {
            double edgeAngle = angle + (arcWidth / 4) * i;
            int ex = originX + (int)(Math.cos(edgeAngle) * range);
            int ey = originY + (int)(Math.sin(edgeAngle) * range);
            if (bounds.contains(ex, ey)) return true;
        }

        return false;
    }

    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    /**
     * Gets a bounding rectangle that contains the entire attack arc.
     * Useful for broad-phase collision detection.
     */
    public Rect getBoundingBox() {
        double leftAngle = angle - arcWidth / 2;
        double rightAngle = angle + arcWidth / 2;

        int x1 = originX + (int)(Math.cos(leftAngle) * range);
        int y1 = originY + (int)(Math.sin(leftAngle) * range);
        int x2 = originX + (int)(Math.cos(rightAngle) * range);
        int y2 = originY + (int)(Math.sin(rightAngle) * range);
        int x3 = originX + (int)(Math.cos(angle) * range);
        int y3 = originY + (int)(Math.sin(angle) * range);

        int minX = Math.min(originX, Math.min(x1, Math.min(x2, x3)));
        int maxX = Math.max(originX, Math.max(x1, Math.max(x2, x3)));
        int minY = Math.min(originY, Math.min(y1, Math.min(y2, y3)));
        int maxY = Math.max(originY, Math.max(y1, Math.max(y2, y3)));

        if (isAngleInArc(0)) maxX = Math.max(maxX, originX + range);
        if (isAngleInArc(Math.PI / 2)) maxY = Math.max(maxY, originY + range);
        if (isAngleInArc(Math.PI) || isAngleInArc(-Math.PI)) minX = Math.min(minX, originX - range);
        if (isAngleInArc(-Math.PI / 2)) minY = Math.min(minY, originY - range);

        return new Rect(minX, minY, maxX, maxY);
    }

    private boolean isAngleInArc(double testAngle) {
        double diff = normalizeAngle(testAngle - angle);
        return Math.abs(diff) <= arcWidth / 2;
    }

    /**
     * Draws the attack hitbox for debugging/visual feedback.
     *
     * @param canvas      Canvas to draw on
     * @param fillColor   Color for the filled area (0 to skip fill)
     * @param borderColor Color for the border (0 to skip border)
     */
    public void draw(Canvas canvas, int fillColor, int borderColor) {
        // Arc bounding rect
        RectF arcRect = new RectF(
            originX - range, originY - range,
            originX + range, originY + range
        );

        // Android drawArc uses degrees, measured counter-clockwise from 3 o'clock
        // AWT uses the same convention but with inverted Y, so we negate the angle
        float startAngle = (float) Math.toDegrees(-angle - arcWidth / 2);
        float sweepAngle = (float) Math.toDegrees(arcWidth);

        // Fill
        if (fillColor != 0) {
            Paint fillPaint = new Paint();
            fillPaint.setColor(fillColor);
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setAntiAlias(true);
            canvas.drawArc(arcRect, startAngle, sweepAngle, true, fillPaint);
        }

        // Border
        if (borderColor != 0) {
            Paint borderPaint = new Paint();
            borderPaint.setColor(borderColor);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(2);
            borderPaint.setAntiAlias(true);
            canvas.drawArc(arcRect, startAngle, sweepAngle, true, borderPaint);
        }

        // Direction indicator (dashed line)
        int lineColor = borderColor != 0 ? borderColor : 0xFFFF0000;
        Paint dashPaint = new Paint();
        dashPaint.setColor(lineColor);
        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setStrokeWidth(1);
        dashPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
        dashPaint.setAntiAlias(true);

        int endX = originX + (int)(Math.cos(angle) * range);
        int endY = originY + (int)(Math.sin(angle) * range);
        canvas.drawLine(originX, originY, endX, endY, dashPaint);
    }

    // ==================== Getters ====================

    public int getOriginX() { return originX; }
    public int getOriginY() { return originY; }
    public double getAngle() { return angle; }
    public int getRange() { return range; }
    public double getArcWidth() { return arcWidth; }

    public double getArcWidthDegrees() {
        return Math.toDegrees(arcWidth);
    }

    /**
     * Updates the origin position (for following entity movement during attack).
     */
    public void updateOrigin(int x, int y) {
        this.originX = x;
        this.originY = y;
    }

    @Override
    public String toString() {
        return String.format("MeleeAttackHitbox[origin=(%d,%d), angle=%.1f°, range=%d, arc=%.1f°]",
            originX, originY, Math.toDegrees(angle), range, Math.toDegrees(arcWidth));
    }
}
