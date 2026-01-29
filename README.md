TITLE	The Amber Moon
AUTHOR Larson Sonderman & CLAUDE AI

DESCRIPTION
A Java Swing-based game engine that runs a 2D platformer game. Plays similarly to Terraria, Super Mario, or 2D Minecraft.
Inspired by Dungeons and Dragons games like Baldur's Gate 3. (Non-turn-based)
This game should feel abundant with the quantity and variations in items and tools.

CURRENT FEATURES
================

This section provides comprehensive documentation for developers working on The Amber Moon.
All systems are designed to be modular and extensible.

--------------------------------------------------------------------------------
1. CORE ARCHITECTURE
--------------------------------------------------------------------------------

The game uses a Java Swing-based engine with a 60 FPS game loop. The architecture
follows these key patterns:

DESIGN PATTERNS USED:
  - Singleton: SceneManager, BlockRegistry, SaveManager, InputManager
  - Interface-based polymorphism: Scene, PlayerBase, CombatCapable, ResourceManager
  - State Machine: Mob AI (IDLE → WANDER → CHASE → ATTACK states)
  - Observer: Input events propagate through SceneManager to active scene

GAME LOOP FLOW:
  1. GamePanel (main rendering surface) runs at 60 FPS
  2. SceneManager (singleton) manages scene transitions with fade effects
  3. Scene.update(input) called each frame for game logic
  4. Scene.draw(g) called each frame for rendering
  5. EntityManager updates all entities and handles physics

KEY ENTRY POINTS:
  - Main.java                    → Application entry point
  - GameWindow.java              → Creates borderless 1920x1080 window
  - GamePanel.java               → Game loop, scene initialization
  - SceneManager.getInstance()   → Access current scene and transitions

COORDINATE SYSTEM:
  - Origin (0,0) at top-left of screen
  - X increases rightward, Y increases downward
  - Screen resolution: 1920x1080
  - Block scale factor: 4x (16px base → 64px rendered)
  - Default ground Y: 720 pixels

--------------------------------------------------------------------------------
2. SCENE SYSTEM (scene/)
--------------------------------------------------------------------------------

All game states are managed through the Scene interface. SceneManager handles
transitions between scenes with configurable fade effects.

AVAILABLE SCENES:
  Scene Class                    | Purpose
  -------------------------------|------------------------------------------
  MainMenuScene                  | Title screen with navigation buttons
  LevelSelectionScene            | Browse and launch levels from levels/
  SpriteCharacterCustomization   | Character creation with equipment
  OverworldScene                 | Open world map navigation
  GameScene                      | Main gameplay with JSON-loaded levels
  LootGameScene                  | Mini-game with treasure chests and vault
  CreativeScene                  | Sandbox/creative mode

SCENE INTERFACE METHODS:
  - init()              → Called once when entering scene
  - update(input)       → Game logic, called every frame
  - draw(g)             → Rendering, called every frame
  - dispose()           → Cleanup when leaving scene
  - onMouse*()          → Mouse event handlers

SCENE TRANSITIONS:
  SceneManager.getInstance().loadScene("scene_name");
  // Transition speed configurable (default 0.12 for snappy transitions)

--------------------------------------------------------------------------------
2.5. CREATIVE MODE (scene/CreativeScene.java)
--------------------------------------------------------------------------------

A comprehensive level editor for designing and testing game levels.

ACCESSING CREATIVE MODE:
  From the main menu, select "Creative Mode" or load via:
  SceneManager.getInstance().loadScene("creative");

NEW LEVEL CREATION:
  When creating a new level, a dialog allows configuration of:

  Size Presets:
    - Small (30x17 blocks)     → Quick test levels
    - Medium (60x17 blocks)    → Standard horizontal levels
    - Large (100x17 blocks)    → Expansive adventure levels
    - Extra Large (150x25)     → Epic-scale environments
    - Vertical (30x40 blocks)  → Tower/climbing levels
    - Square (50x50 blocks)    → Exploration-focused maps
    - Custom                   → Any dimensions (10-500 blocks)

PALETTE CATEGORIES (Tab to cycle):
  Category      | Contents
  --------------|--------------------------------------------------
  BLOCKS        | All block types from BlockRegistry
  MOVING_BLOCKS | Blocks with movement patterns (horizontal/vertical/circular)
  OVERLAYS      | Block masks (grass, snow, ice, moss, vines) - apply to blocks
  ITEMS         | All items from ItemRegistry
  MOBS          | All mobs from MobRegistry (auto-populated)
  LIGHTS        | Torch, campfire, lantern, magic, crystal
  INTERACTIVE   | Doors, buttons, pressure plates, vaults/chests
  PARALLAX      | Background/foreground layers with depth labels

PARALLAX LAYER SYSTEM:
  Parallax layers add depth with scrolling backgrounds at different speeds.
  Supports both horizontal and vertical parallax scrolling.

  Available Layers (sorted by depth):
    Layer Name        | Z-Order | Speed
    ------------------|---------|-------
    Sky Background    | -5      | 0.05
    Distant Buildings | -4      | 0.15
    Mid Buildings     | -3      | 0.35
    Near Buildings    | -2      | 0.55
    Main Background   | -1      | 0.20
    Foreground        | +2      | 1.15

  ANIMATED GIF SUPPORT:
    Parallax layers support animated GIF files for dynamic backgrounds.
    Place .gif files in assets/parallax/ alongside .png files.

