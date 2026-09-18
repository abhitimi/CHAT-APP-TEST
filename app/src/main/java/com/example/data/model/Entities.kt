package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String, // "Grandma", "Dad", "Mom", "Son", "Family"
    val avatarColor: Long, // ARGB Color
    val statusMessage: String,
    val isOnline: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis(),
    val e2eeFingerprint: String = "4819 0281 9920 7311",
    val isCurrentUser: Boolean = false
)

enum class MessageType {
    TEXT,
    PHOTO,
    VIDEO,
    AUDIO,
    LOCATION,
    CALL_EVENT
}

enum class MessageStatus {
    PENDING_SYNC,
    SENT,
    DELIVERED,
    READ
}

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val messageType: MessageType = MessageType.TEXT,
    val mediaUri: String? = null,
    val mediaDurationSeconds: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.READ,
    val isEncrypted: Boolean = true,
    val encryptionKeyId: String = "AES-256-GCM-FAMILY-KEY"
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isGroup: Boolean,
    val memberIds: String, // Comma-separated user IDs
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val avatarColor: Long = 0xFF0F766EL,
    val isPinned: Boolean = false,
    val e2eeKeyFingerprint: String = "E2EE-4819-0281"
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userName: String,
    val userRole: String,
    val userAvatarColor: Long,
    val mediaUri: String,
    val caption: String,
    val timestamp: Long = System.currentTimeMillis(),
    val expiryHours: Int = 24, // 24h, 48h (2d), 72h (3d), 96h (4d), 120h (5d), 144h (6d), 168h (7d)
    val isSeen: Boolean = false
) {
    val expiryTimestamp: Long
        get() = timestamp + (expiryHours * 3600_000L)

    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiryTimestamp
}

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val peerId: String,
    val peerName: String,
    val peerRole: String,
    val isVideo: Boolean,
    val isIncoming: Boolean,
    val isMissed: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val e2eeVerified: Boolean = true
)
