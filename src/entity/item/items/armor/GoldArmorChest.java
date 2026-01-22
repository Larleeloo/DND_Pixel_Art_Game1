package entity.item.items.armor;

import entity.item.Item;

/**
 * Gold Armor Chestplate - Gleaming golden protection.
 * Uncommon armor with moderate defense.
 */
public class GoldArmorChest extends Item {

    public GoldArmorChest() {
        super("Gold Armor Chestplate", ItemCategory.ARMOR);
        setDefense(12);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Gleaming golden protection");
    }

    @Override
    public Item copy() {
        return new GoldArmorChest();
    }
}
