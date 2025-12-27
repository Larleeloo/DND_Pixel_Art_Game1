package entity.mob;
import entity.*;
import entity.player.*;
import block.*;
import animation.*;
import animation.bone.*;
import graphics.*;

import java.awt.*;
import java.util.List;

/**
 * A quadruped (4-legged animal) mob entity using the QuadrupedSkeleton system.
 * Can be configured for different animal types: wolf, dog, cat, horse, pig, cow, etc.
 *
 * ============================================================================
 * BEHAVIOR TYPES:
 * ============================================================================
 *
 * PASSIVE   - Never attacks, flees when hurt
 * NEUTRAL   - Only attacks when provoked
 * HOSTILE   - Attacks player on sight
 *
 * ============================================================================
 */
public class QuadrupedMobEntity extends MobEntity {

    /**
     * Behavior type for the mob.
     */
    public enum BehaviorType {
        PASSIVE,    // Never attacks (cow, sheep, pig)
        NEUTRAL,    // Attacks when provoked (wolf pack, bear)
        HOSTILE     // Attacks on sight (wolf)
    }

    // Mob configuration
    private QuadrupedSkeleton.AnimalType animalType;
    private BehaviorType behaviorType;
    private String textureDir;
    private boolean useTextures = false;

    // Spawn and territory
    private double spawnX;
    private double spawnY;
    private double territoryRadius = 300;

    // Pack behavior
    private boolean isPack = false;
    private QuadrupedMobEntity packLeader;

    /**
     * Creates a new quadruped mob.
     *
     * @param x           Initial X position
     * @param y           Initial Y position
     * @param animalType  Type of animal (wolf, horse, etc.)
     * @param behaviorType How the mob behaves (passive, neutral, hostile)
     */
    public QuadrupedMobEntity(int x, int y, QuadrupedSkeleton.AnimalType animalType,
                              BehaviorType behaviorType) {
        super(x, y);
        this.animalType = animalType;
        this.behaviorType = behaviorType;
        this.spawnX = x;
        this.spawnY = y;

        // Configure based on animal type
        configureForAnimal();

        // Create skeleton and animations
        createSkeleton();
        setupAnimations();

        // Set wander bounds based on territory
        setWanderBounds(spawnX - territoryRadius, spawnX + territoryRadius);
    }

    /**
     * Creates a quadruped mob with textures from a directory.
     */
    public QuadrupedMobEntity(int x, int y, QuadrupedSkeleton.AnimalType animalType,
                              BehaviorType behaviorType, String textureDir) {
        this(x, y, animalType, behaviorType);
        this.textureDir = textureDir;
        this.useTextures = true;

        // Reload skeleton with textures
        if (useTextures && textureDir != null) {
            this.skeleton = QuadrupedSkeleton.createQuadrupedWithTextures(animalType, textureDir);
            setupAnimations();
            skeleton.playAnimation("idle");
        }
    }

