package com.ambermoongame.entity;

import android.graphics.Canvas;
import android.graphics.Rect;

import com.ambermoongame.input.TouchInputManager;

/**
 * ProjectileEntity - Stub class providing enums for item classes.
 *
 * This is a minimal stub to allow item classes (ammo, ranged weapons, throwables)
 * to compile. The full ProjectileEntity implementation with physics, rendering,
 * and collision detection will be added when the projectile system is ported.
 *
 * Desktop equivalent: entity/ProjectileEntity.java
 */
public class ProjectileEntity extends Entity {

    // Projectile types used by ranged weapons and ammo
    public enum ProjectileType {
        ARROW,          // Arrows from bows
        BOLT,           // Crossbow bolts
        MAGIC_BOLT,     // Magic projectiles
        FIREBALL,       // Fire spell
        ICEBALL,        // Ice spell
        THROWING_KNIFE, // Thrown weapon
        THROWING_AXE,   // Thrown axe
        ROCK,           // Thrown rock
        POTION,         // Thrown potion
        BOMB,           // Explosive
        FISH            // Tiny fish projectile (from Mirror to Other Realms)
    }

    // Status effect types for special projectiles
    public enum StatusEffectType {
        NONE,
        BURNING,
        FROZEN,
        POISONED
    }

    // Minimal fields for stub
    private ProjectileType type = ProjectileType.ARROW;
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

    public void setType(ProjectileType type) {
        this.type = type;
    }

    public ProjectileType getType() {
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
