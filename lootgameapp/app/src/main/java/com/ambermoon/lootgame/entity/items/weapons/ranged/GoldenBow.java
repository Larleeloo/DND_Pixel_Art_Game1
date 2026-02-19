package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;

public class GoldenBow extends Item {

    public GoldenBow() {
        super("Golden Bow", ItemCategory.RANGED_WEAPON);
        setRarity(ItemRarity.RARE);
        setDescription("A bow of gleaming gold");
        setDamage(18);
        setAttackSpeed(1.0f);
        setRange(130);
    }

    @Override
    public Item copy() {
        return new GoldenBow();
    }
}
