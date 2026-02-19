package com.ambermoon.lootgame.entity.items.food;

import com.ambermoon.lootgame.entity.Item;

public class Berries extends Item {

    public Berries() {
        super("Berries", ItemCategory.FOOD);
        setRarity(ItemRarity.COMMON);
        setDescription("A handful of fresh berries");
        setHealthRestore(8);
        setStaminaRestore(3);
        setConsumeTime(1.0f);
    }

    @Override
    public Item copy() {
        return new Berries();
    }
}
