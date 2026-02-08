# Loot Game App - Slot Machine Design

## Overview

The slot machine is a coin-based mini-game within the Loot Game App. Players
spend coins earned from daily/monthly chests to pull a 3-reel slot machine.
Matching symbols pay out coins according to a payout table.

## Machine Design

### Visual Layout
```
╔═══════════════════════════════════════╗
║            LUCKY SLOTS                ║
║                                       ║
║   Your Coins: ◈ 1,250                ║
║                                       ║
║   ┌─────────┬─────────┬─────────┐    ║
║   │ ╔═════╗ │ ╔═════╗ │ ╔═════╗ │    ║
║   │ ║     ║ │ ║     ║ │ ║     ║ │    ║
║   │ ║ 🍎  ║ │ ║ 💎  ║ │ ║ 🍎  ║ │    ║  ← Pay line
║   │ ║     ║ │ ║     ║ │ ║     ║ │    ║
║   │ ╚═════╝ │ ╚═════╝ │ ╚═════╝ │    ║
║   └─────────┴─────────┴─────────┘    ║
║                                       ║
║   ┌─────────────────────────────┐    ║
║   │      🔻  PULL  (◈25)       │    ║
║   └─────────────────────────────┘    ║
╚═══════════════════════════════════════╝
```

### Reel Symbols

Each reel contains the same 6 symbols, weighted differently. Symbols are
displayed as **static item images** (first frame of idle GIF) via `SlotReelView`:

| Symbol | Item ID (SlotReelView)    | Asset Path                        | Weight | Rarity     |
|--------|---------------------------|-----------------------------------|--------|------------|
| Apple  | apple                     | items/apple/idle.gif              | 30     | Common     |
| Sword  | iron_sword                | items/iron_sword/idle.gif         | 25     | Uncommon   |
| Shield | steel_shield              | items/steel_shield/idle.gif       | 20     | Rare       |
| Gem    | diamond                   | items/diamond/idle.gif            | 15     | Epic       |
| Star   | magic_crystal             | items/magic_crystal/idle.gif      | 8      | Legendary  |
| Crown  | ancient_crown             | items/ancient_crown/idle.gif      | 2      | Mythic     |

All symbols render as **static images** (first frame of idle.gif) within the
reel window via `SlotReelView`. Dedicated static PNGs can replace these when
provided. If an image fails to load, a **rarity-colored circle** is shown as
fallback (White=Common, Green=Uncommon, Blue=Rare, Purple=Epic, Orange=Legendary,
Cyan=Mythic).

## Reel Mechanics

### Reel Strip

Each reel has a virtual strip of 100 positions, populated according to weights:
```
Positions  0-29:  Apple   (30 positions)
Positions 30-54:  Sword   (25 positions)
Positions 55-74:  Shield  (20 positions)
Positions 75-89:  Gem     (15 positions)
Positions 90-97:  Star    (8 positions)
Positions 98-99:  Crown   (2 positions)
```

### Spin Animation

The slot machine uses `SlotReelView` to display static item images that
create a fun rolling animation:

```
Time 0.0s:  Player taps PULL button
            - 25 coins deducted from balance
            - All 3 SlotReelViews show spinning indicator (gray bars)
            - Results determined upfront via weighted random

Time 0.5s:  Reel 1 REVEALS
            - SlotReelView displays static item sprite image
            - Instant swap from spinning to final symbol

Time 1.0s:  Reel 2 REVEALS
            - Same instant reveal with item sprite

Time 1.5s:  Reel 3 REVEALS
            - Same instant reveal with item sprite
            - Result evaluation begins immediately

Time 1.5s+: Result Display
            - If NO MATCH: "No match" text in red
            - If DOUBLE MATCH: "WIN!" text in green + payout amount
            - If TRIPLE MATCH: "WIN!" text in green + payout amount
            - If JACKPOT (3x Crown): large payout display
```

### Jackpot Sequence (3x Crown)
```
Time 0.0s:  Screen flash (gold)
Time 0.1s:  All reels pulse with golden glow
Time 0.3s:  "JACKPOT!" text scales up from center
Time 0.5s:  Firework particles burst from all corners
Time 0.7s:  Coin shower animation (coins rain down screen)
Time 1.0s:  Payout counter: counts up from 0 to 5000
Time 2.0s:  Crown symbols continue to glow
Time 3.0s:  Animation settles, balance updated
```

## Payout Table

| Combination    | Payout | Probability | Avg Return/Pull |
|----------------|--------|-------------|-----------------|
| 3× Crown       | 5000   | 0.001%      | 0.050           |
| 3× Star        | 1000   | 0.051%      | 0.510           |
| 3× Gem         | 500    | 0.338%      | 1.690           |
| 3× Shield      | 200    | 0.800%      | 1.600           |
| 3× Sword       | 100    | 1.563%      | 1.563           |
| 3× Apple       | 50     | 2.700%      | 1.350           |
| Any 2 Match    | 10     | 50.19%      | 5.019           |
| No Match       | 0      | 44.36%      | 0.000           |

