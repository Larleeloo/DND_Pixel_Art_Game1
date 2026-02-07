# Loot Game App - Design & Implementation Plan

TITLE: The Amber Moon - Loot Game
AUTHOR: Larson Sonderman & CLAUDE AI
PLATFORM: Android (standalone app)
PACKAGE: com.ambermoon.lootgame

## Overview

A standalone Android app focused entirely on the loot collection, crafting, and
gambling aspects of The Amber Moon. This is NOT a platformer - it is a tab-based
inventory management game where players collect items from timed loot chests,
craft new items via alchemy, deconstruct items into components, and gamble coins
on a slot machine. All item sprites use animated GIFs.

This app is completely separate from the main Amber Moon Android port. It shares
the same item definitions, rarity system, alchemy recipes, and asset files, but
has its own project structure, UI paradigm (tabbed interface), and unique features
(coin economy, slot machine).

--------------------------------------------------------------------------------
## 1. APP ARCHITECTURE
--------------------------------------------------------------------------------

### 1.1 Project Structure

```
lootgameapp/
├── PLAN.md                          # This file
├── app/
│   ├── src/main/
│   │   ├── java/com/ambermoon/lootgame/
│   │   │   ├── core/               # App entry, activities, game loop
│   │   │   │   ├── MainActivity.java        # Launcher → LoginActivity or TabActivity
│   │   │   │   ├── LoginActivity.java       # Google Drive login + sync screen
│   │   │   │   ├── TabActivity.java         # Main tabbed interface host
│   │   │   │   └── GamePreferences.java     # SharedPreferences wrapper
│   │   │   │
│   │   │   ├── tabs/               # Tab implementations (each a Fragment-like view)
│   │   │   │   ├── DailyChestTab.java       # Daily loot chest tab
│   │   │   │   ├── MonthlyChestTab.java     # Monthly loot chest tab
│   │   │   │   ├── AlchemyTab.java          # Alchemy crafting tab
│   │   │   │   ├── DeconstructTab.java      # Item deconstruction tab
│   │   │   │   ├── SlotMachineTab.java      # Coin slot machine tab
│   │   │   │   └── VaultTab.java            # Vault inventory browser (bonus tab)
│   │   │   │
│   │   │   ├── entity/             # Item and data model classes
│   │   │   │   ├── Item.java                # Item base class (reused from main game)
│   │   │   │   ├── ItemCategory.java        # Category enum
│   │   │   │   ├── ItemRarity.java          # Rarity enum with colors
│   │   │   │   ├── ItemRegistry.java        # Registry of all 198+ items
│   │   │   │   ├── RecipeManager.java       # Alchemy recipe matching
│   │   │   │   ├── Recipe.java              # Recipe data class
│   │   │   │   ├── LootTable.java           # Weighted random item selection
│   │   │   │   ├── CoinReward.java          # Daily/monthly coin reward calculation
│   │   │   │   └── items/                   # 198 individual item class files
│   │   │   │       ├── weapons/melee/       # 22 melee weapons
│   │   │   │       ├── weapons/ranged/      # 16 ranged weapons
│   │   │   │       ├── weapons/throwing/    # 3 throwing weapons
│   │   │   │       ├── armor/               # 20 armor pieces
│   │   │   │       ├── tools/               # 10 tools
│   │   │   │       ├── food/                # 11 food items
│   │   │   │       ├── potions/             # 14 potions
│   │   │   │       ├── materials/           # 24 materials
│   │   │   │       ├── collectibles/        # 36 collectibles
│   │   │   │       ├── accessories/         # 1 accessory
│   │   │   │       ├── clothing/            # 12 clothing
│   │   │   │       ├── keys/               # 4 keys
│   │   │   │       ├── blocks/              # 17 blocks
│   │   │   │       ├── ammo/               # 6 ammo types
│   │   │   │       └── throwables/          # 2 throwables
│   │   │   │
│   │   │   ├── ui/                 # Shared UI components
│   │   │   │   ├── ItemSlotView.java        # Single item slot (drag source/target)
│   │   │   │   ├── ItemGridView.java        # Scrollable grid of ItemSlotViews
│   │   │   │   ├── ItemTooltip.java         # Hover/long-press item details popup
│   │   │   │   ├── RarityColors.java        # Color constants per rarity
│   │   │   │   ├── TabBar.java              # Bottom tab navigation bar
│   │   │   │   ├── ChestOpenAnimation.java  # Chest opening visual + particles
│   │   │   │   ├── LootBeamEffect.java      # Rarity-colored light beams
│   │   │   │   ├── SlotMachineView.java     # Slot machine reels + lever
│   │   │   │   ├── CoinDisplay.java         # Coin counter in header bar
│   │   │   │   ├── TimerDisplay.java        # Countdown timer for chests
│   │   │   │   └── DragDropManager.java     # Cross-view drag-and-drop handling
│   │   │   │
│   │   │   ├── graphics/           # Asset loading and GIF rendering
│   │   │   │   ├── GifDecoder.java          # Full GIF frame extraction
│   │   │   │   ├── AnimatedSprite.java      # Animated GIF rendering component
│   │   │   │   ├── AssetLoader.java         # Asset loading with caching
│   │   │   │   └── ParticleSystem.java      # Lightweight particle effects
│   │   │   │
│   │   │   ├── save/               # Persistence and cloud sync
│   │   │   │   ├── SaveManager.java         # Local JSON persistence
│   │   │   │   ├── GoogleDriveSyncManager.java # Google Drive cloud sync
│   │   │   │   ├── SaveData.java            # Save data model class
│   │   │   │   └── ConflictResolver.java    # Cloud sync conflict resolution
│   │   │   │
│   │   │   └── audio/              # Sound effects
│   │   │       └── SoundManager.java        # SoundPool-based audio
│   │   │
│   │   ├── res/
│   │   │   ├── drawable/           # App icons, UI backgrounds
│   │   │   ├── layout/             # XML layouts for activities/tabs
│   │   │   ├── values/             # Colors, strings, styles, dimens
│   │   │   ├── anim/               # Tab transition animations
│   │   │   └── xml/                # Backup rules, network security
│   │   │
│   │   ├── assets/                 # Game assets (shared from main game)
│   │   │   ├── items/              # Item GIF sprites (198+)
│   │   │   ├── chests/             # Chest opening GIFs
│   │   │   ├── ui/                 # UI element sprites
│   │   │   └── data/               # alchemy_recipes.json
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   └── libs/                       # External JARs if needed
│
├── scripts/
│   ├── build.sh                    # Linux/Mac build script
│   └── build.bat                   # Windows build script
│
└── docs/
    ├── UI_MOCKUPS.md               # Tab layout descriptions
    ├── COIN_ECONOMY.md             # Coin system design
    └── SLOT_MACHINE.md             # Slot machine mechanics
```

