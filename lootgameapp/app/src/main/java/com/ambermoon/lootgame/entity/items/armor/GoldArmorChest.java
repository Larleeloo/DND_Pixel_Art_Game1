package com.ambermoon.lootgame.entity.items.armor;

import com.ambermoon.lootgame.entity.Item;

/**
 * Gold Armor Chestplate - Gleaming golden protection.
 * Uncommon armor with moderate defense.
 */
public class GoldArmorChest extends Item {

    public GoldArmorChest() {
        super("Gold Armor Chestplate", ItemCategory.ARMOR);
        setDefense(12);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Gleaming golden protection");
    }

    @Override
    public Item copy() {
        return new GoldArmorChest();
    }
}
