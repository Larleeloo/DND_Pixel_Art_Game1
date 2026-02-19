package com.ambermoon.lootgame.entity.items.collectibles;

import com.ambermoon.lootgame.entity.Item;

public class MirrorToOtherRealms extends Item {

    public MirrorToOtherRealms() {
        super("Mirror to Other Realms", ItemCategory.OTHER);
        setRarity(ItemRarity.LEGENDARY);
        setDescription("A shimmering mirror revealing other dimensions");
        setSpecialEffect("Peer into other realms");
    }

    @Override
    public Item copy() {
        return new MirrorToOtherRealms();
    }
}
