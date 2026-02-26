# Clothing Preview System

The clothing preview system allows players to customize a 64x64 pixel art avatar by layering equipment sprites on top of a base character. The result is displayed as a 256x256 upscaled animation (walking or idle) in the Cosmetics popup.

## Overview

- **3 base avatars**: Knight, Mage, Rogue (selectable)
- **10 equipment slots**: Headwear, Shirt, Armor, Gauntlets, Pants, Leggings, Shoes, Boots, Rings/Gloves, Necklace
- **Walking animation**: 15 frames, 1.5 seconds (100ms per frame), looping GIF
- **Idle pose**: 1 frame, static GIF
- **Display size**: 64x64 native, upscaled to 256x256 with nearest-neighbor (crisp pixels)

## How It Works

### Sprite Layering

Equipment sprites are composited on top of the base avatar using alpha transparency. Each sprite layer is a full 64x64 image where only the relevant body region has pixel data — the rest is transparent.

**Render order (bottom to top):**

1. Base avatar (always drawn first)
2. Pants
3. Shoes
4. Boots
5. Shirt
6. Armor
7. Leggings
8. Gauntlets
9. Necklace
10. Headwear
11. Rings/Gloves (drawn last, on top)

This ordering ensures visually correct layering — armor covers shirts, boots cover shoes, headwear sits on top of everything, etc.

### Frame Synchronization

All layers share the same animation timing:
- Walk cycle: 15 frames at 100ms each = 1.5 second loop
- Idle: Single frame

Equipment sprites must have the same frame count as the avatar. The compositor draws frame N of each layer simultaneously, ensuring perfect sync.

### The Compositing Pipeline

```
1. User selects avatar (0-2)
   → AvatarRegistry.getWalkFrames(idx) returns Bitmap[15]
   → AvatarRegistry.getIdleFrame(idx) returns Bitmap

2. User equips items per slot
   → For each equipped slot, generate overlay Bitmap[15] + Bitmap

3. SpriteLayerCompositor.compositeWalkAnimation()
   → For each of 15 frames:
      → Create blank 64x64 ARGB_8888 canvas
      → Draw base avatar frame
      → Draw equipment overlays in RENDER_ORDER
   → Returns composited Bitmap[15]

4. SpriteLayerCompositor.compositeIdle()
   → Same process for single idle frame

5. ClothingPreviewView.setFrames(walk[], idle)
   → View cycles walk frames at 100ms, or shows idle
   → Canvas draws 64x64 → 256x256 with nearest-neighbor
```

## Equipment Slots

| Slot | Body Region | Example Items |
|------|-------------|---------------|
| Headwear | Top of head (y: 2-12) | Hats, Caps, Crowns, Helmets |
| Shirt | Torso (y: 20-38) | Shirts, Dresses, Robes, Suits |
| Armor | Torso + shoulders | Chestplates, Armor Robes |
| Gauntlets | Hands (y: 34-40) | Iron/Titan/Obsidian/Void Gauntlets |
| Pants | Legs (y: 38-52) | All color pants, Chainmail Pants |
| Leggings | Legs + knee guards | Iron/Obsidian/Void Leggings |
| Shoes | Feet, short (y: 52-60) | Worn Shoes, colored Shoes |
| Boots | Feet, tall (y: 48-60) | Boots, Iron/Fancy/Obsidian/Void Boots |
| Rings/Gloves | Wrists/fingers | Bracelets, Ruby Skull |
| Necklace | Neck (y: 18-22) | Silver/Gold Necklaces |

## Auto-Assignment

Items are automatically mapped to equipment slots based on their name using keyword matching in `EquipmentSlot.getSlotForItem()`:

- Name contains "Hat", "Cap", "Crown", "Helmet" → **Headwear**
- Name contains "Shirt", "Dress", "Robe" (clothing), "Suit", etc. → **Shirt**
- Name contains "Chestplate", or armor-category "Robes" → **Armor**
- Name contains "Gauntlets" → **Gauntlets**
- Name contains "Pants" → **Pants**
- Name contains "Leggings" → **Leggings**
- Name contains "Shoes" → **Shoes**
- Name contains "Boots" → **Boots**
- Name contains "Necklace" → **Necklace**
- Name contains "Bracelet" or equals "Ruby Skull" → **Rings/Gloves**

Items that don't match any keyword (shields, weapons, food, etc.) have no slot.

## User Interface

### Access Path
1. Tap sparkles (✨) button in the header bar
2. In the Cosmetics popup, find the "Character Preview" section
3. See the static idle thumbnail of your current avatar
4. Tap "Customize" to open the full Character Preview popup

### Character Preview Popup
- **Top**: 256x256 animated preview showing your character walking
- **Walk/Idle toggle**: Switch between walking animation and idle pose
- **Avatar selector**: Three buttons (Knight, Mage, Rogue)
- **Equipment slots**: 10 slots in a 2×5 grid, each showing:
  - Slot name
  - Currently equipped item (or "Empty")
  - Text colored by item rarity
- **Item picker**: Tap a slot to select from vault items matching that slot
- **Clear All**: Remove all equipped items at once

### Persistence
Selections are saved to `SaveData`:
- `selectedAvatarIndex` (int: 0, 1, or 2)
- `equippedCosmetics` (Map: slot_name → item_registry_id)

These persist across sessions and sync with cloud save.

## Adding Real Sprites

The current system uses programmatic placeholder sprites (colored shapes). To replace with real pixel art:

### Avatar Sprites
1. Create 64x64 PNG sprite sheets or individual frame PNGs
2. Place walk frames in `assets/avatars/{avatar_name}/walk_00.png` through `walk_14.png`
3. Place idle frame at `assets/avatars/{avatar_name}/idle.png`
4. Modify `AvatarRegistry` to load from assets instead of generating programmatically

### Equipment Sprites
1. Create 64x64 PNGs with transparent backgrounds for each equipment item
2. Each item needs 16 images: 15 walk frames + 1 idle frame
3. Place at `assets/equipment/{item_id}/walk_00.png` through `walk_14.png` and `idle.png`
4. Modify `SpriteLayerCompositor` to load from assets instead of `generatePlaceholderEquipment()`

### Important Sprite Requirements
- All sprites must be exactly **64x64 pixels**
- Use **ARGB with alpha transparency** (PNG format)
- Equipment overlays should only draw pixels in their relevant body region
- Walk frames should sync with the avatar's body position (bounce, arm swing, leg swing)
- The avatar body positions per frame are documented in `AvatarRegistry.generateFrame()`

## File Reference

| File | Purpose |
|------|---------|
| `graphics/EquipmentSlot.java` | Slot constants, render order, auto-assignment |
| `graphics/AvatarRegistry.java` | 3 base avatar definitions with walk/idle frames |
| `graphics/SpriteLayerCompositor.java` | Multi-layer sprite compositing engine |
| `core/ClothingPreviewView.java` | Custom View for animated 256x256 preview |
| `core/ClothingPreviewPopup.java` | Equipment selection dialog |
| `core/CosmeticsPopup.java` | Entry point (Character Preview section) |
| `entity/Item.java` | Equipment slot field on items |
| `entity/ItemRegistry.java` | Auto-assigns slots during initialization |
| `save/SaveData.java` | Persistence for avatar + equipment selections |
