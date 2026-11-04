package com.example.ui.chats

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chat
import com.example.data.model.Status
import com.example.data.model.User
import com.example.data.repository.ChatRepository
import com.example.data.repository.StatusRepository
import com.example.data.repository.UserRepository
import com.example.ui.theme.PitchOutline
import com.example.ui.theme.PitchPrimary
import com.example.ui.theme.PitchPrimaryContainer
import com.example.ui.theme.PitchSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    currentUser: User,
    chatRepository: ChatRepository,
    userRepository: UserRepository,
    statusRepository: StatusRepository,
    onChatSelected: (chatId: String) -> Unit,
    onStatusSelected: (Status) -> Unit,
    onNewStatusClicked: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val chats by chatRepository.observeChats(currentUser.userId).collectAsState(initial = emptyList())
    val statuses by statusRepository.observeStatuses().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Unread", "Groups", "Pinned"
    var showNewChatDialog by remember { mutableStateOf(false) }
    var showNewGroupDialog by remember { mutableStateOf(false) }

    // Filtered chats
    val filteredChats = chats.filter { chat ->
        val nameMatches = (chat.name ?: "").contains(searchQuery, ignoreCase = true) ||
                (chat.lastMessage ?: "").contains(searchQuery, ignoreCase = true)
        val filterMatches = when (selectedFilter) {
            "Groups" -> chat.isGroup
            "Pinned" -> chat.pinnedBy.contains(currentUser.userId)
            "Unread" -> true // All active for demo
            else -> !chat.archivedBy.contains(currentUser.userId)
        }
        nameMatches && filterMatches
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PitchPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FTBL Chats",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                text = "${currentUser.favoriteClub} Fan • @${currentUser.username}",
                                style = MaterialTheme.typography.labelSmall,
                                color = PitchSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showNewGroupDialog = true },
                        modifier = Modifier.testTag("action_create_group")
                    ) {
                        Icon(imageVector = Icons.Default.GroupAdd, contentDescription = "Create Group")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewChatDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Chat") },
                containerColor = PitchPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_new_chat")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search conversations, clubs, or squad messages...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = PitchPrimary,
                    unfocusedBorderColor = PitchOutline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_chats_input")
            )

            // Status Stories Pill Carousel
            StatusCarousel(
                currentUser = currentUser,
                statuses = statuses,
                onNewStatusClicked = onNewStatusClicked,
                onStatusSelected = onStatusSelected
            )

            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Groups", "Pinned", "Unread")
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PitchPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Conversations List
            if (filteredChats.isEmpty()) {
                EmptyChatsView(
                    searchQuery = searchQuery,
                    onStartNewChat = { showNewChatDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    items(filteredChats, key = { it.chatId }) { chat ->
                        ChatItemRow(
                            chat = chat,
                            currentUserId = currentUser.userId,
                            onClick = { onChatSelected(chat.chatId) },
                            onTogglePin = { isPinned ->
                                coroutineScope.launch {
                                    chatRepository.togglePinChat(chat.chatId, currentUser.userId, isPinned)
                                }
                            },
                            onToggleMute = { isMuted ->
                                coroutineScope.launch {
                                    chatRepository.toggleMuteChat(chat.chatId, currentUser.userId, isMuted)
                                }
                            },
                            onToggleArchive = { isArchived ->
                                coroutineScope.launch {
                                    chatRepository.toggleArchiveChat(chat.chatId, currentUser.userId, isArchived)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // New 1:1 Chat Dialog with Fan Search
    if (showNewChatDialog) {
        NewChatDialog(
            currentUser = currentUser,
            userRepository = userRepository,
            onDismiss = { showNewChatDialog = false },
            onUserSelected = { targetUser ->
                showNewChatDialog = false
                coroutineScope.launch {
                    val chatId = chatRepository.createOrGetDirectChat(currentUser, targetUser)
                    onChatSelected(chatId)
                }
            }
        )
    }

    // New Squad / Group Dialog
    if (showNewGroupDialog) {
        CreateGroupDialog(
            currentUser = currentUser,
            userRepository = userRepository,
            onDismiss = { showNewGroupDialog = false },
            onCreateGroup = { groupName, description, members ->
                showNewGroupDialog = false
                coroutineScope.launch {
                    val chatId = chatRepository.createGroupChat(
                        creator = currentUser,
                        groupName = groupName,
                        description = description,
                        memberUids = members
                    )
                    onChatSelected(chatId)
                }
            }
        )
    }
}

@Composable
private fun StatusCarousel(
    currentUser: User,
    statuses: List<Status>,
    onNewStatusClicked: () -> Unit,
    onStatusSelected: (Status) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // "My Status" Add Button
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNewStatusClicked() }
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PitchPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (currentUser.displayName.take(1).ifBlank { "M" }).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PitchPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(PitchSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Status",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "My Status",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }

        // Other Fans' Statuses
        items(statuses.distinctBy { it.userId }, key = { it.statusId }) { status ->
            val hasViewed = status.viewedBy.contains(currentUser.userId)
            val borderColor = if (hasViewed) PitchOutline else PitchSecondary

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onStatusSelected(status) }
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(PitchPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (status.userName.take(1).ifBlank { "F" }).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = status.userName.split(" ").firstOrNull() ?: status.userName,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ChatItemRow(
    chat: Chat,
    currentUserId: String,
    onClick: () -> Unit,
    onTogglePin: (Boolean) -> Unit,
    onToggleMute: (Boolean) -> Unit,
    onToggleArchive: (Boolean) -> Unit
) {
    val isPinned = chat.pinnedBy.contains(currentUserId)
    val isMuted = chat.mutedBy.contains(currentUserId)
    val isArchived = chat.archivedBy.contains(currentUserId)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .testTag("chat_item_${chat.chatId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (chat.isGroup) PitchSecondary else PitchPrimary),
                contentAlignment = Alignment.Center
            ) {
                if (chat.isGroup) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Text(
                        text = (chat.name?.take(1) ?: "F").uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and Last Message
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.name ?: (if (chat.isGroup) "Squad Chat" else "Direct Chat"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    val timeStr = chat.lastMessageTime?.toDate()?.let {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                    } ?: ""
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = PitchSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = chat.lastMessage ?: "No messages yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = PitchPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (isMuted) {
                        Icon(
                            imageVector = Icons.Default.VolumeMute,
                            contentDescription = "Muted",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isPinned) "Unpin Chat" else "Pin to Top") },
                        onClick = {
                            showMenu = false
                            onTogglePin(isPinned)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isMuted) "Unmute" else "Mute Notifications") },
                        onClick = {
                            showMenu = false
                            onToggleMute(isMuted)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isArchived) "Unarchive" else "Archive Chat") },
                        onClick = {
                            showMenu = false
                            onToggleArchive(isArchived)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyChatsView(
    searchQuery: String,
    onStartNewChat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PitchPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = PitchPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (searchQuery.isBlank()) "No matchday chats yet" else "No matches found for \"$searchQuery\"",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Connect with other club supporters and start the banter!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onStartNewChat,
            colors = ButtonDefaults.buttonColors(containerColor = PitchPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Find Football Fans")
        }
    }
}

@Composable
private fun NewChatDialog(
    currentUser: User,
    userRepository: UserRepository,
    onDismiss: () -> Unit,
    onUserSelected: (User) -> Unit
) {
    var searchFanQuery by remember { mutableStateOf("") }
    val searchResults by userRepository.searchUsers(searchFanQuery).collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Pitchside Chat",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchFanQuery,
                    onValueChange = { searchFanQuery = it },
                    placeholder = { Text("Search by username or club...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (searchResults.isEmpty()) {
                    Text(
                        text = if (searchFanQuery.isBlank()) "Search for supporters to message" else "No fans found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.height(260.dp)) {
                        items(searchResults, key = { it.userId }) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUserSelected(user) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(PitchPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (user.displayName.take(1).ifBlank { "F" }).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.displayName,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "@${user.username} • ${user.favoriteClub}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PitchSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CreateGroupDialog(
    currentUser: User,
    userRepository: UserRepository,
    onDismiss: () -> Unit,
    onCreateGroup: (groupName: String, description: String, memberUids: List<String>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val allFans by userRepository.searchUsers("").collectAsState(initial = emptyList())
    val selectedMembers = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Assemble Squad / Group",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name") },
                    placeholder = { Text("e.g. North London Derby Banter") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Match predictions, lineup debates") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Add Squad Members (${selectedMembers.size} selected):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(modifier = Modifier.height(180.dp)) {
                    items(allFans, key = { it.userId }) { fan ->
                        val isSelected = selectedMembers.contains(fan.userId)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedMembers.remove(fan.userId)
                                    else selectedMembers.add(fan.userId)
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) PitchSecondary else PitchPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text(fan.displayName.take(1).uppercase(), color = Color.White, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(fan.displayName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text("@${fan.username} • ${fan.favoriteClub}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (groupName.isNotBlank()) {
                        onCreateGroup(groupName.trim(), description.trim(), selectedMembers.toList())
                    }
                },
                enabled = groupName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PitchPrimary)
            ) {
                Text("Create Squad")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
