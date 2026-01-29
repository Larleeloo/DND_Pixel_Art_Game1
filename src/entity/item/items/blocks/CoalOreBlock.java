package entity.item.items.blocks;

import entity.item.Item;

/**
 * Coal Ore Block - A block containing coal ore.
 * Common placeable block item.
 */
public class CoalOreBlock extends Item {

    public CoalOreBlock() {
        super("Coal Ore Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A block of coal ore");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new CoalOreBlock();
    }
}
