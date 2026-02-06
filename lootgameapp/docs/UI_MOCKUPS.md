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

### Item Slot (ItemSlotView)
```
┌──────────┐
│ ┌──────┐ │  56x56dp total
│ │ GIF  │ │  48x48dp icon area
│ │ icon │ │  4dp rarity-colored border
│ └──────┘ │
│    x16   │  Stack count (bottom-right, white text, black outline)
└──────────┘
```
- Rarity color determines border color
- Empty slot: dashed border, darker interior
- Hover/press: brightness +20%, subtle scale up (1.05x)
- Drag source: semi-transparent (0.5 alpha), ghost image follows finger
- Drop target: pulsing border animation

### Item Tooltip (ItemTooltip)
```
┌─────────────────────────────────┐
│ [GIF]  Iron Sword               │  Name in rarity color
│  64x64 ─────────────────────    │
│        Rarity: ██ Rare          │  Colored badge
│        Category: Weapon         │
│        ─────────────────────    │
│        Damage: 15               │  Only show non-zero stats
│        Attack Speed: 1.2        │
│        Crit Chance: 8%          │
│        Range: 80                │
│        ─────────────────────    │
│        "A well-forged blade     │  Description in gray italic
│         of tempered iron"       │
│        ─────────────────────    │
│        Stack: 1 / 1            │
│        Vault Count: 3           │  Total in vault
└─────────────────────────────────┘
```
- Appears on long-press (300ms) or single tap in vault
- Slides up from bottom of screen
- Dismiss by tapping outside or swiping down
- Background: semi-transparent dark panel with blur

### Chest Animation Sequence
```
Phase 1: Idle (chest available)
  - Chest GIF plays idle shimmer loop
  - Subtle glow particles around chest
  - "TAP TO OPEN" text pulses below

Phase 2: Opening (on tap)
  - Chest GIF plays forward (closed → open)
  - Burst of particles from chest center
  - Screen flash (white, 100ms fade)
  - Camera shake effect (subtle, 200ms)
  - Haptic: medium vibration (200ms)

Phase 3: Item Reveal (sequential)
  - Items appear one at a time (0.5s delay each)
  - Each item slides up from chest with bounce easing
  - Rarity light beam fades in behind item
  - Sound effect per rarity tier
  - Haptic per item (intensity scales with rarity)

Phase 4: Coin Reveal (after all items)
  - Coin icon spins in
  - Number counts up from 0 to reward amount (1s duration)
  - Satisfying "ding" sound at completion

Phase 5: Collection
  - "Collect All" button slides in
  - Tap: items fly to vault icon with trail effect
  - Coins add to header counter with animation
```

### Slot Machine Reel Animation
```
┌─────────────────────────────┐
│         LUCKY SLOTS         │
│                             │
│  ╔═══════╦═══════╦═══════╗  │
│  ║ ▲     ║ ▲     ║ ▲     ║  │
│  ║       ║       ║       ║  │
│  ║[ITEM] ║[ITEM] ║[ITEM] ║  │  ← Pay line (center row)
│  ║       ║       ║       ║  │
│  ║ ▼     ║ ▼     ║ ▼     ║  │
│  ╚═══════╩═══════╩═══════╝  │
│                             │
│  ┌─────────────────────┐    │
│  │    🔻 PULL 🔻       │    │  ← Lever button
│  └─────────────────────┘    │
└─────────────────────────────┘

Reel Spin Sequence:
1. Pull lever → lever animates down
2. All 3 reels start spinning (blur effect)
3. Reel 1 stops (0.8s) → bounce settle
4. Reel 2 stops (1.3s) → bounce settle
5. Reel 3 stops (1.8s) → bounce settle
6. If match: flash animation + payout display
7. If jackpot: firework particles + screen shake
```

### Tab Bar Design
```
┌────┬────┬────┬────┬────┬────┐
│ 📦 │ 📦 │ ⚗️ │ 🔨 │ 🎰 │ 🏛️ │
│Day │Mon │Alc │Dec │Slt │Vlt │
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
