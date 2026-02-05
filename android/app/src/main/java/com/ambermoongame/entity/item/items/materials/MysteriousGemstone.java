package com.ambermoongame.entity.item.items.materials;

import com.ambermoongame.entity.item.Item;

/**
 * Mysterious Gemstone - Pulses with unknown energy.
 * Rare crafting material.
 */
public class MysteriousGemstone extends Item {

    public MysteriousGemstone() {
        super("Mysterious Gemstone", ItemCategory.MATERIAL);
        setRarity(ItemRarity.RARE);
        setDescription("Pulses with unknown energy");
    }

    @Override
    public Item copy() {
        return new MysteriousGemstone();
    }
}
