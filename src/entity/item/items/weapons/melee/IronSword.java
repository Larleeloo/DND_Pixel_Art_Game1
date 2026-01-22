package entity.item.items.weapons.melee;

import entity.item.Item;

/**
 * Iron Sword - A reliable iron blade.
 * Standard melee weapon with balanced stats.
 */
public class IronSword extends Item {

    public IronSword() {
        super("Iron Sword", ItemCategory.WEAPON);
        setDamage(12);
        setAttackSpeed(1.0f);
        setRange(60);
        setRarity(ItemRarity.COMMON);
        setDescription("A reliable iron blade");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new IronSword();
    }
}
