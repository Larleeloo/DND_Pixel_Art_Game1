package entity.item.items.collectibles;

import entity.item.Item;

/**
 * Marbles - Colorful glass spheres.
 * Common collectible item.
 */
public class Marbles extends Item {

    public Marbles() {
        super("Marbles", ItemCategory.OTHER);
        setRarity(ItemRarity.COMMON);
        setDescription("Colorful glass spheres");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Marbles();
    }
}
