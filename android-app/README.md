# Olympus Launcher App

This folder contains the native Android helper app for Olympus.

## What it does

- gives Olympus a proper Android UI;
- sends install and launch commands to Termux;
- keeps the emulator path native instead of moving it into a slower compatibility layer.

## Build

1. Open this folder in Android Studio.
2. Let Android Studio download the required Gradle and Android SDK components.
3. Build the debug APK and install it on the phone.

## Important

The app is a launcher and control surface. The heavy RPCS3 work still runs through the native Termux stack installed by the main Olympus scripts.
