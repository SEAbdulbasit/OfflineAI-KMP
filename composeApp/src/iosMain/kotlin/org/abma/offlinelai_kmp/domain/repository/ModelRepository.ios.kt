package org.abma.offlinelai_kmp.domain.repository

import org.abma.offlinelai_kmp.domain.model.ModelConfig
import platform.Foundation.NSUserDefaults

actual class ModelRepository {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual fun saveModel(model: LoadedModel) {
        val models = getLoadedModels().toMutableList()
        models.removeAll { it.path == model.path }
        models.add(model)

        val serialized = models.map { modelToString(it) }
        userDefaults.setObject(serialized, forKey = KEY_LOADED_MODELS)
        userDefaults.synchronize()
    }

    actual fun getLoadedModels(): List<LoadedModel> {
        @Suppress("UNCHECKED_CAST")
        val serialized = userDefaults.arrayForKey(KEY_LOADED_MODELS) as? List<String> ?: emptyList()
        return serialized.mapNotNull { stringToModel(it) }
            .sortedByDescending { it.loadedAt }
    }

    actual fun getCurrentModelPath(): String? {
        return userDefaults.stringForKey(KEY_CURRENT_MODEL)
    }

    actual fun setCurrentModelPath(path: String) {
        userDefaults.setObject(path, forKey = KEY_CURRENT_MODEL)
        userDefaults.synchronize()
    }

    actual fun removeModel(path: String) {
        val models = getLoadedModels().filter { it.path != path }
        val serialized = models.map { modelToString(it) }
        userDefaults.setObject(serialized, forKey = KEY_LOADED_MODELS)
        userDefaults.synchronize()
    }

    actual fun clearAll() {
        userDefaults.removeObjectForKey(KEY_LOADED_MODELS)
        userDefaults.removeObjectForKey(KEY_CURRENT_MODEL)
        userDefaults.synchronize()
    }

    private fun modelToString(model: LoadedModel): String {
        return "${model.name}|${model.path}|${model.config.maxTokens}|${model.config.temperature}|${model.config.topK}|${model.loadedAt}"
    }

    private fun stringToModel(str: String): LoadedModel? {
        return try {
            val parts = str.split("|")
            if (parts.size >= 6) {
                LoadedModel(
                    name = parts[0],
                    path = parts[1],
                    config = ModelConfig(
                        maxTokens = parts[2].toInt(),
                        temperature = parts[3].toFloat(),
                        topK = parts[4].toInt()
                    ),
                    loadedAt = parts[5].toLong()
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val KEY_LOADED_MODELS = "offlinelai_loaded_models"
        private const val KEY_CURRENT_MODEL = "offlinelai_current_model"
    }
}