EDITOR CONTROLS:
  Key/Action      | Function
  ----------------|--------------------------------------------
  Left Click      | Place selected entity (grid-snapped for blocks)
  Right Click     | Remove entity at cursor
  WASD/Arrows     | Pan camera around level
  Tab             | Cycle palette categories
  Mouse Wheel     | Scroll palette items
  P               | Toggle Play/Edit mode (test the level)
  Ctrl+S          | Save level dialog
  W (near door)   | Configure door properties
  E (near button) | Configure button linkage
  Escape          | Exit to main menu / cancel dialog

LEVEL JSON FORMAT:
  Levels are saved as JSON files in the levels/ directory.
  Key fields: name, levelWidth, levelHeight, groundY, scrollingEnabled,
  verticalScrollEnabled, parallaxEnabled, parallaxLayers, blocks, items, mobs

--------------------------------------------------------------------------------
3. ENTITY SYSTEM (entity/)
--------------------------------------------------------------------------------

All game objects inherit from the Entity base class. EntityManager maintains
and updates all entities in the current scene.

ENTITY HIERARCHY:
  Entity (abstract)
  ├── SpriteEntity
  │   ├── SpritePlayerEntity     → Modern GIF-based player (RECOMMENDED)
  │   ├── PlayerEntity           → Legacy static PNG player
  │   ├── GroundEntity           → Ground/platform sprites
  │   └── ItemEntity             → Dropped items in world
  ├── MobEntity
  │   ├── SpriteMobEntity        → Modern GIF-based enemies (RECOMMENDED)
  │   └── old/                   → Deprecated bone-based mobs
  ├── ProjectileEntity           → Arrows, fireballs, thrown items
  ├── BlockEntity                → Placeable/mineable blocks
  ├── LootChestEntity            → Treasure chests
  ├── DoorEntity                 → Interactive doors
  ├── TriggerEntity              → Invisible trigger zones
  └── VaultEntity                → Persistent storage chests

ENTITY CAPABILITIES (interfaces in entity/capabilities/):
  - CombatCapable        → attack(), takeDamage(), getAttackDamage()
  - ResourceManager      → getHealth(), getMana(), getStamina()
  - BlockInteractionHandler → handleBlockInteraction()

ENTITY MANAGER USAGE:
  EntityManager em = new EntityManager();
  em.addEntity(entity);
  em.update(input);  // Updates all entities
  em.draw(g);        // Renders all entities
  em.removeDeadEntities();  // Cleanup

--------------------------------------------------------------------------------
4. PLAYER SYSTEM (entity/player/)
--------------------------------------------------------------------------------

Two player implementations exist. SpritePlayerEntity is the modern, recommended
approach using GIF animations.

SPRITE PLAYER ENTITY (Recommended):
  - Location: entity/player/SpritePlayerEntity.java
  - Uses GIF sprite animations (idle.gif, walk.gif, jump.gif, etc.)
  - Supports equipment overlay system (armor renders on top of player)
  - Multi-jump support (configurable: double, triple jump)
  - Sprint with stamina drain
  - Full combat: melee attacks, ranged projectiles, charged shots
  - Block interaction: mining and placement

PLAYER BASE INTERFACE:
  All player implementations provide these methods:
  - getHealth(), getMaxHealth(), takeDamage(amount)
  - getMana(), getMaxMana(), useMana(amount)
  - getStamina(), getMaxStamina(), useStamina(amount)
  - getInventory() → Player's inventory object
  - isFacingRight() → Direction player is facing
  - isInvincible() → True during damage immunity frames

RESOURCE SYSTEM:
  Resource  | Default Max | Usage
  ----------|-------------|----------------------------------------
  Health    | 100         | Decreases from damage, restored by potions
  Mana      | 100         | Used for magic attacks, auto-regenerates
  Stamina   | 100         | Used for sprinting, regenerates when idle

MOVEMENT CONTROLS (default keys, rebindable via Settings > Controls):
  Key       | Action
  ----------|----------------------------------------
  A/D       | Move left/right
  Space     | Jump (press again in air for multi-jump)
  Shift     | Sprint (drains stamina)
  W/S       | Climb ladders (when implemented)
  M         | Open Settings menu

COMBAT CONTROLS (default keys, rebindable via Settings > Controls):
  Key       | Action
  ----------|----------------------------------------
  Left Click| Attack/Mine/Place blocks
  Hold Left | Charge attack (bows, magic staffs)
  E         | Interact/Mine selected block
  F         | Attack/Equip item
  Arrows    | Change mining direction

--------------------------------------------------------------------------------
5. PLAYABLE CHARACTERS & ABILITY SCORES (entity/player/)
--------------------------------------------------------------------------------

The game features a DnD-inspired ability score system with 7 playable characters
plus a special Loot Game character. Each character has unique starting stats.

ABILITY SCORES (Baseline = 5):
  Ability      | Effect
  -------------|----------------------------------------------------------
  Charisma     | Affects cutscenes and dialogue options (not yet implemented)
  Strength     | Melee weapon damage (+/-17% per point), backpack capacity (+/-20%)
  Constitution | Max HP (+/-10%), status effect resistance (+/-10%), carry capacity (+/-5%)
  Intelligence | Magical item damage (+/-15% per point)
  Wisdom       | Locks ancient artifacts unless wisdom is high enough
  Dexterity    | Item usage: +10%/pt double-use chance (above 5), -20%/pt miss chance (below 5)

