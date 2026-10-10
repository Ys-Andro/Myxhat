package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.ui.PersonaViewModel
import com.example.ui.screens.CallScreen
import com.example.ui.screens.CharacterProfileDialog
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.FullscreenImageDialog
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PersonaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                PersonaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PersonaApp(viewModel: PersonaViewModel) {
    val isOnboarded by viewModel.isOnboarded.collectAsState()
    val characterProfile by viewModel.characterProfile.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val isCallActive by viewModel.isCallActive.collectAsState()
    val callTranscript by viewModel.callTranscript.collectAsState()
    val lastAiVoiceReply by viewModel.lastAiVoiceReply.collectAsState()
    val showProfileDialog by viewModel.showProfileDialog.collectAsState()
    val fullscreenImage by viewModel.fullscreenImage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Permission launcher for Microphone (RECORD_AUDIO)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startCall()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Se requiere permiso de micrófono para la llamada de voz.")
            }
        }
    }

    val voiceInputLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.voiceManager.startListening(
                onResult = { text -> viewModel.sendMessage(text, isFromVoice = true) },
                onError = { err ->
                    scope.launch { snackbarHostState.showSnackbar(err) }
                }
            )
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Se requiere permiso de micrófono para hablar.")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                !isOnboarded -> {
                    OnboardingScreen(
                        onCharacterCreated = { profile ->
                            viewModel.completeOnboarding(profile)
                        },
                        onTestVoice = { tone, text ->
                            viewModel.voiceManager.setVoiceTone(tone)
                            viewModel.voiceManager.speak(text)
                        }
                    )
                }
                isCallActive -> {
                    CallScreen(
                        profile = characterProfile,
                        voiceManager = viewModel.voiceManager,
                        callTranscript = callTranscript,
                        lastAiVoiceReply = lastAiVoiceReply,
                        onEndCall = { viewModel.endCall() },
                        onVoiceInputReceived = { recognizedText ->
                            viewModel.handleVoiceInput(recognizedText)
                        },
                        onError = { error ->
                            scope.launch { snackbarHostState.showSnackbar(error) }
                        }
                    )
                }
                else -> {
                    ChatScreen(
                        profile = characterProfile,
                        messages = messages,
                        isTyping = isTyping,
                        isApiKeyConfigured = viewModel.isApiKeyAvailable,
                        onSendMessage = { text -> viewModel.sendMessage(text) },
                        onRequestPhoto = { viewModel.requestSelfie() },
                        onStartCall = {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        onOpenProfile = { viewModel.openProfileDialog() },
                        onSpeakMessage = { text -> viewModel.speakMessage(text) },
                        onClearChat = { viewModel.clearChat() },
                        onImageClick = { msg -> viewModel.openFullscreenImage(msg) },
                        onMicClick = {
                            voiceInputLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    )
                }
            }
        }

        // Edit Profile Dialog
        if (showProfileDialog) {
            CharacterProfileDialog(
                currentProfile = characterProfile,
                onSaveProfile = { updated ->
                    viewModel.updateProfile(updated)
                },
                onDismiss = { viewModel.closeProfileDialog() }
            )
        }

        // Fullscreen Photo Dialog
        fullscreenImage?.let { msg ->
            FullscreenImageDialog(
                message = msg,
                onDismiss = { viewModel.closeFullscreenImage() }
            )
        }
    }
}
