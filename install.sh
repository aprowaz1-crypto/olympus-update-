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
  if [ "${HELIOS3_SKIP_PKG:-${OLYMPUS_SKIP_PKG:-0}}" = "1" ]; then
    warn "Skipping pkg dependency installation because HELIOS3_SKIP_PKG=1"
    return 0
  fi

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
  if ! pkg update -y; then
    cat >&2 <<'EOF'
[x] Termux package manager is currently broken or has no mirror selected.

Fix it first with:
  termux-change-repo
  apt update && apt full-upgrade -y

Then restart Termux and run:
  bash install.sh

If you already fixed the dependencies manually and only want to continue this installer:
  HELIOS3_SKIP_PKG=1 bash install.sh
EOF
    exit 1
  fi

  if ! pkg install -y x11-repo tur-repo; then
    warn "Could not enable one of the Termux repositories automatically"
  fi

  if ! pkg install -y "${required_packages[@]}"; then
    cat >&2 <<'EOF'
[x] Required Termux packages could not be installed.
Make sure the mirror is set correctly, then run:
  apt update && apt full-upgrade -y
  pkg install -y curl wget unzip p7zip tar which patchelf pulseaudio
EOF
    exit 1
  fi

  for package in "${optional_packages[@]}"; do
    pkg install -y "$package" >/dev/null 2>&1 || warn "Optional package unavailable: $package"
  done
}

install_launcher() {
  mkdir -p "$PREFIX/bin"
  install -m 0755 "$SCRIPT_DIR/scripts/launch-rpcs3.sh" "$PREFIX/bin/helios3-rpcs3"
  install -m 0755 "$SCRIPT_DIR/scripts/start-session.sh" "$PREFIX/bin/helios3-start"
  install -m 0755 "$SCRIPT_DIR/scripts/enable-gamepad.sh" "$PREFIX/bin/helios3-gamepad-fix"
  install -m 0755 "$SCRIPT_DIR/scripts/doctor.sh" "$PREFIX/bin/helios3-doctor"
  install -m 0755 "$SCRIPT_DIR/scripts/check-updates.sh" "$PREFIX/bin/helios3-check-updates"

  ln -sf "$PREFIX/bin/helios3-rpcs3" "$PREFIX/bin/olympus-rpcs3"
  ln -sf "$PREFIX/bin/helios3-start" "$PREFIX/bin/olympus-start"
  ln -sf "$PREFIX/bin/helios3-gamepad-fix" "$PREFIX/bin/olympus-gamepad-fix"
  ln -sf "$PREFIX/bin/helios3-doctor" "$PREFIX/bin/olympus-doctor"
  ln -sf "$PREFIX/bin/helios3-check-updates" "$PREFIX/bin/olympus-check-updates"
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

  archive="$HELIOS3_CACHE_DIR/$(basename "$url")"
  download_file "$url" "$archive"
  extract_rpcs3 "$archive"
  record_installed_version "$url"
  "$SCRIPT_DIR/scripts/enable-gamepad.sh"

  cat <<EOF

✅ Helios3 native update is installed.

Run:
  termux-x11 :0 &
  am start --user 0 -n com.termux.x11/com.termux.x11.MainActivity
  helios3-rpcs3

EOF
}

main "$@"
