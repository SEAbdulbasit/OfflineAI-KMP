package org.abma.offlinelai_kmp.inference

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaults

actual class GemmaInference {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private var modelLoaded = false

    actual suspend fun loadModel(modelPath: String, config: ModelConfig) {
        // In this workshop, the Swift side handles the actual loading via the path
        // We just notify the Swift bridge about the model path
        userDefaults.setObject(modelPath, forKey = "gemma_model_path")
        modelLoaded = true
    }

    actual fun generateResponse(prompt: String): Flow<String> = flow {
        // Clear previous response
        userDefaults.removeObjectForKey("gemma_response")
        
        // Notify Swift to start generation
        NSNotificationCenter.defaultCenter.postNotificationName(
            aName = "GemmaGenerateRequest",
            `object` = null,
            userInfo = mapOf("prompt" to prompt)
        )

        // Poll for response (Simple workshop approach)
        var fullResponse = ""
        var isDone = false
        
        while (!isDone) {
            val response = userDefaults.stringForKey("gemma_response")
            if (response != null && response != fullResponse) {
                val newContent = response.substring(fullResponse.length)
                if (newContent.isNotEmpty()) {
                    fullResponse = response
                    emit(newContent)
                }
            }
            
            if (userDefaults.boolForKey("gemma_done")) {
                isDone = true
            }
            
            delay(100)
        }
        
        userDefaults.setBool(false, forKey = "gemma_done")
    }

    actual fun generateResponseWithHistory(
        systemPrompt: String,
        currentPrompt: String
    ): Flow<String> = generateResponse(systemPrompt + currentPrompt)

    actual fun isModelLoaded(): Boolean = modelLoaded

    actual fun getLoadingProgress(): Float = if (modelLoaded) 1f else 0f

    actual fun close() {
        modelLoaded = false
        NSNotificationCenter.defaultCenter.postNotificationName("GemmaCloseRequest", `object` = null)
    }
}
