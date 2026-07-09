package org.abma.offlinelai_kmp.domain.usecase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.inference.GemmaInference
import org.abma.offlinelai_kmp.tools.ToolCall
import org.abma.offlinelai_kmp.tools.extractToolCall

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: Result Types for Generation
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Using sealed classes for type-safe result handling.
 * The ViewModel can use `when` to handle each case exhaustively.
 */
sealed class GenerateResponseResult {
    /** Emitted for each token during streaming. partialResponse is the CUMULATIVE text. */
    data class Streaming(val partialResponse: String) : GenerateResponseResult()

    /** Emitted once when generation completes. May include detected tool call. */
    data class Complete(val response: String, val toolCall: ToolCall?) : GenerateResponseResult()

    /** Emitted if an error occurs during generation. */
    data class Error(val exception: Exception) : GenerateResponseResult()
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: GenerateResponseUseCase - The Prompt Builder & Streamer
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * This use case handles the CRITICAL task of prompt formatting.
 *
 * KEY RESPONSIBILITIES:
 * 1. Format conversation history with turn tokens
 * 2. Combine system prompt + history + user message
 * 3. Stream tokens from GemmaInference
 * 4. Accumulate into complete response
 * 5. Detect tool calls (advanced feature)
 *
 * WHY A USE CASE?
 * - Clean Architecture separation
 * - Testable business logic
 * - Reusable across ViewModels
 * - Single Responsibility: just handles prompt building + streaming
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
class GenerateResponseUseCase(
    private val gemmaInference: GemmaInference
) {
    /**
     * Generate a streaming response from Gemma.
     *
     * WORKSHOP: Prompt Formatting Deep Dive
     *
     * Gemma expects this EXACT format:
     * ```
     * <start_of_turn>user
     * First message<end_of_turn>
     * <start_of_turn>model
     * First response<end_of_turn>
     * <start_of_turn>user
     * Second message<end_of_turn>
     * <start_of_turn>model
     * ← Model generates from here
     * ```
     *
     * WHAT THIS METHOD DOES:
     * 1. Convert message history to formatted string
     * 2. Append current user message
     * 3. End with "<start_of_turn>model\n" (model's turn to speak)
     * 4. Stream tokens back as Flow<GenerateResponseResult>
     *
     * @param systemPrompt AI persona/instructions (prepended to generation)
     * @param messages Previous conversation history
     * @param userPrompt The current user message
     * @return Flow emitting Streaming results, then Complete
     */
    operator fun invoke(
        systemPrompt: String,
        messages: List<ChatMessage>,
        userPrompt: String
    ): Flow<GenerateResponseResult> = flow {

        // ═══════════════════════════════════════════════════════════════════
        // STEP 1: Format conversation history
        // ═══════════════════════════════════════════════════════════════════
        // Convert each ChatMessage to the turn-token format Gemma expects
        val history = messages.joinToString("") { message ->
            val role = if (message.isFromUser) "user" else "model"
            "<start_of_turn>$role\n${message.content}<end_of_turn>\n"
        }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 2: Build the complete prompt
        // ═══════════════════════════════════════════════════════════════════
        // history + current message + model turn start
        val formattedPrompt = history +
            "<start_of_turn>user\n$userPrompt<end_of_turn>\n" +
            "<start_of_turn>model\n"

        // WORKSHOP DEBUG: Log the prompt (helpful during development)
        println("═══════════════════════════════════════════════════════════")
        println("📤 SENDING TO LLM:")
        println("───────────────────────────────────────────────────────────")
        println("System Prompt (${systemPrompt.length} chars):")
        println(systemPrompt)  // Full prompt, no truncation
        println("───────────────────────────────────────────────────────────")
        println("Formatted Prompt:")
        println(formattedPrompt)
        println("───────────────────────────────────────────────────────────")
        println("History Messages: ${messages.size}")
        println("═══════════════════════════════════════════════════════════")

        // ═══════════════════════════════════════════════════════════════════
        // STEP 3: Stream tokens and accumulate
        // ═══════════════════════════════════════════════════════════════════
        var fullResponse = ""

        // Collect from GemmaInference's Flow
        // Each emission is a NEW token(s), not cumulative
        gemmaInference.generateResponseWithHistory(systemPrompt, formattedPrompt)
            .collect { token ->
                // Accumulate tokens into full response
                fullResponse += token

                // Emit streaming result with CUMULATIVE text
                // UI will replace previous content, showing smooth streaming
                emit(GenerateResponseResult.Streaming(fullResponse))
            }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 4: Handle completion
        // ═══════════════════════════════════════════════════════════════════
        println("═══════════════════════════════════════════════════════════")
        println("📥 LLM RESPONSE:")
        println("───────────────────────────────────────────────────────────")
        println(fullResponse)
        println("═══════════════════════════════════════════════════════════")

        // Check for tool calls (advanced feature - function calling)
        val toolCall = extractToolCall(fullResponse)
        if (toolCall != null) {
            println("🔧 DETECTED TOOL CALL: ${toolCall.tool}")
            println("   Arguments: ${toolCall.arguments}")
        } else {
            println("💬 No tool call detected - regular response")
        }
        println("═══════════════════════════════════════════════════════════")

        // Emit final complete result
        emit(GenerateResponseResult.Complete(fullResponse, toolCall))

    }.catch { e ->
        // ═══════════════════════════════════════════════════════════════════
        // ERROR HANDLING
        // ═══════════════════════════════════════════════════════════════════
        // CancellationException must be re-thrown to allow proper coroutine
        // cancellation (e.g., when user stops generation). Without this,
        // the inner callbackFlow's awaitClose never runs, leaving the
        // inference engine stuck and unable to accept new requests.
        if (e is CancellationException) throw e
        // .catch() intercepts any other exception in the Flow
        // Allows graceful error handling rather than crashing
        emit(GenerateResponseResult.Error(e as? Exception ?: Exception(e)))
    }
}
