#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/common.sh
source "$SCRIPT_DIR/common.sh"

ensure_dirs

printf 'Helios3 native doctor\n'
printf '=====================\n'
printf 'Arch: %s\n' "$(uname -m)"
printf 'Prefix: %s\n' "$PREFIX"
printf 'Display: %s\n' "${DISPLAY:-:0}"

for cmd in pulseaudio termux-x11 am curl; do
  if command -v "$cmd" >/dev/null 2>&1; then
    printf '[ok] %s\n' "$cmd"
  else
    printf '[--] %s not found\n' "$cmd"
  fi
done

if ls /dev/input/event* >/dev/null 2>&1; then
  printf '\nVisible input nodes:\n'
  ls /dev/input/event* 2>/dev/null | sed 's/^/  - /'
else
  printf '\nNo /dev/input/event* nodes are visible to Termux right now.\n'
fi

if [ -f "$HELIOS3_CONFIG_DIR/gamepad.env" ]; then
  printf '\nGamepad config: %s\n' "$HELIOS3_CONFIG_DIR/gamepad.env"
else
  printf '\nGamepad config has not been generated yet. Run helios3-gamepad-fix\n'
fi
