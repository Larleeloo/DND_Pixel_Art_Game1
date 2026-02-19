package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

/**
 * Iron Sword - A reliable iron blade.
 * Standard melee weapon with balanced stats.
 */
public class IronSword extends Item {

    public IronSword() {
        super("Iron Sword", ItemCategory.WEAPON);
        setDamage(12);
        setAttackSpeed(1.0f);
        setRange(60);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A reliable iron blade");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new IronSword();
    }
}
