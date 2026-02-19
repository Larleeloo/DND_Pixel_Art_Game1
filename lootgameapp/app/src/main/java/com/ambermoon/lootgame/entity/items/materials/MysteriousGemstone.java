package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

/**
 * Mysterious Gemstone - Pulses with unknown energy.
 * Rare crafting material.
 */
public class MysteriousGemstone extends Item {

    public MysteriousGemstone() {
        super("Magic Gemstone", ItemCategory.MATERIAL);
        setRarity(ItemRarity.EPIC);
        setDescription("A gemstone pulsing with arcane energy");
    }

    @Override
    public Item copy() {
        return new MysteriousGemstone();
    }
}
