package entity.mob;
import entity.*;
import entity.player.*;
import block.*;
import animation.*;
import animation.bone.*;
import graphics.*;
import input.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all AI-controlled mobs (creatures/enemies).
 * Provides skeleton-based animation, AI state machine, physics, and hitbox support.
 *
 * ============================================================================
 * MOB AI STATE MACHINE:
 * ============================================================================
 *
 * States:
 *   IDLE     - Standing still, occasionally looking around
 *   WANDER   - Moving randomly in the area
 *   CHASE    - Pursuing the player
 *   ATTACK   - Attacking the player
 *   FLEE     - Running away from danger
 *   HURT     - Reacting to damage
 *   DEAD     - Death animation, then despawn
 *
 * Transitions:
 *   IDLE → WANDER: After random idle time
 *   WANDER → IDLE: After reaching wander destination
 *   IDLE/WANDER → CHASE: Player enters detection range
 *   CHASE → ATTACK: Player in attack range
 *   ATTACK → CHASE: Attack complete, player still in range
 *   Any → HURT: Taking damage
 *   HURT → Previous state: After hurt animation
 *   Any → DEAD: Health reaches 0
 *
 * ============================================================================
 */
public abstract class MobEntity extends Entity {

    // ==================== AI States ====================

    public enum AIState {
        IDLE,
        WANDER,
        CHASE,
        ATTACK,
        FLEE,
        HURT,
        DEAD
    }

    // ==================== Mob Properties ====================

    // Position and physics
    protected double posX;
    protected double posY;
    protected double velocityX;
    protected double velocityY;
    protected boolean onGround;
    protected boolean facingRight = true;

    // Hitbox
    protected int hitboxWidth;
    protected int hitboxHeight;
    protected int hitboxOffsetX;  // Offset from position
    protected int hitboxOffsetY;

    // Health and combat
    protected int maxHealth;
    protected int currentHealth;
    protected int attackDamage;
    protected double attackRange;
    protected double attackCooldown;
    protected double attackTimer;
    protected double invincibilityTime;
    protected double invincibilityTimer;

    // AI configuration
    protected AIState currentState = AIState.IDLE;
    protected AIState previousState = AIState.IDLE;
    protected double detectionRange;
    protected double loseTargetRange;
    protected double wanderSpeed;
    protected double chaseSpeed;
    protected double stateTimer;
    protected double idleDuration = 2.0;
    protected double wanderDuration = 3.0;

    // Wander behavior
    protected double wanderTargetX;
    protected double wanderMinX;
    protected double wanderMaxX;

    // Target tracking
    protected PlayerBase target;
    protected double targetLastX;
    protected double targetLastY;

    // Animation
    protected Skeleton skeleton;
    protected double animationScale = 1.0;
    protected double skeletonOffsetY = 0;  // Offset from position to skeleton anchor (for feet on ground)

    // Physics constants
    protected static final double GRAVITY = 0.5;
    protected double groundY = 920;  // Ground level from level data

    // Visual bounds padding for camera culling (skeleton may be larger than hitbox)
    protected int visualPaddingX = 50;
    protected int visualPaddingY = 50;

    // Debug
    protected boolean debugDraw = false;

    // ==================== Constructor ====================

    /**
     * Creates a new mob entity.
     *
     * @param x Initial X position
     * @param y Initial Y position
     */
    public MobEntity(int x, int y) {
        super(x, y);
        this.posX = x;
        this.posY = y;
        this.x = x;
        this.y = y;

        // Default values - override in subclasses
        this.hitboxWidth = 48;
        this.hitboxHeight = 48;
        this.hitboxOffsetX = -24;
        this.hitboxOffsetY = -48;

        this.maxHealth = 20;
        this.currentHealth = maxHealth;
        this.attackDamage = 5;
        this.attackRange = 50;
        this.attackCooldown = 1.0;
        this.invincibilityTime = 0.5;

        this.detectionRange = 200;
        this.loseTargetRange = 400;
        this.wanderSpeed = 50;
        this.chaseSpeed = 100;

        this.wanderMinX = x - 200;
        this.wanderMaxX = x + 200;

        // Start with wander target at spawn position to prevent immediate movement
        this.wanderTargetX = x;

        // Randomize initial state timer to prevent all mobs from acting in sync
        this.stateTimer = Math.random() * idleDuration;
    }

