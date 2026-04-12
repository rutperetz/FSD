package com.example.smart_group.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_group.data.model.User
import com.example.smart_group.data.repository.AuthRepository
import com.example.smart_group.data.repository.StudentRepository
import com.example.smart_group.data.repository.UserRepository
import kotlinx.coroutines.launch



sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository(),
    private val studentRepo: StudentRepository = StudentRepository()
) : ViewModel() {

    private val _state = MutableLiveData<ProfileState>()
    val state: LiveData<ProfileState> = _state

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _state.value = ProfileState.Loading

                val uid = authRepo.getCurrentUserId()
                    ?: throw Exception("No logged-in user")

                // מרעננים את FirebaseAuth כדי לקבל מייל מעודכן אם המשתמש כבר אישר את המייל החדש
                authRepo.reloadCurrentUser()

                val authEmail = authRepo.getCurrentEmail()
                    ?: throw Exception("Email not found in Firebase Auth")

                val firestoreUser = userRepo.getUser(uid)
                    ?: throw Exception("User document not found in Firestore")

                // אם ב-Auth יש מייל חדש, מסנכרנים אותו ל-Firestore
                val finalUser =
                    if (authEmail != firestoreUser.email) {
                        userRepo.updateUserFields(uid, email = authEmail)
                        studentRepo.updateStudentFieldsByUserId(uid, email = authEmail)
                        firestoreUser.copy(email = authEmail)
                    } else {
                        firestoreUser
                    }

                _state.value = ProfileState.Success(finalUser)

            } catch (e: Exception) {
                _state.value = ProfileState.Error(e.message ?: "Unknown error")
            }
        }
    }
}