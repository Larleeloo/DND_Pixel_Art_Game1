package entity.mob;
import entity.*;
import entity.player.*;
import block.*;
import animation.*;
import graphics.*;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Base class for sprite-based AI-controlled mobs.
 * Uses SpriteAnimation for GIF-based animations instead of bone-based Skeleton.
 *
 * This is an alternative to the bone-based MobEntity that supports:
 * - GIF animations for idle, walk, run, attack, hurt, death actions
 * - Proper hitbox collision detection
 * - AI state machine for mob behavior
 *
 * Expected sprite files in mob directory:
 *   - idle.gif (standing/breathing animation)
 *   - walk.gif (walking animation)
 *   - run.gif (optional, running/chasing animation)
 *   - attack.gif (optional, attack animation)
 *   - hurt.gif (optional, damage reaction)
 *   - death.gif (optional, death animation)
 *
 * Usage in level JSON:
 *   "mobType": "sprite_wolf",
 *   "spriteDir": "assets/mobs/wolf/sprites"
 */
public class SpriteMobEntity extends MobEntity {

    // Sprite animation system
    protected SpriteAnimation spriteAnimation;
    protected String spriteDir;

    // Animation state
    protected String currentAnimState = "idle";
    protected long lastUpdateTime;

    // Visual dimensions (from sprite)
    protected int spriteWidth;
    protected int spriteHeight;
    protected static final int SCALE = 2;

    // Multi-jump system
    protected int maxJumps = 1;           // Default to single jump
    protected int jumpsRemaining = 1;
    protected int currentJumpNumber = 0;
    protected double doubleJumpStrength = -8;
    protected double tripleJumpStrength = -7;

    // Sprint system
    protected boolean isSprinting = false;
    protected double sprintSpeed = 0;      // Set by subclasses

    // Ranged attack system
    protected boolean canFireProjectiles = false;
    protected ProjectileEntity.ProjectileType projectileType;
    protected int projectileDamage = 5;
    protected double projectileSpeed = 12.0;
    protected double projectileCooldown = 2.0;
    protected double projectileTimer = 0;
    protected double preferredAttackRange = 200; // Range to start firing
    protected List<ProjectileEntity> activeProjectiles = new ArrayList<>();

    // Eating animation (for herbivore mobs)
    protected boolean isEating = false;
    protected double eatTimer = 0;

    /**
     * Creates a sprite-based mob entity.
     *
     * @param x Initial X position
     * @param y Initial Y position
     * @param spriteDir Directory containing animation GIFs
     */
    public SpriteMobEntity(int x, int y, String spriteDir) {
        super(x, y);
        this.spriteDir = spriteDir;

        // Initialize sprite animation
        this.spriteAnimation = new SpriteAnimation();
        loadAnimations(spriteDir);

        // Set dimensions from sprite
        this.spriteWidth = spriteAnimation.getBaseWidth() * SCALE;
        this.spriteHeight = spriteAnimation.getBaseHeight() * SCALE;

        // Set hitbox based on sprite size (slightly smaller for better gameplay feel)
        this.hitboxWidth = (int)(spriteWidth * 0.8);
        this.hitboxHeight = (int)(spriteHeight * 0.9);
        this.hitboxOffsetX = -hitboxWidth / 2;
        this.hitboxOffsetY = -hitboxHeight;

        // Larger sprites need larger attack range
        this.attackRange = 80;

        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Loads animations from the sprite directory.
     *
     * @param dir Directory containing animation GIFs
     */
    protected void loadAnimations(String dir) {
        // Map of file names to action states (extended for new animations)
        java.util.Map<String, SpriteAnimation.ActionState> actionMap = new java.util.HashMap<>();
        actionMap.put("idle", SpriteAnimation.ActionState.IDLE);
        actionMap.put("walk", SpriteAnimation.ActionState.WALK);
        actionMap.put("run", SpriteAnimation.ActionState.RUN);
        actionMap.put("sprint", SpriteAnimation.ActionState.SPRINT);
        actionMap.put("jump", SpriteAnimation.ActionState.JUMP);
        actionMap.put("double_jump", SpriteAnimation.ActionState.DOUBLE_JUMP);
        actionMap.put("triple_jump", SpriteAnimation.ActionState.TRIPLE_JUMP);
        actionMap.put("fall", SpriteAnimation.ActionState.FALL);
        actionMap.put("attack", SpriteAnimation.ActionState.ATTACK);
        actionMap.put("fire", SpriteAnimation.ActionState.FIRE);
        actionMap.put("cast", SpriteAnimation.ActionState.CAST);
        actionMap.put("eat", SpriteAnimation.ActionState.EAT);
        actionMap.put("hurt", SpriteAnimation.ActionState.HURT);
        actionMap.put("death", SpriteAnimation.ActionState.DEAD);

        for (java.util.Map.Entry<String, SpriteAnimation.ActionState> entry : actionMap.entrySet()) {
            String path = dir + "/" + entry.getKey() + ".gif";
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                if (spriteAnimation.loadAction(entry.getValue(), path)) {
                    System.out.println("SpriteMobEntity: Loaded animation: " + entry.getKey() + " from " + path);
                }
            }
        }

        // Fallback: if no animations loaded, create placeholder
        if (spriteAnimation.getBaseWidth() <= 0) {
            System.err.println("SpriteMobEntity: No animations loaded from " + dir + ", using placeholder");
            createPlaceholderSprite();
        }
    }

