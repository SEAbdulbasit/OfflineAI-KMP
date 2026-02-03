package org.abma.offlinelai_kmp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.abma.offlinelai_kmp.domain.model.Attachment
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.domain.repository.ModelRepository
import org.abma.offlinelai_kmp.inference.GemmaInference
import org.abma.offlinelai_kmp.tools.*
import kotlin.time.Clock

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
    private val toolRegistry = createDefaultToolRegistry()
    private var streamingMessageId: String? = null

    init {
        // Load saved models on initialization
        refreshLoadedModels()
    }

    fun refreshLoadedModels() {
        val models = modelRepository.getLoadedModels()
        val currentPath = modelRepository.getCurrentModelPath()
        _uiState.update {
            it.copy(
                loadedModels = models,
                currentModelPath = currentPath
            )
        }
    }

    fun loadModel(modelPath: String, config: ModelConfig = ModelConfig()) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(modelState = ModelState.LOADING, errorMessage = null) }
            try {
                gemmaInference.loadModel(modelPath, config)

                // Save successfully loaded model
                val modelName = modelPath.substringAfterLast("/").substringBeforeLast(".")
                val currentTime = Clock.System.now().toEpochMilliseconds()
                val loadedModel = LoadedModel(
                    name = modelName,
                    path = modelPath,
                    config = config,
                    loadedAt = currentTime
                )
                modelRepository.saveModel(loadedModel)
                modelRepository.setCurrentModelPath(modelPath)

                _uiState.update {
                    it.copy(
                        modelState = ModelState.READY,
                        loadingProgress = 1f,
                        currentModelPath = modelPath,
                        loadedModels = modelRepository.getLoadedModels()
                    )
                }
            } catch (e: Exception) {
                println("Error loading model: ${e.message}")
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        modelState = ModelState.ERROR,
                        errorMessage = e.message ?: "Failed to load model",
                        loadingProgress = 0f
                    )
                }
            }
        }
    }

    fun removeLoadedModel(path: String) {
        modelRepository.removeModel(path)
        refreshLoadedModels()
    }

    fun updateInput(input: String) {
        _uiState.update { it.copy(currentInput = input) }
    }

    fun sendMessage() {
        val input = _uiState.value.currentInput.trim()
        val attachments = _uiState.value.pendingAttachments

        // Allow sending with just attachments even if text is empty
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

        // Build prompt with attachment info and tool instructions
        val promptWithAttachments = buildPromptWithAttachments(input, attachments)
        val toolAwarePrompt = buildToolAwarePrompt(promptWithAttachments, toolRegistry.specs())
        generateResponse(toolAwarePrompt)
    }

    private fun buildPromptWithAttachments(text: String, attachments: List<Attachment>): String {
        if (attachments.isEmpty()) return text

        val attachmentDescriptions = attachments.joinToString("\n") { attachment ->
            when (attachment.type) {
                org.abma.offlinelai_kmp.domain.model.AttachmentType.IMAGE ->
                    "[User attached an image: ${attachment.fileName}]"
                org.abma.offlinelai_kmp.domain.model.AttachmentType.PDF ->
                    "[User attached a PDF document: ${attachment.fileName}]"
                org.abma.offlinelai_kmp.domain.model.AttachmentType.DOCUMENT ->
                    "[User attached a document: ${attachment.fileName}]"
            }
        }

        return if (text.isNotEmpty()) {
            "$attachmentDescriptions\n\n$text"
        } else {
            attachmentDescriptions
        }
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

            val messagesForContext = _uiState.value.messages
                .dropLast(1) // Exclude the current streaming message
                .takeLast(10) // Keep last 10 messages for context
                .map { it.content to it.isFromUser }

            var accumulatedResponse = ""

            gemmaInference.generateResponseWithHistory(messagesForContext, prompt)
                .catch { e ->
                    println("Error generating response: ${e.message}")
                    e.printStackTrace()
                    _uiState.update { state ->
                        val updatedMessages = state.messages.map { msg ->
                            if (msg.id == streamingMessageId) {
                                msg.copy(
                                    content = "Error: ${e.message}",
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
                }
                .onCompletion {
                    // Check for tool call in the response
                    val toolCall = extractToolCall(accumulatedResponse)
                    if (toolCall != null) {
                        handleToolCall(toolCall, accumulatedResponse, messagesForContext)
                    } else {
                        _uiState.update { state ->
                            val updatedMessages = state.messages.map { msg ->
                                if (msg.id == streamingMessageId) {
                                    msg.copy(isStreaming = false)
                                } else msg
                            }
                            state.copy(
                                messages = updatedMessages,
                                modelState = ModelState.READY
                            )
                        }
                        streamingMessageId = null
                    }
                }
                .collect { token ->
                    accumulatedResponse += token
                    _uiState.update { state ->
                        val updatedMessages = state.messages.map { msg ->
                            if (msg.id == streamingMessageId) {
                                msg.copy(content = accumulatedResponse)
                            } else msg
                        }
                        state.copy(messages = updatedMessages)
                    }
                }
        }
    }

    private fun handleToolCall(
        toolCall: ToolCall,
        originalResponse: String,
        messagesForContext: List<Pair<String, Boolean>>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isToolCallInProgress = true) }

            try {
                // Create tool context
                val toolContext = ToolContext(
                    loadedModels = _uiState.value.loadedModels,
                    currentModelPath = _uiState.value.currentModelPath
                )

                // Execute the tool
                val toolResult = toolRegistry.execute(toolCall, toolContext)
                println("Tool '${toolCall.tool}' executed. Result: ${toolResult.result}")

                // Update the AI message to show tool was called
                val toolCallDisplay = "🔧 Calling ${toolCall.tool}...\n\n"
                _uiState.update { state ->
                    val updatedMessages = state.messages.map { msg ->
                        if (msg.id == streamingMessageId) {
                            msg.copy(content = toolCallDisplay, isStreaming = true)
                        } else msg
                    }
                    state.copy(messages = updatedMessages)
                }

                // Build follow-up prompt with tool result
                val toolResultPrompt = buildToolResultPrompt(toolCall, toolResult)
                val followUpPrompt = formatPromptWithHistoryAndToolResult(
                    messagesForContext,
                    toolCall,
                    toolResultPrompt
                )

                // Generate follow-up response
                var followUpResponse = ""
                gemmaInference.generateResponse(followUpPrompt)
                    .catch { e ->
                        println("Error in follow-up generation: ${e.message}")
                        _uiState.update { state ->
                            val updatedMessages = state.messages.map { msg ->
                                if (msg.id == streamingMessageId) {
                                    msg.copy(
                                        content = toolCallDisplay + "Error processing tool result: ${e.message}",
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
                    }
                    .onCompletion {
                        _uiState.update { state ->
                            val updatedMessages = state.messages.map { msg ->
                                if (msg.id == streamingMessageId) {
                                    msg.copy(isStreaming = false)
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
                    .collect { token ->
                        followUpResponse += token
                        // Strip any nested tool calls from follow-up
                        val displayResponse = stripToolCallBlock(followUpResponse)
                        _uiState.update { state ->
                            val updatedMessages = state.messages.map { msg ->
                                if (msg.id == streamingMessageId) {
                                    msg.copy(content = toolCallDisplay + displayResponse)
                                } else msg
                            }
                            state.copy(messages = updatedMessages)
                        }
                    }

            } catch (e: Exception) {
                println("Error handling tool call: ${e.message}")
                e.printStackTrace()
                _uiState.update { state ->
                    val updatedMessages = state.messages.map { msg ->
                        if (msg.id == streamingMessageId) {
                            msg.copy(
                                content = "Error executing tool: ${e.message}",
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
        }
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
