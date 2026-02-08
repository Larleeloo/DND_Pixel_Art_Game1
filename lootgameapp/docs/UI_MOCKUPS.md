# Loot Game App - UI Mockups & Design Guide

## Visual Style

The app uses a dark fantasy aesthetic consistent with The Amber Moon:
- Dark background tones (deep purple, navy, charcoal)
- Pixel art sprites with animated GIFs
- Rarity-colored glows and borders
- Particle effects for key interactions
- Portrait orientation optimized for one-handed phone use

## Color Palette

### Background Colors
```
Primary Background:     #1A1525  (dark purple-black)
Card/Panel Background:  #28233A  (dark purple)
Tab Bar Background:     #0F0D17  (near-black)
Header Background:      #1E1830  (dark indigo)
Input Field Background: #3C3555  (medium purple)
```

### Accent Colors
```
Alchemy Green:          #64DC96  (crafting accent)
Deconstruct Purple:     #B464FF  (reverse crafting accent)
Slot Machine Gold:      #FFD700  (gambling accent)
Chest Amber:            #FFB347  (loot accent)
Sync Blue:              #4DA6FF  (network accent)
Error Red:              #FF4444  (error states)
Success Green:          #44FF44  (success states)
```

### Rarity Colors (Android Color int values)
```
COMMON:     #FFFFFF  White
UNCOMMON:   #1EFF1E  Bright Green
RARE:       #1E64FF  Bright Blue
EPIC:       #B41EFF  Purple
LEGENDARY:  #FFA500  Orange/Gold
MYTHIC:     #00FFFF  Cyan
```

## Component Designs

### Item Slot (ItemIconView)
```
┌──────────┐
│ ┌──────┐ │  56x56dp total (or 72-120dp depending on context)
│ │ 1st  │ │  Icon area with 4dp rarity-colored border
│ │frame │ │  Shows first frame of item's idle.gif
│ └──────┘ │
│    x16   │  Stack count (centered below, rarity-colored text)
└──────────┘
```
- Renders the **first frame** of the item's idle GIF as a static sprite image
- Rarity color determines border color
- **Fallback**: If no icon is available, a filled **rarity-colored circle** is
  drawn instead (White=Common, Green=Uncommon, Blue=Rare, Purple=Epic,
  Orange=Legendary, Cyan=Mythic)
- Empty slot: dashed border, darker interior
- Hover/press: brightness +20%, subtle scale up (1.05x)
- Drag source: semi-transparent (0.5 alpha), ghost image follows finger
- Drop target: pulsing border animation

### Item Detail Panel (Vault Tab)
```
┌─────────────────────────────────┐
│       ┌──────────────┐          │
│       │  [Animated   │          │  160x160dp animated GIF preview
│       │   GIF plays  │          │  (loops continuously)
│       │   on loop]   │          │
│       └──────────────┘          │
│                                 │
│  [idle] [attack] [block] [use]  │  Animation state selector buttons
│                                 │  (shows available states from folder)
│  Iron Sword                     │  Name in rarity color
│  Rarity: ██ Rare                │  Colored badge
│  Category: Weapon               │
│  Damage: 15                     │  Only show non-zero stats
│  Attack Speed: 1.2              │
│  Crit Chance: 8%                │
│  "A well-forged blade..."      │  Description in gray italic
│  Vault Count: 3                 │  Total in vault
└─────────────────────────────────┘
```
- Appears on tap in vault grid
- Shows animated GIF playing on loop (default: idle state)
- Animation state buttons let user switch between idle, attack, block,
  critical, use, break (only buttons for available states are shown)