PLAYABLE CHARACTERS:
  Character         | CHA | STR | CON | INT | WIS | DEX | Description
  ------------------|-----|-----|-----|-----|-----|-----|---------------------------
  Crystal           |  8  |  4  |  4  |  6  |  7  |  5  | Charismatic mage, high wisdom
  Filvendor Venrona |  7  |  3  |  4  |  9  |  7  |  5  | Scholarly wizard, exceptional INT
  Breaya            |  4  |  4  |  5  |  7  |  7  |  8  | Nimble alchemist, high dexterity
  Asteria           |  1  |  8  |  8  |  8  |  4  |  6  | Powerful explorer, strong stats
  Olyrei            |  5  |  5  |  5  |  7  |  7  |  6  | Balanced spellcaster
  Trethas           |  6  |  9  |  7  |  5  |  5  |  3  | Mighty warrior, unmatched strength
  Gridius           |  5  |  5  |  5  |  6  |  6  |  8  | Agile rogue, high dexterity
  Merlin*           |  5  |  5  |  5  |  5  |  5  |  5  | Loot Game only, baseline stats

  * Merlin is only available in the Loot Game mini-game

CHARACTER SELECTION:
  - Access via "Customize Character" from main menu
  - Ability scores displayed with color coding (green = high, red = low)
  - Selected character's sprites are used in gameplay
  - Selection persists across game sessions

CHARACTER FILES:
  Location: entity/player/
  - AbilityScores.java         → Ability score calculations and modifiers
  - PlayableCharacter.java     → Character data class
  - PlayableCharacterRegistry.java → Registry of all characters

--------------------------------------------------------------------------------
6. COMBAT SYSTEM
--------------------------------------------------------------------------------

Combat is handled through the CombatCapable interface. Both players and mobs
can implement this interface to participate in combat.

COMBAT CAPABLE INTERFACE:
  - attack()                           → Initiate attack
  - isAttacking()                      → Check if currently attacking
  - getAttackDamage()                  → Base damage value
  - getAttackRange()                   → Melee attack distance in pixels
  - getAttackCooldown()                → Frames between attacks
  - takeDamage(damage, knockbackX, knockbackY) → Receive damage

ATTACK TYPES:
  1. MELEE ATTACKS
     - Close range physical attacks
     - Damage = baseDamage + weaponDamage
     - Range typically 50-100 pixels

  2. RANGED ATTACKS
     - Fire ProjectileEntity toward cursor
     - Projectile handles collision and damage
     - Bows consume arrows, magic consumes mana

  3. CHARGED ATTACKS
     - Hold attack button to charge (up to 5 seconds)
     - Damage multiplier: 1x (no charge) to 3x (full charge)
     - Visual feedback: projectile grows during charge

DAMAGE CALCULATION:
  finalDamage = baseDamage * weaponMultiplier * chargeMultiplier * critMultiplier
  - Knockback pushes target away from attacker
  - Invincibility frames prevent damage stacking

--------------------------------------------------------------------------------
7. PROJECTILE SYSTEM (entity/ProjectileEntity.java)
--------------------------------------------------------------------------------

Projectiles are fired by players and mobs for ranged attacks.

PROJECTILE TYPES:
  Type           | Source              | Properties
  ---------------|---------------------|--------------------------------
  ARROW          | Bows                | Gravity, consumes arrows
  BOLT           | Crossbows           | Fast, flat trajectory
  MAGIC_BOLT     | Wands               | No gravity, mana cost
  FIREBALL       | Fire Staff          | Explosion on impact, burn effect
  ICEBALL        | Ice Staff           | Freeze effect on hit
  THROWING_KNIFE | Thrown              | Fast, small hitbox
  THROWING_AXE   | Thrown              | Slower, more damage
  ROCK           | Thrown              | Gravity, blunt damage
  POTION         | Thrown              | Splash effect on impact
  BOMB           | Thrown              | Explosion radius
  FISH           | Mirror to Realms    | Special weapon projectile

STATUS EFFECTS (applied on hit):
  Effect   | Duration | Damage/Tick | Special
  ---------|----------|-------------|------------------
  BURNING  | 3 sec    | 5 damage    | Orange tint, fire particles
  FROZEN   | 4 sec    | 3 damage    | Blue tint, 50% slow, ice particles
  POISONED | 5 sec    | 2 damage    | Green tint, bubble particles

--------------------------------------------------------------------------------
8. INVENTORY SYSTEM (ui/Inventory.java)
--------------------------------------------------------------------------------

The inventory manages player items with Minecraft-style navigation, drag-and-drop
support, hotbar quick access, and vault integration for persistent storage.

INVENTORY STRUCTURE:
  - 32 item slots in 8x4 grid
  - 5-slot hotbar for quick access (bottom of inventory)
  - Items stack up to their maxStackSize
  - Scroll wheel cycles hotbar selection
  - Minecraft-style cursor navigation with D-pad/arrow keys

INVENTORY INTERACTIONS (Mouse Mode):
  Action        | Result
  --------------|----------------------------------------
  Left Click    | Select slot / start drag
  Drag & Drop   | Move item between slots
  Right Click   | Drop item to world
  Mouse Wheel   | Cycle hotbar / scroll inventory
  F Key         | Equip hovered item to hotbar

INVENTORY NAVIGATION (Keyboard/Controller Mode):
  Action             | Keyboard      | Xbox Controller
  -------------------|---------------|------------------
  Open/Close         | I key         | Y Button
  Navigate Grid      | Arrow Keys    | D-Pad
  Pick Up/Place Item | Enter         | A Button
  Equip to Hotbar    | F key         | (use navigation)
  Select Hotbar Slot | 1-5 keys      | LB/RB Bumpers

