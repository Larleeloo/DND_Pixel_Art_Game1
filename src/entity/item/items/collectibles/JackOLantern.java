package entity.item.items.collectibles;

import entity.item.Item;

/**
 * Jack-O-Lantern - A carved pumpkin that glows.
 * Uncommon collectible item.
 */
public class JackOLantern extends Item {

    public JackOLantern() {
        super("Jack-O-Lantern", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A carved pumpkin that glows");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new JackOLantern();
    }
}
