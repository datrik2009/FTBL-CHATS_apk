package com.example.ui.chats

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.model.Message
import com.example.data.model.User
import com.example.data.repository.CallRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.ModerationRepository
import com.example.data.repository.UserRepository
import com.example.ui.theme.PitchOutline
import com.example.ui.theme.PitchPrimary
import com.example.ui.theme.PitchPrimaryContainer
import com.example.ui.theme.PitchSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

val EMOJI_REACTIONS = listOf("⚽", "🔥", "👏", "🏆", "❤️", "🟥", "🟨", "😂")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    currentUser: User,
    chatRepository: ChatRepository,
    userRepository: UserRepository,
    callRepository: CallRepository,
    moderationRepository: ModerationRepository,
    onNavigateBack: () -> Unit,
    onStartCall: (callId: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val chat by chatRepository.observeChat(chatId).collectAsState(initial = null)
    val messages by chatRepository.observeMessages(chatId).collectAsState(initial = emptyList())
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var selectedMessageForMenu by remember { mutableStateOf<Message?>(null) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableLongStateOf(0L) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showChatMenu by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    // Voice record timer
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingDuration = 0L
            while (isRecordingVoice) {
                delay(1000)
                recordingDuration++
            }
        }
    }

    // Scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showChatMenu = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (chat?.isGroup == true) PitchSecondary else PitchPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (chat?.isGroup == true) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = Color.White)
                            } else {
                                Text(
                                    text = (chat?.name?.take(1) ?: "F").uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = chat?.name ?: "Chat",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (chat?.isGroup == true) "${chat?.memberUids?.size ?: 0} squad members" else "Tap for options",
                                style = MaterialTheme.typography.labelSmall,
                                color = PitchSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Voice Call Button
                    IconButton(
                        onClick = {
                            val otherUid = chat?.memberUids?.firstOrNull { it != currentUser.userId }
                            if (otherUid != null) {
                                coroutineScope.launch {
                                    val otherUser = userRepository.getUser(otherUid) ?: User(userId = otherUid, displayName = chat?.name ?: "Opponent")
                                    val callId = callRepository.initiateCall(currentUser, otherUser, "voice")
                                    onStartCall(callId)
                                }
                            }
                        },
                        modifier = Modifier.testTag("action_voice_call")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = PitchPrimary)
                    }

                    // Video Call Button
                    IconButton(
                        onClick = {
                            val otherUid = chat?.memberUids?.firstOrNull { it != currentUser.userId }
                            if (otherUid != null) {
                                coroutineScope.launch {
                                    val otherUser = userRepository.getUser(otherUid) ?: User(userId = otherUid, displayName = chat?.name ?: "Opponent")
                                    val callId = callRepository.initiateCall(currentUser, otherUser, "video")
                                    onStartCall(callId)
                                }
                            }
                        },
                        modifier = Modifier.testTag("action_video_call")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = PitchPrimary)
                    }

                    Box {
                        IconButton(onClick = { showChatMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Chat Menu")
                        }
                        DropdownMenu(
                            expanded = showChatMenu,
                            onDismissRequest = { showChatMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Report Chat") },
                                onClick = {
                                    showChatMenu = false
                                    showReportDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear / Leave") },
                                onClick = {
                                    showChatMenu = false
                                    onNavigateBack()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(messages, key = { it.messageId }) { msg ->
                    val isMe = msg.senderId == currentUser.userId
                    MessageBubble(
                        message = msg,
                        isMe = isMe,
                        onReply = { replyingToMessage = msg },
                        onReaction = { emoji ->
                            coroutineScope.launch {
                                chatRepository.toggleReaction(chatId, msg.messageId, currentUser.userId, emoji)
                            }
                        },
                        onLongClick = { selectedMessageForMenu = msg }
                    )
                }
            }

            // Reply Preview Banner
            AnimatedVisibility(visible = replyingToMessage != null) {
                if (replyingToMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = null,
                            tint = PitchPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Replying to ${replyingToMessage?.senderName}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = PitchPrimary
                            )
                            Text(
                                text = replyingToMessage?.text ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { replyingToMessage = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Reply", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Edit Preview Banner
            AnimatedVisibility(visible = editingMessage != null) {
                if (editingMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PitchPrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = PitchPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Editing message...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            editingMessage = null
                            inputText = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Edit")
                        }
                    }
                }
            }

            // Message Composer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach Football Photo / Highlight
                IconButton(
                    onClick = {
                        val memberUids = chat?.memberUids ?: listOf(currentUser.userId)
                        coroutineScope.launch {
                            chatRepository.sendMessage(
                                chatId = chatId,
                                sender = currentUser,
                                type = "image",
                                text = "Match Highlight 📸",
                                mediaUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=800",
                                memberUids = memberUids
                            )
                        }
                    },
                    modifier = Modifier.testTag("action_attach_media")
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Attach Media", tint = PitchPrimary)
                }

                // Voice Recording or Text Field
                if (isRecordingVoice) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(PitchSecondary.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recording 0:${recordingDuration.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.Red,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                isRecordingVoice = false
                                val memberUids = chat?.memberUids ?: listOf(currentUser.userId)
                                coroutineScope.launch {
                                    chatRepository.sendMessage(
                                        chatId = chatId,
                                        sender = currentUser,
                                        type = "voice",
                                        text = "Voice note (${recordingDuration}s) 🎙️",
                                        memberUids = memberUids
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Send Voice", tint = PitchSecondary)
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Pitch banter or tactical note...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("message_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedBorderColor = PitchPrimary,
                            unfocusedBorderColor = PitchOutline
                        ),
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                if (inputText.isNotBlank()) {
                    IconButton(
                        onClick = {
                            val textToSend = inputText.trim()
                            val memberUids = chat?.memberUids ?: listOf(currentUser.userId)
                            val currentEditing = editingMessage
                            val currentReply = replyingToMessage

                            inputText = ""
                            editingMessage = null
                            replyingToMessage = null

                            coroutineScope.launch {
                                if (currentEditing != null) {
                                    chatRepository.editMessage(chatId, currentEditing.messageId, textToSend)
                                } else {
                                    chatRepository.sendMessage(
                                        chatId = chatId,
                                        sender = currentUser,
                                        type = "text",
                                        text = textToSend,
                                        replyToId = currentReply?.messageId,
                                        replyToText = currentReply?.text,
                                        replyToSender = currentReply?.senderName,
                                        memberUids = memberUids
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PitchPrimary)
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = Color.White
                        )
                    }
                } else if (!isRecordingVoice) {
                    IconButton(
                        onClick = { isRecordingVoice = true },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PitchSecondary)
                            .testTag("record_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record Voice Message",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    // Message Context Actions Dialog (Edit, Copy, Delete)
    if (selectedMessageForMenu != null) {
        val msg = selectedMessageForMenu!!
        AlertDialog(
            onDismissRequest = { selectedMessageForMenu = null },
            title = { Text("Message Options") },
            text = {
                Column {
                    // Emoji reactions bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        EMOJI_REACTIONS.take(5).forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .clickable {
                                        coroutineScope.launch {
                                            chatRepository.toggleReaction(chatId, msg.messageId, currentUser.userId, emoji)
                                            selectedMessageForMenu = null
                                        }
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("FTBL Chat", msg.text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                selectedMessageForMenu = null
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PitchPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Copy Text")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                replyingToMessage = msg
                                selectedMessageForMenu = null
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = PitchPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Reply")
                    }

                    if (msg.senderId == currentUser.userId) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingMessage = msg
                                    inputText = msg.text
                                    selectedMessageForMenu = null
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = PitchSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Edit Message")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        chatRepository.deleteMessage(chatId, msg.messageId)
                                        selectedMessageForMenu = null
                                    }
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Message", color = Color.Red)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedMessageForMenu = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Report Dialog
    if (showReportDialog) {
        var reportReason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Chat or Content") },
            text = {
                Column {
                    Text("Help keep FTBL Chats safe and respectful.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("Enter reason for reporting...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            moderationRepository.reportTarget(chatId, "chat", reportReason)
                            showReportDialog = false
                            Toast.makeText(context, "Report submitted. Thank you.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PitchPrimary)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isMe: Boolean,
    onReply: () -> Unit,
    onReaction: (String) -> Unit,
    onLongClick: () -> Unit
) {
    val bubbleColor = if (isMe) PitchPrimary else MaterialTheme.colorScheme.surface
    val textColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
    val timeColor = if (isMe) Color.White.copy(alpha = 0.75f) else Color.Gray

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clickable { onLongClick() }
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Sender name in group
                if (!isMe) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PitchSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Quoted / Replied message preview
                if (message.replyToText != null) {
                    Surface(
                        color = if (isMe) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = message.replyToSender ?: "Reply",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isMe) Color.White else PitchPrimary
                            )
                            Text(
                                text = message.replyToText,
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Message Body by Type
                when (message.type) {
                    "image" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                                Text("Match Action Photo", color = Color.White, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = message.text, color = textColor, style = MaterialTheme.typography.bodyMedium)
                    }
                    "voice" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isMe) Color.White.copy(alpha = 0.25f) else PitchSecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = message.text,
                                    color = textColor,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "0:12 • Audio Note",
                                    color = timeColor,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time, edit badge and read ticks
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isEdited) {
                        Text(
                            text = "edited ",
                            style = MaterialTheme.typography.labelSmall,
                            color = timeColor
                        )
                    }
                    val timeStr = message.createdAt?.toDate()?.let {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                    } ?: ""
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = timeColor
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = if (isMe) Color.White else PitchSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Reaction chips below bubble
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 2.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.reactions.values.distinct().forEach { emoji ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp,
                        modifier = Modifier.clickable { onReaction(emoji) }
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
