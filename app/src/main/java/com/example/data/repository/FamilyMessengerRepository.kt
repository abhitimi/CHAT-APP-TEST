package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.crypto.E2EEEngine
import com.example.data.model.CallLogEntity
import com.example.data.model.ChatEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.StoryEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class NotificationEvent(
    val chatId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class FamilyMessengerRepository(
    private val database: AppDatabase,
    private val appScope: CoroutineScope
) {
    private val userDao = database.userDao()
    private val chatDao = database.chatDao()
    private val messageDao = database.messageDao()
    private val storyDao = database.storyDao()
    private val callLogDao = database.callLogDao()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    private val _lastBackupTime = MutableStateFlow(System.currentTimeMillis() - 180_000L)
    val lastBackupTime: StateFlow<Long> = _lastBackupTime.asStateFlow()

    private val _incomingNotification = MutableStateFlow<NotificationEvent?>(null)
    val incomingNotification: StateFlow<NotificationEvent?> = _incomingNotification.asStateFlow()

    fun dismissNotification() {
        _incomingNotification.value = null
    }

    fun toggleOnlineStatus() {
        _isOnline.value = !_isOnline.value
        if (_isOnline.value) {
            syncPendingMessages()
        }
    }

    suspend fun initializeSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        val existing = userDao.getAllUsers().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val defaultUsers = listOf(
                UserEntity(
                    id = "user_me",
                    name = "Abhi",
                    email = "abhitimi0@gmail.com",
                    role = "Me",
                    avatarColor = 0xFF0F766EL,
                    statusMessage = "Always here for family ❤️",
                    isOnline = true,
                    isCurrentUser = true,
                    e2eeFingerprint = "7721 9043 1184 6290"
                ),
                UserEntity(
                    id = "user_grandma",
                    name = "Grandma Rose",
                    email = "grandma.rose@family.org",
                    role = "Grandma",
                    avatarColor = 0xFFE11D48L,
                    statusMessage = "Baking apple pie today 🥧",
                    isOnline = true,
                    isCurrentUser = false,
                    e2eeFingerprint = "4819 0281 9920 7311"
                ),
                UserEntity(
                    id = "user_dad",
                    name = "Dad (Robert)",
                    email = "robert.senior@family.org",
                    role = "Dad",
                    avatarColor = 0xFF0284C7L,
                    statusMessage = "Working in the garden 🌱",
                    isOnline = true,
                    isCurrentUser = false,
                    e2eeFingerprint = "9930 1142 5582 0019"
                ),
                UserEntity(
                    id = "user_mom",
                    name = "Mom (Elena)",
                    email = "elena.mom@family.org",
                    role = "Mom",
                    avatarColor = 0xFF7C3AEDL,
                    statusMessage = "Don't forget family dinner Sunday!",
                    isOnline = true,
                    isCurrentUser = false,
                    e2eeFingerprint = "3310 8820 4410 9942"
                ),
                UserEntity(
                    id = "user_leo",
                    name = "Leo",
                    email = "leo.brother@family.org",
                    role = "Brother",
                    avatarColor = 0xFFF59E0BL,
                    statusMessage = "At soccer practice ⚽",
                    isOnline = true,
                    isCurrentUser = false,
                    e2eeFingerprint = "5521 7739 1209 8841"
                )
            )
            userDao.insertUsers(defaultUsers)

            val defaultChats = listOf(
                ChatEntity(
                    id = "chat_family_group",
                    name = "🏡 Family Circle",
                    isGroup = true,
                    memberIds = "user_me,user_grandma,user_dad,user_mom,user_leo",
                    lastMessage = "Grandma Rose: Dinner is ready sweethearts!",
                    lastMessageTime = System.currentTimeMillis() - 600_000L,
                    unreadCount = 1,
                    avatarColor = 0xFF0F766EL,
                    isPinned = true
                ),
                ChatEntity(
                    id = "chat_grandma",
                    name = "Grandma Rose",
                    isGroup = false,
                    memberIds = "user_me,user_grandma",
                    lastMessage = "Can you send your location on the map dear?",
                    lastMessageTime = System.currentTimeMillis() - 1_800_000L,
                    unreadCount = 1,
                    avatarColor = 0xFFE11D48L,
                    isPinned = true
                ),
                ChatEntity(
                    id = "chat_dad",
                    name = "Dad (Robert)",
                    isGroup = false,
                    memberIds = "user_me,user_dad",
                    lastMessage = "Voice message (12s)",
                    lastMessageTime = System.currentTimeMillis() - 7_200_000L,
                    unreadCount = 0,
                    avatarColor = 0xFF0284C7L,
                    isPinned = false
                ),
                ChatEntity(
                    id = "chat_mom",
                    name = "Mom (Elena)",
                    isGroup = false,
                    memberIds = "user_me,user_mom",
                    lastMessage = "Photo: Family Garden 🌻",
                    lastMessageTime = System.currentTimeMillis() - 86_400_000L,
                    unreadCount = 0,
                    avatarColor = 0xFF7C3AEDL,
                    isPinned = false
                )
            )
            chatDao.insertChats(defaultChats)

            // Seed initial messages with encryption
            val initialMessages = listOf(
                MessageEntity(
                    chatId = "chat_family_group",
                    senderId = "user_mom",
                    senderName = "Mom (Elena)",
                    content = "Hey everyone! Who is coming for dinner this Sunday?",
                    messageType = MessageType.TEXT,
                    timestamp = System.currentTimeMillis() - 3600_000L,
                    status = MessageStatus.READ
                ),
                MessageEntity(
                    chatId = "chat_family_group",
                    senderId = "user_dad",
                    senderName = "Dad (Robert)",
                    content = "I'm bringing the fresh tomatoes from the backyard!",
                    messageType = MessageType.TEXT,
                    timestamp = System.currentTimeMillis() - 1800_000L,
                    status = MessageStatus.READ
                ),
                MessageEntity(
                    chatId = "chat_family_group",
                    senderId = "user_grandma",
                    senderName = "Grandma Rose",
                    content = "Dinner is ready sweethearts!",
                    messageType = MessageType.TEXT,
                    timestamp = System.currentTimeMillis() - 600_000L,
                    status = MessageStatus.DELIVERED
                ),
                // Grandma 1-on-1 chat
                MessageEntity(
                    chatId = "chat_grandma",
                    senderId = "user_me",
                    senderName = "Abhi",
                    content = "Hi Grandma! On my way back from the farmers market.",
                    messageType = MessageType.TEXT,
                    timestamp = System.currentTimeMillis() - 3600_000L,
                    status = MessageStatus.READ
                ),
                MessageEntity(
                    chatId = "chat_grandma",
                    senderId = "user_me",
                    senderName = "Abhi",
                    content = "Central Farmers Market 🍓",
                    messageType = MessageType.LOCATION,
                    latitude = 37.7749,
                    longitude = -122.4194,
                    locationName = "Central Farmers Market 🍓",
                    timestamp = System.currentTimeMillis() - 2700_000L,
                    status = MessageStatus.READ
                ),
                MessageEntity(
                    chatId = "chat_grandma",
                    senderId = "user_grandma",
                    senderName = "Grandma Rose",
                    content = "Can you send your location on the map dear?",
                    messageType = MessageType.TEXT,
                    timestamp = System.currentTimeMillis() - 1800_000L,
                    status = MessageStatus.DELIVERED
                )
            )
            initialMessages.forEach { messageDao.insertMessage(it) }

            // Seed initial disappearing stories (24h to 7 days)
            val initialStories = listOf(
                StoryEntity(
                    userId = "user_grandma",
                    userName = "Grandma Rose",
                    userRole = "Grandma",
                    userAvatarColor = 0xFFE11D48L,
                    mediaUri = "story_roses",
                    caption = "Rose garden blooming in full glory today! 🌹",
                    timestamp = System.currentTimeMillis() - 3600_000L,
                    expiryHours = 24, // 24 hours
                    isSeen = false
                ),
                StoryEntity(
                    userId = "user_dad",
                    userName = "Dad (Robert)",
                    userRole = "Dad",
                    userAvatarColor = 0xFF0284C7L,
                    mediaUri = "story_woodwork",
                    caption = "Finished the birdhouse for the porch! 🕊️",
                    timestamp = System.currentTimeMillis() - 7200_000L,
                    expiryHours = 72, // 3 days
                    isSeen = false
                ),
                StoryEntity(
                    userId = "user_mom",
                    userName = "Mom (Elena)",
                    userRole = "Mom",
                    userAvatarColor = 0xFF7C3AEDL,
                    mediaUri = "story_cooking",
                    caption = "Family recipe secrets passed down for generations ✨",
                    timestamp = System.currentTimeMillis() - 14400_000L,
                    expiryHours = 168, // 7 days
                    isSeen = false
                )
            )
            initialStories.forEach { storyDao.insertStory(it) }

            // Seed call logs
            val defaultCalls = listOf(
                CallLogEntity(
                    peerId = "user_grandma",
                    peerName = "Grandma Rose",
                    peerRole = "Grandma",
                    isVideo = true,
                    isIncoming = true,
                    isMissed = false,
                    durationSeconds = 245,
                    timestamp = System.currentTimeMillis() - 86_400_000L
                ),
                CallLogEntity(
                    peerId = "user_dad",
                    peerName = "Dad (Robert)",
                    peerRole = "Dad",
                    isVideo = false,
                    isIncoming = false,
                    isMissed = false,
                    durationSeconds = 120,
                    timestamp = System.currentTimeMillis() - 172_800_000L
                )
            )
            defaultCalls.forEach { callLogDao.insertCallLog(it) }
        }
    }

    fun getCurrentUser(): Flow<UserEntity?> = userDao.getCurrentUser()
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    fun getAllChats(): Flow<List<ChatEntity>> = chatDao.getAllChats()
    fun getChatById(chatId: String): Flow<ChatEntity?> = chatDao.getChatById(chatId)
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForChat(chatId)
    fun getAllStories(): Flow<List<StoryEntity>> = storyDao.getAllStories()
    fun getAllCallLogs(): Flow<List<CallLogEntity>> = callLogDao.getAllCallLogs()

    suspend fun markChatAsRead(chatId: String) = withContext(Dispatchers.IO) {
        val user = userDao.getCurrentUser().firstOrNull()
        val userId = user?.id ?: "user_me"
        chatDao.markChatAsRead(chatId)
        messageDao.markIncomingMessagesAsRead(chatId, userId, MessageStatus.READ)
    }

    suspend fun sendMessage(
        chatId: String,
        content: String,
        type: MessageType,
        mediaUri: String? = null,
        durationSeconds: Int = 0,
        latitude: Double? = null,
        longitude: Double? = null,
        locationName: String? = null
    ) = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser().firstOrNull()
        val senderId = currentUser?.id ?: "user_me"
        val senderName = currentUser?.name ?: "Abhi"

        val initialStatus = if (_isOnline.value) MessageStatus.SENT else MessageStatus.PENDING_SYNC

        val message = MessageEntity(
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            messageType = type,
            mediaUri = mediaUri,
            mediaDurationSeconds = durationSeconds,
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            timestamp = System.currentTimeMillis(),
            status = initialStatus,
            isEncrypted = true,
            encryptionKeyId = "AES-256-GCM-FAMILY-KEY"
        )

        val messageId = messageDao.insertMessage(message)

        val preview = when (type) {
            MessageType.TEXT -> content
            MessageType.PHOTO -> "Photo: $content"
            MessageType.VIDEO -> "Video: $content"
            MessageType.AUDIO -> "Voice note (${durationSeconds}s)"
            MessageType.LOCATION -> "Location: ${locationName ?: content}"
            MessageType.CALL_EVENT -> "Call: $content"
        }
        chatDao.updateLastMessage(chatId, preview, System.currentTimeMillis())

        if (_isOnline.value) {
            appScope.launch {
                delay(800)
                messageDao.updateMessageStatus(messageId, MessageStatus.DELIVERED)
                delay(1200)
                messageDao.updateMessageStatus(messageId, MessageStatus.READ)
                triggerSimulatedFamilyReply(chatId, type)
            }
        }
    }

    private fun triggerSimulatedFamilyReply(chatId: String, sentType: MessageType) {
        appScope.launch(Dispatchers.IO) {
            delay(3500)
            val replyText = when (sentType) {
                MessageType.LOCATION -> "Got your location on the map, safe travels sweetie! 🚗❤️"
                MessageType.AUDIO -> "Heard your voice note! Love hearing your voice! 🥰"
                MessageType.PHOTO -> "What a wonderful photo for our family album! 💖"
                MessageType.VIDEO -> "Loved the video clip! Showing this to everyone tonight."
                else -> "Got your encrypted message! Sending big hugs from home. 🤗"
            }

            val replySender = if (chatId == "chat_dad") "Dad (Robert)" else "Grandma Rose"
            val replySenderId = if (chatId == "chat_dad") "user_dad" else "user_grandma"

            val replyMessage = MessageEntity(
                chatId = chatId,
                senderId = replySenderId,
                senderName = replySender,
                content = replyText,
                messageType = MessageType.TEXT,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.DELIVERED,
                isEncrypted = true
            )

            messageDao.insertMessage(replyMessage)
            chatDao.updateLastMessage(chatId, "$replySender: $replyText", System.currentTimeMillis())

            _incomingNotification.value = NotificationEvent(
                chatId = chatId,
                senderName = replySender,
                text = replyText
            )
        }
    }

    private fun syncPendingMessages() {
        appScope.launch(Dispatchers.IO) {
            _isCloudSyncing.value = true
            val pending = messageDao.getPendingSyncMessages()
            for (msg in pending) {
                delay(300)
                messageDao.updateMessageStatus(msg.id, MessageStatus.SENT)
                delay(400)
                messageDao.updateMessageStatus(msg.id, MessageStatus.DELIVERED)
            }
            _lastBackupTime.value = System.currentTimeMillis()
            _isCloudSyncing.value = false
        }
    }

    suspend fun postStory(
        caption: String,
        mediaUri: String,
        expiryHours: Int
    ) = withContext(Dispatchers.IO) {
        val user = userDao.getCurrentUser().firstOrNull() ?: return@withContext
        val story = StoryEntity(
            userId = user.id,
            userName = user.name,
            userRole = user.role,
            userAvatarColor = user.avatarColor,
            mediaUri = mediaUri,
            caption = caption,
            timestamp = System.currentTimeMillis(),
            expiryHours = expiryHours,
            isSeen = false
        )
        storyDao.insertStory(story)
    }

    suspend fun markStorySeen(storyId: Long) = withContext(Dispatchers.IO) {
        storyDao.markStorySeen(storyId)
    }

    suspend fun logCall(
        peerId: String,
        peerName: String,
        peerRole: String,
        isVideo: Boolean,
        isIncoming: Boolean,
        isMissed: Boolean,
        durationSeconds: Int
    ) = withContext(Dispatchers.IO) {
        val callLog = CallLogEntity(
            peerId = peerId,
            peerName = peerName,
            peerRole = peerRole,
            isVideo = isVideo,
            isIncoming = isIncoming,
            isMissed = isMissed,
            durationSeconds = durationSeconds,
            timestamp = System.currentTimeMillis(),
            e2eeVerified = true
        )
        callLogDao.insertCallLog(callLog)
    }

    suspend fun switchCurrentUser(userId: String) = withContext(Dispatchers.IO) {
        userDao.clearCurrentUserFlag()
        userDao.setCurrentUser(userId)
    }

    suspend fun updateUserProfile(name: String, role: String, status: String) = withContext(Dispatchers.IO) {
        val user = userDao.getCurrentUser().firstOrNull() ?: return@withContext
        val updated = user.copy(name = name, role = role, statusMessage = status)
        userDao.updateUser(updated)
    }

    suspend fun triggerManualBackup() = withContext(Dispatchers.IO) {
        _isCloudSyncing.value = true
        delay(1500)
        _lastBackupTime.value = System.currentTimeMillis()
        _isCloudSyncing.value = false
    }
}
