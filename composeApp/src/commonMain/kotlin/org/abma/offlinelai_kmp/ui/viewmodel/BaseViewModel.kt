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
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.domain.repository.ModelRepository
import org.abma.offlinelai_kmp.domain.usecase.*
import org.abma.offlinelai_kmp.inference.GemmaInference

data class ConversationUiState(
    val messages: List<ChatMessage> = emptyList(),
    val modelState: ModelState = ModelState.NOT_LOADED,
    val loadingProgress: Float = 0f,
    val currentInput: String = "",
    val errorMessage: String? = null,
    val currentModelPath: String? = null,
    val loadedModels: List<LoadedModel> = emptyList()
)

abstract class BaseConversationViewModel : ViewModel() {
    protected val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    protected val gemmaInference = GemmaInference()
    protected val modelRepository = ModelRepository()
    protected var streamingMessageId: String? = null

    private val loadModelUseCase = LoadModelUseCase(gemmaInference, modelRepository)
    private val getLoadedModelsUseCase = GetLoadedModelsUseCase(modelRepository)
    private val removeModelUseCase = RemoveModelUseCase(modelRepository)

    protected abstract val systemPrompt: String

    init {
        refreshLoadedModels()
    }

    fun refreshLoadedModels() {
        getLoadedModelsUseCase().onSuccess { models ->
            val currentPath = modelRepository.getCurrentModelPath()
            _uiState.update {
                it.copy(loadedModels = models, currentModelPath = currentPath)
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

    abstract fun sendMessage()

    fun clearChat() {
        _uiState.update { it.copy(messages = emptyList()) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    protected fun updateStreamingMessage(content: String) {
        val messageId = streamingMessageId ?: return
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == messageId) msg.copy(content = content) else msg
                }
            )
        }
    }

    protected fun finishStreaming() {
        val messageId = streamingMessageId ?: return
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == messageId) msg.copy(isStreaming = false) else msg
                },
                modelState = ModelState.READY
            )
        }
        streamingMessageId = null
    }

    protected fun handleError(exception: Exception) {
        val messageId = streamingMessageId ?: return
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(content = "Error: ${exception.message}", isStreaming = false, isError = true)
                    } else msg
                },
                modelState = ModelState.READY,
                errorMessage = exception.message
            )
        }
        streamingMessageId = null
    }
}
