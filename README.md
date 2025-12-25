TITLE	The Amber Moon
AUTHOR Larson Sonderman & CLAUDE AI

DESCRIPTION
A Java Swing-based game engine that runs a 2D platformer game. Plays similarly to Terraria, Super Mario, or 2D Minecraft.
Inspired by Dungeons and Dragons games like Baldur's Gate 3. (Non-turn-based)

CURRENT FEATURES
-JSON file-based level loading
-Block-based level construction
-Bone-based animations for the player entity with blockbench import support
-Entity management with custom AI for Humanoid and Quadruped entities
-Basic lighting support (simple dark mask and light sources)
-Simple parallax
-Scrolling camera that follows the player (needs fixing for vertical scrolling level, see 'KNOWN ISSUES')

TODOs
Begin debugging the following 'KNOWN ISSUES'

KNOWN ISSUES

File order is confusing. Player textures are in 3 redundant locations
Java class structure should be organized by relationship to other classes
Texture generators should not be part of the game engine, as only PNG and GIF files should be used for textures and imported externally into the assets folder
Some sound files are located in the base 'assets' folder and should be located in sounds
'blocks' assets should be a sub folder within textures 
Blockbench support unclear in usage
Occasional stuttering when rendering player
Mob hitboxes too small
Moving entities (Mobs and player) mask more than non-transparent pixels when changing color
Quadrupeds shape and textures need to be refined
Red behind blocks when breaking does not disappear. Should only appear when block is targeted
When mobs attack (specifically quadrupeds) they target the center of the player. Mobs should target the front or back of the player's hitbox
End of levels does not immediately take you back to menu
Vertical scrolling has black bars on top and bottom of screen (found after parallax was added). Parallax level should consider vertical scrolling. This is important because most levels will require the player to move vertically and horizontally
Effect for night and darkness is too opaque
All texture files should support GIF file types.
Lighting demo level unnecessary (uses java class not JSON file to render)
'e' to break blocks should be bound to left mouse click

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
