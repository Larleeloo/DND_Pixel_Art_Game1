# Android Port Analysis - The Amber Moon

## Executive Summary

The Android port currently has **20 Java files** covering infrastructure (activities, input, audio, rendering, saves, scene management). The desktop codebase has **200+ Java files** across entity, item, block, animation, graphics, UI, and scene packages. Most Android scenes are **placeholder stubs** that draw static text. The Loot Game scene requires porting approximately **50 core classes** plus **198 individual item classes** to become functional.

---

## 1. Current Android Port Status

### Fully Functional (Infrastructure Layer)

| Android File | Desktop Equivalent | Status |
|---|---|---|
| `core/MainActivity.java` | `core/Main.java` | Done - App entry point |
| `core/GameActivity.java` | `core/GameWindow.java` | Done - Hosts game surface |
| `core/GameSurfaceView.java` | `core/GamePanel.java` | Done - 60 FPS render loop, scaling |
| `core/GamePreferences.java` | *(new for Android)* | Done - SharedPreferences wrapper |
| `input/TouchInputManager.java` | `input/InputManager.java` | Done - Touch-to-keyboard mapping |
| `input/AndroidControllerManager.java` | `input/ControllerManager.java` | Done - Gamepad support |
| `audio/AndroidAudioManager.java` | `audio/AudioManager.java` | Done - MediaPlayer + SoundPool |
| `graphics/AndroidAssetLoader.java` | `graphics/AssetLoader.java` | Done - Bitmap loading (GIF partial) |
| `save/CloudSaveManager.java` | `save/SaveManager.java` | Done - Local + GitHub sync |
| `scene/AndroidScene.java` | `scene/Scene.java` | Done - Scene interface |
| `scene/BaseScene.java` | *(new for Android)* | Done - Base scene implementation |
| `scene/AndroidSceneManager.java` | `scene/SceneManager.java` | Done - Scene transitions |
| `scene/MainMenuScene.java` | `scene/MainMenuScene.java` | Done - Functional menu with buttons |
| `scene/LevelSelectionScene.java` | `scene/LevelSelectionScene.java` | Done - Level list with selection |
| `ui/TouchControlOverlay.java` | *(new for Android)* | Done - Virtual D-pad + buttons |

### Placeholder Stubs (Draw Text Only, No Functionality)

| Android File | Desktop Equivalent | What's Missing |
|---|---|---|
| `scene/LootGameScene.java` | `scene/LootGameScene.java` | Everything - just draws 3 lines of text |
| `scene/GameScene.java` | `scene/GameScene.java` | Placeholder rectangle player, no entities/blocks/levels |
| `scene/CreativeScene.java` | `scene/CreativeScene.java` | Everything - just draws text |
| `scene/OverworldScene.java` | `scene/OverworldScene.java` | Everything - just draws text |
| `scene/SpriteCharacterCustomization.java` | `scene/SpriteCharacterCustomization.java` | Everything - just draws text |

---

## 2. Classes Required for Loot Game (Priority Ordered)

The desktop `LootGameScene` depends on a deep class hierarchy. Below is every class that must be converted from `java.awt.Graphics` to `android.graphics.Canvas`, organized into tiers by dependency order.

### Tier 1: Core Entity Framework (Port First)

These are the foundation classes everything else builds on.

| Desktop Class | Package | Purpose | Conversion Notes |
|---|---|---|---|
| `Entity.java` | `entity/` | Abstract base for all game objects | Replace `java.awt.Rectangle` with `android.graphics.Rect` |
| `EntityManager.java` | `entity/` | Manages entity list, update/draw loop | Replace `Graphics2D` draw calls with `Canvas` |
| `EntityPhysics.java` | `entity/` | Gravity, collision detection | Mostly math - minimal platform changes |
| `SpriteEntity.java` | `entity/` | Sprite-based entity with animation | Replace `BufferedImage` with `Bitmap`, `Graphics2D` with `Canvas` |
| `GroundEntity.java` | `entity/` | Ground/platform collision | Replace `Graphics2D` rendering |

