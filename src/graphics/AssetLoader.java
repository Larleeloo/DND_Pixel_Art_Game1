package graphics;
import animation.*;
import block.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AssetLoader {

    public static class ImageAsset {
        public final BufferedImage staticImage;
        public final ImageIcon animatedIcon;  // For GIFs
        public final int width;
        public final int height;

        public ImageAsset(BufferedImage staticImage, ImageIcon animatedIcon) {
            this.staticImage = staticImage;
            this.animatedIcon = animatedIcon;

            if (animatedIcon != null) {
                this.width = animatedIcon.getIconWidth();
                this.height = animatedIcon.getIconHeight();
            } else if (staticImage != null) {
                this.width = staticImage.getWidth();
                this.height = staticImage.getHeight();
            } else {
                this.width = 32;
                this.height = 32;
            }
        }
    }

    /**
     * Load an image from file path.
     * Supports both static images (PNG, JPG) and animated GIFs.
     * Returns a 32x32 placeholder if loading fails.
     */
    public static ImageAsset load(String path) {
        try {
            File f = new File(path);
            if (f.exists()) {
                // Check if it's a GIF
                if (path.toLowerCase().endsWith(".gif")) {
                    ImageIcon icon = new ImageIcon(path);
                    // Also load first frame as static image
                    BufferedImage staticImg = ImageIO.read(f);
                    System.out.println("Loaded GIF: " + path + " (" + icon.getIconWidth() + "x" + icon.getIconHeight() + ")");
                    return new ImageAsset(staticImg, icon);
                } else {
                    // Static image (PNG, JPG, etc.)
                    BufferedImage img = ImageIO.read(f);
                    return new ImageAsset(img, null);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to load: " + path + " - " + e.getMessage());
        }

        // File not found - return null so bones use placeholder colors
        System.out.println("Asset not found: " + path);
        return new ImageAsset(null, null);
    }
}