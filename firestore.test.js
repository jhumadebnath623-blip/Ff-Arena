const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_001";
const ADMIN_EMAIL = "jhumadebnath623@gmail.com";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function adminContext() {
  return testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  });
}

function spoofedAdminContext() {
  return testEnv.authenticatedContext("spoof_999", {
    email: ADMIN_EMAIL,
    email_verified: false,
  });
}

function playerContext(uid) {
  return testEnv.authenticatedContext(uid, {
    email: `${uid}@gmail.com`,
    email_verified: true,
  });
}

function sampleTournament(createdBy = ADMIN_UID) {
  const past = new Date(Date.now() - 2000);
  const futureMatch = Date.now() + 3600000;
  return {
    title: "Bermuda Pro Scrims #1",
    mode: "SQUAD",
    category: "BATTLE_ROYALE",
    status: "UPCOMING",
    bannerKey: "battle_royale",
    mapName: "Bermuda",
    matchTimeMillis: futureMatch,
    roomOpenTimeMillis: futureMatch - 900000,
    maxSlots: 12,
    registeredSlots: 0,
    entryType: "FREE",
    prizeDetails: "Championship Trophy & MVP Crown",
    rulesText: "No emulators, no teaming, level 30+ accounts only.",
    featured: true,
    createdBy,
    createdAt: past,
    updatedAt: past,
  };
}

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

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("tournaments").get());
  await assertFails(unauthDb.collection("announcements").get());
  await assertFails(unauthDb.collection("leaderboard").get());
  await assertFails(unauthDb.collection("public_players").get());
  await assertFails(unauthDb.collection("players").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("tournaments").doc("t1").set(sampleTournament())
  );
});

test("Email spoofing with unverified admin email is rejected", async () => {
  const spoofDb = spoofedAdminContext().firestore();
  await assertFails(
    spoofDb.collection("tournaments").doc("t1").set(sampleTournament("spoof_999"))
  );
});

test("Admin can create/update tournaments, announcements, and room credentials", async () => {
  const adminDb = adminContext().firestore();
  const past = new Date(Date.now() - 2000);

  await assertSucceeds(
    adminDb.collection("tournaments").doc("t1").set(sampleTournament())
  );
  await assertSucceeds(
    adminDb
      .collection("tournaments")
      .doc("t1")
      .collection("room_access")
      .doc("credentials")
      .set({
        tournamentId: "t1",
        roomId: "88442211",
        roomPassword: "FFARENA",
        notes: "Join 10 mins early.",
        updatedBy: ADMIN_UID,
        createdAt: past,
        updatedAt: past,
      })
  );
  await assertSucceeds(
    adminDb.collection("announcements").doc("a1").set({
      title: "Match Day Live",
      message: "Bermuda Pro Scrims #1 opens soon.",
      type: "NEW_TOURNAMENT",
      tournamentId: "t1",
      createdBy: ADMIN_UID,
      createdAt: past,
    })
  );
});

