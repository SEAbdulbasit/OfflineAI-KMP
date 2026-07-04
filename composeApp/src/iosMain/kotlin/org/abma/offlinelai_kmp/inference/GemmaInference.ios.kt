package org.abma.offlinelai_kmp.inference

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import platform.Foundation.NSNotificationCenter

actual class GemmaInference {
    private var isLoaded = false
    private var loadingProgress = 0f
    private var resolvedModelPath: String? = null

    actual suspend fun loadModel(modelPath: String, config: ModelConfig) {
        withContext(Dispatchers.IO) {
            println("GemmaInference: Loading model from $modelPath")
            try {
                loadingProgress = 0.1f

                val resolvedPath = ModelPathResolver.resolve(modelPath)
                    ?: throw IllegalArgumentException(
                        "Model file not found: $modelPath\n\n" +
                                "📁 Add the model via Finder:\n" +
                                "1. Connect iPhone to Mac\n" +
                                "2. Open Finder → Select iPhone → Files tab\n" +
                                "3. Drag model file into this app's folder\n\n" +
                                "Searched in:\n${
                                    ModelPathResolver.getSearchPaths(modelPath).take(4).joinToString("\n") { " • $it" }
                                }\n\n" +
                                "Documents: ${ModelPathResolver.getDocumentsDirectory()}"
                    )

                loadingProgress = 0.5f
                resolvedModelPath = resolvedPath
                loadingProgress = 1.0f
                isLoaded = true
                println("GemmaInference: Model loaded successfully at $resolvedPath")
            } catch (e: Exception) {
                println("GemmaInference: Error loading model: ${e.message}")
                isLoaded = false
                loadingProgress = 0f
                throw e
            }
        }
    }

    actual fun generateResponse(prompt: String): Flow<String> = callbackFlow {
        if (!isLoaded) {
            throw IllegalStateException("Model not loaded")
        }

        val modelPath = resolvedModelPath ?: throw IllegalStateException("Model path not resolved")
        val center = NSNotificationCenter.defaultCenter

        val tokenObserver = center.addObserverForName(
            name = NOTIFICATION_TOKEN,
            `object` = null,
            queue = null
        ) { notification ->
            val token = notification?.userInfo?.get("token") as? String ?: return@addObserverForName
            trySend(token)
        }

        val doneObserver = center.addObserverForName(
            name = NOTIFICATION_DONE,
            `object` = null,
            queue = null
        ) {
            close()
        }

        val errorObserver = center.addObserverForName(
            name = NOTIFICATION_ERROR,
            `object` = null,
            queue = null
        ) { notification ->
            val message = notification?.userInfo?.get("error") as? String ?: "Generation failed"
            close(IllegalStateException(message))
        }

        center.postNotificationName(
            aName = NOTIFICATION_GENERATE,
            `object` = null,
            userInfo = mapOf(
                "prompt" to prompt,
                "modelPath" to modelPath
            )
        )

        awaitClose {
            center.removeObserver(tokenObserver)
            center.removeObserver(doneObserver)
            center.removeObserver(errorObserver)
        }
    }.buffer(capacity = 64)

    actual fun generateResponseWithHistory(
        systemPrompt: String,
        currentPrompt: String
    ): Flow<String> = generateResponse(systemPrompt + currentPrompt)

    actual fun isModelLoaded(): Boolean = isLoaded

    actual fun getLoadingProgress(): Float = loadingProgress

    actual fun close() {
        isLoaded = false
        loadingProgress = 0f
        resolvedModelPath = null
    }

    companion object {
        private const val NOTIFICATION_GENERATE = "GemmaGenerateRequest"
        private const val NOTIFICATION_TOKEN = "GemmaTokenResponse"
        private const val NOTIFICATION_DONE = "GemmaGenerationDone"
        private const val NOTIFICATION_ERROR = "GemmaGenerationError"
    }
}
