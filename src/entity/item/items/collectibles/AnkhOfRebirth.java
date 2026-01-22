package entity.item.items.collectibles;

import entity.item.Item;

/**
 * Ankh of Rebirth - Symbol of eternal life.
 * Legendary collectible with auto-revive effect.
 */
public class AnkhOfRebirth extends Item {

    public AnkhOfRebirth() {
        super("Ankh of Rebirth", ItemCategory.OTHER);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Symbol of eternal life");
        setSpecialEffect("Auto-revive with full health");
        setWisdomRequirement(7);
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new AnkhOfRebirth();
    }
}
