package entity.item.items.clothing;

import entity.item.Item;

/**
 * Yellow Cloak - A bright flowing cloak.
 * Uncommon cosmetic clothing item.
 */
public class YellowCloak extends Item {

    public YellowCloak() {
        super("Yellow Cloak", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A bright flowing cloak");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new YellowCloak();
    }
}
