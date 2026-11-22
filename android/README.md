# SM CALL Android app

This is the native Android application foundation for SM CALL. It is separate from the GitHub Pages web prototype.

## App identity

- Android package name: `com.smcall.app`
- Languages: Bengali, English, Arabic, Hindi
- Minimum Android version: Android 7.0 (API 24)
- Build output: debug APK from GitHub Actions

## Implemented in this milestone

- Native Android Activity and mobile layout.
- Email + password account creation and sign-in.
- Email verification link, resend verification, reload verification status, and Spam/Junk guidance.
- Unverified accounts are kept on the verification screen and cannot enter the app home.
- Password reset email flow.
- Four-language labels and Arabic right-to-left layout.
- Saves verified user's email profile to Firestore.
- Chat, one-to-one calls, and group calls are honest placeholders; they are not implemented yet.

## Firebase setup required

1. In Firebase Console, open project `sm-call-bf23e`.
2. Add an **Android app** using this exact package name: `com.smcall.app`.
3. Download `google-services.json`.
4. Put it at `android/app/google-services.json`.
5. In Firebase Authentication → Sign-in method, enable **Email/Password**.
6. In Authentication → Settings → Authorized domains, ensure the domain used for email action links is allowed.
7. Configure an email template and test the verification and password reset emails.

The Android build workflow checks the optional Firebase configuration. Without `google-services.json`, CI intentionally creates a UI-only APK; Firebase sign-in will not work. A Firebase-configured APK still requires the Email/Password provider to be enabled.

Never commit a Firebase Admin SDK service-account JSON file or private key.

## Build and verify

1. Open the repository's **Actions** tab.
2. Open the latest **Build SM CALL Android** run.
3. Confirm **Validate Firebase Android configuration**, **Run Android unit tests**, and **Build debug APK** succeeded.
4. Download the APK artifact shown by that run.
5. If the artifact is marked UI-only, add the Firebase Android configuration before expecting login to work.

The APK is for development testing only. It is not a Play Store release.

## Not yet included

- Real-time chat and group messaging in the native app.
- One-to-one audio/video calls or WebRTC.
- Group audio/video call enforcement and signaling (target: maximum 5 participants).
- Release signing and AAB generation.
- Physical-device verification.
