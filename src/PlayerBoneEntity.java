import java.awt.*;
import java.util.ArrayList;

/**
 * Player entity that uses bone-based skeletal animation.
 * Supports running, jumping, and idle animations with textured bones.
 */
public class PlayerBoneEntity extends Entity implements PlayerBase {

    // Physics
    private double velX = 0;
    private double velY = 0;
    private final double gravity = 0.5;
    private final double jumpStrength = -10;
    private final int moveSpeed = 4;

    // State
    private boolean onGround = false;
    private boolean facingRight = true;
    private int airTime = 0;
    private boolean isMoving = false;

    // Dimensions (collision box)
    private int width = 48;   // Smaller than sprite for tighter collisions
    private int height = 64;

    // Ground level
    private int groundY = 720;

    // Skeleton animation system
    private Skeleton skeleton;

    // Animation state names
    public static final String ANIM_IDLE = "idle";
    public static final String ANIM_RUN = "run";
    public static final String ANIM_JUMP = "jump";

    // Rendering
    private static final double RENDER_SCALE = 4.0;  // Scale factor for bones

    // Audio
    private AudioManager audioManager;

    // Inventory (optional, can be added)
    private Inventory inventory;

    /**
     * Creates a new bone-animated player.
     * @param x Starting X position
     * @param y Starting Y position
     */
    public PlayerBoneEntity(int x, int y) {
        super(x, y);

        // Create the skeleton with default humanoid structure
        skeleton = Skeleton.createHumanoid();
        skeleton.setScale(RENDER_SCALE);

        // Set up default animations
        setupAnimations();

        // Initialize inventory
        this.inventory = new Inventory(10);

        System.out.println("PlayerBoneEntity created at (" + x + ", " + y + ")");
    }

    /**
     * Creates a player with bone textures from a directory.
     * @param x Starting X position
     * @param y Starting Y position
     * @param textureDir Directory containing bone texture PNGs
     */
    public PlayerBoneEntity(int x, int y, String textureDir) {
        super(x, y);

        // Create skeleton with textures
        skeleton = Skeleton.createHumanoidWithTextures(textureDir);
        skeleton.setScale(RENDER_SCALE);

        // Set up animations
        setupAnimations();

        // Initialize inventory
        this.inventory = new Inventory(10);

        System.out.println("PlayerBoneEntity created with textures from: " + textureDir);
    }

    /**
     * Sets up the default animations for the player.
     */
    private void setupAnimations() {
        // Idle animation - subtle breathing
        BoneAnimation idle = BoneAnimation.createIdleAnimation("torso");
        skeleton.addAnimation(idle);

        // Running animation
        BoneAnimation run = BoneAnimation.createRunAnimation(
            "leg_left", "leg_right", "arm_left", "arm_right"
        );
        skeleton.addAnimation(run);

        // Jump animation
        BoneAnimation jump = BoneAnimation.createJumpAnimation(
            "torso", "leg_left", "leg_right", "arm_left", "arm_right"
        );
        skeleton.addAnimation(jump);

        // Start with idle
        skeleton.playAnimation(ANIM_IDLE);
    }

    /**
     * Loads textures for a specific bone.
     * @param boneName Name of the bone
     * @param texturePath Path to PNG or GIF texture
     */
    public void setBoneTexture(String boneName, String texturePath) {
        Bone bone = skeleton.findBone(boneName);
        if (bone != null) {
            bone.loadTexture(texturePath);
        } else {
            System.out.println("Warning: Bone '" + boneName + "' not found");
        }
    }

    /**
     * Gets the skeleton for direct manipulation.
     * @return The player's skeleton
     */
    public Skeleton getSkeleton() {
        return skeleton;
    }

    /**
     * Adds a custom animation to the player.
     * @param animation The animation to add
     */
    public void addAnimation(BoneAnimation animation) {
        skeleton.addAnimation(animation);
    }

    /**
     * Sets the audio manager for sound effects.
     */
    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    /**
     * Sets the ground Y level.
     */
    public void setGroundY(int groundY) {
        this.groundY = groundY;
    }

