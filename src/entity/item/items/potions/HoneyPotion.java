package entity.item.items.potions;

import entity.item.Item;

/**
 * Honey Potion - Sweet and restorative.
 * Common potion that restores health, mana, and stamina.
 */
public class HoneyPotion extends Item {

    public HoneyPotion() {
        super("Honey Potion", ItemCategory.POTION);
        setHealthRestore(25);
        setManaRestore(10);
        setStaminaRestore(20);
        setConsumeTime(0.5f);
        setRarity(ItemRarity.COMMON);
        setDescription("Sweet and restorative");
    }

    @Override
    public Item copy() {
        return new HoneyPotion();
    }
}