### 1.2 Technology Choices

| Component         | Choice                    | Rationale                              |
|-------------------|---------------------------|----------------------------------------|
| UI Framework      | Custom Canvas + Views     | Consistent with existing Android port  |
| Rendering         | Android Canvas API        | 2D sprites, no OpenGL needed           |
| GIF Decoding      | Custom GifDecoder         | Full frame extraction, no external lib |
| Build System      | Command-line (aapt2/d8)   | Consistent with existing Android port  |
| Persistence       | JSON files                | Compatible with desktop save format    |
| Cloud Sync        | Google Drive API v3       | Public file download, token-based upload |
| Audio             | SoundPool                 | Lightweight, low-latency effects       |
| Target SDK        | API 34 (Android 14)       | Same as existing port                  |
| Min SDK           | API 24 (Android 7.0)      | Same as existing port                  |

### 1.3 Key Design Decisions

1. **Tab-Based UI, Not Platformer**: No player movement, no physics, no camera.
   The entire app is a vertical scrollable interface navigated by bottom tabs.

2. **Portrait Orientation**: Unlike the landscape main game, the loot app runs
   in portrait mode - natural for phone use, better for vertical item grids.

3. **Shared Item Data**: Item classes, registry, and recipe JSON are copied from
   the main game but adapted for Android Bitmap rendering instead of BufferedImage.

4. **Coin Economy**: New system not present in the desktop game. Coins are
   earned from chests (guaranteed, variable amount) and spent on slot machine
   pulls. Coins have no other use to keep the economy simple.

5. **Universal GIF Support**: Every sprite in the app renders as animated GIF.
   A custom GifDecoder extracts all frames and per-frame delays from GIF files,
   feeding them to AnimatedSprite for smooth playback.

--------------------------------------------------------------------------------
## 2. SCREENS & NAVIGATION FLOW
--------------------------------------------------------------------------------

### 2.1 Navigation Flow

```
App Launch
    │
    ▼
[MainActivity] ─── Check saved login ──► [TabActivity] (if token exists)
    │                                         │
    ▼                                         │
[LoginActivity]                               │
    │ Google Drive access token entered       │
    │ Initial sync from cloud                 │
    ▼                                         │
[TabActivity] ◄───────────────────────────────┘
    │
    ├── Tab 1: Daily Chest
    ├── Tab 2: Monthly Chest
    ├── Tab 3: Alchemy Crafting
    ├── Tab 4: Deconstruction
    ├── Tab 5: Slot Machine
    └── Tab 6: Vault (item browser/storage)
```

### 2.2 Login Screen (LoginActivity)

**Purpose**: Google Drive authentication for cross-device vault synchronization.

**Layout (Portrait)**:
```
┌──────────────────────────┐
│                          │
│    [Amber Moon Logo]     │
│    ── Loot Game ──       │
│                          │
│  ┌────────────────────┐  │
│  │ Google Drive Access │  │
│  │ Token (paste)       │  │
│  └────────────────────┘  │
│                          │
│  [  Connect & Sync  ]   │
│                          │
│  [  Play Offline   ]    │
│                          │
│  ┌────────────────────┐  │
│  │ Sync Status:       │  │
│  │ "Not connected"    │  │
│  └────────────────────┘  │
│                          │
│  "Sync your vault items │
│   across devices via    │
│   Google Drive."        │
│                          │
└──────────────────────────┘
```

**Features**:
- Google OAuth access token input (Drive file access required)
- "Connect & Sync" validates token, pulls cloud save, merges vault
- "Play Offline" skips login, uses local save only
- Sync status indicator (connected/syncing/error/offline)
- Token stored in SharedPreferences
- Auto-login on subsequent launches if token is saved
- Manual logout button in settings (accessible from any tab)

**Google Drive Sync Details**:
- File: Public Google Drive file (ID: 1xINYQBBSiJ2o_12qAWT9tvCtrVoTpWfx)
- Download URL: `https://drive.google.com/uc?export=download&id={fileId}` (no auth required)
- Upload URL: `https://www.googleapis.com/upload/drive/v3/files/{fileId}?uploadType=media`
- Sync triggers: On login, on app pause, on chest open, on craft, manual button
- Download works without login (public file); upload requires access token

### 2.3 Tab Activity (TabActivity)

