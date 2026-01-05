package animation;

import graphics.AnimatedTexture;
import graphics.AssetLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TriggeredAnimationManager coordinates animations that play alongside entity actions.
 * This includes item animations (bow draw, sword swing) and particle effects (sprint lines,
 * impact sparks).
 *
 * The manager handles:
 * - Loading and caching animation assets from organized folder structures
 * - Triggering animations based on entity states
 * - Managing active particle emitters
 * - Synchronizing item animations with entity actions
 *
 * Folder Structure Expected:
 *   assets/items/{item_name}/{state}.gif
 *   assets/particles/{particle_type}/{effect}.gif
 *   assets/textures/blocks/{block_name}/{state}.gif
 *
 * Usage:
 *   TriggeredAnimationManager manager = TriggeredAnimationManager.getInstance();
 *   manager.triggerItemAnimation("bow", ItemAnimationState.DRAW);
 *   manager.triggerParticle(ParticleAnimationState.SPRINT_LINES, x, y);
 */
public class TriggeredAnimationManager {

    private static TriggeredAnimationManager instance;

    // Cache for loaded animations
    private final Map<String, Map<ItemAnimationState, AnimatedTexture>> itemAnimationCache;
    private final Map<ParticleAnimationState, AnimatedTexture> particleAnimationCache;
    private final Map<String, Map<String, AnimatedTexture>> blockAnimationCache;

    // Active particle instances
    private final List<ActiveParticle> activeParticles;

    // Active item animation instances (attached to entities)
    private final Map<Object, ActiveItemAnimation> activeItemAnimations;

    // Configuration
    private static final String ITEMS_BASE_PATH = "assets/items/";
    private static final String PARTICLES_BASE_PATH = "assets/particles/";
    private static final String BLOCKS_BASE_PATH = "assets/textures/blocks/";

    // Maximum particles
    private static final int MAX_PARTICLES = 100;

    /**
     * Private constructor for singleton pattern.
     */
    private TriggeredAnimationManager() {
        this.itemAnimationCache = new ConcurrentHashMap<>();
        this.particleAnimationCache = new ConcurrentHashMap<>();
        this.blockAnimationCache = new ConcurrentHashMap<>();
        this.activeParticles = Collections.synchronizedList(new ArrayList<>());
        this.activeItemAnimations = new ConcurrentHashMap<>();
    }

    /**
     * Gets the singleton instance of the manager.
     * @return The TriggeredAnimationManager instance
     */
    public static synchronized TriggeredAnimationManager getInstance() {
        if (instance == null) {
            instance = new TriggeredAnimationManager();
        }
        return instance;
    }

    // ==================== Item Animation Methods ====================

    /**
     * Loads all animation states for an item from its folder.
     * @param itemId The item's registry ID (e.g., "wooden_bow", "fire_sword")
     * @return Map of available animation states
     */
    public Map<ItemAnimationState, AnimatedTexture> loadItemAnimations(String itemId) {
        if (itemAnimationCache.containsKey(itemId)) {
            return itemAnimationCache.get(itemId);
        }

        Map<ItemAnimationState, AnimatedTexture> animations = new HashMap<>();
        String itemFolder = ITEMS_BASE_PATH + itemId + "/";

        File folder = new File(itemFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            // Folder doesn't exist, return empty map
            itemAnimationCache.put(itemId, animations);
            return animations;
        }

        // Try to load each animation state
        for (ItemAnimationState state : ItemAnimationState.values()) {
            String filePath = state.getFilePath(itemFolder);
            File file = new File(filePath);

            if (file.exists()) {
                try {
                    AssetLoader.ImageAsset asset = AssetLoader.load(filePath);
                    if (asset.animatedTexture != null) {
                        animations.put(state, asset.animatedTexture);
                    }
                } catch (Exception e) {
                    System.err.println("TriggeredAnimationManager: Failed to load " + filePath);
                }
            }
        }

        itemAnimationCache.put(itemId, animations);
        return animations;
    }

    /**
     * Gets an item's animation for a specific state.
     * @param itemId The item's registry ID
     * @param state The animation state
     * @return The AnimatedTexture, or null if not available
     */
    public AnimatedTexture getItemAnimation(String itemId, ItemAnimationState state) {
        Map<ItemAnimationState, AnimatedTexture> animations = loadItemAnimations(itemId);
        return animations.get(state);
    }

