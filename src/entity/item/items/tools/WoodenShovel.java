package entity.item.items.tools;

import entity.item.Item;

/**
 * Wooden Shovel - Digs soft ground.
 * Common tool for digging dirt and sand.
 */
public class WoodenShovel extends Item {

    public WoodenShovel() {
        super("Wooden Shovel", ItemCategory.TOOL);
        setDamage(2);
        setRarity(ItemRarity.COMMON);
        setDescription("Digs soft ground");
    }

    @Override
    public Item copy() {
        return new WoodenShovel();
    }
}
