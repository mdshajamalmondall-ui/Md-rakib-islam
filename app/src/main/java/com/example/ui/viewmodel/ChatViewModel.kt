package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AnswerDepth
import com.example.data.model.ChatMessage
import com.example.data.model.NoteSourceType
import com.example.data.repository.AiNursingRepository
import com.example.data.repository.AiStreamEvent
import com.example.data.repository.NursingDataRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

data class ChatUiState(
    val queryInput: String = "",
    val selectedDepth: AnswerDepth = AnswerDepth.STANDARD,
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val statusBanner: String? = null
)

class ChatViewModel(
    private val aiRepository: AiNursingRepository,
    private val dataRepository: NursingDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var activeJob: Job? = null

    fun onQueryChanged(value: String) {
        _uiState.update { it.copy(queryInput = value) }
    }

    fun onDepthSelected(depth: AnswerDepth) {
        _uiState.update { it.copy(selectedDepth = depth) }
    }

    fun clearBanner() {
        _uiState.update { it.copy(statusBanner = null) }
    }

    fun clearChat() {
        activeJob?.cancel()
        _uiState.update {
            it.copy(
                messages = emptyList(),
                isGenerating = false,
                statusBanner = null
            )
        }
    }

    fun sendQuestion(customQuestion: String? = null) {
        val question = (customQuestion ?: _uiState.value.queryInput).trim()
        if (question.isEmpty() || _uiState.value.isGenerating) return

        val depth = _uiState.value.selectedDepth
        val historyContext = _uiState.value.messages
            .filter { !it.isError && !it.isStreaming && it.text.isNotBlank() }
            .takeLast(4)
            .joinToString("\n") { msg ->
                val role = if (msg.isUser) "ব্যবহারকারী" else "নার্সিং এআই"
                "$role: ${msg.text.take(350)}"
            }

        val userMsgId = UUID.randomUUID().toString()
        val aiMsgId = UUID.randomUUID().toString()

        val userMessage = ChatMessage(
            id = userMsgId,
            isUser = true,
            text = question,
            depth = depth
        )
        val placeholderAi = ChatMessage(
            id = aiMsgId,
            isUser = false,
            text = "",
            depth = depth,
            isStreaming = true,
            queryForTitle = question
        )

        _uiState.update {
            it.copy(
                queryInput = if (customQuestion == null) "" else it.queryInput,
                messages = it.messages + userMessage + placeholderAi,
                isGenerating = true,
                statusBanner = null
            )
        }

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            try {
                aiRepository.streamNursingAnswer(
                    question = question,
                    depth = depth,
                    conversationContext = historyContext
                ).collect { event ->
                    when (event) {
                        is AiStreamEvent.Chunk -> {
                            updateMessage(aiMsgId) {
                                it.copy(
                                    text = event.accumulatedText,
                                    matchedBookSections = event.bookCitation,
                                    isStreaming = true
                                )
                            }
                        }

                        is AiStreamEvent.Completed -> {
                            updateMessage(aiMsgId) {
                                it.copy(
                                    text = event.fullText,
                                    matchedBookSections = event.bookCitation,
                                    isStreaming = false
                                )
                            }
                            _uiState.update { it.copy(isGenerating = false) }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateMessage(aiMsgId) {
                    it.copy(
                        text = e.message ?: "উত্তর তৈরি করতে সমস্যা হয়েছে। আবার চেষ্টা করুন।",
                        isStreaming = false,
                        isError = true
                    )
                }
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    fun saveAnswerToFiles(messageId: String) {
        val target = _uiState.value.messages.firstOrNull { it.id == messageId } ?: return
        if (target.isUser || target.isError || target.text.isBlank() || target.savedToNotes) return

        val suggestedTitle = target.queryForTitle
            .ifBlank { target.text.lineSequence().firstOrNull().orEmpty() }
            .take(80)
            .ifBlank { "নার্সিং এআই উত্তর" }

        val category = if (!target.matchedBookSections.isNullOrBlank()) {
            "মেডিকেল সার্জিক্যাল"
        } else {
            "নার্সিং এআই গাইড"
        }

        viewModelScope.launch {
            val result = dataRepository.addNote(
                title = suggestedTitle,
                content = buildString {
                    if (!target.matchedBookSections.isNullOrBlank()) {
                        append("[সূত্র: ${target.matchedBookSections}]\n\n")
                    }
                    append(target.text)
                },
                category = category,
                sourceType = NoteSourceType.AI_ANSWER,
                depthMode = target.depth.name,
                pinned = false
            )
            result.onSuccess {
                updateMessage(messageId) { it.copy(savedToNotes = true) }
                _uiState.update { it.copy(statusBanner = "উত্তরটি 'ফাইল' ট্যাবে সেভ করা হয়েছে।") }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(statusBanner = "সেভ করতে ব্যর্থ: ${err.localizedMessage ?: "ত্রুটি"}")
                }
            }
        }
    }

    private fun updateMessage(id: String, transform: (ChatMessage) -> ChatMessage) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == id) transform(msg) else msg
                }
            )
        }
    }

    companion object {
        fun factory(
            aiRepository: AiNursingRepository,
            dataRepository: NursingDataRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(aiRepository, dataRepository) as T
            }
        }
    }
}
