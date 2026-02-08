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

fun formatPrompt(userMessage: String): String = buildString {
    append("<start_of_turn>user\n$userMessage<end_of_turn>\n")
    append("<start_of_turn>model\n")
}
