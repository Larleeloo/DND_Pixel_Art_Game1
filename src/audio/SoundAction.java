package audio;

/**
 * Enumeration of all game actions that can trigger sound effects.
 * Each action maps to a potential MP3 file in sounds/compressed/[category]/[action].mp3
 *
 * Usage:
 *   audioManager.playAction(SoundAction.JUMP);
 *   audioManager.playAction(SoundAction.USE_BATTLE_AXE);
 */
public enum SoundAction {
    // ============================================
    // PLAYER MOVEMENT
    // ============================================
    IDLE("player/idle"),
    WALK("player/walk"),
    RUN("player/run"),
    SPRINT("player/sprint"),
    JUMP("player/jump"),
    DOUBLE_JUMP("player/double_jump"),
    TRIPLE_JUMP("player/triple_jump"),
    FALL("player/fall"),
    LAND("player/land"),
    LAND_HARD("player/land_hard"),

    // ============================================
    // PLAYER COMBAT - GENERAL
    // ============================================
    ATTACK("combat/attack"),
    ATTACK_MISS("combat/attack_miss"),
    ATTACK_CRITICAL("combat/attack_critical"),
    BLOCK("combat/block"),
    BLOCK_BREAK("combat/block_break"),
    PARRY("combat/parry"),
    DODGE("combat/dodge"),

    // ============================================
    // PLAYER COMBAT - MELEE WEAPONS
    // ============================================
    USE_SWORD("combat/melee/use_sword"),
    USE_BATTLE_AXE("combat/melee/use_battle_axe"),
    USE_MACE("combat/melee/use_mace"),
    USE_DAGGER("combat/melee/use_dagger"),
    USE_SPEAR("combat/melee/use_spear"),
    USE_HAMMER("combat/melee/use_hammer"),
    USE_CLUB("combat/melee/use_club"),
    USE_SCYTHE("combat/melee/use_scythe"),
    USE_FISTS("combat/melee/use_fists"),

    // ============================================
    // PLAYER COMBAT - RANGED WEAPONS
    // ============================================
    FIRE("combat/ranged/fire"),
    FIRE_BOW("combat/ranged/fire_bow"),
    FIRE_CROSSBOW("combat/ranged/fire_crossbow"),
    FIRE_WAND("combat/ranged/fire_wand"),
    FIRE_STAFF("combat/ranged/fire_staff"),
    FIRE_SLINGSHOT("combat/ranged/fire_slingshot"),
    DRAW_BOW("combat/ranged/draw_bow"),
    DRAW_CROSSBOW("combat/ranged/draw_crossbow"),
    RELOAD_CROSSBOW("combat/ranged/reload_crossbow"),
    CHARGE_MAGIC("combat/ranged/charge_magic"),
    RELEASE_MAGIC("combat/ranged/release_magic"),

    // ============================================
    // PLAYER COMBAT - THROWABLES
    // ============================================
    THROW("combat/throw/throw"),
    THROW_KNIFE("combat/throw/throw_knife"),
    THROW_AXE("combat/throw/throw_axe"),
    THROW_ROCK("combat/throw/throw_rock"),
    THROW_BOMB("combat/throw/throw_bomb"),
    THROW_POTION("combat/throw/throw_potion"),

    // ============================================
    // PROJECTILE IMPACTS
    // ============================================
    IMPACT_ARROW("combat/impact/impact_arrow"),
    IMPACT_BOLT("combat/impact/impact_bolt"),
    IMPACT_MAGIC("combat/impact/impact_magic"),
    IMPACT_FIREBALL("combat/impact/impact_fireball"),
    IMPACT_ICEBALL("combat/impact/impact_iceball"),
    IMPACT_KNIFE("combat/impact/impact_knife"),
    IMPACT_AXE("combat/impact/impact_axe"),
    IMPACT_ROCK("combat/impact/impact_rock"),
    IMPACT_BOMB("combat/impact/impact_bomb"),
    IMPACT_POTION("combat/impact/impact_potion"),
    IMPACT_FISH("combat/impact/impact_fish"),
    EXPLOSION("combat/impact/explosion"),

