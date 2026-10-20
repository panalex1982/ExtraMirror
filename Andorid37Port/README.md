# Extra Mirror: Android 17 / Jetpack Compose port

A port of the legacy Extra Mirror app (repository root, Eclipse/Java, API 9–19) to Kotlin and Jetpack Compose, targeting Android 17 (API 37).

- **[PERMISSIONS_AND_MIGRATION.md](PERMISSIONS_AND_MIGRATION.md):** what is no longer allowed, permissions, behaviour changes, Play Console checklist.
- **[PRIVACY_POLICY.md](PRIVACY_POLICY.md):** privacy policy to host and link from Google Play. The app also shows a summary of it.

## Stack

| | |
|---|---|
| Language / build | Kotlin 2.4.21, AGP 9.4 (built-in Kotlin), Gradle 9.6.1, version catalog |
| SDK | `compileSdk 37`, `targetSdk 37`, `minSdk 26` |
| UI | Jetpack Compose (BOM 2026.09.00), Material 3, edge-to-edge, predictive back |
| Camera | CameraX 1.6.2 + `camera-compose` (`CameraXViewfinder`) |
| Location | Platform `LocationManager` via `LocationManagerCompat` (GPS, foreground only) |
| State | ViewModel + `StateFlow` + `SavedStateHandle`, Preferences DataStore (migrates the legacy SharedPreferences) |
| Ads / consent | Google Mobile Ads SDK 25.4.0, User Messaging Platform 4.0.0 |

## Building

Open `Andorid37Port/` in a current Android Studio (with the API 37 SDK), or run:

```sh
cd Andorid37Port
./gradlew :app:assembleDebug        # build
./gradlew :app:testDebugUnitTest    # JVM unit tests (trip logic, unit formatting)
./gradlew :app:lintDebug            # lint
```

AdMob IDs and the privacy-policy URL are Gradle properties (see `gradle.properties`). The defaults are Google's **test** IDs. Put the real values in `~/.gradle/gradle.properties` so they stay out of git:

```properties
extramirror.admobAppId=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
extramirror.admobBannerId=ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ
extramirror.privacyPolicyUrl=https://your.site/extramirror/privacy
```

The `applicationId` is still `com.bue.extramirror` and `versionCode` is 12, so a release signed with the original key ships as an update to the existing Play listing.

## Where the legacy code went

| Legacy (Java) | Port (Kotlin / Compose) |
|---|---|
| `IntroActivity` + `res/drawable/intro_animation.xml` | `ui/intro/IntroScreen.kt` |
| `MirrorActivity` (layouts, toggles, menu, back dialog, brightness, fullscreen) | `ui/mirror/MirrorScreen.kt`, `ui/mirror/MirrorWindowEffects.kt` |
| `customviews/AnalogSpeedMeterView` | `ui/mirror/Speedometer.kt` (Compose `Canvas`, same geometry) |
| `customviews/CameraPreview` + `android.hardware.Camera` | `ui/camera/CameraMirror.kt` (CameraX, swipe-to-zoom kept) |
| `customviews/SettingsDialogFragment` | `ui/settings/SettingsDialog.kt` |
| `tasks/TimerRunnable` + motion logic in `MirrorActivity` | `trip/TripTracker.kt`, driven by `ui/MainViewModel.kt` |
| `LocationManager` listener in `MirrorActivity` | `location/GpsLocationSource.kt` (`Flow<GpsFix>`) |
| `utilities/ExtraMirrorSharedPreferences` (SharedPreferences) | `data/SettingsRepository.kt` (DataStore, same keys, auto-migration) |
| `utilities/Utilities`, `utilities/Clock` | `util/Units.kt` |
| `utilities/RuntimeSharedObjects` | removed (single activity) |
| AdMob 6.2.1 `AdView` | `ads/BannerAd.kt`, `ads/AdsConsentManager.kt` (UMP consent) |
| Google Analytics `EasyTracker` | removed (service shut down; see the migration doc) |
| new | `ui/permissions/AppPermissionsState.kt`, `ui/privacy/PrivacyPolicyScreen.kt` |
