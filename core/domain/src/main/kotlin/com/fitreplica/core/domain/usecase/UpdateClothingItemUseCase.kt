package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.model.ClothingItem
import javax.inject.Inject

class UpdateClothingItemUseCase
    @Inject
    constructor(
        private val clothingRepository: ClothingRepository,
    ) {
        suspend operator fun invoke(item: ClothingItem) {
            clothingRepository.updateItem(item)
        }
    }
