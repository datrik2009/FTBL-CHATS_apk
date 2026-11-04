package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavTab(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    CHATS("Chats", Icons.Default.ChatBubble, "tab_chats"),
    STATUS("Status", Icons.Default.DonutLarge, "tab_status"),
    CALLS("Calls", Icons.Default.Call, "tab_calls"),
    SETTINGS("Settings", Icons.Default.Settings, "tab_settings")
}
