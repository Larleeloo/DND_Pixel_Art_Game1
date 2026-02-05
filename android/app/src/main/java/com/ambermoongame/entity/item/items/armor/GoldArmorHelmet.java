package com.ambermoongame.entity.item.items.armor;

import com.ambermoongame.entity.item.Item;

/**
 * Gold Armor Helmet - Ornate golden headpiece.
 * Uncommon armor with moderate defense.
 */
public class GoldArmorHelmet extends Item {

    public GoldArmorHelmet() {
        super("Gold Armor Helmet", ItemCategory.ARMOR);
        setDefense(6);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("Ornate golden headpiece");
    }

    @Override
    public Item copy() {
        return new GoldArmorHelmet();
    }
}