### Tier 2: Capabilities & Interfaces (Port Second)

| Desktop Class | Package | Purpose | Conversion Notes |
|---|---|---|---|
| `CombatCapable.java` | `entity/capabilities/` | Combat interface | No AWT dependencies - direct port |
| `ResourceManager.java` | `entity/capabilities/` | Health/mana/stamina interface | No AWT dependencies - direct port |
| `BlockInteractionHandler.java` | `entity/capabilities/` | Block interaction interface | No AWT dependencies - direct port |
| `BlockInteractionHelper.java` | `entity/capabilities/` | Block interaction logic | Minor Rectangle → Rect conversion |
| `PlayerBase.java` | `entity/player/` | Player interface | No AWT dependencies - direct port |

### Tier 3: Block System

| Desktop Class | Package | Purpose | Conversion Notes |
|---|---|---|---|
| `BlockType.java` | `block/` | Enum of block types | No AWT dependencies - direct port |
| `BlockAttributes.java` | `block/` | Block properties (hardness, solid) | No AWT dependencies - direct port |
| `BlockOverlay.java` | `block/` | Block overlay enum (grass, snow) | No AWT dependencies - direct port |
| `BlockRegistry.java` | `block/` | Block type registry + textures | Replace `BufferedImage` with `Bitmap` |
| `BlockEntity.java` | `block/` | Block entity in world | Replace `Graphics2D` rendering |
| `MovingBlockEntity.java` | `block/` | Moving platform blocks | Replace `Graphics2D` rendering |

### Tier 4: Item System (Core)

| Desktop Class | Package | Purpose | Conversion Notes |
|---|---|---|---|
| `Item.java` | `entity/item/` | Base item class with properties | Replace `java.awt.Color` with `android.graphics.Color`, `BufferedImage` with `Bitmap` |
| `ItemRegistry.java` | `entity/item/` | Registry of all 198 items | Replace image loading to use `AndroidAssetLoader` |
| `ItemEntity.java` | `entity/item/` | Dropped item in world | Replace `Graphics2D` rendering, physics unchanged |
| `RecipeManager.java` | `entity/item/` | Alchemy recipe loading/matching | JSON parsing - minor platform changes |

### Tier 5: Interactive Entities (Loot Game Specific)

| Desktop Class | Package | Purpose | Conversion Notes |
|---|---|---|---|
| `LootChestEntity.java` | `entity/item/` | Daily/monthly loot chests | Replace `Graphics2D` rendering, particle effects |
| `VaultEntity.java` | `entity/item/` | Persistent storage chest | Replace `Graphics2D` rendering |
| `DoorEntity.java` | `entity/item/` | Interactive doors | Replace `Graphics2D` rendering |
| `AlchemyTableEntity.java` | `entity/item/` | Crafting table entity | Replace `Graphics2D` rendering |
| `MirrorToOtherRealms.java` | `entity/item/` | Special multi-realm weapon | Replace `Graphics2D` rendering |
| `ButtonEntity.java` | `entity/item/` | Interactive switches | Replace `Graphics2D` rendering |
| `TriggerEntity.java` | `entity/` | Invisible trigger zones | Minimal rendering changes |
| `BackgroundEntity.java` | `entity/` | Background decoration | Replace `Graphics2D` rendering |
| `ProjectileEntity.java` | `entity/` | Projectile system | Replace `Graphics2D` rendering |
| `MeleeAttackHitbox.java` | `entity/` | Melee attack collision | Replace `Rectangle` with `Rect` |

### Tier 6: Player System

