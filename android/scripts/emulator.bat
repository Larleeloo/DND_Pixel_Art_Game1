@echo off
REM ============================================================================
REM The Amber Moon - Android Emulator Launcher
REM ============================================================================
REM Launches the first available Android emulator AVD.
REM
REM Usage: emulator.bat [avd-name]
REM   If no AVD name is provided, launches the first one found.
REM ============================================================================

setlocal enabledelayedexpansion

REM Check project-local SDK first, then standard install location
set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%..") do set "ANDROID_DIR=%%~fi"
set "LOCAL_SDK=%ANDROID_DIR%\sdk"
set "USER_SDK=%LOCALAPPDATA%\Android\Sdk"

REM Find emulator executable
set "EMULATOR="
if exist "%LOCAL_SDK%\emulator\emulator.exe" (
    set "EMULATOR=%LOCAL_SDK%\emulator\emulator.exe"
) else if exist "%USER_SDK%\emulator\emulator.exe" (
    set "EMULATOR=%USER_SDK%\emulator\emulator.exe"
) else (
    echo ERROR: Android emulator not found.
    echo.
    echo Checked:
    echo   %LOCAL_SDK%\emulator\emulator.exe
    echo   %USER_SDK%\emulator\emulator.exe
    echo.
    echo Install via Android Studio: SDK Manager ^> SDK Tools ^> Android Emulator
    exit /b 1
)

echo Found emulator: %EMULATOR%
echo.

REM Use provided AVD name or find the first available one
if not "%~1"=="" (
    set "AVD_NAME=%~1"
    echo Using specified AVD: !AVD_NAME!
) else (
    echo Searching for available AVDs...
    set "AVD_NAME="

    REM List AVDs and grab the first one
    for /f "tokens=*" %%a in ('"%EMULATOR%" -list-avds 2^>nul') do (
        if not defined AVD_NAME (
            set "AVD_NAME=%%a"
        )
    )

    if not defined AVD_NAME (
        echo ERROR: No AVDs found.
        echo.
        echo Create one in Android Studio: Device Manager ^> Create Device
        echo   Recommended: Pixel 7a with API 34
        exit /b 1
    )

    echo Found AVD: !AVD_NAME!
)

echo.
echo Launching emulator...
echo ============================================================================
echo.

REM Launch emulator (stays in foreground so IntelliJ can stop it)
"%EMULATOR%" -avd "!AVD_NAME!"