    // ==================== Abstract Methods ====================

    /**
     * Creates the skeleton for this mob type.
     * Override in subclasses to create the appropriate skeleton.
     */
    protected abstract void createSkeleton();

    /**
     * Adds animations to the skeleton.
     * Override in subclasses to add appropriate animations.
     */
    protected abstract void setupAnimations();

    /**
     * Called when the mob attacks.
     * Override to implement attack behavior.
     */
    protected abstract void performAttack();

    // ==================== Entity Interface ====================

    @Override
    public Rectangle getBounds() {
        return new Rectangle(
            (int)posX + hitboxOffsetX,
            (int)posY + hitboxOffsetY,
            hitboxWidth,
            hitboxHeight
        );
    }

    /**
     * Gets the visual bounds of the mob for camera culling.
     * This is larger than the hitbox to account for the skeleton/animation.
     *
     * @return Rectangle representing the visual bounds
     */
    public Rectangle getVisualBounds() {
        // Visual bounds include the skeleton which extends above the position
        // and may be wider than the hitbox
        double scaledOffsetY = skeletonOffsetY * animationScale;
        int visualWidth = (int)(Math.max(hitboxWidth, hitboxWidth * animationScale) + visualPaddingX * 2);
        int visualHeight = (int)(scaledOffsetY + hitboxHeight * animationScale + visualPaddingY);

        return new Rectangle(
            (int)posX - visualWidth / 2,
            (int)(posY - scaledOffsetY - visualPaddingY / 2),
            visualWidth,
            visualHeight
        );
    }

    @Override
    public void update(InputManager input) {
        // Get delta time (assuming 60 FPS if not available)
        double deltaTime = 1.0 / 60.0;
        update(deltaTime, null);
    }

    /**
     * Updates the mob with delta time and entity list for collisions.
     *
     * @param deltaTime Time since last update in seconds
     * @param entities  List of all entities for collision detection
     */
    public void update(double deltaTime, List<Entity> entities) {
        // Update timers
        attackTimer = Math.max(0, attackTimer - deltaTime);
        invincibilityTimer = Math.max(0, invincibilityTimer - deltaTime);
        stateTimer += deltaTime;

        // Don't process AI if dead
        if (currentState == AIState.DEAD) {
            updateDeadState(deltaTime);
            return;
        }

        // Find target (player)
        if (entities != null) {
            findTarget(entities);
        }

        // Update AI state
        updateAI(deltaTime);

        // Apply physics
        applyPhysics(deltaTime, entities);

        // Update animation
        updateAnimation(deltaTime);

        // Sync position
        this.x = (int)posX;
        this.y = (int)posY;
    }

    @Override
    public void draw(Graphics g) {
        if (skeleton == null) return;

        Graphics2D g2d = (Graphics2D) g;

        // Position skeleton with feet at ground level
        // skeletonOffsetY accounts for the distance from body center (anchor) to feet
        // Must scale the offset to match the animation scale
        double scaledOffsetY = skeletonOffsetY * animationScale;
        skeleton.setPosition(posX, posY - scaledOffsetY);
        skeleton.setScale(animationScale);
        skeleton.setFlipX(!facingRight);

        // Flash when hurt
        if (invincibilityTimer > 0) {
            // Simple flash effect by skipping some frames
            if ((int)(invincibilityTimer * 10) % 2 == 0) {
                skeleton.draw(g2d);
            }
        } else {
            skeleton.draw(g2d);
        }

        // Debug hitbox
        if (debugDraw) {
            g2d.setColor(new Color(255, 0, 0, 100));
            Rectangle bounds = getBounds();
            g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            g2d.setColor(Color.RED);
            g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

            // Draw state and health
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString(currentState.name(), (int)posX - 20, (int)posY - hitboxHeight - 20);
            g2d.drawString("HP: " + currentHealth + "/" + maxHealth, (int)posX - 20, (int)posY - hitboxHeight - 10);

            // Draw detection range
            g2d.setColor(new Color(0, 255, 0, 50));
            g2d.drawOval((int)(posX - detectionRange), (int)(posY - detectionRange),
                        (int)(detectionRange * 2), (int)(detectionRange * 2));
        }
    }

