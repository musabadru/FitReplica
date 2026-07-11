package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.fake.FakeClothingRepository
import com.fitreplica.core.domain.fake.FakeImageRepository
import com.fitreplica.core.domain.fake.sampleClothingItem
import com.fitreplica.core.model.Image
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteClothingItemUseCaseTest {
    @Test
    fun `removes the item and deletes its images`() =
        runTest {
            val clothingRepository = FakeClothingRepository()
            val imageRepository = FakeImageRepository()
            val item = sampleClothingItem()
            clothingRepository.addItem(item)
            imageRepository.seed(
                Image(
                    id = "img-1",
                    itemId = item.id,
                    uri = "img-1",
                    thumbnailUri = "img-1",
                    isPrimary = true,
                    takenAt = 0L,
                ),
                Image(
                    id = "img-2",
                    itemId = item.id,
                    uri = "img-2",
                    thumbnailUri = "img-2",
                    isPrimary = false,
                    takenAt = 0L,
                ),
            )
            val useCase = DeleteClothingItemUseCase(clothingRepository, imageRepository)

            useCase(item.id)

            assertNull(clothingRepository.observeItem(item.id).first())
            assertTrue(imageRepository.observeImages(item.id).first().isEmpty())
            assertEquals(listOf("img-1", "img-2"), imageRepository.deletedImageIds)
        }
}
