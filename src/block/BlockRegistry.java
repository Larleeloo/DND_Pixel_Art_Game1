package block;
import entity.*;
import graphics.*;
import audio.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for block textures and definitions.
 * Uses singleton pattern to ensure textures are loaded only once
 * and shared across all block entities for memory efficiency.
 * Supports both static textures (PNG) and animated textures (GIF).
 *
 * Key optimizations:
 * - Texture caching: Each texture is loaded once and reused
 * - Pre-scaled textures: Textures are scaled at load time, not render time
 * - Square optimization: Since blocks are always square, only one dimension needed
 * - Animated texture support: GIF textures are properly handled with frame cycling
 */
public class BlockRegistry {

    private static BlockRegistry instance;

    // Default block size in pixels (before scaling)
    public static final int BASE_BLOCK_SIZE = 16;

    // Scale factor for rendering (similar to SpriteEntity.SCALE)
    public static final int BLOCK_SCALE = 4;

    // Final rendered block size
    public static final int BLOCK_SIZE = BASE_BLOCK_SIZE * BLOCK_SCALE; // 64 pixels

    // Cache for loaded and scaled textures (static)
    private final Map<BlockType, BufferedImage> textureCache;

    // Cache for animated textures (GIF blocks)
    private final Map<BlockType, AnimatedTexture> animatedTextureCache;

    // Cache for tinted texture variants (key: BlockType + color hash)
    private final Map<String, BufferedImage> tintedTextureCache;

    // Fallback texture for missing assets
    private BufferedImage fallbackTexture;

    private BlockRegistry() {
        textureCache = new HashMap<>();
        animatedTextureCache = new HashMap<>();
        tintedTextureCache = new HashMap<>();
        createFallbackTexture();
    }

    /**
     * Gets the singleton instance of BlockRegistry.
     * @return The BlockRegistry instance
     */
    public static synchronized BlockRegistry getInstance() {
        if (instance == null) {
            instance = new BlockRegistry();
        }
        return instance;
    }

