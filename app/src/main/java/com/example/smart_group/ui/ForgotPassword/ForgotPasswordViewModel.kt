package com.example.smart_group.ui.forgotpassword

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.repository.AuthRepository
import kotlinx.coroutines.launch
import com.example.smart_group.R

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessageRes = MutableLiveData<Int?>()
    val toastMessageRes: LiveData<Int?> = _toastMessageRes

    private val _navigateBackToLogin = MutableLiveData(false)
    val navigateBackToLogin: LiveData<Boolean> = _navigateBackToLogin

    fun sendResetEmail(emailRaw: String) {
        val email = emailRaw.trim()

        if (email.isEmpty()) {
            _toastMessageRes.value = R.string.msg_enter_email
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _toastMessageRes.value = R.string.msg_invalid_email
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.sendPasswordResetEmail(email)
                _toastMessageRes.value = R.string.msg_reset_email_sent
                _navigateBackToLogin.value = true
            } catch (_: Exception) {
                _toastMessageRes.value = R.string.msg_generic_error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onToastShown() {
        _toastMessageRes.value = null
    }

    fun onNavigated() {
        _navigateBackToLogin.value = false
    }
}