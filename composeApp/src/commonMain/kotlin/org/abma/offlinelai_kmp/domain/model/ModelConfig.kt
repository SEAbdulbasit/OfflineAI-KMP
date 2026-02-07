package org.abma.offlinelai_kmp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ModelConfig(
    val maxTokens: Int = 2048,
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

