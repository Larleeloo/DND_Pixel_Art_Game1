package animation;

import graphics.AnimatedTexture;
import graphics.AssetLoader;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * EquipmentOverlay manages clothing and armor GIF overlays that synchronize
 * with the base sprite animation. This allows for layered character appearance
 * where equipment visually overlays the base character sprite.
 *
 * Features:
 * - Multiple equipment slots (helmet, chest, legs, boots, weapon, etc.)
 * - GIF animations that sync with base sprite frame timing
 * - Layer ordering (render order based on slot type)
 * - Per-slot tinting for color customization
 * - Support for different equipment per action state
 *
 * Usage:
 *   EquipmentOverlay overlay = new EquipmentOverlay();
 *   overlay.equipItem(EquipmentSlot.CHEST, ActionState.IDLE, "assets/armor/iron_chest_idle.gif");
 *   overlay.equipItem(EquipmentSlot.CHEST, ActionState.WALK, "assets/armor/iron_chest_walk.gif");
 *   overlay.syncToFrame(baseAnimation.getCurrentFrameIndex());
 *   overlay.draw(g, x, y, width, height, facingRight, ActionState.WALK);
 */
public class EquipmentOverlay {

    /**
     * Equipment slots that can hold overlay items.
     * Order determines render order (lower values drawn first/behind).
     */
    public enum EquipmentSlot {
        BACK(0),        // Capes, backpacks - drawn behind character
        LEGS(1),        // Pants, greaves
        BOOTS(2),       // Shoes, boots
        CHEST(3),       // Shirts, armor
        GLOVES(4),      // Gloves, gauntlets
        HELMET(5),      // Hats, helmets
        WEAPON(6),      // Held items - drawn in front
        ACCESSORY(7);   // Jewelry, effects

        private final int renderOrder;

        EquipmentSlot(int renderOrder) {
            this.renderOrder = renderOrder;
        }

        public int getRenderOrder() {
            return renderOrder;
        }
    }

    /**
     * Represents a single equipped item with animations for each action state.
     */
    public static class EquippedItem {
        private final String name;
        private final EquipmentSlot slot;
        private final Map<SpriteAnimation.ActionState, AnimatedTexture> animations;
        private Color tintColor;
        private boolean visible;

        public EquippedItem(String name, EquipmentSlot slot) {
            this.name = name;
            this.slot = slot;
            this.animations = new HashMap<>();
            this.tintColor = null;
            this.visible = true;
        }

        public String getName() {
            return name;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }

        public void setAnimation(SpriteAnimation.ActionState state, AnimatedTexture texture) {
            if (texture != null) {
                animations.put(state, texture);
            }
        }

        public AnimatedTexture getAnimation(SpriteAnimation.ActionState state) {
            AnimatedTexture anim = animations.get(state);
            if (anim == null) {
                // Fallback to IDLE if specific animation not found
                anim = animations.get(SpriteAnimation.ActionState.IDLE);
            }
            return anim;
        }

        public boolean hasAnimation(SpriteAnimation.ActionState state) {
            return animations.containsKey(state);
        }

        public void setTint(Color color) {
            this.tintColor = color;
        }

        public Color getTint() {
            return tintColor;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isVisible() {
            return visible;
        }

        /**
         * Syncs this item's animation to a specific frame index.
         *
         * @param frameIndex Target frame index
         * @param state Current action state
         */
        public void syncToFrame(int frameIndex, SpriteAnimation.ActionState state) {
            AnimatedTexture anim = getAnimation(state);
            if (anim != null && anim.getFrameCount() > 0) {
                // Map frame index to this animation's frame count
                int mappedFrame = frameIndex % anim.getFrameCount();
                anim.setCurrentFrameIndex(mappedFrame);
            }
        }

        /**
         * Gets the current frame for the given action state.
         *
         * @param state Action state
         * @return BufferedImage frame or null
         */
        public BufferedImage getCurrentFrame(SpriteAnimation.ActionState state) {
            AnimatedTexture anim = getAnimation(state);
            if (anim != null) {
                if (tintColor != null) {
                    return anim.getCurrentFrame(tintColor);
                }
                return anim.getCurrentFrame();
            }
            return null;
        }
    }

