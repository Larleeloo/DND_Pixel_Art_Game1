package com.ambermoongame.entity;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.ambermoongame.input.TouchInputManager;

/**
 * ProjectileEntity - Stub class providing constants for item classes.
 * Uses int constants instead of enums to avoid D8 compiler crash.
 *
 * This is a minimal stub to allow item classes (ammo, ranged weapons, throwables)
 * to compile. The full ProjectileEntity implementation with physics, rendering,
 * and collision detection will be added when the projectile system is ported.
 *
 * Desktop equivalent: entity/ProjectileEntity.java
 */
public class ProjectileEntity extends Entity {

    // Projectile type constants (replaces enum)
    public static final int PROJECTILE_ARROW = 0;
    public static final int PROJECTILE_BOLT = 1;
    public static final int PROJECTILE_MAGIC_BOLT = 2;
    public static final int PROJECTILE_FIREBALL = 3;
    public static final int PROJECTILE_ICEBALL = 4;
    public static final int PROJECTILE_THROWING_KNIFE = 5;
    public static final int PROJECTILE_THROWING_AXE = 6;
    public static final int PROJECTILE_ROCK = 7;
    public static final int PROJECTILE_POTION = 8;
    public static final int PROJECTILE_BOMB = 9;
    public static final int PROJECTILE_FISH = 10;
    public static final int PROJECTILE_COUNT = 11;

    // Status effect type constants (replaces enum)
    public static final int EFFECT_NONE = 0;
    public static final int EFFECT_BURNING = 1;
    public static final int EFFECT_FROZEN = 2;
    public static final int EFFECT_POISONED = 3;
    public static final int EFFECT_COUNT = 4;

    public static String getProjectileTypeName(int type) {
        switch (type) {
            case PROJECTILE_ARROW: return "ARROW";
            case PROJECTILE_BOLT: return "BOLT";
            case PROJECTILE_MAGIC_BOLT: return "MAGIC_BOLT";
            case PROJECTILE_FIREBALL: return "FIREBALL";
            case PROJECTILE_ICEBALL: return "ICEBALL";
            case PROJECTILE_THROWING_KNIFE: return "THROWING_KNIFE";
            case PROJECTILE_THROWING_AXE: return "THROWING_AXE";
            case PROJECTILE_ROCK: return "ROCK";
            case PROJECTILE_POTION: return "POTION";
            case PROJECTILE_BOMB: return "BOMB";
            case PROJECTILE_FISH: return "FISH";
            default: return "UNKNOWN";
        }
    }

    public static int projectileTypeFromName(String name) {
        if (name == null) return PROJECTILE_ARROW;
        switch (name.toUpperCase()) {
            case "ARROW": return PROJECTILE_ARROW;
            case "BOLT": return PROJECTILE_BOLT;
            case "MAGIC_BOLT": return PROJECTILE_MAGIC_BOLT;
            case "FIREBALL": return PROJECTILE_FIREBALL;
            case "ICEBALL": return PROJECTILE_ICEBALL;
            case "THROWING_KNIFE": return PROJECTILE_THROWING_KNIFE;
            case "THROWING_AXE": return PROJECTILE_THROWING_AXE;
            case "ROCK": return PROJECTILE_ROCK;
            case "POTION": return PROJECTILE_POTION;
            case "BOMB": return PROJECTILE_BOMB;
            case "FISH": return PROJECTILE_FISH;
            default: return PROJECTILE_ARROW;
        }
    }

    public static String getStatusEffectName(int effect) {
        switch (effect) {
            case EFFECT_NONE: return "NONE";
            case EFFECT_BURNING: return "BURNING";
            case EFFECT_FROZEN: return "FROZEN";
            case EFFECT_POISONED: return "POISONED";
            default: return "UNKNOWN";
        }
    }

    public static int statusEffectFromName(String name) {
        if (name == null) return EFFECT_NONE;
        switch (name.toUpperCase()) {
            case "NONE": return EFFECT_NONE;
            case "BURNING": return EFFECT_BURNING;
            case "FROZEN": return EFFECT_FROZEN;
            case "POISONED": return EFFECT_POISONED;
            default: return EFFECT_NONE;
        }
    }

    // Minimal fields for stub
    private int type = PROJECTILE_ARROW;
    private boolean active = true;
    private boolean fromPlayer = true;
    private int damage = 0;

    public ProjectileEntity(int x, int y) {
        super(x, y);
    }

    public ProjectileEntity(int x, int y, String spritePath, int damage,
                           double velX, double velY, boolean fromPlayer) {
        super(x, y);
        this.damage = damage;
        this.fromPlayer = fromPlayer;
    }

    // --- Stub methods - implement when full port is done ---

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    @Override
    public void update(TouchInputManager input) {
        // Stub - full implementation pending
    }

    @Override
    public void draw(Canvas canvas) {
        // Stub - full implementation pending
    }

    @Override
    public Rect getBounds() {
        return new Rect(x, y, x + 16, y + 16);
    }
}
