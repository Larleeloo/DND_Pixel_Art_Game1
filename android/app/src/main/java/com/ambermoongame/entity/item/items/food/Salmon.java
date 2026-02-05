package com.ambermoongame.entity.item.items.food;

import com.ambermoongame.entity.item.Item;

/**
 * Salmon - Fresh caught fish.
 * Common food item that restores health and stamina.
 */
public class Salmon extends Item {

    public Salmon() {
        super("Salmon", ItemCategory.FOOD);
        setHealthRestore(25);
        setManaRestore(0);
        setStaminaRestore(12);
        setConsumeTime(1.5f);
        setRarity(ItemRarity.COMMON);
        setDescription("Fresh caught fish");
    }

    @Override
    public Item copy() {
        return new Salmon();
    }
}
