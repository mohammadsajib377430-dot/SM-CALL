# SM CALL

SM CALL is an Android-first communication project. The public GitHub Pages site is a separate web prototype, not the finished Android application.

## Project links

- Web prototype: https://mohammadsajib377430-dot.github.io/SM-CALL/
- Repository: https://github.com/mohammadsajib377430-dot/SM-CALL
- Android package: `com.smcall.app`
- Android Firebase project: `sm-call-665e5`
- Web prototype Firebase project: `sm-call-bf23e`

The Android and Web clients currently use different Firebase projects. Their Authentication accounts and Firestore data are not shared automatically. Do not migrate either client until the canonical project and data migration plan are agreed.

## Current implementation

### Native Android

- Native Email/Password registration, email verification and password reset.
- A separate `PhoneOtpActivity` using Firebase `PhoneAuthProvider` for SMS OTP sign-in.
- Bengali, English, Arabic and Hindi language options; translation coverage is still partial.
- Firebase user profiles and personal chat/message screens.
- A native `CallActivity` containing one-to-one WebRTC media setup and Firestore SDP/ICE signaling code. It has compiled in CI but has not yet been verified in a two-device physical call.
- Current call-room capacity is five. The 20-person group-video target is not implemented.

### GitHub Pages Web prototype

- Mobile-friendly login, profile, personal chat and group chat screens.
- Email/Password sign-in and email verification.
- Local-only demo mode; local demo data does not sync to another device.
- Initial Bengali, English, Arabic and Hindi interface translations.
- Audio and video cards are explicit placeholders in the Web prototype; they do not start calls.
- The Web prototype uses Firebase project `sm-call-bf23e`, not the Android project's `sm-call-665e5`.

### Group limits

- Firestore chat-group rules allow up to 20 members including the creator.
- Call rooms are still limited to five participants. Do not simply raise this number: a reliable 20-person video room needs an SFU media architecture, server-side capacity enforcement, TURN/NAT planning and real-device load tests.

## Authentication and Firestore security

The native phone OTP screen alone is not sufficient. The Android verified-account policy and `firestore.rules` must both accept Firebase-verified email accounts and Firebase Phone Auth accounts. Public profiles must contain only a `phoneVerified` marker, not the phone number itself. Keep actual phone data in owner-only `privateUsers/{uid}`.

The repository includes `firestore.rules` and Firestore Emulator tests in `tests/firestore.rules.test.mjs`. CI emulator tests do not publish rules to a live Firebase project. After review, deploy to the intended Android project only:

```sh
firebase deploy --only firestore:rules --project sm-call-665e5
```

The Firebase Phone provider, current signing fingerprints, SMS region policy, billing/quota configuration and test phone numbers must be checked in Firebase Console. Use test numbers before attempting real SMS.

## Automated checks

GitHub Actions runs:
- Android Gradle unit tests and debug APK assembly.
- Firebase Android configuration validation.
- Web prototype syntax/smoke checks.
- Firestore Rules unit tests against the local emulator.

These checks do not send SMS, publish production rules, or prove that chat/calls work between physical devices.

## Development priorities

1. Restore and regression-test the native phone-verified account policy in both Kotlin and Firestore Rules.
2. Complete Firebase Console Phone provider setup and test OTP on a physical Android device.
3. Verify one-to-one WebRTC calling on two devices, including ICE/TURN and lifecycle handling.
4. Finish secure group-chat invitations and full localization.
5. Design an SFU-based architecture before implementing 20-person group video.
6. Align the Web and Android Firebase projects only after choosing a canonical project and planning data migration.
7. Add stable release signing, AAB generation and Play Store readiness.

A screen, button, source file or successful build is not proof that a feature works. Mark features complete only after backend configuration and physical-device testing.
