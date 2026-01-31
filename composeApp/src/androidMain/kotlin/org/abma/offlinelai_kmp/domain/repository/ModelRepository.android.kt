package org.abma.offlinelai_kmp.domain.repository

import android.content.Context
import android.content.SharedPreferences
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.inference.AndroidContextProvider

actual class ModelRepository {
    private val prefs: SharedPreferences by lazy {
        AndroidContextProvider.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }


    actual fun saveModel(model: LoadedModel) {
        val models = getLoadedModels().toMutableList()
        // Remove if already exists (to update)
        models.removeAll { it.path == model.path }
        models.add(model)

        val serialized = models.map { modelToString(it) }
        prefs.edit()
            .putStringSet(KEY_LOADED_MODELS, serialized.toSet())
            .apply()
    }

    actual fun getLoadedModels(): List<LoadedModel> {
        val serialized = prefs.getStringSet(KEY_LOADED_MODELS, emptySet()) ?: emptySet()
        return serialized.mapNotNull { stringToModel(it) }
            .sortedByDescending { it.loadedAt }
    }

    actual fun getCurrentModelPath(): String? {
        return prefs.getString(KEY_CURRENT_MODEL, null)
    }

    actual fun setCurrentModelPath(path: String) {
        prefs.edit()
            .putString(KEY_CURRENT_MODEL, path)
            .apply()
    }

    actual fun removeModel(path: String) {
        val models = getLoadedModels().filter { it.path != path }
        val serialized = models.map { modelToString(it) }
        prefs.edit()
            .putStringSet(KEY_LOADED_MODELS, serialized.toSet())
            .apply()
    }

    actual fun clearAll() {
        prefs.edit().clear().apply()
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
        private const val PREFS_NAME = "offlinelai_models"
        private const val KEY_LOADED_MODELS = "loaded_models"
        private const val KEY_CURRENT_MODEL = "current_model"
    }
}
