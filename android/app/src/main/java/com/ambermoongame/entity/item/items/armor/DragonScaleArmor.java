package com.ambermoongame.entity.item.items.armor;

import com.ambermoongame.entity.item.Item;

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
