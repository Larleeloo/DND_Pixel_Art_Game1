package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;

public class MetalBow extends Item {

    public MetalBow() {
        super("Metal Bow", ItemCategory.RANGED_WEAPON);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A bow reinforced with metal");
        setDamage(14);
        setAttackSpeed(0.9f);
        setRange(120);
    }

    @Override
    public Item copy() {
        return new MetalBow();
    }
}
