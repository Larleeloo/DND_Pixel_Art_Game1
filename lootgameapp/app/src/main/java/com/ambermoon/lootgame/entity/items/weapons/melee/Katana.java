package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

public class Katana extends Item {

    public Katana() {
        super("Katana", ItemCategory.WEAPON);
        setRarity(ItemRarity.RARE);
        setDescription("A finely crafted katana");
        setDamage(20);
        setAttackSpeed(1.3f);
        setRange(65);
        setCritChance(0.10f);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new Katana();
    }
}
