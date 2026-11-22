import { appendFileSync, existsSync, readFileSync } from "node:fs";
import { validateFirebaseAndroidConfig } from "./firebase-config-validation.mjs";

const configPath = "android/app/google-services.json";

function reportBuildMode(mode, summary) {
  const outputPath = process.env.GITHUB_OUTPUT;
  if (outputPath) appendFileSync(outputPath, `mode=${mode}\n`);

  const summaryPath = process.env.GITHUB_STEP_SUMMARY;
  if (summaryPath) {
    appendFileSync(
      summaryPath,
      `## Android build mode\n\n${summary}\n\nApplication ID: com.smcall.app\n`
    );
  }
}

if (!existsSync(configPath)) {
  const message =
    "**UI-only APK:** google-services.json is absent. Firebase email/password authentication is not initialized in this build.";
  console.log(
    "::notice::google-services.json is absent. Building the UI-only APK; Firebase email/password authentication will remain unavailable."
  );
  reportBuildMode("ui-only", message);
  process.exit(0);
}

let config;
try {
  config = JSON.parse(readFileSync(configPath, "utf8"));
} catch (error) {
  console.error(`Invalid JSON in ${configPath}: ${error.message}`);
  process.exit(1);
}

const errors = validateFirebaseAndroidConfig(config);
if (errors.length > 0) {
  for (const error of errors) console.error(`::error::${error}`);
  process.exit(1);
}

reportBuildMode(
  "firebase-enabled",
  "**Firebase-configured APK:** the Android client configuration matches project sm-call-665e5 and package com.smcall.app. The Email/Password sign-in provider must be enabled in Firebase Console; live sign-up, login, and password-reset flows still require device testing."
);
console.log(
  "Firebase Android configuration is valid for sm-call-665e5 / com.smcall.app."
);
