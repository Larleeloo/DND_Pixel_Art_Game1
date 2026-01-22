package entity.item.items.blocks;

import entity.item.Item;

/**
 * Sand Block - A block of sand.
 * Common placeable block item.
 */
public class SandBlock extends Item {

    public SandBlock() {
        super("Sand Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A block of sand");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new SandBlock();
    }
}
