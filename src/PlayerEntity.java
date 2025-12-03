import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class PlayerEntity extends SpriteEntity {

    private double velY = 0;
    private final double gravity = 0.5;
    private final double jumpStrength = -10;

    private boolean onGround = false;

    // Jump animation (optional GIF)
    private ImageIcon jumpAnimatedIcon;
    private Image jumpSpriteImage;
    private int jumpWidth, jumpHeight;

    // Simple ground height - use screen height from GamePanel
    private static final int GROUND_Y = 720; // Match GamePanel.GROUND_Y

    public PlayerEntity(int x, int y, String spritePath) {
        super(x, y, spritePath, false);

        // Optional jump animation
        tryLoadJumpSprite(spritePath.replace(".png", "_jump.gif"));

        System.out.println("Player created at: x=" + x + " y=" + y + " width=" + width + " height=" + height);
        System.out.println("Ground level set at: " + GROUND_Y);
    }

    private void tryLoadJumpSprite(String path) {
        try {
            AssetLoader.ImageAsset jumpAsset = AssetLoader.load(path);

            // Store both animated and static versions
            jumpAnimatedIcon = jumpAsset.animatedIcon;
            jumpSpriteImage = jumpAsset.staticImage;
            jumpWidth = jumpAsset.width * SCALE;
            jumpHeight = jumpAsset.height * SCALE;

            System.out.println("Loaded jump sprite: " + path +
                    " (animated=" + (jumpAnimatedIcon != null) + ")");
        } catch (Exception e) {
            System.out.println("Jump animation not found: " + path + " - " + e.getMessage());
        }
    }

    public void update(InputManager input, ArrayList<Entity> entities) {
        int speed = 4;
        int newX = x;
        int newY = y;

        // Horizontal movement
        if (input.isKeyPressed('a')) newX -= speed;
        if (input.isKeyPressed('d')) newX += speed;

        // Check horizontal collision with obstacles
        Rectangle futureXBounds = new Rectangle(newX, y, width, height);
        boolean xCollision = false;

        for (Entity e : entities) {
            if (e instanceof SpriteEntity && ((SpriteEntity) e).isSolid() && e != this) {
                if (futureXBounds.intersects(e.getBounds())) {
                    xCollision = true;
                    break;
                }
            }
        }

        // Only apply horizontal movement if no collision
        if (!xCollision) {
            x = newX;
        }

        // Jumping
        if (input.isKeyPressed(' ') && onGround) {
            velY = jumpStrength;
            onGround = false;
            System.out.println("Jump! velY=" + velY);
        }

        // Apply gravity
        velY += gravity;

        // Calculate new Y position
        newY += (int)velY;

        // Check vertical collision with obstacles
        Rectangle futureYBounds = new Rectangle(x, newY, width, height);
        boolean yCollisionAbove = false;
        boolean yCollisionBelow = false;

        for (Entity e : entities) {
            if (e instanceof SpriteEntity && ((SpriteEntity) e).isSolid() && e != this) {
                if (futureYBounds.intersects(e.getBounds())) {
                    // Determine if collision is from above or below
                    if (velY > 0) {
                        // Falling down, hit obstacle from above
                        yCollisionBelow = true;
                        newY = e.getBounds().y - height;
                        velY = 0;
                        onGround = true;
                    } else if (velY < 0) {
                        // Jumping up, hit obstacle from below
                        yCollisionAbove = true;
                        newY = e.getBounds().y + e.getBounds().height;
                        velY = 0;
                    }
                    break;
                }
            }
        }

        // Ground collision (if not already standing on obstacle)
        if (!yCollisionBelow && newY + height >= GROUND_Y) {
            newY = GROUND_Y - height;
            velY = 0;
            onGround = true;
        } else if (!yCollisionBelow && !yCollisionAbove) {
            onGround = false;
        }

        // Apply vertical movement
        y = newY;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Determine what to draw
        boolean useJumpAnimation = !onGround && (jumpAnimatedIcon != null || jumpSpriteImage != null);

        if (useJumpAnimation) {
            // Draw jump animation
            if (jumpAnimatedIcon != null) {
                // Use animated GIF - scale it properly
                Image jumpImage = jumpAnimatedIcon.getImage();
                g.drawImage(jumpImage, x, y, jumpWidth, jumpHeight, null);
            } else if (jumpSpriteImage != null) {
                // Fallback to static jump image
                g.drawImage(jumpSpriteImage, x, y, jumpWidth, jumpHeight, null);
            }
        } else {
            // Draw normal sprite
            if (sprite != null) {
                g.drawImage(sprite, x, y, width, height, null);
            } else {
                // Fallback placeholder
                g.setColor(Color.YELLOW);
                g.fillRect(x, y, width, height);
            }
        }

        // Debug: draw bounds - RED for ground, ORANGE for air (optional - remove later)
        g.setColor(onGround ? Color.RED : Color.ORANGE);
        g2d.setStroke(new BasicStroke(2));
        g.drawRect(x, y, width, height);
    }
}