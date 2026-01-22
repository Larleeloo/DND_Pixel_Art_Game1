package entity.item.items.collectibles;

import entity.item.Item;

/**
 * Trip-Wire Trap - Catches unsuspecting foes.
 * Uncommon collectible item.
 */
public class TripWireTrap extends Item {

    public TripWireTrap() {
        super("Trip-Wire Trap", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Catches unsuspecting foes");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new TripWireTrap();
    }
}
