#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

export DISPLAY="${DISPLAY:-:0}"

if ! pgrep -f 'termux-x11.*:0' >/dev/null 2>&1; then
  termux-x11 "$DISPLAY" >/dev/null 2>&1 &
fi

if command -v am >/dev/null 2>&1; then
  am start --user 0 -n com.termux.x11/com.termux.x11.MainActivity >/dev/null 2>&1 || true
fi

printf 'Termux:X11 session is ready on %s\n' "$DISPLAY"
