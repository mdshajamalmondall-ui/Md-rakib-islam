package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.NoteSourceType
import com.example.data.model.SavedNote
import com.example.data.model.UiState
import com.example.data.repository.NursingDataRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FilesFilter(val labelBn: String) {
    ALL("সব ফাইল"),
    AI_ONLY("AI উত্তর"),
    NOTES_ONLY("ব্যক্তিগত নোট")
}

data class FilesUiMeta(
    val searchQuery: String = "",
    val filter: FilesFilter = FilesFilter.ALL,
    val isSubmitting: Boolean = false,
    val bannerMessage: String? = null
)

class FilesViewModel(
    private val repository: NursingDataRepository
) : ViewModel() {

    val notesState: StateFlow<UiState<List<SavedNote>>> = repository.observeNotes()
        .map<List<SavedNote>, UiState<List<SavedNote>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            emit(UiState.Error(root.message ?: "সেভ করা ফাইলগুলো লোড করা যায়নি।"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    private val _meta = MutableStateFlow(FilesUiMeta())
    val meta: StateFlow<FilesUiMeta> = _meta.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _meta.update { it.copy(searchQuery = query) }
    }

    fun onFilterChanged(filter: FilesFilter) {
        _meta.update { it.copy(filter = filter) }
    }

    fun clearBanner() {
        _meta.update { it.copy(bannerMessage = null) }
    }

    fun saveNote(
        existingId: String?,
        title: String,
        content: String,
        category: String,
        pinned: Boolean,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _meta.update { it.copy(isSubmitting = true, bannerMessage = null) }
            val result = if (existingId.isNullOrBlank()) {
                repository.addNote(
                    title = title,
                    content = content,
                    category = category,
                    sourceType = NoteSourceType.PERSONAL_NOTE,
                    depthMode = null,
                    pinned = pinned
                ).map { }
            } else {
                repository.updateNote(
                    noteId = existingId,
                    title = title,
                    content = content,
                    category = category,
                    pinned = pinned
                )
            }
            result.onSuccess {
                _meta.update {
                    it.copy(
                        isSubmitting = false,
                        bannerMessage = if (existingId.isNullOrBlank()) "নতুন নোট সেভ হয়েছে।" else "নোট আপডেট করা হয়েছে।"
                    )
                }
                onComplete()
            }.onFailure { err ->
                _meta.update {
                    it.copy(
                        isSubmitting = false,
                        bannerMessage = err.localizedMessage ?: "নোট সেভ করা যায়নি।"
                    )
                }
            }
        }
    }

    fun togglePin(noteId: String, pinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinNote(noteId, pinned)
                .onFailure { err ->
                    _meta.update { it.copy(bannerMessage = err.localizedMessage ?: "পিন পরিবর্তন ব্যর্থ।") }
                }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
                .onSuccess {
                    _meta.update { it.copy(bannerMessage = "ফাইলটি মুছে ফেলা হয়েছে।") }
                }
                .onFailure { err ->
                    _meta.update { it.copy(bannerMessage = err.localizedMessage ?: "মুছে ফেলা যায়নি।") }
                }
        }
    }

    companion object {
        fun factory(repository: NursingDataRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FilesViewModel(repository) as T
                }
            }
    }
}
