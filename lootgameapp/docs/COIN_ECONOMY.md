# Loot Game App - Coin Economy Design

## Overview

Coins are the secondary currency in the Loot Game App. They serve as a fun
gambling mechanic through the slot machine. Coins have no pay-to-win impact -
they exist purely for entertainment value alongside the item collection loop.

## Coin Sources

### Daily Chest (Every 24 Hours)
```
Base reward:       50 coins (guaranteed)
Random bonus:      10-160 coins (uniform distribution)
Streak bonus:      +5 per consecutive day (caps at +50)

Minimum per open:  60 coins  (50 base + 10 minimum random)
Maximum per open:  260 coins (50 base + 160 random + 50 streak cap)
Average per open:  ~135 coins
```

### Monthly Chest (Every 30 Days)
```
Base reward:       500 coins (guaranteed)
Random bonus:      100-1500 coins (uniform distribution)

Minimum per open:  600 coins
Maximum per open:  2000 coins
Average per open:  ~1300 coins
```

### First Launch Bonus
```
One-time:          500 coins
Purpose:           Give new players enough to try the slot machine 20 times
```

### Consecutive Day Streak
```
Formula:           min(consecutiveDays * 5, 50)
Day 1:             +5 bonus
Day 2:             +10 bonus
Day 5:             +25 bonus
Day 10+:           +50 bonus (capped)

Streak resets if a day is missed entirely (no daily chest opened).
```

## Coin Sinks

### Slot Machine Pull
```
Cost per pull:     25 coins
```

This is the only coin sink in the game. Coins cannot be exchanged for items,
used in crafting, or traded. This simplicity keeps the economy balanced.

## Monthly Income Projection

Assuming a player opens their daily chest every day:

```
Daily chest coins:     ~135/day average × 30 days = 4,050/month
Monthly chest coins:   ~1,300/month
Total monthly income:  ~5,350 coins/month
```

### Monthly Spend Capacity

```
Pulls per month:       5,350 / 25 = ~214 pulls
Expected return:       214 × 25 × 0.85 = ~4,547 coins back
Net monthly loss:      ~803 coins (the "entertainment tax")
```

This means players who use the slot machine heavily will slowly deplete their
coin balance, but the daily/monthly chests replenish enough to keep playing
indefinitely. Casual slot players will accumulate coins over time.

## Slot Machine Mathematics

### Symbol Weights (Per Reel)
```
Apple  (Common):     30/100 = 30%
Sword  (Uncommon):   25/100 = 25%
Shield (Rare):       20/100 = 20%
Gem    (Epic):       15/100 = 15%
Star   (Legendary):   8/100 =  8%
Crown  (Mythic):      2/100 =  2%
Total:              100
```

### Triple Match Probabilities
```
3× Apple:    (30/100)³ = 2.700%  → pays 50
3× Sword:    (25/100)³ = 1.563%  → pays 100
3× Shield:   (20/100)³ = 0.800%  → pays 200
3× Gem:      (15/100)³ = 0.338%  → pays 500
3× Star:     (8/100)³  = 0.051%  → pays 1000
3× Crown:    (2/100)³  = 0.001%  → pays 5000

Total triple probability: 5.453%
```

### Double Match Probabilities
```
Any 2 matching (not 3): calculated as
  For each symbol S with weight w:
    P(exactly 2 of S) = 3 × w² × (1-w)

  Apple:   3 × 0.30² × 0.70 = 18.90%
  Sword:   3 × 0.25² × 0.75 = 14.06%
  Shield:  3 × 0.20² × 0.80 = 9.60%
  Gem:     3 × 0.15² × 0.85 = 5.74%
  Star:    3 × 0.08² × 0.92 = 1.77%
  Crown:   3 × 0.02² × 0.98 = 0.12%

Total double (not triple): 50.19%
Double match pays: 10 coins
```

### No Match Probability
```
No match: 1 - 5.453% - 50.19% = 44.36%
No match pays: 0 coins
```

### Expected Value Per Pull
```
EV = (0.02700 × 50) + (0.01563 × 100) + (0.00800 × 200) +
     (0.00338 × 500) + (0.00051 × 1000) + (0.00001 × 5000) +
     (0.50190 × 10) + (0.44357 × 0)

EV = 1.350 + 1.563 + 1.600 + 1.690 + 0.510 + 0.050 + 5.019 + 0
EV = 11.782 coins per 25-coin pull

Return rate: 11.782 / 25 = 47.1%
House edge: 52.9%
```

Note: The house edge is high, but coins are free (earned from chests). This
ensures coins remain a finite resource that requires daily engagement to
replenish, giving players a reason to return each day.

## Coin Display

### Header Bar
```
◈ 1,250
```
- Animated coin sprite (spinning GIF)
- Number updates with count-up/count-down animation
- Flash green on gain, flash red on spend
- Large font, always visible regardless of active tab

### Chest Reward Display
```
┌────────────────┐
│  ◈ +175 coins  │  ← Appears after item reveal
│  (Streak: +25) │  ← Shows streak bonus if applicable
└────────────────┘
```

### Slot Machine Display
```
Your Coins: ◈ 1,250
Cost: ◈ 25

[After pull]
Result: ⚔⚔⚔
Payout: ◈ +100
Balance: ◈ 1,325
```

## Save Data Structure

```json
{
  "coins": 1250,
  "totalCoinsEarned": 8500,
  "totalCoinsSpent": 7250,
  "consecutiveDays": 7,
  "lastLoginDate": "2025-01-15",
  "slotMachinePulls": 290,
  "biggestJackpot": 1000,
  "slotMachineWins": 142,
  "slotMachineLosses": 148,
  "totalSlotWinnings": 6200,
  "totalSlotSpent": 7250
}
```