**Purpose**: Main app host with bottom tab navigation and persistent header.

**Layout**:
```
┌──────────────────────────┐
│ ◈ 1,250 coins    [⚙][↻] │  ← Header: coin count, settings, sync
├──────────────────────────┤
│                          │
│   [ Active Tab Content ] │  ← Scrollable content area
│                          │
│                          │
│                          │
├──────────────────────────┤
│ 📦  📦  ⚗  🔨  🎰  🏛  │  ← Bottom tab bar (icons only)
│ Day Mon Alc Dec Slt Vlt  │  ← Labels under icons
└──────────────────────────┘
```

**Header Bar**:
- Animated coin icon + coin count (updates with animation on change)
- Settings gear icon (opens overlay: sound, sync, logout, dev mode)
- Sync button (manual cloud sync trigger with spin animation)
- Subtle notification badges on tabs when chests are available

**Tab Bar**:
- 6 tabs with pixel-art icons
- Active tab highlighted with accent color
- Badge indicators: green dot on Daily/Monthly when chest is available
- Smooth slide transition between tab contents

--------------------------------------------------------------------------------
## 3. TAB DESIGNS
--------------------------------------------------------------------------------

### 3.1 Daily Chest Tab

**Purpose**: Open one loot chest every 24 hours for 3 items + coins.

**Layout**:
```
┌──────────────────────────┐
│     DAILY TREASURE       │
│                          │
│   ┌──────────────────┐   │
│   │                  │   │
│   │  [Animated Chest │   │
│   │   GIF Sprite]    │   │
│   │                  │   │
│   └──────────────────┘   │
│                          │
│   ◈ +50-200 coins        │  ← Coin reward preview
│   ★ 3 items              │  ← Item count preview
│                          │
│   [ OPEN CHEST ]         │  ← Big button (or tap chest)
│   ── or ──               │
│   "Available in 14:32:07"│  ← Countdown if on cooldown
│                          │
│ ─── Loot Drops ───────── │
│ ┌────┐ ┌────┐ ┌────┐    │
│ │item│ │item│ │item│    │  ← Dropped items with light beams
│ │ 1  │ │ 2  │ │ 3  │    │
│ └────┘ └────┘ └────┘    │
│  Epic   Common  Rare     │  ← Rarity labels
│                          │
│ [Collect All to Vault]   │  ← Sends items to vault
│                          │
│ ─── Statistics ───────── │
│ Total items: 542         │
│ Legendary found: 3       │
│ Mythic found: 1          │
│ Consecutive days: 7      │
└──────────────────────────┘
```

**Behavior**:
- Chest shows animated idle GIF when available (gentle glow/shimmer)
- Chest is grayed out with countdown timer when on cooldown
- Tapping "OPEN CHEST" or tapping chest directly triggers opening animation
- Opening animation: GIF plays forward, particles burst, items appear one-by-one
- Each item drops in with a rarity-colored light beam (Borderlands style)
- Item reveal delay: 0.5s between each item for dramatic effect
- Coin reward shows with spinning coin animation and counting-up number
- "Collect All to Vault" button appears after all items revealed
- Items can also be individually tapped to see tooltip before collecting
- Haptic feedback on chest open (vibration pattern)

**Coin Rewards (Daily)**:
- Base: 50 coins (guaranteed minimum)
- Bonus: +10-150 random coins (weighted toward lower values)
- Streak bonus: +5 coins per consecutive day (caps at +50)
- Total range: 50-250 coins per daily chest

**Item Drops (Daily)**:
- 3 items per chest
- Rarity weights (rarityBoost = 1.0):
  ```
  COMMON:    100 weight
  UNCOMMON:   50 weight
  RARE:       25 weight
  EPIC:       10 weight
  LEGENDARY:   3 weight
  MYTHIC:      1 weight
  ```

**Cooldown**: 24 hours from last open time (System.currentTimeMillis() based)

### 3.2 Monthly Chest Tab

**Purpose**: Open one premium loot chest every 30 days for 10 items + coins.

**Layout**: Same structure as Daily Chest but with:
- Larger, more ornate chest GIF (rainbow glow)
- 10 item drop slots (displayed in 2 rows of 5)
- Higher coin reward
- Longer countdown (days:hours:minutes:seconds)
- Rainbow particle effects during opening

**Coin Rewards (Monthly)**:
- Base: 500 coins (guaranteed minimum)
- Bonus: +100-1500 random coins
- Total range: 500-2000 coins per monthly chest

**Item Drops (Monthly)**:
- 10 items per chest
- Rarity weights (rarityBoost = 2.5):
  ```
  COMMON:     40 weight  (100 / 2.5)
  UNCOMMON:  100 weight  (50 * 2.0)
  RARE:       62 weight  (25 * 2.5)
  EPIC:       37 weight  (10 * 2.5 * 1.5)
  LEGENDARY:  15 weight  (3 * 2.5 * 2)
  MYTHIC:      7 weight  (1 * 2.5 * 3)
  ```

**Cooldown**: 30 days (2,592,000,000 ms) from last open time

### 3.3 Alchemy Crafting Tab

**Purpose**: Combine 1-3 items from vault to craft new items.

