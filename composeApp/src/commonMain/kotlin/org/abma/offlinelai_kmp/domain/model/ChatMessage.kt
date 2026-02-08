package org.abma.offlinelai_kmp.domain.model

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class ChatMessage(
    val id: String = generateId(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false,
    val attachments: List<Attachment> = emptyList()
) {
    companion object {
        fun user(content: String, attachments: List<Attachment> = emptyList()) =
            ChatMessage(content = content, isFromUser = true, attachments = attachments)

        fun ai(content: String, isStreaming: Boolean = false) =
            ChatMessage(content = content, isFromUser = false, isStreaming = isStreaming)

        fun error(message: String) =
            ChatMessage(content = message, isFromUser = false, isError = true)

        @OptIn(ExperimentalTime::class)
        private fun generateId() = "${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(10000)}"
    }
}
