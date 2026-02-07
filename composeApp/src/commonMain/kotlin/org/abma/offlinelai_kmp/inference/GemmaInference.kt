package org.abma.offlinelai_kmp.inference

import kotlinx.coroutines.flow.Flow
import org.abma.offlinelai_kmp.domain.model.ModelConfig

expect class GemmaInference() {
    /**
     * Load a Gemma model from the specified path.
     * @param modelPath The model filename (searched in platform-specific locations)
     * @param config Optional model configuration
     */
    suspend fun loadModel(modelPath: String, config: ModelConfig = ModelConfig())

    /**
     * Generate a response for the given prompt.
     * @param prompt The input text
     * @return Flow emitting the generated response
     */
    fun generateResponse(prompt: String): Flow<String>

    /**
     * Generate a response with conversation history.
     * @param messages List of (content, isFromUser) pairs
     * @param currentPrompt The current user prompt
     * @return Flow emitting the generated response
     */
    fun generateResponseWithHistory(
        systemPrompt: String,
        currentPrompt: String
    ): Flow<String>

    /** Check if a model is currently loaded */
    fun isModelLoaded(): Boolean

    /** Get the current loading progress (0.0 to 1.0) */
    fun getLoadingProgress(): Float

    /** Release resources */
    fun close()
}

/**
 * Format conversation history into Gemma chat format.
 */
fun formatPromptWithHistory(
    messages: List<Pair<String, Boolean>>,
    currentPrompt: String
): String = buildString {
    messages.forEach { (content, isFromUser) ->
        if (isFromUser) {
            append("<start_of_turn>user\n$content<end_of_turn>\n")
        } else {
            append("<start_of_turn>model\n$content<end_of_turn>\n")
        }
    }
    append("<start_of_turn>user\n$currentPrompt<end_of_turn>\n")
    append("<start_of_turn>model\n")
}
