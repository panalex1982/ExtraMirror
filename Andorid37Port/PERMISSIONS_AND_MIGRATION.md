# Extra Mirror: Android 17 (API 37) port, permissions and what's no longer allowed

The legacy app (`/src`, `/res`, `AndroidManifest.xml` at the repository root) is an Eclipse/ADT project from 2013–2014: `minSdk 9`, `targetSdk 19`, Java, `android.hardware.Camera`, AdMob SDK 6.2.1 and Google Analytics v3 jars. This document lists everything that changed for the port in `Andorid37Port/` (Kotlin + Jetpack Compose, `compileSdk`/`targetSdk 37`, `minSdk 26`), with the emphasis on **what the old app did that Android or Google Play no longer allows**.

---

## 1. No longer allowed, or no longer works

| # | What the legacy app did | Why it no longer works | What the port does |
|---|---|---|---|
| 1 | **Got CAMERA and ACCESS_FINE_LOCATION at install time** (`targetSdk 19`). | Runtime permissions apply since Android 6 (API 23). **Android 14+ refuses to install any app with `targetSdk < 23`**, so the current APK can't be installed on new phones at all. Google Play also requires a recent `targetSdk` for updates. | Both permissions are requested at runtime, after a one-time explanation dialog. If one is denied, the app keeps working without that feature and shows an "Allow / Open settings" chip. State is re-checked on every resume, because Android 11+ has "Only this time" grants and auto-reset of unused apps' permissions. |
| 2 | **Kept receiving GPS in the background.** The location listener was only removed in `onDestroy()`, so the trip kept counting after you left the app. | Android 8 throttles background location to a few fixes per hour. **Android 10+ delivers no location to a backgrounded app** unless it holds `ACCESS_BACKGROUND_LOCATION`, which Google Play only approves after a policy review when background location is the app's core feature, or runs a **foreground service of type `location`**. That service needs the `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_LOCATION` permissions (Android 14+), a persistent notification and a Play Console foreground-service declaration with a video. | Location and the trip clock run **only while the mirror screen is visible**, so the timers pause in the background. No background-location permission is requested. If you want trips to keep counting while e.g. a navigation app is in front, the compliant route is a `location` foreground service. That isn't implemented; it changes the privacy policy and Play declarations. |
| 3 | Asked only for `ACCESS_FINE_LOCATION`. | **Android 12+**: an app targeting API 31+ must request `ACCESS_COARSE_LOCATION` together with FINE, or the request is ignored. The user may also grant **approximate location only**, which gives no usable speed. | Both are declared and requested together. With approximate only, the speedometer shows "Speed needs precise location", and tapping it asks the user to upgrade. GPS is only started when precise location is granted. |
| 4 | **Locked the screen orientation** (`setRequestedOrientation(...)` with the rotation at launch). | Since apps targeting **Android 16** (API 36), on displays with smallest width ≥ 600dp (tablets, unfolded foldables, desktop windows), `screenOrientation`, `setRequestedOrientation()`, `resizeableActivity="false"` and min/max aspect ratio are **ignored**. API 36 still had an opt-out property (`PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY`). **When targeting API 37 there is no opt-out.** | Orientation is still locked (`SCREEN_ORIENTATION_LOCKED`) while the mirror screen is shown; this keeps working on phones. On large screens Android ignores it, so the Compose layout adapts to any size and aspect ratio (portrait and landscape arrangements, safe-area padding). |
| 5 | Overrode **`onBackPressed()`** to show "Close Extra Mirror! Are you sure?". | For apps targeting **Android 16+**, predictive back is on: **`onBackPressed()` is no longer called** and `KEYCODE_BACK` is not dispatched. | Compose `BackHandler` shows the same confirmation dialog. |
| 6 | Full screen through **`FLAG_FULLSCREEN`**, plus non-edge-to-edge windows. | `FLAG_FULLSCREEN` is deprecated (API 30). Apps targeting API 35+ are always drawn edge-to-edge, and the opt-out (`windowOptOutEdgeToEdgeEnforcement`) is disabled when targeting API 36+. | `enableEdgeToEdge()`. The mirror screen hides the system bars with `WindowInsetsControllerCompat` (swipe to reveal) and keeps its controls inside the safe drawing area (display cutouts). |
| 7 | **AdMob SDK 6.2.1** jar (`com.google.ads.*`, `new AdView(this, AdSize.BANNER, key)`). | That SDK was retired years ago and no longer serves ads. The current Google Mobile Ads SDK **crashes at startup without the AdMob App ID `<meta-data>`** in the manifest. | Google Mobile Ads SDK **25.4.0** (`play-services-ads`), initialized off the main thread. App ID and banner unit ID come from Gradle properties; the defaults are Google's test IDs. |
| 8 | Requested ads **without asking for consent**. | Google's EU User Consent Policy requires publishers serving ads to users in the **EEA, UK and Switzerland** to use a **Google-certified consent management platform** (enforced for the EEA and UK since January 2024, and for Switzerland since mid-2024). US state privacy laws require an opt-out of "selling/sharing" for targeted ads. | Google **User Messaging Platform 4.0.0**. Consent is gathered on every launch before any ad request, and a **"Privacy choices (ads)"** entry appears in the menu, in Settings and on the privacy screen whenever UMP says it is required. You must also create the GDPR and US-states messages in the AdMob console (*Privacy & messaging*). |
| 9 | **Google Analytics v3** (`EasyTracker`, `libGoogleAnalyticsServices.jar`). | Google shut it down: Universal Analytics stopped processing data on 1 July 2023, and the Android "Google Analytics Services SDK" it relied on was discontinued before that. It no longer collects anything. | **Removed.** If you want analytics, the replacement is Google Analytics for Firebase. It needs your `google-services.json`, an update to the privacy policy and Data safety form, and consent integration (UMP → Consent Mode). It isn't added because it can't be configured without your Firebase project. |
| 10 | `android:allowBackup="false"` as the only backup setting. | On **Android 12+** (target 31+), `allowBackup="false"` **no longer prevents device-to-device transfers**. | Kept `allowBackup="false"` and added `dataExtractionRules`, which exclude everything from both cloud backup and device transfer. |
| 11 | Launcher activity without `android:exported`. | Since **Android 12**, any component with an intent filter must declare `android:exported`, or the APK fails to install. | `android:exported="true"` on the single `MainActivity`. |
| 12 | Read the advertising ID implicitly. | Apps targeting **Android 13+** must declare `com.google.android.gms.permission.AD_ID`, or they receive a zeroed ID. Play Console requires an Advertising ID declaration. | Declared explicitly (the Ads SDK also merges it). See the Play checklist below. |
| 13 | Hardware features implied by permissions. | Since `targetSdk 21`, Play doesn't infer the GPS feature from location permissions. `CAMERA` still implies a *required* back camera and autofocus unless declared otherwise. | `camera`, `camera.any`, `camera.front`, `camera.autofocus` and `location.gps` are declared with `required="false"`, so devices without them can still install the app, as before. |
| 14 | `RuntimeSharedObjects.closeApplication` static flag to close two activities. | Not disallowed, but fragile across process death. | Single activity; "Exit" calls `finish()`. |

