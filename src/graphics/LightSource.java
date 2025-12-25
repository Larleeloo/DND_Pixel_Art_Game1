package graphics;
import animation.*;
import block.*;

import java.awt.*;

/**
 * Represents a light source in the game world.
 * Light sources illuminate areas around them, creating bright spots in the darkness.
 */
class LightSource {

    private double x, y;           // Position in world coordinates
    private double radius;          // Radius of full brightness
    private double falloffRadius;   // Radius where light fades to zero
    private Color color;            // Light color
    private double intensity;       // Light intensity (0.0 - 1.0)
    private boolean enabled;        // Whether the light is currently on

    // Optional flicker effect
    private boolean flickerEnabled;
    private double flickerAmount;   // How much the intensity can vary (0.0 - 1.0)
    private double flickerSpeed;    // How fast the flicker changes
    private double flickerOffset;   // Random offset for flicker timing

    /**
     * Create a light source with default white color.
     * @param x World x position
     * @param y World y position
     * @param radius Radius of full brightness
     */
    public LightSource(double x, double y, double radius) {
        this(x, y, radius, Color.WHITE);
    }

    /**
     * Create a light source with a specific color.
     * @param x World x position
     * @param y World y position
     * @param radius Radius of full brightness
     * @param color Light color
     */
    public LightSource(double x, double y, double radius, Color color) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.falloffRadius = radius * 2.0; // Default falloff is 2x the radius
        this.color = color;
        this.intensity = 1.0;
        this.enabled = true;
        this.flickerEnabled = false;
        this.flickerAmount = 0.2;
        this.flickerSpeed = 5.0;
        this.flickerOffset = Math.random() * Math.PI * 2;
    }

    /**
     * Get the current effective intensity, including flicker effects.
     * @param time Current game time in seconds
     * @return Effective intensity value
     */
    public double getEffectiveIntensity(double time) {
        if (!enabled) return 0.0;

        double effectiveIntensity = intensity;

        if (flickerEnabled) {
            // Create a flickering effect using noise-like sine waves
            double flicker = Math.sin(time * flickerSpeed + flickerOffset) * 0.5 + 0.5;
            flicker += Math.sin(time * flickerSpeed * 2.3 + flickerOffset * 1.7) * 0.3;
            flicker = Math.max(0, Math.min(1, flicker / 1.3));

            effectiveIntensity = intensity * (1.0 - flickerAmount + flickerAmount * flicker);
        }

        return effectiveIntensity;
    }

    /**
     * Calculate the light contribution at a specific point.
     * @param px Point x coordinate
     * @param py Point y coordinate
     * @param time Current game time for flicker effects
     * @return Light intensity at the point (0.0 - 1.0)
     */
    public double getLightAt(double px, double py, double time) {
        if (!enabled) return 0.0;

        double dx = px - x;
        double dy = py - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= radius) {
            // Full intensity within the inner radius
            return getEffectiveIntensity(time);
        } else if (distance <= falloffRadius) {
            // Linear falloff between inner and outer radius
            double t = (distance - radius) / (falloffRadius - radius);
            return getEffectiveIntensity(time) * (1.0 - t);
        }

        return 0.0;
    }

    // Getters and setters

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }

    public double getFalloffRadius() { return falloffRadius; }
    public void setFalloffRadius(double falloffRadius) { this.falloffRadius = falloffRadius; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public double getIntensity() { return intensity; }
    public void setIntensity(double intensity) {
        this.intensity = Math.max(0, Math.min(1, intensity));
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isFlickerEnabled() { return flickerEnabled; }
    public void setFlickerEnabled(boolean flickerEnabled) { this.flickerEnabled = flickerEnabled; }

    public double getFlickerAmount() { return flickerAmount; }
    public void setFlickerAmount(double flickerAmount) {
        this.flickerAmount = Math.max(0, Math.min(1, flickerAmount));
    }

    public double getFlickerSpeed() { return flickerSpeed; }
    public void setFlickerSpeed(double flickerSpeed) { this.flickerSpeed = flickerSpeed; }

    /**
     * Enable flickering with specified parameters.
     * @param amount How much intensity varies (0.0-1.0)
     * @param speed How fast the flicker occurs
     */
    public void enableFlicker(double amount, double speed) {
        this.flickerEnabled = true;
        this.flickerAmount = Math.max(0, Math.min(1, amount));
        this.flickerSpeed = speed;
    }

    /**
     * Disable flickering effect.
     */
    public void disableFlicker() {
        this.flickerEnabled = false;
    }
}
