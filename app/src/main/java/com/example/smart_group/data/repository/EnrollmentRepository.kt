package com.example.smart_group.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.smart_group.data.model.Enrollment
import kotlinx.coroutines.tasks.await

class EnrollmentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun enrollmentsRef(courseId: String) =
        db.collection("courses").document(courseId).collection("enrollments")

    suspend fun getEnrollments(courseId: String): List<Enrollment> =
        enrollmentsRef(courseId).get().await().toObjects(Enrollment::class.java)

    suspend fun getEnrollment(courseId: String, enrollmentId: String): Enrollment? =
        enrollmentsRef(courseId).document(enrollmentId).get().await().toObject(Enrollment::class.java)

    suspend fun addEnrollment(courseId: String, enrollment: Enrollment) {
        enrollmentsRef(courseId).document(enrollment.enrollmentId).set(enrollment).await()
    }

    suspend fun updateEnrollment(courseId: String, enrollment: Enrollment) {
        enrollmentsRef(courseId).document(enrollment.enrollmentId).set(enrollment).await()
    }

    suspend fun deleteEnrollment(courseId: String, enrollmentId: String) {
        enrollmentsRef(courseId).document(enrollmentId).delete().await()
    }
}
