package entity.mob;

import entity.*;
import animation.*;
import graphics.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

/**
 * RabbitSprite - A sprite-based passive mob entity representing a rabbit.
 *
 * This mob uses custom animation states specific to rabbit behavior:
 * - hop: Movement animation (replaces walk/run)
 * - idle: Standing still animation
 * - sleep: Resting animation
 *
 * Rabbits are passive mobs that do not attack.
 *
 * Animation timing for hop (6 frames over 1000ms):
 * - 0-400ms: Charge/preparation (crouching on ground)
 * - 400-900ms: In air (500ms duration)
 * - 900-1000ms: Landing (100ms duration)
 *
 * Rabbits have a lower jump height and shorter jump distance than frogs.
 *
 * Asset folder structure:
 *   assets/mobs/rabbit/{variant}/
 *     - hop.gif (movement animation)
 *     - idle.gif (idle animation)
 *     - sleep.gif (sleeping animation)
 *
 * Usage in level JSON:
 *   "mobType": "rabbit",
 *   "spriteDir": "assets/mobs/rabbit/brown_rabbit"
 */
public class RabbitSprite extends SpriteMobEntity {

    // Rabbit-specific animation states
    public enum RabbitAnimState {
        HOP,        // Movement animation
        IDLE,       // Standing still
        SLEEP       // Resting
    }

    // Current rabbit animation state
    protected RabbitAnimState currentRabbitState = RabbitAnimState.IDLE;

    // Rabbit variant (e.g., "brown_rabbit", "white_rabbit", etc.)
    protected String variant;

    // Hopping behavior - Animation timing (6 frames over 1000ms):
    // 0-400ms: Charge/preparation (on ground, crouching)
    // 400-900ms: In air (500ms)
    // 900-1000ms: Landing (on ground, finishing hop animation)
    protected double hopCooldown = 0;
    protected static final double HOP_PREP_TIME = 0.4;    // 400ms preparation before jump
    protected static final double HOP_AIR_TIME = 0.5;     // 500ms in air (400-900ms of animation)
    protected static final double HOP_LAND_TIME = 0.1;    // 100ms landing animation (900-1000ms)
    protected static final double HOP_STRENGTH = -5;      // Lower vertical velocity than frog
    protected static final double HOP_SPEED = 40;         // Shorter horizontal movement than frog
    protected double hopPrepTimer = 0;                    // Tracks preparation time
    protected double hopAirTimer = 0;                     // Tracks time in air
    protected double hopLandTimer = 0;                    // Tracks landing animation time
    protected boolean isHopping = false;                  // Currently in a hop (includes prep)
    protected boolean isPreparing = false;                // In preparation phase (before launch)
    protected boolean isLanding = false;                  // In landing phase (after touching ground)
    protected boolean wasOnGround = true;                 // Track ground state changes

    // Sleep behavior
    protected double sleepTimer = 0;
    protected static final double MIN_SLEEP_TIME = 3.0;  // Minimum time sleeping (rabbits rest longer)
    protected boolean isSleeping = false;

    // Idle behavior
    protected double idleTimer = 0;
    protected static final double MIN_IDLE_TIME = 1.0;   // Minimum time idle before hopping

    /**
     * Creates a new RabbitSprite entity.
     *
     * @param x Initial X position
     * @param y Initial Y position
     * @param spriteDir Directory containing rabbit animation GIFs
     */
    public RabbitSprite(int x, int y, String spriteDir) {
        super(x, y, spriteDir);

        // Extract variant from sprite directory
        this.variant = extractVariant(spriteDir);

        // Configure rabbit-specific settings
        configureRabbitStats();

        // Set body type to small quadruped and apply rabbit dimensions
        // (animations already loaded via overridden loadAnimations)
        this.bodyType = MobBodyType.SMALL;
        applyRabbitDimensions();

        // Rabbits are always passive - they never attack
        setHostile(false);
        this.detectionRange = 0;  // Don't detect players
        this.attackDamage = 0;    // No attack

        System.out.println("RabbitSprite: Created with spriteDir=" + spriteDir + ", variant=" + variant);
    }

