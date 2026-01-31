package org.abma.offlinelai_kmp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.domain.repository.ModelRepository
import org.abma.offlinelai_kmp.inference.GemmaInference
import kotlin.time.Clock

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val modelState: ModelState = ModelState.NOT_LOADED,
    val loadingProgress: Float = 0f,
    val currentInput: String = "",
    val errorMessage: String? = null,
    val currentModelPath: String? = null,
    val loadedModels: List<LoadedModel> = emptyList()
)

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val gemmaInference = GemmaInference()
    private val modelRepository = ModelRepository()
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
