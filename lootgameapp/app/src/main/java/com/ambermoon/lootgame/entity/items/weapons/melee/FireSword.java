package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

/**
 * Flame Blade - Burns enemies on hit.
 * Rare melee weapon with fire damage over time effect.
 */
public class FireSword extends Item {

    public FireSword() {
        super("Flame Blade", ItemCategory.WEAPON);
        setDamage(20);
        setAttackSpeed(0.9f);
        setRange(65);
        setRarity(ItemRarity.RARE);
        setDescription("Burns enemies on hit");
        setSpecialEffect("Burn damage over time");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new FireSword();
    }
}
