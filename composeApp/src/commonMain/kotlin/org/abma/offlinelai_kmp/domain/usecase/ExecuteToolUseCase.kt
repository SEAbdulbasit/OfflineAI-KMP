package org.abma.offlinelai_kmp.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.tools.*

sealed class ExecuteToolResult {
    data class Executing(val toolName: String) : ExecuteToolResult()
    data class Streaming(val toolDisplay: String, val partialResponse: String) : ExecuteToolResult()
    data class Complete(val toolDisplay: String, val response: String) : ExecuteToolResult()
    data class Error(val exception: Exception) : ExecuteToolResult()
}

class ExecuteToolUseCase(
    private val toolRegistry: ToolRegistry
) {
    operator fun invoke(
        toolCall: ToolCall,
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
            val toolCallDisplay = "🔧 ${toolCall.tool}\n\n"

            // Emit the tool result directly - no need for follow-up LLM call
            emit(ExecuteToolResult.Complete(toolCallDisplay, toolResult.result))
        } catch (e: Exception) {
            emit(ExecuteToolResult.Error(e))
        }
    }
}
