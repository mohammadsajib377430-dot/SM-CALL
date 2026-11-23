# SM CALL Android app

Native Android client for SM CALL.

## App identity

- Android package: `com.smcall.app`
- Minimum Android: API 24 (Android 7.0)
- UI language options: Bengali, English, Arabic and Hindi (coverage is partial and still needs review)
- Firebase Android project in the checked-in client configuration: `sm-call-665e5`

## Current verified repository facts — 23 November 2026 (updated after merge)

- `android/app/google-services.json` exists and identifies Firebase project `sm-call-665e5` for package `com.smcall.app`.
- The GitHub Pages web prototype in the root `index.html` still uses project `sm-call-bf23e`. These Firebase projects do not share Authentication users or Firestore data.
- The latest phone-OTP Firebase-enabled debug APK is from [workflow run 45123936308](https://github.com/mohammadsajib377430-dot/SM-CALL/actions/runs/45123936308). The Android unit-test and APK-build steps passed; the Firestore emulator/prototype workflow [45123936341](https://github.com/mohammadsajib377430-dot/SM-CALL/actions/runs/45123936341) also passed.
- The native app already contains Email/Password registration, email verification, password reset, a personal-chat screen and Firestore message listeners. The personal-chat flow has not been confirmed by a new physical-device test in this update.
- Audio/video buttons still show a not-implemented message. The WebRTC dependency alone does not implement media transport or signaling.
- Firestore chat-group rules allow up to 20 members, but native group-chat invitation and membership management are not complete.
- Firestore call-room rules remain limited to 5 participants. The requested 20-person group video target is not implemented.

## Phone OTP implementation — merged 23 November 2026

The implementation from [PR #2 — Add native Firebase phone OTP sign-in](https://github.com/mohammadsajib377430-dot/SM-CALL/pull/2) is merged into `main` as [commit 1b09620](https://github.com/mohammadsajib377430-dot/SM-CALL/commit/1b09620e800043f13c4bf28875de265f5824fe6e). It adds:

- A native `PhoneOtpActivity` using Firebase `PhoneAuthProvider`.
- E.164 phone input, SMS code entry, automatic verification callback, verification-session handling and resend-token support.
- Bengali, Arabic, Persian and Devanagari digit normalization.
- A phone OTP option on the existing sign-in screen; Email/Password remains available for compatibility.
- Phone-verified profile handling in Android and matching Firestore rule/test changes.

The CI checks passed: Android unit tests, Firebase configuration validation, APK assembly/artifact upload, prototype smoke checks and Firestore emulator tests. Real Firebase SMS delivery, production-rule deployment and physical-device behavior are still unverified.

## Firebase Console setup still required

For Android project `sm-call-665e5`:

1. Enable **Authentication → Sign-in method → Phone**.
2. Register the current debug build's SHA-1 and SHA-256 fingerprints in the Android app settings.
3. Configure the SMS region policy for only the countries being tested.
4. Add a Firebase test phone number and fixed code.
5. Test with that test number on a physical Android device before sending real SMS.

Real SMS may require an eligible billing configuration and is subject to country-specific quotas and anti-abuse controls. Use Firebase test phone numbers first.

See [PHONE_OTP_SETUP.md](PHONE_OTP_SETUP.md) for the safe setup and deployment sequence.

## Firestore security

The pull request updates repository rules to accept either:
- a Firebase ID token with `email_verified == true`, or
- a Firebase Phone Auth ID token with a non-empty `phone_number` claim.

The matching public profile marker must be `emailVerified: true` or `phoneVerified: true`. Phone numbers must not be copied into public user profiles; private phone data belongs in owner-only `privateUsers/{uid}`.

Rules in GitHub are not automatically published. After reviewing and merging the change, deploy only to the intended Android project:

```sh
firebase deploy --only firestore:rules --project sm-call-665e5
```

Confirm the active project before running this production-changing command. Local emulator tests do not publish rules.

## Remaining Android work

- Complete and verify the Phone OTP pull request.
- Align the Android and web clients to one Firebase project only after the canonical project is explicitly chosen; no web migration is included in PR #2.
- Complete localization across all screens and error states.
- Finish secure group-chat invitation and membership management.
- Implement real one-to-one audio/video calling with signaling, ICE/TURN and device lifecycle handling.
- Design and test an SFU-based architecture before targeting 20 simultaneous video participants; a peer-to-peer mesh is not a reliable 20-person design.
- Increase call-room rules and server-side limits from 5 only when the 20-person call implementation is ready and tested.
- Add release signing, AAB generation and physical-device regression tests.

A successful build is not proof of SMS delivery, Firestore production-rule deployment, or working calls.
