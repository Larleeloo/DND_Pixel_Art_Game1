package animation;
import graphics.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single bone in a skeletal animation system.
 * Each bone can have a texture (PNG/GIF), position, rotation, and pivot point.
 * Bones can be organized hierarchically with parent-child relationships.
 * Supports animated GIF textures with automatic frame cycling.
 */
public class Bone {

    private String name;

    // Texture - supports both static and animated textures
    private BufferedImage texture;  // Static texture or current frame for backward compatibility
    private AnimatedTexture animatedTexture;  // Animated texture support (GIF)
    private int textureWidth;
    private int textureHeight;

    // Local transform (relative to parent)
    private double localX;       // X offset from parent's pivot
    private double localY;       // Y offset from parent's pivot
    private double rotation;     // Rotation in degrees (clockwise)
    private double scaleX = 1.0;      // Animation scale (set by animations)
    private double scaleY = 1.0;
    private double baseScaleX = 1.0;  // Customization scale (persists through animations)
    private double baseScaleY = 1.0;

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
    private Color tintColor = null;  // Color tint applied to textures (null = no tint)
    private BufferedImage tintedTexture = null;  // Cached tinted texture (only non-transparent pixels)
    private Color cachedTintColor = null;  // Tint color used for cache

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
     * Supports PNG, JPG, and animated GIF files.
     * GIF animations will cycle automatically when update() is called.
     * @param path Path to the image file
     */
    public void loadTexture(String path) {
        AssetLoader.ImageAsset asset = AssetLoader.load(path);
        this.texture = asset.staticImage;
        this.animatedTexture = asset.animatedTexture;
        // Invalidate tinted texture cache when base texture changes
        this.tintedTexture = null;
        if (this.texture != null) {
            this.textureWidth = asset.width;
            this.textureHeight = asset.height;
            String animInfo = (animatedTexture != null && animatedTexture.isAnimated())
                ? ", " + animatedTexture.getFrameCount() + " frames" : "";
            System.out.println("Bone '" + name + "' loaded texture: " + path +
                              " (" + textureWidth + "x" + textureHeight + animInfo + ")");
        } else {
            // Texture not found - will use placeholder color
            this.textureWidth = 0;
            this.textureHeight = 0;
            this.animatedTexture = null;
            System.out.println("Bone '" + name + "' texture not found: " + path +
                              " (using placeholder color)");
        }
    }

    /**
     * Updates animated textures. Call this each frame.
     * @param deltaMs Time elapsed since last update in milliseconds
     */
    public void updateAnimation(long deltaMs) {
        if (animatedTexture != null && animatedTexture.isAnimated()) {
            animatedTexture.update(deltaMs);
            // Update the static texture reference for tinting compatibility
            texture = animatedTexture.getCurrentFrame();
            // Invalidate tint cache when frame changes
            tintedTexture = null;
        }
        // Update children recursively
        for (Bone child : children) {
            child.updateAnimation(deltaMs);
        }
    }

    /**
     * Checks if this bone has an animated texture.
     * @return true if the texture is animated (multi-frame GIF)
     */
    public boolean isAnimated() {
        return animatedTexture != null && animatedTexture.isAnimated();
    }

    /**
     * Gets the animated texture if available.
     * @return AnimatedTexture or null if static
     */
    public AnimatedTexture getAnimatedTexture() {
        return animatedTexture;
    }

    /**
     * Sets an animated texture directly.
     * @param animTex The AnimatedTexture to use
     */
    public void setAnimatedTexture(AnimatedTexture animTex) {
        this.animatedTexture = animTex;
        if (animTex != null) {
            this.texture = animTex.getCurrentFrame();
            this.textureWidth = animTex.getWidth();
            this.textureHeight = animTex.getHeight();
        }
        this.tintedTexture = null;
    }

