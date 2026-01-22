package entity.item.items.materials;

import entity.item.Item;

/**
 * Rocks - Simple stones.
 * Common crafting material.
 */
public class Rocks extends Item {

    public Rocks() {
        super("Rocks", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Simple stones");
    }

    @Override
    public Item copy() {
        return new Rocks();
    }
}
