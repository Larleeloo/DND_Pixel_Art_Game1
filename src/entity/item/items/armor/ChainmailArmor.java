package entity.item.items.armor;

import entity.item.Item;

/**
 * Chainmail Armor - Flexible chain protection.
 * Common armor with moderate defense.
 */
public class ChainmailArmor extends Item {

    public ChainmailArmor() {
        super("Chainmail Armor", ItemCategory.ARMOR);
        setDefense(8);
        setRarity(ItemRarity.COMMON);
        setDescription("Flexible chain protection");
    }

    @Override
    public Item copy() {
        return new ChainmailArmor();
    }
}
