import java.awt.*;
import java.util.List;

/**
 * A humanoid mob entity using the standard 15-bone humanoid skeleton.
 * Can be configured for different enemy types: Zombie, Skeleton, Goblin, etc.
 *
 * ============================================================================
 * SPECIAL BEHAVIORS:
 * ============================================================================
 *
 * ZOMBIE:    Slow movement, arms outstretched, persistent chase
 * SKELETON:  Fast movement, jumpy, uses ranged attacks sometimes
 * GOBLIN:    Very fast, hits and runs, groups together
 * ORC:       Heavy hitter, slow but powerful
 * BANDIT:    Balanced, uses tactics
 * KNIGHT:    Heavily armored, blocks attacks
 * MAGE:      Stays at range, uses projectiles
 *
 * ============================================================================
 */
public class HumanoidMobEntity extends MobEntity {

    // Mob configuration
    private HumanoidVariants.VariantType variantType;
    private HumanoidVariants.VariantConfig config;
    private String textureDir;
    private boolean useTextures = false;

    // Spawn and territory
    private double spawnX;
    private double spawnY;
    private double territoryRadius = 400;

    // Special behavior flags
    private boolean canJump = false;
    private boolean isRanged = false;
    private double jumpCooldown = 2.0;
    private double jumpTimer = 0;

    /**
     * Creates a new humanoid mob.
     *
     * @param x           Initial X position
     * @param y           Initial Y position
     * @param variantType Type of humanoid (zombie, skeleton, etc.)
     */
    public HumanoidMobEntity(int x, int y, HumanoidVariants.VariantType variantType) {
        super(x, y);
        this.variantType = variantType;
        this.spawnX = x;
        this.spawnY = y;

        // Get configuration
        this.config = HumanoidVariants.getConfig(variantType);

        // Apply configuration
        configureFromVariant();

        // Create skeleton and animations
        createSkeleton();
        setupAnimations();

        // Set wander bounds
        setWanderBounds(spawnX - territoryRadius, spawnX + territoryRadius);
    }

    /**
     * Creates a humanoid mob with textures.
     */
    public HumanoidMobEntity(int x, int y, HumanoidVariants.VariantType variantType, String textureDir) {
        this(x, y, variantType);
        this.textureDir = textureDir;
        this.useTextures = true;

        // Reload skeleton with textures
        if (useTextures && textureDir != null) {
            this.skeleton = HumanoidVariants.createVariantWithTextures(variantType, textureDir);
            setupAnimations();
            skeleton.playAnimation("idle");
        }
    }

    /**
     * Configures mob stats from variant config.
     */
    private void configureFromVariant() {
        maxHealth = config.health;
        currentHealth = maxHealth;
        attackDamage = config.damage;
        wanderSpeed = config.speed * 0.5;
        chaseSpeed = config.speed;
        attackRange = config.attackRange;
        detectionRange = config.detectionRange;
        loseTargetRange = detectionRange * 2;

        // Hitbox based on scale - increased by 50% for better hit detection
        // Visual skeleton is often larger than collision box, so hitbox needs to match
        hitboxWidth = (int)(60 * config.scaleX);  // Was 40, now 60
        hitboxHeight = (int)(120 * config.scaleY); // Was 100, now 120
        hitboxOffsetX = -hitboxWidth / 2;
        hitboxOffsetY = -hitboxHeight;

        animationScale = config.scaleX;

        // Calculate skeleton offset for proper vertical positioning
        // Humanoid skeleton: torso at y=0, legs extend below
        // legUpperY=16, legLowerOffset=20, footOffset=20, footHeight=8
        // Total offset from torso center to feet: 16 + 20 + 20 + 8 = 64
        // Add small buffer (4 pixels) to prevent feet clipping during walk animation
        // Note: skeleton uses uniform scaling (scaleX), so don't pre-scale with scaleY
        // The draw() method will scale this by animationScale (which is scaleX)
        skeletonOffsetY = 64 + 4;

        // Variant-specific abilities
        switch (variantType) {
            case SKELETON:
                canJump = true;
                jumpCooldown = 3.0;
                break;
            case GOBLIN:
                canJump = true;
                jumpCooldown = 2.0;
                break;
            case MAGE:
                isRanged = true;
                attackRange = 150;
                break;
            default:
                break;
        }
    }