**Layout**:
```
┌──────────────────────────┐
│     ALCHEMY TABLE        │
│                          │
│  ┌──────────────────────┐│
│  │ [Alchemy Table GIF]  ││  ← Animated alchemy table sprite
│  └──────────────────────┘│
│                          │
│  Input Slots:            │
│  ┌────┐ ┌────┐ ┌────┐   │
│  │ 1  │ │ 2  │ │ 3  │   │  ← Drag items here from vault below
│  └────┘ └────┘ └────┘   │
│           ↓              │
│       ┌────────┐         │
│       │ Result │         │  ← Shows craftable output (or "?")
│       └────────┘         │
│   [   CRAFT   ]         │  ← Craft button (grayed if no recipe)
│                          │
│  "Drag items from your   │
│   vault to the input     │
│   slots above"           │
│                          │
│ ─── Your Vault ───────── │
│ [Filter: All ▼] [Sort ▼] │
│ ┌────┬────┬────┬────┐    │
│ │    │    │    │    │    │  ← Scrollable item grid (vault contents)
│ ├────┼────┼────┼────┤    │
│ │    │    │    │    │    │
│ ├────┼────┼────┼────┤    │
│ │    │    │    │    │    │
│ └────┴────┴────┴────┘    │
│      (scrollable)        │
└──────────────────────────┘
```

**Behavior**:
- Vault items displayed in scrollable grid at bottom half of screen
- Drag items from vault grid to input slots (1-3 slots)
- Recipe matching is order-independent (same as desktop)
- When a valid recipe matches, output slot shows the result item with green glow
- "CRAFT" button becomes active with green highlight
- Pressing CRAFT: input items consumed (1 each), result item added to vault
- Crafting animation: alchemy table GIF plays special animation, sparkle particles
- Invalid combinations show "?" in output with red tint
- Tap input slot to return item to vault
- Long-press any item for tooltip (name, rarity, stats)
- Filter dropdown: All, Weapons, Armor, Materials, Potions, Food, etc.
- Sort dropdown: By Rarity, By Name, By Category, By Recent

**Recipe Data**: Loaded from `data/alchemy_recipes.json` (same 1264+ recipes)

### 3.4 Deconstruction Tab

**Purpose**: Break down items into component materials.

**Layout**:
```
┌──────────────────────────┐
│   REVERSE CRAFTING       │
│                          │
│  ┌──────────────────────┐│
│  │[Rev. Craft Table GIF]││  ← Animated purple-glow table
│  └──────────────────────┘│
│                          │
│  Input:          Output: │
│  ┌────┐    ┌────┬────┬────┐
│  │item│ →  │ A  │ B  │ C  │ ← 1 input, up to 3 outputs
│  └────┘    └────┴────┴────┘
│                          │
│  [  DECONSTRUCT  ]       │
│                          │
│  "Only reversible items  │
│   can be deconstructed"  │
│                          │
│ ─── Your Vault ───────── │
│ [Filter ▼] [Sort ▼]     │
│ ┌────┬────┬────┬────┐    │
│ │    │    │    │    │    │
│ ├────┼────┼────┼────┤    │
│ │    │    │    │    │    │
│ └────┴────┴────┴────┘    │
│      (scrollable)        │
└──────────────────────────┘
```

**Behavior**:
- Drag one item from vault to input slot
- If item has a reversible recipe, output slots show components
- If item is NOT reversible, input slot shows red X overlay, message displayed
- Press DECONSTRUCT: input item consumed, component items added to vault
- Purple particle effect on deconstruction
- Long-press items for tooltip
- Only items where `recipe.reversible == true` can be deconstructed

### 3.5 Slot Machine Tab

**Purpose**: Spend coins on a slot machine for a chance at bonus coins.

**Layout**:
```
┌──────────────────────────┐
│     LUCKY SLOTS          │
│                          │
│  Your Coins: ◈ 1,250     │
│                          │
│  ┌──────────────────────┐│
│  │  ╔═════╦═════╦═════╗ ││
│  │  ║     ║     ║     ║ ││
│  │  ║ 🍎  ║ 💎  ║ 🍎  ║ ││  ← 3 spinning reels
│  │  ║     ║     ║     ║ ││
│  │  ╚═════╩═════╩═════╝ ││
│  │                      ││
│  │   ┌──────────────┐   ││
│  │   │  PULL LEVER  │   ││  ← Costs coins to pull
│  │   └──────────────┘   ││
│  └──────────────────────┘│
│                          │
│  Cost: ◈ 25 per pull     │
│                          │
│ ─── Payout Table ─────── │
│ 🍎🍎🍎  = ◈ 50           │
│ ⚔⚔⚔   = ◈ 100          │
│ 🛡🛡🛡  = ◈ 200          │
│ 💎💎💎  = ◈ 500          │
│ ⭐⭐⭐  = ◈ 1000         │
│ 👑👑👑  = ◈ 5000 JACKPOT │
│ Any 2  = ◈ 10            │
│ No match = ◈ 0           │
│                          │
│ ─── History ──────────── │
│ Pull #1: 🍎💎⚔ = ◈ 0    │
│ Pull #2: ⚔⚔⚔ = ◈ 100   │
│ Pull #3: 🍎🍎💎 = ◈ 10  │
└──────────────────────────┘
```

**Behavior**:
- 3 reels with 6 possible symbols each
- Each pull costs 25 coins
- Reels spin sequentially (left → middle → right) with 0.5s delay between stops
- Reel symbols are animated GIF sprites (item icons from the game)
- Matching symbols pay out according to payout table
- Jackpot (3 crowns) triggers special animation with fireworks
- Pull history shows last 10 results
- Cannot pull if insufficient coins
- Haptic feedback on reel stop and on wins

