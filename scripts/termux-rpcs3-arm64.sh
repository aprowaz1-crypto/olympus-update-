#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
source "$SCRIPT_DIR/common.sh"

is_termux() {
  [[ -n "${TERMUX_VERSION:-}" ]] && return 0
  [[ "${PREFIX:-}" == *"/com.termux/files/usr"* ]] && return 0
  command -v termux-info >/dev/null 2>&1
}

ensure_rpcs3_installed() {
  if [[ -x "$HELIOS3_HOME/rpcs3.AppImage" ]] || [[ -x "$HELIOS3_HOME/rpcs3/bin/rpcs3" ]] || [[ -x "$HELIOS3_HOME/rpcs3/rpcs3" ]]; then
    return 0
  fi

  log "RPCS3 arm64 build not found. Running install.sh..."
  "$ROOT_DIR/install.sh"
}

main() {
  local arch
  arch="$(uname -m)"
  ensure_dirs

  if [[ "$arch" != "aarch64" && "$arch" != "arm64" ]]; then
    die "This launcher is for ARM64 only. Current arch: $arch"
  fi

  if ! is_termux; then
    warn "Termux environment was not detected. Trying to continue anyway."
  fi

  ensure_rpcs3_installed

  export TMPDIR="${TMPDIR:-${PREFIX:-/tmp}/tmp}"
  mkdir -p "$TMPDIR"
  export XDG_RUNTIME_DIR="${XDG_RUNTIME_DIR:-$TMPDIR}"
  mkdir -p "$XDG_RUNTIME_DIR"

  export GALLIUM_DRIVER="${GALLIUM_DRIVER:-zink}"
  export MESA_LOADER_DRIVER_OVERRIDE="${MESA_LOADER_DRIVER_OVERRIDE:-zink}"
  export SDL_JOYSTICK_HIDAPI="${SDL_JOYSTICK_HIDAPI:-0}"
  export SDL_GAMECONTROLLER_USE_BUTTON_LABELS="${SDL_GAMECONTROLLER_USE_BUTTON_LABELS:-0}"
  export SDL_GAMECONTROLLERCONFIG_FILE="$HELIOS3_CONFIG_DIR/gamecontrollerdb.txt"

  if [[ -f "$HELIOS3_CONFIG_DIR/gamepad.env" ]]; then
    # shellcheck disable=SC1090
    source "$HELIOS3_CONFIG_DIR/gamepad.env"
  fi

  if [[ -x "$HELIOS3_HOME/rpcs3.AppImage" ]]; then
    exec "$HELIOS3_HOME/rpcs3.AppImage" --appimage-extract-and-run "$@"
  fi

  if [[ -x "$HELIOS3_HOME/rpcs3/bin/rpcs3" ]]; then
    exec "$HELIOS3_HOME/rpcs3/bin/rpcs3" "$@"
  fi

  if [[ -x "$HELIOS3_HOME/rpcs3/rpcs3" ]]; then
    exec "$HELIOS3_HOME/rpcs3/rpcs3" "$@"
  fi

  die "RPCS3 binary was not found after installation attempt."
}

main "$@"