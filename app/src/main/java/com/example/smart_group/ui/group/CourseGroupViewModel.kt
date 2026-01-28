package com.example.smart_group.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.GroupRepository
import com.example.smart_group.model.MatchGroup
import com.example.smart_group.model.RejectReasons
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CourseGroupUiState(
    val isLoading: Boolean = false,
    val group: MatchGroup? = null,
    val message: String = ""
)

class CourseGroupViewModel(
    private val repository: GroupRepository = GroupRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(CourseGroupUiState(isLoading = true, message = "Loading..."))
    val state: StateFlow<CourseGroupUiState> = _state

    private var roundListener: ListenerRegistration? = null

    // ✅ Realtime subscription (use this instead of load)
    fun subscribe(courseId: String, roundId: String, groupId: String) {
        _state.value = CourseGroupUiState(isLoading = true, message = "Loading...")

        roundListener?.remove()
        roundListener = repository.listenToRound(
            courseId = courseId,
            roundId = roundId,
            onUpdate = { round ->
                val group = round?.matchGroups?.firstOrNull { it.groupId == groupId }
                _state.value = CourseGroupUiState(
                    isLoading = false,
                    group = group,
                    message = if (group != null) "Loaded from Firestore" else "Group not found"
                )
            },
            onError = { e ->
                _state.value = CourseGroupUiState(
                    isLoading = false,
                    message = e.message ?: "Error"
                )
            }
        )
    }

    fun accept(courseId: String, roundId: String, groupId: String) {
        viewModelScope.launch {
            repository.setGroupStatus(courseId, roundId, groupId, true)
            _state.value = _state.value.copy(message = "Group accepted")
        }
    }

    fun reject(
        courseId: String,
        roundId: String,
        groupId: String,
        reasons: RejectReasons
    ) {
        viewModelScope.launch {
            repository.setGroupStatus(courseId, roundId, groupId, false)
            repository.saveRejectReasons(courseId, roundId, groupId, reasons)
            _state.value = _state.value.copy(message = "Group rejected")
        }
    }

    override fun onCleared() {
        roundListener?.remove()
        super.onCleared()
    }
}