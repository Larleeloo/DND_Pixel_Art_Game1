import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Generates burning and frozen effect sprites for mobs.
 * Creates animated GIFs with fire/ice overlay effects based on existing idle sprites.
 *
 * Usage: java StatusEffectSpriteGenerator <mob_folder>
 * Example: java StatusEffectSpriteGenerator assets/mobs/zombie
 */
public class StatusEffectSpriteGenerator {

    public static void main(String[] args) {
        // List of demo level mobs to process
        String[] mobs = {"zombie", "skeleton", "orc", "goblin", "bandit", "knight", "mage", "wolf", "bear"};
        String basePath = "assets/mobs/";

        for (String mob : mobs) {
            String mobPath = basePath + mob;
            try {
                generateEffectSprites(mobPath, mob);
                System.out.println("Generated effect sprites for: " + mob);
            } catch (Exception e) {
                System.err.println("Failed to generate sprites for " + mob + ": " + e.getMessage());
            }
        }
    }

    private static void generateEffectSprites(String mobPath, String mobName) throws IOException {
        File idleFile = new File(mobPath + "/idle.gif");
        if (!idleFile.exists()) {
            System.out.println("No idle.gif found for " + mobName + ", creating placeholder");
            createPlaceholderEffectSprites(mobPath, mobName);
            return;
        }

        // Read frames from idle.gif
        List<BufferedImage> frames = readGifFrames(idleFile);
        List<Integer> delays = readGifDelays(idleFile);

        if (frames.isEmpty()) {
            createPlaceholderEffectSprites(mobPath, mobName);
            return;
        }

        // Generate burning.gif
        List<BufferedImage> burningFrames = new ArrayList<>();
        for (BufferedImage frame : frames) {
            burningFrames.add(createBurningFrame(frame));
        }
        writeAnimatedGif(burningFrames, delays, new File(mobPath + "/burning.gif"));

        // Generate frozen.gif
        List<BufferedImage> frozenFrames = new ArrayList<>();
        for (BufferedImage frame : frames) {
            frozenFrames.add(createFrozenFrame(frame));
        }
        writeAnimatedGif(frozenFrames, delays, new File(mobPath + "/frozen.gif"));
    }

    private static List<BufferedImage> readGifFrames(File file) throws IOException {
        List<BufferedImage> frames = new ArrayList<>();
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(ImageIO.createImageInputStream(file));

        int numFrames = reader.getNumImages(true);
        int width = 0, height = 0;
        BufferedImage composite = null;

        for (int i = 0; i < numFrames; i++) {
            BufferedImage frame = reader.read(i);
            if (composite == null) {
                width = frame.getWidth();
                height = frame.getHeight();
                composite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }

            // Draw frame onto composite
            Graphics2D g = composite.createGraphics();
            g.drawImage(frame, 0, 0, null);
            g.dispose();

            // Copy composite for this frame
            BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = copy.createGraphics();
            g2.drawImage(composite, 0, 0, null);
            g2.dispose();
            frames.add(copy);
        }

        reader.dispose();
        return frames;
    }

    private static List<Integer> readGifDelays(File file) throws IOException {
        List<Integer> delays = new ArrayList<>();
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        reader.setInput(ImageIO.createImageInputStream(file));

        int numFrames = reader.getNumImages(true);
        for (int i = 0; i < numFrames; i++) {
            IIOMetadata metadata = reader.getImageMetadata(i);
            String[] names = metadata.getMetadataFormatNames();
            for (String name : names) {
                if (name.equals("javax_imageio_gif_image_1.0")) {
                    javax.imageio.metadata.IIOMetadataNode root =
                        (javax.imageio.metadata.IIOMetadataNode) metadata.getAsTree(name);
                    javax.imageio.metadata.IIOMetadataNode gce = getNode(root, "GraphicControlExtension");
                    if (gce != null) {
                        int delay = Integer.parseInt(gce.getAttribute("delayTime")) * 10;
                        delays.add(Math.max(delay, 50));  // Minimum 50ms
                    } else {
                        delays.add(100);
                    }
                }
            }
        }

        reader.dispose();
        if (delays.isEmpty()) {
            delays.add(100);
        }
        return delays;
    }

    private static javax.imageio.metadata.IIOMetadataNode getNode(
            javax.imageio.metadata.IIOMetadataNode rootNode, String nodeName) {
        for (int i = 0; i < rootNode.getLength(); i++) {
            if (rootNode.item(i).getNodeName().equals(nodeName)) {
                return (javax.imageio.metadata.IIOMetadataNode) rootNode.item(i);
            }
        }
        // Check children
        for (int i = 0; i < rootNode.getLength(); i++) {
            if (rootNode.item(i) instanceof javax.imageio.metadata.IIOMetadataNode) {
                javax.imageio.metadata.IIOMetadataNode result = getNode(
                    (javax.imageio.metadata.IIOMetadataNode) rootNode.item(i), nodeName);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static BufferedImage createBurningFrame(BufferedImage source) {
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();

        // Draw original with red/orange tint
        g.drawImage(source, 0, 0, null);

        // Apply fire color overlay
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.35f));
        g.setColor(new Color(255, 100, 0));  // Orange-red
        g.fillRect(0, 0, w, h);

        // Add some fire glow at the bottom
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        GradientPaint fireGlow = new GradientPaint(
            0, h * 2/3, new Color(255, 200, 0, 150),
            0, h, new Color(255, 50, 0, 200)
        );
        g.setPaint(fireGlow);
        g.fillRect(0, h * 2/3, w, h/3);

        // Add random fire particles
        Random rand = new Random();
        for (int i = 0; i < 3; i++) {
            int px = rand.nextInt(w);
            int py = h - 5 - rand.nextInt(h/4);
            int size = 2 + rand.nextInt(3);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
            g.setColor(new Color(255, 200 + rand.nextInt(55), 0));
            g.fillOval(px, py, size, size);
        }

        g.dispose();
        return result;
    }

    private static BufferedImage createFrozenFrame(BufferedImage source) {
        int w = source.getWidth();
        int h = source.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();

        // Draw original with blue tint
        g.drawImage(source, 0, 0, null);

        // Apply ice color overlay
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.4f));
        g.setColor(new Color(100, 200, 255));  // Ice blue
        g.fillRect(0, 0, w, h);

