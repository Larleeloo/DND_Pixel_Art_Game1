package com.ambermoon.lootgame.entity.items.materials;

import com.ambermoon.lootgame.entity.Item;

public class LavaStone extends Item {

    public LavaStone() {
        super("Lava Stone", ItemCategory.MATERIAL);
        setRarity(ItemRarity.UNCOMMON);
        setDescription("A stone formed from cooled lava");
    }

    @Override
    public Item copy() {
        return new LavaStone();
    }
}
