package org.abma.offlinelai_kmp.ui.viewmodel

import org.abma.offlinelai_kmp.domain.model.ModelConfig

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: UI Actions (MVI Pattern)
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Sealed interface for all possible user actions.
 *
 * WHY SEALED?
 * - Exhaustive `when` checks (compiler ensures you handle all cases)
 * - Single source of truth for all UI events
 * - Easy to add logging/analytics in one place
 *
 * HOW IT WORKS:
 * 1. UI dispatches action: viewModel.onAction(ChatAction.SendMessage)
 * 2. ViewModel handles in when block
 * 3. ViewModel updates state
 * 4. UI recomposes based on new state
 *
 * This is cleaner than having multiple callback functions for each action.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
sealed interface ChatAction {
    /** Load a Gemma model from the given path */
    data class LoadModel(val path: String, val config: ModelConfig = ModelConfig()) : ChatAction

    /** Remove/unload a model from the repository */
    data class RemoveModel(val path: String) : ChatAction

    /** User is typing - update the input field */
    data class UpdateInput(val text: String) : ChatAction

    /** User pressed send button */
    data object SendMessage : ChatAction

    /** User wants to clear chat history */
    data object ClearChat : ChatAction

    /** User dismissed the error snackbar */
    data object DismissError : ChatAction

    /** Refresh the list of available models */
    data object RefreshModels : ChatAction
}
