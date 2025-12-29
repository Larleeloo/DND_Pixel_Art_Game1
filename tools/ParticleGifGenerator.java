import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Generates particle effect GIFs for status effects.
 * Creates animated GIFs with fire/ice/poison particle overlays.
 *
 * Usage: java ParticleGifGenerator
 * Output: assets/particles/fire_particles.gif, ice_particles.gif, poison_particles.gif
 */
public class ParticleGifGenerator {

    // Particle size for overlay GIFs (will be scaled when rendered)
    private static final int WIDTH = 64;
    private static final int HEIGHT = 96;
    private static final int FRAME_COUNT = 8;
    private static final int FRAME_DELAY = 100; // milliseconds

    public static void main(String[] args) {
        String outputDir = "assets/particles/";

        try {
            // Create output directory if needed
            new File(outputDir).mkdirs();

            // Generate fire particles
            generateFireParticles(outputDir + "fire_particles.gif");
            System.out.println("Generated: fire_particles.gif");

            // Generate ice particles
            generateIceParticles(outputDir + "ice_particles.gif");
            System.out.println("Generated: ice_particles.gif");

            // Generate poison particles
            generatePoisonParticles(outputDir + "poison_particles.gif");
            System.out.println("Generated: poison_particles.gif");

            System.out.println("All particle GIFs generated successfully!");

        } catch (Exception e) {
            System.err.println("Error generating particles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates fire particle animation - rising flames and sparks
     */
    private static void generateFireParticles(String outputPath) throws IOException {
        List<BufferedImage> frames = new ArrayList<>();
        Random rand = new Random(42); // Fixed seed for consistency

        for (int f = 0; f < FRAME_COUNT; f++) {
            BufferedImage frame = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Clear background
            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setComposite(AlphaComposite.SrcOver);

            // Generate fire particles rising from bottom
            int particleCount = 12;
            for (int i = 0; i < particleCount; i++) {
                // Particle properties
                int baseX = rand.nextInt(WIDTH - 8) + 4;
                int baseY = HEIGHT - 10 - rand.nextInt(HEIGHT / 2);

                // Animate upward movement
                int yOffset = (f * 4 + i * 3) % 40;
                int actualY = baseY - yOffset;

                // Wobble horizontally
                int xWobble = (int)(Math.sin((f + i) * 0.8) * 3);
                int actualX = baseX + xWobble;

                // Size decreases as particle rises
                int size = Math.max(2, 6 - yOffset / 8);

                // Alpha fades as particle rises
                float alpha = Math.max(0.2f, 1.0f - yOffset / 50.0f);

                // Color varies from yellow to red
                Color color;
                if (yOffset < 15) {
                    color = new Color(255, 200 + rand.nextInt(55), 0, (int)(alpha * 255));
                } else if (yOffset < 30) {
                    color = new Color(255, 100 + rand.nextInt(100), 0, (int)(alpha * 255));
                } else {
                    color = new Color(200, 50 + rand.nextInt(50), 0, (int)(alpha * 200));
                }

                g.setColor(color);
                g.fillOval(actualX, actualY, size, size);

                // Add glow around larger particles
                if (size > 3) {
                    g.setColor(new Color(255, 200, 0, (int)(alpha * 80)));
                    g.fillOval(actualX - 2, actualY - 2, size + 4, size + 4);
                }
            }

            // Add some sparks
            for (int i = 0; i < 5; i++) {
                int sparkX = rand.nextInt(WIDTH);
                int sparkY = HEIGHT - 20 - rand.nextInt(HEIGHT - 30);
                int sparkOffset = (f + i * 2) % 8;

                g.setColor(new Color(255, 255, 200, 150 - sparkOffset * 15));
                g.fillRect(sparkX, sparkY - sparkOffset * 2, 2, 2);
            }

            g.dispose();
            frames.add(frame);
        }

        writeAnimatedGif(frames, FRAME_DELAY, new File(outputPath));
    }

    /**
     * Generates ice particle animation - falling snowflakes and crystals
     */
    private static void generateIceParticles(String outputPath) throws IOException {
        List<BufferedImage> frames = new ArrayList<>();
        Random rand = new Random(42);

        for (int f = 0; f < FRAME_COUNT; f++) {
            BufferedImage frame = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setComposite(AlphaComposite.SrcOver);

            // Generate ice crystals and snowflakes
            int particleCount = 10;
            for (int i = 0; i < particleCount; i++) {
                int baseX = rand.nextInt(WIDTH - 6) + 3;
                int baseY = 10 + rand.nextInt(HEIGHT - 20);

                // Gentle downward drift
                int yOffset = (f * 2 + i * 5) % 30;
                int actualY = baseY + yOffset;

                // Slight horizontal sway
                int xSway = (int)(Math.sin((f + i) * 0.5) * 2);
                int actualX = baseX + xSway;

                int size = 4 + rand.nextInt(4);
                float alpha = 0.6f + rand.nextFloat() * 0.4f;

                // Draw crystal shape (star/snowflake)
                g.setColor(new Color(200, 230, 255, (int)(alpha * 255)));
                drawCrystal(g, actualX, actualY, size);

                // Add sparkle
                if ((f + i) % 3 == 0) {
                    g.setColor(new Color(255, 255, 255, 200));
                    g.fillRect(actualX + size/2 - 1, actualY + size/2 - 1, 2, 2);
                }
            }

            // Add frost edge effects
            g.setColor(new Color(180, 220, 255, 100));
            g.setStroke(new BasicStroke(2));
            // Frost at top corners
            g.drawLine(0, 0, 8, 0);
            g.drawLine(0, 0, 0, 8);
            g.drawLine(WIDTH - 1, 0, WIDTH - 9, 0);
            g.drawLine(WIDTH - 1, 0, WIDTH - 1, 8);

            g.dispose();
            frames.add(frame);
        }

        writeAnimatedGif(frames, FRAME_DELAY + 50, new File(outputPath)); // Slower for ice
    }

    /**
     * Draws a simple crystal/snowflake shape
     */
    private static void drawCrystal(Graphics2D g, int x, int y, int size) {
        int cx = x + size / 2;
        int cy = y + size / 2;
        int radius = size / 2;

        // Horizontal line
        g.drawLine(cx - radius, cy, cx + radius, cy);
        // Vertical line
        g.drawLine(cx, cy - radius, cx, cy + radius);
        // Diagonals
        int diagRadius = radius * 2 / 3;
        g.drawLine(cx - diagRadius, cy - diagRadius, cx + diagRadius, cy + diagRadius);
        g.drawLine(cx + diagRadius, cy - diagRadius, cx - diagRadius, cy + diagRadius);
    }

    /**
     * Generates poison particle animation - rising bubbles
     */
    private static void generatePoisonParticles(String outputPath) throws IOException {
        List<BufferedImage> frames = new ArrayList<>();
        Random rand = new Random(42);

        for (int f = 0; f < FRAME_COUNT; f++) {
            BufferedImage frame = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = frame.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setComposite(AlphaComposite.Clear);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            g.setComposite(AlphaComposite.SrcOver);

            // Generate poison bubbles rising from bottom
            int particleCount = 8;
            for (int i = 0; i < particleCount; i++) {
                int baseX = rand.nextInt(WIDTH - 10) + 5;
                int baseY = HEIGHT - 15 - rand.nextInt(HEIGHT / 3);

                // Bubbles rise upward
                int yOffset = (f * 3 + i * 8) % 50;
                int actualY = baseY - yOffset;

                // Wobble side to side
                int xWobble = (int)(Math.sin((f + i) * 0.6) * 4);
                int actualX = baseX + xWobble;

                // Size varies
                int size = 4 + rand.nextInt(5);

                // Alpha fades as bubble rises
                float alpha = Math.max(0.3f, 1.0f - yOffset / 60.0f);

                // Poison green colors
                Color outerColor = new Color(50, 180, 50, (int)(alpha * 180));
                Color innerColor = new Color(100, 220, 80, (int)(alpha * 120));

                // Draw bubble
                g.setColor(outerColor);
                g.fillOval(actualX, actualY, size, size);

                // Bubble highlight
                g.setColor(innerColor);
                g.fillOval(actualX + 1, actualY + 1, size / 2, size / 2);

                // Bubble outline
                g.setColor(new Color(30, 100, 30, (int)(alpha * 200)));
                g.drawOval(actualX, actualY, size, size);
            }

            // Add dripping effect at bottom
            for (int i = 0; i < 3; i++) {
                int dripX = 10 + rand.nextInt(WIDTH - 20);
                int dripY = HEIGHT - 5 - ((f + i * 2) % 6);
                g.setColor(new Color(80, 200, 60, 150));
                g.fillOval(dripX, dripY, 4, 6);
            }

            g.dispose();
            frames.add(frame);
        }

        writeAnimatedGif(frames, FRAME_DELAY, new File(outputPath));
    }

    /**
     * Writes frames to an animated GIF file
     */
    private static void writeAnimatedGif(List<BufferedImage> frames, int delayMs, File output) throws IOException {
        ImageOutputStream ios = ImageIO.createImageOutputStream(output);
        ImageWriter writer = ImageIO.getImageWritersByFormatName("gif").next();
        writer.setOutput(ios);
        writer.prepareWriteSequence(null);

        for (int i = 0; i < frames.size(); i++) {
            BufferedImage frame = frames.get(i);

            ImageWriteParam param = writer.getDefaultWriteParam();
            IIOMetadata metadata = writer.getDefaultImageMetadata(
                new ImageTypeSpecifier(frame), param);

            configureGifMetadata(metadata, delayMs, i == 0);
            writer.writeToSequence(new IIOImage(frame, null, metadata), param);
        }

        writer.endWriteSequence();
        ios.close();
    }

    /**
     * Configures GIF metadata for animation
     */
    private static void configureGifMetadata(IIOMetadata metadata, int delayMs, boolean isFirstFrame) {
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

            // Add application extension for looping (only on first frame)
            if (isFirstFrame) {
                javax.imageio.metadata.IIOMetadataNode appExtensions =
                    new javax.imageio.metadata.IIOMetadataNode("ApplicationExtensions");
                javax.imageio.metadata.IIOMetadataNode appExt =
                    new javax.imageio.metadata.IIOMetadataNode("ApplicationExtension");
                appExt.setAttribute("applicationID", "NETSCAPE");
                appExt.setAttribute("authenticationCode", "2.0");
                appExt.setUserObject(new byte[]{1, 0, 0}); // Loop forever
                appExtensions.appendChild(appExt);
                root.appendChild(appExtensions);
            }

            root.appendChild(gce);
            metadata.setFromTree(formatName, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
