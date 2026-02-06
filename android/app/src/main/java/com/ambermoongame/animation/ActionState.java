package com.ambermoongame.animation;

/**
 * Animation action states for SpriteAnimation.
 * Using int constants instead of enum to avoid D8 compiler bugs.
 */
public final class ActionState {
    public static final int IDLE = 0;
    public static final int WALK = 1;
    public static final int RUN = 2;
    public static final int SPRINT = 3;
    public static final int JUMP = 4;
    public static final int DOUBLE_JUMP = 5;
    public static final int TRIPLE_JUMP = 6;
    public static final int FALL = 7;
    public static final int ATTACK = 8;
    public static final int FIRE = 9;
    public static final int USE_ITEM = 10;
    public static final int EAT = 11;
    public static final int HURT = 12;
    public static final int DEAD = 13;
    public static final int BLOCK = 14;
    public static final int CAST = 15;
    public static final int BURNING = 16;
    public static final int FROZEN = 17;
    public static final int POISONED = 18;

    public static final int COUNT = 19;

    private ActionState() {}

    public static String getName(int state) {
        switch (state) {
            case IDLE: return "IDLE";
            case WALK: return "WALK";
            case RUN: return "RUN";
            case SPRINT: return "SPRINT";
            case JUMP: return "JUMP";
            case DOUBLE_JUMP: return "DOUBLE_JUMP";
            case TRIPLE_JUMP: return "TRIPLE_JUMP";
            case FALL: return "FALL";
            case ATTACK: return "ATTACK";
            case FIRE: return "FIRE";
            case USE_ITEM: return "USE_ITEM";
            case EAT: return "EAT";
            case HURT: return "HURT";
            case DEAD: return "DEAD";
            case BLOCK: return "BLOCK";
            case CAST: return "CAST";
            case BURNING: return "BURNING";
            case FROZEN: return "FROZEN";
            case POISONED: return "POISONED";
            default: return "UNKNOWN";
        }
    }
}