### Deprecated APIs that were replaced (still technically allowed)

- `android.hardware.Camera` (deprecated since API 21) → **CameraX 1.6.2** with the Compose `CameraXViewfinder`. CameraX handles rotation, aspect ratio and front-camera mirroring; the legacy rotation workaround is gone.
- `android-support-v4.jar` → **AndroidX**.
- `SharedPreferences` → **Preferences DataStore**, with an automatic one-time migration of the old `ExtraMirrorSharedPreferences` file, so updating users keep their settings.
- `android.text.format.Time` and `Display.getWidth()` → `java.time` and Compose layout.
- `android:installLocation="preferExternal"` was dropped (internal storage is the default; external installs mainly matter on very old low-storage devices).
- The `Keys.java` file (git-ignored, held the AdMob key) → Gradle properties (`extramirror.admobAppId`, `extramirror.admobBannerId`).

---

## 2. Permissions used by the port

| Permission | Kind | Requested | Purpose |
|---|---|---|---|
| `android.permission.CAMERA` | Runtime | On the mirror screen, after the one-time explanation, or from the "Allow" chip | Live mirror / road view, only while visible. Nothing recorded. |
| `android.permission.ACCESS_FINE_LOCATION` | Runtime | Same | GPS speed, distance, altitude, moving/standing time, only while visible. |
| `android.permission.ACCESS_COARSE_LOCATION` | Runtime | Together with FINE (mandatory on Android 12+) | Lets the user choose approximate location. |
| `android.permission.INTERNET` | Normal | Install time | Ads and the consent form. |
| `android.permission.ACCESS_NETWORK_STATE` | Normal | Install time | Ads SDK connectivity checks. |
| `com.google.android.gms.permission.AD_ID` | Normal | Install time | Advertising ID for AdMob (Android 13+). |

**Not requested:** `ACCESS_BACKGROUND_LOCATION`, `FOREGROUND_SERVICE*`, `POST_NOTIFICATIONS`, `WAKE_LOCK` (keep-screen-on uses the window flag instead), `WRITE_SETTINGS` (brightness is per-window), storage or media, microphone, Bluetooth, `ACCESS_LOCAL_NETWORK`.

**Check the merged manifest before release.** Google's SDKs can merge more entries (for example Ads-related `<property>`/`<uses-permission>` items, depending on the SDK version). Check *Android Studio → AndroidManifest.xml → Merged Manifest*, and make sure the privacy policy and Data safety form match what you see.

---

## 3. Android 17 (API 37) changes that were checked

