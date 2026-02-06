package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

/**
 * Dragon Scale Armor - Forged from dragon scales.
 * Legendary armor with fire resistance.
 */
public class DragonScaleArmor extends Item {

    public DragonScaleArmor() {
        super("Dragon Scale Armor", ItemCategory.ARMOR);
        setDefense(25);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Forged from dragon scales");
        setSpecialEffect("+50% fire resistance");
    }

    @Override
    public Item copy() {
        return new DragonScaleArmor();
    }
}
