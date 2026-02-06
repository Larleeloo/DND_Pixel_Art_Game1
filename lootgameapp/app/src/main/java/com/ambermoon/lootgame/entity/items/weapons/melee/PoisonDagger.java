package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

/**
 * Poison Dagger - Coated in deadly venom.
 * Rare melee weapon with poison damage over time.
 */
public class PoisonDagger extends Item {

    public PoisonDagger() {
        super("Poison Dagger", ItemCategory.WEAPON);
        setDamage(12);
        setAttackSpeed(2.2f);
        setRange(45);
        setRarity(ItemRarity.RARE);
        setDescription("Coated in deadly venom");
        setSpecialEffect("Poison damage over time");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new PoisonDagger();
    }
}
