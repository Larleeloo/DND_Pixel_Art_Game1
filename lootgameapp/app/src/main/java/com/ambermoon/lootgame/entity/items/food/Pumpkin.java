package com.ambermoon.lootgame.entity.items.food;

import com.ambermoon.lootgame.entity.Item;

/**
 * Pumpkin - A seasonal gourd.
 * Common food item that restores moderate health.
 */
public class Pumpkin extends Item {

    public Pumpkin() {
        super("Pumpkin", ItemCategory.FOOD);
        setHealthRestore(15);
        setManaRestore(0);
        setStaminaRestore(5);
        setConsumeTime(1.5f);
        setRarity(ItemRarity.COMMON);
        setDescription("A seasonal gourd");
    }

    @Override
    public Item copy() {
        return new Pumpkin();
    }
}
