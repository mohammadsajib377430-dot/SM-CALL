import test from "node:test";
import assert from "node:assert/strict";
import { validateFirebaseAndroidConfig } from "../scripts/firebase-config-validation.mjs";

function validConfig() {
  return {
    project_info: { project_id: "sm-call-665e5" },
    client: [
      {
        client_info: {
          mobilesdk_app_id: "1:123456789012:android:abcdef1234567890",
          android_client_info: { package_name: "com.smcall.app" }
        },
        api_key: [{ current_key: "AIzaSyExampleClientKeyForUnitTests123456" }]
      }
    ]
  };
}

test("accepts a valid SM CALL Firebase Android configuration", () => {
  assert.deepEqual(validateFirebaseAndroidConfig(validConfig()), []);
});

test("rejects a configuration for a different Firebase project", () => {
  const config = validConfig();
  config.project_info.project_id = "another-project";
  assert.match(validateFirebaseAndroidConfig(config).join(" "), /project_info\.project_id/);
});

test("rejects a missing Android package client", () => {
  const config = validConfig();
  config.client[0].client_info.android_client_info.package_name = "com.example.other";
  assert.match(validateFirebaseAndroidConfig(config).join(" "), /No Android client/);
});

test("rejects a malformed Firebase Android App ID", () => {
  const config = validConfig();
  config.client[0].client_info.mobilesdk_app_id = "1:not-a-number:android:abcdef";
  assert.match(validateFirebaseAndroidConfig(config).join(" "), /mobilesdk_app_id/);
});

test("rejects a missing client API key", () => {
  const config = validConfig();
  config.client[0].api_key = [];
  assert.match(validateFirebaseAndroidConfig(config).join(" "), /current_key/);
});

test("rejects a non-object JSON value", () => {
  assert.match(validateFirebaseAndroidConfig(null).join(" "), /JSON object/);
});
