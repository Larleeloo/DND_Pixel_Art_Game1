package com.ambermoon.lootgame.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * LootTable provides weighted random item generation for chests and rewards.
 *
 * Uses rarity weights to determine the likelihood of generating items
 * of each rarity tier. A rarityBoost parameter allows adjusting the
 * distribution (higher boost = more rare items).
 *
 * Rarity tiers (index order):
 *   0 = Common, 1 = Uncommon, 2 = Rare, 3 = Epic, 4 = Legendary, 5 = Mythic
 *
 * Usage:
 *   List<Item> dailyLoot = LootTable.generateLoot(3, 1.0f);   // 3 items, normal rates
 *   List<Item> monthlyLoot = LootTable.generateLoot(5, 2.0f);  // 5 items, boosted rates
 */
public class LootTable {
    private static final Random random = new Random();

    // Rarity weights for daily chest (rarityBoost = 1.0)
    private static final int[] BASE_WEIGHTS = {1000, 500, 250, 100, 3, 1}; // common..mythic

    /**
     * Generates a list of random loot items.
     *
     * @param count Number of items to generate
     * @param rarityBoost Multiplier for rare item chances (1.0 = normal, 2.0 = boosted)
     * @return List of generated items
     */
    public static List<Item> generateLoot(int count, float rarityBoost) {
        List<Item> loot = new ArrayList<>();
        Set<String> allIds = ItemRegistry.getAllItemIds();
        List<String> idList = new ArrayList<>(allIds);
        if (idList.isEmpty()) return loot;

        for (int i = 0; i < count; i++) {
            int targetRarity = rollRarity(rarityBoost);
            // Find items matching target rarity, fallback to any
            List<String> candidates = new ArrayList<>();
            for (String id : idList) {
                Item template = ItemRegistry.getTemplate(id);
                if (template != null && template.getRarity().ordinal() == targetRarity) {
                    candidates.add(id);
                }
            }
            if (candidates.isEmpty()) candidates = idList;
            String chosenId = candidates.get(random.nextInt(candidates.size()));
            Item item = ItemRegistry.create(chosenId);
            if (item != null) loot.add(item);
        }
        return loot;
    }

    /**
     * Rolls a rarity tier based on weighted random selection.
     *
     * @param boost Rarity boost multiplier. Higher values reduce common weight
     *              and increase rare/epic/legendary/mythic weights.
     * @return Rarity tier index (0-5)
     */
    private static int rollRarity(float boost) {
        // Apply boost: higher boost = more rare items
        int[] weights = new int[6];
        weights[0] = (int)(BASE_WEIGHTS[0] / Math.max(1, boost));
        for (int i = 1; i < 6; i++) {
            weights[i] = (int)(BASE_WEIGHTS[i] * boost);
        }
        int total = 0;
        for (int w : weights) total += w;
        int roll = random.nextInt(total);
        int cumulative = 0;
        for (int i = 0; i < 6; i++) {
            cumulative += weights[i];
            if (roll < cumulative) return i;
        }
        return 0;
    }
}