| Desktop Class | Package | Purpose | Conversion Notes |
|---|---|---|---|
| `AbilityScores.java` | `entity/player/` | DnD ability score calculations | No AWT dependencies - direct port |
| `PlayableCharacter.java` | `entity/player/` | Character data class | No AWT dependencies - direct port |
| `PlayableCharacterRegistry.java` | `entity/player/` | Character registry (8 characters) | No AWT dependencies - direct port |
| `SpritePlayerEntity.java` | `entity/player/` | Full player implementation | Large class - replace all `Graphics2D` rendering, `BufferedImage` with `Bitmap` |

### Tier 7: Animation System

| Desktop Class | Package | Purpose | Conversion Notes |
|---|---|---|---|
| `AnimatedTexture.java` | `graphics/` | GIF frame extraction | Replace `javax.imageio` with Android GIF decoder (need library or custom decoder) |
| `SpriteAnimation.java` | `animation/` | Animation state machine | Replace `BufferedImage` with `Bitmap` |
| `EquipmentOverlay.java` | `animation/` | Equipment rendering on player | Replace `Graphics2D` overlay compositing |
| `ItemAnimationState.java` | `animation/` | Item animation states | No AWT dependencies - direct port |
| `ParticleAnimationState.java` | `animation/` | Particle effects | No AWT dependencies - direct port |
| `TriggeredAnimationManager.java` | `animation/` | Triggered animation playback | Replace `BufferedImage` with `Bitmap` |

### Tier 8: Graphics & Camera

| Desktop Class | Package | Purpose | Conversion Notes |
|---|---|---|---|
| `Camera.java` | `graphics/` | Camera following, transforms | Replace `AffineTransform` with `Canvas.translate()` |
| `TextureManager.java` | `graphics/` | Texture loading and caching | Replace with `AndroidAssetLoader` or wrap it |
| `LightSource.java` | `graphics/` | Point light data | No AWT dependencies - direct port |
| `LightingSystem.java` | `graphics/` | Night/day, dynamic lights | Replace `Graphics2D` alpha compositing with `Canvas` + `PorterDuff` |
| `ParallaxBackground.java` | `graphics/` | Parallax layer manager | Replace `Graphics2D` rendering |
| `ParallaxLayer.java` | `graphics/` | Individual parallax layer | Replace `BufferedImage` with `Bitmap` |

### Tier 9: UI Components

| Desktop Class | Package | Purpose | Conversion Notes |
|---|---|---|---|
| `UIButton.java` | `ui/` | Clickable button | Replace `Graphics2D` rendering with `Canvas` + `Paint` |
| `UISlider.java` | `ui/` | Settings slider | Replace `Graphics2D` rendering |
| `Inventory.java` | `ui/` | 32-slot inventory UI | Large class - replace all `Graphics2D` rendering, drag-and-drop |
| `VaultInventory.java` | `ui/` | 10,000-slot vault UI | Replace `Graphics2D` rendering |
| `PlayerStatusBar.java` | `ui/` | Health/mana/stamina bars | Replace `Graphics2D` rendering |
| `AlchemyTableUI.java` | `ui/` | Crafting interface | Replace `Graphics2D` rendering |
| `ReverseCraftingUI.java` | `ui/` | Deconstruction interface | Replace `Graphics2D` rendering |
| `SettingsOverlay.java` | `ui/` | Full settings panel | Replace `Graphics2D` rendering (partial stub exists in AndroidSceneManager) |
| `ToolType.java` | `ui/` | Tool type enum | No AWT dependencies - direct port |

### Tier 10: All 198 Individual Item Classes

Every item class in `entity/item/items/` must be ported. These are mostly data classes with minimal rendering logic, so bulk conversion is feasible.

