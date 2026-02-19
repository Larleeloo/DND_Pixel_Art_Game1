package com.ambermoon.lootgame.entity.items.tools;

import com.ambermoon.lootgame.entity.Item;

public class MagicShovel extends Item {

    public MagicShovel() {
        super("Magic Shovel", ItemCategory.TOOL);
        setRarity(ItemRarity.EPIC);
        setDescription("An enchanted shovel that digs effortlessly");
        setSpecialEffect("Digs through any material");
    }

    @Override
    public Item copy() {
        return new MagicShovel();
    }
}
