package entity;

import entity.player.*;
import entity.mob.*;
import entity.mob.SpriteMobEntity.StatusEffect;
import entity.item.ItemEntity;
import entity.item.ItemRegistry;
import entity.item.Item;
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
        BOMB,           // Explosive
        FISH            // Tiny fish projectile (from Mirror to Other Realms)
    }

    // Sprite and animation
    private AnimatedTexture animation;
    private BufferedImage staticSprite;
    private int width;
    private int height;
    private static final int SCALE = 2;
    private float scaleFactor = 1.0f;  // Additional scale for charged shots

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

    // Status effect properties (for special arrows/magic)
    public enum StatusEffectType {
        NONE,
        BURNING,
        FROZEN,
        POISONED
    }
    private StatusEffectType statusEffect = StatusEffectType.NONE;
    private double effectDuration = 0;      // Duration of effect in seconds
    private int effectDamagePerTick = 0;    // Damage per tick for DoT effects
    private float effectDamageMultiplier = 1.0f;  // Damage multiplier for impact

    // Explosion effect
    private boolean showExplosion = false;
    private double explosionTimer = 0;
    private double explosionDuration = 0.3;
    private int explosionFrame = 0;
    private List<Entity> entitiesReference;  // Reference to entities for explosion damage

    // Source item tracking for throwables that can be recovered
    private String sourceItemId = null;  // Registry ID for creating dropped items
    private entity.item.ItemEntity pendingDroppedItem = null;  // Item to drop on impact

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
        String spritePath = "assets/projectiles/" + type.name().toLowerCase() + ".gif";

        switch (type) {
            case ARROW:
                gravity = 0.3;
                rotateWithVelocity = true;
                hasTrail = false;
                if (!tryLoadSprite(spritePath)) {
                    createArrowSprite(16, 4, new Color(139, 90, 43), new Color(100, 100, 100));
                }
                break;
            case BOLT:
                gravity = 0.2;
                rotateWithVelocity = true;
                if (!tryLoadSprite(spritePath)) {
                    createBoltSprite(18, 4, new Color(100, 100, 100));
                }
                break;
            case MAGIC_BOLT:
                gravity = 0.0;
                rotateWithVelocity = false;
                hasTrail = true;
                trailColor = new Color(100, 100, 255);
                if (!tryLoadSprite(spritePath)) {
                    createMagicBoltSprite(12, 12, new Color(100, 150, 255));
                }
                break;
            case FIREBALL:
                gravity = 0.0;
                hasTrail = true;
                trailColor = new Color(255, 100, 0);
                explosive = true;
                explosionRadius = 64;
                if (!tryLoadSprite(spritePath)) {
                    createFireballSprite(16, 16);
                }
                break;
            case ICEBALL:
                gravity = 0.1;
                hasTrail = true;
                trailColor = new Color(150, 200, 255);
                if (!tryLoadSprite(spritePath)) {
                    createIceballSprite(14, 14);
                }
                break;
            case THROWING_KNIFE:
                gravity = 0.4;
                rotateWithVelocity = true;
                if (!tryLoadSprite(spritePath)) {
                    createKnifeSprite(14, 4, new Color(180, 180, 180));
                }
                break;
            case THROWING_AXE:
                gravity = 0.5;
                rotateWithVelocity = true;
                if (!tryLoadSprite(spritePath)) {
                    createAxeSprite(16, 16);
                }
                break;
            case ROCK:
                gravity = 0.6;
                if (!tryLoadSprite(spritePath)) {
                    createRockSprite(10, 10, new Color(100, 100, 100));
                }
                break;
            case POTION:
                gravity = 0.5;
                explosive = true;
                explosionRadius = 48;
                if (!tryLoadSprite(spritePath)) {
                    createPotionSprite(10, 14, new Color(100, 200, 100));
                }
                break;
            case BOMB:
                gravity = 0.4;
                explosive = true;
                explosionRadius = 96;
                if (!tryLoadSprite(spritePath)) {
                    createBombSprite(14, 14);
                }
                break;
            case FISH:
                gravity = 0.2;
                rotateWithVelocity = true;
                hasTrail = true;
                trailColor = new Color(100, 180, 255);  // Watery blue trail
                if (!tryLoadSprite(spritePath)) {
                    createFishSprite(14, 8);
                }
                break;
        }
    }

    /**
     * Attempts to load a sprite from a file path.
     * @return true if sprite was loaded successfully
     */
    private boolean tryLoadSprite(String path) {
        try {
            java.io.File file = new java.io.File(path);
            if (!file.exists()) return false;

            AssetLoader.ImageAsset asset = AssetLoader.load(path);
            if (asset.animatedTexture != null) {
                this.animation = asset.animatedTexture;
                this.width = asset.width * SCALE;
                this.height = asset.height * SCALE;
                return true;
            } else if (asset.staticImage != null) {
                this.staticSprite = asset.staticImage;
                this.width = asset.width * SCALE;
                this.height = asset.height * SCALE;
                return true;
            }
        } catch (Exception e) {
            // Sprite loading failed, will use placeholder
        }
        return false;
    }

    /**
     * Creates a detailed arrow sprite.
     */
    private void createArrowSprite(int w, int h, Color shaftColor, Color tipColor) {
        this.width = w * SCALE;
        this.height = h * SCALE;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Shaft
        g.setColor(shaftColor);
        g.fillRect(0, h/2 - 1, w - 4, 2);

        // Arrowhead (triangle)
        g.setColor(tipColor);
        int[] xPoints = {w - 4, w, w - 4};
        int[] yPoints = {0, h/2, h};
        g.fillPolygon(xPoints, yPoints, 3);

        // Fletching (back feathers)
        g.setColor(new Color(200, 50, 50));
        g.fillRect(0, 0, 3, h);

        g.dispose();
        this.staticSprite = img;
    }

    /**
     * Creates a detailed bolt sprite (crossbow bolt).
     */
    private void createBoltSprite(int w, int h, Color metalColor) {
        this.width = w * SCALE;
        this.height = h * SCALE;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Metal shaft
        g.setColor(metalColor);
        g.fillRect(0, h/2 - 1, w - 3, 2);

        // Pointed tip
        g.setColor(metalColor.brighter());
        int[] xPoints = {w - 3, w, w - 3};
        int[] yPoints = {0, h/2, h};
        g.fillPolygon(xPoints, yPoints, 3);

        // Small fins at back
        g.setColor(new Color(60, 60, 60));
        g.fillRect(0, 0, 4, h);

        g.dispose();
        this.staticSprite = img;
    }

    /**
     * Creates a glowing magic bolt sprite.
     */
    private void createMagicBoltSprite(int w, int h, Color magicColor) {
        this.width = w * SCALE;
        this.height = h * SCALE;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Outer glow
        g.setColor(new Color(magicColor.getRed(), magicColor.getGreen(), magicColor.getBlue(), 100));
        g.fillOval(0, 0, w, h);

        // Inner core
        g.setColor(magicColor);
        g.fillOval(w/4, h/4, w/2, h/2);

        // Bright center
        g.setColor(Color.WHITE);
        g.fillOval(w/3, h/3, w/3, h/3);

        g.dispose();
        this.staticSprite = img;
    }

    /**
     * Creates a fireball sprite with flame effect.
     */
    private void createFireballSprite(int w, int h) {
        this.width = w * SCALE;
        this.height = h * SCALE;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Outer red glow
        g.setColor(new Color(255, 50, 0, 150));
        g.fillOval(0, 0, w, h);

        // Orange middle
        g.setColor(new Color(255, 150, 0));
        g.fillOval(w/6, h/6, w*2/3, h*2/3);

        // Yellow core
        g.setColor(new Color(255, 255, 100));
        g.fillOval(w/3, h/3, w/3, h/3);

        // White hot center
        g.setColor(new Color(255, 255, 255));
        g.fillOval(w*3/8, h*3/8, w/4, h/4);

        g.dispose();
        this.staticSprite = img;
    }

    /**
     * Creates an iceball sprite with frost effect.
     */
    private void createIceballSprite(int w, int h) {
        this.width = w * SCALE;
        this.height = h * SCALE;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Outer frost glow
        g.setColor(new Color(150, 200, 255, 150));
        g.fillOval(0, 0, w, h);

        // Blue middle
        g.setColor(new Color(100, 180, 255));
        g.fillOval(w/6, h/6, w*2/3, h*2/3);

        // Light blue core
        g.setColor(new Color(200, 230, 255));
        g.fillOval(w/3, h/3, w/3, h/3);

        // White crystal center
        g.setColor(Color.WHITE);
        g.fillOval(w*3/8, h*3/8, w/4, h/4);

        g.dispose();
        this.staticSprite = img;
    }

    /**
     * Creates a throwing knife sprite.
     */
    private void createKnifeSprite(int w, int h, Color bladeColor) {
        this.width = w * SCALE;
        this.height = h * SCALE;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Blade
        g.setColor(bladeColor);
        g.fillRect(4, h/2 - 1, w - 6, 2);

        // Pointed tip
        g.setColor(bladeColor.brighter());
        int[] xPoints = {w - 2, w, w - 2};
        int[] yPoints = {0, h/2, h};
        g.fillPolygon(xPoints, yPoints, 3);

        // Handle
        g.setColor(new Color(100, 60, 30));
        g.fillRect(0, 0, 5, h);

        g.dispose();
        this.staticSprite = img;
    }

    /**
     * Creates a throwing axe sprite.
     */
    private void createAxeSprite(int w, int h) {
        this.width = w * SCALE;
        this.height = h * SCALE;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Handle
        g.setColor(new Color(139, 90, 43));
        g.fillRect(w/2 - 1, 2, 3, h - 4);

        // Axe head
        g.setColor(new Color(120, 120, 120));
        g.fillArc(0, 0, w*2/3, h, 270, 180);

        // Axe edge highlight
        g.setColor(new Color(180, 180, 180));
        g.drawArc(1, 1, w*2/3 - 2, h - 2, 270, 180);

        g.dispose();
        this.staticSprite = img;
    }

    /**
     * Creates a rock sprite.
     */
    private void createRockSprite(int w, int h, Color rockColor) {
        this.width = w * SCALE;
        this.height = h * SCALE;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Main rock shape
        g.setColor(rockColor);
        g.fillOval(1, 1, w - 2, h - 2);

        // Shadow
        g.setColor(rockColor.darker());
        g.fillArc(2, h/2, w - 4, h/2, 180, 180);

        // Highlight
        g.setColor(rockColor.brighter());
        g.fillOval(w/4, h/4, w/3, h/3);

        g.dispose();
        this.staticSprite = img;
    }

    /**
     * Creates a potion bottle sprite.
     */
    private void createPotionSprite(int w, int h, Color liquidColor) {
        this.width = w * SCALE;
        this.height = h * SCALE;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Glass bottle
        g.setColor(new Color(200, 200, 255, 180));
        g.fillOval(1, h/3, w - 2, h*2/3 - 1);

        // Liquid inside
        g.setColor(liquidColor);
        g.fillOval(2, h/2, w - 4, h/2 - 2);

        // Bottle neck
        g.setColor(new Color(200, 200, 255, 180));
        g.fillRect(w/3, 2, w/3, h/3);

        // Cork
        g.setColor(new Color(139, 90, 43));
        g.fillRect(w/3, 0, w/3, 3);

        g.dispose();
        this.staticSprite = img;
    }

    /**
     * Creates a bomb sprite.
     */
    private void createBombSprite(int w, int h) {
        this.width = w * SCALE;
        this.height = h * SCALE;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Main bomb body
        g.setColor(new Color(40, 40, 40));
        g.fillOval(1, 3, w - 2, h - 4);

        // Highlight
        g.setColor(new Color(80, 80, 80));
        g.fillArc(2, 4, w/2, h/2, 45, 90);

        // Fuse
        g.setColor(new Color(139, 90, 43));
        g.fillRect(w/2 - 1, 0, 2, 4);

        // Spark
        g.setColor(new Color(255, 200, 0));
        g.fillOval(w/2 - 2, 0, 4, 4);
        g.setColor(new Color(255, 100, 0));
        g.fillOval(w/2 - 1, 1, 2, 2);

        g.dispose();
        this.staticSprite = img;
    }

    /**
     * Creates a tiny fish sprite for the Mirror to Other Realms ocean projectile.
     */
    private void createFishSprite(int w, int h) {
        this.width = w * SCALE;
        this.height = h * SCALE;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fish body (oval shape) - teal/cyan color
        g.setColor(new Color(50, 180, 200));
        g.fillOval(2, 1, w - 5, h - 2);

        // Fish belly (lighter)
        g.setColor(new Color(150, 220, 240));
        g.fillOval(3, h/2, w - 7, h/2 - 1);

        // Tail fin (triangle at back)
        g.setColor(new Color(30, 140, 170));
        int[] tailX = {0, 3, 3};
        int[] tailY = {h/2, 1, h - 1};
        g.fillPolygon(tailX, tailY, 3);

        // Dorsal fin (small triangle on top)
        g.setColor(new Color(30, 140, 170));
        int[] dorsalX = {w/2 - 1, w/2 + 2, w/2 + 2};
        int[] dorsalY = {0, 0, 2};
        g.fillPolygon(dorsalX, dorsalY, 3);

        // Eye
        g.setColor(Color.WHITE);
        g.fillOval(w - 5, h/2 - 2, 3, 3);
        g.setColor(Color.BLACK);
        g.fillOval(w - 4, h/2 - 1, 2, 2);

        // Mouth line
        g.setColor(new Color(30, 100, 130));
        g.drawLine(w - 2, h/2, w - 1, h/2);

        // Scales shimmer (optional detail)
        g.setColor(new Color(100, 200, 220, 100));
        g.drawArc(4, 2, 4, 3, 0, 180);
        g.drawArc(7, 2, 4, 3, 0, 180);

        g.dispose();
        this.staticSprite = img;
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
                createFallbackSprite();
            }
        } catch (Exception e) {
            System.err.println("ProjectileEntity: Failed to load sprite: " + spritePath);
            createFallbackSprite();
        }
    }

    /**
     * Creates a basic fallback sprite when loading fails.
     */
    private void createFallbackSprite() {
        this.width = 16 * SCALE;
        this.height = 16 * SCALE;

        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.YELLOW);
        g.fillOval(2, 2, 12, 12);
        g.setColor(Color.WHITE);
        g.fillOval(4, 4, 4, 4);
        g.dispose();
        this.staticSprite = img;
    }

    /**
     * Updates the projectile's position and checks for collisions.
     */
    public void update(double deltaTime, List<Entity> entities) {
        // Store reference for explosion damage
        this.entitiesReference = entities;

        // Handle explosion animation if active
        if (showExplosion) {
            explosionTimer += deltaTime;
            explosionFrame = (int)((explosionTimer / explosionDuration) * 5);  // 5 explosion frames
            if (explosionTimer >= explosionDuration) {
                active = false;
                showExplosion = false;
            }
            return;  // Don't move or check collisions during explosion
        }

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
                    // Calculate damage with effect multiplier
                    int finalDamage = (int)(damage * effectDamageMultiplier);

                    // Deal damage
                    double knockbackDir = velX > 0 ? 1 : -1;
                    mob.takeDamage(finalDamage, knockbackDir * knockbackForce, -knockbackForce / 2);

                    // Apply status effect if this projectile has one
                    if (statusEffect != StatusEffectType.NONE && mob instanceof SpriteMobEntity) {
                        SpriteMobEntity spriteMob = (SpriteMobEntity) mob;
                        StatusEffect effect = convertToMobStatusEffect(statusEffect);
                        spriteMob.applyStatusEffect(effect, effectDuration, effectDamagePerTick, effectDamageMultiplier);
                    }

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
        if (explosive && explosionRadius > 0) {
            // Trigger explosion effect
            showExplosion = true;
            explosionTimer = 0;
            explosionFrame = 0;

            // Deal damage to all entities within explosion radius
            if (entitiesReference != null) {
                int centerX = x + width / 2;
                int centerY = y + height / 2;

                for (Entity entity : entitiesReference) {
                    if (entity == this || entity == source) continue;

                    // Calculate distance to entity center
                    Rectangle bounds = entity.getBounds();
                    int entityCenterX = bounds.x + bounds.width / 2;
                    int entityCenterY = bounds.y + bounds.height / 2;
                    double distance = Math.sqrt(
                        Math.pow(entityCenterX - centerX, 2) +
                        Math.pow(entityCenterY - centerY, 2)
                    );

                    if (distance <= explosionRadius) {
                        // Calculate damage falloff (100% at center, 25% at edge)
                        double damageMultiplier = 1.0 - (distance / explosionRadius) * 0.75;
                        int explosionDamage = (int)(damage * damageMultiplier);

                        // Calculate knockback direction (away from explosion center)
                        double knockbackX = 0;
                        double knockbackY = 0;
                        if (distance > 0) {
                            knockbackX = ((entityCenterX - centerX) / distance) * knockbackForce * 1.5;
                            knockbackY = ((entityCenterY - centerY) / distance) * knockbackForce - 3;  // Upward boost
                        } else {
                            knockbackY = -knockbackForce;  // Direct hit - knock upward
                        }

                        // Deal damage based on entity type
                        if (fromPlayer && entity instanceof MobEntity) {
                            MobEntity mob = (MobEntity) entity;
                            if (mob.getCurrentHealth() > 0) {
                                mob.takeDamage(explosionDamage, knockbackX, knockbackY);
                            }
                        } else if (!fromPlayer && entity instanceof PlayerBase) {
                            PlayerBase player = (PlayerBase) entity;
                            if (!player.isInvincible()) {
                                player.takeDamage(explosionDamage, knockbackX, knockbackY);
                            }
                        }
                    }
                }
            }

            // Stop movement but keep active for explosion animation
            velX = 0;
            velY = 0;
        } else {
            // Create dropped item for recoverable throwables (knives, axes, rocks)
            createDroppedItem();
            active = false;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!active && !showExplosion) return;

        Graphics2D g2d = (Graphics2D) g;

        // Draw explosion effect if active
        if (showExplosion) {
            drawExplosion(g2d);
            return;
        }

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
            // Fallback drawing with scale
            int scaledWidth = (int)(width * scaleFactor);
            int scaledHeight = (int)(height * scaleFactor);
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(x, y, scaledWidth, scaledHeight);
            return;
        }

        // Calculate scaled dimensions
        int scaledWidth = (int)(width * scaleFactor);
        int scaledHeight = (int)(height * scaleFactor);

        // Save transform
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();

        // Apply rotation
        if (rotateWithVelocity) {
            g2d.translate(x + scaledWidth/2, y + scaledHeight/2);
            g2d.rotate(rotationAngle);
            g2d.translate(-scaledWidth/2, -scaledHeight/2);
            g2d.drawImage(frame, 0, 0, scaledWidth, scaledHeight, null);
        } else {
            g2d.drawImage(frame, x, y, scaledWidth, scaledHeight, null);
        }

        // Restore transform
        g2d.setTransform(oldTransform);
    }

    /**
     * Draws the explosion effect animation.
     */
    private void drawExplosion(Graphics2D g2d) {
        int centerX = x + width / 2;
        int centerY = y + height / 2;

        // Calculate current explosion size based on frame
        float progress = (float) explosionFrame / 5;
        int currentRadius = (int)(explosionRadius * (0.3 + progress * 0.7));

        // Determine colors based on projectile type
        Color innerColor, outerColor;
        switch (type) {
            case FIREBALL:
                innerColor = new Color(255, 255, 200);  // White-yellow center
                outerColor = new Color(255, 100, 0);    // Orange outer
                break;
            case ICEBALL:
                innerColor = new Color(200, 240, 255);  // Light blue center
                outerColor = new Color(100, 180, 255);  // Blue outer
                break;
            case BOMB:
            case POTION:
            default:
                innerColor = new Color(255, 255, 150);  // Yellow center
                outerColor = new Color(255, 80, 0);     // Red-orange outer
                break;
        }

        // Calculate alpha (fade out towards end)
        int alpha = (int)(255 * (1.0 - progress * 0.6));

        // Draw outer explosion circle
        g2d.setColor(new Color(outerColor.getRed(), outerColor.getGreen(), outerColor.getBlue(), Math.max(0, alpha - 50)));
        g2d.fillOval(centerX - currentRadius, centerY - currentRadius, currentRadius * 2, currentRadius * 2);

        // Draw middle ring
        int midRadius = (int)(currentRadius * 0.7);
        g2d.setColor(new Color(
            (innerColor.getRed() + outerColor.getRed()) / 2,
            (innerColor.getGreen() + outerColor.getGreen()) / 2,
            (innerColor.getBlue() + outerColor.getBlue()) / 2,
            Math.max(0, alpha)
        ));
        g2d.fillOval(centerX - midRadius, centerY - midRadius, midRadius * 2, midRadius * 2);

        // Draw inner bright core
        int coreRadius = (int)(currentRadius * 0.35);
        g2d.setColor(new Color(innerColor.getRed(), innerColor.getGreen(), innerColor.getBlue(), Math.min(255, alpha + 50)));
        g2d.fillOval(centerX - coreRadius, centerY - coreRadius, coreRadius * 2, coreRadius * 2);

        // Draw spark particles
        g2d.setColor(new Color(255, 255, 200, alpha));
        int numSparks = 8;
        for (int i = 0; i < numSparks; i++) {
            double angle = (Math.PI * 2 * i / numSparks) + (progress * Math.PI);
            int sparkDist = (int)(currentRadius * (0.5 + progress * 0.5));
            int sparkX = centerX + (int)(Math.cos(angle) * sparkDist);
            int sparkY = centerY + (int)(Math.sin(angle) * sparkDist);
            int sparkSize = Math.max(2, (int)(6 * (1 - progress)));
            g2d.fillOval(sparkX - sparkSize/2, sparkY - sparkSize/2, sparkSize, sparkSize);
        }
    }

    @Override
    public Rectangle getBounds() {
        // Use scaled bounds for collision detection (includes charge scale)
        // Ensure minimum size of 1x1 for visibility checking
        int scaledWidth = (int)(width * scaleFactor);
        int scaledHeight = (int)(height * scaleFactor);
        int hitboxPadding = Math.min(4, Math.min(scaledWidth, scaledHeight) / 4);
        int boundsWidth = Math.max(1, scaledWidth - hitboxPadding * 2);
        int boundsHeight = Math.max(1, scaledHeight - hitboxPadding * 2);
        return new Rectangle(
            x + hitboxPadding,
            y + hitboxPadding,
            boundsWidth,
            boundsHeight
        );
    }

    // ==================== Getters and Setters ====================

    public boolean isActive() {
        return active || showExplosion;  // Keep active during explosion animation
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

    /**
     * Gets the current scale factor for this projectile.
     */
    public float getScale() {
        return scaleFactor;
    }

    /**
     * Sets the scale factor for this projectile (affects visual size and hitbox).
     * Used for charged shots where larger projectiles indicate more power.
     *
     * @param scale Scale factor (1.0 = normal, 2.0 = double size, etc.)
     */
    public void setScale(float scale) {
        this.scaleFactor = Math.max(0.5f, Math.min(3.0f, scale));
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

    // ==================== Status Effect Methods ====================

    /**
     * Sets the status effect this projectile will apply on hit.
     *
     * @param effect Type of status effect
     * @param duration Duration in seconds
     * @param damagePerTick Damage dealt per tick (for DoT)
     * @param damageMultiplier Multiplier for impact damage
     */
    public void setStatusEffect(StatusEffectType effect, double duration, int damagePerTick, float damageMultiplier) {
        this.statusEffect = effect;
        this.effectDuration = duration;
        this.effectDamagePerTick = damagePerTick;
        this.effectDamageMultiplier = damageMultiplier;
    }

    /**
     * Gets the status effect type.
     */
    public StatusEffectType getStatusEffect() {
        return statusEffect;
    }

    /**
     * Converts projectile StatusEffectType to mob StatusEffect.
     */
    private StatusEffect convertToMobStatusEffect(StatusEffectType type) {
        switch (type) {
            case BURNING: return StatusEffect.BURNING;
            case FROZEN: return StatusEffect.FROZEN;
            case POISONED: return StatusEffect.POISONED;
            default: return StatusEffect.NONE;
        }
    }

    // ==================== Throwable Item Drop Methods ====================

    /**
     * Sets the source item ID for this projectile.
     * Used to create dropped items when recoverable throwables impact.
     *
     * @param itemId The registry ID of the source item
     */
    public void setSourceItemId(String itemId) {
        this.sourceItemId = itemId;
    }

    /**
     * Gets the source item ID.
     */
    public String getSourceItemId() {
        return sourceItemId;
    }

    /**
     * Checks if this projectile type should drop an item on impact.
     * All physical throwables drop items so they can be recovered.
     * Only magical projectiles (that have no physical form) don't drop.
     *
     * @return true if this projectile should drop an item on impact
     */
    public boolean shouldDropOnImpact() {
        if (sourceItemId == null || sourceItemId.isEmpty()) {
            return false;
        }

        // All physical projectiles drop on impact - only magical ones don't
        switch (type) {
            case FIREBALL:
            case ICEBALL:
            case MAGIC_BOLT:
                // Magical projectiles don't drop physical items
                return false;
            case THROWING_KNIFE:
            case THROWING_AXE:
            case ROCK:
            case BOMB:
            case POTION:
            case ARROW:
            case BOLT:
            case FISH:
            default:
                // All physical projectiles drop on impact
                return true;
        }
    }

    /**
     * Creates a dropped item at the current projectile position.
     * Called on impact for recoverable throwables.
     */
    private void createDroppedItem() {
        if (!shouldDropOnImpact()) {
            System.out.println("[PROJECTILE] " + type + " shouldDropOnImpact=false (sourceItemId=" + sourceItemId + ")");
            return;
        }
        if (sourceItemId == null) {
            System.out.println("[PROJECTILE] " + type + " sourceItemId is null - cannot drop");
            return;
        }

        System.out.println("[PROJECTILE] " + type + " creating drop for: " + sourceItemId + " at (" + x + "," + y + ")");

        // Create the dropped item entity at impact location
        ItemEntity dropped = new ItemEntity(x, y, sourceItemId);
        dropped.setStackCount(1);  // Single item drop
        dropped.enablePhysics(0, -2, 720);  // Small upward bounce
        dropped.setShowLightBeam(true);

        // Store for collection by EntityManager
        this.pendingDroppedItem = dropped;
    }

    /**
     * Gets and clears the pending dropped item.
     * Call this from EntityManager to add dropped items to the world.
     *
     * @return The dropped item, or null if none
     */
    public ItemEntity collectDroppedItem() {
        ItemEntity item = this.pendingDroppedItem;
        this.pendingDroppedItem = null;
        return item;
    }

    /**
     * Checks if there is a pending dropped item.
     */
    public boolean hasPendingDroppedItem() {
        return pendingDroppedItem != null;
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
