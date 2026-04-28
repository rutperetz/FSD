//package com.example.smart_group.data.repository
//
//import com.example.smart_group.data.model.Group
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.tasks.await
//
//class GroupRepository {
//
//    private val db = FirebaseFirestore.getInstance()
//    private val groupsRef = db.collection("groups")
//
//    suspend fun saveGroup(group: Group): Result<Unit> {
//        return try {
//            groupsRef.document(group.groupId)
//                .set(group)
//                .await()
//
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun getGroupsByCourse(courseId: String): Result<List<Group>> {
//        return try {
//            val groups = groupsRef
//                .whereEqualTo("courseId", courseId)
//                .get()
//                .await()
//                .toObjects(Group::class.java)
//
//            Result.success(groups)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun getGroupByStudent(courseId: String, studentId: String): Result<Group?> {
//        return try {
//            val groups = groupsRef
//                .whereEqualTo("courseId", courseId)
//                .get()
//                .await()
//                .toObjects(Group::class.java)
//
//            val group = groups.firstOrNull { it.memberIds.contains(studentId) }
//            Result.success(group)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//}