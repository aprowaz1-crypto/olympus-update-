# Helios3 RPCS3 Port

Fully native Android launcher and porting base for RPCS3 on ARM64 devices.

> Project note: the early Helios3 groundwork was built with help from GitHub Copilot using Goldeneye (Preview).

## What this does

- builds a native Android launcher UI;
- includes firmware setup, driver management, diagnostics, and update checks;
- compiles an Android NDK bootstrap core layer;
- prepares the path for direct upstream RPCS3 integration.

## Quick start

```bash
git clone https://github.com/aprowaz1-crypto/olympus-update-.git
cd olympus-update-
bash install.sh
```

Then open [android-app/README.md](android-app/README.md) and build the APK.

## GitHub Actions build

The repository includes an Android workflow in [.github/workflows/android-build.yml](.github/workflows/android-build.yml).

On each relevant push, pull request, or manual run, it builds the Android APKs and uploads them as workflow artifacts.

## Android port direction

A real Android port starter is included in [android-app/README.md](android-app/README.md).

This project ports the existing RPCS3 core to Android through the NDK rather than writing a brand-new emulator.

See the porting notes in [PORTING.md](PORTING.md) and the 0.1 readiness list in [RELEASE_0_1.md](RELEASE_0_1.md).

## Notes

- Target platform: Android + ARM64.
- For unstable Vulkan drivers, start with Zink or a safe driver preset.
