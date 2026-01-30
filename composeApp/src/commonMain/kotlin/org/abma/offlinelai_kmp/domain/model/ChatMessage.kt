package org.abma.offlinelai_kmp.domain.model

import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class ChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = currentTimeMillis(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false
) {
    companion object {
        fun userMessage(content: String): ChatMessage {
            return ChatMessage(
                id = generateId(),
                content = content,
                isFromUser = true
            )
        }

        fun aiMessage(content: String, isStreaming: Boolean = false): ChatMessage {
            return ChatMessage(
                id = generateId(),
                content = content,
                isFromUser = false,
                isStreaming = isStreaming
            )
        }

        fun errorMessage(error: String): ChatMessage {
            return ChatMessage(
                id = generateId(),
                content = error,
                isFromUser = false,
                isError = true
            )
        }

        private fun generateId(): String {
            return currentTimeMillis().toString() +
                   Random.nextInt(0, 9999).toString().padStart(4, '0')
        }
    }
}

// Platform-independent way to get current time
internal expect fun currentTimeMillis(): Long

