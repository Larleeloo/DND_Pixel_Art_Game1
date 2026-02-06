package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

/**
 * Iron Mace - Good against armored foes.
 * Common melee weapon with balanced stats.
 */
public class Mace extends Item {

    public Mace() {
        super("Iron Mace", ItemCategory.WEAPON);
        setDamage(15);
        setAttackSpeed(0.9f);
        setRange(55);
        setRarity(ItemRarity.COMMON);
        setDescription("Good against armored foes");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new Mace();
    }
}
