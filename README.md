TITLE	The Amber Moon
AUTHOR Larson Sonderman & CLAUDE AI

DESCRIPTION
A Java Swing-based game engine that runs a 2D platformer game. Plays similarly to Terraria, Super Mario, or 2D Minecraft.
Inspired by Dungeons and Dragons games like Baldur's Gate 3. (Non-turn-based)
This game should feel abundant with the quantity and variations in items and tools.

CURRENT FEATURES
-JSON file-based level loading
-Block-based level construction
-Sprite-based player animations with GIF support
-Sprite-based mob animations with hitbox collision detection
-Equipment overlay system for character customization (helmets, chest, legs, boots, etc.)
-Block overlay system (grass, snow, ice overlays on base blocks)
-Bone-based animations (legacy, moved to animation.bone package)
-Entity management with custom AI for Humanoid and Quadruped entities
-Basic lighting support (simple dark mask and light sources)
-Simple parallax with animated background support
-Scrolling camera that follows the player

PROJECT STRUCTURE
src/                    - Game engine source code (organized by package)
  core/                 - Main application classes (Main, GameWindow, GamePanel)
  scene/                - Scene management (Scene, SceneManager, GameScene, menus)
  entity/               - Base entity classes (Entity, EntityManager, SpriteEntity)
    player/             - Player-specific classes (PlayerBase, PlayerEntity, PlayerBoneEntity)
    mob/                - Mob AI classes (MobEntity, SpriteMobEntity, HumanoidMobEntity, QuadrupedMobEntity)
  block/                - Block system (BlockEntity, BlockType, BlockRegistry, BlockAttributes)
  animation/            - Animation system (SpriteAnimation, EquipmentOverlay, AnimatedTexture)
    bone/               - Legacy bone animation (Skeleton, Bone, BoneAnimation, QuadrupedSkeleton)
  graphics/             - Rendering (Camera, LightingSystem, TextureManager, Parallax)
  level/                - Level loading (LevelData, LevelLoader)
  audio/                - Sound management (AudioManager)
  input/                - Input handling (InputManager)
  ui/                   - UI components (UIButton, UISlider, Inventory, ToolType)
devtools/               - Development tools (texture generators, animation importers)
  - TextureGenerator.java           - Generates player bone textures
  - HumanoidTextureGenerator.java   - Generates humanoid mob textures
  - QuadrupedTextureGenerator.java  - Generates quadruped animal textures
  - BlockTextureGenerator.java      - Generates block textures
  - ParallaxTextureGenerator.java   - Generates parallax backgrounds
  blockbench/             - Blockbench import tools (legacy bone animation support)
    - BoneTextureGenerator.java       - Generates simple bone textures
    - BlockbenchAnimationImporter.java - Imports Blockbench animation files
assets/
  textures/
    humanoid/           - Humanoid character textures (player, orc, zombie, skeleton)
    quadruped/          - Animal textures (wolf, dog, cat, horse, etc.)
    blocks/             - Block textures (grass, dirt, stone, etc.)
  parallax/             - Parallax background layers
sounds/                 - All sound files (music, effects, footsteps)
levels/                 - Level JSON files

TODOs
Work on section 1.01 (Inventory and items) 

KNOWN ISSUES

(All previously listed issues have been resolved - see RESOLVED ISSUES section)

FUTURE FEATURES (ROADMAP)

1.01 Inventory and Items
	-Robust 32 slot inventory system with hot bars
		-Allows stacking of identical items
		-Displays item details and stats when hovering over items
		-Still scrolls with player as they move
		-Has a custom texture for the UI that is located in the assets folder
		-Can filter items by rarity and alphabetical
		-Items dragged out of inventory drop on ground in front of player
	-Robust item system
		-Items have distinct properties and categories, not just names
			-Categories include:
				-Tools
				-Weapons
				-Armor and clothing
				-Blocks
				-Food/Potion
				-Other
			-Properties include:
				-Rarity (white/green/blue/purple/orange/cyan)
				-Range (Weapons and tools)
				-Defense %
				-Special effect
				-Other
		-Items should all be held by the player when equipped in hot bar. Hot bar scrolling should be bound to right click
		-Projectiles should be included with some items (crossbow for example). This projectile system should have its own hitbox system with entities
		-A crafting system should be present. It will function by allowing players to combine 2 to 3 items at certain locations to create new items.
		-Blocks should have variants that are just texture masks of the base block. For example, a frozen block would use an ice mask that is semi-transparent to cover the block or a grass block would use a grass mask over dirt. These block variants must be broken down before being mined. Ice must shatter and grass must be uprooted before the player can mine the block.
		-The player should be able to place blocks immediately in front of them
		-Some items should work as 'keys' to activate certain entities such as other items or doors
		-Some items will have an area of effect range
		-List of item examples:
			-wizards hat
			-exploding potion
			-crossbow
			-sword
			-bread
			-string
			-magic wand
			-scroll
			
