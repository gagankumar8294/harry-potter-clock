# ⚡ Harry Potter Custom Lock Screen Clock for Android

A custom, high-performance Harry Potter-themed analog clock application for Android 14 (compatible with Motorola Hello UI, Moto G64 5G, and stock Android devices). 

This project delivers a custom lock screen experience featuring an authentic Hogwarts astrolabe dial, Elder Wand & Wizard Wand clock hands, a Golden Snitch second hand, and a Deathly Hallows center medallion cap.

---

## 📸 Design & Features

- **Hogwarts Astrolabe Dial**: High-resolution custom background with Gryffindor, Slytherin, Hufflepuff, and Ravenclaw house emblems and Roman numerals (`XII`, `I`, `II`, etc.).
- **Elder Wand Hour Hand**: Real-time rotating Elder Wand asset scaled precisely for 12-hour rotation.
- **Wizard Wand Minute Hand**: Rotating magic wand minute hand asset scaled for 60-minute rotation.
- **Golden Snitch Second Hand**: Animated Golden Snitch second hand sweeping around the dial at 60 FPS.
- **Deathly Hallows Medallion**: Center cap anchoring the wand hands with the iconic Deathly Hallows symbol.
- **Dynamic Luminescence Engine**: +30% luminescence boost color matrix applied to defeat Android 14 lock screen wallpaper dimming.
- **Full-Screen Lock Takeover**: Utilizes Android 14 `USE_FULL_SCREEN_INTENT` and `showWhenLocked` flags to cover stock system clock digits cleanly on screen wake.
- **24/7 Foreground Service**: Persistent `ScreenWatcherService` listening for `ACTION_SCREEN_ON` and `ACTION_SCREEN_OFF` events with `BootReceiver` auto-start.

---

## 🏗️ Architecture & Android 14 Keyguard Layering

Android 14 keyguard security restricts third-party apps from mutating system keyguard UI directly. This application implements a dual-layer approach:

```
┌───────────────────────────────────────────────────────────┐
│ Layer 2 (Top): Full-Screen Activity Takeover             │
│ (HarryPotterLockActivity - covers stock Moto clock digits)│
├───────────────────────────────────────────────────────────┤
│ Layer 1 (Middle): SystemUI Keyguard                       │
│ (Stock Lock Screen: PIN/Fingerprint & System Status Bar)  │
├───────────────────────────────────────────────────────────┤
│ Layer 0 (Bottom): Live Wallpaper                          │
│ (HarryPotterWallpaperService - native animated engine)    │
└───────────────────────────────────────────────────────────┘
```

1. **`HarryPotterWallpaperService.kt`**: Extends `WallpaperService` for native lock screen live wallpaper rendering.
2. **`HarryPotterClockView.kt`**: Custom 60 FPS Canvas rendering engine handling vector mathematics, rotation angles, matrix math, date formatting, and asset scaling.
3. **`HarryPotterLockActivity.kt`**: Full-screen window takeover (`setShowWhenLocked(true)`, `setTurnScreenOn(true)`) preventing system clock digit overlaps.
4. **`ScreenWatcherService.kt`**: Background service monitoring display states to launch the takeover activity instantly when the screen is powered on.

---

## 🛠️ Required Permissions

The app relies on mandatory Android 14 permissions declared in `AndroidManifest.xml`:

- `android.permission.USE_FULL_SCREEN_INTENT`: Enables high-priority full-screen lock screen notifications.
- `android.permission.SYSTEM_ALERT_WINDOW`: Allows display over other apps.
- `android.permission.POST_NOTIFICATIONS`: Android 13+ notification permissions for foreground service.
- `android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: Prevents OS battery savers from killing the background monitor.
- `android.permission.SET_WALLPAPER`: Allows setting the live wallpaper directly from the app interface.
- `android.permission.RECEIVE_BOOT_COMPLETED`: Auto-launches the background service upon device restart.

---

## 🚀 Getting Started

### Prerequisites

- Android SDK 34 (Android 14)
- JDK 17 / Android Studio Hedgehog or newer
- Android Device (API level 26 / Android 8.0 or higher recommended)

### Build Instructions

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/gagankumar8294/harry-potter-clock.git
   cd harry-potter-clock
   ```

2. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Install via ADB**:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Grant App Permissions**:
   - Open **Harry Potter Clock** from your app drawer.
   - Tap **Set Live Wallpaper** and set it for **Home screen and lock screen**.
   - Tap **Enable Full-Screen Lock Takeover** to allow full-screen notifications.
   - Tap **Allow Notification Permission** and **Disable Battery Restrictions**.

---

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.
