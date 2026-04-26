package com.example.smart_group.ui.questionnaire

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class QuestionnaireAnswers(
    val gender: String,
    val genderPreference: String,
    val availability: List<String>,
    val workStyle: List<String>,
    val workMode: List<String>,
    val language: List<String>,
    val taskPreference: List<String>
)

sealed class QuestionnaireUiState {
    data object Idle : QuestionnaireUiState()
    data class Saved(val answers: QuestionnaireAnswers) : QuestionnaireUiState()
}

class QuestionnaireViewModel : ViewModel() {

    private val _state = MutableLiveData<QuestionnaireUiState>(QuestionnaireUiState.Idle)
    val state: LiveData<QuestionnaireUiState> = _state

    fun validate(answers: QuestionnaireAnswers): Boolean {
        if (answers.gender.isBlank()) return false
        if (answers.genderPreference.isBlank()) return false
        if (answers.availability.isEmpty()) return false
        if (answers.workStyle.isEmpty()) return false
        if (answers.workMode.isEmpty()) return false
        if (answers.language.isEmpty()) return false
        if (answers.taskPreference.isEmpty()) return false
        return true
    }

    fun saveLocal(answers: QuestionnaireAnswers) {
        _state.value = QuestionnaireUiState.Saved(answers)
    }

    fun reset() {
        _state.value = QuestionnaireUiState.Idle
    }
}
