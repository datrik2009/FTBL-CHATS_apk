package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.BlockedUser
import com.example.data.model.Report
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ModerationRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeBlockedUsers(userId: String): Flow<List<BlockedUser>> {
        val path = "blockedUsers"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { it.toObjects(BlockedUser::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun blockUser(targetUserId: String) {
        val uid = requireUserId()
        val blockId = "block_${uid}_${targetUserId}"
        val blockRef = db.collection("blockedUsers").document(blockId)

        val data = mapOf(
            "blockId" to blockId,
            "userId" to uid,
            "blockedUserId" to targetUserId,
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            blockRef.set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "blockedUsers/$blockId")
            throw e
        }
    }

    suspend fun unblockUser(blockId: String) {
        val path = "blockedUsers/$blockId"
        try {
            db.collection("blockedUsers").document(blockId).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    suspend fun reportTarget(targetId: String, targetType: String, reason: String) {
        val uid = requireUserId()
        val reportId = "rep_${UUID.randomUUID()}"
        val reportRef = db.collection("reports").document(reportId)

        val data = mapOf(
            "reportId" to reportId,
            "reporterId" to uid,
            "reportedTargetId" to targetId,
            "reportedType" to targetType,
            "reason" to reason,
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            reportRef.set(data).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "reports/$reportId")
            throw e
        }
    }
}
