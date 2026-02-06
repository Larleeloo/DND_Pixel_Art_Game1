package com.ambermoon.lootgame.entity.items.potions;

import com.ambermoon.lootgame.entity.Item;

/**
 * Strength Potion - Temporarily increases damage.
 * Uncommon potion with damage boost effect.
 */
public class StrengthPotion extends Item {

    public StrengthPotion() {
        super("Strength Potion", ItemCategory.POTION);
        setHealthRestore(0);
        setManaRestore(0);
        setStaminaRestore(0);
        setConsumeTime(0.5f);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Temporarily increases damage");
        setSpecialEffect("+50% damage for 30 seconds");
    }

    @Override
    public Item copy() {
        return new StrengthPotion();
    }
}
