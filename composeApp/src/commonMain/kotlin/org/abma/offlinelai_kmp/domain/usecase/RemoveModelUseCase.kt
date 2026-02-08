package org.abma.offlinelai_kmp.domain.usecase

import org.abma.offlinelai_kmp.domain.repository.ModelRepository

class RemoveModelUseCase(
    private val modelRepository: ModelRepository
) {
    operator fun invoke(path: String): Result<Unit> = runCatching {
        modelRepository.removeModel(path)
    }
}
