package entity;

import entity.item.Item.ItemRarity;
import java.util.ArrayList;
import java.util.List;

/**
 * CompanionRegistry manages companion character alternates.
 *
 * Companions are AI-controlled characters that can follow and assist the player.
 * Unlike items, companions are selected in the character customization menu
 * as player character alternates.
 *
 * Features:
 * - Each companion has unique stats and special abilities
 * - Companions can be unlocked through gameplay
 * - Only one companion can be active at a time
 * - Companions are saved with character customization
 */
public class CompanionRegistry {

    private static List<CompanionData> companions = new ArrayList<>();
    private static boolean initialized = false;

    /**
     * Data class representing a companion character.
     */
    public static class CompanionData {
        public final String id;
        public final String name;
        public final String description;
        public final ItemRarity rarity;
        public final String specialEffect;
        public final String spriteDir;  // Directory containing companion sprites
        public final int baseHealth;
        public final int baseDamage;
        public final double moveSpeed;
        public boolean unlocked;  // Whether this companion is available to select

        public CompanionData(String id, String name, String description, ItemRarity rarity,
                             String specialEffect, String spriteDir, int baseHealth,
                             int baseDamage, double moveSpeed, boolean unlocked) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.rarity = rarity;
            this.specialEffect = specialEffect;
            this.spriteDir = spriteDir;
            this.baseHealth = baseHealth;
            this.baseDamage = baseDamage;
            this.moveSpeed = moveSpeed;
            this.unlocked = unlocked;
        }

        /**
         * Gets the display color based on rarity.
         */
        public java.awt.Color getRarityColor() {
            switch (rarity) {
                case COMMON: return java.awt.Color.WHITE;
                case UNCOMMON: return new java.awt.Color(100, 255, 100);
                case RARE: return new java.awt.Color(100, 150, 255);
                case EPIC: return new java.awt.Color(180, 100, 255);
                case LEGENDARY: return new java.awt.Color(255, 180, 50);
                case MYTHIC: return new java.awt.Color(100, 255, 255);
                default: return java.awt.Color.WHITE;
            }
        }
    }

    /**
     * Initializes the companion registry with all available companions.
     */
    public static void initialize() {
        if (initialized) return;

        companions.clear();

        // Register all companions (moved from ItemRegistry)
        // Companions are unlocked by default for testing; in final game, set unlocked=false

        registerCompanion("scarecrow_companion", "Scarecrow Companion",
                "A friendly scarecrow that distracts enemies",
                ItemRarity.RARE, null,
                "assets/companions/scarecrow",
                40, 5, 3.0, true);

        registerCompanion("parrot_companion", "Parrot Companion",
                "A colorful bird that scouts ahead and warns of danger",
                ItemRarity.RARE, "Reveals enemies within 300 range",
                "assets/companions/parrot",
                25, 3, 5.0, true);

        registerCompanion("asteria_explorer", "Asteria the Explorer",
                "A legendary explorer who has mapped countless dungeons",
                ItemRarity.LEGENDARY, "Reveals hidden treasures",
                "assets/companions/asteria",
                60, 12, 4.0, true);

        registerCompanion("breaya_alchemist", "Breaya the Alchemist",
                "A master alchemist who brews powerful potions",
                ItemRarity.LEGENDARY, "Doubles potion effectiveness",
                "assets/companions/breaya",
                50, 8, 3.5, true);

        registerCompanion("gipp", "Gipp",
                "A mysterious magical creature of unknown origin",
                ItemRarity.EPIC, "Random magical effects on hit",
                "assets/companions/gipp",
                45, 15, 4.5, true);

        registerCompanion("noctra_solen", "Noctra and Solen",
                "Twin spirits that embody night and day",
                ItemRarity.LEGENDARY, "Bonus damage during day/night",
                "assets/companions/noctra_solen",
                70, 18, 3.5, true);

        registerCompanion("filvendor", "Filvendor Venrona",
                "An ancient elven sage with vast knowledge",
                ItemRarity.EPIC, "+15% experience gain",
                "assets/companions/filvendor",
                55, 10, 3.0, true);

        initialized = true;
        System.out.println("CompanionRegistry: Initialized with " + companions.size() + " companions");
    }

    /**
     * Registers a companion.
     */
    private static void registerCompanion(String id, String name, String description,
                                           ItemRarity rarity, String specialEffect,
                                           String spriteDir, int health, int damage,
                                           double speed, boolean unlocked) {
        companions.add(new CompanionData(id, name, description, rarity, specialEffect,
                                         spriteDir, health, damage, speed, unlocked));
    }

    /**
     * Gets all registered companions.
     */
    public static List<CompanionData> getAll() {
        initialize();
        return new ArrayList<>(companions);
    }

    /**
     * Gets only unlocked companions.
     */
    public static List<CompanionData> getUnlocked() {
        initialize();
        List<CompanionData> unlocked = new ArrayList<>();
        for (CompanionData c : companions) {
            if (c.unlocked) {
                unlocked.add(c);
            }
        }
        return unlocked;
    }

    /**
     * Gets a companion by ID.
     */
    public static CompanionData get(String id) {
        initialize();
        for (CompanionData c : companions) {
            if (c.id.equals(id)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Unlocks a companion by ID.
     */
    public static void unlock(String id) {
        CompanionData c = get(id);
        if (c != null) {
            c.unlocked = true;
            System.out.println("CompanionRegistry: Unlocked companion: " + c.name);
        }
    }

    /**
     * Checks if a companion is unlocked.
     */
    public static boolean isUnlocked(String id) {
        CompanionData c = get(id);
        return c != null && c.unlocked;
    }

    /**
     * Gets the total number of companions.
     */
    public static int getCount() {
        initialize();
        return companions.size();
    }
}
