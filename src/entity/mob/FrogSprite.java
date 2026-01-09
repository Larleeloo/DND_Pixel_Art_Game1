package entity.mob;

import entity.*;
import animation.*;
import graphics.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

/**
 * FrogSprite - A sprite-based passive mob entity representing a frog.
 *
 * This mob uses custom animation states specific to frog behavior:
 * - hop: Movement animation (replaces walk/run)
 * - tongue: Attack animation with delayed damage (0.5s into 1s animation)
 * - hurt: Damage reaction animation
 * - sleep: Idle/resting animation (replaces standard idle)
 *
 * Frogs are passive mobs that will only attack if provoked.
 * The tongue attack deals damage at 0.5 seconds into the 1 second animation.
 *
 * Asset folder structure:
 *   assets/mobs/frog/{variant}/
 *     - hop.gif (movement animation)
 *     - tongue.gif (attack animation, ~1 second duration)
 *     - hurt.gif (damage reaction)
 *     - sleep.gif (idle animation)
 *
 * Usage in level JSON:
 *   "mobType": "frog",
 *   "spriteDir": "assets/mobs/frog/purple_frog"
 */
public class FrogSprite extends SpriteMobEntity {

    // Frog-specific animation states
    public enum FrogAnimState {
        HOP,        // Movement animation
        TONGUE,     // Attack animation
        HURT,       // Damage reaction
        SLEEP       // Idle/resting
    }

    // Current frog animation state
    protected FrogAnimState currentFrogState = FrogAnimState.SLEEP;

    // Tongue attack timing
    protected static final double TONGUE_ANIMATION_DURATION = 1.0;  // 1 second
    protected static final double TONGUE_DAMAGE_DELAY = 0.5;        // 0.5 seconds into animation
    protected double tongueAnimTimer = 0;
    protected boolean tongueHasDealtDamage = false;

    // Frog variant (e.g., "purple_frog", "green_frog", etc.)
    protected String variant;

    // Hopping behavior - Animation timing:
    // 0-400ms: Preparation (on ground, crouching)
    // 400-800ms: In air
    // 800-1000ms: Landing transition to idle
    protected double hopCooldown = 0;
    protected static final double HOP_PREP_TIME = 0.4;    // 400ms preparation before jump
    protected static final double HOP_AIR_TIME = 0.4;     // 400ms in air (400-800ms of animation)
    protected static final double HOP_STRENGTH = -8;      // Vertical velocity for ~400ms air time
    protected static final double HOP_SPEED = 60;         // Horizontal movement speed
    protected double hopPrepTimer = 0;                    // Tracks preparation time
    protected double hopAirTimer = 0;                     // Tracks time in air
    protected boolean isHopping = false;                  // Currently in a hop (includes prep)
    protected boolean isPreparing = false;                // In preparation phase (before launch)
    protected boolean wasOnGround = true;                 // Track ground state changes

    // Sleep behavior
    protected double sleepTimer = 0;
    protected static final double MIN_SLEEP_TIME = 2.0;  // Minimum time sleeping
    protected boolean isSleeping = true;

    /**
     * Creates a new FrogSprite entity.
     *
     * @param x Initial X position
     * @param y Initial Y position
     * @param spriteDir Directory containing frog animation GIFs
     */
    public FrogSprite(int x, int y, String spriteDir) {
        super(x, y, spriteDir);

        // Extract variant from sprite directory
        this.variant = extractVariant(spriteDir);

        // Configure frog-specific settings
        configureFrogStats();

        // Set body type to small quadruped and apply frog dimensions
        // (animations already loaded via overridden loadAnimations)
        this.bodyType = MobBodyType.SMALL;
        applyFrogDimensions();

        // Frogs are passive by default
        setHostile(false);
        this.detectionRange = 0;  // Don't detect players

        System.out.println("FrogSprite: Created with spriteDir=" + spriteDir + ", variant=" + variant);
    }

    /**
     * Extracts the variant name from the sprite directory path.
     * e.g., "assets/mobs/frog/purple_frog" -> "purple_frog"
     */
    private String extractVariant(String spriteDir) {
        if (spriteDir == null) return "default";
        String[] parts = spriteDir.replace("\\", "/").split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "default";
    }

    /**
     * Configures frog-specific stats.
     */
    private void configureFrogStats() {
        this.maxHealth = 15;
        this.currentHealth = maxHealth;
        this.attackDamage = 3;
        this.wanderSpeed = HOP_SPEED;
        this.chaseSpeed = HOP_SPEED * 1.5;
        this.attackRange = 60;           // Tongue reach
        this.attackCooldown = 2.0;       // Time between attacks
        this.jumpStrength = HOP_STRENGTH;
    }

