package com.ambermoongame.entity.item.items.weapons.throwing;

import com.ambermoongame.entity.item.Item;
import entity.ProjectileEntity.ProjectileType;

/**
 * Throwing Knife - Quick and accurate.
 * Common throwable weapon that stacks.
 */
public class ThrowingKnife extends Item {

    public ThrowingKnife() {
        super("Throwing Knife", ItemCategory.THROWABLE);
        setRangedWeapon(true, ProjectileType.THROWING_KNIFE, 10, 18.0f);
        setRarity(ItemRarity.COMMON);
        setDescription("Quick and accurate");
        setStackable(true);
        setMaxStackSize(16);
        setScalesWithDexterity(true);
    }

    @Override
    public Item copy() {
        return new ThrowingKnife();
    }
}
