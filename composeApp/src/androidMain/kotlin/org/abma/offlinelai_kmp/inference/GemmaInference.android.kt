package org.abma.offlinelai_kmp.inference

import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.abma.offlinelai_kmp.domain.model.ModelConfig

actual class GemmaInference {
    actual suspend fun loadModel(modelPath: String, config: ModelConfig) {
        // TODO: Workshop Step 2 - Implement Android LLM loading
    }

    actual fun generateResponse(prompt: String): Flow<String> = callbackFlow {
        // TODO: Workshop Step 2 - Implement Android LLM generation
        close()
        awaitClose { }
    }

    actual fun generateResponseWithHistory(
        systemPrompt: String,
        currentPrompt: String
    ): Flow<String> = generateResponse(systemPrompt + currentPrompt)

    actual fun isModelLoaded(): Boolean = false

    actual fun getLoadingProgress(): Float = 0f

    actual fun close() {
        // TODO: Workshop Step 2 - Implement cleanup
    }
}
