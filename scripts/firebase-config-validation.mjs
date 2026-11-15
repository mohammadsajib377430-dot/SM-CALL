export const EXPECTED_FIREBASE_PROJECT_ID = "sm-call-bf23e";
export const EXPECTED_ANDROID_PACKAGE_NAME = "com.smcall.app";

export function validateFirebaseAndroidConfig(config) {
  const errors = [];

  if (!config || typeof config !== "object" || Array.isArray(config)) {
    return ["Firebase configuration must be a JSON object."];
  }

  const actualProjectId = config?.project_info?.project_id;
  if (actualProjectId !== EXPECTED_FIREBASE_PROJECT_ID) {
    errors.push(
      \`project_info.project_id must be "\${EXPECTED_FIREBASE_PROJECT_ID}", received "\${actualProjectId ?? "(missing)"}".\`
    );
  }

  const clients = Array.isArray(config.client) ? config.client : [];
  const androidClient = clients.find(
    (client) =>
      client?.client_info?.android_client_info?.package_name ===
      EXPECTED_ANDROID_PACKAGE_NAME
  );

  if (!androidClient) {
    errors.push(
      \`No Android client was found for package "\${EXPECTED_ANDROID_PACKAGE_NAME}".\`
    );
    return errors;
  }

  const appId = androidClient?.client_info?.mobilesdk_app_id;
  // Firebase Android App IDs have the form 1:<project-number>:android:<hex-id>.
  if (!/^1:\d+:android:[a-f0-9]+$/i.test(appId ?? "")) {
    errors.push("The matching Android client is missing a valid mobilesdk_app_id.");
  }

  const apiKeys = Array.isArray(androidClient.api_key)
    ? androidClient.api_key
    : [];
  const hasUsableApiKey = apiKeys.some(
    (entry) =>
      typeof entry?.current_key === "string" &&
      entry.current_key.trim().length > 0 &&
      !entry.current_key.includes("YOUR_API_KEY")
  );

  if (!hasUsableApiKey) {
    errors.push("The matching Android client is missing a usable current_key.");
  }

  return errors;
}
