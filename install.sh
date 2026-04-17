#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/common.sh
source "$SCRIPT_DIR/scripts/common.sh"

ensure_dirs

if [ "$(uname -m)" != "aarch64" ]; then
  die "This installer is for native ARM64 Termux only."
fi

install_termux_packages() {
  if ! command -v pkg >/dev/null 2>&1; then
    warn "pkg not found; skipping package installation"
    return 0
  fi

  local required_packages=(
    curl wget unzip p7zip tar which patchelf pulseaudio
  )
  local optional_packages=(
    mesa vulkan-loader-android termux-x11-nightly termux-x11
    virglrenderer-android sdl2 sdl2-jstest
  )

  log "Installing native Termux dependencies"
  pkg update -y
  pkg install -y x11-repo tur-repo
  pkg install -y "${required_packages[@]}"

  for package in "${optional_packages[@]}"; do
    pkg install -y "$package" >/dev/null 2>&1 || warn "Optional package unavailable: $package"
  done
}

install_launcher() {
  mkdir -p "$PREFIX/bin"
  install -m 0755 "$SCRIPT_DIR/scripts/launch-rpcs3.sh" "$PREFIX/bin/olympus-rpcs3"
  install -m 0755 "$SCRIPT_DIR/scripts/start-session.sh" "$PREFIX/bin/olympus-start"
  install -m 0755 "$SCRIPT_DIR/scripts/enable-gamepad.sh" "$PREFIX/bin/olympus-gamepad-fix"
  install -m 0755 "$SCRIPT_DIR/scripts/doctor.sh" "$PREFIX/bin/olympus-doctor"
}

main() {
  install_termux_packages
  install_launcher

  local url archive
  if ! url="$(pick_working_url)"; then
    cat >&2 <<'EOF'
[x] Could not auto-detect a current ARM64 RPCS3 package.
    Re-run with a direct download URL, for example:

    RPCS3_URL='https://example.com/rpcs3-arm64.AppImage' ./install.sh
EOF
    exit 1
  fi

  archive="$OLYMPUS_CACHE_DIR/$(basename "$url")"
  download_file "$url" "$archive"
  extract_rpcs3 "$archive"
  "$SCRIPT_DIR/scripts/enable-gamepad.sh"

  cat <<EOF

✅ Olympus native update is installed.

Run:
  termux-x11 :0 &
  am start --user 0 -n com.termux.x11/com.termux.x11.MainActivity
  olympus-rpcs3

EOF
}

main "$@"
