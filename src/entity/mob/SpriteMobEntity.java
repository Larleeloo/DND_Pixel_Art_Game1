package entity.mob;
import entity.*;
import entity.player.*;
import block.*;
import animation.*;
import graphics.*;

import java.awt.*;
import java.util.List;

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

        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Loads animations from the sprite directory.
     *
     * @param dir Directory containing animation GIFs
     */
    protected void loadAnimations(String dir) {
        // Try to load standard mob animations using path-based loading
        String[] actions = {"idle", "walk", "run", "attack", "hurt", "death"};

        for (String action : actions) {
            String path = dir + "/" + action + ".gif";
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                SpriteAnimation.ActionState state = SpriteAnimation.ActionState.IDLE;
                switch (action) {
                    case "idle": state = SpriteAnimation.ActionState.IDLE; break;
                    case "walk": state = SpriteAnimation.ActionState.WALK; break;
                    case "run": state = SpriteAnimation.ActionState.RUN; break;
                    case "attack": state = SpriteAnimation.ActionState.ATTACK; break;
                    case "hurt": state = SpriteAnimation.ActionState.HURT; break;
                    case "death": state = SpriteAnimation.ActionState.DEAD; break;
                }
                // Use loadAction with path (String) - it handles loading internally
                if (spriteAnimation.loadAction(state, path)) {
                    System.out.println("SpriteMobEntity: Loaded animation: " + action + " from " + path);
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
        spriteAnimation.loadAction(SpriteAnimation.ActionState.IDLE, anim);

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
        // Basic attack behavior - can be overridden in subclasses
        if (target != null && attackTimer <= 0) {
            double dist = getDistanceToTargetFace();
            if (dist <= attackRange) {
                target.takeDamage(attackDamage);
                attackTimer = attackCooldown;
                setAnimationState("attack");
                System.out.println("SpriteMobEntity: Attacked player for " + attackDamage + " damage");
            }
        }
    }

    // ==================== Animation State ====================

    /**
     * Sets the current animation state.
     *
     * @param state Animation state name (idle, walk, run, attack, hurt, death)
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
                case "attack":
                    actionState = SpriteAnimation.ActionState.ATTACK;
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

        // Update sprite animation
        spriteAnimation.update(elapsed);

        // Update animation state based on AI state
        updateAnimationFromAIState();

        // Call parent update for AI and physics
        super.update(deltaTime, entities);

        // Sync entity position with mob position
        this.x = (int)posX;
        this.y = (int)posY;
    }

    /**
     * Updates the animation state based on current AI state.
     */
    protected void updateAnimationFromAIState() {
        switch (currentState) {
            case IDLE:
                setAnimationState("idle");
                break;
            case WANDER:
                setAnimationState("walk");
                break;
            case CHASE:
                setAnimationState("run");
                break;
            case ATTACK:
                setAnimationState("attack");
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
        if (currentHealth >= maxHealth) return;

        int barWidth = hitboxWidth;
        int barHeight = 4;
        int barX = (int)posX - barWidth / 2;
        int barY = (int)posY - spriteHeight - 10;

        // Background
        g2d.setColor(new Color(50, 50, 50, 180));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        // Health fill
        double healthPercent = (double)currentHealth / maxHealth;
        int fillWidth = (int)(barWidth * healthPercent);
        Color healthColor = healthPercent > 0.5 ? Color.GREEN :
                           healthPercent > 0.25 ? Color.YELLOW : Color.RED;
        g2d.setColor(healthColor);
        g2d.fillRect(barX, barY, fillWidth, barHeight);

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
