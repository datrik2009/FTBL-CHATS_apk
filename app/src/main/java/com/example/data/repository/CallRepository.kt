package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Call
import com.example.data.model.Signal
import com.example.data.model.User
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CallRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeCalls(userId: String): Flow<List<Call>> {
        val path = "calls"
        return db.collection(path)
            .whereArrayContains("participantUids", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Call::class.java).sortedByDescending {
                    it.createdAt
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeActiveCall(callId: String): Flow<Call?> {
        val path = "calls/$callId"
        return db.collection("calls").document(callId)
            .snapshots()
            .map { it.toObject(Call::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                throw error
            }
    }

    suspend fun initiateCall(caller: User, receiver: User, callType: String): String {
        val uid = requireUserId()
        val callId = "call_${UUID.randomUUID()}"
        val callRef = db.collection("calls").document(callId)

        val callData = mapOf(
            "callId" to callId,
            "callerId" to uid,
            "callerName" to caller.displayName,
            "callerPhotoUrl" to caller.photoUrl,
            "receiverId" to receiver.userId,
            "receiverName" to receiver.displayName,
            "receiverPhotoUrl" to receiver.photoUrl,
            "callType" to callType,
            "status" to "calling",
            "participantUids" to listOf(uid, receiver.userId),
            "durationSeconds" to 0L,
            "startedAt" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            callRef.set(callData).await()
            return callId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "calls/$callId")
            throw e
        }
    }

    suspend fun updateCallStatus(callId: String, status: String, duration: Long = 0L) {
        val path = "calls/$callId"
        try {
            val updateData = mutableMapOf<String, Any>(
                "status" to status,
                "durationSeconds" to duration,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (status == "ended" || status == "rejected") {
                updateData["endedAt"] = FieldValue.serverTimestamp()
            }
            db.collection("calls").document(callId).update(updateData).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    suspend fun sendSignal(callId: String, senderId: String, type: String, payload: String, participantUids: List<String>) {
        val signalId = "sig_${UUID.randomUUID()}"
        val signalRef = db.collection("calls").document(callId).collection("signals").document(signalId)

        val signalData = mapOf(
            "signalId" to signalId,
            "callId" to callId,
            "senderId" to senderId,
            "type" to type,
            "payload" to payload,
            "participantUids" to participantUids,
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            signalRef.set(signalData).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "calls/$callId/signals/$signalId")
        }
    }

    fun observeSignals(callId: String): Flow<List<Signal>> {
        val path = "calls/$callId/signals"
        return db.collection("calls").document(callId).collection("signals")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(Signal::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }
}