    /**
     * Applies frog-specific dimensions (small quadruped).
     */
    private void applyFrogDimensions() {
        // Frogs are small: 32x32 base, scaled to 64x64
        this.spriteWidth = 32 * SCALE;
        this.spriteHeight = 32 * SCALE;
        this.hitboxWidth = (int)(spriteWidth * 0.7);
        this.hitboxHeight = (int)(spriteHeight * 0.6);
        this.hitboxOffsetX = -hitboxWidth / 2;
        this.hitboxOffsetY = -hitboxHeight;
    }

    /**
     * Loads frog-specific animations from the sprite directory.
     * Maps frog states to standard ActionStates:
     * - hop -> WALK (and RUN)
     * - tongue -> ATTACK
     * - hurt -> HURT
     * - sleep -> IDLE
     */
    @Override
    protected void loadAnimations(String dir) {
        // Don't call super - we use custom loading
        loadFrogAnimations(dir);
    }

    /**
     * Loads frog-specific animations.
     */
    private void loadFrogAnimations(String dir) {
        System.out.println("FrogSprite: Loading animations from " + dir);

        // Load custom frog animations and map to standard states
        String sleepPath = dir + "/sleep.gif";
        String hopPath = dir + "/hop.gif";
        String tonguePath = dir + "/tongue.gif";
        String hurtPath = dir + "/hurt.gif";

        // Check if files exist before loading
        java.io.File sleepFile = new java.io.File(sleepPath);
        java.io.File hopFile = new java.io.File(hopPath);
        System.out.println("FrogSprite: sleep.gif exists: " + sleepFile.exists() + " at " + sleepFile.getAbsolutePath());
        System.out.println("FrogSprite: hop.gif exists: " + hopFile.exists() + " at " + hopFile.getAbsolutePath());

        boolean sleepLoaded = spriteAnimation.loadAction(SpriteAnimation.ActionState.IDLE, sleepPath);
        boolean hopLoaded = spriteAnimation.loadAction(SpriteAnimation.ActionState.JUMP, hopPath);
        // Also load hop for walk/run as fallback
        spriteAnimation.loadAction(SpriteAnimation.ActionState.WALK, hopPath);
        spriteAnimation.loadAction(SpriteAnimation.ActionState.RUN, hopPath);
        spriteAnimation.loadAction(SpriteAnimation.ActionState.ATTACK, tonguePath);
        spriteAnimation.loadAction(SpriteAnimation.ActionState.HURT, hurtPath);

        System.out.println("FrogSprite: sleep loaded: " + sleepLoaded + ", hop loaded: " + hopLoaded);
        System.out.println("FrogSprite: Base dimensions: " + spriteAnimation.getBaseWidth() + "x" + spriteAnimation.getBaseHeight());

        // Ensure basic animations exist (creates placeholders if needed)
        spriteAnimation.ensureBasicAnimations();

        // If no valid dimensions loaded, create frog-specific placeholder
        if (spriteAnimation.getBaseWidth() <= 0) {
            System.out.println("FrogSprite: Creating placeholder (no valid dimensions loaded)");
            createFrogPlaceholder();
        }
    }

    /**
     * Creates a frog-shaped placeholder sprite.
     */
    private void createFrogPlaceholder() {
        int w = 32 * SCALE;  // 64px
        int h = 32 * SCALE;  // 64px

        // Determine color based on variant
        Color bodyColor = getFrogColor();
        Color darkerColor = bodyColor.darker();

        BufferedImage placeholder = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = placeholder.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw frog body (oval)
        g.setColor(bodyColor);
        g.fillOval(8, 16, w - 16, h - 24);

        // Draw head (slightly overlapping body)
        g.fillOval(w/2 - 16, 8, 32, 24);

        // Draw eyes (large, bulging)
        g.setColor(Color.WHITE);
        g.fillOval(w/2 - 14, 4, 12, 12);
        g.fillOval(w/2 + 2, 4, 12, 12);

        // Draw pupils
        g.setColor(Color.BLACK);
        g.fillOval(w/2 - 10, 6, 6, 6);
        g.fillOval(w/2 + 4, 6, 6, 6);

        // Draw front legs
        g.setColor(darkerColor);
        g.fillOval(12, h - 16, 10, 10);
        g.fillOval(w - 22, h - 16, 10, 10);

        // Draw back legs (larger)
        g.fillOval(4, h - 20, 14, 16);
        g.fillOval(w - 18, h - 20, 14, 16);

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
        spriteAnimation.setAction(SpriteAnimation.ActionState.ATTACK, anim);
        spriteAnimation.setAction(SpriteAnimation.ActionState.HURT, anim);
    }

