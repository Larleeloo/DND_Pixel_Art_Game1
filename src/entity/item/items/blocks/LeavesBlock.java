package entity.item.items.blocks;

import entity.item.Item;

/**
 * Leaves Block - A cluster of leaves.
 * Common placeable block item.
 */
public class LeavesBlock extends Item {

    public LeavesBlock() {
        super("Leaves Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A cluster of leaves");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new LeavesBlock();
    }
}
