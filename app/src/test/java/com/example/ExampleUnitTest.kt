package com.example

import com.example.data.model.Chat
import com.example.data.model.Message
import com.example.data.model.Status
import com.example.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testUserModel_defaultValues() {
        val user = User(
            userId = "user_123",
            displayName = "Bukayo Saka",
            username = "saka7",
            favoriteClub = "Arsenal"
        )
        assertEquals("user_123", user.userId)
        assertEquals("Bukayo Saka", user.displayName)
        assertEquals("saka7", user.username)
        assertEquals("Arsenal", user.favoriteClub)
        assertTrue(user.isOnline)
    }

    @Test
    fun testChatModel_directChat() {
        val chat = Chat(
            chatId = "dm_alice_bob",
            isGroup = false,
            memberUids = listOf("alice", "bob"),
            creatorUid = "alice"
        )
        assertFalse(chat.isGroup)
        assertEquals(2, chat.memberUids.size)
        assertTrue(chat.memberUids.contains("alice"))
        assertTrue(chat.memberUids.contains("bob"))
    }

    @Test
    fun testMessageModel_textMessage() {
        val msg = Message(
            messageId = "msg_1",
            chatId = "dm_alice_bob",
            senderId = "alice",
            senderName = "Alice",
            type = "text",
            text = "Great match today! ⚽",
            memberUids = listOf("alice", "bob")
        )
        assertEquals("text", msg.type)
        assertEquals("Great match today! ⚽", msg.text)
        assertFalse(msg.isEdited)
    }

    @Test
    fun testStatusModel() {
        val status = Status(
            statusId = "status_1",
            userId = "alice",
            userName = "Alice",
            type = "text",
            caption = "3 points in the bag!",
            backgroundColor = "#004AC6"
        )
        assertEquals("text", status.type)
        assertEquals("#004AC6", status.backgroundColor)
        assertTrue(status.viewedBy.isEmpty())
    }
}
