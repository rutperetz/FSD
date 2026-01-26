package com.example.smart_group.data.repository



import com.google.firebase.firestore.FirebaseFirestore
import com.example.smart_group.data.model.Student
import kotlinx.coroutines.tasks.await

class StudentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val studentsRef = db.collection("students")

    suspend fun getStudent(studentId: String): Student? =
        studentsRef.document(studentId).get().await().toObject(Student::class.java)

    suspend fun getStudentByUserId(userId: String): Student? =
        studentsRef.whereEqualTo("userId", userId).get().await().toObjects(Student::class.java).firstOrNull()

    suspend fun getAllStudents(): List<Student> =
        studentsRef.get().await().toObjects(Student::class.java)

    suspend fun addStudent(student: Student) {
        studentsRef.document(student.studentId).set(student).await()
    }

    suspend fun updateStudent(student: Student) {
        studentsRef.document(student.studentId).set(student).await()
    }

    suspend fun deleteStudent(studentId: String) {
        studentsRef.document(studentId).delete().await()
    }
}
