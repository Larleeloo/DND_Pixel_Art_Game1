package entity.item.items.armor;

import entity.item.Item;

/**
 * Gold Armor Boots - Shimmering golden footwear.
 * Uncommon armor with moderate defense.
 */
public class GoldArmorBoots extends Item {

    public GoldArmorBoots() {
        super("Gold Armor Boots", ItemCategory.ARMOR);
        setDefense(5);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Shimmering golden footwear");
    }

    @Override
    public Item copy() {
        return new GoldArmorBoots();
    }
}
