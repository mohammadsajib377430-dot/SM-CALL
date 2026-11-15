import { existsSync, readFileSync } from "node:fs";
import { validateFirebaseAndroidConfig } from "./firebase-config-validation.mjs";

const configPath = "android/app/google-services.json";

if (!existsSync(configPath)) {
  console.log(
    "::notice::google-services.json is absent. Building the UI-only APK; Firebase Phone Authentication will remain unavailable."
  );
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

console.log(
  "Firebase Android configuration is valid for sm-call-bf23e / com.smcall.app."
);