    // ============================================
    // PLAYER STATUS - DAMAGE
    // ============================================
    HURT("player/hurt"),
    HURT_LIGHT("player/hurt_light"),
    HURT_HEAVY("player/hurt_heavy"),
    DEAD("player/dead"),
    RESPAWN("player/respawn"),
    INVINCIBLE_START("player/invincible_start"),
    INVINCIBLE_END("player/invincible_end"),

    // ============================================
    // PLAYER STATUS - EFFECTS
    // ============================================
    BURNING("effects/burning"),
    BURNING_START("effects/burning_start"),
    BURNING_END("effects/burning_end"),
    FROZEN("effects/frozen"),
    FROZEN_START("effects/frozen_start"),
    FROZEN_END("effects/frozen_end"),
    POISONED("effects/poisoned"),
    POISONED_START("effects/poisoned_start"),
    POISONED_END("effects/poisoned_end"),
    HEALING("effects/healing"),
    BUFF_APPLIED("effects/buff_applied"),
    DEBUFF_APPLIED("effects/debuff_applied"),

    // ============================================
    // PLAYER ACTIONS - ITEMS
    // ============================================
    USE_ITEM("items/use_item"),
    EAT("items/eat"),
    EAT_BREAD("items/eat_bread"),
    EAT_APPLE("items/eat_apple"),
    EAT_MEAT("items/eat_meat"),
    DRINK("items/drink"),
    DRINK_POTION("items/drink_potion"),
    DRINK_WATER("items/drink_water"),
    CAST("items/cast"),
    CAST_SPELL("items/cast_spell"),
    CAST_SCROLL("items/cast_scroll"),
    READ_SCROLL("items/read_scroll"),

    // ============================================
    // TOOLS - MINING
    // ============================================
    USE_PICKAXE("tools/use_pickaxe"),
    USE_PICKAXE_WOOD("tools/use_pickaxe_wood"),
    USE_PICKAXE_STONE("tools/use_pickaxe_stone"),
    USE_PICKAXE_IRON("tools/use_pickaxe_iron"),
    USE_PICKAXE_GOLD("tools/use_pickaxe_gold"),
    USE_PICKAXE_DIAMOND("tools/use_pickaxe_diamond"),

    // ============================================
    // TOOLS - CHOPPING
    // ============================================
    USE_AXE("tools/use_axe"),
    USE_AXE_WOOD("tools/use_axe_wood"),
    USE_AXE_STONE("tools/use_axe_stone"),
    USE_AXE_IRON("tools/use_axe_iron"),

    // ============================================
    // TOOLS - DIGGING
    // ============================================
    USE_SHOVEL("tools/use_shovel"),
    USE_SHOVEL_WOOD("tools/use_shovel_wood"),
    USE_SHOVEL_STONE("tools/use_shovel_stone"),
    USE_SHOVEL_IRON("tools/use_shovel_iron"),

    // ============================================
    // TOOLS - OTHER
    // ============================================
    USE_HOE("tools/use_hoe"),
    USE_FISHING_ROD("tools/use_fishing_rod"),
    USE_TORCH("tools/use_torch"),
    USE_LANTERN("tools/use_lantern"),

    // ============================================
    // BLOCK BREAKING
    // ============================================
    BREAK_DIRT("blocks/break/break_dirt"),
    BREAK_GRASS("blocks/break/break_grass"),
    BREAK_STONE("blocks/break/break_stone"),
    BREAK_COBBLESTONE("blocks/break/break_cobblestone"),
    BREAK_WOOD("blocks/break/break_wood"),
    BREAK_LEAVES("blocks/break/break_leaves"),
    BREAK_SAND("blocks/break/break_sand"),
    BREAK_GRAVEL("blocks/break/break_gravel"),
    BREAK_GLASS("blocks/break/break_glass"),
    BREAK_BRICK("blocks/break/break_brick"),
    BREAK_METAL("blocks/break/break_metal"),
    BREAK_ICE("blocks/break/break_ice"),
    BREAK_SNOW("blocks/break/break_snow"),
    BREAK_COAL_ORE("blocks/break/break_coal_ore"),
    BREAK_IRON_ORE("blocks/break/break_iron_ore"),
    BREAK_GOLD_ORE("blocks/break/break_gold_ore"),
    BREAK_DIAMOND_ORE("blocks/break/break_diamond_ore"),