VAULT INVENTORY (ui/VaultInventory.java):
  - Up to 10,000 slots persistent storage
  - Accessed via VaultEntity (treasure chests)
  - Items persist across game sessions (JSON save)
  - Sorting: by rarity or alphabetical
  - Drag/drop between vault and player inventory

--------------------------------------------------------------------------------
9. ITEM SYSTEM (entity/item/)
--------------------------------------------------------------------------------

Items are defined with categories, rarities, and various properties. Each item
has its own dedicated class file in entity/item/items/ for easy customization.

ITEM CLASS LOCATION (entity/item/items/):
  Category       | Location                          | Count
  ---------------|-----------------------------------|-------
  Melee Weapons  | items/weapons/melee/              | 22
  Ranged Weapons | items/weapons/ranged/             | 16
  Throwing       | items/weapons/throwing/           | 3
  Ammo           | items/ammo/                       | 6
  Throwables     | items/throwables/                 | 2
  Tools          | items/tools/                      | 10
  Armor          | items/armor/                      | 20
  Food           | items/food/                       | 11
  Potions        | items/potions/                    | 14
  Materials      | items/materials/                  | 24
  Keys           | items/keys/                       | 4
  Clothing       | items/clothing/                   | 12
  Collectibles   | items/collectibles/               | 36
  Accessories    | items/accessories/                | 1
  Blocks         | items/blocks/                     | 17
  --------------------------------------------------------
  TOTAL                                              | 198

ITEM CATEGORIES (ItemCategory enum):
  WEAPON, RANGED_WEAPON, TOOL, ARMOR, CLOTHING, BLOCK, FOOD, POTION,
  MATERIAL, KEY, ACCESSORY, THROWABLE

ITEM RARITY (ItemRarity enum):
  Rarity    | Color  | Drop Rate
  ----------|--------|----------
  COMMON    | White  | 50%
  UNCOMMON  | Green  | 25%
  RARE      | Blue   | 15%
  EPIC      | Purple | 7%
  LEGENDARY | Orange | 2.5%
  MYTHIC    | Cyan   | 0.5%

ITEM PROPERTIES:
  - damage, defense           → Combat stats
  - attackSpeed, range        → Combat mechanics
  - critChance, critMultiplier→ Critical hit stats
  - healthRestore, manaRestore→ Consumable effects
  - specialEffect             → Unique item abilities
  - maxStackSize              → Inventory stacking limit

NOTABLE SPECIAL ITEMS:
  - Mirror to Other Realms (MirrorToOtherRealms.java):
    - Cycles through 3 realms every 400ms
    - Volcano Realm → Fireballs, Forest Realm → Arrows, Ocean Realm → Fish
    - Fires 3 projectiles per use, 25 mana cost

--------------------------------------------------------------------------------
10. MOB/ENEMY SYSTEM (entity/mob/)
--------------------------------------------------------------------------------

Enemies use an AI state machine for behavior. Each mob type has its own
dedicated class file for easy customization.

MOB CLASS LOCATION (entity/mob/mobs/):
  Category       | Location                          | Count
  ---------------|-----------------------------------|-------
  Humanoid       | mobs/humanoid/                    | 7
  Quadruped      | mobs/quadruped/                   | 10
  Special        | mobs/special/                     | 6
  --------------------------------------------------------
  TOTAL                                              | 23

MOB REGISTRY USAGE:
  SpriteMobEntity zombie = MobRegistry.create("zombie", 100, 500);
  SpriteMobEntity wolf = MobRegistry.create("wolf", 200, 500, "assets/mobs/wolf/alpha");

AI STATE MACHINE:
  State  | Behavior
  -------|------------------------------------------------
  IDLE   | Stand still, wait for player detection
  WANDER | Move randomly within area
  CHASE  | Move toward detected player
  ATTACK | Attack when in range
  HURT   | Flinch after taking damage
  FLEE   | Run away when health low
  DEAD   | Death animation, then despawn

HUMANOID MOBS (mobs/humanoid/):
  Type     | Health | Damage | Speed  | Special
  ---------|--------|--------|--------|------------------
  zombie   | 50     | 8      | 60     | Slow, persistent
  skeleton | 40     | 6      | 80     | Ranged bow attacks
  goblin   | 40     | 5      | 100    | Very fast, double jump
  orc      | 60     | 15     | 60     | Tank, heavy hitter
  bandit   | 45     | 8      | 70     | Balanced
  knight   | 55     | 12     | 50     | Reduced knockback
  mage     | 40     | 15     | 40     | Fire/Ice/Arcane magic

HUMANOID MOB WEAPON USAGE AI:
  Mob      | Weapon Preference      | Starting Loadout
  ---------|------------------------|------------------------------------------
  zombie   | MELEE_ONLY             | 60% armed: wooden sword, iron sword, mace
  skeleton | RANGED_ONLY            | Bow + backup sword
  goblin   | MELEE_AND_THROWABLE    | Dagger + rocks/throwing knives
  orc      | MELEE_AND_THROWABLE    | Battle axe + throwing axes
  bandit   | MELEE_AND_THROWABLE    | Sword + throwing knives/axes
  knight   | MELEE_ONLY             | Steel sword, mace, or legendary sword
  mage     | MAGIC_ONLY             | Magic wand, fire/ice/arcane staff

