# MediTime – APK Installation & Testing Guide

## 1. Generated APK Summary
- **File Name:** `MediTime-v1.0.apk`
- **Primary Location:** `release-apk/MediTime-v1.0.apk`
- **Gradle Build Output:** `app/build/outputs/apk/debug/app-debug.apk`
- **File Size:** ~23 MB
- **Package Name (Application ID):** `com.aistudio.meditime.kxmtpz`
- **Target SDK:** 36 (Android 15+)
- **Minimum SDK:** 24 (Android 7.0 / 8.0+)
- **Build Status:** Verified & Ready for direct physical device installation.

---

## 2. Included Permissions & System Integrations
- `POST_NOTIFICATIONS`: Runtime notification permission for timely dosage alerts.
- `USE_EXACT_ALARM` & `SCHEDULE_EXACT_ALARM`: High-precision alarms for exact-time medicine reminders.
- `RECEIVE_BOOT_COMPLETED`: Automatically restores and recalculates reminders when device reboots.
- `VIBRATE`: Vibration pattern for silent/audible reminders.
- `INTERNET` & `ACCESS_NETWORK_STATE`: General system compatibility.

---

## 3. How to Install on Your Physical Android Phone

### Method A: Download via AI Studio Interface
1. In the AI Studio file explorer on the left/top, open the folder `release-apk/` (or `app/build/outputs/apk/debug/`).
2. Right-click on **`MediTime-v1.0.apk`** (or `app-debug.apk`) and select **Download**.
3. Transfer the downloaded `.apk` file to your Android phone via:
   - USB Cable, OR
   - Google Drive / WhatsApp / Telegram saved messages.

### Method B: Install Directly on Phone
1. Open your phone's **Files / File Manager** app and tap on `MediTime-v1.0.apk`.
2. If prompted with *"For your security, your phone is not allowed to install unknown apps from this source"*:
   - Tap **Settings**.
   - Enable **Allow from this source**.
3. Tap **Install**.
4. Tap **Open** to launch **MediTime**.

---

## 4. First-Time Setup on Physical Device
1. **Notifications:** When prompted on first launch, tap **Allow** for Notifications.
2. **Exact Alarms:** For Android 12+ (API 31+), if required by your device OEM, open **Settings > Exact Alarms** in MediTime to verify exact alarm scheduling.
3. **Battery Optimization:** In phone settings under `Battery > Background usage limits / App power management`, set MediTime to **Unrestricted** to prevent deep sleep delays.

---

## 5. Keystore Guide for Future Play Store Release Signing
If you later plan to generate a signed Release APK for Google Play distribution:
1. Open a terminal or Android Studio command line:
   ```bash
   keytool -genkey -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
   ```
2. Enter your secure password when prompted.
3. To build with this keystore:
   ```bash
   export KEYSTORE_PATH="/path/to/my-upload-key.jks"
   export STORE_PASSWORD="your-secure-password"
   export KEY_PASSWORD="your-secure-password"
   gradle assembleRelease
   ```