    // ============================================
    // BLOCK PLACING
    // ============================================
    PLACE_DIRT("blocks/place/place_dirt"),
    PLACE_GRASS("blocks/place/place_grass"),
    PLACE_STONE("blocks/place/place_stone"),
    PLACE_COBBLESTONE("blocks/place/place_cobblestone"),
    PLACE_WOOD("blocks/place/place_wood"),
    PLACE_LEAVES("blocks/place/place_leaves"),
    PLACE_SAND("blocks/place/place_sand"),
    PLACE_GRAVEL("blocks/place/place_gravel"),
    PLACE_GLASS("blocks/place/place_glass"),
    PLACE_BRICK("blocks/place/place_brick"),
    PLACE_METAL("blocks/place/place_metal"),

    // ============================================
    // BLOCK OVERLAYS
    // ============================================
    BREAK_OVERLAY_GRASS("blocks/overlay/break_overlay_grass"),
    BREAK_OVERLAY_SNOW("blocks/overlay/break_overlay_snow"),
    BREAK_OVERLAY_ICE("blocks/overlay/break_overlay_ice"),
    BREAK_OVERLAY_MOSS("blocks/overlay/break_overlay_moss"),
    BREAK_OVERLAY_VINES("blocks/overlay/break_overlay_vines"),

    // ============================================
    // FOOTSTEPS
    // ============================================
    STEP_GRASS("footsteps/step_grass"),
    STEP_DIRT("footsteps/step_dirt"),
    STEP_STONE("footsteps/step_stone"),
    STEP_WOOD("footsteps/step_wood"),
    STEP_SAND("footsteps/step_sand"),
    STEP_GRAVEL("footsteps/step_gravel"),
    STEP_WATER("footsteps/step_water"),
    STEP_SNOW("footsteps/step_snow"),
    STEP_METAL("footsteps/step_metal"),
    STEP_LEAVES("footsteps/step_leaves"),

    // ============================================
    // INVENTORY MANAGEMENT
    // ============================================
    COLLECT("inventory/collect"),
    COLLECT_ITEM("inventory/collect_item"),
    COLLECT_COIN("inventory/collect_coin"),
    COLLECT_GEM("inventory/collect_gem"),
    COLLECT_KEY("inventory/collect_key"),
    DROP("inventory/drop"),
    DROP_ITEM("inventory/drop_item"),
    DROP_STACK("inventory/drop_stack"),
    EQUIP("inventory/equip"),
    EQUIP_WEAPON("inventory/equip_weapon"),
    EQUIP_ARMOR("inventory/equip_armor"),
    EQUIP_ACCESSORY("inventory/equip_accessory"),
    UNEQUIP("inventory/unequip"),
    HOTBAR_SWITCH("inventory/hotbar_switch"),
    HOTBAR_SCROLL("inventory/hotbar_scroll"),
    INVENTORY_OPEN("inventory/inventory_open"),
    INVENTORY_CLOSE("inventory/inventory_close"),
    INVENTORY_SORT("inventory/inventory_sort"),
    ITEM_STACK("inventory/item_stack"),
    ITEM_SPLIT("inventory/item_split"),

    // ============================================
    // CHESTS AND VAULTS
    // ============================================
    OPEN_CHEST("chests/open_chest"),
    OPEN_WOODEN_CHEST("chests/open_wooden_chest"),
    OPEN_IRON_CHEST("chests/open_iron_chest"),
    OPEN_GOLD_CHEST("chests/open_gold_chest"),
    OPEN_DAILY_CHEST("chests/open_daily_chest"),
    OPEN_MONTHLY_CHEST("chests/open_monthly_chest"),
    CLOSE_CHEST("chests/close_chest"),
    CHEST_LOCKED("chests/chest_locked"),
    CHEST_UNLOCK("chests/chest_unlock"),
    OPEN_VAULT("chests/open_vault"),
    CLOSE_VAULT("chests/close_vault"),
    LOOT_DROP("chests/loot_drop"),
    LOOT_LEGENDARY("chests/loot_legendary"),
    LOOT_MYTHIC("chests/loot_mythic"),

