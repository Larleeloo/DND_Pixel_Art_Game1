import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all entities in a scene, handling updates and rendering.
 * Supports camera-based rendering for scrolling levels.
 */
class EntityManager {

    private ArrayList<Entity> entities = new ArrayList<>();
    private long lastUpdateTime = System.nanoTime();

    public void addEntity(Entity e) {
        entities.add(e);
    }

    public void removeEntity(Entity e) {
        entities.remove(e);
    }

    public void clear() {
        entities.clear();
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public void updateAll(InputManager input) {
        // Calculate delta time
        long currentTime = System.nanoTime();
        double deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0;
        lastUpdateTime = currentTime;

        // Cap delta time to avoid physics issues on lag spikes
        if (deltaTime > 0.1) deltaTime = 0.1;

        PlayerBase player = null;

        // Find the player (supports both PlayerEntity and PlayerBoneEntity)
        for (Entity e : entities) {
            if (e instanceof PlayerBase) {
                player = (PlayerBase) e;
                break;
            }
        }

        // Update player with entity list for collisions
        if (player != null) {
            player.update(input, entities);
        }

        // Collect dead mobs for removal
        ArrayList<Entity> toRemove = new ArrayList<>();

        // Update all other entities (items, obstacles, mobs, etc.)
        for (Entity e : entities) {
            if (e instanceof PlayerBase) {
                continue; // Already updated
            }

            // Special handling for mob entities - they need deltaTime and entity list
            if (e instanceof MobEntity) {
                MobEntity mob = (MobEntity) e;
                mob.update(deltaTime, entities);

                // Mark dead mobs for removal after their death animation
                if (mob.isDead()) {
                    toRemove.add(e);
                }
            } else {
                // Regular entity update
                e.update(input);
            }
        }

        // Remove dead mobs
        for (Entity e : toRemove) {
            entities.remove(e);
        }
    }

    /**
     * Draws all entities without camera transformation.
     * Use this for scenes without scrolling.
     */
    public void drawAll(Graphics g) {
        for (Entity e : entities) {
            e.draw(g);
        }
    }

    /**
     * Draws all entities with camera-based visibility culling.
     * Assumes the camera transform has already been applied by the caller.
     * Uses camera only for visibility checking.
     *
     * @param g      Graphics context (with camera transform already applied)
     * @param camera Camera for visibility checking only
     */
    public void drawAll(Graphics g, Camera camera) {
        // Draw all entities (they draw at their world positions)
        for (Entity e : entities) {
            // Skip entities that are off-screen for performance
            if (camera.isVisible(e.getBounds())) {
                e.draw(g);
            }
        }
    }

    /**
     * Draws all entities with background handling.
     * Assumes the camera transform has already been applied by the caller.
     * Uses camera only for visibility culling.
     *
     * @param g           Graphics context (with camera transform already applied)
     * @param camera      Camera for visibility checking only
     * @param background  Background entity to draw with tiling (can be null)
     */
    public void drawAllWithBackground(Graphics g, Camera camera, BackgroundEntity background) {
        // Draw background first (it handles its own tiling)
        if (background != null) {
            background.draw(g, camera);
        }

        // Draw all other entities
        for (Entity e : entities) {
            if (e instanceof BackgroundEntity) {
                continue; // Skip background, already drawn
            }

            // Skip entities that are off-screen for performance
            if (camera.isVisible(e.getBounds())) {
                e.draw(g);
            }
        }
    }

    /**
     * Gets the player entity (supports both PlayerEntity and PlayerBoneEntity).
     * @return The player as PlayerBase, or null if not found
     */
    public PlayerBase getPlayer() {
        for (Entity e : entities) {
            if (e instanceof PlayerBase) {
                return (PlayerBase) e;
            }
        }
        return null;
    }

    /**
     * Finds the background entity in the entity list.
     *
     * @return BackgroundEntity if found, null otherwise
     */
    public BackgroundEntity getBackground() {
        for (Entity e : entities) {
            if (e instanceof BackgroundEntity) {
                return (BackgroundEntity) e;
            }
        }
        return null;
    }

    /**
     * Gets the count of entities.
     */
    public int size() {
        return entities.size();
    }
}
