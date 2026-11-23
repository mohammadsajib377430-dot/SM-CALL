# SM CALL Android Phone OTP setup

## Current project boundary

The checked-in Android client configuration at `android/app/google-services.json` targets Firebase project `sm-call-665e5` and Android package `com.smcall.app`.

The GitHub Pages web prototype still targets `sm-call-bf23e`. These are separate Firebase projects, so they do not share Authentication users or Firestore data. This Android OTP work deliberately does not migrate the web demo or production data.

## Before testing OTP

1. Open Firebase Console for project `sm-call-665e5`.
2. Go to **Authentication → Sign-in method** and enable **Phone**.
3. Register the SHA-1 and SHA-256 fingerprints from the latest Android debug APK artifact in **Project settings → Your apps → Android app**. Re-check the fingerprints after changing the signing key or build variant.
4. In **Authentication → Settings**, configure the SMS region policy to allow only the countries you intend to test.
5. Add a Firebase **test phone number and fixed test code** under the Phone provider's testing section. Use this first; test numbers do not send a real SMS.
6. Install the Firebase-enabled debug APK on a physical Android device and test the test number before trying a real number.

## SMS cost and limits

Real SMS delivery is subject to Firebase's current quotas, country support, anti-abuse checks, and billing requirements. Do not repeatedly send real OTP messages during development. Use test phone numbers first. Enable billing only after reviewing the current Firebase pricing and SMS limits for the countries you need.

## Firestore rules

The repository rules now allow either:
- a Firebase ID token with `email_verified == true`, or
- a Firebase Phone Auth ID token with a non-empty `phone_number` claim.

A public profile must carry the matching verification marker: `emailVerified: true` for a verified email account, or `phoneVerified: true` for a phone-authenticated account. A phone-only token cannot claim email verification.

After reviewing and merging the rules change, publish it only to the Android Firebase project:

```sh
firebase deploy --only firestore:rules --project sm-call-665e5
```

Confirm the active project before deployment. This command changes production Firestore rules. A passing local emulator test does not publish rules.

## What this does not prove

A successful APK build does not prove SMS delivery. The real-device test must confirm that Firebase sends or accepts the test code, signs the user in, creates the profile, and allows the signed-in user to read/write only their permitted chat data.

Do not upload a Firebase Admin service-account key to the Android app or repository.
