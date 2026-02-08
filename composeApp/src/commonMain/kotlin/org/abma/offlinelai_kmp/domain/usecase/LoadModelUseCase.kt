package org.abma.offlinelai_kmp.domain.usecase

import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.domain.repository.ModelRepository
import org.abma.offlinelai_kmp.inference.GemmaInference
import kotlin.time.Clock

class LoadModelUseCase(
    private val gemmaInference: GemmaInference,
    private val modelRepository: ModelRepository
) {
    suspend operator fun invoke(
        modelPath: String,
        config: ModelConfig = ModelConfig()
    ): Result<LoadedModel> = runCatching {
        gemmaInference.loadModel(modelPath, config)

        val modelName = modelPath.substringAfterLast("/").substringBeforeLast(".")
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val loadedModel = LoadedModel(
            name = modelName,
            path = modelPath,
            config = config,
            loadedAt = currentTime
        )

        modelRepository.saveModel(loadedModel)
        modelRepository.setCurrentModelPath(modelPath)

        loadedModel
    }
}
