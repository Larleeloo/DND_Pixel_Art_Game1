package com.ambermoon.lootgame.entity.items.weapons.ranged;

import com.ambermoon.lootgame.entity.Item;
import com.ambermoon.lootgame.entity.ProjectileEntity;

/**
 * Phoenix Bow - Rises from the ashes.
 * Legendary ranged weapon with fire arrows that resurrect. Requires Wisdom.
 */
public class PhoenixBow extends Item {

    public PhoenixBow() {
        super("Phoenix Bow", ItemCategory.RANGED_WEAPON);
        setRangedWeapon(true, ProjectileEntity.PROJECTILE_ARROW, 35, 22.0f);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("Rises from the ashes");
        setSpecialEffect("Fire arrows that resurrect");
        setAmmoItemName("arrow");
        setChargeable(true, 2.0f, 0, 3.5f);
        setWisdomRequirement(7);
        setScalesWithDexterity(true);
    }

    @Override
    public Item copy() {
        return new PhoenixBow();
    }
}
