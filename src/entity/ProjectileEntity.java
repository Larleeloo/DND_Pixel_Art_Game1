package entity;

import entity.player.*;
import entity.mob.*;
import block.*;
import graphics.*;
import animation.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * ProjectileEntity represents a fired or thrown projectile in the game world.
 * Projectiles have velocity, damage, and can hit entities or blocks.
 *
 * Features:
 * - GIF-based animated projectile sprites
 * - Velocity-based movement with optional gravity
 * - Collision detection with entities and blocks
 * - Damage dealing on hit
 * - Optional effects on impact (explosion, piercing, etc.)
 * - Trail effects for visual polish
 *
 * Usage:
 *   ProjectileEntity arrow = new ProjectileEntity(x, y, "assets/projectiles/arrow.gif",
 *       10, 15.0, -2.0, true);
 *   arrow.setGravity(0.3);
 */
public class ProjectileEntity extends Entity {

    // Projectile types
    public enum ProjectileType {
        ARROW,          // Arrows from bows
        BOLT,           // Crossbow bolts
        MAGIC_BOLT,     // Magic projectiles
        FIREBALL,       // Fire spell
        ICEBALL,        // Ice spell
        THROWING_KNIFE, // Thrown weapon
        THROWING_AXE,   // Thrown axe
        ROCK,           // Thrown rock
        POTION,         // Thrown potion
        BOMB            // Explosive
    }

    // Sprite and animation
    private AnimatedTexture animation;
    private BufferedImage staticSprite;
    private int width;
    private int height;
    private static final int SCALE = 2;

    // Movement
    private double velX;
    private double velY;
    private double gravity = 0.0;
    private double rotationAngle = 0;
    private boolean rotateWithVelocity = true;

    // Combat properties
    private int damage;
    private double knockbackForce = 5.0;
    private boolean fromPlayer; // true if fired by player, false if fired by mob
    private Entity source; // Entity that fired this projectile

    // Projectile behavior
    private ProjectileType type;
    private boolean piercing = false;
    private int pierceCount = 0;
    private int maxPierceCount = 1;
    private boolean explosive = false;
    private int explosionRadius = 0;
    private boolean active = true;
    private double lifetime = 5.0; // Seconds until despawn
    private double lifeTimer = 0;

    // Trail effect
    private boolean hasTrail = false;
    private Color trailColor = Color.WHITE;
    private java.util.List<Point> trailPoints = new java.util.ArrayList<>();
    private int maxTrailLength = 10;

    // Timing
    private long lastUpdateTime;

    /**
     * Creates a projectile entity.
     *
     * @param x Starting X position
     * @param y Starting Y position
     * @param spritePath Path to projectile sprite (PNG or GIF)
     * @param damage Damage dealt on hit
     * @param velX Horizontal velocity
     * @param velY Vertical velocity
     * @param fromPlayer True if player-fired, false if mob-fired
     */
    public ProjectileEntity(int x, int y, String spritePath, int damage,
                            double velX, double velY, boolean fromPlayer) {
        super(x, y);
        this.damage = damage;
        this.velX = velX;
        this.velY = velY;
        this.fromPlayer = fromPlayer;
        this.type = ProjectileType.ARROW;
        this.lastUpdateTime = System.currentTimeMillis();

        loadSprite(spritePath);
    }

    /**
     * Creates a projectile with a specific type.
     */
    public ProjectileEntity(int x, int y, ProjectileType type, int damage,
                            double velX, double velY, boolean fromPlayer) {
        super(x, y);
        this.damage = damage;
        this.velX = velX;
        this.velY = velY;
        this.fromPlayer = fromPlayer;
        this.type = type;
        this.lastUpdateTime = System.currentTimeMillis();

        // Set properties based on type
        configureForType(type);
    }

    /**
     * Configures projectile behavior based on type.
     */
    private void configureForType(ProjectileType type) {
        switch (type) {
            case ARROW:
                gravity = 0.3;
                rotateWithVelocity = true;
                hasTrail = false;
                createPlaceholderSprite(16, 4, new Color(139, 90, 43));
                break;
            case BOLT:
                gravity = 0.2;
                rotateWithVelocity = true;
                createPlaceholderSprite(18, 3, new Color(100, 100, 100));
                break;
            case MAGIC_BOLT:
                gravity = 0.0;
                rotateWithVelocity = false;
                hasTrail = true;
                trailColor = new Color(100, 100, 255);
                createPlaceholderSprite(12, 12, new Color(100, 150, 255));
                break;
            case FIREBALL:
                gravity = 0.0;
                hasTrail = true;
                trailColor = new Color(255, 100, 0);
                explosive = true;
                explosionRadius = 64;
                createPlaceholderSprite(16, 16, new Color(255, 100, 0));
                break;
            case ICEBALL:
                gravity = 0.1;
                hasTrail = true;
                trailColor = new Color(150, 200, 255);
                createPlaceholderSprite(14, 14, new Color(150, 200, 255));
                break;
            case THROWING_KNIFE:
                gravity = 0.4;
                rotateWithVelocity = true;
                createPlaceholderSprite(12, 4, new Color(150, 150, 150));
                break;
            case THROWING_AXE:
                gravity = 0.5;
                rotateWithVelocity = true;
                createPlaceholderSprite(16, 16, new Color(100, 100, 100));
                break;
            case ROCK:
                gravity = 0.6;
                createPlaceholderSprite(10, 10, new Color(100, 100, 100));
                break;
            case POTION:
                gravity = 0.5;
                explosive = true;
                explosionRadius = 48;
                createPlaceholderSprite(10, 14, new Color(100, 200, 100));
                break;
            case BOMB:
                gravity = 0.4;
                explosive = true;
                explosionRadius = 96;
                createPlaceholderSprite(14, 14, new Color(50, 50, 50));
                break;
        }
    }

