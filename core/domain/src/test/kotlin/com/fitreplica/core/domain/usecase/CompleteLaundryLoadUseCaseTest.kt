package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.fake.FakeLaundryRepository
import com.fitreplica.core.model.ClothingId
import com.fitreplica.core.model.LaundryLoadId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CompleteLaundryLoadUseCaseTest {
    @Test
    fun `delegates completed load id`() =
        runTest {
            val repository = FakeLaundryRepository()
            val useCase = CompleteLaundryLoadUseCase(repository)

            useCase(LaundryLoadId("load-1"))

            assertEquals(listOf(LaundryLoadId("load-1")), repository.completedLoads)
        }

    @Test
    fun `fake repository publishes completed loads`() =
        runTest {
            val repository = FakeLaundryRepository()
            repository.createLoad(listOf(ClothingId("item-1")))
            val loadId = repository.observeLoads().first().single().id
            val useCase = CompleteLaundryLoadUseCase(repository)

            useCase(loadId)

            assertEquals(1L, repository.observeLoads().first().single().completedAt)
        }
}
