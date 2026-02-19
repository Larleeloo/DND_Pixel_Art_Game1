package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

public class Club extends Item {

    public Club() {
        super("Club", ItemCategory.WEAPON);
        setRarity(ItemRarity.COMMON);
        setDescription("A crude wooden club");
        setDamage(5);
        setAttackSpeed(0.8f);
        setRange(50);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new Club();
    }
}
