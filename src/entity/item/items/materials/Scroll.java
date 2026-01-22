package entity.item.items.materials;

import entity.item.Item;

/**
 * Scroll - Contains ancient knowledge.
 * Uncommon crafting material.
 */
public class Scroll extends Item {

    public Scroll() {
        super("Scroll", ItemCategory.MATERIAL);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Contains ancient knowledge");
    }

    @Override
    public Item copy() {
        return new Scroll();
    }
}
