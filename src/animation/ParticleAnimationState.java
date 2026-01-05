package animation;

import java.awt.Color;

/**
 * Defines animation states for particles that can be triggered by entity actions.
 * Particles are visual effects that play alongside entity and item animations.
 *
 * Animation files should be organized in folders:
 *   assets/particles/{particle_type}/{state_name}.gif
 *
 * Examples:
 *   - assets/particles/sprint/air_lines.gif
 *   - assets/particles/fire/flames.gif
 *   - assets/particles/magic/sparkle.gif
 *   - assets/particles/impact/dust.gif
 */
public enum ParticleAnimationState {

    // ==================== Movement Particles ====================

    /**
     * Air lines effect when sprinting.
     * Shows horizontal speed lines behind the entity.
     * File: air_lines.gif
     */
    SPRINT_LINES("sprint", "air_lines", new Color(200, 200, 255, 150)),

    /**
     * Dust cloud when running.
     * Small puffs of dust at feet.
     * File: dust.gif
     */
    RUN_DUST("dust", "run_dust", new Color(150, 120, 80, 180)),

    /**
     * Jump burst effect.
     * Circular burst when jumping.
     * File: jump.gif
     */
    JUMP_BURST("dust", "jump", new Color(255, 255, 255, 150)),

    /**
     * Landing impact dust.
     * Dust cloud when landing from a fall.
     * File: land.gif
     */
    LAND_DUST("dust", "land", new Color(150, 120, 80, 200)),

    /**
     * Double/triple jump magic effect.
     * Mystical rings or wings.
     * File: multi_jump.gif
     */
    MULTI_JUMP("magic", "multi_jump", new Color(100, 200, 255, 180)),

    /**
     * Dash trail effect.
     * Afterimage when dashing.
     * File: dash.gif
     */
    DASH_TRAIL("sprint", "dash", new Color(100, 150, 255, 120)),

    // ==================== Combat Particles ====================

    /**
     * Melee attack slash effect.
     * Arc or slash lines during attack.
     * File: slash.gif
     */
    ATTACK_SLASH("impact", "slash", new Color(255, 255, 255, 200)),

    /**
     * Hit impact sparks.
     * Sparks when hitting an enemy.
     * File: hit.gif
     */
    HIT_SPARKS("impact", "hit", new Color(255, 200, 50, 220)),

    /**
     * Critical hit effect.
     * Dramatic effect for critical hits.
     * File: critical.gif
     */
    CRITICAL_HIT("impact", "critical", new Color(255, 100, 50, 255)),

    /**
     * Block/parry effect.
     * Shield or weapon block sparks.
     * File: block.gif
     */
    BLOCK_SPARKS("impact", "block", new Color(150, 200, 255, 200)),

    // ==================== Status Effect Particles ====================

    /**
     * Burning flames effect.
     * Rising flames and sparks.
     * File: burning.gif
     */
    BURNING("fire", "burning", new Color(255, 100, 0, 200)),

    /**
     * Fire trail effect.
     * Flames left behind when moving while on fire.
     * File: fire_trail.gif
     */
    FIRE_TRAIL("fire", "fire_trail", new Color(255, 150, 0, 150)),

    /**
     * Frozen ice crystals.
     * Falling snowflakes and ice shards.
     * File: frozen.gif
     */
    FROZEN("ice", "frozen", new Color(150, 200, 255, 200)),

    /**
     * Ice shatter effect.
     * Breaking ice when unfreezing.
     * File: ice_shatter.gif
     */
    ICE_SHATTER("ice", "ice_shatter", new Color(200, 230, 255, 220)),

    /**
     * Poison bubbles.
     * Rising toxic bubbles.
     * File: poison.gif
     */
    POISONED("poison", "poison", new Color(100, 200, 50, 200)),

    /**
     * Poison drip effect.
     * Dripping poison drops.
     * File: poison_drip.gif
     */
    POISON_DRIP("poison", "poison_drip", new Color(80, 180, 40, 180)),

    /**
     * Electric shock effect.
     * Lightning bolts and sparks.
     * File: shock.gif
     */
    SHOCKED("magic", "shock", new Color(255, 255, 100, 220)),

    // ==================== Magic Particles ====================

    /**
     * Magic charge effect.
     * Gathering magical energy.
     * File: charge.gif
     */
    MAGIC_CHARGE("magic", "charge", new Color(150, 100, 255, 180)),

    /**
     * Magic cast effect.
     * Release of magical energy.
     * File: cast.gif
     */
    MAGIC_CAST("magic", "cast", new Color(100, 150, 255, 200)),

    /**
     * Magic sparkle effect.
     * Ambient magical particles.
     * File: sparkle.gif
     */
    MAGIC_SPARKLE("magic", "sparkle", new Color(200, 180, 255, 150)),

    /**
     * Healing effect.
     * Rising green/gold particles.
     * File: heal.gif
     */
    HEAL("magic", "heal", new Color(100, 255, 150, 200)),