    /**
     * Checks if an item has a specific animation state.
     * @param itemId The item's registry ID
     * @param state The animation state
     * @return true if the animation exists
     */
    public boolean hasItemAnimation(String itemId, ItemAnimationState state) {
        return getItemAnimation(itemId, state) != null;
    }

    /**
     * Triggers an item animation attached to an entity.
     * @param entityKey Unique key for the entity (usually the entity object)
     * @param itemId The item's registry ID
     * @param state The animation state to trigger
     * @param x World X position
     * @param y World Y position
     * @param width Render width
     * @param height Render height
     * @param facingRight Direction facing
     */
    public void triggerItemAnimation(Object entityKey, String itemId, ItemAnimationState state,
                                      int x, int y, int width, int height, boolean facingRight) {
        AnimatedTexture texture = getItemAnimation(itemId, state);
        if (texture == null) return;

        ActiveItemAnimation active = new ActiveItemAnimation(
            itemId, state, texture, x, y, width, height, facingRight
        );
        activeItemAnimations.put(entityKey, active);
    }

    /**
     * Updates the position and state of an entity's item animation.
     * @param entityKey Unique key for the entity
     * @param x World X position
     * @param y World Y position
     * @param facingRight Direction facing
     */
    public void updateItemAnimation(Object entityKey, int x, int y, boolean facingRight) {
        ActiveItemAnimation active = activeItemAnimations.get(entityKey);
        if (active != null) {
            active.x = x;
            active.y = y;
            active.facingRight = facingRight;
        }
    }

    /**
     * Stops an entity's item animation.
     * @param entityKey Unique key for the entity
     */
    public void stopItemAnimation(Object entityKey) {
        activeItemAnimations.remove(entityKey);
    }

    /**
     * Gets the active item animation for an entity.
     * @param entityKey Unique key for the entity
     * @return The active item animation, or null
     */
    public ActiveItemAnimation getActiveItemAnimation(Object entityKey) {
        return activeItemAnimations.get(entityKey);
    }

    // ==================== Particle Methods ====================

    /**
     * Loads a particle animation.
     * @param state The particle animation state
     * @return The AnimatedTexture, or null if not available
     */
    public AnimatedTexture loadParticleAnimation(ParticleAnimationState state) {
        if (particleAnimationCache.containsKey(state)) {
            return particleAnimationCache.get(state);
        }

        String filePath = state.getDefaultFilePath();
        File file = new File(filePath);

        if (file.exists()) {
            try {
                AssetLoader.ImageAsset asset = AssetLoader.load(filePath);
                if (asset.animatedTexture != null) {
                    particleAnimationCache.put(state, asset.animatedTexture);
                    return asset.animatedTexture;
                }
            } catch (Exception e) {
                System.err.println("TriggeredAnimationManager: Failed to load " + filePath);
            }
        }

        return null;
    }

    /**
     * Triggers a particle effect at a position.
     * @param state The particle animation state
     * @param x World X position
     * @param y World Y position
     * @return The created ActiveParticle, or null if animation not available
     */
    public ActiveParticle triggerParticle(ParticleAnimationState state, int x, int y) {
        return triggerParticle(state, x, y, 32, 32, null, 1.0, false);
    }

    /**
     * Triggers a particle effect at a position with custom parameters.
     * @param state The particle animation state
     * @param x World X position
     * @param y World Y position
     * @param width Render width
     * @param height Render height
     * @param tint Optional color tint (null for default)
     * @param duration Duration in seconds (0 for single play)
     * @param looping Whether to loop the animation
     * @return The created ActiveParticle, or null if animation not available
     */
    public ActiveParticle triggerParticle(ParticleAnimationState state, int x, int y,
                                           int width, int height, Color tint,
                                           double duration, boolean looping) {
        AnimatedTexture texture = loadParticleAnimation(state);
        Color finalTint = tint != null ? tint : state.getDefaultTint();

        // Create particle even without texture (will use procedural fallback)
        ActiveParticle particle = new ActiveParticle(
            state, texture, x, y, width, height, finalTint, duration, looping
        );

        // Limit particle count
        while (activeParticles.size() >= MAX_PARTICLES) {
            activeParticles.remove(0);
        }

        activeParticles.add(particle);
        return particle;
    }

