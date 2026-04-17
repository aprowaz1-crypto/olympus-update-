# Olympus RPCS3 Android Port Starter

This folder now targets a real Android port direction for the existing RPCS3 project.

## What is inside

- a native Android app front end;
- an NDK bridge and CMake build entry point;
- a path to import the upstream RPCS3 source tree;
- temporary bootstrap actions for the current Termux-based core while the direct port is still being integrated.

## Build

1. Open this folder in Android Studio.
2. Let Android Studio install the Gradle and Android SDK requirements.
3. Build the debug APK.
4. For actual core integration, import the upstream RPCS3 source with the repository tools and extend the NDK build.

## Scope

This is not a brand new emulator. It is the beginning of an Android port around the existing RPCS3 codebase.
