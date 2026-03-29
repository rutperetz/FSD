package com.example.smart_group.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.model.UserRole
import com.example.smart_group.data.repository.AuthRepository
import com.example.smart_group.data.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _navigateToStudent = MutableLiveData(false)
    val navigateToStudent: LiveData<Boolean> = _navigateToStudent

    private val _navigateToAdmin = MutableLiveData(false)
    val navigateToAdmin: LiveData<Boolean> = _navigateToAdmin

    fun login(emailRaw: String, passwordRaw: String) {
        val email = emailRaw.trim()
        val password = passwordRaw

        if (email.isEmpty() && password.isEmpty()) {
            _toastMessage.value = "Please enter your email and password"
            return
        }
        if (email.isEmpty()) {
            _toastMessage.value = "Please enter your email"
            return
        }
        if (password.isEmpty()) {
            _toastMessage.value = "Please enter your password"
            return
        }

        val emailError = validateEmail(email)
        if (emailError != null) {
            _toastMessage.value = emailError
            return
        }

        val passError = validatePassword(password)
        if (passError != null) {
            _toastMessage.value = passError
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = authRepository.login(email, password)

                val user = try {
                    userRepository.getUser(uid)
                } catch (e: Exception) {
                    _toastMessage.value = "Login succeeded but failed to load user data: ${e.message}"
                    null
                }

                when (user?.role) {
                    UserRole.ADMIN -> _navigateToAdmin.value = true
                    UserRole.STUDENT -> _navigateToStudent.value = true
                    null -> {
                        if (_toastMessage.value == null) {
                            _toastMessage.value = "User record was not found in Firestore"
                        }
                    }
                    else -> _toastMessage.value = "User role not found"
                }

            } catch (e: Exception) {
                _toastMessage.value = "Firebase login failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onToastShown() {
        _toastMessage.value = null
    }

    fun onStudentNavigated() {
        _navigateToStudent.value = false
    }

    fun onAdminNavigated() {
        _navigateToAdmin.value = false
    }

    private fun validateEmail(email: String): String? {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid email address"
        }
        return null
    }

    private fun validatePassword(password: String): String? {

//        val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d).{12,15}$")
        val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#\$%^&*]{12,15}$")
        if (!passwordRegex.matches(password)) {
            return "Password must be 8–10 characters and include letters and numbers"
        }
        return null
    }
}