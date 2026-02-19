package com.ambermoon.lootgame.entity.items.food;

import com.ambermoon.lootgame.entity.Item;

public class Chicken extends Item {

    public Chicken() {
        super("Chicken", ItemCategory.FOOD);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A roasted chicken");
        setHealthRestore(30);
        setStaminaRestore(15);
        setConsumeTime(2.0f);
    }

    @Override
    public Item copy() {
        return new Chicken();
    }
}
