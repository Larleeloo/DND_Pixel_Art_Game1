package com.ambermoon.lootgame.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Defines 3 selectable base avatars for the clothing preview system.
 * Each avatar has a 15-frame walking animation (1.5s at 100ms/frame)
 * and a 1-frame idle pose, all at 64x64 pixels.
 *
 * These are programmatic placeholder sprites — simple colored humanoid
 * silhouettes meant to be replaced with real pixel art later.
 *
 * Avatar 0: Knight (gray)
 * Avatar 1: Mage (blue)
 * Avatar 2: Rogue (green)
 */
public class AvatarRegistry {

    public static final int AVATAR_COUNT = 3;
    public static final int FRAME_COUNT = 15;
    public static final int SPRITE_SIZE = 64;
    public static final int FRAME_DELAY_MS = 100; // 1.5s / 15 frames

    public static final String[] AVATAR_NAMES = { "Knight", "Mage", "Rogue" };

    // Avatar body colors
    private static final int[] SKIN_COLORS = {
        Color.rgb(210, 180, 140),  // Knight: tan skin
        Color.rgb(210, 180, 140),  // Mage: tan skin
        Color.rgb(200, 170, 130)   // Rogue: slightly darker tan
    };

    private static final int[] BODY_COLORS = {
        Color.rgb(120, 120, 130),  // Knight: steel gray
        Color.rgb(60, 80, 160),    // Mage: deep blue
        Color.rgb(60, 100, 60)     // Rogue: forest green
    };

    private static final int[] ACCENT_COLORS = {
        Color.rgb(180, 180, 190),  // Knight: light steel
        Color.rgb(100, 120, 200),  // Mage: lighter blue
        Color.rgb(90, 140, 90)     // Rogue: lighter green
    };

    private static final int[] HAIR_COLORS = {
        Color.rgb(90, 60, 30),     // Knight: brown hair
        Color.rgb(180, 180, 200),  // Mage: silver/white hair
        Color.rgb(40, 40, 40)      // Rogue: black hair
    };

    private static Bitmap[][] walkFramesCache;
    private static Bitmap[] idleFramesCache;

    /**
     * Gets the 15 walking animation frames for an avatar.
     * Frames are generated on first access and cached.
     */
    public static Bitmap[] getWalkFrames(int avatarIndex) {
        if (avatarIndex < 0 || avatarIndex >= AVATAR_COUNT) avatarIndex = 0;
        ensureGenerated();
        return walkFramesCache[avatarIndex];
    }

    /**
     * Gets the single idle frame for an avatar.
     */
    public static Bitmap getIdleFrame(int avatarIndex) {
        if (avatarIndex < 0 || avatarIndex >= AVATAR_COUNT) avatarIndex = 0;
        ensureGenerated();
        return idleFramesCache[avatarIndex];
    }

    private static void ensureGenerated() {
        if (walkFramesCache != null) return;
        walkFramesCache = new Bitmap[AVATAR_COUNT][];
        idleFramesCache = new Bitmap[AVATAR_COUNT];
        for (int i = 0; i < AVATAR_COUNT; i++) {
            walkFramesCache[i] = generateWalkFrames(i);
            idleFramesCache[i] = generateIdleFrame(i);
        }
    }

    /**
     * Generates 15 walking animation frames for an avatar.
     * The walk cycle uses a sine-based leg swing and subtle body bounce.
     */
    private static Bitmap[] generateWalkFrames(int avatarIndex) {
        Bitmap[] frames = new Bitmap[FRAME_COUNT];
        for (int f = 0; f < FRAME_COUNT; f++) {
            frames[f] = generateFrame(avatarIndex, f, true);
        }
        return frames;
    }

    private static Bitmap generateIdleFrame(int avatarIndex) {
        return generateFrame(avatarIndex, -1, false);
    }

