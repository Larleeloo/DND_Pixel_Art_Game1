package entity.item.items.ammo;

import entity.item.Item;
import entity.ProjectileEntity.ProjectileType;

/**
 * Arrow - Standard ammunition for bows.
 * Common stackable ammo item.
 */
public class Arrow extends Item {

    public Arrow() {
        super("Arrow", ItemCategory.MATERIAL);
        setDamage(5);
        setRarity(ItemRarity.COMMON);
        setDescription("Standard ammunition for bows");
        setStackable(true);
        setMaxStackSize(16);
        setRangedWeapon(false, ProjectileType.ARROW, 5, 0);
    }

    @Override
    public Item copy() {
        return new Arrow();
    }
}
