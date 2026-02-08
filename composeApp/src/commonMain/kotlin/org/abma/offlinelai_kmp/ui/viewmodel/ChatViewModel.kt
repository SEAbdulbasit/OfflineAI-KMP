package org.abma.offlinelai_kmp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.abma.offlinelai_kmp.domain.model.Attachment
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.domain.repository.ModelRepository
import org.abma.offlinelai_kmp.domain.usecase.*
import org.abma.offlinelai_kmp.inference.GemmaInference
import org.abma.offlinelai_kmp.tools.ToolCall
import org.abma.offlinelai_kmp.tools.ToolRegistry
import org.abma.offlinelai_kmp.tools.buildSystemPrompt
import org.abma.offlinelai_kmp.tools.createDefaultToolRegistry

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val modelState: ModelState = ModelState.NOT_LOADED,
    val loadingProgress: Float = 0f,
    val currentInput: String = "",
    val errorMessage: String? = null,
    val currentModelPath: String? = null,
    val loadedModels: List<LoadedModel> = emptyList(),
    val pendingAttachments: List<Attachment> = emptyList(),
    val isAttachmentLoading: Boolean = false,
    val isToolCallInProgress: Boolean = false
)

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val gemmaInference = GemmaInference()
    private val modelRepository = ModelRepository()
    private val toolRegistry: ToolRegistry = createDefaultToolRegistry()
    private var streamingMessageId: String? = null

    private val loadModelUseCase = LoadModelUseCase(gemmaInference, modelRepository)
    private val generateResponseUseCase = GenerateResponseUseCase(gemmaInference)
    private val executeToolUseCase = ExecuteToolUseCase(gemmaInference, toolRegistry)
    private val getLoadedModelsUseCase = GetLoadedModelsUseCase(modelRepository)
    private val removeModelUseCase = RemoveModelUseCase(modelRepository)
    private val buildPromptWithAttachmentsUseCase = BuildPromptWithAttachmentsUseCase()

    private val systemPrompt: String by lazy {
        buildSystemPrompt(toolRegistry.specs())
    }

    init {
        refreshLoadedModels()
    }

    fun refreshLoadedModels() {
        getLoadedModelsUseCase().onSuccess { models ->
            val currentPath = modelRepository.getCurrentModelPath()
            _uiState.update {
                it.copy(
                    loadedModels = models,
                    currentModelPath = currentPath
                )
            }
        }
    }

    fun loadModel(modelPath: String, config: ModelConfig = ModelConfig()) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(modelState = ModelState.LOADING, errorMessage = null) }

            loadModelUseCase(modelPath, config)
                .onSuccess { loadedModel ->
                    _uiState.update {
                        it.copy(
                            modelState = ModelState.READY,
                            loadingProgress = 1f,
                            currentModelPath = loadedModel.path,
                            loadedModels = modelRepository.getLoadedModels()
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            modelState = ModelState.ERROR,
                            errorMessage = exception.message ?: "Failed to load model",
                            loadingProgress = 0f
                        )
                    }
                }
        }
    }

    fun removeLoadedModel(path: String) {
        removeModelUseCase(path)
        refreshLoadedModels()
    }

    fun updateInput(input: String) {
        _uiState.update { it.copy(currentInput = input) }
    }

    fun sendMessage() {
        val input = _uiState.value.currentInput.trim()
        val attachments = _uiState.value.pendingAttachments

        if (input.isEmpty() && attachments.isEmpty()) return
        if (_uiState.value.modelState != ModelState.READY) return

        val userMessage = ChatMessage.user(input, attachments)

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                currentInput = "",
                pendingAttachments = emptyList(),
                modelState = ModelState.GENERATING
            )
        }

        val promptWithAttachments = buildPromptWithAttachmentsUseCase(input, attachments)
        generateResponse(promptWithAttachments)
    }

    fun addAttachment(attachment: Attachment) {
        _uiState.update { state ->
            state.copy(pendingAttachments = state.pendingAttachments + attachment)
        }
    }

    fun removeAttachment(attachmentId: String) {
        _uiState.update { state ->
            state.copy(pendingAttachments = state.pendingAttachments.filter { it.id != attachmentId })
        }
    }

    fun clearAttachments() {
        _uiState.update { state ->
            state.copy(pendingAttachments = emptyList())
        }
    }

    fun setAttachmentLoading(loading: Boolean) {
        _uiState.update { state ->
            state.copy(isAttachmentLoading = loading)
        }
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
            _uiState.update { it.copy(isToolCallInProgress = true) }

            executeToolUseCase(
                toolCall = toolCall,
                loadedModels = _uiState.value.loadedModels,
                currentModelPath = _uiState.value.currentModelPath
            ).collect { result ->
                when (result) {
                    is ExecuteToolResult.Executing -> {
                        updateStreamingMessage("🔧 Calling ${result.toolName}...\n\n")
                    }
                    is ExecuteToolResult.Streaming -> {
                        updateStreamingMessage(result.toolDisplay + result.partialResponse)
                    }
                    is ExecuteToolResult.Complete -> {
                        updateStreamingMessage(result.toolDisplay + result.response)
                        finishToolCall()
                    }
                    is ExecuteToolResult.Error -> {
                        handleToolError(toolCall.tool, result.exception)
                    }
                }
            }
        }
    }

    private fun updateStreamingMessage(content: String) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) msg.copy(content = content)
                else msg
            }
            state.copy(messages = updatedMessages)
        }
    }

    private fun finishStreaming() {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) msg.copy(isStreaming = false)
                else msg
            }
            state.copy(
                messages = updatedMessages,
                modelState = ModelState.READY
            )
        }
        streamingMessageId = null
    }

    private fun finishToolCall() {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) msg.copy(isStreaming = false)
                else msg
            }
            state.copy(
                messages = updatedMessages,
                modelState = ModelState.READY,
                isToolCallInProgress = false
            )
        }
        streamingMessageId = null
    }

    private fun handleError(exception: Exception) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) {
                    msg.copy(
                        content = "Error: ${exception.message}",
                        isStreaming = false,
                        isError = true
                    )
                } else msg
            }
            state.copy(
                messages = updatedMessages,
                modelState = ModelState.READY
            )
        }
        streamingMessageId = null
    }

    private fun handleToolError(toolName: String, exception: Exception) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == streamingMessageId) {
                    msg.copy(
                        content = "❌ Error executing $toolName: ${exception.message}",
                        isStreaming = false,
                        isError = true
                    )
                } else msg
            }
            state.copy(
                messages = updatedMessages,
                modelState = ModelState.READY,
                isToolCallInProgress = false
            )
        }
        streamingMessageId = null
    }

    fun clearChat() {
        _uiState.update { it.copy(messages = emptyList()) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        gemmaInference.close()
    }
}
