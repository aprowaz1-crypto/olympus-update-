#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/common.sh
source "$SCRIPT_DIR/common.sh"

ensure_dirs

cat > "$OLYMPUS_CONFIG_DIR/gamepad.env" <<'EOF'
# Olympus native gamepad tuning for Termux:X11
export SDL_JOYSTICK_HIDAPI=0
export SDL_GAMECONTROLLER_USE_BUTTON_LABELS=0
# Add custom mappings below if one of your pads is not detected correctly.
# export SDL_GAMECONTROLLERCONFIG='your_mapping_here'
EOF

cat > "$OLYMPUS_CONFIG_DIR/gamecontrollerdb.txt" <<'EOF'
# Minimal SDL controller mappings. You can append more from SDL_GameControllerDB.
030000005e0400008e02000014010000,Xbox Wireless Controller,a:b0,b:b1,back:b6,dpdown:h0.4,dpleft:h0.8,dpright:h0.2,dpup:h0.1,guide:b8,leftshoulder:b4,leftstick:b9,lefttrigger:a4,leftx:a0,lefty:a1,rightshoulder:b5,rightstick:b10,righttrigger:a5,rightx:a2,righty:a3,start:b7,x:b2,y:b3,
050000004c050000cc09000000010000,PS5 Controller,a:b1,b:b2,back:b8,dpdown:h0.4,dpleft:h0.8,dpright:h0.2,dpup:h0.1,guide:b12,leftshoulder:b4,leftstick:b10,lefttrigger:a4,leftx:a0,lefty:a1,rightshoulder:b5,rightstick:b11,righttrigger:a5,rightx:a2,righty:a3,start:b9,x:b0,y:b3,
050000004c050000c405000000010000,PS4 Controller,a:b1,b:b2,back:b8,dpdown:h0.4,dpleft:h0.8,dpright:h0.2,dpup:h0.1,guide:b12,leftshoulder:b4,leftstick:b10,lefttrigger:a3,leftx:a0,lefty:a1,rightshoulder:b5,rightstick:b11,righttrigger:a4,rightx:a2,righty:a5,start:b9,x:b0,y:b3,
EOF

log "Gamepad profile written to $OLYMPUS_CONFIG_DIR"
log "If detection still fails, pair the controller before launching Termux:X11 and test with: sdl2-jstest --list"
