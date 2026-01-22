package entity.item.items.clothing;

import entity.item.Item;

/**
 * Witch's Hat - A pointy magical hat.
 * Uncommon clothing with magic damage bonus.
 */
public class WitchsHat extends Item {

    public WitchsHat() {
        super("Witch's Hat", ItemCategory.CLOTHING);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pointy magical hat");
        setSpecialEffect("+5% magic damage");
        setStackable(false);
    }

    @Override
    public Item copy() {
        return new WitchsHat();
    }
}