- AnimatedItemView handles frame-by-frame looping playback
- Background: dark panel (#28233A)
- Fallback: rarity-colored circle if GIF fails to load

### Chest Animation Sequence (ChestIconView)
```
Phase 1: Idle (chest available)
  - ChestIconView shows FIRST FRAME of chest GIF (closed chest)
  - "OPEN CHEST" button is enabled below

Phase 2: Opening (on tap)
  - ChestIconView plays GIF FORWARD ONCE (closed → open animation)
  - GIF plays in forward order only, frame by frame
  - When animation completes, holds on LAST FRAME (open chest)
  - Haptic: medium vibration (200ms)

Phase 3: Item Reveal (sequential)
  - Items appear with ItemIconView sprites (first frame of idle GIF)
  - Each item shown with rarity-colored border
  - Rarity label text below each item sprite
  - Fallback: rarity-colored circle if sprite not available

Phase 4: Coin Reveal (after all items)
  - Coin amount displayed with coin symbol (◈)

Phase 5: Cooldown State
  - ChestIconView shows LAST FRAME of GIF (opened/empty chest)
  - Countdown timer displayed
  - "ON COOLDOWN" button state
```

### Slot Machine Reel Animation (SlotReelView)
```
┌─────────────────────────────┐
│         LUCKY SLOTS         │
│                             │
│  ╔═══════╦═══════╦═══════╗  │
│  ║       ║       ║       ║  │
│  ║[ITEM] ║[ITEM] ║[ITEM] ║  │  ← Static item sprite images
│  ║ IMG   ║ IMG   ║ IMG   ║  │     (1st frame of idle GIF)
│  ║       ║       ║       ║  │
│  ╚═══════╩═══════╩═══════╝  │
│                             │
│  ┌─────────────────────┐    │
│  │   PULL  (◈ 25)      │    │  ← Pull button
│  └─────────────────────┘    │
└─────────────────────────────┘

Reel Symbols (static item images):
  Apple   → assets/items/apple/idle.gif (1st frame)
  Sword   → assets/items/iron_sword/idle.gif (1st frame)
  Shield  → assets/items/steel_shield/idle.gif (1st frame)
  Gem     → assets/items/diamond/idle.gif (1st frame)
  Star    → assets/items/magic_crystal/idle.gif (1st frame)
  Crown   → assets/items/ancient_crown/idle.gif (1st frame)

Fallback: Rarity-colored circle if image not available.
Future: Replace with dedicated static PNGs when provided.

Reel Spin Sequence:
1. Tap PULL button → 25 coins deducted
2. All 3 SlotReelViews show spinning indicator bars
3. Reel 1 reveals static item image (0.5s)
4. Reel 2 reveals static item image (1.0s)
5. Reel 3 reveals static item image (1.5s)
6. If match: WIN text + payout display
7. If jackpot: special highlight
```

### Tab Bar Design
```
┌────┬────┬────┬────┬────┬────┐
│Day │Mon │Alc │Dec │Slt │Vlt │
│    │    │    │    │    │    │
├────┴────┴────┴────┴────┴────┤
Active tab:
  - Icon tinted with accent color
  - Label text visible
  - Subtle glow under icon
  - Tab bar notch/bump above active

Inactive tab:
  - Icon in gray/dim
  - Label text dimmer
  - No glow

Badge (chest available):
  - Green dot (8dp) on top-right of tab icon
  - Subtle pulse animation
```

## Screen Transitions

- Tab switch: horizontal slide (content slides left/right)
- Login → Tab: fade through black (500ms)
- Tooltip open: slide up from bottom (200ms, ease-out)
- Tooltip close: slide down (150ms, ease-in)
- Dialog open: scale up from center (200ms) + dim background

## Typography

All text uses the system default monospace or a pixel-art font if bundled.

| Element           | Size  | Weight | Color     |
|-------------------|-------|--------|-----------|
| Tab title         | 20sp  | Bold   | White     |
| Section header    | 16sp  | Bold   | #CCCCCC   |
| Body text         | 14sp  | Normal | #AAAAAA   |
| Item name         | 14sp  | Bold   | Rarity    |
| Stack count       | 12sp  | Bold   | White     |
| Timer             | 18sp  | Bold   | #FF6666   |
| Coin count        | 16sp  | Bold   | #FFD700   |
| Button label      | 16sp  | Bold   | White     |

## Touch Targets

- Minimum touch target: 48x48dp (Android guideline)
- Item slots: 56x56dp (comfortable for grid)
- Buttons: 48dp height minimum, full-width preferred
- Tab bar items: equal width, 56dp height
- Spacing between interactive elements: minimum 8dp
