#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/common.sh
source "$SCRIPT_DIR/common.sh"

ensure_dirs

installed_version="$(cat "$HELIOS3_CONFIG_DIR/installed-version.txt" 2>/dev/null || true)"
latest_url="$(pick_working_url 2>/dev/null || true)"
latest_version="$(basename "$latest_url")"

if [ -z "$latest_url" ] || [ "$latest_version" = "." ]; then
  die "Could not check for a new RPCS3 build right now."
fi

if [ -z "$installed_version" ]; then
  log "RPCS3 build available: $latest_version"
  log "Run bash install.sh to install it."
  exit 0
fi

if [ "$installed_version" = "$latest_version" ]; then
  log "RPCS3 is up to date: $installed_version"
else
  log "Update available"
  log "Installed: $installed_version"
  log "Latest:    $latest_version"
  log "Run bash install.sh to update."
fi
