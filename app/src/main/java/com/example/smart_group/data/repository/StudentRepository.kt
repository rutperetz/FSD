package com.example.smart_group.data.repository

import com.example.smart_group.data.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.smart_group.data.model.Answers
import com.google.firebase.firestore.SetOptions

class StudentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val studentsRef = db.collection("students")

    suspend fun getStudent(studentId: String): Student? =
        studentsRef.document(studentId).get().await().toObject(Student::class.java)

    suspend fun getStudentByUserId(userId: String): Student? =
        studentsRef.whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(Student::class.java)
            .firstOrNull()

    suspend fun getStudentByEmail(email: String): Student? =
        studentsRef.whereEqualTo("email", email)
            .get()
            .await()
            .toObjects(Student::class.java)
            .firstOrNull()

    suspend fun linkStudentToUser(
        email: String,
        userId: String,
        userName: String,
        answers: Answers
    ) {
        val existingStudent = getStudentByEmail(email)
            ?: throw Exception("Student document not found for email=$email")

        val updatedStudent = existingStudent.copy(
            userId = userId,
            userName = userName,
            answers = answers,
            questionnaireCompleted = true
        )

        studentsRef.document(existingStudent.studentId).set(updatedStudent).await()
    }

    suspend fun addStudent(student: Student) {
        studentsRef.document(student.studentId).set(student).await()
    }

    suspend fun updateStudentFieldsByUserId(
        userId: String,
        userName: String? = null,
        email: String? = null
    ) {
        val student = getStudentByUserId(userId) ?: return

        val updates = mutableMapOf<String, Any>()
        if (userName != null) updates["userName"] = userName
        if (email != null) updates["email"] = email

        if (updates.isNotEmpty()) {
            studentsRef.document(student.studentId).update(updates).await()
        }
    }

    suspend fun getAnswersByUserId(userId: String): Answers? {
        val student = getStudentByUserId(userId)
        return student?.answers
    }

    suspend fun updateAnswersByUserId(userId: String, answers: Answers) {
        val snapshot = studentsRef
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val document = snapshot.documents.firstOrNull()
            ?: throw Exception("Student document not found for userId=$userId")

        document.reference.set(
            mapOf("answers" to answers),
            SetOptions.merge()
        ).await()
    }
}
