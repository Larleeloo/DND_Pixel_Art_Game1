package com.ambermoongame.entity.item.items.weapons.melee;

import com.ambermoongame.entity.item.Item;

/**
 * Electrified Katana - Crackles with lightning energy.
 * Legendary melee weapon with chain lightning on critical hits.
 */
public class ElectrifiedKatana extends Item {

    public ElectrifiedKatana() {
        super("Electrified Katana", ItemCategory.WEAPON);
        setDamage(32);
        setAttackSpeed(1.4f);
        setRange(75);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Crackles with lightning energy");
        setSpecialEffect("Chain lightning on critical hits");
        setCritChance(0.20f);
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new ElectrifiedKatana();
    }
}