    // ==================== AI System ====================

    /**
     * Finds the player target from entity list.
     */
    protected void findTarget(List<Entity> entities) {
        for (Entity e : entities) {
            if (e instanceof PlayerBase) {
                target = (PlayerBase) e;
                targetLastX = target.getX();
                targetLastY = target.getY();
                break;
            }
        }
    }

    /**
     * Updates the AI state machine.
     */
    protected void updateAI(double deltaTime) {
        // Check for state transitions
        checkStateTransitions();

        // Execute current state behavior
        switch (currentState) {
            case IDLE:
                updateIdleState(deltaTime);
                break;
            case WANDER:
                updateWanderState(deltaTime);
                break;
            case CHASE:
                updateChaseState(deltaTime);
                break;
            case ATTACK:
                updateAttackState(deltaTime);
                break;
            case FLEE:
                updateFleeState(deltaTime);
                break;
            case HURT:
                updateHurtState(deltaTime);
                break;
            case DEAD:
                updateDeadState(deltaTime);
                break;
        }
    }

    /**
     * Checks for AI state transitions.
     */
    protected void checkStateTransitions() {
        if (currentState == AIState.HURT || currentState == AIState.DEAD) {
            return; // Don't interrupt these states
        }

        double distanceToTarget = getDistanceToTarget();

        // Check for chase trigger
        if (target != null && distanceToTarget < detectionRange) {
            if (currentState != AIState.CHASE && currentState != AIState.ATTACK) {
                changeState(AIState.CHASE);
            }
        }

        // Check for attack trigger
        if (target != null && distanceToTarget < attackRange && attackTimer <= 0) {
            if (currentState == AIState.CHASE) {
                changeState(AIState.ATTACK);
            }
        }

        // Check for losing target
        if (currentState == AIState.CHASE && distanceToTarget > loseTargetRange) {
            changeState(AIState.WANDER);
        }
    }

    /**
     * Changes the AI state.
     */
    protected void changeState(AIState newState) {
        if (newState == currentState) return;

        previousState = currentState;
        currentState = newState;
        stateTimer = 0;

        // State entry actions
        switch (newState) {
            case IDLE:
                velocityX = 0;
                if (skeleton != null) skeleton.transitionTo("idle", 0.2);
                break;
            case WANDER:
                // Update wander bounds to center around current position
                // This prevents mobs from walking huge distances after losing a target
                double wanderRadius = (wanderMaxX - wanderMinX) / 2;
                wanderMinX = posX - wanderRadius;
                wanderMaxX = posX + wanderRadius;
                pickWanderTarget();
                // Set initial velocity toward target
                double dxToTarget = wanderTargetX - posX;
                if (Math.abs(dxToTarget) >= 10) {
                    facingRight = dxToTarget > 0;
                    velocityX = facingRight ? wanderSpeed : -wanderSpeed;
                }
                if (skeleton != null) skeleton.transitionTo("walk", 0.2);
                break;
            case CHASE:
                if (skeleton != null) skeleton.transitionTo("run", 0.2);
                break;
            case ATTACK:
                velocityX = 0;
                if (skeleton != null) skeleton.transitionTo("attack", 0.1);
                break;
            case HURT:
                if (skeleton != null) skeleton.transitionTo("hurt", 0.1);
                break;
            case DEAD:
                velocityX = 0;
                if (skeleton != null) skeleton.transitionTo("death", 0.1);
                break;
            case FLEE:
                if (skeleton != null) skeleton.transitionTo("run", 0.2);
                break;
        }
    }

    // ==================== State Update Methods ====================

    protected void updateIdleState(double deltaTime) {
        velocityX = 0;

        if (stateTimer > idleDuration) {
            changeState(AIState.WANDER);
        }
    }

