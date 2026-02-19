package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

/**
 * Vampiric Blade - Thirsts for blood.
 * Rare melee weapon with 10% lifesteal.
 */
public class VampiricBlade extends Item {

    public VampiricBlade() {
        super("Vampiric Dagger", ItemCategory.WEAPON);
        setDamage(20);
        setAttackSpeed(1.0f);
        setRange(60);
        setRarity(ItemRarity.EPIC);
        setDescription("A cursed dagger that drains life force");
        setSpecialEffect("10% lifesteal");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new VampiricBlade();
    }
}
