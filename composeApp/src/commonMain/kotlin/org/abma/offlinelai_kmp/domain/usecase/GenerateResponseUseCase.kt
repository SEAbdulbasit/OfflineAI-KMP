package org.abma.offlinelai_kmp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.abma.offlinelai_kmp.inference.GemmaInference
import org.abma.offlinelai_kmp.inference.formatPromptWithHistory
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
        userPrompt: String,
        conversationHistory: List<Pair<String, Boolean>>
    ): Flow<GenerateResponseResult> = flow {
        try {
            val formattedPrompt = formatPromptWithHistory(conversationHistory, userPrompt)

            var fullResponse = ""
            gemmaInference.generateResponseWithHistory(systemPrompt, formattedPrompt)
                .collect { token ->
                    fullResponse += token
                    emit(GenerateResponseResult.Streaming(fullResponse))
                }

            val toolCall = extractToolCall(fullResponse)
            emit(GenerateResponseResult.Complete(fullResponse, toolCall))
        } catch (e: Exception) {
            emit(GenerateResponseResult.Error(e))
        }
    }
}