**Total expected return per 25-coin pull: 11.78 coins (47.1%)**

## Pull History

The last 20 pulls are stored and displayed below the machine:

```
─── Pull History ──────────────
#1:  🍎 💎 ⚔️  = No match
#2:  ⚔️ ⚔️ ⚔️  = ◈ 100  ← highlighted green
#3:  🍎 🍎 💎  = ◈ 10
#4:  🛡 💎 ⭐  = No match
#5:  🍎 🍎 🍎  = ◈ 50   ← highlighted green
     (scrollable)
```

- Wins highlighted in green
- Jackpots highlighted in gold with star icon
- Losses in dim gray
- Shows symbol icons + payout amount

## Statistics Display

Below pull history, lifetime stats are shown:

```
─── Slot Machine Stats ────────
Total Pulls:       290
Total Won:         ◈ 6,200
Total Spent:       ◈ 7,250
Net:               ◈ -1,050 (red)
Win Rate:          49.0%
Biggest Win:       ◈ 1,000
Current Streak:    3 losses
Best Win Streak:   7 wins
```

## Sound Design

| Event              | Sound File          | Volume | Notes               |
|--------------------|---------------------|--------|---------------------|
| Pull lever         | slot_pull.mp3       | 80%    | Mechanical lever    |
| Reel spinning      | reel_spin_loop.mp3  | 50%    | Looping whoosh      |
| Reel 1 stop        | reel_stop_1.mp3     | 70%    | Click-thunk         |
| Reel 2 stop        | reel_stop_2.mp3     | 70%    | Click-thunk         |
| Reel 3 stop        | reel_stop_3.mp3     | 75%    | Slightly louder     |
| Double match       | win_small.mp3       | 70%    | Cheerful chime      |
| Triple match       | win_medium.mp3      | 80%    | Fanfare             |
| Jackpot            | win_jackpot.mp3     | 100%   | Grand fanfare       |
| No match           | (silence)           | 0%     | No negative sound   |
| Coin count up      | coin_tick.mp3       | 40%    | Per-coin tick sound  |
| Insufficient coins | error_buzz.mp3      | 60%    | Gentle buzz         |

## Haptic Feedback

| Event              | Pattern                    | Intensity |
|--------------------|----------------------------|-----------|
| Pull lever         | Single pulse, 100ms        | Medium    |
| Reel stop (each)   | Single pulse, 50ms         | Light     |
| Double match       | Double pulse, 50ms+50ms    | Medium    |
| Triple match       | Triple pulse, 80ms each    | Strong    |
| Jackpot            | Long vibrate, 500ms        | Maximum   |
| Insufficient coins | Single buzz, 30ms          | Light     |

## Implementation Notes

### Random Number Generation

Use `java.security.SecureRandom` for reel outcomes to ensure fair randomness.
Each reel is independently determined at pull time (not pre-determined).

```java
public int[] spinReels() {
    SecureRandom rng = new SecureRandom();
    int[] results = new int[3];
    for (int i = 0; i < 3; i++) {
        int roll = rng.nextInt(100);
        if (roll < 30)      results[i] = APPLE;
        else if (roll < 55) results[i] = SWORD;
        else if (roll < 75) results[i] = SHIELD;
        else if (roll < 90) results[i] = GEM;
        else if (roll < 98) results[i] = STAR;
        else                results[i] = CROWN;
    }
    return results;
}
```

### Animation Implementation (SlotReelView)

Reel display uses `SlotReelView`, a custom View that renders static item images:

```java
// SlotReelView states:
// 1. setSpinning()  - shows spinning indicator (gray bars)
// 2. setSymbol(idx) - shows static item sprite for symbol index
//
// Symbol images are preloaded from item registry icons
// (first frame of idle.gif). Falls back to rarity-colored circles.

// SlotMachineTab animation uses Handler delays:
handler.postDelayed(() -> reelViews[0].setSymbol(results[0]), 500);
handler.postDelayed(() -> reelViews[1].setSymbol(results[1]), 1000);
handler.postDelayed(() -> reelViews[2].setSymbol(results[2]), 1500);
```

Future enhancement: Replace first-frame GIF icons with dedicated static PNGs
when they are provided, for a more polished slot machine appearance.

### Preventing Exploits

- Results are determined at pull time, not visible during spin
- No way to "time" the stop for a desired result
- Coin balance validated server-side if cloud sync is active
- Pull history is tamper-evident (stored with checksums)
