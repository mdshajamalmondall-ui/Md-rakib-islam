const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
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

function validProfilePayload(uid, ts = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    displayName: "রাহিমা খাতুন",
    email: "rahima@example.com",
    designation: "সিনিয়র স্টাফ নার্স",
    institution: "ঢাকা মেডিকেল কলেজ হাসপাতাল",
    ward: "সার্জিক্যাল ওয়ার্ড",
    registrationId: "BNMC-45210",
    bio: "মেডিকেল ও সার্জিক্যাল নার্সিং",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validNotePayload(uid, ts = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    title: "পেপটিক আলসার নার্সিং কেয়ার",
    content: "H. pylori এবং স্ট্রেসজনিত কারণে পেপটিক আলসার হয়।",
    category: "মেডিকেল সার্জিক্যাল",
    sourceType: "AI_ANSWER",
    depthMode: "STANDARD",
    pinned: false,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validMedicationPayload(uid, ts = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    medicineName: "Ceftriaxone",
    dose: "1 g",
    route: "IV",
    frequency: "BD",
    scheduleTime: "08:00 AM, 08:00 PM",
    patientLabel: "বেড ০৪",
    notes: "ধীরে ধীরে IV পুশ দিতে হবে",
    active: true,
    dosesGivenCount: 0,
    createdAt: ts,
    updatedAt: ts,
  };
}

// --- Unauthenticated & Cross-User Isolation ---

test("Unauthenticated user cannot read or write profiles, notes, or medications", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("notes").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("medications").get()
  );
});

test("Cross-user access is strictly denied across profiles, notes, and medications", async () => {
  const ts = new Date(Date.now() - 10000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(BOB_UID).set(validProfilePayload(BOB_UID, ts));
    await db.collection("users").doc(BOB_UID).collection("notes").doc("note_bob").set(validNotePayload(BOB_UID, ts));
    await db.collection("users").doc(BOB_UID).collection("medications").doc("med_bob").set(validMedicationPayload(BOB_UID, ts));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).update({ displayName: "Hijacked", updatedAt: new Date(Date.now() - 1000) }));
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("notes").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("notes").doc("note_bob").delete());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("medications").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("medications").doc("med_bob").delete());
});

// --- UserProfile CRUD & Security Bounds ---

test("Owner can create, read, and partially update UserProfile; rejects tampered createdAt/userId/future timestamp", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);

  await assertSucceeds(profileRef.set(validProfilePayload(ALICE_UID, created)));
  await assertSucceeds(profileRef.get());

  // Partial update allowed keys
  await assertSucceeds(
    profileRef.update({
      ward: "আইসিইউ",
      designation: "ইনচার্জ নার্স",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Cannot list /users collection
  await assertFails(aliceDb.collection("users").get());

  // Cannot mutate createdAt or userId
  await assertFails(
    profileRef.update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    profileRef.update({
      userId: BOB_UID,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost field on create
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      ...validProfilePayload(BOB_UID, created),
      id: BOB_UID,
    })
  );

  // Reject future timestamp
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set(
      validProfilePayload(BOB_UID, new Date(Date.now() + 60000))
    )
  );
});

// --- SavedNote CRUD & Query Alignment ---

test("Owner can create, list, pin, update, and delete SavedNote; enforces schema and immutability", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const notesCol = aliceDb.collection("users").doc(ALICE_UID).collection("notes");

  // List even when empty succeeds
  await assertSucceeds(notesCol.get());

  // Create AI_ANSWER note with depthMode
  await assertSucceeds(notesCol.doc("note_1").set(validNotePayload(ALICE_UID, created)));

  // Create PERSONAL_NOTE without optional depthMode
  await assertSucceeds(
    notesCol.doc("note_2").set({
      userId: ALICE_UID,
      title: "ওয়ার্ড নোট",
      content: "রোগীর ইনপুট-আউটপুট চার্ট",
      category: "ব্যক্তিগত নোট",
      sourceType: "PERSONAL_NOTE",
      pinned: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Pin toggle partial update
  await assertSucceeds(
    notesCol.doc("note_1").update({
      pinned: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Edit note content partial update
  await assertSucceeds(
    notesCol.doc("note_1").update({
      title: "আপডেটেড টাইটেল",
      content: "বিস্তারিত নার্সিং কেয়ার প্ল্যান",
      category: "নার্সিং কেয়ার প্ল্যান",
      pinned: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject @DocumentId 'id' field in payload
  await assertFails(
    notesCol.doc("note_bad_id").set({
      ...validNotePayload(ALICE_UID, created),
      id: "note_bad_id",
    })
  );

  // Reject invalid sourceType
  await assertFails(
    notesCol.doc("note_bad_src").set({
      ...validNotePayload(ALICE_UID, created),
      sourceType: "UNKNOWN",
    })
  );

  // Reject createdAt overwrite
  await assertFails(
    notesCol.doc("note_1").update({
      createdAt: new Date(Date.now() - 200),
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Delete succeeds
  await assertSucceeds(notesCol.doc("note_2").delete());
});

// --- MedicationItem CRUD, Dose Logging & Bounds ---

test("Owner can create, list, edit, mark dose given, toggle active, and delete MedicationItem", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);
  const medsCol = aliceDb.collection("users").doc(ALICE_UID).collection("medications");

  // List empty collection succeeds
  await assertSucceeds(medsCol.get());

  // Create without optional lastGivenAt
  await assertSucceeds(medsCol.doc("med_1").set(validMedicationPayload(ALICE_UID, created)));

  // Mark dose given partial update
  await assertSucceeds(
    medsCol.doc("med_1").update({
      dosesGivenCount: 1,
      lastGivenAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Toggle active partial update
  await assertSucceeds(
    medsCol.doc("med_1").update({
      active: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Edit details partial update
  await assertSucceeds(
    medsCol.doc("med_1").update({
      medicineName: "Ceftriaxone Sodium",
      dose: "2 g",
      route: "IV",
      frequency: "OD",
      scheduleTime: "09:00 AM",
      patientLabel: "বেড ০৫",
      notes: "নতুন অর্ডার",
      active: true,
      updatedAt: new Date(Date.now() - 200),
    })
  );

  // Reject negative dosesGivenCount
  await assertFails(
    medsCol.doc("med_1").update({
      dosesGivenCount: -1,
      lastGivenAt: new Date(Date.now() - 100),
      updatedAt: new Date(Date.now() - 100),
    })
  );

  // Reject invalid route
  await assertFails(
    medsCol.doc("med_bad_route").set({
      ...validMedicationPayload(ALICE_UID, created),
      route: "INTRATHECAL",
    })
  );

  // Reject ghost 'id' field
  await assertFails(
    medsCol.doc("med_bad_id").set({
      ...validMedicationPayload(ALICE_UID, created),
      id: "med_bad_id",
    })
  );

  // Delete succeeds
  await assertSucceeds(medsCol.doc("med_1").delete());
});
