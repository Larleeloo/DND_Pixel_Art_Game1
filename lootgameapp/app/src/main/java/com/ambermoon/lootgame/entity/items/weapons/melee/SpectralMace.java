package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

public class SpectralMace extends Item {

    public SpectralMace() {
        super("Spectral Mace", ItemCategory.WEAPON);
        setRarity(ItemRarity.EPIC);
        setDescription("A ghostly mace from beyond the veil");
        setDamage(26);
        setAttackSpeed(0.85f);
        setRange(55);
        setSpecialEffect("Phases through armor");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new SpectralMace();
    }
}
