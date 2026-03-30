package com.example.smart_group.ui.questionnaire

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.model.Answers
import com.example.smart_group.data.repository.StudentRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

sealed class EditQuestionnaireUiState {

    object Idle : EditQuestionnaireUiState()
    object Loading : EditQuestionnaireUiState()
    data class Loaded(val answers: Answers) : EditQuestionnaireUiState()
    object Saved : EditQuestionnaireUiState()
    data class Error(val message: String) : EditQuestionnaireUiState()
}

class EditQuestionnaireViewModel : ViewModel() {

    private val repository = StudentRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableLiveData<EditQuestionnaireUiState>(EditQuestionnaireUiState.Idle)
    val state: LiveData<EditQuestionnaireUiState> = _state

    fun loadCurrentUserAnswers() {
        val userId = auth.currentUser?.uid

        if (userId.isNullOrBlank()) {
            _state.value = EditQuestionnaireUiState.Error("No logged in user found")
            return
        }

        viewModelScope.launch {
            _state.value = EditQuestionnaireUiState.Loading
            try {
                val answers = repository.getAnswersByUserId(userId)
                if (answers != null) {
                    _state.value = EditQuestionnaireUiState.Loaded(answers)
                } else {
                    _state.value = EditQuestionnaireUiState.Error("No questionnaire answers found")
                }
            } catch (e: Exception) {
                _state.value = EditQuestionnaireUiState.Error(
                    e.message ?: "Failed to load questionnaire"
                )
            }
        }
    }

    fun saveAnswers(answers: Answers) {
        val userId = auth.currentUser?.uid

        if (userId.isNullOrBlank()) {
            _state.value = EditQuestionnaireUiState.Error("No logged in user found")
            return
        }

        viewModelScope.launch {
            _state.value = EditQuestionnaireUiState.Loading
            try {
                repository.updateAnswersByUserId(userId, answers)
                _state.value = EditQuestionnaireUiState.Saved
            } catch (e: Exception) {
                _state.value = EditQuestionnaireUiState.Error(
                    e.message ?: "Failed to save questionnaire"
                )
            }
        }
    }

    fun validate(answers: Answers): Boolean {
        if (answers.gender.isBlank()) return false
        if (answers.genderPreference.isBlank()) return false
        if (answers.availability.isEmpty()) return false
        if (answers.workStyle.isEmpty()) return false
        if (answers.workMode.isEmpty()) return false
        if (answers.language.isEmpty()) return false
        if (answers.taskPreference.isEmpty()) return false
        return true
    }

    fun resetState() {
        _state.value = EditQuestionnaireUiState.Idle
    }
}