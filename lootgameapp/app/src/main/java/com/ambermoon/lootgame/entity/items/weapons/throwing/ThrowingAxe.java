package com.ambermoon.lootgame.entity.items.weapons.throwing;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ProjectileEntity;

/**
 * Throwing Axe - Heavy but powerful.
 * Uncommon throwable weapon that stacks.
 */
public class ThrowingAxe extends Item {

    public ThrowingAxe() {
        super("Throwing Axe", ItemCategory.THROWABLE);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_THROWING_AXE, 18, 14.0f);
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
