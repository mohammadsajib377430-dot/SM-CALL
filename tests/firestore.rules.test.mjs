import { readFile } from "node:fs/promises";
import test, { after, before, beforeEach } from "node:test";
import assert from "node:assert/strict";
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment
} from "@firebase/rules-unit-testing";
import {
  addDoc,
  collection,
  doc,
  getDoc,
  getDocs,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
  where
} from "firebase/firestore";

const projectId = "demo-sm-call";
let testEnv;

before(async () => {
  const rules = await readFile(new URL("../firestore.rules", import.meta.url), "utf8");
  testEnv = await initializeTestEnvironment({
    projectId,
    firestore: { rules }
  });
});

after(async () => {
  await testEnv?.cleanup();
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

function userDb(uid) {
  return testEnv.authenticatedContext(uid).firestore();
}

function chatData(members, type = "group", name = "Test group") {
  const data = {
    members,
    type,
    createdAt: serverTimestamp()
  };
  if (type === "group") data.name = name;
  return data;
}

test("a signed-in user can create and read their own public profile", async () => {
  const alice = userDb("alice");
  const profile = doc(alice, "users", "alice");

  await assertSucceeds(setDoc(profile, {
    uid: "alice",
    name: "Alice",
    updatedAt: serverTimestamp()
  }));

  await assertSucceeds(getDoc(profile));
});

test("another signed-in user can look up a public profile but cannot list profiles", async () => {
  const alice = userDb("alice");
  const bob = userDb("bob");

  await assertSucceeds(setDoc(doc(alice, "users", "alice"), {
    uid: "alice",
    name: "Alice",
    updatedAt: serverTimestamp()
  }));

  await assertSucceeds(getDoc(doc(bob, "users", "alice")));
  await assertFails(getDocs(collection(bob, "users")));
});

test("only the owner can read a private phone-number document", async () => {
  const alice = userDb("alice");
  const bob = userDb("bob");
  const privateProfile = doc(alice, "privateUsers", "alice");

  await assertSucceeds(setDoc(privateProfile, {
    uid: "alice",
    phone: "+8801712345678",
    updatedAt: serverTimestamp()
  }));

  await assertSucceeds(getDoc(doc(alice, "privateUsers", "alice")));
  await assertFails(getDoc(doc(bob, "privateUsers", "alice")));
  await assertFails(getDocs(collection(bob, "privateUsers")));
});

test("a group with 20 members including its creator can be created", async () => {
  const alice = userDb("alice");
  const members = ["alice", ...Array.from({ length: 19 }, (_, i) => `member-${i + 1}`)];

  await assertSucceeds(setDoc(
    doc(alice, "chats", "group-20"),
    chatData(members)
  ));
});

test("a group with 21 members is rejected", async () => {
  const alice = userDb("alice");
  const members = ["alice", ...Array.from({ length: 20 }, (_, i) => `member-${i + 1}`)];

  await assertFails(setDoc(
    doc(alice, "chats", "group-21"),
    chatData(members)
  ));
});

test("a user cannot create a chat that does not include themselves", async () => {
  const alice = userDb("alice");

  await assertFails(setDoc(
    doc(alice, "chats", "not-my-chat"),
    chatData(["bob", "carol"])
  ));
});

test("a group member can send a message but a non-member cannot", async () => {
  const alice = userDb("alice");
  const mallory = userDb("mallory");

  await assertSucceeds(setDoc(
    doc(alice, "chats", "members-only"),
    chatData(["alice", "bob"])
  ));

  const messages = collection(alice, "chats", "members-only", "messages");
  await assertSucceeds(addDoc(messages, {
    sender: "alice",
    text: "Hello",
    createdAt: serverTimestamp()
  }));

  await assertFails(addDoc(
    collection(mallory, "chats", "members-only", "messages"),
    {
      sender: "mallory",
      text: "I should not be here",
      createdAt: serverTimestamp()
    }
  ));
});

test("a member query is allowed and a broad chat-list query is rejected", async () => {
  const alice = userDb("alice");
  await assertSucceeds(setDoc(
    doc(alice, "chats", "query-test"),
    chatData(["alice", "bob"])
  ));

  await assertSucceeds(getDocs(query(
    collection(alice, "chats"),
    where("members", "array-contains", "alice")
  )));
  await assertFails(getDocs(collection(alice, "chats")));
});

test("chat membership and existing message content cannot be edited", async () => {
  const alice = userDb("alice");
  const chatRef = doc(alice, "chats", "immutable-chat");

  await assertSucceeds(setDoc(chatRef, chatData(["alice", "bob"])));
  await assertFails(updateDoc(chatRef, { members: ["alice", "mallory"] }));

  const messageRef = doc(alice, "chats", "immutable-chat", "messages", "message-1");
  await assertSucceeds(setDoc(messageRef, {
    sender: "alice",
    text: "Original",
    createdAt: serverTimestamp()
  }));
  await assertFails(updateDoc(messageRef, { text: "Edited" }));
});
