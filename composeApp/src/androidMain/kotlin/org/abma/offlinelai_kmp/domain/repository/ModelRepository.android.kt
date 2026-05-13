package org.abma.offlinelai_kmp.domain.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.abma.offlinelai_kmp.inference.AndroidContextProvider

actual class ModelRepository {
    private val prefs: SharedPreferences by lazy {
        AndroidContextProvider.applicationContext.getSharedPreferences("offlinelai_models", Context.MODE_PRIVATE)
    }

    private val json = Json { ignoreUnknownKeys = true }

    actual fun saveModel(model: LoadedModel) {
        val models = getLoadedModels().toMutableList()
        models.removeAll { it.path == model.path }
        models.add(model)
        saveModels(models)
    }

    actual fun getLoadedModels(): List<LoadedModel> {
        val serialized = prefs.getString("loaded_models_json", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<LoadedModel>>(serialized).sortedByDescending { it.loadedAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    actual fun getCurrentModelPath(): String? = prefs.getString("current_model", null)

    actual fun setCurrentModelPath(path: String) {
        prefs.edit().putString("current_model", path).apply()
    }

    actual fun removeModel(path: String) {
        val models = getLoadedModels().filter { it.path != path }
        saveModels(models)
    }

    private fun saveModels(models: List<LoadedModel>) {
        val serialized = json.encodeToString(models)
        prefs.edit().putString("loaded_models_json", serialized).apply()
    }
}
