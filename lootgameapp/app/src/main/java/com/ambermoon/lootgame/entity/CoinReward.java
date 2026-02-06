package com.ambermoon.lootgame.entity;

import java.util.Random;

/**
 * CoinReward handles coin reward calculations for chests.
 *
 * Daily chests award 50-260 coins depending on streak bonus.
 * Monthly chests award 500-2000 coins.
 *
 * Usage:
 *   int dailyCoins = CoinReward.calculateDaily(consecutiveDays);
 *   int monthlyCoins = CoinReward.calculateMonthly();
 */
public class CoinReward {
    private static final Random random = new Random();

    /**
     * Calculates daily chest coin reward.
     *
     * Base: 50 coins
     * Random bonus: 10-160 coins
     * Streak bonus: +5 per consecutive day (max +50)
     *
     * Total range: 65-260 coins
     *
     * @param consecutiveDays Number of consecutive daily login days
     * @return Coin reward amount
     */
    public static int calculateDaily(int consecutiveDays) {
        int base = 50;
        int bonus = random.nextInt(151) + 10; // 10-160
        int streak = Math.min(consecutiveDays * 5, 50);
        return base + bonus + streak;
    }

    /**
     * Calculates monthly chest coin reward.
     *
     * Base: 500 coins
     * Random bonus: 100-1500 coins
     *
     * Total range: 600-2000 coins
     *
     * @return Coin reward amount
     */
    public static int calculateMonthly() {
        int base = 500;
        int bonus = random.nextInt(1401) + 100; // 100-1500
        return base + bonus;
    }
}
