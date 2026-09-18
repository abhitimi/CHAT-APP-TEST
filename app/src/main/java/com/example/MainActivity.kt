package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.IncomingCallDialog
import com.example.ui.screens.CallScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.StoryViewerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MessengerViewModel
import com.example.ui.viewmodel.ThemeSetting

class MainActivity : ComponentActivity() {
    private val viewModel: MessengerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeSetting by viewModel.themeSetting.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()
            val useDarkTheme = when (themeSetting) {
                ThemeSetting.DARK -> true
                ThemeSetting.LIGHT -> false
                ThemeSetting.SYSTEM -> isSystemDark
            }

            MyApplicationTheme(darkTheme = useDarkTheme) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MessengerViewModel) {
    val currentRoute by viewModel.currentRoute.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val activeChatId by viewModel.activeChatId.collectAsStateWithLifecycle()
    val activeStoryIndex by viewModel.activeStoryIndex.collectAsStateWithLifecycle()
    val activeNotification by viewModel.activeNotification.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(activeNotification) {
        activeNotification?.let { notif ->
            snackbarHostState.showSnackbar("${notif.senderName}: ${notif.text}")
            viewModel.dismissNotification()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Screen Navigation
        when {
            currentRoute == "CALL_SCREEN" || (activeCall != null && activeCall?.isConnected == true) -> {
                CallScreen(viewModel = viewModel)
            }
            currentRoute == "CHAT_DETAIL" || activeChatId != null -> {
                ChatDetailScreen(viewModel = viewModel)
            }
            currentRoute == "STORY_VIEWER" || activeStoryIndex != null -> {
                StoryViewerScreen(viewModel = viewModel)
            }
            currentRoute == "PROFILE" -> {
                ProfileScreen(viewModel = viewModel)
            }
            else -> {
                HomeScreen(viewModel = viewModel)
            }
        }

        // Incoming Call Dialog Overlay
        if (activeCall?.isIncoming == true && activeCall?.isConnected == false) {
            IncomingCallDialog(
                callState = activeCall,
                onAccept = { viewModel.acceptIncomingCall() },
                onDecline = { viewModel.declineOrEndCall() },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

// Maintained for backward-compatible screenshot tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
