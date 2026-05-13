package org.abma.offlinelai_kmp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.abma.offlinelai_kmp.domain.model.ChatMessage

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isFromUser
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), contentAlignment = alignment) {
        Surface(
            color = if (message.isError) MaterialTheme.colorScheme.errorContainer else color,
            contentColor = if (message.isError) MaterialTheme.colorScheme.onErrorContainer else contentColor,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                if (!isUser) {
                    Text("Gemma", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    text = if (message.isStreaming && message.content.isEmpty()) "..." else message.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
