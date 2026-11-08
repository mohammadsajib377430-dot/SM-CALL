# SM CALL

SM CALL is an Android-first communication project. The public GitHub Pages site is currently a Firebase-backed web prototype, **not yet a finished Android application**.

## Live prototype

- Demo: https://mohammadsajib377430-dot.github.io/SM-CALL/
- Repository: https://github.com/mohammadsajib377430-dot/SM-CALL
- Firebase project: `sm-call-bf23e`

## What is currently implemented

- Mobile-friendly login and home screens.
- Firebase Anonymous Authentication option labelled **Online Demo**. This does not verify a phone number and does not send an OTP.
- Local-only demo account option. Local demo data stays in the same browser/device and cannot chat with another device.
- Firebase user profile documents. Public display names are stored in `users/{uid}`; phone numbers are stored separately in owner-only `privateUsers/{uid}`.
- Firebase Firestore personal chat and group chat message screens.
- Personal chat uses a shared chat document based on the two account UIDs.
- Group creation validates invited account UIDs and limits membership to 20 people including the creator.
- A language switcher with initial interface labels for Bengali, English, Arabic, and Hindi. This is **partial localization**, not a complete translation of every screen and message.
- Firestore rules are included in `firestore.rules`; they must be published to the Firebase project for the protections described in the rules to apply.

## Not implemented / not verified as working

- Real SMS OTP sign-in. The current Online Demo uses anonymous sign-in. Phone OTP previously returned `auth/billing-not-enabled`; it requires Firebase phone-auth setup, an eligible billing configuration, authorized domains, and SMS region settings.
- Normal carrier SMS to phone numbers. Firestore chat messages are in-app messages, not SMS delivered to a phone's messaging inbox. Carrier SMS requires an SMS provider and paid usage.
- One-to-one audio calls and video calls.
- Group audio/video calls for up to 20 people. These require WebRTC signaling and, for reliable large group calls, an SFU/TURN service and its configuration.
- Complete Bengali, English, Arabic, and Hindi localization across all screens, validation messages, and chat flows.
- Native Android application, signed APK/AAB, device-tested release, and Play Store publication.

## Automated prototype checks

GitHub Actions runs `node scripts/validate-prototype.mjs` on pushes to `main` and pull requests. The checks verify embedded JavaScript syntax, required screens, the four language options, the 20-member group limit in both the UI and Firestore rules, the Firebase rules file path, and honest demo/call placeholders.

These are static smoke checks. They do **not** connect to Firebase, deploy Firestore rules, send OTPs, or prove that chat works between real devices.

## Firebase and security notes

The web client uses Firebase web configuration values in `index.html`. These are client-side project identifiers, not service-account secrets. Never commit Firebase Admin SDK private keys, service-account JSON files, or provider credentials.

The rules in `firestore.rules` restrict profile and chat access. Updating the file in GitHub does **not** automatically publish it to Firestore unless a deployment workflow or Firebase CLI deployment is configured and run. Confirm the deployed rules in Firebase Console before using real accounts.

## Development priorities

1. Preserve and regression-test the existing personal chat.
2. Verify group creation and group messaging with two or more real Online Demo accounts.
3. Finish localization and improve account/profile flows.
4. Add secure call signaling and integrate a configured WebRTC/SFU provider before enabling audio/video call buttons.
5. Create a native Android project, configure Firebase for Android, build APK/AAB, and test on physical Android devices.
6. Enable phone OTP and carrier SMS only after the required providers and billing are configured.

## Important

A screen or button is not proof that a feature works. SM CALL will only mark a feature complete after its backend is configured and the behavior has been tested successfully on real devices.
