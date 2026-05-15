package org.abma.offlinelai_kmp.domain.model

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: ChatMessage - The Core Data Model
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Each message in the chat is represented by this data class.
 * Used for both user messages and AI responses.
 *
 * KEY PROPERTIES:
 * - id: Unique identifier for list diffing in Compose
 * - content: The actual message text
 * - isFromUser: true = user message, false = AI message
 * - isStreaming: true while AI is generating (shows typing indicator)
 * - isError: true if generation failed
 *
 * WORKSHOP: Why `isStreaming`?
 * During token streaming, we create a message with isStreaming=true.
 * The UI shows a typing indicator. As tokens arrive, we update content.
 * When done, we set isStreaming=false.
 *
 * This pattern gives smooth UX without creating new messages constantly.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
@OptIn(ExperimentalTime::class)
data class ChatMessage(
    /** Unique ID for identifying this message. Used by Compose LazyColumn for efficient diffing. */
    val id: String = generateId(),

    /** The message text content. For streaming messages, this is updated incrementally. */
    val content: String,

    /** True if this message is from the user, false if from AI. */
    val isFromUser: Boolean,

    /** Timestamp when the message was created. */
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),

    /**
     * True while AI is actively generating this message.
     * UI can show typing indicator when true.
     * Set to false when generation completes.
     */
    val isStreaming: Boolean = false,

    /** True if this message represents an error (generation failed). */
    val isError: Boolean = false
) {
    companion object {
        /**
         * Create a user message.
         *
         * Usage in ViewModel:
         * ```kotlin
         * val userMsg = ChatMessage.user("Hello!")
         * _uiState.update { it.copy(messages = it.messages + userMsg) }
         * ```
         */
        fun user(content: String) =
            ChatMessage(content = content, isFromUser = true)

        /**
         * Create an AI message.
         *
         * WORKSHOP: Streaming Pattern
         * ```kotlin
         * // 1. Create placeholder with isStreaming=true
         * val aiMsg = ChatMessage.ai("", isStreaming = true)
         * streamingId = aiMsg.id
         *
         * // 2. As tokens arrive, update content
         * messages.map { if (it.id == streamingId) it.copy(content = newContent) else it }
         *
         * // 3. When done, set isStreaming=false
         * messages.map { if (it.id == streamingId) it.copy(isStreaming = false) else it }
         * ```
         */
        fun ai(content: String, isStreaming: Boolean = false) =
            ChatMessage(content = content, isFromUser = false, isStreaming = isStreaming)

        /**
         * Create an error message.
         */
        fun error(message: String) =
            ChatMessage(content = message, isFromUser = false, isError = true)

        /**
         * Generate unique ID using timestamp + random.
         * Good enough for our use case, no external dependencies.
         */
        @OptIn(ExperimentalTime::class)
        private fun generateId() = "${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(10000)}"
    }
}
