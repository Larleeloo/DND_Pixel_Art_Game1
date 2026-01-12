import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

/**
 * Generator tool for creating animated GIF sprites for projectiles.
 * Creates fireball and fish projectile sprites for the Mirror to Other Realms item.
 *
 * Output:
 *   assets/projectiles/fireball.gif - Animated fireball sprite
 *   assets/projectiles/fish.gif - Animated tiny fish sprite
 *
 * Run from project root:
 *   javac tools/ProjectileSpriteGenerator.java
 *   java -cp tools ProjectileSpriteGenerator
 */
public class ProjectileSpriteGenerator {

    private static final int FIREBALL_SIZE = 16;
    private static final int FISH_WIDTH = 14;
    private static final int FISH_HEIGHT = 8;
    private static final int FRAME_COUNT = 4;
    private static final int FRAME_DELAY_MS = 100;

    public static void main(String[] args) throws Exception {
        // Create output directory
        File outputDir = new File("assets/projectiles");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
            System.out.println("Created directory: " + outputDir.getAbsolutePath());
        }

        // Generate sprites
        generateFireballGif(new File(outputDir, "fireball.gif"));
        generateFishGif(new File(outputDir, "fish.gif"));

        System.out.println("Projectile sprites generated successfully!");
    }

    /**
     * Generates an animated fireball GIF with flickering flame effect.
     */
    private static void generateFireballGif(File outputFile) throws Exception {
        BufferedImage[] frames = new BufferedImage[FRAME_COUNT];

        for (int i = 0; i < FRAME_COUNT; i++) {
            frames[i] = createFireballFrame(FIREBALL_SIZE, FIREBALL_SIZE, i);
        }

        writeAnimatedGif(frames, outputFile, FRAME_DELAY_MS);
        System.out.println("Generated: " + outputFile.getPath());
    }

    /**
     * Creates a single frame of the fireball animation.
     */
    private static BufferedImage createFireballFrame(int w, int h, int frameIndex) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Frame-based variation for animation
        double phase = frameIndex * Math.PI / 2;
        int variation = (int)(Math.sin(phase) * 2);

        // Outer red glow (pulsing)
        int glowAlpha = 120 + (int)(Math.sin(phase) * 30);
        g.setColor(new Color(255, 50, 0, glowAlpha));
        g.fillOval(variation, variation, w - variation * 2, h - variation * 2);

        // Orange middle layer
        int orangeOffset = 2 + variation / 2;
        g.setColor(new Color(255, 150, 0));
        g.fillOval(orangeOffset, orangeOffset, w - orangeOffset * 2, h - orangeOffset * 2);

        // Yellow core
        int yellowOffset = 4 + variation / 2;
        g.setColor(new Color(255, 255, 100));
        g.fillOval(yellowOffset, yellowOffset, w - yellowOffset * 2, h - yellowOffset * 2);

        // White hot center
        int whiteOffset = 6;
        g.setColor(new Color(255, 255, 255));
        g.fillOval(whiteOffset, whiteOffset, w - whiteOffset * 2, h - whiteOffset * 2);

        // Flame wisps (vary by frame)
        g.setColor(new Color(255, 200, 0, 150));
        int wispOffset = (frameIndex % 2) * 2;
        g.fillOval(1 + wispOffset, 2, 4, 3);
        g.fillOval(w - 5 - wispOffset, 3, 4, 3);

        g.dispose();
        return img;
    }

    /**
     * Generates an animated fish GIF with swimming motion.
     */
    private static void generateFishGif(File outputFile) throws Exception {
        BufferedImage[] frames = new BufferedImage[FRAME_COUNT];

        for (int i = 0; i < FRAME_COUNT; i++) {
            frames[i] = createFishFrame(FISH_WIDTH, FISH_HEIGHT, i);
        }

        writeAnimatedGif(frames, outputFile, FRAME_DELAY_MS);
        System.out.println("Generated: " + outputFile.getPath());
    }

    /**
     * Creates a single frame of the fish animation.
     */
    private static BufferedImage createFishFrame(int w, int h, int frameIndex) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Tail wiggle animation
        double wiggle = Math.sin(frameIndex * Math.PI / 2) * 1;

        // Fish body (oval shape) - teal/cyan color
        g.setColor(new Color(50, 180, 200));
        g.fillOval(3, 1, w - 6, h - 2);

        // Fish belly (lighter)
        g.setColor(new Color(150, 220, 240));
        g.fillOval(4, h / 2, w - 8, h / 2 - 1);

        // Tail fin (triangle at back) - with wiggle
        g.setColor(new Color(30, 140, 170));
        int tailTipY = h / 2 + (int) wiggle;
        int[] tailX = {0, 4, 4};
        int[] tailY = {tailTipY, 1, h - 1};
        g.fillPolygon(tailX, tailY, 3);

        // Dorsal fin (small triangle on top)
        g.setColor(new Color(30, 140, 170));
        int[] dorsalX = {w / 2 - 1, w / 2 + 2, w / 2 + 1};
        int[] dorsalY = {0, 1, 3};
        g.fillPolygon(dorsalX, dorsalY, 3);

        // Pectoral fin (side fin) - animates slightly
        int finOffset = (frameIndex % 2);
        g.setColor(new Color(40, 150, 180, 180));
        g.fillOval(w / 2 - 2, h / 2 + finOffset, 4, 2);

        // Eye
        g.setColor(Color.WHITE);
        g.fillOval(w - 6, h / 2 - 2, 3, 3);
        g.setColor(Color.BLACK);
        g.fillOval(w - 5, h / 2 - 1, 2, 2);

        // Mouth (opens slightly on some frames)
        g.setColor(new Color(30, 100, 130));
        if (frameIndex % 2 == 0) {
            g.drawLine(w - 2, h / 2, w - 1, h / 2);
        } else {
            g.fillOval(w - 2, h / 2 - 1, 2, 2);
        }

        // Scales shimmer (subtle detail)
        g.setColor(new Color(100, 200, 220, 80 + frameIndex * 20));
        g.drawArc(5, 2, 4, 3, 0, 180);
        g.drawArc(8, 2, 4, 3, 0, 180);

        // Water bubble effect (appears on some frames)
        if (frameIndex == 1 || frameIndex == 3) {
            g.setColor(new Color(200, 240, 255, 150));
            g.fillOval(w - 3, 1, 2, 2);
        }

        g.dispose();
        return img;
    }

    /**
     * Writes an array of BufferedImages as an animated GIF.
     */
    private static void writeAnimatedGif(BufferedImage[] frames, File outputFile, int delayMs)
            throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();
        IIOMetadata metadata = writer.getDefaultImageMetadata(
                new ImageTypeSpecifier(frames[0]), param);

        configureGifMetadata(metadata, delayMs, true);

        writer.prepareWriteSequence(null);

        for (BufferedImage frame : frames) {
            // Convert to indexed color for GIF
            BufferedImage indexedFrame = convertToIndexed(frame);
            IIOMetadata frameMetadata = writer.getDefaultImageMetadata(
                    new ImageTypeSpecifier(indexedFrame), param);
            configureGifMetadata(frameMetadata, delayMs, false);

            writer.writeToSequence(new IIOImage(indexedFrame, null, frameMetadata), param);
        }

        writer.endWriteSequence();
        ios.close();
        writer.dispose();
    }

    /**
     * Configures GIF metadata for animation.
     */
    private static void configureGifMetadata(IIOMetadata metadata, int delayMs, boolean isFirst)
            throws Exception {
        String metaFormatName = metadata.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

        // Graphics control extension for timing
        IIOMetadataNode gce = getOrCreateNode(root, "GraphicControlExtension");
        gce.setAttribute("disposalMethod", "restoreToBackgroundColor");
        gce.setAttribute("userInputFlag", "FALSE");
        gce.setAttribute("transparentColorFlag", "TRUE");
        gce.setAttribute("transparentColorIndex", "0");
        gce.setAttribute("delayTime", String.valueOf(delayMs / 10)); // In 1/100ths of second

        // Application extension for looping (only on first frame)
        if (isFirst) {
            IIOMetadataNode appExtensions = getOrCreateNode(root, "ApplicationExtensions");
            IIOMetadataNode appExt = new IIOMetadataNode("ApplicationExtension");
            appExt.setAttribute("applicationID", "NETSCAPE");
            appExt.setAttribute("authenticationCode", "2.0");

            // Loop forever
            byte[] loopData = new byte[]{0x01, 0x00, 0x00};
            appExt.setUserObject(loopData);
            appExtensions.appendChild(appExt);
        }

        metadata.setFromTree(metaFormatName, root);
    }

    /**
     * Gets or creates a child node with the given name.
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
     * Converts a BufferedImage to indexed color model for GIF.
     */
    private static BufferedImage convertToIndexed(BufferedImage src) {
        BufferedImage indexed = new BufferedImage(
                src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
        Graphics2D g = indexed.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return indexed;
    }
}