    /**
     * Generates a single 64x64 avatar frame.
     *
     * @param avatarIndex 0-2
     * @param frameNum 0-14 for walk frames, -1 for idle
     * @param walking true for walk animation, false for idle
     */
    private static Bitmap generateFrame(int avatarIndex, int frameNum, boolean walking) {
        Bitmap bmp = Bitmap.createBitmap(SPRITE_SIZE, SPRITE_SIZE, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint();
        p.setAntiAlias(false);
        p.setFilterBitmap(false);

        int skinColor = SKIN_COLORS[avatarIndex];
        int bodyColor = BODY_COLORS[avatarIndex];
        int accentColor = ACCENT_COLORS[avatarIndex];
        int hairColor = HAIR_COLORS[avatarIndex];

        // Walk cycle parameters
        float walkPhase = walking ? (float)(frameNum * 2 * Math.PI / FRAME_COUNT) : 0;
        int bounce = walking ? (int)(Math.abs(Math.sin(walkPhase * 2)) * 2) : 0;
        int legSwing = walking ? (int)(Math.sin(walkPhase) * 4) : 0;
        int armSwing = walking ? (int)(Math.sin(walkPhase) * 3) : 0;

        // Center X, base Y (feet position), adjusted for bounce
        int cx = 32;
        int baseY = 58 - bounce;

        // --- Draw based on avatar type ---
        switch (avatarIndex) {
            case 0: drawKnight(c, p, cx, baseY, skinColor, bodyColor, accentColor, hairColor, legSwing, armSwing); break;
            case 1: drawMage(c, p, cx, baseY, skinColor, bodyColor, accentColor, hairColor, legSwing, armSwing); break;
            case 2: drawRogue(c, p, cx, baseY, skinColor, bodyColor, accentColor, hairColor, legSwing, armSwing); break;
        }

        return bmp;
    }

    /**
     * Knight: Broad shoulders, square stance, simple armor silhouette.
     */
    private static void drawKnight(Canvas c, Paint p, int cx, int baseY,
                                    int skin, int body, int accent, int hair,
                                    int legSwing, int armSwing) {
        // Legs
        p.setColor(Color.rgb(80, 70, 60)); // dark pants
        c.drawRect(cx - 5, baseY - 18, cx - 1, baseY - 4 + legSwing, p);  // left leg
        c.drawRect(cx + 1, baseY - 18, cx + 5, baseY - 4 - legSwing, p);  // right leg

        // Feet/boots
        p.setColor(Color.rgb(70, 50, 30));
        c.drawRect(cx - 6, baseY - 4 + legSwing, cx, baseY, p);  // left boot
        c.drawRect(cx, baseY - 4 - legSwing, cx + 6, baseY, p);   // right boot

        // Torso (armor)
        p.setColor(body);
        c.drawRect(cx - 8, baseY - 34, cx + 8, baseY - 18, p);

        // Armor accent lines
        p.setColor(accent);
        c.drawRect(cx - 8, baseY - 34, cx + 8, baseY - 32, p);  // top trim
        c.drawRect(cx - 8, baseY - 20, cx + 8, baseY - 18, p);  // bottom trim
        c.drawRect(cx - 1, baseY - 34, cx + 1, baseY - 18, p);  // center stripe

        // Arms
        p.setColor(body);
        c.drawRect(cx - 12, baseY - 32 - armSwing, cx - 8, baseY - 20, p);  // left arm
        c.drawRect(cx + 8, baseY - 32 + armSwing, cx + 12, baseY - 20, p);  // right arm

        // Hands
        p.setColor(skin);
        c.drawRect(cx - 12, baseY - 20, cx - 9, baseY - 17, p);  // left hand
        c.drawRect(cx + 9, baseY - 20, cx + 12, baseY - 17, p);  // right hand

        // Neck
        p.setColor(skin);
        c.drawRect(cx - 3, baseY - 38, cx + 3, baseY - 34, p);

        // Head
        p.setColor(skin);
        c.drawRect(cx - 6, baseY - 48, cx + 6, baseY - 36, p);

        // Eyes
        p.setColor(Color.rgb(40, 40, 40));
        c.drawRect(cx - 4, baseY - 44, cx - 2, baseY - 42, p);  // left eye
        c.drawRect(cx + 2, baseY - 44, cx + 4, baseY - 42, p);  // right eye

        // Hair
        p.setColor(hair);
        c.drawRect(cx - 7, baseY - 50, cx + 7, baseY - 46, p);  // top
        c.drawRect(cx - 7, baseY - 50, cx - 5, baseY - 40, p);  // left side
        c.drawRect(cx + 5, baseY - 50, cx + 7, baseY - 40, p);  // right side
    }

    /**
     * Mage: Flowing robes, pointed hat shape, thinner build.
     */
    private static void drawMage(Canvas c, Paint p, int cx, int baseY,
                                  int skin, int body, int accent, int hair,
                                  int legSwing, int armSwing) {
        // Robe bottom (covers legs, slight swing)
        p.setColor(body);
        c.drawRect(cx - 10, baseY - 22, cx + 10, baseY - 2, p);

        // Robe sway
        p.setColor(accent);
        c.drawRect(cx - 10 + legSwing / 2, baseY - 4, cx - 4 + legSwing / 2, baseY, p);
        c.drawRect(cx + 4 - legSwing / 2, baseY - 4, cx + 10 - legSwing / 2, baseY, p);

        // Feet peek out
        p.setColor(Color.rgb(100, 80, 60));
        c.drawRect(cx - 3, baseY - 2, cx + 3, baseY, p);

        // Torso (robe top)
        p.setColor(body);
        c.drawRect(cx - 8, baseY - 34, cx + 8, baseY - 22, p);

        // Belt
        p.setColor(Color.rgb(160, 130, 50)); // gold belt
        c.drawRect(cx - 8, baseY - 24, cx + 8, baseY - 22, p);

        // Sleeves (flowing)
        p.setColor(body);
        c.drawRect(cx - 14, baseY - 32 - armSwing, cx - 8, baseY - 22, p);  // left sleeve
        c.drawRect(cx + 8, baseY - 32 + armSwing, cx + 14, baseY - 22, p);  // right sleeve

        // Hands
        p.setColor(skin);
        c.drawRect(cx - 14, baseY - 22, cx - 11, baseY - 19, p);
        c.drawRect(cx + 11, baseY - 22, cx + 14, baseY - 19, p);

        // Neck
        p.setColor(skin);
        c.drawRect(cx - 3, baseY - 38, cx + 3, baseY - 34, p);

        // Head
        p.setColor(skin);
        c.drawRect(cx - 5, baseY - 48, cx + 5, baseY - 36, p);

        // Eyes
        p.setColor(Color.rgb(40, 40, 60));
        c.drawRect(cx - 3, baseY - 44, cx - 1, baseY - 42, p);
        c.drawRect(cx + 1, baseY - 44, cx + 3, baseY - 42, p);

        // Hair (long, silver)
        p.setColor(hair);
        c.drawRect(cx - 6, baseY - 50, cx + 6, baseY - 46, p);
        c.drawRect(cx - 6, baseY - 50, cx - 4, baseY - 36, p);
        c.drawRect(cx + 4, baseY - 50, cx + 6, baseY - 36, p);
        // Hair extends down back
        c.drawRect(cx + 4, baseY - 36, cx + 6, baseY - 28, p);
        c.drawRect(cx - 6, baseY - 36, cx - 4, baseY - 28, p);
    }

    /**
     * Rogue: Hooded, slim build, agile stance.
     */
    private static void drawRogue(Canvas c, Paint p, int cx, int baseY,
                                   int skin, int body, int accent, int hair,
                                   int legSwing, int armSwing) {
        // Legs (slim)
        p.setColor(Color.rgb(60, 50, 40)); // dark leather
        c.drawRect(cx - 4, baseY - 18, cx - 1, baseY - 4 + legSwing, p);
        c.drawRect(cx + 1, baseY - 18, cx + 4, baseY - 4 - legSwing, p);

        // Feet (light boots)
        p.setColor(Color.rgb(80, 60, 40));
        c.drawRect(cx - 5, baseY - 4 + legSwing, cx, baseY, p);
        c.drawRect(cx, baseY - 4 - legSwing, cx + 5, baseY, p);

        // Torso (leather vest)
        p.setColor(body);
        c.drawRect(cx - 7, baseY - 32, cx + 7, baseY - 18, p);

        // Belt with buckle
        p.setColor(Color.rgb(80, 60, 40));
        c.drawRect(cx - 7, baseY - 20, cx + 7, baseY - 18, p);
        p.setColor(Color.rgb(180, 160, 50));
        c.drawRect(cx - 1, baseY - 20, cx + 1, baseY - 18, p);  // buckle

        // Vest collar
        p.setColor(accent);
        c.drawRect(cx - 7, baseY - 32, cx + 7, baseY - 30, p);

        // Arms (slim)
        p.setColor(body);
        c.drawRect(cx - 10, baseY - 30 - armSwing, cx - 7, baseY - 20, p);
        c.drawRect(cx + 7, baseY - 30 + armSwing, cx + 10, baseY - 20, p);

        // Hands
        p.setColor(skin);
        c.drawRect(cx - 10, baseY - 20, cx - 8, baseY - 17, p);
        c.drawRect(cx + 8, baseY - 20, cx + 10, baseY - 17, p);

        // Neck
        p.setColor(skin);
        c.drawRect(cx - 2, baseY - 36, cx + 2, baseY - 32, p);

        // Head
        p.setColor(skin);
        c.drawRect(cx - 5, baseY - 46, cx + 5, baseY - 35, p);

        // Eyes (sharper looking)
        p.setColor(Color.rgb(30, 30, 30));
        c.drawRect(cx - 3, baseY - 42, cx - 1, baseY - 41, p);
        c.drawRect(cx + 1, baseY - 42, cx + 3, baseY - 41, p);

        // Hood
        p.setColor(body);
        c.drawRect(cx - 7, baseY - 50, cx + 7, baseY - 44, p);  // hood top
        c.drawRect(cx - 7, baseY - 50, cx - 5, baseY - 38, p);  // hood left
        c.drawRect(cx + 5, baseY - 50, cx + 7, baseY - 38, p);  // hood right

        // Hood point
        p.setColor(accent);
        c.drawRect(cx - 1, baseY - 52, cx + 1, baseY - 50, p);
    }
}
