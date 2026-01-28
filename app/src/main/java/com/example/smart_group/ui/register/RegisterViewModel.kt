package com.example.smart_group.ui.register

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.model.Answers
import com.example.smart_group.data.model.Student
import com.example.smart_group.data.model.User
import com.example.smart_group.data.model.UserRole
import com.example.smart_group.data.repository.AuthRepository
import com.example.smart_group.data.repository.StudentRepository
import com.example.smart_group.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.util.UUID

class RegisterViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository(),
    private val studentRepo: StudentRepository = StudentRepository()
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private val _navigateToLogin = MutableLiveData(false)
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    private val _usernameError = MutableLiveData<String?>()
    val usernameError: LiveData<String?> = _usernameError

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    fun onToastShown() { _toastMessage.value = null }
    fun onNavigatedToLogin() { _navigateToLogin.value = false }

    fun signUp(
        usernameRaw: String,
        emailRaw: String,
        passwordRaw: String,
        questionnaireAnswers: Map<String, Any>?
    ) {
        val cleanedUserName = usernameRaw.trim()
        val cleanedEmail = emailRaw.trim()
        val cleanedPassword = passwordRaw

        _usernameError.value = null
        _emailError.value = null
        _passwordError.value = null

        val userNameRegex = Regex("^[A-Za-z]{1,15}$")
        val passRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,10}$")

        if (cleanedUserName.isEmpty()) {
            _usernameError.value = "Username is required"
            return
        }
        if (!userNameRegex.matches(cleanedUserName)) {
            _usernameError.value = "Username must contain only English letters (max 15)"
            return
        }

        if (cleanedEmail.isEmpty()) {
            _emailError.value = "Email is required"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanedEmail).matches()) {
            _emailError.value = "Invalid email address"
            return
        }

        if (cleanedPassword.isEmpty()) {
            _passwordError.value = "Password is required"
            return
        }
        if (!passRegex.matches(cleanedPassword)) {
            _passwordError.value = "Password must be 8–10 characters and include letters and numbers"
            return
        }

        if (questionnaireAnswers == null) {
            _toastMessage.value = "Please complete the questionnaire before signing up."
            return
        }

        // המרה מ-Map -> Answers (תואם schema)
        val convertedAnswers = Answers(
            gender = (questionnaireAnswers["gender"] as? String) ?: "",
            genderPreference = (questionnaireAnswers["genderPreference"] as? String) ?: "",

            availability = (questionnaireAnswers["availability"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            workStyle = (questionnaireAnswers["workStyle"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            workMode = (questionnaireAnswers["workMode"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            language = (questionnaireAnswers["language"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            taskPreference = (questionnaireAnswers["taskPreference"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        )

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1) Auth
                val createdUid = authRepo.register(cleanedEmail, cleanedPassword)

                // 2) users/{uid}
                val createdUser = User(
                    userId = createdUid,
                    userName = cleanedUserName,
                    email = cleanedEmail,
                    role = UserRole.STUDENT
                )
                userRepo.addUser(createdUser)

                // 3) students/{studentId} + answers
                val createdStudentId = UUID.randomUUID().toString()

                val createdStudent = Student(
                    studentId = createdStudentId,
                    userId = createdUid,
                    userName = cleanedUserName,
                    email = cleanedEmail,
                    answers = convertedAnswers,
                    normalizedAnswers = emptyList(),
                    questionnaireCompleted = true
                )

                studentRepo.addStudent(createdStudent)

                _toastMessage.value = "Registered successfully "
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
