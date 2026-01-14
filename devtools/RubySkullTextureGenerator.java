package devtools;

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

/**
 * Generates placeholder GIF textures for The Ruby Skull item.
 * Creates idle.gif, use.gif, and break.gif animations.
 *
 * Run: javac devtools/RubySkullTextureGenerator.java && java -cp . devtools.RubySkullTextureGenerator
 */
public class RubySkullTextureGenerator {

    private static final int SIZE = 32;  // 32x32 pixel art
    private static final String OUTPUT_DIR = "assets/items/ruby_skull/";

    // Ruby/crimson color palette
    private static final Color RUBY_DARK = new Color(139, 0, 0);       // Dark red
    private static final Color RUBY_MID = new Color(178, 34, 34);      // Firebrick
    private static final Color RUBY_LIGHT = new Color(220, 60, 60);    // Light ruby
    private static final Color RUBY_HIGHLIGHT = new Color(255, 100, 100); // Bright highlight
    private static final Color RUBY_GLOW = new Color(255, 50, 50, 150);   // Glowing effect
    private static final Color EYE_GLOW = new Color(255, 200, 50);     // Golden eye glow
    private static final Color SHADOW = new Color(80, 0, 0);           // Deep shadow

    public static void main(String[] args) {
        System.out.println("Generating Ruby Skull textures...");

        // Create output directory
        new File(OUTPUT_DIR).mkdirs();

        try {
            // Generate idle animation (pulsing glow effect)
            generateIdleAnimation();
            System.out.println("Created: " + OUTPUT_DIR + "idle.gif");

            // Generate use animation (glowing intensely)
            generateUseAnimation();
            System.out.println("Created: " + OUTPUT_DIR + "use.gif");

            // Generate break animation (cracking/shattering)
            generateBreakAnimation();
            System.out.println("Created: " + OUTPUT_DIR + "break.gif");

            System.out.println("Done! Ruby Skull textures generated in " + OUTPUT_DIR);

        } catch (Exception e) {
            System.err.println("Error generating textures: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Draw the base skull shape
     */
    private static void drawSkull(Graphics2D g, int offsetX, int offsetY, float glowIntensity) {
        // Skull cranium (top dome)
        g.setColor(RUBY_DARK);
        g.fillOval(offsetX + 6, offsetY + 4, 20, 18);

        // Mid tone layer
        g.setColor(RUBY_MID);
        g.fillOval(offsetX + 8, offsetY + 5, 16, 14);

        // Highlight on top
        g.setColor(RUBY_LIGHT);
        g.fillOval(offsetX + 10, offsetY + 6, 10, 8);

        // Bright highlight spot
        g.setColor(RUBY_HIGHLIGHT);
        g.fillOval(offsetX + 11, offsetY + 7, 4, 3);

        // Jaw/lower face
        g.setColor(RUBY_DARK);
        g.fillRoundRect(offsetX + 9, offsetY + 16, 14, 10, 4, 4);

        g.setColor(RUBY_MID);
        g.fillRoundRect(offsetX + 10, offsetY + 17, 12, 8, 3, 3);

        // Eye sockets (dark)
        g.setColor(SHADOW);
        g.fillOval(offsetX + 9, offsetY + 10, 5, 6);
        g.fillOval(offsetX + 18, offsetY + 10, 5, 6);

        // Glowing eyes
        Color eyeColor = new Color(
            EYE_GLOW.getRed(),
            EYE_GLOW.getGreen(),
            EYE_GLOW.getBlue(),
            (int)(200 * glowIntensity)
        );
        g.setColor(eyeColor);
        g.fillOval(offsetX + 10, offsetY + 11, 3, 4);
        g.fillOval(offsetX + 19, offsetY + 11, 3, 4);

        // Nose hole (triangle)
        g.setColor(SHADOW);
        int[] noseX = {offsetX + 15, offsetX + 17, offsetX + 16};
        int[] noseY = {offsetY + 16, offsetY + 16, offsetY + 19};
        g.fillPolygon(noseX, noseY, 3);

        // Teeth
        g.setColor(RUBY_LIGHT);
        for (int i = 0; i < 4; i++) {
            g.fillRect(offsetX + 11 + i * 3, offsetY + 22, 2, 3);
        }

        // Outer glow effect
        if (glowIntensity > 0.3f) {
            Composite oldComp = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowIntensity * 0.3f));
            g.setColor(RUBY_GLOW);
            g.fillOval(offsetX + 2, offsetY + 0, 28, 28);
            g.setComposite(oldComp);
        }
    }

    /**
     * Generate idle animation - subtle pulsing glow
     */
    private static void generateIdleAnimation() throws IOException {
        int frames = 8;
        BufferedImage[] images = new BufferedImage[frames];

        for (int i = 0; i < frames; i++) {
            images[i] = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = images[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            // Pulsing glow intensity
            float glow = 0.5f + 0.5f * (float)Math.sin(i * Math.PI * 2 / frames);
            drawSkull(g, 0, 0, glow);

            g.dispose();
        }

        writeAnimatedGif(images, OUTPUT_DIR + "idle.gif", 150);
    }

    /**
     * Generate use animation - intense glowing
     */
    private static void generateUseAnimation() throws IOException {
        int frames = 6;
        BufferedImage[] images = new BufferedImage[frames];

        for (int i = 0; i < frames; i++) {
            images[i] = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = images[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            // Rapid intense pulsing
            float glow = 0.7f + 0.3f * (float)Math.sin(i * Math.PI * 2 / 3);
            drawSkull(g, 0, 0, glow);

            // Extra bright flash on use
            if (i == 0 || i == 3) {
                Composite oldComp = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                g.setColor(Color.WHITE);
                g.fillOval(4, 2, 24, 24);
                g.setComposite(oldComp);
            }

            g.dispose();
        }

        writeAnimatedGif(images, OUTPUT_DIR + "use.gif", 80);
    }

    /**
     * Generate break animation - cracking and fading
     */
    private static void generateBreakAnimation() throws IOException {
        int frames = 6;
        BufferedImage[] images = new BufferedImage[frames];

        for (int i = 0; i < frames; i++) {
            images[i] = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = images[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            // Fading glow as it breaks
            float glow = 1.0f - (i * 0.15f);
            float alpha = 1.0f - (i * 0.16f);

            Composite oldComp = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.1f, alpha)));

            drawSkull(g, 0, 0, Math.max(0.1f, glow));

            g.setComposite(oldComp);

            // Draw cracks
            g.setColor(SHADOW);
            g.setStroke(new BasicStroke(1));
            if (i >= 1) {
                g.drawLine(16, 8, 14, 16);
            }
            if (i >= 2) {
                g.drawLine(18, 6, 22, 14);
                g.drawLine(10, 12, 8, 18);
            }
            if (i >= 3) {
                g.drawLine(12, 4, 16, 12);
                g.drawLine(20, 10, 24, 16);
            }
            if (i >= 4) {
                // Fragments flying off
                g.setColor(RUBY_MID);
                g.fillRect(4 - i, 6 - i, 3, 3);
                g.fillRect(26 + i, 8 - i, 3, 3);
                g.fillRect(8 - i, 22 + i, 2, 2);
                g.fillRect(22 + i, 20 + i, 2, 2);
            }

            g.dispose();
        }

        writeAnimatedGif(images, OUTPUT_DIR + "break.gif", 100);
    }

    /**
     * Write frames as an animated GIF
     */
    private static void writeAnimatedGif(BufferedImage[] frames, String outputPath, int delayMs) throws IOException {
        ImageOutputStream output = new FileImageOutputStream(new File(outputPath));

        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        ImageWriteParam params = writer.getDefaultWriteParam();

        ImageTypeSpecifier imageType = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
        IIOMetadata metadata = writer.getDefaultImageMetadata(imageType, params);

        configureGifMetadata(metadata, delayMs, true);

        writer.setOutput(output);
        writer.prepareWriteSequence(null);

        for (BufferedImage frame : frames) {
            // Convert to indexed color for GIF compatibility
            BufferedImage indexedFrame = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = indexedFrame.createGraphics();
            g.drawImage(frame, 0, 0, null);
            g.dispose();

            IIOMetadata frameMetadata = writer.getDefaultImageMetadata(imageType, params);
            configureGifMetadata(frameMetadata, delayMs, false);

            writer.writeToSequence(new IIOImage(indexedFrame, null, frameMetadata), params);
        }

        writer.endWriteSequence();
        output.close();
    }

    /**
     * Configure GIF metadata for animation
     */
    private static void configureGifMetadata(IIOMetadata metadata, int delayMs, boolean isFirst) {
        String metaFormatName = metadata.getNativeMetadataFormatName();

        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("disposalMethod", "restoreToBackgroundColor");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "TRUE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delayMs / 10));
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        if (isFirst) {
            IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
            IIOMetadataNode appExtensionNode = new IIOMetadataNode("ApplicationExtension");
            appExtensionNode.setAttribute("applicationID", "NETSCAPE");
            appExtensionNode.setAttribute("authenticationCode", "2.0");
            appExtensionNode.setUserObject(new byte[]{0x1, 0x0, 0x0}); // Loop forever
            appExtensionsNode.appendChild(appExtensionNode);
        }

        try {
            metadata.setFromTree(metaFormatName, root);
        } catch (IIOInvalidTreeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get or create a metadata node
     */
    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }
}
