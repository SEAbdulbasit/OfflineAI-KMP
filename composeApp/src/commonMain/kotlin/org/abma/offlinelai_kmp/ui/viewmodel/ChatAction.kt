package org.abma.offlinelai_kmp.ui.viewmodel

import org.abma.offlinelai_kmp.domain.model.ModelConfig

sealed interface ChatAction {
    data class LoadModel(val path: String, val config: ModelConfig = ModelConfig()) : ChatAction
    data class RemoveModel(val path: String) : ChatAction
    data class UpdateInput(val text: String) : ChatAction
    data object SendMessage : ChatAction
    data object ClearChat : ChatAction
    data object DismissError : ChatAction
    data object RefreshModels : ChatAction
}
