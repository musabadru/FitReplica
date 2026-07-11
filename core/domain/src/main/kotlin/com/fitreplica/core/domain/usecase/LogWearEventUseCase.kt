package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.model.ClothingId
import javax.inject.Inject

class LogWearEventUseCase
    @Inject
    constructor(
        private val clothingRepository: ClothingRepository,
    ) {
        suspend operator fun invoke(
            itemId: ClothingId,
            context: String? = null,
        ) {
            clothingRepository.logWear(itemId, context)
        }
    }
