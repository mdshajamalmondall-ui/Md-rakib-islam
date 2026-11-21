package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.MedicationFrequency
import com.example.data.model.MedicationItem
import com.example.data.model.MedicationRoute
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

data class ToolsFeedbackState(
    val isSubmitting: Boolean = false,
    val message: String? = null
)

class ToolsViewModel(
    private val repository: NursingDataRepository
) : ViewModel() {

    val medicationsState: StateFlow<UiState<List<MedicationItem>>> = repository.observeMedications()
        .map<List<MedicationItem>, UiState<List<MedicationItem>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            emit(UiState.Error(root.message ?: "ওষুধের তালিকা লোড করা যায়নি।"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    private val _feedback = MutableStateFlow(ToolsFeedbackState())
    val feedback: StateFlow<ToolsFeedbackState> = _feedback.asStateFlow()

    fun clearMessage() {
        _feedback.update { it.copy(message = null) }
    }

    fun saveMedication(
        existingId: String?,
        medicineName: String,
        dose: String,
        route: MedicationRoute,
        frequency: MedicationFrequency,
        scheduleTime: String,
        patientLabel: String,
        notes: String,
        active: Boolean,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _feedback.update { it.copy(isSubmitting = true, message = null) }
            val result = if (existingId.isNullOrBlank()) {
                repository.addMedication(
                    medicineName = medicineName,
                    dose = dose,
                    route = route,
                    frequency = frequency,
                    scheduleTime = scheduleTime,
                    patientLabel = patientLabel,
                    notes = notes
                ).map { }
            } else {
                repository.updateMedication(
                    medId = existingId,
                    medicineName = medicineName,
                    dose = dose,
                    route = route,
                    frequency = frequency,
                    scheduleTime = scheduleTime,
                    patientLabel = patientLabel,
                    notes = notes,
                    active = active
                )
            }
            result.onSuccess {
                _feedback.update {
                    it.copy(
                        isSubmitting = false,
                        message = if (existingId.isNullOrBlank()) "নতুন ওষুধ যুক্ত হয়েছে।" else "ওষুধের তথ্য আপডেট হয়েছে।"
                    )
                }
                onComplete()
            }.onFailure { err ->
                _feedback.update {
                    it.copy(
                        isSubmitting = false,
                        message = err.localizedMessage ?: "ওষুধ সেভ করা যায়নি।"
                    )
                }
            }
        }
    }

    fun markDoseGiven(medId: String) {
        viewModelScope.launch {
            repository.markDoseGiven(medId)
                .onSuccess {
                    _feedback.update { it.copy(message = "ডোজ দেওয়ার রেকর্ড সংরক্ষিত হয়েছে।") }
                }
                .onFailure { err ->
                    _feedback.update { it.copy(message = err.localizedMessage ?: "ডোজ আপডেট ব্যর্থ।") }
                }
        }
    }

    fun toggleActive(medId: String, active: Boolean) {
        viewModelScope.launch {
            repository.toggleMedicationActive(medId, active)
                .onFailure { err ->
                    _feedback.update { it.copy(message = err.localizedMessage ?: "স্ট্যাটাস পরিবর্তন ব্যর্থ।") }
                }
        }
    }

    fun deleteMedication(medId: String) {
        viewModelScope.launch {
            repository.deleteMedication(medId)
                .onSuccess {
                    _feedback.update { it.copy(message = "ওষুধের এন্ট্রি মুছে ফেলা হয়েছে।") }
                }
                .onFailure { err ->
                    _feedback.update { it.copy(message = err.localizedMessage ?: "মুছে ফেলা যায়নি।") }
                }
        }
    }

    companion object {
        fun factory(repository: NursingDataRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ToolsViewModel(repository) as T
                }
            }
    }
}