    @Override
    protected void createSkeleton() {
        if (useTextures && textureDir != null) {
            // Use custom texture directory
            this.skeleton = HumanoidVariants.createVariantWithTextures(variantType, textureDir);
        } else {
            // Use TextureManager to ensure PNG textures exist (generates defaults if missing)
            // Then load them from files so users can edit them
            String texDir = TextureManager.ensureHumanoidTextures(variantType.name().toLowerCase());
            this.skeleton = HumanoidVariants.createVariant(variantType);

            String[] humanoidBones = {
                "torso", "neck", "head",
                "arm_upper_left", "arm_upper_right",
                "arm_lower_left", "arm_lower_right",
                "hand_left", "hand_right",
                "leg_upper_left", "leg_upper_right",
                "leg_lower_left", "leg_lower_right",
                "foot_left", "foot_right"
            };
            TextureManager.applyTexturesFromDir(skeleton, texDir, humanoidBones);
        }
    }

    @Override
    protected void setupAnimations() {
        if (skeleton != null) {
            // Add standard humanoid animations
            skeleton.addAnimation(BoneAnimation.createIdleAnimation("torso"));
            skeleton.addAnimation(BoneAnimation.createWalkAnimation());  // For wandering
            skeleton.addAnimation(BoneAnimation.createRunAnimation());   // For chasing
            skeleton.addAnimation(BoneAnimation.createJumpAnimation());

            // Create variant-specific animations
            skeleton.addAnimation(createAttackAnimation());
            skeleton.addAnimation(createHurtAnimation());
            skeleton.addAnimation(createDeathAnimation());

            skeleton.playAnimation("idle");
        }
    }

    /**
     * Creates an attack animation for humanoids.
     */
    private BoneAnimation createAttackAnimation() {
        BoneAnimation anim = new BoneAnimation("attack", 0.5, false);

        // Torso twist
        anim.addKeyframe("torso", 0.0, 0, 0, 0);
        anim.addKeyframe("torso", 0.15, 0, 0, -15);  // Wind up
        anim.addKeyframe("torso", 0.3, 0, 0, 20);    // Strike
        anim.addKeyframe("torso", 0.5, 0, 0, 0);

        // Right arm swing (attack arm)
        anim.addKeyframe("arm_upper_right", 0.0, 0, 0, 0);
        anim.addKeyframe("arm_upper_right", 0.15, 0, 0, 60);    // Wind up back
        anim.addKeyframe("arm_upper_right", 0.3, 0, 0, -60);    // Swing forward
        anim.addKeyframe("arm_upper_right", 0.5, 0, 0, 0);

        anim.addKeyframe("arm_lower_right", 0.0, 0, 0, -15);
        anim.addKeyframe("arm_lower_right", 0.15, 0, 0, -60);   // Bent back
        anim.addKeyframe("arm_lower_right", 0.3, 0, 0, -20);    // Extended
        anim.addKeyframe("arm_lower_right", 0.5, 0, 0, -15);

        anim.addKeyframe("hand_right", 0.0, 0, 0, 0);
        anim.addKeyframe("hand_right", 0.15, 0, 0, 20);
        anim.addKeyframe("hand_right", 0.3, 0, 0, -10);
        anim.addKeyframe("hand_right", 0.5, 0, 0, 0);

        // Left arm counter-balance
        anim.addKeyframe("arm_upper_left", 0.0, 0, 0, 0);
        anim.addKeyframe("arm_upper_left", 0.15, 0, 0, -20);
        anim.addKeyframe("arm_upper_left", 0.3, 0, 0, 10);
        anim.addKeyframe("arm_upper_left", 0.5, 0, 0, 0);

        anim.addKeyframe("arm_lower_left", 0.0, 0, 0, -15);
        anim.addKeyframe("arm_lower_left", 0.5, 0, 0, -15);

        // Head follows strike
        anim.addKeyframe("head", 0.0, 0, 0, 0);
        anim.addKeyframe("head", 0.15, 0, 0, 5);
        anim.addKeyframe("head", 0.3, 0, 0, -10);
        anim.addKeyframe("head", 0.5, 0, 0, 0);

        // Legs stay mostly stable
        String[] legs = {"leg_upper_left", "leg_upper_right", "leg_lower_left",
                        "leg_lower_right", "foot_left", "foot_right"};
        for (String leg : legs) {
            anim.addKeyframe(leg, 0.0, 0, 0, 0);
            anim.addKeyframe(leg, 0.5, 0, 0, 0);
        }

        return anim;
    }

