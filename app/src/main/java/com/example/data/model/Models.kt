package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class User(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val username: String = "",
    val bio: String = "",
    val photoUrl: String? = null,
    val phoneNumber: String? = null,
    val favoriteClub: String = "Arsenal",
    val isOnline: Boolean = true,
    @ServerTimestamp val lastSeen: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Chat(
    @DocumentId val id: String = "",
    val chatId: String = "",
    val isGroup: Boolean = false,
    val name: String? = null,
    val description: String? = null,
    val photoUrl: String? = null,
    val memberUids: List<String> = emptyList(),
    val adminUids: List<String> = emptyList(),
    val creatorUid: String = "",
    val lastMessage: String? = null,
    val lastMessageSenderId: String? = null,
    val lastMessageType: String = "text",
    @ServerTimestamp val lastMessageTime: Timestamp? = null,
    val pinnedBy: List<String> = emptyList(),
    val mutedBy: List<String> = emptyList(),
    val archivedBy: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Message(
    @DocumentId val id: String = "",
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String? = null,
    val type: String = "text", // "text", "image", "video", "voice", "file"
    val text: String = "",
    val mediaUrl: String? = null,
    val replyToId: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val reactions: Map<String, String> = emptyMap(),
    val readBy: List<String> = emptyList(),
    val isEdited: Boolean = false,
    val memberUids: List<String> = emptyList(), // denormalized for flat subcollection security
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Status(
    @DocumentId val id: String = "",
    val statusId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val type: String = "text", // "text", "image", "video"
    val caption: String = "",
    val mediaUrl: String? = null,
    val backgroundColor: String = "#004AC6",
    val viewedBy: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Call(
    @DocumentId val id: String = "",
    val callId: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerPhotoUrl: String? = null,
    val receiverId: String = "",
    val receiverName: String = "",
    val receiverPhotoUrl: String? = null,
    val callType: String = "video", // "voice" or "video"
    val status: String = "calling", // "calling", "ringing", "connected", "ended", "rejected"
    val startedAt: Timestamp? = null,
    val endedAt: Timestamp? = null,
    val durationSeconds: Long = 0L,
    val participantUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Signal(
    @DocumentId val id: String = "",
    val signalId: String = "",
    val callId: String = "",
    val senderId: String = "",
    val type: String = "offer", // "offer", "answer", "candidate", "hangup"
    val payload: String = "",
    val participantUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BlockedUser(
    @DocumentId val id: String = "",
    val blockId: String = "",
    val userId: String = "",
    val blockedUserId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Report(
    @DocumentId val id: String = "",
    val reportId: String = "",
    val reporterId: String = "",
    val reportedTargetId: String = "",
    val reportedType: String = "user",
    val reason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)
