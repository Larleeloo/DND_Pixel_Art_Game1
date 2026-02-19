package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;

public class Musket extends Item {

    public Musket() {
        super("Musket", ItemCategory.RANGED_WEAPON);
        setRarity(ItemRarity.EPIC);
        setDescription("A powerful black powder firearm");
        setDamage(35);
        setAttackSpeed(0.3f);
        setRange(160);
        setCritChance(0.15f);
    }

    @Override
    public Item copy() {
        return new Musket();
    }
}
