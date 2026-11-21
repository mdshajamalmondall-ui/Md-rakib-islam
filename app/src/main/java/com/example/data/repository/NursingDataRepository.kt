package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.MedicationFrequency
import com.example.data.model.MedicationItem
import com.example.data.model.MedicationRoute
import com.example.data.model.NoteSourceType
import com.example.data.model.SavedNote
import com.example.data.model.UserProfile
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class NursingDataRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("লগইন ছাড়া তথ্য অ্যাক্সেস করা যাবে না।")
    }

    // --- User Profile ---

    fun observeProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureInitialProfile(
        displayName: String,
        email: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val safeName = displayName.trim().ifBlank { "নার্সিং ব্যবহারকারী" }.take(100)
                val profile = UserProfile(
                    userId = uid,
                    displayName = safeName,
                    email = email.trim().take(150),
                    designation = "সিনিয়র স্টাফ নার্স / শিক্ষার্থী",
                    institution = "",
                    ward = "",
                    registrationId = "",
                    bio = ""
                )
                docRef.set(profile.toCreateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        displayName: String,
        email: String,
        designation: String,
        institution: String,
        ward: String,
        registrationId: String,
        bio: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val trimmedName = displayName.trim()
            require(trimmedName.isNotEmpty()) { "নাম খালি রাখা যাবে না।" }

            val docRef = db.collection("users").document(uid)
            docPath = docRef.path
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "displayName" to trimmedName.take(100),
                        "email" to email.trim().take(150),
                        "designation" to designation.trim().take(100),
                        "institution" to institution.trim().take(150),
                        "ward" to ward.trim().take(100),
                        "registrationId" to registrationId.trim().take(80),
                        "bio" to bio.trim().take(500),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val profile = UserProfile(
                    userId = uid,
                    displayName = trimmedName.take(100),
                    email = email.trim().take(150),
                    designation = designation.trim().take(100),
                    institution = institution.trim().take(150),
                    ward = ward.trim().take(100),
                    registrationId = registrationId.trim().take(80),
                    bio = bio.trim().take(500)
                )
                docRef.set(profile.toCreateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    // --- Saved Notes & Files ---

    fun observeNotes(): Flow<List<SavedNote>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/notes"
        emitAll(
            db.collection("users").document(uid).collection("notes")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SavedNote::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareByDescending<SavedNote> { it.pinned }
                            .thenByDescending { it.updatedAt?.seconds ?: 0L }
                            .thenByDescending { it.createdAt?.seconds ?: 0L }
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addNote(
        title: String,
        content: String,
        category: String,
        sourceType: NoteSourceType,
        depthMode: String? = null,
        pinned: Boolean = false
    ): Result<String> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/notes"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim()
            val cleanContent = content.trim()
            val cleanCategory = category.trim().ifBlank { "সাধারণ" }
            require(cleanTitle.isNotEmpty()) { "নোটের শিরোনাম দিন।" }
            require(cleanContent.isNotEmpty()) { "নোটের বিষয়বস্তু লিখুন।" }

            val docRef = db.collection("users").document(uid).collection("notes").document()
            docPath = docRef.path
            val note = SavedNote(
                userId = uid,
                title = cleanTitle.take(200),
                content = cleanContent.take(15000),
                category = cleanCategory.take(60),
                sourceType = sourceType.name,
                depthMode = depthMode?.takeIf { it in listOf("BRIEF", "STANDARD", "DETAILED") },
                pinned = pinned
            )
            docRef.set(note.toCreateMap()).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateNote(
        noteId: String,
        title: String,
        content: String,
        category: String,
        pinned: Boolean
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/notes/$noteId"
        return try {
            val uid = requireUserId()
            require(noteId.isNotBlank()) { "Note ID খালি হতে পারবে না।" }
            val cleanTitle = title.trim()
            val cleanContent = content.trim()
            val cleanCategory = category.trim().ifBlank { "সাধারণ" }
            require(cleanTitle.isNotEmpty()) { "নোটের শিরোনাম দিন।" }
            require(cleanContent.isNotEmpty()) { "নোটের বিষয়বস্তু লিখুন।" }

            val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "title" to cleanTitle.take(200),
                    "content" to cleanContent.take(15000),
                    "category" to cleanCategory.take(60),
                    "pinned" to pinned,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun togglePinNote(noteId: String, pinned: Boolean): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/notes/$noteId"
        return try {
            val uid = requireUserId()
            require(noteId.isNotBlank())
            val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "pinned" to pinned,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/notes/$noteId"
        return try {
            val uid = requireUserId()
            require(noteId.isNotBlank())
            val docRef = db.collection("users").document(uid).collection("notes").document(noteId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    // --- Medications & Dose Maintenance ---

    fun observeMedications(): Flow<List<MedicationItem>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/medications"
        emitAll(
            db.collection("users").document(uid).collection("medications")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        MedicationItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareByDescending<MedicationItem> { it.active }
                            .thenByDescending { it.updatedAt?.seconds ?: 0L }
                            .thenByDescending { it.createdAt?.seconds ?: 0L }
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addMedication(
        medicineName: String,
        dose: String,
        route: MedicationRoute,
        frequency: MedicationFrequency,
        scheduleTime: String,
        patientLabel: String,
        notes: String
    ): Result<String> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/medications"
        return try {
            val uid = requireUserId()
            val cleanName = medicineName.trim()
            val cleanDose = dose.trim()
            require(cleanName.isNotEmpty()) { "ওষুধের নাম লিখুন।" }
            require(cleanDose.isNotEmpty()) { "ডোজের পরিমাণ লিখুন (যেমন: 500 mg)।" }

            val docRef = db.collection("users").document(uid).collection("medications").document()
            docPath = docRef.path
            val item = MedicationItem(
                userId = uid,
                medicineName = cleanName.take(120),
                dose = cleanDose.take(80),
                route = route.name,
                frequency = frequency.name,
                scheduleTime = scheduleTime.trim().take(60),
                patientLabel = patientLabel.trim().take(100),
                notes = notes.trim().take(1000),
                active = true,
                dosesGivenCount = 0L
            )
            docRef.set(item.toCreateMap()).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateMedication(
        medId: String,
        medicineName: String,
        dose: String,
        route: MedicationRoute,
        frequency: MedicationFrequency,
        scheduleTime: String,
        patientLabel: String,
        notes: String,
        active: Boolean
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/medications/$medId"
        return try {
            val uid = requireUserId()
            require(medId.isNotBlank())
            val cleanName = medicineName.trim()
            val cleanDose = dose.trim()
            require(cleanName.isNotEmpty()) { "ওষুধের নাম লিখুন।" }
            require(cleanDose.isNotEmpty()) { "ডোজের পরিমাণ লিখুন।" }

            val docRef = db.collection("users").document(uid).collection("medications").document(medId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "medicineName" to cleanName.take(120),
                    "dose" to cleanDose.take(80),
                    "route" to route.name,
                    "frequency" to frequency.name,
                    "scheduleTime" to scheduleTime.trim().take(60),
                    "patientLabel" to patientLabel.trim().take(100),
                    "notes" to notes.trim().take(1000),
                    "active" to active,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun markDoseGiven(medId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/medications/$medId"
        return try {
            val uid = requireUserId()
            require(medId.isNotBlank())
            val docRef = db.collection("users").document(uid).collection("medications").document(medId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "dosesGivenCount" to FieldValue.increment(1L),
                    "lastGivenAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleMedicationActive(medId: String, active: Boolean): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/medications/$medId"
        return try {
            val uid = requireUserId()
            require(medId.isNotBlank())
            val docRef = db.collection("users").document(uid).collection("medications").document(medId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "active" to active,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteMedication(medId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/medications/$medId"
        return try {
            val uid = requireUserId()
            require(medId.isNotBlank())
            val docRef = db.collection("users").document(uid).collection("medications").document(medId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}
