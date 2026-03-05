package com.example.smart_group.data.repository


class FirestoreRepository(
    val userRepository: UserRepository = UserRepository(),
    val studentRepository: StudentRepository = StudentRepository(),
    val courseRepository: CourseRepository = CourseRepository(),
    val enrollmentRepository: EnrollmentRepository = EnrollmentRepository()
)
