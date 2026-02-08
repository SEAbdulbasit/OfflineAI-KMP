package org.abma.offlinelai_kmp.ui.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.domain.usecase.ExecuteToolResult
import org.abma.offlinelai_kmp.domain.usecase.ExecuteToolUseCase
import org.abma.offlinelai_kmp.domain.usecase.GenerateResponseResult
import org.abma.offlinelai_kmp.domain.usecase.GenerateResponseUseCase
import org.abma.offlinelai_kmp.tools.ToolCall
import org.abma.offlinelai_kmp.tools.ToolRegistry
import org.abma.offlinelai_kmp.tools.buildSystemPrompt
import org.abma.offlinelai_kmp.tools.createDefaultToolRegistry

class ActionsViewModel : BaseConversationViewModel() {

    private val toolRegistry: ToolRegistry = createDefaultToolRegistry()
    private val generateResponseUseCase = GenerateResponseUseCase(gemmaInference)
    private val executeToolUseCase = ExecuteToolUseCase(gemmaInference, toolRegistry)

    override val systemPrompt: String by lazy {
        buildSystemPrompt(toolRegistry.specs())
    }

    override fun sendMessage() {
        val input = _uiState.value.currentInput.trim()
        if (input.isEmpty()) return
        if (_uiState.value.modelState != ModelState.READY) return

        val userMessage = ChatMessage.user(input)

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                currentInput = "",
                modelState = ModelState.GENERATING
            )
        }

        generateResponse(input)
    }

    private fun generateResponse(prompt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val aiMessage = ChatMessage.ai("", isStreaming = true)
            streamingMessageId = aiMessage.id
            _uiState.update { state ->
                state.copy(messages = state.messages + aiMessage)
            }

            generateResponseUseCase(systemPrompt, prompt)
                .collect { result ->
                    when (result) {
                        is GenerateResponseResult.Streaming -> {
                            updateStreamingMessage(result.partialResponse)
                        }
                        is GenerateResponseResult.Complete -> {
                            if (result.toolCall != null) {
                                handleToolCall(result.toolCall)
                            } else {
                                finishStreaming()
                            }
                        }
                        is GenerateResponseResult.Error -> {
                            handleError(result.exception)
                        }
                    }
                }
        }
    }

    private fun handleToolCall(toolCall: ToolCall) {
        viewModelScope.launch(Dispatchers.IO) {
            executeToolUseCase(
                toolCall = toolCall,
                loadedModels = _uiState.value.loadedModels,
                currentModelPath = _uiState.value.currentModelPath
            ).collect { result ->
                when (result) {
                    is ExecuteToolResult.Executing -> {
                        updateStreamingMessage("🔧 Executing ${result.toolName}...\n\n")
                    }
                    is ExecuteToolResult.Streaming -> {
                        updateStreamingMessage(result.toolDisplay + result.partialResponse)
                    }
                    is ExecuteToolResult.Complete -> {
                        updateStreamingMessage(result.toolDisplay + result.response)
                        finishStreaming()
                    }
                    is ExecuteToolResult.Error -> {
                        handleToolError(toolCall.tool, result.exception)
                    }
                }
            }
        }
    }

    private fun handleToolError(toolName: String, exception: Exception) {
        val messageId = streamingMessageId ?: return
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(
                            content = "❌ Tool '$toolName' failed: ${exception.message}",
                            isStreaming = false,
                            isError = true
                        )
                    } else msg
                },
                modelState = ModelState.READY,
                errorMessage = "Tool execution failed: ${exception.message}"
            )
        }
        streamingMessageId = null
    }
}
