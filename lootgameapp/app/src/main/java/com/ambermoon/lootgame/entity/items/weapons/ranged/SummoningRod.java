package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ProjectileEntity;

/**
 * Summoning Rod - Calls forth magical entities.
 * Rare ranged weapon that summons spectral allies.
 */
public class SummoningRod extends Item {

    public SummoningRod() {
        super("Summoning Rod", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_MAGIC_BOLT, 18, 10.0f);
        setRarity(ItemRarity.RARE);
        setDescription("Calls forth magical entities");
        setSpecialEffect("Summons spectral allies");
        setAmmoItemName("mana");
        setChargeable(true, 3.0f, 35, 2.5f);
        setScalesWithIntelligence(true);
    }

    @Override
    public Item copy() {
        return new SummoningRod();
    }
}
