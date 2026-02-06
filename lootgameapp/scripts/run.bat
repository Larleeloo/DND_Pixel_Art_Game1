@echo off
REM ============================================================
REM Loot Game App - Build + Install + Launch (Windows)
REM ============================================================
REM One-click build, install on emulator/device, and launch
REM ============================================================

setlocal enabledelayedexpansion

echo ============================================================
echo Loot Game App - Build + Install + Launch
echo ============================================================

REM Step 1: Build
call "%~dp0build.bat"
if errorlevel 1 (
    echo Build failed! Cannot install.
    exit /b 1
)

REM Resolve paths
for %%i in ("%~dp0..") do set LOOTGAME_DIR=%%~fi
for %%i in ("%LOOTGAME_DIR%\..") do set PROJECT_DIR=%%~fi
set SDK_ROOT=%PROJECT_DIR%\android\sdk
set ADB=%SDK_ROOT%\platform-tools\adb.exe
set APK=%LOOTGAME_DIR%\app\build\loot-game.apk

REM Check ADB
if not exist "%ADB%" (
    echo ERROR: adb not found at %ADB%
    echo Please ensure Android SDK platform-tools are installed.
    exit /b 1
)

REM Check APK
if not exist "%APK%" (
    echo ERROR: APK not found at %APK%
    echo Build may have failed.
    exit /b 1
)

REM Step 2: Install
echo.
echo Installing APK...
"%ADB%" install -r "%APK%"
if errorlevel 1 (
    echo.
    echo Install failed. Trying to uninstall first...
    "%ADB%" uninstall com.ambermoon.lootgame
    "%ADB%" install "%APK%"
    if errorlevel 1 (
        echo ERROR: Installation failed!
        exit /b 1
    )
)

REM Step 3: Launch
echo.
echo Launching Loot Game...
"%ADB%" shell am start -n com.ambermoon.lootgame/.core.MainActivity
if errorlevel 1 (
    echo WARNING: Launch command failed, but app may still be installed.
)

echo.
echo ============================================================
echo DONE! Loot Game should be running on your device/emulator.
echo ============================================================

endlocal
