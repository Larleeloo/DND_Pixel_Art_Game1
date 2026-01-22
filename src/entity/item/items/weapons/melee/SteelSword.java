package entity.item.items.weapons.melee;

import entity.item.Item;

/**
 * Steel Sword - A well-crafted steel blade.
 * Uncommon melee weapon with improved damage.
 */
public class SteelSword extends Item {

    public SteelSword() {
        super("Steel Sword", ItemCategory.WEAPON);
        setDamage(18);
        setAttackSpeed(1.0f);
        setRange(65);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A well-crafted steel blade");
        setScalesWithStrength(true);
    }

    @Override
    public Item copy() {
        return new SteelSword();
    }
}