    /**
     * Extracts the variant name from the sprite directory path.
     * e.g., "assets/mobs/rabbit/brown_rabbit" -> "brown_rabbit"
     */
    private String extractVariant(String spriteDir) {
        if (spriteDir == null) return "default";
        String[] parts = spriteDir.replace("\\", "/").split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "default";
    }

    /**
     * Configures rabbit-specific stats.
     */
    private void configureRabbitStats() {
        this.maxHealth = 8;               // Rabbits are fragile
        this.currentHealth = maxHealth;
        this.attackDamage = 0;            // No attack
        this.wanderSpeed = HOP_SPEED;
        this.chaseSpeed = HOP_SPEED;      // Rabbits don't chase
        this.attackRange = 0;             // No attack
        this.attackCooldown = 0;          // No attack
        this.jumpStrength = HOP_STRENGTH;
    }

    /**
     * Applies rabbit-specific dimensions (small quadruped).
     */
    private void applyRabbitDimensions() {
        // Rabbits are small: 32x32 base, scaled to 64x64
        this.spriteWidth = 32 * SCALE;
        this.spriteHeight = 32 * SCALE;
        this.hitboxWidth = (int)(spriteWidth * 0.6);
        this.hitboxHeight = (int)(spriteHeight * 0.5);
        this.hitboxOffsetX = -hitboxWidth / 2;
        this.hitboxOffsetY = -hitboxHeight;
    }

    /**
     * Loads rabbit-specific animations from the sprite directory.
     * Maps rabbit states to standard ActionStates:
     * - hop -> JUMP (and WALK, RUN)
     * - idle -> IDLE
     * - sleep -> IDLE (fallback)
     */
    @Override
    protected void loadAnimations(String dir) {
        // Don't call super - we use custom loading
        loadRabbitAnimations(dir);
    }

    /**
     * Loads rabbit-specific animations.
     */
    private void loadRabbitAnimations(String dir) {
        System.out.println("RabbitSprite: Loading animations from " + dir);

        // Load custom rabbit animations and map to standard states
        String idlePath = dir + "/idle.gif";
        String hopPath = dir + "/hop.gif";
        String sleepPath = dir + "/sleep.gif";

        // Check if files exist before loading
        java.io.File idleFile = new java.io.File(idlePath);
        java.io.File hopFile = new java.io.File(hopPath);
        java.io.File sleepFile = new java.io.File(sleepPath);
        System.out.println("RabbitSprite: idle.gif exists: " + idleFile.exists() + " at " + idleFile.getAbsolutePath());
        System.out.println("RabbitSprite: hop.gif exists: " + hopFile.exists() + " at " + hopFile.getAbsolutePath());
        System.out.println("RabbitSprite: sleep.gif exists: " + sleepFile.exists() + " at " + sleepFile.getAbsolutePath());

        boolean idleLoaded = spriteAnimation.loadAction(SpriteAnimation.ActionState.IDLE, idlePath);
        boolean hopLoaded = spriteAnimation.loadAction(SpriteAnimation.ActionState.JUMP, hopPath);
        // Also load hop for walk/run as fallback
        spriteAnimation.loadAction(SpriteAnimation.ActionState.WALK, hopPath);
        spriteAnimation.loadAction(SpriteAnimation.ActionState.RUN, hopPath);

        // Try to load sleep animation, fall back to idle if not available
        boolean sleepLoaded = spriteAnimation.loadAction(SpriteAnimation.ActionState.HURT, sleepPath);
        if (!sleepLoaded && idleLoaded) {
            // Use idle as fallback for sleep
            spriteAnimation.loadAction(SpriteAnimation.ActionState.HURT, idlePath);
        }

        System.out.println("RabbitSprite: idle loaded: " + idleLoaded + ", hop loaded: " + hopLoaded);
        System.out.println("RabbitSprite: Base dimensions: " + spriteAnimation.getBaseWidth() + "x" + spriteAnimation.getBaseHeight());

        // Ensure basic animations exist (creates placeholders if needed)
        spriteAnimation.ensureBasicAnimations();

        // If no valid dimensions loaded, create rabbit-specific placeholder
        if (spriteAnimation.getBaseWidth() <= 0) {
            System.out.println("RabbitSprite: Creating placeholder (no valid dimensions loaded)");
            createRabbitPlaceholder();
        }
    }

