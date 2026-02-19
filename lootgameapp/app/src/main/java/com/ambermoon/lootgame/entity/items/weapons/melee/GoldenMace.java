package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

public class GoldenMace extends Item {

    public GoldenMace() {
        super("Golden Mace", ItemCategory.WEAPON);
        setRarity(ItemRarity.RARE);
        setDescription("A heavy golden mace");
        setDamage(20);
        setAttackSpeed(0.8f);
        setRange(55);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new GoldenMace();
    }
}
