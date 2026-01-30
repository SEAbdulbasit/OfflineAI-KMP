package org.abma.offlinelai_kmp.inference

import kotlinx.coroutines.flow.Flow
import org.abma.offlinelai_kmp.domain.model.ModelConfig

/**
 * Expected interface for Gemma model inference.
 * Platform-specific implementations will use MediaPipe LLM Inference API.
 */
expect class GemmaInference() {
    suspend fun loadModel(modelPath: String, config: ModelConfig = ModelConfig())
    fun generateResponse(prompt: String): Flow<String>
    fun generateResponseWithHistory(
        messages: List<Pair<String, Boolean>>,
        currentPrompt: String
    ): Flow<String>
    fun isModelLoaded(): Boolean
    fun getLoadingProgress(): Float
    fun close()
}
