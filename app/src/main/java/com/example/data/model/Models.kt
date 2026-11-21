package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class AnswerDepth(
    val labelBn: String,
    val subtitleBn: String,
    val badgeBn: String
) {
    BRIEF(
        labelBn = "সংক্ষিপ্ত",
        subtitleBn = "দ্রুত মূল পয়েন্ট",
        badgeBn = "সংক্ষিপ্ত উত্তর"
    ),
    STANDARD(
        labelBn = "মাঝারি",
        subtitleBn = "সহজ ও গোছানো ব্যাখ্যা",
        badgeBn = "সহজ ব্যাখ্যা"
    ),
    DETAILED(
        labelBn = "বিস্তারিত",
        subtitleBn = "গভীর ক্লিনিক্যাল ও নার্সিং কেয়ার",
        badgeBn = "বিস্তারিত"
    )
}

enum class NoteSourceType(val labelBn: String) {
    AI_ANSWER("AI উত্তর"),
    PERSONAL_NOTE("ব্যক্তিগত নোট");

    companion object {
        fun fromRaw(raw: String?): NoteSourceType =
            entries.firstOrNull { it.name == raw } ?: PERSONAL_NOTE
    }
}

enum class MedicationRoute(val labelBn: String) {
    IV("IV (শিরা)"),
    IM("IM (মাংসপেশি)"),
    PO("PO (মুখে)"),
    SC("SC (চামড়ার নিচে)"),
    NEB("Neb (নেবুলাইজার)"),
    TOPICAL("Topical (বাহ্যিক)"),
    OTHER("অন্যান্য");

    companion object {
        fun fromRaw(raw: String?): MedicationRoute =
            entries.firstOrNull { it.name == raw } ?: IV
    }
}

enum class MedicationFrequency(val labelBn: String) {
    OD("OD (দিনে ১ বার)"),
    BD("BD (দিনে ২ বার)"),
    TDS("TDS (দিনে ৩ বার)"),
    QID("QID (দিনে ৪ বার)"),
    Q8H("Q8H (৮ ঘণ্টা পরপর)"),
    Q12H("Q12H (১২ ঘণ্টা পরপর)"),
    STAT("STAT (তাৎক্ষণিক)"),
    PRN("PRN (প্রয়োজন অনুযায়ী)");

    companion object {
        fun fromRaw(raw: String?): MedicationFrequency =
            entries.firstOrNull { it.name == raw } ?: BD
    }
}

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val depth: AnswerDepth = AnswerDepth.STANDARD,
    val matchedBookSections: String? = null,
    val isStreaming: Boolean = false,
    val isError: Boolean = false,
    val savedToNotes: Boolean = false,
    val queryForTitle: String = ""
)

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val designation: String = "",
    val institution: String = "",
    val ward: String = "",
    val registrationId: String = "",
    val bio: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "displayName" to displayName.take(100),
        "email" to email.take(150),
        "designation" to designation.take(100),
        "institution" to institution.take(150),
        "ward" to ward.take(100),
        "registrationId" to registrationId.take(80),
        "bio" to bio.take(500),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class SavedNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "সাধারণ",
    val sourceType: String = NoteSourceType.PERSONAL_NOTE.name,
    val depthMode: String? = null,
    val pinned: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun resolvedSourceType(): NoteSourceType = NoteSourceType.fromRaw(sourceType)

    fun resolvedDepthMode(): AnswerDepth? =
        depthMode?.let { raw -> AnswerDepth.entries.firstOrNull { it.name == raw } }

    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title.take(200),
        "content" to content.take(15000),
        "category" to category.take(60),
        "sourceType" to NoteSourceType.fromRaw(sourceType).name,
        "depthMode" to depthMode,
        "pinned" to pinned,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null }.mapValues { it.value!! }
}

@Keep
@IgnoreExtraProperties
data class MedicationItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val medicineName: String = "",
    val dose: String = "",
    val route: String = MedicationRoute.IV.name,
    val frequency: String = MedicationFrequency.BD.name,
    val scheduleTime: String = "",
    val patientLabel: String = "",
    val notes: String = "",
    val active: Boolean = true,
    val dosesGivenCount: Long = 0L,
    val lastGivenAt: Timestamp? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun resolvedRoute(): MedicationRoute = MedicationRoute.fromRaw(route)

    fun resolvedFrequency(): MedicationFrequency = MedicationFrequency.fromRaw(frequency)

    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "medicineName" to medicineName.take(120),
        "dose" to dose.take(80),
        "route" to MedicationRoute.fromRaw(route).name,
        "frequency" to MedicationFrequency.fromRaw(frequency).name,
        "scheduleTime" to scheduleTime.take(60),
        "patientLabel" to patientLabel.take(100),
        "notes" to notes.take(1000),
        "active" to active,
        "dosesGivenCount" to dosesGivenCount.coerceIn(0L, 100000L),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}
