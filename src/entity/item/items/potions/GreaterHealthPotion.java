package entity.item.items.potions;

import entity.item.Item;

/**
 * Greater Health Potion - Fully restores health.
 * Uncommon potion for full health recovery.
 */
public class GreaterHealthPotion extends Item {

    public GreaterHealthPotion() {
        super("Greater Health Potion", ItemCategory.POTION);
        setHealthRestore(100);
        setManaRestore(0);
        setStaminaRestore(0);
        setConsumeTime(0.5f);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Fully restores health");
    }

    @Override
    public Item copy() {
        return new GreaterHealthPotion();
    }
}
