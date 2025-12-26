package graphics;
import animation.*;
import block.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handles loading of image assets from the filesystem.
 * Supports both static images (PNG, JPG) and animated GIFs with frame extraction.
 */
public class AssetLoader {

    /**
     * Represents a loaded image asset, which can be either static or animated.
     */
    public static class ImageAsset {
        public final BufferedImage staticImage;
        public final ImageIcon animatedIcon;  // For legacy GIF support
        public final AnimatedTexture animatedTexture;  // New GIF frame support
        public final int width;
        public final int height;

        public ImageAsset(BufferedImage staticImage, ImageIcon animatedIcon, AnimatedTexture animatedTexture) {
            this.staticImage = staticImage;
            this.animatedIcon = animatedIcon;
            this.animatedTexture = animatedTexture;

            if (animatedTexture != null) {
                this.width = animatedTexture.getWidth();
                this.height = animatedTexture.getHeight();
            } else if (animatedIcon != null) {
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

        // Legacy constructor for backward compatibility
        public ImageAsset(BufferedImage staticImage, ImageIcon animatedIcon) {
            this(staticImage, animatedIcon, null);
        }

        /**
         * Checks if this asset is animated (has multiple frames).
         * @return true if animated
         */
        public boolean isAnimated() {
            return animatedTexture != null && animatedTexture.isAnimated();
        }
    }

    /**
     * Load an image from file path.
     * Supports both static images (PNG, JPG) and animated GIFs.
     * For GIFs, extracts all frames with proper timing.
     *
     * @param path Path to the image file
     * @return ImageAsset containing the loaded image data
     */
    public static ImageAsset load(String path) {
        try {
            File f = new File(path);
            if (f.exists()) {
                // Check if it's a GIF
                if (path.toLowerCase().endsWith(".gif")) {
                    return loadGif(f, path);
                } else {
                    // Static image (PNG, JPG, etc.)
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        // Create single-frame AnimatedTexture for consistency
                        AnimatedTexture animTex = new AnimatedTexture(img);
                        return new ImageAsset(img, null, animTex);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to load: " + path + " - " + e.getMessage());
        }

        // File not found - return null so bones use placeholder colors
        System.out.println("Asset not found: " + path);
        return new ImageAsset(null, null, null);
    }

    /**
     * Loads a GIF file with full frame extraction and timing.
     *
     * @param file The GIF file
     * @param path Path string for logging
     * @return ImageAsset with animated texture
     */
    private static ImageAsset loadGif(File file, String path) throws IOException {
        List<BufferedImage> frames = new ArrayList<>();
        List<Integer> delays = new ArrayList<>();

        ImageInputStream stream = ImageIO.createImageInputStream(file);
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");

        if (!readers.hasNext()) {
            // Fallback to simple load if no GIF reader available
            BufferedImage img = ImageIO.read(file);
            AnimatedTexture animTex = new AnimatedTexture(img);
            return new ImageAsset(img, null, animTex);
        }

        ImageReader reader = readers.next();
        reader.setInput(stream);

        int numFrames = reader.getNumImages(true);

        // GIFs need to handle frame disposal properly
        BufferedImage masterImage = null;

        for (int i = 0; i < numFrames; i++) {
            BufferedImage frame = reader.read(i);
            IIOMetadata metadata = reader.getImageMetadata(i);

            // Extract frame delay from metadata
            int delay = extractFrameDelay(metadata);
            delays.add(delay);

            // Get frame position and disposal info
            int[] frameInfo = extractFrameInfo(metadata);
            int frameX = frameInfo[0];
            int frameY = frameInfo[1];
            String disposal = getDisposalMethod(metadata);

            // Initialize master image on first frame
            if (masterImage == null) {
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                masterImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }

            // Handle disposal methods
            BufferedImage currentFrame;
            if ("restoreToPrevious".equals(disposal)) {
                // Save current state before drawing
                currentFrame = copyImage(masterImage);
            } else {
                currentFrame = masterImage;
            }

            // Draw frame onto master at correct position
            Graphics2D g = masterImage.createGraphics();
            if ("restoreToBackgroundColor".equals(disposal) && i > 0) {
                // Clear previous frame area (for transparent backgrounds)
                g.setComposite(AlphaComposite.Clear);
                g.fillRect(0, 0, masterImage.getWidth(), masterImage.getHeight());
                g.setComposite(AlphaComposite.SrcOver);
            }
            g.drawImage(frame, frameX, frameY, null);
            g.dispose();

            // Store a copy of the composited frame
            frames.add(copyImage(masterImage));

            // Restore if needed
            if ("restoreToPrevious".equals(disposal)) {
                masterImage = currentFrame;
            } else if ("restoreToBackgroundColor".equals(disposal)) {
                // Clear for next frame
                Graphics2D g2 = masterImage.createGraphics();
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(frameX, frameY, frame.getWidth(), frame.getHeight());
                g2.dispose();
            }
        }

        reader.dispose();
        stream.close();

        // Create AnimatedTexture with frames and delays
        AnimatedTexture animTex = new AnimatedTexture(frames, delays);

        // Also create ImageIcon for legacy support
        ImageIcon icon = new ImageIcon(path);

        System.out.println("Loaded GIF: " + path + " (" + animTex.getWidth() + "x" +
                          animTex.getHeight() + ", " + frames.size() + " frames)");

        return new ImageAsset(frames.get(0), icon, animTex);
    }

    /**
     * Extracts the frame delay from GIF metadata.
     * Returns delay in milliseconds.
     */
    private static int extractFrameDelay(IIOMetadata metadata) {
        try {
            String metaFormatName = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

            IIOMetadataNode graphicsControlNode = getNode(root, "GraphicControlExtension");
            if (graphicsControlNode != null) {
                String delayStr = graphicsControlNode.getAttribute("delayTime");
                if (delayStr != null && !delayStr.isEmpty()) {
                    // GIF delay is in centiseconds (1/100 of a second)
                    int delay = Integer.parseInt(delayStr) * 10;
                    // Minimum delay of 20ms to prevent too-fast animations
                    return Math.max(20, delay);
                }
            }
        } catch (Exception e) {
            // Ignore metadata parsing errors
        }
        return AnimatedTexture.DEFAULT_FRAME_DELAY;
    }

    /**
     * Extracts frame position info from GIF metadata.
     * Returns [x, y] offset for the frame.
     */
    private static int[] extractFrameInfo(IIOMetadata metadata) {
        int x = 0, y = 0;
        try {
            String metaFormatName = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

            IIOMetadataNode imgDescNode = getNode(root, "ImageDescriptor");
            if (imgDescNode != null) {
                String xStr = imgDescNode.getAttribute("imageLeftPosition");
                String yStr = imgDescNode.getAttribute("imageTopPosition");
                if (xStr != null && !xStr.isEmpty()) x = Integer.parseInt(xStr);
                if (yStr != null && !yStr.isEmpty()) y = Integer.parseInt(yStr);
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return new int[]{x, y};
    }

    /**
     * Gets the disposal method from GIF metadata.
     */
    private static String getDisposalMethod(IIOMetadata metadata) {
        try {
            String metaFormatName = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

            IIOMetadataNode gce = getNode(root, "GraphicControlExtension");
            if (gce != null) {
                return gce.getAttribute("disposalMethod");
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return "none";
    }

    /**
     * Finds a child node by name in the metadata tree.
     */
    private static IIOMetadataNode getNode(IIOMetadataNode root, String nodeName) {
        int nNodes = root.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (root.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) root.item(i);
            }
        }
        // Search children recursively
        for (int i = 0; i < nNodes; i++) {
            if (root.item(i) instanceof IIOMetadataNode) {
                IIOMetadataNode found = getNode((IIOMetadataNode) root.item(i), nodeName);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Creates a deep copy of a BufferedImage.
     */
    private static BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(
            source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copy.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }
}