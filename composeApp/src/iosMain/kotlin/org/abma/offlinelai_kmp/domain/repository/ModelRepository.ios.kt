package org.abma.offlinelai_kmp.domain.repository

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

actual class ModelRepository {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val json = Json { ignoreUnknownKeys = true }

    actual fun saveModel(model: LoadedModel) {
        val models = getLoadedModels().toMutableList()
        models.removeAll { it.path == model.path }
        models.add(model)
        saveModels(models)
    }

    actual fun getLoadedModels(): List<LoadedModel> {
        val serialized = userDefaults.stringForKey("loaded_models_json") ?: return emptyList()
        return try {
            json.decodeFromString<List<LoadedModel>>(serialized).sortedByDescending { it.loadedAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    actual fun getCurrentModelPath(): String? = userDefaults.stringForKey("current_model")

    actual fun setCurrentModelPath(path: String) {
        userDefaults.setObject(path, forKey = "current_model")
    }

    actual fun removeModel(path: String) {
        val models = getLoadedModels().filter { it.path != path }
        saveModels(models)
    }

    private fun saveModels(models: List<LoadedModel>) {
        val serialized = json.encodeToString(models)
        userDefaults.setObject(serialized, forKey = "loaded_models_json")
    }
}
