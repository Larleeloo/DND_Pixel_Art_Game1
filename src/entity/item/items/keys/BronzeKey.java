package entity.item.items.keys;

import entity.item.Item;

/**
 * Bronze Key - Opens bronze locks.
 * Common key item.
 */
public class BronzeKey extends Item {

    public BronzeKey() {
        super("Bronze Key", ItemCategory.KEY);
        setRarity(ItemRarity.COMMON);
        setDescription("Opens bronze locks");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new BronzeKey();
    }
}
