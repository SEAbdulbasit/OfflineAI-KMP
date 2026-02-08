package org.abma.offlinelai_kmp.inference

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import platform.Foundation.*


actual class GemmaInference {
    private var isLoaded = false
    private var loadingProgress = 0f
    private var resolvedModelPath: String? = null

    actual suspend fun loadModel(modelPath: String, config: ModelConfig) {
        withContext(Dispatchers.IO) {
            try {
                loadingProgress = 0.1f

                val resolvedPath = ModelPathResolver.resolve(modelPath)
                    ?: throw IllegalArgumentException(
                        "Model file not found: $modelPath\n\n" +
                        "📁 Add the model via Finder:\n" +
                        "1. Connect iPhone to Mac\n" +
                        "2. Open Finder → Select iPhone → Files tab\n" +
                        "3. Drag model file into this app's folder\n\n" +
                        "Searched in:\n${ModelPathResolver.getSearchPaths(modelPath).take(4).joinToString("\n") { " • $it" }}\n\n" +
                        "Documents: ${ModelPathResolver.getDocumentsDirectory()}"
                    )

                loadingProgress = 0.5f
                resolvedModelPath = resolvedPath

                NSUserDefaults.standardUserDefaults.setObject(resolvedPath, forKey = PREF_MODEL_PATH)
                NSUserDefaults.standardUserDefaults.synchronize()

                loadingProgress = 1.0f
                isLoaded = true
            } catch (e: Exception) {
                isLoaded = false
                loadingProgress = 0f
                throw e
            }
        }
    }

    actual fun generateResponse(prompt: String): Flow<String> = flow {
        if (!isLoaded) throw IllegalStateException("Model not loaded")

        NSUserDefaults.standardUserDefaults.removeObjectForKey(PREF_RESPONSE)

        NSNotificationCenter.defaultCenter.postNotificationName(
            NOTIFICATION_GENERATE,
            `object` = null,
            userInfo = mapOf("prompt" to prompt, "modelPath" to (resolvedModelPath ?: ""))
        )

        val response = pollForResponse(timeoutMs = 60_000, intervalMs = 100)
        emit(response ?: "Error: Generation timeout")
    }

    private suspend fun pollForResponse(timeoutMs: Long, intervalMs: Long): String? {
        val maxAttempts = (timeoutMs / intervalMs).toInt()
        repeat(maxAttempts) {
            delay(intervalMs)
            NSUserDefaults.standardUserDefaults.stringForKey(PREF_RESPONSE)?.let { response ->
                NSUserDefaults.standardUserDefaults.removeObjectForKey(PREF_RESPONSE)
                NSUserDefaults.standardUserDefaults.synchronize()
                return response
            }
        }
        return null
    }

    actual fun generateResponseWithHistory(
        systemPrompt: String,
        currentPrompt: String
    ): Flow<String> = generateResponse(systemPrompt+currentPrompt)

    actual fun isModelLoaded(): Boolean = isLoaded

    actual fun getLoadingProgress(): Float = loadingProgress

    actual fun close() {
        listOf(PREF_MODEL_PATH, PREF_PROMPT, PREF_RESPONSE).forEach {
            NSUserDefaults.standardUserDefaults.removeObjectForKey(it)
        }
        NSUserDefaults.standardUserDefaults.synchronize()

        isLoaded = false
        loadingProgress = 0f
        resolvedModelPath = null
    }

    companion object {
        private const val PREF_MODEL_PATH = "gemma_model_path"
        private const val PREF_PROMPT = "gemma_current_prompt"
        private const val PREF_RESPONSE = "gemma_response"
        private const val NOTIFICATION_GENERATE = "GemmaGenerateRequest"
    }
}
