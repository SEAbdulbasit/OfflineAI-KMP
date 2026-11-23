package org.abma.offlinelai_kmp.domain.usecase

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.GenerationMetrics
import org.abma.offlinelai_kmp.inference.ContextTrimmer
import org.abma.offlinelai_kmp.inference.GemmaInference
import org.abma.offlinelai_kmp.inference.MetricsCollector
import org.abma.offlinelai_kmp.inference.PromptBuilder
import org.abma.offlinelai_kmp.inference.TrimmingStrategy
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
    data class Streaming(
        val partialResponse: String,
        val liveTokensPerSecond: Double = 0.0,
        val liveTokenCount: Int = 0
    ) : GenerateResponseResult()

    /** Emitted once when generation completes. May include detected tool call and metrics. */
    data class Complete(
        val response: String,
        val toolCall: ToolCall?,
        val metrics: GenerationMetrics? = null
    ) : GenerateResponseResult()

    /** Emitted if an error occurs during generation. */
    data class Error(val exception: Exception) : GenerateResponseResult()
}

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: GenerateResponseUseCase - The Prompt Builder & Streamer
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * This use case handles the CRITICAL task of prompt formatting and live telemetry tracking.
 */
class GenerateResponseUseCase(
    private val gemmaInference: GemmaInference
) {
    operator fun invoke(
        systemPrompt: String,
        messages: List<ChatMessage>,
        userPrompt: String,
        trimmingStrategy: TrimmingStrategy = TrimmingStrategy.KEEP_RECENT,
        maxContextTokens: Int = PromptBuilder.MAX_CONTEXT_TOKENS
    ): Flow<GenerateResponseResult> = flow {

        // ═══════════════════════════════════════════════════════════════════
        // STEP 1: Trim & Format conversation history within context budget
        // ═══════════════════════════════════════════════════════════════════
        val trimmedMessages = ContextTrimmer.trim(
            messages = messages,
            maxTokens = maxContextTokens,
            strategy = trimmingStrategy
        )
        val history = trimmedMessages.joinToString("") { message ->
            val role = if (message.isFromUser) "user" else "model"
            "<start_of_turn>$role\n${message.content}<end_of_turn>\n"
        }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 2: Build the complete prompt & start metrics collection
        // ═══════════════════════════════════════════════════════════════════
        val formattedPrompt = history +
            "<start_of_turn>user\n$userPrompt<end_of_turn>\n" +
            "<start_of_turn>model\n"

        val estimatedPromptTokens = PromptBuilder.estimatePromptTokens(
            systemPrompt = systemPrompt,
            messages = trimmedMessages,
            currentMessage = userPrompt
        )
        MetricsCollector.startGeneration(estimatedPromptTokens)

        // ═══════════════════════════════════════════════════════════════════
        // STEP 3: Stream tokens and accumulate
        // ═══════════════════════════════════════════════════════════════════
        var fullResponse = ""

        gemmaInference.generateResponseWithHistory(systemPrompt, formattedPrompt)
            .collect { token ->
                fullResponse += token
                MetricsCollector.onTokenGenerated(token)

                emit(
                    GenerateResponseResult.Streaming(
                        partialResponse = fullResponse,
                        liveTokensPerSecond = MetricsCollector.liveTokensPerSecond.value,
                        liveTokenCount = MetricsCollector.liveTokenCount.value
                    )
                )
            }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 4: Handle completion & finalize metrics
        // ═══════════════════════════════════════════════════════════════════
        val metrics = MetricsCollector.endGeneration()
        val toolCall = extractToolCall(fullResponse)

        emit(GenerateResponseResult.Complete(fullResponse, toolCall, metrics))

    }.catch { e ->
        MetricsCollector.cancelGeneration()
        if (e is CancellationException) throw e
        emit(GenerateResponseResult.Error(e as? Exception ?: Exception(e)))
    }
}
