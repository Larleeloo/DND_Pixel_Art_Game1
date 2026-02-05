package com.ambermoongame.entity.item.items.potions;

import com.ambermoongame.entity.item.Item;

/**
 * Exploding Potion - Explosive on impact.
 * Rare potion with area effect damage.
 */
public class ExplodingPotion extends Item {

    public ExplodingPotion() {
        super("Exploding Potion", ItemCategory.POTION);
        setHealthRestore(0);
        setManaRestore(0);
        setStaminaRestore(0);
        setConsumeTime(0.5f);
        setRarity(ItemRarity.RARE);
        setDescription("Explosive on impact");
        setAreaEffect(true, 96);
    }

    @Override
    public Item copy() {
        return new ExplodingPotion();
    }
}