    /**
     * Loads the projectile sprite.
     */
    private void loadSprite(String spritePath) {
        try {
            AssetLoader.ImageAsset asset = AssetLoader.load(spritePath);
            if (asset.animatedTexture != null) {
                this.animation = asset.animatedTexture;
                this.width = asset.width * SCALE;
                this.height = asset.height * SCALE;
            } else if (asset.staticImage != null) {
                this.staticSprite = asset.staticImage;
                this.width = asset.width * SCALE;
                this.height = asset.height * SCALE;
            } else {
                createPlaceholderSprite(8, 8, Color.YELLOW);
            }
        } catch (Exception e) {
            System.err.println("ProjectileEntity: Failed to load sprite: " + spritePath);
            createPlaceholderSprite(8, 8, Color.YELLOW);
        }
    }

    /**
     * Creates a placeholder sprite for the projectile.
     */
    private void createPlaceholderSprite(int w, int h, Color color) {
        this.width = w * SCALE;
        this.height = h * SCALE;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Draw projectile shape based on type
        if (type == ProjectileType.ARROW || type == ProjectileType.BOLT ||
            type == ProjectileType.THROWING_KNIFE) {
            // Arrow/bolt shape
            g.setColor(color);
            g.fillRect(0, h/4, w - 4, h/2);
            g.setColor(color.darker());
            int[] xPoints = {w - 4, w, w - 4};
            int[] yPoints = {0, h/2, h};
            g.fillPolygon(xPoints, yPoints, 3);
        } else if (type == ProjectileType.FIREBALL || type == ProjectileType.MAGIC_BOLT ||
                   type == ProjectileType.ICEBALL) {
            // Round projectile
            g.setColor(color);
            g.fillOval(0, 0, w, h);
            g.setColor(new Color(255, 255, 255, 100));
            g.fillOval(w/4, h/4, w/3, h/3);
        } else {
            // Generic round shape
            g.setColor(color);
            g.fillOval(0, 0, w, h);
        }

        g.dispose();
        this.staticSprite = img;
    }

    /**
     * Updates the projectile's position and checks for collisions.
     */
    public void update(double deltaTime, List<Entity> entities) {
        if (!active) return;

        // Update timer
        lifeTimer += deltaTime;
        if (lifeTimer >= lifetime) {
            active = false;
            return;
        }

        // Update animation
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;
        if (animation != null) {
            animation.update(elapsed);
        }

        // Store previous position for trail
        if (hasTrail) {
            trailPoints.add(0, new Point(x + width/2, y + height/2));
            while (trailPoints.size() > maxTrailLength) {
                trailPoints.remove(trailPoints.size() - 1);
            }
        }

        // Apply gravity
        velY += gravity;

        // Move projectile
        x += (int)velX;
        y += (int)velY;

        // Update rotation based on velocity
        if (rotateWithVelocity && (velX != 0 || velY != 0)) {
            rotationAngle = Math.atan2(velY, velX);
        }

        // Check collisions
        checkCollisions(entities);
    }