    // ============================================
    // DOORS
    // ============================================
    DOOR_OPEN("doors/door_open"),
    DOOR_OPEN_WOOD("doors/door_open_wood"),
    DOOR_OPEN_METAL("doors/door_open_metal"),
    DOOR_OPEN_STONE("doors/door_open_stone"),
    DOOR_CLOSE("doors/door_close"),
    DOOR_CLOSE_WOOD("doors/door_close_wood"),
    DOOR_CLOSE_METAL("doors/door_close_metal"),
    DOOR_CLOSE_STONE("doors/door_close_stone"),
    DOOR_LOCKED("doors/door_locked"),
    DOOR_UNLOCK("doors/door_unlock"),
    DOOR_BREAK("doors/door_break"),
    GATE_OPEN("doors/gate_open"),
    GATE_CLOSE("doors/gate_close"),
    PORTCULLIS_OPEN("doors/portcullis_open"),
    PORTCULLIS_CLOSE("doors/portcullis_close"),

    // ============================================
    // MOB GENERAL
    // ============================================
    MOB_IDLE("mobs/general/mob_idle"),
    MOB_WALK("mobs/general/mob_walk"),
    MOB_RUN("mobs/general/mob_run"),
    MOB_ATTACK("mobs/general/mob_attack"),
    MOB_HURT("mobs/general/mob_hurt"),
    MOB_DEATH("mobs/general/mob_death"),
    MOB_SPAWN("mobs/general/mob_spawn"),
    MOB_AGGRO("mobs/general/mob_aggro"),
    MOB_FLEE("mobs/general/mob_flee"),

    // ============================================
    // MOB SPECIFIC - ZOMBIE
    // ============================================
    ZOMBIE_IDLE("mobs/zombie/zombie_idle"),
    ZOMBIE_WALK("mobs/zombie/zombie_walk"),
    ZOMBIE_ATTACK("mobs/zombie/zombie_attack"),
    ZOMBIE_HURT("mobs/zombie/zombie_hurt"),
    ZOMBIE_DEATH("mobs/zombie/zombie_death"),
    ZOMBIE_GROAN("mobs/zombie/zombie_groan"),

    // ============================================
    // MOB SPECIFIC - SKELETON
    // ============================================
    SKELETON_IDLE("mobs/skeleton/skeleton_idle"),
    SKELETON_WALK("mobs/skeleton/skeleton_walk"),
    SKELETON_ATTACK("mobs/skeleton/skeleton_attack"),
    SKELETON_HURT("mobs/skeleton/skeleton_hurt"),
    SKELETON_DEATH("mobs/skeleton/skeleton_death"),
    SKELETON_RATTLE("mobs/skeleton/skeleton_rattle"),

    // ============================================
    // MOB SPECIFIC - GOBLIN
    // ============================================
    GOBLIN_IDLE("mobs/goblin/goblin_idle"),
    GOBLIN_WALK("mobs/goblin/goblin_walk"),
    GOBLIN_ATTACK("mobs/goblin/goblin_attack"),
    GOBLIN_HURT("mobs/goblin/goblin_hurt"),
    GOBLIN_DEATH("mobs/goblin/goblin_death"),
    GOBLIN_LAUGH("mobs/goblin/goblin_laugh"),

    // ============================================
    // MOB SPECIFIC - ORC
    // ============================================
    ORC_IDLE("mobs/orc/orc_idle"),
    ORC_WALK("mobs/orc/orc_walk"),
    ORC_ATTACK("mobs/orc/orc_attack"),
    ORC_HURT("mobs/orc/orc_hurt"),
    ORC_DEATH("mobs/orc/orc_death"),
    ORC_ROAR("mobs/orc/orc_roar"),

    // ============================================
    // MOB SPECIFIC - BANDIT
    // ============================================
    BANDIT_IDLE("mobs/bandit/bandit_idle"),
    BANDIT_WALK("mobs/bandit/bandit_walk"),
    BANDIT_ATTACK("mobs/bandit/bandit_attack"),
    BANDIT_HURT("mobs/bandit/bandit_hurt"),
    BANDIT_DEATH("mobs/bandit/bandit_death"),
    BANDIT_TAUNT("mobs/bandit/bandit_taunt"),

