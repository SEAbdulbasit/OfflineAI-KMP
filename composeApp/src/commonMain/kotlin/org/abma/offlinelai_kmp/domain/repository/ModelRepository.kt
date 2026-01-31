package org.abma.offlinelai_kmp.domain.repository

import org.abma.offlinelai_kmp.domain.model.ModelConfig

/**
 * Data class representing a successfully loaded model.
 */
data class LoadedModel(
    val name: String,
    val path: String,
    val config: ModelConfig = ModelConfig(),
    val loadedAt: Long = 0L // Timestamp when first loaded
)

/**
 * Repository to store and retrieve successfully loaded models.
 */
expect class ModelRepository() {
    /**
     * Save a model that was successfully loaded.
     */
    fun saveModel(model: LoadedModel)

    /**
     * Get all successfully loaded models.
     */
    fun getLoadedModels(): List<LoadedModel>

    /**
     * Get the currently selected model path.
     */
    fun getCurrentModelPath(): String?

    /**
     * Set the current model path.
     */
    fun setCurrentModelPath(path: String)

    /**
     * Remove a model from the saved list.
     */
    fun removeModel(path: String)

    /**
     * Clear all saved models.
     */
    fun clearAll()
}
