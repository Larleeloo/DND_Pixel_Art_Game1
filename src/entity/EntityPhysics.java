package entity;

import entity.player.PlayerBase;
import entity.player.SpritePlayerEntity;
import entity.mob.MobEntity;
import entity.mob.SpriteMobEntity;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles physics interactions between entities including:
 * - Collision detection and response
 * - Push/separation when entities overlap
 * - Attack hitbox detection
 */
public class EntityPhysics {

    // Push force when entities collide - gentle push
    private static final double PUSH_FORCE = 0.8;
    private static final double VERTICAL_PUSH = 0.0;

    /**
     * Processes all entity collisions and applies appropriate responses.
     *
     * @param entities List of all entities in the scene
     * @param player The player entity
     * @param deltaTime Time since last update
     */
    public static void processCollisions(List<Entity> entities, PlayerBase player, double deltaTime) {
        if (player == null || entities == null) return;

        Rectangle playerBounds = player.getBounds();

        // Check player against all mobs
        for (Entity entity : entities) {
            if (entity instanceof MobEntity) {
                MobEntity mob = (MobEntity) entity;

                // Skip dead mobs
                if (mob.getState() == MobEntity.AIState.DEAD) continue;

                Rectangle mobBounds = mob.getBounds();

                // Check for collision
                if (playerBounds.intersects(mobBounds)) {
                    // Calculate push direction
                    double playerCenterX = playerBounds.x + playerBounds.width / 2.0;
                    double playerCenterY = playerBounds.y + playerBounds.height / 2.0;
                    double mobCenterX = mobBounds.x + mobBounds.width / 2.0;
                    double mobCenterY = mobBounds.y + mobBounds.height / 2.0;

                    double dx = playerCenterX - mobCenterX;
                    double dy = playerCenterY - mobCenterY;

                    // Normalize and apply push
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist > 0) {
                        double pushX = (dx / dist) * PUSH_FORCE;
                        double pushY = (dy / dist) * VERTICAL_PUSH;

                        // Apply push to player (via velocity if available)
                        applyPushToPlayer(player, pushX, pushY);
                    }
                }
            }
        }

