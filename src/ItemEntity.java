import java.awt.*;

/**
 * Represents a collectible item in the game world
 */
class ItemEntity extends Entity {

    private Image sprite;
    private int width, height;
    private String itemName;
    private String itemType;
    public boolean collected; // Made public so it can be reset when dropped
    private float bobOffset; // For floating animation
    private float bobSpeed;

    public static final int SCALE = 3;

    public ItemEntity(int x, int y, String spritePath, String itemName, String itemType) {
        super(x, y);
        this.itemName = itemName;
        this.itemType = itemType;
        this.collected = false;
        this.bobOffset = 0;
        this.bobSpeed = 0.05f;

        AssetLoader.ImageAsset asset = AssetLoader.load(spritePath);
        this.sprite = asset.staticImage;
        this.width = Math.max(1, asset.width) * SCALE;
        this.height = Math.max(1, asset.height) * SCALE;

        System.out.println("Item created: " + itemName + " (" + itemType + ")");
    }

    @Override
    public Rectangle getBounds() {
        if (collected) return new Rectangle(0, 0, 0, 0);
        return new Rectangle(x, y + (int)bobOffset, width, height);
    }

    @Override
    public void update(InputManager input) {
        if (!collected) {
            // Bobbing animation - use milliseconds for smooth animation
            bobOffset = (float)(Math.sin(System.currentTimeMillis() * 0.003) * 8);
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!collected) {
            Graphics2D g2d = (Graphics2D) g;

            // Draw glow effect
            g2d.setColor(new Color(255, 255, 100, 100));
            g2d.fillOval(x - 5, y + (int)bobOffset - 5, width + 10, height + 10);

            // Draw sprite
            if (sprite != null) {
                g.drawImage(sprite, x, y + (int)bobOffset, width, height, null);
            } else {
                // Fallback
                g.setColor(Color.YELLOW);
                g.fillRect(x, y + (int)bobOffset, width, height);
            }

            // Draw item name below
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            FontMetrics fm = g.getFontMetrics();
            int textX = x + (width - fm.stringWidth(itemName)) / 2;
            g.drawString(itemName, textX, y + height + (int)bobOffset + 15);
        }
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        this.collected = true;
        System.out.println("Collected: " + itemName);
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemType() {
        return itemType;
    }

    public Image getSprite() {
        return sprite;
    }
}