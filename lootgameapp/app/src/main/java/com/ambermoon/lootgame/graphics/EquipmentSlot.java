package com.ambermoon.lootgame.graphics;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ItemRegistry;
import com.ambermoon.lootgame.save.SaveData;

import java.util.ArrayList;
import java.util.List;

/**
 * Equipment slot constants and utilities for the clothing preview system.
 * Uses int constants instead of enums to avoid D8 compiler issues.
 *
 * Slots are ordered by render priority (bottom-to-top layering):
 *   PANTS → SHOES → BOOTS → SHIRT → ARMOR → LEGGINGS →
 *   GAUNTLETS → NECKLACE → HEADWEAR → RINGS_GLOVES
 */
public class EquipmentSlot {

    // Slot type constants
    public static final int SLOT_NONE = -1;
    public static final int SLOT_HEADWEAR = 0;
    public static final int SLOT_SHIRT = 1;
    public static final int SLOT_ARMOR = 2;
    public static final int SLOT_GAUNTLETS = 3;
    public static final int SLOT_PANTS = 4;
    public static final int SLOT_LEGGINGS = 5;
    public static final int SLOT_SHOES = 6;
    public static final int SLOT_BOOTS = 7;
    public static final int SLOT_RINGS_GLOVES = 8;
    public static final int SLOT_NECKLACE = 9;
    public static final int SLOT_COUNT = 10;

    // Internal names (used as map keys in save data)
    public static final String[] SLOT_NAMES = {
        "headwear", "shirt", "armor", "gauntlets", "pants",
        "leggings", "shoes", "boots", "rings_gloves", "necklace"
    };

    // Display names for UI
    public static final String[] SLOT_DISPLAY_NAMES = {
        "Headwear", "Shirt", "Armor", "Gauntlets", "Pants",
        "Leggings", "Shoes", "Boots", "Rings/Gloves", "Necklace"
    };

    /**
     * Render order for sprite layer compositing (bottom-to-top).
     * Items earlier in this array are drawn first (behind later items).
     */
    public static final int[] RENDER_ORDER = {
        SLOT_PANTS,        // 0 - drawn first (behind everything)
        SLOT_SHOES,        // 1
        SLOT_BOOTS,        // 2
        SLOT_SHIRT,        // 3
        SLOT_ARMOR,        // 4
        SLOT_LEGGINGS,     // 5
        SLOT_GAUNTLETS,    // 6
        SLOT_NECKLACE,     // 7
        SLOT_HEADWEAR,     // 8
        SLOT_RINGS_GLOVES  // 9 - drawn last (on top of everything)
    };

    public static String getSlotName(int slot) {
        if (slot >= 0 && slot < SLOT_COUNT) return SLOT_NAMES[slot];
        return "none";
    }

    public static String getDisplayName(int slot) {
        if (slot >= 0 && slot < SLOT_COUNT) return SLOT_DISPLAY_NAMES[slot];
        return "None";
    }

    public static int fromName(String name) {
        if (name == null) return SLOT_NONE;
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (SLOT_NAMES[i].equals(name)) return i;
        }
        return SLOT_NONE;
    }

    /**
     * Determines the equipment slot for an item based on its name and category.
     * Returns SLOT_NONE (-1) if the item doesn't fit any slot.
     */
    public static int getSlotForItem(Item item) {
        if (item == null) return SLOT_NONE;

        String name = item.getName();
        if (name == null) return SLOT_NONE;

        int category = item.getCategory().intValue();

        // Headwear: hats, caps, crowns, helmets
        if (containsAny(name, "Hat", "Cap", "Crown", "Helmet")) {
            return SLOT_HEADWEAR;
        }

        // Gauntlets
        if (name.contains("Gauntlets")) {
            return SLOT_GAUNTLETS;
        }

        // Leggings
        if (name.contains("Leggings")) {
            return SLOT_LEGGINGS;
        }

        // Chestplate armor
        if (name.contains("Chestplate")) {
            return SLOT_ARMOR;
        }

        // Armor-category robes (Celestial Robes, Archmage Robes)
        if (category == Item.CATEGORY_ARMOR && name.contains("Robes")) {
            return SLOT_ARMOR;
        }

        // Shirts, dresses, robes (clothing category), tunics, suits, capes, cloaks, gowns, swimwear
        if (containsAny(name, "Shirt", "Dress", "Tunic", "Suit", "Swimwear",
                "Cape", "Cloak", "Gown")) {
            return SLOT_SHIRT;
        }
        // Clothing-category robes go to shirt slot
        if (category == Item.CATEGORY_CLOTHING && name.contains("Robe")) {
            return SLOT_SHIRT;
        }

        // Boots (before shoes since "Boots" is more specific)
        if (name.contains("Boots")) {
            return SLOT_BOOTS;
        }

        // Shoes
        if (name.contains("Shoes")) {
            return SLOT_SHOES;
        }

        // Pants
        if (name.contains("Pants")) {
            return SLOT_PANTS;
        }

        // Necklaces
        if (name.contains("Necklace")) {
            return SLOT_NECKLACE;
        }

        // Bracelets, Ruby Skull → rings/gloves
        if (name.contains("Bracelet") || name.equals("Ruby Skull")) {
            return SLOT_RINGS_GLOVES;
        }

        return SLOT_NONE;
    }

    /**
     * Filters vault items to find those matching a specific equipment slot.
     *
     * @param slot The equipment slot to filter for
     * @param vaultItems The player's vault items
     * @return List of item registry IDs that match the slot
     */
    public static List<String> getItemIdsForSlot(int slot, List<SaveData.VaultItem> vaultItems) {
        List<String> result = new ArrayList<>();
        if (vaultItems == null || slot < 0) return result;

        for (SaveData.VaultItem vi : vaultItems) {
            Item item = ItemRegistry.create(vi.itemId);
            if (item != null && item.getEquipmentSlot() == slot) {
                if (!result.contains(vi.itemId)) {
                    result.add(vi.itemId);
                }
            }
        }
        return result;
    }

    private static boolean containsAny(String name, String... keywords) {
        for (String kw : keywords) {
            if (name.contains(kw)) return true;
        }
        return false;
    }
}
