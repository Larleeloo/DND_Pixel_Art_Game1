package graphics;
import animation.*;
import block.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the day/night cycle and lighting effects.
 * Renders a darkness overlay with light sources cutting through it.
 */
public class LightingSystem {

    // Light sources in the scene
    private List<LightSource> lightSources;

    // Day/night state
    private boolean isNight;
    private double nightDarkness;  // How dark the night is (0.0 = no darkness, 1.0 = maximum darkness)

    // Ambient lighting
    private Color ambientColor;    // Color of the ambient darkness
    private double ambientLevel;   // Base ambient light level (prevents total darkness)

    // Timing for effects
    private double gameTime;       // Game time in seconds for flicker effects

    // Rendering buffer for efficient lighting
    private BufferedImage lightBuffer;
    private int bufferWidth, bufferHeight;
    private int renderScale;       // Scale down the light buffer for performance

    // Screen dimensions
    private int screenWidth;
    private int screenHeight;

    /**
     * Create a new lighting system.
     * @param screenWidth Width of the game screen
     * @param screenHeight Height of the game screen
     */
    public LightingSystem(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.lightSources = new ArrayList<>();
        this.isNight = false;
        this.nightDarkness = 0.55;  // Default 55% darkness at night (visible but atmospheric)
        this.ambientColor = new Color(15, 15, 50);  // Dark blue-ish night color
        this.ambientLevel = 0.25;   // 25% ambient light even in total darkness
        this.gameTime = 0;
        this.renderScale = 4;       // Render at 1/4 resolution for performance

        // Create the light buffer at reduced resolution
        this.bufferWidth = screenWidth / renderScale;
        this.bufferHeight = screenHeight / renderScale;
        this.lightBuffer = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Update the lighting system.
     * @param deltaTime Time since last update in seconds
     */
    public void update(double deltaTime) {
        gameTime += deltaTime;
    }

    /**
     * Add a light source to the system.
     * @param light The light source to add
     */
    public void addLightSource(LightSource light) {
        lightSources.add(light);
    }

    /**
     * Remove a light source from the system.
     * @param light The light source to remove
     */
    public void removeLightSource(LightSource light) {
        lightSources.remove(light);
    }

    /**
     * Clear all light sources.
     */
    public void clearLightSources() {
        lightSources.clear();
    }

    /**
     * Get all light sources.
     */
    public List<LightSource> getLightSources() {
        return lightSources;
    }

    /**
     * Set whether it's night time.
     */
    public void setNight(boolean night) {
        this.isNight = night;
    }

    /**
     * Toggle between day and night.
     */
    public void toggleDayNight() {
        this.isNight = !this.isNight;
    }

    /**
     * Check if it's currently night.
     */
    public boolean isNight() {
        return isNight;
    }

    /**
     * Set the darkness level for night (0.0 - 1.0).
     */
    public void setNightDarkness(double darkness) {
        this.nightDarkness = Math.max(0, Math.min(1, darkness));
    }

    /**
     * Set the ambient light level (minimum brightness, 0.0 - 1.0).
     */
    public void setAmbientLevel(double level) {
        this.ambientLevel = Math.max(0, Math.min(1, level));
    }

    /**
     * Set the ambient/darkness color.
     */
    public void setAmbientColor(Color color) {
        this.ambientColor = color;
    }

    /**
     * Render the lighting overlay.
     * Call this after drawing all game entities but before UI.
     * @param g Graphics context
     * @param cameraX Camera X offset (for scrolling)
     * @param cameraY Camera Y offset (for scrolling)
     */
    public void render(Graphics2D g, double cameraX, double cameraY) {
        if (!isNight) {
            return; // No lighting needed during the day
        }

        // Clear the light buffer with the darkness color
        Graphics2D bufferG = lightBuffer.createGraphics();

        // Set up rendering hints for smooth gradients
        bufferG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bufferG.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        // First, clear the buffer to fully transparent using Src composite
        // This ensures proper alpha blending when the buffer is drawn to screen
        bufferG.setComposite(AlphaComposite.Src);
        bufferG.setColor(new Color(0, 0, 0, 0));
        bufferG.fillRect(0, 0, bufferWidth, bufferHeight);

        // Reset to SrcOver for normal drawing
        bufferG.setComposite(AlphaComposite.SrcOver);

        // Calculate the alpha for the darkness overlay
        // Adjusted by ambient level so it's never completely dark
        int darknessAlpha = (int) ((nightDarkness * (1.0 - ambientLevel)) * 255);
        Color darkColor = new Color(ambientColor.getRed(), ambientColor.getGreen(),
                                    ambientColor.getBlue(), darknessAlpha);

        // Fill with darkness
        bufferG.setColor(darkColor);
        bufferG.fillRect(0, 0, bufferWidth, bufferHeight);

        // Now "subtract" light from the darkness using DST_OUT composite
        // This will cut holes in the darkness where lights are
        bufferG.setComposite(AlphaComposite.DstOut);

        // Render each light source
        for (LightSource light : lightSources) {
            if (!light.isEnabled()) continue;

            // Convert world coordinates to screen coordinates
            double screenX = (light.getX() - cameraX) / renderScale;
            double screenY = (light.getY() - cameraY) / renderScale;

            // Calculate scaled radii
            double innerRadius = light.getRadius() / renderScale;
            double outerRadius = light.getFalloffRadius() / renderScale;

            // Ensure outerRadius is always at least as large as innerRadius
            if (outerRadius < innerRadius) {
                outerRadius = innerRadius * 2.0;
            }

            // Skip if completely off screen
            if (screenX + outerRadius < 0 || screenX - outerRadius > bufferWidth ||
                screenY + outerRadius < 0 || screenY - outerRadius > bufferHeight) {
                continue;
            }

            // Get light properties
            double effectiveIntensity = light.getEffectiveIntensity(gameTime);
            Color lightColor = light.getColor();

            // Create a radial gradient for the light
            // The gradient goes from full intensity at center to zero at the edge
            float[] fractions = {0.0f, (float)(innerRadius / outerRadius), 1.0f};

            // For DST_OUT, white = full removal of darkness, black = no removal
            int centerAlpha = (int) (effectiveIntensity * 255);
            Color[] colors = {
                new Color(255, 255, 255, centerAlpha),  // Full light at center
                new Color(255, 255, 255, centerAlpha),  // Full light up to inner radius
                new Color(255, 255, 255, 0)             // No light at outer radius
            };

            // Create the gradient paint
            RadialGradientPaint gradient = new RadialGradientPaint(
                (float) screenX, (float) screenY,
                (float) outerRadius,
                fractions, colors,
                MultipleGradientPaint.CycleMethod.NO_CYCLE
            );

            bufferG.setPaint(gradient);
            bufferG.fillOval(
                (int) (screenX - outerRadius),
                (int) (screenY - outerRadius),
                (int) (outerRadius * 2),
                (int) (outerRadius * 2)
            );
        }

        bufferG.dispose();

        // Draw the light buffer scaled up to screen size
        g.drawImage(lightBuffer, 0, 0, screenWidth, screenHeight, null);
    }

    /**
     * Render without camera offset (for non-scrolling scenes).
     */
    public void render(Graphics2D g) {
        render(g, 0, 0);
    }

    /**
     * Get the current game time (for external use with flicker sync).
     */
    public double getGameTime() {
        return gameTime;
    }

    /**
     * Create a torch-like light source with orange color and flickering.
     */
    public static LightSource createTorchLight(double x, double y) {
        LightSource torch = new LightSource(x, y, 80, new Color(255, 200, 100));
        torch.setFalloffRadius(180);
        torch.enableFlicker(0.15, 8.0);
        return torch;
    }

    /**
     * Create a campfire-like light source with warm orange color and flickering.
     */
    public static LightSource createCampfireLight(double x, double y) {
        LightSource campfire = new LightSource(x, y, 120, new Color(255, 150, 50));
        campfire.setFalloffRadius(280);
        campfire.enableFlicker(0.25, 6.0);
        return campfire;
    }

    /**
     * Create a lantern-like light source with steady yellow light.
     */
    public static LightSource createLanternLight(double x, double y) {
        LightSource lantern = new LightSource(x, y, 100, new Color(255, 240, 180));
        lantern.setFalloffRadius(200);
        return lantern;
    }

    /**
     * Create a magical light source with blue/purple color.
     */
    public static LightSource createMagicLight(double x, double y) {
        LightSource magic = new LightSource(x, y, 90, new Color(150, 150, 255));
        magic.setFalloffRadius(180);
        magic.enableFlicker(0.1, 3.0);
        return magic;
    }

    /**
     * Create a large area light (like moonlight through a window).
     */
    public static LightSource createAreaLight(double x, double y, double radius) {
        LightSource area = new LightSource(x, y, radius, new Color(200, 200, 255));
        area.setFalloffRadius(radius * 1.5);
        area.setIntensity(0.6);
        return area;
    }
}
