package org.abma.offlinelai_kmp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.abma.offlinelai_kmp.inference.GemmaInference
import org.abma.offlinelai_kmp.inference.formatPrompt
import org.abma.offlinelai_kmp.tools.ToolCall
import org.abma.offlinelai_kmp.tools.extractToolCall

sealed class GenerateResponseResult {
    data class Streaming(val partialResponse: String) : GenerateResponseResult()
    data class Complete(val response: String, val toolCall: ToolCall?) : GenerateResponseResult()
    data class Error(val exception: Exception) : GenerateResponseResult()
}

class GenerateResponseUseCase(
    private val gemmaInference: GemmaInference
) {
    operator fun invoke(
        systemPrompt: String,
        userPrompt: String
    ): Flow<GenerateResponseResult> = flow {
        val formattedPrompt = formatPrompt(userPrompt)

        println("═══════════════════════════════════════════════════════════")
        println("📤 SENDING TO LLM:")
        println("───────────────────────────────────────────────────────────")
        println("System Prompt (${systemPrompt.length} chars):")
        println(systemPrompt.take(500))
        if (systemPrompt.length > 500) println("... [truncated]")
        println("───────────────────────────────────────────────────────────")
        println("User Prompt: $userPrompt")
        println("───────────────────────────────────────────────────────────")
        println("Formatted Prompt: $formattedPrompt")
        println("═══════════════════════════════════════════════════════════")

        var fullResponse = ""

        gemmaInference.generateResponseWithHistory(systemPrompt, formattedPrompt)
            .collect { token ->
                fullResponse += token
                emit(GenerateResponseResult.Streaming(fullResponse))
            }

        println("═══════════════════════════════════════════════════════════")
        println("📥 LLM RESPONSE:")
        println("───────────────────────────────────────────────────────────")
        println(fullResponse)
        println("═══════════════════════════════════════════════════════════")

        val toolCall = extractToolCall(fullResponse)
        if (toolCall != null) {
            println("🔧 DETECTED TOOL CALL: ${toolCall.tool}")
            println("   Arguments: ${toolCall.arguments}")
        } else {
            println("💬 No tool call detected - regular response")
        }
        println("═══════════════════════════════════════════════════════════")

        emit(GenerateResponseResult.Complete(fullResponse, toolCall))
    }.catch { e ->
        emit(GenerateResponseResult.Error(e as? Exception ?: Exception(e)))
    }
}
