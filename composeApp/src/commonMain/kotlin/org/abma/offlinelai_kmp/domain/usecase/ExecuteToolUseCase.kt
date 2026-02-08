package org.abma.offlinelai_kmp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.inference.GemmaInference
import org.abma.offlinelai_kmp.tools.*

sealed class ExecuteToolResult {
    data class Executing(val toolName: String) : ExecuteToolResult()
    data class Streaming(val toolDisplay: String, val partialResponse: String) : ExecuteToolResult()
    data class Complete(val toolDisplay: String, val response: String) : ExecuteToolResult()
    data class Error(val exception: Exception) : ExecuteToolResult()
}

class ExecuteToolUseCase(
    private val gemmaInference: GemmaInference,
    private val toolRegistry: ToolRegistry
) {
    operator fun invoke(
        toolCall: ToolCall,
        messagesForContext: List<Pair<String, Boolean>>,
        loadedModels: List<LoadedModel>,
        currentModelPath: String?
    ): Flow<ExecuteToolResult> = flow {
        try {
            emit(ExecuteToolResult.Executing(toolCall.tool))

            val toolContext = ToolContext(
                loadedModels = loadedModels,
                currentModelPath = currentModelPath
            )

            val toolResult = toolRegistry.execute(toolCall, toolContext)
            val toolCallDisplay = "🔧 Calling ${toolCall.tool}...\n\n"

            val toolResultPrompt = buildToolResultPrompt(toolCall, toolResult)
            val followUpPrompt = formatPromptWithHistoryAndToolResult(
                messagesForContext,
                toolCall,
                toolResultPrompt
            )

            var naturalResponse = ""
            gemmaInference.generateResponse(followUpPrompt)
                .collect { token ->
                    naturalResponse += token
                    val displayResponse = stripToolCallBlock(naturalResponse)
                    emit(ExecuteToolResult.Streaming(toolCallDisplay, displayResponse))
                }

            val finalDisplayResponse = stripToolCallBlock(naturalResponse)
            emit(ExecuteToolResult.Complete(toolCallDisplay, finalDisplayResponse))
        } catch (e: Exception) {
            emit(ExecuteToolResult.Error(e))
        }
    }
}
