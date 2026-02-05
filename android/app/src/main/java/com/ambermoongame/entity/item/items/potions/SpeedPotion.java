package com.ambermoongame.entity.item.items.potions;

import com.ambermoongame.entity.item.Item;

/**
 * Speed Potion - Temporarily increases speed.
 * Uncommon potion with movement speed boost effect.
 */
public class SpeedPotion extends Item {

    public SpeedPotion() {
        super("Speed Potion", ItemCategory.POTION);
        setHealthRestore(0);
        setManaRestore(0);
        setStaminaRestore(0);
        setConsumeTime(0.5f);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Temporarily increases speed");
        setSpecialEffect("+30% movement speed for 30 seconds");
    }

    @Override
    public Item copy() {
        return new SpeedPotion();
    }
}
