package entity.item.items.ammo;

import entity.item.Item;
import entity.ProjectileEntity.ProjectileType;

/**
 * Bolt - Standard crossbow ammunition.
 * Common stackable ammo item.
 */
public class Bolt extends Item {

    public Bolt() {
        super("Bolt", ItemCategory.MATERIAL);
        setDamage(8);
        setRarity(ItemRarity.COMMON);
        setDescription("Standard crossbow ammunition");
        setStackable(true);
        setMaxStackSize(16);
        setRangedWeapon(false, ProjectileType.BOLT, 8, 0);
    }

    @Override
    public Item copy() {
        return new Bolt();
    }
}
