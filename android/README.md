# SM CALL Android app

This is the native Android application foundation for SM CALL. It is separate from the GitHub Pages web prototype.

## App identity

- Android package name: `com.smcall.app`
- Languages: Bengali, English, Arabic, Hindi (current UI labels are present; full localization coverage still needs review)
- Minimum Android version: Android 7.0 (API 24)
- Build output: debug APK from GitHub Actions

## Verified repository state — 22 November 2026

- `android/app/google-services.json` is present and the Android build validates it for Firebase project `sm-call-665e5` and package `com.smcall.app`.
- The GitHub Pages web demo in `index.html` still uses Firebase project `sm-call-bf23e`. These are different Firebase projects. Authentication users and Firestore data are **not shared** between them. Choose one canonical project and align both clients before expecting Android and web users to see the same accounts or data.
- Native `MainActivity.kt` currently implements **Email/Password** registration and sign-in, email verification, and password reset. **Phone-number OTP login is not implemented yet.** A valid `google-services.json` only configures the Android Firebase client; it does not prove that Phone Authentication or billing is enabled.
- The latest verified Firebase-configured Android debug build is [workflow run 44988535806](https://github.com/mohammadsajib377430-dot/SM-CALL/actions/runs/44988535806). Its Gradle `testDebugUnitTest` task reported `NO-SOURCE`; do not interpret that task as proof of Android unit-test coverage.
- The prototype validation workflow [run 44988832465](https://github.com/mohammadsajib377430-dot/SM-CALL/actions/runs/44988832465) passed 6 Firebase-config validation tests and 14 Firestore Emulator rules tests.
- Firestore chat-group creation allows up to 20 members. Native call-room repository and current Firestore call-room rules are limited to **5 participants**. The WebRTC dependency is present, but actual audio/video media transport and call signaling are not implemented.

## Firebase project alignment — required

Current project IDs:

| Client | Firebase project |
|---|---|
| Native Android | `sm-call-665e5` |
| GitHub Pages web demo | `sm-call-bf23e` |

Do not assume these clients share Authentication accounts, Firestore documents, or other Firebase resources. Before integration testing, confirm which Firebase project is canonical. If the original `sm-call-bf23e` project is intended, replace the Android configuration with an Android app configuration downloaded from that project. If `sm-call-665e5` is intended, migrate the web demo to that project using its own Web App configuration. Never copy an API key or app ID from another project.

## Current Android authentication

The native app currently uses Firebase Email/Password authentication and requires verified email accounts before showing the home screen. The requested phone-number OTP flow remains a separate implementation task:

1. Add a phone-number screen using E.164 input.
2. Implement Firebase `PhoneAuthProvider.verifyPhoneNumber` with resend-token and verification-session binding.
3. Support test phone numbers first; verify Phone Authentication provider and billing in the selected Firebase project before testing real SMS.
4. Update Firestore rules and profile creation to accept Firebase phone-authenticated users safely, while keeping private phone numbers owner-only.
5. Test on a physical Android device.

## Current Firestore and calling limits

- Public user profiles contain UID and display name; private phone data belongs in `privateUsers/{uid}`.
- Chat membership rules allow 2–20 members. Membership invitation/join workflow still needs implementation.
- Call rooms currently allow a maximum of 5 participants, not the 20-person target.
- `CallRoomRepository` manages room membership only. It does not transmit audio/video.
- WebRTC media setup, signaling, ICE/TURN configuration, lifecycle handling, and real-device call testing remain to be implemented.

## Firebase Android configuration

The Android configuration file belongs at:

`android/app/google-services.json`

The build validator checks the project ID, Android package, Firebase Android App ID, and presence of a client API key. It does **not** verify Firebase Console provider settings, billing, SMS region policy, SHA registration, or whether an OTP can actually be delivered.

Never commit a Firebase Admin SDK service-account JSON file or private key. The Android `google-services.json` is client configuration, not an Admin credential; still keep project settings and API-key restrictions correct.

## Build and verify

1. Open the repository's **Actions** tab.
2. Open the latest **Build SM CALL Android** run.
3. Confirm Firebase configuration validation, APK assembly, and artifact upload succeeded.
4. Read `build-info.txt` in the artifact and confirm its project ID and build mode.
5. Do not treat a successful APK build as proof that sign-in, SMS delivery, Firestore production rules, or calls work.
6. Test the APK on a physical Android device after the correct Firebase project and providers are confirmed.

The APK is for development testing only. It is not a Play Store release.

## Not yet included

- Native phone-number OTP login.
- Real-time native chat and group messaging.
- One-to-one audio/video calls or working WebRTC media.
- Group audio/video calls with the target of 20 participants.
- Secure invitation flow for chat and call-room membership.
- Release signing and AAB generation.
- Physical-device verification.
