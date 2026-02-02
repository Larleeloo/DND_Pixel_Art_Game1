# The Amber Moon - Android Port Setup Guide

This document provides comprehensive instructions for setting up the Android development environment in IntelliJ IDEA on Windows 11 without using Gradle.

## Table of Contents
1. [Required Downloads](#required-downloads)
2. [Android SDK Setup](#android-sdk-setup)
3. [IntelliJ IDEA Configuration](#intellij-idea-configuration)
4. [Project Structure](#project-structure)
5. [Building the APK](#building-the-apk)
6. [Testing on Device/Emulator](#testing-on-deviceemulator)
7. [Cross-Platform Save Setup](#cross-platform-save-setup)
8. [Troubleshooting](#troubleshooting)

---

## Required Downloads

### 1. Android SDK Command-Line Tools

Download the Android SDK command-line tools (NOT Android Studio):

**Windows:**
```
https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip
```

**Direct Link:** [Download SDK Command-Line Tools (Windows)](https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip)

### 2. Java Development Kit (JDK) 17+

If not already installed:
```
https://adoptium.net/temurin/releases/?version=17
```

**Direct Link:** [Eclipse Temurin JDK 17](https://adoptium.net/temurin/releases/?version=17&package=jdk&os=windows&arch=x64)

### 3. External Libraries (Place in `android/app/libs/`)

| Library | Version | Purpose | Download Link |
|---------|---------|---------|---------------|
| Gson | 2.10.1 | JSON parsing | [Download](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar) |

**Direct Links:**
- **Gson 2.10.1:** https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar

---

## Android SDK Setup

### Step 1: Extract SDK Tools

1. Create the SDK directory:
   ```
   DND_Pixel_Art_Game1/android/sdk/
   ```

2. Extract the downloaded `commandlinetools-win-*.zip`

3. **IMPORTANT:** Create the correct folder structure:
   ```
   android/sdk/cmdline-tools/latest/
   ```

4. Move the contents of the extracted folder INTO `latest/`:
   ```
   android/sdk/cmdline-tools/latest/bin/
   android/sdk/cmdline-tools/latest/lib/
   android/sdk/cmdline-tools/latest/NOTICE.txt
   android/sdk/cmdline-tools/latest/source.properties
   ```

### Step 2: Install SDK Components

Open Command Prompt (as Administrator) and run:

```batch
cd C:\path\to\DND_Pixel_Art_Game1\android\sdk\cmdline-tools\latest\bin

sdkmanager --sdk_root=C:\path\to\DND_Pixel_Art_Game1\android\sdk "platforms;android-34"
sdkmanager --sdk_root=C:\path\to\DND_Pixel_Art_Game1\android\sdk "platforms;android-21"
sdkmanager --sdk_root=C:\path\to\DND_Pixel_Art_Game1\android\sdk "build-tools;34.0.0"
sdkmanager --sdk_root=C:\path\to\DND_Pixel_Art_Game1\android\sdk "platform-tools"
```

Accept the licenses when prompted:
```batch
sdkmanager --sdk_root=C:\path\to\DND_Pixel_Art_Game1\android\sdk --licenses
```

### Step 3: Verify Installation

Your SDK folder should now contain:
```
android/sdk/
├── build-tools/
│   └── 34.0.0/
│       ├── aapt2.exe
│       ├── d8.bat
│       ├── apksigner.bat
│       ├── zipalign.exe
│       └── ...
├── cmdline-tools/
│   └── latest/
├── platforms/
│   ├── android-21/
│   └── android-34/
│       └── android.jar
└── platform-tools/
    ├── adb.exe
    └── ...
```

---

## IntelliJ IDEA Configuration

### Step 1: Install Android Plugin

1. Open IntelliJ IDEA
2. Go to **File → Settings → Plugins**
3. Search for "**Android**"
4. Install the official Android plugin
5. **Restart IntelliJ IDEA**

### Step 2: Configure Android SDK Location

1. Go to **File → Project Structure → Platform Settings → SDKs**
2. Click the **+** button
3. Select **Android SDK**
4. Browse to: `C:\path\to\DND_Pixel_Art_Game1\android\sdk`
5. Select **Android API 34** as the build target
6. Click **OK**

### Step 3: Create Android Module

1. Go to **File → Project Structure → Modules**
2. Click the **+** button
3. Select **Import Module**
4. Navigate to `DND_Pixel_Art_Game1/android/app`
5. Select **Create module from existing sources**
6. Follow the wizard, ensuring:
   - Module SDK: Android API 34
   - Sources: `src/main/java`

### Step 4: Configure Module Dependencies

1. In **Project Structure → Modules → android-app**
2. Go to the **Dependencies** tab
3. Click **+** → **JARs or directories**
4. Add:
   - `android/sdk/platforms/android-34/android.jar`
   - All JARs from `android/app/libs/`

### Step 5: Mark Directories

Right-click on folders and mark as:
- `android/app/src/main/java` → **Sources Root** (blue folder)
- `android/app/src/main/res` → **Resources Root**
- `android/app/build` → **Excluded**

### Step 6: Configure Run Configuration

1. Go to **Run → Edit Configurations**
2. Click **+** → **Android App**
3. Configure:
   - **Module:** android-app
   - **Deploy:** APK from app module
   - **Launch Activity:** `com.ambermoongame.core.MainActivity`

---

## Project Structure

```
android/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/ambermoongame/
│   │       │   ├── core/           # MainActivity, GameActivity, GameSurfaceView
│   │       │   ├── scene/          # All scene implementations
│   │       │   ├── entity/         # Entity classes (port from desktop)
│   │       │   ├── input/          # TouchInputManager, ControllerManager
│   │       │   ├── graphics/       # AndroidAssetLoader
│   │       │   ├── audio/          # AndroidAudioManager
│   │       │   ├── save/           # CloudSaveManager
│   │       │   └── ui/             # TouchControlOverlay
│   │       ├── res/
│   │       │   ├── values/         # strings.xml, colors.xml, styles.xml
│   │       │   ├── drawable/       # App icons (create ic_launcher.png)
│   │       │   └── layout/         # (not used - custom rendering)
│   │       ├── assets/             # Copy game assets here
│   │       └── AndroidManifest.xml
│   ├── libs/                       # External JAR libraries
│   └── build/                      # Build output (auto-generated)
├── sdk/                            # Android SDK
├── keystore/                       # APK signing keys
├── scripts/                        # Build scripts
└── ANDROID_SETUP.md               # This file
```

---

## Building the APK

### Option 1: Using Build Script (Recommended)

**Windows:**
```batch
cd android\scripts
build.bat
```

**Linux/macOS:**
```bash
cd android/scripts
./build.sh
```

The APK will be created at: `android/app/build/amber-moon.apk`

### Option 2: Manual Build

1. **Compile Resources:**
   ```batch
   android\sdk\build-tools\34.0.0\aapt2.exe compile --dir android\app\src\main\res -o android\app\build\res.zip
   ```

2. **Link Resources:**
   ```batch
   android\sdk\build-tools\34.0.0\aapt2.exe link -o android\app\build\app.unsigned.apk -I android\sdk\platforms\android-34\android.jar --manifest android\app\src\main\AndroidManifest.xml -R android\app\build\res.zip --auto-add-overlay
   ```

3. **Compile Java:**
   ```batch
   javac -source 1.8 -target 1.8 -classpath "android\sdk\platforms\android-34\android.jar;android\app\libs\*" -d android\app\build\classes android\app\src\main\java\com\ambermoongame\**\*.java
   ```

4. **Create DEX:**
   ```batch
   android\sdk\build-tools\34.0.0\d8.bat --output android\app\build\dex android\app\build\classes
   ```

5. **Sign APK:**
   ```batch
   android\sdk\build-tools\34.0.0\apksigner.bat sign --ks android\keystore\debug.keystore --ks-pass pass:android --out android\app\build\amber-moon.apk android\app\build\app.aligned.apk
   ```

### Option 3: IntelliJ Build

1. Use **Build → Make Project** (Ctrl+F9)
2. Use **Build → Build APK(s)**
3. APK output: `android/app/build/outputs/apk/`

---

## Testing on Device/Emulator

### Using Physical Device

1. Enable **Developer Options** on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times

2. Enable **USB Debugging** in Developer Options

3. Connect device via USB

4. Install APK:
   ```batch
   android\sdk\platform-tools\adb.exe install android\app\build\amber-moon.apk
   ```

### Using Emulator

1. Create AVD (Android Virtual Device) in IntelliJ:
   - **Tools → AVD Manager**
   - Create Virtual Device → Tablet → Pixel C
   - System Image: API 34
   - Enable hardware keyboard

2. Start emulator and install APK

---

## Cross-Platform Save Setup

### GitHub Personal Access Token

1. Go to https://github.com/settings/tokens
2. Click **Generate new token (classic)**
3. Select scopes:
   - `repo` (Full control of private repositories)
4. Copy the generated token

### Configure in Game

1. Open Settings in the game
2. Go to Cloud Sync tab
3. Enter your GitHub username
4. Paste the Personal Access Token
5. Enable Cloud Sync

### Repository Structure

The cloud saves will be stored at:
```
https://github.com/Larleeloo/DND_Pixel_Art_Game1/cloud-saves/<your-username>/player_data.json
```

---

## Troubleshooting

### "SDK not found" Error

1. Verify SDK path in IntelliJ: **File → Project Structure → SDKs**
2. Ensure `android.jar` exists at `android/sdk/platforms/android-34/android.jar`

### "Cannot find symbol" Compile Errors

1. Check dependencies are added correctly
2. Ensure `android.jar` is in classpath
3. Mark `src/main/java` as Sources Root

### APK Won't Install

1. Uninstall existing version first:
   ```batch
   adb uninstall com.ambermoongame
   ```
2. Check device has USB debugging enabled
3. Allow installation from unknown sources

### Touch Controls Not Responding

1. Verify `TouchControlOverlay` is added to layout
2. Check touch events are being forwarded
3. Enable debug mode to see touch points

### Cloud Sync Fails

1. Verify GitHub token has `repo` permissions
2. Check internet connection
3. Ensure repository structure exists
4. Check token hasn't expired

---

## Quick Reference: File Locations

| Component | Path |
|-----------|------|
| Android SDK | `android/sdk/` |
| Build Tools | `android/sdk/build-tools/34.0.0/` |
| Platform JAR | `android/sdk/platforms/android-34/android.jar` |
| Source Code | `android/app/src/main/java/` |
| Resources | `android/app/src/main/res/` |
| Game Assets | `android/app/src/main/assets/` |
| External JARs | `android/app/libs/` |
| Build Output | `android/app/build/` |
| APK Output | `android/app/build/amber-moon.apk` |
| Build Script | `android/scripts/build.bat` |

---

## Next Steps

1. **Copy Game Assets:** Copy contents from the main `assets/` folder to `android/app/src/main/assets/`

2. **Create App Icons:** Create launcher icons:
   - `android/app/src/main/res/drawable/ic_launcher.png` (48x48, 72x72, 96x96, 144x144, 192x192)
   - `android/app/src/main/res/drawable/ic_launcher_round.png` (same sizes, circular)

3. **Port Remaining Scenes:** The scene placeholders need full implementations ported from desktop

4. **Test on Multiple Devices:** Test touch controls and scaling on different screen sizes

5. **Optimize Assets:** Consider compressing images for mobile devices

---

*Last updated: February 2026*
