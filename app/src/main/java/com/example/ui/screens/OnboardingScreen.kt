package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CharacterProfile
import com.example.model.ConversationStyle
import com.example.model.VisualStyle
import com.example.model.VoiceTone
import com.example.ui.components.AvatarView
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.WarmRose

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onCharacterCreated: (CharacterProfile) -> Unit,
    onTestVoice: (VoiceTone, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPresetIndex by remember { mutableStateOf(0) }
    val initialPreset = CharacterProfile.PRESETS.first()

    var name by remember { mutableStateOf(initialPreset.name) }
    var personality by remember { mutableStateOf(initialPreset.personality) }
    var appearance by remember { mutableStateOf(initialPreset.appearance) }
    var visualStyle by remember { mutableStateOf(initialPreset.visualStyle) }
    var voiceTone by remember { mutableStateOf(initialPreset.voiceTone) }
    var conversationStyle by remember { mutableStateOf(initialPreset.conversationStyle) }
    var avatarDrawableRes by remember { mutableStateOf(initialPreset.avatarDrawableRes) }

    fun applyPreset(index: Int) {
        val preset = CharacterProfile.PRESETS[index]
        selectedPresetIndex = index
        name = preset.name
        personality = preset.personality
        appearance = preset.appearance
        visualStyle = preset.visualStyle
        voiceTone = preset.voiceTone
        conversationStyle = preset.conversationStyle
        avatarDrawableRes = preset.avatarDrawableRes
    }

    val currentProfile = remember(name, personality, appearance, visualStyle, voiceTone, conversationStyle, avatarDrawableRes) {
        CharacterProfile(
            name = name.ifBlank { "Mi IA" },
            personality = personality,
            appearance = appearance,
            visualStyle = visualStyle,
            voiceTone = voiceTone,
            conversationStyle = conversationStyle,
            greetingMessage = "¡Hola! He estado esperando este momento para conocerte. ¿De qué te gustaría hablar hoy? ✨",
            avatarDrawableRes = avatarDrawableRes
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Header Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                NeonPurple.copy(alpha = 0.35f),
                                NeonCyan.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .border(1.dp, NeonPurpleLight.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CREADOR DE PERSONAJE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Diseña a tu Compañero IA",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Personaliza personalidad, aspecto, estilo y voz a tu gusto.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    AvatarView(
                        profile = currentProfile,
                        size = 76.dp,
                        showOnlineIndicator = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Presets Selector
            Text(
                text = "Plantillas rápidas:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CharacterProfile.PRESETS.forEachIndexed { index, preset ->
                    val isSelected = selectedPresetIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { applyPreset(index) },
                        label = { Text(preset.name) },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonPurple,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("preset_chip_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Character Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Personaje") },
                placeholder = { Text("Ej. Aria, Luna, Kaito, Elena...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("character_name_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurpleLight,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Personality Field
            OutlinedTextField(
                value = personality,
                onValueChange = { personality = it },
                label = { Text("Personalidad y Carácter") },
                placeholder = { Text("Ej. Cariñosa, curiosa, divertida y atenta") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth().testTag("personality_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurpleLight,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Physical Appearance Field
            OutlinedTextField(
                value = appearance,
                onValueChange = { appearance = it },
                label = { Text("Apariencia física / Vestimenta") },
                placeholder = { Text("Ej. Pelo violeta neón, ojos esmeralda, chaqueta futurista...") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth().testTag("appearance_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurpleLight,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Visual Style Selection
            Text(
                text = "Estilo Visual Ilustrado:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VisualStyle.values().forEach { style ->
                    val isSelected = visualStyle == style
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            visualStyle = style
                            avatarDrawableRes = when (style) {
                                VisualStyle.ANIME -> R.drawable.persona_anime
                                VisualStyle.REALISTIC -> R.drawable.persona_real
                                VisualStyle.CYBERPUNK -> R.drawable.ic_app_avatar
                                VisualStyle.FANTASY -> R.drawable.persona_anime
                            }
                        },
                        label = { Text(style.displayName) },
                        leadingIcon = {
                            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = MaterialTheme.colorScheme.scrim
                        ),
                        modifier = Modifier.testTag("visual_style_${style.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Voice Tone Selection
            Text(
                text = "Tono de Voz (Modo Llamada):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VoiceTone.values().forEach { tone ->
                    val isSelected = voiceTone == tone
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { voiceTone = tone }
                            .testTag("voice_tone_${tone.name.lowercase()}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) NeonPurple.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, NeonPurpleLight) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tone.displayName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tone.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    onTestVoice(tone, "¡Hola! Así suena mi tono de voz en modo llamada. ¿Te agrada?")
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Probar voz",
                                    tint = if (isSelected) NeonPurpleLight else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Conversation Style Selection
            Text(
                text = "Estilo de Conversación:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConversationStyle.values().forEach { style ->
                    val isSelected = conversationStyle == style
                    FilterChip(
                        selected = isSelected,
                        onClick = { conversationStyle = style },
                        label = { Text(style.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarmRose,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("conv_style_${style.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Finish Onboarding Button
            Button(
                onClick = { onCharacterCreated(currentProfile) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("create_character_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPurple
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Iniciar Conexión con ${currentProfile.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
