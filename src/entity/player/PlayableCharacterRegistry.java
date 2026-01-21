package entity.player;

import entity.item.Item.ItemRarity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of all playable characters in the game.
 * Singleton pattern for global access.
 *
 * Characters available for normal gameplay:
 * - Crystal: Charismatic mage with high wisdom
 * - Filvendor Venrona: Scholarly wizard with exceptional intelligence
 * - Breaya: Nimble alchemist with high dexterity
 * - Asteria: Powerful explorer with strength and constitution
 * - Olyrei: Balanced spellcaster
 * - Trethas: Mighty warrior with unmatched strength
 * - Gridius: Agile rogue with high dexterity
 *
 * Loot Game only:
 * - Merlin: Baseline character for Loot Game
 */
public class PlayableCharacterRegistry {

    private static PlayableCharacterRegistry instance;
    private Map<String, PlayableCharacter> characters;
    private List<PlayableCharacter> playableCharacters; // Excludes loot-game-only
    private List<PlayableCharacter> allCharacters;

    // Currently selected character for gameplay
    private static String selectedCharacterId = "crystal"; // Default selection

    private PlayableCharacterRegistry() {
        characters = new HashMap<>();
        playableCharacters = new ArrayList<>();
        allCharacters = new ArrayList<>();
        initialize();
    }

    public static PlayableCharacterRegistry getInstance() {
        if (instance == null) {
            instance = new PlayableCharacterRegistry();
        }
        return instance;
    }

    /**
     * Registers all playable characters.
     */
    private void initialize() {
        // ========================================
        // PLAYABLE CHARACTERS (7)
        // ========================================

        // Crystal - Charismatic mage with high wisdom
        // Stats: CHA:8, STR:4, CON:4, INT:6, WIS:7, DEX:5
        register(new PlayableCharacter.Builder("crystal", "Crystal")
            .description("A charismatic sorceress with deep wisdom and magical insight. "
                + "Her persuasive nature opens many doors, while her wisdom guides her "
                + "in the use of ancient artifacts.")
            .rarity(ItemRarity.RARE)
            .spritePath("assets/characters/crystal/sprites")
            .abilityScores(8, 4, 4, 6, 7, 5)
            .build());

        // Filvendor Venrona - Scholarly wizard with exceptional intelligence
        // Stats: CHA:7, STR:3, CON:4, INT:9, WIS:7, DEX:5
        register(new PlayableCharacter.Builder("filvendor_venrona", "Filvendor Venrona")
            .description("An elven scholar of immense magical knowledge. "
                + "His exceptional intelligence amplifies all magical abilities, "
                + "though his physical prowess is lacking.")
            .rarity(ItemRarity.EPIC)
            .spritePath("assets/characters/filvendor_venrona/sprites")
            .abilityScores(7, 3, 4, 9, 7, 5)
            .build());

        // Breaya - Nimble alchemist with high dexterity
        // Stats: CHA:4, STR:4, CON:5, INT:7, WIS:7, DEX:8
        register(new PlayableCharacter.Builder("breaya", "Breaya")
            .description("A skilled alchemist with incredible dexterity. "
                + "Her nimble fingers often allow her to use items with enhanced "
                + "efficiency, and her wisdom aids in artifact usage.")
            .rarity(ItemRarity.RARE)
            .spritePath("assets/characters/breaya/sprites")
            .abilityScores(4, 4, 5, 7, 7, 8)
            .build());

        // Asteria - Powerful explorer with strength and constitution
        // Stats: CHA:1, STR:8, CON:8, INT:8, WIS:4, DEX:6
        register(new PlayableCharacter.Builder("asteria", "Asteria")
            .description("A legendary explorer of unmatched power. "
                + "Her incredible strength, constitution, and intelligence make her "
                + "formidable in combat and magic, though she lacks social grace.")
            .rarity(ItemRarity.LEGENDARY)
            .spritePath("assets/characters/asteria/sprites")
            .abilityScores(1, 8, 8, 8, 4, 6)
            .build());

        // Olyrei - Balanced spellcaster
        // Stats: CHA:5, STR:5, CON:5, INT:7, WIS:7, DEX:6
        register(new PlayableCharacter.Builder("olyrei", "Olyrei")
            .description("A balanced spellcaster with well-rounded abilities. "
                + "Her wisdom and intelligence make her adept at magic and artifact use, "
                + "with no glaring weaknesses.")
            .rarity(ItemRarity.UNCOMMON)
            .spritePath("assets/characters/olyrei/sprites")
            .abilityScores(5, 5, 5, 7, 7, 6)
            .build());

        // Trethas - Mighty warrior with unmatched strength
        // Stats: CHA:6, STR:9, CON:7, INT:5, WIS:5, DEX:3
        register(new PlayableCharacter.Builder("trethas", "Trethas")
            .description("A mighty warrior whose strength knows no equal. "
                + "His melee attacks devastate enemies and he can carry vast amounts, "
                + "but his clumsy dexterity sometimes fails him.")
            .rarity(ItemRarity.EPIC)
            .spritePath("assets/characters/trethas/sprites")
            .abilityScores(6, 9, 7, 5, 5, 3)
            .build());

        // Gridius - Agile rogue with high dexterity
        // Stats: CHA:5, STR:5, CON:5, INT:6, WIS:6, DEX:8
        register(new PlayableCharacter.Builder("gridius", "Gridius")
            .description("An agile rogue with exceptional reflexes. "
                + "His high dexterity gives him a chance to use items twice "
                + "at no extra cost, making him efficient in combat.")
            .rarity(ItemRarity.RARE)
            .spritePath("assets/characters/gridius/sprites")
            .abilityScores(5, 5, 5, 6, 6, 8)
            .build());

        // ========================================
        // LOOT GAME ONLY CHARACTER (1)
        // ========================================

        // Merlin - Baseline character for Loot Game
        // Stats: All 5 (baseline)
        register(new PlayableCharacter.Builder("merlin", "Merlin")
            .description("A mysterious wizard who appears only in the Loot Game. "
                + "All abilities are perfectly balanced at baseline level.")
            .rarity(ItemRarity.COMMON)
            .spritePath("assets/characters/merlin/sprites")
            .abilityScores(5, 5, 5, 5, 5, 5)
            .lootGameOnly(true)
            .build());
    }

