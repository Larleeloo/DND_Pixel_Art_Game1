package com.ambermoon.lootgame.entity.items.potions;

import com.ambermoon.lootgame.entity.Item;

/**
 * Mana Potion - Restores 50 mana.
 * Common potion for mana recovery.
 */
public class ManaPotion extends Item {

    public ManaPotion() {
        super("Mana Potion", ItemCategory.POTION);
        setHealthRestore(0);
        setManaRestore(50);
        setStaminaRestore(0);
        setConsumeTime(0.5f);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Restores 50 mana");
    }

    @Override
    public Item copy() {
        return new ManaPotion();
    }
}
