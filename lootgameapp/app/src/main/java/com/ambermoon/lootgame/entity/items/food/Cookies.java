package com.ambermoon.lootgame.entity.items.food;

import com.ambermoon.lootgame.entity.Item;

/**
 * Cookies - Freshly baked cookies.
 * Common food item that restores small amounts.
 */
public class Cookies extends Item {

    public Cookies() {
        super("Cookies", ItemCategory.FOOD);
        setHealthRestore(12);
        setManaRestore(0);
        setStaminaRestore(8);
        setConsumeTime(1.5f);
        setRarity(ItemRarity.COMMON);
        setDescription("Freshly baked cookies");
    }

    @Override
    public Item copy() {
        return new Cookies();
    }
}
