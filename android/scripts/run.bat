@echo off
REM ============================================================================
REM The Amber Moon - Android Build, Install, and Launch Script
REM ============================================================================
REM This script builds the APK, installs it on a connected device/emulator,
REM and launches the app automatically.
REM
REM Usage: run.bat [options]
REM Options:
REM   --skip-build    Skip the build step (use existing APK)
REM   --no-launch     Install but don't launch the app
REM   --clean         Clean build (delete build directory first)
REM   --logcat        Show logcat output after launch
REM ============================================================================

setlocal enabledelayedexpansion

REM Parse command line arguments
set SKIP_BUILD=0
set NO_LAUNCH=0
set CLEAN_BUILD=0
set SHOW_LOGCAT=0

:parse_args
if "%~1"=="" goto :args_done
if /i "%~1"=="--skip-build" set SKIP_BUILD=1
if /i "%~1"=="--no-launch" set NO_LAUNCH=1
if /i "%~1"=="--clean" set CLEAN_BUILD=1
if /i "%~1"=="--logcat" set SHOW_LOGCAT=1
shift
goto :parse_args
:args_done

REM Get script directory
set "SCRIPT_DIR=%~dp0"
set "ANDROID_DIR=%SCRIPT_DIR%..\"
set "APP_DIR=%ANDROID_DIR%app\"
set "SDK_DIR=%ANDROID_DIR%sdk\"
set "APK_PATH=%APP_DIR%build\amber-moon.apk"

REM App package and activity
set "PACKAGE=com.ambermoongame"
set "ACTIVITY=.core.MainActivity"

echo ============================================================================
echo   The Amber Moon - Android Build and Launch
echo ============================================================================
echo.

REM Check for ADB
set "ADB=%SDK_DIR%platform-tools\adb.exe"
if not exist "%ADB%" (
    echo ERROR: ADB not found at %ADB%
    echo Please ensure Android SDK platform-tools is installed.
    exit /b 1
)

REM Check for connected device/emulator
echo Checking for connected devices...
"%ADB%" devices | findstr /r /c:"device$" >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: No device or emulator connected.
    echo.
    echo Please either:
    echo   1. Start an Android emulator, or
    echo   2. Connect an Android device with USB debugging enabled
    echo.
    echo To start an emulator from command line:
    echo   %SDK_DIR%emulator\emulator.exe -avd YOUR_AVD_NAME
    echo.
    echo To list available AVDs:
    echo   %SDK_DIR%emulator\emulator.exe -list-avds
    echo.
    exit /b 1
)

echo Device/emulator connected.
echo.

REM Clean build if requested
if %CLEAN_BUILD%==1 (
    echo Cleaning previous build...
    if exist "%APP_DIR%build" (
        rmdir /s /q "%APP_DIR%build"
    )
    echo Clean complete.
    echo.
)

REM Build step
if %SKIP_BUILD%==0 (
    echo ============================================================================
    echo   Building APK...
    echo ============================================================================
    echo.

    pushd "%SCRIPT_DIR%"
    call build.bat
    if errorlevel 1 (
        echo.
        echo ERROR: Build failed!
        popd
        exit /b 1
    )
    popd

    echo.
    echo Build complete.
    echo.
) else (
    echo Skipping build (--skip-build specified)
    echo.

    if not exist "%APK_PATH%" (
        echo ERROR: APK not found at %APK_PATH%
        echo Please run without --skip-build first.
        exit /b 1
    )
)

REM Install APK
echo ============================================================================
echo   Installing APK...
echo ============================================================================
echo.

echo Installing %APK_PATH%...
"%ADB%" install -r "%APK_PATH%"
if errorlevel 1 (
    echo.
    echo ERROR: Installation failed!
    echo.
    echo Common causes:
    echo   - App signature mismatch (uninstall old version first)
    echo   - Insufficient storage on device
    echo   - Device/emulator not properly connected
    echo.
    echo To uninstall existing app:
    echo   %ADB% uninstall %PACKAGE%
    echo.
    exit /b 1
)

echo.
echo Installation successful.
echo.

REM Launch app
if %NO_LAUNCH%==0 (
    echo ============================================================================
    echo   Launching app...
    echo ============================================================================
    echo.

    echo Starting %PACKAGE%/%ACTIVITY%...
    "%ADB%" shell am start -n "%PACKAGE%/%ACTIVITY%"
    if errorlevel 1 (
        echo.
        echo WARNING: Failed to launch app. It may need to be started manually.
    ) else (
        echo.
        echo App launched successfully!
    )
    echo.
) else (
    echo Skipping launch (--no-launch specified)
    echo.
)

REM Show logcat if requested
if %SHOW_LOGCAT%==1 (
    echo ============================================================================
    echo   Logcat Output (Ctrl+C to stop)
    echo ============================================================================
    echo.
    echo Filtering for: %PACKAGE%, AndroidRuntime, GameSurfaceView, SceneManager
    echo.

    "%ADB%" logcat -c
    "%ADB%" logcat -s "%PACKAGE%:V" "AndroidRuntime:E" "GameSurfaceView:D" "SceneManager:D" "AmberMoon:V"
)

echo ============================================================================
echo   Done!
echo ============================================================================
echo.
echo Useful commands:
echo   View logs:     %ADB% logcat -s %PACKAGE%:V AndroidRuntime:E
echo   Uninstall:     %ADB% uninstall %PACKAGE%
echo   Stop app:      %ADB% shell am force-stop %PACKAGE%
echo   Restart app:   %ADB% shell am start -n %PACKAGE%/%ACTIVITY%
echo.

endlocal
exit /b 0
