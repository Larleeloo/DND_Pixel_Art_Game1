package com.ambermoon.lootgame.entity.items.throwables;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ProjectileEntity;

/**
 * Bomb - Explodes on impact.
 * Uncommon consumable throwable with area damage.
 */
public class Bomb extends Item {

    public Bomb() {
        super("Bomb", ItemCategory.THROWABLE);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_BOMB, 40, 14.0f);
        setRarity(ItemRarity.RARE);
        setDescription("Explodes on impact");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Bomb();
    }
}
