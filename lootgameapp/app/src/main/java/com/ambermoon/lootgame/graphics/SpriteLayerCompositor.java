package com.ambermoon.lootgame.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.Map;

/**
 * Composites multiple 64x64 sprite layers with alpha transparency
 * for the clothing preview system.
 *
 * Layers are composited in the order defined by {@link EquipmentSlot#RENDER_ORDER},
 * with the base avatar drawn first and equipment overlays on top.
 *
 * Also provides placeholder equipment sprite generation for all 10 slot types.
 */
public class SpriteLayerCompositor {

    private static final int SIZE = AvatarRegistry.SPRITE_SIZE; // 64
    private static final int FRAME_COUNT = AvatarRegistry.FRAME_COUNT; // 15

    private static final Paint drawPaint = new Paint();
    static {
        drawPaint.setAntiAlias(false);
        drawPaint.setFilterBitmap(false);
    }

    /**
     * Composites a single frame by layering equipment sprites on top of a base avatar frame.
     *
     * @param baseFrame The base avatar frame (64x64)
     * @param slotFrames Map of slot → equipment frame for that slot (can be sparse)
     * @return A new 64x64 composited bitmap
     */
    public static Bitmap compositeFrame(Bitmap baseFrame, Map<Integer, Bitmap> slotFrames) {
        Bitmap result = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // Draw base avatar
        if (baseFrame != null) {
            canvas.drawBitmap(baseFrame, 0, 0, drawPaint);
        }

        // Draw equipment layers in render order
        if (slotFrames != null) {
            for (int slot : EquipmentSlot.RENDER_ORDER) {
                Bitmap frame = slotFrames.get(slot);
                if (frame != null) {
                    canvas.drawBitmap(frame, 0, 0, drawPaint);
                }
            }
        }

        return result;
    }

    /**
     * Composites all 15 walking animation frames.
     *
     * @param baseWalk 15 base avatar walk frames
     * @param slotWalkFrames Map of slot → 15 equipment walk frames (can be sparse)
     * @return Array of 15 composited 64x64 frames
     */
    public static Bitmap[] compositeWalkAnimation(Bitmap[] baseWalk,
                                                    Map<Integer, Bitmap[]> slotWalkFrames) {
        Bitmap[] result = new Bitmap[FRAME_COUNT];
        for (int f = 0; f < FRAME_COUNT; f++) {
            Bitmap base = (baseWalk != null && f < baseWalk.length) ? baseWalk[f] : null;

            // Build per-frame slot map
            java.util.HashMap<Integer, Bitmap> frameSlots = new java.util.HashMap<>();
            if (slotWalkFrames != null) {
                for (Map.Entry<Integer, Bitmap[]> entry : slotWalkFrames.entrySet()) {
                    Bitmap[] frames = entry.getValue();
                    if (frames != null && f < frames.length && frames[f] != null) {
                        frameSlots.put(entry.getKey(), frames[f]);
                    }
                }
            }

            result[f] = compositeFrame(base, frameSlots);
        }
        return result;
    }

    /**
     * Composites the idle frame.
     *
     * @param baseIdle The base avatar idle frame
     * @param slotIdleFrames Map of slot → equipment idle frame (can be sparse)
     * @return A single composited 64x64 frame
     */
    public static Bitmap compositeIdle(Bitmap baseIdle, Map<Integer, Bitmap> slotIdleFrames) {
        return compositeFrame(baseIdle, slotIdleFrames);
    }

    /**
     * Generates placeholder equipment sprites for a given slot.
     * Returns 16 bitmaps: [0-14] = walk frames, [15] = idle frame.
     *
     * Each placeholder is a simple colored shape at the appropriate body region
     * for the slot type, with alpha transparency.
     *
     * @param slot Equipment slot constant
     * @param color Primary color for the placeholder
     * @return Array of 16 Bitmaps (15 walk + 1 idle)
     */
    public static Bitmap[] generatePlaceholderEquipment(int slot, int color) {
        Bitmap[] result = new Bitmap[FRAME_COUNT + 1];

        for (int f = 0; f <= FRAME_COUNT; f++) {
            boolean walking = (f < FRAME_COUNT);
            int frameNum = walking ? f : 0;
            float walkPhase = walking ? (float)(frameNum * 2 * Math.PI / FRAME_COUNT) : 0;
            int legSwing = walking ? (int)(Math.sin(walkPhase) * 4) : 0;
            int armSwing = walking ? (int)(Math.sin(walkPhase) * 3) : 0;
            int bounce = walking ? (int)(Math.abs(Math.sin(walkPhase * 2)) * 2) : 0;
            int baseY = 58 - bounce;

            result[f] = generateSlotOverlay(slot, color, baseY, legSwing, armSwing);
        }

        return result;
    }

