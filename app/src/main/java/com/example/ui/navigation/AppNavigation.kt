package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.data.model.Status
import com.example.data.model.User
import com.example.data.repository.CallRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.ModerationRepository
import com.example.data.repository.StatusRepository
import com.example.data.repository.UserRepository
import com.example.ui.calls.ActiveCallScreen
import com.example.ui.calls.CallsScreen
import com.example.ui.chats.ChatDetailScreen
import com.example.ui.chats.ChatListScreen
import com.example.ui.profile.SettingsScreen
import com.example.ui.status.StatusScreen
import com.example.ui.theme.PitchPrimary

@Composable
fun AppNavigation(
    currentUser: User,
    userRepository: UserRepository,
    chatRepository: ChatRepository,
    statusRepository: StatusRepository,
    callRepository: CallRepository,
    moderationRepository: ModerationRepository,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(BottomNavTab.CHATS) }
    var activeChatId by remember { mutableStateOf<String?>(null) }
    var activeCallId by remember { mutableStateOf<String?>(null) }

    when {
        activeCallId != null -> {
            ActiveCallScreen(
                callId = activeCallId!!,
                currentUser = currentUser,
                callRepository = callRepository,
                onCallEnded = { activeCallId = null }
            )
        }
        activeChatId != null -> {
            ChatDetailScreen(
                chatId = activeChatId!!,
                currentUser = currentUser,
                chatRepository = chatRepository,
                userRepository = userRepository,
                callRepository = callRepository,
                moderationRepository = moderationRepository,
                onNavigateBack = { activeChatId = null },
                onStartCall = { callId -> activeCallId = callId }
            )
        }
        else -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        BottomNavTab.values().forEach { tab ->
                            val isSelected = tab == selectedTab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PitchPrimary,
                                    selectedTextColor = PitchPrimary,
                                    indicatorColor = PitchPrimary.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (selectedTab) {
                        BottomNavTab.CHATS -> {
                            ChatListScreen(
                                currentUser = currentUser,
                                chatRepository = chatRepository,
                                userRepository = userRepository,
                                statusRepository = statusRepository,
                                onChatSelected = { chatId -> activeChatId = chatId },
                                onStatusSelected = { /* handled in carousel or status tab */ },
                                onNewStatusClicked = { selectedTab = BottomNavTab.STATUS }
                            )
                        }
                        BottomNavTab.STATUS -> {
                            StatusScreen(
                                currentUser = currentUser,
                                statusRepository = statusRepository,
                                chatRepository = chatRepository,
                                onNavigateToChat = { chatId -> activeChatId = chatId }
                            )
                        }
                        BottomNavTab.CALLS -> {
                            CallsScreen(
                                currentUser = currentUser,
                                callRepository = callRepository,
                                userRepository = userRepository,
                                onStartCall = { callId -> activeCallId = callId }
                            )
                        }
                        BottomNavTab.SETTINGS -> {
                            SettingsScreen(
                                currentUser = currentUser,
                                userRepository = userRepository,
                                moderationRepository = moderationRepository,
                                onSignOut = onSignOut
                            )
                        }
                    }
                }
            }
        }
    }
}
