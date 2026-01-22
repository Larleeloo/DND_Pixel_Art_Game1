package entity.item.items.collectibles;

import entity.item.Item;

/**
 * Crystal (Winter Edition) - A crystal infused with winter's chill.
 * Legendary collectible with ice immunity.
 */
public class CrystalWinter extends Item {

    public CrystalWinter() {
        super("Crystal (Winter Edition)", ItemCategory.OTHER);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A crystal infused with winter's chill");
        setSpecialEffect("Ice damage immunity");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new CrystalWinter();
    }
}