    /**
     * Creates a rabbit-shaped placeholder sprite.
     */
    private void createRabbitPlaceholder() {
        int w = 32 * SCALE;  // 64px
        int h = 32 * SCALE;  // 64px

        // Determine color based on variant
        Color bodyColor = getRabbitColor();
        Color darkerColor = bodyColor.darker();
        Color lighterColor = bodyColor.brighter();

        BufferedImage placeholder = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw rabbit body (oval)
        g.setColor(bodyColor);
        g.fillOval(12, 24, w - 24, h - 32);

        // Draw head (slightly overlapping body)
        g.fillOval(w/2 - 12, 16, 24, 20);

        // Draw ears (long rabbit ears)
        g.fillOval(w/2 - 10, 0, 8, 20);
        g.fillOval(w/2 + 2, 0, 8, 20);

        // Draw inner ears (pink)
        g.setColor(new Color(255, 200, 200));
        g.fillOval(w/2 - 8, 4, 4, 14);
        g.fillOval(w/2 + 4, 4, 4, 14);

        // Draw eyes
        g.setColor(Color.BLACK);
        g.fillOval(w/2 - 8, 20, 5, 5);
        g.fillOval(w/2 + 3, 20, 5, 5);

        // Draw nose (small pink)
        g.setColor(new Color(255, 180, 180));
        g.fillOval(w/2 - 2, 28, 4, 3);

        // Draw tail (small fluffy ball)
        g.setColor(lighterColor);
        g.fillOval(w - 20, h - 20, 10, 10);

        // Draw front legs
        g.setColor(darkerColor);
        g.fillOval(16, h - 12, 8, 10);
        g.fillOval(w - 24, h - 12, 8, 10);

        // Draw back legs (larger, for hopping)
        g.fillOval(8, h - 16, 12, 14);
        g.fillOval(w - 20, h - 16, 12, 14);

        g.dispose();

        // Create animated texture from placeholder
        java.util.List<BufferedImage> frames = new ArrayList<>();
        frames.add(placeholder);
        java.util.List<Integer> delays = new ArrayList<>();
        delays.add(100);
        AnimatedTexture anim = new AnimatedTexture(frames, delays);

        // Set for all states
        spriteAnimation.setAction(SpriteAnimation.ActionState.IDLE, anim);
        spriteAnimation.setAction(SpriteAnimation.ActionState.WALK, anim);
        spriteAnimation.setAction(SpriteAnimation.ActionState.RUN, anim);
        spriteAnimation.setAction(SpriteAnimation.ActionState.JUMP, anim);
        spriteAnimation.setAction(SpriteAnimation.ActionState.HURT, anim);
    }

    /**
     * Gets the rabbit's body color based on variant.
     */
    private Color getRabbitColor() {
        if (variant == null) return new Color(139, 119, 101);  // Default brown

        String lowerVariant = variant.toLowerCase();
        if (lowerVariant.contains("white") || lowerVariant.contains("snow")) {
            return new Color(245, 245, 245);   // White
        } else if (lowerVariant.contains("gray") || lowerVariant.contains("grey")) {
            return new Color(150, 150, 150);   // Gray
        } else if (lowerVariant.contains("black")) {
            return new Color(50, 50, 50);      // Black
        } else if (lowerVariant.contains("tan") || lowerVariant.contains("sandy")) {
            return new Color(210, 180, 140);   // Tan/Sandy
        } else if (lowerVariant.contains("spotted") || lowerVariant.contains("dutch")) {
            return new Color(180, 160, 140);   // Dutch spotted base
        } else {
            return new Color(139, 119, 101);   // Default brown
        }
    }

    /**
     * Updates the rabbit entity.
     */
    @Override
    public void update(double deltaTime, List<Entity> entities) {
        // Update sleep timer
        if (isSleeping) {
            sleepTimer += deltaTime;
        }

        // Update idle timer
        if (currentRabbitState == RabbitAnimState.IDLE && !isSleeping) {
            idleTimer += deltaTime;
        }

        // Call parent update
        super.update(deltaTime, entities);

        // Update rabbit-specific animation state
        updateRabbitAnimState();
    }

