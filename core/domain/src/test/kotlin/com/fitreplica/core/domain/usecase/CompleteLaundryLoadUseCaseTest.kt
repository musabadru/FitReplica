package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.fake.FakeLaundryRepository
import com.fitreplica.core.model.LaundryLoadId
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
}
