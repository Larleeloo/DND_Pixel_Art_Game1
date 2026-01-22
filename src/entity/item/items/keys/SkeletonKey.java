package entity.item.items.keys;

import entity.item.Item;

/**
 * Skeleton Key - Opens many locks.
 * Epic key item that can open multiple lock types.
 */
public class SkeletonKey extends Item {

    public SkeletonKey() {
        super("Skeleton Key", ItemCategory.KEY);
        setRarity(ItemRarity.EPIC);
        setDescription("Opens many locks");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new SkeletonKey();
    }
}
