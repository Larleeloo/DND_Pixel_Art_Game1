package animation;

/**
 * Defines animation states for items that can be triggered by entity actions.
 * Items have their own animation cycles that play alongside entity animations.
 *
 * Animation files should be organized in folders:
 *   assets/items/{item_name}/{state_name}.gif
 *
 * Examples:
 *   - assets/items/bow/idle.gif
 *   - assets/items/bow/draw.gif
 *   - assets/items/bow/fire.gif
 *   - assets/items/fire_staff/idle.gif
 *   - assets/items/fire_staff/charge.gif
 *   - assets/items/fire_staff/cast.gif
 */
public enum ItemAnimationState {

    // ==================== Common States ====================

    /**
     * Default state when item is held but not in use.
     * File: idle.gif
     */
    IDLE("idle"),

    /**
     * Item is being actively used/swung.
     * File: use.gif
     */
    USE("use"),

    /**
     * Item is being broken or destroyed.
     * File: break.gif
     */
    BREAK("break"),

    // ==================== Weapon States ====================

    /**
     * Melee weapon attack swing animation.
     * File: attack.gif
     */
    ATTACK("attack"),

    /**
     * Weapon blocking/parrying animation.
     * File: block.gif
     */
    BLOCK("block"),

    /**
     * Weapon critical hit or special attack.
     * File: critical.gif
     */
    CRITICAL("critical"),

    // ==================== Ranged Weapon States ====================

    /**
     * Bow drawing back animation (charging).
     * File: draw.gif
     */
    DRAW("draw"),

    /**
     * Bow/crossbow firing animation.
     * File: fire.gif
     */
    FIRE("fire"),

    /**
     * Crossbow reload animation.
     * File: reload.gif
     */
    RELOAD("reload"),

    // ==================== Magic Weapon States ====================

    /**
     * Magic staff/wand charging animation.
     * File: charge.gif
     */
    CHARGE("charge"),

    /**
     * Magic staff/wand casting animation.
     * File: cast.gif
     */
    CAST("cast"),

    /**
     * Magic effect glow/pulse animation.
     * File: glow.gif
     */
    GLOW("glow"),

    /**
     * Channeling magic continuously.
     * File: channel.gif
     */
    CHANNEL("channel"),

    // ==================== Tool States ====================

    /**
     * Tool mining/digging animation.
     * File: mine.gif
     */
    MINE("mine"),

    /**
     * Tool chopping animation.
     * File: chop.gif
     */
    CHOP("chop"),

    /**
     * Tool impact animation when hitting.
     * File: impact.gif
     */
    IMPACT("impact"),

    // ==================== Special States ====================

    /**
     * Item lighting up or activating.
     * File: activate.gif
     */
    ACTIVATE("activate"),

    /**
     * Item powering down or deactivating.
     * File: deactivate.gif
     */
    DEACTIVATE("deactivate"),

    /**
     * Special triggered effect (entity-specific).
     * File: special.gif
     */
    SPECIAL("special"),

    /**
     * Item enchantment or magical effect overlay.
     * File: enchant.gif
     */
    ENCHANT("enchant"),

    /**
     * Item on fire or burning effect.
     * File: burning.gif
     */
    BURNING("burning"),

    /**
     * Item frozen or ice effect.
     * File: frozen.gif
     */
    FROZEN("frozen"),

    /**
     * Item electrified or lightning effect.
     * File: electric.gif
     */
    ELECTRIC("electric");

    private final String fileName;

    ItemAnimationState(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the expected file name for this animation state.
     * @return File name without extension (e.g., "idle", "draw")
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the full file path for this animation state.
     * @param itemFolderPath Path to the item's animation folder
     * @return Full path to the GIF file
     */
    public String getFilePath(String itemFolderPath) {
        String basePath = itemFolderPath.endsWith("/") ? itemFolderPath : itemFolderPath + "/";
        return basePath + fileName + ".gif";
    }

    /**
     * Maps an entity ActionState to the corresponding ItemAnimationState.
     * @param actionState The entity's current action state
     * @return The corresponding item animation state
     */
    public static ItemAnimationState fromEntityAction(SpriteAnimation.ActionState actionState) {
        if (actionState == null) return IDLE;

        switch (actionState) {
            case IDLE:
            case WALK:
            case RUN:
            case SPRINT:
                return IDLE;
            case ATTACK:
                return ATTACK;
            case FIRE:
                return FIRE;
            case CAST:
                return CAST;
            case BLOCK:
                return BLOCK;
            case USE_ITEM:
                return USE;
            case EAT:
                return USE;
            default:
                return IDLE;
        }
    }

    /**
     * Gets a charging state appropriate for the weapon type.
     * @param isRanged Whether the weapon is ranged
     * @param isMagic Whether the weapon is magic-based
     * @return The appropriate charging animation state
     */
    public static ItemAnimationState getChargingState(boolean isRanged, boolean isMagic) {
        if (isMagic) {
            return CHARGE;
        } else if (isRanged) {
            return DRAW;
        }
        return USE;
    }

    /**
     * Gets a firing/release state appropriate for the weapon type.
     * @param isRanged Whether the weapon is ranged
     * @param isMagic Whether the weapon is magic-based
     * @return The appropriate firing animation state
     */
    public static ItemAnimationState getFiringState(boolean isRanged, boolean isMagic) {
        if (isMagic) {
            return CAST;
        } else if (isRanged) {
            return FIRE;
        }
        return ATTACK;
    }
}
