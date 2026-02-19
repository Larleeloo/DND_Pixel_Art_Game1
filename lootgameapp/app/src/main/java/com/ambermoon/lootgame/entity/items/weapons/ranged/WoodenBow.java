package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ProjectileEntity;

/**
 * Wooden Bow - A simple hunting bow.
 * Common ranged weapon with chargeable shots.
 */
public class WoodenBow extends Item {

    public WoodenBow() {
        super("Bow", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_ARROW, 8, 12.0f);
        setRarity(ItemRarity.COMMON);
        setDescription("A simple wooden bow");
        setAmmoItemName("arrow");
        setChargeable(true, 2.0f, 0, 2.0f);
        setChargeSpeedMultiplier(1.5f);
        setScalesWithDexterity(true);
    }

    @Override
    public Item copy() {
        return new WoodenBow();
    }
}
