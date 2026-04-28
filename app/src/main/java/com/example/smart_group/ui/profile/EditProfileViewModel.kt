package com.example.smart_group.ui.profile

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.model.User
import com.example.smart_group.data.repository.AuthRepository
import com.example.smart_group.data.repository.StudentRepository
import com.example.smart_group.data.repository.UserRepository
import kotlinx.coroutines.launch

sealed class EditProfileState {
    object Idle : EditProfileState()
    object Loading : EditProfileState()
    object NavigateBackToProfile : EditProfileState()
    data class NavigateToLogin(val message: String) : EditProfileState()
}

class EditProfileViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository(),
    private val studentRepo: StudentRepository = StudentRepository()
) : ViewModel() {

    private val _state = MutableLiveData<EditProfileState>(EditProfileState.Idle)
    val state: LiveData<EditProfileState> = _state

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    fun onToastShown() {
        _toastMessage.value = null
    }

    fun loadUser() {
        viewModelScope.launch {
            try {
                _state.value = EditProfileState.Loading

                val uid = authRepo.getCurrentUserId()
                    ?: throw Exception("No logged-in user")

                val currentUser = userRepo.getUser(uid)
                    ?: throw Exception("User not found in Firestore")

                _user.value = currentUser
                _state.value = EditProfileState.Idle
            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Unknown error"
                _state.value = EditProfileState.Idle
            }
        }
    }

    fun saveChanges(
        newUserNameRaw: String,
        newEmailRaw: String,
        newPasswordRaw: String
    ) {
        val userName = newUserNameRaw.trim()
        val email = newEmailRaw.trim()
        val password = newPasswordRaw.trim()

        if (userName.isEmpty() && email.isEmpty()) {
            _toastMessage.value = "Please enter your username and email"
            return
        }
        if (userName.isEmpty()) {
            _toastMessage.value = "Please enter your username"
            return
        }
        if (email.isEmpty()) {
            _toastMessage.value = "Please enter your email"
            return
        }

        validateUserName(userName)?.let {
            _toastMessage.value = it
            return
        }

        validateEmail(email)?.let {
            _toastMessage.value = it
            return
        }

        if (password.isNotEmpty()) {
            validatePassword(password)?.let {
                _toastMessage.value = it
                return
            }
        }

        viewModelScope.launch {
            try {
                _state.value = EditProfileState.Loading

                val uid = authRepo.getCurrentUserId()
                    ?: throw Exception("No logged-in user")

                val currentUser = userRepo.getUser(uid)
                    ?: throw Exception("User not found in Firestore")

                val currentUserName = currentUser.userName.trim()
                val currentEmail = currentUser.email.trim()

                val isUserNameChanged = userName != currentUserName
                val isEmailChanged = email != currentEmail
                val isPasswordChanged = password.isNotEmpty()

                if (!isUserNameChanged && !isEmailChanged && !isPasswordChanged) {
                    _state.value = EditProfileState.NavigateBackToProfile
                    return@launch
                }

                if (isUserNameChanged) {
                    userRepo.updateUserFields(uid, userName = userName)
                    studentRepo.updateStudentFieldsByUserId(uid, userName = userName)
                }

                if (isPasswordChanged) {
                    authRepo.updatePassword(password)
                }

                if (isEmailChanged) {
                    authRepo.updateEmail(email)
                    userRepo.updateUserFields(uid, email = email)
                    studentRepo.updateStudentFieldsByUserId(uid, email = email)
                    _state.value = EditProfileState.NavigateToLogin(
                        "Verification email sent. Please verify your new email and then log in again."
                    )
                    return@launch
                }

                _state.value = EditProfileState.NavigateToLogin(
                    "Profile updated successfully. Please log in again."
                )

            } catch (e: Exception) {
                _toastMessage.value = e.message ?: "Unknown error"
                _state.value = EditProfileState.Idle
            }
        }
    }


    private fun validateUserName(userName: String): String? {
        val userNameRegex = Regex("^[A-Za-z]{1,15}$")
        return if (!userNameRegex.matches(userName)) {
            "Username must contain only English letters (max 15)"
        } else {
            null
        }
    }

    private fun validateEmail(email: String): String? {
        return if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Invalid email address"
        } else {
            null
        }
    }

    private fun validatePassword(password: String): String? {
        val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#\$%^&*]{12,15}$")
        return if (!passwordRegex.matches(password)) {
            "Password must be 12–15 characters, include letters and numbers (can use !@#\$%^&amp;*)"
        } else {
            null
        }
    }
}