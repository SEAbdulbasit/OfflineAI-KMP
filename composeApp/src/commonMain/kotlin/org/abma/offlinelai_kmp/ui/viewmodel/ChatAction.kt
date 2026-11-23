package org.abma.offlinelai_kmp.ui.viewmodel

import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.model.PersonaPreset
import org.abma.offlinelai_kmp.inference.TrimmingStrategy

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: UI Actions (MVI Pattern)
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Sealed interface for all possible user actions in the Chat & Sentient OS experience.
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

    /** Directly send a specific prompt string (e.g., from quick actions or command palette) */
    data class SendPrompt(val prompt: String) : ChatAction

    /** User wants to stop the current generation */
    data object StopGeneration : ChatAction

    /** User wants to clear chat history */
    data object ClearChat : ChatAction

    /** User dismissed the error snackbar */
    data object DismissError : ChatAction

    /** Refresh the list of available models */
    data object RefreshModels : ChatAction

    /** Switch the active AI persona / agent mode */
    data class SelectPersona(val persona: PersonaPreset) : ChatAction

    /** Update the custom system prompt directive */
    data class UpdateCustomSystemPrompt(val prompt: String) : ChatAction

    /** Update runtime model configuration (temperature, topK, maxTokens, topP) */
    data class UpdateModelConfig(val config: ModelConfig) : ChatAction

    /** Update conversation context window trimming strategy */
    data class UpdateTrimmingStrategy(val strategy: TrimmingStrategy) : ChatAction

    /** Regenerate an AI response (either by specific message ID or the latest turn) */
    data class RegenerateMessage(val messageId: String? = null) : ChatAction

    /** Edit a user message in-place, branch conversation history from that point, and resend */
    data class EditAndResendMessage(val messageId: String, val newContent: String) : ChatAction

    /** Pin or unpin a message to active context memory */
    data class TogglePinMessage(val messageId: String) : ChatAction

    /** Clear all pinned context memory cards */
    data object ClearPinnedMessages : ChatAction

    /** Toggle the expandable Sentient OS Telemetry Inspector HUD */
    data object ToggleTelemetryHud : ChatAction

    /** Toggle the Relay Command & Tool Deck sheet */
    data object ToggleCommandDeck : ChatAction

    /** Toggle in-conversation search bar visibility */
    data object ToggleSearch : ChatAction

    /** Update the in-conversation search query */
    data class SetSearchQuery(val query: String) : ChatAction

    /** Export the current chat transcript (via system share sheet or clipboard) */
    data class ExportChat(val shareViaSystemSheet: Boolean = true) : ChatAction

    /** Reset aggregated performance telemetry metrics */
    data object ResetMetrics : ChatAction

    /** Directly execute a registered device tool from the Command Deck */
    data class ExecuteQuickTool(val toolName: String, val argumentsJson: String = "{}") : ChatAction
}
