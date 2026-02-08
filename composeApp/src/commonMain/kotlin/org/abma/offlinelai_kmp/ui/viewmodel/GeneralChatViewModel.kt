package org.abma.offlinelai_kmp.ui.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.domain.usecase.GenerateResponseResult
import org.abma.offlinelai_kmp.domain.usecase.GenerateResponseUseCase
import org.abma.offlinelai_kmp.inference.GemmaInference

class GeneralChatViewModel : BaseConversationViewModel() {

    private val generateResponseUseCase = GenerateResponseUseCase(gemmaInference)

    override val systemPrompt: String = """
        You are a helpful, friendly AI assistant. 
        Provide clear, concise, and accurate responses.
        Be conversational and engaging while staying informative.
    """.trimIndent()

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
                            finishStreaming()
                        }
                        is GenerateResponseResult.Error -> {
                            handleError(result.exception)
                        }
                    }
                }
        }
    }
}
