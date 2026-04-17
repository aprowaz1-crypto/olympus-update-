#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/scripts/common.sh"

ensure_dirs

main() {
  local url archive
  if ! url="$(pick_working_url)"; then
    cat >&2 <<'EOF'
[x] Could not auto-detect a current ARM64 RPCS3 package.
    Re-run with a direct download URL, for example:

    RPCS3_URL='https://example.com/rpcs3-arm64.AppImage' ./install.sh
EOF
    exit 1
  fi

  archive="$HELIOS3_CACHE_DIR/$(basename "$url")"
  download_file "$url" "$archive"
  extract_rpcs3 "$archive"
  record_installed_version "$url"
  "$SCRIPT_DIR/scripts/enable-gamepad.sh"

  cat <<EOF

✅ Helios3 Android native assets are prepared.

Next steps:
  1. Build the APK from android-app/
  2. Install it on your Android device
  3. Open the native launcher and boot the core

EOF
}

main "$@"
