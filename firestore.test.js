const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read users profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create their own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Football",
      username: "alice_ftbl",
      bio: "Gunners fan!",
      favoriteClub: "Arsenal",
      createdAt: new Date(),
    })
  );
});

test("Cross-user access: Alice cannot update Bob's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob Silva",
      username: "bob_silva",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({
      displayName: "Hacked by Alice",
    })
  );
});

test("Chat isolation: Participant can read chat, non-participant is denied", async () => {
  const chatId = "chat_abc";
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("chats").doc(chatId).set({
      chatId,
      isGroup: false,
      memberUids: [ALICE_UID, BOB_UID],
      creatorUid: ALICE_UID,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("chats").doc(chatId).get());

  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  await assertFails(charlieDb.collection("chats").doc(chatId).get());
});

test("Messages: Participant can send message; non-participant cannot", async () => {
  const chatId = "chat_abc";
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("chats").doc(chatId).set({
      chatId,
      isGroup: false,
      memberUids: [ALICE_UID, BOB_UID],
      creatorUid: ALICE_UID,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("chats").doc(chatId).collection("messages").doc("msg_1").set({
      messageId: "msg_1",
      chatId,
      senderId: ALICE_UID,
      senderName: "Alice",
      type: "text",
      text: "Did you see that goal?!",
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: new Date(),
    })
  );

  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  await assertFails(
    charlieDb.collection("chats").doc(chatId).collection("messages").doc("msg_2").set({
      messageId: "msg_2",
      chatId,
      senderId: CHARLIE_UID,
      senderName: "Charlie",
      type: "text",
      text: "Invading chat",
      memberUids: [ALICE_UID, BOB_UID],
      createdAt: new Date(),
    })
  );
});

test("Calls: Participant can access call; non-participant is denied", async () => {
  const callId = "call_xyz";
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("calls").doc(callId).set({
      callId,
      callerId: ALICE_UID,
      receiverId: BOB_UID,
      callType: "video",
      status: "calling",
      participantUids: [ALICE_UID, BOB_UID],
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("calls").doc(callId).get());

  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  await assertFails(charlieDb.collection("calls").doc(callId).get());
});
