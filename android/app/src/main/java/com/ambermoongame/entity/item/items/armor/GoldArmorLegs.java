package com.ambermoongame.entity.item.items.armor;

import com.ambermoongame.entity.item.Item;

/**
 * Gold Armor Leggings - Royal golden leg guards.
 * Uncommon armor with moderate defense.
 */
public class GoldArmorLegs extends Item {

    public GoldArmorLegs() {
        super("Gold Armor Leggings", ItemCategory.ARMOR);
        setDefense(9);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Royal golden leg guards");
    }

    @Override
    public Item copy() {
        return new GoldArmorLegs();
    }
}
