package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

/**
 * Magic Crystal - Imbued with arcane energy.
 * Rare crafting material for magical items.
 */
public class MagicCrystalMaterial extends Item {

    public MagicCrystalMaterial() {
        super("Magic Crystal", ItemCategory.MATERIAL);
        setRarity(ItemRarity.RARE);
        setDescription("Imbued with arcane energy");
    }

    @Override
    public Item copy() {
        return new MagicCrystalMaterial();
    }
}
