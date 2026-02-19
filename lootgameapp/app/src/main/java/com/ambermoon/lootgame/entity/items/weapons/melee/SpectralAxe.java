package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

public class SpectralAxe extends Item {

    public SpectralAxe() {
        super("Spectral Axe", ItemCategory.WEAPON);
        setRarity(ItemRarity.EPIC);
        setDescription("A ghostly axe from beyond the veil");
        setDamage(30);
        setAttackSpeed(0.9f);
        setRange(60);
        setSpecialEffect("Phases through armor");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new SpectralAxe();
    }
}
