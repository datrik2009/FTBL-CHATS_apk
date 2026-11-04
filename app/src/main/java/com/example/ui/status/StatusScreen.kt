package com.example.ui.status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Status
import com.example.data.model.User
import com.example.data.repository.ChatRepository
import com.example.data.repository.StatusRepository
import com.example.ui.theme.PitchOutline
import com.example.ui.theme.PitchPrimary
import com.example.ui.theme.PitchPrimaryContainer
import com.example.ui.theme.PitchSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    currentUser: User,
    statusRepository: StatusRepository,
    chatRepository: ChatRepository,
    onNavigateToChat: (chatId: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val allStatuses by statusRepository.observeStatuses().collectAsState(initial = emptyList())

    var showCreateDialog by remember { mutableStateOf(false) }
    var activeViewingStatus by remember { mutableStateOf<Status?>(null) }

    val myStatuses = allStatuses.filter { it.userId == currentUser.userId }
    val otherStatuses = allStatuses.filter { it.userId != currentUser.userId }
    val recentUpdates = otherStatuses.filter { !it.viewedBy.contains(currentUser.userId) }
    val viewedUpdates = otherStatuses.filter { it.viewedBy.contains(currentUser.userId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Matchday Status",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = PitchPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_create_status")
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Create Status")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // My Status Section
            item {
                Text(
                    text = "My Status",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (myStatuses.isNotEmpty()) {
                                activeViewingStatus = myStatuses.first()
                            } else {
                                showCreateDialog = true
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(PitchPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser.displayName.take(1).ifBlank { "M" }).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (myStatuses.isNotEmpty()) "My Matchday Update" else "Tap to add status update",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (myStatuses.isNotEmpty()) {
                                    val time = myStatuses.first().createdAt?.toDate()?.let {
                                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                                    } ?: "Today"
                                    "$time • ${myStatuses.first().viewedBy.size} views"
                                } else "Share football highlights, lineups, or match scores",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "New Status", tint = PitchPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Recent Updates
            if (recentUpdates.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Updates (${recentUpdates.size})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(recentUpdates, key = { it.statusId }) { status ->
                    StatusRowItem(
                        status = status,
                        isViewed = false,
                        onClick = {
                            activeViewingStatus = status
                            coroutineScope.launch {
                                statusRepository.markViewed(status.statusId, currentUser.userId)
                            }
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Viewed Updates
            if (viewedUpdates.isNotEmpty()) {
                item {
                    Text(
                        text = "Viewed Updates",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(viewedUpdates, key = { it.statusId }) { status ->
                    StatusRowItem(
                        status = status,
                        isViewed = true,
                        onClick = { activeViewingStatus = status }
                    )
                }
            }

            if (otherStatuses.isEmpty() && myStatuses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                tint = PitchPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No active updates right now",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Post your matchday thoughts or match photo!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Full Screen Status Viewer Modal
    if (activeViewingStatus != null) {
        StatusViewerModal(
            status = activeViewingStatus!!,
            currentUser = currentUser,
            onDismiss = { activeViewingStatus = null },
            onDelete = {
                coroutineScope.launch {
                    statusRepository.deleteStatus(activeViewingStatus!!.statusId)
                    activeViewingStatus = null
                }
            },
            onReply = { replyText ->
                val otherUser = User(userId = activeViewingStatus!!.userId, displayName = activeViewingStatus!!.userName)
                coroutineScope.launch {
                    val chatId = chatRepository.createOrGetDirectChat(currentUser, otherUser)
                    chatRepository.sendMessage(
                        chatId = chatId,
                        sender = currentUser,
                        type = "text",
                        text = "Replying to your status: $replyText",
                        memberUids = listOf(currentUser.userId, otherUser.userId)
                    )
                    activeViewingStatus = null
                    onNavigateToChat(chatId)
                }
            }
        )
    }

    // Create Status Dialog
    if (showCreateDialog) {
        CreateStatusDialog(
            currentUser = currentUser,
            onDismiss = { showCreateDialog = false },
            onPost = { type, caption, mediaUrl, bgColor ->
                showCreateDialog = false
                coroutineScope.launch {
                    statusRepository.postStatus(currentUser, type, caption, mediaUrl, bgColor)
                }
            }
        )
    }
}

@Composable
private fun StatusRowItem(
    status: Status,
    isViewed: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isViewed) PitchOutline else PitchSecondary)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(PitchPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (status.userName.take(1).ifBlank { "F" }).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = status.userName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                val timeStr = status.createdAt?.toDate()?.let {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                } ?: "Today"
                Text(
                    text = "$timeStr • ${if (status.type == "image") "Photo status" else status.caption}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun StatusViewerModal(
    status: Status,
    currentUser: User,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onReply: (String) -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var replyText by remember { mutableStateOf("") }
    val isMyStatus = status.userId == currentUser.userId

    // Auto-advance timer (5 seconds)
    LaunchedEffect(Unit) {
        val totalMs = 5000L
        val intervalMs = 50L
        val steps = totalMs / intervalMs
        for (i in 1..steps) {
            delay(intervalMs)
            progress = i.toFloat() / steps
        }
        onDismiss()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(android.graphics.Color.parseColor(status.backgroundColor.ifBlank { "#004AC6" }))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Author Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = status.userName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = status.userName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Matchday update",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (isMyStatus) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Status", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Status Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (status.type == "image") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                            Text("Match Highlight ⚽", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = status.caption,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Reply or Views footer
            if (isMyStatus) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Viewed by ${status.viewedBy.size} supporters",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Reply to ${status.userName}...", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onReply(replyText.trim())
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Reply", tint = PitchPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateStatusDialog(
    currentUser: User,
    onDismiss: () -> Unit,
    onPost: (type: String, caption: String, mediaUrl: String?, bgColor: String) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#004AC6") }
    var statusType by remember { mutableStateOf("text") } // "text" or "image"

    val colorOptions = listOf("#004AC6", "#059669", "#7C3AED", "#DC2626", "#EA580C", "#0F172A")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Matchday Status",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { statusType = "text" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (statusType == "text") PitchPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (statusType == "text") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Text Status")
                    }
                    Button(
                        onClick = { statusType = "image" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (statusType == "image") PitchPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (statusType == "image") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Match Photo")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text(if (statusType == "text") "Status Text" else "Photo Caption") },
                    placeholder = { Text("What's your matchday take?") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )

                if (statusType == "text") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Background Theme:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        colorOptions.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColor = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == hex) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (caption.isNotBlank()) {
                        val media = if (statusType == "image") "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=800" else null
                        onPost(statusType, caption.trim(), media, selectedColor)
                    }
                },
                enabled = caption.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PitchPrimary)
            ) {
                Text("Post Status")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
