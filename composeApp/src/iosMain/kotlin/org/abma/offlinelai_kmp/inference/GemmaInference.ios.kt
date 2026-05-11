package org.abma.offlinelai_kmp.inference

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaults


actual class GemmaInference {
    actual suspend fun loadModel(modelPath: String, config: ModelConfig) {
        // TODO: Workshop Step 3 - Implement iOS model loading path resolution
    }

    actual fun generateResponse(prompt: String): Flow<String> = flow {
        // TODO: Workshop Step 3 - Implement iOS generation via NotificationCenter
    }

    actual fun generateResponseWithHistory(
        systemPrompt: String,
        currentPrompt: String
    ): Flow<String> = generateResponse(systemPrompt + currentPrompt)

    actual fun isModelLoaded(): Boolean = false

    actual fun getLoadingProgress(): Float = 0f

    actual fun close() {
        // TODO: Workshop Step 3 - Implement cleanup
    }
}
