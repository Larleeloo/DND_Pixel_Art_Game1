import java.awt.*;

class BackgroundEntity extends Entity {

    private Image image;
    private int width, height;

    public static final int SCALE = 10; // 10× scaling for backgrounds

    public BackgroundEntity(String path) {
        super(0, 0);

        AssetLoader.ImageAsset asset = AssetLoader.load(path);
        this.image = asset.staticImage;

        this.width  = asset.width  * SCALE;
        this.height = asset.height * SCALE;

        System.out.println("Background loaded: " + width + "x" + height);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(0, 0, width, height);
    }

    @Override
    public void update(InputManager input) {
        // Background doesn't update or collide
    }

    @Override
    public void draw(Graphics g) {
        // Make background semi-transparent so we can see things on top
        Graphics2D g2d = (Graphics2D) g;
        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

        g.drawImage(image, 0, 0, width, height, null);

        g2d.setComposite(oldComposite);

        // Draw a test rectangle to verify background is drawing
        g.setColor(Color.BLUE);
        g.drawRect(0, 0, 100, 100);
    }
}