QUADRUPED MOBS (mobs/quadruped/):
  Type     | Health | Damage | Speed  | Behavior
  ---------|--------|--------|--------|------------------
  wolf     | 45     | 6      | 150    | Hostile, pack hunter
  bear     | 55     | 12     | 100    | Neutral, high damage
  dog      | 35     | 4      | 140    | Passive, fast
  cat      | 25     | 3      | 120    | Passive, flees when hit
  cow      | 40     | 3      | 80     | Passive, slow
  pig      | 30     | 2      | 60     | Passive
  sheep    | 35     | 2      | 70     | Passive, herding
  horse    | 50     | 4      | 120    | Passive, rideable (future)
  deer     | 40     | 3      | 110    | Passive, flees on sight
  fox      | 30     | 5      | 130    | Neutral, double jump

SPECIAL MOBS (mobs/special/):
  Type     | Health | Damage | Speed  | Special
  ---------|--------|--------|--------|------------------
  slime    | 25     | 3      | 60     | Small, bouncy, splits
  bat      | 20     | 4      | 120    | Small, triple jump
  spider   | 35     | 8      | 120    | Poison attack, climbing
  dragon   | 150    | 25     | 80     | Boss, fire/ice breath
  ogre     | 100    | 20     | 50     | Mini-boss, reduced knockback
  troll    | 80     | 18     | 55     | Health regeneration

REQUIRED MOB SPRITE FILES:
  assets/mobs/[mob_name]/
  ├── idle.gif, walk.gif, run.gif (optional), attack.gif
  ├── hurt.gif, death.gif
  └── burning.gif (optional), frozen.gif (optional)

--------------------------------------------------------------------------------
11. ANIMATION SYSTEM (animation/)
--------------------------------------------------------------------------------

The game supports both modern GIF-based animations and legacy bone-based
animations. GIF sprites are the recommended approach.

SPRITE ANIMATION (animation/SpriteAnimation.java):
  Manages GIF-based animations for different action states.

ACTION STATES (ActionState enum):
  Movement: IDLE, WALK, RUN, SPRINT
  Jumping:  JUMP, DOUBLE_JUMP, TRIPLE_JUMP, FALL
  Combat:   ATTACK, FIRE, BLOCK, CAST
  Items:    USE_ITEM, EAT
  Reactions: HURT, DEAD
  Effects:  BURNING, FROZEN, POISONED

EQUIPMENT OVERLAY (animation/EquipmentOverlay.java):
  Renders equipment on top of the player sprite:
  Slot       | Render Order | Description
  -----------|--------------|---------------------------
  HAIR_BACK  | Behind       | Ponytails, long hair back
  BOOTS      | 1            | Footwear
  LEGS       | 2            | Pants, leggings
  BELT       | 3            | Belts, sashes
  CHEST      | 4            | Shirts, armor
  HELMET     | 5            | Hats, helmets
  HAIR_FRONT | 6            | Bangs, front hair
  HELD_ITEM  | 7            | Currently held item

ANIMATED TEXTURE (animation/AnimatedTexture.java):
  - Extracts frames from GIF files
  - Respects per-frame delays from GIF metadata
  - Provides getCurrentFrame() for rendering

--------------------------------------------------------------------------------
12. BLOCK SYSTEM (block/)
--------------------------------------------------------------------------------

Blocks form the destructible/constructible terrain.

BLOCK TYPES (BlockType enum):
  Type        | Solid | Description
  ------------|-------|---------------------------
  GRASS       | Yes   | Dirt with grass overlay
  DIRT        | Yes   | Basic terrain
  STONE       | Yes   | Hard rock
  COBBLESTONE | Yes   | Broken stone
  WOOD        | Yes   | Tree wood
  LEAVES      | No    | Tree foliage (passable)
  BRICK       | Yes   | Construction
  SAND        | Yes   | Beach/desert
  WATER       | No    | Liquid (non-solid)
  GLASS       | No    | Transparent
  COAL_ORE    | Yes   | Mineable ore
  IRON_ORE    | Yes   | Mineable ore
  GOLD_ORE    | Yes   | Mineable ore
  SNOW        | Yes   | Packed snow block
  ICE         | No    | Frozen ice (slippery)
  MOSS        | Yes   | Moss-covered block
  VINES       | No    | Hanging vines (passable)
  PLATFORM    | Semi  | One-way platform

BLOCK OVERLAYS (BlockOverlay enum):
  Overlays render on top of base blocks and must be removed before mining:
  - GRASS  → Green grass tufts on top
  - SNOW   → White snow layer
  - ICE    → Semi-transparent ice coating
  - MOSS   → Green moss patches
  - VINES  → Hanging vine decoration

BLOCK INTERACTION:
  1. Click block within 3-block radius to select
  2. Selected block shows yellow highlight
  3. Arrow keys change mining direction indicator
  4. Click selected block again (or press E) to mine from the chosen direction
  5. Clicking elsewhere or moving out of range deselects the block

BLOCK PLACEMENT:
  - Player must hold a BLOCK category item
  - Left click empty space within 3-block radius
  - Cannot place where player is standing or on existing blocks

--------------------------------------------------------------------------------
13. LEVEL SYSTEM (level/)
--------------------------------------------------------------------------------

Levels are defined in JSON files in the levels/ directory.

LEVEL JSON STRUCTURE:
  {
    "name": "Level Name",
    "description": "Level description",
    "playerSpawnX": 100, "playerSpawnY": 620,
    "levelWidth": 1920, "levelHeight": 1080,
    "groundY": 720,
    "scrollingEnabled": true, "verticalScrollEnabled": false,
    "nightMode": false, "nightDarkness": 0.8, "ambientLight": 0.12,
    "parallaxEnabled": true, "parallaxLayers": [...],
    "platforms": [...], "blocks": [...], "items": [...], "mobs": [...],
    "doors": [...], "buttons": [...], "vaults": [...], "triggers": [...]
  }

