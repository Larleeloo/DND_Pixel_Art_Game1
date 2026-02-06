package com.ambermoon.lootgame.entity.items.food;

import com.ambermoon.lootgame.entity.Item;

/**
 * Melon - A juicy refreshing melon.
 * Common food item that restores moderate health.
 */
public class Melon extends Item {

    public Melon() {
        super("Melon", ItemCategory.FOOD);
        setHealthRestore(18);
        setManaRestore(0);
        setStaminaRestore(10);
        setConsumeTime(1.5f);
        setRarity(ItemRarity.COMMON);
        setDescription("A juicy refreshing melon");
    }

    @Override
    public Item copy() {
        return new Melon();
    }
}
