package com.example.ui.screens

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.data.model.UserEntity
import com.example.ui.components.AvatarView
import com.example.ui.components.E2EESecurityDialog
import com.example.ui.components.LocationBubble
import com.example.ui.components.MessageStatusIcon
import com.example.ui.components.PhotoBubble
import com.example.ui.components.VideoBubble
import com.example.ui.components.VoiceNoteBubble
import com.example.ui.components.VoiceRecordingBar
import com.example.ui.theme.EncryptedGreen
import com.example.ui.viewmodel.MessengerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    viewModel: MessengerViewModel,
    modifier: Modifier = Modifier
) {
    val activeChat by viewModel.activeChat.collectAsStateWithLifecycle()
    val messages by viewModel.activeChatMessages.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsStateWithLifecycle()
    val recordingDurationSeconds by viewModel.recordingDurationSeconds.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showE2EEDialog by remember { mutableStateOf(false) }
    var showMapsToast by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val currentUserId = currentUser?.id ?: "user_me"
    val chat = activeChat

    // Target peer for direct calls/identity
    val peerUser = allUsers.find { !it.isCurrentUser && chat?.memberIds?.contains(it.id) == true }
        ?: UserEntity(
            id = "user_grandma",
            name = chat?.name ?: "Family Member",
            email = "",
            role = "Family",
            avatarColor = chat?.avatarColor ?: 0xFF0F766EL,
            statusMessage = ""
        )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.closeChat() },
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    if (chat != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showE2EEDialog = true }
                        ) {
                            AvatarView(
                                name = chat.name,
                                avatarColor = chat.avatarColor,
                                sizeDp = 38,
                                isOnline = true
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = chat.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Encrypted",
                                        tint = EncryptedGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    text = if (chat.isGroup) "Family Circle Group • E2EE" else "Active now • Tap for E2EE code",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Audio Call Button
                    IconButton(
                        onClick = { viewModel.startCall(peerUser, isVideo = false) },
                        modifier = Modifier.testTag("chat_audio_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Audio Call",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Video Call Button
                    IconButton(
                        onClick = { viewModel.startCall(peerUser, isVideo = true) },
                        modifier = Modifier.testTag("chat_video_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (isRecordingVoice) {
                VoiceRecordingBar(
                    durationSeconds = recordingDurationSeconds,
                    onCancel = { viewModel.cancelVoiceRecording() },
                    onSend = {
                        if (chat != null) {
                            viewModel.stopAndSendVoiceRecording(chat.id)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        // Attachment Button (+)
                        IconButton(
                            onClick = { showAttachmentMenu = true },
                            modifier = Modifier.testTag("attachment_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Attachment",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Message Text Input
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = {
                                Text(
                                    text = "Encrypted message...",
                                    fontSize = 14.sp
                                )
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            maxLines = 4,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_message_input")
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // If text is present, show Send button; otherwise show Mic button to record audio
                        if (textInput.isNotBlank()) {
                            FilledIconButton(
                                onClick = {
                                    if (chat != null) {
                                        viewModel.sendTextMessage(chat.id, textInput)
                                        textInput = ""
                                    }
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .size(46.dp)
                                    .testTag("send_message_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            FilledIconButton(
                                onClick = { viewModel.startVoiceRecording() },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier
                                    .size(46.dp)
                                    .testTag("record_audio_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Record Audio",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Initial E2EE encryption banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { showE2EEDialog = true }
                    .testTag("e2ee_security_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = EncryptedGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Messages & calls in this chat are end-to-end encrypted with AES-256-GCM. Tap to verify safety numbers.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            // Message list
            val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val isSelf = message.senderId == currentUserId

                    Column(
                        horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        // Sender label for group chat
                        if (!isSelf && chat?.isGroup == true) {
                            Text(
                                text = message.senderName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                            )
                        }

                        // Message bubble content
                        when (message.messageType) {
                            MessageType.TEXT -> {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isSelf) 16.dp else 4.dp,
                                        bottomEnd = if (isSelf) 4.dp else 16.dp
                                    ),
                                    color = if (isSelf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 2.dp,
                                    modifier = Modifier.testTag("message_bubble_${message.id}")
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(
                                            text = message.content,
                                            color = if (isSelf) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Text(
                                                text = timeFormatter.format(Date(message.timestamp)),
                                                fontSize = 10.sp,
                                                color = if (isSelf) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                            if (isSelf) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                MessageStatusIcon(status = message.status)
                                            }
                                        }
                                    }
                                }
                            }

                            MessageType.PHOTO -> {
                                PhotoBubble(
                                    caption = message.content,
                                    isSelf = isSelf
                                )
                            }

                            MessageType.VIDEO -> {
                                VideoBubble(
                                    title = message.content,
                                    durationSeconds = message.mediaDurationSeconds,
                                    isSelf = isSelf
                                )
                            }

                            MessageType.AUDIO -> {
                                VoiceNoteBubble(
                                    durationSeconds = message.mediaDurationSeconds,
                                    isSelf = isSelf
                                )
                            }

                            MessageType.LOCATION -> {
                                val isRequest = message.latitude == 0.0 || 
                                    message.content.contains("Real-time Location Request") || 
                                    message.locationName?.contains("Real-time Location Request") == true

                                if (isRequest) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelf) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                                        ),
                                        modifier = Modifier.width(260.dp).testTag("realtime_location_request_bubble")
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Security,
                                                    contentDescription = "Safety Alert",
                                                    tint = if (isSelf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Family Safety Radar",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = if (isSelf) {
                                                    "You requested real-time location from family for safety."
                                                } else {
                                                    "${message.senderName} is requesting your live GPS location for safety."
                                                },
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                            if (!isSelf && chat != null) {
                                                Button(
                                                    onClick = {
                                                        viewModel.sendLocationMessage(
                                                            chatId = chat.id,
                                                            locationTitle = "Live GPS: 450 Sutter St, San Francisco 📍",
                                                            lat = 37.7891,
                                                            lng = -122.4082
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary
                                                    ),
                                                    modifier = Modifier.fillMaxWidth().testTag("respond_live_location_button")
                                                ) {
                                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Share Live GPS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "⏳ Live ping sent. Waiting for response...",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(6.dp),
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    LocationBubble(
                                        locationTitle = message.locationName ?: message.content,
                                        lat = message.latitude ?: 37.7749,
                                        lng = message.longitude ?: -122.4194,
                                        isSelf = isSelf,
                                        onOpenInMaps = {
                                            showMapsToast = "Viewing ${message.locationName ?: "Location"} on GPS Map"
                                        }
                                    )
                                }
                            }

                            MessageType.CALL_EVENT -> {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text(
                                        text = "📞 ${message.content}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Attachment Options Dialog (Photo, Video, Location update)
    if (showAttachmentMenu) {
        AttachmentSelectionDialog(
            onDismiss = { showAttachmentMenu = false },
            onSendPhoto = { label ->
                if (chat != null) {
                    viewModel.sendPhotoMessage(chat.id, label)
                }
                showAttachmentMenu = false
            },
            onSendVideo = { title ->
                if (chat != null) {
                    viewModel.sendVideoMessage(chat.id, title)
                }
                showAttachmentMenu = false
            },
            onSendLocation = { locTitle, lat, lng ->
                if (chat != null) {
                    viewModel.sendLocationMessage(chat.id, locTitle, lat, lng)
                }
                showAttachmentMenu = false
            },
            onSendLocationRequest = {
                if (chat != null) {
                    viewModel.sendLocationMessage(
                        chatId = chat.id,
                        locationTitle = "🛡️ Real-time Location Request (Family Safety)",
                        lat = 0.0,
                        lng = 0.0
                    )
                }
                showAttachmentMenu = false
            }
        )
    }

    // E2EE Security Safety Numbers verification dialog
    if (showE2EEDialog) {
        E2EESecurityDialog(
            user = peerUser,
            currentUserId = currentUserId,
            onDismiss = { showE2EEDialog = false }
        )
    }

    // Maps Toast notification
    if (showMapsToast != null) {
        AlertDialog(
            onDismissRequest = { showMapsToast = null },
            icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Encrypted Family Location") },
            text = { Text(showMapsToast ?: "") },
            confirmButton = {
                TextButton(onClick = { showMapsToast = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun AttachmentSelectionDialog(
    onDismiss: () -> Unit,
    onSendPhoto: (String) -> Unit,
    onSendVideo: (String) -> Unit,
    onSendLocation: (String, Double, Double) -> Unit,
    onSendLocationRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share with Family", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Photo Option
                Surface(
                    onClick = { onSendPhoto("Sunday Family Gathering 📸") },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth().testTag("attach_photo_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Send Photo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("High quality encrypted photo", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Video Option
                Surface(
                    onClick = { onSendVideo("Quick family hello video clip 🎥") },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth().testTag("attach_video_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Send Video Message", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Compressed for low data usage", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Location Check-in Option
                Surface(
                    onClick = {
                        onSendLocation("Grandma's House, 142 Maple St 🏡", 37.7833, -122.4167)
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.fillMaxWidth().testTag("attach_location_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Share Location Check-in", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Real-time GPS safe update", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Real-time Location Request for Family Safety
                Surface(
                    onClick = onSendLocationRequest,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth().testTag("attach_location_request_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Request Real-time Location", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text("Family Safety Radar - Ask member for live GPS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
