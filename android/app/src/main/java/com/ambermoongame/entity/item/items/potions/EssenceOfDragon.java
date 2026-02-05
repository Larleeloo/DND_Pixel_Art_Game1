package com.ambermoongame.entity.item.items.potions;

import com.ambermoongame.entity.item.Item;

/**
 * Essence of Dragon - Contains a dragon's power.
 * Legendary potion that grants fire breath ability.
 */
public class EssenceOfDragon extends Item {

    public EssenceOfDragon() {
        super("Essence of Dragon", ItemCategory.POTION);
        setHealthRestore(0);
        setManaRestore(50);
        setStaminaRestore(0);
        setConsumeTime(0.5f);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Contains a dragon's power");
        setSpecialEffect("Breathe fire for 30 seconds");
    }

    @Override
    public Item copy() {
        return new EssenceOfDragon();
    }
}
