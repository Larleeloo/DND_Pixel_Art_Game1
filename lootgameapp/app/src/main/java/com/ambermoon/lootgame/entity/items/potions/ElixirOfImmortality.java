package com.ambermoon.lootgame.entity.items.potions;

import com.ambermoon.lootgame.entity.Item;

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
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Grants temporary invincibility");
        setSpecialEffect("Immune to all damage for 30 seconds");
    }

    @Override
    public Item copy() {
        return new ElixirOfImmortality();
    }
}
