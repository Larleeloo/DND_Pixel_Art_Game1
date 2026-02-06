package com.ambermoongame.entity.item.items.weapons.ranged;

import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.ProjectileEntity.ProjectileType;

/**
 * Heavy Crossbow - Devastating power.
 * Rare ranged weapon with very high damage but slow attack speed.
 */
public class HeavyCrossbow extends Item {

    public HeavyCrossbow() {
        super("Heavy Crossbow", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileType.BOLT, 30, 22.0f);
        setRarity(ItemRarity.RARE);
        setDescription("Devastating power");
        setAttackSpeed(0.5f);
        setAmmoItemName("bolt");
        setScalesWithDexterity(true);
    }

    @Override
    public Item copy() {
        return new HeavyCrossbow();
    }
}
