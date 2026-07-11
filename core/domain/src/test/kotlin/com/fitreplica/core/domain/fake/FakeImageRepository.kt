package com.fitreplica.core.domain.fake

import com.fitreplica.core.common.Result
import com.fitreplica.core.domain.repository.ImageRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.Image
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeImageRepository : ImageRepository {
    private val images = MutableStateFlow<List<Image>>(emptyList())
    val deletedImageIds = mutableListOf<String>()

    fun seed(vararg seeded: Image) {
        images.value = images.value + seeded
    }

    override fun observeImages(itemId: ClothingId) = images.map { list -> list.filter { it.itemId == itemId } }

    override suspend fun getImages(itemId: ClothingId) = images.value.filter { it.itemId == itemId }

    override suspend fun addImage(
        itemId: ClothingId,
        sourceUri: String,
        isPrimary: Boolean,
    ): Result<Image> {
        val image =
            Image(
                id = sourceUri,
                itemId = itemId,
                uri = sourceUri,
                thumbnailUri = sourceUri,
                isPrimary = isPrimary,
                takenAt = 0L,
            )
        images.value = images.value + image
        return Result.Success(image)
    }

    override suspend fun setPrimaryImage(
        itemId: ClothingId,
        imageId: String,
    ) {
        images.value =
            images.value.map {
                if (it.itemId != itemId) it else it.copy(isPrimary = it.id == imageId)
            }
    }

    override suspend fun deleteImage(imageId: String) {
        deletedImageIds += imageId
        val deleted = images.value.find { it.id == imageId }
        images.value = images.value.filterNot { it.id == imageId }

        if (deleted?.isPrimary == true) {
            val next = images.value.filter { it.itemId == deleted.itemId }.minByOrNull { it.takenAt }
            if (next != null) {
                images.value = images.value.map { if (it.id == next.id) it.copy(isPrimary = true) else it }
            }
        }
    }
}
