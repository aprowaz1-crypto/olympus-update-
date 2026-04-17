#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

: "${HOME:=$PWD}"
if [ -z "${PREFIX:-}" ]; then
  if [ -d /data/data/com.termux/files/usr ]; then
    PREFIX=/data/data/com.termux/files/usr
  else
    PREFIX="$HOME/.local"
  fi
fi

OLYMPUS_HOME="${OLYMPUS_HOME:-$PREFIX/opt/olympus}"
OLYMPUS_CONFIG_DIR="${OLYMPUS_CONFIG_DIR:-$HOME/.config/olympus}"
OLYMPUS_CACHE_DIR="${OLYMPUS_CACHE_DIR:-$HOME/.cache/olympus}"

log() {
  printf '[*] %s\n' "$*"
}

warn() {
  printf '[!] %s\n' "$*" >&2
}

die() {
  printf '[x] %s\n' "$*" >&2
  exit 1
}

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "Missing required command: $1"
}

have_working_curl() {
  command -v curl >/dev/null 2>&1 && curl --version >/dev/null 2>&1
}

ensure_dirs() {
  mkdir -p "$OLYMPUS_HOME" "$OLYMPUS_CONFIG_DIR" "$OLYMPUS_CACHE_DIR"
}

fetch_latest_asset_url() {
  need_cmd python3
  python3 - <<'PY'
import json, os, re, urllib.request
repo = os.environ.get('RPCS3_BINARY_REPO', 'RPCS3/rpcs3-binaries-linux-arm64')
headers = {'User-Agent': 'olympus-native-installer'}

try:
    api_url = f'https://api.github.com/repos/{repo}/releases/latest'
    req = urllib.request.Request(api_url, headers=headers)
    with urllib.request.urlopen(req, timeout=20) as r:
        data = json.load(r)
    for asset in data.get('assets', []):
        name = asset.get('name', '').lower()
        if any(key in name for key in ('aarch64', 'arm64')) and asset.get('browser_download_url'):
            print(asset['browser_download_url'])
            raise SystemExit(0)
except Exception:
    pass

latest_page = f'https://github.com/{repo}/releases/latest'
req = urllib.request.Request(latest_page, headers=headers)
with urllib.request.urlopen(req, timeout=20) as r:
    html = r.read().decode('utf-8', 'ignore')

match = re.search(r'/releases/expanded_assets/([^"?#]+)', html)
if not match:
    raise SystemExit(1)

expanded_url = f'https://github.com/{repo}/releases/expanded_assets/{match.group(1)}'
req = urllib.request.Request(expanded_url, headers=headers)
with urllib.request.urlopen(req, timeout=20) as r:
    assets_html = r.read().decode('utf-8', 'ignore')

for asset_path in re.findall(r'href="([^"]+)"', assets_html):
    lowered = asset_path.lower()
    if '/releases/download/' in lowered and any(key in lowered for key in ('aarch64', 'arm64', 'appimage')):
        print('https://github.com' + asset_path)
        raise SystemExit(0)

raise SystemExit(1)
PY
}

resolve_rpcs3_url() {
  if [ -n "${RPCS3_URL:-}" ]; then
    printf '%s\n' "$RPCS3_URL"
    return 0
  fi

  local official_url
  official_url="$(fetch_latest_asset_url 2>/dev/null || true)"
  if [ -n "$official_url" ]; then
    printf '%s\n' "$official_url"
    return 0
  fi

  cat <<'EOF'
https://github.com/RPCS3/rpcs3-binaries-linux-arm64/releases/latest/download/rpcs3-latest-linux_aarch64.AppImage
EOF
}

pick_working_url() {
  while IFS= read -r url; do
    [ -n "$url" ] || continue

    if have_working_curl; then
      if curl -fsIL --retry 2 --connect-timeout 10 "$url" >/dev/null 2>&1; then
        printf '%s\n' "$url"
        return 0
      fi
    elif command -v python3 >/dev/null 2>&1; then
      if python3 - "$url" <<'PY'
import sys, urllib.request
url = sys.argv[1]
req = urllib.request.Request(url, method='HEAD', headers={'User-Agent': 'olympus'})
with urllib.request.urlopen(req, timeout=15) as r:
    code = getattr(r, 'status', 200)
raise SystemExit(0 if 200 <= code < 400 else 1)
PY
      then
        printf '%s\n' "$url"
        return 0
      fi
    fi
  done < <(resolve_rpcs3_url)
  return 1
}

download_file() {
  local url="$1"
  local out="$2"
  log "Downloading $(basename "$out")"

  if have_working_curl; then
    curl -fL --retry 3 --connect-timeout 15 "$url" -o "$out"
    return 0
  fi

  need_cmd python3
  python3 - "$url" "$out" <<'PY'
import sys, urllib.request
url, out = sys.argv[1], sys.argv[2]
req = urllib.request.Request(url, headers={'User-Agent': 'olympus'})
with urllib.request.urlopen(req, timeout=60) as r, open(out, 'wb') as f:
    while True:
        chunk = r.read(1024 * 1024)
        if not chunk:
            break
        f.write(chunk)
PY
}

record_installed_version() {
  local url="$1"
  local version
  version="$(basename "$url")"
  printf '%s\n' "$url" > "$OLYMPUS_CONFIG_DIR/installed-url.txt"
  printf '%s\n' "$version" > "$OLYMPUS_CONFIG_DIR/installed-version.txt"
}

extract_rpcs3() {
  local archive="$1"
  rm -rf "$OLYMPUS_HOME/rpcs3" "$OLYMPUS_HOME/AppDir"
  mkdir -p "$OLYMPUS_HOME/rpcs3"

  case "$archive" in
    *.AppImage)
      chmod +x "$archive"
      cp "$archive" "$OLYMPUS_HOME/rpcs3.AppImage"
      ;;
    *.tar.gz|*.tgz)
      tar -xzf "$archive" -C "$OLYMPUS_HOME/rpcs3" --strip-components=1
      ;;
    *.zip)
      unzip -o "$archive" -d "$OLYMPUS_HOME/rpcs3"
      ;;
    *)
      die "Unsupported package format: $archive"
      ;;
  esac
}