    /**
     * Configures mob stats based on animal type.
     */
    private void configureForAnimal() {
        QuadrupedSkeleton.AnimalConfig config = QuadrupedSkeleton.getConfig(animalType);

        // Hitbox sizes increased by ~40% for better hit detection
        // Visual skeleton is often larger than collision box
        switch (animalType) {
            case WOLF:
                maxHealth = 20;
                currentHealth = maxHealth;
                attackDamage = 6;
                attackRange = 40;
                detectionRange = 250;
                wanderSpeed = 60;
                chaseSpeed = 150;
                hitboxWidth = 85;   // Was 60
                hitboxHeight = 56;  // Was 40
                animationScale = 1.0;
                break;

            case DOG:
                maxHealth = 15;
                currentHealth = maxHealth;
                attackDamage = 4;
                attackRange = 35;
                detectionRange = 200;
                wanderSpeed = 70;
                chaseSpeed = 140;
                hitboxWidth = 70;   // Was 50
                hitboxHeight = 50;  // Was 35
                animationScale = 0.9;
                break;

            case CAT:
                maxHealth = 10;
                currentHealth = maxHealth;
                attackDamage = 3;
                attackRange = 30;
                detectionRange = 150;
                wanderSpeed = 50;
                chaseSpeed = 120;
                hitboxWidth = 56;   // Was 40
                hitboxHeight = 35;  // Was 25
                animationScale = 0.7;
                break;

            case HORSE:
                maxHealth = 30;
                currentHealth = maxHealth;
                attackDamage = 8;
                attackRange = 50;
                detectionRange = 180;
                wanderSpeed = 80;
                chaseSpeed = 200;
                hitboxWidth = 126;  // Was 90
                hitboxHeight = 100; // Was 70
                animationScale = 1.5;
                break;

            case PIG:
                maxHealth = 12;
                currentHealth = maxHealth;
                attackDamage = 2;
                attackRange = 25;
                detectionRange = 100;
                wanderSpeed = 40;
                chaseSpeed = 80;
                hitboxWidth = 77;   // Was 55
                hitboxHeight = 50;  // Was 35
                animationScale = 1.1;
                break;

            case COW:
                maxHealth = 25;
                currentHealth = maxHealth;
                attackDamage = 5;
                attackRange = 45;
                detectionRange = 120;
                wanderSpeed = 35;
                chaseSpeed = 70;
                hitboxWidth = 112;  // Was 80
                hitboxHeight = 77;  // Was 55
                animationScale = 1.4;
                break;

            case SHEEP:
                maxHealth = 10;
                currentHealth = maxHealth;
                attackDamage = 1;
                attackRange = 20;
                detectionRange = 100;
                wanderSpeed = 35;
                chaseSpeed = 90;
                hitboxWidth = 70;   // Was 50
                hitboxHeight = 56;  // Was 40
                animationScale = 1.0;
                break;

            case DEER:
                maxHealth = 15;
                currentHealth = maxHealth;
                attackDamage = 4;
                attackRange = 40;
                detectionRange = 300;
                wanderSpeed = 60;
                chaseSpeed = 180;
                hitboxWidth = 77;   // Was 55
                hitboxHeight = 77;  // Was 55
                animationScale = 1.1;
                break;

            case BEAR:
                maxHealth = 50;
                currentHealth = maxHealth;
                attackDamage = 12;
                attackRange = 60;
                detectionRange = 200;
                wanderSpeed = 40;
                chaseSpeed = 100;
                hitboxWidth = 112;  // Was 80
                hitboxHeight = 90;  // Was 65
                animationScale = 1.5;
                break;

            case FOX:
                maxHealth = 12;
                currentHealth = maxHealth;
                attackDamage = 4;
                attackRange = 35;
                detectionRange = 200;
                wanderSpeed = 55;
                chaseSpeed = 130;
                hitboxWidth = 63;   // Was 45
                hitboxHeight = 42;  // Was 30
                animationScale = 0.8;
                break;

            default:
                // Default values from MobEntity
                break;
        }

        // Adjust hitbox offsets
        hitboxOffsetX = -hitboxWidth / 2;
        hitboxOffsetY = -hitboxHeight;

        // Calculate skeleton offset for proper vertical positioning
        // The skeleton anchor is at body center, but paw bottoms need to be on ground
        // Legs attach at bodyHeight/2, then: upperLeg + lowerLeg + pawHeight
        QuadrupedSkeleton.AnimalConfig animalConfig = QuadrupedSkeleton.getConfig(animalType);
        int bodyHeight = (int)(24 * animalConfig.bodyScaleY);
        int legLength = (int)(16 * animalConfig.legLengthMultiplier);
        int pawHeight = 6;
        // Total offset from body center to paw bottom
        // Add extra buffer (8 pixels) to prevent legs clipping into ground during animation
        // (leg rotation during walk can extend paws beyond rest position)
        skeletonOffsetY = (bodyHeight / 2) + legLength + legLength + pawHeight + 8;
    }

