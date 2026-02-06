package com.ambermoon.lootgame.entity.items.weapons.melee;

import com.ambermoon.lootgame.entity.Item;

/**
 * Crystal Sword - Made of pure crystal.
 * Rare melee weapon that reflects magic.
 */
public class CrystalSword extends Item {

    public CrystalSword() {
        super("Crystal Sword", ItemCategory.WEAPON);
        setDamage(22);
        setAttackSpeed(1.1f);
        setRange(65);
        setRarity(ItemRarity.RARE);
        setDescription("Made of pure crystal");
        setSpecialEffect("Reflects magic");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new CrystalSword();
    }
}
