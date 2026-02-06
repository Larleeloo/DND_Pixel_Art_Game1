package com.ambermoon.lootgame.entity.items.weapons.throwing;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ProjectileEntity;

/**
 * Rock - A simple projectile.
 * Common throwable weapon that stacks.
 */
public class Rock extends Item {

    public Rock() {
        super("Rock", ItemCategory.THROWABLE);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_ROCK, 5, 12.0f);
        setRarity(ItemRarity.COMMON);
        setDescription("A simple projectile");
        setStackable(true);
        setMaxStackSize(16);
        setScalesWithDexterity(true);
    }

    @Override
    public Item copy() {
        return new Rock();
    }
}
