package com.fitreplica.core.domain.usecase

import com.fitreplica.core.domain.fake.FakeClothingRepository
import com.fitreplica.core.domain.fake.sampleClothingItem
import com.fitreplica.core.model.Condition
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateConditionUseCaseTest {
    @Test
    fun `updates condition on the matching item`() =
        runTest {
            val repository = FakeClothingRepository()
            val item = sampleClothingItem(condition = Condition.NEW)
            repository.addItem(item)
            val useCase = UpdateConditionUseCase(repository)

            useCase(item.id, Condition.NEEDS_REPAIR)

            val updated = repository.observeItem(item.id).first()
            assertEquals(Condition.NEEDS_REPAIR, updated?.condition)
        }
}
