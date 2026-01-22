package entity.item.items.food;

import entity.item.Item;

/**
 * Cake - A delicious layered cake.
 * Uncommon food item that restores health and mana.
 */
public class Cake extends Item {

    public Cake() {
        super("Cake", ItemCategory.FOOD);
        setHealthRestore(35);
        setManaRestore(5);
        setStaminaRestore(15);
        setConsumeTime(1.5f);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A delicious layered cake");
    }

    @Override
    public Item copy() {
        return new Cake();
    }
}