    @Override
    protected void createSkeleton() {
        if (useTextures && textureDir != null) {
            // Use custom texture directory
            this.skeleton = QuadrupedSkeleton.createQuadrupedWithTextures(animalType, textureDir);
        } else {
            // Use TextureManager to ensure PNG textures exist (generates defaults if missing)
            // Then load them from files so users can edit them
            String texDir = TextureManager.ensureQuadrupedTextures(animalType);
            this.skeleton = QuadrupedSkeleton.createQuadruped(animalType);
            TextureManager.applyTexturesFromDir(skeleton, texDir, QuadrupedSkeleton.getQuadrupedBoneNames());
        }
    }

    @Override
    protected void setupAnimations() {
        if (skeleton != null) {
            QuadrupedAnimation.addAllAnimations(skeleton);
            skeleton.playAnimation("idle");
        }
    }

    @Override
    protected void performAttack() {
        if (target == null) return;

        // Use distance to player's hitbox edge, not center
        double distance = getDistanceToTargetFace();

        if (distance <= attackRange) {
            // Deal damage to player
            System.out.println(animalType.name() + " attacks for " + attackDamage + " damage!");

            // Calculate knockback direction based on position relative to player hitbox
            Rectangle playerBounds = target.getBounds();
            double playerCenterX = playerBounds.x + playerBounds.width / 2;
            double knockbackDir = posX < playerCenterX ? 1 : -1;
            // target.takeDamage(attackDamage, knockbackDir * 5, -3);
        }
    }

    @Override
    protected void checkStateTransitions() {
        // Behavior-specific state transitions
        switch (behaviorType) {
            case PASSIVE:
                // Never chase or attack
                if (currentState == AIState.CHASE || currentState == AIState.ATTACK) {
                    changeState(AIState.FLEE);
                }
                break;

            case NEUTRAL:
                // Only attack if provoked (took damage recently)
                if (invincibilityTimer > 0 && currentState != AIState.CHASE &&
                    currentState != AIState.ATTACK && currentState != AIState.HURT) {
                    if (target != null && getDistanceToTarget() < detectionRange) {
                        changeState(AIState.CHASE);
                    }
                }
                break;

            case HOSTILE:
                // Default hostile behavior from parent class
                super.checkStateTransitions();
                break;
        }

        // Call parent for remaining transitions
        if (behaviorType == BehaviorType.HOSTILE) {
            // Already called
        } else {
            // Handle non-hostile idle/wander transitions
            if (currentState != AIState.CHASE && currentState != AIState.ATTACK &&
                currentState != AIState.FLEE && currentState != AIState.HURT &&
                currentState != AIState.DEAD) {

                if (currentState == AIState.IDLE && stateTimer > idleDuration) {
                    changeState(AIState.WANDER);
                } else if (currentState == AIState.WANDER && stateTimer > wanderDuration) {
                    changeState(AIState.IDLE);
                }
            }
        }
    }

    @Override
    protected void updateFleeState(double deltaTime) {
        if (target == null) {
            changeState(AIState.WANDER);
            return;
        }

        // Run away from threat
        double dx = target.getX() - posX;
        facingRight = dx < 0;  // Face away from target
        velocityX = facingRight ? chaseSpeed * 1.2 : -chaseSpeed * 1.2;

        // Stop fleeing after getting far enough away or timeout
        if (getDistanceToTarget() > loseTargetRange || stateTimer > 5.0) {
            changeState(AIState.WANDER);
        }
    }

    @Override
    public void takeDamage(int damage, double knockbackX, double knockbackY) {
        super.takeDamage(damage, knockbackX, knockbackY);

        // Passive mobs flee when hurt
        if (behaviorType == BehaviorType.PASSIVE && currentHealth > 0) {
            changeState(AIState.FLEE);
        }
    }

    // ==================== Draw with Health Bar ====================

    @Override
    public void draw(Graphics g) {
        if (skeleton == null) return;

        Graphics2D g2d = (Graphics2D) g;

        // Position skeleton with feet at ground level
        double scaledOffsetY = skeletonOffsetY * animationScale;
        skeleton.setPosition(posX, posY - scaledOffsetY);
        skeleton.setScale(animationScale);

        // Quadruped skeleton faces LEFT by default, so flip logic is inverted from humanoids
        // When facingRight is true, we need to flip the skeleton
        skeleton.setFlipX(facingRight);

        // Flash when hurt
        if (invincibilityTimer > 0) {
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

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString(currentState.name(), (int)posX - 20, (int)posY - hitboxHeight - 20);
            g2d.drawString("HP: " + currentHealth + "/" + maxHealth, (int)posX - 20, (int)posY - hitboxHeight - 10);
        }

        // Draw health bar above mob
        if (currentHealth < maxHealth && currentState != AIState.DEAD) {
            drawHealthBar(g);
        }
    }

