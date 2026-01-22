package entity.item.items.armor;

import entity.item.Item;

/**
 * Phoenix Crown - Blazes with eternal fire.
 * Epic armor with fire resistance and auto-revive.
 */
public class PhoenixCrown extends Item {

    public PhoenixCrown() {
        super("Phoenix Crown", ItemCategory.ARMOR);
        setDefense(15);
        setRarity(ItemRarity.EPIC);
        setDescription("Blazes with eternal fire");
        setSpecialEffect("+100% fire resistance, auto-revive once");
    }

    @Override
    public Item copy() {
        return new PhoenixCrown();
    }
}