LEVEL DATA CLASSES:
  PlatformData, BlockData, ItemData, MobData, DoorData, ButtonData,
  VaultData, TriggerData, LightSourceData, ParallaxLayerData

--------------------------------------------------------------------------------
14. GRAPHICS SYSTEM (graphics/)
--------------------------------------------------------------------------------

Handles rendering, camera, lighting, and parallax backgrounds.

CAMERA (Camera.java):
  - Follows player with smooth interpolation
  - Dead zone prevents jitter on small movements
  - Clamps to level bounds
  - Converts screen ↔ world coordinates

LIGHTING SYSTEM (LightingSystem.java):
  - Day/night cycle support
  - Dynamic point light sources
  - Ambient lighting level
  - Darkness overlay with light cutouts

PARALLAX BACKGROUND (ParallaxBackground.java, ParallaxLayer.java):
  Layers at different depths scroll at different speeds.
  Supports both static images (PNG, JPG) and animated GIFs.

  Depth Level      | Z-Index | Scroll Speed
  -----------------|---------|-------------
  Z_BACKGROUND     | -2      | 0.1x (slowest)
  Z_MIDDLEGROUND_3 | -1      | 0.3x
  Z_MIDDLEGROUND_2 | 0       | 0.5x
  Z_MIDDLEGROUND_1 | 1       | 0.7x
  Z_FOREGROUND     | 2       | 1.2x (fastest)

RENDER PIPELINE ORDER:
  1. Clear screen
  2. Draw parallax background layers (world space)
  3. Draw game entities (world space, camera transformed)
  4. Draw blocks and terrain
  5. Draw lighting overlay
  6. Draw UI elements (screen space)
  7. Draw settings/menu overlays

--------------------------------------------------------------------------------
15. INPUT SYSTEM (input/InputManager.java, input/ControllerManager.java)
--------------------------------------------------------------------------------

Singleton that captures all keyboard, mouse, and Xbox controller input.

XBOX CONTROLLER SUPPORT:
  The game supports Xbox controllers via JInput library.
  When a controller is connected, a visible crosshair cursor appears.

  CONTROLLER SETUP:
    1. Download JInput library (see lib/README.md)
    2. Place jinput-2.0.10.jar in lib/ folder
    3. Add JARs to project classpath
    4. Configure VM args: -Djava.library.path=lib

  CONTROLLER MAPPINGS:
    Controller Input    | Game Action        | Keyboard Equivalent
    --------------------|--------------------|-----------------------
    Left Stick          | Movement           | WASD keys
    Left Stick Click    | Sprint             | Shift key
    Right Stick         | Mouse cursor       | Mouse movement
    Right Trigger (RT)  | Click/Select/Drag  | Left mouse click
    Left/Right Bumper   | Hotbar prev/next   | Scroll wheel
    D-Pad               | Inventory navigate | Arrow keys
    A Button            | Jump / Inv. select | Space / Enter
    X Button            | Interact/Mine      | E key
    Y Button            | Inventory          | I key
    Start Button        | Menu/Settings      | M key
    Back Button         | Back/Cancel        | Escape key

CONTROLLER VIBRATION/HAPTIC FEEDBACK:
  Pattern Type  | Intensity | Duration | Use Case
  --------------|-----------|----------|----------------------------------
  MINOR         | 0.08-0.25 | 30-100ms | UI clicks, item pickups, footsteps
  GREATER       | 0.35-0.90 | 80-300ms | Damage, attacks, jumps, explosions
  INTRICATE     | Variable  | 500-2000ms | Chest opening, boss encounters

KEYBOARD INPUT:
  InputManager input = InputManager.getInstance();
  if (input.isKeyPressed('a')) { /* move left */ }
  if (input.isKeyJustPressed('e')) { /* interact */ }

MOUSE INPUT:
  int mouseX = input.getMouseX();
  if (input.isLeftMousePressed()) { /* attack/mine */ }
  if (input.isLeftMouseJustPressed()) { /* one-shot click */ }

--------------------------------------------------------------------------------
16. SETTINGS MENU (ui/SettingsOverlay.java, input/KeyBindings.java)
--------------------------------------------------------------------------------

The game features a comprehensive in-game settings menu accessible by pressing
the M key or Start button on a controller.

OPENING THE SETTINGS MENU:
  - Keyboard: Press M key
  - Controller: Press Start button
  - In-Game: Click the "Settings (M)" button in top-right corner

SETTINGS TABS:

  1. AUDIO TAB
     - Music Volume slider (0-100%)
     - Sound Effects Volume slider (0-100%)
     - Mute All toggle button

  2. CONTROLS TAB
     - Toggle between Keyboard and Controller bindings
     - Click any control to rebind it
     - If a key/button is already bound, bindings are swapped automatically
     - "Reset to Defaults" button restores original bindings

     KEYBOARD BINDINGS:
       Move Left (A), Move Right (D), Move Up (W), Move Down (S),
       Jump (Space), Sprint (Shift), Interact (E), Inventory (I), Attack (F)

     CONTROLLER BINDINGS:
       A (Jump), B (Back), X (Interact), Y (Inventory),
       LB/RB (Hotbar), Start (Menu), Back (Cancel), L3 (Sprint), RT (Attack)

  3. GAME TAB
     - Vibration Toggle
     - Day/Night Toggle
     - Debug Mode Toggle (F3)

  4. ACTIONS TAB
     - Return to Main Menu
     - Customize Character
     - Toggle Music
     - Exit Game

