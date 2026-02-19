package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;

public class Staff extends Item {

    public Staff() {
        super("Staff", ItemCategory.RANGED_WEAPON);
        setRarity(ItemRarity.COMMON);
        setDescription("A basic wooden staff");
        setDamage(4);
        setAttackSpeed(0.9f);
        setRange(55);
    }

    @Override
    public Item copy() {
        return new Staff();
    }
}
