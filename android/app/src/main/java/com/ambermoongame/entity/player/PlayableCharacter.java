package com.ambermoongame.entity.player;

import android.graphics.Color;

import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.item.Item.ItemRarity;

/**
 * Represents a playable character in the game.
 * Each character has unique ability scores and properties.
 *
 * Conversion notes:
 * - java.awt.Color -> android.graphics.Color (int values)
 * - getRarityColor() returns int instead of java.awt.Color
 */
public class PlayableCharacter {

    private final String id;
    private final String displayName;
    private final String description;
    private final ItemRarity rarity;
    private final String spritePath;
    private final AbilityScores baseAbilityScores;
    private final boolean lootGameOnly;

    /**
     * Creates a new playable character.
     *
     * @param id Unique identifier for the character
     * @param displayName Display name shown in menus
     * @param description Character description/backstory
     * @param rarity Character rarity (affects display)
     * @param spritePath Path to sprite directory
     * @param abilityScores Starting ability scores
     * @param lootGameOnly If true, character only appears in Loot Game
     */
    public PlayableCharacter(String id, String displayName, String description,
                             ItemRarity rarity, String spritePath,
                             AbilityScores abilityScores, boolean lootGameOnly) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.rarity = rarity;
        this.spritePath = spritePath;
        this.baseAbilityScores = abilityScores;
        this.lootGameOnly = lootGameOnly;
    }

    /**
     * Creates a regular playable character (not loot game only).
     */
    public PlayableCharacter(String id, String displayName, String description,
                             ItemRarity rarity, String spritePath,
                             AbilityScores abilityScores) {
        this(id, displayName, description, rarity, spritePath, abilityScores, false);
    }

    // === GETTERS ===

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public ItemRarity getRarity() { return rarity; }
    public String getSpritePath() { return spritePath; }
    public boolean isLootGameOnly() { return lootGameOnly; }

    /**
     * Gets a copy of the base ability scores.
     * Returns a copy so the original cannot be modified.
     */
    public AbilityScores getBaseAbilityScores() {
        return new AbilityScores(baseAbilityScores);
    }

    /**
     * Gets the sprite directory path for this character.
     */
    public String getSpriteDirectory() {
        return spritePath;
    }

    /**
     * Gets the path to the idle sprite for preview.
     */
    public String getIdleSpritePath() {
        return spritePath + "/idle.gif";
    }

    /**
     * Gets the formatted ability scores string for display.
     */
    public String getAbilityScoresSummary() {
        AbilityScores scores = baseAbilityScores;
        return String.format("CHA:%d  STR:%d  CON:%d  INT:%d  WIS:%d  DEX:%d",
            scores.getCharisma(), scores.getStrength(), scores.getConstitution(),
            scores.getIntelligence(), scores.getWisdom(), scores.getDexterity());
    }

    /**
     * Gets the color associated with this character's rarity.
     * Returns an Android int color instead of java.awt.Color.
     */
    public int getRarityColor() {
        switch (rarity.intValue()) {
            case Item.RARITY_COMMON: return Color.WHITE;
            case Item.RARITY_UNCOMMON: return Color.rgb(50, 205, 50);    // Green
            case Item.RARITY_RARE: return Color.rgb(30, 144, 255);       // Blue
            case Item.RARITY_EPIC: return Color.rgb(148, 0, 211);        // Purple
            case Item.RARITY_LEGENDARY: return Color.rgb(255, 165, 0);   // Orange
            case Item.RARITY_MYTHIC: return Color.rgb(0, 255, 255);      // Cyan
            default: return Color.WHITE;
        }
    }

    @Override
    public String toString() {
        return String.format("PlayableCharacter[%s: %s, %s]",
            id, displayName, baseAbilityScores);
    }

    /**
     * Builder class for creating PlayableCharacter instances.
     */
    public static class Builder {
        private String id;
        private String displayName;
        private String description = "";
        private ItemRarity rarity = ItemRarity.COMMON;
        private String spritePath;
        private AbilityScores abilityScores;
        private boolean lootGameOnly = false;

        public Builder(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder rarity(ItemRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder spritePath(String spritePath) {
            this.spritePath = spritePath;
            return this;
        }

        public Builder abilityScores(int cha, int str, int con, int intel, int wis, int dex) {
            this.abilityScores = new AbilityScores(cha, str, con, intel, wis, dex);
            return this;
        }

        public Builder abilityScores(AbilityScores scores) {
            this.abilityScores = scores;
            return this;
        }

        public Builder lootGameOnly(boolean lootGameOnly) {
            this.lootGameOnly = lootGameOnly;
            return this;
        }

        public PlayableCharacter build() {
            if (spritePath == null) {
                spritePath = "assets/characters/" + id + "/sprites";
            }
            if (abilityScores == null) {
                abilityScores = new AbilityScores(); // All baseline
            }
            return new PlayableCharacter(id, displayName, description,
                rarity, spritePath, abilityScores, lootGameOnly);
        }
    }
}