    /**
     * Updates the rabbit's animation state based on behavior.
     * Handles the hop -> idle transition on landing.
     * Animation timing: 0-400ms prep, 400-900ms air, 900-1000ms landing to idle.
     */
    private void updateRabbitAnimState() {
        // Hurt state takes priority
        if (currentState == AIState.HURT) {
            setRabbitAnimState(RabbitAnimState.IDLE);
            isHopping = false;
            isPreparing = false;
            isLanding = false;
            return;
        }

        // Detect landing (was in air, now on ground) - start 900-1000ms landing phase
        if (!wasOnGround && onGround && !isLanding) {
            // Just touched ground - start landing phase (100ms)
            isLanding = true;
            hopLandTimer = 0;
            isPreparing = false;
            hopAirTimer = 0;
            velocityX = 0;  // Stop horizontal movement on landing
            // Keep HOP animation during landing (frames 900-1000ms)
            setRabbitAnimState(RabbitAnimState.HOP);
            isSleeping = false;
        }
        // In landing phase - wait 100ms before transitioning
        else if (isLanding && onGround) {
            // Keep HOP animation during landing
            setRabbitAnimState(RabbitAnimState.HOP);
            isSleeping = false;
        }
        // Currently in air (hopping) - 400-900ms of animation
        else if (!onGround && !isPreparing) {
            setRabbitAnimState(RabbitAnimState.HOP);
            isSleeping = false;
            sleepTimer = 0;
        }
        // In preparation phase on ground - 0-400ms of animation
        else if (onGround && isHopping && isPreparing) {
            setRabbitAnimState(RabbitAnimState.HOP);
            isSleeping = false;
            sleepTimer = 0;
        }
        // On ground and idle (not hopping, preparing, or landing)
        else if (onGround && !isHopping && !isPreparing && !isLanding) {
            if (isSleeping) {
                setRabbitAnimState(RabbitAnimState.SLEEP);
            } else {
                setRabbitAnimState(RabbitAnimState.IDLE);
            }
        }

        // Track ground state for next frame
        wasOnGround = onGround;
    }

    /**
     * Sets the rabbit animation state and syncs with sprite animation.
     */
    private void setRabbitAnimState(RabbitAnimState state) {
        if (currentRabbitState != state) {
            currentRabbitState = state;

            switch (state) {
                case HOP:
                    spriteAnimation.setState(SpriteAnimation.ActionState.JUMP);
                    break;
                case SLEEP:
                    // Use HURT state slot for sleep animation
                    spriteAnimation.setState(SpriteAnimation.ActionState.HURT);
                    break;
                case IDLE:
                default:
                    spriteAnimation.setState(SpriteAnimation.ActionState.IDLE);
                    break;
            }
        }
    }

    /**
     * Rabbits do not attack - override to do nothing.
     */
    @Override
    protected void performAttack() {
        // Rabbits are peaceful and do not attack
    }

