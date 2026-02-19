package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;

public class SpectralBow extends Item {

    public SpectralBow() {
        super("Spectral Bow", ItemCategory.RANGED_WEAPON);
        setRarity(ItemRarity.EPIC);
        setDescription("A ghostly bow from beyond the veil");
        setDamage(25);
        setAttackSpeed(1.1f);
        setRange(140);
        setSpecialEffect("Phases through armor");
    }

    @Override
    public Item copy() {
        return new SpectralBow();
    }
}