test("PII isolation: players/{uid} is restricted to owner & admin; public_players prevents stat tampering", async () => {
  const aliceDb = playerContext(ALICE_UID).firestore();
  const bobDb = playerContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 2000);

  await assertSucceeds(
    aliceDb.collection("public_players").doc(ALICE_UID).set({
      userId: ALICE_UID,
      playerName: "Alice",
      ign: "ALICE_OP",
      freeFireUid: "123456789",
      avatarKey: "cyber_red",
      totalWins: 0,
      totalKills: 0,
      matchesPlayed: 0,
      totalPoints: 0,
      achievements: [],
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    aliceDb.collection("players").doc(ALICE_UID).set({
      userId: ALICE_UID,
      playerName: "Alice",
      ign: "ALICE_OP",
      freeFireUid: "123456789",
      whatsappNumber: "+919999999999",
      email: "alice_123@gmail.com",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Bob cannot read Alice's private PII or list players collection
  await assertFails(bobDb.collection("players").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("players").get());

  // Alice cannot tamper with her own stats or inject ghost fields
  await assertFails(
    aliceDb.collection("public_players").doc(ALICE_UID).update({
      totalWins: 99,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("public_players").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Alice can update her own profile fields
  await assertSucceeds(
    aliceDb.collection("public_players").doc(ALICE_UID).update({
      ign: "ALICE_PRO",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Registration, atomic slot reservation, and strict Room Credentials gating", async () => {
  const adminDb = adminContext().firestore();
  const aliceDb = playerContext(ALICE_UID).firestore();
  const bobDb = playerContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 2000);

  await assertSucceeds(
    adminDb.collection("tournaments").doc("t1").set(sampleTournament())
  );
  await assertSucceeds(
    adminDb
      .collection("tournaments")
      .doc("t1")
      .collection("room_access")
      .doc("credentials")
      .set({
        tournamentId: "t1",
        roomId: "77665544",
        roomPassword: "ARENA99",
        notes: "Squad slot #3",
        updatedBy: ADMIN_UID,
        createdAt: past,
        updatedAt: past,
      })
  );

  // Before registering, Alice & Bob CANNOT read room credentials
  await assertFails(
    aliceDb
      .collection("tournaments")
      .doc("t1")
      .collection("room_access")
      .doc("credentials")
      .get()
  );

  // Alice cannot self-approve registration
  const regId = `t1_${ALICE_UID}`;
  await assertFails(
    aliceDb.collection("registrations").doc(regId).set({
      userId: ALICE_UID,
      tournamentId: "t1",
      tournamentTitle: "Bermuda Pro Scrims #1",
      mode: "SQUAD",
      playerName: "Alice",
      ign: "ALICE_OP",
      freeFireUid: "123456789",
      whatsappNumber: "+919999999999",
      teamName: "Team Phoenix",
      teammateIgns: ["Mate1", "Mate2", "Mate3"],
      teammateUids: ["111111", "222222", "333333"],
      status: "APPROVED",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Alice performs valid batched registration + atomic slot increment
  const batch = aliceDb.batch();
  batch.set(aliceDb.collection("registrations").doc(regId), {
    userId: ALICE_UID,
    tournamentId: "t1",
    tournamentTitle: "Bermuda Pro Scrims #1",
    mode: "SQUAD",
    playerName: "Alice",
    ign: "ALICE_OP",
    freeFireUid: "123456789",
    whatsappNumber: "+919999999999",
    teamName: "Team Phoenix",
    teammateIgns: ["Mate1", "Mate2", "Mate3"],
    teammateUids: ["111111", "222222", "333333"],
    status: "PENDING",
    createdAt: past,
    updatedAt: past,
  });
  batch.update(aliceDb.collection("tournaments").doc("t1"), {
    registeredSlots: 1,
    updatedAt: past,
  });
  await assertSucceeds(batch.commit());

  // While PENDING, Alice STILL cannot read room credentials
  await assertFails(
    aliceDb
      .collection("tournaments")
      .doc("t1")
      .collection("room_access")
      .doc("credentials")
      .get()
  );

  // Bob cannot list all registrations or read Alice's registration
  await assertFails(bobDb.collection("registrations").get());
  await assertFails(bobDb.collection("registrations").doc(regId).get());

  // Alice can list her own registrations with userId filter
  await assertSucceeds(
    aliceDb.collection("registrations").where("userId", "==", ALICE_UID).get()
  );

  // Admin approves Alice
  await assertSucceeds(
    adminDb.collection("registrations").doc(regId).update({
      status: "APPROVED",
      adminNote: "Verified Squad",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Now Alice CAN read room credentials, while Bob STILL CANNOT!
  await assertSucceeds(
    aliceDb
      .collection("tournaments")
      .doc("t1")
      .collection("room_access")
      .doc("credentials")
      .get()
  );
  await assertFails(
    bobDb
      .collection("tournaments")
      .doc("t1")
      .collection("room_access")
      .doc("credentials")
      .get()
  );
});

test("Leaderboard & personal notifications isolation", async () => {
  const adminDb = adminContext().firestore();
  const aliceDb = playerContext(ALICE_UID).firestore();
  const bobDb = playerContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 2000);

  // Only admin can write leaderboard
  await assertFails(
    aliceDb.collection("leaderboard").doc("t1_alice").set({
      tournamentId: "t1",
      tournamentTitle: "Bermuda Pro Scrims #1",
      userId: ALICE_UID,
      playerName: "Alice",
      ign: "ALICE_OP",
      teamName: "Team Phoenix",
      freeFireUid: "123456789",
      kills: 12,
      placementRank: 1,
      positionPoints: 12,
      totalPoints: 24,
      winnerBadge: "CHAMPION",
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    adminDb.collection("leaderboard").doc("t1_alice").set({
      tournamentId: "t1",
      tournamentTitle: "Bermuda Pro Scrims #1",
      userId: ALICE_UID,
      playerName: "Alice",
      ign: "ALICE_OP",
      teamName: "Team Phoenix",
      freeFireUid: "123456789",
      kills: 12,
      placementRank: 1,
      positionPoints: 12,
      totalPoints: 24,
      winnerBadge: "CHAMPION",
      createdAt: past,
      updatedAt: past,
    })
  );

  // Personal notifications: admin creates, Alice reads & marks readFlag, Bob cannot access
  await assertSucceeds(
    adminDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("notifications")
      .doc("n1")
      .set({
        title: "Registration Approved",
        message: "You are in!",
        type: "APPROVAL",
        tournamentId: "t1",
        readFlag: false,
        createdAt: past,
      })
  );

  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("notifications").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notifications").get()
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("notifications")
      .doc("n1")
      .update({ readFlag: true })
  );
});