    /**
     * Gets the inventory.
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Updates the player each frame.
     */
    public void update(InputManager input, ArrayList<Entity> entities) {
        // Track previous movement state
        boolean wasMoving = isMoving;
        isMoving = false;

        // Toggle inventory
        if (input.isKeyJustPressed('i')) {
            inventory.toggleOpen();
        }

        // Hotbar selection
        for (char c = '1'; c <= '5'; c++) {
            if (input.isKeyJustPressed(c)) {
                inventory.handleHotbarKey(c);
            }
        }

        // Horizontal movement
        int newX = x;
        if (input.isKeyPressed('a')) {
            newX -= moveSpeed;
            facingRight = false;
            isMoving = true;
        }
        if (input.isKeyPressed('d')) {
            newX += moveSpeed;
            facingRight = true;
            isMoving = true;
        }

        // Horizontal collision check
        Rectangle futureXBounds = new Rectangle(newX, y, width, height);
        boolean xCollision = false;

        for (Entity e : entities) {
            if (e == this) continue;

            boolean isSolid = false;
            if (e instanceof SpriteEntity && ((SpriteEntity) e).isSolid()) {
                isSolid = true;
            } else if (e instanceof BlockEntity && ((BlockEntity) e).isSolid()) {
                isSolid = true;
            }

            if (isSolid && futureXBounds.intersects(e.getBounds())) {
                xCollision = true;
                break;
            }
        }

        if (!xCollision) {
            x = newX;
        }

        // Collect items
        Rectangle playerBounds = new Rectangle(x, y, width, height);
        for (Entity e : entities) {
            if (e instanceof ItemEntity) {
                ItemEntity item = (ItemEntity) e;
                if (!item.isCollected() && playerBounds.intersects(e.getBounds())) {
                    item.collect();
                    inventory.addItem(item);
                    if (audioManager != null) {
                        audioManager.playSound("collect");
                    }
                }
            }
        }

        // Jumping
        if (input.isKeyPressed(' ') && onGround) {
            velY = jumpStrength;
            onGround = false;
            if (audioManager != null) {
                audioManager.playSound("jump");
            }
        }

        // Apply gravity
        velY += gravity;

        // Calculate new Y
        int newY = y + (int) velY;

        // Vertical collision
        Rectangle futureYBounds = new Rectangle(x, newY, width, height);
        boolean foundPlatform = false;

        for (Entity e : entities) {
            if (e == this) continue;

            boolean isSolid = false;
            if (e instanceof SpriteEntity && ((SpriteEntity) e).isSolid()) {
                isSolid = true;
            } else if (e instanceof BlockEntity && ((BlockEntity) e).isSolid()) {
                isSolid = true;
            }

            if (isSolid) {
                Rectangle platformBounds = e.getBounds();
                if (futureYBounds.intersects(platformBounds)) {
                    if (velY > 0) {
                        newY = platformBounds.y - height;
                        velY = 0;
                        onGround = true;
                        foundPlatform = true;
                    } else if (velY < 0) {
                        newY = platformBounds.y + platformBounds.height;
                        velY = 0;
                    }
                    break;
                }
            }
        }

        // Ground collision
        if (newY + height >= groundY) {
            newY = groundY - height;
            velY = 0;
            onGround = true;
        } else if (!foundPlatform) {
            onGround = false;
        }

        y = newY;

        // Track air time
        if (onGround) {
            airTime = 0;
        } else {
            airTime++;
        }

        // Update animation state
        updateAnimationState();

        // Update skeleton animation (60 FPS = ~16.67ms per frame)
        skeleton.update(1.0 / 60.0);
    }

    /**
     * Updates which animation should be playing based on player state.
     */
    private void updateAnimationState() {
        // Update facing direction
        skeleton.setFlipX(!facingRight);

        // Determine animation
        if (!onGround && airTime > 3) {
            // In the air - play jump animation
            if (!skeleton.isPlayingAnimation(ANIM_JUMP)) {
                skeleton.transitionTo(ANIM_JUMP, 0.1);
            }
        } else if (isMoving && onGround) {
            // Moving on ground - run animation
            if (!skeleton.isPlayingAnimation(ANIM_RUN)) {
                skeleton.transitionTo(ANIM_RUN, 0.15);
            }
        } else if (onGround) {
            // Standing still - idle animation
            if (!skeleton.isPlayingAnimation(ANIM_IDLE)) {
                skeleton.transitionTo(ANIM_IDLE, 0.2);
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        // Update skeleton position to match player
        // Center the skeleton on the collision box
        skeleton.setPosition(x + width / 2.0, y + height / 2.0);

        // Draw the skeleton
        skeleton.draw(g);

        // Debug: draw collision box
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));

        if (airTime > 3) {
            g.setColor(Color.ORANGE);
        } else if (isMoving) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.RED);
        }
        g.drawRect(x, y, width, height);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Checks if the player is facing right.
     */
    public boolean isFacingRight() {
        return facingRight;
    }

    /**
     * Checks if the player is on the ground.
     */
    public boolean isOnGround() {
        return onGround;
    }

    /**
     * Gets the current air time (frames).
     */
    public int getAirTime() {
        return airTime;
    }

    /**
     * Enables or disables debug drawing for the skeleton.
     */
    public void setDebugDraw(boolean enabled) {
        skeleton.setDebugDraw(enabled);
    }

    /**
     * Sets the collision box dimensions.
     */
    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Drops an item in front of the player.
     */
    public void dropItem(ItemEntity item) {
        int dropOffset = facingRight ? width + 20 : -20 - item.getBounds().width;
        item.x = x + dropOffset;
        item.y = y + (height / 2);
        if (audioManager != null) {
            audioManager.playSound("drop");
        }
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }
}
