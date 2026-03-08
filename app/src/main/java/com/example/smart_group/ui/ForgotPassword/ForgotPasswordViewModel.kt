package com.example.smart_group.ui.forgotpassword

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _navigateBackToLogin = MutableLiveData(false)
    val navigateBackToLogin: LiveData<Boolean> = _navigateBackToLogin

    fun sendResetEmail(emailRaw: String) {
        val email = emailRaw.trim()

        if (email.isEmpty()) {
            _toastMessage.value = "Please enter your email"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _toastMessage.value = "Invalid email address"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.sendPasswordResetEmail(email)
                _toastMessage.value = "Password reset email sent. Please check your inbox"
                _navigateBackToLogin.value = true
            } catch (e: Exception) {
                _toastMessage.value = "No account found with this email"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onToastShown() {
        _toastMessage.value = null
    }

    fun onNavigated() {
        _navigateBackToLogin.value = false
    }
}