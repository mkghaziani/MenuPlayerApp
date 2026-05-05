# 📺 MenuPlayer — Android TV Kiosk App for ERPNext

A production-ready Android kiosk app that fetches menu data from ERPNext and displays it full-screen on TVs. No browser, no address bar — pure native app.

---

## ✅ Features

| Feature | Detail |
|---|---|
| **Auto-start on boot** | Launches immediately when TV powers on |
| **Fullscreen kiosk** | Navigation bar hidden, immersive mode |
| **Image slideshow** | Glide-powered, configurable duration |
| **Video playback** | ExoPlayer, auto-advances after end |
| **Offline fallback** | Shows cached data when network drops |
| **Multi-screen** | Each TV has its own Screen ID |
| **PIN-protected settings** | Remote key sequence → PIN prompt |
| **Auto-refresh** | Polls ERPNext every N seconds (configurable) |
| **Foreground service** | Keeps app alive on memory-constrained TVs |

---

## 🏗️ Project Structure

```
MenuPlayerApp/
├── app/src/main/
│   ├── AndroidManifest.xml
│   └── java/com/menuplayer/
│       ├── MenuPlayerApp.kt          ← Application class
│       ├── SplashActivity.kt         ← Entry point / router
│       ├── SettingsActivity.kt       ← Server config (PIN-protected)
│       ├── PlayerActivity.kt         ← Main display controller
│       ├── api/
│       │   ├── ApiService.kt         ← Retrofit interface
│       │   └── RetrofitClient.kt     ← OkHttp + auth setup
│       ├── model/
│       │   └── Models.kt             ← MenuResponse, MenuItem, ScreenSettings
│       ├── receiver/
│       │   └── BootReceiver.kt       ← Auto-start on TV boot
│       └── utils/
│           ├── PrefsManager.kt       ← All device settings
│           ├── CacheManager.kt       ← JSON offline cache
│           ├── NetworkUtils.kt       ← Connectivity check
│           └── SyncForegroundService.kt ← Keep-alive service
├── erpnext_custom_method/
│   └── menu.py                       ← ERPNext API method
└── README.md
```

---

## ⚙️ Step 1 — ERPNext Setup

### Install the custom method

Copy `erpnext_custom_method/menu.py` to your ERPNext app:

```bash
cp menu.py ~/frappe-bench/apps/YOUR_APP/YOUR_APP/api/menu.py
```

