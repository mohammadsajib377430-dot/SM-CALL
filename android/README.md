# SM CALL Android app

This is the first native Android application foundation for SM CALL. It is separate from the existing GitHub Pages web prototype.

## App identity

- Android package name: `com.smcall.app`
- Language options in this starter: Bengali, English, Arabic, Hindi
- Minimum Android version: Android 7.0 (API 24)
- Build output: debug APK from GitHub Actions

## Included in this first milestone

- Native Android Activity and mobile layout.
- Four-language login labels and Arabic right-to-left layout.
- Firebase Phone Authentication flow using the Android Firebase Auth SDK.
- Saves the signed-in user's public profile and owner-only phone document using the existing Firestore collection names.
- Honest placeholders for chat, audio call, video call, and group video; those features are not implemented in this native app yet.

## Firebase setup required

The project intentionally builds without Firebase configuration so a debug UI APK can be produced first.

1. In Firebase Console, add an **Android app** to project `sm-call-bf23e`.
2. Enter this exact package name: `com.smcall.app`.
3. Download `google-services.json`.
4. Put the file at `android/app/google-services.json` with that exact name. This is Firebase client configuration, not an Admin SDK credential. It can be committed to the public repository if you want CI to build an APK with Firebase enabled. Never commit a Firebase Admin SDK service-account JSON file or private key.
5. Add the signing certificate fingerprints for the exact APK you will install. The Android build workflow prints the debug APK's SHA-1 and SHA-256 fingerprints in the step named **Print debug APK signing fingerprints**. Copy both values into Firebase Console → Project settings → Your apps → the `com.smcall.app` Android app → SHA certificate fingerprints.

The workflow's fingerprint belongs to the APK produced by that run. The downloaded `sm-call-debug-apk` artifact contains both `app-debug.apk` and `fingerprints.txt`; use the values in that text file for the same APK. If a later build prints a different fingerprint, add that fingerprint too before testing the newer APK.

When the JSON file is present, the Google Services Gradle plugin is applied automatically. Without it, the app displays a clear Firebase setup message instead of crashing.

Phone OTP delivery still depends on the Firebase project's phone sign-in provider, app verification, allowed SMS regions, quotas, and billing rules. Use Firebase's fictional test phone numbers for development where available; do not send real SMS until the project is configured and costs are understood.

## Build and verify

The GitHub Actions workflow `.github/workflows/android-build.yml` builds a debug APK with Gradle 9.6.0, prints the APK signing fingerprints, and uploads the APK as an artifact for 14 days.

- Open the repository's **Actions** tab.
- Open the latest **Build SM CALL Android** run.
- Confirm the **Build debug APK** and **Print debug APK signing fingerprints** steps succeeded.
- Copy the SHA-1 and SHA-256 values from the fingerprint step.
- Download the `sm-call-debug-apk` artifact from that same run. It contains `app-debug.apk` and `fingerprints.txt`.
- Check the run's **Report Firebase configuration mode** step. If it says `google-services.json` is absent, the APK is UI-only and Firebase Phone Authentication cannot initialize.

The APK from this workflow is for development testing only. It is not a signed Play Store release.

## Not yet included

- Native chat and group messaging screens.
- Audio/video calling or WebRTC.
- Group calls with up to 20 participants.
- Release signing and AAB generation.
- Physical-device verification.
