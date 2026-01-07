package graphics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages animated textures loaded from GIF files.
 * Handles frame cycling based on elapsed time and supports tinting.
 *
 * This class provides support for:
 * - Multi-frame GIF animations
 * - Per-frame timing (respects GIF frame delays)
 * - Tinted texture caching
 * - Static texture fallback for single-frame images
 *
 * Usage:
 *   AnimatedTexture texture = new AnimatedTexture(frames, delays);
 *   texture.update(deltaTimeMs);  // Call each frame
 *   BufferedImage currentFrame = texture.getCurrentFrame();
 */
public class AnimatedTexture {

    // Animation frames
    private final List<BufferedImage> frames;
    private final List<Integer> frameDelays;  // Delay per frame in milliseconds

    // Current animation state
    private int currentFrameIndex;
    private long elapsedTime;  // Time since current frame started (ms)
    private boolean paused;
    private boolean looping;

    // Dimensions (all frames should have same dimensions)
    private final int width;
    private final int height;

    // Tinted frame cache: key = "frameIndex_R_G_B"
    private final Map<String, BufferedImage> tintedFrameCache;
    private Color currentTintColor;

    // Default frame delay if not specified (in ms)
    public static final int DEFAULT_FRAME_DELAY = 100;

    /**
     * Creates an animated texture from a list of frames.
     *
     * @param frames List of BufferedImage frames
     * @param frameDelays List of delays per frame in milliseconds (can be null for default timing)
     */
    public AnimatedTexture(List<BufferedImage> frames, List<Integer> frameDelays) {
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("AnimatedTexture requires at least one frame");
        }

        this.frames = new ArrayList<>(frames);
        this.frameDelays = new ArrayList<>();

        // Set up frame delays
        if (frameDelays != null && !frameDelays.isEmpty()) {
            for (int i = 0; i < frames.size(); i++) {
                if (i < frameDelays.size() && frameDelays.get(i) != null && frameDelays.get(i) > 0) {
                    this.frameDelays.add(frameDelays.get(i));
                } else {
                    this.frameDelays.add(DEFAULT_FRAME_DELAY);
                }
            }
        } else {
            // Use default delay for all frames
            for (int i = 0; i < frames.size(); i++) {
                this.frameDelays.add(DEFAULT_FRAME_DELAY);
            }
        }

        this.currentFrameIndex = 0;
        this.elapsedTime = 0;
        this.paused = false;
        this.looping = true;

        // Get dimensions from first frame
        BufferedImage firstFrame = frames.get(0);
        this.width = firstFrame.getWidth();
        this.height = firstFrame.getHeight();

