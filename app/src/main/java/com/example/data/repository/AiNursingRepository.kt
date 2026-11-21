package com.example.data.repository

import android.util.Log
import com.example.data.knowledge.MedicalSurgicalKnowledgeBase
import com.example.data.knowledge.RetrievalResult
import com.example.data.model.AnswerDepth
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "AiNursingRepository"

sealed interface AiStreamEvent {
    data class Chunk(val accumulatedText: String, val bookCitation: String?) : AiStreamEvent
    data class Completed(val fullText: String, val bookCitation: String?) : AiStreamEvent
}

class AiNursingRepository {
    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    fun streamNursingAnswer(
        question: String,
        depth: AnswerDepth,
        conversationContext: String
    ): Flow<AiStreamEvent> = flow {
        val retrieval: RetrievalResult = MedicalSurgicalKnowledgeBase.retrieveRelevantContext(question)
        val prompt = buildNursingPrompt(
            question = question,
            depth = depth,
            retrieval = retrieval,
            conversationContext = conversationContext
        )

        val accumulatedText = StringBuilder()
        try {
            withTimeout(60_000L) {
                model.generateContentStream(prompt).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotEmpty()) {
                        accumulatedText.append(chunkText)
                        emit(
                            AiStreamEvent.Chunk(
                                accumulatedText = accumulatedText.toString(),
                                bookCitation = retrieval.citationLabel
                            )
                        )
                    }
                }
            }
            val finalText = accumulatedText.toString().trim()
            if (finalText.isBlank()) {
                throw IllegalStateException("AI থেকে কোনো উত্তর পাওয়া যায়নি। অনুগ্রহ করে আবার চেষ্টা করুন।")
            }
            emit(
                AiStreamEvent.Completed(
                    fullText = finalText,
                    bookCitation = retrieval.citationLabel
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI generation timed out after 60 seconds", e)
            throw IllegalStateException("উত্তর তৈরি করতে ৬০ সেকেন্ডের বেশি সময় লেগেছে। আবার চেষ্টা করুন।", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI generation failed", e)
            throw IllegalStateException(
                e.localizedMessage ?: "AI উত্তর তৈরি করতে সমস্যা হয়েছে। ইন্টারনেট সংযোগ পরীক্ষা করুন।",
                e
            )
        }
    }

    private fun buildNursingPrompt(
        question: String,
        depth: AnswerDepth,
        retrieval: RetrievalResult,
        conversationContext: String
    ): String {
        val depthInstruction = when (depth) {
            AnswerDepth.BRIEF -> """
                উত্তরের ধরন: সংক্ষিপ্ত (Brief / Quick Recall)
                - ৪-৬টি সহজ বুলেট পয়েন্টে সরাসরি মূল উত্তর দাও (সর্বোচ্চ ১২০ শব্দ)।
                - একদম প্রয়োজনীয় সংজ্ঞা, প্রধান লক্ষণ এবং সবচেয়ে গুরুত্বপূর্ণ নার্সিং অ্যাকশন উল্লেখ করো।
            """.trimIndent()

            AnswerDepth.STANDARD -> """
                উত্তরের ধরন: মাঝারি ও সহজ (Standard / Easy-to-Understand)
                - পরিষ্কার বাংলায় সহজ ভাষায় ধাপে ধাপে ব্যাখ্যা দাও (প্রায় ২০০-৩২০ শব্দ)।
                - প্রয়োজনীয় ইংরেজি মেডিকেল টার্ম পাশে বন্ধনীতে রাখো (যেমন: পেপটিক আলসার / Peptic Ulcer)।
                - বিভাগ রাখো: ১) মূল ধারণা/কারণ, ২) প্রধান লক্ষণ, ৩) জরুরি নার্সিং কেয়ার ও সতর্কতা।
            """.trimIndent()

            AnswerDepth.DETAILED -> """
                উত্তরের ধরন: বিস্তারিত ও গভীর (Detailed / Clinical & Exam Mastery)
                - একজন সিনিয়র নার্স ও নার্সিং পরীক্ষার্থীর উপযোগী করে গভীরভাবে ব্যাখ্যা করো।
                - বিভাগ রাখো:
                  ১) সংজ্ঞা ও কারণ (Definition & Etiology / Pathophysiology)
                  ২) ক্লিনিক্যাল লক্ষণ (Signs & Symptoms)
                  ৩) পরীক্ষা-নিরীক্ষা (Diagnostic Investigations)
                  ৪) মেডিকেল ও সার্জিক্যাল চিকিৎসা (Medical / Surgical Management)
                  ৫) নার্সিং কেয়ার প্ল্যান (Nursing Diagnosis, Interventions, Patient & Family Education)
            """.trimIndent()
        }

        val bookSection = if (retrieval.hasBookMatch) {
            """
            নিচে ব্যবহারকারীর Medical Surgical Nursing বই (${retrieval.citationLabel}) থেকে প্রাসঙ্গিক তথ্য দেওয়া হলো। মেডিকেল-সার্জিক্যাল প্রশ্নের উত্তরে এই বইয়ের তথ্যকে অগ্রাধিকার দিয়ে ব্যবহার করো এবং উত্তরের শেষে রেফারেন্স হিসেবে "${retrieval.citationLabel}" উল্লেখ করো:
            ---
            ${retrieval.contextBlock}
            ---
            """.trimIndent()
        } else {
            """
            যদি প্রশ্নটি মেডিকেল সার্জিক্যাল, ফার্মাকোলজি, ফান্ডামেন্টালস অব নার্সিং, বা ওয়ার্ড ম্যানেজমেন্ট বিষয়ক হয়, তবে প্রমাণ-ভিত্তিক নার্সিং গাইডলাইন ও ক্লিনিক্যাল সেফটি অনুযায়ী সঠিক উত্তর দাও।
            """.trimIndent()
        }

        val historyBlock = if (conversationContext.isNotBlank()) {
            """
            পূর্ববর্তী কথোপকথনের সারসংক্ষেপ:
            $conversationContext
            """.trimIndent()
        } else ""

        return """
            তুমি "নার্সিং এআই" — বাংলাদেশি নার্স এবং নার্সিং শিক্ষার্থীদের জন্য একজন বিশেষজ্ঞ ক্লিনিক্যাল নার্সিং অ্যাসিস্ট্যান্ট।
            তোমার উত্তর হবে বাংলায় (প্রয়োজনীয় মেডিকেল ও ফার্মাকোলজিক্যাল পরিভাষা ইংরেজিতে ব্র্যাকেটে রেখে), নির্ভুল, ক্লিনিক্যালি নিরাপদ এবং সহজবোধ্য।
            গুরুত্বপূর্ণ: ডোজ বা জরুরি ওষুধের ক্ষেত্রে সর্বদা চিকিৎসকের অর্ডার যাচাই ও রোগীর নিরাপত্তা (patient safety) মনে করিয়ে দেবে।

            $depthInstruction

            $bookSection

            $historyBlock

            ব্যবহারকারীর প্রশ্ন: $question
        """.trimIndent()
    }
}
