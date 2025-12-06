import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

/**
 * Manages all entities in a scene, handling updates and rendering.
 * Supports camera-based rendering for scrolling levels.
 */
class EntityManager {

    private ArrayList<Entity> entities = new ArrayList<>();

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
        PlayerEntity player = null;

        // Find the player
        for (Entity e : entities) {
            if (e instanceof PlayerEntity) {
                player = (PlayerEntity) e;
                break;
            }
        }

        // Update player with entity list for collisions
        if (player != null) {
            player.update(input, entities);
        }

        // Update all other entities (items, obstacles, etc.)
        for (Entity e : entities) {
            if (!(e instanceof PlayerEntity)) {
                e.update(input);
            }
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
     * Draws all entities with camera transformation applied.
     * The camera translates the graphics context so entities are
     * drawn relative to the viewport.
     *
     * @param g      Graphics context
     * @param camera Camera for viewport transformation
     */
    public void drawAll(Graphics g, Camera camera) {
        Graphics2D g2d = (Graphics2D) g;

        // Save the current transform
        AffineTransform oldTransform = g2d.getTransform();

        // Apply camera transformation
        camera.applyTransform(g2d);

        // Draw all entities (they draw at their world positions)
        for (Entity e : entities) {
            // Optionally skip entities that are off-screen for performance
            if (camera.isVisible(e.getBounds())) {
                e.draw(g);
            }
        }

        // Restore the original transform
        g2d.setTransform(oldTransform);
    }

    /**
     * Draws all entities with camera transformation, with background handling.
     * Background entities are drawn with tiling support if configured.
     *
     * @param g           Graphics context
     * @param camera      Camera for viewport transformation
     * @param background  Background entity to draw with tiling (can be null)
     */
    public void drawAllWithBackground(Graphics g, Camera camera, BackgroundEntity background) {
        Graphics2D g2d = (Graphics2D) g;

        // Save the current transform
        AffineTransform oldTransform = g2d.getTransform();

        // Apply camera transformation
        camera.applyTransform(g2d);

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

        // Restore the original transform
        g2d.setTransform(oldTransform);
    }

    public PlayerEntity getPlayer() {
        for (Entity e : entities) {
            if (e instanceof PlayerEntity) {
                return (PlayerEntity) e;
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
