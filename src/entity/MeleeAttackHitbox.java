package entity;

import java.awt.*;
import java.awt.geom.*;

/**
 * Represents a melee attack hitbox that supports 360-degree directional attacks.
 * The hitbox is an arc/sector shape that extends from the attacker toward the target direction.
 *
 * Features:
 * - Dynamic range based on weapon properties
 * - 360-degree rotation following mouse cursor direction
 * - Arc-based collision detection for natural melee weapon sweeps
 * - Visual rendering for debug/feedback purposes
 *
 * The hitbox is defined by:
 * - Origin point (player center/hand position)
 * - Direction angle (toward mouse cursor)
 * - Range (weapon reach distance)
 * - Arc width (angular spread of the attack, e.g., 90 degrees for sword swings)
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
    // A sword might have 90 degrees (PI/2), a dagger might have 60 degrees (PI/3)
    private double arcWidth;

    // Minimum arc width for very fast weapons (prevents too narrow hitboxes)
    private static final double MIN_ARC_WIDTH = Math.PI / 6;  // 30 degrees

    // Maximum arc width for slow powerful weapons
    private static final double MAX_ARC_WIDTH = Math.PI * 2 / 3;  // 120 degrees

    // Default arc width for standard weapons
    private static final double DEFAULT_ARC_WIDTH = Math.PI / 2;  // 90 degrees

    /**
     * Creates a melee attack hitbox.
     *
     * @param originX X coordinate of attack origin
     * @param originY Y coordinate of attack origin
     * @param angle Direction angle in radians (0 = right, PI/2 = down, PI = left, -PI/2 = up)
     * @param range Distance the attack can reach in pixels
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
     *
     * @param originX X coordinate of attack origin
     * @param originY Y coordinate of attack origin
     * @param angle Direction angle in radians
     * @param range Distance the attack can reach
     * @param attackSpeed Weapon attack speed (attacks per second)
     */
    public static MeleeAttackHitbox fromWeaponSpeed(int originX, int originY, double angle,
                                                     int range, float attackSpeed) {
        // Map attack speed to arc width:
        // Fast weapons (2.0+ attacks/sec) = narrow arc (30-45 degrees)
        // Normal weapons (1.0 attacks/sec) = standard arc (90 degrees)
        // Slow weapons (0.5 attacks/sec) = wide arc (120 degrees)
        double arcWidth;
        if (attackSpeed >= 2.0f) {
            // Fast weapons: 30-60 degree arc
            arcWidth = MIN_ARC_WIDTH + (Math.PI / 6) * Math.max(0, (2.5f - attackSpeed) / 0.5f);
        } else if (attackSpeed >= 1.0f) {
            // Normal weapons: 60-90 degree arc
            arcWidth = Math.PI / 3 + (Math.PI / 6) * (2.0f - attackSpeed);
        } else {
            // Slow weapons: 90-120 degree arc
            arcWidth = Math.PI / 2 + (Math.PI / 6) * Math.min(1.0f, (1.0f - attackSpeed) / 0.5f);
        }

        return new MeleeAttackHitbox(originX, originY, angle, range, arcWidth);
    }

    /**
     * Checks if a point is within the attack hitbox.
     *
     * @param px X coordinate of the point
     * @param py Y coordinate of the point
     * @return true if the point is within the attack arc
     */
    public boolean containsPoint(int px, int py) {
        // Calculate distance from origin to point
        double dx = px - originX;
        double dy = py - originY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Check if within range
        if (distance > range) {
            return false;
        }

        // Check if within arc angle
        double pointAngle = Math.atan2(dy, dx);
        double angleDiff = normalizeAngle(pointAngle - angle);

        return Math.abs(angleDiff) <= arcWidth / 2;
    }

    /**
     * Checks if a rectangle (entity bounds) intersects with the attack hitbox.
     * Uses multiple sample points on the rectangle for more accurate detection.
     *
     * @param bounds The rectangle to check
     * @return true if any part of the rectangle is within the attack arc
     */
    public boolean intersects(Rectangle bounds) {
        if (bounds == null) return false;

        // Quick bounding box check first
        int maxDist = range + Math.max(bounds.width, bounds.height);
        if (Math.abs(bounds.getCenterX() - originX) > maxDist ||
            Math.abs(bounds.getCenterY() - originY) > maxDist) {
            return false;
        }

        // Check center point
        if (containsPoint((int)bounds.getCenterX(), (int)bounds.getCenterY())) {
            return true;
        }

        // Check corners
        if (containsPoint(bounds.x, bounds.y)) return true;
        if (containsPoint(bounds.x + bounds.width, bounds.y)) return true;
        if (containsPoint(bounds.x, bounds.y + bounds.height)) return true;
        if (containsPoint(bounds.x + bounds.width, bounds.y + bounds.height)) return true;

        // Check edge midpoints for better coverage
        if (containsPoint(bounds.x + bounds.width / 2, bounds.y)) return true;
        if (containsPoint(bounds.x + bounds.width / 2, bounds.y + bounds.height)) return true;
        if (containsPoint(bounds.x, bounds.y + bounds.height / 2)) return true;
        if (containsPoint(bounds.x + bounds.width, bounds.y + bounds.height / 2)) return true;

        // Check if arc passes through the rectangle even if no corners are inside
        // This handles cases where the rectangle is close but corners are just outside the arc
        return checkArcIntersectsRectangle(bounds);
    }

    /**
     * Additional check for arc edges intersecting with rectangle edges.
     */
    private boolean checkArcIntersectsRectangle(Rectangle bounds) {
        // Calculate the two edge angles of the arc
        double leftEdgeAngle = angle - arcWidth / 2;
        double rightEdgeAngle = angle + arcWidth / 2;

        // Check points along both arc edges
        for (int i = 1; i <= 3; i++) {
            double dist = range * i / 4.0;

            // Left edge point
            int lx = originX + (int)(Math.cos(leftEdgeAngle) * dist);
            int ly = originY + (int)(Math.sin(leftEdgeAngle) * dist);
            if (bounds.contains(lx, ly)) return true;

            // Right edge point
            int rx = originX + (int)(Math.cos(rightEdgeAngle) * dist);
            int ry = originY + (int)(Math.sin(rightEdgeAngle) * dist);
            if (bounds.contains(rx, ry)) return true;
        }

        // Check arc outer edge (the curved part at max range)
        for (int i = -2; i <= 2; i++) {
            double edgeAngle = angle + (arcWidth / 4) * i;
            int ex = originX + (int)(Math.cos(edgeAngle) * range);
            int ey = originY + (int)(Math.sin(edgeAngle) * range);
            if (bounds.contains(ex, ey)) return true;
        }

        return false;
    }

    /**
     * Normalizes an angle to the range [-PI, PI].
     */
    private double normalizeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    /**
     * Gets a bounding rectangle that contains the entire attack arc.
     * Useful for broad-phase collision detection.
     *
     * @return Rectangle containing the attack arc
     */
    public Rectangle getBoundingBox() {
        // Calculate the extreme points of the arc
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

        // Account for arcs that cross cardinal directions
        if (isAngleInArc(0)) maxX = Math.max(maxX, originX + range);
        if (isAngleInArc(Math.PI / 2)) maxY = Math.max(maxY, originY + range);
        if (isAngleInArc(Math.PI) || isAngleInArc(-Math.PI)) minX = Math.min(minX, originX - range);
        if (isAngleInArc(-Math.PI / 2)) minY = Math.min(minY, originY - range);

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Checks if a given angle is within the attack arc.
     */
    private boolean isAngleInArc(double testAngle) {
        double diff = normalizeAngle(testAngle - angle);
        return Math.abs(diff) <= arcWidth / 2;
    }

    /**
     * Draws the attack hitbox for debugging/visual feedback.
     *
     * @param g Graphics context
     * @param fillColor Color for the filled area
     * @param borderColor Color for the border
     */
    public void draw(Graphics2D g, Color fillColor, Color borderColor) {
        // Create arc shape
        Arc2D arc = new Arc2D.Double();
        arc.setArcByCenter(
            originX, originY, range,
            -Math.toDegrees(angle + arcWidth / 2),  // Start angle (inverted for screen coords)
            Math.toDegrees(arcWidth),                // Arc extent
            Arc2D.PIE
        );

        // Fill
        if (fillColor != null) {
            g.setColor(fillColor);
            g.fill(arc);
        }

        // Border
        if (borderColor != null) {
            g.setColor(borderColor);
            g.setStroke(new BasicStroke(2));
            g.draw(arc);
        }

        // Draw direction indicator (line from origin in attack direction)
        g.setColor(borderColor != null ? borderColor : Color.RED);
        int endX = originX + (int)(Math.cos(angle) * range);
        int endY = originY + (int)(Math.sin(angle) * range);
        g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                     0, new float[]{5, 5}, 0));
        g.drawLine(originX, originY, endX, endY);
    }

    // ==================== Getters ====================

    public int getOriginX() { return originX; }
    public int getOriginY() { return originY; }
    public double getAngle() { return angle; }
    public int getRange() { return range; }
    public double getArcWidth() { return arcWidth; }

    /**
     * Gets the arc width in degrees.
     */
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
