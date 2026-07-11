package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.domain.repository.ImageRepository
import com.fitreplica.core.model.ClothingId
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteClothingItemUseCase
    @Inject
    constructor(
        private val clothingRepository: ClothingRepository,
        private val imageRepository: ImageRepository,
    ) {
        // Deleting the item row cascades wear_events/images/outfit cross-refs at the DB
        // level, but nothing cleans up the physical image files on disk — do that first,
        // while the image rows (and their file paths) still exist to look up.
        suspend operator fun invoke(itemId: ClothingId) {
            val images = imageRepository.observeImages(itemId).first()
            images.forEach { image -> imageRepository.deleteImage(image.id) }
            clothingRepository.deleteItem(itemId)
        }
    }
