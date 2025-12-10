import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single bone in a skeletal animation system.
 * Each bone can have a texture (PNG/GIF), position, rotation, and pivot point.
 * Bones can be organized hierarchically with parent-child relationships.
 */
public class Bone {

    private String name;

    // Texture
    private BufferedImage texture;
    private int textureWidth;
    private int textureHeight;

    // Local transform (relative to parent)
    private double localX;       // X offset from parent's pivot
    private double localY;       // Y offset from parent's pivot
    private double rotation;     // Rotation in degrees (clockwise)
    private double scaleX = 1.0;
    private double scaleY = 1.0;

    // Pivot point (origin for rotation, relative to texture)
    // Values from 0.0 to 1.0 (0.5, 0.5 = center)
    private double pivotX = 0.5;
    private double pivotY = 0.5;

    // Hierarchy
    private Bone parent;
    private List<Bone> children;

    // Rendering
    private int zOrder = 0;  // Higher = drawn on top
    private boolean visible = true;

    // Default size for bones without textures
    private int defaultWidth = 8;
    private int defaultHeight = 12;
    private Color placeholderColor = new Color(180, 140, 100);  // Skin tone

    // Cached world transform
    private double worldX;
    private double worldY;
    private double worldRotation;
    private boolean transformDirty = true;

    /**
     * Creates a new bone with a name.
     * @param name Unique identifier for this bone
     */
    public Bone(String name) {
        this.name = name;
        this.children = new ArrayList<>();
        this.localX = 0;
        this.localY = 0;
        this.rotation = 0;
    }

    /**
     * Creates a new bone with a name and texture.
     * @param name Unique identifier for this bone
     * @param texturePath Path to PNG or GIF texture file
     */
    public Bone(String name, String texturePath) {
        this(name);
        loadTexture(texturePath);
    }

    /**
     * Loads a texture from a file path.
     * Supports PNG, JPG, and GIF (first frame for GIF).
     * @param path Path to the image file
     */
    public void loadTexture(String path) {
        AssetLoader.ImageAsset asset = AssetLoader.load(path);
        this.texture = asset.staticImage;
        if (this.texture != null) {
            this.textureWidth = asset.width;
            this.textureHeight = asset.height;
            System.out.println("Bone '" + name + "' loaded texture: " + path +
                              " (" + textureWidth + "x" + textureHeight + ")");
        } else {
            // Texture not found - will use placeholder color
            this.textureWidth = 0;
            this.textureHeight = 0;
            System.out.println("Bone '" + name + "' texture not found: " + path +
                              " (using placeholder color)");
        }
    }

    /**
     * Sets the texture directly from a BufferedImage.
     * @param texture The texture image
     */
    public void setTexture(BufferedImage texture) {
        this.texture = texture;
        if (texture != null) {
            this.textureWidth = texture.getWidth();
            this.textureHeight = texture.getHeight();
        }
    }

    // ==================== Transform Methods ====================

    /**
     * Sets the local position relative to parent's pivot point.
     * @param x X offset
     * @param y Y offset
     */
    public void setLocalPosition(double x, double y) {
        this.localX = x;
        this.localY = y;
        markTransformDirty();
    }

    /**
     * Sets the rotation in degrees (clockwise).
     * @param degrees Rotation angle
     */
    public void setRotation(double degrees) {
        this.rotation = degrees;
        markTransformDirty();
    }

    /**
     * Gets the current rotation in degrees.
     * @return Rotation angle
     */
    public double getRotation() {
        return rotation;
    }

    /**
     * Sets the scale factors.
     * @param scaleX Horizontal scale (1.0 = normal)
     * @param scaleY Vertical scale (1.0 = normal)
     */
    public void setScale(double scaleX, double scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        markTransformDirty();
    }

    /**
     * Sets the pivot point (origin for rotation) as a fraction of texture size.
     * (0, 0) = top-left, (0.5, 0.5) = center, (1, 1) = bottom-right
     * @param pivotX X pivot (0.0 to 1.0)
     * @param pivotY Y pivot (0.0 to 1.0)
     */
    public void setPivot(double pivotX, double pivotY) {
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        markTransformDirty();
    }