KEY BINDINGS PERSISTENCE:
  - Keyboard: saves/keybindings.dat
  - Controller: saves/controller_bindings.dat

--------------------------------------------------------------------------------
17. SAVE SYSTEM (save/SaveManager.java)
--------------------------------------------------------------------------------

Singleton that handles JSON-based persistence.

SAVE FILE LOCATION: saves/player_data.json

SAVED DATA:
  - Player inventory items with stack counts
  - Vault inventory (up to 10,000 items)
  - Chest cooldowns (daily/monthly)
  - Statistics (items collected, legendary count)
  - Developer mode flag
  - Equipment loadout
  - Character customization (skin tone, hair color)

SAVE MANAGER USAGE:
  SaveManager save = SaveManager.getInstance();
  save.saveInventory(player.getInventory());
  save.loadInventory(player.getInventory());
  if (save.canOpenDailyChest()) { save.recordDailyChestOpened(); }

CHEST COOLDOWNS:
  - DAILY_COOLDOWN = 24 hours
  - MONTHLY_COOLDOWN = 30 days
  - Cooldowns reset in developer mode for testing

--------------------------------------------------------------------------------
18. AUDIO SYSTEM (audio/AudioManager.java, audio/SoundAction.java)
--------------------------------------------------------------------------------

Manages background music and sound effects with MP3 support via JLayer.

JLAYER SETUP (Required for MP3 support):
  1. Download jlayer-1.0.1.jar from Maven repository
  2. Place the JAR in the lib/ folder
  3. Add lib/jlayer-1.0.1.jar to your project classpath

SUPPORTED FORMATS:
  - MP3 files (via JLayer) - recommended
  - WAV files (via javax.sound.sampled) - legacy support

AUDIO MANAGER USAGE:
  AudioManager audio = new AudioManager();
  audio.playAction(SoundAction.JUMP);
  audio.playAction(SoundAction.USE_BATTLE_AXE);
  audio.loadMusicMP3("sounds/compressed/music/music_level_1.mp3");
  audio.playMusic();
  audio.setMusicVolume(0.7f);
  audio.setSFXVolume(0.8f);
  audio.setMuteAll(true);

SOUND ACTION CATEGORIES:
  player, combat/melee, combat/ranged, combat/throw, combat/impact,
  effects, items, tools, blocks/break, blocks/place, footsteps,
  inventory, chests, doors, mobs, ui, music, ambient, events, npc, crafting, special

SOUND FILE LOCATIONS:
  sounds/                  → Legacy WAV files
  sounds/compressed/       → MP3 files organized by category

--------------------------------------------------------------------------------
19. UI SYSTEM (ui/)
--------------------------------------------------------------------------------

Custom UI components rendered directly on the game canvas.

UI BUTTON (UIButton.java):
  UIButton btn = new UIButton(x, y, width, height, "Label");
  btn.setOnClick(() -> { /* action */ });
  btn.update(input);
  btn.draw(g);

UI SLIDER (UISlider.java):
  UISlider slider = new UISlider(x, y, width, min, max, initial);
  slider.setOnChange((value) -> { /* handle value */ });

PLAYER STATUS BAR (PlayerStatusBar.java):
  Displays health (red), mana (blue), and stamina (green) bars.

SETTINGS OVERLAY (SettingsOverlay.java):
  Comprehensive tabbed settings panel (see Section 16).

INVENTORY UI:
  - 8x4 grid (32 slots) with 5-slot hotbar
  - Rarity color-coded borders
  - Stack count display
  - Drag-and-drop support
  - Tooltip on hover (item stats)

--------------------------------------------------------------------------------
20. SPECIAL GAME FEATURES
--------------------------------------------------------------------------------

LOOT GAME SCENE:
  Mini-game focused on collecting loot from chests:
  - Wide level (6000px) with limited vertical space
  - Daily Chest (center): Opens once per 24 hours, drops 3 items
  - Monthly Chest (right): Opens once per 30 days, drops 10 items
  - Vault for persistent storage
  - Items bounce with physics when dropped
  - Rarity-colored light beams (Borderlands style)
  - Alchemy Table for crafting items
  - Reverse Crafting Table for deconstructing items

ALCHEMY/CRAFTING SYSTEM:
  ALCHEMY TABLE (Green Glow):
  - Combines 1-3 items to create new items
  - Approach table and press 'E' to open UI
  - Drag items from inventory to 3 input slots
  - Output slot shows the craftable result

  REVERSE CRAFTING TABLE (Purple Glow):
  - Breaks down items into component parts
  - 1 input slot, 3 output slots

  RECIPES (data/alchemy_recipes.json):
  - 100+ recipes: Weapons, Armor, Tools, Potions, Materials, Ammo, Blocks

DOOR AND TRIGGER SYSTEM:
  DoorEntity: Interactive doors between areas, lock/key system
  TriggerEntity: Invisible activation zones
  ButtonEntity: Interactive switches/levers

CHARACTER CUSTOMIZATION:
  Available in SpriteCharacterCustomization scene:
  - 8 skin tone presets
  - 13 hair color presets
  - Equipment slots: Hair, Helmet, Chest, Legs, Boots, Belt
  - Companion selection

