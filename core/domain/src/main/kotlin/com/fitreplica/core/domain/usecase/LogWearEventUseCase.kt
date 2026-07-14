package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.Condition
import com.fitreplica.core.model.OutfitId
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LogWearEventUseCase
    @Inject
    constructor(
        private val clothingRepository: ClothingRepository,
    ) {
        suspend operator fun invoke(
            itemId: ClothingId,
            outfitId: OutfitId? = null,
        context: String? = null,
    ) {
        if (clothingRepository.observeItem(itemId).first()?.condition == Condition.RETIRED) return
        clothingRepository.logWear(itemId, outfitId, context)
    }
}
