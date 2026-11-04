package com.example.ui.calls

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Call
import com.example.data.model.User
import com.example.data.repository.CallRepository
import com.example.data.repository.UserRepository
import com.example.ui.theme.PitchPrimary
import com.example.ui.theme.PitchSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(
    currentUser: User,
    callRepository: CallRepository,
    userRepository: UserRepository,
    onStartCall: (callId: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val calls by callRepository.observeCalls(currentUser.userId).collectAsState(initial = emptyList())
    var showStartCallDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pitchside Calls",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showStartCallDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Call") },
                containerColor = PitchPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_new_call")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            if (calls.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(PitchPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = PitchPrimary, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No calls made yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Start a direct voice or video call with any football supporter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(calls, key = { it.callId }) { call ->
                        val isOutbound = call.callerId == currentUser.userId
                        val otherName = if (isOutbound) call.receiverName else call.callerName
                        val isVideo = call.callType == "video"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
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
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (isVideo) PitchPrimary else PitchSecondary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (otherName.take(1).ifBlank { "F" }).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = otherName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isOutbound) Icons.Default.CallMade else Icons.Default.CallReceived,
                                            contentDescription = null,
                                            tint = if (isOutbound) PitchSecondary else Color.Red,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val timeStr = call.createdAt?.toDate()?.let {
                                            SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(it)
                                        } ?: ""
                                        Text(
                                            text = "$timeStr • ${call.status}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val otherUid = if (isOutbound) call.receiverId else call.callerId
                                            val otherUser = userRepository.getUser(otherUid) ?: User(userId = otherUid, displayName = otherName)
                                            val newCallId = callRepository.initiateCall(currentUser, otherUser, call.callType)
                                            onStartCall(newCallId)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                                        contentDescription = "Callback",
                                        tint = PitchPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStartCallDialog) {
        var query by remember { mutableStateOf("") }
        val fans by userRepository.searchUsers(query).collectAsState(initial = emptyList())

        AlertDialog(
            onDismissRequest = { showStartCallDialog = false },
            title = { Text("Start Voice or Video Call") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search supporter by name or club...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(modifier = Modifier.height(240.dp)) {
                        items(fans, key = { it.userId }) { fan ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PitchPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(fan.displayName.take(1).uppercase(), color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(fan.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("@${fan.username} • ${fan.favoriteClub}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }

                                IconButton(
                                    onClick = {
                                        showStartCallDialog = false
                                        coroutineScope.launch {
                                            val callId = callRepository.initiateCall(currentUser, fan, "voice")
                                            onStartCall(callId)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = PitchSecondary)
                                }

                                IconButton(
                                    onClick = {
                                        showStartCallDialog = false
                                        coroutineScope.launch {
                                            val callId = callRepository.initiateCall(currentUser, fan, "video")
                                            onStartCall(callId)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = PitchPrimary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStartCallDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