    protected void updateWanderState(double deltaTime) {
        // Move toward wander target
        double dx = wanderTargetX - posX;

        // Debug output - remove after fixing
        if (debugDraw) {
            System.out.println("WANDER: pos=" + (int)posX + " target=" + (int)wanderTargetX +
                               " dx=" + (int)dx + " vX=" + (int)velocityX + " speed=" + (int)wanderSpeed);
        }

        if (Math.abs(dx) < 10) {
            // Reached target, pick a new one or go idle
            pickWanderTarget();
            dx = wanderTargetX - posX;
            if (Math.abs(dx) < 10) {
                // Still too close, go idle
                changeState(AIState.IDLE);
                return;
            }
        }

        // Move toward target
        facingRight = dx > 0;
        velocityX = facingRight ? wanderSpeed : -wanderSpeed;

        if (stateTimer > wanderDuration) {
            changeState(AIState.IDLE);
        }
    }

    protected void updateChaseState(double deltaTime) {
        if (target == null) {
            changeState(AIState.WANDER);
            return;
        }

        double dx = target.getX() - posX;
        facingRight = dx > 0;
        velocityX = facingRight ? chaseSpeed : -chaseSpeed;
    }

    protected void updateAttackState(double deltaTime) {
        if (stateTimer > 0.3 && attackTimer <= 0) {
            performAttack();
            attackTimer = attackCooldown;
        }

        // Return to chase after attack animation
        if (stateTimer > 0.5) {
            changeState(AIState.CHASE);
        }
    }

    protected void updateFleeState(double deltaTime) {
        if (target == null) {
            changeState(AIState.WANDER);
            return;
        }

        // Run away from target
        double dx = target.getX() - posX;
        facingRight = dx < 0; // Face away
        velocityX = facingRight ? chaseSpeed : -chaseSpeed;

        // Stop fleeing after some time
        if (stateTimer > 3.0) {
            changeState(AIState.WANDER);
        }
    }

    protected void updateHurtState(double deltaTime) {
        velocityX *= 0.8; // Slow down during hurt

        // Return to previous state after hurt animation
        if (stateTimer > 0.3) {
            changeState(previousState);
        }
    }

    protected void updateDeadState(double deltaTime) {
        velocityX = 0;
        // Entity will be removed by the game after death animation
    }

    // ==================== Physics ====================

