package com.ambermoongame.entity.item.items.potions;

import com.ambermoongame.entity.item.Item;

/**
 * Health Potion - Restores 50 health.
 * Common potion for quick healing.
 */
public class HealthPotion extends Item {

    public HealthPotion() {
        super("Health Potion", ItemCategory.POTION);
        setHealthRestore(50);
        setManaRestore(0);
        setStaminaRestore(0);
        setConsumeTime(0.5f);
        setRarity(ItemRarity.COMMON);
        setDescription("Restores 50 health");
    }

    @Override
    public Item copy() {
        return new HealthPotion();
    }
}
