package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

public class Gramophone extends Item {

    public Gramophone() {
        super("Gramophone", ItemCategory.OTHER);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("An enchanted gramophone that plays otherworldly music");
    }

    @Override
    public Item copy() {
        return new Gramophone();
    }
}
