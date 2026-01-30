package org.abma.offlinelai_kmp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.abma.offlinelai_kmp.ui.theme.GradientEnd
import org.abma.offlinelai_kmp.ui.theme.GradientStart

enum class EmptyStateType {
    NO_MODEL,
    NO_MESSAGES,
    ERROR,
    LOADING
}

@Composable
fun EmptyStateView(
    type: EmptyStateType,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val icon: ImageVector = when (type) {
        EmptyStateType.NO_MODEL -> Icons.Default.CloudOff
        EmptyStateType.NO_MESSAGES -> Icons.AutoMirrored.Filled.Chat
        EmptyStateType.ERROR -> Icons.Default.Error
        EmptyStateType.LOADING -> Icons.Default.Settings
    }

    val iconBackgroundColor = when (type) {
        EmptyStateType.ERROR -> MaterialTheme.colorScheme.errorContainer
        else -> null // Will use gradient
    }

    val iconTint = when (type) {
        EmptyStateType.ERROR -> MaterialTheme.colorScheme.error
        else -> Color.White
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .then(
                    if (iconBackgroundColor != null) {
                        Modifier.background(iconBackgroundColor)
                    } else {
                        Modifier.background(
                            Brush.linearGradient(
                                listOf(
                                    GradientStart.copy(alpha = 0.2f),
                                    GradientEnd.copy(alpha = 0.2f)
                                )
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = if (type == EmptyStateType.ERROR) {
                    iconTint
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = actionLabel,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
