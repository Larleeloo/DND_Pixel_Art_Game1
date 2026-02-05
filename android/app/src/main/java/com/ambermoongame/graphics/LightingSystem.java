package com.ambermoongame.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;

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
    private int ambientColor;      // Color of the ambient darkness (Android color int)
    private double ambientLevel;   // Base ambient light level (prevents total darkness)

    // Timing for effects
    private double gameTime;       // Game time in seconds for flicker effects

    // Rendering buffer for efficient lighting
    private Bitmap lightBuffer;
    private Canvas bufferCanvas;
    private int bufferWidth, bufferHeight;
    private int renderScale;       // Scale down the light buffer for performance

    // Screen dimensions
    private int screenWidth;
    private int screenHeight;

    // Reusable Paint objects to avoid per-frame allocation
    private final Paint clearPaint;
    private final Paint darknessPaint;
    private final Paint lightPaint;
    private final Paint drawPaint;
    private final Rect srcRect;
    private final Rect dstRect;

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
        this.nightDarkness = 0.55;  // Default 55% darkness at night
        this.ambientColor = Color.rgb(15, 15, 50);  // Dark blue-ish night color
        this.ambientLevel = 0.25;   // 25% ambient light even in total darkness
        this.gameTime = 0;
        this.renderScale = 4;       // Render at 1/4 resolution for performance

        // Create the light buffer at reduced resolution
        this.bufferWidth = screenWidth / renderScale;
        this.bufferHeight = screenHeight / renderScale;
        this.lightBuffer = Bitmap.createBitmap(bufferWidth, bufferHeight, Bitmap.Config.ARGB_8888);
        this.bufferCanvas = new Canvas(lightBuffer);

        // Pre-allocate Paint objects
        this.clearPaint = new Paint();
        this.clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        this.darknessPaint = new Paint();
        this.darknessPaint.setStyle(Paint.Style.FILL);

        this.lightPaint = new Paint();
        this.lightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        this.lightPaint.setAntiAlias(true);

        this.drawPaint = new Paint();
        this.drawPaint.setFilterBitmap(false);  // Nearest-neighbor for pixel art style

        this.srcRect = new Rect(0, 0, bufferWidth, bufferHeight);
        this.dstRect = new Rect(0, 0, screenWidth, screenHeight);
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
     */
    public void addLightSource(LightSource light) {
        lightSources.add(light);
    }

    /**
     * Remove a light source from the system.
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
    public void setAmbientColor(int color) {
        this.ambientColor = color;
    }

    /**
     * Render the lighting overlay.
     * Call this after drawing all game entities but before UI.
     * @param canvas Canvas to draw on
     * @param cameraX Camera X offset (for scrolling)
     * @param cameraY Camera Y offset (for scrolling)
     */
    public void render(Canvas canvas, double cameraX, double cameraY) {
        if (!isNight) {
            return; // No lighting needed during the day
        }

        // Clear the light buffer to transparent
        bufferCanvas.drawRect(0, 0, bufferWidth, bufferHeight, clearPaint);

        // Calculate the alpha for the darkness overlay
        int darknessAlpha = (int) ((nightDarkness * (1.0 - ambientLevel)) * 255);
        int darkColor = Color.argb(darknessAlpha,
                Color.red(ambientColor), Color.green(ambientColor), Color.blue(ambientColor));

        // Fill with darkness (normal compositing)
        darknessPaint.setColor(darkColor);
        darknessPaint.setXfermode(null);
        bufferCanvas.drawRect(0, 0, bufferWidth, bufferHeight, darknessPaint);

        // Now "subtract" light from the darkness using DST_OUT
        // This will cut holes in the darkness where lights are
        for (LightSource light : lightSources) {
            if (!light.isEnabled()) continue;

            // Convert world coordinates to buffer coordinates
            double screenX = (light.getX() - cameraX) / renderScale;
            double screenY = (light.getY() - cameraY) / renderScale;

            // Calculate scaled radii
            double innerRadius = light.getRadius() / renderScale;
            double outerRadius = light.getFalloffRadius() / renderScale;

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
            int centerAlpha = (int) (effectiveIntensity * 255);

            // Create a radial gradient for the light
            // For DST_OUT, white with alpha = full removal of darkness
            float innerFraction = (float) (innerRadius / outerRadius);
            // Clamp fraction to valid range
            innerFraction = Math.max(0.001f, Math.min(innerFraction, 0.999f));

            RadialGradient gradient = new RadialGradient(
                (float) screenX, (float) screenY,
                (float) outerRadius,
                new int[]{
                    Color.argb(centerAlpha, 255, 255, 255),  // Full light at center
                    Color.argb(centerAlpha, 255, 255, 255),  // Full light up to inner radius
                    Color.argb(0, 255, 255, 255)             // No light at outer radius
                },
                new float[]{0.0f, innerFraction, 1.0f},
                Shader.TileMode.CLAMP
            );

            lightPaint.setShader(gradient);
            bufferCanvas.drawCircle(
                (float) screenX, (float) screenY,
                (float) outerRadius,
                lightPaint
            );
            lightPaint.setShader(null);
        }

        // Draw the light buffer scaled up to screen size
        canvas.drawBitmap(lightBuffer, srcRect, dstRect, drawPaint);
    }

    /**
     * Render without camera offset (for non-scrolling scenes).
     */
    public void render(Canvas canvas) {
        render(canvas, 0, 0);
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
        LightSource torch = new LightSource(x, y, 80, Color.rgb(255, 200, 100));
        torch.setFalloffRadius(180);
        torch.enableFlicker(0.15, 8.0);
        return torch;
    }

    /**
     * Create a campfire-like light source with warm orange color and flickering.
     */
    public static LightSource createCampfireLight(double x, double y) {
        LightSource campfire = new LightSource(x, y, 120, Color.rgb(255, 150, 50));
        campfire.setFalloffRadius(280);
        campfire.enableFlicker(0.25, 6.0);
        return campfire;
    }

    /**
     * Create a lantern-like light source with steady yellow light.
     */
    public static LightSource createLanternLight(double x, double y) {
        LightSource lantern = new LightSource(x, y, 100, Color.rgb(255, 240, 180));
        lantern.setFalloffRadius(200);
        return lantern;
    }

    /**
     * Create a magical light source with blue/purple color.
     */
    public static LightSource createMagicLight(double x, double y) {
        LightSource magic = new LightSource(x, y, 90, Color.rgb(150, 150, 255));
        magic.setFalloffRadius(180);
        magic.enableFlicker(0.1, 3.0);
        return magic;
    }

    /**
     * Create a large area light (like moonlight through a window).
     */
    public static LightSource createAreaLight(double x, double y, double radius) {
        LightSource area = new LightSource(x, y, radius, Color.rgb(200, 200, 255));
        area.setFalloffRadius(radius * 1.5);
        area.setIntensity(0.6);
        return area;
    }

    /**
     * Recycles the light buffer bitmap to free memory.
     * Call when this system is no longer needed.
     */
    public void recycle() {
        if (lightBuffer != null && !lightBuffer.isRecycled()) {
            lightBuffer.recycle();
            lightBuffer = null;
        }
    }
}
