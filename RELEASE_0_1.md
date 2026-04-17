# Olympus RPCS3 Port — 0.1 Readiness

This file tracks what is needed for the first public 0.1 preview.

## Included in 0.1

- native Termux ARM64 bootstrap path
- RPCS3 build download and update detection
- Android launcher app starter
- Android native surface bridge
- early gamepad forwarding and SDL profile setup
- diagnostics and recovery commands for broken Termux setups

## Still needed before calling 0.1 ready

- build and install the Android APK on-device
- verify first-launch flow on at least one real ARM64 phone
- verify that update popup text looks correct on device
- confirm controller mapping for at least one Xbox pad and one PlayStation pad
- make sure the current install/update path is stable after a fresh Termux setup

## Suggested 0.1 label

Olympus RPCS3 Port 0.1 Preview

## 0.1 focus

The goal of 0.1 is not full PS3 compatibility yet.
The goal is to prove:

- install flow
- launch flow
- update flow
- Android front end
- native porting direction