**Reel Symbols (Using Game Item Sprites)**:
| Symbol | GIF Source               | Represents  |
|--------|--------------------------|-------------|
| Apple  | assets/items/apple       | Common      |
| Sword  | assets/items/iron_sword  | Uncommon    |
| Shield | assets/items/iron_shield | Rare        |
| Gem    | assets/items/diamond     | Epic        |
| Star   | assets/items/magic_crystal | Legendary |
| Crown  | assets/items/ancient_crown | Mythic    |

**Payout Table**:
| Match         | Payout  | Probability |
|---------------|---------|-------------|
| 3x Apple      | 50      | 4.6%        |
| 3x Sword      | 100     | 2.7%        |
| 3x Shield     | 200     | 1.5%        |
| 3x Gem        | 500     | 0.8%        |
| 3x Star       | 1000    | 0.3%        |
| 3x Crown      | 5000    | 0.05%       |
| Any 2 match   | 10      | 40%         |
| No match      | 0       | 50%         |

**Expected Value**: ~0.85x per pull (slight house edge, coins are free from chests)

**Reel Weights (Per Reel)**:
```
Apple:  30 weight  (common)
Sword:  25 weight  (uncommon)
Shield: 20 weight  (rare)
Gem:    15 weight  (epic)
Star:    8 weight  (legendary)
Crown:   2 weight  (mythic)
```

### 3.6 Vault Tab

**Purpose**: Browse, sort, and manage all collected items.

**Layout**:
```
┌──────────────────────────┐
│       YOUR VAULT         │
│  542 items in 234 stacks │
│                          │
│ [Filter: All ▼] [Sort ▼] │
│ [Search: _____________ ] │
│                          │
│ ┌────┬────┬────┬────┐    │
│ │Iron│Gold│Dia-│Mana│    │
│ │Swd │Ingot│mond│Leaf│   │
│ │x1  │x16 │x8  │x12 │   │
│ ├────┼────┼────┼────┤    │
│ │Hlth│Fire│Stl │Wood│    │
│ │Pot │Swd │Helm│Bow │    │
│ │x5  │x1  │x1  │x2  │   │
│ ├────┼────┼────┼────┤    │
│ │    │    │    │    │    │
│ │ .. │ .. │ .. │ .. │    │
│ └────┴────┴────┴────┘    │
│      (scrollable)        │
│                          │
│ ─── Selected Item ────── │
│ ┌──────────────────────┐ │
│ │ [GIF] Iron Sword     │ │
│ │ Rarity: Rare (blue)  │ │
│ │ Damage: 15  Speed: 1 │ │
│ │ "A sturdy blade..."  │ │
│ │ Stack: 1/1           │ │
│ └──────────────────────┘ │
└──────────────────────────┘
```

**Features**:
- Full vault contents (up to 10,000 slots)
- 4-column scrollable grid with item GIF icons
- Stack counts displayed on each slot
- Rarity-colored borders on each item slot
- Filter by category (All, Weapons, Armor, Materials, etc.)
- Sort by rarity, name, category, stack count
- Search bar for finding specific items
- Tap item to show detailed tooltip panel at bottom
- Long-press for quick actions (move to alchemy input, mark favorite)
- Cloud sync indicator showing last sync time

--------------------------------------------------------------------------------
## 4. COIN ECONOMY DESIGN
--------------------------------------------------------------------------------

### 4.1 Coin Sources

| Source                  | Amount       | Frequency      |
|-------------------------|-------------|----------------|
| Daily Chest             | 50-250      | Every 24 hours |
| Monthly Chest           | 500-2000    | Every 30 days  |
| Consecutive Day Streak  | +5/day      | Daily (caps +50)|
| First Launch Bonus      | 500         | One-time       |

### 4.2 Coin Sinks

| Sink                    | Cost        | Notes          |
|-------------------------|-------------|----------------|
| Slot Machine Pull       | 25 coins    | Primary sink   |

### 4.3 Economy Balance

- Daily income (average): ~125 coins/day from daily chest
- Monthly income (average): ~1000 coins/month from monthly chest
- Daily pulls possible: ~5 pulls/day from daily income alone
- The slot machine has a ~0.85x expected return, creating a slow drain
- Players accumulate coins over time, encouraging daily engagement
- No pay-to-win: coins are cosmetic gambling currency only

### 4.4 Coin Persistence

Coins are stored in SaveData alongside vault items and synced via Google Drive.

```json
{
  "coins": 1250,
  "totalCoinsEarned": 8500,
  "totalCoinsSpent": 7250,
  "slotMachinePulls": 290,
  "biggestJackpot": 1000,
  "consecutiveDays": 7,
  "lastLoginDate": "2025-01-15"
}
```

--------------------------------------------------------------------------------
## 5. UNIVERSAL GIF SUPPORT
--------------------------------------------------------------------------------

### 5.1 GifDecoder (Custom Implementation)

The existing Android port only extracts the first frame of GIFs. The Loot Game
App requires full animated GIF playback for all sprites. A custom GifDecoder
class will handle this.

**GifDecoder.java** responsibilities:
- Parse GIF89a binary format from InputStream
- Extract all frames as individual Bitmap objects
- Read per-frame delay from Graphic Control Extension blocks
- Handle disposal methods (restore to background, restore to previous)
- Support transparency (transparent color index)
- Return `List<Bitmap> frames` and `List<Integer> delays`

**Decoding Pipeline**:
```
InputStream (from AssetManager)
    → GifDecoder.decode(inputStream)
    → Parse GIF header (width, height, global color table)
    → For each frame:
        → Read Graphic Control Extension (delay, disposal, transparency)
        → Read Image Descriptor (position, size, local color table)
        → Decompress LZW data
        → Composite onto canvas Bitmap (handle disposal)
        → Copy canvas to frame Bitmap
    → Return GifResult(frames, delays, width, height)
```

