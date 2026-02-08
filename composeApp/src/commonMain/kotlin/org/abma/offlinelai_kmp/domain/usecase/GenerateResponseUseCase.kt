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
        var fullResponse = ""

        gemmaInference.generateResponseWithHistory(systemPrompt, formattedPrompt)
            .collect { token ->
                fullResponse += token
                emit(GenerateResponseResult.Streaming(fullResponse))
            }

        val toolCall = extractToolCall(fullResponse)
        emit(GenerateResponseResult.Complete(fullResponse, toolCall))
    }.catch { e ->
        emit(GenerateResponseResult.Error(e as? Exception ?: Exception(e)))
    }
}
