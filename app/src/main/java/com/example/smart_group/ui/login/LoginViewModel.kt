package com.example.smart_group.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    //מונע לחיצות-בקשות כפולות של התחברות
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // מחליט האם צריך לתת הודעה למשתמש?
    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    //מחליט האם צריך לעבור מסך?
    private val _navigateToNext = MutableLiveData(false)
    val navigateToNext: LiveData<Boolean> = _navigateToNext

    // אקטיביטי קורא לפונקציה הזו שלוחצים כל כפתור LOGIN
    fun login(emailRaw: String, passwordRaw: String) {
        val email = emailRaw.trim()
        val password = passwordRaw

        //בדיקה קודם אם השדות רקים
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

        //בודק אם המייל תקין-במידה ולא מחזיר הודעה בהתאם
        val emailError = validateEmail(email)
        if (emailError != null) {
            _toastMessage.value = emailError
            return
        }

        //בודק אם סיסמא נכונה-במידה שלא מחזיר הודעה בהתאם
        val passError = validatePassword(password)
        if (passError != null) {
            _toastMessage.value = passError
            return
        }

        //ההתחברות מול FirebaseAuth
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authRepository.login(email, password)
                _navigateToNext.value = true
            } catch (e: Exception) {
                _toastMessage.value = "Login failed: incorrect email or password"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onToastShown() {
        _toastMessage.value = null
    }

    fun onNavigated() {
        _navigateToNext.value = false
    }

    //ולידציה של המייל
    private fun validateEmail(email: String): String? {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Invalid email address" // מייל לא תקין לפי תבנית מובנית של אנדרואיד
        return null
    }

    //ולידציה של הסיסמא
    private fun validatePassword(password: String): String? {
        val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,10}$") //כללי הסיסמא
        if (!passwordRegex.matches(password)) {
            return "Password must be 8–10 characters and include letters and numbers"
        }
        return null
    }
}
