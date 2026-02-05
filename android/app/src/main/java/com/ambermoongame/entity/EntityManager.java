package com.ambermoongame.entity;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.ambermoongame.input.TouchInputManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all entities in a scene, handling updates and rendering.
 * Supports camera-based rendering for scrolling levels.
 * Equivalent to entity/EntityManager.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.Graphics   -> android.graphics.Canvas
 * - java.awt.Rectangle  -> android.graphics.Rect
 * - InputManager         -> TouchInputManager
 *
 * Dependencies not yet ported (uncomment when available):
 * - PlayerBase, MobEntity, SpriteMobEntity, ItemEntity, ProjectileEntity
 * - BackgroundEntity, Camera
 */
public class EntityManager {

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

    /**
     * Updates all entities.
     *
     * NOTE: The desktop version has special-case handling for PlayerBase (calls
     * player.update(input, entities)), MobEntity (calls mob.update(deltaTime, entities)),
     * ItemEntity (sets entity list for block collisions), and ProjectileEntity
     * (removes inactive projectiles and collects dropped items).
     *
     * This simplified version calls update(input) on every entity.
     * Once PlayerBase, MobEntity, ItemEntity, and ProjectileEntity are ported,
     * this method should be expanded to match the desktop behavior by uncommenting
     * the type-specific update logic below.
     */
    public void updateAll(TouchInputManager input) {
        // Calculate delta time
        long currentTime = System.nanoTime();
        double deltaTime = (currentTime - lastUpdateTime) / 1_000_000_000.0;
        lastUpdateTime = currentTime;

        // Cap delta time to avoid physics issues on lag spikes
        if (deltaTime > 0.1) deltaTime = 0.1;

        // --- Full desktop logic (enable as classes are ported) ---
        //
        // PlayerBase player = null;
        // for (Entity e : entities) {
        //     if (e instanceof PlayerBase) {
        //         player = (PlayerBase) e;
        //         break;
        //     }
        // }
        // if (player != null) {
        //     player.update(input, entities);
        // }

        ArrayList<Entity> toRemove = new ArrayList<>();
        ArrayList<Entity> toAdd = new ArrayList<>();

        for (Entity e : entities) {
            // --- Uncomment when PlayerBase is ported ---
            // if (e instanceof PlayerBase) continue; // Already updated above

            // --- Uncomment when MobEntity/SpriteMobEntity are ported ---
            // if (e instanceof MobEntity) {
            //     MobEntity mob = (MobEntity) e;
            //     mob.update(deltaTime, entities);
            //     if (mob instanceof SpriteMobEntity) {
            //         SpriteMobEntity spriteMob = (SpriteMobEntity) mob;
            //         if (spriteMob.hasPendingDroppedItems()) {
            //             List<ItemEntity> droppedItems = spriteMob.collectDroppedItems();
            //             for (ItemEntity item : droppedItems) {
            //                 item.setEntityList(entities);
            //             }
            //             toAdd.addAll(droppedItems);
            //         }
            //         if (spriteMob.hasPendingProjectiles()) {
            //             toAdd.addAll(spriteMob.collectPendingProjectiles());
            //         }
            //     }
            //     if (mob.isDead()) {
            //         toRemove.add(e);
            //     }
            //     continue;
            // }

            // --- Uncomment when ItemEntity is ported ---
            // if (e instanceof ItemEntity) {
            //     ItemEntity item = (ItemEntity) e;
            //     item.setEntityList(entities);
            //     item.update(input);
            //     continue;
            // }

            // --- Uncomment when ProjectileEntity is ported ---
            // if (e instanceof ProjectileEntity) {
            //     ProjectileEntity proj = (ProjectileEntity) e;
            //     if (!proj.isActive()) {
            //         if (proj.hasPendingDroppedItem()) {
            //             ItemEntity droppedItem = proj.collectDroppedItem();
            //             if (droppedItem != null) {
            //                 droppedItem.setEntityList(entities);
            //                 toAdd.add(droppedItem);
            //             }
            //         }
            //         toRemove.add(e);
            //     }
            //     continue;
            // }

            // Regular entity update
            e.update(input);
        }

        // Add spawned entities
        for (Entity e : toAdd) {
            entities.add(e);
        }

        // Remove dead/inactive entities
        for (Entity e : toRemove) {
            entities.remove(e);
        }
    }

    /**
     * Draws all entities without camera transformation.
     * Use this for scenes without scrolling.
     */
    public void drawAll(Canvas canvas) {
        for (Entity e : entities) {
            e.draw(canvas);
        }
    }

    // --- Uncomment when Camera is ported ---
    //
    // /**
    //  * Draws all entities with camera-based visibility culling.
    //  * Assumes the camera transform has already been applied by the caller.
    //  *
    //  * @param canvas Canvas (with camera transform already applied)
    //  * @param camera Camera for visibility checking only
    //  */
    // public void drawAll(Canvas canvas, Camera camera) {
    //     for (Entity e : entities) {
    //         Rect bounds;
    //         if (e instanceof MobEntity) {
    //             bounds = ((MobEntity) e).getVisualBounds();
    //         } else {
    //             bounds = e.getBounds();
    //         }
    //         if (camera.isVisible(bounds)) {
    //             e.draw(canvas);
    //         }
    //     }
    // }
    //
    // /**
    //  * Draws all entities with background handling.
    //  * Assumes the camera transform has already been applied by the caller.
    //  *
    //  * @param canvas      Canvas (with camera transform already applied)
    //  * @param camera      Camera for visibility checking only
    //  * @param background  Background entity to draw with tiling (can be null)
    //  */
    // public void drawAllWithBackground(Canvas canvas, Camera camera, BackgroundEntity background) {
    //     if (background != null) {
    //         background.draw(canvas, camera);
    //     }
    //     for (Entity e : entities) {
    //         if (e instanceof BackgroundEntity) continue;
    //         Rect bounds;
    //         if (e instanceof MobEntity) {
    //             bounds = ((MobEntity) e).getVisualBounds();
    //         } else {
    //             bounds = e.getBounds();
    //         }
    //         if (camera.isVisible(bounds)) {
    //             e.draw(canvas);
    //         }
    //     }
    // }

    // --- Uncomment when PlayerBase is ported ---
    //
    // /**
    //  * Gets the player entity.
    //  * @return The player as PlayerBase, or null if not found
    //  */
    // public PlayerBase getPlayer() {
    //     for (Entity e : entities) {
    //         if (e instanceof PlayerBase) {
    //             return (PlayerBase) e;
    //         }
    //     }
    //     return null;
    // }

    // --- Uncomment when BackgroundEntity is ported ---
    //
    // /**
    //  * Finds the background entity in the entity list.
    //  * @return BackgroundEntity if found, null otherwise
    //  */
    // public BackgroundEntity getBackground() {
    //     for (Entity e : entities) {
    //         if (e instanceof BackgroundEntity) {
    //             return (BackgroundEntity) e;
    //         }
    //     }
    //     return null;
    // }

    /**
     * Gets the count of entities.
     */
    public int size() {
        return entities.size();
    }
}
