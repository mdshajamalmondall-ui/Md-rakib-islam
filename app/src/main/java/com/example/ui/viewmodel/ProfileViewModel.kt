package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.repository.NursingDataRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
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

data class ProfileSaveState(
    val isSaving: Boolean = false,
    val feedbackMessage: String? = null
)

class ProfileViewModel(
    private val repository: NursingDataRepository
) : ViewModel() {

    val profileState: StateFlow<UiState<UserProfile?>> = repository.observeProfile()
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            emit(UiState.Error(root.message ?: "প্রোফাইল তথ্য লোড করা যায়নি।"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    private val _saveState = MutableStateFlow(ProfileSaveState())
    val saveState: StateFlow<ProfileSaveState> = _saveState.asStateFlow()

    init {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                repository.ensureInitialProfile(
                    displayName = currentUser.displayName ?: "নার্সিং ব্যবহারকারী",
                    email = currentUser.email ?: ""
                )
            }
        }
    }

    fun clearFeedback() {
        _saveState.update { it.copy(feedbackMessage = null) }
    }

    fun updateProfile(
        displayName: String,
        email: String,
        designation: String,
        institution: String,
        ward: String,
        registrationId: String,
        bio: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _saveState.update { it.copy(isSaving = true, feedbackMessage = null) }
            repository.updateProfile(
                displayName = displayName,
                email = email,
                designation = designation,
                institution = institution,
                ward = ward,
                registrationId = registrationId,
                bio = bio
            ).onSuccess {
                _saveState.update {
                    it.copy(isSaving = false, feedbackMessage = "প্রোফাইল সফলভাবে আপডেট হয়েছে।")
                }
                onComplete()
            }.onFailure { err ->
                _saveState.update {
                    it.copy(
                        isSaving = false,
                        feedbackMessage = err.localizedMessage ?: "প্রোফাইল সংরক্ষণ ব্যর্থ হয়েছে।"
                    )
                }
            }
        }
    }

    companion object {
        fun factory(repository: NursingDataRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(repository) as T
                }
            }
    }
}
