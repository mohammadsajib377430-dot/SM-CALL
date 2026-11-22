import { readFileSync } from "node:fs";
import assert from "node:assert/strict";
import { Script } from "node:vm";

const html = readFileSync(new URL("../index.html", import.meta.url), "utf8");
const rules = readFileSync(new URL("../firestore.rules", import.meta.url), "utf8");
const firebaseConfig = JSON.parse(
  readFileSync(new URL("../firebase.json", import.meta.url), "utf8")
);

const inlineScripts = [...html.matchAll(/<script\b([^>]*)>([\s\S]*?)<\/script>/gi)]
  .filter(([, attributes, body]) => !/\bsrc\s*=/i.test(attributes) && body.trim());

assert.ok(inlineScripts.length > 0, "index.html must contain an inline application script");
inlineScripts.forEach(([, , source], index) => {
  new Script(source, { filename: `index.html:inline-script-${index + 1}` });
});

const requiredScreens = [
  "loginScreen",
  "signupScreen",
  "verificationScreen",
  "homeScreen",
  "chatsScreen",
  "groupScreen",
  "chatRoomScreen",
  "profileScreen"
];
for (const id of requiredScreens) {
  assert.match(html, new RegExp(`id=["']${id}["']`), `Missing screen: ${id}`);
}

const languageMatch = html.match(/const supportedLanguages\s*=\s*\[([^\]]+)\]/);
assert.ok(languageMatch, "Language switcher must declare supportedLanguages");
const languages = [...languageMatch[1].matchAll(/["']([a-z]{2})["']/g)].map(match => match[1]);
assert.deepEqual(languages, ["bn", "en", "ar", "hi"], "Expected Bengali, English, Arabic and Hindi language options");
for (const language of languages) {
  assert.match(html, new RegExp(`\\b${language}:\\s*\\{`), `Missing translation dictionary for ${language}`);
}

assert.match(html, /otherMembers\.length\s*\+\s*1\s*>\s*20/, "Client must enforce the 20-member group limit");
assert.match(rules, /request\.resource\.data\.members\.size\(\)\s*<=\s*20/, "Firestore rules must enforce the 20-member group limit");
assert.equal(firebaseConfig.firestore?.rules, "firestore.rules", "firebase.json must point to firestore.rules");

assert.match(html, /function createEmailAccount\s*\(/, "Email registration flow is missing");
assert.match(html, /function loginWithEmail\s*\(/, "Email/password login flow is missing");
assert.match(html, /sendEmailVerification\s*\(/, "Email verification flow is missing");
assert.match(html, /emailVerified/, "Verified-email access gate is missing");
assert.match(html, /function resendVerificationEmail\s*\(/, "Resend verification flow is missing");
assert.match(html, /function resetPassword\s*\(/, "Password reset flow is missing");
assert.doesNotMatch(html, /signInWithPhoneNumber|sendOTP|verifyOTP|RecaptchaVerifier|signInAnonymously/, "Phone OTP and anonymous login must be removed");
assert.match(html, /MAX_GROUP_CALL_PARTICIPANTS\s*=\s*5/, "Group audio/video call limit must be five");
assert.match(rules, /request\.auth\.token\.email_verified\s*==\s*true/, "Firestore rules must require verified email");

assert.match(html, /function coming\s*\(feature\)/, "Call placeholder handler is missing");
assert.match(html, /এখনো চালু হয়নি/, "Call UI must not imply real calling is implemented");

console.log("SM CALL prototype checks passed:");
console.log("- Inline JavaScript syntax");
console.log("- Required login, chat, group and profile screens");
console.log("- bn/en/ar/hi language options and dictionaries");
console.log("- 20-member group limit in UI and Firestore rules");
console.log("- Firebase rules configuration path");
console.log("- Email/password authentication, verification, resend and password reset");
console.log("- Five-person group-call limit and honest call-feature placeholder");