### 5.2 AnimatedSprite (Rendering Component)

Wraps a decoded GIF for easy rendering anywhere in the app.

```java
public class AnimatedSprite {
    private List<Bitmap> frames;
    private List<Integer> delays;  // ms per frame
    private int currentFrame;
    private long elapsed;
    private boolean looping = true;
    private boolean paused = false;

    public AnimatedSprite(GifResult gif) { ... }

    public void update(long deltaMs) {
        if (paused || frames.size() <= 1) return;
        elapsed += deltaMs;
        while (elapsed >= delays.get(currentFrame)) {
            elapsed -= delays.get(currentFrame);
            currentFrame++;
            if (currentFrame >= frames.size()) {
                currentFrame = looping ? 0 : frames.size() - 1;
                if (!looping) paused = true;
            }
        }
    }

    public void draw(Canvas canvas, float x, float y, float w, float h) {
        canvas.drawBitmap(frames.get(currentFrame), null,
            new RectF(x, y, x + w, y + h), paint);
    }

    // Control methods
    public void playForward() { ... }
    public void playReverse() { ... }
    public void reset() { ... }
    public void setLooping(boolean loop) { ... }
}
```

### 5.3 Where GIFs Are Used

| Context              | Asset Path                    | Behavior          |
|----------------------|-------------------------------|--------------------|
| Item icons           | assets/items/{id}/idle.gif    | Looping idle anim  |
| Daily chest          | assets/chests/daily_chest.gif | Open/close on tap  |
| Monthly chest        | assets/chests/monthly_chest.gif | Open/close on tap |
| Alchemy table        | assets/items/alchemy_table.gif | Looping ambient   |
| Reverse craft table  | assets/items/reverse_crafting_table.gif | Looping  |
| Slot machine reels   | assets/items/{symbol}/idle.gif | Spinning + stop   |
| Loot light beams     | Generated procedurally        | Color per rarity   |
| Tab bar icons        | assets/ui/tab_*.gif           | Subtle animation   |
| Coin icon            | assets/ui/coin.gif            | Spinning coin      |

### 5.4 Performance Considerations

- Cache decoded frames in memory (LruCache with configurable size)
- Recycle Bitmaps when sprites are off-screen
- Limit concurrent animated sprites to ~30 (typical tab shows 4-16 items visible)
- Item grid sprites: only animate visible items (recycle on scroll)
- Use ARGB_8888 format for quality, RGB_565 for non-transparent backgrounds
- Target: 60 FPS with all visible sprites animating

--------------------------------------------------------------------------------
## 6. SAVE & SYNC SYSTEM
--------------------------------------------------------------------------------

### 6.1 Local Save (SaveManager)

**File**: `{app_files_dir}/saves/loot_game_save.json`

**SaveData Structure**:
```json
{
  "version": 1,
  "platform": "android_loot",
  "lastModified": 1707064800000,
  "syncId": "uuid-v4",

  "coins": 1250,
  "totalCoinsEarned": 8500,
  "totalCoinsSpent": 7250,

  "dailyChestLastOpened": 1707064800000,
  "monthlyChestLastOpened": 1704547200000,

  "totalItemsCollected": 542,
  "legendaryItemsFound": 3,
  "mythicItemsFound": 1,

  "consecutiveDays": 7,
  "lastLoginDate": "2025-01-15",

  "slotMachinePulls": 290,
  "biggestJackpot": 1000,

  "developerMode": false,

  "vaultItems": [
    { "itemId": "iron_sword", "stackCount": 1 },
    { "itemId": "gold_ingot", "stackCount": 16 },
    { "itemId": "diamond", "stackCount": 8 }
  ]
}
```

**Auto-Save Triggers**:
- After opening any chest
- After crafting an item
- After deconstructing an item
- After slot machine pull
- On app pause (onPause lifecycle)
- On manual sync button press

### 6.2 Google Drive Cloud Sync (GoogleDriveSyncManager)

**Sync Model**: Last-write-wins via public Google Drive file.

**Google Drive File ID**: `1xINYQBBSiJ2o_12qAWT9tvCtrVoTpWfx`
**Public URL**: `https://drive.google.com/file/d/1xINYQBBSiJ2o_12qAWT9tvCtrVoTpWfx/view?usp=drive_link`

**Sync Operations**:

1. **Push (Local → Cloud)**:
   - Serialize SaveData to JSON
   - PATCH to Google Drive API v3: `upload/drive/v3/files/{fileId}?uploadType=media`
   - Auth: `Authorization: Bearer {Google OAuth access token}`
   - Content-Type: application/json

2. **Pull (Cloud → Local)**:
   - GET from public download URL: `drive.google.com/uc?export=download&id={fileId}`
   - No authentication required (file is publicly shared)
   - Validate response is JSON (not HTML confirmation page)
   - Deserialize to SaveData

3. **Token Validation**:
   - GET file metadata from Drive API v3: `drive/v3/files/{fileId}?fields=id,name,modifiedTime`
   - Verifies the access token has permission to read/write the file

### 6.3 Cross-App Vault Sharing

The Loot Game App vault is stored separately from the main Amber Moon save
(`loot_game_save.json` vs `player_data.json`). However, a future feature could
allow import/export between the two saves. For now, they are independent.

--------------------------------------------------------------------------------
## 7. ITEM SYSTEM ADAPTATION
--------------------------------------------------------------------------------

### 7.1 Reused from Desktop Game

