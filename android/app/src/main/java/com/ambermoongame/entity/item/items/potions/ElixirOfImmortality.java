package com.ambermoongame.entity.item.items.potions;

import com.ambermoongame.entity.item.Item;

/**
 * Elixir of Immortality - Grants temporary invincibility.
 * Mythic potion with total damage immunity.
 */
public class ElixirOfImmortality extends Item {

    public ElixirOfImmortality() {
        super("Elixir of Immortality", ItemCategory.POTION);
        setHealthRestore(0);
        setManaRestore(0);
        setStaminaRestore(0);
        setConsumeTime(0.5f);
        setRarity(ItemRarity.MYTHIC);
        setDescription("Grants temporary invincibility");
        setSpecialEffect("Immune to all damage for 30 seconds");
    }

    @Override
    public Item copy() {
        return new ElixirOfImmortality();
    }
}