| Category | Directory | Count | Conversion Notes |
|---|---|---|---|
| Melee Weapons | `items/weapons/melee/` | 22 | Replace `Color` references |
| Ranged Weapons | `items/weapons/ranged/` | 16 | Replace `Color` references |
| Throwing Weapons | `items/weapons/throwing/` | 3 | Replace `Color` references |
| Ammo | `items/ammo/` | 6 | Replace `Color` references |
| Throwables | `items/throwables/` | 2 | Replace `Color` references |
| Tools | `items/tools/` | 10 | Replace `Color` references |
| Armor | `items/armor/` | 20 | Replace `Color` references |
| Food | `items/food/` | 11 | Replace `Color` references |
| Potions | `items/potions/` | 14 | Replace `Color` references |
| Materials | `items/materials/` | 24 | Replace `Color` references |
| Keys | `items/keys/` | 4 | Replace `Color` references |
| Clothing | `items/clothing/` | 12 | Replace `Color` references |
| Collectibles | `items/collectibles/` | 36 | Replace `Color` references |
| Accessories | `items/accessories/` | 1 | Replace `Color` references |
| Blocks | `items/blocks/` | 17 | Replace `Color` references |
| **TOTAL** | | **198** | |

### Tier 11: Supporting Classes (Not Required for Loot Game MVP, but needed for full port)

| Desktop Class | Purpose | Loot Game Need |
|---|---|---|
| `audio/SoundAction.java` | Sound effect enum | Nice-to-have |
| `input/KeyBindings.java` | Rebindable controls | Not needed (touch overlay handles this) |
| `input/ControllerBindings.java` | Controller binds | Not needed |
| `input/VibrationPattern.java` | Vibration patterns | Nice-to-have for haptic feedback |
| `input/XInputVibration.java` | Desktop-only | Not needed |
| `entity/mob/SpriteMobEntity.java` | Mob rendering | Not in Loot Game |
| `entity/mob/MobRegistry.java` | Mob creation | Not in Loot Game |
| `entity/mob/MobEntity.java` | Base mob class | Not in Loot Game |
| All 23 individual mob classes | Individual mobs | Not in Loot Game |
| `entity/player/PlayerEntity.java` | Legacy player | Not needed |
| `entity/player/PlayerBoneEntity.java` | Legacy bone player | Not needed |
| `entity/CompanionRegistry.java` | Companions | Not in Loot Game MVP |
| `entity/mob/FrogSprite.java` | Ambient mob | Not in Loot Game |
| `entity/mob/RabbitSprite.java` | Ambient mob | Not in Loot Game |
| `scene/CreativeScene.java` | Level editor | Separate effort |
| `scene/creative/CreativePaletteManager.java` | Editor palette | Separate effort |
| `level/LevelData.java` | Level JSON structure | Needed for GameScene, not Loot Game |
| `level/LevelLoader.java` | Level loading | Needed for GameScene, not Loot Game |
| `ui/CutsceneOverlay.java` | Cutscenes | Not in Loot Game |
| All 5 bone animation classes | Legacy system | Not needed |
| All 7 tool scripts | Dev tools | Not needed |

---

## 3. Conversion Summary

| Category | Classes to Port | Effort |
|---|---|---|
| Core Entity Framework | 5 | High - foundational |
| Capabilities/Interfaces | 5 | Low - no AWT deps |
| Block System | 6 | Medium |
| Item System (Core) | 4 | High - complex registry |
| Interactive Entities | 10 | Medium |
| Player System | 4 | High - SpritePlayerEntity is large |
| Animation System | 6 | High - GIF decoder needed |
| Graphics & Camera | 6 | High - rendering pipeline |
| UI Components | 9 | High - complex inventory |
| Individual Item Classes | 198 | Low per-class, high total |
| **TOTAL** | **~253** | |

### Common AWT → Android Replacements

