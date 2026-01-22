package entity.item.items.collectibles;

import entity.item.Item;

/**
 * Backpack - Increases carrying capacity.
 * Uncommon collectible with inventory bonus.
 */
public class Backpack extends Item {

    public Backpack() {
        super("Backpack", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Increases carrying capacity");
        setSpecialEffect("+8 inventory slots");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Backpack();
    }
}
