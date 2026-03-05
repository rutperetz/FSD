package com.example.smart_group.data.repository



import com.google.firebase.firestore.FirebaseFirestore
import com.example.smart_group.data.model.Course
import kotlinx.coroutines.tasks.await

class CourseRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val coursesRef = db.collection("courses")

    suspend fun getCourse(courseId: String): Course? =
        coursesRef.document(courseId).get().await().toObject(Course::class.java)

    suspend fun getAllCourses(): List<Course> =
        coursesRef.get().await().toObjects(Course::class.java)

    suspend fun addCourse(course: Course) {
        coursesRef.document(course.courseId).set(course).await()
    }

    suspend fun updateCourse(course: Course) {
        coursesRef.document(course.courseId).set(course).await()
    }

    suspend fun deleteCourse(courseId: String) {
        coursesRef.document(courseId).delete().await()
    }
}
