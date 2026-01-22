package entity.item.items.armor;

import entity.item.Item;

/**
 * Iron Boots - Basic foot protection.
 * Common armor piece with moderate defense.
 */
public class IronBoots extends Item {

    public IronBoots() {
        super("Iron Boots", ItemCategory.ARMOR);
        setDefense(4);
        setRarity(ItemRarity.COMMON);
        setDescription("Basic foot protection");
    }

    @Override
    public Item copy() {
        return new IronBoots();
    }
}
