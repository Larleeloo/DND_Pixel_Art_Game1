package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

public class SpectralSword extends Item {

    public SpectralSword() {
        super("Spectral Sword", ItemCategory.WEAPON);
        setRarity(ItemRarity.EPIC);
        setDescription("A ghostly blade from beyond the veil");
        setDamage(28);
        setAttackSpeed(1.2f);
        setRange(65);
        setSpecialEffect("Phases through armor");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new SpectralSword();
    }
}
