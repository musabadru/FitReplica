package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.fake.FakeLaundryRepository
import com.fitreplica.core.model.ClothingId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CreateLaundryLoadUseCaseTest {
    @Test
    fun `rejects empty loads`() {
        val useCase = CreateLaundryLoadUseCase(FakeLaundryRepository())

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                useCase(emptyList())
            }
        }
    }

    @Test
    fun `deduplicates item ids before creating load`() =
        runTest {
            val repository = FakeLaundryRepository()
            val useCase = CreateLaundryLoadUseCase(repository)

            useCase(listOf(ClothingId("item-1"), ClothingId("item-1"), ClothingId("item-2")))

            assertEquals(listOf(listOf(ClothingId("item-1"), ClothingId("item-2"))), repository.createdLoads)
        }

    @Test
    fun `fake repository publishes created loads`() =
        runTest {
            val repository = FakeLaundryRepository()
            val useCase = CreateLaundryLoadUseCase(repository)
            val itemIds = listOf(ClothingId("item-1"))

            useCase(itemIds)

            assertEquals(itemIds, repository.observeLoads().first().single().itemIds)
        }
}
