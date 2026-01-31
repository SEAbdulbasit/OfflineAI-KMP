package org.abma.offlinelai_kmp.inference

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import platform.Foundation.*

/**
 * iOS implementation of GemmaInference.
 *
 * MediaPipe inference is handled by the Swift layer (InferenceBridge.swift).
 * The Kotlin code validates the model file exists and manages state.
 * Actual inference calls are made from the iOS app layer.
 */
actual class GemmaInference {
    private var isLoaded = false
    private var loadingProgress = 0f
    private var modelPath: String? = null
    private var config: ModelConfig? = null
    private var resolvedModelPath: String? = null

    actual suspend fun loadModel(modelPath: String, config: ModelConfig) {
        withContext(Dispatchers.IO) {
            try {
                loadingProgress = 0.1f
                this@GemmaInference.modelPath = modelPath
                this@GemmaInference.config = config

                // Resolve model path
                val resolvedPath = resolveModelPath(modelPath)

                loadingProgress = 0.3f

                // Check if file exists
                val fileManager = NSFileManager.defaultManager
                if (!fileManager.fileExistsAtPath(resolvedPath)) {
                    val availablePaths = getSearchPaths(modelPath)
                    val appDocuments = NSSearchPathForDirectoriesInDomains(
                        NSDocumentDirectory,
                        NSUserDomainMask,
                        true
                    ).firstOrNull() as? String ?: "Unknown"

                    throw IllegalArgumentException(
                        "Model file not found: $modelPath\n\n" +
                        "📁 Add the model via Finder:\n" +
                        "1. Connect iPhone to Mac\n" +
                        "2. Open Finder → Select iPhone → Files tab\n" +
                        "3. Find this app and drag the model file into it\n\n" +
                        "Searched locations:\n${availablePaths.take(4).joinToString("\n") { " • $it" }}\n\n" +
                        "App Documents: $appDocuments"
                    )
                }

                loadingProgress = 0.5f
                resolvedModelPath = resolvedPath

                // Store the path for the Swift bridge to use
                // The actual MediaPipe loading happens when generate is called
                NSUserDefaults.standardUserDefaults.setObject(resolvedPath, forKey = "gemma_model_path")
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

    private fun resolveModelPath(modelPath: String): String {
        val fileManager = NSFileManager.defaultManager
        val searchPaths = getSearchPaths(modelPath)
        return searchPaths.firstOrNull { path ->
            fileManager.fileExistsAtPath(path)
        } ?: searchPaths.first()
    }

    private fun getSearchPaths(modelPath: String): List<String> {
        val documentsDir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String ?: ""

        val cachesDir = NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String ?: ""

        val homeDir = NSHomeDirectory()

        val bundlePath = NSBundle.mainBundle.pathForResource(
            modelPath.removeSuffix(".bin").removeSuffix(".task"),
            ofType = if (modelPath.endsWith(".bin")) "bin" else "task"
        ) ?: ""

        return listOf(
            "$documentsDir/$modelPath",
            "$documentsDir/models/$modelPath",
            "$cachesDir/$modelPath",
            bundlePath,
            modelPath,
            "$homeDir/Documents/$modelPath"
        ).filter { it.isNotEmpty() }
    }

    actual fun generateResponse(prompt: String): Flow<String> = flow {
        if (!isLoaded) {
            throw IllegalStateException("Model not loaded")
        }

        // Store the prompt for the Swift bridge
        NSUserDefaults.standardUserDefaults.setObject(prompt, forKey = "gemma_current_prompt")
        NSUserDefaults.standardUserDefaults.synchronize()

        // Post notification that generation is requested
        NSNotificationCenter.defaultCenter.postNotificationName(
            "GemmaGenerateRequest",
            `object` = null,
            userInfo = mapOf("prompt" to prompt, "modelPath" to (resolvedModelPath ?: ""))
        )

        // Wait for response (stored by Swift bridge)
        // Poll for the response with timeout
        var response: String? = null
        var attempts = 0
        val maxAttempts = 600 // 60 seconds timeout (100ms per attempt)

        while (response == null && attempts < maxAttempts) {
            kotlinx.coroutines.delay(100)
            response = NSUserDefaults.standardUserDefaults.stringForKey("gemma_response")
            if (response != null) {
                // Clear the response
                NSUserDefaults.standardUserDefaults.removeObjectForKey("gemma_response")
                NSUserDefaults.standardUserDefaults.synchronize()
            }
            attempts++
        }

        if (response != null) {
            emit(response)
        } else {
            emit("Error: Generation timeout. Please ensure the model is loaded correctly.")
        }
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
        NSUserDefaults.standardUserDefaults.removeObjectForKey("gemma_model_path")
        NSUserDefaults.standardUserDefaults.removeObjectForKey("gemma_current_prompt")
        NSUserDefaults.standardUserDefaults.removeObjectForKey("gemma_response")
        NSUserDefaults.standardUserDefaults.synchronize()

        isLoaded = false
        loadingProgress = 0f
        modelPath = null
        config = null
        resolvedModelPath = null
    }
}
