package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

/**
 * Wizard Hat - Increases mana regen.
 * Uncommon armor with magic bonus.
 */
public class WizardHat extends Item {

    public WizardHat() {
        super("Wizard Hat", ItemCategory.ARMOR);
        setDefense(2);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Increases mana regen");
        setSpecialEffect("+10% mana regeneration");
    }

    @Override
    public Item copy() {
        return new WizardHat();
    }
}
