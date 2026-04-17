#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_DIR="$ROOT_DIR/third_party/rpcs3"

mkdir -p "$ROOT_DIR/third_party"

if [ -d "$TARGET_DIR/.git" ]; then
  echo "RPCS3 source already exists at $TARGET_DIR"
  exit 0
fi

git clone --depth 1 https://github.com/RPCS3/rpcs3.git "$TARGET_DIR"
echo "Upstream RPCS3 source imported into $TARGET_DIR"