| AWT/Swing | Android Equivalent |
|---|---|
| `java.awt.Graphics2D` | `android.graphics.Canvas` |
| `java.awt.Color` | `android.graphics.Color` (int-based) |
| `java.awt.Rectangle` | `android.graphics.Rect` / `RectF` |
| `java.awt.Font` | `android.graphics.Paint` + `setTextSize()` |
| `java.awt.FontMetrics` | `Paint.measureText()` + `Paint.getTextBounds()` |
| `java.awt.image.BufferedImage` | `android.graphics.Bitmap` |
| `java.awt.geom.AffineTransform` | `Canvas.save()/translate()/restore()` |
| `java.awt.GradientPaint` | `android.graphics.LinearGradient` / `Shader` |
| `java.awt.BasicStroke` | `Paint.setStrokeWidth()` + `Paint.setStyle()` |
| `java.awt.RenderingHints` | `Paint.setAntiAlias(true)` |
| `javax.imageio.ImageIO` | `BitmapFactory.decodeStream()` |
| `Graphics2D.fillRect()` | `Canvas.drawRect()` |
| `Graphics2D.drawString()` | `Canvas.drawText()` |
| `Graphics2D.drawImage()` | `Canvas.drawBitmap()` |
| `Graphics2D.fillOval()` | `Canvas.drawOval()` / `Canvas.drawCircle()` |
| `Graphics2D.setComposite(AlphaComposite)` | `Paint.setAlpha()` |
| `InputManager` | `TouchInputManager` (already done) |
| `SceneManager` | `AndroidSceneManager` (already done) |

---

## 4. Recommended Porting Order (for Loot Game MVP)

### Phase 1: Foundation
1. Port `Entity`, `EntityManager`, `EntityPhysics`, `SpriteEntity`
2. Port capability interfaces (`CombatCapable`, `ResourceManager`, `PlayerBase`)
3. Port `Camera` with `Canvas.translate()` instead of `AffineTransform`

### Phase 2: Blocks & Items Data
4. Port `BlockType`, `BlockAttributes`, `BlockOverlay` (enums, no rendering)
5. Port `BlockRegistry`, `BlockEntity` (with Canvas rendering)
6. Port `Item`, `ItemRegistry`, `RecipeManager` (data + registry)
7. Bulk-convert all 198 item classes (mostly `Color` references)

### Phase 3: Player
8. Port `AbilityScores`, `PlayableCharacter`, `PlayableCharacterRegistry`
9. Port `SpriteAnimation`, `AnimatedTexture`, `EquipmentOverlay`
10. Port `SpritePlayerEntity` (largest single class)

### Phase 4: Interactive Entities
11. Port `ItemEntity`, `LootChestEntity`, `VaultEntity`
12. Port `DoorEntity`, `AlchemyTableEntity`
13. Port `ProjectileEntity`, `MeleeAttackHitbox`

### Phase 5: UI
14. Port `UIButton` (already partially reimplemented in MainMenuScene)
15. Port `Inventory`, `VaultInventory`
16. Port `AlchemyTableUI`, `ReverseCraftingUI`
17. Port `PlayerStatusBar`

### Phase 6: Loot Game Scene
18. Rewrite `LootGameScene` using all ported classes
19. Integration testing

---

## 5. Secure User Login & GitHub Save Synchronization Outline

### Problem Statement

The current `CloudSaveManager` stores a raw GitHub Personal Access Token (PAT) in `SharedPreferences` (`GamePreferences.getGitHubToken()`). This is insecure because:
- PATs have broad repository access (read/write to all user repos)
- `SharedPreferences` is stored as plaintext XML on the device
- Token is sent directly from the client to GitHub API
- No user identity verification
- No token rotation or expiration management

### Proposed Architecture: OAuth + Backend Proxy

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────┐
│  Android App │────>│  Backend Proxy    │────>│  GitHub API   │
│              │<────│  (Cloud Function) │<────│              │
└──────────────┘     └──────────────────┘     └──────────────┘
        │                     │
        │  OAuth 2.0 PKCE     │  Server-side PAT
        │  (no client secret) │  (never exposed)
        └─────────────────────┘
```

### Component 1: GitHub OAuth App Registration

1. Register an OAuth App at `github.com/settings/developers`
2. Set callback URL to a custom URI scheme: `ambermoon://auth/callback`
3. This gives you a **Client ID** (public, safe to embed in app) and a **Client Secret** (kept server-side only)

### Component 2: Android Login Flow (OAuth 2.0 with PKCE)

