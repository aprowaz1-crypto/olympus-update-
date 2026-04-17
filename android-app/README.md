# Helios3 RPCS3 Android Port Starter

This folder targets a real native Android port direction for the existing RPCS3 project.

## What is inside

- a native Android app front end;
- built-in graphics, audio, CPU, firmware, and driver-management settings UI;
- a compiled NDK bootstrap core layer with surface rendering and input routing;
- a path to integrate the upstream RPCS3 source tree directly.

## Build

1. Open this folder in Android Studio.
2. Copy [local.properties.example](local.properties.example) to a new local.properties file.
3. Set sdk.dir to your installed Android SDK path.
4. Let Android Studio install any missing SDK or NDK components.
5. Build the debug APK.

## Scope

This is not a brand new emulator. It is a native Android front end and porting base around the existing RPCS3 codebase.
