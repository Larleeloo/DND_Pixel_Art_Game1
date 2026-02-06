package com.ambermoon.lootgame.entity.items.potions;

import com.ambermoon.lootgame.entity.Item;

/**
 * Mana Leaf - Restores mana when consumed.
 * Uncommon consumable material.
 */
public class ManaLeaf extends Item {

    public ManaLeaf() {
        super("Mana Leaf", ItemCategory.POTION);
        setHealthRestore(0);
        setManaRestore(30);
        setStaminaRestore(0);
        setConsumeTime(1.0f);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Restores mana when consumed");
    }

    @Override
    public Item copy() {
        return new ManaLeaf();
    }
}
