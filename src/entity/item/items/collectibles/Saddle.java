package entity.item.items.collectibles;

import entity.item.Item;

/**
 * Saddle - For riding mounts.
 * Uncommon collectible item.
 */
public class Saddle extends Item {

    public Saddle() {
        super("Saddle", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("For riding mounts");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Saddle();
    }
}
