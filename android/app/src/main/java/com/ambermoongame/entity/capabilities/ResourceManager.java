package com.ambermoongame.entity.capabilities;

/**
 * Interface for entities that manage resources like mana, stamina, and health.
 * Provides a unified contract for resource management that can be implemented by
 * players, companions, bosses, or any entity that uses resource-based abilities.
 * Equivalent to entity/capabilities/ResourceManager.java from the desktop version.
 *
 * Conversion notes:
 * - No AWT dependencies - direct port with only package change.
 */
public interface ResourceManager {

    // ==================== Mana System ====================

    /**
     * Gets the current mana value.
     */
    int getMana();

    /**
     * Gets the maximum mana value.
     */
    int getMaxMana();

    /**
     * Sets the current mana value.
     * @param mana New mana value (will be clamped to 0-maxMana)
     */
    void setMana(int mana);

    /**
     * Uses mana for an action.
     * @param amount Amount of mana to use
     * @return true if enough mana was available and was consumed
     */
    default boolean useMana(int amount) {
        if (getMana() >= amount) {
            setMana(getMana() - amount);
            return true;
        }
        return false;
    }

    /**
     * Restores mana.
     */
    default void restoreMana(int amount) {
        setMana(Math.min(getMaxMana(), getMana() + amount));
    }

    /**
     * Checks if the entity has enough mana for an action.
     */
    default boolean hasMana(int amount) {
        return getMana() >= amount;
    }

    /**
     * Gets the mana regeneration rate.
     * @return Mana restored per second
     */
    double getManaRegenRate();

    /**
     * Sets the mana regeneration rate.
     * @param rate Mana to restore per second
     */
    void setManaRegenRate(double rate);

    // ==================== Stamina System ====================

    /**
     * Gets the current stamina value.
     */
    int getStamina();

    /**
     * Gets the maximum stamina value.
     */
    int getMaxStamina();

    /**
     * Sets the current stamina value.
     * @param stamina New stamina value (will be clamped to 0-maxStamina)
     */
    void setStamina(int stamina);

    /**
     * Uses stamina for an action.
     * @param amount Amount of stamina to use
     * @return true if enough stamina was available and was consumed
     */
    default boolean useStamina(int amount) {
        if (getStamina() >= amount) {
            setStamina(getStamina() - amount);
            return true;
        }
        return false;
    }

    /**
     * Restores stamina.
     */
    default void restoreStamina(int amount) {
        setStamina(Math.min(getMaxStamina(), getStamina() + amount));
    }

    /**
     * Checks if the entity has enough stamina for an action.
     */
    default boolean hasStamina(int amount) {
        return getStamina() >= amount;
    }

    /**
     * Gets the stamina regeneration rate.
     * @return Stamina restored per second
     */
    double getStaminaRegenRate();

    /**
     * Sets the stamina regeneration rate.
     * @param rate Stamina to restore per second
     */
    void setStaminaRegenRate(double rate);

    /**
     * Gets the stamina drain rate (for sprinting, etc.).
     * @return Stamina drained per second during continuous use
     */
    double getStaminaDrainRate();

    /**
     * Sets the stamina drain rate.
     * @param rate Stamina to drain per second
     */
    void setStaminaDrainRate(double rate);

    // ==================== Health System ====================

    /**
     * Gets the current health value.
     */
    int getHealth();

    /**
     * Gets the maximum health value.
     */
    int getMaxHealth();

    /**
     * Sets the current health value.
     * @param health New health value (will be clamped to 0-maxHealth)
     */
    void setHealth(int health);

    /**
     * Heals the entity.
     */
    default void heal(int amount) {
        setHealth(Math.min(getMaxHealth(), getHealth() + amount));
    }

    /**
     * Checks if the entity is alive.
     */
    default boolean isAlive() {
        return getHealth() > 0;
    }

    /**
     * Checks if the entity is at full health.
     */
    default boolean isFullHealth() {
        return getHealth() >= getMaxHealth();
    }

    // ==================== Resource Regeneration ====================

    /**
     * Updates resource regeneration based on elapsed time.
     * Should be called each update frame.
     * @param deltaSeconds Time elapsed since last update
     */
    void updateResourceRegeneration(double deltaSeconds);

    /**
     * Gets the current resource percentages for UI display.
     * @return Array of [healthPercent, manaPercent, staminaPercent] (0.0 to 1.0)
     */
    default double[] getResourcePercentages() {
        return new double[] {
            (double) getHealth() / getMaxHealth(),
            (double) getMana() / getMaxMana(),
            (double) getStamina() / getMaxStamina()
        };
    }
}