    /**
     * Creates a hurt animation for humanoids.
     */
    private BoneAnimation createHurtAnimation() {
        BoneAnimation anim = new BoneAnimation("hurt", 0.3, false);

        // Recoil back
        anim.addKeyframe("torso", 0.0, 0, 0, 0);
        anim.addKeyframe("torso", 0.1, 3, 2, -15);
        anim.addKeyframe("torso", 0.3, 0, 0, 0);

        anim.addKeyframe("head", 0.0, 0, 0, 0);
        anim.addKeyframe("head", 0.1, 0, -2, 20);
        anim.addKeyframe("head", 0.3, 0, 0, 0);

        // Arms flinch
        String[] arms = {"arm_upper_left", "arm_upper_right", "arm_lower_left", "arm_lower_right"};
        for (String arm : arms) {
            anim.addKeyframe(arm, 0.0, 0, 0, 0);
            anim.addKeyframe(arm, 0.1, 0, 0, 15);
            anim.addKeyframe(arm, 0.3, 0, 0, 0);
        }

        // Legs buckle
        anim.addKeyframe("leg_upper_left", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_upper_left", 0.1, 0, 0, 10);
        anim.addKeyframe("leg_upper_left", 0.3, 0, 0, 0);

        anim.addKeyframe("leg_upper_right", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_upper_right", 0.1, 0, 0, 10);
        anim.addKeyframe("leg_upper_right", 0.3, 0, 0, 0);

        return anim;
    }

    /**
     * Creates a death animation for humanoids.
     */
    private BoneAnimation createDeathAnimation() {
        BoneAnimation anim = new BoneAnimation("death", 1.2, false);

        // Fall backward
        anim.addKeyframe("torso", 0.0, 0, 0, 0);
        anim.addKeyframe("torso", 0.3, 0, 5, -20);
        anim.addKeyframe("torso", 0.6, 5, 20, -60);
        anim.addKeyframe("torso", 1.0, 10, 40, -90);
        anim.addKeyframe("torso", 1.2, 10, 45, -90);

        anim.addKeyframe("head", 0.0, 0, 0, 0);
        anim.addKeyframe("head", 0.3, 0, 0, 30);
        anim.addKeyframe("head", 0.6, 0, 0, 45);
        anim.addKeyframe("head", 1.2, 0, 5, 60);

        // Arms go limp
        anim.addKeyframe("arm_upper_left", 0.0, 0, 0, 0);
        anim.addKeyframe("arm_upper_left", 1.2, 0, 0, 90);
        anim.addKeyframe("arm_lower_left", 0.0, 0, 0, -15);
        anim.addKeyframe("arm_lower_left", 1.2, 0, 0, 30);

        anim.addKeyframe("arm_upper_right", 0.0, 0, 0, 0);
        anim.addKeyframe("arm_upper_right", 1.2, 0, 0, -70);
        anim.addKeyframe("arm_lower_right", 0.0, 0, 0, -15);
        anim.addKeyframe("arm_lower_right", 1.2, 0, 0, -40);

        // Legs splay
        anim.addKeyframe("leg_upper_left", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_upper_left", 1.2, 0, 0, 30);
        anim.addKeyframe("leg_lower_left", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_lower_left", 1.2, 0, 0, -20);

        anim.addKeyframe("leg_upper_right", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_upper_right", 1.2, 0, 0, -20);
        anim.addKeyframe("leg_lower_right", 0.0, 0, 0, 0);
        anim.addKeyframe("leg_lower_right", 1.2, 0, 0, 10);

        return anim;
    }

    @Override
    protected void performAttack() {
        if (target == null) return;

        // Use distance to player's hitbox edge, not center
        double distance = getDistanceToTargetFace();

        if (distance <= attackRange) {
            System.out.println(variantType.name() + " attacks for " + attackDamage + " damage!");

            // Calculate knockback direction based on position relative to player hitbox
            Rectangle playerBounds = target.getBounds();
            double playerCenterX = playerBounds.x + playerBounds.width / 2;
            double knockbackDir = posX < playerCenterX ? 1 : -1;
            // target.takeDamage(attackDamage, knockbackDir * 5, -3);
        }
    }

    @Override
    protected void updateChaseState(double deltaTime) {
        super.updateChaseState(deltaTime);

        // Update jump timer
        if (canJump) {
            jumpTimer = Math.max(0, jumpTimer - deltaTime);

            // Jump if player is above and we can jump
            if (target != null && jumpTimer <= 0 && onGround) {
                double dy = target.getY() - posY;
                if (dy < -50) {  // Player is significantly above
                    velocityY = -12;  // Jump!
                    jumpTimer = jumpCooldown;
                    if (skeleton != null) skeleton.playAnimation("jump");
                }
            }
        }

        // Ranged behavior - maintain distance
        if (isRanged && target != null) {
            double distance = getDistanceToTarget();
            if (distance < attackRange * 0.5) {
                // Too close, back up
                facingRight = target.getX() > posX;
                velocityX = facingRight ? -wanderSpeed : wanderSpeed;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);

        // Draw health bar
        if (currentHealth < maxHealth && currentState != AIState.DEAD) {
            drawHealthBar(g);
        }

        // Draw variant-specific effects
        drawVariantEffects(g);
    }

    private void drawHealthBar(Graphics g) {
        int barWidth = 50;
        int barHeight = 5;
        int barX = (int)posX - barWidth / 2;
        int barY = (int)posY - hitboxHeight - 15;

        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barWidth, barHeight);

        int healthWidth = (int)((double)currentHealth / maxHealth * barWidth);
        g.setColor(getHealthColor());
        g.fillRect(barX, barY, healthWidth, barHeight);

        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barWidth, barHeight);
    }

    private Color getHealthColor() {
        double percent = (double)currentHealth / maxHealth;
        if (percent > 0.6) return Color.GREEN;
        if (percent > 0.3) return Color.YELLOW;
        return Color.RED;
    }

    private void drawVariantEffects(Graphics g) {
        // Draw special effects based on variant
        if (variantType == HumanoidVariants.VariantType.MAGE && currentState == AIState.ATTACK) {
            // Magic glow
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(100, 50, 200, 100));
            g2d.fillOval((int)posX - 20, (int)posY - hitboxHeight / 2 - 20, 40, 40);
        }
    }

    // ==================== Getters ====================

    public HumanoidVariants.VariantType getVariantType() {
        return variantType;
    }

    // ==================== Factory Methods ====================

    public static HumanoidMobEntity createZombie(int x, int y) {
        return new HumanoidMobEntity(x, y, HumanoidVariants.VariantType.ZOMBIE);
    }

    public static HumanoidMobEntity createSkeleton(int x, int y) {
        return new HumanoidMobEntity(x, y, HumanoidVariants.VariantType.SKELETON);
    }

    public static HumanoidMobEntity createGoblin(int x, int y) {
        return new HumanoidMobEntity(x, y, HumanoidVariants.VariantType.GOBLIN);
    }

    public static HumanoidMobEntity createOrc(int x, int y) {
        return new HumanoidMobEntity(x, y, HumanoidVariants.VariantType.ORC);
    }

    public static HumanoidMobEntity createBandit(int x, int y) {
        return new HumanoidMobEntity(x, y, HumanoidVariants.VariantType.BANDIT);
    }

    public static HumanoidMobEntity createKnight(int x, int y) {
        return new HumanoidMobEntity(x, y, HumanoidVariants.VariantType.KNIGHT);
    }

    public static HumanoidMobEntity createMage(int x, int y) {
        return new HumanoidMobEntity(x, y, HumanoidVariants.VariantType.MAGE);
    }
}