    private void drawHealthBar(Graphics g) {
        int barWidth = 40;
        int barHeight = 4;
        int barX = (int)posX - barWidth / 2;
        int barY = (int)posY - hitboxHeight - 10;

        // Background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barWidth, barHeight);

        // Health fill
        int healthWidth = (int)((double)currentHealth / maxHealth * barWidth);
        g.setColor(getHealthColor());
        g.fillRect(barX, barY, healthWidth, barHeight);

        // Border
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barWidth, barHeight);
    }

    private Color getHealthColor() {
        double healthPercent = (double)currentHealth / maxHealth;
        if (healthPercent > 0.6) return Color.GREEN;
        if (healthPercent > 0.3) return Color.YELLOW;
        return Color.RED;
    }

    // ==================== Getters ====================

    public QuadrupedSkeleton.AnimalType getAnimalType() {
        return animalType;
    }

    public BehaviorType getBehaviorType() {
        return behaviorType;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a hostile wolf mob.
     */
    public static QuadrupedMobEntity createWolf(int x, int y) {
        return new QuadrupedMobEntity(x, y, QuadrupedSkeleton.AnimalType.WOLF, BehaviorType.HOSTILE);
    }

    /**
     * Creates a passive pig mob.
     */
    public static QuadrupedMobEntity createPig(int x, int y) {
        return new QuadrupedMobEntity(x, y, QuadrupedSkeleton.AnimalType.PIG, BehaviorType.PASSIVE);
    }

    /**
     * Creates a passive cow mob.
     */
    public static QuadrupedMobEntity createCow(int x, int y) {
        return new QuadrupedMobEntity(x, y, QuadrupedSkeleton.AnimalType.COW, BehaviorType.PASSIVE);
    }

    /**
     * Creates a passive sheep mob.
     */
    public static QuadrupedMobEntity createSheep(int x, int y) {
        return new QuadrupedMobEntity(x, y, QuadrupedSkeleton.AnimalType.SHEEP, BehaviorType.PASSIVE);
    }

    /**
     * Creates a neutral bear mob.
     */
    public static QuadrupedMobEntity createBear(int x, int y) {
        return new QuadrupedMobEntity(x, y, QuadrupedSkeleton.AnimalType.BEAR, BehaviorType.NEUTRAL);
    }

    /**
     * Creates a passive deer mob.
     */
    public static QuadrupedMobEntity createDeer(int x, int y) {
        return new QuadrupedMobEntity(x, y, QuadrupedSkeleton.AnimalType.DEER, BehaviorType.PASSIVE);
    }

    /**
     * Creates a neutral fox mob.
     */
    public static QuadrupedMobEntity createFox(int x, int y) {
        return new QuadrupedMobEntity(x, y, QuadrupedSkeleton.AnimalType.FOX, BehaviorType.NEUTRAL);
    }

    /**
     * Creates a friendly dog mob.
     */
    public static QuadrupedMobEntity createDog(int x, int y) {
        return new QuadrupedMobEntity(x, y, QuadrupedSkeleton.AnimalType.DOG, BehaviorType.PASSIVE);
    }

    /**
     * Creates a passive horse mob.
     */
    public static QuadrupedMobEntity createHorse(int x, int y) {
        return new QuadrupedMobEntity(x, y, QuadrupedSkeleton.AnimalType.HORSE, BehaviorType.PASSIVE);
    }

    /**
     * Creates a hostile cat mob (like a wild cat).
     */
    public static QuadrupedMobEntity createWildCat(int x, int y) {
        return new QuadrupedMobEntity(x, y, QuadrupedSkeleton.AnimalType.CAT, BehaviorType.HOSTILE);
    }
}
