package com.ambermoongame.entity.item.items.food;

import com.ambermoongame.entity.item.Item;

/**
 * Apple - A fresh apple.
 * Common food item that restores a small amount of health.
 */
public class Apple extends Item {

    public Apple() {
        super("Apple", ItemCategory.FOOD);
        setHealthRestore(10);
        setManaRestore(0);
        setStaminaRestore(5);
        setConsumeTime(1.5f);
        setRarity(ItemRarity.COMMON);
        setDescription("A fresh apple");
    }

    @Override
    public Item copy() {
        return new Apple();
    }
}
