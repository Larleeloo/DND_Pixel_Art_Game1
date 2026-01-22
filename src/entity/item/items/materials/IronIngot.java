package entity.item.items.materials;

import entity.item.Item;

/**
 * Iron Ingot - Refined iron for crafting.
 * Common crafting material.
 */
public class IronIngot extends Item {

    public IronIngot() {
        super("Iron Ingot", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Refined iron for crafting");
    }

    @Override
    public Item copy() {
        return new IronIngot();
    }
}
