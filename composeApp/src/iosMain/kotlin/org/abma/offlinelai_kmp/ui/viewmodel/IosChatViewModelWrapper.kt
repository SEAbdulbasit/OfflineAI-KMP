package org.abma.offlinelai_kmp.ui.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.abma.offlinelai_kmp.domain.model.ModelConfig

class IosChatViewModelWrapper {
    private val viewModel = ChatViewModel()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var stateCallback: ((ChatUiState) -> Unit)? = null

    init {
        viewModel.uiState.onEach { state ->
            stateCallback?.invoke(state)
        }.launchIn(scope)
    }

    fun observeState(callback: (ChatUiState) -> Unit) {
        stateCallback = callback
        callback(viewModel.uiState.value)
    }

    fun loadModel(modelPath: String) {
        viewModel.onAction(ChatAction.LoadModel(modelPath, ModelConfig()))
    }

    fun sendMessage(text: String) {
        viewModel.onAction(ChatAction.UpdateInput(text))
        viewModel.onAction(ChatAction.SendMessage)
    }

    fun updateInput(text: String) {
        viewModel.onAction(ChatAction.UpdateInput(text))
    }

    fun clearChat() {
        viewModel.onAction(ChatAction.ClearChat)
    }

    fun dismissError() {
        viewModel.onAction(ChatAction.DismissError)
    }

    fun removeModel(path: String) {
        viewModel.onAction(ChatAction.RemoveModel(path))
    }

    fun refreshModels() {
        viewModel.onAction(ChatAction.RefreshModels)
    }

    fun dispose() {
        scope.cancel()
    }
}