    /**
     * Registers a character in the registry.
     */
    private void register(PlayableCharacter character) {
        characters.put(character.getId(), character);
        allCharacters.add(character);
        if (!character.isLootGameOnly()) {
            playableCharacters.add(character);
        }
    }

    /**
     * Gets a character by ID.
     */
    public PlayableCharacter getCharacter(String id) {
        return characters.get(id);
    }

    /**
     * Gets all playable characters (excludes loot-game-only).
     */
    public List<PlayableCharacter> getPlayableCharacters() {
        return new ArrayList<>(playableCharacters);
    }

    /**
     * Gets all characters including loot-game-only.
     */
    public List<PlayableCharacter> getAllCharacters() {
        return new ArrayList<>(allCharacters);
    }

    /**
     * Gets the number of playable characters.
     */
    public int getPlayableCount() {
        return playableCharacters.size();
    }

    /**
     * Gets the Merlin character (for Loot Game).
     */
    public PlayableCharacter getMerlin() {
        return characters.get("merlin");
    }

    /**
     * Gets the currently selected character ID.
     */
    public static String getSelectedCharacterId() {
        return selectedCharacterId;
    }

    /**
     * Sets the currently selected character.
     */
    public static void setSelectedCharacter(String characterId) {
        if (getInstance().characters.containsKey(characterId)) {
            PlayableCharacter character = getInstance().characters.get(characterId);
            // Don't allow selecting loot-game-only characters for normal play
            if (!character.isLootGameOnly()) {
                selectedCharacterId = characterId;
            }
        }
    }

    /**
     * Gets the currently selected character.
     */
    public static PlayableCharacter getSelectedCharacter() {
        return getInstance().characters.get(selectedCharacterId);
    }

    /**
     * Gets the ability scores of the currently selected character.
     */
    public static AbilityScores getSelectedAbilityScores() {
        PlayableCharacter character = getSelectedCharacter();
        return character != null ? character.getBaseAbilityScores() : new AbilityScores();
    }

    /**
     * Gets the sprite path for the currently selected character.
     */
    public static String getSelectedSpritePath() {
        PlayableCharacter character = getSelectedCharacter();
        return character != null ? character.getSpritePath() : "assets/player/sprites";
    }

    /**
     * Checks if a character is available for selection.
     * In the future, this could check for unlock status.
     */
    public boolean isCharacterAvailable(String characterId) {
        PlayableCharacter character = characters.get(characterId);
        return character != null && !character.isLootGameOnly();
    }
}
