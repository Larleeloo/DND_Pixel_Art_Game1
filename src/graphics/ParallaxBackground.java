package graphics;
import animation.*;
import block.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Manages multiple parallax layers for creating depth in 2D backgrounds.
 * Supports 5 standard depth levels:
 * - BACKGROUND (z=-2): Sky, distant mountains - slowest scroll
 * - MIDDLEGROUND_3 (z=-1): Far hills, distant structures
 * - MIDDLEGROUND_2 (z=0): Mid-range scenery
 * - MIDDLEGROUND_1 (z=1): Near background elements
 * - FOREGROUND (z=2): Closest elements - fastest scroll, can overlap player
 *
 * Each layer scrolls at a different rate relative to camera movement,
 * creating a parallax depth effect similar to Terraria.
 */
class ParallaxBackground {

    // Standard z-order constants for the 5 depth levels
    public static final int Z_BACKGROUND = -2;      // Furthest back (sky)
    public static final int Z_MIDDLEGROUND_3 = -1;  // Distant
    public static final int Z_MIDDLEGROUND_2 = 0;   // Mid-range
    public static final int Z_MIDDLEGROUND_1 = 1;   // Near
    public static final int Z_FOREGROUND = 2;       // Closest (in front of game world)

    // Default scroll speeds for each depth level
    public static final double SPEED_BACKGROUND = 0.1;      // Very slow
    public static final double SPEED_MIDDLEGROUND_3 = 0.3;  // Slow
    public static final double SPEED_MIDDLEGROUND_2 = 0.5;  // Medium
    public static final double SPEED_MIDDLEGROUND_1 = 0.7;  // Faster
    public static final double SPEED_FOREGROUND = 1.2;      // Faster than camera

    // List of all parallax layers
    private List<ParallaxLayer> layers;

    // Flag to track if layers need re-sorting
    private boolean needsSort;

    /**
     * Creates a new ParallaxBackground manager.
     */
    public ParallaxBackground() {
        this.layers = new ArrayList<>();
        this.needsSort = false;
    }

    /**
     * Add a layer to the parallax system.
     *
     * @param layer The layer to add
     */
    public void addLayer(ParallaxLayer layer) {
        layers.add(layer);
        needsSort = true;
    }

    /**
     * Add a layer with standard depth settings.
     *
     * @param name       Layer name
     * @param imagePath  Path to layer image
     * @param depthLevel One of Z_BACKGROUND, Z_MIDDLEGROUND_*, or Z_FOREGROUND
     * @return The created layer for further configuration
     */
    public ParallaxLayer addLayer(String name, String imagePath, int depthLevel) {
        double scrollSpeed = getDefaultScrollSpeed(depthLevel);
        ParallaxLayer layer = new ParallaxLayer(name, imagePath, scrollSpeed, depthLevel);
        addLayer(layer);
        return layer;
    }

    /**
     * Add a layer with custom scroll speed.
     *
     * @param name        Layer name
     * @param imagePath   Path to layer image
     * @param scrollSpeed Scroll speed (0.0 = static, 1.0 = world speed)
     * @param zOrder      Z-order for depth sorting
     * @return The created layer for further configuration
     */
    public ParallaxLayer addLayer(String name, String imagePath, double scrollSpeed, int zOrder) {
        ParallaxLayer layer = new ParallaxLayer(name, imagePath, scrollSpeed, zOrder);
        addLayer(layer);
        return layer;
    }

    /**
     * Get the default scroll speed for a depth level.
     */
    private double getDefaultScrollSpeed(int depthLevel) {
        switch (depthLevel) {
            case Z_BACKGROUND:
                return SPEED_BACKGROUND;
            case Z_MIDDLEGROUND_3:
                return SPEED_MIDDLEGROUND_3;
            case Z_MIDDLEGROUND_2:
                return SPEED_MIDDLEGROUND_2;
            case Z_MIDDLEGROUND_1:
                return SPEED_MIDDLEGROUND_1;
            case Z_FOREGROUND:
                return SPEED_FOREGROUND;
            default:
                return SPEED_MIDDLEGROUND_2;
        }
    }

    /**
     * Remove a layer by reference.
     */
    public void removeLayer(ParallaxLayer layer) {
        layers.remove(layer);
    }

    /**
     * Remove a layer by name.
     */
    public void removeLayer(String name) {
        layers.removeIf(layer -> layer.getName().equals(name));
    }

    /**
     * Get a layer by name.
     */
    public ParallaxLayer getLayer(String name) {
        for (ParallaxLayer layer : layers) {
            if (layer.getName().equals(name)) {
                return layer;
            }
        }
        return null;
    }

    /**
     * Get all layers.
     */
    public List<ParallaxLayer> getLayers() {
        return new ArrayList<>(layers);
    }

    /**
     * Get the number of layers.
     */
    public int getLayerCount() {
        return layers.size();
    }

    /**
     * Clear all layers.
     */
    public void clearLayers() {
        layers.clear();
    }

    /**
     * Sort layers by z-order if needed.
     */
    private void sortLayers() {
        if (needsSort) {
            Collections.sort(layers, Comparator.comparingInt(ParallaxLayer::getZOrder));
            needsSort = false;
        }
    }

