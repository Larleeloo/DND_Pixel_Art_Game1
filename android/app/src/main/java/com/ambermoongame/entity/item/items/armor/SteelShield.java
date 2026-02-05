package com.ambermoongame.entity.item.items.armor;

import com.ambermoongame.entity.item.Item;

/**
 * Steel Shield - A sturdy defensive shield.
 * Uncommon armor that blocks incoming projectiles.
 */
public class SteelShield extends Item {

    public SteelShield() {
        super("Steel Shield", ItemCategory.ARMOR);
        setDefense(12);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A sturdy defensive shield");
        setSpecialEffect("Block incoming projectiles");
    }

    @Override
    public Item copy() {
        return new SteelShield();
    }
}
