package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Chat
import com.example.data.model.Message
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

class ChatRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeChats(userId: String): Flow<List<Chat>> {
        val path = "chats"
        return db.collection(path)
            .whereArrayContains("memberUids", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Chat::class.java).sortedByDescending {
                    it.lastMessageTime ?: it.createdAt
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeChat(chatId: String): Flow<Chat?> {
        val path = "chats/$chatId"
        return db.collection("chats").document(chatId)
            .snapshots()
            .map { it.toObject(Chat::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                throw error
            }
    }

    fun observeMessages(chatId: String): Flow<List<Message>> {
        val path = "chats/$chatId/messages"
        return db.collection("chats").document(chatId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Message::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun createOrGetDirectChat(currentUser: User, targetUser: User): String {
        val uid = requireUserId()
        // Deterministic ID for 1:1 chat between two users
        val sortedUids = listOf(uid, targetUser.userId).sorted()
        val chatId = "dm_${sortedUids[0]}_${sortedUids[1]}"
        val chatRef = db.collection("chats").document(chatId)

        val existing = chatRef.get().await()
        if (existing.exists()) {
            return chatId
        }

        val chatData = mapOf(
            "chatId" to chatId,
            "isGroup" to false,
            "name" to targetUser.displayName,
            "description" to "${currentUser.displayName} and ${targetUser.displayName}",
            "photoUrl" to targetUser.photoUrl,
            "memberUids" to sortedUids,
            "creatorUid" to uid,
            "lastMessage" to "Conversation started",
            "lastMessageSenderId" to uid,
            "lastMessageType" to "text",
            "lastMessageTime" to FieldValue.serverTimestamp(),
            "pinnedBy" to emptyList<String>(),
            "mutedBy" to emptyList<String>(),
            "archivedBy" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            chatRef.set(chatData).await()
            return chatId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "chats/$chatId")
            throw e
        }
    }

    suspend fun createGroupChat(
        creator: User,
        groupName: String,
        description: String,
        memberUids: List<String>
    ): String {
        val uid = requireUserId()
        val chatId = "group_${UUID.randomUUID()}"
        val chatRef = db.collection("chats").document(chatId)

        val allMembers = (memberUids + uid).distinct()
        val chatData = mapOf(
            "chatId" to chatId,
            "isGroup" to true,
            "name" to groupName,
            "description" to description,
            "photoUrl" to null,
            "memberUids" to allMembers,
            "adminUids" to listOf(uid),
            "creatorUid" to uid,
            "lastMessage" to "Group created by ${creator.displayName}",
            "lastMessageSenderId" to uid,
            "lastMessageType" to "text",
            "lastMessageTime" to FieldValue.serverTimestamp(),
            "pinnedBy" to emptyList<String>(),
            "mutedBy" to emptyList<String>(),
            "archivedBy" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            chatRef.set(chatData).await()
            return chatId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "chats/$chatId")
            throw e
        }
    }

    suspend fun sendMessage(
        chatId: String,
        sender: User,
        type: String,
        text: String,
        mediaUrl: String? = null,
        replyToId: String? = null,
        replyToText: String? = null,
        replyToSender: String? = null,
        memberUids: List<String>
    ) {
        val uid = requireUserId()
        val messageId = "msg_${UUID.randomUUID()}"
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        val chatRef = db.collection("chats").document(chatId)

        val messageData = mapOf(
            "messageId" to messageId,
            "chatId" to chatId,
            "senderId" to uid,
            "senderName" to sender.displayName,
            "senderPhotoUrl" to sender.photoUrl,
            "type" to type,
            "text" to text,
            "mediaUrl" to mediaUrl,
            "replyToId" to replyToId,
            "replyToText" to replyToText,
            "replyToSender" to replyToSender,
            "reactions" to emptyMap<String, String>(),
            "readBy" to listOf(uid),
            "isEdited" to false,
            "memberUids" to memberUids,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }

        try {
            msgRef.set(messageData).await()
            chatRef.update(
                mapOf(
                    "lastMessage" to (if (type == "text") text else "[$type]"),
                    "lastMessageSenderId" to uid,
                    "lastMessageType" to type,
                    "lastMessageTime" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "chats/$chatId/messages/$messageId")
            throw e
        }
    }

    suspend fun toggleReaction(chatId: String, messageId: String, userId: String, emoji: String) {
        val path = "chats/$chatId/messages/$messageId"
        val docRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        try {
            val doc = docRef.get().await()
            val currentReactions = (doc.get("reactions") as? Map<*, *>)?.mapKeys { it.key.toString() }?.mapValues { it.value.toString() }?.toMutableMap() ?: mutableMapOf()
            if (currentReactions[userId] == emoji) {
                currentReactions.remove(userId)
            } else {
                currentReactions[userId] = emoji
            }
            docRef.update(
                mapOf(
                    "reactions" to currentReactions,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    suspend fun markMessageRead(chatId: String, messageId: String, userId: String) {
        val path = "chats/$chatId/messages/$messageId"
        try {
            db.collection("chats").document(chatId).collection("messages").document(messageId)
                .update("readBy", FieldValue.arrayUnion(userId)).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    suspend fun editMessage(chatId: String, messageId: String, newText: String) {
        val path = "chats/$chatId/messages/$messageId"
        try {
            db.collection("chats").document(chatId).collection("messages").document(messageId)
                .update(
                    mapOf(
                        "text" to newText,
                        "isEdited" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            throw e
        }
    }

    suspend fun deleteMessage(chatId: String, messageId: String) {
        val path = "chats/$chatId/messages/$messageId"
        try {
            db.collection("chats").document(chatId).collection("messages").document(messageId).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    suspend fun togglePinChat(chatId: String, userId: String, isPinned: Boolean) {
        val op = if (isPinned) FieldValue.arrayRemove(userId) else FieldValue.arrayUnion(userId)
        try {
            db.collection("chats").document(chatId).update("pinnedBy", op).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "chats/$chatId")
        }
    }

    suspend fun toggleMuteChat(chatId: String, userId: String, isMuted: Boolean) {
        val op = if (isMuted) FieldValue.arrayRemove(userId) else FieldValue.arrayUnion(userId)
        try {
            db.collection("chats").document(chatId).update("mutedBy", op).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "chats/$chatId")
        }
    }

    suspend fun toggleArchiveChat(chatId: String, userId: String, isArchived: Boolean) {
        val op = if (isArchived) FieldValue.arrayRemove(userId) else FieldValue.arrayUnion(userId)
        try {
            db.collection("chats").document(chatId).update("archivedBy", op).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "chats/$chatId")
        }
    }
}