1.02 Character Selection
	-A selection of around 10 customizable characters
		-Characters will be unlocked through gameplay
		-Every level will require that you select one unlocked character before playing
		-Each character has their own stats and abilities
		-Wearable clothes, armor and other customization features
		-Each level will be tailored to one or several character abilities
		
1.03 NPCs and dialogue
	-Pixel art dialogue overlays similar to Pokemon
	-NPC characters with custom dialogue that appears in game as a blurb next to their body
	-Place for generic sounds as the NPCs talk
	-Shops with variable prices
	-Companion system where some NPCs will follow the player
	-NPCs should act like complex entities similar to the player but with automated functionality
	
1.04 Boss Fights
	-Complex and unique AI for special 'end-of-area' boss fights
	-Unique movement and attack mechanics for boss fights
	-At least 10 variable boss AI variations, ranging in difficulty
	
1.05 Overworld for level loading and fast travel
	-Each major section of the game will be divided into overworlds with levels similar to those in later Super Mario games
	-Each overworld area will contain levels, shops, boss fights, and player owned houses
		-Player owned houses will be places to customize and display rare loot collected throughout the game
		
1.06 Area 1 level design
	-Level 1-9
	-1 shop unlocked at level 3
	-1 boss fight

1.07 Finish game and level designs (Areas 2-6)

1.08 Bug fixes and testing

RESOLVED ISSUES

[FIXED] File order is confusing. Player textures are in 3 redundant locations
  -> Consolidated to single canonical location: assets/textures/humanoid/player/

[FIXED] Texture generators should not be part of the game engine, as only PNG and GIF files should be used for textures and imported externally into the assets folder
  -> Moved all texture generators to devtools/ directory

[FIXED] Some sound files are located in the base 'assets' folder and should be located in sounds
  -> Moved collect.wav, drop.wav, jump.wav, music.wav to sounds/ directory

[FIXED] 'blocks' assets should be a sub folder within textures
  -> Moved blocks to assets/textures/blocks/

[FIXED] Blockbench support unclear in usage
  -> BlockbenchAnimationImporter moved to devtools/, runtime import deprecated

[FIXED] When mobs attack (specifically quadrupeds) they target the center of the player
  -> Mobs now target the nearest edge of player's hitbox using getDistanceToTargetFace()
  -> Attack distance calculated to front/back of player, not center point

[FIXED] Red behind blocks when breaking does not disappear
  -> Red damage overlay now only appears when block is actively targeted by player
  -> Added targeted state to BlockEntity, resets each frame

[FIXED] Mob hitboxes too small
  -> Increased all mob hitbox sizes by ~40% for better hit detection
  -> Humanoid hitboxes: 60x120 (was 40x100)
  -> Quadruped hitboxes scaled proportionally per animal type

[FIXED] Hostile mobs ignore block collisions and walk through solid blocks
  -> Rewrote MobEntity.applyPhysics() to check collisions BEFORE applying movement
  -> Added horizontal collision detection with solid blocks
  -> Added proper vertical collision detection (both falling and jumping)
  -> Mobs now stop at walls and land on platforms correctly

[FIXED] Java class structure should be organized by relationship to other classes
  -> Reorganized all 48 Java files into logical packages under src/
  -> core/ - Main application entry points
  -> scene/ - Scene management and game states
  -> entity/ - Entity hierarchy with player/ and mob/ subpackages
  -> block/ - Block system classes
  -> animation/ - Skeleton and bone animation system
  -> graphics/ - Rendering, camera, lighting, parallax
  -> level/ - Level data and loading
  -> audio/ - Sound management
  -> input/ - Input handling
  -> ui/ - UI components and inventory

