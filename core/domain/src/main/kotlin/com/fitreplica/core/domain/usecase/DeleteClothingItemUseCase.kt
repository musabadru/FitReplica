package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.repository.ClothingRepository
import com.fitreplica.core.domain.repository.ImageRepository
import com.fitreplica.core.model.ClothingId
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class DeleteClothingItemUseCase
    @Inject
    constructor(
        private val clothingRepository: ClothingRepository,
        private val imageRepository: ImageRepository,
    ) {
        // Deleting the item row cascades wear_events/images/outfit cross-refs at the DB
        // level, but nothing cleans up the physical image files on disk — do that first,
        // while the image rows (and their file paths) still exist to look up. A one-shot
        // snapshot (not a Flow) avoids racing a concurrent image addition for this item.
        // Each file cleanup is best-effort: if one fails, the rest still run and the item
        // itself is always deleted — a partially-cleaned-up image is preferable to leaving
        // the item stuck undeleted.
        suspend operator fun invoke(itemId: ClothingId) {
            val images = imageRepository.getImages(itemId)
            images.forEach { image ->
                try {
                    imageRepository.deleteImage(image.id)
                } catch (e: CancellationException) {
                    throw e
                } catch (ignored: Exception) {
                    // Best-effort: the DB cascade from deleteItem below removes the image
                    // row regardless, so a failure here only risks an orphaned file, not
                    // an inconsistent item/image relationship.
                }
            }
            clothingRepository.deleteItem(itemId)
        }
    }
