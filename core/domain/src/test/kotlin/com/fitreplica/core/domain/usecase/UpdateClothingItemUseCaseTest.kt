package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.fake.FakeClothingRepository
import com.fitreplica.core.domain.fake.sampleClothingItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateClothingItemUseCaseTest {
    @Test
    fun `replaces the matching item`() =
        runTest {
            val repository = FakeClothingRepository()
            val item = sampleClothingItem(name = "Original name")
            repository.addItem(item)
            val useCase = UpdateClothingItemUseCase(repository)

            useCase(item.copy(name = "Updated name"))

            val updated = repository.observeItem(item.id).first()
            assertEquals("Updated name", updated?.name)
        }
}
