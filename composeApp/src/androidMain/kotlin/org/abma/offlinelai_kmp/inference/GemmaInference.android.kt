package org.abma.offlinelai_kmp.inference

import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.abma.offlinelai_kmp.domain.model.ModelConfig

actual class GemmaInference {
    private var llmInference: LlmInference? = null

    actual suspend fun loadModel(modelPath: String, config: ModelConfig) {
        val options = LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(config.maxTokens)
            .build()

        llmInference = LlmInference.createFromOptions(AndroidContextProvider.applicationContext, options)
    }

    actual fun generateResponse(prompt: String): Flow<String> = flow {
        val llm = llmInference ?: return@flow
        val response = llm.generateResponse(prompt)
        emit(response)
    }

    actual fun generateResponseWithHistory(
        systemPrompt: String,
        currentPrompt: String
    ): Flow<String> = generateResponse(systemPrompt + currentPrompt)

    actual fun isModelLoaded(): Boolean = llmInference != null

    actual fun getLoadingProgress(): Float = if (isModelLoaded()) 1f else 0f

    actual fun close() {
        llmInference?.close()
        llmInference = null
    }
}
