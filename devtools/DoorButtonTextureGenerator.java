import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

/**
 * Generates placeholder GIF textures for doors and buttons.
 * These are simple animated textures that can be replaced with custom art.
 *
 * Usage: java DoorButtonTextureGenerator
 *
 * Generates:
 * - assets/doors/wooden_door.gif (10 frames, opening animation)
 * - assets/doors/iron_door.gif (10 frames, opening animation)
 * - assets/doors/stone_door.gif (10 frames, opening animation)
 * - assets/buttons/stone_button.gif (5 frames, press animation)
 * - assets/buttons/wooden_button.gif (5 frames, press animation)
 * - assets/buttons/pressure_plate.gif (5 frames, press animation)
 */
public class DoorButtonTextureGenerator {

    private static final String OUTPUT_DIR = "assets";

    public static void main(String[] args) {
        System.out.println("=== Door and Button Texture Generator ===");

        try {
            // Create output directories
            new File(OUTPUT_DIR + "/doors").mkdirs();
            new File(OUTPUT_DIR + "/buttons").mkdirs();

            // Generate door textures
            generateDoorTexture("wooden_door", new Color(139, 90, 43), new Color(101, 67, 33), new Color(255, 215, 0));
            generateDoorTexture("iron_door", new Color(150, 150, 160), new Color(100, 100, 110), new Color(200, 200, 220));
            generateDoorTexture("stone_door", new Color(120, 120, 120), new Color(80, 80, 80), new Color(160, 160, 160));

            // Generate button textures
            generateButtonTexture("stone_button", new Color(100, 100, 100), new Color(70, 70, 70), 32, 16);
            generateButtonTexture("wooden_button", new Color(139, 90, 43), new Color(101, 67, 33), 32, 16);
            generateButtonTexture("pressure_plate", new Color(80, 80, 80), new Color(50, 50, 50), 48, 12);

            System.out.println("\nAll textures generated successfully!");
            System.out.println("Check assets/doors/ and assets/buttons/ directories.");

        } catch (Exception e) {
            System.err.println("Error generating textures: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates an animated door texture (10 frames showing door opening).
     */
    private static void generateDoorTexture(String name, Color baseColor, Color frameColor, Color handleColor)
            throws IOException {
        int width = 64;
        int height = 128;
        int frameCount = 10;
        int frameDelay = 5;  // 50ms per frame

        System.out.println("Generating door: " + name + " (" + frameCount + " frames)");

        BufferedImage[] frames = new BufferedImage[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Calculate opening progress (0.0 = closed, 1.0 = fully open)
            float openProgress = (float) i / (frameCount - 1);

            // Draw door frame (always visible)
            g.setColor(frameColor);
            g.fillRect(0, 0, 8, height);  // Left frame
            g.fillRect(width - 8, 0, 8, height);  // Right frame
            g.fillRect(0, 0, width, 8);  // Top frame

            // Draw door panel (shrinks as it opens)
            int doorWidth = (int) (width - 16 - (width - 16) * openProgress * 0.7);
            int doorX = 8 + (int) ((width - 16 - doorWidth) * openProgress);

            g.setColor(baseColor);
            g.fillRect(doorX, 8, doorWidth, height - 8);

            // Door panel details
            if (doorWidth > 20) {
                // Vertical panel lines
                g.setColor(frameColor);
                g.fillRect(doorX + doorWidth / 3, 20, 2, height - 40);
                g.fillRect(doorX + 2 * doorWidth / 3, 20, 2, height - 40);

                // Horizontal panel lines
                g.fillRect(doorX + 4, height / 3, doorWidth - 8, 2);
                g.fillRect(doorX + 4, 2 * height / 3, doorWidth - 8, 2);

                // Handle
                int handleX = doorX + doorWidth - 18;
                g.setColor(handleColor);
                g.fillOval(handleX, height / 2 - 6, 12, 12);

                // Keyhole
                g.setColor(Color.BLACK);
                g.fillOval(handleX + 4, height / 2 + 8, 4, 4);
                g.fillRect(handleX + 5, height / 2 + 11, 2, 6);
            }

            // Highlight on frame
            g.setColor(new Color(255, 255, 255, 50));
            g.fillRect(1, 1, 6, height - 2);

            g.dispose();
        }

        // Save as animated GIF
        saveAnimatedGif(frames, frameDelay, OUTPUT_DIR + "/doors/" + name + ".gif");
    }

    /**
     * Generates an animated button texture (5 frames showing button press).
     */
    private static void generateButtonTexture(String name, Color topColor, Color sideColor, int width, int height)
            throws IOException {
        int frameCount = 5;
        int frameDelay = 4;  // 40ms per frame

        System.out.println("Generating button: " + name + " (" + frameCount + " frames)");

        BufferedImage[] frames = new BufferedImage[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Calculate press depth (0 = not pressed, max = fully pressed)
            int pressDepth = (i * 4) / (frameCount - 1);
            int buttonHeight = height - pressDepth - 2;

            // Button shadow/side
            g.setColor(sideColor);
            g.fillRoundRect(2, 2 + pressDepth, width - 4, buttonHeight, 4, 4);

            // Button top
            Color adjustedTop = pressDepth > 0 ? topColor.darker() : topColor;
            g.setColor(adjustedTop);
            g.fillRoundRect(2, pressDepth, width - 4, buttonHeight - 2, 4, 4);

            // Highlight
            if (pressDepth < 2) {
                g.setColor(new Color(255, 255, 255, 80));
                g.fillRoundRect(4, pressDepth + 2, width - 8, (buttonHeight - 4) / 2, 3, 3);
            }

            // Border
            g.setColor(sideColor.darker());
            g.setStroke(new BasicStroke(1));
            g.drawRoundRect(2, pressDepth, width - 4, buttonHeight, 4, 4);

            g.dispose();
        }

        // Save as animated GIF
        saveAnimatedGif(frames, frameDelay, OUTPUT_DIR + "/buttons/" + name + ".gif");
    }

    /**
     * Saves an array of BufferedImages as an animated GIF.
     */
    private static void saveAnimatedGif(BufferedImage[] frames, int delayCs, String outputPath)
            throws IOException {

        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(new File(outputPath));
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();
        IIOMetadata metadata = writer.getDefaultImageMetadata(
                ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB), param);

        configureGifMetadata(metadata, delayCs, true);

        writer.prepareWriteSequence(null);

        for (int i = 0; i < frames.length; i++) {
            // Convert to indexed color for GIF compatibility
            BufferedImage indexedImage = convertToIndexedColor(frames[i]);

            IIOMetadata frameMetadata = writer.getDefaultImageMetadata(
                    ImageTypeSpecifier.createFromBufferedImageType(indexedImage.getType()), param);
            configureGifMetadata(frameMetadata, delayCs, i == 0);

            writer.writeToSequence(new IIOImage(indexedImage, null, frameMetadata), param);
        }

        writer.endWriteSequence();
        ios.close();

        System.out.println("  Saved: " + outputPath);
    }

    /**
     * Configures GIF metadata for animation.
     */
    private static void configureGifMetadata(IIOMetadata metadata, int delayCs, boolean firstFrame)
            throws IIOInvalidTreeException {

        String metaFormat = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);

        // Find or create GraphicControlExtension
        IIOMetadataNode gce = getOrCreateNode(root, "GraphicControlExtension");
        gce.setAttribute("disposalMethod", "none");
        gce.setAttribute("userInputFlag", "FALSE");
        gce.setAttribute("transparentColorFlag", "TRUE");
        gce.setAttribute("delayTime", String.valueOf(delayCs));
        gce.setAttribute("transparentColorIndex", "0");

        // Application extension for looping
        if (firstFrame) {
            IIOMetadataNode appExts = getOrCreateNode(root, "ApplicationExtensions");
            IIOMetadataNode appExt = new IIOMetadataNode("ApplicationExtension");
            appExt.setAttribute("applicationID", "NETSCAPE");
            appExt.setAttribute("authenticationCode", "2.0");

            // Loop forever (0 = infinite loop)
            byte[] loopData = new byte[]{1, 0, 0};
            appExt.setUserObject(loopData);
            appExts.appendChild(appExt);
        }

        metadata.setFromTree(metaFormat, root);
    }

    /**
     * Gets or creates a child node in the metadata tree.
     */
    private static IIOMetadataNode getOrCreateNode(IIOMetadataNode root, String nodeName) {
        for (int i = 0; i < root.getLength(); i++) {
            if (root.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) root.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        root.appendChild(node);
        return node;
    }

    /**
     * Converts a BufferedImage to indexed color format for GIF compatibility.
     */
    private static BufferedImage convertToIndexedColor(BufferedImage src) {
        BufferedImage indexed = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
        Graphics2D g = indexed.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return indexed;
    }
}
