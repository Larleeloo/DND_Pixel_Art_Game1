package entity.item.items.collectibles;

import entity.item.Item;

/**
 * Undead Scroll - Contains necromantic knowledge.
 * Rare collectible item.
 */
public class UndeadScroll extends Item {

    public UndeadScroll() {
        super("Undead Scroll", ItemCategory.OTHER);
        setRarity(ItemRarity.RARE);
        setDescription("Contains necromantic knowledge");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new UndeadScroll();
    }
}