    /**
     * Gets the walk frames (indices 0-14) from a full placeholder array.
     */
    public static Bitmap[] getWalkFrames(Bitmap[] allFrames) {
        if (allFrames == null || allFrames.length < FRAME_COUNT) return null;
        Bitmap[] walk = new Bitmap[FRAME_COUNT];
        System.arraycopy(allFrames, 0, walk, 0, FRAME_COUNT);
        return walk;
    }

    /**
     * Gets the idle frame (index 15) from a full placeholder array.
     */
    public static Bitmap getIdleFrame(Bitmap[] allFrames) {
        if (allFrames == null || allFrames.length <= FRAME_COUNT) return null;
        return allFrames[FRAME_COUNT];
    }

    /**
     * Generates a single frame overlay for a slot at the given body position.
     */
    private static Bitmap generateSlotOverlay(int slot, int color, int baseY,
                                               int legSwing, int armSwing) {
        Bitmap bmp = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint();
        p.setAntiAlias(false);
        p.setFilterBitmap(false);
        p.setColor(color);

        int cx = 32;

        // Semi-transparent for placeholder visibility
        int alphaColor = Color.argb(200, Color.red(color), Color.green(color), Color.blue(color));
        int borderColor = Color.argb(255,
            Math.max(0, Color.red(color) - 40),
            Math.max(0, Color.green(color) - 40),
            Math.max(0, Color.blue(color) - 40));

        switch (slot) {
            case EquipmentSlot.SLOT_HEADWEAR:
                // Hat/helmet shape on top of head
                p.setColor(alphaColor);
                c.drawRect(cx - 8, baseY - 54, cx + 8, baseY - 48, p);  // brim
                c.drawRect(cx - 6, baseY - 58, cx + 6, baseY - 54, p);  // top
                p.setColor(borderColor);
                c.drawRect(cx - 8, baseY - 54, cx + 8, baseY - 53, p);  // brim edge
                break;

            case EquipmentSlot.SLOT_SHIRT:
                // Shirt/tunic overlay on torso
                p.setColor(alphaColor);
                c.drawRect(cx - 8, baseY - 34, cx + 8, baseY - 18, p);
                // Sleeves
                c.drawRect(cx - 12, baseY - 32 - armSwing, cx - 8, baseY - 22, p);
                c.drawRect(cx + 8, baseY - 32 + armSwing, cx + 12, baseY - 22, p);
                p.setColor(borderColor);
                c.drawRect(cx - 8, baseY - 34, cx + 8, baseY - 33, p);  // collar
                break;

            case EquipmentSlot.SLOT_ARMOR:
                // Chestplate overlay (slightly larger, with border)
                p.setColor(alphaColor);
                c.drawRect(cx - 9, baseY - 34, cx + 9, baseY - 18, p);
                p.setColor(borderColor);
                c.drawRect(cx - 9, baseY - 34, cx + 9, baseY - 33, p);  // top edge
                c.drawRect(cx - 9, baseY - 19, cx + 9, baseY - 18, p);  // bottom edge
                c.drawRect(cx - 9, baseY - 34, cx - 8, baseY - 18, p);  // left edge
                c.drawRect(cx + 8, baseY - 34, cx + 9, baseY - 18, p);  // right edge
                // Shoulder pads
                c.drawRect(cx - 13, baseY - 34, cx - 8, baseY - 30, p);
                c.drawRect(cx + 8, baseY - 34, cx + 13, baseY - 30, p);
                break;

            case EquipmentSlot.SLOT_GAUNTLETS:
                // Gloves/gauntlets at hand positions
                p.setColor(alphaColor);
                c.drawRect(cx - 13, baseY - 22, cx - 8, baseY - 17, p);  // left
                c.drawRect(cx + 8, baseY - 22, cx + 13, baseY - 17, p);  // right
                p.setColor(borderColor);
                c.drawRect(cx - 13, baseY - 22, cx - 8, baseY - 21, p);  // left cuff
                c.drawRect(cx + 8, baseY - 22, cx + 13, baseY - 21, p);  // right cuff
                break;

            case EquipmentSlot.SLOT_PANTS:
                // Pants on leg area
                p.setColor(alphaColor);
                c.drawRect(cx - 5, baseY - 18, cx - 1, baseY - 4 + legSwing, p);
                c.drawRect(cx + 1, baseY - 18, cx + 5, baseY - 4 - legSwing, p);
                p.setColor(borderColor);
                c.drawRect(cx - 5, baseY - 18, cx + 5, baseY - 17, p);  // waistband
                break;

            case EquipmentSlot.SLOT_LEGGINGS:
                // Armored leggings (thicker than pants, with border)
                p.setColor(alphaColor);
                c.drawRect(cx - 6, baseY - 18, cx - 1, baseY - 4 + legSwing, p);
                c.drawRect(cx + 1, baseY - 18, cx + 6, baseY - 4 - legSwing, p);
                p.setColor(borderColor);
                // Knee guards
                c.drawRect(cx - 6, baseY - 14, cx, baseY - 12, p);
                c.drawRect(cx, baseY - 14, cx + 6, baseY - 12, p);
                break;

            case EquipmentSlot.SLOT_SHOES:
                // Shoes at feet
                p.setColor(alphaColor);
                c.drawRect(cx - 6, baseY - 3 + legSwing, cx, baseY, p);
                c.drawRect(cx, baseY - 3 - legSwing, cx + 6, baseY, p);
                break;

            case EquipmentSlot.SLOT_BOOTS:
                // Tall boots (higher than shoes)
                p.setColor(alphaColor);
                c.drawRect(cx - 6, baseY - 6 + legSwing, cx, baseY, p);
                c.drawRect(cx, baseY - 6 - legSwing, cx + 6, baseY, p);
                p.setColor(borderColor);
                // Boot tops
                c.drawRect(cx - 6, baseY - 6 + legSwing, cx, baseY - 5 + legSwing, p);
                c.drawRect(cx, baseY - 6 - legSwing, cx + 6, baseY - 5 - legSwing, p);
                break;

            case EquipmentSlot.SLOT_NECKLACE:
                // Necklace at neck area
                p.setColor(alphaColor);
                c.drawRect(cx - 4, baseY - 36, cx + 4, baseY - 34, p);  // chain
                // Pendant
                p.setColor(borderColor);
                c.drawRect(cx - 2, baseY - 34, cx + 2, baseY - 32, p);
                break;

            case EquipmentSlot.SLOT_RINGS_GLOVES:
                // Small accent dots at wrist/finger positions
                p.setColor(alphaColor);
                c.drawRect(cx - 11, baseY - 19, cx - 9, baseY - 17, p);  // left ring
                c.drawRect(cx + 9, baseY - 19, cx + 11, baseY - 17, p);  // right ring
                // Sparkle accent
                p.setColor(Color.argb(180, 255, 255, 200));
                c.drawRect(cx - 10, baseY - 18, cx - 10, baseY - 18, p);
                c.drawRect(cx + 10, baseY - 18, cx + 10, baseY - 18, p);
                break;
        }

        return bmp;
    }