    /**
     * Creates a placeholder sprite for testing.
     */
    protected void createPlaceholderSprite() {
        // Create a simple colored rectangle as placeholder
        int w = 32, h = 48;
        java.awt.image.BufferedImage placeholder = new java.awt.image.BufferedImage(
            w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();
        g.setColor(new Color(150, 100, 100, 200));
        g.fillRect(0, 0, w, h);
        g.setColor(Color.RED);
        g.fillOval(w/4, h/8, w/2, w/2);
        g.dispose();

        java.util.List<java.awt.image.BufferedImage> frames = new java.util.ArrayList<>();
        frames.add(placeholder);
        java.util.List<Integer> delays = new java.util.ArrayList<>();
        delays.add(100);
        AnimatedTexture anim = new AnimatedTexture(frames, delays);
        // Use setAction (not loadAction) to set AnimatedTexture directly
        spriteAnimation.setAction(SpriteAnimation.ActionState.IDLE, anim);

        this.spriteWidth = w * SCALE;
        this.spriteHeight = h * SCALE;
    }

    // ==================== Override Abstract Methods ====================

    @Override
    protected void createSkeleton() {
        // Not used for sprite-based mobs - skeleton is null
        this.skeleton = null;
    }

    @Override
    protected void setupAnimations() {
        // Animations are loaded in loadAnimations() instead
    }

    @Override
    protected void performAttack() {
        if (target == null) return;

        double dist = getDistanceToTargetFace();

        // Check for ranged attack first
        if (canFireProjectiles && dist <= preferredAttackRange && projectileTimer <= 0) {
            fireProjectile();
            return;
        }

        // Melee attack if in range
        if (attackTimer <= 0 && dist <= attackRange) {
            // Calculate knockback direction based on mob position relative to player
            Rectangle playerBounds = target.getBounds();
            double playerCenterX = playerBounds.x + playerBounds.width / 2;
            double knockbackDir = posX < playerCenterX ? 1 : -1;
            target.takeDamage(attackDamage, knockbackDir * 5, -3);
            attackTimer = attackCooldown;
            setAnimationState("attack");
            System.out.println("SpriteMobEntity: Attacked player for " + attackDamage + " damage");
        }
    }

    /**
     * Fires a projectile at the target.
     */
    protected void fireProjectile() {
        if (target == null || projectileType == null) return;

        // Calculate direction to target
        Rectangle targetBounds = target.getBounds();
        double targetCenterX = targetBounds.x + targetBounds.width / 2;
        double targetCenterY = targetBounds.y + targetBounds.height / 2;

        double dx = targetCenterX - posX;
        double dy = targetCenterY - (posY - spriteHeight / 2);
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length > 0) {
            dx /= length;
            dy /= length;
        }

        // Create projectile
        int projX = (int)posX;
        int projY = (int)(posY - spriteHeight / 2);

        ProjectileEntity projectile = new ProjectileEntity(
            projX, projY, projectileType, projectileDamage,
            dx * projectileSpeed, dy * projectileSpeed, false
        );
        projectile.setSource(this);
        activeProjectiles.add(projectile);

        projectileTimer = projectileCooldown;
        setAnimationState("fire");

        System.out.println("SpriteMobEntity: Fired projectile at player");
    }