        // Process mob-to-mob collisions
        processMobCollisions(entities, deltaTime);
    }

    /**
     * Applies push force to player.
     */
    private static void applyPushToPlayer(PlayerBase player, double pushX, double pushY) {
        // Get player bounds and apply position offset
        Rectangle bounds = player.getBounds();

        // We need to set velocity - check if player has velocity methods
        // For now, we'll apply direct position change via collision response
        if (player instanceof entity.player.SpritePlayerEntity) {
            entity.player.SpritePlayerEntity spritePlayer = (entity.player.SpritePlayerEntity) player;
            spritePlayer.applyPush(pushX, pushY);
        } else if (player instanceof entity.player.PlayerEntity) {
            entity.player.PlayerEntity basicPlayer = (entity.player.PlayerEntity) player;
            basicPlayer.applyPush(pushX, pushY);
        } else if (player instanceof entity.player.PlayerBoneEntity) {
            entity.player.PlayerBoneEntity bonePlayer = (entity.player.PlayerBoneEntity) player;
            bonePlayer.applyPush(pushX, pushY);
        }
    }

    /**
     * Processes collisions between mobs.
     */
    private static void processMobCollisions(List<Entity> entities, double deltaTime) {
        List<MobEntity> mobs = new ArrayList<>();

        for (Entity e : entities) {
            if (e instanceof MobEntity) {
                MobEntity mob = (MobEntity) e;
                if (mob.getState() != MobEntity.AIState.DEAD) {
                    mobs.add(mob);
                }
            }
        }

        // Check each pair of mobs
        for (int i = 0; i < mobs.size(); i++) {
            for (int j = i + 1; j < mobs.size(); j++) {
                MobEntity mob1 = mobs.get(i);
                MobEntity mob2 = mobs.get(j);

                Rectangle bounds1 = mob1.getBounds();
                Rectangle bounds2 = mob2.getBounds();

                if (bounds1.intersects(bounds2)) {
                    // Calculate separation
                    double center1X = bounds1.x + bounds1.width / 2.0;
                    double center2X = bounds2.x + bounds2.width / 2.0;

                    double dx = center1X - center2X;
                    double separation = PUSH_FORCE * 0.5;

                    if (dx > 0) {
                        mob1.applyPush(separation, 0);
                        mob2.applyPush(-separation, 0);
                    } else {
                        mob1.applyPush(-separation, 0);
                        mob2.applyPush(separation, 0);
                    }
                }
            }
        }
    }

    /**
     * Checks if a player attack hits any mobs and applies damage.
     * This version supports both legacy Rectangle hitboxes and the new arc-based MeleeAttackHitbox.
     * For SpritePlayerEntity, it will use the arc-based hitbox if available.
     *
     * @param player The attacking player
     * @param attackBounds The attack hitbox (Rectangle for broad-phase, ignored if arc hitbox available)
     * @param damage Damage to apply
     * @param knockbackForce Knockback force to apply
     * @param entities List of all entities
     * @return List of mobs that were hit
     */
    public static List<MobEntity> checkPlayerAttack(PlayerBase player, Rectangle attackBounds,
            int damage, double knockbackForce, List<Entity> entities) {

        List<MobEntity> hitMobs = new ArrayList<>();

        if (player == null || entities == null) {
            return hitMobs;
        }

        // Check if we have access to arc-based hitbox via SpritePlayerEntity
        MeleeAttackHitbox arcHitbox = null;
        double attackDirX = player.isFacingRight() ? 1.0 : -1.0;
        double attackDirY = 0;

        if (player instanceof SpritePlayerEntity) {
            SpritePlayerEntity spritePlayer = (SpritePlayerEntity) player;
            arcHitbox = spritePlayer.getMeleeAttackHitbox();
            if (arcHitbox != null) {
                // Get attack direction for knockback calculation
                attackDirX = spritePlayer.getAttackDirX();
                attackDirY = spritePlayer.getAttackDirY();
            }
        }

        // If no arc hitbox and no rectangle bounds, can't check attack
        if (arcHitbox == null && attackBounds == null) {
            return hitMobs;
        }

        for (Entity entity : entities) {
            if (entity instanceof MobEntity) {
                MobEntity mob = (MobEntity) entity;

                // Skip dead mobs
                if (mob.getState() == MobEntity.AIState.DEAD) continue;

                Rectangle mobBounds = mob.getBounds();
                boolean hit = false;

                // Use arc-based collision if available, otherwise fall back to rectangle
                if (arcHitbox != null) {
                    hit = arcHitbox.intersects(mobBounds);
                } else if (attackBounds != null) {
                    hit = attackBounds.intersects(mobBounds);
                }

                if (hit) {
                    // Calculate knockback direction based on attack direction
                    // Knockback is in the direction of the attack, with a slight upward component
                    double knockbackX = attackDirX * knockbackForce;
                    double knockbackY = attackDirY * knockbackForce * 0.8 - knockbackForce * 0.3;

                    // Apply damage and knockback
                    mob.takeDamage(damage, knockbackX, knockbackY);
                    hitMobs.add(mob);
                }
            }
        }

        return hitMobs;
    }

    /**
     * Checks if a player attack hits any mobs using arc-based hitbox detection.
     * This is the preferred method for the new dynamic attack system.
     *
     * @param player The attacking player (must be SpritePlayerEntity)
     * @param damage Damage to apply
     * @param knockbackForce Knockback force to apply
     * @param entities List of all entities
     * @return List of mobs that were hit
     */
    public static List<MobEntity> checkPlayerMeleeAttack(SpritePlayerEntity player,
            int damage, double knockbackForce, List<Entity> entities) {

        List<MobEntity> hitMobs = new ArrayList<>();

        if (player == null || entities == null) {
            return hitMobs;
        }

        MeleeAttackHitbox hitbox = player.getMeleeAttackHitbox();
        if (hitbox == null) {
            return hitMobs;
        }

        double attackDirX = player.getAttackDirX();
        double attackDirY = player.getAttackDirY();

        for (Entity entity : entities) {
            if (entity instanceof MobEntity) {
                MobEntity mob = (MobEntity) entity;

                // Skip dead mobs
                if (mob.getState() == MobEntity.AIState.DEAD) continue;

                Rectangle mobBounds = mob.getBounds();

                if (hitbox.intersects(mobBounds)) {
                    // Calculate knockback direction based on attack direction
                    double knockbackX = attackDirX * knockbackForce;
                    double knockbackY = attackDirY * knockbackForce * 0.8 - knockbackForce * 0.3;

                    // Apply damage and knockback
                    mob.takeDamage(damage, knockbackX, knockbackY);
                    hitMobs.add(mob);
                }
            }
        }

        return hitMobs;
    }

    /**
     * Gets the attack hitbox for a player based on their facing direction.
     *
     * @param player The player
     * @param attackRange How far the attack reaches
     * @param attackWidth Width of the attack hitbox
     * @param attackHeight Height of the attack hitbox
     * @return Rectangle representing the attack hitbox
     */
    public static Rectangle getPlayerAttackBounds(PlayerBase player, int attackRange,
            int attackWidth, int attackHeight) {

        Rectangle playerBounds = player.getBounds();
        int attackX;

        if (player.isFacingRight()) {
            attackX = playerBounds.x + playerBounds.width;
        } else {
            attackX = playerBounds.x - attackRange;
        }

        int attackY = playerBounds.y + (playerBounds.height - attackHeight) / 2;

        return new Rectangle(attackX, attackY, attackRange, attackHeight);
    }
}
