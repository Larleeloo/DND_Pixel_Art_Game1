package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

public class GoldenHeavyBattleaxe extends Item {

    public GoldenHeavyBattleaxe() {
        super("Golden Heavy Battleaxe", ItemCategory.WEAPON);
        setRarity(ItemRarity.RARE);
        setDescription("A massive golden battleaxe");
        setDamage(22);
        setAttackSpeed(0.7f);
        setRange(70);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new GoldenHeavyBattleaxe();
    }
}
