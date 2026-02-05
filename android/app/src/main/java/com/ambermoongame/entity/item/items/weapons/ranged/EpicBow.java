package com.ambermoongame.entity.item.items.weapons.ranged;

import com.ambermoongame.entity.item.Item;
import entity.ProjectileEntity.ProjectileType;

/**
 * Epic Bow - A masterfully crafted bow.
 * Epic ranged weapon with high damage and crit chance.
 */
public class EpicBow extends Item {

    public EpicBow() {
        super("Epic Bow", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileType.ARROW, 22, 20.0f);
        setRarity(ItemRarity.EPIC);
        setDescription("A masterfully crafted bow");
        setAmmoItemName("arrow");
        setChargeable(true, 1.8f, 0, 2.8f);
        setChargeSpeedMultiplier(2.0f);
        setCritChance(0.15f);
        setScalesWithDexterity(true);
    }

    @Override
    public Item copy() {
        return new EpicBow();
    }
}
