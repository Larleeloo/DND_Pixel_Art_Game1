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

    // Physics constants
    protected static final double GRAVITY = 0.5;
    protected double groundY = 920;  // Ground level from level data

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

        // Position skeleton at mob's feet
        skeleton.setPosition(posX, posY);
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
                pickWanderTarget();
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

        if (Math.abs(dx) < 10) {
            // Reached target
            changeState(AIState.IDLE);
        } else {
            // Move toward target
            facingRight = dx > 0;
            velocityX = facingRight ? wanderSpeed : -wanderSpeed;
        }

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

        // Apply velocity
        posX += velocityX * deltaTime;
        posY += velocityY * deltaTime;

        // Constrain to wander bounds when wandering
        if (currentState == AIState.WANDER) {
            posX = Math.max(wanderMinX, Math.min(wanderMaxX, posX));
        }

        // Ground collision
        checkGroundCollision(entities);
    }

    protected void checkGroundCollision(List<Entity> entities) {
        double groundLevel = groundY;

        // Check for block collisions
        if (entities != null) {
            Rectangle futureBounds = new Rectangle(
                (int)posX + hitboxOffsetX,
                (int)posY + hitboxOffsetY + (int)velocityY,
                hitboxWidth,
                hitboxHeight
            );

            for (Entity e : entities) {
                if (e instanceof BlockEntity) {
                    Rectangle blockBounds = e.getBounds();
                    if (futureBounds.intersects(blockBounds) && velocityY > 0) {
                        groundLevel = Math.min(groundLevel, blockBounds.y);
                    }
                }
            }
        }

        // Simple ground check
        if (posY >= groundLevel) {
            posY = groundLevel;
            velocityY = 0;
            onGround = true;
        } else {
            onGround = false;
        }
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
        double dx = target.getX() - posX;
        double dy = target.getY() - posY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    protected void pickWanderTarget() {
        // Pick a random point within wander bounds
        wanderTargetX = wanderMinX + Math.random() * (wanderMaxX - wanderMinX);
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

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public int getX() { return (int)posX; }

    public int getY() { return (int)posY; }
}
