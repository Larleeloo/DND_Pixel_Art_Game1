package entity.item.items.materials;

import entity.item.Item;

/**
 * Leather - Tanned animal hide.
 * Common crafting material.
 */
public class Leather extends Item {

    public Leather() {
        super("Leather", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Tanned animal hide");
    }

    @Override
    public Item copy() {
        return new Leather();
    }
}