    /**
     * Gets the frog's body color based on variant.
     */
    private Color getFrogColor() {
        if (variant == null) return new Color(50, 150, 50);  // Default green

        String lowerVariant = variant.toLowerCase();
        if (lowerVariant.contains("purple")) {
            return new Color(128, 60, 180);   // Purple
        } else if (lowerVariant.contains("blue")) {
            return new Color(60, 120, 200);   // Blue
        } else if (lowerVariant.contains("red") || lowerVariant.contains("poison")) {
            return new Color(200, 50, 50);    // Red/poison dart
        } else if (lowerVariant.contains("gold") || lowerVariant.contains("yellow")) {
            return new Color(220, 180, 50);   // Golden
        } else if (lowerVariant.contains("brown") || lowerVariant.contains("toad")) {
            return new Color(139, 90, 60);    // Brown toad
        } else {
            return new Color(50, 150, 50);    // Default green
        }
    }

    /**
     * Updates the frog entity.
     */
    @Override
    public void update(double deltaTime, List<Entity> entities) {
        // Update sleep timer
        if (isSleeping) {
            sleepTimer += deltaTime;
        }

        // Update tongue attack timing
        if (currentFrogState == FrogAnimState.TONGUE) {
            tongueAnimTimer += deltaTime;

            // Deal damage at 0.5 seconds into the animation
            if (!tongueHasDealtDamage && tongueAnimTimer >= TONGUE_DAMAGE_DELAY) {
                performTongueDamage();
                tongueHasDealtDamage = true;
            }

            // End attack animation after 1 second
            if (tongueAnimTimer >= TONGUE_ANIMATION_DURATION) {
                endTongueAttack();
            }
        }

        // Call parent update
        super.update(deltaTime, entities);

        // Update frog-specific animation state
        updateFrogAnimState();
    }

    /**
     * Updates the frog's animation state based on behavior.
     * Handles the hop -> idle transition on landing.
     * Animation timing: 0-400ms prep, 400-800ms air, 800-1000ms landing to idle.
     */
    private void updateFrogAnimState() {
        // Don't change state during tongue attack
        if (currentFrogState == FrogAnimState.TONGUE) {
            return;
        }

        // Hurt state takes priority
        if (currentState == AIState.HURT) {
            setFrogAnimState(FrogAnimState.HURT);
            isHopping = false;
            isPreparing = false;
            return;
        }

        // Detect landing (was in air, now on ground) - triggers 800-1000ms of animation
        if (!wasOnGround && onGround) {
            // Just landed - transition to idle (landing frames 800-1000ms handled by animation)
            isHopping = false;
            isPreparing = false;
            hopAirTimer = 0;
            hopPrepTimer = 0;
            velocityX = 0;  // Stop horizontal movement on landing
            setFrogAnimState(FrogAnimState.SLEEP);
            isSleeping = true;
            sleepTimer = 0;
        }
        // Currently in air (hopping) - 400-800ms of animation
        else if (!onGround && !isPreparing) {
            setFrogAnimState(FrogAnimState.HOP);
            isSleeping = false;
            sleepTimer = 0;
        }
        // In preparation phase on ground - 0-400ms of animation
        else if (onGround && isHopping && isPreparing) {
            setFrogAnimState(FrogAnimState.HOP);
            isSleeping = false;
            sleepTimer = 0;
        }
        // On ground and idle (not hopping or preparing)
        else if (onGround && !isHopping && !isPreparing) {
            setFrogAnimState(FrogAnimState.SLEEP);
            isSleeping = true;
        }

        // Track ground state for next frame
        wasOnGround = onGround;
    }

    /**
     * Sets the frog animation state and syncs with sprite animation.
     */
    private void setFrogAnimState(FrogAnimState state) {
        if (currentFrogState != state) {
            currentFrogState = state;

            switch (state) {
                case HOP:
                    spriteAnimation.setState(SpriteAnimation.ActionState.JUMP);
                    break;
                case TONGUE:
                    spriteAnimation.setState(SpriteAnimation.ActionState.ATTACK);
                    break;
                case HURT:
                    spriteAnimation.setState(SpriteAnimation.ActionState.HURT);
                    break;
                case SLEEP:
                default:
                    spriteAnimation.setState(SpriteAnimation.ActionState.IDLE);
                    break;
            }
        }
    }

    /**
     * Performs the frog's attack (tongue lash).
     * Overrides parent to use tongue timing mechanics.
     */
    @Override
    protected void performAttack() {
        if (target == null) return;

        double dist = getDistanceToTargetFace();

        // Only start tongue attack if in range and cooldown expired
        if (attackTimer <= 0 && dist <= attackRange) {
            startTongueAttack();
            attackTimer = attackCooldown;
        }
    }

