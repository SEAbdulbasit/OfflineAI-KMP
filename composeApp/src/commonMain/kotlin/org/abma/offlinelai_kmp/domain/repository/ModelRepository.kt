package org.abma.offlinelai_kmp.domain.repository

import org.abma.offlinelai_kmp.domain.model.ModelConfig

data class LoadedModel(
    val name: String,
    val path: String,
    val config: ModelConfig = ModelConfig(),
    val loadedAt: Long = 0L
)

expect class ModelRepository() {
    fun saveModel(model: LoadedModel)
    fun getLoadedModels(): List<LoadedModel>
    fun getCurrentModelPath(): String?
    fun setCurrentModelPath(path: String)
    fun removeModel(path: String)
    fun clearAll()
}
