package org.abma.offlinelai_kmp.ui.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.model.ModelState
import org.abma.offlinelai_kmp.domain.repository.LoadedModel

data class IosUiState(
    val messages: List<IosChatMessage>,
    val modelState: String,
    val loadingProgress: Float,
    val currentInput: String,
    val errorMessage: String?,
    val currentModelPath: String?,
    val loadedModels: List<IosLoadedModel>,
    val isGenerating: Boolean,
    val isToolCallInProgress: Boolean = false
)

data class IosChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val isStreaming: Boolean,
    val isError: Boolean
)

data class IosLoadedModel(
    val name: String,
    val path: String,
    val loadedAt: Long
)

// Wrapper for General Chat (no tools)
class IosGeneralChatWrapper {
    private val viewModel = GeneralChatViewModel()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var stateCallback: ((IosUiState) -> Unit)? = null

    init {
        viewModel.uiState.onEach { state ->
            stateCallback?.invoke(state.toIosState())
        }.launchIn(scope)
    }

    fun observeState(callback: (IosUiState) -> Unit) {
        stateCallback = callback
        callback(viewModel.uiState.value.toIosState())
    }

    fun loadModel(modelPath: String) {
        viewModel.loadModel(modelPath, ModelConfig())
    }

    fun sendMessage(text: String) {
        viewModel.updateInput(text)
        viewModel.sendMessage()
    }

    fun updateInput(text: String) {
        viewModel.updateInput(text)
    }

    fun clearChat() {
        viewModel.clearChat()
    }

    fun dismissError() {
        viewModel.dismissError()
    }

    fun removeModel(path: String) {
        viewModel.removeLoadedModel(path)
    }

    fun refreshModels() {
        viewModel.refreshLoadedModels()
    }

    fun dispose() {
        scope.cancel()
    }

    private fun ConversationUiState.toIosState() = IosUiState(
        messages = messages.map { it.toIosMessage() },
        modelState = modelState.name,
        loadingProgress = loadingProgress,
        currentInput = currentInput,
        errorMessage = errorMessage,
        currentModelPath = currentModelPath,
        loadedModels = loadedModels.map { it.toIosModel() },
        isGenerating = modelState == ModelState.GENERATING
    )

    private fun ChatMessage.toIosMessage() = IosChatMessage(
        id = id,
        content = content,
        isFromUser = isFromUser,
        timestamp = timestamp,
        isStreaming = isStreaming,
        isError = isError
    )

    private fun LoadedModel.toIosModel() = IosLoadedModel(
        name = name,
        path = path,
        loadedAt = loadedAt
    )
}

// Wrapper for Actions/Tools
class IosActionsWrapper {
    private val viewModel = ActionsViewModel()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var stateCallback: ((IosUiState) -> Unit)? = null

    init {
        viewModel.uiState.onEach { state ->
            stateCallback?.invoke(state.toIosState())
        }.launchIn(scope)
    }

    fun observeState(callback: (IosUiState) -> Unit) {
        stateCallback = callback
        callback(viewModel.uiState.value.toIosState())
    }

    fun loadModel(modelPath: String) {
        viewModel.loadModel(modelPath, ModelConfig())
    }

    fun sendMessage(text: String) {
        viewModel.updateInput(text)
        viewModel.sendMessage()
    }

    fun updateInput(text: String) {
        viewModel.updateInput(text)
    }

    fun clearChat() {
        viewModel.clearChat()
    }

    fun dismissError() {
        viewModel.dismissError()
    }

    fun removeModel(path: String) {
        viewModel.removeLoadedModel(path)
    }

    fun refreshModels() {
        viewModel.refreshLoadedModels()
    }

    fun dispose() {
        scope.cancel()
    }

    private fun ConversationUiState.toIosState() = IosUiState(
        messages = messages.map { it.toIosMessage() },
        modelState = modelState.name,
        loadingProgress = loadingProgress,
        currentInput = currentInput,
        errorMessage = errorMessage,
        currentModelPath = currentModelPath,
        loadedModels = loadedModels.map { it.toIosModel() },
        isGenerating = modelState == ModelState.GENERATING
    )

    private fun ChatMessage.toIosMessage() = IosChatMessage(
        id = id,
        content = content,
        isFromUser = isFromUser,
        timestamp = timestamp,
        isStreaming = isStreaming,
        isError = isError
    )

    private fun LoadedModel.toIosModel() = IosLoadedModel(
        name = name,
        path = path,
        loadedAt = loadedAt
    )
}

// Backward compatibility wrapper - delegates to IosActionsWrapper
class IosChatViewModelWrapper {
    private val delegate = IosActionsWrapper()

    fun observeState(callback: (IosUiState) -> Unit) = delegate.observeState(callback)
    fun loadModel(modelPath: String) = delegate.loadModel(modelPath)
    fun sendMessage(text: String) = delegate.sendMessage(text)
    fun updateInput(text: String) = delegate.updateInput(text)
    fun clearChat() = delegate.clearChat()
    fun dismissError() = delegate.dismissError()
    fun removeModel(path: String) = delegate.removeModel(path)
    fun refreshModels() = delegate.refreshModels()
    fun dispose() = delegate.dispose()
}