    /**
     * Creates a magenta/black checkerboard fallback texture for missing assets.
     */
    private void createFallbackTexture() {
        fallbackTexture = new BufferedImage(BASE_BLOCK_SIZE, BASE_BLOCK_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fallbackTexture.createGraphics();

        // Create checkerboard pattern (indicates missing texture)
        int halfSize = BASE_BLOCK_SIZE / 2;
        g.setColor(Color.MAGENTA);
        g.fillRect(0, 0, halfSize, halfSize);
        g.fillRect(halfSize, halfSize, halfSize, halfSize);
        g.setColor(Color.BLACK);
        g.fillRect(halfSize, 0, halfSize, halfSize);
        g.fillRect(0, halfSize, halfSize, halfSize);

        g.dispose();
    }

    /**
     * Gets the texture for a block type, loading it if necessary.
     * The returned texture is already scaled to BLOCK_SIZE.
     * For animated blocks, returns the current frame.
     *
     * @param type The block type
     * @return Scaled BufferedImage for the block
     */
    public BufferedImage getTexture(BlockType type) {
        // Check if this is an animated block
        if (animatedTextureCache.containsKey(type)) {
            return animatedTextureCache.get(type).getCurrentFrame();
        }

        if (textureCache.containsKey(type)) {
            return textureCache.get(type);
        }

        // Load and cache the texture
        return loadAndCacheTexture(type);
    }

    /**
     * Gets the animated texture for a block type if it exists.
     * Returns null for static textures.
     *
     * @param type The block type
     * @return AnimatedTexture or null if static
     */
    public AnimatedTexture getAnimatedTexture(BlockType type) {
        if (!animatedTextureCache.containsKey(type) && !textureCache.containsKey(type)) {
            loadAndCacheTexture(type);
        }
        return animatedTextureCache.get(type);
    }

    /**
     * Checks if a block type has an animated texture.
     *
     * @param type The block type
     * @return true if the block is animated
     */
    public boolean isAnimated(BlockType type) {
        AnimatedTexture anim = getAnimatedTexture(type);
        return anim != null && anim.isAnimated();
    }

    /**
     * Updates all animated block textures.
     * Call this every frame to advance GIF animations.
     *
     * @param deltaMs Time elapsed since last update in milliseconds
     */
    public void updateAnimations(long deltaMs) {
        for (AnimatedTexture anim : animatedTextureCache.values()) {
            anim.update(deltaMs);
        }
    }

    /**
     * Loads and caches a texture for a block type.
     * Handles both static and animated textures.
     */
    private BufferedImage loadAndCacheTexture(BlockType type) {
        AssetLoader.ImageAsset asset = AssetLoader.load(type.getTexturePath());

        if (asset == null || asset.staticImage == null) {
            System.err.println("BlockRegistry: Failed to load texture: " + type.getTexturePath() + ", using fallback");
            BufferedImage scaled = scaleTexture(fallbackTexture);
            textureCache.put(type, scaled);
            return scaled;
        }

        // Check if this is an animated texture
        if (asset.animatedTexture != null && asset.animatedTexture.isAnimated()) {
            // Scale all frames and create a new AnimatedTexture
            AnimatedTexture scaledAnim = scaleAnimatedTexture(asset.animatedTexture);
            animatedTextureCache.put(type, scaledAnim);
            System.out.println("BlockRegistry: Loaded animated block texture: " + type.name() +
                              " (" + scaledAnim.getFrameCount() + " frames)");
            return scaledAnim.getCurrentFrame();
        } else {
            // Static texture
            BufferedImage scaled = scaleTexture(asset.staticImage);
            textureCache.put(type, scaled);
            return scaled;
        }
    }

    /**
     * Gets a tinted version of a block texture.
     * Tinted textures are cached for reuse.
     *
     * @param type The block type
     * @param red Red component (0-255)
     * @param green Green component (0-255)
     * @param blue Blue component (0-255)
     * @return Tinted and scaled BufferedImage
     */
    public BufferedImage getTintedTexture(BlockType type, int red, int green, int blue) {
        String cacheKey = type.name() + "_" + red + "_" + green + "_" + blue;

        if (tintedTextureCache.containsKey(cacheKey)) {
            return tintedTextureCache.get(cacheKey);
        }

        // Get base texture and apply tint
        BufferedImage baseTexture = getTexture(type);
        BufferedImage tinted = applyTint(baseTexture, red, green, blue);
        tintedTextureCache.put(cacheKey, tinted);
        return tinted;
    }

    /**
     * Scales an animated texture - all frames scaled to BLOCK_SIZE.
     */
    private AnimatedTexture scaleAnimatedTexture(AnimatedTexture source) {
        java.util.List<BufferedImage> scaledFrames = new java.util.ArrayList<>();
        java.util.List<Integer> delays = new java.util.ArrayList<>();

        for (int i = 0; i < source.getFrameCount(); i++) {
            BufferedImage frame = source.getFrame(i);
            scaledFrames.add(scaleTexture(frame));
        }

        // Copy delays from original (approximate - AnimatedTexture doesn't expose delays directly)
        int avgDelay = source.getTotalDuration() / source.getFrameCount();
        for (int i = 0; i < source.getFrameCount(); i++) {
            delays.add(avgDelay);
        }

        return new AnimatedTexture(scaledFrames, delays);
    }

    /**
     * Scales a texture to BLOCK_SIZE x BLOCK_SIZE using nearest neighbor
     * interpolation to preserve pixel art appearance.
     */
    private BufferedImage scaleTexture(BufferedImage source) {
        BufferedImage scaled = new BufferedImage(BLOCK_SIZE, BLOCK_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();

        // Use nearest neighbor for crisp pixel art
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(source, 0, 0, BLOCK_SIZE, BLOCK_SIZE, null);
        g.dispose();

        return scaled;
    }

    /**
     * Applies a color tint to an image using multiplicative blending.
     */
    private BufferedImage applyTint(BufferedImage source, int red, int green, int blue) {
        int width = source.getWidth();
        int height = source.getHeight();

        BufferedImage tinted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        float rFactor = red / 255.0f;
        float gFactor = green / 255.0f;
        float bFactor = blue / 255.0f;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = source.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                if (alpha > 0) {
                    r = Math.min(255, Math.round(r * rFactor));
                    g = Math.min(255, Math.round(g * gFactor));
                    b = Math.min(255, Math.round(b * bFactor));
                }

                int newArgb = (alpha << 24) | (r << 16) | (g << 8) | b;
                tinted.setRGB(x, y, newArgb);
            }
        }

        return tinted;
    }

    /**
     * Preloads all block textures for faster runtime performance.
     * Call this during game initialization.
     */
    public void preloadAllTextures() {
        System.out.println("BlockRegistry: Preloading all block textures...");
        for (BlockType type : BlockType.values()) {
            getTexture(type);
        }
        System.out.println("BlockRegistry: Preloaded " + textureCache.size() + " textures");
    }

    /**
     * Clears all cached textures to free memory.
     * Use when switching levels or during cleanup.
     */
    public void clearCache() {
        textureCache.clear();
        animatedTextureCache.clear();
        tintedTextureCache.clear();
        System.out.println("BlockRegistry: Cache cleared");
    }

    /**
     * Gets the standard block size (scaled).
     * @return Block size in pixels
     */
    public int getBlockSize() {
        return BLOCK_SIZE;
    }
}
