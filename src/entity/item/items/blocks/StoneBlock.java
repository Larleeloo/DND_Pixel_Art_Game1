package entity.item.items.blocks;

import entity.item.Item;

/**
 * Stone Block - A solid stone block.
 * Common placeable block item.
 */
public class StoneBlock extends Item {

    public StoneBlock() {
        super("Stone Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A solid stone block");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new StoneBlock();
    }
}
