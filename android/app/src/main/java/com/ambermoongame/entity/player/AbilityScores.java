package com.ambermoongame.entity.player;

/**
 * Represents the DnD-style ability scores for a character.
 * Each ability affects different aspects of gameplay:
 *
 * - Charisma: Affects cutscenes and dialogue options (not yet implemented)
 * - Strength: Melee weapon damage (+/-17% per point), backpack capacity (+/-20% per point)
 * - Constitution: HP (+/-10%), status effect resistance (+/-10%), carrying capacity (+/-5%)
 * - Intelligence: Magical item damage (+/-15% per point)
 * - Wisdom: Locks ancient artifacts from being used unless wisdom is high enough
 * - Dexterity: Item usage chance - above 5 gives +10% chance for double use without cost,
 *              below 5 gives -20% chance to miss but still cost resources
 *
 * Baseline for all abilities is 5.
 *
 * Conversion notes:
 * - No AWT dependencies - direct port with package change only.
 */
public class AbilityScores {

    public static final int BASELINE = 5;

    // The six ability scores
    private int charisma;
    private int strength;
    private int constitution;
    private int intelligence;
    private int wisdom;
    private int dexterity;

    /**
     * Creates ability scores with all baseline values (5).
     */
    public AbilityScores() {
        this.charisma = BASELINE;
        this.strength = BASELINE;
        this.constitution = BASELINE;
        this.intelligence = BASELINE;
        this.wisdom = BASELINE;
        this.dexterity = BASELINE;
    }

    /**
     * Creates ability scores with specified values.
     */
    public AbilityScores(int charisma, int strength, int constitution,
                         int intelligence, int wisdom, int dexterity) {
        this.charisma = charisma;
        this.strength = strength;
        this.constitution = constitution;
        this.intelligence = intelligence;
        this.wisdom = wisdom;
        this.dexterity = dexterity;
    }

    /**
     * Creates a copy of the given ability scores.
     */
    public AbilityScores(AbilityScores other) {
        this.charisma = other.charisma;
        this.strength = other.strength;
        this.constitution = other.constitution;
        this.intelligence = other.intelligence;
        this.wisdom = other.wisdom;
        this.dexterity = other.dexterity;
    }

    // === GETTERS ===

    public int getCharisma() { return charisma; }
    public int getStrength() { return strength; }
    public int getConstitution() { return constitution; }
    public int getIntelligence() { return intelligence; }
    public int getWisdom() { return wisdom; }
    public int getDexterity() { return dexterity; }

    // === SETTERS ===

    public void setCharisma(int charisma) { this.charisma = Math.max(1, charisma); }
    public void setStrength(int strength) { this.strength = Math.max(1, strength); }
    public void setConstitution(int constitution) { this.constitution = Math.max(1, constitution); }
    public void setIntelligence(int intelligence) { this.intelligence = Math.max(1, intelligence); }
    public void setWisdom(int wisdom) { this.wisdom = Math.max(1, wisdom); }
    public void setDexterity(int dexterity) { this.dexterity = Math.max(1, dexterity); }

    // === MODIFIER CALCULATIONS ===

    /**
     * Gets the strength modifier for melee damage.
     * +/-17% per point above/below baseline (5).
     * @return Multiplier (e.g., 1.17 for strength 6, 0.83 for strength 4)
     */
    public double getMeleeDamageModifier() {
        int diff = strength - BASELINE;
        return 1.0 + (diff * 0.17);
    }

    /**
     * Gets the strength modifier for backpack carrying capacity.
     * +/-20% per point above/below baseline (5).
     * @return Multiplier for capacity
     */
    public double getStrengthCarryingCapacityModifier() {
        int diff = strength - BASELINE;
        return 1.0 + (diff * 0.20);
    }

    /**
     * Gets the constitution modifier for max HP.
     * +/-10% per point above/below baseline (5).
     * @return Multiplier for max health
     */
    public double getHealthModifier() {
        int diff = constitution - BASELINE;
        return 1.0 + (diff * 0.10);
    }

    /**
     * Gets the constitution modifier for status effect resistance.
     * +/-10% per point above/below baseline (5).
     * Higher values mean shorter status effect duration.
     * @return Multiplier for status effect duration (lower is better)
     */
    public double getStatusEffectResistanceModifier() {
        int diff = constitution - BASELINE;
        // Inverted: higher constitution = shorter duration
        return 1.0 - (diff * 0.10);
    }

    /**
     * Gets the constitution modifier for carrying capacity.
     * +/-5% per point above/below baseline (5).
     * @return Multiplier for carrying capacity
     */
    public double getConstitutionCarryingCapacityModifier() {
        int diff = constitution - BASELINE;
        return 1.0 + (diff * 0.05);
    }

