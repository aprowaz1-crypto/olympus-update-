# RPCS3 Android Port Direction

This repository is now aimed at a real Android port path for RPCS3 rather than a brand new emulator.

## Goal

Reuse the existing RPCS3 core and adapt it for Android by:

- compiling the core with the Android NDK;
- replacing desktop-only glue where needed;
- keeping Vulkan rendering, audio, and input on Android-friendly backends;
- using a native Android app as the front end.

## Immediate steps

1. Pull the upstream RPCS3 sources into the local third-party folder.
2. Add Android-specific build patches without forking the entire architecture.
3. Replace or shim desktop assumptions around windows, files, input, and memory mapping.
4. Bring up the app with a minimal native boot path before tackling full game compatibility.
