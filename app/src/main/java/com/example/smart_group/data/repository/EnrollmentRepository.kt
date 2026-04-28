package com.example.smart_group.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.smart_group.data.model.Enrollment
import kotlinx.coroutines.tasks.await

class EnrollmentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun enrollmentsRef(courseId: String) =
        db.collection("courses").document(courseId).collection("enrollments")


    suspend fun updateEnrollment(courseId: String, enrollment: Enrollment) {
        enrollmentsRef(courseId).document(enrollment.enrollmentId).set(enrollment).await()
    }

    suspend fun getEnrollmentsByStudentId(studentId: String): List<Enrollment> {
        val courses = db.collection("courses").get().await()

        val result = mutableListOf<Enrollment>()

        for (course in courses.documents) {
            val enrollments = course.reference
                .collection("enrollments")
                .whereEqualTo("studentId", studentId)
                .get()
                .await()

            result.addAll(enrollments.toObjects(Enrollment::class.java))
        }

        return result
    }

    suspend fun getEnrollmentByStudent(courseId: String, studentId: String): Enrollment? {

        val result = enrollmentsRef(courseId)
            .whereEqualTo("studentId", studentId)
            .get()
            .await()

        return result.toObjects(Enrollment::class.java).firstOrNull()
    }
}