| API 37 change | Affects Extra Mirror? |
|---|---|
| **Large screens: orientation/resizability/aspect-ratio restrictions ignored, opt-out removed** | **Yes.** See item 4 above; the layout is adaptive. |
| `ACCESS_LOCAL_NETWORK` runtime permission for LAN access | No. Ads use the public internet, and nothing talks to local devices. |
| Background audio hardening | No. The app plays no audio. |
| Certificate Transparency enabled by default | No action needed. Only Google's SDKs make network calls. |
| Cleartext traffic (`usesCleartextTraffic` being deprecated) | No. No cleartext traffic, and the attribute isn't set. |
| SMS OTP delay, Contacts Provider restrictions, contact picker | No. Not used. |
| `IntentSender` background-activity-launch hardening | No. Not used. |
| Lock-free `MessageQueue`, unmodifiable `static final` fields via reflection | No. No reflection on platform internals. |
| Safer native code loading (`System.load` read-only) | No. No native code. |
| Per-app keystore key limit | No. The keystore isn't used. |

Inherited from Android 15/16 when targeting 37: edge-to-edge enforcement (item 6), predictive back (item 5), and the large-screen orientation rules (item 4).

---

## 4. Behaviour differences you will notice

- **Trip timers pause while the app isn't visible** (item 2). The trip survives rotation and Android closing the app in the background (`SavedStateHandle`), as the legacy `onSaveInstanceState` did.
- **"Moving" starts at 0.5 m/s (about 1.8 km/h)** instead of any non-zero GPS speed, so GPS jitter while parked isn't counted as driving time or distance.
- **Altitude** uses mean-sea-level altitude on Android 14+ when the GPS provides it (previously the raw ellipsoid height, which can be tens of meters off). It shows "—" until the first fix instead of "0 m".
- **"Enable camera"** applies immediately. The legacy app needed a restart.
- **Legacy bugs fixed:**
  - In imperial mode the analog gauge drew its digital speed off-screen (`y = -radius/10`).
  - In digital mode the distance overlapped the timers once it had four or more characters.
  - The digital "Km/h" label overlapped the speed digits.
- The traffic-light icons are drawn as vectors (sharp at any size) instead of the 15×39 px bitmaps.
- The app uses a dark theme with a black background, which is better at night and over the camera picture.
- New screens and dialogs: a one-time **camera and location explanation** before the system prompts, status chips (location off, approximate only, waiting for GPS, permission needed), and an **in-app privacy policy** with a link to the full policy.

---

## 5. Before publishing: Google Play Console checklist

1. **Privacy policy URL.** Host `PRIVACY_POLICY.md` at a public, non-PDF, non-geofenced URL and enter it in *Policy → App content → Privacy policy*. Put the same URL in `extramirror.privacyPolicyUrl` (the in-app link). The default points to this file on GitHub, which only works if the repository is public.
2. **Replace `<CONTACT EMAIL>`** in `PRIVACY_POLICY.md`.
3. **AdMob IDs.** Put your real App ID and banner unit ID in `~/.gradle/gradle.properties` (keys listed in `gradle.properties`). Debug builds with the defaults show Google test ads only.
4. **AdMob → Privacy & messaging.** Create and publish the **European regulations (GDPR)** message and, if you serve US users, the **US state regulations** message. Otherwise UMP has nothing to show.
5. **Data safety form.** Suggested answers, based on Google's published guidance for the Google Mobile Ads SDK; check them against the current AdMob Data safety guidance:
   - Data collected and shared with Google for **Advertising or marketing**, **Analytics** (ad measurement) and **Fraud prevention**: **Device or other IDs** (advertising ID), **Approximate location** (from IP), **App interactions**, **Crash logs** and **Diagnostics**.
   - **Precise location** and **camera images** are processed only on the device and are not sent anywhere. Under Play's definitions that is not "collection", so don't declare them as collected.
   - Data encrypted in transit: **Yes**. Users can request deletion: the app holds no data; point to Google's controls.
6. **Advertising ID declaration:** *Yes*, used for advertising.
7. **Ads:** *Contains ads: Yes*.
8. **Target audience:** adults or general audience, **not** children. Otherwise Families policy and child-directed ad settings apply.
9. **No** background-location or foreground-service declarations are needed (neither is used).
10. **Ship as an update:** same `applicationId` (`com.bue.extramirror`), higher `versionCode` (12 > 11), and sign with the **same key** as the existing Play listing (or your Play App Signing upload key).

---

## 6. Build and verification status

The cloud environment used for this port couldn't reach `dl.google.com` (Google's Maven repository and Android SDK downloads), so **the Android build itself (AGP, `assembleDebug`, lint) could not be run here.** What was checked:

- **Unit tests (12) passed.** They cover the trip logic (`TripTracker`) and unit/format helpers (`Units`), compiled with Kotlin 2.4.21 from the real sources in `app/src/main` and `app/src/test`.
- **The speedometer composable (`Speedometer.kt`) compiled with the Compose compiler** (Kotlin 2.4.21, against Compose Desktop) and was rendered to images in analog, digital, metric and imperial modes. The renders looked right except for two overlaps in the digital layout, and those were fixed.
- **Every Kotlin file was parsed** with no syntax errors.
- The newer AndroidX APIs used (`ProcessCameraProvider.awaitInstance`, `CameraXViewfinder`, `AndroidViewModelFactory.APPLICATION_KEY`) were checked against the AndroidX sources.

**Still to do on your machine:** open `Andorid37Port/` in Android Studio, run `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`, and test on an Android 17 phone and on a tablet or foldable emulator (for the large-screen behaviour).
