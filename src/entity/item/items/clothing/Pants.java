package entity.item.items.clothing;

import entity.item.Item;

/**
 * Pants - Basic comfortable trousers.
 * Common cosmetic clothing item.
 */
public class Pants extends Item {

    public Pants() {
        super("Pants", ItemCategory.CLOTHING);
        setRarity(ItemRarity.COMMON);
        setDescription("Basic comfortable trousers");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new Pants();
    }
}
