package com.ambermoon.lootgame.entity.items.ammo;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ProjectileEntity;

/**
 * Heavy Bolt - Heavier bolts for more damage.
 * Uncommon stackable ammo with higher damage.
 */
public class HeavyBolt extends Item {

    public HeavyBolt() {
        super("Heavy Bolt", ItemCategory.MATERIAL);
        setDamage(12);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Heavier bolts for more damage");
        setStackable(true);
        setMaxStackSize(16);
        setRangedWeapon(false, ProjectileEntity.PROJECTILE_BOLT, 12, 0);
    }

    @Override
    public Item copy() {
        return new HeavyBolt();
    }
}
