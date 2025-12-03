import javax.swing.*;
import java.awt.*;

/**
 * Simplified SpriteEntity for rollback version.
 * - Handles static sprite (PNG) only, optional GIF for jump.
 * - Keeps width/height scaled by SCALE.
 */
class SpriteEntity extends Entity {

    protected Image sprite;       // image to draw
    protected ImageIcon animatedIcon; // optional animated GIF
    protected int width, height;
    private boolean solid;

    public static final int SCALE = 4;

    public SpriteEntity(int x, int y, String spritePath, boolean solid) {
        super(x, y);
        this.solid = solid;

        AssetLoader.ImageAsset asset = AssetLoader.load(spritePath);
        this.sprite = asset.staticImage;
        //this.animatedIcon = asset.animatedIcon; // optional GIF

        this.width = Math.max(1, asset.width) * SCALE;
        this.height = Math.max(1, asset.height) * SCALE;

        // Debug
        System.out.println("Loaded sprite \"" + spritePath + "\" -> w=" + asset.width + " h=" + asset.height
                + " scaled -> " + this.width + "x" + this.height
                + " animated=" + (this.animatedIcon != null));
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public void draw(Graphics g) {
        if (sprite != null) {
            g.drawImage(sprite, x, y, width, height, null);
        } else {
            // placeholder so we see something
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, width, height);
        }
    }

    public boolean isSolid() {
        return solid;
    }
}
