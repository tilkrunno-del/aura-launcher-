# AURA Launcher Edition (Open Source)

A calm, intent-first Android launcher inspired by the AURA OS concept.

- **Offline-first** command bar
- **No telemetry** by default
- Gesture UX: **Swipe ↓** for Command, **Swipe ↑** for Quick

## Features (v0.1)
- App search + launch
- Rule-based commands (Estonian):
  - `pane äratus 6:30`
  - `taimer 10 min`
  - `helista +372...`
  - `ava wifi` / `ava seaded`

## Build
Open the project in Android Studio and run **app**.

Or build an APK:

```bash
(From Android Studio: Build → Build Bundle(s)/APK(s) → Build APK(s))
```

APK:
`app/build/outputs/apk/debug/app-debug.apk`

## Install (Huawei EMUI 12)
- Install the APK.
- Set as default Home app:
  Settings → Apps → Default apps → Home app → **AURA Launcher**
- Recommended: disable battery optimization for the launcher.

## License
Apache-2.0. See `LICENSE`.