    /**
     * Returns a suitable placeholder color for a given slot.
     */
    public static int getDefaultSlotColor(int slot) {
        switch (slot) {
            case EquipmentSlot.SLOT_HEADWEAR:    return Color.rgb(160, 120, 60);   // brown leather
            case EquipmentSlot.SLOT_SHIRT:       return Color.rgb(140, 100, 160);  // purple cloth
            case EquipmentSlot.SLOT_ARMOR:       return Color.rgb(160, 160, 170);  // steel
            case EquipmentSlot.SLOT_GAUNTLETS:   return Color.rgb(140, 140, 150);  // iron
            case EquipmentSlot.SLOT_PANTS:       return Color.rgb(100, 80, 60);    // brown
            case EquipmentSlot.SLOT_LEGGINGS:    return Color.rgb(130, 130, 140);  // chainmail
            case EquipmentSlot.SLOT_SHOES:       return Color.rgb(80, 60, 40);     // dark leather
            case EquipmentSlot.SLOT_BOOTS:       return Color.rgb(100, 70, 40);    // leather
            case EquipmentSlot.SLOT_RINGS_GLOVES:return Color.rgb(200, 180, 50);   // gold
            case EquipmentSlot.SLOT_NECKLACE:    return Color.rgb(180, 180, 200);  // silver
            default: return Color.GRAY;
        }
    }

    /**
     * Returns a placeholder color derived from the item's rarity.
     */
    public static int getColorForRarity(int rarity) {
        switch (rarity) {
            case 0: return Color.rgb(180, 180, 180);  // Common: gray
            case 1: return Color.rgb(80, 200, 80);    // Uncommon: green
            case 2: return Color.rgb(80, 130, 220);   // Rare: blue
            case 3: return Color.rgb(160, 80, 220);   // Epic: purple
            case 4: return Color.rgb(220, 150, 40);   // Legendary: orange
            case 5: return Color.rgb(40, 220, 220);   // Mythic: cyan
            default: return Color.GRAY;
        }
    }
}
