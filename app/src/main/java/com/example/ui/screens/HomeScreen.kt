package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CallLogEntity
import com.example.data.model.ChatEntity
import com.example.data.model.StoryEntity
import com.example.data.model.UserEntity
import com.example.ui.components.AvatarView
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EncryptedGreen
import com.example.ui.viewmodel.MessengerViewModel
import com.example.ui.viewmodel.ThemeSetting
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MessengerViewModel,
    modifier: Modifier = Modifier
) {
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val callLogs by viewModel.callLogs.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsStateWithLifecycle()
    val themeSetting by viewModel.themeSetting.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf("CHATS") } // "CHATS", "CALLS", "STORIES", "SETTINGS"
    var showAddStoryDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showNewChatDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Family Chat & Call",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "E2EE",
                                tint = EncryptedGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = if (isOnline) "End-to-End Encrypted • Cloud Synced" else "Offline Mode • Queue Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOnline) EncryptedGreen else MaterialTheme.colorScheme.error,
                            fontSize = 10.sp
                        )
                    }
                },
                actions = {
                    // Online / Offline toggle pill for easy testing of offline message sync
                    Surface(
                        onClick = { viewModel.toggleNetwork() },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOnline) EncryptedGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.testTag("network_toggle_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = "Network Status",
                                tint = if (isOnline) EncryptedGreen else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOnline) "Online" else "Offline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isOnline) EncryptedGreen else MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Dark Mode Quick Toggle
                    IconButton(
                        onClick = {
                            viewModel.themeSetting.value = when (themeSetting) {
                                ThemeSetting.LIGHT -> ThemeSetting.DARK
                                ThemeSetting.DARK -> ThemeSetting.LIGHT
                                ThemeSetting.SYSTEM -> ThemeSetting.DARK
                            }
                        },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (themeSetting == ThemeSetting.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTab == "CHATS",
                    onClick = { selectedTab = "CHATS" },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chats") },
                    label = { Text("Chats") },
                    modifier = Modifier.testTag("nav_chats")
                )
                NavigationBarItem(
                    selected = selectedTab == "CALLS",
                    onClick = { selectedTab = "CALLS" },
                    icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
                    label = { Text("Calls") },
                    modifier = Modifier.testTag("nav_calls")
                )
                NavigationBarItem(
                    selected = selectedTab == "STORIES",
                    onClick = { selectedTab = "STORIES" },
                    icon = { Icon(Icons.Default.History, contentDescription = "Stories") },
                    label = { Text("Stories") },
                    modifier = Modifier.testTag("nav_stories")
                )
                NavigationBarItem(
                    selected = selectedTab == "SAFETY",
                    onClick = { selectedTab = "SAFETY" },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Family Safety") },
                    label = { Text("Safety") },
                    modifier = Modifier.testTag("nav_safety")
                )
                NavigationBarItem(
                    selected = selectedTab == "PROFILE",
                    onClick = {
                        viewModel.currentRoute.value = "PROFILE"
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Family Profile") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == "CHATS") {
                FloatingActionButton(
                    onClick = { showNewChatDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("new_chat_fab")
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Start Chat")
                }
            } else if (selectedTab == "STORIES") {
                FloatingActionButton(
                    onClick = { showAddStoryDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_story_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Story")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "CHATS" -> {
                    // Stories Tray at top of Chats
                    StoriesTray(
                        stories = stories,
                        currentUserName = currentUser?.name ?: "You",
                        onAddStoryClick = { showAddStoryDialog = true },
                        onStoryClick = { index -> viewModel.openStoryViewer(index) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        placeholder = { Text("Search family chats & members...") },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .testTag("search_chats_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Chat list
                    val filteredChats = chats.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.lastMessage.contains(searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredChats, key = { it.id }) { chat ->
                            ChatItemRow(
                                chat = chat,
                                onClick = { viewModel.openChat(chat.id) }
                            )
                        }
                    }
                }

                "CALLS" -> {
                    CallsTabContent(
                        callLogs = callLogs,
                        allUsers = allUsers,
                        onStartCall = { user, isVideo ->
                            viewModel.startCall(user, isVideo)
                        }
                    )
                }

                "STORIES" -> {
                    StoriesTabContent(
                        stories = stories,
                        onAddStory = { showAddStoryDialog = true },
                        onViewStory = { index -> viewModel.openStoryViewer(index) }
                    )
                }

                "SAFETY" -> {
                    FamilySafetyTabContent(
                        allUsers = allUsers.filter { !it.isCurrentUser },
                        onStartCall = { user: UserEntity, isVideo: Boolean ->
                            viewModel.startCall(user, isVideo)
                        },
                        onRequestLocation = { user: UserEntity ->
                            val chat = chats.find { it.memberIds.contains(user.id) }
                            val chatId = chat?.id ?: "chat_grandma"
                            viewModel.sendLocationMessage(
                                chatId = chatId,
                                locationTitle = "🛡️ Real-time Location Request (Family Safety)",
                                lat = 0.0,
                                lng = 0.0
                            )
                            viewModel.openChat(chatId)
                        },
                        onSendEmergencySos = {
                            viewModel.sendLocationMessage(
                                chatId = "chat_family_group",
                                locationTitle = "🚨 EMERGENCY SOS: Family Safety Alert at 450 Sutter St, SF 📍",
                                lat = 37.7891,
                                lng = -122.4082
                            )
                            viewModel.openChat("chat_family_group")
                        }
                    )
                }
            }
        }
    }

    // Add Story Dialog with 24 hours up to 7 days expiry options
    if (showAddStoryDialog) {
        AddStoryDialog(
            onDismiss = { showAddStoryDialog = false },
            onPostStory = { caption, expiryHours ->
                viewModel.postStory(caption, expiryHours)
                showAddStoryDialog = false
            }
        )
    }

    // New Chat / Direct Call Picker Dialog
    if (showNewChatDialog) {
        NewChatDialog(
            users = allUsers.filter { !it.isCurrentUser },
            onDismiss = { showNewChatDialog = false },
            onSelectUser = { user ->
                showNewChatDialog = false
                // Find existing chat or open direct
                val targetChat = chats.find { !it.isGroup && it.memberIds.contains(user.id) }
                if (targetChat != null) {
                    viewModel.openChat(targetChat.id)
                } else {
                    viewModel.openChat("chat_grandma")
                }
            },
            onCallUser = { user, isVideo ->
                showNewChatDialog = false
                viewModel.startCall(user, isVideo)
            }
        )
    }
}

@Composable
fun StoriesTray(
    stories: List<StoryEntity>,
    currentUserName: String,
    onAddStoryClick: () -> Unit,
    onStoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("stories_tray")
    ) {
        // Add Story item
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onAddStoryClick() }
                    .testTag("add_story_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Story",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Add Story",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Contact Stories
        itemsIndexed(stories) { index, story ->
            val isSeen = story.isSeen
            val borderBrush = if (!isSeen) {
                Brush.sweepGradient(listOf(Color(0xFF0F766E), Color(0xFFE11D48), Color(0xFFF59E0B), Color(0xFF0F766E)))
            } else {
                Brush.linearGradient(listOf(Color.Gray.copy(alpha = 0.5f), Color.Gray.copy(alpha = 0.5f)))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onStoryClick(index) }
                    .testTag("story_item_$index")
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, borderBrush, CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarView(
                        name = story.userName,
                        role = story.userRole,
                        avatarColor = story.userAvatarColor,
                        sizeDp = 52
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.userName.split(" ").firstOrNull() ?: story.userName,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ChatItemRow(
    chat: ChatEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = timeFormatter.format(Date(chat.lastMessageTime))

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .testTag("chat_row_${chat.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            AvatarView(
                name = chat.name,
                avatarColor = chat.avatarColor,
                sizeDp = 50,
                isOnline = true
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = chat.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (chat.isPinned) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chat.lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (chat.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallsTabContent(
    callLogs: List<CallLogEntity>,
    allUsers: List<UserEntity>,
    onStartCall: (UserEntity, Boolean) -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Recent Encrypted Calls",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        items(callLogs, key = { it.id }) { call ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = if (call.isMissed) Icons.Default.PhoneMissed else if (call.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = "Call",
                        tint = if (call.isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${call.peerName} (${call.peerRole})",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        val callDetails = if (call.isMissed) "Missed call" else if (call.durationSeconds > 0) "${call.durationSeconds / 60}m ${call.durationSeconds % 60}s" else "Call ended"
                        Text(
                            text = "$callDetails • ${timeFormatter.format(Date(call.timestamp))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Quick call back button
                    val peer = allUsers.find { it.id == call.peerId } ?: UserEntity(
                        id = call.peerId,
                        name = call.peerName,
                        email = "",
                        role = call.peerRole,
                        avatarColor = 0xFF0F766EL,
                        statusMessage = ""
                    )

                    IconButton(onClick = { onStartCall(peer, false) }) {
                        Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onStartCall(peer, true) }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

@Composable
fun StoriesTabContent(
    stories: List<StoryEntity>,
    onAddStory: () -> Unit,
    onViewStory: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddStory() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Story", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Share a Family Story",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Photos disappear after 24 hours, 2, 3, 4, 5, 6, or 7 days.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Active Family Stories",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        itemsIndexed(stories) { index, story ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onViewStory(index) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    AvatarView(
                        name = story.userName,
                        role = story.userRole,
                        avatarColor = story.userAvatarColor,
                        sizeDp = 48
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${story.userName} (${story.userRole})",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = story.caption,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val hoursRemaining = ((story.expiryTimestamp - System.currentTimeMillis()) / 3600_000L).coerceAtLeast(1)
                        Text(
                            text = "⏳ Expires in ${hoursRemaining}h (${story.expiryHours / 24}d duration)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddStoryDialog(
    onDismiss: () -> Unit,
    onPostStory: (caption: String, expiryHours: Int) -> Unit
) {
    var caption by remember { mutableStateOf("") }
    var selectedExpiryHours by remember { mutableIntStateOf(24) } // default 24h

    // Supported expiry options: 24h, 2 days (48h), 3 days (72h), 4 days (96h), 5 days (120h), 6 days (144h), 7 days (168h)
    val expiryOptions = listOf(
        Pair("24 Hours", 24),
        Pair("2 Days", 48),
        Pair("3 Days", 72),
        Pair("4 Days", 96),
        Pair("5 Days", 120),
        Pair("6 Days", 144),
        Pair("7 Days", 168)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Upload Family Story", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Upload a photo to your family circle with custom disappearing timer:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Simulated Photo Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F766E), Color(0xFF0284C7), Color(0xFFF59E0B))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📷 Photo Selected",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Story caption...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Select Expiry Period:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(expiryOptions) { (label, hours) ->
                        FilterChip(
                            selected = selectedExpiryHours == hours,
                            onClick = { selectedExpiryHours = hours },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPostStory(caption.ifBlank { "Family Story" }, selectedExpiryHours)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Post Story")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NewChatDialog(
    users: List<UserEntity>,
    onDismiss: () -> Unit,
    onSelectUser: (UserEntity) -> Unit,
    onCallUser: (UserEntity, Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Family & Friends", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Select a family member to chat or call with end-to-end encryption:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn {
                    items(users) { user ->
                        Surface(
                            onClick = { onSelectUser(user) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                AvatarView(
                                    name = user.name,
                                    role = user.role,
                                    avatarColor = user.avatarColor,
                                    sizeDp = 40,
                                    isOnline = user.isOnline
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = user.role,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { onCallUser(user, false) }) {
                                    Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onCallUser(user, true) }) {
                                    Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun FamilySafetyTabContent(
    allUsers: List<UserEntity>,
    onStartCall: (UserEntity, Boolean) -> Unit,
    onRequestLocation: (UserEntity) -> Unit,
    onSendEmergencySos: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSosConfirmation by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Radar Overview Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Family Safety Radar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Encrypted real-time location & emergency alerts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Emergency SOS button
                    Button(
                        onClick = { showSosConfirmation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_emergency_sos_button")
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send Emergency SOS to Family",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Family Members Safe Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Family members with live battery, last check-in, and location request button
        items(allUsers, key = { it.id }) { user ->
            val batteryLevel = when (user.id) {
                "user_grandma" -> 92
                "user_mom" -> 78
                "user_dad" -> 45
                "user_brother" -> 64
                else -> 85
            }
            val lastLocation = when (user.id) {
                "user_grandma" -> "Home • 142 Maple St 🏡"
                "user_mom" -> "Memorial Health Clinic 🏥"
                "user_dad" -> "Downtown Design Studio 🏢"
                "user_brother" -> "Lincoln High School 🏫"
                else -> "Family Circle Safe Zone"
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().testTag("safety_user_${user.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AvatarView(
                            name = user.name,
                            role = user.role,
                            avatarColor = user.avatarColor,
                            sizeDp = 42,
                            isOnline = user.isOnline
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = user.role,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lastLocation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Battery indicator
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (batteryLevel > 20) EncryptedGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🔋 $batteryLevel%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (batteryLevel > 20) EncryptedGreen else DangerRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Request Real-time Location Button
                        FilledTonalButton(
                            onClick = { onRequestLocation(user) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("request_location_${user.id}")
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Request Live GPS", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Quick Call Buttons
                        IconButton(
                            onClick = { onStartCall(user, false) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = { onStartCall(user, true) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }

    // SOS Confirmation Alert
    if (showSosConfirmation) {
        AlertDialog(
            onDismissRequest = { showSosConfirmation = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(32.dp)) },
            title = { Text("Send Emergency SOS?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will immediately send an urgent emergency notification with your live GPS coordinates to all family members.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSosConfirmation = false
                        onSendEmergencySos()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Send SOS Alert Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun FamilySafetyTabContent(
    allUsers: List<UserEntity>,
    onStartCall: (UserEntity, Boolean) -> Unit,
    onRequestLocation: (UserEntity) -> Unit,
    onSendEmergencySos: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSosConfirmation by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Radar Overview Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Family Safety Radar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Encrypted real-time location & emergency alerts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Emergency SOS button
                    Button(
                        onClick = { showSosConfirmation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("send_emergency_sos_button")
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send Emergency SOS to Family",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "Family Members Safe Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Family members with live battery, last check-in, and location request button
        items(allUsers, key = { it.id }) { user ->
            val batteryLevel = when (user.id) {
                "user_grandma" -> 92
                "user_mom" -> 78
                "user_dad" -> 45
                "user_brother" -> 64
                else -> 85
            }
            val lastLocation = when (user.id) {
                "user_grandma" -> "Home • 142 Maple St 🏡"
                "user_mom" -> "Memorial Health Clinic 🏥"
                "user_dad" -> "Downtown Design Studio 🏢"
                "user_brother" -> "Lincoln High School 🏫"
                else -> "Family Circle Safe Zone"
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().testTag("safety_user_${user.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AvatarView(
                            name = user.name,
                            role = user.role,
                            avatarColor = user.avatarColor,
                            sizeDp = 42,
                            isOnline = user.isOnline
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = user.role,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = lastLocation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Battery indicator
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (batteryLevel > 20) EncryptedGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🔋 $batteryLevel%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (batteryLevel > 20) EncryptedGreen else DangerRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Request Real-time Location Button
                        FilledTonalButton(
                            onClick = { onRequestLocation(user) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("request_location_${user.id}")
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Request Live GPS", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Quick Call Buttons
                        IconButton(
                            onClick = { onStartCall(user, false) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = { onStartCall(user, true) },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }

    // SOS Confirmation Alert
    if (showSosConfirmation) {
        AlertDialog(
            onDismissRequest = { showSosConfirmation = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(32.dp)) },
            title = { Text("Send Emergency SOS?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will immediately send an urgent emergency notification with your live GPS coordinates to all family members.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSosConfirmation = false
                        onSendEmergencySos()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Send SOS Alert Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