Whitelist it by adding to `YOUR_APP/hooks.py` (or it's already whitelisted via `@frappe.whitelist`).

Restart the bench:
```bash
bench restart
```

### Test the API

```bash
curl "http://YOUR_SERVER/api/method/YOUR_APP.api.menu.get_screen_data?screen=screen-1"
```

Expected response:
```json
{
  "message": {
    "screen": "screen-1",
    "items": [
      {
        "name": "Zinger Burger",
        "price": 450,
        "image": "http://server/files/zinger.jpg",
        "video": "",
        "available": true,
        "category": "Burgers",
        "description": "Crispy chicken burger",
        "duration": 5
      }
    ],
    "settings": {
      "refresh_interval_sec": 30,
      "slide_duration_sec": 5,
      "currency_symbol": "Rs",
      "show_price": true
    }
  }
}
```

> **Note:** ERPNext wraps responses in `{ "message": ... }`. Update `MenuResponse` parsing if needed, or strip the wrapper in `RetrofitClient`.

### Create API Token (recommended)

1. ERPNext → Settings → Users → create `menu_player` user (System User, read-only)
2. API Access → Generate Keys
3. Copy `api_key:api_secret` — you'll paste this into the app settings

---

## 📱 Step 2 — Android App Setup

### Prerequisites

- Android Studio Hedgehog (2023.1) or newer
- JDK 17+
- Android device/TV with Android 5.0+ (API 21+)

### Configure & Build

1. Open the project in Android Studio
2. In `app/build.gradle`, update:
   ```groovy
   buildConfigField "String", "BASE_URL", '"http://YOUR-ERPNEXT-SERVER.com/"'
   buildConfigField "String", "DEFAULT_SCREEN_ID", '"screen-1"'
   ```
3. Build → Generate Signed APK (or use debug APK for testing)

### First Launch

On first launch the app opens **Settings screen**:

| Field | Example |
|---|---|
| Server URL | `http://192.168.1.100:8000` |
| API Token | `abc123:def456` |
| Screen ID | `screen-1` |
| Settings PIN | `1234` |

Tap **Save & Start Player** — the player launches immediately.

---

## 📺 Step 3 — Deploy to TVs

### Sideload via ADB

```bash
adb connect 192.168.1.101:5555       # TV's IP (enable ADB in TV developer settings)
adb install MenuPlayerApp-release.apk
adb shell am start -n com.menuplayer/.SplashActivity
```

### Multi-screen deployment

For each TV, before sideloading — edit `DEFAULT_SCREEN_ID` in `build.gradle`, or use build flavors:

```groovy
productFlavors {
    screen1 { buildConfigField "String", "DEFAULT_SCREEN_ID", '"screen-1"' }
    screen2 { buildConfigField "String", "DEFAULT_SCREEN_ID", '"screen-2"' }
    screen3 { buildConfigField "String", "DEFAULT_SCREEN_ID", '"screen-3"' }
}
```

Or simply configure each TV via the Settings screen (Screen ID field).

---

## 🔒 Kiosk Mode

The app:
- Hides system navigation bar (immersive sticky mode)
- Disables back button in PlayerActivity
- Keeps screen always on (`FLAG_KEEP_SCREEN_ON`)
- Re-applies fullscreen when focus is regained

### Access Settings on a running TV

Press this key sequence on the TV remote:  
**↑ ↑ ↓ ↓ ENTER**  

A PIN dialog appears. Enter your PIN (default: `1234`).

### Full device lock (Device Owner mode)

For maximum kiosk lockdown (disable home button, prevent app switching):
```bash
# One-time setup per device — device must have no accounts
adb shell dpm set-device-owner com.menuplayer/.receiver.DeviceAdminReceiver
```
This requires implementing `DeviceAdminReceiver` and `DevicePolicyManager` (advanced).

---

## 🔄 Auto-Refresh Logic

```
On startup:
  1. Load from cache (instant display)
  2. Fetch from ERPNext API
  3. Update cache + display

Every N seconds (default 30, override from server settings):
  → Fetch new data → update display live (no flicker)

On network failure:
  → Show "Offline" badge + continue showing cached data
```

---

## 🎨 Customization

| What | Where |
|---|---|
| Accent color | `res/values/colors.xml` → `accent` |
| Slide duration | `MenuItem.displayDurationSec` from API |
| Refresh interval | `ScreenSettings.refreshIntervalSec` from API |
| Price format | `PlayerActivity.kt` → `tvItemPrice.text` |
| Currency symbol | `ScreenSettings.currencySymbol` from API |

---

## 🐛 Troubleshooting

| Problem | Fix |
|---|---|
| App can't reach server | Check `network_security_config.xml` — add your server's IP/domain |
| `CLEARTEXT not permitted` | Add server IP to `network_security_config.xml` or use HTTPS |
| Blank screen | Check API token, verify `/api/method/...` returns data |
| Video not playing | Ensure ExoPlayer version matches `media3` in `build.gradle` |
| App doesn't auto-start | Enable "Autostart" in TV's app settings; check ADB `RECEIVE_BOOT_COMPLETED` |

---

## 📦 Dependencies Summary

| Library | Purpose | Version |
|---|---|---|
| Retrofit | HTTP client | 2.9.0 |
| OkHttp | Network layer + logging | 4.12.0 |
| Glide | Image loading + caching | 4.16.0 |
| ExoPlayer (media3) | Video playback | 1.2.1 |
| Gson | JSON parsing | 2.10.1 |
| Coroutines | Async networking | 1.7.3 |
| WorkManager | Background tasks | 2.9.0 |
