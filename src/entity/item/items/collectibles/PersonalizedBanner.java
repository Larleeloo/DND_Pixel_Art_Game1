package entity.item.items.collectibles;

import entity.item.Item;

/**
 * Personalized Banner - Your personal emblem.
 * Uncommon collectible item.
 */
public class PersonalizedBanner extends Item {

    public PersonalizedBanner() {
        super("Personalized Banner", ItemCategory.OTHER);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Your personal emblem");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new PersonalizedBanner();
    }
}
