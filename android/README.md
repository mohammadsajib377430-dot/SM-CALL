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

To connect Firebase:

1. In Firebase Console, add an **Android app** to project `sm-call-bf23e`.
2. Enter this exact package name: `com.smcall.app`.
3. Download `google-services.json`.
4. Put the file at `android/app/google-services.json`. Do not rename it or commit it to GitHub.
5. Add the Android app's SHA-256 signing certificate fingerprint in Firebase project settings before testing phone authentication with Play Integrity.

When the JSON file is present, the Google Services Gradle plugin is applied automatically. Without it, the app displays a clear Firebase setup message instead of crashing.

Phone OTP delivery still depends on the Firebase project's phone sign-in settings, app verification, allowed regions, quotas, and billing rules. Use Firebase's fictional test phone numbers for development where available; do not send real SMS until the project is configured and costs are understood.

## Build

The GitHub Actions workflow `.github/workflows/android-build.yml` builds a debug APK with Gradle 9.6.0 and uploads it as an artifact. The workflow can build the UI even before `google-services.json` is added.

The APK from this workflow is for development testing only. It is not a signed Play Store release.

## Not yet included

- Native chat and group messaging screens.
- Audio/video calling or WebRTC.
- Group calls with up to 20 participants.
- Release signing and AAB generation.
- Physical-device verification.
