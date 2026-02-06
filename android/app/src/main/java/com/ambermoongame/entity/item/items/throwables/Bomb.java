package com.ambermoongame.entity.item.items.throwables;

import com.ambermoongame.entity.item.Item;
import com.ambermoongame.entity.ProjectileEntity.ProjectileType;

/**
 * Bomb - Explodes on impact.
 * Uncommon consumable throwable with area damage.
 */
public class Bomb extends Item {

    public Bomb() {
        super("Bomb", ItemCategory.THROWABLE);
        setRangedWeapon(true, ProjectileType.BOMB, 40, 14.0f);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Explodes on impact");
        setStackable(true);
        setMaxStackSize(16);
    }

    @Override
    public Item copy() {
        return new Bomb();
    }
}
