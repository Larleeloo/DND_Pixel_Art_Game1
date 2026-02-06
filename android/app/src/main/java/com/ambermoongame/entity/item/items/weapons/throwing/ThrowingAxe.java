package com.ambermoongame.entity.item.items.weapons.throwing;

import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.ProjectileEntity.ProjectileType;

/**
 * Throwing Axe - Heavy but powerful.
 * Uncommon throwable weapon that stacks.
 */
public class ThrowingAxe extends Item {

    public ThrowingAxe() {
        super("Throwing Axe", ItemCategory.THROWABLE);
        setRangedWeapon(true, ProjectileType.THROWING_AXE, 18, 14.0f);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Heavy but powerful");
        setStackable(true);
        setMaxStackSize(16);
        setScalesWithDexterity(true);
    }

    @Override
    public Item copy() {
        return new ThrowingAxe();
    }
}
