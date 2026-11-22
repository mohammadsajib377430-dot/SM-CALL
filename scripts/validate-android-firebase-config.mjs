import { appendFileSync, existsSync, readFileSync } from "node:fs";
import {
  EXPECTED_FIREBASE_PROJECT_ID,
  validateFirebaseAndroidConfig
} from "./firebase-config-validation.mjs";

const configPath = "android/app/google-services.json";
const webIndexPath = "index.html";

function getPagesFirebaseProjectId() {
  if (!existsSync(webIndexPath)) return null;
  const html = readFileSync(webIndexPath, "utf8");
  return html.match(/projectId\s*:\s*["']([^"']+)["']/)?.[1] ?? null;
}

function reportBuildMode(mode, summary) {
  const outputPath = process.env.GITHUB_OUTPUT;
  if (outputPath) appendFileSync(outputPath, `mode=${mode}\n`);

  const summaryPath = process.env.GITHUB_STEP_SUMMARY;
  if (summaryPath) {
    appendFileSync(
      summaryPath,
      `## Android build mode\n\n${summary}\n\nApplication ID: \`com.smcall.app\`\n`
    );
  }
}

if (!existsSync(configPath)) {
  const message =
    "**UI-only APK:** `google-services.json` is absent. Firebase Phone Authentication is not initialized in this build.";
  console.log(
    "::notice::google-services.json is absent. Building the UI-only APK; Firebase Phone Authentication will remain unavailable."
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

const androidProjectId = config.project_info.project_id;
const pagesProjectId = getPagesFirebaseProjectId();
const projectAlignmentWarning =
  pagesProjectId && pagesProjectId !== androidProjectId
    ? `\n\n**Project mismatch warning:** Android uses \`${androidProjectId}\`, while the GitHub Pages demo uses \`${pagesProjectId}\`. Firebase Auth users and Firestore data are separate between these projects until both clients use the same project.`
    : "";

const summary =
  `**Firebase-configured APK:** Android client configuration matches project \`${EXPECTED_FIREBASE_PROJECT_ID}\` and package \`com.smcall.app\`. Phone Authentication provider, billing, SMS-region settings, and real/test OTP behavior still require separate verification.` +
  projectAlignmentWarning;

if (pagesProjectId && pagesProjectId !== androidProjectId) {
  console.log(
    `::warning::Android Firebase project ${androidProjectId} differs from GitHub Pages project ${pagesProjectId}; Auth users and Firestore data are not shared.`
  );
}

reportBuildMode("firebase-enabled", summary);
console.log(
  `Firebase Android configuration is valid for ${EXPECTED_FIREBASE_PROJECT_ID} / com.smcall.app.`
);
