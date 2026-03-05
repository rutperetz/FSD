package com.example.smart_group.ui.profile

import androidx.lifecycle.*
import com.example.smart_group.data.model.User
import com.example.smart_group.data.repository.AuthRepository
import com.example.smart_group.data.repository.UserRepository
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    private val _state = MutableLiveData<ProfileState>()
    val state: LiveData<ProfileState> = _state

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _state.value = ProfileState.Loading

                val uid = authRepo.getCurrentUserId()
                    ?: throw Exception("No logged-in user")

                val user = userRepo.getUser(uid)
                    ?: throw Exception("User document not found in Firestore")

                _state.value = ProfileState.Success(user)
            } catch (e: Exception) {
                _state.value = ProfileState.Error(e.message ?: "Unknown error")
            }
        }
    }
}