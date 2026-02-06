package com.ambermoon.lootgame.entity.items.food;

import com.ambermoon.lootgame.entity.Item;

/**
 * Chicken Egg - A nutritious egg.
 * Common food item that restores small amounts.
 */
public class ChickenEgg extends Item {

    public ChickenEgg() {
        super("Chicken Egg", ItemCategory.FOOD);
        setHealthRestore(8);
        setManaRestore(0);
        setStaminaRestore(5);
        setConsumeTime(1.5f);
        setRarity(ItemRarity.COMMON);
        setDescription("A nutritious egg");
    }

    @Override
    public Item copy() {
        return new ChickenEgg();
    }
}