    /**
     * Starts the tongue attack animation.
     */
    private void startTongueAttack() {
        currentFrogState = FrogAnimState.TONGUE;
        tongueAnimTimer = 0;
        tongueHasDealtDamage = false;
        spriteAnimation.setState(SpriteAnimation.ActionState.ATTACK);
    }

    /**
     * Performs the actual tongue damage at the correct timing.
     */
    private void performTongueDamage() {
        if (target == null) return;

        double dist = getDistanceToTargetFace();

        // Check if still in range
        if (dist <= attackRange * 1.2) {  // Slight leeway for moving targets
            // Calculate knockback direction
            Rectangle playerBounds = target.getBounds();
            double playerCenterX = playerBounds.x + playerBounds.width / 2;
            double knockbackDir = posX < playerCenterX ? 1 : -1;

            // Deal damage with light knockback
            target.takeDamage(attackDamage, knockbackDir * 2, -1);
        }
    }

    /**
     * Ends the tongue attack and returns to normal state.
     */
    private void endTongueAttack() {
        currentFrogState = FrogAnimState.SLEEP;
        tongueAnimTimer = 0;
        tongueHasDealtDamage = false;
        isSleeping = true;
        sleepTimer = 0;
    }

    /**
     * Frog-specific hopping movement.
     * Overrides wander state update to use hopping.
     * Animation timing: 0-400ms prep, 400-800ms air, 800-1000ms landing to idle.
     */
    @Override
    protected void updateWanderState(double deltaTime) {
        // Update hop cooldown
        if (hopCooldown > 0) {
            hopCooldown -= deltaTime;
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

        // On ground and not hopping - start a new hop sequence
        if (onGround && !isHopping && !isPreparing) {
            // Wait for cooldown before next hop
            if (hopCooldown <= 0) {
                // Random direction for wander
                if (Math.random() < 0.3) {  // 30% chance to change direction
                    facingRight = Math.random() > 0.5;
                }

                // Start preparation phase (0-400ms of animation)
                isHopping = true;
                isPreparing = true;
                hopPrepTimer = 0;
                hopAirTimer = 0;
                velocityX = 0;  // Stay still during preparation

                // Start hop animation (shows crouch/prep frames)
                setFrogAnimState(FrogAnimState.HOP);
            } else {
                // Waiting on ground - stay in idle
                velocityX = 0;
            }
        }
        // In preparation phase - stay on ground with hop animation
        else if (onGround && isHopping && isPreparing) {
            // Keep hop animation during prep
            setFrogAnimState(FrogAnimState.HOP);
        }
        // In the air - maintain hop animation
        else if (!onGround && isHopping) {
            // Keep hop animation while in air
            setFrogAnimState(FrogAnimState.HOP);
        }
        // Just landed - handled in updateFrogAnimState, set cooldown for next hop
        else if (onGround && wasOnGround && !isHopping && !isPreparing) {
            // Set a short cooldown before next hop (idle time on ground)
            if (hopCooldown <= 0) {
                hopCooldown = 0.5;  // Brief pause between hops
            }
        }

        // Update state timer for occasional rest
        stateTimer += deltaTime;
        if (stateTimer > 5.0 + Math.random() * 3) {
            // Small chance to rest longer
            if (Math.random() < 0.2) {
                changeState(AIState.IDLE);
            }
            stateTimer = 0;
        }
    }

    /**
     * Frog reaction to being hit.
     * Makes the frog briefly hostile.
     */
    @Override
    public void takeDamage(int damage, double knockbackX, double knockbackY) {
        super.takeDamage(damage, knockbackX, knockbackY);

        // Frog becomes temporarily aggressive when attacked
        if (currentHealth > 0) {
            setFrogAnimState(FrogAnimState.HURT);
            isSleeping = false;
            sleepTimer = 0;

            // Brief aggression period
            this.detectionRange = 150;
            // Will reset to passive after attack or fleeing
        }
    }

    /**
     * Gets the current frog animation state.
     */
    public FrogAnimState getFrogAnimState() {
        return currentFrogState;
    }

    /**
     * Gets the frog variant name.
     */
    public String getVariant() {
        return variant;
    }

    /**
     * Sets whether this frog is sleeping.
     */
    public void setSleeping(boolean sleeping) {
        this.isSleeping = sleeping;
        if (sleeping) {
            setFrogAnimState(FrogAnimState.SLEEP);
        }
    }

    /**
     * Checks if the frog is currently sleeping.
     */
    public boolean isSleeping() {
        return isSleeping;
    }

    @Override
    public String toString() {
        return "FrogSprite{" +
                "variant='" + variant + "'" +
                ", pos=(" + (int)posX + "," + (int)posY + ")" +
                ", state=" + currentFrogState +
                ", health=" + currentHealth + "/" + maxHealth +
                ", sleeping=" + isSleeping +
                "}";
    }
}
