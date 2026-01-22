package entity.item.items.materials;

import entity.item.Item;

/**
 * Yarn - Spun wool or fiber.
 * Common crafting material.
 */
public class Yarn extends Item {

    public Yarn() {
        super("Yarn", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Spun wool or fiber");
    }

    @Override
    public Item copy() {
        return new Yarn();
    }
}
