package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EncryptedGreen
import com.example.ui.viewmodel.MessengerViewModel

@Composable
fun CallScreen(
    viewModel: MessengerViewModel,
    modifier: Modifier = Modifier
) {
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val isDataSaver by viewModel.isDataSaverEnabled.collectAsStateWithLifecycle()
    var showSafetyVerificationDialog by remember { mutableStateOf(false) }

    val call = activeCall ?: return

    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .statusBarsPadding()
            .testTag("call_screen")
    ) {
        // Main Viewport: Video Stream or Audio Avatar
        if (call.isVideo && call.isCameraOn) {
            // Simulated High-Quality Peer Video Stream
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF1E1B4B))
                        )
                    )
            ) {
                // Video stream atmosphere & peer badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Color(call.peerAvatarColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = call.peerName.take(2).uppercase(),
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${call.peerName} (${call.peerRole})",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = if (call.isConnected) "HD 1080p Video • E2EE" else "Connecting...",
                            color = EncryptedGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // PiP Window (Picture-in-Picture self camera preview)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 80.dp, end = 16.dp)
                        .size(width = 100.dp, height = 140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF334155))
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                        .clickable { viewModel.flipCallCamera() }
                        .testTag("pip_self_video")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = if (call.isFrontCamera) "Front" else "Rear",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        } else {
            // Audio-Only Call UI
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                // Pulsing audio waves around avatar
                Box(contentAlignment = Alignment.Center) {
                    if (call.isConnected) {
                        Box(
                            modifier = Modifier
                                .scale(waveScale)
                                .size(180.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Color(call.peerAvatarColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = call.peerName.take(2).uppercase(),
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = call.peerName,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = call.peerRole,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                val minutes = call.durationSeconds / 60
                val seconds = call.durationSeconds % 60
                Text(
                    text = if (call.isConnected) "%02d:%02d".format(minutes, seconds) else "Calling family member...",
                    color = if (call.isConnected) Color.White else Color.Gray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Top Status Header: E2EE Code & Performance
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // E2EE Badge
            Surface(
                onClick = { showSafetyVerificationDialog = true },
                shape = RoundedCornerShape(16.dp),
                color = EncryptedGreen.copy(alpha = 0.2f),
                modifier = Modifier.testTag("call_e2ee_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted Call",
                        tint = EncryptedGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "E2EE Secured • ${call.e2eeVerificationCode.take(16)}...",
                        color = EncryptedGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Low Data Usage & Performance stats
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Network Performance",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${call.latencyMs}ms • ${call.dataRateKbps} kbps • ${if (isDataSaver) "Data Saver: ON" else "High Quality"}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Bottom Call Controls Bar
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 8.dp)
            ) {
                // Mute / Unmute
                FilledIconButton(
                    onClick = { viewModel.toggleCallMute() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (call.isMuted) DangerRed else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("call_mute_button")
                ) {
                    Icon(
                        imageVector = if (call.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = Color.White
                    )
                }

                // Video toggle
                FilledIconButton(
                    onClick = { viewModel.toggleCallCamera() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (!call.isCameraOn) Color.Gray else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("call_camera_toggle")
                ) {
                    Icon(
                        imageVector = if (call.isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Camera",
                        tint = Color.White
                    )
                }

                // Camera Flip (Front/Back)
                FilledIconButton(
                    onClick = { viewModel.flipCallCamera() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("call_camera_flip")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Flip Camera",
                        tint = Color.White
                    )
                }

                // Speakerphone
                FilledIconButton(
                    onClick = { viewModel.toggleCallSpeaker() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (call.isSpeakerOn) MaterialTheme.colorScheme.primary else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .size(52.dp)
                        .testTag("call_speaker_button")
                ) {
                    Icon(
                        imageVector = if (call.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "Speaker",
                        tint = Color.White
                    )
                }

                // End Call (Big Red)
                FilledIconButton(
                    onClick = { viewModel.declineOrEndCall() },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = DangerRed
                    ),
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("end_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    // Safety Verification Dialog
    if (showSafetyVerificationDialog) {
        AlertDialog(
            onDismissRequest = { showSafetyVerificationDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = EncryptedGreen,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Call Encryption Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "This call with ${call.peerName} is protected with end-to-end encryption. DTLS-SRTP and AES-256-GCM cipher ensure zero interception.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text("SECURITY SAFETY CODE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = call.e2eeVerificationCode,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = EncryptedGreen,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSafetyVerificationDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
