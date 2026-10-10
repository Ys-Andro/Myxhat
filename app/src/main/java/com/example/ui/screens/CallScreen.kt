package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.CharacterProfile
import com.example.ui.components.AvatarView
import com.example.ui.theme.BotBubbleColor
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.SuccessGreen
import com.example.voice.VoiceManager
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    profile: CharacterProfile,
    voiceManager: VoiceManager,
    callTranscript: String,
    lastAiVoiceReply: String,
    onEndCall: () -> Unit,
    onVoiceInputReceived: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onEndCall()
    }

    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val isListening by voiceManager.isListening.collectAsState()
    val rmsAmplitude by voiceManager.rmsAmplitude.collectAsState()

    var callDurationSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDurationSeconds++
        }
    }

    val formattedDuration = remember(callDurationSeconds) {
        val minutes = callDurationSeconds / 60
        val seconds = callDurationSeconds % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "audio_ring")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DeepBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Name and Call State
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isListening -> NeonCyan
                                    isSpeaking -> NeonPurpleLight
                                    else -> SuccessGreen
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when {
                            isListening -> "Escuchándote con atención..."
                            isSpeaking -> "${profile.name} está hablando..."
                            else -> "Conectado • $formattedDuration"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            isListening -> NeonCyan
                            isSpeaking -> NeonPurpleLight
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Center: Big Pulsating Avatar with Sound Wave Aura
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Outer Pulse Ring 2
                val activeRingMultiplier = if (isListening) (1f + rmsAmplitude * 0.4f) else if (isSpeaking) pulseScale else 1f
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(activeRingMultiplier)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    (if (isListening) NeonCyan else NeonPurple).copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Outer Pulse Ring 1
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(if (isListening) 1f + rmsAmplitude * 0.2f else if (isSpeaking) (pulseScale * 0.95f) else 1f)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = (if (isListening) NeonCyan else NeonPurpleLight).copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                )

                // Core Avatar
                AvatarView(
                    profile = profile,
                    size = 140.dp,
                    showOnlineIndicator = false,
                    isSpeaking = isSpeaking || isListening
                )
            }

            // Subtitle / Live Transcript Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = BotBubbleColor.copy(alpha = 0.9f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurpleLight.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (callTranscript.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tú dijiste:",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "\"$callTranscript\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(start = 22.dp, top = 2.dp, bottom = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = NeonPurpleLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${profile.name}:",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonPurpleLight,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (lastAiVoiceReply.isNotBlank()) {
                            IconButton(
                                onClick = { voiceManager.speak(lastAiVoiceReply) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Repetir voz",
                                    tint = NeonPurpleLight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = lastAiVoiceReply.ifBlank { "Toca el micrófono abajo para comenzar a hablar..." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (lastAiVoiceReply.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 22.dp, top = 2.dp)
                    )
                }
            }

            // Bottom Call Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Switch back to Chat mode
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "Volver a chat de texto",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Microphone Button (Toggle / Push to speak)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(if (isListening) NeonCyan else NeonPurple)
                        .clickable {
                            if (isListening) {
                                voiceManager.stopListening()
                            } else {
                                voiceManager.startListening(
                                    onResult = { text -> onVoiceInputReceived(text) },
                                    onError = { err -> onError(err) }
                                )
                            }
                        }
                        .testTag("call_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.Mic,
                        contentDescription = if (isListening) "Detener escucha" else "Hablar",
                        tint = if (isListening) Color.Black else Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Hang Up Button
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(ErrorRed)
                        .testTag("hang_up_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Colgar llamada",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
