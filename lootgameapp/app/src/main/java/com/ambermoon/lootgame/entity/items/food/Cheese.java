package com.ambermoon.lootgame.entity.items.food;

import com.ambermoon.lootgame.entity.Item;

/**
 * Cheese - A wedge of cheese.
 * Common food item that restores moderate health.
 */
public class Cheese extends Item {

    public Cheese() {
        super("Cheese", ItemCategory.FOOD);
        setHealthRestore(15);
        setManaRestore(0);
        setStaminaRestore(8);
        setConsumeTime(1.5f);
        setRarity(ItemRarity.COMMON);
        setDescription("A wedge of cheese");
    }

    @Override
    public Item copy() {
        return new Cheese();
    }
}