    // ============================================
    // MOB SPECIFIC - KNIGHT
    // ============================================
    KNIGHT_IDLE("mobs/knight/knight_idle"),
    KNIGHT_WALK("mobs/knight/knight_walk"),
    KNIGHT_ATTACK("mobs/knight/knight_attack"),
    KNIGHT_HURT("mobs/knight/knight_hurt"),
    KNIGHT_DEATH("mobs/knight/knight_death"),
    KNIGHT_ARMOR_CLANK("mobs/knight/knight_armor_clank"),

    // ============================================
    // MOB SPECIFIC - MAGE
    // ============================================
    MAGE_IDLE("mobs/mage/mage_idle"),
    MAGE_WALK("mobs/mage/mage_walk"),
    MAGE_ATTACK("mobs/mage/mage_attack"),
    MAGE_HURT("mobs/mage/mage_hurt"),
    MAGE_DEATH("mobs/mage/mage_death"),
    MAGE_CAST("mobs/mage/mage_cast"),

    // ============================================
    // MOB SPECIFIC - WOLF
    // ============================================
    WOLF_IDLE("mobs/wolf/wolf_idle"),
    WOLF_WALK("mobs/wolf/wolf_walk"),
    WOLF_RUN("mobs/wolf/wolf_run"),
    WOLF_ATTACK("mobs/wolf/wolf_attack"),
    WOLF_HURT("mobs/wolf/wolf_hurt"),
    WOLF_DEATH("mobs/wolf/wolf_death"),
    WOLF_BARK("mobs/wolf/wolf_bark"),
    WOLF_HOWL("mobs/wolf/wolf_howl"),
    WOLF_GROWL("mobs/wolf/wolf_growl"),

    // ============================================
    // MOB SPECIFIC - BEAR
    // ============================================
    BEAR_IDLE("mobs/bear/bear_idle"),
    BEAR_WALK("mobs/bear/bear_walk"),
    BEAR_ATTACK("mobs/bear/bear_attack"),
    BEAR_HURT("mobs/bear/bear_hurt"),
    BEAR_DEATH("mobs/bear/bear_death"),
    BEAR_ROAR("mobs/bear/bear_roar"),

    // ============================================
    // MOB SPECIFIC - FROG
    // ============================================
    FROG_IDLE("mobs/frog/frog_idle"),
    FROG_HOP("mobs/frog/frog_hop"),
    FROG_ATTACK("mobs/frog/frog_attack"),
    FROG_HURT("mobs/frog/frog_hurt"),
    FROG_DEATH("mobs/frog/frog_death"),
    FROG_CROAK("mobs/frog/frog_croak"),

    // ============================================
    // MOB SPECIFIC - SPIDER
    // ============================================
    SPIDER_IDLE("mobs/spider/spider_idle"),
    SPIDER_WALK("mobs/spider/spider_walk"),
    SPIDER_ATTACK("mobs/spider/spider_attack"),
    SPIDER_HURT("mobs/spider/spider_hurt"),
    SPIDER_DEATH("mobs/spider/spider_death"),
    SPIDER_HISS("mobs/spider/spider_hiss"),

    // ============================================
    // MOB SPECIFIC - SLIME
    // ============================================
    SLIME_IDLE("mobs/slime/slime_idle"),
    SLIME_MOVE("mobs/slime/slime_move"),
    SLIME_ATTACK("mobs/slime/slime_attack"),
    SLIME_HURT("mobs/slime/slime_hurt"),
    SLIME_DEATH("mobs/slime/slime_death"),
    SLIME_BOUNCE("mobs/slime/slime_bounce"),
    SLIME_SPLIT("mobs/slime/slime_split"),

    // ============================================
    // BOSS MOBS
    // ============================================
    BOSS_INTRO("mobs/boss/boss_intro"),
    BOSS_PHASE_CHANGE("mobs/boss/boss_phase_change"),
    BOSS_ATTACK("mobs/boss/boss_attack"),
    BOSS_SPECIAL_ATTACK("mobs/boss/boss_special_attack"),
    BOSS_HURT("mobs/boss/boss_hurt"),
    BOSS_DEATH("mobs/boss/boss_death"),
    BOSS_ROAR("mobs/boss/boss_roar"),