    protected void applyPhysics(double deltaTime, List<Entity> entities) {
        // Apply gravity
        if (!onGround) {
            velocityY += GRAVITY;
        }

        // Debug: log movement when in WANDER state
        if (debugDraw && currentState == AIState.WANDER && velocityX != 0) {
            double movement = velocityX * deltaTime;
            System.out.println("PHYSICS: vX=" + (int)velocityX + " dt=" + deltaTime +
                               " movement=" + movement + " oldX=" + (int)posX);
        }

        // Calculate new positions
        double newX = posX + velocityX * deltaTime;
        double newY = posY + velocityY * deltaTime;

        // Check horizontal collision with solid blocks BEFORE applying movement
        if (entities != null && velocityX != 0) {
            Rectangle futureXBounds = new Rectangle(
                (int)newX + hitboxOffsetX,
                (int)posY + hitboxOffsetY,
                hitboxWidth,
                hitboxHeight
            );

            boolean xCollision = false;
            for (Entity e : entities) {
                if (e instanceof BlockEntity) {
                    BlockEntity block = (BlockEntity) e;
                    if (block.isSolid() && futureXBounds.intersects(block.getBounds())) {
                        xCollision = true;
                        break;
                    }
                }
            }

            // Only apply horizontal movement if no collision
            if (!xCollision) {
                posX = newX;
            } else {
                velocityX = 0; // Stop horizontal movement on collision
            }
        } else {
            posX = newX;
        }

        // Check vertical collision with solid blocks BEFORE applying movement
        if (entities != null) {
            Rectangle futureYBounds = new Rectangle(
                (int)posX + hitboxOffsetX,
                (int)newY + hitboxOffsetY,
                hitboxWidth,
                hitboxHeight
            );

            boolean yCollision = false;
            double landingY = groundY;

            for (Entity e : entities) {
                if (e instanceof BlockEntity) {
                    BlockEntity block = (BlockEntity) e;
                    if (block.isSolid() && futureYBounds.intersects(block.getBounds())) {
                        Rectangle blockBounds = block.getBounds();
                        yCollision = true;

                        if (velocityY > 0) {
                            // Falling down - land on top of block
                            landingY = Math.min(landingY, blockBounds.y);
                        } else if (velocityY < 0) {
                            // Jumping up - hit head on bottom of block
                            newY = blockBounds.y + blockBounds.height - hitboxOffsetY;
                            velocityY = 0;
                        }
                    }
                }
            }

            if (yCollision && velocityY > 0) {
                // Land on the block
                posY = landingY;
                velocityY = 0;
                onGround = true;
            } else {
                posY = newY;
                // Check if on ground
                if (posY >= groundY) {
                    posY = groundY;
                    velocityY = 0;
                    onGround = true;
                } else {
                    onGround = false;
                }
            }
        } else {
            posY = newY;
            // Simple ground check when no entities
            if (posY >= groundY) {
                posY = groundY;
                velocityY = 0;
                onGround = true;
            } else {
                onGround = false;
            }
        }

        // Soft constraint to wander bounds when wandering (no teleporting)
        // Only gently push back if far outside bounds
        if (currentState == AIState.WANDER || currentState == AIState.IDLE) {
            if (posX < wanderMinX - 50) {
                // Too far left, pick a target back in bounds
                wanderTargetX = wanderMinX + 50;
                facingRight = true;
            } else if (posX > wanderMaxX + 50) {
                // Too far right, pick a target back in bounds
                wanderTargetX = wanderMaxX - 50;
                facingRight = false;
            }
        }
    }

    /**
     * @deprecated Use the collision checking in applyPhysics instead.
     * This method is kept for backwards compatibility but no longer used.
     */
    @Deprecated
    protected void checkGroundCollision(List<Entity> entities) {
        // Ground collision is now handled in applyPhysics()
        // This method is kept for subclasses that might override it
    }

    // ==================== Animation ====================

    protected void updateAnimation(double deltaTime) {
        if (skeleton != null) {
            skeleton.update(deltaTime);
        }
    }

    // ==================== Combat ====================

    /**
     * Applies damage to this mob.
     *
     * @param damage Amount of damage
     * @param knockbackX Knockback force X
     * @param knockbackY Knockback force Y
     */
    public void takeDamage(int damage, double knockbackX, double knockbackY) {
        if (invincibilityTimer > 0 || currentState == AIState.DEAD) {
            return;
        }

        currentHealth -= damage;
        invincibilityTimer = invincibilityTime;

        // Apply knockback
        velocityX += knockbackX;
        velocityY += knockbackY;

        if (currentHealth <= 0) {
            currentHealth = 0;
            changeState(AIState.DEAD);
        } else {
            changeState(AIState.HURT);
        }
    }

    /**
     * Checks if the mob is dead.
     */
    public boolean isDead() {
        return currentState == AIState.DEAD && stateTimer > 1.0;
    }

    // ==================== Helper Methods ====================