        // Add frost border effect
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.setColor(new Color(200, 230, 255));
        g.setStroke(new BasicStroke(2));
        g.drawRect(1, 1, w - 2, h - 2);

        // Add ice crystals
        Random rand = new Random();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        for (int i = 0; i < 4; i++) {
            int cx = rand.nextInt(w);
            int cy = rand.nextInt(h);
            int size = 3 + rand.nextInt(4);
            g.setColor(new Color(220, 240, 255));
            // Draw simple crystal shape
            g.drawLine(cx - size, cy, cx + size, cy);
            g.drawLine(cx, cy - size, cx, cy + size);
            g.drawLine(cx - size/2, cy - size/2, cx + size/2, cy + size/2);
        }

        g.dispose();
        return result;
    }

    private static void createPlaceholderEffectSprites(String mobPath, String mobName) throws IOException {
        // Create simple 32x64 placeholder frames
        int w = 32, h = 64;

        // Burning placeholder
        List<BufferedImage> burningFrames = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            BufferedImage frame = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();
            g.setColor(new Color(255, 100, 0, 180));
            g.fillRoundRect(w/4, h/4, w/2, h*3/4 - h/4, 8, 8);
            g.setColor(new Color(255, 200, 0, 150));
            int flicker = (i % 2) * 3;
            g.fillOval(w/3 - flicker, h/3 - flicker, w/3 + flicker*2, h/3 + flicker*2);
            g.dispose();
            burningFrames.add(frame);
        }
        List<Integer> delays = Arrays.asList(100, 100, 100, 100);
        writeAnimatedGif(burningFrames, delays, new File(mobPath + "/burning.gif"));

        // Frozen placeholder
        List<BufferedImage> frozenFrames = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            BufferedImage frame = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();
            g.setColor(new Color(100, 200, 255, 180));
            g.fillRoundRect(w/4, h/4, w/2, h*3/4 - h/4, 8, 8);
            g.setColor(new Color(200, 230, 255, 200));
            g.drawRect(w/4, h/4, w/2, h*3/4 - h/4);
            g.dispose();
            frozenFrames.add(frame);
        }
        delays = Arrays.asList(200, 200);
        writeAnimatedGif(frozenFrames, delays, new File(mobPath + "/frozen.gif"));
    }

    private static void writeAnimatedGif(List<BufferedImage> frames, List<Integer> delays, File output) throws IOException {
        ImageOutputStream ios = ImageIO.createImageOutputStream(output);
        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        writer.setOutput(ios);
        writer.prepareWriteSequence(null);

        for (int i = 0; i < frames.size(); i++) {
            BufferedImage frame = frames.get(i);
            int delay = (i < delays.size()) ? delays.get(i) : 100;

            ImageWriteParam param = writer.getDefaultWriteParam();
            IIOMetadata metadata = writer.getDefaultImageMetadata(
                new ImageTypeSpecifier(frame), param);

            configureGifMetadata(metadata, delay);
            writer.writeToSequence(new IIOImage(frame, null, metadata), param);
        }

        writer.endWriteSequence();
        ios.close();
    }

    private static void configureGifMetadata(IIOMetadata metadata, int delayMs) {
        try {
            String formatName = "javax_imageio_gif_image_1.0";
            javax.imageio.metadata.IIOMetadataNode root =
                (javax.imageio.metadata.IIOMetadataNode) metadata.getAsTree(formatName);

            // Configure graphic control extension
            javax.imageio.metadata.IIOMetadataNode gce =
                new javax.imageio.metadata.IIOMetadataNode("GraphicControlExtension");
            gce.setAttribute("disposalMethod", "restoreToBackgroundColor");
            gce.setAttribute("userInputFlag", "FALSE");
            gce.setAttribute("transparentColorFlag", "TRUE");
            gce.setAttribute("transparentColorIndex", "0");
            gce.setAttribute("delayTime", String.valueOf(delayMs / 10));

            // Add application extension for looping
            javax.imageio.metadata.IIOMetadataNode appExtensions =
                new javax.imageio.metadata.IIOMetadataNode("ApplicationExtensions");
            javax.imageio.metadata.IIOMetadataNode appExt =
                new javax.imageio.metadata.IIOMetadataNode("ApplicationExtension");
            appExt.setAttribute("applicationID", "NETSCAPE");
            appExt.setAttribute("authenticationCode", "2.0");
            appExt.setUserObject(new byte[]{1, 0, 0});
            appExtensions.appendChild(appExt);

            root.appendChild(gce);
            root.appendChild(appExtensions);

            metadata.setFromTree(formatName, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
