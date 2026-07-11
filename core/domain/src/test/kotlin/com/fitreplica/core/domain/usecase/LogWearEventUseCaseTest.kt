package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.fake.FakeClothingRepository
import com.fitreplica.core.domain.fake.sampleClothingItem
import com.fitreplica.core.model.OutfitId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LogWearEventUseCaseTest {
    @Test
    fun `logs wear with optional outfit and context`() =
        runTest {
            val repository = FakeClothingRepository()
            val item = sampleClothingItem()
            repository.addItem(item)
            val useCase = LogWearEventUseCase(repository)

            useCase(item.id, OutfitId("outfit-1"), "work")

            assertEquals(1, repository.wearLog.size)
            assertEquals(Triple(item.id, OutfitId("outfit-1"), "work"), repository.wearLog.single())
        }

    @Test
    fun `defaults outfit and context to null`() =
        runTest {
            val repository = FakeClothingRepository()
            val item = sampleClothingItem()
            repository.addItem(item)
            val useCase = LogWearEventUseCase(repository)

            useCase(item.id)

            assertEquals(Triple(item.id, null, null), repository.wearLog.single())
        }
}