    /**
     * Gets the local X position.
     * @return X offset from parent
     */
    public double getLocalX() {
        return localX;
    }

    /**
     * Gets the local Y position.
     * @return Y offset from parent
     */
    public double getLocalY() {
        return localY;
    }

    /**
     * Sets the default size for this bone when no texture is loaded.
     * @param width Default width in pixels
     * @param height Default height in pixels
     */
    public void setDefaultSize(int width, int height) {
        this.defaultWidth = width;
        this.defaultHeight = height;
    }

    /**
     * Sets the placeholder color when no texture is loaded.
     * @param color The color to use
     */
    public void setPlaceholderColor(Color color) {
        this.placeholderColor = color;
    }

    // ==================== Hierarchy Methods ====================

    /**
     * Adds a child bone to this bone.
     * @param child The bone to add as a child
     */
    public void addChild(Bone child) {
        if (child.parent != null) {
            child.parent.removeChild(child);
        }
        child.parent = this;
        children.add(child);
        child.markTransformDirty();
    }

    /**
     * Removes a child bone.
     * @param child The bone to remove
     */
    public void removeChild(Bone child) {
        if (children.remove(child)) {
            child.parent = null;
        }
    }

    /**
     * Gets the parent bone.
     * @return Parent bone, or null if this is a root bone
     */
    public Bone getParent() {
        return parent;
    }

    /**
     * Gets all child bones.
     * @return List of child bones
     */
    public List<Bone> getChildren() {
        return new ArrayList<>(children);
    }