PROJECT STRUCTURE
src/                    - Game engine source code (organized by package)
  core/                 - Main application classes (Main, GameWindow, GamePanel)
  scene/                - Scene management (Scene, SceneManager, GameScene, menus)
  entity/               - Base entity classes (Entity, EntityManager, SpriteEntity)
    - Item.java, ItemRegistry.java, ProjectileEntity.java
    - RecipeManager.java, AlchemyTableEntity.java, MirrorToOtherRealms.java
    item/items/         - 190 individual item classes organized by category
    player/             - Player classes (SpritePlayerEntity, AbilityScores, PlayableCharacter)
    mob/mobs/           - 23 individual mob classes (humanoid/, quadruped/, special/)
    mob/old/            - Deprecated bone-based mob classes (legacy)
  block/                - Block system (BlockEntity, BlockType, BlockRegistry)
  animation/            - Animation system (SpriteAnimation, EquipmentOverlay, AnimatedTexture)
    bone/               - Legacy bone animation (Skeleton, Bone, BoneAnimation)
  graphics/             - Rendering (Camera, LightingSystem, TextureManager, Parallax)
  level/                - Level loading (LevelData, LevelLoader)
  audio/                - Sound management (AudioManager, SoundAction)
  input/                - Input handling (InputManager, ControllerManager, KeyBindings, VibrationPattern)
  ui/                   - UI components (UIButton, Inventory, AlchemyTableUI, SettingsOverlay)
devtools/               - Development tools (texture generators, animation importers)
tools/                  - Utility tools for asset generation
assets/
  characters/           - Playable character sprites (crystal, breaya, trethas, etc.)
  textures/             - humanoid/, quadruped/, blocks/
  particles/            - Status effect particle overlays
  parallax/             - Parallax background layers
  mobs/                 - GIF-based mob sprites
sounds/                 - Sound files (organized in sounds/compressed/)
levels/                 - Level JSON files
data/                   - Game data files (alchemy_recipes.json)

TODOs
Work on section 1.01 (Inventory and items)

KNOWN ISSUES
(None - all known issues have been resolved)

FUTURE FEATURES (ROADMAP)

1.01 Inventory and Items
	-Robust 32 slot inventory system with hot bars
		-Allows stacking of identical items
		-Displays item details and stats when hovering over items
		-Has a custom texture for the UI
		-Can filter items by rarity and alphabetical
		-Items dragged out of inventory drop on ground
	-Robust item system
		-Categories: Tools, Weapons, Armor/clothing, Blocks, Food/Potion, Other
		-Properties: Rarity, Range, Defense, Special effect
		-Projectile system with hitbox detection
		-Crafting system (combine 2-3 items)
		-Block variants with texture masks
		-Key/lock system
		-Area of effect items

1.02 Character Selection
	-~10 customizable characters unlocked through gameplay
	-Each character has unique stats and abilities
	-Wearable clothes, armor and customization features

1.03 NPCs and dialogue
	-Pixel art dialogue overlays
	-NPC characters with custom dialogue
	-Shops with variable prices
	-Companion system

1.04 Boss Fights
	-Complex AI for end-of-area boss fights
	-At least 10 variable boss AI variations

1.05 Overworld
	-Level hubs with levels, shops, boss fights, player houses

1.06-1.08 Area Level Design and Testing
	-Areas 1-6 with 9 levels each, shops, boss fights

RESOLVED ISSUES

The following issues have been fixed. Details grouped by category:

FILE ORGANIZATION:
  - Consolidated player textures to assets/textures/humanoid/player/
  - Moved texture generators to devtools/
  - Moved sound files to sounds/ directory
  - Moved blocks to assets/textures/blocks/
  - Moved BlockbenchAnimationImporter to devtools/
  - Reorganized 48 Java files into logical packages (core, scene, entity, etc.)
  - Moved bone animation classes to animation/bone/
  - Moved deprecated bone-based mobs to entity/mob/old/

MOB SYSTEM:
  - Mobs now target nearest edge of player hitbox, not center
  - Increased all mob hitbox sizes by ~40%
  - Fixed mobs walking through solid blocks (proper collision detection)
  - Created SpriteMobEntity for GIF-based mob animations
  - Auto-configuration of HP/stats based on mob type
  - Status effects (BURNING, FROZEN, POISONED) with particle overlays

BLOCK SYSTEM:
  - Red damage overlay only appears when block is actively targeted
  - Added block overlay system (GRASS, SNOW, ICE, MOSS, VINES)
  - Directional mining with arrow indicator
  - Block placement via left click

PLAYER SYSTEM:
  - Fixed triple jump double-trigger bug
  - Added skin tone selection (8 presets)
  - Added belt and hair equipment slots
  - Fixed charged shots: bows consume arrows, magic consumes mana

UI/UX:
  - Character customization UI reorganized for 1920x1080
  - Color sliders no longer overlap with character screen
  - Main menu opens sprite-based customization
  - Removed unnecessary Lighting Demo scene
  - UI elements now consume clicks to prevent game actions
  - Companions are now player character alternates, not items

INPUT/CONTROLS:
  - Left click works for block mining alongside E key
  - Scroll wheel sensitivity adjusted (threshold 1.5)
  - Xbox controller: swapped stick functions, added visible cursor
  - RT emulates mouse button for full UI navigation
  - LB/RB cycle hotbar slots

GRAPHICS:
  - Fixed moving entities masking non-transparent pixels
  - Refined quadruped shapes and textures (10 distinct animal types)
  - Added GIF support to all texture systems (AnimatedTexture class)
  - Fixed vertical scrolling black bars (parallax now supports vertical)
  - Fixed darkness overlay opacity (proper alpha blending)

WINDOW:
  - Borderless 1920x1080 window (not fullscreen)
  - Window no longer steals focus from other applications
  - Scene transitions sped up (0.12 transition speed)