    protected double getDistanceToTarget() {
        if (target == null) return Double.MAX_VALUE;

        // Get the player's hitbox bounds
        Rectangle playerBounds = target.getBounds();

        // Get mob's hitbox center for more accurate distance calculation
        Rectangle mobBounds = getBounds();
        double mobCenterX = mobBounds.x + mobBounds.width / 2.0;
        double mobCenterY = mobBounds.y + mobBounds.height / 2.0;

        // Calculate distance to the nearest edge of the player's hitbox
        // This makes attacks feel more natural - mobs attack when close to the actual hitbox
        double nearestX;
        double nearestY;

        // Find nearest X point on player hitbox
        if (mobCenterX < playerBounds.x) {
            nearestX = playerBounds.x; // Left edge
        } else if (mobCenterX > playerBounds.x + playerBounds.width) {
            nearestX = playerBounds.x + playerBounds.width; // Right edge
        } else {
            nearestX = mobCenterX; // Inside hitbox horizontally
        }

        // Find nearest Y point on player hitbox
        if (mobCenterY < playerBounds.y) {
            nearestY = playerBounds.y; // Top edge
        } else if (mobCenterY > playerBounds.y + playerBounds.height) {
            nearestY = playerBounds.y + playerBounds.height; // Bottom edge
        } else {
            nearestY = mobCenterY; // Inside hitbox vertically
        }

        double dx = nearestX - mobCenterX;
        double dy = nearestY - mobCenterY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Gets the distance to the front or back edge of the target's hitbox.
     * Mobs should target the edge facing them, not the center.
     */
    protected double getDistanceToTargetFace() {
        if (target == null) return Double.MAX_VALUE;

        Rectangle playerBounds = target.getBounds();

        // Get mob's hitbox center for more accurate distance calculation
        Rectangle mobBounds = getBounds();
        double mobCenterX = mobBounds.x + mobBounds.width / 2.0;
        double mobCenterY = mobBounds.y + mobBounds.height / 2.0;

        // Determine which edge to target based on mob position
        double targetX;
        if (mobCenterX < playerBounds.x + playerBounds.width / 2) {
            // Mob is to the left, target player's left edge (their back if facing right)
            targetX = playerBounds.x;
        } else {
            // Mob is to the right, target player's right edge
            targetX = playerBounds.x + playerBounds.width;
        }

        // Use player's vertical center for Y
        double targetY = playerBounds.y + playerBounds.height / 2;

        double dx = targetX - mobCenterX;
        double dy = targetY - mobCenterY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    protected void pickWanderTarget() {
        // Pick a random point within wander bounds
        // Ensure target is at least minDistance away from current position
        double minDistance = 50;
        double range = wanderMaxX - wanderMinX;

        // If wander bounds are too small, just pick a direction
        if (range < minDistance * 2) {
            // Pick a random direction and go that way
            wanderTargetX = posX + (Math.random() > 0.5 ? 100 : -100);
            return;
        }

        // Try to find a target at least minDistance away
        for (int attempts = 0; attempts < 10; attempts++) {
            wanderTargetX = wanderMinX + Math.random() * range;
            if (Math.abs(wanderTargetX - posX) >= minDistance) {
                return;  // Found a good target
            }
        }

        // If all attempts failed, pick the far edge of bounds
        double distToMin = Math.abs(posX - wanderMinX);
        double distToMax = Math.abs(wanderMaxX - posX);
        wanderTargetX = (distToMin > distToMax) ? wanderMinX + 20 : wanderMaxX - 20;
    }

    // ==================== Getters and Setters ====================

    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public int getAttackDamage() { return attackDamage; }
    public AIState getState() { return currentState; }
    public boolean isFacingRight() { return facingRight; }

    public void setWanderBounds(double minX, double maxX) {
        this.wanderMinX = minX;
        this.wanderMaxX = maxX;
    }

    public void setDebugDraw(boolean debug) {
        this.debugDraw = debug;
        if (skeleton != null) {
            skeleton.setDebugDraw(debug);
        }
    }

    public void setGroundY(double groundY) {
        this.groundY = groundY;
    }

    /**
     * Sets the detection/aggro range for this mob.
     * @param range Detection range in pixels
     */
    public void setDetectionRange(double range) {
        this.detectionRange = range;
    }

    /**
     * Sets whether this mob is hostile (will actively seek and attack players).
     * @param hostile true if hostile, false for passive/neutral
     */
    public void setHostile(boolean hostile) {
        if (hostile) {
            // Hostile mobs have larger detection range
            if (this.detectionRange < 200) {
                this.detectionRange = 200;
            }
        } else {
            // Passive mobs don't detect players
            this.detectionRange = 0;
        }
    }

    /**
     * Sets the aggro range (alias for setDetectionRange).
     * @param range Aggro range in pixels
     */
    public void setAggroRange(double range) {
        this.detectionRange = range;
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public int getX() { return (int)posX; }

    public int getY() { return (int)posY; }
}
