# Extra Mirror – Privacy Policy

**Effective date:** 20 October 2026
**App:** Extra Mirror for Android (package `com.bue.extramirror`), version 2.0.0 and later
**Developer:** Panagiotis Alexandropoulos
**Contact:** `<CONTACT EMAIL>` <!-- TODO: replace with a real, monitored address before publishing -->

Extra Mirror turns your phone into a car dashboard companion. It shows a live camera view (the road ahead, or behind you like a mirror) together with a GPS speedometer, trip distance, altitude, and moving/standing timers.

This policy explains what data the app uses, why it uses it, and where that data goes. In short:

- Extra Mirror has **no accounts and no servers**. The developer does **not** receive any of your data.
- **Camera** and **location** are processed **only on your device**, and only while the app is open on screen. Nothing from them is recorded, stored, or sent anywhere.
- The app shows **ads from Google AdMob**. Google can collect some device data to serve those ads, as described in [section 5](#5-advertising-google-admob).

---

## 1. Summary

| Data | Used for | Leaves your device? | Stored? |
|---|---|---|---|
| Camera picture | Live mirror / road view on screen | **No** | **No.** Never recorded or saved. |
| Precise location (GPS) | Speed, trip distance, altitude, moving/standing time | **No** | **No.** Coordinates are never stored. Only the trip counters (distance and durations) are kept while the app is running. |
| Your settings | Remembering units, brightness, speedometer style, camera choice | **No** | On your device only. Excluded from backups and device transfers. |
| Advertising ID, IP address, device and ad-interaction data | Serving and measuring ads (Google AdMob) | **Yes**, sent to Google by the Google Mobile Ads SDK | By Google, under Google's policies |

The app contains **no analytics or tracking SDK** of its own. Earlier versions included Google Analytics; it has been removed.

## 2. Camera

- **Permission:** `android.permission.CAMERA` (you are asked at runtime).
- **Purpose:** to show the live picture from the back camera ("Front" view, the road ahead) or the front/selfie camera ("Mirror" view, behind you).
- **Processing:** frames are displayed on screen and processed only in your device's memory, for example to zoom when you swipe. **No photos or videos are taken, saved, or transmitted.**
- **When:** only while Extra Mirror is open on screen. The camera is released as soon as the app goes to the background. Android also shows its camera-in-use indicator.
- **Your control:** you can turn the camera off in *Settings → Enable camera*, or revoke the permission in Android settings. The speedometer keeps working without the camera.

## 3. Location

- **Permissions:** `android.permission.ACCESS_FINE_LOCATION` and `android.permission.ACCESS_COARSE_LOCATION` (you are asked at runtime). Since Android 12 you can choose to grant only *approximate* location. The speedometer needs *precise* location, so with approximate location the app shows no speed, and tells you so.
- **Purpose:** to calculate your current speed, trip distance, altitude, and moving/standing time from GPS fixes.
- **Processing:** location comes from the device's GPS through Android's own `LocationManager`. **It does not go through Google Play services' location APIs, it is not sent to the developer or to any third party, and coordinates are never written to storage.** Only the derived trip counters (distance and two durations) are kept in memory, and in Android's saved app state, so the trip survives if Android briefly closes the app.
- **When:** only while Extra Mirror is open on screen. **The app does not request background location** (`ACCESS_BACKGROUND_LOCATION`) and does not run location in the background. The trip timers pause while the app is not visible.
- **Your control:** revoke or downgrade the permission at any time in *Android Settings → Apps → Extra Mirror → Permissions → Location*.

The Google Mobile Ads SDK may infer an *approximate* location from your IP address. It does not use the app's GPS location. See [section 5](#5-advertising-google-admob).

## 4. Data stored on your device

The app stores your preferences on your device: measure units, screen brightness, keep-screen-on, speedometer style, whether to show the zoom instructions, whether the camera is enabled, the last camera used, and whether the permission explanation was shown.

- This data is **not** sent anywhere.
- It is **excluded from Android cloud backup and device-to-device transfer.**
- It is deleted when you uninstall the app or clear its storage (*Android Settings → Apps → Extra Mirror → Storage → Clear storage*).

## 5. Advertising (Google AdMob)

Extra Mirror is free and shows banner ads provided by **Google AdMob** through the **Google Mobile Ads SDK**. Consent is gathered with Google's **User Messaging Platform (UMP)**.

**What Google may collect.** To serve, measure, and protect ads, and to personalize them where you have allowed it, the Google Mobile Ads SDK may collect and send to Google:

- your device's **advertising ID** (permission `com.google.android.gms.permission.AD_ID`), or another device identifier where the advertising ID is unavailable;
- your **IP address**, which can be used to estimate an *approximate* location;
- **device and app information**, such as device model, operating system version, language, and app version;
- **ad interactions**, such as ad views and taps, and **diagnostics**, such as crash and performance data.

The SDK uses the normal permissions `android.permission.INTERNET` and `android.permission.ACCESS_NETWORK_STATE` to load ads. The data is sent encrypted (HTTPS).

**Consent and your choices.**

- **EEA, UK, Switzerland:** before any ad is requested, you see Google's consent form (a Google-certified consent management platform) and can accept or refuse personalized advertising and other purposes. If you refuse, Google may still show non-personalized or limited ads.
- **US states with privacy laws** (for example California): where configured, Google's consent form lets you opt out of the "sale" or "sharing" of personal information for targeted advertising.
- You can change your choice at any time from **Privacy choices (ads)** in the app's menu, in Settings, or on the in-app privacy screen. The entry appears whenever Google's rules require it in your region.
- On Android 12 and later you can **delete your advertising ID** in *Android Settings → Privacy → Ads*. On older versions you can reset it or opt out of ads personalization there.

**More information:**

- How Google uses information from apps that use its services: <https://policies.google.com/technologies/partner-sites>
- Google Privacy Policy: <https://policies.google.com/privacy>
- Google's advertising controls: <https://myadcenter.google.com/>

The developer does not receive personal data from AdMob. The developer only sees aggregated reports, such as the number of ad impressions.

## 6. Permissions overview

| Permission | Type | Why |
|---|---|---|
| `CAMERA` | Runtime (you are asked) | Live mirror / road view. Only while the app is visible. |
| `ACCESS_FINE_LOCATION` | Runtime (you are asked) | Precise GPS speed, distance, and altitude. Only while the app is visible. |
| `ACCESS_COARSE_LOCATION` | Runtime (you are asked) | Required by Android 12+ alongside the precise-location request. Lets you choose approximate location. |
| `INTERNET` | Normal (granted at install) | Loading ads and the consent form. |
| `ACCESS_NETWORK_STATE` | Normal (granted at install) | Used by the Ads SDK to check connectivity before loading an ad. |
| `com.google.android.gms.permission.AD_ID` | Normal (granted at install) | Lets the Ads SDK read the advertising ID. You can delete the ID in Android settings. |

Extra Mirror does **not** request background location, contacts, storage/media, microphone, phone, SMS, notifications, Bluetooth, or local-network permissions.

## 7. Children

Extra Mirror is a driving companion. It is **not directed at children under 13** (or the equivalent minimum age in your country), and the developer does not knowingly collect data from children. Because the developer collects no personal data at all, there is nothing for the developer to delete. Questions about ad data can be sent to the contact address above, or to Google using the links in section 5.

## 8. Data retention and deletion

- **Developer:** holds no personal data about you, so there is nothing to retain or delete.
- **On your device:** clear the app's storage or uninstall the app to delete all locally stored settings.
- **Google (ads):** retention is governed by Google's policies. You can delete your advertising ID and manage ad settings as described in section 5.

## 9. Your rights

Depending on where you live (for example under the EU/UK GDPR or US state privacy laws), you may have rights to access, correct, delete, or object to the processing of your personal data, and to withdraw consent. Because the app does not send personal data to the developer, requests about advertising data are best directed to Google (section 5). You can withdraw ads consent at any time from **Privacy choices (ads)** in the app. For any other question, use the contact address at the top of this policy. You also have the right to lodge a complaint with your local data protection authority.

## 10. Security

All camera and location processing happens on your device, inside Android's app sandbox. The app has no backend. The Ads and consent SDKs communicate with Google over encrypted connections. The app does not use cleartext (unencrypted HTTP) traffic.

## 11. Safety notice

Do not interact with the app while driving. Set it up before you start moving, and always follow local laws on phone use and phone mounting in vehicles. Extra Mirror does not replace your vehicle's mirrors or speedometer.

## 12. Changes to this policy

If this policy changes, the new version will be published at the same address with a new effective date. Significant changes, such as a new kind of data being collected, will also be announced in the app's release notes.

## 13. Contact

Questions or requests about this policy: `<CONTACT EMAIL>`
