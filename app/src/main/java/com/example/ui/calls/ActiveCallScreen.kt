package com.example.ui.calls

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Call
import com.example.data.model.User
import com.example.data.repository.CallRepository
import com.example.ui.theme.PitchPrimary
import com.example.ui.theme.PitchPrimaryContainer
import com.example.ui.theme.PitchSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ActiveCallScreen(
    callId: String,
    currentUser: User,
    callRepository: CallRepository,
    onCallEnded: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val call by callRepository.observeActiveCall(callId).collectAsState(initial = null)
    val signals by callRepository.observeSignals(callId).collectAsState(initial = emptyList())

    var isMuted by remember { mutableStateOf(false) }
    var isVideoEnabled by remember { mutableStateOf(true) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var callDuration by remember { mutableLongStateOf(0L) }

    val otherUserName = if (call?.callerId == currentUser.userId) call?.receiverName else call?.callerName
    val isVideo = call?.callType == "video"

    BackHandler {
        coroutineScope.launch {
            callRepository.updateCallStatus(callId, "ended", callDuration)
            onCallEnded()
        }
    }

    // Call status and timer logic
    LaunchedEffect(call?.status) {
        if (call?.status == "calling") {
            // Simulate answer after 2 seconds
            delay(2000)
            callRepository.updateCallStatus(callId, "connected")
            // Send initial WebRTC SDP offer signal
            call?.let {
                callRepository.sendSignal(
                    callId = callId,
                    senderId = currentUser.userId,
                    type = "offer",
                    payload = "{\"sdp\":\"v=0\\r\\no=FTBL 123456 2 IN IP4 127.0.0.1...\",\"type\":\"offer\"}",
                    participantUids = it.participantUids
                )
            }
        }
    }

    LaunchedEffect(call?.status) {
        if (call?.status == "connected") {
            while (true) {
                delay(1000)
                callDuration++
            }
        } else if (call?.status == "ended" || call?.status == "rejected") {
            delay(1000)
            onCallEnded()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Video Stream Feed (or stadium gradient for voice)
            if (isVideo && isVideoEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(PitchPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (otherUserName?.take(1) ?: "P").uppercase(),
                                color = Color.White,
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = otherUserName ?: "Opponent",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "HD Video Feed Connected ⚽",
                            color = PitchSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                // Voice Call Display
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(PitchPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (otherUserName?.take(1) ?: "F").uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = otherUserName ?: "Supporter",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (call?.status == "connected") {
                            val mins = callDuration / 60
                            val secs = callDuration % 60
                            "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
                        } else {
                            call?.status?.replaceFirstChar { it.uppercase() } ?: "Calling..."
                        },
                        color = PitchSecondary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            // Top Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isVideo) "FTBL Video Call" else "FTBL Voice Call",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Encrypted WebRTC Signaling",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                if (call?.status == "connected") {
                    Box(
                        modifier = Modifier
                            .background(PitchSecondary, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        val mins = callDuration / 60
                        val secs = callDuration % 60
                        Text(
                            text = "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Floating PIP Self Camera Preview
            if (isVideo && isVideoEnabled) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 100.dp, end = 20.dp)
                        .size(width = 100.dp, height = 140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                            Text("You", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Bottom Call Controls Bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute Mic Button
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) Color.Red else Color.White.copy(alpha = 0.2f))
                        .testTag("action_mute_call")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Video toggle (if video call)
                if (isVideo) {
                    IconButton(
                        onClick = { isVideoEnabled = !isVideoEnabled },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (!isVideoEnabled) Color.Red else Color.White.copy(alpha = 0.2f))
                            .testTag("action_toggle_video")
                    ) {
                        Icon(
                            imageVector = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Video",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Flip camera
                    IconButton(
                        onClick = { isFrontCamera = !isFrontCamera },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Flip Camera",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // End Call Button
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            callRepository.updateCallStatus(callId, "ended", callDuration)
                            onCallEnded()
                        }
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .testTag("action_end_call")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
