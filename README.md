# Olympus Native Update

Native Termux-based launcher/installer for ARM64 RPCS3 on Android.

## What this does

- stays **native** on Termux and does **not** use proot, chroot, or a fake Linux shell layer;
- pulls the newest build from the official RPCS3 ARM64 binary feed automatically;
- still allows a manual direct URL override if upstream naming changes again;
- installs a reusable launcher for Termux:X11;
- writes sane SDL gamepad defaults for Xbox, DualShock 4, and DualSense pads.

## Quick start

```bash
pkg update -y
pkg install -y git
git clone https://github.com/aprowaz1-crypto/olympus-update-.git
cd olympus-update-
bash install.sh
```

If upstream changes the ARM64 asset name again, you can force a direct build URL:

```bash
RPCS3_URL='https://your-direct-link/rpcs3-arm64.AppImage' bash install.sh
```

## Launch RPCS3

```bash
olympus-start
olympus-rpcs3
```

## Gamepad support

Generate or refresh the controller profile:

```bash
olympus-gamepad-fix
```

Check the environment and visible inputs:

```bash
olympus-doctor
sdl2-jstest --list
```

## Notes

- Target platform: **Android + Termux + ARM64**.
- For the best controller detection, pair the pad before opening Termux:X11.
- If your device exposes unstable Vulkan drivers, start with Zink and only enable VirGL manually.
