package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;

public class Cannon extends Item {

    public Cannon() {
        super("Cannon", ItemCategory.RANGED_WEAPON);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A devastating portable cannon");
        setDamage(50);
        setAttackSpeed(0.15f);
        setRange(200);
    }

    @Override
    public Item copy() {
        return new Cannon();
    }
}