        this.tintedFrameCache = new HashMap<>();
        this.currentTintColor = null;
    }

    /**
     * Creates an animated texture from a single static image.
     * Useful for consistency when mixing static and animated textures.
     *
     * @param staticImage A single BufferedImage
     */
    public AnimatedTexture(BufferedImage staticImage) {
        this(List.of(staticImage), null);
    }

    /**
     * Updates the animation state based on elapsed time.
     * Call this every frame with the time delta.
     *
     * @param deltaMs Time elapsed since last update in milliseconds
     */
    public void update(long deltaMs) {
        if (paused || frames.size() <= 1) {
            return;
        }

        elapsedTime += deltaMs;
        int currentDelay = frameDelays.get(currentFrameIndex);

        // Advance frames if enough time has passed
        while (elapsedTime >= currentDelay) {
            elapsedTime -= currentDelay;
            currentFrameIndex++;

            if (currentFrameIndex >= frames.size()) {
                if (looping) {
                    currentFrameIndex = 0;
                } else {
                    currentFrameIndex = frames.size() - 1;
                    paused = true;
                    break;
                }
            }

            currentDelay = frameDelays.get(currentFrameIndex);
        }
    }

    /**
     * Gets the current animation frame.
     *
     * @return The current BufferedImage frame
     */
    public BufferedImage getCurrentFrame() {
        return frames.get(currentFrameIndex);
    }

    /**
     * Gets the current frame with a tint applied.
     * Tinted frames are cached for performance.
     *
     * @param tintColor The color to tint with (null for no tint)
     * @return The tinted frame, or original if tintColor is null
     */
    public BufferedImage getCurrentFrame(Color tintColor) {
        if (tintColor == null) {
            return getCurrentFrame();
        }

        // Check if tint color changed - clear cache if so
        if (!tintColor.equals(currentTintColor)) {
            tintedFrameCache.clear();
            currentTintColor = tintColor;
        }

        String cacheKey = currentFrameIndex + "_" + tintColor.getRed() + "_" +
                          tintColor.getGreen() + "_" + tintColor.getBlue();

        if (tintedFrameCache.containsKey(cacheKey)) {
            return tintedFrameCache.get(cacheKey);
        }

        // Create tinted frame
        BufferedImage tinted = createTintedFrame(frames.get(currentFrameIndex), tintColor);
        tintedFrameCache.put(cacheKey, tinted);
        return tinted;
    }

    /**
     * Creates a tinted copy of a frame, only affecting non-transparent pixels.
     *
     * @param frame The source frame
     * @param tintColor The tint color
     * @return Tinted frame image
     */
    private BufferedImage createTintedFrame(BufferedImage frame, Color tintColor) {
        BufferedImage tinted = new BufferedImage(
            frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);

        float tintR = tintColor.getRed() / 255.0f;
        float tintG = tintColor.getGreen() / 255.0f;
        float tintB = tintColor.getBlue() / 255.0f;
        float blendFactor = 0.4f;  // How much tint to apply

        for (int y = 0; y < frame.getHeight(); y++) {
            for (int x = 0; x < frame.getWidth(); x++) {
                int pixel = frame.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xFF;

                if (alpha > 0) {
                    int r = (pixel >> 16) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = pixel & 0xFF;

                    int newR = Math.min(255, (int)(r * (1 - blendFactor) + tintR * 255 * blendFactor));
                    int newG = Math.min(255, (int)(g * (1 - blendFactor) + tintG * 255 * blendFactor));
                    int newB = Math.min(255, (int)(b * (1 - blendFactor) + tintB * 255 * blendFactor));

                    tinted.setRGB(x, y, (alpha << 24) | (newR << 16) | (newG << 8) | newB);
                } else {
                    tinted.setRGB(x, y, 0);
                }
            }
        }

        return tinted;
    }

    /**
     * Gets a specific frame by index.
     *
     * @param index Frame index
     * @return The frame at that index
     */
    public BufferedImage getFrame(int index) {
        if (index < 0 || index >= frames.size()) {
            return frames.get(0);
        }
        return frames.get(index);
    }

    /**
     * Resets the animation to the first frame.
     */
    public void reset() {
        currentFrameIndex = 0;
        elapsedTime = 0;
        paused = false;
    }

    /**
     * Pauses the animation.
     */
    public void pause() {
        paused = true;
    }

    /**
     * Resumes the animation.
     */
    public void resume() {
        paused = false;
    }

    /**
     * Checks if the animation is paused.
     * @return true if paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Sets whether the animation should loop.
     * @param looping true to loop, false to stop at last frame
     */
    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    /**
     * Checks whether the animation is set to loop.
     * @return true if looping, false if stops at last frame
     */
    public boolean isLooping() {
        return looping;
    }

    /**
     * Checks if this is an animated texture (more than one frame).
     * @return true if animated
     */
    public boolean isAnimated() {
        return frames.size() > 1;
    }

    /**
     * Gets the number of frames in the animation.
     * @return Frame count
     */
    public int getFrameCount() {
        return frames.size();
    }

    /**
     * Gets the current frame index.
     * @return Current frame index
     */
    public int getCurrentFrameIndex() {
        return currentFrameIndex;
    }

    /**
     * Sets the current frame index directly.
     * @param index Frame index to set
     */
    public void setCurrentFrameIndex(int index) {
        if (index >= 0 && index < frames.size()) {
            currentFrameIndex = index;
            elapsedTime = 0;
        }
    }

    /**
     * Gets the width of the texture (first frame width).
     * @return Width in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the texture (first frame height).
     * @return Height in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the total animation duration in milliseconds.
     * @return Total duration
     */
    public int getTotalDuration() {
        int total = 0;
        for (int delay : frameDelays) {
            total += delay;
        }
        return total;
    }

    /**
     * Clears the tinted frame cache to free memory.
     */
    public void clearTintCache() {
        tintedFrameCache.clear();
        currentTintColor = null;
    }

    /**
     * Gets the first frame as a static BufferedImage.
     * Useful for systems that need a static fallback.
     * @return First frame
     */
    public BufferedImage getStaticImage() {
        return frames.get(0);
    }

    /**
     * Calculates the scale factor needed to render this texture at a target display size.
     * This allows higher-resolution textures (32x32, 48x48, etc.) to appear at the
     * same in-game size as lower-resolution textures (16x16).
     *
     * @param targetSize The desired display size (e.g., 16 for standard item size)
     * @return Scale factor (1.0 for matching size, 0.5 for 2x texture, etc.)
     */
    public double getScaleFactorForSize(int targetSize) {
        if (width <= 0) return 1.0;
        return (double) targetSize / width;
    }

    /**
     * Gets the scale factor relative to the standard 16x16 base size.
     * Convenience method for common item rendering.
     *
     * @return Scale factor relative to 16x16 base (1.0 for 16x16, 0.5 for 32x32, etc.)
     */
    public double getBaseScaleFactor() {
        return getScaleFactorForSize(16);
    }
}
