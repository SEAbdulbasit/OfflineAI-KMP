package org.abma.offlinelai_kmp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ModelConfig(
    val maxTokens: Int = 1024,
    val temperature: Float = 0.8f,
    val topK: Int = 40
)

enum class ModelState {
    NOT_LOADED,
    LOADING,
    READY,
    ERROR,
    GENERATING
}

data class ModelInfo(
    val name: String,
    val description: String,
    val sizeInMB: Long,
    val downloadUrl: String,
    val fileName: String
) {
    companion object {
        val GEMMA_2B_IT_GPU = ModelInfo(
            name = "Gemma 2B (GPU)",
            description = "GPU-optimized, requires OpenCL",
            sizeInMB = 1500,
            downloadUrl = "https://huggingface.co/google/gemma-2b-it-gpu-int4",
            fileName = "gemma-2b-it-gpu-int4.bin"
        )

        val GEMMA_2B_IT_CPU = ModelInfo(
            name = "Gemma 2B (CPU)",
            description = "CPU-compatible, works on all devices",
            sizeInMB = 1500,
            downloadUrl = "https://kaggle.com/models/google/gemma/tfLite",
            fileName = "gemma-2b-it-cpu-int4.task"
        )

        val availableModels = listOf(GEMMA_2B_IT_GPU, GEMMA_2B_IT_CPU)
    }
}
