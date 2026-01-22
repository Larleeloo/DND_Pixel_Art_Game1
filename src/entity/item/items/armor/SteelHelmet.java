package entity.item.items.armor;

import entity.item.Item;

/**
 * Steel Helmet - Better head protection.
 * Uncommon armor piece with improved defense.
 */
public class SteelHelmet extends Item {

    public SteelHelmet() {
        super("Steel Helmet", ItemCategory.ARMOR);
        setDefense(8);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Better head protection");
    }

    @Override
    public Item copy() {
        return new SteelHelmet();
    }
}