The following are copied and adapted from the main game's `src/entity/item/`:

- **Item.java**: Base item class (adapted: BufferedImage → Bitmap)
- **ItemCategory.java**: Enum (unchanged)
- **ItemRarity.java**: Enum with Android Color values instead of java.awt.Color
- **ItemRegistry.java**: Singleton registry (adapted for Android asset loading)
- **RecipeManager.java**: Recipe matching logic (unchanged)
- **198 item classes**: Individual item definitions (adapted: texture loading)

### 7.2 New for Loot Game App

- **LootTable.java**: Weighted random item selection with rarity boost
  ```java
  public class LootTable {
      public static List<Item> generateLoot(int count, float rarityBoost) {
          // Build weighted pool from all items in registry
          // Apply rarityBoost to weight calculation
          // Select 'count' items via weighted random
      }
  }
  ```

- **CoinReward.java**: Coin calculation for chests
  ```java
  public class CoinReward {
      public static int calculateDaily(int consecutiveDays) {
          int base = 50;
          int random = Random.nextInt(151) + 10;  // 10-160
          int streak = Math.min(consecutiveDays * 5, 50);
          return base + random + streak;
      }
      public static int calculateMonthly() {
          return 500 + Random.nextInt(1501) + 100;  // 600-2100
      }
  }
  ```

### 7.3 Item Icon Handling

Each item's GIF sprite serves as its icon in the app:
- Items with folder-based sprites: use `idle.gif` from the folder
- Items with single-file sprites: use the `.gif` file directly
- Items with only `.png`: wrap in single-frame AnimatedSprite
- Missing sprites: generate colored placeholder with rarity border

Priority: `{id}/idle.gif` > `{id}.gif` > `{id}.png` > generated placeholder

--------------------------------------------------------------------------------
## 8. AUDIO DESIGN
--------------------------------------------------------------------------------

### 8.1 Sound Effects

| Event                   | Sound                         | Priority |
|-------------------------|-------------------------------|----------|
| Chest open (daily)      | chest_open.mp3                | High     |
| Chest open (monthly)    | chest_open_epic.mp3           | High     |
| Item reveal (per item)  | item_drop.mp3                 | Medium   |
| Rare+ item reveal       | rare_item_reveal.mp3          | High     |
| Legendary item reveal   | legendary_fanfare.mp3         | High     |
| Mythic item reveal      | mythic_reveal.mp3             | High     |
| Craft success           | craft_success.mp3             | Medium   |
| Deconstruct             | deconstruct.mp3               | Medium   |
| Slot machine pull       | slot_pull.mp3                 | Medium   |
| Reel stop               | reel_stop.mp3 (x3)           | Medium   |
| Slot win (small)        | slot_win_small.mp3            | Medium   |
| Slot win (jackpot)      | slot_jackpot.mp3              | High     |
| Coin collect            | coin_collect.mp3              | Low      |
| Tab switch              | tab_switch.mp3                | Low      |
| Button tap              | button_tap.mp3                | Low      |
| Error/invalid           | error_buzz.mp3                | Low      |

### 8.2 Implementation

- Use Android SoundPool (max 10 simultaneous streams)
- Preload all sounds on app launch
- Volume controlled via GamePreferences
- Haptic feedback accompanies key sounds (chest open, wins)

--------------------------------------------------------------------------------
## 9. BUILD SYSTEM
--------------------------------------------------------------------------------

### 9.1 Build Script (Mirrors Existing Android Port)

The build system follows the same command-line approach as the main Android port:

1. **Compile Resources**: `aapt2 compile` all res/ drawables
2. **Link Resources**: `aapt2 link` with AndroidManifest.xml
3. **Compile Java**: `javac` all .java files against android.jar
4. **Create JAR**: Package .class files
5. **DEX**: `d8` converts to classes.dex
6. **APK Assembly**: Combine DEX + resources + assets
7. **Sign**: Debug keystore (auto-generated)
8. **Align**: `zipalign` for optimization

### 9.2 Requirements

- JDK 11+ (JDK 21 recommended)
- Android SDK Build Tools 34.0.0
- Android Platform API 34
- Platform Tools (adb)

### 9.3 AndroidManifest.xml

```xml
<manifest package="com.ambermoon.lootgame">
    <uses-sdk android:minSdkVersion="24" android:targetSdkVersion="34" />

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.VIBRATE" />

    <application
        android:label="Loot Game"
        android:icon="@drawable/ic_launcher"
        android:hardwareAccelerated="true"
        android:largeHeap="true"
        android:theme="@style/AppTheme">

        <activity android:name=".core.MainActivity"
            android:screenOrientation="portrait"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity android:name=".core.LoginActivity"
            android:screenOrientation="portrait" />

        <activity android:name=".core.TabActivity"
            android:screenOrientation="portrait"
            android:launchMode="singleTop"
            android:windowSoftInputMode="adjustPan" />
    </application>
</manifest>
```

Key differences from main game:
- **Portrait orientation** (not landscape)
- **Different package name**: `com.ambermoon.lootgame`
- **3 activities** instead of 2 (added LoginActivity)
- **No gamepad requirement** (touch-only interface)

--------------------------------------------------------------------------------
## 10. IMPLEMENTATION PHASES
--------------------------------------------------------------------------------

### Phase 1: Foundation
- [ ] Create project directory structure
- [ ] Set up AndroidManifest.xml and build scripts
- [ ] Implement GifDecoder and AnimatedSprite classes
- [ ] Implement AssetLoader with GIF caching
- [ ] Port Item, ItemCategory, ItemRarity, ItemRegistry for Android
- [ ] Copy and adapt 198 item class files
- [ ] Copy alchemy_recipes.json to assets/data/
- [ ] Implement SaveManager (local JSON persistence)
- [ ] Implement basic TabActivity with 6 tab shells

