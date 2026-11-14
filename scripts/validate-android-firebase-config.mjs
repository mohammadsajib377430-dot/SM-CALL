import { existsSync, readFileSync } from "node:fs";

const configPath = "android/app/google-services.json";
const expectedProjectId = "sm-call-bf23e";
const expectedPackageName = "com.smcall.app";

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

const errors = [];
const actualProjectId = config?.project_info?.project_id;

if (actualProjectId !== expectedProjectId) {
  errors.push(
    `project_info.project_id must be "${expectedProjectId}", received "${actualProjectId ?? "(missing)"}".`
  );
}

const clients = Array.isArray(config?.client) ? config.client : [];
const androidClient = clients.find(
  (client) =>
    client?.client_info?.android_client_info?.package_name === expectedPackageName
);

if (!androidClient) {
  errors.push(
    `No Android client was found for package "${expectedPackageName}".`
  );
} else {
  const appId = androidClient?.client_info?.mobilesdk_app_id;
  if (!/^1:\\d+:android:[a-f0-9]+$/i.test(appId ?? "")) {
    errors.push("The matching Android client is missing a valid mobilesdk_app_id.");
  }

  const apiKeys = Array.isArray(androidClient.api_key) ? androidClient.api_key : [];
  const hasUsableApiKey = apiKeys.some(
    (entry) =>
      typeof entry?.current_key === "string" &&
      entry.current_key.trim().length > 0 &&
      !entry.current_key.includes("YOUR_API_KEY")
  );
  if (!hasUsableApiKey) {
    errors.push("The matching Android client is missing a usable current_key.");
  }
}

if (errors.length > 0) {
  for (const error of errors) console.error(`::error::${error}`);
  process.exit(1);
}

console.log(
  `Firebase Android configuration is valid for ${expectedProjectId} / ${expectedPackageName}.`
);