    // Equipped items by slot
    private final Map<EquipmentSlot, EquippedItem> equippedItems;

    // Sorted list for render order
    private List<EquippedItem> renderOrder;
    private boolean renderOrderDirty;

    /**
     * Creates a new EquipmentOverlay system.
     */
    public EquipmentOverlay() {
        this.equippedItems = new EnumMap<>(EquipmentSlot.class);
        this.renderOrder = new ArrayList<>();
        this.renderOrderDirty = true;
    }

    /**
     * Equips an item to a slot, loading a GIF animation for a specific action state.
     *
     * @param slot Equipment slot
     * @param state Action state for this animation
     * @param gifPath Path to the GIF file
     * @param itemName Display name for the item
     * @return true if loaded successfully
     */
    public boolean equipItem(EquipmentSlot slot, SpriteAnimation.ActionState state,
                             String gifPath, String itemName) {
        try {
            AssetLoader.ImageAsset asset = AssetLoader.load(gifPath);

            // Get or create the equipped item for this slot
            EquippedItem item = equippedItems.get(slot);
            if (item == null) {
                item = new EquippedItem(itemName, slot);
                equippedItems.put(slot, item);
                renderOrderDirty = true;
            }

            // Add the animation for this action state
            AnimatedTexture texture;
            if (asset.animatedTexture != null) {
                texture = asset.animatedTexture;
            } else {
                texture = new AnimatedTexture(asset.staticImage);
            }
            item.setAnimation(state, texture);

            System.out.println("EquipmentOverlay: Equipped " + itemName + " to " + slot +
                    " for " + state + " from " + gifPath);
            return true;
        } catch (Exception e) {
            System.err.println("EquipmentOverlay: Failed to load " + gifPath + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Equips an item with a pre-loaded animation.
     *
     * @param slot Equipment slot
     * @param state Action state
     * @param texture Pre-loaded AnimatedTexture
     * @param itemName Display name
     */
    public void equipItem(EquipmentSlot slot, SpriteAnimation.ActionState state,
                          AnimatedTexture texture, String itemName) {
        EquippedItem item = equippedItems.get(slot);
        if (item == null) {
            item = new EquippedItem(itemName, slot);
            equippedItems.put(slot, item);
            renderOrderDirty = true;
        }
        item.setAnimation(state, texture);
    }

    /**
     * Removes an item from a slot.
     *
     * @param slot Slot to clear
     */
    public void unequipItem(EquipmentSlot slot) {
        if (equippedItems.remove(slot) != null) {
            renderOrderDirty = true;
            System.out.println("EquipmentOverlay: Unequipped item from " + slot);
        }
    }

    /**
     * Gets the equipped item in a slot.
     *
     * @param slot Equipment slot
     * @return EquippedItem or null if empty
     */
    public EquippedItem getEquippedItem(EquipmentSlot slot) {
        return equippedItems.get(slot);
    }

    /**
     * Checks if a slot has an item equipped.
     *
     * @param slot Equipment slot
     * @return true if item is equipped
     */
    public boolean hasItemEquipped(EquipmentSlot slot) {
        return equippedItems.containsKey(slot);
    }

    /**
     * Sets the tint color for an equipped item.
     *
     * @param slot Equipment slot
     * @param color Tint color (null to clear)
     */
    public void setItemTint(EquipmentSlot slot, Color color) {
        EquippedItem item = equippedItems.get(slot);
        if (item != null) {
            item.setTint(color);
        }
    }

    /**
     * Sets the visibility of an equipped item.
     *
     * @param slot Equipment slot
     * @param visible true to show, false to hide
     */
    public void setItemVisible(EquipmentSlot slot, boolean visible) {
        EquippedItem item = equippedItems.get(slot);
        if (item != null) {
            item.setVisible(visible);
        }
    }

    /**
     * Synchronizes all overlay animations to match the base sprite's frame.
     *
     * @param frameIndex Current frame index of the base sprite
     * @param state Current action state
     */
    public void syncToFrame(int frameIndex, SpriteAnimation.ActionState state) {
        for (EquippedItem item : equippedItems.values()) {
            item.syncToFrame(frameIndex, state);
        }
    }

    /**
     * Updates all overlay animations.
     *
     * @param deltaMs Time elapsed since last update
     * @param state Current action state
     */
    public void update(long deltaMs, SpriteAnimation.ActionState state) {
        for (EquippedItem item : equippedItems.values()) {
            AnimatedTexture anim = item.getAnimation(state);
            if (anim != null) {
                anim.update(deltaMs);
            }
        }
    }

    /**
     * Updates the render order list if items have changed.
     */
    private void updateRenderOrder() {
        if (!renderOrderDirty) return;

        renderOrder = new ArrayList<>(equippedItems.values());
        renderOrder.sort(Comparator.comparingInt(item -> item.getSlot().getRenderOrder()));
        renderOrderDirty = false;
    }

    /**
     * Draws all equipped items in proper render order.
     *
     * @param g Graphics context
     * @param x X position
     * @param y Y position
     * @param width Scaled width
     * @param height Scaled height
     * @param facingRight True if facing right
     * @param state Current action state
     */
    public void draw(Graphics g, int x, int y, int width, int height,
                     boolean facingRight, SpriteAnimation.ActionState state) {
        updateRenderOrder();

        Graphics2D g2d = (Graphics2D) g;

        for (EquippedItem item : renderOrder) {
            if (!item.isVisible()) continue;

            BufferedImage frame = item.getCurrentFrame(state);
            if (frame == null) continue;

            if (facingRight) {
                g2d.drawImage(frame, x, y, width, height, null);
            } else {
                // Flip horizontally
                g2d.drawImage(frame, x + width, y, -width, height, null);
            }
        }
    }

    /**
     * Draws items that should render behind the character (BACK slot).
     *
     * @param g Graphics context
     * @param x X position
     * @param y Y position
     * @param width Scaled width
     * @param height Scaled height
     * @param facingRight True if facing right
     * @param state Current action state
     */
    public void drawBehind(Graphics g, int x, int y, int width, int height,
                           boolean facingRight, SpriteAnimation.ActionState state) {
        Graphics2D g2d = (Graphics2D) g;

        EquippedItem backItem = equippedItems.get(EquipmentSlot.BACK);
        if (backItem != null && backItem.isVisible()) {
            BufferedImage frame = backItem.getCurrentFrame(state);
            if (frame != null) {
                if (facingRight) {
                    g2d.drawImage(frame, x, y, width, height, null);
                } else {
                    g2d.drawImage(frame, x + width, y, -width, height, null);
                }
            }
        }
    }

    /**
     * Draws items that should render in front of the character (all except BACK).
     *
     * @param g Graphics context
     * @param x X position
     * @param y Y position
     * @param width Scaled width
     * @param height Scaled height
     * @param facingRight True if facing right
     * @param state Current action state
     */
    public void drawInFront(Graphics g, int x, int y, int width, int height,
                            boolean facingRight, SpriteAnimation.ActionState state) {
        updateRenderOrder();

        Graphics2D g2d = (Graphics2D) g;

        for (EquippedItem item : renderOrder) {
            if (!item.isVisible()) continue;
            if (item.getSlot() == EquipmentSlot.BACK) continue; // Already drawn behind

            BufferedImage frame = item.getCurrentFrame(state);
            if (frame == null) continue;

            if (facingRight) {
                g2d.drawImage(frame, x, y, width, height, null);
            } else {
                g2d.drawImage(frame, x + width, y, -width, height, null);
            }
        }
    }

    /**
     * Gets the number of equipped items.
     *
     * @return Number of equipped items
     */
    public int getEquippedCount() {
        return equippedItems.size();
    }

    /**
     * Clears all equipped items.
     */
    public void clearAll() {
        equippedItems.clear();
        renderOrderDirty = true;
    }

    /**
     * Gets all equipped slots.
     *
     * @return Set of equipped slots
     */
    public Set<EquipmentSlot> getEquippedSlots() {
        return equippedItems.keySet();
    }
}
