package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.crypto.E2EEEngine
import com.example.data.model.CallLogEntity
import com.example.data.model.ChatEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.data.model.StoryEntity
import com.example.data.model.UserEntity
import com.example.data.repository.FamilyMessengerRepository
import com.example.data.repository.NotificationEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ThemeSetting {
    SYSTEM,
    LIGHT,
    DARK
}

data class ActiveCallState(
    val peerId: String,
    val peerName: String,
    val peerRole: String,
    val peerAvatarColor: Long,
    val isVideo: Boolean,
    val isIncoming: Boolean,
    val isConnected: Boolean = false,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isCameraOn: Boolean = true,
    val isFrontCamera: Boolean = true,
    val e2eeVerificationCode: String = "AES-GCM • 4819 0281 9920",
    val latencyMs: Int = 28,
    val dataRateKbps: Int = 450
)

class MessengerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = FamilyMessengerRepository(database, viewModelScope)

    init {
        viewModelScope.launch {
            repository.initializeSampleDataIfEmpty()
        }
    }

    // App Preferences
    val themeSetting = MutableStateFlow(ThemeSetting.SYSTEM)
    val isDataSaverEnabled = MutableStateFlow(false)
    val isLargeFontAccessibility = MutableStateFlow(false)

    // Repository Flows
    val currentUser: StateFlow<UserEntity?> = repository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chats: StateFlow<List<ChatEntity>> = repository.getAllChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stories: StateFlow<List<StoryEntity>> = repository.getAllStories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val callLogs: StateFlow<List<CallLogEntity>> = repository.getAllCallLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOnline: StateFlow<Boolean> = repository.isOnline
    val isCloudSyncing: StateFlow<Boolean> = repository.isCloudSyncing
    val lastBackupTime: StateFlow<Long> = repository.lastBackupTime
    val incomingNotification: StateFlow<NotificationEvent?> = repository.incomingNotification
    val activeNotification: StateFlow<NotificationEvent?> = incomingNotification

    // Selected Chat
    val activeChatId = MutableStateFlow<String?>(null)

    val activeChatMessages: StateFlow<List<MessageEntity>> = activeChatId
        .flatMapLatest { id ->
            if (id != null) repository.getMessagesForChat(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeChat: StateFlow<ChatEntity?> = activeChatId
        .flatMapLatest { id ->
            if (id != null) repository.getChatById(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Active Call
    private val _activeCall = MutableStateFlow<ActiveCallState?>(null)
    val activeCall: StateFlow<ActiveCallState?> = _activeCall.asStateFlow()

    private var callTimerJob: Job? = null

    // Audio Voice Recording State
    val isRecordingVoice = MutableStateFlow(false)
    val recordingDurationSeconds = MutableStateFlow(0)
    private var recordingJob: Job? = null

    // Selected Story for viewing
    val activeStoryIndex = MutableStateFlow<Int?>(null)

    // Security Details Dialog
    val showE2EEDialogForUser = MutableStateFlow<UserEntity?>(null)

    // Navigation state: "CHATS", "CALLS", "STORIES", "PROFILE", "CHAT_DETAIL", "CALL_SCREEN", "STORY_VIEWER"
    val currentRoute = MutableStateFlow("CHATS")

    fun openChat(chatId: String) {
        activeChatId.value = chatId
        currentRoute.value = "CHAT_DETAIL"
        viewModelScope.launch {
            repository.markChatAsRead(chatId)
        }
    }

    fun closeChat() {
        activeChatId.value = null
        currentRoute.value = "CHATS"
    }

    fun toggleNetwork() {
        repository.toggleOnlineStatus()
    }

    fun triggerCloudBackup() {
        viewModelScope.launch {
            repository.triggerManualBackup()
        }
    }

    fun dismissNotification() {
        repository.dismissNotification()
    }

    // Messaging Actions
    fun sendTextMessage(chatId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(chatId = chatId, content = text.trim(), type = MessageType.TEXT)
        }
    }

    fun sendPhotoMessage(chatId: String, photoLabel: String) {
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                content = photoLabel,
                type = MessageType.PHOTO,
                mediaUri = "photo_family_shared"
            )
        }
    }

    fun sendVideoMessage(chatId: String, videoTitle: String, durationSec: Int = 18) {
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                content = videoTitle,
                type = MessageType.VIDEO,
                mediaUri = "video_family_clip",
                durationSeconds = durationSec
            )
        }
    }

    fun sendLocationMessage(chatId: String, locationTitle: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                content = locationTitle,
                type = MessageType.LOCATION,
                latitude = lat,
                longitude = lng,
                locationName = locationTitle
            )
        }
    }

    // Voice Note Recording
    fun startVoiceRecording() {
        isRecordingVoice.value = true
        recordingDurationSeconds.value = 0
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (isRecordingVoice.value) {
                delay(1000)
                recordingDurationSeconds.value += 1
            }
        }
    }

    fun cancelVoiceRecording() {
        isRecordingVoice.value = false
        recordingJob?.cancel()
        recordingDurationSeconds.value = 0
    }

    fun stopAndSendVoiceRecording(chatId: String) {
        val duration = recordingDurationSeconds.value.coerceAtLeast(1)
        isRecordingVoice.value = false
        recordingJob?.cancel()
        recordingDurationSeconds.value = 0

        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                content = "Voice message (${duration}s)",
                type = MessageType.AUDIO,
                mediaUri = "audio_voice_note",
                durationSeconds = duration
            )
        }
    }

    // Stories Feature
    fun postStory(caption: String, expiryHours: Int) {
        viewModelScope.launch {
            repository.postStory(
                caption = caption,
                mediaUri = "story_custom_moment",
                expiryHours = expiryHours
            )
        }
    }

    fun openStoryViewer(index: Int) {
        activeStoryIndex.value = index
        currentRoute.value = "STORY_VIEWER"
        viewModelScope.launch {
            val list = stories.value
            if (index in list.indices) {
                repository.markStorySeen(list[index].id)
            }
        }
    }

    fun closeStoryViewer() {
        activeStoryIndex.value = null
        currentRoute.value = "CHATS"
    }

    // Calling System
    fun startCall(peer: UserEntity, isVideo: Boolean) {
        val code = E2EEEngine.generateSafetyNumber(currentUser.value?.id ?: "me", peer.id)
        _activeCall.value = ActiveCallState(
            peerId = peer.id,
            peerName = peer.name,
            peerRole = peer.role,
            peerAvatarColor = peer.avatarColor,
            isVideo = isVideo,
            isIncoming = false,
            isConnected = false,
            e2eeVerificationCode = "AES-256-GCM • $code",
            latencyMs = if (isDataSaverEnabled.value) 42 else 24,
            dataRateKbps = if (isDataSaverEnabled.value) 180 else 720
        )
        currentRoute.value = "CALL_SCREEN"

        // Simulate peer picking up after 2.5 seconds
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            delay(2500)
            _activeCall.value = _activeCall.value?.copy(isConnected = true)
            while (_activeCall.value?.isConnected == true) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }
    }

    fun triggerIncomingCallSimulation(caller: UserEntity, isVideo: Boolean) {
        val code = E2EEEngine.generateSafetyNumber(currentUser.value?.id ?: "me", caller.id)
        _activeCall.value = ActiveCallState(
            peerId = caller.id,
            peerName = caller.name,
            peerRole = caller.role,
            peerAvatarColor = caller.avatarColor,
            isVideo = isVideo,
            isIncoming = true,
            isConnected = false,
            e2eeVerificationCode = "AES-256-GCM • $code"
        )
    }

    fun acceptIncomingCall() {
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(isConnected = true, isIncoming = false)
        currentRoute.value = "CALL_SCREEN"

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_activeCall.value?.isConnected == true) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let { it.copy(durationSeconds = it.durationSeconds + 1) }
            }
        }
    }

    fun declineOrEndCall() {
        val call = _activeCall.value
        if (call != null) {
            viewModelScope.launch {
                repository.logCall(
                    peerId = call.peerId,
                    peerName = call.peerName,
                    peerRole = call.peerRole,
                    isVideo = call.isVideo,
                    isIncoming = call.isIncoming,
                    isMissed = call.isIncoming && !call.isConnected,
                    durationSeconds = call.durationSeconds
                )
            }
        }
        callTimerJob?.cancel()
        _activeCall.value = null
        if (currentRoute.value == "CALL_SCREEN") {
            currentRoute.value = "CHATS"
        }
    }

    fun toggleCallMute() {
        _activeCall.value = _activeCall.value?.let { it.copy(isMuted = !it.isMuted) }
    }

    fun toggleCallSpeaker() {
        _activeCall.value = _activeCall.value?.let { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun toggleCallCamera() {
        _activeCall.value = _activeCall.value?.let { it.copy(isCameraOn = !it.isCameraOn) }
    }

    fun flipCallCamera() {
        _activeCall.value = _activeCall.value?.let { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    // Profile & Family Account Switch
    fun switchUserAccount(userId: String) {
        viewModelScope.launch {
            repository.switchCurrentUser(userId)
            activeChatId.value = null
            currentRoute.value = "CHATS"
        }
    }

    fun updateProfile(name: String, role: String, status: String) {
        viewModelScope.launch {
            repository.updateUserProfile(name, role, status)
        }
    }
}
