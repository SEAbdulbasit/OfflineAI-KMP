package org.abma.offlinelai_kmp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ModelConfig(
    val modelPath: String = "",
    val maxTokens: Int = 1024,
    val temperature: Float = 0.8f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val randomSeed: Int = 0
)

@Serializable
enum class ModelState {
    NOT_LOADED,
    LOADING,
    READY,
    ERROR,
    GENERATING
}

@Serializable
data class ModelInfo(
    val name: String,
    val description: String,
    val sizeInMB: Long,
    val downloadUrl: String,
    val fileName: String
) {
    companion object {
        val GEMMA_2B_IT_GPU = ModelInfo(
            name = "Gemma 2B Instruct (GPU)",
            description = "GPU-optimized model - requires OpenCL support",
            sizeInMB = 1500,
            downloadUrl = "https://huggingface.co/google/gemma-2b-it-gpu-int4/resolve/main/gemma-2b-it-gpu-int4.bin",
            fileName = "gemma-2b-it-gpu-int4.bin"
        )

        val GEMMA_2B_IT_CPU = ModelInfo(
            name = "Gemma 2B Instruct (CPU)",
            description = "CPU-compatible model - works on all devices",
            sizeInMB = 1500,
            downloadUrl = "https://www.kaggle.com/models/google/gemma/tfLite/gemma-2b-it-cpu-int4",
            fileName = "gemma-3n-E2B-it-int4.task"
        )

        val availableModels = listOf(GEMMA_2B_IT_CPU, GEMMA_2B_IT_GPU)
    }
}
