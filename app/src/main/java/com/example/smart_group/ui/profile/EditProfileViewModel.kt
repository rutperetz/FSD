package com.example.smart_group.ui.profile

import androidx.lifecycle.*
import com.example.smart_group.data.model.User
import com.example.smart_group.data.repository.AuthRepository
import com.example.smart_group.data.repository.UserRepository
import kotlinx.coroutines.launch

sealed class EditProfileState {
    object Idle : EditProfileState()
    object Loading : EditProfileState()
    object Saved : EditProfileState()
    data class Error(val message: String) : EditProfileState()
}

class EditProfileViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    private val _state = MutableLiveData<EditProfileState>(EditProfileState.Idle)
    val state: LiveData<EditProfileState> = _state

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    fun loadUser() {
        viewModelScope.launch {
            try {
                _state.value = EditProfileState.Loading
                val uid = authRepo.getCurrentUserId() ?: throw Exception("No logged-in user")
                val u = userRepo.getUser(uid) ?: throw Exception("User not found in Firestore")
                _user.value = u
                _state.value = EditProfileState.Idle
            } catch (e: Exception) {
                _state.value = EditProfileState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveChanges(newUserName: String, newEmail: String, newPassword: String) {
        viewModelScope.launch {
            try {
                _state.value = EditProfileState.Loading

                val uid = authRepo.getCurrentUserId() ?: throw Exception("No logged-in user")
                val current = userRepo.getUser(uid) ?: throw Exception("User not found in Firestore")

                // 1) אם אימייל השתנה -> קודם Auth, ואז Firestore
                if (newEmail.isNotBlank() && newEmail != current.email) {
                    authRepo.updateEmail(newEmail)
                }

                // 2) אם סיסמה הוקלדה -> עדכון ב-Auth בלבד
                if (newPassword.isNotBlank()) {
                    authRepo.updatePassword(newPassword)
                }

                // 3) עדכון משתמש בפיירסטור (בלי סיסמה!)
                val updated = current.copy(
                    userName = newUserName.ifBlank { current.userName },
                    email = if (newEmail.isNotBlank()) newEmail else current.email
                )
                userRepo.updateUser(updated)

                _state.value = EditProfileState.Saved
            } catch (e: Exception) {
                _state.value = EditProfileState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun logout() {
        authRepo.logout()
    }
}