    // ============================================
    // UI - BUTTONS
    // ============================================
    UI_BUTTON_CLICK("ui/ui_button_click"),
    UI_BUTTON_HOVER("ui/ui_button_hover"),
    UI_BUTTON_DISABLED("ui/ui_button_disabled"),
    UI_CONFIRM("ui/ui_confirm"),
    UI_CANCEL("ui/ui_cancel"),
    UI_BACK("ui/ui_back"),
    UI_SELECT("ui/ui_select"),

    // ============================================
    // UI - SLIDERS
    // ============================================
    UI_SLIDER_CHANGE("ui/ui_slider_change"),
    UI_SLIDER_MIN("ui/ui_slider_min"),
    UI_SLIDER_MAX("ui/ui_slider_max"),
    UI_TOGGLE_ON("ui/ui_toggle_on"),
    UI_TOGGLE_OFF("ui/ui_toggle_off"),

    // ============================================
    // UI - MENUS
    // ============================================
    MENU_OPEN("ui/menu_open"),
    MENU_CLOSE("ui/menu_close"),
    MENU_TRANSITION("ui/menu_transition"),
    SETTINGS_OPEN("ui/settings_open"),
    SETTINGS_CLOSE("ui/settings_close"),
    PAUSE("ui/pause"),
    UNPAUSE("ui/unpause"),

    // ============================================
    // UI - NOTIFICATIONS
    // ============================================
    NOTIFICATION("ui/notification"),
    ACHIEVEMENT("ui/achievement"),
    LEVEL_UP("ui/level_up"),
    QUEST_ACCEPT("ui/quest_accept"),
    QUEST_COMPLETE("ui/quest_complete"),
    WARNING("ui/warning"),
    ERROR("ui/error"),

    // ============================================
    // MUSIC - BACKGROUND
    // ============================================
    MUSIC_MENU("music/music_menu"),
    MUSIC_LEVEL_1("music/music_level_1"),
    MUSIC_LEVEL_2("music/music_level_2"),
    MUSIC_LEVEL_3("music/music_level_3"),
    MUSIC_BOSS("music/music_boss"),
    MUSIC_VICTORY("music/music_victory"),
    MUSIC_DEFEAT("music/music_defeat"),
    MUSIC_PEACEFUL("music/music_peaceful"),
    MUSIC_COMBAT("music/music_combat"),
    MUSIC_DUNGEON("music/music_dungeon"),
    MUSIC_CAVE("music/music_cave"),
    MUSIC_FOREST("music/music_forest"),
    MUSIC_DESERT("music/music_desert"),
    MUSIC_SNOW("music/music_snow"),
    MUSIC_LOOT("music/music_loot"),
    MUSIC_CREDITS("music/music_credits"),

    // ============================================
    // AMBIENT - ENVIRONMENT
    // ============================================
    AMBIENT_WIND("ambient/ambient_wind"),
    AMBIENT_WIND_STRONG("ambient/ambient_wind_strong"),
    AMBIENT_RAIN("ambient/ambient_rain"),
    AMBIENT_RAIN_HEAVY("ambient/ambient_rain_heavy"),
    AMBIENT_THUNDER("ambient/ambient_thunder"),
    AMBIENT_THUNDER_CLOSE("ambient/ambient_thunder_close"),
    AMBIENT_WATER_FLOW("ambient/ambient_water_flow"),
    AMBIENT_WATERFALL("ambient/ambient_waterfall"),
    AMBIENT_CAVE("ambient/ambient_cave"),
    AMBIENT_CAVE_DRIP("ambient/ambient_cave_drip"),
    AMBIENT_FOREST("ambient/ambient_forest"),
    AMBIENT_BIRDS("ambient/ambient_birds"),
    AMBIENT_CRICKETS("ambient/ambient_crickets"),
    AMBIENT_FIRE_CRACKLE("ambient/ambient_fire_crackle"),
    AMBIENT_LAVA("ambient/ambient_lava"),
    AMBIENT_DESERT_WIND("ambient/ambient_desert_wind"),
    AMBIENT_SNOW_WIND("ambient/ambient_snow_wind"),