[FIXED] End of levels does not immediately take you back to menu
  -> TriggerEntity now supports menu return for empty/menu targets
  -> Scene transitions sped up (transitionSpeed increased from 0.05 to 0.12)

[FIXED] Vertical scrolling has black bars on top and bottom of screen
  -> Black bars only drawn when parallax background is not enabled
  -> Parallax layers now properly support vertical scrolling with anchorBottom property
  -> verticalMargin can be set to 0 to eliminate letterboxing

[FIXED] Effect for night and darkness is too opaque
  -> Lighting buffer now properly cleared to transparent before drawing darkness overlay
  -> Darkness overlay correctly blends with parallax background layers
  -> Default nightDarkness reduced, ambientLevel increased for better visibility

[FIXED] 'e' to break blocks should be bound to left mouse click
  -> Added MouseListener implementation to InputManager
  -> Left mouse click now works for block mining alongside 'E' key

[FIXED] Moving entities mask more than non-transparent pixels when changing color
  -> Replaced AlphaComposite overlay with per-pixel tinting in Bone.java
  -> Tint now only affects pixels where alpha > 0, preserving transparency
  -> Cached tinted textures for performance optimization

[FIXED] Quadrupeds shape and textures need to be refined
  -> Added species-specific head generation (canine snout, cat round face, pig disc nose, etc.)
  -> Added species-specific body features (cow spots, sheep wool, cat stripes, fox white chest)
  -> Improved walk/run animations with smoother, more natural movement
  -> All 10 animal types now visually distinct and recognizable

[FIXED] All texture files should support GIF file types
  -> Created AnimatedTexture class for managing GIF frame cycling with proper timing
  -> Updated AssetLoader to extract all frames from GIF files with per-frame delays
  -> Added GIF support to Bone class for animated bone textures (e.g., glowing effects)
  -> Added GIF support to ParallaxLayer for animated backgrounds (e.g., moving clouds)
  -> Added GIF support to BlockRegistry for animated blocks (e.g., lava, water)
  -> Added GIF support to SpriteEntity for animated sprites
  -> GIF animations respect original frame delays and loop automatically
  -> All components include update() methods for frame advancement

[FIXED] Character customization UI color sliders overlapped with character screen
  -> Moved slider startY from 120 to 150 to prevent overlap
  -> Removed dead code that incorrectly accessed slider values for positioning

[FIXED] Old bone-based character customization should be removed from main menu
  -> Main menu now opens sprite-based customization (SpriteCharacterCustomization)
  -> Bone-based CharacterCustomizationScene kept for legacy PlayerBoneEntity support

[FIXED] Lighting demo level unnecessary (uses java class not JSON file)
  -> Removed LightingDemoScene.java and its registration in GamePanel
  -> Removed Lighting Demo button from main menu

[FIXED] Old bone-based animation assets need to be moved to their own folder
  -> Moved Bone.java, BoneAnimation.java, Skeleton.java, QuadrupedSkeleton.java, QuadrupedAnimation.java to animation/bone/ package
  -> Updated all imports in 10+ files to use animation.bone.*
  -> Moved BlockbenchAnimationImporter.java and BoneTextureGenerator.java to devtools/blockbench/

[FIXED] Update README.md with current features
  -> Updated CURRENT FEATURES to reflect sprite-based animation system
  -> Updated PROJECT STRUCTURE with new package organization
  -> Moved resolved issues from KNOWN ISSUES to RESOLVED ISSUES

[FIXED] Blocks should work with an overlay system
  -> Created BlockOverlay enum with GRASS, SNOW, ICE, MOSS, VINES overlay types
  -> Added overlay support to BlockEntity with rendering and damage tracking
  -> Overlays render on top of base block textures with semi-transparency
  -> Overlays must be removed before mining the base block
  -> Includes procedural texture generation for overlays when files not available
  -> Added overlay texture caching to BlockRegistry for efficiency

[FIXED] Mobs should be sprite based with hitbox collision detection
  -> Created SpriteMobEntity class for GIF-based mob animations
  -> Supports idle, walk, run, attack, hurt, death animation states
  -> Proper hitbox collision detection with configurable hitbox size
  -> Inherits AI state machine from MobEntity (IDLE, WANDER, CHASE, ATTACK, etc.)
  -> Health bar rendering and invincibility flash effects
  -> Debug mode for visualizing hitboxes and mob state
  -> Placeholder sprite generation for testing without assets
