#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

ensure_dirs

export XDG_RUNTIME_DIR="${XDG_RUNTIME_DIR:-${TMPDIR:-/tmp}}"
mkdir -p "$XDG_RUNTIME_DIR"
export GALLIUM_DRIVER="${GALLIUM_DRIVER:-zink}"
export MESA_LOADER_DRIVER_OVERRIDE="${MESA_LOADER_DRIVER_OVERRIDE:-zink}"
export SDL_JOYSTICK_HIDAPI="${SDL_JOYSTICK_HIDAPI:-0}"
export SDL_GAMECONTROLLER_USE_BUTTON_LABELS="${SDL_GAMECONTROLLER_USE_BUTTON_LABELS:-0}"
export SDL_GAMECONTROLLERCONFIG_FILE="$HELIOS3_CONFIG_DIR/gamecontrollerdb.txt"

if [ -f "$HELIOS3_CONFIG_DIR/gamepad.env" ]; then
  source "$HELIOS3_CONFIG_DIR/gamepad.env"
fi

APPIMAGE="$HELIOS3_HOME/rpcs3.AppImage"
BIN_CANDIDATES=(
  "$HELIOS3_HOME/rpcs3/bin/rpcs3"
  "$HELIOS3_HOME/rpcs3/rpcs3"
)

if [ -x "$APPIMAGE" ]; then
  exec "$APPIMAGE" --appimage-extract-and-run "$@"
fi

for bin in "${BIN_CANDIDATES[@]}"; do
  if [ -x "$bin" ]; then
    exec "$bin" "$@"
  fi
done

die "Native RPCS3 assets were not found. Run ./install.sh first."