    /**
     * Checks for collisions with entities and blocks.
     */
    private void checkCollisions(List<Entity> entities) {
        if (!active) return;

        Rectangle bounds = getBounds();

        for (Entity entity : entities) {
            if (entity == this || entity == source) continue;

            // Skip if this projectile shouldn't hit this entity type
            if (fromPlayer && entity instanceof PlayerBase) continue;
            if (!fromPlayer && entity instanceof MobEntity) continue;

            // Check collision with mobs (if player projectile)
            if (fromPlayer && entity instanceof MobEntity) {
                MobEntity mob = (MobEntity) entity;
                if (bounds.intersects(mob.getBounds()) && mob.getCurrentHealth() > 0) {
                    // Deal damage
                    double knockbackDir = velX > 0 ? 1 : -1;
                    mob.takeDamage(damage, knockbackDir * knockbackForce, -knockbackForce / 2);
                    System.out.println("Projectile hit mob for " + damage + " damage");

                    // Handle piercing or deactivate
                    if (piercing && pierceCount < maxPierceCount) {
                        pierceCount++;
                        damage = (int)(damage * 0.7); // Reduce damage on pierce
                    } else {
                        onImpact();
                        return;
                    }
                }
            }

            // Check collision with player (if mob projectile)
            if (!fromPlayer && entity instanceof PlayerBase) {
                PlayerBase player = (PlayerBase) entity;
                if (bounds.intersects(player.getBounds()) && !player.isInvincible()) {
                    // Deal damage
                    double knockbackDir = velX > 0 ? 1 : -1;
                    player.takeDamage(damage, knockbackDir * knockbackForce, -knockbackForce / 2);
                    System.out.println("Projectile hit player for " + damage + " damage");
                    onImpact();
                    return;
                }
            }

            // Check collision with blocks
            if (entity instanceof BlockEntity) {
                BlockEntity block = (BlockEntity) entity;
                if (block.isSolid() && !block.isBroken() && bounds.intersects(block.getBounds())) {
                    onImpact();
                    return;
                }
            }
        }
    }

    /**
     * Called when projectile impacts something.
     */
    private void onImpact() {
        if (explosive) {
            // TODO: Create explosion effect and damage nearby entities
            System.out.println("Projectile exploded with radius " + explosionRadius);
        }
        active = false;
    }

    @Override
    public void draw(Graphics g) {
        if (!active) return;

        Graphics2D g2d = (Graphics2D) g;

        // Draw trail
        if (hasTrail && trailPoints.size() > 1) {
            for (int i = 0; i < trailPoints.size() - 1; i++) {
                Point p1 = trailPoints.get(i);
                Point p2 = trailPoints.get(i + 1);
                float alpha = 1.0f - (float)i / trailPoints.size();
                int thickness = (int)(3 * (1.0f - (float)i / trailPoints.size()));

                g2d.setColor(new Color(
                    trailColor.getRed(),
                    trailColor.getGreen(),
                    trailColor.getBlue(),
                    (int)(alpha * 150)
                ));
                g2d.setStroke(new BasicStroke(Math.max(1, thickness)));
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        // Get current frame
        BufferedImage frame = null;
        if (animation != null) {
            frame = animation.getCurrentFrame();
        } else {
            frame = staticSprite;
        }

        if (frame == null) {
            // Fallback drawing
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(x, y, width, height);
            return;
        }

        // Save transform
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();

        // Apply rotation
        if (rotateWithVelocity) {
            g2d.translate(x + width/2, y + height/2);
            g2d.rotate(rotationAngle);
            g2d.translate(-width/2, -height/2);
            g2d.drawImage(frame, 0, 0, width, height, null);
        } else {
            g2d.drawImage(frame, x, y, width, height, null);
        }

        // Restore transform
        g2d.setTransform(oldTransform);
    }

    @Override
    public Rectangle getBounds() {
        // Use a slightly smaller hitbox for better gameplay feel
        int hitboxPadding = 4;
        return new Rectangle(
            x + hitboxPadding,
            y + hitboxPadding,
            width - hitboxPadding * 2,
            height - hitboxPadding * 2
        );
    }

    // ==================== Getters and Setters ====================

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public double getVelX() {
        return velX;
    }

    public void setVelX(double velX) {
        this.velX = velX;
    }

    public double getVelY() {
        return velY;
    }

    public void setVelY(double velY) {
        this.velY = velY;
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }

    public void setSource(Entity source) {
        this.source = source;
    }

    public Entity getSource() {
        return source;
    }

    public ProjectileType getType() {
        return type;
    }

    public void setType(ProjectileType type) {
        this.type = type;
    }

    public void setPiercing(boolean piercing, int maxPierceCount) {
        this.piercing = piercing;
        this.maxPierceCount = maxPierceCount;
    }

    public void setExplosive(boolean explosive, int radius) {
        this.explosive = explosive;
        this.explosionRadius = radius;
    }

    public void setTrail(boolean hasTrail, Color color, int maxLength) {
        this.hasTrail = hasTrail;
        this.trailColor = color;
        this.maxTrailLength = maxLength;
    }

    public void setLifetime(double seconds) {
        this.lifetime = seconds;
    }

    public void setKnockbackForce(double force) {
        this.knockbackForce = force;
    }

    public void setRotateWithVelocity(boolean rotate) {
        this.rotateWithVelocity = rotate;
    }

    @Override
    public String toString() {
        return "ProjectileEntity{" +
                "type=" + type +
                ", pos=(" + x + "," + y + ")" +
                ", vel=(" + String.format("%.1f", velX) + "," + String.format("%.1f", velY) + ")" +
                ", damage=" + damage +
                ", active=" + active +
                "}";
    }
}