### Phase 2: Core Tabs
- [ ] Implement Vault Tab (item grid, filtering, sorting, tooltips)
- [ ] Implement ItemSlotView and ItemGridView UI components
- [ ] Implement LootTable (weighted random item generation)
- [ ] Implement Daily Chest Tab (chest animation, item drops, coin reward)
- [ ] Implement Monthly Chest Tab (same structure, different params)
- [ ] Implement chest cooldown timers with countdown display
- [ ] Implement coin counter in header bar

### Phase 3: Crafting
- [ ] Port RecipeManager for Android
- [ ] Implement Alchemy Tab (3 input slots, recipe matching, craft button)
- [ ] Implement Deconstruct Tab (1 input, 3 output, reversible check)
- [ ] Implement drag-and-drop between vault grid and crafting slots
- [ ] Add crafting animations and particle effects

### Phase 4: Slot Machine
- [ ] Implement SlotMachineView (3 reels with weighted symbols)
- [ ] Implement reel spin animation (sequential stop)
- [ ] Implement payout calculation and coin reward
- [ ] Implement pull history display
- [ ] Add slot machine sound effects and haptics

### Phase 5: Login & Sync
- [x] Implement LoginActivity (Google Drive access token input)
- [x] Implement GoogleDriveSyncManager (push via Drive API, pull via public URL)
- [ ] Implement ConflictResolver (merge strategies)
- [x] Add sync button to header bar
- [ ] Add auto-sync on key events (chest open, craft, app pause)
- [x] Token validation and error handling

### Phase 6: Polish
- [ ] Add sound effects for all interactions
- [ ] Add haptic feedback patterns
- [ ] Add particle effects (chest opening, crafting, slot wins)
- [ ] Add loot light beams (rarity-colored)
- [ ] Add tab transition animations
- [ ] Add notification badges on tabs (chest available)
- [ ] Developer mode toggle (reset cooldowns for testing)
- [ ] Performance testing and optimization
- [ ] Edge case handling (no internet, corrupt save, etc.)

--------------------------------------------------------------------------------
## 11. ASSET REQUIREMENTS
--------------------------------------------------------------------------------

### 11.1 Shared Assets (Copy from Main Game)

| Asset Type          | Source Path            | Count  |
|---------------------|------------------------|--------|
| Item sprites        | assets/items/          | 198+   |
| Chest GIFs          | assets/chests/         | 2      |
| Alchemy table       | assets/items/alchemy_table.gif | 1 |
| Reverse craft table | assets/items/reverse_crafting_table.gif | 1 |
| Recipe data         | data/alchemy_recipes.json | 1   |

### 11.2 New Assets Required

| Asset                | Description                          |
|----------------------|--------------------------------------|
| App icon             | Loot chest icon for launcher         |
| Tab bar icons (x6)   | Pixel art icons for each tab        |
| Coin sprite          | Animated spinning coin GIF           |
| Slot machine frame   | Slot machine border/frame sprite     |
| Slot machine lever   | Animated pull lever GIF              |
| Reel frame           | Individual reel window border        |
| Login background     | Dark fantasy-themed background       |
| UI backgrounds       | Tab content area backgrounds         |
| Particle sprites     | Sparkle, fire, glow particle sheets  |

### 11.3 Placeholder Strategy

For initial development, missing assets will use:
- Colored rectangles with rarity-appropriate borders
- Text labels for identification
- Simple geometric shapes for particles
- These are replaced with proper pixel art sprites in the polish phase

--------------------------------------------------------------------------------
## 12. TESTING STRATEGY
--------------------------------------------------------------------------------

### 12.1 Manual Test Checklist

- [ ] Login with valid Google access token → sync succeeds
- [ ] Login with invalid token → clear error message
- [ ] Play offline → all features work without network
- [ ] Open daily chest → 3 items + coins added to vault
- [ ] Daily chest cooldown → timer shows, chest grayed out
- [ ] Open monthly chest → 10 items + coins, rainbow effects
- [ ] Monthly chest cooldown → 30-day timer displays correctly
- [ ] Alchemy: valid recipe → output shows, craft works
- [ ] Alchemy: invalid combo → shows "?" output
- [ ] Alchemy: crafted item appears in vault
- [ ] Deconstruct reversible item → components appear
- [ ] Deconstruct non-reversible item → error message
- [ ] Slot machine: pull with enough coins → reels spin
- [ ] Slot machine: pull without coins → error/disabled
- [ ] Slot machine: matching symbols → correct payout
- [ ] Vault: scroll through items, filter, sort, search
- [ ] Vault: tap item → tooltip with full stats
- [ ] Coins: display updates on earn/spend
- [ ] Sync: manual sync button → data syncs to Google Drive
- [ ] Sync: conflict detection → resolution dialog
- [ ] App pause/resume → save preserved
- [ ] App kill and restart → save loaded correctly
- [ ] Developer mode: cooldowns reset on toggle
- [ ] All GIF sprites animate smoothly (60 FPS target)
- [ ] Sound effects play for all interactions
- [ ] Haptic feedback on key events

### 12.2 Developer Mode

Toggle in settings (or hidden long-press on app title):
- Resets daily/monthly cooldowns
- Grants 10,000 coins
- Fills vault with one of every item
- Shows FPS counter and memory usage
- Enables verbose logging
