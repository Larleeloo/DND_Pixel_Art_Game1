@echo off
REM ============================================================
REM The Amber Moon - Android Emulator Setup Script (Windows)
REM ============================================================
REM This script sets up a Pixel 7a emulator for testing
REM ============================================================

setlocal enabledelayedexpansion

set SDK_ROOT=%~dp0..\sdk
set SDKMANAGER=%SDK_ROOT%\cmdline-tools\latest\bin\sdkmanager.bat
set AVDMANAGER=%SDK_ROOT%\cmdline-tools\latest\bin\avdmanager.bat
set EMULATOR=%SDK_ROOT%\emulator\emulator.exe

echo ============================================================
echo The Amber Moon - Emulator Setup
echo ============================================================

REM Check if sdkmanager exists
if not exist "%SDKMANAGER%" (
    echo ERROR: SDK Manager not found at %SDKMANAGER%
    echo Please ensure Android SDK command-line tools are installed.
    goto :error
)

echo.
echo Step 1: Installing required SDK components...
echo.

REM Install emulator and system image
call "%SDKMANAGER%" "emulator"
call "%SDKMANAGER%" "system-images;android-34;google_apis;x86_64"

echo.
echo Step 2: Creating Pixel 7a AVD...
echo.

REM Check if AVD already exists
"%AVDMANAGER%" list avd | findstr /C:"pixel_7a" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Pixel 7a AVD already exists. Skipping creation.
) else (
    REM Create AVD
    echo no | "%AVDMANAGER%" create avd ^
        --name "pixel_7a" ^
        --package "system-images;android-34;google_apis;x86_64" ^
        --device "pixel_7a"

    if errorlevel 1 (
        echo Failed to create AVD. Trying with default device...
        echo no | "%AVDMANAGER%" create avd ^
            --name "pixel_7a" ^
            --package "system-images;android-34;google_apis;x86_64"
    )
)

echo.
echo ============================================================
echo EMULATOR SETUP COMPLETE!
echo ============================================================
echo.
echo To start the emulator, run:
echo   run-emulator.bat
echo.
echo Or manually:
echo   %EMULATOR% -avd pixel_7a
echo ============================================================
goto :end

:error
echo ============================================================
echo SETUP FAILED!
echo ============================================================
exit /b 1

:end
endlocal
