package org.abma.offlinelai_kmp.inference

import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.abma.offlinelai_kmp.domain.model.ModelConfig

actual class GemmaInference {
    private var llmInference: LlmInference? = null
    private var isLoaded = false
    private var loadingProgress = 0f

    actual suspend fun loadModel(modelPath: String, config: ModelConfig) {
        withContext(Dispatchers.IO) {
            try {
                loadingProgress = 0.1f
                val resolvedPath = ModelPathResolver.resolve(modelPath)
                    ?: throw IllegalArgumentException(
                        "Model file not found: $modelPath\n\n" +
                                "Searched in:\n${
                                    ModelPathResolver.getSearchPaths(modelPath).joinToString("\n") { " • $it" }
                                }"
                    )

                loadingProgress = 0.3f

                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(resolvedPath)
                    .setMaxTokens(config.maxTokens)
                    .setMaxTopK(config.topK)
                    .build()

                loadingProgress = 0.6f

                llmInference = LlmInference.createFromOptions(
                    AndroidContextProvider.applicationContext,
                    options
                )

                loadingProgress = 1.0f
                isLoaded = true
            } catch (e: Exception) {
                isLoaded = false
                loadingProgress = 0f
                throw e
            }
        }
    }

    actual fun generateResponse(prompt: String): Flow<String> = callbackFlow {
        val inference = llmInference ?: throw IllegalStateException("Model not loaded")
        trySend(inference.generateResponse(prompt))
        close()
        awaitClose { }
    }

    actual fun generateResponseWithHistory(
        systemPrompt: String,
        currentPrompt: String
    ): Flow<String> = generateResponse(systemPrompt + currentPrompt)

    actual fun close() {
        llmInference?.close()
        llmInference = null
        isLoaded = false
        loadingProgress = 0f
    }
}
