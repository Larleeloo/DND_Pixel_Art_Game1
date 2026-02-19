package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

public class GoldSword extends Item {

    public GoldSword() {
        super("Gold Sword", ItemCategory.WEAPON);
        setRarity(ItemRarity.RARE);
        setDescription("A gleaming golden sword");
        setDamage(18);
        setAttackSpeed(1.1f);
        setRange(65);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new GoldSword();
    }
}
