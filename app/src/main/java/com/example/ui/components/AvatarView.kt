package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.CharacterProfile
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.SuccessGreen

@Composable
fun AvatarView(
    profile: CharacterProfile,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showOnlineIndicator: Boolean = true,
    isSpeaking: Boolean = false
) {
    val decodedBitmap = remember(profile.customAvatarBase64) {
        if (!profile.customAvatarBase64.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(profile.customAvatarBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(
                    width = if (isSpeaking) 2.5.dp else 1.5.dp,
                    color = if (isSpeaking) NeonPurpleLight else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            when {
                decodedBitmap != null -> {
                    Image(
                        bitmap = decodedBitmap,
                        contentDescription = "Avatar de ${profile.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(size)
                    )
                }
                profile.avatarDrawableRes != 0 -> {
                    Image(
                        painter = painterResource(id = profile.avatarDrawableRes),
                        contentDescription = "Avatar de ${profile.name}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(size)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar por defecto",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(size * 0.6f)
                    )
                }
            }
        }

        if (showOnlineIndicator) {
            val indicatorSize = (size * 0.25f).coerceIn(10.dp, 16.dp)
            Box(
                modifier = Modifier
                    .size(indicatorSize)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(SuccessGreen)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}
