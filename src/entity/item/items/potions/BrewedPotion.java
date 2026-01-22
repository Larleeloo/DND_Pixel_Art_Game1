package entity.item.items.potions;

import entity.item.Item;

/**
 * Brewed Potion - A carefully brewed elixir.
 * Common potion that restores health and mana.
 */
public class BrewedPotion extends Item {

    public BrewedPotion() {
        super("Brewed Potion", ItemCategory.POTION);
        setHealthRestore(30);
        setManaRestore(15);
        setStaminaRestore(0);
        setConsumeTime(0.5f);
        setRarity(ItemRarity.COMMON);
        setDescription("A carefully brewed elixir");
    }

    @Override
    public Item copy() {
        return new BrewedPotion();
    }
}
