package entity.item.items.materials;

import entity.item.Item;

/**
 * Gold Coins - Currency of the realm.
 * Common currency that stacks to 64.
 */
public class GoldCoins extends Item {

    public GoldCoins() {
        super("Gold Coins", ItemCategory.MATERIAL);
        setRarity(ItemRarity.COMMON);
        setDescription("Currency of the realm");
        setStackable(true);
        setMaxStackSize(64);
    }

    @Override
    public Item copy() {
        return new GoldCoins();
    }
}
