package com.ambermoon.lootgame.entity.items.food;

import com.ambermoon.lootgame.entity.Item;

public class Fish extends Item {

    public Fish() {
        super("Fish", ItemCategory.FOOD);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A freshly caught fish");
        setHealthRestore(20);
        setStaminaRestore(10);
        setConsumeTime(1.5f);
    }

    @Override
    public Item copy() {
        return new Fish();
    }
}