    /**
     * Buff/power-up effect.
     * Swirling energy around entity.
     * File: buff.gif
     */
    BUFF("magic", "buff", new Color(255, 200, 100, 180)),

    /**
     * Dark/shadow magic effect.
     * Dark wisps and shadows.
     * File: dark.gif
     */
    DARK_MAGIC("magic", "dark", new Color(80, 0, 120, 200)),

    // ==================== Item Interaction Particles ====================

    /**
     * Item pickup sparkle.
     * Effect when collecting an item.
     * File: pickup.gif
     */
    ITEM_PICKUP("magic", "pickup", new Color(255, 255, 200, 200)),

    /**
     * Item glow effect.
     * Magical item glowing.
     * File: item_glow.gif
     */
    ITEM_GLOW("magic", "item_glow", new Color(255, 230, 150, 150)),

    /**
     * Item break effect.
     * When an item breaks/shatters.
     * File: item_break.gif
     */
    ITEM_BREAK("impact", "item_break", new Color(200, 150, 100, 220)),

    // ==================== Environmental Particles ====================

    /**
     * Smoke effect.
     * Rising smoke.
     * File: smoke.gif
     */
    SMOKE("dust", "smoke", new Color(100, 100, 100, 180)),

    /**
     * Steam effect.
     * Rising steam/vapor.
     * File: steam.gif
     */
    STEAM("dust", "steam", new Color(200, 200, 200, 120)),

    /**
     * Water splash.
     * Splashing water drops.
     * File: splash.gif
     */
    WATER_SPLASH("dust", "splash", new Color(100, 150, 255, 180)),

    /**
     * Leaves rustling.
     * Falling leaves effect.
     * File: leaves.gif
     */
    LEAVES("dust", "leaves", new Color(100, 180, 50, 180));

    private final String folder;
    private final String fileName;
    private final Color defaultTint;

    ParticleAnimationState(String folder, String fileName, Color defaultTint) {
        this.folder = folder;
        this.fileName = fileName;
        this.defaultTint = defaultTint;
    }

    /**
     * Gets the folder name for this particle type.
     * @return Folder name (e.g., "sprint", "fire")
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Gets the expected file name for this animation state.
     * @return File name without extension (e.g., "air_lines", "burning")
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the default tint color for this particle.
     * @return Default color with alpha
     */
    public Color getDefaultTint() {
        return defaultTint;
    }

    /**
     * Gets the full file path for this animation state.
     * @param basePath Base path to the particles folder (e.g., "assets/particles")
     * @return Full path to the GIF file
     */
    public String getFilePath(String basePath) {
        String path = basePath.endsWith("/") ? basePath : basePath + "/";
        return path + folder + "/" + fileName + ".gif";
    }

    /**
     * Gets the default file path using standard assets location.
     * @return Full path to the GIF file
     */
    public String getDefaultFilePath() {
        return getFilePath("assets/particles");
    }

    /**
     * Gets particles appropriate for sprinting based on speed.
     * @param speed Current movement speed
     * @return Array of applicable particle states
     */
    public static ParticleAnimationState[] getSprintParticles(double speed) {
        if (speed > 6) {
            return new ParticleAnimationState[] { SPRINT_LINES, RUN_DUST };
        } else if (speed > 4) {
            return new ParticleAnimationState[] { RUN_DUST };
        }
        return new ParticleAnimationState[0];
    }

    /**
     * Gets particles appropriate for jumping based on jump number.
     * @param jumpNumber Current jump (1 = first, 2 = double, 3 = triple)
     * @return Array of applicable particle states
     */
    public static ParticleAnimationState[] getJumpParticles(int jumpNumber) {
        if (jumpNumber >= 2) {
            return new ParticleAnimationState[] { JUMP_BURST, MULTI_JUMP };
        } else if (jumpNumber == 1) {
            return new ParticleAnimationState[] { JUMP_BURST };
        }
        return new ParticleAnimationState[0];
    }

    /**
     * Gets particles for a status effect.
     * @param effectType Status effect type name (BURNING, FROZEN, POISONED)
     * @return The corresponding particle state
     */
    public static ParticleAnimationState getStatusParticle(String effectType) {
        if (effectType == null) return null;

        switch (effectType.toUpperCase()) {
            case "BURNING":
                return BURNING;
            case "FROZEN":
                return FROZEN;
            case "POISONED":
                return POISONED;
            case "SHOCKED":
            case "ELECTRIC":
                return SHOCKED;
            default:
                return null;
        }
    }

    /**
     * Gets particles for combat actions.
     * @param isCritical Whether the hit was critical
     * @param isBlocking Whether blocking
     * @return Array of applicable particle states
     */
    public static ParticleAnimationState[] getCombatParticles(boolean isCritical, boolean isBlocking) {
        if (isBlocking) {
            return new ParticleAnimationState[] { BLOCK_SPARKS };
        } else if (isCritical) {
            return new ParticleAnimationState[] { CRITICAL_HIT, HIT_SPARKS };
        } else {
            return new ParticleAnimationState[] { HIT_SPARKS };
        }
    }
}
