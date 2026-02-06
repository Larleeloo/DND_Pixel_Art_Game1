package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ProjectileEntity;

/**
 * Longbow - Greater range and power.
 * Uncommon ranged weapon with improved stats and chargeable shots.
 */
public class Longbow extends Item {

    public Longbow() {
        super("Longbow", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_ARROW, 15, 18.0f);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Greater range and power");
        setAmmoItemName("arrow");
        setChargeable(true, 2.5f, 0, 2.5f);
        setChargeSpeedMultiplier(1.8f);
        setScalesWithDexterity(true);
    }

    @Override
    public Item copy() {
        return new Longbow();
    }
}
