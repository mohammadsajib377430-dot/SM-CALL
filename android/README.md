# SM CALL Android app

Native Android client for SM CALL.

## App identity

- Android package: `com.smcall.app`
- Minimum Android: API 24 (Android 7.0)
- UI language options: Bengali, English, Arabic and Hindi (translation coverage is partial)
- Android Firebase project in `google-services.json`: `sm-call-665e5`

## Current repository state — 26 November 2026

- The latest repository commit at the start of this update was `121d45a` on `main`.
- Android Firebase configuration exists for project `sm-call-665e5` and package `com.smcall.app`.
- The GitHub Pages web prototype in the root `index.html` uses a different Firebase project, `sm-call-bf23e`. Authentication users and Firestore data are not shared automatically.
- The native Android client contains both Email/Password authentication and a separate `PhoneOtpActivity` using Firebase `PhoneAuthProvider`.
- A regression was introduced by commits `03298de` and `7593dca`: the shared account policy and Firestore rules were changed back to email-only, even though the phone OTP screen and phone-profile code remained. This prevented phone-only accounts from entering the native app's verified area and caused the Firestore emulator suite to fail.
- The Android build for commit `121d45a` passed in [run 45667477400](https://github.com/mohammadsajib377430-dot/SM-CALL/actions/runs/45667477400).
- The prototype/Firestore workflow failed in [run 45667477389](https://github.com/mohammadsajib377430-dot/SM-CALL/actions/runs/45667477389): 12 of 16 Firestore tests passed; four failed because phone-only access was rejected and call-room test fixtures omitted the required `targetUid`.
- GitHub Pages build and deployment for commit `121d45a` succeeded in [run 45667477811](https://github.com/mohammadsajib377430-dot/SM-CALL/actions/runs/45667477811).

## Phone OTP flow

`PhoneOtpActivity.kt` uses Firebase Phone Auth to request an SMS code, handle automatic verification, accept a manually entered six-digit code, and sign in with the Firebase phone credential. Bengali, Arabic, Persian and Devanagari digit normalization is included.

The main screen launches this activity. After Firebase verifies the phone number, the Android client stores only a `phoneVerified: true` marker in the public profile; the actual phone number must remain in owner-only private data.

The account policy and Firestore rules must both accept either:
- a Firebase ID token with `email_verified == true`, with a matching `emailVerified: true` profile marker; or
- a Firebase Phone Auth ID token with a non-empty `phone_number` claim, with a matching `phoneVerified: true` profile marker.

This change restores that policy and fixes the emulator fixtures. It does not enable the Phone provider in Firebase Console, publish production rules, deliver an SMS, or prove device login.

## WebRTC status

- Native Android contains a `CallActivity` with one-to-one WebRTC peer setup, SDP offer/answer exchange, Firestore ICE candidate signaling, microphone/camera capture, and local/remote video renderers.
- The code has passed Android compilation in the latest successful build, but no two-device physical-call test is recorded. A successful APK build is not proof of working media, incoming-call reliability, NAT traversal or TURN availability.
- Current call-room capacity remains five. The current Android call flow is one-to-one; 20-person group video is not implemented.
- A reliable 20-person video room needs an SFU architecture, not a 20-peer mesh. Keep the current five-person limit until that architecture is built and tested.

## Firebase Console setup still required

For Android project `sm-call-665e5`:

1. Enable **Authentication → Sign-in method → Phone**.
2. Register the signing certificate fingerprints for the exact APK being tested.
3. Restrict the SMS region policy to countries actually needed for testing.
4. Add a Firebase test phone number and fixed code.
5. Test the fixed test number on a physical Android device before sending real SMS.

Use Firebase test numbers first to avoid unnecessary SMS charges. Real SMS delivery may require eligible billing and is subject to country quotas and anti-abuse controls.

## Firestore security

The repository rules file is `firestore.rules`, referenced by `firebase.json`. The emulator tests run in CI but do not publish production rules.

After reviewing and merging the rules, deploy only to the intended Android project:

```sh
firebase deploy --only firestore:rules --project sm-call-665e5
```

Confirm the active Firebase project in the Console before running a production-changing command. Do not deploy these rules to the Web Demo project without reviewing its separate data model.

## Remaining Android work

- Complete the phone OTP regression fix and obtain a passing Firestore emulator run.
- Enable Phone Auth and test with a Firebase test number on a physical Android device.
- Register a stable signing key; GitHub-hosted debug signing fingerprints can change between runs.
- Verify the one-to-one WebRTC call on two physical devices and configure TURN if required.
- Finish localization across every screen and error state.
- Complete secure group-chat invitation and membership management.
- Design and test an SFU before raising group video above the current five-person limit.
- Add release signing, AAB generation and physical-device regression tests.

A passing unit test or APK build does not prove SMS delivery, production-rule publication, or successful calls.
