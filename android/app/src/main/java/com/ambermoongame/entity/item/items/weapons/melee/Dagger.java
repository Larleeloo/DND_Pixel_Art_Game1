package com.ambermoongame.entity.item.items.weapons.melee;

import com.ambermoongame.entity.item.Item;

/**
 * Iron Dagger - Quick but short range.
 * Common melee weapon with fast attacks but limited reach.
 */
public class Dagger extends Item {

    public Dagger() {
        super("Iron Dagger", ItemCategory.WEAPON);
        setDamage(8);
        setAttackSpeed(2.0f);
        setRange(40);
        setRarity(ItemRarity.COMMON);
        setDescription("Quick but short range");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new Dagger();
    }
}
