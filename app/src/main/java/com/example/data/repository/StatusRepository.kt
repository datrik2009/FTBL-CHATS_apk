package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Status
import com.example.data.model.User
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class StatusRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeStatuses(): Flow<List<Status>> {
        val path = "statuses"
        val now = Timestamp.now()
        return db.collection(path)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Status::class.java).filter { status ->
                    status.expiresAt == null || status.expiresAt.toDate().after(now.toDate())
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun postStatus(
        user: User,
        type: String,
        caption: String,
        mediaUrl: String? = null,
        backgroundColor: String = "#004AC6"
    ) {
        val uid = requireUserId()
        val statusId = "status_${UUID.randomUUID()}"
        val statusRef = db.collection("statuses").document(statusId)

        // 24 hours expiry
        val expiryTime = Timestamp(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))

        val statusData = mapOf(
            "statusId" to statusId,
            "userId" to uid,
            "userName" to user.displayName,
            "userPhotoUrl" to user.photoUrl,
            "type" to type,
            "caption" to caption,
            "mediaUrl" to mediaUrl,
            "backgroundColor" to backgroundColor,
            "viewedBy" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "expiresAt" to expiryTime
        ).filterValues { it != null }

        try {
            statusRef.set(statusData).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "statuses/$statusId")
            throw e
        }
    }

    suspend fun markViewed(statusId: String, userId: String) {
        val path = "statuses/$statusId"
        try {
            db.collection("statuses").document(statusId)
                .update("viewedBy", FieldValue.arrayUnion(userId)).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    suspend fun deleteStatus(statusId: String) {
        val path = "statuses/$statusId"
        try {
            db.collection("statuses").document(statusId).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }
}
