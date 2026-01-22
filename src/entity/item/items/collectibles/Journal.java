package entity.item.items.collectibles;

import entity.item.Item;

/**
 * Journal - A book for recording thoughts.
 * Common collectible item.
 */
public class Journal extends Item {

    public Journal() {
        super("Journal", ItemCategory.OTHER);
        setRarity(ItemRarity.COMMON);
        setDescription("A book for recording thoughts");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Journal();
    }
}
