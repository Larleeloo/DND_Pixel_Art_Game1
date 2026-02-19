package com.ambermoon.lootgame.entity.items.food;

import com.ambermoon.lootgame.entity.Item;

/**
 * Salmon - Fresh caught fish.
 * Common food item that restores health and stamina.
 */
public class Salmon extends Item {

    public Salmon() {
        super("Cooked Salmon", ItemCategory.FOOD);
        setHealthRestore(25);
        setManaRestore(0);
        setStaminaRestore(12);
        setConsumeTime(1.5f);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A perfectly cooked salmon fillet");
    }

    @Override
    public Item copy() {
        return new Salmon();
    }
}
