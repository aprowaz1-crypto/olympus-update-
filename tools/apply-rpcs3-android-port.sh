#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RPCS3_DIR="$ROOT_DIR/third_party/rpcs3"
PATCH_DIR="$ROOT_DIR/android-port-overlay"

if [ ! -d "$RPCS3_DIR" ]; then
  echo "Missing upstream source at $RPCS3_DIR"
  echo "Run tools/import-rpcs3.sh first"
  exit 1
fi

mkdir -p "$PATCH_DIR"

echo "Android port overlay directory: $PATCH_DIR"
echo "Place Android-specific RPCS3 patch files here and integrate them through the NDK build."
