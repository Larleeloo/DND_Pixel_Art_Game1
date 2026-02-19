package com.ambermoon.lootgame.entity.items.potions;

import com.ambermoon.lootgame.entity.Item;

/**
 * Lucky Potion - Increases your fortune.
 * Rare potion with drop rate bonus.
 */
public class LuckyPotion extends Item {

    public LuckyPotion() {
        super("Lucky Potion", ItemCategory.POTION);
        setHealthRestore(0);
        setManaRestore(0);
        setStaminaRestore(0);
        setConsumeTime(0.5f);
        setRarity(ItemRarity.EPIC);
        setDescription("Increases your fortune");
        setSpecialEffect("+25% drop rate for 60 seconds");
    }

    @Override
    public Item copy() {
        return new LuckyPotion();
    }
}
