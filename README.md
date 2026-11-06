# SM CALL

**SM CALL** is an Android-first communication app project. The repository currently contains a mobile-friendly web prototype published with GitHub Pages. It is not yet a production Android app.

## Project goals

- Phone-number registration and sign-in using SMS OTP
- Bengali, English, Arabic, and Hindi interface
- Personal messaging and group messaging
- Audio and video calling
- Group video calls for up to 20 participants
- Android application package (APK/AAB) for testing and eventual release

## Current prototype

The current web demo is implemented in `index.html` and uses Firebase Authentication (Phone) and Cloud Firestore.

Implemented in the prototype:
- Responsive SM CALL home and login screens
- Phone-number OTP flow with reCAPTCHA
- User profile creation in Firestore
- Personal and group chat screens
- Group membership limit in the current interface: 20 people

Not yet production-ready:
- Real-time audio/video calling and call signaling
- Secure, tested Firestore security rules
- Complete Bengali, English, Arabic, and Hindi localization
- Android-native build, device testing, and release signing
- End-to-end OTP verification on real phone numbers

## Firebase setup

The web prototype is configured for the Firebase project `sm-call-bf23e`.

Before testing:
1. Confirm **Phone** is enabled under Firebase Authentication sign-in providers.
2. Add the deployed GitHub Pages hostname to Firebase Authentication's authorized domains.
3. Confirm the SMS region policy allows the countries you intend to test.
4. Create and publish restrictive Firestore Security Rules. Do not use open public read/write rules.
5. Test OTP delivery and verification with a real phone number.

Firebase web configuration values are client-side identifiers; never put service-account private keys or Admin SDK credentials in this repository.

## Run the web demo

Open the GitHub Pages site configured for this repository, or serve `index.html` from a local static web server. Firebase Authentication's phone flow requires a supported browser environment and authorized domain.

## Android app direction

The current site is a prototype, not an installable Android application. The next engineering phase is to create a native Android project (package name planned as `com.mohammadsajib.smcall`), connect Firebase using the Android app configuration, implement and test WebRTC calling with appropriate signaling and TURN infrastructure, then build and test an APK/AAB.

## Repository

- Source: https://github.com/mohammadsajib377430-dot/SM-CALL
- Main web entry point: `index.html`

---
*Project status: prototype in progress. Features are not considered complete until they have been implemented, tested, and verified on devices.*
