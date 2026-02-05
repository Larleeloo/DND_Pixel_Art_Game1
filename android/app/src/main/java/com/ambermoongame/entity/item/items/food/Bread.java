package com.ambermoongame.entity.item.items.food;

import com.ambermoongame.entity.item.Item;

/**
 * Bread - A hearty loaf of bread.
 * Common food item that restores health.
 */
public class Bread extends Item {

    public Bread() {
        super("Bread", ItemCategory.FOOD);
        setHealthRestore(20);
        setManaRestore(0);
        setStaminaRestore(10);
        setConsumeTime(1.5f);
        setRarity(ItemRarity.COMMON);
        setDescription("A hearty loaf of bread");
    }

    @Override
    public Item copy() {
        return new Bread();
    }
}