    /**
     * Triggers a particle attached to an entity (follows entity).
     * @param state The particle animation state
     * @param entityKey Key to track with
     * @param offsetX Offset from entity position
     * @param offsetY Offset from entity position
     * @param width Render width
     * @param height Render height
     * @return The created ActiveParticle
     */
    public ActiveParticle triggerAttachedParticle(ParticleAnimationState state, Object entityKey,
                                                    int offsetX, int offsetY, int width, int height) {
        ActiveParticle particle = triggerParticle(state, offsetX, offsetY, width, height, null, 0, true);
        if (particle != null) {
            particle.attachedTo = entityKey;
            particle.offsetX = offsetX;
            particle.offsetY = offsetY;
        }
        return particle;
    }

    /**
     * Updates all attached particles to follow their entities.
     * @param entityPositions Map of entity keys to (x, y) positions
     */
    public void updateAttachedParticles(Map<Object, int[]> entityPositions) {
        synchronized (activeParticles) {
            for (ActiveParticle particle : activeParticles) {
                if (particle.attachedTo != null) {
                    int[] pos = entityPositions.get(particle.attachedTo);
                    if (pos != null) {
                        particle.x = pos[0] + particle.offsetX;
                        particle.y = pos[1] + particle.offsetY;
                    }
                }
            }
        }
    }

    /**
     * Removes all particles attached to an entity.
     * @param entityKey The entity key
     */
    public void removeAttachedParticles(Object entityKey) {
        synchronized (activeParticles) {
            activeParticles.removeIf(p -> entityKey.equals(p.attachedTo));
        }
    }

    // ==================== Block Animation Methods ====================