    /**
     * Sets the texture directly from a BufferedImage.
     * This clears any animated texture and uses a static image.
     * @param texture The texture image
     */
    public void setTexture(BufferedImage texture) {
        this.texture = texture;
        this.animatedTexture = null;  // Clear animated texture when setting static
        // Invalidate tinted texture cache when base texture changes
        this.tintedTexture = null;
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
     * Sets the animation scale factors (set by animations each frame).
     * @param scaleX Horizontal scale (1.0 = normal)
     * @param scaleY Vertical scale (1.0 = normal)
     */
    public void setScale(double scaleX, double scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        markTransformDirty();
    }

    /**
     * Sets the base/customization scale factors.
     * This scale persists through animation changes and is multiplied with animation scale.
     * @param scaleX Horizontal base scale (1.0 = normal)
     * @param scaleY Vertical base scale (1.0 = normal)
     */
    public void setBaseScale(double scaleX, double scaleY) {
        this.baseScaleX = scaleX;
        this.baseScaleY = scaleY;
        markTransformDirty();
    }

    /**
     * Gets the base scale X value.
     * @return Base scale X
     */
    public double getBaseScaleX() {
        return baseScaleX;
    }

    /**
     * Gets the base scale Y value.
     * @return Base scale Y
     */
    public double getBaseScaleY() {
        return baseScaleY;
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
     * Also sets the tint color for textured bones.
     * @param color The color to use
     */
    public void setPlaceholderColor(Color color) {
        this.placeholderColor = color;
        // Use setTintColor to properly handle cache invalidation
        setTintColor(color);
    }

    /**
     * Sets the tint color applied to textured bones.
     * @param color Tint color (null for no tint)
     */
    public void setTintColor(Color color) {
        this.tintColor = color;
        // Invalidate cached tinted texture if color changed
        if ((color == null && cachedTintColor != null) ||
            (color != null && !color.equals(cachedTintColor))) {
            tintedTexture = null;
            cachedTintColor = null;
        }
    }

    /**
     * Gets the tint color.
     * @return Tint color or null
     */
    public Color getTintColor() {
        return tintColor;
    }

    /**
     * Creates a tinted copy of the texture, only affecting non-transparent pixels.
     * The tint is blended with the original pixel colors.
     * @return Tinted texture image
     */
    private BufferedImage createTintedTexture() {
        if (texture == null) return null;

        BufferedImage tinted = new BufferedImage(
            textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);

        float tintR = tintColor.getRed() / 255.0f;
        float tintG = tintColor.getGreen() / 255.0f;
        float tintB = tintColor.getBlue() / 255.0f;
        float blendFactor = 0.4f;  // How much of the tint to apply (0.0 = none, 1.0 = full)

        for (int y = 0; y < textureHeight; y++) {
            for (int x = 0; x < textureWidth; x++) {
                int pixel = texture.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xFF;

                // Only tint non-transparent pixels
                if (alpha > 0) {
                    int r = (pixel >> 16) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = pixel & 0xFF;

                    // Blend original color with tint color
                    int newR = Math.min(255, (int)(r * (1 - blendFactor) + tintR * 255 * blendFactor));
                    int newG = Math.min(255, (int)(g * (1 - blendFactor) + tintG * 255 * blendFactor));
                    int newB = Math.min(255, (int)(b * (1 - blendFactor) + tintB * 255 * blendFactor));

                    tinted.setRGB(x, y, (alpha << 24) | (newR << 16) | (newG << 8) | newB);
                } else {
                    // Keep fully transparent pixels transparent
                    tinted.setRGB(x, y, 0);
                }
            }
        }

        return tinted;
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
        // Pass scale so local positions are properly scaled for screen coordinates
        updateWorldTransform(rootX, rootY, 0, rootScale);

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

        // Calculate the scaled dimensions (baseScale * animationScale)
        double totalScaleX = baseScaleX * scaleX;
        double totalScaleY = baseScaleY * scaleY;
        int drawWidth = (int)(texW * rootScale * totalScaleX);
        int drawHeight = (int)(texH * rootScale * totalScaleY);

        // Calculate pivot in pixels
        double pivotPixelX = drawWidth * pivotX;
        double pivotPixelY = drawHeight * pivotY;

        // Create transform: translate to world position, rotate around pivot
        AffineTransform boneTransform = new AffineTransform();
        boneTransform.translate(worldX, worldY);
        boneTransform.rotate(Math.toRadians(worldRotation));
        boneTransform.translate(-pivotPixelX, -pivotPixelY);
        boneTransform.scale(rootScale * totalScaleX, rootScale * totalScaleY);

        // Concatenate with existing transform (preserves camera transform)
        AffineTransform combined = new AffineTransform(oldTransform);
        combined.concatenate(boneTransform);
        g.setTransform(combined);

        // Draw the texture or a placeholder
        if (texture != null) {
            // Use tinted texture if tint color is set
            if (tintColor != null) {
                // Create and cache tinted texture if needed
                if (tintedTexture == null || !tintColor.equals(cachedTintColor)) {
                    tintedTexture = createTintedTexture();
                    cachedTintColor = tintColor;
                }
                // Draw the pre-tinted texture (only non-transparent pixels are tinted)
                g.drawImage(tintedTexture, 0, 0, textureWidth, textureHeight, null);
            } else {
                // Draw original texture without tint
                g.drawImage(texture, 0, 0, textureWidth, textureHeight, null);
            }
        } else {
            // Draw placeholder rectangle for bones without textures
            // Use tintColor if set, otherwise use placeholderColor
            Color displayColor = (tintColor != null) ? tintColor : placeholderColor;
            g.setColor(displayColor);
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
     * Local positions are scaled by rootScale to match screen coordinates.
     * @param rootX World X position of skeleton root
     * @param rootY World Y position of skeleton root
     * @param parentRotation Parent's world rotation in degrees
     * @param scale Scale factor for converting local to screen coordinates
     */
    private void updateWorldTransform(double rootX, double rootY, double parentRotation, double scale) {
        if (parent == null) {
            // Root bone: use root position + scaled local offset
            worldRotation = rotation;

            worldX = rootX + localX * scale;
            worldY = rootY + localY * scale;
        } else {
            // Child bone: inherit parent transform
            worldRotation = parentRotation + rotation;

            // Scale local offset by:
            // 1. Root scale (for screen coordinates)
            // 2. Parent's baseScale (so children stay connected when parent scales)
            //
            // Example: arm_lower.localY = 16 (arm_upper's height)
            // When arm_upper.baseScaleY = 2.0, arm_upper is now 32 pixels tall
            // So arm_lower needs to be at 16 * 2.0 = 32 pixels from arm_upper's position
            double scaledLocalX = localX * scale * parent.baseScaleX;
            double scaledLocalY = localY * scale * parent.baseScaleY;

            double rad = Math.toRadians(parentRotation);
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);

            double rotatedX = scaledLocalX * cos - scaledLocalY * sin;
            double rotatedY = scaledLocalX * sin + scaledLocalY * cos;

            worldX = parent.worldX + rotatedX;
            worldY = parent.worldY + rotatedY;
        }

        transformDirty = false;

        // Update children
        for (Bone child : children) {
            child.updateWorldTransform(rootX, rootY, worldRotation, scale);
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
