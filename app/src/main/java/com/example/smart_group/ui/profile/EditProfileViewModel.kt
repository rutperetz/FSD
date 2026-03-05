package com.example.smart_group.ui.profile

import androidx.lifecycle.*
import com.example.smart_group.data.model.User
import com.example.smart_group.data.repository.AuthRepository
import com.example.smart_group.data.repository.UserRepository
import kotlinx.coroutines.launch
import android.util.Patterns

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
    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _usernameError = MutableLiveData<String?>()
    val usernameError: LiveData<String?> = _usernameError

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    fun onToastShown() { _toastMessage.value = null }

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

    fun saveChanges(newUserNameRaw: String, newEmailRaw: String, newPasswordRaw: String) {
        val cleanedUserName = newUserNameRaw.trim()
        val cleanedEmail = newEmailRaw.trim()
        val cleanedPassword = newPasswordRaw

        // בדיוק כמו Register
        val userNameRegex = Regex("^[A-Za-z]{1,15}$") // :contentReference[oaicite:9]{index=9}
        val passRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,10}$") // :contentReference[oaicite:10]{index=10}

        viewModelScope.launch {
            try {
                // ===== Validations (same messages as Register) =====
                if (cleanedUserName.isEmpty()) {
                    _state.value = EditProfileState.Error("Username is required")
                    return@launch
                }
                if (!userNameRegex.matches(cleanedUserName)) {
                    _state.value = EditProfileState.Error("Username must contain only English letters (max 15)")
                    return@launch
                }

                if (cleanedEmail.isEmpty()) {
                    _state.value = EditProfileState.Error("Email is required")
                    return@launch
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(cleanedEmail).matches()) {
                    _state.value = EditProfileState.Error("Invalid email address")
                    return@launch
                }

                // סיסמה ב-Edit Profile: רק אם המשתמש הקליד (אצלך גם ככה לא מעדכן אם ריק) :contentReference[oaicite:11]{index=11}
                if (cleanedPassword.isNotEmpty() && !passRegex.matches(cleanedPassword)) {
                    _state.value = EditProfileState.Error("Password must be 8–10 characters and include letters and numbers")
                    return@launch
                }

                // ===== Proceed saving =====
                _state.value = EditProfileState.Loading

                val uid = authRepo.getCurrentUserId() ?: throw Exception("No logged-in user")
                val current = userRepo.getUser(uid) ?: throw Exception("User not found in Firestore")

                // Email changed -> Auth then Firestore :contentReference[oaicite:12]{index=12} :contentReference[oaicite:13]{index=13}
                if (cleanedEmail != current.email) {
                    authRepo.updateEmail(cleanedEmail)
                }

                // Password update (only if typed) :contentReference[oaicite:14]{index=14}
                if (cleanedPassword.isNotEmpty()) {
                    authRepo.updatePassword(cleanedPassword)
                }

                val updated = current.copy(
                    userName = cleanedUserName,
                    email = cleanedEmail
                )
                userRepo.updateUser(updated) // :contentReference[oaicite:15]{index=15}

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