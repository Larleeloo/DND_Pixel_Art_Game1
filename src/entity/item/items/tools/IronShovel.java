package entity.item.items.tools;

import entity.item.Item;

/**
 * Iron Shovel - Faster digging.
 * Common tool with improved digging speed.
 */
public class IronShovel extends Item {

    public IronShovel() {
        super("Iron Shovel", ItemCategory.TOOL);
        setDamage(4);
        setRarity(ItemRarity.COMMON);
        setDescription("Faster digging");
    }

    @Override
    public Item copy() {
        return new IronShovel();
    }
}