    // ============================================
    // WATER INTERACTIONS
    // ============================================
    WATER_SPLASH("water/water_splash"),
    WATER_SPLASH_SMALL("water/water_splash_small"),
    WATER_SPLASH_BIG("water/water_splash_big"),
    WATER_ENTER("water/water_enter"),
    WATER_EXIT("water/water_exit"),
    WATER_SWIM("water/water_swim"),
    WATER_SUBMERGE("water/water_submerge"),
    WATER_BUBBLE("water/water_bubble"),

    // ============================================
    // SPECIAL EVENTS
    // ============================================
    LEVEL_START("events/level_start"),
    LEVEL_COMPLETE("events/level_complete"),
    CHECKPOINT("events/checkpoint"),
    SECRET_FOUND("events/secret_found"),
    TREASURE_REVEAL("events/treasure_reveal"),
    FANFARE_SHORT("events/fanfare_short"),
    FANFARE_LONG("events/fanfare_long"),
    DEATH_JINGLE("events/death_jingle"),
    GAME_OVER("events/game_over"),
    TELEPORT("events/teleport"),
    PORTAL_OPEN("events/portal_open"),
    PORTAL_CLOSE("events/portal_close"),
    PORTAL_ENTER("events/portal_enter"),

    // ============================================
    // NPC INTERACTIONS
    // ============================================
    NPC_GREETING("npc/npc_greeting"),
    NPC_GOODBYE("npc/npc_goodbye"),
    NPC_TALK("npc/npc_talk"),
    NPC_TALK_BLIP("npc/npc_talk_blip"),
    NPC_LAUGH("npc/npc_laugh"),
    NPC_ANGRY("npc/npc_angry"),
    NPC_SAD("npc/npc_sad"),
    NPC_SURPRISED("npc/npc_surprised"),
    SHOP_OPEN("npc/shop_open"),
    SHOP_CLOSE("npc/shop_close"),
    SHOP_BUY("npc/shop_buy"),
    SHOP_SELL("npc/shop_sell"),
    SHOP_CANT_AFFORD("npc/shop_cant_afford"),

    // ============================================
    // CRAFTING
    // ============================================
    CRAFT_START("crafting/craft_start"),
    CRAFT_SUCCESS("crafting/craft_success"),
    CRAFT_FAIL("crafting/craft_fail"),
    FORGE_HAMMER("crafting/forge_hammer"),
    FORGE_HEAT("crafting/forge_heat"),
    ALCHEMY_BUBBLE("crafting/alchemy_bubble"),
    ALCHEMY_SUCCESS("crafting/alchemy_success"),
    ENCHANT_START("crafting/enchant_start"),
    ENCHANT_SUCCESS("crafting/enchant_success"),

    // ============================================
    // MIRROR TO OTHER REALMS (SPECIAL ITEM)
    // ============================================
    MIRROR_ACTIVATE("special/mirror_activate"),
    MIRROR_REALM_CHANGE("special/mirror_realm_change"),
    MIRROR_VOLCANO("special/mirror_volcano"),
    MIRROR_FOREST("special/mirror_forest"),
    MIRROR_OCEAN("special/mirror_ocean");

    private final String path;

    SoundAction(String path) {
        this.path = path;
    }

    /**
     * Get the relative path for this sound action.
     * Does not include the base directory or file extension.
     * @return Relative path like "player/jump" or "combat/melee/use_sword"
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the full file path for the MP3 version of this sound.
     * @param basePath Base sounds directory (e.g., "sounds/compressed")
     * @return Full path like "sounds/compressed/player/jump.mp3"
     */
    public String getMP3Path(String basePath) {
        return basePath + "/" + path + ".mp3";
    }

    /**
     * Get the full file path for the WAV version of this sound.
     * @param basePath Base sounds directory (e.g., "sounds")
     * @return Full path like "sounds/player/jump.wav"
     */
    public String getWAVPath(String basePath) {
        return basePath + "/" + path + ".wav";
    }

    /**
     * Get the category of this sound action (first part of path).
     * @return Category like "player", "combat", "blocks", etc.
     */
    public String getCategory() {
        int slashIndex = path.indexOf('/');
        return slashIndex > 0 ? path.substring(0, slashIndex) : path;
    }

    /**
     * Get the action name (last part of path).
     * @return Action name like "jump", "use_sword", "break_dirt", etc.
     */
    public String getActionName() {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
}
