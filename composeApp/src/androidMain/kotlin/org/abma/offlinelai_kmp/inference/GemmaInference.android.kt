package org.abma.offlinelai_kmp.inference

import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.VisionModelOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import java.io.File

actual class GemmaInference {
    private var llmInference: LlmInference? = null
    private var isLoaded = false
    private var loadingProgress = 0f

    actual suspend fun loadModel(modelPath: String, config: ModelConfig) {
        withContext(Dispatchers.IO) {
            try {
                loadingProgress = 0.1f

                // Try multiple locations for the model
                val possiblePaths = listOf(
                    "/data/local/tmp/llm/$modelPath",
                    "${AndroidContextProvider.applicationContext.getExternalFilesDir(null)?.absolutePath}/$modelPath",
                    "${AndroidContextProvider.applicationContext.filesDir.absolutePath}/$modelPath",
                    "${AndroidContextProvider.applicationContext.cacheDir.absolutePath}/$modelPath",
                    "/storage/emulated/0/Download/$modelPath"
                )

                val modelFile = possiblePaths.firstOrNull { File(it).exists() }
                    ?: throw IllegalArgumentException("Model file not found at any of: ${possiblePaths.joinToString()}")

                loadingProgress = 0.3f

                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile)
                    .setMaxTokens(config.maxTokens)
                    .setMaxTopK(config.topK)
                    .setVisionModelOptions(VisionModelOptions.builder().build())
                    .build()

                loadingProgress = 0.5f
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

        val result = inference.generateResponse(prompt)
        trySend(result)
        close()

        awaitClose { }
    }

    actual fun generateResponseWithHistory(
        messages: List<Pair<String, Boolean>>,
        currentPrompt: String
    ): Flow<String> {
        val formattedPrompt = buildString {
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
        return generateResponse(formattedPrompt)
    }

    actual fun isModelLoaded(): Boolean = isLoaded

    actual fun getLoadingProgress(): Float = loadingProgress

    actual fun close() {
        llmInference?.close()
        llmInference = null
        isLoaded = false
        loadingProgress = 0f
    }
}
