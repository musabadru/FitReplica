package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.Condition
import javax.inject.Inject

class UpdateConditionUseCase
    @Inject
    constructor(
        private val clothingRepository: ClothingRepository,
    ) {
        suspend operator fun invoke(
            itemId: ClothingId,
            condition: Condition,
            notes: String? = null,
        ) {
            clothingRepository.updateCondition(itemId, condition, notes)
        }
    }
