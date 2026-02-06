package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

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
