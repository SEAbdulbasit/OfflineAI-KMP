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
import org.abma.offlinelai_kmp.tools.*

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val modelState: ModelState = ModelState.NOT_LOADED,
    val loadingProgress: Float = 0f,
    val currentInput: String = "",
    val errorMessage: String? = null,
    val currentModelPath: String? = null,
    val loadedModels: List<LoadedModel> = emptyList()
)

sealed interface ChatAction {
    data class LoadModel(val path: String, val config: ModelConfig = ModelConfig()) : ChatAction
    data class RemoveModel(val path: String) : ChatAction
    data class UpdateInput(val text: String) : ChatAction
    data object SendMessage : ChatAction
    data object ClearChat : ChatAction
    data object DismissError : ChatAction
    data object RefreshModels : ChatAction
}

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val gemma = GemmaInference()
    private val repository = ModelRepository()
    private val tools = createDefaultToolRegistry()
    private var streamingId: String? = null

    init {
        refreshModels()
    }

    fun onAction(action: ChatAction) {
        when (action) {
            is ChatAction.LoadModel -> loadModel(action.path, action.config)
            is ChatAction.RemoveModel -> removeModel(action.path)
            is ChatAction.UpdateInput -> _uiState.update { it.copy(currentInput = action.text) }
            is ChatAction.SendMessage -> sendMessage()
            is ChatAction.ClearChat -> _uiState.update { it.copy(messages = emptyList()) }
            is ChatAction.DismissError -> _uiState.update { it.copy(errorMessage = null) }
            is ChatAction.RefreshModels -> refreshModels()
        }
    }

    private fun refreshModels() {
        val models = repository.getLoadedModels()
        val path = repository.getCurrentModelPath()
        _uiState.update { it.copy(loadedModels = models, currentModelPath = path) }
    }

    private fun loadModel(path: String, config: ModelConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(modelState = ModelState.LOADING, errorMessage = null) }
            try {
                gemma.loadModel(path, config)
                repository.saveModel(LoadedModel("Gemma", path, config, 0L))
                repository.setCurrentModelPath(path)
                refreshModels()
                _uiState.update { it.copy(modelState = ModelState.READY, loadingProgress = 1f) }
            } catch (e: Exception) {
                _uiState.update { it.copy(modelState = ModelState.ERROR, errorMessage = e.message) }
            }
        }
    }

    private fun removeModel(path: String) {
        repository.removeModel(path)
        refreshModels()
    }

    private fun sendMessage() {
        val input = _uiState.value.currentInput.trim()
        if (input.isEmpty() || _uiState.value.modelState != ModelState.READY) return

        _uiState.update { it.copy(
            messages = it.messages + ChatMessage.user(input),
            currentInput = "",
            modelState = ModelState.GENERATING
        )}

        generateResponse(input)
    }

    private fun generateResponse(prompt: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val aiMsg = ChatMessage.ai("", isStreaming = true)
            streamingId = aiMsg.id
            _uiState.update { it.copy(messages = it.messages + aiMsg) }

            val systemPrompt = buildSystemPrompt(tools.specs())
            var fullResponse = ""

            try {
                gemma.generateResponseWithHistory(systemPrompt, prompt).collect { token ->
                    fullResponse += token
                    updateStreaming(fullResponse)
                }
                
                val toolCall = extractToolCall(fullResponse)
                if (toolCall != null) {
                    executeTool(toolCall)
                } else {
                    finishStreaming()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private suspend fun executeTool(call: ToolCall) {
        updateStreaming("🔧 Calling ${call.tool}...")
        val result = tools.execute(call, ToolContext(_uiState.value.loadedModels, _uiState.value.currentModelPath))
        updateStreaming("🔧 Tool Result (${call.tool}):\n${result.result}\n\n")
        finishStreaming()
    }

    private fun updateStreaming(content: String) {
        _uiState.update { state ->
            state.copy(messages = state.messages.map { 
                if (it.id == streamingId) it.copy(content = content) else it 
            })
        }
    }

    private fun finishStreaming() {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { if (it.id == streamingId) it.copy(isStreaming = false) else it },
                modelState = ModelState.READY
            )
        }
        streamingId = null
    }

    private fun handleError(e: Exception) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { 
                    if (it.id == streamingId) it.copy(content = "Error: ${e.message}", isStreaming = false, isError = true) else it 
                },
                modelState = ModelState.READY
            )
        }
        streamingId = null
    }

    override fun onCleared() {
        gemma.close()
    }
}
