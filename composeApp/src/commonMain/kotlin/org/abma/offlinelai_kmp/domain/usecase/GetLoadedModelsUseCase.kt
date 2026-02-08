package org.abma.offlinelai_kmp.domain.usecase

import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.domain.repository.ModelRepository

class GetLoadedModelsUseCase(
    private val modelRepository: ModelRepository
) {
    operator fun invoke(): Result<List<LoadedModel>> = runCatching {
        modelRepository.getLoadedModels()
    }
}
