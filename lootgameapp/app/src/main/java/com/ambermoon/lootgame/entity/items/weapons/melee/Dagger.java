package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

/**
 * Iron Dagger - Quick but short range.
 * Common melee weapon with fast attacks but limited reach.
 */
public class Dagger extends Item {

    public Dagger() {
        super("Daggers", ItemCategory.WEAPON);
        setDamage(8);
        setAttackSpeed(2.0f);
        setRange(40);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A pair of sharp daggers");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new Dagger();
    }
}
