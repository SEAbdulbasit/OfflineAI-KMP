package org.abma.offlinelai_kmp.inference

import kotlinx.coroutines.flow.Flow
import org.abma.offlinelai_kmp.domain.model.ModelConfig

expect class GemmaInference() {
    suspend fun loadModel(modelPath: String, config: ModelConfig = ModelConfig())
    fun generateResponse(prompt: String): Flow<String>
    fun generateResponseWithHistory(
        systemPrompt: String,
        currentPrompt: String
    ): Flow<String>
    fun isModelLoaded(): Boolean
    fun getLoadingProgress(): Float
    fun close()
}

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
