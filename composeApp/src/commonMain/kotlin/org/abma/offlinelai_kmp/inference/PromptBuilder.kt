package org.abma.offlinelai_kmp.inference

import org.abma.offlinelai_kmp.domain.model.ChatMessage

/**
 * Builds formatted prompts for Gemma with conversation history.
 *
 * Workshop: This class demonstrates proper prompt construction for multi-turn
 * conversations with Gemma. Key responsibilities:
 *
 * 1. Format messages with turn tokens (<start_of_turn>, <end_of_turn>)
 * 2. Include conversation history for context
 * 3. Handle system prompts for persona customization
 * 4. Trim history to fit within context window limits
 */
object PromptBuilder {

    private const val MAX_CONTEXT_TOKENS = 7500  // Leave ~700 tokens for response
    private const val TURN_START = "<start_of_turn>"
    private const val TURN_END = "<end_of_turn>"

    /**
     * Build a complete prompt with conversation history.
     *
     * The resulting prompt structure:
     * ```
     * <start_of_turn>user
     * System Instructions: [system prompt]<end_of_turn>
     * <start_of_turn>model
     * I understand. I'll follow these instructions.<end_of_turn>
     * <start_of_turn>user
     * [message 1]<end_of_turn>
     * <start_of_turn>model
     * [response 1]<end_of_turn>
     * ... more history ...
     * <start_of_turn>user
     * [current message]<end_of_turn>
     * <start_of_turn>model
     * ```
     *
     * @param systemPrompt Optional system instructions for persona/behavior
     * @param messages Conversation history (may be trimmed if too long)
     * @param currentMessage The new user message to respond to
     * @return Formatted prompt string ready for inference
     */
    fun buildPrompt(
        systemPrompt: String? = null,
        messages: List<ChatMessage>,
        currentMessage: String
    ): String = buildString {
        // 1. System prompt (if provided)
        if (!systemPrompt.isNullOrBlank()) {
            append("${TURN_START}user\n")
            append("System Instructions: $systemPrompt$TURN_END\n")
            append("${TURN_START}model\n")
            append("I understand. I'll follow these instructions.$TURN_END\n")
        }

        // 2. Conversation history (trimmed to fit context)
        val trimmedMessages = trimToContextLimit(messages)
        for (message in trimmedMessages) {
            val role = if (message.isFromUser) "user" else "model"
            append("$TURN_START$role\n")
            append("${message.content}$TURN_END\n")
        }

        // 3. Current user message
        append("${TURN_START}user\n")
        append("$currentMessage$TURN_END\n")

        // 4. Start model's turn (model generates from here)
        append("${TURN_START}model\n")
    }

    /**
     * Trim messages to fit within context window.
     *
     * Strategy: Keep most recent messages, drop oldest first.
     * This ensures the model has the latest context for relevant responses.
     */
    private fun trimToContextLimit(messages: List<ChatMessage>): List<ChatMessage> {
        var totalTokens = 0
        val result = mutableListOf<ChatMessage>()

        // Process from newest to oldest
        for (message in messages.reversed()) {
            val messageTokens = estimateTokens(message.content) + 20  // +20 for turn tokens

            if (totalTokens + messageTokens > MAX_CONTEXT_TOKENS) {
                println("⚠️ Context trimming: Dropping ${messages.size - result.size} old messages")
                break  // Stop adding messages
            }

            totalTokens += messageTokens
            result.add(0, message)  // Add to front to maintain chronological order
        }

        return result
    }

    /**
     * Estimate total tokens in a prompt.
     *
     * Useful for UI display (show context usage) and debugging.
     */
    fun estimatePromptTokens(
        systemPrompt: String?,
        messages: List<ChatMessage>,
        currentMessage: String
    ): Int {
        var total = 0

        // System prompt + wrapper
        if (!systemPrompt.isNullOrBlank()) {
            total += estimateTokens(systemPrompt) + 50
        }

        // All messages + their turn tokens
        total += messages.sumOf { estimateTokens(it.content) + 20 }

        // Current message + turn tokens
        total += estimateTokens(currentMessage) + 20

        return total
    }

    /**
     * Rough token estimation: ~4 characters per token for English.
     *
     * This is an approximation. Actual tokenization varies by:
     * - Language (non-English often uses more tokens)
     * - Special characters and numbers
     * - Code vs prose
     *
     * For accurate counts, you'd need the actual tokenizer.
     */
    fun estimateTokens(text: String): Int = (text.length / 4) + 1

