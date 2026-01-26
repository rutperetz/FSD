package com.example.smart_group.ui.register

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.model.User
import com.example.smart_group.data.model.UserRole
import com.example.smart_group.data.repository.AuthRepository
import com.example.smart_group.data.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    // מונע לחיצות כפולות
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Toast
    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    // ניווט ללוגין אחרי הצלחה
    private val _navigateToLogin = MutableLiveData(false)
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    // Errors לשדות
    private val _usernameError = MutableLiveData<String?>()
    val usernameError: LiveData<String?> = _usernameError

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    fun onToastShown() {
        _toastMessage.value = null
    }

    fun onNavigatedToLogin() {
        _navigateToLogin.value = false
    }

    fun signUp(usernameRaw: String, emailRaw: String, passwordRaw: String) {
        val cleanUsername = usernameRaw.trim()
        val cleanEmail = emailRaw.trim()
        val cleanPassword = passwordRaw

        // ניקוי שגיאות קודמות
        _usernameError.value = null
        _emailError.value = null
        _passwordError.value = null

        // ולידציות כמו אצלך
        val usernameRegex = Regex("^[A-Za-z]{1,15}$")
        val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,10}$")

        if (cleanUsername.isEmpty()) {
            _usernameError.value = "Username is required"
            return
        }
        if (!usernameRegex.matches(cleanUsername)) {
            _usernameError.value = "Username must contain only English letters (max 15)"
            return
        }

        if (cleanEmail.isEmpty()) {
            _emailError.value = "Email is required"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _emailError.value = "Invalid email address"
            return
        }

        if (cleanPassword.isEmpty()) {
            _passwordError.value = "Password is required"
            return
        }
        if (!passwordRegex.matches(cleanPassword)) {
            _passwordError.value = "Password must be 8–10 characters and include letters and numbers"
            return
        }

        // ✅ הרשמה אמיתית: Auth -> uid -> Firestore users/{uid}
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newUid = authRepo.register(cleanEmail, cleanPassword)

                val userDoc = User(
                    userId = newUid,
                    userName = cleanUsername,
                    email = cleanEmail,
                    role = UserRole.STUDENT
                )

                userRepo.addUser(userDoc)

                _toastMessage.value = "Registered successfully ✅"
                _navigateToLogin.value = true
            } catch (e: Exception) {
                val msg = (e.message ?: "").lowercase()
                _toastMessage.value = when {
                    "already in use" in msg || "exists" in msg -> "This email is already in use"
                    "badly formatted" in msg || "invalid" in msg -> "Invalid email address"
                    "password" in msg && "6" in msg -> "Password is too weak"
                    else -> "Registration failed. Please try again."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