    /**
     * Gets the total carrying capacity modifier (strength + constitution).
     * @return Combined multiplier for carrying capacity
     */
    public double getTotalCarryingCapacityModifier() {
        return getStrengthCarryingCapacityModifier() * getConstitutionCarryingCapacityModifier();
    }

    /**
     * Gets the intelligence modifier for magical item damage.
     * +/-15% per point above/below baseline (5).
     * @return Multiplier for magical damage
     */
    public double getMagicalDamageModifier() {
        int diff = intelligence - BASELINE;
        return 1.0 + (diff * 0.15);
    }

    /**
     * Gets the minimum wisdom required to use an ancient artifact.
     * @param requiredWisdom The wisdom requirement of the artifact
     * @return true if wisdom is high enough to use the artifact
     */
    public boolean canUseAncientArtifact(int requiredWisdom) {
        return wisdom >= requiredWisdom;
    }

    /**
     * Calculates the dexterity effect on item usage.
     * At baseline (5): 100% normal usage
     * Above baseline: +10% chance per point to use item twice without consuming resources
     * Below baseline: -20% chance per point to miss usage but still consume resources
     * @return DexterityResult with the usage outcome
     */
    public DexterityResult calculateDexterityEffect() {
        int diff = dexterity - BASELINE;
        double roll = Math.random() * 100;

        if (diff > 0) {
            // Above baseline: chance for double usage without extra cost
            double doubleChance = diff * 10.0; // +10% per point above baseline
            if (roll < doubleChance) {
                return new DexterityResult(true, false, true); // success, no extra cost, double use
            }
            return new DexterityResult(true, true, false); // normal success
        } else if (diff < 0) {
            // Below baseline: chance to miss but still consume
            double missChance = Math.abs(diff) * 20.0; // -20% per point below baseline
            if (roll < missChance) {
                return new DexterityResult(false, true, false); // miss, still costs resources
            }
            return new DexterityResult(true, true, false); // normal success
        }

        // At baseline: normal usage
        return new DexterityResult(true, true, false);
    }

    /**
     * Gets the chance for an item to be used twice without extra cost (dexterity above baseline).
     * @return Percentage chance (0-100+)
     */
    public double getDoubleUseChance() {
        int diff = dexterity - BASELINE;
        if (diff > 0) {
            return diff * 10.0;
        }
        return 0.0;
    }

    /**
     * Gets the chance to miss item usage but still consume resources (dexterity below baseline).
     * @return Percentage chance (0-100+)
     */
    public double getMissChance() {
        int diff = dexterity - BASELINE;
        if (diff < 0) {
            return Math.abs(diff) * 20.0;
        }
        return 0.0;
    }

    /**
     * Increases an ability score by a specified amount.
     * Used for character progression.
     */
    public void increaseAbility(AbilityType type, int amount) {
        switch (type) {
            case CHARISMA: charisma += amount; break;
            case STRENGTH: strength += amount; break;
            case CONSTITUTION: constitution += amount; break;
            case INTELLIGENCE: intelligence += amount; break;
            case WISDOM: wisdom += amount; break;
            case DEXTERITY: dexterity += amount; break;
        }
    }

    /**
     * Gets the total of all ability scores.
     */
    public int getTotal() {
        return charisma + strength + constitution + intelligence + wisdom + dexterity;
    }

    @Override
    public String toString() {
        return String.format("AbilityScores[CHA:%d, STR:%d, CON:%d, INT:%d, WIS:%d, DEX:%d]",
            charisma, strength, constitution, intelligence, wisdom, dexterity);
    }

    /**
     * Enum for ability types, used for progression.
     */
    public enum AbilityType {
        CHARISMA("Charisma", "CHA"),
        STRENGTH("Strength", "STR"),
        CONSTITUTION("Constitution", "CON"),
        INTELLIGENCE("Intelligence", "INT"),
        WISDOM("Wisdom", "WIS"),
        DEXTERITY("Dexterity", "DEX");

        private final String displayName;
        private final String shortName;

        AbilityType(String displayName, String shortName) {
            this.displayName = displayName;
            this.shortName = shortName;
        }

        public String getDisplayName() { return displayName; }
        public String getShortName() { return shortName; }
    }

    /**
     * Result class for dexterity-based item usage.
     */
    public static class DexterityResult {
        private final boolean success;
        private final boolean consumesResources;
        private final boolean doubleUse;

        public DexterityResult(boolean success, boolean consumesResources, boolean doubleUse) {
            this.success = success;
            this.consumesResources = consumesResources;
            this.doubleUse = doubleUse;
        }

        /** Whether the item usage was successful */
        public boolean isSuccess() { return success; }

        /** Whether resources (mana/stamina) should be consumed */
        public boolean consumesResources() { return consumesResources; }

        /** Whether the item should be used twice (high dexterity bonus) */
        public boolean isDoubleUse() { return doubleUse; }
    }
}