    /**
     * Draw all background layers (z-order < 0).
     * Call this BEFORE drawing game entities.
     *
     * @param g      Graphics context
     * @param camera Camera for viewport information
     */
    public void drawBackground(Graphics2D g, Camera camera) {
        sortLayers();

        for (ParallaxLayer layer : layers) {
            if (layer.getZOrder() < 0) {
                layer.draw(g, camera);
            }
        }
    }

    /**
     * Draw all middleground layers (z-order = 0).
     * Call this after background but before game entities.
     *
     * @param g      Graphics context
     * @param camera Camera for viewport information
     */
    public void drawMiddleground(Graphics2D g, Camera camera) {
        sortLayers();

        for (ParallaxLayer layer : layers) {
            if (layer.getZOrder() == 0) {
                layer.draw(g, camera);
            }
        }
    }

    /**
     * Draw all foreground layers (z-order > 0).
     * Call this AFTER drawing game entities.
     *
     * @param g      Graphics context
     * @param camera Camera for viewport information
     */
    public void drawForeground(Graphics2D g, Camera camera) {
        sortLayers();

        for (ParallaxLayer layer : layers) {
            if (layer.getZOrder() > 0) {
                layer.draw(g, camera);
            }
        }
    }

    /**
     * Draw all layers in z-order.
     * Convenience method that draws everything in correct order.
     *
     * @param g      Graphics context
     * @param camera Camera for viewport information
     */
    public void drawAll(Graphics2D g, Camera camera) {
        sortLayers();

        for (ParallaxLayer layer : layers) {
            layer.draw(g, camera);
        }
    }

    /**
     * Draw layers within a specific z-order range.
     *
     * @param g        Graphics context
     * @param camera   Camera for viewport information
     * @param minZ     Minimum z-order (inclusive)
     * @param maxZ     Maximum z-order (inclusive)
     */
    public void drawLayersInRange(Graphics2D g, Camera camera, int minZ, int maxZ) {
        sortLayers();

        for (ParallaxLayer layer : layers) {
            int z = layer.getZOrder();
            if (z >= minZ && z <= maxZ) {
                layer.draw(g, camera);
            }
        }
    }

    /**
     * Notify that z-orders have changed and need re-sorting.
     */
    public void markDirty() {
        needsSort = true;
    }

    // ========== Factory Methods for Common Setups ==========

    /**
     * Create a sky layer with typical settings.
     * Tiles horizontally, very slow scroll, full opacity.
     *
     * @param imagePath Path to sky image
     * @return The created sky layer
     */
    public ParallaxLayer createSkyLayer(String imagePath) {
        ParallaxLayer sky = addLayer("sky", imagePath, SPEED_BACKGROUND, Z_BACKGROUND);
        sky.setTiling(true, false);
        sky.setScale(10.0); // Match BackgroundEntity scale
        return sky;
    }

    /**
     * Create a distant mountain/hill layer.
     *
     * @param imagePath Path to mountain image
     * @return The created layer
     */
    public ParallaxLayer createDistantLayer(String imagePath) {
        ParallaxLayer distant = addLayer("distant", imagePath, SPEED_MIDDLEGROUND_3, Z_MIDDLEGROUND_3);
        distant.setTiling(true, false);
        distant.setScale(10.0);
        distant.setOpacity(0.8f);
        return distant;
    }

    /**
     * Create a mid-range decoration layer.
     *
     * @param imagePath Path to decoration image
     * @return The created layer
     */
    public ParallaxLayer createMidgroundLayer(String imagePath) {
        ParallaxLayer midground = addLayer("midground", imagePath, SPEED_MIDDLEGROUND_2, Z_MIDDLEGROUND_2);
        midground.setTiling(true, false);
        midground.setScale(10.0);
        return midground;
    }

    /**
     * Create a near background layer.
     *
     * @param imagePath Path to near background image
     * @return The created layer
     */
    public ParallaxLayer createNearLayer(String imagePath) {
        ParallaxLayer near = addLayer("near", imagePath, SPEED_MIDDLEGROUND_1, Z_MIDDLEGROUND_1);
        near.setTiling(true, false);
        near.setScale(10.0);
        return near;
    }

    /**
     * Create a foreground overlay layer that appears in front of game elements.
     *
     * @param imagePath Path to foreground image
     * @return The created layer
     */
    public ParallaxLayer createForegroundLayer(String imagePath) {
        ParallaxLayer foreground = addLayer("foreground", imagePath, SPEED_FOREGROUND, Z_FOREGROUND);
        foreground.setTiling(true, false);
        foreground.setScale(10.0);
        foreground.setOpacity(0.7f);
        return foreground;
    }

    /**
     * Set up a standard 5-layer parallax background.
     * Uses the same image for all layers with different settings.
     *
     * @param backgroundPath Path to the background image
     */
    public void setupStandardLayers(String backgroundPath) {
        // Clear existing
        clearLayers();

        // Add all 5 standard layers
        createSkyLayer(backgroundPath);
        createDistantLayer(backgroundPath);
        createMidgroundLayer(backgroundPath);
        createNearLayer(backgroundPath);
        // Foreground is optional - usually for decorative elements
    }
}
