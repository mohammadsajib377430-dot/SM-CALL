# SM CALL

SM CALL is an Android-first communication project. The public GitHub Pages site is a Firebase-backed web prototype, **not yet a finished Android application**.

## Live prototype

- Demo: https://mohammadsajib377430-dot.github.io/SM-CALL/
- Repository: https://github.com/mohammadsajib377430-dot/SM-CALL
- Firebase project: sm-call-665e5

## What is currently implemented

- Mobile-friendly web prototype login and home screens.
- A native Android project scaffold in android/ using Firebase Authentication with **email and password** sign-up/login and password-reset email.
- Firebase Android client configuration at android/app/google-services.json for project sm-call-665e5 and package com.smcall.app.
- Native Android chat screen backed by the existing Firestore users, chats, and messages collections.
- Personal chat and group chat message screens; group membership is limited to 20 people including the creator.
- Public display names are stored in users/{uid}; private contact fields are stored separately in owner-only privateUsers/{uid}.
- A language switcher with initial interface labels for Bengali, English, Arabic, and Hindi. This is **partial localization**, not a complete translation of every screen and message.
- Firestore rules are included in firestore.rules; they must be published to the Firebase project for the protections described in the rules to apply.

## Login setup required in Firebase Console

In Firebase Console, open project sm-call-665e5, then go to **Authentication → Sign-in method → Email/Password** and enable Email/Password. The app's email/password sign-up, login, and password-reset functions cannot work until this provider is enabled.

The native Android login no longer uses phone-number OTP. Do not enable or configure the Phone provider for this Android login flow.

## Not implemented / not verified as working

- Real account creation, login, and password reset on physical Android devices; these still need Firebase provider setup and device testing.
- Normal carrier SMS to phone numbers. Firestore chat messages are in-app messages, not SMS delivered to a phone's messaging inbox. Carrier SMS requires an SMS provider and paid usage.
- One-to-one audio calls and video calls.
- Group audio/video calls for up to 20 people. These require WebRTC signaling and, for reliable large group calls, an SFU/TURN service and its configuration.
- Complete Bengali, English, Arabic, and Hindi localization across all screens, validation messages, and chat flows.
- A completed native Android application, signed release APK/AAB, physical-device verification, and Play Store publication. The native scaffold exists, but its call features are not implemented yet.

## Automated Android checks

GitHub Actions validates the Firebase Android client configuration, runs Android unit tests, and builds a debug APK for the Android pull request. A successful build proves that the code compiles; it does **not** prove that Firebase provider settings or live sign-up/login work on a phone.

## Firestore security

The rules in firestore.rules restrict profile and chat access. Updating the file in GitHub does **not** automatically publish it to Firestore unless a deployment workflow or Firebase CLI deployment is configured and run. Confirm the deployed rules in Firebase Console before using real accounts.

## Important

A screen or button is not proof that a feature works. SM CALL will only mark a feature complete after its backend is configured and the behavior has been tested successfully on real devices.