PKCE (Proof Key for Code Exchange) is required because mobile apps cannot securely store a client secret.

**Step-by-step flow:**

1. **User taps "Sign In with GitHub"** in the settings menu
2. **App generates PKCE parameters:**
   - `code_verifier`: Random 43-128 character string (stored in memory)
   - `code_challenge`: SHA-256 hash of `code_verifier`, Base64-URL encoded
3. **App opens system browser** (not a WebView) to:
   ```
   https://github.com/login/oauth/authorize
     ?client_id=<APP_CLIENT_ID>
     ?redirect_uri=ambermoon://auth/callback
     ?scope=repo
     ?state=<random_csrf_token>
     ?code_challenge=<code_challenge>
     ?code_challenge_method=S256
   ```
4. **User authenticates with GitHub** in their browser (app never sees password)
5. **GitHub redirects to** `ambermoon://auth/callback?code=<auth_code>&state=<csrf>`
6. **App's intent filter catches the redirect**, validates `state` matches
7. **App sends `auth_code` + `code_verifier` to backend proxy** (not directly to GitHub)
8. **Backend proxy exchanges code for access token:**
   - Sends `auth_code` + `code_verifier` + `client_secret` to GitHub
   - Receives access token
   - Generates a **session token** (JWT) for the app
   - Stores mapping: `session_token → github_access_token` (encrypted)
   - Returns only the session token to the app
9. **App stores session token** in Android Keystore (hardware-backed, not SharedPreferences)

**Android implementation classes needed:**

| Class | Purpose |
|---|---|
| `auth/GitHubAuthManager.java` | PKCE flow, browser launch, callback handling |
| `auth/SecureTokenStorage.java` | Android Keystore wrapper for session tokens |
| `auth/AuthState.java` | Login state tracking (logged out, pending, authenticated) |

**AndroidManifest.xml addition:**
```xml
<activity android:name=".auth.AuthCallbackActivity"
          android:exported="true"
          android:launchMode="singleTask">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="ambermoon" android:host="auth" android:path="/callback" />
    </intent-filter>
</activity>
```

### Component 3: Backend Proxy (Cloud Function)

A lightweight serverless function (Firebase Functions, AWS Lambda, or Cloudflare Workers) that:

1. **`POST /auth/token`** - Exchanges OAuth code for session token
   - Input: `auth_code`, `code_verifier`
   - Validates with GitHub using `client_secret` (stored as environment variable)
   - Returns: `session_token` (JWT, 30-day expiry)

2. **`POST /save/upload`** - Uploads save data to GitHub
   - Input: `session_token`, `save_data` (JSON)
   - Validates session token
   - Resolves GitHub username from stored access token
   - Commits save to `cloud-saves/<github_username>/player_data.json`
   - Returns: success/failure

3. **`GET /save/download`** - Downloads save data from GitHub
   - Input: `session_token`
   - Validates session token
   - Fetches save from `cloud-saves/<github_username>/player_data.json`
   - Returns: save data JSON

4. **`POST /auth/refresh`** - Refreshes session token
   - Input: `session_token` (near-expiry)
   - Returns: new `session_token`

5. **`POST /auth/logout`** - Revokes session
   - Input: `session_token`
   - Deletes session mapping
   - Optionally revokes GitHub access token

### Component 4: Secure Token Storage (Android)