    /**
     * Finds a bone by name in this bone's subtree.
     * @param boneName Name of the bone to find
     * @return The bone, or null if not found
     */
    public Bone findBone(String boneName) {
        if (this.name.equals(boneName)) {
            return this;
        }
        for (Bone child : children) {
            Bone found = child.findBone(boneName);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    // ==================== Rendering Methods ====================

    /**
     * Sets the z-order (drawing order). Higher values are drawn on top.
     * @param zOrder Z-order value
     */
    public void setZOrder(int zOrder) {
        this.zOrder = zOrder;
    }

    /**
     * Gets the z-order.
     * @return Z-order value
     */
    public int getZOrder() {
        return zOrder;
    }

    /**
     * Sets whether this bone is visible.
     * @param visible True to show, false to hide
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Checks if this bone is visible.
     * @return True if visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Gets the bone's name.
     * @return Bone name
     */
    public String getName() {
        return name;
    }

    /**
     * Draws this bone and all its children.
     * @param g Graphics context
     * @param rootX World X position of the skeleton root
     * @param rootY World Y position of the skeleton root
     * @param rootScale Scale factor for rendering
     */
    public void draw(Graphics2D g, double rootX, double rootY, double rootScale) {
        // Always update transforms for all bones (even invisible ones)
        // Individual bone visibility is handled in drawSingle()
        updateWorldTransform(rootX, rootY, 0);

        // Collect all bones for z-order sorting
        List<Bone> allBones = new ArrayList<>();
        collectBones(allBones);

        // Debug: show collected bones count
        if (debugRendering && debugCounter < 3) {
            System.out.println("=== Drawing skeleton at (" + (int)rootX + "," + (int)rootY +
                             ") scale=" + rootScale + " bones=" + allBones.size() + " ===");
        }

        // Sort by z-order
        allBones.sort((a, b) -> Integer.compare(a.zOrder, b.zOrder));

        // Draw in z-order
        for (Bone bone : allBones) {
            bone.drawSingle(g, rootX, rootY, rootScale);
        }

        // Increment debug counter after full skeleton draw
        if (debugRendering) {
            debugCounter++;
        }
    }

    /**
     * Collects this bone and all descendants into a list.
     */
    private void collectBones(List<Bone> list) {
        list.add(this);
        for (Bone child : children) {
            child.collectBones(list);
        }
    }

    // Debug flag - set to true to see bone rendering info
    private static boolean debugRendering = true;
    private static int debugCounter = 0;

    /**
     * Draws just this bone (without children).
     */
    private void drawSingle(Graphics2D g, double rootX, double rootY, double rootScale) {
        if (!visible) return;

        // Use default size if no texture
        int texW = textureWidth > 0 ? textureWidth : defaultWidth;
        int texH = textureHeight > 0 ? textureHeight : defaultHeight;

        // Debug output (only first few frames to avoid spam)
        if (debugRendering && debugCounter < 3) {
            System.out.println("Drawing bone '" + name + "' at world(" + (int)worldX + "," + (int)worldY +
                             ") size=" + texW + "x" + texH + " scale=" + rootScale +
                             " hasTexture=" + (texture != null));
        }

        // Save the current transform (includes camera transform in scrolling levels)
        AffineTransform oldTransform = g.getTransform();

        // Calculate the scaled dimensions
        int drawWidth = (int)(texW * rootScale * scaleX);
        int drawHeight = (int)(texH * rootScale * scaleY);

        // Calculate pivot in pixels
        double pivotPixelX = drawWidth * pivotX;
        double pivotPixelY = drawHeight * pivotY;

        // Create transform: translate to world position, rotate around pivot
        AffineTransform boneTransform = new AffineTransform();
        boneTransform.translate(worldX, worldY);
        boneTransform.rotate(Math.toRadians(worldRotation));
        boneTransform.translate(-pivotPixelX, -pivotPixelY);
        boneTransform.scale(rootScale * scaleX, rootScale * scaleY);

        // Concatenate with existing transform (preserves camera transform)
        AffineTransform combined = new AffineTransform(oldTransform);
        combined.concatenate(boneTransform);
        g.setTransform(combined);

        // Draw the texture or a placeholder
        if (texture != null) {
            g.drawImage(texture, 0, 0, textureWidth, textureHeight, null);
        } else {
            // Draw placeholder rectangle for bones without textures
            g.setColor(placeholderColor);
            g.fillRect(0, 0, texW, texH);
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, texW - 1, texH - 1);
        }

        // Restore transform
        g.setTransform(oldTransform);
    }

    // ==================== Transform Calculation ====================

    /**
     * Marks this bone and all children as needing transform recalculation.
     */
    private void markTransformDirty() {
        transformDirty = true;
        for (Bone child : children) {
            child.markTransformDirty();
        }
    }

    /**
     * Updates the world transform based on parent transforms.
     */
    private void updateWorldTransform(double rootX, double rootY, double parentRotation) {
        if (parent == null) {
            // Root bone: use root position + local offset
            worldRotation = rotation;

            // Apply rotation to local offset
            double rad = Math.toRadians(worldRotation);
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);

            worldX = rootX + localX;
            worldY = rootY + localY;
        } else {
            // Child bone: inherit parent transform
            worldRotation = parentRotation + rotation;

            // Rotate local offset by parent's world rotation
            double rad = Math.toRadians(parentRotation);
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);

            double rotatedX = localX * cos - localY * sin;
            double rotatedY = localX * sin + localY * cos;

            worldX = parent.worldX + rotatedX;
            worldY = parent.worldY + rotatedY;
        }

        transformDirty = false;

        // Update children
        for (Bone child : children) {
            child.updateWorldTransform(rootX, rootY, worldRotation);
        }
    }

    /**
     * Gets the calculated world X position.
     * @return World X coordinate
     */
    public double getWorldX() {
        return worldX;
    }

    /**
     * Gets the calculated world Y position.
     * @return World Y coordinate
     */
    public double getWorldY() {
        return worldY;
    }

    /**
     * Gets the calculated world rotation.
     * @return World rotation in degrees
     */
    public double getWorldRotation() {
        return worldRotation;
    }

    /**
     * Gets the texture width.
     * @return Texture width in pixels
     */
    public int getTextureWidth() {
        return textureWidth;
    }

    /**
     * Gets the texture height.
     * @return Texture height in pixels
     */
    public int getTextureHeight() {
        return textureHeight;
    }
}
