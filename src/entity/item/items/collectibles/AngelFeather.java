package entity.item.items.collectibles;

import entity.item.Item;

/**
 * Angel Feather - A feather from divine wings.
 * Epic collectible with holy damage and slow fall.
 */
public class AngelFeather extends Item {

    public AngelFeather() {
        super("Angel Feather", ItemCategory.OTHER);
        setRarity(ItemRarity.EPIC);
        setDescription("A feather from divine wings");
        setSpecialEffect("+25% holy damage, slow fall");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new AngelFeather();
    }
}
