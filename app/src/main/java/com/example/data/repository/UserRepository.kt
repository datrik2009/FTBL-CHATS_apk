package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.User
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeCurrentUser(userId: String): Flow<User?> =
        db.collection("users").document(userId)
            .snapshots()
            .map { it.toObject(User::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, "users/$userId")
                throw e
            }

    suspend fun getUser(userId: String): User? {
        val doc = db.collection("users").document(userId).get().await()
        return doc.toObject(User::class.java)
    }

    suspend fun saveUserProfile(user: User) {
        val uid = requireUserId()
        val path = "users/$uid"
        val userMap = mapOf(
            "userId" to uid,
            "displayName" to user.displayName,
            "username" to user.username.lowercase().trim(),
            "bio" to user.bio,
            "photoUrl" to user.photoUrl,
            "phoneNumber" to user.phoneNumber,
            "favoriteClub" to user.favoriteClub,
            "isOnline" to true,
            "lastSeen" to FieldValue.serverTimestamp(),
            "createdAt" to (user.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            db.collection("users").document(uid).set(userMap).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun updatePresence(isOnline: Boolean) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val path = "users/$uid"
        try {
            db.collection("users").document(uid).update(
                mapOf(
                    "isOnline" to isOnline,
                    "lastSeen" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    fun searchUsers(query: String): Flow<List<User>> = callbackFlow {
        val uid = Firebase.auth.currentUser?.uid ?: ""
        val listener = db.collection("users")
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, "users")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.toObjects(User::class.java)?.filter {
                    it.userId != uid && (
                        it.username.contains(query, ignoreCase = true) ||
                        it.displayName.contains(query, ignoreCase = true) ||
                        it.favoriteClub.contains(query, ignoreCase = true)
                    )
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }
}
