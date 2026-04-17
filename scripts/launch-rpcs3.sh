#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/common.sh
source "$SCRIPT_DIR/common.sh"

ensure_dirs

export DISPLAY="${DISPLAY:-:0}"
export XDG_RUNTIME_DIR="${XDG_RUNTIME_DIR:-${TMPDIR:-/tmp}}"
mkdir -p "$XDG_RUNTIME_DIR"
export PULSE_SERVER="${PULSE_SERVER:-127.0.0.1}"
export GALLIUM_DRIVER="${GALLIUM_DRIVER:-zink}"
export MESA_LOADER_DRIVER_OVERRIDE="${MESA_LOADER_DRIVER_OVERRIDE:-zink}"
export QT_QPA_PLATFORM="${QT_QPA_PLATFORM:-xcb}"
export QT_XCB_GL_INTEGRATION="${QT_XCB_GL_INTEGRATION:-xcb_egl}"
export SDL_JOYSTICK_HIDAPI="${SDL_JOYSTICK_HIDAPI:-0}"
export SDL_GAMECONTROLLER_USE_BUTTON_LABELS="${SDL_GAMECONTROLLER_USE_BUTTON_LABELS:-0}"
export SDL_GAMECONTROLLERCONFIG_FILE="$HELIOS3_CONFIG_DIR/gamecontrollerdb.txt"

if [ -f "$HELIOS3_CONFIG_DIR/gamepad.env" ]; then
  # shellcheck disable=SC1090
  source "$HELIOS3_CONFIG_DIR/gamepad.env"
fi

if command -v pulseaudio >/dev/null 2>&1; then
  pulseaudio --check >/dev/null 2>&1 || pulseaudio --start >/dev/null 2>&1 || true
fi

if command -v termux-wake-lock >/dev/null 2>&1; then
  termux-wake-lock >/dev/null 2>&1 || true
fi

if [ "${HELIOS3_USE_VIRGL:-${OLYMPUS_USE_VIRGL:-0}}" = "1" ] && command -v virgl_test_server_android >/dev/null 2>&1; then
  pgrep -f virgl_test_server_android >/dev/null 2>&1 || (
    virgl_test_server_android >/dev/null 2>&1 &
  )
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

die "RPCS3 binary not found. Run ./install.sh first."