    /**
     * Configures this mob for ranged attacks.
     */
    public void setRangedAttack(ProjectileEntity.ProjectileType type, int damage, double speed, double cooldown, double range) {
        this.canFireProjectiles = true;
        this.projectileType = type;
        this.projectileDamage = damage;
        this.projectileSpeed = speed;
        this.projectileCooldown = cooldown;
        this.preferredAttackRange = range;
    }

    /**
     * Sets the maximum number of jumps for this mob.
     */
    public void setMaxJumps(int jumps) {
        this.maxJumps = Math.max(1, Math.min(3, jumps));
        this.jumpsRemaining = this.maxJumps;
    }

    /**
     * Sets the sprint speed for this mob.
     */
    public void setSprintSpeed(double speed) {
        this.sprintSpeed = speed;
    }

    // ==================== Animation State ====================

    /**
     * Sets the current animation state.
     *
     * @param state Animation state name (idle, walk, run, sprint, attack, fire, hurt, death, eat)
     */
    protected void setAnimationState(String state) {
        if (!state.equals(currentAnimState)) {
            currentAnimState = state;
            SpriteAnimation.ActionState actionState = SpriteAnimation.ActionState.IDLE;
            switch (state) {
                case "walk":
                    actionState = SpriteAnimation.ActionState.WALK;
                    break;
                case "run":
                case "chase":
                    actionState = SpriteAnimation.ActionState.RUN;
                    break;
                case "sprint":
                    actionState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.SPRINT)
                            ? SpriteAnimation.ActionState.SPRINT
                            : SpriteAnimation.ActionState.RUN;
                    break;
                case "jump":
                    actionState = SpriteAnimation.ActionState.JUMP;
                    break;
                case "double_jump":
                    actionState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.DOUBLE_JUMP)
                            ? SpriteAnimation.ActionState.DOUBLE_JUMP
                            : SpriteAnimation.ActionState.JUMP;
                    break;
                case "fall":
                    actionState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.FALL)
                            ? SpriteAnimation.ActionState.FALL
                            : SpriteAnimation.ActionState.JUMP;
                    break;
                case "attack":
                    actionState = SpriteAnimation.ActionState.ATTACK;
                    break;
                case "fire":
                    actionState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.FIRE)
                            ? SpriteAnimation.ActionState.FIRE
                            : SpriteAnimation.ActionState.ATTACK;
                    break;
                case "cast":
                    actionState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.CAST)
                            ? SpriteAnimation.ActionState.CAST
                            : SpriteAnimation.ActionState.ATTACK;
                    break;
                case "eat":
                    actionState = spriteAnimation.hasAnimation(SpriteAnimation.ActionState.EAT)
                            ? SpriteAnimation.ActionState.EAT
                            : SpriteAnimation.ActionState.IDLE;
                    break;
                case "hurt":
                    actionState = SpriteAnimation.ActionState.HURT;
                    break;
                case "death":
                    actionState = SpriteAnimation.ActionState.DEAD;
                    break;
                default:
                    actionState = SpriteAnimation.ActionState.IDLE;
            }
            spriteAnimation.setState(actionState);
        }
    }

    // ==================== Update and Draw ====================

    @Override
    public void update(double deltaTime, List<Entity> entities) {
        // Update timers
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Update projectile timer
        if (projectileTimer > 0) {
            projectileTimer -= deltaTime;
        }

        // Update eating timer
        if (isEating) {
            eatTimer -= deltaTime;
            if (eatTimer <= 0) {
                isEating = false;
            }
        }

        // Determine if sprinting (when chasing and has sprint speed set)
        isSprinting = (currentState == AIState.CHASE && sprintSpeed > 0);

        // Update sprite animation
        spriteAnimation.update(elapsed);

        // Update animation state based on AI state
        updateAnimationFromAIState();

        // Call parent update for AI and physics
        super.update(deltaTime, entities);

        // Update projectiles
        updateProjectiles(deltaTime, entities);

        // Sync entity position with mob position
        this.x = (int)posX;
        this.y = (int)posY;
    }

    /**
     * Updates active projectiles.
     */
    protected void updateProjectiles(double deltaTime, List<Entity> entities) {
        Iterator<ProjectileEntity> iterator = activeProjectiles.iterator();
        while (iterator.hasNext()) {
            ProjectileEntity proj = iterator.next();
            proj.update(deltaTime, entities);

            if (!proj.isActive()) {
                iterator.remove();
                entities.remove(proj);
            }
        }
    }

    /**
     * Gets active projectiles for drawing.
     */
    public List<ProjectileEntity> getActiveProjectiles() {
        return activeProjectiles;
    }

    /**
     * Updates the animation state based on current AI state.
     */
    protected void updateAnimationFromAIState() {
        // Priority: eating > firing > special states
        if (isEating) {
            setAnimationState("eat");
            return;
        }

        switch (currentState) {
            case IDLE:
                setAnimationState("idle");
                break;
            case WANDER:
                setAnimationState("walk");
                break;
            case CHASE:
                // Use sprint if available and configured, otherwise run
                if (isSprinting && spriteAnimation.hasAnimation(SpriteAnimation.ActionState.SPRINT)) {
                    setAnimationState("sprint");
                } else {
                    setAnimationState("run");
                }
                break;
            case ATTACK:
                // Check if we're in ranged or melee mode
                if (canFireProjectiles && projectileTimer > projectileCooldown - 0.3) {
                    setAnimationState("fire");
                } else {
                    setAnimationState("attack");
                }
                break;
            case FLEE:
                setAnimationState("run");
                break;
            case HURT:
                setAnimationState("hurt");
                break;
            case DEAD:
                setAnimationState("death");
                break;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (currentState == AIState.DEAD && stateTimer > 2.0) {
            return; // Don't draw if dead and animation complete
        }

        Graphics2D g2d = (Graphics2D) g;

        // Get current sprite frame
        java.awt.image.BufferedImage frame = spriteAnimation.getCurrentFrame();
        if (frame == null) return;

        // Calculate draw position
        int drawX = (int)posX - spriteWidth / 2;
        int drawY = (int)posY - spriteHeight;

        // Apply flip if facing left
        if (!facingRight) {
            g2d.drawImage(frame,
                drawX + spriteWidth, drawY, -spriteWidth, spriteHeight,
                null);
        } else {
            g2d.drawImage(frame,
                drawX, drawY, spriteWidth, spriteHeight,
                null);
        }

        // Draw invincibility flash effect
        if (invincibilityTimer > 0 && (int)(invincibilityTimer * 10) % 2 == 0) {
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillRect(drawX, drawY, spriteWidth, spriteHeight);
        }

        // Draw hitbox in debug mode
        if (debugDraw) {
            g2d.setColor(new Color(255, 0, 0, 100));
            Rectangle hitbox = getBounds();
            g2d.fillRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
            g2d.setColor(Color.RED);
            g2d.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);

            // Draw position marker
            g2d.setColor(Color.YELLOW);
            g2d.fillOval((int)posX - 3, (int)posY - 3, 6, 6);

            // Draw state text
            g2d.setColor(Color.WHITE);
            g2d.drawString(currentState.name(), (int)posX - 20, (int)posY - spriteHeight - 10);
            g2d.drawString("HP: " + currentHealth + "/" + maxHealth, (int)posX - 30, (int)posY - spriteHeight - 25);
        }

        // Draw health bar
        drawHealthBar(g2d);
    }

    /**
     * Draws a health bar above the mob.
     */
    protected void drawHealthBar(Graphics2D g2d) {
        // Always show health bar for mobs
        int barWidth = Math.max(hitboxWidth, 60);
        int barHeight = 6;
        int barX = (int)posX - barWidth / 2;
        int barY = (int)posY - spriteHeight - 12;

        // Background (dark red for lost health)
        g2d.setColor(new Color(100, 20, 20));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        // Health fill (bright red/green based on health)
        if (maxHealth > 0 && currentHealth > 0) {
            double healthPercent = (double)currentHealth / maxHealth;
            int fillWidth = (int)(barWidth * healthPercent);
            fillWidth = Math.max(fillWidth, 1); // At least 1 pixel if alive

            // Color gradient: green when healthy, yellow when hurt, red when critical
            Color healthColor;
            if (healthPercent > 0.6) {
                healthColor = new Color(50, 220, 50); // Green
            } else if (healthPercent > 0.3) {
                healthColor = new Color(220, 200, 50); // Yellow
            } else {
                healthColor = new Color(220, 50, 50); // Red
            }
            g2d.setColor(healthColor);
            g2d.fillRect(barX, barY, fillWidth, barHeight);
        }

        // Border
        g2d.setColor(Color.BLACK);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }

    // ==================== Hitbox Collision ====================

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
     * Gets the visual bounds for camera culling.
     */
    @Override
    public Rectangle getVisualBounds() {
        return new Rectangle(
            (int)posX - spriteWidth / 2 - 10,
            (int)posY - spriteHeight - 10,
            spriteWidth + 20,
            spriteHeight + 20
        );
    }

    /**
     * Sets the hitbox size.
     *
     * @param width Hitbox width
     * @param height Hitbox height
     */
    public void setHitboxSize(int width, int height) {
        this.hitboxWidth = width;
        this.hitboxHeight = height;
        this.hitboxOffsetX = -width / 2;
        this.hitboxOffsetY = -height;
    }

    /**
     * Checks collision with a block.
     *
     * @param block Block to check collision with
     * @return true if colliding
     */
    public boolean collidesWith(BlockEntity block) {
        if (block == null || !block.isSolid()) return false;
        return getBounds().intersects(block.getBounds());
    }

    /**
     * Checks collision with another entity.
     *
     * @param entity Entity to check collision with
     * @return true if colliding
     */
    public boolean collidesWith(Entity entity) {
        if (entity == null || entity == this) return false;
        return getBounds().intersects(entity.getBounds());
    }

    // ==================== Accessors ====================

    /**
     * Gets the sprite width.
     */
    public int getSpriteWidth() {
        return spriteWidth;
    }

    /**
     * Gets the sprite height.
     */
    public int getSpriteHeight() {
        return spriteHeight;
    }

    /**
     * Enables or disables debug drawing.
     */
    public void setDebugDraw(boolean debug) {
        this.debugDraw = debug;
    }

    @Override
    public String toString() {
        return "SpriteMobEntity{" +
                "pos=(" + (int)posX + "," + (int)posY + ")" +
                ", state=" + currentState +
                ", health=" + currentHealth + "/" + maxHealth +
                ", facing=" + (facingRight ? "R" : "L") +
                ", hitbox=" + hitboxWidth + "x" + hitboxHeight +
                "}";
    }
}