    /**
     * Rabbit-specific hopping movement.
     * Overrides wander state update to use hopping.
     * Animation timing: 0-400ms prep, 400-900ms air, 900-1000ms landing to idle.
     */
    @Override
    protected void updateWanderState(double deltaTime) {
        // Update hop cooldown
        if (hopCooldown > 0) {
            hopCooldown -= deltaTime;
        }

        // Track landing time (100ms after touching ground)
        if (isLanding) {
            hopLandTimer += deltaTime;

            // After 100ms landing animation, transition to idle
            if (hopLandTimer >= HOP_LAND_TIME) {
                isLanding = false;
                isHopping = false;
                hopLandTimer = 0;
                setRabbitAnimState(RabbitAnimState.IDLE);
                isSleeping = false;
                idleTimer = 0;
                // Set cooldown before next hop
                hopCooldown = 0.5 + Math.random() * 0.5;  // 0.5-1.0s pause between hops
            }
        }

        // Track preparation time
        if (isPreparing) {
            hopPrepTimer += deltaTime;

            // After 400ms preparation, launch into the air
            if (hopPrepTimer >= HOP_PREP_TIME) {
                isPreparing = false;
                velocityX = facingRight ? HOP_SPEED : -HOP_SPEED;
                velocityY = HOP_STRENGTH;
                onGround = false;
            }
        }

        // Track air time during hop
        if (isHopping && !onGround && !isPreparing) {
            hopAirTimer += deltaTime;
        }

        // On ground and not hopping or landing - start a new hop sequence
        if (onGround && !isHopping && !isPreparing && !isLanding) {
            // Wait for cooldown and minimum idle time before next hop
            if (hopCooldown <= 0 && idleTimer >= MIN_IDLE_TIME) {
                // Random direction for wander
                if (Math.random() < 0.4) {  // 40% chance to change direction
                    facingRight = Math.random() > 0.5;
                }

                // Start preparation phase (0-400ms of animation)
                isHopping = true;
                isPreparing = true;
                hopPrepTimer = 0;
                hopAirTimer = 0;
                velocityX = 0;  // Stay still during preparation

                // Start hop animation (shows crouch/prep frames)
                setRabbitAnimState(RabbitAnimState.HOP);
            } else {
                // Waiting on ground - stay in idle
                velocityX = 0;
            }
        }
        // In preparation phase - stay on ground with hop animation
        else if (onGround && isHopping && isPreparing) {
            // Keep hop animation during prep
            setRabbitAnimState(RabbitAnimState.HOP);
        }
        // In the air - maintain hop animation
        else if (!onGround && isHopping) {
            // Keep hop animation while in air
            setRabbitAnimState(RabbitAnimState.HOP);
        }
        // In landing phase - keep hop animation (handled by timer above)
        else if (isLanding) {
            setRabbitAnimState(RabbitAnimState.HOP);
        }

        // Update state timer for occasional sleep
        stateTimer += deltaTime;
        if (stateTimer > 8.0 + Math.random() * 4) {
            // Chance to go to sleep
            if (Math.random() < 0.3) {
                isSleeping = true;
                sleepTimer = 0;
                setRabbitAnimState(RabbitAnimState.SLEEP);
            }
            stateTimer = 0;
        }

        // Wake up from sleep after minimum time
        if (isSleeping && sleepTimer >= MIN_SLEEP_TIME) {
            if (Math.random() < 0.1) {  // 10% chance per frame to wake up
                isSleeping = false;
                sleepTimer = 0;
                setRabbitAnimState(RabbitAnimState.IDLE);
                idleTimer = 0;
            }
        }
    }

    /**
     * Rabbit reaction to being hit.
     * Rabbits flee when attacked but never become hostile.
     */
    @Override
    public void takeDamage(int damage, double knockbackX, double knockbackY) {
        super.takeDamage(damage, knockbackX, knockbackY);

        // Rabbits flee when attacked but never become hostile
        if (currentHealth > 0) {
            isSleeping = false;
            sleepTimer = 0;
            idleTimer = MIN_IDLE_TIME;  // Ready to hop immediately

            // Flee in opposite direction from damage source
            facingRight = knockbackX < 0;
        }
    }

    /**
     * Gets the current rabbit animation state.
     */
    public RabbitAnimState getRabbitAnimState() {
        return currentRabbitState;
    }

    /**
     * Gets the rabbit variant name.
     */
    public String getVariant() {
        return variant;
    }

    /**
     * Sets whether this rabbit is sleeping.
     */
    public void setSleeping(boolean sleeping) {
        this.isSleeping = sleeping;
        if (sleeping) {
            setRabbitAnimState(RabbitAnimState.SLEEP);
        } else {
            setRabbitAnimState(RabbitAnimState.IDLE);
        }
    }

    /**
     * Checks if the rabbit is currently sleeping.
     */
    public boolean isSleeping() {
        return isSleeping;
    }

    @Override
    public String toString() {
        return "RabbitSprite{" +
                "variant='" + variant + "'" +
                ", pos=(" + (int)posX + "," + (int)posY + ")" +
                ", state=" + currentRabbitState +
                ", health=" + currentHealth + "/" + maxHealth +
                ", sleeping=" + isSleeping +
                "}";
    }
}
