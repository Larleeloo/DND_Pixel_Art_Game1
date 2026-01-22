package entity.item.items.weapons.throwing;

import entity.item.Item;
import entity.ProjectileEntity.ProjectileType;

/**
 * Rock - A simple projectile.
 * Common throwable weapon that stacks.
 */
public class Rock extends Item {

    public Rock() {
        super("Rock", ItemCategory.THROWABLE);
        setRangedWeapon(true, ProjectileType.ROCK, 5, 12.0f);
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
