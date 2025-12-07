import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Utility class for generating simple bone textures programmatically.
 * Useful for testing and prototyping before creating actual pixel art.
 */
public class BoneTextureGenerator {

    /**
     * Generates a simple rectangular bone texture with rounded corners.
     * @param width Width in pixels
     * @param height Height in pixels
     * @param color Fill color
     * @param outlineColor Outline color
     * @return Generated BufferedImage
     */
    public static BufferedImage generateRectBone(int width, int height, Color color, Color outlineColor) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Enable anti-aliasing for smoother edges
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw rounded rectangle
        int arc = Math.min(width, height) / 3;
        g.setColor(color);
        g.fillRoundRect(1, 1, width - 2, height - 2, arc, arc);

        // Draw outline
        g.setColor(outlineColor);
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(1, 1, width - 3, height - 3, arc, arc);

        g.dispose();
        return img;
    }

    /**
     * Generates an oval/ellipse bone texture.
     * @param width Width in pixels
     * @param height Height in pixels
     * @param color Fill color
     * @param outlineColor Outline color
     * @return Generated BufferedImage
     */
    public static BufferedImage generateOvalBone(int width, int height, Color color, Color outlineColor) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(color);
        g.fillOval(1, 1, width - 2, height - 2);

        g.setColor(outlineColor);
        g.setStroke(new BasicStroke(1));
        g.drawOval(1, 1, width - 3, height - 3);

        g.dispose();
        return img;
    }

    /**
     * Generates a limb bone (arm or leg) with a tapered shape.
     * @param width Width in pixels
     * @param height Height in pixels
     * @param color Fill color
     * @param outlineColor Outline color
     * @return Generated BufferedImage
     */
    public static BufferedImage generateLimbBone(int width, int height, Color color, Color outlineColor) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Create a tapered polygon
        int[] xPoints = {width/4, 3*width/4, 2*width/3, width/3};
        int[] yPoints = {0, 0, height, height};

        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, 4);

        // Add rounded ends
        g.fillOval(width/4 - 2, -2, width/2 + 4, 6);
        g.fillOval(width/3 - 2, height - 4, width/3 + 4, 6);

        g.setColor(outlineColor);
        g.setStroke(new BasicStroke(1));
        g.drawPolygon(xPoints, yPoints, 4);

        g.dispose();
        return img;
    }

    /**
     * Generates a complete set of humanoid bone textures and saves them to a directory.
     * @param outputDir Directory to save textures
     * @param skinColor Base skin/body color
     */
    public static void generateHumanoidSet(String outputDir, Color skinColor) {
        Color outline = skinColor.darker().darker();

        try {
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Torso - larger rectangle
            BufferedImage torso = generateRectBone(12, 16, skinColor, outline);
            ImageIO.write(torso, "PNG", new File(outputDir + "/torso.png"));

            // Head - oval
            BufferedImage head = generateOvalBone(10, 12, skinColor, outline);
            ImageIO.write(head, "PNG", new File(outputDir + "/head.png"));

            // Arms - small rectangles
            BufferedImage armLeft = generateLimbBone(6, 14, skinColor, outline);
            ImageIO.write(armLeft, "PNG", new File(outputDir + "/arm_left.png"));

            BufferedImage armRight = generateLimbBone(6, 14, skinColor, outline);
            ImageIO.write(armRight, "PNG", new File(outputDir + "/arm_right.png"));

            // Legs - longer tapered
            BufferedImage legLeft = generateLimbBone(8, 18, skinColor, outline);
            ImageIO.write(legLeft, "PNG", new File(outputDir + "/leg_left.png"));

            BufferedImage legRight = generateLimbBone(8, 18, skinColor, outline);
            ImageIO.write(legRight, "PNG", new File(outputDir + "/leg_right.png"));

            System.out.println("Generated humanoid bone textures in: " + outputDir);
        } catch (IOException e) {
            System.err.println("Failed to generate bone textures: " + e.getMessage());
        }
    }

    /**
     * Generates placeholder bone textures with a pixel art style.
     * @param outputDir Directory to save textures
     * @param primaryColor Main body color
     * @param secondaryColor Detail/shading color
     */
    public static void generatePixelArtSet(String outputDir, Color primaryColor, Color secondaryColor) {
        try {
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Torso - 8x10 pixel art
            BufferedImage torso = createPixelArtTorso(8, 10, primaryColor, secondaryColor);
            ImageIO.write(torso, "PNG", new File(outputDir + "/torso.png"));

            // Head - 6x6 pixel art
            BufferedImage head = createPixelArtHead(6, 6, primaryColor, secondaryColor);
            ImageIO.write(head, "PNG", new File(outputDir + "/head.png"));

            // Arms - 3x8 pixel art
            BufferedImage armLeft = createPixelArtLimb(3, 8, primaryColor, secondaryColor);
            ImageIO.write(armLeft, "PNG", new File(outputDir + "/arm_left.png"));
            ImageIO.write(armLeft, "PNG", new File(outputDir + "/arm_right.png"));

            // Legs - 4x10 pixel art
            BufferedImage legLeft = createPixelArtLimb(4, 10, primaryColor, secondaryColor);
            ImageIO.write(legLeft, "PNG", new File(outputDir + "/leg_left.png"));
            ImageIO.write(legLeft, "PNG", new File(outputDir + "/leg_right.png"));

            System.out.println("Generated pixel art bone textures in: " + outputDir);
        } catch (IOException e) {
            System.err.println("Failed to generate bone textures: " + e.getMessage());
        }
    }

    private static BufferedImage createPixelArtTorso(int w, int h, Color primary, Color secondary) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Fill main body
        g.setColor(primary);
        g.fillRect(1, 0, w - 2, h);

        // Add shading on sides
        g.setColor(secondary);
        g.fillRect(0, 1, 1, h - 2);
        g.fillRect(w - 1, 1, 1, h - 2);

        // Add highlight
        g.setColor(primary.brighter());
        g.fillRect(2, 1, 1, h - 2);

        g.dispose();
        return img;
    }

    private static BufferedImage createPixelArtHead(int w, int h, Color primary, Color secondary) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Main head shape
        g.setColor(primary);
        g.fillRect(1, 1, w - 2, h - 2);
        g.fillRect(0, 2, w, h - 4);

        // Shading
        g.setColor(secondary);
        g.fillRect(0, 2, 1, h - 4);

        // Eyes (simple dots)
        g.setColor(Color.BLACK);
        g.fillRect(1, 2, 1, 1);
        g.fillRect(w - 2, 2, 1, 1);

        g.dispose();
        return img;
    }

    private static BufferedImage createPixelArtLimb(int w, int h, Color primary, Color secondary) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Main limb
        g.setColor(primary);
        g.fillRect(0, 0, w, h);

        // Side shading
        g.setColor(secondary);
        g.fillRect(0, 0, 1, h);

        // Highlight
        g.setColor(primary.brighter());
        g.fillRect(w - 1, 0, 1, h);

        g.dispose();
        return img;
    }

    /**
     * Command-line utility to generate bone textures.
     * Usage: java BoneTextureGenerator [outputDir] [style]
     * style: "smooth" or "pixel"
     */
    public static void main(String[] args) {
        String outputDir = "assets/bones";
        String style = "pixel";

        if (args.length > 0) {
            outputDir = args[0];
        }
        if (args.length > 1) {
            style = args[1];
        }

        // Default colors - a pleasant skin tone
        Color skinColor = new Color(255, 200, 150);

        if (style.equals("smooth")) {
            generateHumanoidSet(outputDir, skinColor);
        } else {
            // Pixel art style with shirt and pants colors
            Color shirtColor = new Color(100, 150, 200);  // Blue shirt
            Color pantsColor = new Color(80, 80, 120);    // Dark pants

            generatePixelArtSet(outputDir, shirtColor, pantsColor);
        }

        System.out.println("Bone textures generated successfully!");
        System.out.println("You can now replace these with your own PNG or GIF files.");
    }
}