```
┌─────────────────────────────────────────────┐
│           Android Keystore System           │
│  ┌───────────────────────────────────────┐  │
│  │  Hardware-backed key storage (TEE)    │  │
│  │  - AES-256 encryption key            │  │
│  │  - Never leaves secure hardware      │  │
│  └───────────────────────────────────────┘  │
│              ↕ encrypt/decrypt              │
│  ┌───────────────────────────────────────┐  │
│  │  EncryptedSharedPreferences           │  │
│  │  - Session token (encrypted)         │  │
│  │  - Token expiry timestamp            │  │
│  │  - GitHub username (encrypted)       │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

**Key decisions:**
- Use `EncryptedSharedPreferences` from AndroidX Security library (requires API 23+, app already targets 24+)
- Session tokens are JWTs with 30-day expiry
- Automatic refresh when within 7 days of expiry
- On logout, both local token and server session are destroyed

### Component 5: Save Data Format (Cross-Platform Compatible)

The save JSON format must be identical between desktop and Android. The current `CloudSaveManager` already uses a compatible format:

```json
{
  "version": 2,
  "platform": "android",
  "lastModified": 1706900000000,
  "syncId": "uuid-here",
  "developerMode": false,
  "dailyChestLastOpened": 1706800000000,
  "monthlyChestLastOpened": 1704200000000,
  "totalItemsCollected": 42,
  "legendaryItemsFound": 3,
  "mythicItemsFound": 1,
  "inventory": [
    {"itemId": "iron_sword", "stackCount": 1}
  ],
  "vaultItems": [
    {"itemId": "health_potion", "stackCount": 5}
  ]
}
```

### Component 6: Conflict Resolution

When both desktop and Android modify save data:

1. **Compare `lastModified` timestamps** on download
2. **If conflict detected**, present user with choice:
   - "Use Local Save (Android)" - overwrites cloud
   - "Use Cloud Save (Desktop)" - overwrites local
   - "Keep Both" - merges vault items (union), keeps newer chest cooldowns
3. **Auto-merge strategy** (for non-conflicting data):
   - Vault items: union of both item sets
   - Chest cooldowns: use the most recent open timestamp
   - Stats: use the higher values (max of totalItemsCollected, etc.)
   - Developer mode: OR (if either has it on, keep it on)

### Component 7: Desktop Integration

The desktop `SaveManager` needs parallel changes:

1. Add optional GitHub OAuth login (browser-based, same flow)
2. Add sync-to-cloud on save (via backend proxy, not direct GitHub API)
3. Add sync-from-cloud on load
4. Store session token in OS keychain (macOS Keychain, Windows Credential Manager, Linux Secret Service)

### Security Summary

| Threat | Mitigation |
|---|---|
| Token theft from device | Android Keystore (hardware-backed encryption) |
| Man-in-the-middle | HTTPS only, certificate pinning on proxy |
| Client secret exposure | PKCE flow - no client secret on device |
| Token replay | Short-lived session tokens with server-side validation |
| Unauthorized repo access | Backend proxy uses scoped token, only writes to `cloud-saves/` |
| Cross-user save tampering | Backend resolves GitHub username from token, user cannot specify arbitrary paths |
| Phishing via WebView | Uses system browser (Chrome Custom Tabs), not embedded WebView |
| Stale tokens | 30-day expiry with auto-refresh, logout revokes server session |

### Minimal Viable Implementation (No Backend)

If a backend proxy is not immediately feasible, a simpler (less secure) approach:

1. Use GitHub's **Device Flow** (OAuth for devices without browsers)
   - User gets a code, enters it at `github.com/login/device`
   - App polls GitHub until user authorizes
   - Receives access token directly (no backend needed)
2. Store token in `EncryptedSharedPreferences` (better than current plaintext)
3. Use **fine-grained PAT** scoped to only the game repository
4. App communicates directly with GitHub API (current approach, but with proper token)

**Trade-off:** Device Flow is simpler to implement but the token lives on-device. The backend proxy approach is more secure because the GitHub token never reaches the client at all.

---

## 6. Recommended Next Steps

1. **Start with Phase 1-2** of the porting order to establish the entity/block foundation
2. **Bulk-convert item classes** using a script (replace `java.awt.Color` → `android.graphics.Color`, etc.)
3. **Implement Device Flow login** as an immediate improvement over raw PAT storage
4. **Plan backend proxy** as a follow-up for production security
5. **Add GIF decoder library** (`android-gif-drawable` or `Glide`) for proper animated sprite support on Android
