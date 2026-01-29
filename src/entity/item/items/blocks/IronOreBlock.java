package entity.item.items.blocks;

import entity.item.Item;

/**
 * Iron Ore Block - A block containing iron ore.
 * Common placeable block item.
 */
public class IronOreBlock extends Item {

    public IronOreBlock() {
        super("Iron Ore Block", ItemCategory.BLOCK);
        setRarity(ItemRarity.COMMON);
        setDescription("A block of iron ore");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new IronOreBlock();
    }
}
