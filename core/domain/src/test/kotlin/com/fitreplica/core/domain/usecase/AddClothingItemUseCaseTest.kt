package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.fake.FakeClothingRepository
import com.fitreplica.core.domain.fake.sampleClothingItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AddClothingItemUseCaseTest {
    @Test
    fun `adds item to repository`() =
        runTest {
            val repository = FakeClothingRepository()
            val useCase = AddClothingItemUseCase(repository)
            val item = sampleClothingItem()

            useCase(item)

            assertEquals(listOf(item), repository.observeItems().first())
        }
}
