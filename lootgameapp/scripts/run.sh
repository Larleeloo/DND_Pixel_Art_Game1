#!/bin/bash
# ============================================================
# Loot Game App - Build + Install + Launch (Linux/macOS)
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOOTGAME_DIR="$(dirname "$SCRIPT_DIR")"
PROJECT_DIR="$(dirname "$LOOTGAME_DIR")"
SDK_ROOT="$PROJECT_DIR/android/sdk"
ADB="$SDK_ROOT/platform-tools/adb"
APK="$LOOTGAME_DIR/app/build/loot-game.apk"

echo "============================================================"
echo "Loot Game App - Build + Install + Launch"
echo "============================================================"

# Step 1: Build
"$SCRIPT_DIR/build.sh"

# Step 2: Install
echo ""
echo "Installing APK..."
"$ADB" install -r "$APK" || {
    echo "Install failed. Trying uninstall first..."
    "$ADB" uninstall com.ambermoon.lootgame 2>/dev/null || true
    "$ADB" install "$APK"
}

# Step 3: Launch
echo ""
echo "Launching Loot Game..."
"$ADB" shell am start -n com.ambermoon.lootgame/.core.MainActivity

echo ""
echo "============================================================"
echo "DONE! Loot Game should be running on your device/emulator."
echo "============================================================"
