package com.ambermoongame.entity;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles physics interactions between entities including:
 * - Collision detection and response
 * - Push/separation when entities overlap
 * - Attack hitbox detection
 * Equivalent to entity/EntityPhysics.java from the desktop version.
 *
 * Conversion notes:
 * - java.awt.Rectangle -> android.graphics.Rect
 * - Rectangle.intersects(Rectangle) -> Rect.intersects(Rect, Rect)
 * - Rectangle.contains(x, y) -> Rect.contains(x, y)
 * - Rectangle.getCenterX() -> rect.centerX()
 *
 * Dependencies not yet ported (uncomment when available):
 * - PlayerBase, SpritePlayerEntity, MobEntity, SpriteMobEntity, MeleeAttackHitbox
 */
public class EntityPhysics {

    // Push force when entities collide - gentle push
    private static final double PUSH_FORCE = 0.8;
    private static final double VERTICAL_PUSH = 0.0;

    // --- Full desktop collision logic (enable when PlayerBase and MobEntity are ported) ---
    //
    // /**
    //  * Processes all entity collisions and applies appropriate responses.
    //  *
    //  * @param entities  List of all entities in the scene
    //  * @param player    The player entity
    //  * @param deltaTime Time since last update
    //  */
    // public static void processCollisions(List<Entity> entities, PlayerBase player, double deltaTime) {
    //     if (player == null || entities == null) return;
    //
    //     Rect playerBounds = player.getBounds();
    //
    //     for (Entity entity : entities) {
    //         if (entity instanceof MobEntity) {
    //             MobEntity mob = (MobEntity) entity;
    //             if (mob.getState() == MobEntity.AIState.DEAD) continue;
    //
    //             Rect mobBounds = mob.getBounds();
    //
    //             if (Rect.intersects(playerBounds, mobBounds)) {
    //                 double playerCenterX = playerBounds.centerX();
    //                 double playerCenterY = playerBounds.centerY();
    //                 double mobCenterX = mobBounds.centerX();
    //                 double mobCenterY = mobBounds.centerY();
    //
    //                 double dx = playerCenterX - mobCenterX;
    //                 double dy = playerCenterY - mobCenterY;
    //
    //                 double dist = Math.sqrt(dx * dx + dy * dy);
    //                 if (dist > 0) {
    //                     double pushX = (dx / dist) * PUSH_FORCE;
    //                     double pushY = (dy / dist) * VERTICAL_PUSH;
    //                     applyPushToPlayer(player, pushX, pushY);
    //                 }
    //             }
    //         }
    //     }
    //
    //     processMobCollisions(entities, deltaTime);
    // }
    //
    // private static void applyPushToPlayer(PlayerBase player, double pushX, double pushY) {
    //     if (player instanceof SpritePlayerEntity) {
    //         ((SpritePlayerEntity) player).applyPush(pushX, pushY);
    //     }
    // }
    //
    // private static void processMobCollisions(List<Entity> entities, double deltaTime) {
    //     List<MobEntity> mobs = new ArrayList<>();
    //     for (Entity e : entities) {
    //         if (e instanceof MobEntity) {
    //             MobEntity mob = (MobEntity) e;
    //             if (mob.getState() != MobEntity.AIState.DEAD) {
    //                 mobs.add(mob);
    //             }
    //         }
    //     }
    //
    //     for (int i = 0; i < mobs.size(); i++) {
    //         for (int j = i + 1; j < mobs.size(); j++) {
    //             MobEntity mob1 = mobs.get(i);
    //             MobEntity mob2 = mobs.get(j);
    //             Rect bounds1 = mob1.getBounds();
    //             Rect bounds2 = mob2.getBounds();
    //
    //             if (Rect.intersects(bounds1, bounds2)) {
    //                 double center1X = bounds1.centerX();
    //                 double center2X = bounds2.centerX();
    //                 double dx = center1X - center2X;
    //                 double separation = PUSH_FORCE * 0.5;
    //
    //                 if (dx > 0) {
    //                     mob1.applyPush(separation, 0);
    //                     mob2.applyPush(-separation, 0);
    //                 } else {
    //                     mob1.applyPush(-separation, 0);
    //                     mob2.applyPush(separation, 0);
    //                 }
    //             }
    //         }
    //     }
    // }

    // --- Full desktop attack logic (enable when PlayerBase, MobEntity, MeleeAttackHitbox are ported) ---
    //
    // /**
    //  * Checks if a player attack hits any mobs and applies damage.
    //  */
    // public static List<MobEntity> checkPlayerAttack(PlayerBase player, Rect attackBounds,
    //         int damage, double knockbackForce, List<Entity> entities) {
    //
    //     List<MobEntity> hitMobs = new ArrayList<>();
    //     if (player == null || entities == null) return hitMobs;
    //
    //     MeleeAttackHitbox arcHitbox = null;
    //     double attackDirX = player.isFacingRight() ? 1.0 : -1.0;
    //     double attackDirY = 0;
    //
    //     if (player instanceof SpritePlayerEntity) {
    //         SpritePlayerEntity spritePlayer = (SpritePlayerEntity) player;
    //         arcHitbox = spritePlayer.getMeleeAttackHitbox();
    //         if (arcHitbox != null) {
    //             attackDirX = spritePlayer.getAttackDirX();
    //             attackDirY = spritePlayer.getAttackDirY();
    //         }
    //     }
    //
    //     if (arcHitbox == null && attackBounds == null) return hitMobs;
    //
    //     for (Entity entity : entities) {
    //         if (entity instanceof MobEntity) {
    //             MobEntity mob = (MobEntity) entity;
    //             if (mob.getState() == MobEntity.AIState.DEAD) continue;
    //
    //             Rect mobBounds = mob.getBounds();
    //             boolean hit = false;
    //
    //             if (arcHitbox != null) {
    //                 hit = arcHitbox.intersects(mobBounds);
    //             } else if (attackBounds != null) {
    //                 hit = Rect.intersects(attackBounds, mobBounds);
    //             }
    //
    //             if (hit) {
    //                 double knockbackX = attackDirX * knockbackForce;
    //                 double knockbackY = attackDirY * knockbackForce * 0.8 - knockbackForce * 0.3;
    //                 mob.takeDamage(damage, knockbackX, knockbackY);
    //                 hitMobs.add(mob);
    //             }
    //         }
    //     }
    //     return hitMobs;
    // }

    /**
     * Gets the attack hitbox for a player based on their facing direction.
     * This is a standalone utility that works with basic Rect types.
     *
     * @param playerBounds  The player's bounding rect
     * @param facingRight   Whether the player is facing right
     * @param attackRange   How far the attack reaches
     * @param attackHeight  Height of the attack hitbox
     * @return Rect representing the attack hitbox
     */
    public static Rect getAttackBounds(Rect playerBounds, boolean facingRight,
                                       int attackRange, int attackHeight) {
        int attackX;
        if (facingRight) {
            attackX = playerBounds.right;
        } else {
            attackX = playerBounds.left - attackRange;
        }
        int attackY = playerBounds.top + (playerBounds.height() - attackHeight) / 2;

        return new Rect(attackX, attackY, attackX + attackRange, attackY + attackHeight);
    }
}