    /**
     * Check if adding a message would exceed context limits.
     */
    fun wouldExceedContext(
        currentPromptTokens: Int,
        newMessageTokens: Int,
        maxTokens: Int = MAX_CONTEXT_TOKENS
    ): Boolean = currentPromptTokens + newMessageTokens > maxTokens

    /**
     * Get context usage as a percentage (0.0 to 1.0).
     */
    fun getContextUsage(
        systemPrompt: String?,
        messages: List<ChatMessage>,
        currentMessage: String
    ): Float {
        val tokens = estimatePromptTokens(systemPrompt, messages, currentMessage)
        return (tokens.toFloat() / MAX_CONTEXT_TOKENS).coerceIn(0f, 1f)
    }
}

/**
 * Different strategies for trimming conversation history.
 */
enum class TrimmingStrategy {
    /** Keep most recent messages, drop oldest. Simple and effective. */
    KEEP_RECENT,

    /** Keep first exchange + recent. Maintains initial context/instructions. */
    KEEP_FIRST_AND_RECENT,

    /** Summarize old messages. Most sophisticated but requires extra inference. */
    SUMMARIZE_OLD
}

/**
 * Advanced context trimmer with multiple strategies.
 */
object ContextTrimmer {

    fun trim(
        messages: List<ChatMessage>,
        maxTokens: Int,
        strategy: TrimmingStrategy
    ): List<ChatMessage> {
        return when (strategy) {
            TrimmingStrategy.KEEP_RECENT -> keepRecent(messages, maxTokens)
            TrimmingStrategy.KEEP_FIRST_AND_RECENT -> keepFirstAndRecent(messages, maxTokens)
            TrimmingStrategy.SUMMARIZE_OLD -> keepRecent(messages, maxTokens) // Simplified
        }
    }

    private fun keepRecent(messages: List<ChatMessage>, maxTokens: Int): List<ChatMessage> {
        var tokens = 0
        return messages.reversed().takeWhile { msg ->
            tokens += PromptBuilder.estimateTokens(msg.content) + 20
            tokens < maxTokens
        }.reversed()
    }

    private fun keepFirstAndRecent(messages: List<ChatMessage>, maxTokens: Int): List<ChatMessage> {
        if (messages.size <= 4) return messages

        // Always keep first 2 messages (establish context)
        val first = messages.take(2)
        val firstTokens = first.sumOf { PromptBuilder.estimateTokens(it.content) + 20 }

        // Fill remaining space with recent messages
        val remainingTokens = maxTokens - firstTokens
        val recent = keepRecent(messages.drop(2), remainingTokens)

        return first + recent
    }
}

// ============================================================================
// SYSTEM PROMPT TEMPLATES
// ============================================================================

/**
 * Default assistant persona - helpful and professional.
 */
val DEFAULT_SYSTEM_PROMPT = """
You are a helpful AI assistant running entirely on-device via Gemma.
You provide concise, accurate responses.
You're friendly but professional.
If you don't know something, admit it rather than making things up.
Keep responses focused and relevant to the user's question.
""".trimIndent()

/**
 * Coding assistant persona - Kotlin expert.
 */
val CODING_ASSISTANT_PROMPT = """
You are an expert Kotlin and Android developer assistant.
When providing code:
- Use Kotlin best practices and idiomatic patterns
- Include brief comments for complex logic
- Prefer coroutines for async operations
- Follow clean architecture principles
- Keep examples concise but complete
- Mention relevant libraries when appropriate
""".trimIndent()

/**
 * Creative writing assistant persona.
 */
val CREATIVE_WRITER_PROMPT = """
You are a creative writing assistant.
You help with stories, poems, and creative content.
You use vivid language and engaging narratives.
You match the user's preferred style and tone.
You offer suggestions while respecting the user's creative vision.
""".trimIndent()

/**
 * Concise assistant - minimal responses.
 */
val CONCISE_ASSISTANT_PROMPT = """
You are an extremely concise assistant.
Give the shortest possible accurate answer.
Use bullet points when listing multiple items.
Avoid unnecessary pleasantries or filler text.
If something can be said in fewer words, do so.
""".trimIndent()