    /**
     * Loads all animation states for a block from its folder.
     * @param blockId The block's ID (e.g., "dirt", "stone")
     * @return Map of state names to animations
     */
    public Map<String, AnimatedTexture> loadBlockAnimations(String blockId) {
        if (blockAnimationCache.containsKey(blockId)) {
            return blockAnimationCache.get(blockId);
        }

        Map<String, AnimatedTexture> animations = new HashMap<>();
        String blockFolder = BLOCKS_BASE_PATH + blockId + "/";

        File folder = new File(blockFolder);
        if (!folder.exists() || !folder.isDirectory()) {
            blockAnimationCache.put(blockId, animations);
            return animations;
        }

        // Load all GIF files in the folder
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".gif"));
        if (files != null) {
            for (File file : files) {
                String stateName = file.getName().replace(".gif", "");
                try {
                    AssetLoader.ImageAsset asset = AssetLoader.load(file.getPath());
                    if (asset.animatedTexture != null) {
                        animations.put(stateName, asset.animatedTexture);
                    }
                } catch (Exception e) {
                    System.err.println("TriggeredAnimationManager: Failed to load " + file.getPath());
                }
            }
        }

        blockAnimationCache.put(blockId, animations);
        return animations;
    }

    /**
     * Gets a block's animation for a specific state.
     * @param blockId The block's ID
     * @param stateName The state name (e.g., "idle", "breaking")
     * @return The AnimatedTexture, or null if not available
     */
    public AnimatedTexture getBlockAnimation(String blockId, String stateName) {
        Map<String, AnimatedTexture> animations = loadBlockAnimations(blockId);
        return animations.get(stateName);
    }

    // ==================== Update and Render ====================

    /**
     * Updates all active animations.
     * @param deltaMs Milliseconds since last update
     */
    public void update(long deltaMs) {
        // Update item animations
        for (ActiveItemAnimation anim : activeItemAnimations.values()) {
            anim.update(deltaMs);
        }

        // Update particles
        synchronized (activeParticles) {
            Iterator<ActiveParticle> iterator = activeParticles.iterator();
            while (iterator.hasNext()) {
                ActiveParticle particle = iterator.next();
                particle.update(deltaMs);

                if (particle.isExpired()) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Draws all active particles.
     * @param g Graphics context
     */
    public void drawParticles(Graphics g) {
        synchronized (activeParticles) {
            for (ActiveParticle particle : activeParticles) {
                particle.draw(g);
            }
        }
    }

    /**
     * Gets the current number of active particles.
     * @return Count of active particles
     */
    public int getActiveParticleCount() {
        return activeParticles.size();
    }

    /**
     * Clears all cached animations (for hot reload).
     */
    public void clearCache() {
        itemAnimationCache.clear();
        particleAnimationCache.clear();
        blockAnimationCache.clear();
    }

    /**
     * Clears all active animations.
     */
    public void clearActive() {
        activeParticles.clear();
        activeItemAnimations.clear();
    }

    // ==================== Inner Classes ====================

    /**
     * Represents an active item animation attached to an entity.
     */
    public static class ActiveItemAnimation {
        public final String itemId;
        public final ItemAnimationState state;
        public final AnimatedTexture texture;
        public int x, y;
        public int width, height;
        public boolean facingRight;
        public boolean completed;

        public ActiveItemAnimation(String itemId, ItemAnimationState state, AnimatedTexture texture,
                                    int x, int y, int width, int height, boolean facingRight) {
            this.itemId = itemId;
            this.state = state;
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.facingRight = facingRight;
            this.completed = false;
        }

        public void update(long deltaMs) {
            if (texture != null) {
                texture.update(deltaMs);
                // Check if animation completed (non-looping)
                if (!texture.isLooping() && texture.getCurrentFrameIndex() == texture.getFrameCount() - 1) {
                    completed = true;
                }
            }
        }

        public void draw(Graphics g) {
            if (texture == null) return;

            BufferedImage frame = texture.getCurrentFrame();
            if (frame == null) return;

            Graphics2D g2d = (Graphics2D) g;
            if (facingRight) {
                g2d.drawImage(frame, x, y, width, height, null);
            } else {
                g2d.drawImage(frame, x + width, y, -width, height, null);
            }
        }
    }

    /**
     * Represents an active particle effect.
     */
    public static class ActiveParticle {
        public final ParticleAnimationState state;
        public final AnimatedTexture texture;
        public int x, y;
        public int width, height;
        public Color tint;
        public double duration;
        public double elapsed;
        public boolean looping;
        public Object attachedTo;
        public int offsetX, offsetY;

        // For procedural particles (when no texture)
        private final List<ProceduralParticle> proceduralParticles;

        public ActiveParticle(ParticleAnimationState state, AnimatedTexture texture,
                               int x, int y, int width, int height, Color tint,
                               double duration, boolean looping) {
            this.state = state;
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.tint = tint;
            this.duration = duration;
            this.elapsed = 0;
            this.looping = looping;
            this.proceduralParticles = new ArrayList<>();

            // Initialize procedural particles if no texture
            if (texture == null) {
                initProceduralParticles();
            }
        }

        private void initProceduralParticles() {
            Random rand = new Random();
            int count = 5 + rand.nextInt(10);

            for (int i = 0; i < count; i++) {
                ProceduralParticle p = new ProceduralParticle();
                p.x = rand.nextDouble() * width;
                p.y = rand.nextDouble() * height;
                p.velX = (rand.nextDouble() - 0.5) * 2;
                p.velY = (rand.nextDouble() - 0.5) * 2;
                p.size = 2 + rand.nextInt(4);
                p.life = 0.5 + rand.nextDouble() * 0.5;
                p.maxLife = p.life;
                proceduralParticles.add(p);
            }
        }

        public void update(long deltaMs) {
            double deltaSeconds = deltaMs / 1000.0;
            elapsed += deltaSeconds;

            if (texture != null) {
                texture.update(deltaMs);
            } else {
                // Update procedural particles
                Iterator<ProceduralParticle> it = proceduralParticles.iterator();
                while (it.hasNext()) {
                    ProceduralParticle p = it.next();
                    p.x += p.velX;
                    p.y += p.velY;
                    p.life -= deltaSeconds;

                    if (p.life <= 0) {
                        if (looping) {
                            // Reset particle
                            Random rand = new Random();
                            p.x = rand.nextDouble() * width;
                            p.y = rand.nextDouble() * height;
                            p.life = p.maxLife;
                        } else {
                            it.remove();
                        }
                    }
                }
            }
        }

        public boolean isExpired() {
            if (duration > 0 && elapsed >= duration) {
                return true;
            }
            if (!looping && texture == null && proceduralParticles.isEmpty()) {
                return true;
            }
            if (!looping && texture != null && !texture.isLooping()) {
                return texture.getCurrentFrameIndex() >= texture.getFrameCount() - 1;
            }
            return false;
        }

        public void draw(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            if (texture != null) {
                BufferedImage frame = tint != null
                    ? texture.getTintedFrame(tint)
                    : texture.getCurrentFrame();

                if (frame != null) {
                    // Apply alpha based on fade out
                    float alpha = 1.0f;
                    if (duration > 0 && elapsed > duration * 0.7) {
                        alpha = (float)((duration - elapsed) / (duration * 0.3));
                    }

                    if (alpha < 1.0f) {
                        Composite oldComp = g2d.getComposite();
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                        g2d.drawImage(frame, x, y, width, height, null);
                        g2d.setComposite(oldComp);
                    } else {
                        g2d.drawImage(frame, x, y, width, height, null);
                    }
                }
            } else {
                // Draw procedural particles
                for (ProceduralParticle p : proceduralParticles) {
                    float alpha = (float)(p.life / p.maxLife);
                    Color c = new Color(
                        tint.getRed(), tint.getGreen(), tint.getBlue(),
                        (int)(tint.getAlpha() * alpha)
                    );
                    g2d.setColor(c);
                    g2d.fillOval(x + (int)p.x, y + (int)p.y, p.size, p.size);
                }
            }
        }
    }

    /**
     * Simple procedural particle for fallback rendering.
     */
    private static class ProceduralParticle {
        double x, y;
        double velX, velY;
        int size;
        double life;
        double maxLife;
    }

    // ==================== Convenience Methods ====================

    /**
     * Triggers sprint particles behind an entity.
     * @param x Entity X position
     * @param y Entity Y position
     * @param width Entity width
     * @param height Entity height
     * @param facingRight Direction facing
     */
    public void triggerSprintEffect(int x, int y, int width, int height, boolean facingRight) {
        int particleX = facingRight ? x - 20 : x + width;
        int particleY = y + height / 2;

        triggerParticle(ParticleAnimationState.SPRINT_LINES,
            particleX, particleY, 30, 20, null, 0.3, false);
    }

    /**
     * Triggers a jump dust effect.
     * @param x Entity center X
     * @param y Ground Y position
     */
    public void triggerJumpDust(int x, int y) {
        triggerParticle(ParticleAnimationState.JUMP_BURST,
            x - 16, y - 8, 32, 16, null, 0.4, false);
    }

    /**
     * Triggers a landing dust effect.
     * @param x Entity center X
     * @param y Ground Y position
     */
    public void triggerLandDust(int x, int y) {
        triggerParticle(ParticleAnimationState.LAND_DUST,
            x - 20, y - 10, 40, 20, null, 0.5, false);
    }

    /**
     * Triggers combat hit sparks.
     * @param x Impact X
     * @param y Impact Y
     * @param isCritical Whether it was a critical hit
     */
    public void triggerHitEffect(int x, int y, boolean isCritical) {
        if (isCritical) {
            triggerParticle(ParticleAnimationState.CRITICAL_HIT,
                x - 24, y - 24, 48, 48, null, 0.6, false);
        } else {
            triggerParticle(ParticleAnimationState.HIT_SPARKS,
                x - 16, y - 16, 32, 32, null, 0.4, false);
        }
    }

    /**
     * Triggers a magic charge effect around an entity.
     * @param x Entity center X
     * @param y Entity center Y
     * @param size Effect size
     */
    public void triggerMagicCharge(int x, int y, int size) {
        triggerParticle(ParticleAnimationState.MAGIC_CHARGE,
            x - size/2, y - size/2, size, size, null, 0, true);
    }

    /**
     * Triggers a healing effect on an entity.
     * @param x Entity center X
     * @param y Entity Y
     * @param height Entity height
     */
    public void triggerHealEffect(int x, int y, int height) {
        triggerParticle(ParticleAnimationState.HEAL,
            x - 20, y, 40, height, null, 1.0, false);
    }
}
